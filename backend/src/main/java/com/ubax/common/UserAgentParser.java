package com.ubax.common;

import java.util.HashMap;
import java.util.Map;

/**
 * Minimal, dependency-free user-agent parser producing device/os/browser buckets.
 */
public class UserAgentParser {

    public static Map<String, String> parse(String ua) {
        Map<String, String> r = new HashMap<>();
        String u = ua == null ? "" : ua;
        String low = u.toLowerCase();

        String device = "Desktop";
        if (low.contains("ipad") || low.contains("tablet") || (low.contains("android") && !low.contains("mobile"))) {
            device = "Tablet";
        } else if (low.contains("iphone") || low.contains("android") || low.contains("mobile")
                || low.contains("harmonyos") || u.contains("MicroMessenger")) {
            device = "Mobile";
        }
        if (u.contains("MicroMessenger")) device = "MiniProgram";
        r.put("device", device);

        String os = "Other";
        if (low.contains("windows")) os = "Windows";
        else if (low.contains("iphone") || low.contains("ipad") || low.contains("ios") || low.contains("mac os")) os = "iOS/macOS";
        else if (low.contains("android")) os = "Android";
        else if (low.contains("mac os") || low.contains("macintosh")) os = "macOS";
        else if (low.contains("linux")) os = "Linux";
        else if (low.contains("harmonyos")) os = "HarmonyOS";
        r.put("os", os);

        String browser = "Other";
        if (u.contains("MicroMessenger")) browser = "WeChat";
        else if (low.contains("edg")) browser = "Edge";
        else if (low.contains("chrome") || low.contains("crios")) browser = "Chrome";
        else if (low.contains("firefox") || low.contains("fxios")) browser = "Firefox";
        else if (low.contains("safari")) browser = "Safari";
        else if (low.contains("opera") || low.contains("opr/")) browser = "Opera";
        r.put("browser", browser);

        return r;
    }
}
