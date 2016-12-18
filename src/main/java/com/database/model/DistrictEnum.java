package com.database.model;

/**
 * Created by miras108 on 2016-12-17.
 */
public enum DistrictEnum {
    ZWIERZYNIEC("Zwierzyniec"),
    STARE_MIASTO("Stare Miasto"),
    PRADNIK_CZERWONY("Pradnik Czerwony"),
    PRADNIK_BIALY("Pradnik Bialy"),
    CZYZYNY("Czyzyny"),
    LOBZOW("Lobzow"),
    WOLA_DUCHACKA("Wola Duchacka"),
    PODGORZE("Podgorze"),
    BIENCZYCE("Bienczyce"),
    SWOSZOWICE("Swoszowice"),
    BRONOWICE("Bronowice"),
    GRZEGORZKI("Grzegorzki"),
    NOWA_HUTA("Nowa Huta"),
    DEBNIKI("Debniki"),
    GREBALOW("Grebalow"),
    PROKOCIM_BIEZANOW("Prokocim-Biezanow"),
    MISTRZEJOWICE("Mistrzejowice"),
    LAGIEWNIKI("Lagiewniki");

    private final String name;

    private DistrictEnum(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
