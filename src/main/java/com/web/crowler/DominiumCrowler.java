package com.web.crowler;

import com.database.dao.DominiumOfferDao;
import com.database.model.Availability;
import com.database.model.DominiumOffer;
import com.database.model.InvestemntType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.*;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Created by miras108 on 2017-01-06.
 */
public class DominiumCrowler {
    private static final String INITIAL_CRAWLING_ADDRESS = "http://www.dominium.pl/inwestycje/wszystkie/krakow";

    private static final String OFFER_TAG_NAME = "data-metrics-category";
    private static final String OFFER_TAG_VALUE = "Wyniki wyszukiwania inwestycji ";
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
    public static final String APPARTMENT_LIST_LINK_VALUE = "Mieszkania";
    public static final String HOUSE_LIST_LINK_VALUE = "Domy";
    public static final String INVESTMENTITEM_NAVIGATION_DETAILS_CLASS_NAME = "investmentitem-navigation-details";

    private static DominiumOfferDao dominiumOfferDao;

    public static void main(String[] args) throws IOException {

        initSpringContext();

        Set<String> allInvestmentsURL = retrieveInvestmentsUrls();

        Set<String> allOffersUrls = retrieveOffersUrls(allInvestmentsURL);

        System.out.println("All offers count: " + allOffersUrls.size());

        List<DominiumOffer> offers = allOffersUrls.parallelStream()
                .map(url -> getOffer(url))
                .filter(offer -> offer != null)
                .collect(toList());

        saveOffersToFile(offers);

        System.out.println("mapped offers: " + offers.size());
    }

    private static void saveOffersToFile(List<DominiumOffer> offers) {
        try {
            Date date = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-mm-ss");

            PrintWriter out = new PrintWriter("offersLoading_" + dateFormat.format(date));
            offers.forEach(offer -> out.println(offer));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private static Set<String> retrieveOffersUrls(Set<String> allInvestmentsURL) {
        Set<String> allOffersUrls = new HashSet<>();

        allInvestmentsURL.parallelStream()
                .forEach(investment -> allOffersUrls.addAll(retriveOffersUrlForInvestment(investment)));

        return allOffersUrls;
    }

    private static Set<String> retriveOffersUrlForInvestment(String investmentUrl) {
        try {
            Document investmentMainPage = Jsoup.connect(investmentUrl).get();

            String apparmentListLink = getApparmentListLink(investmentMainPage);

            if (apparmentListLink != null) {
                Document investmentAppartmentListPage = Jsoup.connect(apparmentListLink).get();
                return retriveOffersListFromAppartmentsList(investmentAppartmentListPage);
            }


        } catch (SocketTimeoutException e) {
            System.out.println("SoccetTimeOutException for link: " + investmentUrl);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return new HashSet<>();
    }

    private static Set<String> retriveOffersListFromAppartmentsList(Document investmentAppartmentListPage) {
        Element offersTableElement = investmentAppartmentListPage.getElementsByClass("investmentdetails-newobjects developmentobjectslist").first();

        Set<String> offersUrls = new HashSet<>();

        if (offersTableElement != null) {
            Element tableBodyElement = offersTableElement.getElementsByTag("tbody").first();
            Elements tableRowsElements = tableBodyElement.getElementsByTag("tr");

            for (int i = 2; i < tableRowsElements.size(); i++) {
                Element row = tableRowsElements.get(i);
                Element field = row.getElementsByTag("td").first();
                Element linkTag = field.getElementsByTag(OFFER_LINK_TAG_NAME).first();

                if (linkTag != null) {
                    String link = linkTag.attr(HREF_ATTRIBUTE_NAME);

                    if (link != null && !link.isEmpty()) {
                        offersUrls.add(link);
                    }
                }
            }
        } else {
            System.out.println("wrong");
        }
        return offersUrls;
    }

    private static String getApparmentListLink(Document investmentMainPage) {
        Optional<Element> appartmentLinkTag = getLinkTagForInvestmentType(investmentMainPage, APPARTMENT_LIST_LINK_VALUE);
        if (!appartmentLinkTag.isPresent()) {
            appartmentLinkTag = getLinkTagForInvestmentType(investmentMainPage, HOUSE_LIST_LINK_VALUE);
        }

        if (appartmentLinkTag.isPresent()) {
            return appartmentLinkTag.get().getElementsByTag(OFFER_LINK_TAG_NAME).first().attr(HREF_ATTRIBUTE_NAME);
        }

        return null;
    }

    private static Optional<Element> getLinkTagForInvestmentType(Document investmentMainPage, String investmentType) {
        Element investmentDetailsElement = investmentMainPage.getElementsByClass(INVESTMENTITEM_NAVIGATION_DETAILS_CLASS_NAME).first();

        return investmentDetailsElement.getElementsContainingOwnText(investmentType)
                .stream()
                .filter(element -> element.tagName().equalsIgnoreCase(OFFER_LINK_TAG_NAME))
                .findAny();
    }

    private static Set<String> retrieveInvestmentsUrls() {
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

        } while (!pagesUrls.isEmpty() &&
                // TODO temporary
                pagesCount < 1);

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

        Elements exposedOffersElements = documentPage.body().getElementsByAttributeValue(OFFER_TAG_NAME, "Wyniki wyszukiwania inwestycji  - inwestycja wyr\u00f3\u017cniona");

        Set<String> offersUrls = offerHrefElements.stream()
                .filter(element -> OFFER_LINK_TAG_NAME.equals(element.tagName()))
                .map(element -> element.attr(HREF_ATTRIBUTE_NAME))
                .collect(toSet());

        Set<String> exposedElements = exposedOffersElements.stream()
                .filter(element -> OFFER_LINK_TAG_NAME.equals(element.tagName()))
                .map(element -> element.attr(HREF_ATTRIBUTE_NAME))
                .collect(toSet());

        offersUrls.addAll(exposedElements);

        return offersUrls;
    }

    private static DominiumOffer getOffer(String url) {
        DominiumOffer dominiumOffer = dominiumOfferDao.getDominiumOfferByUrl(url);
        if (dominiumOffer == null) {
            dominiumOffer = new DominiumOffer();
        }

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
                Availability availability = getAvailability(tableRows);
                InvestemntType investmentType = getInvestmentType(offerPage);
                Integer floors = getFloors(tableRows);
                String district = getDistrict(offerPage);

                System.out.println("TotalPrice: " + totalPrice + " Price: " + price + " yardage: " + yardage + " floor: " + floor + " number of rooms: " + numberOfRooms
                        + " date of completion: " + dateOfCompletion + " quarter: " + dateOfCompletionQuarter + " year: " + dateOfCompletionYear + " availability: " + availability
                        + " investemntType: " + investmentType + " floors: " + floors + " district: " + district);


                dominiumOffer.setUrl(url);
                dominiumOffer.setAvailability(availability);
                dominiumOffer.setDateOfCompletion(dateOfCompletion);
                dominiumOffer.setDistrict(district);
                dominiumOffer.setFloor(floor);
                dominiumOffer.setFloors(floors);
                dominiumOffer.setInvestemntType(investmentType);
                dominiumOffer.setNumberOfRooms(numberOfRooms);
                dominiumOffer.setOfferLoadingDate(new Date());
                dominiumOffer.setPrice(price);
                dominiumOffer.setTotalPrice(totalPrice);
                dominiumOffer.setYardage(yardage);

                dominiumOfferDao.save(dominiumOffer);

                return dominiumOffer;
            }
        }
        return null;
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

    private static Integer getFloors(Elements tableRows) {
        String floors = getRowValue(tableRows, "Kondygnacje");
        return convertToInt(floors);
    }

    private static String getDateOfCompletion(Elements tableRows) {
        return getRowValue(tableRows, DATE_OF_COMPLETION_ROW_NAME);
    }

    private static Availability getAvailability(Elements tableRows) {
        String availabilityRowValue = getRowValue(tableRows, AVAILABILITY_ROW_NAME);
        if ("wolne".equalsIgnoreCase(availabilityRowValue)) {
            return Availability.FREE;
        } else if ("rezerwacja".equalsIgnoreCase(availabilityRowValue)) {
            return Availability.RESERVED;
        }
        return null;
    }

    private static String getDistrict(Document page) {
        Element districtTag = page.body().getElementsByClass("investmentitem-ilocation2 bold").first();
        return districtTag.text();
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

    private static InvestemntType getInvestmentType(Document page) {
        Element investmentTag = page.getElementById("newobjectdetails-data");
        if (investmentTag != null) {
            Element investmentTypeTag = investmentTag.getElementsByTag("h2").first();
            if (investmentTypeTag != null) {
                String investmentType = investmentTag.text();
                if (investmentType.contains("Mieszkanie")) {
                    return InvestemntType.APARTMENT;
                } else if (investmentType.contains("Dom")) {
                    return InvestemntType.HOUSE;
                }
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

    private static void initSpringContext() {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/application-context.xml");

        DominiumCrowler.dominiumOfferDao = (DominiumOfferDao) applicationContext.getBean("domminiumOfferDao");
    }
}
