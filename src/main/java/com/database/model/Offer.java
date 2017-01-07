package com.database.model;

import javax.persistence.*;

/**
 * Created by miras108 on 2016-11-27.
 */
public class Offer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String url;
    private String rawAddress;
    private double price;
    private double totalPrice;
    private double yardage;
    private short numberOfRooms;
    private short floor;
    private short maxFloor;

    @Enumerated(EnumType.STRING)
    private Market market;
    private int yearOfConstruction;
    private Address address;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getRawAddress() {
        return rawAddress;
    }

    public void setRawAddress(String rawAddress) {
        this.rawAddress = rawAddress;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public double getYardage() {
        return yardage;
    }

    public void setYardage(double yardage) {
        this.yardage = yardage;
    }

    public short getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(short numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public short getFloor() {
        return floor;
    }

    public void setFloor(short floor) {
        this.floor = floor;
    }

    public short getMaxFloor() {
        return maxFloor;
    }

    public void setMaxFloor(short maxFloor) {
        this.maxFloor = maxFloor;
    }

    public Market getMarket() {
        return market;
    }

    public void setMarket(Market market) {
        this.market = market;
    }

    public int getYearOfConstruction() {
        return yearOfConstruction;
    }

    public void setYearOfConstruction(int yearOfConstruction) {
        this.yearOfConstruction = yearOfConstruction;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }
}
