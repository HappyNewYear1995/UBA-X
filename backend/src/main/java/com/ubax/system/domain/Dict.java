package com.ubax.system.domain;

import jakarta.persistence.*;
import lombok.Data;

/** Data-dictionary entry: a label/value pair grouped under a dict type. */
@Data
@Entity
@Table(name = "uba_sys_dict", indexes = @Index(name = "idx_dict_type", columnList = "dict_type"))
public class Dict {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "dict_type", nullable = false, length = 60)
    private String dictType;

    @Column(name = "dict_label", nullable = false, length = 80)
    private String dictLabel;

    @Column(name = "dict_value", nullable = false, length = 120)
    private String dictValue;

    @Column(name = "sort_no")
    private Integer sortNo = 0;

    @Column(length = 16)
    private String status = "ENABLED";

    @Column(length = 200)
    private String remark;
}
