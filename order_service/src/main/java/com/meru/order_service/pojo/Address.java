package com.meru.order_service.pojo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {

	private int addressId;

	private String address1;

	private String address2;

	private String city;

	private String state;

	private int pincode;

	private boolean defaultAddress;
}
