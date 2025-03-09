package com.github.jkaste03.uefa_cc_sim.enums;

/**
 * Enum representing various countries participating in UEFA competitions.
 * Each enum constant is associated with the country's full name.
 */
public enum Country {
    ENG("England"),
    ITA("Italy"),
    ESP("Spain"),
    GER("Germany"),
    FRA("France"),
    NED("Netherlands"),
    POR("Portugal"),
    BEL("Belgium"),
    TUR("Turkey"),
    CZE("Czech Republic"),
    SCO("Scotland"),
    SUI("Switzerland"),
    AUT("Austria"),
    NOR("Norway"),
    DEN("Denmark"),
    GRE("Greece"),
    ISR("Israel"),
    UKR("Ukraine"),
    SRB("Serbia"),
    CRO("Croatia"),
    POL("Poland"),
    RUS("Russia"),
    CYP("Cyprus"),
    HUN("Hungary"),
    SWE("Sweden"),
    ROM("Romania"),
    BUL("Bulgaria"),
    AZE("Azerbaijan"),
    SLK("Slovakia"),
    SVN("Slovenia"),
    MOL("Moldova"),
    KOS("Kosovo"),
    KAZ("Kazakhstan"),
    FIN("Finland"),
    IRL("Ireland"),
    ARM("Armenia"),
    LAT("Latvia"),
    FAR("Faroe Islands"),
    BHZ("Bosnia and Herzegovina"),
    LIE("Liechtenstein"),
    ISL("Iceland"),
    NIR("Northern Ireland"),
    LUX("Luxembourg"),
    LIT("Lithuania"),
    MLT("Malta"),
    GEO("Georgia"),
    ALB("Albania"),
    EST("Estonia"),
    BLR("Belarus"),
    MAC("North Macedonia"),
    AND("Andorra"),
    WAL("Wales"),
    MNT("Montenegro"),
    GIB("Gibraltar"),
    SMR("San Marino");

    private final String countryName;

    /**
     * Constructor to initialize the enum constant with the country's full name.
     *
     * @param countryName the full name of the country
     */
    Country(String countryName) {
        this.countryName = countryName;
    }

    /**
     * Gets the full name of the country.
     *
     * @return the full name of the country
     */
    public String getCountryName() {
        return countryName;
    }
}