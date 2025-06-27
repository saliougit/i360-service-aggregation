package com.innov4africa.service_aggregation.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public class ProductOrderDetail {
    private Integer quantite;
    @JsonProperty("quantite_stock")
    private Integer quantiteStock;
    @JsonProperty("product_name")
    private String productName;
    @JsonProperty("product_id")
    private Integer productId;
    @JsonProperty("is_review")
    private String isReview;
    @JsonProperty("product_price")
    private Double productPrice;
    @JsonProperty("product_amount")
    private Double productAmount;
    @JsonProperty("address_rammassage")
    private String addressRammassage;
    @JsonProperty("product_longitude")
    private Double productLongitude;
    @JsonProperty("product_latitude")
    private Double productLatitude;
    private Integer rate;
    private List<String> media;
    
    // Getters and Setters
    public Integer getQuantite() { return quantite; }
    public void setQuantite(Integer quantite) { this.quantite = quantite; }

    public Integer getQuantiteStock() { return quantiteStock; }
    public void setQuantiteStock(Integer quantiteStock) { this.quantiteStock = quantiteStock; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public String getIsReview() { return isReview; }
    public void setIsReview(String isReview) { this.isReview = isReview; }

    public Double getProductPrice() { return productPrice; }
    public void setProductPrice(Double productPrice) { this.productPrice = productPrice; }

    public Double getProductAmount() { return productAmount; }
    public void setProductAmount(Double productAmount) { this.productAmount = productAmount; }

    public String getAddressRammassage() { return addressRammassage; }
    public void setAddressRammassage(String addressRammassage) { this.addressRammassage = addressRammassage; }

    public Double getProductLongitude() { return productLongitude; }
    public void setProductLongitude(Double productLongitude) { this.productLongitude = productLongitude; }

    public Double getProductLatitude() { return productLatitude; }
    public void setProductLatitude(Double productLatitude) { this.productLatitude = productLatitude; }

    public Integer getRate() { return rate; }
    public void setRate(Integer rate) { this.rate = rate; }

    public List<String> getMedia() { return media; }
    public void setMedia(List<String> media) { this.media = media; }
}
