package com.shopkun.dao;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopkun.entity.Product;
import com.shopkun.entity.Review;

public interface ReviewDao extends JpaRepository<Review, Long> {

	@Query("from Reviews r where r.product = ?1 order by r.createDate desc")
	Page<Review> findAllByProductId(Product product, Pageable page);

	@Query("from Reviews r where r.product = ?1 order by r.createDate desc")
	List<Review> findAllByProduct(Product product);

	// tìm kiếm và lọc sản phẩm
	@Query("from Reviews r where r.rate = ?1")
	Page<Review> findAllByStar(Double star, Pageable page);

	@Query("from Reviews r where r.comment like :query or r.account.fullName like :query "
			+ "order by r.createDate desc")
	List<Review> findAllByComment(@Param("query") String comment);

	@Query("from Reviews r where (r.id = :id or r.product.id = :id) and r.rate = :rate " + "order by r.createDate desc")
	List<Review> findReviewById(@Param("id") Long reiviewId, @Param("rate") Double star);
	
	@Query("from Reviews r where r.id = :id or r.product.id = :id")
	List<Review> findReviewById(@Param("id") Long reiviewId);

	@Query("from Reviews r where (r.comment like :query or r.account.fullName like :query) and r.rate = :rate order by r.createDate desc")
	List<Review> findAllByComment(@Param("query") String comment, @Param("rate") Double rate);
}
