package com.shopkun.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import com.shopkun.dao.ShoppingCartDao;
import com.shopkun.entity.Product;

@Service("cart")
@SessionScope
public class ShoppingCartService implements ShoppingCartDao {
	private List<Product> list = new ArrayList<>();

	@Override
	public void add(Product product) {
		int flag = 0;
		for (int i = 0; i < list.size(); i++) {
			if (product.getId() == list.get(i).getId()) {
				if (list.get(i).getQuantity() < product.getQuantity()) {
					list.get(i).setQuantity(list.get(i).getQuantity() + 1);
					list.set(i, list.get(i));
				}
				flag = 1;
				break;
			}
		}
		if (flag == 0) {
			product.setQuantity(1);
			list.add(product);
		}
	}

	@Override
	public void update(Product product, int quantity) {
		int flag = 0;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getId() == product.getId()) {
				int checkQtity = list.get(i).getQuantity() + quantity;
				if (checkQtity < product.getQuantity()) {
					list.get(i).setQuantity(list.get(i).getQuantity() + quantity);
					list.set(i, list.get(i));
				} else {
					list.get(i).setQuantity(product.getQuantity());
					list.set(i, list.get(i));
				}
				flag = 1;
				break;
			}
		}

		if (flag == 0) {
			if (quantity <= product.getQuantity()) {
				product.setQuantity(quantity);
				list.add(product);
			}
		}
	}

	@Override
	public void updateInShoppingCart(Product product, int quantity) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getId() == product.getId()) {
				if (quantity <= product.getQuantity()) {
					list.get(i).setQuantity(quantity);
					list.set(i, list.get(i));
				} else {
					list.get(i).setQuantity(product.getQuantity());
					list.set(i, list.get(i));
				}
				break;
			}
		}
	}

	@Override
	public void remove(Long id) {
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getId() == id) {
				list.remove(i);
				break;
			}
		}
	}

	@Override
	public Product findById(Long id) {
		Product product = null;
		for (int i = 0; i < list.size(); i++) {
			if (list.get(i).getId() == id) {
				product = list.get(i);
				break;
			}
		}
		return product;
	}

	@Override
	public List<Product> findAll() {
		return list;
	}

	@Override
	public void clear() {
		list.clear();
	}

	@Override
	public long getTotal() {
		return list.size();
	}
}
