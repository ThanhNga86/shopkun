package com.shopkun.entity;

import java.io.Serializable;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class SellingProducts implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	private Long id;
	private Product product;
	private Double price;
	private Long sold;

	public SellingProducts() {
	}

	public SellingProducts(Long id, Product product, Double price, Long sold) {
		this.id = id;
		this.product = product;
		this.price = price;
		this.sold = sold;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Product getProduct() {
		return product;
	}

	public void setProduct(Product product) {
		this.product = product;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public Long getSold() {
		return sold;
	}

	public void setSold(Long sold) {
		this.sold = sold;
	}

}
