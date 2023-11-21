package com.shopkun.dao;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopkun.entity.Account;
import com.shopkun.entity.Order;

public interface OrderDao extends JpaRepository<Order, Long> {

	@Query("from Orders o where o.account = ?1 order by o.createDate desc")
	List<Order> findAllByAccount(Account account);

	@Query("from Orders o where o.status = ?1")
	Page<Order> findAllByStatus(String status, Pageable page);

	@Query("from Orders o where o.account = ?1 and o.status = ?2 order by o.createDate desc")
	List<Order> findAllByStatus(Account account, String status);

	@Query("from Orders o where o.id = ?1 and o.account = ?2")
	Optional<Order> findOrderById(Long orderId, Account account);

	// tìm kiếm và lọc đơn hàng
	@Query("from Orders o where o.fullName like :query or o.address like :query or o.phone like :query "
			+ "order by o.createDate desc")
	List<Order> findAllOrders(@Param("query") String query);
	
	@Query("from Orders o where (o.fullName like :query or o.address like :query or o.phone like :query) "
			+ "and o.status = :status order by o.createDate desc")
	List<Order> findAllOrdersByStatus(@Param("query") String query, @Param("status") String filter);

	@Query("from Orders o where o.id = ?1 and o.status = ?2")
	Order findStatusById(Long orderId, String filter);
}
