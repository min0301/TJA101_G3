package com.pixeltribe.forumsys.forumpost.model;

import com.fasterxml.jackson.annotation.JsonBackReference; // 確保導入
import com.fasterxml.jackson.annotation.JsonProperty; // 確保導入
import com.pixeltribe.forumsys.entity.*;
import com.pixeltribe.forumsys.forumtag.model.ForumTag;
import org.hibernate.annotations.ColumnDefault;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pixeltribe.forumsys.forum.model.Forum;
import com.pixeltribe.membersys.member.model.Member;

import com.pixeltribe.forumsys.message.model.ForumMes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

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
    @JsonBackReference  // 保持 JsonBackReference，它與 Forum.java 的 JsonManagedReference 配對
    @NotEmpty(message="討論區編號: 請勿空白")
    private Forum forNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEM_NO")
    @JsonBackReference
    private Member memNo;

    // **確保這個方法存在** nick new
    @JsonProperty("forumName")
    public String getForumName() {
        if (this.forNo != null) {
            return this.forNo.getForName(); // 呼叫 Forum Entity 的 getForName()
        }
        return "未知討論區";
    }

    // **確保這個方法存在** nick new
    @JsonProperty("memberName")
    public String getMemberName() {
        if (this.memNo != null) {
            return this.memNo.getMemName(); // 呼叫 Member Entity 的 getMemName()
        }
        return "匿名會員";
    }
    // **確保這個方法存在 (供 DTO 獲取 ID)** nick new
    @JsonProperty("forNoId")
    public Integer getForNoId() {
        if (this.forNo != null) {
            return this.forNo.getId();
        }
        return null;
    }

    // **確保這個方法存在 (供 DTO 獲取 ID)** nick new
    @JsonProperty("memNoId")
    public Integer getMemNoId() {
        if (this.memNo != null) {
            return this.memNo.getId();
        }
        return null;
    }

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FTAG_NO")
    private ForumTag ftagNo;

    @Size(max = 50)
    @Column(name = "POST_TITLE", length = 50)
    @Pattern(regexp = "^[一-龥-a-zA-Z0-9_]{2,10}$", message = "文章標題: 只能是中、英文字母、數字和_ , 且長度必需在2到10之間")
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

//    @Lob
//    @Column(name = "POST_COVER_IMAGE")
//    private byte[] postCoverImage; // **確保這個欄位存在** nick new

    @JsonIgnore
    @OneToMany(mappedBy = "postNo")
    private Set<ArticleReport> articleReports = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "postNo")
    private Set<ForumImage> forumImages = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "postNo")
    private Set<ForumMes> forumMes = new LinkedHashSet<>();

    @JsonIgnore
    @OneToMany(mappedBy = "postNo")
    private Set<PostCollect> postCollects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "postNo")
    @JsonIgnore
    private Set<PostLike> postLikes = new LinkedHashSet<>();

}