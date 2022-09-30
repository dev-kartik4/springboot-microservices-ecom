package com.apigateway.gateway_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/fallbackerror")
public class GatewayController {

    @GetMapping("/products")
    public String productFallback(){
        return "SEEMS LIKE PRODUCT SERVICE IS DOWN AND ENJOYING";
    }

    @GetMapping("/inventory")
    public String inventoryFallback(){
        return "UH OH ! INVENTORY-STOCK SERVICE IS OUT OF RANGE FOR NOW";
    }

    @GetMapping("/promotions")
    public String promotionFallback(){
        return "PROMOTIONAL OFFER DETAILS CANT'T BE FETCHED RIGHT NOW";
    }
}
