package com.ubax.sdk;

import org.springframework.core.io.ClassPathResource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Serves the client-side SDK artifacts bundled in the jar under classpath:/sdk/.
 * <ul>
 *   <li>{@code /sdk/pilot-agent.sh} — the ubax-pilot probe installer/runtime (curl | bash).</li>
 *   <li>{@code /sdk/ubax.js} — the browser behavior-collection SDK embedded by apps.</li>
 * </ul>
 * These paths are open (see SecurityConfig {@code /sdk/**}). Scripts are served as
 * plain text with a short cache so freshly created agents and upgrades pull the latest.
 */
@RestController
@RequestMapping("/sdk")
public class SdkController {

    private static final CacheControl NO_CACHE = CacheControl.noCache().cachePrivate();

    @GetMapping(value = "/pilot-agent.sh", produces = "text/plain;charset=UTF-8")
    public ResponseEntity<String> pilotAgent() throws IOException {
        return text("sdk/pilot-agent.sh", NO_CACHE);
    }

    @GetMapping(value = "/ubax.js", produces = "application/javascript;charset=UTF-8")
    public ResponseEntity<String> ubaxJs() throws IOException {
        return text("sdk/ubax.js", CacheControl.maxAge(5, TimeUnit.MINUTES).cachePrivate());
    }

    private ResponseEntity<String> text(String classpathLocation, CacheControl cache) throws IOException {
        ClassPathResource res = new ClassPathResource(classpathLocation);
        if (!res.exists()) {
            return ResponseEntity.notFound().build();
        }
        String body;
        try (InputStream in = res.getInputStream()) {
            body = StreamUtils.copyToString(in, StandardCharsets.UTF_8);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setCacheControl(cache);
        headers.setContentType(new MediaType("text", "plain", StandardCharsets.UTF_8));
        if (classpathLocation.endsWith(".js")) {
            headers.setContentType(new MediaType("application", "javascript", StandardCharsets.UTF_8));
        }
        return new ResponseEntity<>(body, headers, org.springframework.http.HttpStatus.OK);
    }
}
