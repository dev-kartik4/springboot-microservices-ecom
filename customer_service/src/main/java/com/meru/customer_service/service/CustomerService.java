package com.meru.customer_service.service;

import java.util.*;

import com.meru.customer_service.bean.Address;
import com.meru.customer_service.exception.CustomerServiceException;
import com.meru.customer_service.model.Cart;
import com.meru.customer_service.model.CartProducts;
import com.meru.customer_service.model.Order;
import com.meru.customer_service.model.ResponseErrorMessage;
import com.meru.customer_service.util.CustomerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.meru.customer_service.bean.Customer;
import com.meru.customer_service.repo.CustomerRepo;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerService {
	
	@Autowired
	private CustomerRepo customerRepo;

	@Autowired
	public RestTemplate restTemplate;

	@Autowired
	private BCryptPasswordEncoder encoder;

	@Autowired
	public CustomerUtil customerUtil;

	private static final Logger LOGGER = LoggerFactory.getLogger(CustomerService.class);

	/**
	 * WILL BE CONTROLLED BY USER AND ADMIN
	 *
	 * METHOD TO REGISTER FIRST TIME NEW CUSTOMER INFORMATION
	 *
	 * @param cust
	 * @return
	 * @throws CustomerServiceException
	 */
	public ResponseEntity<?> createCustomer(Customer cust) throws CustomerServiceException {

		Optional<Customer> cust1 = customerRepo.findByEmailId(cust.getEmailId());
		ResponseErrorMessage responseErrorMessage = null;
		try{
			if(!cust1.isPresent()){
				Customer custo = new Customer();
				custo.setCustomerId(customerUtil.generateCustomerId());
				custo.setCustomerName(cust.getCustomerName());
				custo.setPhone(cust.getPhone());
				custo.setPassword(encoder.encode(cust.getPassword()));
				custo.setEmailId(cust.getEmailId());
				custo.setAddress(cust.getAddress());
				custo.setMyOrders(new ArrayList<Order>());
				customerRepo.save(custo);

				Cart cart = new Cart();
				cart.setCustomerIdForCart(custo.getCustomerId());
				cart.setCartProducts(new LinkedHashSet<CartProducts>());
				createCartForCustomerViaRest(cart);
				LOGGER.info("CUSTOMER AND THEIR CART IS BEING CREATED SUCCESSFULLY");
				return new ResponseEntity(custo, HttpStatus.CREATED);
			}else{
				throw new CustomerServiceException("YOU ARE ALREADY REGISTERED WITH US!");
			}
		}catch(CustomerServiceException custEx){
			LOGGER.error("YOU ARE ALREADY REGISTERED WITH US!");
			custEx.printStackTrace();
			throw new CustomerServiceException("YOU ARE ALREADY REGISTERED WITH US!");
		}
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN
	 *
	 * METHOD FOR FETCHING CUSTOMER INFORMATION BY CUSTOMER ID
	 *
	 * @param customerId
	 * @return
	 * @throws CustomerServiceException
	 */
	public Optional<Customer> getCustomerById(int customerId) throws CustomerServiceException {
		try{
			Optional<Customer> customerData = customerRepo.findById(customerId);
			if(customerData.isPresent()){
				LOGGER.info("FETCHED CUSTOMER DETAILS WITH CUSTOMER ID ["+customerId+"] SUCCESSFULLY");
				return customerData;
			}else{
				throw new CustomerServiceException("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER ID ["+customerId+"]");
			}
		}catch(CustomerServiceException custEx){
			LOGGER.error("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER ID ["+customerId+"]");
			custEx.printStackTrace();
			throw new CustomerServiceException("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER ID ["+customerId+"]");
		}
	}

	/**
	 * WILL BE CONTROLLED BY USER FIRST AND ADMIN LATER
	 *
	 * METHOD TO FETCH CUSTOMER INFORMATION BY EMAIL ADDRESS
	 *
	 * @param emailId
	 * @return
	 * @throws CustomerServiceException
	 */
	public Optional<Customer> getCustomerByEmail(String emailId) throws CustomerServiceException {
		try{
			Optional<Customer> customerData = customerRepo.findByEmailId(emailId);
			if(customerData.isPresent()){
				LOGGER.info("FETCHED CUSTOMER DETAILS WITH CUSTOMER EMAIL ID ["+emailId+"]  SUCCESSFULLY");
				return customerData;
			}else{
				throw new CustomerServiceException("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER EMAIL ID ["+emailId+"]");
			}
		}catch(CustomerServiceException custEx) {
			LOGGER.error("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER EMAIL ID [" + emailId + "]",HttpStatus.NOT_FOUND);
			custEx.printStackTrace();
			throw new CustomerServiceException("ERROR FETCHING CUSTOMER DETAILS WITH CUSTOMER EMAIL ID ["+emailId+"]");
		}
	}

	/**
	 * WILL BE CONTROLLED BY USER FIRST AND ADMIN LATER
	 *
	 * METHOD TO FETCH CUSTOMER ADDRESS BY EMAIL ID
	 *
	 * @param emailId
	 * @return
	 * @throws CustomerServiceException
	 */
	public Optional<Address> getDefaultAddressSelectedByCustomerEmailId(String emailId) throws CustomerServiceException {
		try{
			Set<Address> customerAddressSet = customerRepo.findSetOfAddressByCustomerEmailId(emailId);
			Optional<Address> defaultSelectedAddress = customerAddressSet.stream().filter(address -> address.isDefaultAddress()).findFirst();

			if(defaultSelectedAddress.isPresent()){
				LOGGER.info("FETCHED ADDRESS WITH CUSTOMER ID ["+emailId+"] SUCCESSFULLY");
				return defaultSelectedAddress;
			}else{
				throw new CustomerServiceException("ERROR FETCHING CUSTOMER ADDRESS WITH CUSTOMER EMAIL ID ["+emailId+"]");
			}
		}catch(CustomerServiceException custEx) {
			LOGGER.error("ERROR FETCHING CUSTOMER ADDRESS WITH CUSTOMER EMAIL ID ["+emailId+"]");
			custEx.printStackTrace();
			throw new CustomerServiceException("ERROR FETCHING CUSTOMER ADDRESS WITH CUSTOMER EMAIL ID ["+emailId+"]");
		}
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN
	 *
	 * METHOD TO FETCH LIST OF CUSTOMERS REGISTERED WITH US
	 *
	 * @return
	 * @throws CustomerServiceException
	 */
	public List<Customer> getAllCustomers() throws CustomerServiceException {
		try{
			List<Customer> allCustomerInfo = customerRepo.findAll();
			if(!allCustomerInfo.isEmpty()){
				LOGGER.info("FETCHED ALL CUSTOMERS INFO SUCCESSFULLY");
				return allCustomerInfo;
			}else{
				throw new CustomerServiceException("ERROR FETCHING ALL CUSTOMER INFO");
			}
		}catch(CustomerServiceException custEx) {
			LOGGER.error("ERROR FETCHING ALL CUSTOMER INFO");
			custEx.printStackTrace();
			throw new CustomerServiceException("ERROR FETCHING ALL CUSTOMER INFO");
		}
	}

	/**
	 * WILL BE CONTROLLED BY USER ONLY
	 *
	 * METHOD TO UPDATE ORDER LIST FOR CUSTOMER
	 *
	 * @param customerOrder
	 * @return
	 */
	public List<Order> updateOrderList(Order customerOrder) throws CustomerServiceException{
		try{
			Customer customer = getCustomerByEmail(customerOrder.getCustomerEmailId()).get();
			if(customer != null){
				customer.setCustomerId(customer.getCustomerId());
				customer.setCustomerName(customer.getCustomerName());
				customer.setEmailId(customer.getEmailId());
				customer.setPassword(customer.getPassword());
				customer.setAddress(customer.getAddress());
				customer.setPhone(customer.getPhone());
				customer.getMyOrders().add(customerOrder);
				LOGGER.info("UPDATING ORDER LIST FOR CUSTOMER WITH EMAIL ID ["+customerOrder.getCustomerEmailId()+"]");
				customerRepo.save(customer);
				return customer.getMyOrders();
			}else{
				throw new CustomerServiceException("ERROR UPDATING ORDER LIST FOR CUSTOMER WITH CUSTOMER EMAIL ID ["+customerOrder.getCustomerEmailId()+"]");
			}
		}catch(CustomerServiceException custEx) {
			LOGGER.error("ERROR UPDATING ORDER LIST FOR CUSTOMER WITH CUSTOMER EMAIL ID ["+customerOrder.getCustomerEmailId()+"]");
			custEx.printStackTrace();
			throw new CustomerServiceException("ERROR UPDATING ORDER LIST FOR CUSTOMER WITH CUSTOMER EMAIL ID ["+customerOrder.getCustomerEmailId()+"]");
		}
	}

	/**
	 * WILL BE CONTROLLED BY USER ONLY
	 *
	 * METHOD TO UPDATE THEIR PROFILE INFORMATION
	 *
	 * @param customer
	 * @return
	 */
	public Optional<Customer> updateCustomerInfo(Customer customer) {

		try{
			if(customerRepo.findByEmailId(customer.getEmailId()) != null){
				Optional<Customer> updatedProfileData = customerRepo.findByEmailId(customer.getEmailId());
				updatedProfileData.get().setCustomerId(customer.getCustomerId());
				updatedProfileData.get().setCustomerName(customer.getCustomerName());
				updatedProfileData.get().setEmailId(customer.getEmailId());
				updatedProfileData.get().setPassword(customer.getPassword());
				updatedProfileData.get().setAddress(customer.getAddress());
				updatedProfileData.get().setPhone(customer.getPhone());

				LOGGER.info("UPDATING PROFILE INFORMATION FOR CUSTOMER WITH EMAIL ADDRESS ["+customer.getEmailId()+"]");

				return updatedProfileData;
			}else
				throw new CustomerServiceException("ERROR FETCHING CUSTOMER DETAILS WITH EMAIL ADDRESS ["+customer.getEmailId()+"]");
		}catch(CustomerServiceException custEx){
			LOGGER.error("ERROR FETCHING CUSTOMER DETAILS WITH EMAIL ADDRESS ["+customer.getEmailId()+"]");
			custEx.printStackTrace();
			throw new CustomerServiceException("ERROR FETCHING CUSTOMER DETAILS WITH EMAIL ADDRESS ["+customer.getEmailId()+"]");
		}
	}

	/**
	 * WILL BE CONTROLLED BY ADMIN ONLY
	 *
	 * METHOD TO DELETE CUSTOMER ACCOUNT BY CUSTOMER ID UPON INACTIVITY FOR LONG TIME
	 *
	 * @param customerId
	 * @throws CustomerServiceException
	 */
	public boolean deleteCustomerById(int customerId) throws CustomerServiceException {

		boolean customerDeletedById = false;
		if(!customerDeletedById) {
			customerRepo.deleteById(customerId);
			customerDeletedById = true;
		}
		return customerDeletedById;
	}

	//deleting customer by emailid

	/**
	 * WILL BE CONTROLLED BY USER ONLY
	 *
	 * METHOD TO DELETE THEIR OWN ACCOUNT BY DEACTIVATING AND LOGGING OUT
	 *
	 * @param customerEmail
	 * @throws CustomerServiceException
	 */
	public boolean deleteCustomerByEmail(String customerEmail) throws CustomerServiceException {

		boolean customerDeletedByEmail = false;
		if(!customerDeletedByEmail){
			customerRepo.deleteByEmailId(customerEmail);
			customerDeletedByEmail = true;
		}
		return customerDeletedByEmail;
	}

	/*CUSTOMER-ORDER MICROSERVICE*/

	/**
	 * WILL BE CONTROLLED BY USER ONLY
	 *
	 * METHOD TO GET ALL ORDERS OF REGISTERED CUSTOMER
	 *
	 * @param emailId
	 * @throws CustomerServiceException
	 */
	public List<Order> getAllOrdersForCustomer(String emailId){

		Customer completeCustomerInfo = customerRepo.findByEmailId(emailId).get();
		List<Order> myOrders = completeCustomerInfo.getMyOrders();
		return myOrders;
	}

	/*CUSTOMER-CART MICROSERVICE*/

	/**
	 *
	 * @param cart
	 * @return
	 */
	public ResponseEntity<Cart> createCartForCustomerViaRest(Cart cart){

		LOGGER.info("CART OBJECT"+cart);
		restTemplate.postForObject("http://cart-service/cart/createCart/customer/"+cart.getCustomerIdForCart(),cart,Cart.class);
		LOGGER.info("CART CREATED THROUGH REST COMMUNICATION");
		return new ResponseEntity(cart,HttpStatus.CREATED);
	}


}
