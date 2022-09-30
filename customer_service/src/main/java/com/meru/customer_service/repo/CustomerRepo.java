package com.meru.customer_service.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.meru.customer_service.bean.Address;
import com.meru.customer_service.bean.Customer;

@Repository
public interface CustomerRepo extends JpaRepository<Customer, Integer>{

	Customer findAddressByCustomerId(int customerId);


}
