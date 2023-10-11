package com.meru.customer_service.repo;

import com.meru.customer_service.bean.Address;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import com.meru.customer_service.bean.Customer;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CustomerRepo extends MongoRepository<Customer, Integer> {

	@Query("{emailId:  ?0}")
	Set<Address> findSetOfAddressByCustomerEmailId(String emailId);

	@Query("{emailId:  ?0}")
	Optional<Customer> findByEmailId(String emailId);

	@Query("{emailId:  ?0}")
	boolean deleteByEmailId(String emailId);
}
