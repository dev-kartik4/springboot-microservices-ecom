package com.shoppix.product_reactive_service.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDescription {

    // fields across all categories
    @Field(name = "BRAND")
    private String brand;

    @Field(name = "DIMENSIONS")
    private String dimensions;

    @Field(name = "WEIGHT")
    private String weight;

    // Electronics specific fields
    @Field(name = "CAMERA")
    private String camera;

    @Field(name = "SPEAKERS")
    private String speakers;

    @Field(name = "MODEL_NUMBER")
    private String modelNumber;

    @Field(name = "OPERATING_SYSTEM")
    private String operatingSystem;

    @Field(name = "DISPLAY_TYPE")
    private String displayType;

    @Field(name = "PROCESSOR")
    private String processor;

    @Field(name = "RAM")
    private String ram;

    @Field(name = "STORAGE")
    private String storage;

    @Field(name = "SCREEN_SIZE")
    private String screenSize;

    @Field(name = "RESOLUTION")
    private String resolution;

    @Field(name = "BATTERY_LIFE")
    private String batteryLife;

    @Field(name = "CONNECTIVITY")
    private List<String> connectivity;

    @Field(name = "CHARGING_TECHNOLOGY")
    private String chargingTechnology;

    @Field(name = "INCLUDED_ACCESSORIES")
    private List<String> includedAccessories;

    @Field(name = "WARRANTY")
    private String warranty;

    // Clothing specific fields
    @Field(name = "MATERIAL")
    private String material;

    @Field(name = "SIZE")
    private String size;

    @Field(name = "COLOR")
    private String color;

    @Field(name = "FIT")
    private String fit;

    @Field(name = "WASH_CARE")
    private String washCare;

    @Field(name = "GENDER")
    private String gender;

    @Field(name = "OCCASION")
    private String occasion;

    // Books specific fields
    @Field(name = "AUTHOR")
    private String author;

    @Field(name = "PUBLISHER")
    private String publisher;

    @Field(name = "LANGUAGE")
    private String language;

    @Field(name = "GENRE")
    private String genre;

    @Field(name = "PAGES")
    private int pages;

    @Field(name = "ISBN")
    private String isbn;

    @Field(name = "PUBLICATION_DATE")
    private String publicationDate;

    @Field(name = "PRICE")
    private double price;

    // Furniture specific fields
    @Field(name = "WEIGHT_CAPACITY")
    private String weightCapacity;

    @Field(name = "ASSEMBLY_REQUIRED")
    private boolean assemblyRequired;

    @Field(name = "STYLE")
    private String style;

    // Beauty and Health specific fields
    @Field(name = "PRODUCT_TYPE")
    private String productType;

    @Field(name = "INGREDIENTS")
    private List<String> ingredients;

    @Field(name = "QUANTITY")
    private String quantity;

    @Field(name = "APPLICATION")
    private String application;

    @Field(name = "USAGE_INSTRUCTIONS")
    private String usageInstructions;

    @Field(name = "EXPIRY_DATE")
    private String expiryDate;

    // Sports specific fields
    @Field(name = "SPORT_TYPE")
    private String sportType;

    @Field(name = "SPORTS_MATERIAL")
    private String sportMaterial;

    @Field(name = "USAGE")
    private String sportUsage;

    // Baby and Kids specific fields
    @Field(name = "AGE_GROUP")
    private String ageGroup;

    @Field(name = "SAFETY_CERTIFICATIONS")
    private List<String> safetyCertifications;

    @Field(name = "RECOMMENDED_USAGE")
    private String recommendedUsage;

    // Color variants (common for many categories)
    @Field(name = "COLOR_VARIANTS")
    private List<String> colorVariants;

    // Getters and Setters
    // You will have all the getter and setter methods for the above fields here

}

