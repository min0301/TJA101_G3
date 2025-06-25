package com.pixeltribe.forumsys.forumVO;

import com.pixeltribe.forumsys.forum.model.ForumVO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

}