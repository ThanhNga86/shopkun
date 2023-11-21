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

@Entity(name = "Products")
public class Product implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;
	private String name;
	@Column(columnDefinition = "varbinary(10000)")
	private List<String> images;
	private double price;
	private int discount;
	private int quantity;
	@Column(columnDefinition = "varbinary(10000)")
	private List<String> describes;
	private boolean promotion = false;
	private Date createDate = new Date();
	@ManyToOne
	@JoinColumn(name = "categoryId")
	private Category category;
	@OneToMany(mappedBy = "product")
	private List<OrderDetail> listOrderDetail;
	@OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
	private List<Review> listReview;
	
	public Product() {
	}

	public Product(String name, List<String> images, double price, int discount, int quantity, List<String> describes,
			boolean promotion, Date createDate, Category category) {
		this.name = name;
		this.images = images;
		this.price = price;
		this.discount = discount;
		this.quantity = quantity;
		this.describes = describes;
		this.promotion = promotion;
		this.createDate = createDate;
		this.category = category;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<String> getImages() {
		return images;
	}

	public void setImages(List<String> images) {
		this.images = images;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public int getDiscount() {
		return discount;
	}

	public void setDiscount(int discount) {
		this.discount = discount;
	}

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	public List<String> getDescribes() {
		return describes;
	}

	public void setDescribes(List<String> describes) {
		this.describes = describes;
	}

	public boolean isPromotion() {
		return promotion;
	}

	public void setPromotion(boolean promotion) {
		this.promotion = promotion;
	}

	public Date getCreateDate() {
		return createDate;
	}

	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	public List<OrderDetail> getListOrderDetail() {
		return listOrderDetail;
	}

	public void setListOrderDetail(List<OrderDetail> listOrderDetail) {
		this.listOrderDetail = listOrderDetail;
	}

	public List<Review> getListReview() {
		return listReview;
	}

	public void setListReview(List<Review> listReview) {
		this.listReview = listReview;
	}

}
