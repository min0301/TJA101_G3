package com.pixeltribe.forumsys.forumimage.model;

import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

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
    @JoinColumn(name = "POST_NO")
    private ForumPost postNo;

    @Size(max = 100)
    @NotNull
    @Column(name = "IMG_TYPE", nullable = false, length = 100)
    private String imgType;

}