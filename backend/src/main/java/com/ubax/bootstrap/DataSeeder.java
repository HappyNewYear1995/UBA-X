package com.ubax.bootstrap;

import com.ubax.app.domain.App;
import com.ubax.app.repo.AppRepository;
import com.ubax.event.domain.Event;
import com.ubax.event.repo.EventRepository;
import com.ubax.pilot.domain.Agent;
import com.ubax.pilot.repo.AgentRepository;
import com.ubax.profile.domain.UserProfile;
import com.ubax.profile.repo.UserProfileRepository;
import com.ubax.stream.domain.RealtimeMinute;
import com.ubax.stream.repo.RealtimeMinuteRepository;
import com.ubax.system.domain.AuditLog;
import com.ubax.system.domain.Dict;
import com.ubax.system.domain.Notice;
import com.ubax.system.domain.ScheduledTask;
import com.ubax.system.domain.SystemConfig;
import com.ubax.system.repo.AuditLogRepository;
import com.ubax.system.repo.DictRepository;
import com.ubax.system.repo.NoticeRepository;
import com.ubax.system.repo.ScheduledTaskRepository;
import com.ubax.system.repo.SystemConfigRepository;
import com.ubax.tenant.domain.Role;
import com.ubax.tenant.domain.Tenant;
import com.ubax.tenant.domain.User;
import com.ubax.tenant.repo.RoleRepository;
import com.ubax.tenant.repo.TenantRepository;
import com.ubax.tenant.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AppRepository appRepository;
    private final EventRepository eventRepository;
    private final UserProfileRepository profileRepository;
    private final RealtimeMinuteRepository realtimeRepository;
    private final AgentRepository agentRepository;
    private final PasswordEncoder passwordEncoder;
    private final SystemConfigRepository configRepository;
    private final DictRepository dictRepository;
    private final NoticeRepository noticeRepository;
    private final ScheduledTaskRepository taskRepository;
    private final AuditLogRepository auditLogRepository;

    public DataSeeder(TenantRepository tenantRepository, UserRepository userRepository, RoleRepository roleRepository,
                      AppRepository appRepository, EventRepository eventRepository,
                      UserProfileRepository profileRepository, RealtimeMinuteRepository realtimeRepository,
                      AgentRepository agentRepository, PasswordEncoder passwordEncoder,
                      SystemConfigRepository configRepository, DictRepository dictRepository,
                      NoticeRepository noticeRepository, ScheduledTaskRepository taskRepository,
                      AuditLogRepository auditLogRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.appRepository = appRepository;
        this.eventRepository = eventRepository;
        this.profileRepository = profileRepository;
        this.realtimeRepository = realtimeRepository;
        this.agentRepository = agentRepository;
        this.passwordEncoder = passwordEncoder;
        this.configRepository = configRepository;
        this.dictRepository = dictRepository;
        this.noticeRepository = noticeRepository;
        this.taskRepository = taskRepository;
        this.auditLogRepository = auditLogRepository;
    }

    private static final String[] PATHS = {"/", "/products", "/product/1001", "/product/2050", "/cart", "/checkout", "/blog", "/about"};
    private static final String[] TITLES = {"首页", "全部商品", "商品详情", "商品详情", "购物车", "结算", "博客", "关于我们"};
    private static final String[] DEVICES = {"Desktop", "Mobile", "Mobile", "Tablet", "Desktop", "MiniProgram"};
    private static final String[] BROWSERS = {"Chrome", "Safari", "Edge", "Firefox", "WeChat", "Chrome", "Safari"};
    private static final String[] OS = {"Windows", "iOS/macOS", "Android", "HarmonyOS", "Linux"};
    private static final String[][] GEO = {
            {"广东省", "深圳市"}, {"广东省", "广州市"}, {"北京市", "北京市"}, {"上海市", "上海市"},
            {"浙江省", "杭州市"}, {"江苏省", "南京市"}, {"四川省", "成都市"}, {"湖北省", "武汉市"},
            {"山东省", "青岛市"}, {"陕西省", "西安市"}
    };
    private static final String[] REFERRERS = {"https://www.baidu.com", "https://weixin.qq.com", "direct", "https://google.com", "https://douyin.com"};
    private static final String[] SCREENS = {"1920x1080", "1440x900", "390x844", "414x896", "1366x768"};

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) seedRoles();
        if (configRepository.count() == 0) seedSystemGlobal();
        if (tenantRepository.count() > 0) {
            log.info("Data already present, skipping seed.");
            return;
        }
        log.info("Seeding demo data...");

        Tenant tenant = new Tenant();
        tenant.setName("数智科技(演示租户)");
        tenant.setCode("demo");
        tenant.setStatus("ACTIVE");
        tenant.setPlan("ENTERPRISE");
        tenant.setContact("ops@demo.com");
        tenant = tenantRepository.save(tenant);

        // super admin (platform level, tenantId null)
        User superAdmin = new User();
        superAdmin.setTenantId(null);
        superAdmin.setUsername("admin");
        superAdmin.setPassword(passwordEncoder.encode("admin123"));
        superAdmin.setNickname("平台管理员");
        superAdmin.setRole("SUPER_ADMIN");
        userRepository.save(superAdmin);

        // tenant admin
        User demoAdmin = new User();
        demoAdmin.setTenantId(tenant.getId());
        demoAdmin.setUsername("demo");
        demoAdmin.setPassword(passwordEncoder.encode("demo123"));
        demoAdmin.setNickname("租户管理员");
        demoAdmin.setRole("ADMIN");
        userRepository.save(demoAdmin);

        User analyst = new User();
        analyst.setTenantId(tenant.getId());
        analyst.setUsername("analyst");
        analyst.setPassword(passwordEncoder.encode("analyst123"));
        analyst.setNickname("数据分析师");
        analyst.setRole("ANALYST");
        userRepository.save(analyst);

        App app = new App();
        app.setTenantId(tenant.getId());
        app.setName("官网商城(Web)");
        app.setPlatform("WEB");
        app.setAppKey("ubax_demo_web_0001");
        app.setAutoTrack(true);
        app.setDescription("演示用 Web 端应用");
        app = appRepository.save(app);

        App app2 = new App();
        app2.setTenantId(tenant.getId());
        app2.setName("小程序端");
        app2.setPlatform("MINIPROGRAM");
        app2.setAppKey("ubax_demo_mini_0002");
        app2.setAutoTrack(true);
        app2.setDescription("演示用小程序端应用");
        app2 = appRepository.save(app2);

        // pilot agents
        seedAgents(tenant.getId(), app.getAppKey());

        // events over last 14 days
        Random rnd = new Random(20260115L);
        List<Event> buffer = new ArrayList<>();
        Map<String, UserProfile> profileMap = new LinkedHashMap<>();
        Set<String> allSessions = new HashSet<>();
        LocalDate today = LocalDate.now();
        Long appId = app.getId();
        Long tid = tenant.getId();

        for (int dayOffset = 13; dayOffset >= 0; dayOffset--) {
            LocalDate day = today.minusDays(dayOffset);
            int sessions = 18 + rnd.nextInt(30) + (13 - dayOffset) * 2; // growing trend
            for (int s = 0; s < sessions; s++) {
                String distinctId = String.format("u_%04d", rnd.nextInt(320));
                String sessionId = UUID.randomUUID().toString().substring(0, 16);
                allSessions.add(sessionId);
                int devIdx = rnd.nextInt(DEVICES.length);
                String device = DEVICES[devIdx];
                String browser = BROWSERS[rnd.nextInt(BROWSERS.length)];
                String os = OS[rnd.nextInt(OS.length)];
                String[] geo = GEO[rnd.nextInt(GEO.length)];
                String screen = SCREENS[rnd.nextInt(SCREENS.length)];
                String referrer = REFERRERS[rnd.nextInt(REFERRERS.length)];
                String ip = "10." + rnd.nextInt(255) + "." + rnd.nextInt(255) + "." + rnd.nextInt(255);

                int startHour = 8 + rnd.nextInt(13);
                LocalDateTime cursor = LocalDateTime.of(day, LocalTime.of(startHour, rnd.nextInt(60), rnd.nextInt(60)));

                boolean isNew = !profileMap.containsKey(distinctId);
                final LocalDateTime firstSeen = cursor;
                UserProfile prof = profileMap.computeIfAbsent(distinctId, k -> {
                    UserProfile p = new UserProfile();
                    p.setTenantId(tid);
                    p.setAppId(appId);
                    p.setDistinctId(k);
                    p.setFirstSeen(firstSeen);
                    p.setVisitCount(0L);
                    p.setEventCount(0L);
                    p.setPageviewCount(0L);
                    return p;
                });
                prof.setVisitCount(prof.getVisitCount() + 1);
                prof.setDevice(device);
                prof.setBrowser(browser);
                prof.setOs(os);
                prof.setRegion(geo[0]);
                prof.setCity(geo[1]);

                // journey
                List<int[]> journey = new ArrayList<>(); // path index
                journey.add(new int[]{0}); // home
                boolean toProducts = rnd.nextDouble() < 0.72;
                boolean toProduct = false, toCart = false, toCheckout = false, toPurchase = false, toSignup = false;
                if (toProducts) {
                    journey.add(new int[]{1});
                    toProduct = rnd.nextDouble() < 0.78;
                    if (toProduct) {
                        journey.add(new int[]{rnd.nextBoolean() ? 2 : 3});
                        toCart = rnd.nextDouble() < 0.55;
                        if (toCart) {
                            journey.add(new int[]{4});
                            toCheckout = rnd.nextDouble() < 0.66;
                            if (toCheckout) {
                                journey.add(new int[]{5});
                                toPurchase = rnd.nextDouble() < 0.7;
                            }
                        }
                    }
                    toSignup = rnd.nextDouble() < 0.18;
                }

                long sessionStart = System.currentTimeMillis();
                for (int ji = 0; ji < journey.size(); ji++) {
                    int pi = journey.get(ji)[0];
                    cursor = cursor.plusSeconds(15 + rnd.nextInt(60));
                    // pageview
                    Event pv = baseEvent(tid, appId, distinctId, sessionId, device, browser, os, geo, screen, ip, rnd);
                    pv.setEventType("pageview");
                    pv.setEventName("$pageview");
                    pv.setUrlPath(PATHS[pi]);
                    pv.setUrl("https://shop.demo.com" + PATHS[pi]);
                    pv.setTitle(TITLES[pi]);
                    pv.setReferrer(ji == 0 ? referrer : "https://shop.demo.com" + PATHS[journey.get(ji - 1)[0]]);
                    pv.setEventTime(cursor);
                    buffer.add(pv);
                    prof.setPageviewCount(prof.getPageviewCount() + 1);
                    prof.setEventCount(prof.getEventCount() + 1);
                    prof.setLastUrl(pv.getUrl());
                    prof.setLastSeen(cursor);

                    // clicks (heatmap) on some pages
                    int clicks = rnd.nextInt(4);
                    for (int c = 0; c < clicks; c++) {
                        Event cl = baseEvent(tid, appId, distinctId, sessionId, device, browser, os, geo, screen, ip, rnd);
                        cl.setEventType("click");
                        cl.setEventName("$click");
                        cl.setUrlPath(PATHS[pi]);
                        cl.setUrl(pv.getUrl());
                        cl.setX((double) (40 + rnd.nextInt(1200)));
                        cl.setY((double) (60 + rnd.nextInt(700)));
                        cl.setElement(pickElement(rnd, pi));
                        cl.setEventTime(cursor.plusSeconds(2 + rnd.nextInt(20)));
                        buffer.add(cl);
                        prof.setEventCount(prof.getEventCount() + 1);
                    }

                    // business events tied to page
                    if (pi == 1 || pi == 2 || pi == 3) {
                        addCustom(buffer, tid, appId, distinctId, sessionId, "view_product", cursor.plusSeconds(5), device, browser, os, geo, screen, ip, rnd, PATHS[pi], pv.getUrl());
                        prof.setEventCount(prof.getEventCount() + 1);
                    }
                    if (pi == 1 && rnd.nextDouble() < 0.4) {
                        addCustom(buffer, tid, appId, distinctId, sessionId, "search", cursor.plusSeconds(8), device, browser, os, geo, screen, ip, rnd, PATHS[pi], pv.getUrl());
                    }
                }
                if (toCart) { addCustom(buffer, tid, appId, distinctId, sessionId, "add_to_cart", cursor.plusSeconds(10), device, browser, os, geo, screen, ip, rnd, "/cart", "https://shop.demo.com/cart"); prof.setEventCount(prof.getEventCount() + 1); }
                if (toCheckout) { addCustom(buffer, tid, appId, distinctId, sessionId, "begin_checkout", cursor.plusSeconds(20), device, browser, os, geo, screen, ip, rnd, "/checkout", "https://shop.demo.com/checkout"); prof.setEventCount(prof.getEventCount() + 1); }
                if (toPurchase) { addCustom(buffer, tid, appId, distinctId, sessionId, "purchase", cursor.plusSeconds(40), device, browser, os, geo, screen, ip, rnd, "/checkout", "https://shop.demo.com/checkout"); prof.setEventCount(prof.getEventCount() + 1); }
                if (toSignup) { addCustom(buffer, tid, appId, distinctId, sessionId, "sign_up", cursor.plusSeconds(45), device, browser, os, geo, screen, ip, rnd, "/about", "https://shop.demo.com/about"); prof.setEventCount(prof.getEventCount() + 1); }

                // leave event with dwell time
                Event leave = baseEvent(tid, appId, distinctId, sessionId, device, browser, os, geo, screen, ip, rnd);
                leave.setEventType("leave");
                leave.setEventName("$leave");
                leave.setUrlPath(PATHS[journey.get(journey.size() - 1)[0]]);
                leave.setUrl("https://shop.demo.com" + leave.getUrlPath());
                leave.setDuration((long) (30000 + rnd.nextInt(240000)));
                leave.setEventTime(cursor.plusSeconds(60));
                buffer.add(leave);

                if (buffer.size() >= 800) {
                    eventRepository.saveAll(buffer);
                    buffer.clear();
                }
            }
        }
        if (!buffer.isEmpty()) { eventRepository.saveAll(buffer); buffer.clear(); }

        // finalize profiles + tags
        for (UserProfile p : profileMap.values()) {
            p.setTags(deriveTags(p));
        }
        profileRepository.saveAll(profileMap.values());

        // realtime minute buckets for last 60 minutes (instant live dashboard)
        seedRealtimeMinutes(tid, appId, rnd);

        // historical audit / login logs for the system-management pages
        seedAuditLogs(tid, superAdmin, demoAdmin, analyst, rnd);

        log.info("Seed complete: {} events, {} profiles.", eventRepository.count(), profileMap.size());
    }

    private void seedRoles() {
        List<Role> roles = new ArrayList<>();
        roles.add(role("SUPER_ADMIN", "平台超级管理员", "跨租户管理平台，管理租户与全局配置",
                "analytics:view,data:view,assistant:use,app:manage,agent:manage,user:manage,role:manage,tenant:manage,system:manage"));
        roles.add(role("ADMIN", "租户管理员", "管理本租户用户、应用与全部数据",
                "analytics:view,data:view,assistant:use,app:manage,agent:manage,user:manage,role:manage,system:manage"));
        roles.add(role("ANALYST", "数据分析师", "查看全部分析与数据，可管理应用",
                "analytics:view,data:view,assistant:use,app:manage,agent:manage"));
        roles.add(role("VIEWER", "只读访客", "仅查看分析报表",
                "analytics:view,data:view,assistant:use"));
        roleRepository.saveAll(roles);
    }

    private Role role(String code, String name, String desc, String perms) {
        Role r = new Role();
        r.setCode(code);
        r.setName(name);
        r.setDescription(desc);
        r.setPermissions(perms);
        return r;
    }

    private void seedAgents(Long tid, String appKey) {
        String[][] defs = {
                {"web-collector-01", "120.55.38.71", "ONLINE", "生产,华东"},
                {"web-collector-02", "120.55.38.72", "ONLINE", "生产,华南"},
                {"edge-probe-sz", "10.0.3.21", "OFFLINE", "边缘,深圳"},
        };
        for (String[] d : defs) {
            Agent a = new Agent();
            a.setTenantId(tid);
            a.setName(d[0]);
            a.setHost(d[1]);
            a.setIp(d[1]);
            a.setStatus(d[2]);
            a.setTags(d[3]);
            a.setVersion("1.0.0");
            a.setOs("linux");
            a.setArch("amd64");
            a.setAgentKey("pilot_" + UUID.randomUUID().toString().replace("-", ""));
            a.setConfig("{\"collectIntervalMs\":5000,\"batchSize\":50,\"sampling\":1.0,\"enabled\":true}");
            a.setConfigVersion(1L);
            a.setMetrics("{\"cpu\":12.5,\"memMb\":180,\"qps\":42}");
            a.setEventsSent((long) (10000 + new Random().nextInt(50000)));
            if ("ONLINE".equals(d[2])) a.setLastHeartbeat(LocalDateTime.now().minusSeconds(10));
            else a.setLastHeartbeat(LocalDateTime.now().minusMinutes(20));
            agentRepository.save(a);
        }
    }

    private Event baseEvent(Long tid, Long appId, String distinctId, String sessionId, String device, String browser,
                            String os, String[] geo, String screen, String ip, Random rnd) {
        Event e = new Event();
        e.setTenantId(tid);
        e.setAppId(appId);
        e.setDistinctId(distinctId);
        e.setSessionId(sessionId);
        e.setDevice(device);
        e.setBrowser(browser);
        e.setOs(os);
        e.setCountry("中国");
        e.setRegion(geo[0]);
        e.setCity(geo[1]);
        e.setScreen(screen);
        e.setIp(ip);
        e.setUserAgent(uaFor(device, browser, os));
        return e;
    }

    private String uaFor(String device, String browser, String os) {
        String b = "Chrome".equals(browser) ? "Chrome/126.0 Safari/537.36"
                : "Safari".equals(browser) ? "Version/17.0 Safari/605.1.15"
                : "Edge".equals(browser) ? "Edg/126.0"
                : "Firefox".equals(browser) ? "Firefox/127.0"
                : "WeChat".equals(browser) ? "MicroMessenger/8.0 Chrome/107"
                : "Chrome/126.0";
        String plat = "Mobile".equals(device) ? "Linux; Android 14" : "Windows NT 10.0; Win64; x64";
        return "Mozilla/5.0 (" + plat + ") AppleWebKit/537.36 " + b;
    }

    private void addCustom(List<Event> buffer, Long tid, Long appId, String distinctId, String sessionId, String name,
                           LocalDateTime t, String device, String browser, String os, String[] geo, String screen,
                           String ip, Random rnd, String path, String url) {
        Event e = baseEvent(tid, appId, distinctId, sessionId, device, browser, os, geo, screen, ip, rnd);
        e.setEventType("event");
        e.setEventName(name);
        e.setUrlPath(path);
        e.setUrl(url);
        e.setEventTime(t);
        if ("purchase".equals(name)) {
            e.setProperties("{\"amount\":" + (80 + rnd.nextInt(900)) + ",\"currency\":\"CNY\",\"items\":" + (1 + rnd.nextInt(4)) + "}");
        } else if ("search".equals(name)) {
            String[] kws = {"无线耳机", "机械键盘", "智能手表", "显示器", "移动电源"};
            e.setProperties("{\"keyword\":\"" + kws[rnd.nextInt(kws.length)] + "\"}");
        }
        buffer.add(e);
    }

    private String pickElement(Random rnd, int pathIndex) {
        String[][] opts = {
                {"#nav-home", ".banner-cta", "#search-box"},
                {".product-card", ".filter-btn", "#sort-select", ".product-card"},
                {".add-to-cart-btn", ".sku-option", "#buy-now"},
                {".add-to-cart-btn", ".gallery-thumb"},
                {".checkout-btn", ".qty-input", ".coupon-link"},
                {".pay-btn", ".address-select"},
                {".blog-post", ".subscribe-btn"},
                {".contact-form", ".map-widget"}
        };
        String[] arr = opts[pathIndex % opts.length];
        return arr[rnd.nextInt(arr.length)];
    }

    private void seedRealtimeMinutes(Long tid, Long appId, Random rnd) {
        LocalDateTime base = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<RealtimeMinute> list = new ArrayList<>();
        for (int i = 59; i >= 0; i--) {
            LocalDateTime minute = base.minusMinutes(i);
            RealtimeMinute m = new RealtimeMinute();
            m.setTenantId(tid);
            m.setAppId(appId);
            m.setMinute(minute);
            int hour = minute.getHour();
            double load = (hour >= 9 && hour <= 22) ? 1.0 : 0.35;
            long pv = (long) (3 + rnd.nextInt(12) * load);
            m.setPv(pv);
            m.setUv((long) Math.max(1, pv * (0.5 + rnd.nextDouble() * 0.3)));
            m.setEvents(pv + (long) (rnd.nextInt(15) * load));
            list.add(m);
        }
        realtimeRepository.saveAll(list);
    }

    private String deriveTags(UserProfile p) {
        StringBuilder sb = new StringBuilder();
        if (p.getVisitCount() >= 5) sb.append("高频用户,");
        if (p.getEventCount() >= 50) sb.append("活跃用户,");
        if ("Mobile".equals(p.getDevice()) || "MiniProgram".equals(p.getDevice())) sb.append("移动端,");
        if ("Desktop".equals(p.getDevice())) sb.append("桌面端,");
        if (sb.length() == 0) sb.append("新用户,");
        String s = sb.toString();
        if (s.endsWith(",")) s = s.substring(0, s.length() - 1);
        return s.length() > 255 ? s.substring(0, 255) : s;
    }

    // ================= system-management seed =================

    private void seedSystemGlobal() {
        log.info("Seeding system config / dict / notice / tasks...");
        List<SystemConfig> configs = new ArrayList<>();
        configs.add(config("platform.name", "平台名称", "UBA-X", "STRING", "平台", "显示在登录页与顶栏的平台名称"));
        configs.add(config("platform.notice.enabled", "启用公告栏", "true", "BOOLEAN", "平台", "是否在顶栏展示通知公告铃铛"));
        configs.add(config("data.retention.days", "事件数据保留天数", "180", "NUMBER", "数据治理", "超期事件由清理任务自动删除"));
        configs.add(config("audit.retention.days", "日志保留天数", "90", "NUMBER", "数据治理", "操作/登录日志的保留周期"));
        configs.add(config("collect.enabled", "启用数据采集", "true", "BOOLEAN", "采集", "全局采集开关，关闭后拒绝上报"));
        configs.add(config("collect.sampling", "采集采样率", "1.0", "NUMBER", "采集", "0~1，1 表示全量采集"));
        configs.add(config("collect.ip.anonymize", "IP 脱敏", "true", "BOOLEAN", "采集", "存储时对末段做匿名化处理"));
        configs.add(config("session.timeout.minutes", "会话超时(分钟)", "30", "NUMBER", "安全", "无操作多久判定会话结束"));
        configs.add(config("login.maxRetry", "登录失败锁定阈值", "5", "NUMBER", "安全", "连续失败次数达到后临时锁定"));
        configRepository.saveAll(configs);

        List<Dict> dicts = new ArrayList<>();
        dicts.add(dict("event_type", "浏览页面", "pageview", 1));
        dicts.add(dict("event_type", "点击", "click", 2));
        dicts.add(dict("event_type", "自定义事件", "event", 3));
        dicts.add(dict("event_type", "离开页面", "leave", 4));
        dicts.add(dict("channel_source", "百度", "baidu", 1));
        dicts.add(dict("channel_source", "谷歌", "google", 2));
        dicts.add(dict("channel_source", "微信", "weixin", 3));
        dicts.add(dict("channel_source", "抖音", "douyin", 4));
        dicts.add(dict("channel_source", "直接访问", "direct", 5));
        dicts.add(dict("device_type", "桌面端", "Desktop", 1));
        dicts.add(dict("device_type", "移动端", "Mobile", 2));
        dicts.add(dict("device_type", "平板", "Tablet", 3));
        dicts.add(dict("device_type", "小程序", "MiniProgram", 4));
        dicts.add(dict("notice_type", "通知", "NOTICE", 1));
        dicts.add(dict("notice_type", "公告", "ANNOUNCE", 2));
        dictRepository.saveAll(dicts);

        Notice n1 = new Notice();
        n1.setTitle("UBA-X 数据应用矩阵上线");
        n1.setContent("新增归因分析、A/B 实验、转化路径、RFM 价值分层等 11 个数据应用，欢迎在「数据应用」中体验。");
        n1.setType("ANNOUNCE");
        n1.setStatus("PUBLISHED");
        n1.setPublisher("admin");
        n1.setTop(true);
        n1.setPublishedAt(LocalDateTime.now().minusDays(1));
        noticeRepository.save(n1);

        Notice n2 = new Notice();
        n2.setTitle("本周系统维护通知");
        n2.setContent("计划于本周六 02:00-04:00 进行数据库维护，期间采集上报可能短暂延迟，请提前知悉。");
        n2.setType("NOTICE");
        n2.setStatus("PUBLISHED");
        n2.setPublisher("admin");
        n2.setPublishedAt(LocalDateTime.now().minusDays(3));
        noticeRepository.save(n2);

        Notice n3 = new Notice();
        n3.setTitle("新版 SDK 发布(草稿)");
        n3.setContent("Web SDK 1.1 版本正在测试，尚未发布。");
        n3.setType("NOTICE");
        n3.setStatus("DRAFT");
        n3.setPublisher("admin");
        noticeRepository.save(n3);

        taskRepository.save(task("clean_expired_events", "清理过期事件", "0 0 3 * * ?", true,
                "按「事件数据保留天数」删除超期事件，每日 03:00 执行"));
        taskRepository.save(task("clean_audit_logs", "清理历史日志", "0 30 3 * * ?", false,
                "按「日志保留天数」删除超期操作/登录日志，每日 03:30 执行"));
        taskRepository.save(task("refresh_monitor", "刷新监控快照", "0 */5 * * * ?", true,
                "定期采集服务器/JVM/数据指标，每 5 分钟执行"));
    }

    private SystemConfig config(String key, String name, String value, String type, String group, String remark) {
        SystemConfig c = new SystemConfig();
        c.setConfigKey(key);
        c.setConfigName(name);
        c.setConfigValue(value);
        c.setValueType(type);
        c.setGroupKey(group);
        c.setRemark(remark);
        c.setBuiltin(true);
        return c;
    }

    private Dict dict(String type, String label, String value, int sort) {
        Dict d = new Dict();
        d.setDictType(type);
        d.setDictLabel(label);
        d.setDictValue(value);
        d.setSortNo(sort);
        d.setStatus("ENABLED");
        return d;
    }

    private ScheduledTask task(String code, String name, String cron, boolean enabled, String desc) {
        ScheduledTask t = new ScheduledTask();
        t.setCode(code);
        t.setName(name);
        t.setCron(cron);
        t.setEnabled(enabled);
        t.setDescription(desc);
        return t;
    }

    private void seedAuditLogs(Long tid, User superAdmin, User demoAdmin, User analyst, Random rnd) {
        List<AuditLog> logs = new ArrayList<>();
        User[] users = {demoAdmin, analyst, superAdmin};
        String[] ips = {"120.55.38.71", "10.0.3.21", "192.168.1.66", "172.16.8.30", "127.0.0.1"};
        String[] uas = {
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/126.0 Safari/537.36",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) Version/17.0 Safari/605.1.15",
                "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 Chrome/126.0 Mobile Safari/537.36"
        };
        String[][] opTemplates = {
                {"用户管理", "新建用户", "CREATE", "用户名=operator_%d, 角色=ANALYST"},
                {"用户管理", "编辑用户", "UPDATE", "用户=analyst"},
                {"用户管理", "重置密码", "UPDATE", "用户=viewer_%d"},
                {"应用管理", "新建应用", "CREATE", "应用=活动落地页(H5)"},
                {"应用管理", "编辑应用", "UPDATE", "应用=官网商城(Web)"},
                {"应用管理", "重置AppKey", "UPDATE", "应用=小程序端"},
                {"角色权限", "编辑角色", "UPDATE", "角色=ANALYST, 权限+app:manage"},
                {"系统配置", "修改参数", "UPDATE", "data.retention.days=180"},
                {"数据字典", "保存字典项", "CREATE", "channel_source:douyin"},
                {"通知公告", "发布公告", "CREATE", "UBA-X 数据应用矩阵上线"},
                {"定时任务", "手动执行任务", "OTHER", "清理过期事件 -> SUCCESS"},
        };

        LocalDate today = LocalDate.now();
        for (int dayOffset = 13; dayOffset >= 0; dayOffset--) {
            LocalDate day = today.minusDays(dayOffset);
            int loginCount = 2 + rnd.nextInt(4);
            for (int i = 0; i < loginCount; i++) {
                User u = users[rnd.nextInt(users.length)];
                LocalTime t = LocalTime.of(8 + rnd.nextInt(12), rnd.nextInt(60), rnd.nextInt(60));
                boolean fail = rnd.nextDouble() < 0.08;
                Long atid = u.getTenantId() != null ? tid : null;
                logs.add(audit(atid, u, "LOGIN", "认证", fail ? "登录失败" : "登录成功",
                        fail ? "用户名或密码错误" : "登录成功", !fail, LocalDateTime.of(day, t),
                        ips[rnd.nextInt(ips.length)], uas[rnd.nextInt(uas.length)]));
            }
            if (day.getDayOfWeek().getValue() <= 5) {
                int opCount = 1 + rnd.nextInt(3);
                for (int i = 0; i < opCount; i++) {
                    String[] tpl = opTemplates[rnd.nextInt(opTemplates.length)];
                    User u = rnd.nextBoolean() ? demoAdmin : (rnd.nextBoolean() ? analyst : superAdmin);
                    LocalTime t = LocalTime.of(9 + rnd.nextInt(10), rnd.nextInt(60), rnd.nextInt(60));
                    String detail = tpl[3].contains("%d") ? String.format(tpl[3], rnd.nextInt(90) + 10) : tpl[3];
                    Long atid = u.getTenantId() != null ? tid : null;
                    logs.add(audit(atid, u, tpl[2], tpl[0], tpl[1], detail, true,
                            LocalDateTime.of(day, t), ips[rnd.nextInt(ips.length)], uas[rnd.nextInt(uas.length)]));
                }
            }
        }
        logs.sort(Comparator.comparing(AuditLog::getCreatedAt).reversed());
        auditLogRepository.saveAll(logs);
    }

    private AuditLog audit(Long tenantId, User u, String type, String module, String action,
                           String detail, boolean success, LocalDateTime at, String ip, String ua) {
        AuditLog a = new AuditLog();
        a.setTenantId(tenantId);
        a.setUserId(u.getId());
        a.setUsername(u.getUsername());
        a.setType(type);
        a.setModule(module);
        a.setAction(action);
        a.setDetail(detail);
        a.setSuccess(success);
        a.setCreatedAt(at);
        a.setIp(ip);
        a.setLocation(ip.startsWith("120.55") ? "外部" : "内网/本机");
        a.setUserAgent(ua);
        return a;
    }
}
