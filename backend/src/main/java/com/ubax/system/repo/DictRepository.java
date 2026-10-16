package com.ubax.system.repo;

import com.ubax.system.domain.Dict;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DictRepository extends JpaRepository<Dict, Long> {
    List<Dict> findByDictTypeOrderBySortNoAscIdAsc(String dictType);

    @Query("SELECT DISTINCT d.dictType FROM Dict d ORDER BY d.dictType")
    List<String> distinctTypes();

    boolean existsByDictTypeAndDictValue(String dictType, String dictValue);
}
