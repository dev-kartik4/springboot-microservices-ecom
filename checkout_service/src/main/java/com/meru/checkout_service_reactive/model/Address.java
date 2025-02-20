package com.meru.checkout_service_reactive.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


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
