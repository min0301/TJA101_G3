package com.pixeltribe.forumsys.forum.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.pixeltribe.forumsys.entity.ForumChatMessage;
import com.pixeltribe.forumsys.entity.ForumCollect;
import com.pixeltribe.forumsys.forumcategory.model.ForumCategory;
import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
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
@Table(name = "forum")
public class Forum {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "FOR_NO", nullable = false)
    private Integer id;

    @Size(max = 30)
    @NotEmpty(message="討論區名稱: 請勿空白")
    @Column(name = "FOR_NAME", nullable = false, length = 30)
    private String forName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CAT_NO")
    private ForumCategory catNo;

    @Size(max = 255)
    @Column(name = "FOR_IMG_URL")
    private String forImgUrl;

    public String CategoryName() {
        if (this.catNo != null) {
            return this.catNo.getCatName();
        }
        return null;
    }

    @Transient
    private Integer categoryId;

    @Size(max = 255)
    @NotEmpty(message="討論區描述: 請勿空白")
    @Column(name = "FOR_DES")
    private String forDes;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "FOR_DATE", insertable = false, updatable = false)
    private Instant forDate;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "FOR_UPDATE", insertable = false, updatable = false)
    private Instant forUpdate;

    @NotNull
    @ColumnDefault("'0'")
    @Column(name = "FOR_STATUS", nullable = false)
    private Character forStatus;



    @OneToMany(mappedBy = "forNo")
    @JsonIgnore
    private Set<ForumChatMessage> forumChatMessages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "forNo")
    @JsonIgnore
    private Set<ForumCollect> forumCollects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "forNo")
    @JsonIgnore
    private Set<ForumPost> forumPosts = new LinkedHashSet<>();



}