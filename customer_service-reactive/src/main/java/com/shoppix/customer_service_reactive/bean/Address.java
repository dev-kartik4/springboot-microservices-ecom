package com.shoppix.customer_service_reactive.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
	
	@Id
	@Field(name="ADDRESS_ID")
	private int addressId;
	
	@Field(name="PRIMARY_ADDRESS")
	private String address1;
	
	@Field(name="SECONDARY_ADDRESS")
	private String address2;
	
	@Field(name="CITY")
	private String city;
	
	@Field(name="STATE")
	private String state;
	
	@Field(name="PINCODE")
	private int pincode;

	@Field(name="DEFAULT_ADDRESS")
	private boolean defaultAddress;
}
