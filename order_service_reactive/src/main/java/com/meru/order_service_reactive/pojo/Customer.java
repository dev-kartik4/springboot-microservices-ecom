package com.meru.order_service_reactive.pojo;

import com.meru.order_service_reactive.bean.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Customer {

	private int customerId;

	private String customerName;

	private long phone;

	private String password;

	private String emailId;

	private Address address;

	private List<Order> myOrders;

}