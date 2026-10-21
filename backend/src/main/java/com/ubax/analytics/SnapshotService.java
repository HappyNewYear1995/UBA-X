package com.ubax.analytics;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.options.ScreenshotType;
import com.microsoft.playwright.options.WaitUntilState;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Captures a full-page PNG screenshot of a real page with headless Chromium so the
 * click heatmap can be overlaid on actual page pixels instead of a simulated layout.
 *
 * <p>Design constraints (the production box is memory-limited):
 * <ul>
 *   <li>Captures are strictly single-flight (a global lock) and serialized.</li>
 *   <li>Results are cached on disk by hash(url + viewport) with a TTL, so a page is
 *       shot at most once per TTL window.</li>
 *   <li>The browser is evicted after a short idle window so a headless Chromium is
 *       never resident in RAM between captures — captures are rare (disk-cached) and
 *       the box is memory-limited, so steady-state footprint stays at zero.</li>
 *   <li>If the browser is not installed the service latches "unavailable" and every
 *       call fast-fails, letting the frontend fall back to the abstract background.</li>
 *   <li>The target URL is always derived server-side from ingested event data, and is
 *       passed through an SSRF guard (loopback / link-local / private / metadata IPs
 *       are rejected) because {@code /api/collect} is an open endpoint.</li>
 * </ul>
 */
@Service
public class SnapshotService {

    public record Result(boolean available, byte[] png, String reason) {
        static Result none(String reason) { return new Result(false, null, reason); }
    }

    private static final String UA = "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 " +
            "(KHTML, like Gecko) Chrome/124.0.0.0 Safari/537.36 UbaxSnapshot/1.0";

    private final Path cacheDir;
    private final long ttlMillis;
    private final int viewportWidth;
    private final boolean enabled;
    private final long idleMillis;

    private final ReentrantLock lock = new ReentrantLock();
    private volatile Playwright playwright;
    private volatile Browser browser;
    private volatile boolean initFailed = false;
    private volatile long lastUseMs = 0L;
    private ScheduledExecutorService evictor;

    public SnapshotService(@Value("${ubax.snapshot.dir:snapshots}") String dir,
                           @Value("${ubax.snapshot.ttl-days:7}") long ttlDays,
                           @Value("${ubax.snapshot.width:1440}") int width,
                           @Value("${ubax.snapshot.idle-seconds:90}") long idleSeconds,
                           @Value("${ubax.snapshot.enabled:true}") boolean enabled) {
        this.cacheDir = Paths.get(dir);
        this.ttlMillis = Duration.ofDays(Math.max(1, ttlDays)).toMillis();
        this.viewportWidth = width > 0 ? width : 1440;
        this.idleMillis = Duration.ofSeconds(Math.max(15, idleSeconds)).toMillis();
        this.enabled = enabled;
    }

    /** Capture (or reuse a cached) full-page screenshot of {@code url}. */
    public Result capture(String url) {
        if (!enabled) return Result.none("disabled");
        if (url == null || url.isBlank()) return Result.none("no-url");
        String normalized = url.trim();
        if (!isHttp(normalized)) return Result.none("bad-scheme");
        if (!isSafePublicUrl(normalized)) return Result.none("blocked-host");

        Path file = cacheFile(normalized);
        Result cached = readFresh(file);
        if (cached != null) return cached;
        if (initFailed) return Result.none("browser-unavailable");

        lock.lock();
        try {
            // single-flight: re-check cache once we hold the lock
            cached = readFresh(file);
            if (cached != null) return cached;
            if (!ensureBrowser()) return Result.none("browser-unavailable");
            byte[] png = shoot(normalized);
            if (png == null || png.length == 0) return Result.none("capture-failed");
            Files.createDirectories(cacheDir);
            Path tmp = Files.createTempFile(cacheDir, "snap", ".tmp");
            Files.write(tmp, png);
            Files.move(tmp, file, StandardCopyOption.REPLACE_EXISTING);
            return new Result(true, png, null);
        } catch (Exception e) {
            return Result.none("error:" + e.getClass().getSimpleName());
        } finally {
            lastUseMs = System.currentTimeMillis();
            lock.unlock();
        }
    }

    private byte[] shoot(String url) {
        BrowserContext ctx = null;
        try {
            ctx = browser.newContext(new Browser.NewContextOptions()
                    .setViewportSize(viewportWidth, 900)
                    .setDeviceScaleFactor(1)
                    .setUserAgent(UA));
            Page page = ctx.newPage();
            page.navigate(url, new Page.NavigateOptions()
                    .setTimeout(20000)
                    .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
            page.waitForTimeout(900); // let late paint / webfonts settle
            return page.screenshot(new Page.ScreenshotOptions()
                    .setFullPage(true)
                    .setType(ScreenshotType.PNG));
        } catch (Exception e) {
            return null;
        } finally {
            if (ctx != null) {
                try { ctx.close(); } catch (Exception ignore) { }
            }
        }
    }

    /** Lazily launch Chromium; latch initFailed when the browser is absent so we fast-fail later. */
    private boolean ensureBrowser() {
        if (initFailed) return false;
        Browser b = browser;
        if (b != null && b.isConnected()) { lastUseMs = System.currentTimeMillis(); return true; }
        try {
            if (playwright == null) playwright = Playwright.create();
            browser = playwright.chromium().launch(new BrowserType.LaunchOptions()
                    .setHeadless(true)
                    .setArgs(List.of("--no-sandbox", "--disable-dev-shm-usage", "--disable-gpu",
                            "--hide-scrollbars", "--force-device-scale-factor=1", "--mute-audio")));
            lastUseMs = System.currentTimeMillis();
            startEvictor();
            return browser != null;
        } catch (Throwable t) {
            browser = null;
            initFailed = true;
            return false;
        }
    }

    /**
     * Start a single daemon that releases the browser after {@code idleMillis} of no
     * captures, so a headless Chromium is never resident between (rare, cached) shots.
     */
    private void startEvictor() {
        if (evictor != null) return;
        evictor = Executors.newSingleThreadScheduledExecutor((r) -> {
            Thread t = new Thread(r, "snapshot-evictor");
            t.setDaemon(true);
            return t;
        });
        long period = Math.max(15_000L, idleMillis / 3);
        evictor.scheduleWithFixedDelay(this::evictIfIdle, period, period, TimeUnit.MILLISECONDS);
    }

    private void evictIfIdle() {
        if (browser == null) return;
        if (System.currentTimeMillis() - lastUseMs < idleMillis) return;
        lock.lock();
        try {
            if (browser != null && System.currentTimeMillis() - lastUseMs >= idleMillis) {
                releaseBrowser();
            }
        } finally {
            lock.unlock();
        }
    }

    /** Close browser + driver. Caller must hold {@link #lock}. */
    private void releaseBrowser() {
        try { if (browser != null) browser.close(); } catch (Exception ignore) { }
        try { if (playwright != null) playwright.close(); } catch (Exception ignore) { }
        browser = null;
        playwright = null;
    }

    @PreDestroy
    public void close() {
        try { if (evictor != null) evictor.shutdownNow(); } catch (Exception ignore) { }
        lock.lock();
        try {
            releaseBrowser();
        } finally {
            lock.unlock();
        }
    }

    // ---- helpers ----

    private Path cacheFile(String url) {
        return cacheDir.resolve(sha256(url + "|" + viewportWidth) + ".png");
    }

    private Result readFresh(Path f) {
        try {
            if (Files.exists(f) && (System.currentTimeMillis() - Files.getLastModifiedTime(f).toMillis()) < ttlMillis) {
                return new Result(true, Files.readAllBytes(f), null);
            }
        } catch (IOException ignore) { }
        return null;
    }

    private static boolean isHttp(String u) {
        String s = u.toLowerCase();
        return s.startsWith("http://") || s.startsWith("https://");
    }

    /**
     * SSRF guard: resolve the host and reject loopback / any-local / link-local
     * (incl. 169.254.169.254 cloud metadata) / site-local (10, 172.16, 192.168)
     * and IPv6 unique-local (fc00::/7) addresses.
     */
    private static boolean isSafePublicUrl(String url) {
        try {
            String host = URI.create(url).getHost();
            if (host == null || host.isBlank()) return false;
            InetAddress[] addrs = InetAddress.getAllByName(host);
            for (InetAddress a : addrs) {
                if (a.isLoopbackAddress() || a.isAnyLocalAddress() || a.isLinkLocalAddress()
                        || a.isSiteLocalAddress() || isUniqueLocalIpv6(a)) {
                    return false;
                }
            }
            return addrs.length > 0;
        } catch (UnknownHostException e) {
            return false; // unresolvable host -> nothing to capture
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isUniqueLocalIpv6(InetAddress a) {
        byte[] b = a.getAddress();
        return b.length == 16 && (b[0] & 0xfe) == 0xfc; // fc00::/7
    }

    private static String sha256(String s) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(md.digest(s.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            return Integer.toHexString(s.hashCode());
        }
    }
}
