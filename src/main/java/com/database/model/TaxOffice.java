package com.database.model;

/**
 * Created by miras108 on 2016-12-17.
 */
public enum TaxOffice {
    KROWODRZA("KR"),
    NOWA_HUTA("NH"),
    PODGORZE("PD"),
    PRADNIK("PK"),
    STARE_MIASTO("SM"),
    SRODMIESCIE("SR");

    private String name;

    TaxOffice(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
