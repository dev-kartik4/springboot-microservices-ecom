package com.meru.order_service.pojo;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.meru.order_service.bean.Order;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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