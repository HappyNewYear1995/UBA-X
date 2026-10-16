package com.ubax.system;

import com.ubax.common.BusinessException;
import com.ubax.system.domain.Dict;
import com.ubax.system.domain.Notice;
import com.ubax.system.domain.SystemConfig;
import com.ubax.system.repo.DictRepository;
import com.ubax.system.repo.NoticeRepository;
import com.ubax.system.repo.SystemConfigRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Config parameters, data dictionary and notices (platform-global data). */
@Service
public class SystemService {

    private final SystemConfigRepository configRepo;
    private final DictRepository dictRepo;
    private final NoticeRepository noticeRepo;

    public SystemService(SystemConfigRepository configRepo, DictRepository dictRepo, NoticeRepository noticeRepo) {
        this.configRepo = configRepo;
        this.dictRepo = dictRepo;
        this.noticeRepo = noticeRepo;
    }

    // ---------- config ----------

    public List<SystemConfig> listConfig() {
        return configRepo.findAllByOrderByGroupKeyAscIdAsc();
    }

    /** Group configs by their groupKey for the settings form. */
    public List<Map<String, Object>> groupedConfig() {
        Map<String, List<SystemConfig>> byGroup = new LinkedHashMap<>();
        for (SystemConfig c : listConfig()) {
            byGroup.computeIfAbsent(c.getGroupKey() == null ? "其他" : c.getGroupKey(), k -> new ArrayList<>()).add(c);
        }
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<String, List<SystemConfig>> e : byGroup.entrySet()) {
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("group", e.getKey());
            m.put("items", e.getValue());
            out.add(m);
        }
        return out;
    }

    public SystemConfig updateConfig(String key, String value) {
        SystemConfig c = configRepo.findByConfigKey(key)
                .orElseThrow(() -> new BusinessException(404, "配置项不存在:" + key));
        c.setConfigValue(value);
        return configRepo.save(c);
    }

    public List<SystemConfig> saveAll(List<Map<String, String>> edits) {
        List<SystemConfig> saved = new ArrayList<>();
        for (Map<String, String> e : edits) {
            String key = e.get("configKey");
            if (key == null) continue;
            configRepo.findByConfigKey(key).ifPresent(c -> {
                c.setConfigValue(e.get("configValue"));
                saved.add(configRepo.save(c));
            });
        }
        return saved;
    }

    public String get(String key, String def) {
        return configRepo.findByConfigKey(key).map(SystemConfig::getConfigValue).filter(v -> v != null && !v.isBlank())
                .orElse(def);
    }

    public int getInt(String key, int def) {
        try { return Integer.parseInt(get(key, String.valueOf(def)).trim()); }
        catch (Exception e) { return def; }
    }

    public boolean getBool(String key, boolean def) {
        String v = get(key, String.valueOf(def));
        return "true".equalsIgnoreCase(v) || "1".equals(v) || "on".equalsIgnoreCase(v);
    }

    // ---------- dict ----------

    public List<String> dictTypes() {
        return dictRepo.distinctTypes();
    }

    public List<Dict> dictByType(String type) {
        return dictRepo.findByDictTypeOrderBySortNoAscIdAsc(type);
    }

    public Map<String, List<Dict>> dictAll() {
        Map<String, List<Dict>> m = new LinkedHashMap<>();
        for (Dict d : dictRepo.findAll()) {
            m.computeIfAbsent(d.getDictType(), k -> new ArrayList<>()).add(d);
        }
        return m;
    }

    public Dict saveDict(Dict d) {
        if (d.getDictType() == null || d.getDictType().isBlank()) throw new BusinessException("字典类型必填");
        if (d.getDictValue() == null || d.getDictValue().isBlank()) throw new BusinessException("字典键值必填");
        if (d.getId() == null && dictRepo.existsByDictTypeAndDictValue(d.getDictType(), d.getDictValue())) {
            throw new BusinessException("该类型下键值已存在");
        }
        if (d.getSortNo() == null) d.setSortNo(0);
        if (d.getStatus() == null) d.setStatus("ENABLED");
        return dictRepo.save(d);
    }

    public void deleteDict(Long id) {
        dictRepo.deleteById(id);
    }

    // ---------- notice ----------

    public List<Notice> listNotice() {
        return noticeRepo.findAllByOrderByTopDescPublishedAtDescIdDesc();
    }

    public List<Notice> publishedNotice() {
        return noticeRepo.findByStatusOrderByTopDescPublishedAtDescIdDesc("PUBLISHED");
    }

    public Notice saveNotice(Notice n, String publisher) {
        if (n.getTitle() == null || n.getTitle().isBlank()) throw new BusinessException("标题必填");
        if (n.getStatus() == null) n.setStatus("DRAFT");
        if ("PUBLISHED".equals(n.getStatus()) && n.getPublishedAt() == null) n.setPublishedAt(LocalDateTime.now());
        if (n.getId() == null && (n.getPublisher() == null || n.getPublisher().isBlank())) n.setPublisher(publisher);
        return noticeRepo.save(n);
    }

    public void deleteNotice(Long id) {
        noticeRepo.deleteById(id);
    }
}
