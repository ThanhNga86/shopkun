package com.shopkun.entity;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

@Entity(name = "Reviews")
public class Review implements Serializable {
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(columnDefinition = "varchar(10000)")
	private String comment;
	@Column(columnDefinition = "varbinary(10000)")
	private List<String> images;
	private double rate;
	private Date createDate = new Date();
	@ManyToOne
	@JoinColumn(name = "username")
	private Account account;
	@ManyToOne
	@JoinColumn(name = "productId")
	private Product product;
	@OneToMany(mappedBy = "review", cascade = CascadeType.ALL)
	private List<ReviewFeedback> listReviewDetail;

	public Review() {
	}

	public Review(String comment, List<String> images, double rate, Date createDate, Account account, Product product) {
		this.comment = comment;
		this.images = images;
		this.rate = rate;
		this.createDate = createDate;
		this.account = account;
		this.product = product;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
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

	public double getRate() {
		return rate;
	}

	public void setRate(double rate) {
		this.rate = rate;
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

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public List<ReviewFeedback> getListReviewDetail() {
		return listReviewDetail;
	}

	public void setListReviewDetail(List<ReviewFeedback> listReviewDetail) {
		this.listReviewDetail = listReviewDetail;
	}

}
