package com.pixeltribe.forumsys.forumVO;

import com.pixeltribe.forumsys.forummes.model.ForumMes;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import com.pixeltribe.membersys.member.model.Member;

import java.time.Instant;

@Getter
@Setter
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

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "MLIKE_UPDATE")
    private Instant mlikeUpdate;

    @Column(name = "FMLIKE_STATUS")
    private Character fmlikeStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "NMLIKE_CRDATE")
    private Instant nmlikeCrdate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MES_NO")
    private ForumMes mesNo;

}