package com.shopkun.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shopkun.entity.Category;
import com.shopkun.entity.Product;

public interface ProductDao extends JpaRepository<Product, Long> {

	@Query("from Products p where p.promotion = true and p.quantity <> 0")
	Page<Product> findAllByPromotion(Pageable page);

	@Query("from Products p where p.id <> ?1 and p.category = ?2 and p.quantity <> 0 ORDER BY RAND() LIMIT 19")
	List<Product> findAllSimilar(Long id, Category category);

	@Query("from Products p where p.quantity <> 0 ORDER BY RAND()")
	List<Product> findAllShuffle();

	@Query("from Products p where p.category = ?1 and p.quantity <> 0 ORDER BY p.createDate desc")
	Page<Product> findAllByCategory(Category category, Pageable page);

	@Query("from Products p where p.category = ?1 and p.quantity <> 0 ORDER BY p.createDate desc")
	List<Product> findAllByCategory(Category category);

	@Query("from Products p where p.name like ?1 and p.quantity <> 0 ORDER BY p.createDate desc")
	List<Product> findAllByName(String name);

	@Query("from Products p where p.name like ?1 order by p.id desc")
	List<Product> findAllByNameAdmin(String query);

	// Lọc có phân trang
	@Query("from Products p where (?1 is null or p.name like ?1) and p.promotion = true")
	Page<Product> findAllByPromotion(String query, Pageable page);

	@Query("from Products p where (?1 is null or p.name like ?1) and p.promotion = false")
	Page<Product> findAllByNotPromotion(String query, Pageable page);

	@Query("from Products p where (?1 is null or p.name like ?1) and p.quantity <> 0 ")
	Page<Product> findAllByStocking(String query, Pageable page);

	@Query("from Products p where (?1 is null or p.name like ?1) and p.quantity = 0")
	Page<Product> findAllByOutStock(String query, Pageable page);

	@Query("from Products p where (?1 is null or p.name like ?1) and p.category = ?2")
	Page<Product> findAllByCategoryId(String query, Category category, Pageable page);

	// Lọc và tìm kiếm
	@Query("from Products p where (?1 is null or p.name like ?1) and p.promotion = true order by p.id desc")
	List<Product> filterByPromotion(String query);

	@Query("from Products p where (?1 is null or p.name like ?1) and p.promotion = false order by p.id desc")
	List<Product> filterByNotPromotion(String query);

	@Query("from Products p where (?1 is null or p.name like ?1) and p.quantity <> 0 order by p.id desc")
	List<Product> filterByStocking(String query);

	@Query("from Products p where (?1 is null or p.name like ?1) and p.quantity = 0 order by p.id desc")
	List<Product> filterByOutStock(String query);

	@Query("from Products p where (?1 is null or p.name like ?1) and p.category.id = ?2")
	List<Product> filterByCategory(String query, Long categoryId);

	@Query("from Products p where (?1 is null or p.id = ?1) and p.promotion = true")
	Product filterByIdPromotion(Long id);

	@Query("from Products p where (?1 is null or p.id = ?1) and p.promotion = false")
	Product filterByIdNotPromotion(Long id);

	@Query("from Products p where (?1 is null or p.id = ?1) and p.quantity <> 0")
	Product filterByIdStocking(Long id);

	@Query("from Products p where (?1 is null or p.id = ?1) and p.quantity = 0")
	Product filterByIdOutStock(Long id);

	@Query("from Products p where (?1 is null or p.id = ?1) and p.category.id = ?2")
	Product filterByCategory(Long id, Long categoryId);
}
