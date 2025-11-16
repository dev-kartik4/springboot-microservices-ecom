package com.shoppix.customer_reactive_service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderRequest{

    @Id
    @JsonIgnore
	private int customerId;

	private String customerEmailId;
	
	private String variantProductId;

	private int orderRequestQuantity;

	@JsonIgnore
	private double totalOrderPrice;

    @JsonIgnore
    private String orderRequestStatus;

    @JsonIgnore
    private String orderRequestGeneratedDateTime;

    @JsonIgnore
    private String orderRequestProcessedDateTime;

    @JsonIgnore
    private String orderRequestCancelledDateTime;
}
