package com.meru.checkout_service_reactive.service;

import com.meru.checkout_service_reactive.entity.PaymentCheckout;
import com.meru.checkout_service_reactive.exception.CheckoutServiceException;
import com.meru.checkout_service_reactive.model.OrderRequest;
import com.meru.checkout_service_reactive.model.PaymentOptions;
import com.meru.checkout_service_reactive.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

@Service
public class PaymentCheckoutService  {

    private static final Logger LOGGER = LoggerFactory.getLogger(PaymentCheckoutService.class);

    public static final String PRODUCT_SERVICE_URL = "http://product-service/products";

    @Autowired
    public WebClient.Builder webClientBuilder;

    /**
     *
     * @param paymentCheckout
     * @return
     * @throws CheckoutServiceException
     */
    public Mono<OrderRequest> getUserOptionForPayments(PaymentCheckout paymentCheckout) throws CheckoutServiceException{

            PaymentOptions paymentOptions = new PaymentOptions();

            Mono<OrderRequest> orderRequest = Mono.just(paymentCheckout.getOrderRequest());
            return orderRequest.map(ordRequest -> {
                ordRequest.setCustomerId(paymentCheckout.getOrderRequest().getCustomerId());
                ordRequest.setCustomerEmailId(paymentCheckout.getOrderRequest().getCustomerEmailId());
                ordRequest.setStatus("ORDER PENDING");
                LOGGER.info("FINAL ORDER REQUEST "+orderRequest.toString());
                Mono<Product> product = webClientBuilder.build()
                        .get()
                        .uri(PRODUCT_SERVICE_URL.concat("/getProduct/").concat(String.valueOf(paymentCheckout.getOrderRequest().getProductId())))
                        .retrieve()
                        .bodyToMono(Product.class)
                        .publishOn(Schedulers.parallel());
                ordRequest.setProductId(Integer.valueOf(String.valueOf(product.map(product1 -> product1.getProductId()))));
                ordRequest.setOrderRequestQuantity(paymentCheckout.getOrderRequest().getOrderRequestQuantity());
                ordRequest.setTotalOrderPrice(Double.valueOf(String.valueOf(product.map(product1 -> product1.getPrice()))));
                if(paymentCheckout.getPaymentOptionSelected().equalsIgnoreCase("optedForCreditOrDebitCard")){
                    paymentOptions.setOptedForCreditOrDebitCard(true);
                    ordRequest.setPaymentModeSelected("optedForCreditOrDebitCard");
                } else if(paymentCheckout.getPaymentOptionSelected().equalsIgnoreCase("optedForNetBanking")){
                    paymentOptions.setOptedForNetBanking(true);
                    ordRequest.setPaymentModeSelected("optedForNetBanking");
                } else if(paymentCheckout.getPaymentOptionSelected().equalsIgnoreCase("optedForEmiFacility")){
                    paymentOptions.setOptedForEmiFacility(true);
                    ordRequest.setPaymentModeSelected("optedForEmiFacility");
                } else if(paymentCheckout.getPaymentOptionSelected().equalsIgnoreCase("optedForCashOnDelivery")){
                    paymentOptions.setOptedForCashOnDelivery(true);
                    ordRequest.setPaymentModeSelected("optedForCashOnDelivery");
                }
                LOGGER.info("FINAL ORDER REQUEST "+orderRequest.toString());
                return ordRequest;
            }).switchIfEmpty(Mono.error(() -> {
                LOGGER.error("ERROR WHILE PAYMENT CHECKOUT FOR YOUR ORDER");
                throw new CheckoutServiceException("ERROR WHILE PAYMENT CHECKOUT FOR YOUR ORDER");
            })).doOnNext(System.out::println).delaySubscription(Duration.ofMillis(3000));
    }
}
