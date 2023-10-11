package com.meru.customer_service.bean;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meru.customer_service.model.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "CUSTOMER_DETAILS")
public class Customer {
	
	@Id
	@Field(name="CUSTOMER_ID")
	@JsonIgnore
	private int customerId;
	
	@Field(name="CUSTOMER_NAME")
	private String customerName;
	
	@Field(name="PHONE")
	private long phone;
	
	@Field(name="PASSWORD")
	private String password;
	
	@Field(name="EMAIL_ID")
	private String emailId;

	@Field(name = "ADDRESS")
	private Set<Address> address;

	@Field(name = "MY_ORDERS")
	private List<Order> myOrders;

}