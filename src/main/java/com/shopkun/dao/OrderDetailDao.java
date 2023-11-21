package com.shopkun.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.shopkun.entity.Account;
import com.shopkun.entity.Category;
import com.shopkun.entity.Order;
import com.shopkun.entity.OrderDetail;
import com.shopkun.entity.SellingProducts;

public interface OrderDetailDao extends JpaRepository<OrderDetail, Long> {

	@Query("select o.product.category from OrderDetails o " + "group by o.product.category "
			+ "order by count(o.product.category) desc")
	Page<Category> findAllByCategory(Pageable page);

	List<OrderDetail> findByOrder(Order order);

	@Query("select o.order from OrderDetails o where o.order.account = ?1 and o.order.id = ?2 "
			+ "order by o.order.createDate desc")
	List<Order> findAllById(Account account, int id);
	
	@Query("select o.order from OrderDetails o where o.order.account = ?1 and o.product.name like ?2 "
			+ "order by o.order.createDate desc")
	List<Order> findAllByName(Account account, String query);
	
	// thống kê doanh thu
	@Query("select sum(o.price*(100-o.product.discount)/100 * o.quantity) from OrderDetails o "
			+ "where DAY(o.order.createDate) = DAY(current_date) and o.order.status = ?1")
	Long revenueToday(String status);
	
	@Query("select sum(o.quantity) from OrderDetails o "
			+ "where DAY(o.order.createDate) = DAY(current_date) and o.order.status = ?1")
	Long soldToday(String status);
	
	@Query("select sum(o.price*(100-o.product.discount)/100 * o.quantity) from OrderDetails o "
			+ "where WEEK(o.order.createDate) = WEEK(current_date) and o.order.status = ?1")
	Long revenueWeek(String status);
	
	@Query("select sum(o.quantity) from OrderDetails o "
			+ "where WEEK(o.order.createDate) = WEEK(current_date) and o.order.status = ?1")
	Long soldWeek(String status);
	
	@Query("select sum(o.price*(100-o.product.discount)/100 * o.quantity) from OrderDetails o "
			+ "where MONTH(o.order.createDate) = MONTH(current_date) and o.order.status = ?1")
	Long revenueMonth(String status);
	
	@Query("select sum(o.quantity) from OrderDetails o "
			+ "where MONTH(o.order.createDate) = MONTH(current_date) and o.order.status = ?1")
	Long soldMonth(String status);
	
	@Query("select sum(o.price*(100-o.product.discount)/100 * o.quantity) from OrderDetails o "
			+ "where o.order.status = ?1")
	Long totalRevenue(String status);
	
	@Query("select sum(o.quantity) from OrderDetails o "
			+ "where o.order.status = ?1")
	Long totalSold(String status);
	
	@Query("select new SellingProducts(o.product.id, o.product, o.price, sum(o.quantity)) from OrderDetails o "
			+ "where o.order.status = ?1 group by o.product.id, o.product, o.price order by sum(o.quantity) desc")
	Page<SellingProducts> findTopSellingProducts(String status ,Pageable page);
}
