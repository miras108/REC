package com.web.crowler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toSet;

/**
 * Created by miras108 on 2017-01-06.
 */
public class DominiumCrowler {
    private static final String INITIAL_CRAWLING_ADDRESS = "http://www.dominium.pl/nowe/sprzedaz/mieszkania/krakow";
    private static final String OFFER_TAG_NAME = "data-metrics-category";
    private static final String OFFER_TAG_VALUE = "Wyniki wyszukiwania pierwotny";
    private static final String OFFER_LINK_TAG_NAME = "a";
    private static final String HREF_ATTRIBUTE_NAME = "href";
    public static final String OFFER_TABLE_ID = "newobjectdetails-data";
    public static final String TOTAL_PRICE_ROW_NAME = "Cena brutto";
    public static final String YARDAGE_ROW_NAME = "Powierzchnia";
    public static final String PRICE_ROW_NAME = "Cena brutto za m2";
    public static final String FLOOR_ROW_NAME = "Pi\u0119tro";
    public static final String NUMBER_OF_ROOMS_ROW_NAME = "Liczba pokoi";
    public static final String ZERO_FLOOR_NAME = "parter";
    public static final String DATE_OF_COMPLETION_ROW_NAME = "Termin realizacji";
    public static final String AVAILABILITY_ROW_NAME = "Dost\u0119pno\u015b\u0107";

    public static void main(String[] args) throws IOException {

        Set<String> allOffersURL = retrieveMainPagesUrls();

        System.out.println("All offers count: " + allOffersURL.size());

        allOffersURL.stream()
                .forEach(url -> getOffer(url));
    }

    private static Set<String> retrieveMainPagesUrls() {
        Set<String> allPagesUrls = new HashSet<>();
        Set<String> pagesUrls = new HashSet<>();

        String currentPageUrl = INITIAL_CRAWLING_ADDRESS;
        int pagesCount = 0;
        do {
            if (pagesCount != 0) {
                currentPageUrl = INITIAL_CRAWLING_ADDRESS + "/" + pagesCount;
            }

            pagesUrls = getPagesUrls(currentPageUrl);
            allPagesUrls.addAll(pagesUrls);

            pagesCount++;

        } while (!pagesUrls.isEmpty()
                // TODO temporary condition
                && pagesCount < 4);

        return allPagesUrls;
    }

    private static Set<String> getPagesUrls(String currentPageUrl) {

        Document documentPage = null;
        try {
            documentPage = Jsoup.connect(currentPageUrl).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Elements offerHrefElements = documentPage.body().getElementsByAttributeValue(OFFER_TAG_NAME, OFFER_TAG_VALUE);

        return offerHrefElements.stream()
                .filter(element -> OFFER_LINK_TAG_NAME.equals(element.tagName()))
                .map(element -> element.attr(HREF_ATTRIBUTE_NAME))
                .collect(toSet());
    }

    private static void getOffer(String url) {
        Document offerPage = null;
        try {
            offerPage = Jsoup.connect(url).get();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Element offerTableDiv = offerPage.getElementById(OFFER_TABLE_ID);

        if (offerTableDiv != null) {
            Elements tableRows = offerTableDiv.getElementsByTag("tr");

            if (!tableRows.isEmpty()) {
                Double totalPrice = getTotalPrice(tableRows);
                Double price = getPrice(tableRows);
                Double yardage = getYardage(tableRows);
                Integer floor = getFloor(tableRows);
                Integer numberOfRooms = getNumberOfRooms(tableRows);
                String dateOfCompletion = getDateOfCompletion(tableRows);
                Integer dateOfCompletionQuarter = getOfCompletionQuarter(dateOfCompletion);
                Integer dateOfCompletionYear = convertToInt(dateOfCompletion);
                String availability = getAvailability(tableRows);

//                System.out.println("offer: " + url);
                System.out.println("TotalPrice: " + totalPrice + " Price: " + price + " yardage: " + yardage + " floor: " + floor + " number of rooms: " + numberOfRooms
                        + " date of completion: " + dateOfCompletion + " quarter: " + dateOfCompletionQuarter + " year: " + dateOfCompletionYear + " availability: " + availability);
            }
        }
    }

    private static Integer getOfCompletionQuarter(String dateOfCompletion) {
        if (dateOfCompletion != null) {
            if (dateOfCompletion.startsWith("III")) {
                return 3;
            } else if (dateOfCompletion.startsWith("II")) {
                return 2;
            } else if (dateOfCompletion.startsWith("IV")) {
                return 4;
            } else if (dateOfCompletion.startsWith("I")) {
                return 1;
            }
        }
        return null;
    }

    private static Double getTotalPrice(Elements tableRows) {
        String totalPriceRowValue = getRowValue(tableRows, TOTAL_PRICE_ROW_NAME);
        return convertToDouble(totalPriceRowValue);
    }

    private static Double getPrice(Elements tableRows) {
        String priceRowValue = getRowValue(tableRows, PRICE_ROW_NAME);
        return convertToDouble(priceRowValue);
    }

    private static Double getYardage(Elements tableRows) {
        String yardageRowValue = getRowValue(tableRows, YARDAGE_ROW_NAME);
        return convertToDouble(yardageRowValue);
    }

    private static Integer getFloor(Elements tableRows) {
        String floorRowValue = getRowValue(tableRows, FLOOR_ROW_NAME);
        if (floorRowValue != null && ZERO_FLOOR_NAME.equalsIgnoreCase(floorRowValue)) {
            return 0;
        }
        return convertToInt(floorRowValue);
    }

    private static Integer getNumberOfRooms(Elements tableRows) {
        String numberOfRooms = getRowValue(tableRows, NUMBER_OF_ROOMS_ROW_NAME);
        return convertToInt(numberOfRooms);
    }

    private static String getDateOfCompletion(Elements tableRows) {
        return getRowValue(tableRows, DATE_OF_COMPLETION_ROW_NAME);
    }

    private static String getAvailability(Elements tableRows) {
        return getRowValue(tableRows, AVAILABILITY_ROW_NAME);
    }

    private static String getRowValue(Elements tableRows, String rowName) {
        Optional<Element> priceRow = tableRows.stream()
                .filter(element -> !element.getElementsMatchingOwnText(rowName).isEmpty())
                .findFirst();

        if (priceRow.isPresent()) {
            Elements priceColumns = priceRow.get().getElementsByTag("td");
            if (priceColumns.size() > 1) {
                return priceColumns.get(1).text();
            }
        }
        return null;
    }

    private static Double convertToDouble(String text) {
        if (text != null) {
            String stringDouble = text.replaceAll("[^\\d,]", "").replace(",", ".");
            return stringDouble.isEmpty() ? null : Double.valueOf(stringDouble);
        }
        return null;
    }

    private static Integer convertToInt(String text) {
        if (text != null) {
            String stringInteger = text.replaceAll("[^\\d,]", "").replace(",", ".");
            return stringInteger.isEmpty() ? null : Integer.valueOf(stringInteger);
        }
        return null;
    }
}
