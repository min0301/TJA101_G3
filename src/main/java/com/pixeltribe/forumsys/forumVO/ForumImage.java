package com.pixeltribe.forumsys.forumVO;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Getter
@Setter
@Entity
@Table(name = "forum_image")
public class ForumImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "IMG_NO", nullable = false)
    private Integer id;

    @Column(name = "IMG_DATA")
    private byte[] imgData;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "POST_NO")
    private ForumPost postNo;

    @Size(max = 100)
    @NotNull
    @Column(name = "IMG_TYPE", nullable = false, length = 100)
    private String imgType;

}