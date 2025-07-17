package com.pixeltribe.forumsys.forumpost.model;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.forumsys.forumimage.model.ForumImage;
import com.pixeltribe.forumsys.forumtag.model.ForumTag;
import com.pixeltribe.forumsys.message.model.ForumMes;
import com.pixeltribe.forumsys.postcollect.model.PostCollect;
import com.pixeltribe.forumsys.postlike.model.PostLike;
import com.pixeltribe.membersys.member.model.Member;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "forum_post")
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "POST_NO", nullable = false)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FOR_NO")
    // 移除 @JsonBackReference，交由 DTO 處理關聯資料
    // @NotEmpty(message = "討論區編號: 請勿空白") // Entity 層的驗證通常較寬鬆，或交由 DTO 處理
    private Forum forNo; // 變數名稱 `forNo` 不可變，因為對應資料庫欄位或 JPA 關聯映射

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    // 移除 @JsonBackReference，交由 DTO 處理關聯資料
    private Member memNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FTAG_NO")
    private ForumTag ftagNo;

    @Size(max = 50)
    @Column(name = "POST_TITLE", length = 50)
    private String postTitle;

    @Size(max = 5000)
    @NotNull
    @Column(name = "POST_CON", nullable = false, length = 5000)
    private String postCon;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "POST_CRDATE")
    private Instant postCrdate;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "POST_UPDATE")
    private Instant postUpdate;

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "POST_PIN", nullable = false)
    private Character postPin;
    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "POST_STATUS", nullable = false)
    private Character postStatus;

    @Column(name = "MES_NUMBERS")
    private Integer mesNumbers;

    @Column(name = "POST_LIKE_COUNT")
    private Integer postLikeCount;

    @Column(name = "POST_LIKE_DLC")
    private Integer postLikeDlc;

    // 將 byte[] 替換為 String，用於儲存圖片 URL
    @Column(name = "POSTIMAGE_URL") //與資料庫名稱相同
    private String postImageUrl; // 新增圖片 URL 欄位，變數名稱 `postCoverImageUrl` 可變，但建議與資料庫欄位名一致

//    @JsonIgnore
//    @OneToMany(mappedBy = "postNo")
//    private Set<ArticleReport> articleReports = new LinkedHashSet<>(); // 變數名稱 `articleReports` 可變

    @JsonIgnore
    @OneToMany(mappedBy = "postNo")
    private Set<ForumImage> forumImages = new LinkedHashSet<>(); // 變數名稱 `forumImages` 可變

    @JsonIgnore
    @OneToMany(mappedBy = "postNo")
    private Set<ForumMes> forumMes = new LinkedHashSet<>(); // 變數名稱 `forumMes` 可變

    @JsonIgnore
    @OneToMany(mappedBy = "postNo")
    private Set<PostCollect> postCollects = new LinkedHashSet<>(); // 變數名稱 `postCollects` 可變

    @JsonIgnore
    @OneToMany(mappedBy = "postNo")
    private Set<PostLike> postLikes = new LinkedHashSet<>(); // 變數名稱 `postLikes` 可變

}