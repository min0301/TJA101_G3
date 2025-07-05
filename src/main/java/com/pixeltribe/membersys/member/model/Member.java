package com.pixeltribe.membersys.member.model;

import java.time.Instant;
import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import org.hibernate.annotations.ColumnDefault;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.pixeltribe.forumsys.entity.ArticleComReport;
import com.pixeltribe.forumsys.entity.ArticleReport;
import com.pixeltribe.forumsys.entity.ForumChatMessage;
import com.pixeltribe.forumsys.entity.ForumChatReport;
import com.pixeltribe.forumsys.entity.ForumCollect;


import com.pixeltribe.forumsys.message.model.ForumMes;

import com.pixeltribe.forumsys.entity.PostLike;


import com.pixeltribe.forumsys.entity.ForumMesLike;
import com.pixeltribe.forumsys.forumpost.model.ForumPost;
import com.pixeltribe.forumsys.entity.PostCollect;
import com.pixeltribe.membersys.friendlist.model.FriendList;
import com.pixeltribe.membersys.memberloginlog.model.MemberLoginLog;
import com.pixeltribe.membersys.privatechatroom.model.PrivateChatroom;
import com.pixeltribe.membersys.privatemessage.model.PrivateMessage;
import com.pixeltribe.newssys.newscomment.model.NewsComment;
import com.pixeltribe.newssys.newscomreport.model.NewsComReport;
import com.pixeltribe.newssys.newslike.model.NewsLike;
import com.pixeltribe.shopsys.couponWallet.model.CouponWallet;
import com.pixeltribe.shopsys.favoriteProduct.model.FavoriteProduct;
import com.pixeltribe.shopsys.order.model.Order;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "MEMBER")
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
    private Set<PostLike> postLikes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumMes> forumMes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumMesLike> forumMesLikes = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<ForumPost> forumPosts = new LinkedHashSet<>();
    
    @JsonManagedReference(value = "member1-friendlist")
    @OneToMany(mappedBy = "member1")
    private Set<FriendList> friendLists1 = new LinkedHashSet<>();
    
    @JsonManagedReference(value = "member2-friendlist")
    @OneToMany(mappedBy = "member2")
    private Set<FriendList> friendLists2 = new LinkedHashSet<>();

    @OneToMany(mappedBy = "memNo")
    private Set<MemberLoginLog> memberLoginLogs = new LinkedHashSet<>();

//    @OneToMany(mappedBy = "memNo")
//    private Set<News> news = new LinkedHashSet<>();

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
    
    @JsonManagedReference(value = "member1-privatechatroom")
    @OneToMany(mappedBy = "member1")
    private Set<PrivateChatroom> privateChatrooms1 = new LinkedHashSet<>();
    
    @JsonManagedReference(value = "member2-privatechatroom")
    @OneToMany(mappedBy = "member2")
    private Set<PrivateChatroom> privateChatrooms2 = new LinkedHashSet<>();
    
    @JsonManagedReference
    @OneToMany(mappedBy = "senderNo")
    private Set<PrivateMessage> privateMessages = new LinkedHashSet<>();

}