package com.shoppix.inventory_service_reactive.pojo;

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

    private double rating;

    private String customerName;

    private String detailedReview;

    private String createdDateTime;

    private boolean isReviewedAndVerified;

    private List<String> imagesUploadedInReviews;
}
