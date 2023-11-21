package com.shopkun.dao;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.shopkun.entity.Account;

public interface AccountDao extends JpaRepository<Account, String>{

	@Query("from Accounts a where a.username like :query or a.fullName like :query or a.email like :query or a.phone like :query")
	List<Account> findAllAccount(@Param("query") String query);
	
	@Query("from Accounts a where a <> ?1")
	Page<Account> findAllOtherAdmin(Account account, Pageable page);
}
