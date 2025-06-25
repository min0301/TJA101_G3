package com.pixeltribe.forumsys.forumVO;

import com.pixeltribe.forumsys.forum.model.ForumVO;
import com.pixeltribe.membersys.vo.Member;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "forum_like")
public class ForumLike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FLIKE_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOR_NO")
    private ForumVO forNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "MEM_NO")
    private Member memNo;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "FLIKE_UPDATE")
    private Instant flikeUpdate;

    @Column(name = "FLIKE_STATUS")
    private Character flikeStatus;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "FLIKE_CRDATE")
    private Instant flikeCrdate;

}