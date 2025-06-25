package com.pixeltribe.forumsys.forum.model;

	import java.io.Serializable;
	import java.sql.Date;
	import java.util.LinkedHashSet;
	import java.util.Set;

	import com.pixeltribe.forumsys.forumVO.ForumChatMessage;
	import com.pixeltribe.forumsys.forumVO.ForumCollect;
	import com.pixeltribe.forumsys.forumVO.ForumLike;
	import com.pixeltribe.forumsys.forumVO.ForumPost;
	import jakarta.persistence.*;
	import jakarta.validation.constraints.NotEmpty;

	import com.pixeltribe.forumsys.forumcategory.model.ForumCategoryVO;

@Entity
	@Table (name = "FORUM")
	public class ForumVO implements Serializable {
		

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		@Column(name = "FOR_NO", updatable = false)
		private Integer forNo;

		@Column(name = "FOR_NAME")
		@NotEmpty(message="討論區名稱: 請勿空白")
		private String forName;
		
		@ManyToOne
		@NotEmpty(message="討論區類別編號: 請勿空白")
		@JoinColumn(name = "CAT_NO" , referencedColumnName = "CAT_NO")
		private ForumCategoryVO forumCategoryVO;

		@Transient
		private Integer catNo;

		@Transient
		private String catName;


		@Column(name = "FOR_DES")
		private String forDes;
		@Column(name = "FOR_DATE", updatable = false)
		private Date forDate;
		@Column(name = "FOR_UPDATE")
		private Date  forUpdate;
		@Column(name = "FCHAT_STATUS")
		private Integer fchatStatus;

	@OneToMany(mappedBy = "forNo")
	private Set<ForumChatMessage> forumChatMessages = new LinkedHashSet<>();

	@OneToMany(mappedBy = "forNo")
	private Set<ForumCollect> forumCollects = new LinkedHashSet<>();

	@OneToMany(mappedBy = "forNo")
	private Set<ForumLike> forumLikes = new LinkedHashSet<>();

	@OneToMany(mappedBy = "forNo")
	private Set<ForumPost> forumPosts = new LinkedHashSet<>();



	public ForumVO() {
			super();
		}



		public ForumVO(Integer forNo, String forName, Integer catNo, String forDes, Date forDate, Date forUpdate,
				Integer fchatStatus) {
			super();
			this.forNo = forNo;
			this.forName = forName;
			this.catNo = catNo;
			this.forDes = forDes;
			this.forDate = forDate;
			this.forUpdate = forUpdate;
			this.fchatStatus = fchatStatus;
		}
		public Integer getForNo() {
			return forNo;
		}
		public void setForNo(Integer forNo) {
			this.forNo = forNo;
		}

		public String getForName() {
			return forName;
		}
		public void setForName(String forName) {
			this.forName = forName;
		}

		public Integer getCatNo() {
			return catNo;
		}
		public void setCatNo(Integer catNo) {
			this.catNo = catNo;
		}

		public String getForDes() {
			return forDes;
		}
		public void setForDes(String forDes) {
			this.forDes = forDes;
		}

		public Date getForDate() {
			return forDate;
		}
		public void setForDate(Date forDate) {
			this.forDate = forDate;
		}

		public Date getForUpdate() {
			return forUpdate;
		}
		public void setForUpdate(Date forUpdate) {
			this.forUpdate = forUpdate;
		}

		public Integer getFchatStatus() {
			return fchatStatus;
		}
		public void setFchatStatus(Integer fchatStatus) {
			this.fchatStatus = fchatStatus;
		}
		
		public String getCatName() {
			return catName;
		}
		public void setCatName(String catName) {
			this.catName = catName;
		}
		
		public ForumCategoryVO getForumCategoryVO() {
			return forumCategoryVO;
		}
		public void setForumCategoryVO(ForumCategoryVO forumCategoryVO) {
			this.forumCategoryVO = forumCategoryVO;
		}



	public Set<ForumPost> getForumPosts() {
		return forumPosts;
	}

	public void setForumPosts(Set<ForumPost> forumPosts) {
		this.forumPosts = forumPosts;
	}


	public Set<ForumLike> getForumLikes() {
		return forumLikes;
	}

	public void setForumLikes(Set<ForumLike> forumLikes) {
		this.forumLikes = forumLikes;
	}


	public Set<ForumCollect> getForumCollects() {
		return forumCollects;
	}

	public void setForumCollects(Set<ForumCollect> forumCollects) {
		this.forumCollects = forumCollects;
	}


	public Set<ForumChatMessage> getForumChatMessages() {
		return forumChatMessages;
	}

	public void setForumChatMessages(Set<ForumChatMessage> forumChatMessages) {
		this.forumChatMessages = forumChatMessages;
	}

	}
