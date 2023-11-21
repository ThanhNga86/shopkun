package com.shopkun.dao;

import java.util.List;

import com.shopkun.entity.Product;

public interface ShoppingCartDao {

	public void add(Product product);

	public void update(Product product, int quantity);
	
	public void updateInShoppingCart(Product product, int quantity);

	public void remove(Long id);
	
	public Product findById(Long id);

	public List<Product> findAll();
	
	public void clear();
	
	public long getTotal();
}
