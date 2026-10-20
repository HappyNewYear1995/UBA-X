package com.ubax.governance.service;

import com.ubax.common.BusinessException;
import com.ubax.governance.domain.IdentityRule;
import com.ubax.governance.domain.UserIdentityAlias;
import com.ubax.governance.repo.IdentityRuleRepository;
import com.ubax.governance.repo.UserIdentityAliasRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Identity resolution: folds an anonymous distinctId onto a unified identity
 * (via the alias table) so pre- and post-login events map to a single profile.
 * Alias lookups run on the per-event hot path and are cached per tenant+app.
 */
@Service
public class IdentityService {

    private final UserIdentityAliasRepository aliasRepo;
    private final IdentityRuleRepository ruleRepo;
    /** "tenantId|appId" -> (distinctId -> unifiedId). */
    private final Map<String, Map<String, String>> aliasCache = new ConcurrentHashMap<>();
    /** tenantId -> default strategy. */
    private final Map<Long, String> strategyCache = new ConcurrentHashMap<>();

    public IdentityService(UserIdentityAliasRepository aliasRepo, IdentityRuleRepository ruleRepo) {
        this.aliasRepo = aliasRepo;
        this.ruleRepo = ruleRepo;
    }

    // ---------- hot path ----------

    private Map<String, String> aliases(Long tenantId, Long appId) {
        String key = tenantId + "|" + appId;
        return aliasCache.computeIfAbsent(key, k -> {
            Map<String, String> m = new ConcurrentHashMap<>();
            for (UserIdentityAlias a : aliasRepo.findByTenantId(tenantId, PageRequest.of(0, 100000)).getContent()) {
                if (Objects.equals(a.getAppId(), appId) || a.getAppId() == null || a.getAppId() == 0L) {
                    m.put(a.getDistinctId(), a.getUnifiedId());
                }
            }
            return m;
        });
    }

    private String strategy(Long tenantId) {
        return strategyCache.computeIfAbsent(tenantId, t ->
                ruleRepo.findFirstByTenantIdAndEnabledTrueOrderByAppIdDesc(t)
                        .map(IdentityRule::getStrategy).orElse("DISTINCT_FIRST"));
    }

    /**
     * Resolve the profile key for an incoming (distinctId, userId).
     * Alias hit wins; otherwise the tenant strategy decides.
     */
    public String resolve(Long tenantId, Long appId, String distinctId, String userId) {
        if (distinctId != null) {
            String unified = aliases(tenantId, appId).get(distinctId);
            if (unified != null) return unified;
            if (appId != null && appId != 0L) {
                String u0 = aliases(tenantId, 0L).get(distinctId);
                if (u0 != null) return u0;
            }
        }
        String s = strategy(tenantId);
        return switch (s) {
            case "USER_FIRST" -> (userId != null && !userId.isBlank()) ? userId
                    : (distinctId != null ? distinctId : userId);
            default -> distinctId;   // DISTINCT_FIRST / EXPLICIT keep the SDK distinctId
        };
    }

    /** Record an explicit distinctId -> userId binding (idempotent upsert). */
    @Transactional
    public void identify(Long tenantId, Long appId, String distinctId, String userId) {
        if (distinctId == null || distinctId.isBlank() || userId == null || userId.isBlank()) {
            throw new BusinessException("distinctId 与 userId 均必填");
        }
        long aid = appId == null ? 0L : appId;
        UserIdentityAlias a = aliasRepo
                .findByTenantIdAndAppIdAndDistinctId(tenantId, aid, distinctId)
                .orElseGet(() -> {
                    UserIdentityAlias n = new UserIdentityAlias();
                    n.setTenantId(tenantId);
                    n.setAppId(aid);
                    n.setDistinctId(distinctId);
                    return n;
                });
        a.setUnifiedId(userId);
        a.setSource("identify");
        a.setMergedAt(LocalDateTime.now());
        aliasRepo.save(a);
        invalidate(tenantId, aid);
    }

    public void invalidate(Long tenantId, Long appId) {
        if (tenantId == null) {
            aliasCache.clear();
            strategyCache.clear();
            return;
        }
        aliasCache.remove(tenantId + "|" + appId);
        strategyCache.remove(tenantId);
    }

    // ---------- management ----------

    public Map<String, Object> aliasPage(Long tenantId, Long appId, int page, int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "id"));
        Page<UserIdentityAlias> p = (appId == null)
                ? aliasRepo.findByTenantId(tenantId, pr)
                : aliasRepo.findByTenantIdAndAppId(tenantId, appId, pr);
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("rows", p.getContent());
        m.put("total", p.getTotalElements());
        m.put("page", page);
        m.put("size", size);
        return m;
    }

    public List<IdentityRule> rules(Long tenantId) {
        return ruleRepo.findByTenantId(tenantId);
    }

    @Transactional
    public IdentityRule saveRule(IdentityRule r, Long tenantId) {
        r.setTenantId(tenantId);
        if (r.getAppId() == null) r.setAppId(0L);
        if (r.getStrategy() == null || r.getStrategy().isBlank()) r.setStrategy("DISTINCT_FIRST");
        IdentityRule saved = ruleRepo.save(r);
        strategyCache.remove(tenantId);
        return saved;
    }

    @Transactional
    public void deleteRule(Long id, Long tenantId) {
        ruleRepo.deleteById(id);
        strategyCache.remove(tenantId);
    }
}
