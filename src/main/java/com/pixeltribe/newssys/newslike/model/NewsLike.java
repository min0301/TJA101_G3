package com.pixeltribe.newssys.newslike.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pixeltribe.membersys.member.model.Member;
import com.pixeltribe.newssys.newscomment.model.NewsComment;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "news_like")
public class NewsLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NLIKE_NO", nullable = false)
    private Integer id;

    /* 1:正常 2:讚 3:倒讚 */
    @NotNull
    @Column(name = "NLIKE_STATUS", nullable = false)
    private Character nlikeStatus;

    /*操作者*/
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "MEM_NO", nullable = false)
    @JsonIgnore
    private Member memNo;


    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NLIKE_UPDATE", nullable = false, insertable = false, updatable = false)
    private Instant nlikeUpdate;


    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NLIKE_CRDATE", nullable = false, insertable = false, updatable = false)
    private Instant nlikeCrdate;

    /*對應留言*/
    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "NCOM_NO", nullable = false)
    @JsonIgnore
    private NewsComment ncomNo;

}