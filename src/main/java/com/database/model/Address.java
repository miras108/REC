package com.database.model;

/**
 * Created by miras108 on 2016-12-17.
 */
public class Address {
    private Integer id;
    private String street;
    private DistrictEnum district;
    private TaxOffice taxOffice;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public DistrictEnum getDistrict() {
        return district;
    }

    public void setDistrict(DistrictEnum district) {
        this.district = district;
    }

    public TaxOffice getTaxOffice() {
        return taxOffice;
    }

    public void setTaxOffice(TaxOffice taxOffice) {
        this.taxOffice = taxOffice;
    }
}
