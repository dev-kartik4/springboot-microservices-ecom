package com.shoppix.customer_service_reactive.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.shoppix.customer_service_reactive.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "CUSTOMER_DETAILS")
public class Customer {
	
	@Id
	@Field(name="CUSTOMER_ID")
	@JsonProperty
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
	private List<Address> address;

	@Field(name = "MY_ORDERS")
	private List<Order> myOrders;

	@Field(name = "CUSTOMER_EVENT_STATUS")
	private String eventStatus;

	@Field(name = "CREATED_DATE_TIME")
	private String createdDateTime;

	@Field(name = "LAST_UPDATED_DATE_TIME")
	private String lastUpdatedDateTime;

	@Field(name = "CUSTOMER_ACCOUNT_EXISTENCE")
	private boolean accountExistence;
}