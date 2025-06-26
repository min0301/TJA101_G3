package com.pixeltribe.membersys.vo;

import com.pixeltribe.forumsys.forumVO.*;
import com.pixeltribe.newssys.vo.News;
import com.pixeltribe.newssys.vo.NewsComReport;
import com.pixeltribe.newssys.vo.NewsComment;
import com.pixeltribe.newssys.vo.NewsLike;
import com.pixeltribe.shopsys.vo.CouponWallet;
import com.pixeltribe.shopsys.vo.FavoriteProduct;
import com.pixeltribe.shopsys.vo.Order;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "member")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEM_NO", nullable = false)
    private Integer id;

    @Size(max = 50)
    @NotNull
    @Column(name = "MEM_NAME", nullable = false, length = 50)
    private String memName;

    @Size(max = 50)
    @NotNull
    @Column(name = "MEM_NICK_NAME", nullable = false, length = 50)
    private String memNickName;

    @Size(max = 50)
    @NotNull
    @Column(name = "MEM_ACCOUNT", nullable = false, length = 50)
    private String memAccount;

    @Size(max = 50)
    @NotNull
    @Column(name = "MEM_PASSWORD", nullable = false, length = 50)
    private String memPassword;

    @Size(max = 100)
    @NotNull
    @Column(name = "MEM_EMAIL", nullable = false, length = 100)
    private String memEmail;

    @Column(name = "MEM_ICON_DATA")
    private byte[] memIconData;

    @Size(max = 100)
    @NotNull
    @Column(name = "MEM_ADDR", nullable = false, length = 100)
    private String memAddr;

    @Size(max = 50)
    @NotNull
    @Column(name = "MEM_PHONE", nullable = false, length = 50)
    private String memPhone;

    @NotNull
    @Column(name = "MEM_BIRTHDAY", nullable = false)
    private LocalDate memBirthday;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "MEM_CREATE")
    private Instant memCreate;

    @Column(name = "MEM_UPDATE")
    private Instant memUpdate;

    @NotNull
    @ColumnDefault("'1'")
    @Column(name = "MEM_STATUS", nullable = false)
    private Character memStatus;

    @Size(max = 50)
    @Column(name = "MEM_TOKEN", length = 50)
    private String memToken;

    @Size(max = 50)
    @Column(name = "MEM_EMAIL_AUTH", length = 50)
    private String memEmailAuth;

    @Column(name = "SEND_AUTH_Email_TIME")
    private Instant sendAuthEmailTime;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "CAN_POST", nullable = false)
    private Boolean canPost = false;

    @NotNull
    @ColumnDefault("1")
    @Column(name = "CAN_COMMENT", nullable = false)
    private Boolean canComment = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "CAN_USED_PRO", nullable = false)
    private Boolean canUsedPro = false;

    @NotNull
    @ColumnDefault("0")
    @Column(name = "PHONE_AUTHED", nullable = false)
    private Boolean phoneAuthed = false;

    @Size(max = 20)
    @Column(name = "AUTH_PROVIDER", length = 20)
    private String authProvider;

    @Size(max = 100)
    @Column(name = "PROVIDER_UID", length = 100)
    private String providerUid;

    @Size(max = 20)
    @NotNull
    @ColumnDefault("'ROLE_USER'")
    @Column(name = "ROLE", nullable = false, length = 20)
    private String role;

    @ColumnDefault("0")
    @Column(name = "POINT")
    private Integer point;

    @OneToMany(mappedBy = "reporter")
    private Set<ArticleComReport> articleComReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "reporter")
    private Set<ArticleReport> articleReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<CouponWallet> couponWallets = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<FavoriteProduct> favoriteProducts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumChatMessage> forumChatMessages = new LinkedHashSet<>();

    @OneToMany(mappedBy = "reporter")
    private Set<ForumChatReport> forumChatReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumCollect> forumCollects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumLike> forumLikes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumMes> forumMes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumMesLike> forumMesLikes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumPost> forumPosts = new LinkedHashSet<>();

    @OneToMany(mappedBy = "member1")
    private Set<FriendList> friendLists = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<MemberLoginLog> memberLoginLogs = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<News> news = new LinkedHashSet<>();

    @OneToMany(mappedBy = "reporter")
    private Set<NewsComReport> newsComReports = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<NewsComment> newsComments = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<NewsLike> newsLikes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<Order> orders = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<PostCollect> postCollects = new LinkedHashSet<>();

    @OneToMany(mappedBy = "member1")
    private Set<PrivateChatroom> privateChatrooms = new LinkedHashSet<>();

    @OneToMany(mappedBy = "senderNo")
    private Set<PrivateMessage> privateMessages = new LinkedHashSet<>();

}