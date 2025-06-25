package com.pixeltribe.newssys.vo;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "new_content_classification")
public class NewContentClassification {
    @Id
    @Column(name = "NCC_NO", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NCAT_NO", nullable = false)
    private NewsCategory ncatNo;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NEWS_NO", nullable = false)
    private News newsNo;

}