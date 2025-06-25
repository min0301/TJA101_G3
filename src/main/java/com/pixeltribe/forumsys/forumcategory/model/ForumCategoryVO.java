package com.pixeltribe.forumsys.forumcategory.model;

import java.sql.Date;
import java.util.Set;

import com.forum.model.ForumVO;
import jakarta.persistence.*;

@Entity
@Table (name = "FORUM_CATEGORY")
public class ForumCategoryVO {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "CAT_NO", updatable = false)
	private Integer catNo;
	
	@Column(name = "CAT_NAME")
	private String catName;
	
	@Column(name = "CAT_DES")
	private String catDes;
	
	@Column(name = "CAT_DATE", updatable = false)
	private Date catDate;
	
	@OneToMany(mappedBy = "catNo")
	@OrderBy("forNo")
	private Set<ForumVO> forums;




	public ForumCategoryVO() {
		super();
	}
	
	public ForumCategoryVO(Integer catNo, String catName, String catDes, Date catDate) {
		super();
		this.catNo = catNo;
		this.catName = catName;
		this.catDes = catDes;
		this.catDate = catDate;
	}

	public Integer getCatNo() {
		return catNo;
	}
	public void setCatNo(Integer catNo) {
		this.catNo = catNo;
	}
	public String getCatName() {
		return catName;
	}
	public void setCatName(String catName) {
		this.catName = catName;
	}
	public String getCatDes() {
		return catDes;
	}
	public void setCatDes(String catDes) {
		this.catDes = catDes;
	}
	public Date getCatDate() {
		return catDate;
	}
	public void setCatDate(Date catDate) {
		this.catDate = catDate;
	}

}
