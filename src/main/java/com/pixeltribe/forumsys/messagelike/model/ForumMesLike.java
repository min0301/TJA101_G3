package com.pixeltribe.forumsys.messagelike.model;


import com.pixeltribe.forumsys.message.model.ForumMes;
import com.pixeltribe.forumsys.shared.LikeStatus;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Data
@Entity

@Table(name = "forum_mes_like")
public class ForumMesLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MLIKE_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @Column(name = "FMLIKE_STATUS")
    @Enumerated(EnumType.STRING)
    private LikeStatus fmlikeStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "FMLIKE_UPDATE", insertable = false, updatable = false)
    private Instant fmlikeUpdate;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "FMLIKE_CRDATE", insertable = false, updatable = false)
    private Instant fmlikeCrdate;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MES_NO")
    private ForumMes mesNo;



}