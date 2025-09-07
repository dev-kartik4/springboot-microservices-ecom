package com.apigateway.gateway_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallbackerror")
public class GatewayController {

    @GetMapping("/identify")
    public String identifyFallback() { return "HEY BUDDY ! WILL SEND YOU IN SOONER"; }

    @GetMapping("/customer")
    public String customerFallback() { return "UH OH ! WE ARE CURRENTLY FACING DOWNTIME, PLEASE COMEBACK LATER "; }

    @GetMapping("/orders")
    public String orderFallback() { return "OOPS SORRY ! WE CANNOT CONTINUE WITH PROCESSING ANY ORDER AT THIS MOMENT"; }

    @GetMapping("/cart")
    public String cartFallback(){ return "OH DEAR ! CART ISN'T RESPONDING , MEANWHILE CONTINUE EXPLORING OUR PRODUCTS"; }

    @GetMapping("/products")
    public String productFallback(){
        return "SEEMS LIKE PRODUCT SERVICE IS IN DEEP SLEEP";
    }

    @GetMapping("/inventory")
    public String inventoryFallback(){
        return "UH OH ! INVENTORY/STOCK SERVICE IS DOWN FOR NOW";
    }

    @GetMapping("/offers")
    public String promotionFallback(){
        return "PROMOTIONAL OFFER DETAILS CANT'T BE FETCHED RIGHT NOW";
    }

    @GetMapping("/checkout")
    public String checkoutFallback() { return "DON'T PANIC ! PLEASE COMEBACK LATER AND CHECKOUT YOUR ORDER REQUEST"; }

    @GetMapping("/merchant")
    public String merchantFallback() { return "DON'T PANIC ! YOU CAN CONTINUE ADDING PRODUCTS LATER"; }

    @GetMapping("/notification")
    public String notificationFallback() { return "OOPS, DOWNTIME! WILL TRIGGER PENDING NOTIFICATIONS SOON"; }
}
