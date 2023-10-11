package com.meru.checkout_service.controller;

import com.meru.checkout_service.entity.PaymentCheckout;
import com.meru.checkout_service.model.FinalPrintableInvoice;
import com.meru.checkout_service.model.Order;
import com.meru.checkout_service.model.OrderRequest;
import com.meru.checkout_service.model.PaymentOptions;
import com.meru.checkout_service.service.PaymentCheckoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@CrossOrigin("*")
@RestController
@RequestMapping("/checkout")
public class PaymentCheckoutController {

    @Autowired
    private PaymentCheckoutService paymentCheckoutService;

    @Autowired
    public RestTemplate restTemplate;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentCheckoutController.class);

    @PostMapping("/proceedForPayment")
    @ResponseBody
    public ResponseEntity<?> proceedForPayment(@RequestBody OrderRequest orderRequest){

        if(orderRequest == null)
            return new ResponseEntity("THERE'S AN ISSUE IN YOUR ORDER CHECKOUT", HttpStatus.NOT_FOUND);
        else{
            FinalPrintableInvoice finalPrintableInvoice = new FinalPrintableInvoice();
            PaymentCheckout paymentCheckout = new PaymentCheckout();
            paymentCheckout.setOrderRequest(orderRequest);
            paymentCheckout.setPaymentOptionSelected(orderRequest.getPaymentModeSelected());
            LOGGER.info("MY ORDER REQUEST "+orderRequest.toString());
            LOGGER.info("GOOD LUCK ! PROCEEDED FOR PAYMENT CHECKOUT PAGE");
            ResponseEntity<?> myOrderRequest = paymentCheckoutService.getUserOptionForPayments(paymentCheckout);
            LOGGER.info("MY ORDER RESPONSE "+myOrderRequest.toString());
            LOGGER.info("PAYMENT DONE ! PROCESSING YOUR ORDER");
            restTemplate.postForObject("http://order-service/orders/placeOrder",myOrderRequest,OrderRequest.class);
            finalPrintableInvoice.getOrderSerialKey();
            return new ResponseEntity(myOrderRequest,HttpStatus.OK);
        }
    }

    @PostMapping("/generateFinalInvoice")
    public ResponseEntity<FinalPrintableInvoice> generateFinalPrintableInvoice(@RequestBody FinalPrintableInvoice finalPrintableInvoice){

        FinalPrintableInvoice finalInvoice = finalPrintableInvoice;

        return new ResponseEntity(finalInvoice,HttpStatus.OK);
    }

}
