package com.database.model;

/**
 * Created by miras108 on 2017-01-07.
 */
public class DominiumOffer {

    private String offerName;
    private String district;
    private Double price;
    private Double totalPrice;
    private Double yardage;

    public String getOfferName() {
        return offerName;
    }

    public void setOfferName(String offerName) {
        this.offerName = offerName;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getYardage() {
        return yardage;
    }

    public void setYardage(Double yardage) {
        this.yardage = yardage;
    }
}
