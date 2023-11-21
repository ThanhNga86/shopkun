package com.shopkun.entity;

import java.io.Serializable;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity(name = "Accounts")
public class Account implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	private String username;
	private String password;
	private String fullName;
	private String address;
	private String email;
	private String phone;
	private boolean activated = true;
	private String role;
	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
	private List<Order> listOrder;
	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
	private List<Review> listReview;
	@OneToMany(mappedBy = "account", cascade = CascadeType.ALL)
	private List<ReviewFeedback> listReviewDetail;
	
	public Account() {
	}

	public Account(String username, String password, String fullName, String address, String email, String phone,
			boolean activated, String role) {
		this.username = username;
		this.password = password;
		this.fullName = fullName;
		this.address = address;
		this.email = email;
		this.phone = phone;
		this.activated = activated;
		this.role = role;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public boolean isActivated() {
		return activated;
	}

	public void setActivated(boolean activated) {
		this.activated = activated;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public List<Order> getListOrder() {
		return listOrder;
	}

	public void setListOrder(List<Order> listOrder) {
		this.listOrder = listOrder;
	}

	public List<Review> getListReview() {
		return listReview;
	}

	public void setListReview(List<Review> listReview) {
		this.listReview = listReview;
	}

	public List<ReviewFeedback> getListReviewDetail() {
		return listReviewDetail;
	}

	public void setListReviewDetail(List<ReviewFeedback> listReviewDetail) {
		this.listReviewDetail = listReviewDetail;
	}

}
