package com.shopkun.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shopkun.entity.Category;

public interface CategoryDao extends JpaRepository<Category, Long> {

	@Query("from Category c where c.name like ?1 order by c.id desc")
	List<Category> findAllByName(String query);
}
