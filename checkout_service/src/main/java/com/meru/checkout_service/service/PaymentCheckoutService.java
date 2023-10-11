package com.meru.checkout_service.service;

import com.meru.checkout_service.controller.PaymentCheckoutController;
import com.meru.checkout_service.entity.PaymentCheckout;
import com.meru.checkout_service.model.OrderRequest;
import com.meru.checkout_service.model.PaymentOptions;
import com.meru.checkout_service.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.RestTemplate;

@Service
public class PaymentCheckoutService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentCheckoutService.class);

    @Autowired
    public RestTemplate restTemplate;

    public ResponseEntity<?> getUserOptionForPayments(PaymentCheckout paymentCheckout){

            String finalMode = "";
            PaymentOptions paymentOptions = new PaymentOptions();
            //LOGGER.info("FINAL ORDER REQUEST "+paymentCheckout.getOrderRequest().toString());
            OrderRequest orderRequest = paymentCheckout.getOrderRequest();
            orderRequest.setCustomerId(orderRequest.getCustomerId());
            orderRequest.setCustomerEmailId(orderRequest.getCustomerEmailId());
            orderRequest.setStatus("ORDER PENDING");
            LOGGER.info("FINAL ORDER REQUEST "+orderRequest.toString());
            Product product = restTemplate.getForObject("http://localhost:8999/products/getProduct/"+orderRequest.getProductId(),Product.class);
            orderRequest.setProductId(product.getProductId());
            orderRequest.setOrderRequestQuantity(orderRequest.getOrderRequestQuantity());
            orderRequest.setTotalOrderPrice(product.getPrice());
            if(paymentCheckout.getPaymentOptionSelected().equalsIgnoreCase("optedForCreditOrDebitCard")){
                paymentOptions.setOptedForCreditOrDebitCard(true);
                finalMode = "optedForCreditOrDebitCard";
                orderRequest.setPaymentModeSelected(finalMode);
            } else if(paymentCheckout.getPaymentOptionSelected().equalsIgnoreCase("optedForNetBanking")){
                paymentOptions.setOptedForNetBanking(true);
                finalMode = "optedForNetBanking";
                orderRequest.setPaymentModeSelected(finalMode);
            } else if(paymentCheckout.getPaymentOptionSelected().equalsIgnoreCase("optedForEmiFacility")){
                paymentOptions.setOptedForEmiFacility(true);
                finalMode = "optedForEmiFacility";
                orderRequest.setPaymentModeSelected(finalMode);
            } else if(paymentCheckout.getPaymentOptionSelected().equalsIgnoreCase("optedForCashOnDelivery")){
                paymentOptions.setOptedForCashOnDelivery(true);
                finalMode = "optedForCashOnDelivery";
                orderRequest.setPaymentModeSelected(finalMode);
            }
            LOGGER.info("FINAL ORDER REQUEST "+orderRequest.toString());
            return new ResponseEntity(orderRequest, HttpStatus.OK);
//        } catch(Exception ex){
//            return new ResponseEntity("ERROR WHILE PAYMENT CHECKOUT FOR YOUR ORDER",HttpStatus.SERVICE_UNAVAILABLE);
//        }
    }
}
