package com.shoppix.customer_reactive_service.repo;

import com.shoppix.customer_reactive_service.entity.Address;
import com.shoppix.customer_reactive_service.entity.Customer;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface CustomerRepo extends ReactiveMongoRepository<Customer, Integer> {

	@Query("{'emailId':  ?0}")
	Mono<Customer> findByEmailId(String emailId);

	@Query("{'emailId':  ?0}")
	Flux<Address> findCustomerAddressByEmailId(String emailId);
}
