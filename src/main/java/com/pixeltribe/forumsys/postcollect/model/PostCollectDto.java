package com.pixeltribe.forumsys.postcollect.model;

import java.time.Instant;

public class PostCollectDto {
	
	private Integer id;
	private String postTitle;
	private Instant postUpdate;
	private Integer postNo;
	
	// Getter & Setter
	public PostCollectDto() {
		
	}
	

    public PostCollectDto(Integer id, String postTitle, Instant postUpdate) {
        this.id = id;
        this.postTitle = postTitle;
        this.postUpdate = postUpdate;
    }
    
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPostTitle() {
		return postTitle;
	}

	public void setPostTitle(String postTitle) {
		this.postTitle = postTitle;
	}

	public Instant getPostUpdate() {
		return postUpdate;
	}

	public void setPostUpdate(Instant postUpdate) {
		this.postUpdate = postUpdate;
	}

	public Integer getPostNo() {
		return postNo;
	}

	public void setPostNo(Integer postNo) {
		this.postNo = postNo;
	}
	
}
