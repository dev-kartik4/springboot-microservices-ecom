package com.shoppix.product_reactive_service.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RatingsAndReviews {

    @Id
    @Field(name = "RATING")
    private double rating;

    @Field(name = "CUSTOMER_NAME")
    private String customerName;

    @Field(name = "DETAILED_REVIEW")
    private String detailedReview;

    @Field(name = "CREATED_DATE_TIME")
    private String createdDateTime;

    @Field(name = "REVIEWED_AND_VERIFIED")
    private boolean isReviewedAndVerified;

    @Field(name = "IMAGES_UPLOADED_BY_REVIEWER")
    private List<String> imagesUploadedInReviews;
}
