package com.shopkun.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class ReviewFeedback implements Serializable{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	@Column(columnDefinition = "varchar(10000)")
	private String comment;
	@Column(columnDefinition = "varbinary(10000)")
	private List<String> images;
	private Date createDate = new Date();
	@ManyToOne
	@JoinColumn(name = "username")
	private Account account;
	@ManyToOne
	@JoinColumn(name = "reviewId")
	private Review review;
	private String respondent;
	
	public ReviewFeedback() {
	}

	public ReviewFeedback(String comment, List<String> images, Date createDate, Account account, Review review,
			String respondent) {
		this.comment = comment;
		this.images = images;
		this.createDate = createDate;
		this.account = account;
		this.review = review;
		this.respondent = respondent;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Account getAccount() {
		return account;
	}

	public void setAccount(Account account) {
		this.account = account;
	}

	public Review getReview() {
		return review;
	}

	public void setReview(Review review) {
		this.review = review;
	}

	public String getRespondent() {
		return respondent;
	}

	public void setRespondent(String respondent) {
		this.respondent = respondent;
	}

}
