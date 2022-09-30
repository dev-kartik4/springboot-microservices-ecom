package com.meru.customer_service.controller;

import java.util.List;
import java.util.Optional;

import com.meru.customer_service.model.Cart;
import com.meru.customer_service.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.meru.customer_service.bean.Address;
import com.meru.customer_service.bean.Customer;
import com.meru.customer_service.model.OrderRequest;
import com.meru.customer_service.model.Product;
import com.meru.customer_service.service.CustomerService;

import javax.swing.text.html.Option;

@RestController
@RequestMapping("/customer")
public class CustomerController {
	
	@Autowired
	private CustomerService custService;
	
	@Autowired
	public RestTemplate restTemplate;
	
//	@Autowired
//	private CustomerException custException;
	
	
	/*CUSTOMER MICROSERVICE*/
	@PostMapping("/addCustomer")
	@ResponseBody
	public ResponseEntity<Customer> saveCustomer(@RequestBody Customer cust) {
		
		System.out.println("CUSTOMER SAVED SUCCESSFULLY");
		Customer custo = custService.saveCustomer(cust);
		
		return new ResponseEntity<Customer>(custo, HttpStatus.OK);
	}
	
	//getting the customer by customerid
	@GetMapping("/getCustomer/{customerId}")
	public ResponseEntity<Customer> getCustomerById(@PathVariable("customerId") int customerId) {
		
		Customer cust = custService.getCustomerById(customerId).get();
		if(cust==null)
			return new ResponseEntity<Customer>(cust, HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<Customer>(cust, HttpStatus.OK);	
	}
	
	//getting the list of all customers
	
	@GetMapping("/getAllCustomers")
	
	public ResponseEntity<List<Customer>> getAllCustomers(){
		
		List<Customer> customers = custService.getAllCustomers();
		if(customers.isEmpty())
			return new ResponseEntity<List<Customer>>(customers, HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<List<Customer>>(customers, HttpStatus.OK);
	}
	
	//deleting the customer by customerid
	@DeleteMapping("/delete/{customerId}")
	public HttpStatus deleteCustomerById(@PathVariable("customerId") int customerId) {
		
		custService.deleteCustomer(customerId);
		System.out.println("CUSTOMER DELETED SUCCESSFULLY");
		return HttpStatus.FORBIDDEN;
		
	}
	
	/*GETTING ADDRESS BY CUSTOMERID*/
	@GetMapping("/getAddress/{customerId}")
	public ResponseEntity<Address> getAddressByCustomerId(@PathVariable("customerId") int customerId){
		
		Address custAddress = custService.getAddressByCustomerId(customerId).getAddress();
		
		if(custAddress==null)
			return new ResponseEntity<Address>(custAddress,HttpStatus.NOT_FOUND);
		else
			return new ResponseEntity<Address>(custAddress,HttpStatus.OK);
	}		 
	
	
	/*CUSTOMER - ORDER MICROSERVICE*/
	@PostMapping("/{customerId}/orderProduct/{productId}")
	@ResponseBody
	public Order placeOrder(@PathVariable("customerId") int customerId, @PathVariable("productId") int productId, @RequestBody Order orderDetails) {
		
		Product prod;
		prod = restTemplate.getForObject("http://product-service/products/getProduct/"+productId, Product.class);
		System.out.println("PRODUCT IS "+prod.getProductId());
		Order order = new Order();
		order.setOrderId(orderDetails.getOrderId());
		order.setCustomerId(customerId);
		order.setAddressId(orderDetails.getAddressId());
		order.setProductId(productId);
		order.setOrderDate(orderDetails.getOrderDate());
		order.setStatus(orderDetails.getStatus());

		restTemplate.postForObject("http://order-service/orders/addOrder",order, Order.class);

//		return prod.getProductId()+" "+prod.getProductName()+"--> ORDER SUCCESSFULL";
		return order;
	}

	//GET ALL ORDERS BY CUSTOMER ID
	@GetMapping("/getAllOrders/{customerId}")
	public List<Order> getAllOrdersByCustomerId(@PathVariable("customerId") int customerId){

		List<Order> orders = restTemplate.getForObject("http://order-service/orders/getAllOrders/"+customerId,List.class);

		return orders;
	}

	//DELETE ORDERS WITH ORDER ID
	@DeleteMapping("/deleteOrder/{orderId}")
	public ResponseEntity<String> deleteOrder(@PathVariable("orderId") int orderId){

		restTemplate.delete("http://order-service/orders/delete/"+orderId,Order.class);

		return new ResponseEntity<String>("DELETION OF ORDER ID "+orderId+"SUCCESSFULL",HttpStatus.OK);
	}


	/*CUSTOMER -- CART MICROSERVICES*/

//	public ResponseEntity<Cart>

}
