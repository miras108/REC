package com.database.model;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import java.util.Date;

/**
 * Created by miras108 on 2017-01-07.
 */
@Entity
public class DominiumOffer {

    @Id
    private String url;

    private Double totalPrice;
    private Double price;
    private Double yardage;
    private Integer floor;
    private Integer numberOfRooms;
    private String dateOfCompletion;
    private Integer quarterOfCompletion;
    private Integer yearOfCompletion;
    @Enumerated(EnumType.STRING)
    private Availability availability;
    @Enumerated(EnumType.STRING)
    private InvestemntType investemntType;
    private Integer floors;
    private String district;
    private Date offerLoadingDate;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Double getYardage() {
        return yardage;
    }

    public void setYardage(Double yardage) {
        this.yardage = yardage;
    }

    public Integer getFloor() {
        return floor;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public String getDateOfCompletion() {
        return dateOfCompletion;
    }

    public void setDateOfCompletion(String dateOfCompletion) {
        this.dateOfCompletion = dateOfCompletion;
    }

    public Integer getQuarterOfCompletion() {
        return quarterOfCompletion;
    }

    public void setQuarterOfCompletion(Integer quarterOfCompletion) {
        this.quarterOfCompletion = quarterOfCompletion;
    }

    public Integer getYearOfCompletion() {
        return yearOfCompletion;
    }

    public void setYearOfCompletion(Integer yearOfCompletion) {
        this.yearOfCompletion = yearOfCompletion;
    }

    public Availability getAvailability() {
        return availability;
    }

    public void setAvailability(Availability availability) {
        this.availability = availability;
    }

    public InvestemntType getInvestemntType() {
        return investemntType;
    }

    public void setInvestemntType(InvestemntType investemntType) {
        this.investemntType = investemntType;
    }

    public Integer getFloors() {
        return floors;
    }

    public void setFloors(Integer floors) {
        this.floors = floors;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public Date getOfferLoadingDate() {
        return offerLoadingDate;
    }

    public void setOfferLoadingDate(Date offerLoadingDate) {
        this.offerLoadingDate = offerLoadingDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DominiumOffer that = (DominiumOffer) o;

        return !(url != null ? !url.equals(that.url) : that.url != null);

    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "DominiumOffer{" +
                "url='" + url + '\'' +
                ", totalPrice=" + totalPrice +
                ", price=" + price +
                ", yardage=" + yardage +
                ", floor=" + floor +
                ", numberOfRooms=" + numberOfRooms +
                ", dateOfCompletion='" + dateOfCompletion + '\'' +
                ", quarterOfCompletion=" + quarterOfCompletion +
                ", yearOfCompletion=" + yearOfCompletion +
                ", availability=" + availability +
                ", investemntType=" + investemntType +
                ", floors=" + floors +
                ", district='" + district + '\'' +
                ", offerLoadingDate=" + offerLoadingDate +
                '}';
    }
}
