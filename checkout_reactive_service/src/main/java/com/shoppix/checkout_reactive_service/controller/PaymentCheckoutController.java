package com.shoppix.checkout_reactive_service.controller;

import com.shoppix.checkout_reactive_service.entity.PaymentCheckout;
import com.shoppix.checkout_reactive_service.exception.CheckoutServiceException;
import com.shoppix.checkout_reactive_service.model.FinalPrintableInvoice;
import com.shoppix.checkout_reactive_service.model.OrderRequest;
import com.shoppix.checkout_reactive_service.service.PaymentCheckoutService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@CrossOrigin("*")
@RestController
@RequestMapping("/checkout")
public class PaymentCheckoutController {

    @Autowired
    private PaymentCheckoutService paymentCheckoutService;

    @Autowired
    public WebClient.Builder webClientBuilder;

    public static final String ORDER_SERVICE_URL = "http://order-service/orders";

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentCheckoutController.class);

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO POST REQUEST TO PROCEED FOR PAYMENT
     *
     * @param orderRequest
     * @return
     */
    @PostMapping("/proceedForPayment")
    @ResponseBody
    public ResponseEntity<Mono<OrderRequest>> proceedForPayment(@RequestBody OrderRequest orderRequest){

        Mono<OrderRequest> newOrderRequest = Mono.just(orderRequest);
        newOrderRequest.publishOn(Schedulers.parallel()).map(ordReq -> {
            FinalPrintableInvoice finalPrintableInvoice = new FinalPrintableInvoice();
            PaymentCheckout paymentCheckout = new PaymentCheckout();
            paymentCheckout.setOrderRequest(orderRequest);
            paymentCheckout.setPaymentOptionSelected(orderRequest.getPaymentModeSelected());
            LOGGER.info("MY ORDER REQUEST ["+orderRequest.toString()+"]");
            LOGGER.info("GOOD LUCK ! PROCEEDED FOR PAYMENT CHECKOUT PAGE");
            Mono<OrderRequest> myOrderRequest = paymentCheckoutService.getUserOptionForPayments(paymentCheckout);
            LOGGER.info("MY ORDER RESPONSE ["+myOrderRequest.toString()+"]");
            LOGGER.info("PAYMENT DONE ! PROCESSING YOUR ORDER");
            webClientBuilder.build()
                            .post()
                                    .uri(ORDER_SERVICE_URL.concat("/placeOrder"))
                                            .bodyValue(myOrderRequest)
                                                    .retrieve()
                                                            .toEntity(OrderRequest.class)
                                                                    .thenReturn(new ResponseEntity<>(orderRequest,HttpStatus.OK)).delaySubscription(Duration.ofMillis(3000)).subscribe();
            finalPrintableInvoice.getOrderSerialKey();
            return new ResponseEntity(newOrderRequest,HttpStatus.OK);
        }).switchIfEmpty(Mono.error(() -> {
            LOGGER.error("THERE'S AN ISSUE IN YOUR ORDER CHECKOUT");
            throw new CheckoutServiceException("THERE'S AN ISSUE IN YOUR ORDER CHECKOUT");
        })).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));
        return new ResponseEntity(newOrderRequest,HttpStatus.OK);
    }

    /**
     * WILL BE CONTROLLED BY USER AND ADMIN BOTH
     * <p>
     * API TO GENERATE FINAL PRINTABLE INVOICE
     *
     * @param finalPrintableInvoice
     * @return
     */
    @PostMapping("/generateFinalInvoice")
    public ResponseEntity<FinalPrintableInvoice> generateFinalPrintableInvoice(@RequestBody FinalPrintableInvoice finalPrintableInvoice){

        FinalPrintableInvoice finalInvoice = finalPrintableInvoice;

        return new ResponseEntity(finalInvoice,HttpStatus.OK);
    }

}
