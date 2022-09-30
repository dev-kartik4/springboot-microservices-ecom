package com.meru.customer_service.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.meru.customer_service.bean.Address;
import com.meru.customer_service.bean.Customer;
import com.meru.customer_service.repo.CustomerRepo;

@Service
public class CustomerService {
	
	@Autowired
	private CustomerRepo custRepo;
	
	@Autowired
	private BCryptPasswordEncoder encoder;
	
	//CUSTOMER DETAILS SAVED
	public Customer saveCustomer(Customer cust) {
		
		System.out.println("CUSTOMER SAVED");
		
		Optional<Customer> cust1 = custRepo.findById(cust.getCustomerId());
		
		if(cust1.isPresent()) {
			Customer custo = new Customer();
			custo.setCustomerId(cust.getCustomerId());
			custo.setCustomerName(cust.getCustomerName());
			custo.setPhone(cust.getPhone());
			custo.setEmailId(cust.getEmailId());
			
			return custo;
		}else
			cust.setPassword(encoder.encode(cust.getPassword()));
			cust = custRepo.save(cust);
		
		return cust;
	}
	
	//fetching customer by customerid
	public Optional<Customer> getCustomerById(int customerId) {
		
		return custRepo.findById(customerId);
	}
	
	//fetching address of the customer by customerid
	public Customer getAddressByCustomerId(int customerId) {
		return custRepo.findAddressByCustomerId(customerId);
	}
	
	//fetching the list of all customers
	public List<Customer> getAllCustomers(){
		
		return custRepo.findAll();
		
	}
	
	//deleting the customer by customerid
	public void deleteCustomer(int customerId) {
		
		custRepo.deleteById(customerId);
	}


}
