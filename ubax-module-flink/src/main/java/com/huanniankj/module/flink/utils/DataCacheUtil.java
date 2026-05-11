package com.huanniankj.module.flink.utils;

import java.nio.charset.Charset;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DataCacheUtil {
    /**
     * 缓存对象
     */
    private static final ConcurrentMap<String, String> htForCity = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> htForProvince = new ConcurrentHashMap<>();
    private static final ConcurrentMap<String, String> htForCountry = new ConcurrentHashMap<>();

    /**
     * 静态代码块进行初始化，确保应用启动时加载数据
     * TODO: 如果需要支持字典文件热更新，可以考虑引入定时任务或 WatchService 监听文件变更
     */
    static {
        loadCity();
        loadProvince();
        loadCountry();
    }

    /**
     * 加载城市数据
     */
    private static void loadCity() {
        try (java.io.InputStream is = DataCacheUtil.class.getClassLoader().getResourceAsStream("iplib/chinacity.txt")) {
            if (is == null) {
                System.err.println("Resource 'iplib/chinacity.txt' not found");
                return;
            }
            List<String> lineCityList = org.apache.commons.io.IOUtils.readLines(is, Charset.forName("GB2312"));
            for (String line : lineCityList) {
                String[] pair = line.split(",");
                if (pair.length >= 2) {
                    htForCity.put(pair[0].toLowerCase(Locale.ROOT), pair[1]);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading city data: " + ex.getMessage());
        }
    }

    /**
     * 加载省份数据
     */
    private static void loadProvince() {
        try (java.io.InputStream is = DataCacheUtil.class.getClassLoader().getResourceAsStream("iplib/chinaprovince.txt")) {
            if (is == null) {
                System.err.println("Resource 'iplib/chinaprovince.txt' not found");
                return;
            }
            List<String> lineProvinceList = org.apache.commons.io.IOUtils.readLines(is, Charset.forName("GB2312"));
            for (String line : lineProvinceList) {
                String[] pair = line.split(",");
                if (pair.length >= 2) {
                    htForProvince.put(pair[0].toLowerCase(Locale.ROOT), pair[1]);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading province data: " + ex.getMessage());
        }
    }

    /**
     * 加载国家数据
     */
    private static void loadCountry() {
        try (java.io.InputStream is = DataCacheUtil.class.getClassLoader().getResourceAsStream("iplib/country.txt")) {
            if (is == null) {
                System.err.println("Resource 'iplib/country.txt' not found");
                return;
            }
            List<String> countryList = org.apache.commons.io.IOUtils.readLines(is, Charset.forName("GB2312"));
            for (String line : countryList) {
                String[] pair = line.split(",");
                if (pair.length >= 2) {
                    htForCountry.put(pair[0].toLowerCase(Locale.ROOT), pair[1]);
                }
            }
        } catch (Exception ex) {
            System.err.println("Error loading country data: " + ex.getMessage());
        }
    }

    /**
     * 获取城市缓存
     */
    public static String getCity(String cityCode) {
        return htForCity.get(cityCode.toLowerCase(Locale.ROOT));
    }

    /**
     * 获取省份缓存
     */
    public static String getProvince(String provinceCode) {
        return htForProvince.get(provinceCode.toLowerCase(Locale.ROOT));
    }

    /**
     * 获取国家缓存
     */
    public static String getCountry(String countryCode) {
        return htForCountry.get(countryCode.toLowerCase(Locale.ROOT));
    }

    /**
     * 获取所有城市
     */
    public static ConcurrentMap<String, String> getAllCities() {
        return htForCity;
    }

    /**
     * 获取所有省份
     */
    public static ConcurrentMap<String, String> getAllProvinces() {
        return htForProvince;
    }

    /**
     * 获取所有国家
     */
    public static ConcurrentMap<String, String> getAllCountries() {
        return htForCountry;
    }

    private static String ipV4TempPath = null;
    private static String ipV6TempPath = null;

    private static String extractResourceToTempFile(String resourcePath) {
        try (java.io.InputStream is = DataCacheUtil.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                System.err.println("Resource '" + resourcePath + "' not found");
                return null;
            }
            String fileName = new java.io.File(resourcePath).getName();
            String prefix = fileName;
            String suffix = "";
            int dotIndex = fileName.lastIndexOf('.');
            if (dotIndex > 0) {
                prefix = fileName.substring(0, dotIndex);
                suffix = fileName.substring(dotIndex);
            }
            if (prefix.length() < 3) {
                prefix += "tmp";
            }
            java.io.File tempFile = java.io.File.createTempFile(prefix + "-", suffix);
            tempFile.deleteOnExit();
            try (java.io.OutputStream out = new java.io.FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            return tempFile.getAbsolutePath();
        } catch (Exception e) {
            System.err.println("Error extracting resource " + resourcePath + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * 获取ipv4文件
     */
    public static synchronized String getIpV4File() {
        if (ipV4TempPath == null) {
            ipV4TempPath = extractResourceToTempFile("iplib/IP2LOCATION-LITE-DB3.BIN");
        }
        return ipV4TempPath;
    }

    /**
     * 获取ipv6文件
     */
    public static synchronized String getIpV6File() {
        if (ipV6TempPath == null) {
            ipV6TempPath = extractResourceToTempFile("iplib/IP2LOCATION-LITE-DB3.IPV6.BIN");
        }
        return ipV6TempPath;
    }
}
