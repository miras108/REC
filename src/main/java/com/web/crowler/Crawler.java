package com.web.crowler;

import com.database.loader.AddressLoader;
import com.database.model.Address;
import com.database.model.DistrictEnum;
import com.database.model.Market;
import com.database.model.Offer;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Created by miras108 on 2016-11-01.
 */
public class Crawler {
    private static List<Address> addresses;

    public static void main(String[] args) throws IOException {

        initSpringContext();

        if (args.length != 1) {
            throw new InputMismatchException("Invalid parameters, please provide source file patch");
        }

        addresses = new AddressLoader().loadAddresses(args[0]);

        String firstPageUrl = "https://otodom.pl/sprzedaz/mieszkanie/krakow/?search%5Bdescription%5D=1&search%5Bdist%5D=0";
        Document page = Jsoup.connect(firstPageUrl).get();
        Elements newsHeadlines = page.select("[div]");

        Set<String> mainPagesUrls = new HashSet<>();
        mainPagesUrls.add(firstPageUrl);

        List<Document> mainPages = new ArrayList<>();
        mainPages.add(page);

        Elements nextPageElement = null;
        do {
            nextPageElement = page.body().getElementsByAttributeValue("data-dir", "next");
            if (nextPageElement != null && !nextPageElement.isEmpty()) {
                String nextPageUrl = nextPageElement.get(0).attr("href");
                System.out.println(nextPageUrl);
                mainPagesUrls.add(nextPageUrl);
                page = Jsoup.connect(nextPageUrl).get();
                mainPages.add(page);
            }
        } while (nextPageElement != null && !nextPageElement.isEmpty() &&
                // temporary
                mainPages.size() < 3);

        Set<String> offerURLs = new HashSet<>();

        System.out.println("");
        System.out.println("Offers");

        List<Offer> offerList = new ArrayList<>();
        for (Document currentPage : mainPages) {
            currentPage.body().getElementsByClass("col-md-content").get(0).getElementsByTag("article").forEach(article -> {
                String url = article.attr("data-url");
                System.out.println(url);
                offerURLs.add(url);

                try {
                    offerList.add(parseOffer(url));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }

        Object[] offers = newsHeadlines.toArray();

        Arrays.asList(offers).forEach(offer -> System.out.println(offer));

    }

    private static void initSpringContext() {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/application-context.xml");
    }

    private static Offer parseOffer(String offerUrl) throws IOException {
        Offer offer = new Offer();
        offer.setUrl(offerUrl);

        Document page = Jsoup.connect(offerUrl).get();
        Elements addresElement = page.body().getElementsByAttributeValue("itemprop", "address");
        if (addresElement != null && !addresElement.isEmpty()) {
            String rawAddress = addresElement.get(0).text();
            offer.setRawAddress(rawAddress);
            offer.setAddress(resolveAddress(rawAddress));
        }

        Elements priceElement = page.getElementsMatchingOwnText("^cena ");
        priceElement.stream()
                .filter(price -> isCorrectPriceElement(price))
                .findFirst()
                .ifPresent(price -> {
                    offer.setPrice(convertToDouble(((TextNode) price.childNode(2)).text()));
                    offer.setTotalPrice(getTotalPrice(price));
                });

        Elements yardageElement = page.getElementsMatchingOwnText("^powierzchnia");
        yardageElement.stream()
                .filter(element -> "li".equals(element.tag().getName()))
                .findFirst()
                .ifPresent(yardage -> offer.setYardage(convertToDouble((((TextNode) yardage.childNode(1).childNode(0).childNode(0)).text()))));

        Elements numberOfRoomsElemet = page.getElementsMatchingOwnText("^liczba pokoi");
        numberOfRoomsElemet.stream()
                .filter(element -> "li".equals(element.tag().getName()))
                .findFirst()
                .ifPresent(rooms -> offer.setNumberOfRooms(convertToShort(((TextNode) rooms.childNode(1).childNode(0).childNode(0)).text())));

        Elements floorElement = page.getElementsMatchingOwnText("^piętro");
        floorElement.stream()
                .filter(element -> "li".equals(element.tag().getName()))
                .findFirst()
                .ifPresent(floor ->
                {
                    offer.setFloor(convertToShort(((TextNode) floor.childNode(1).childNode(0).childNode(0)).text()));
                    if (floor.childNode(1).childNodeSize() > 1) {
                        offer.setMaxFloor(convertToShort(((TextNode) floor.childNode(1).childNode(1)).text()));
                    }
                });

        Elements marketElement = page.getElementsMatchingOwnText("^rynek:");
        marketElement.stream()
                .filter(element -> "strong".equals(element.tag().getName()))
                .findFirst()
                .ifPresent(market -> offer.setMarket(getMarket((TextNode) market.parent().childNode(1))));

        Elements constructionYearElement = page.getElementsMatchingOwnText("^rok budowy:");
        constructionYearElement.stream()
                .filter(element -> "strong".equals(element.tag().getName()))
                .findFirst()
                .ifPresent(market -> offer.setYearOfConstruction(convertToInt(((TextNode) market.parent().childNode(1)).text())));

        return offer;
    }

    private static Address resolveAddress(String rawAddress) {

        List<Address> addresses = resolveAddressWithFullAddress(rawAddress);

        if (addresses.size() > 1) {
            List<Address> addressesReslovedWithStreet = resolveAddressWithStreet(rawAddress);
            if (!addressesReslovedWithStreet.isEmpty() && addressesReslovedWithStreet.size() < addresses.size()) {
                addresses = addressesReslovedWithStreet;
            }

            if (addresses.size() > 1) {
                Address addressByDistrict = resolveAddressWithDistrict(rawAddress);

                if (addressByDistrict != null) {
                    return addressByDistrict;
                }

                if (addresses.size() > 1) {
                    System.out.println("Multpile address elements found for address: " + rawAddress);
                }
            }
        }

        if (addresses.isEmpty()) {
            addresses = resolveAddressWithFullAddress(rawAddress);
            if (addresses.isEmpty() || addresses.size() > 1) {
                Address addressByDistrict = resolveAddressWithDistrict(rawAddress);
                if (addressByDistrict != null) {
                    return addressByDistrict;
                }
            }

            if (addresses.size() > 1) {
                System.out.println("Multpile address elements found for address: " + rawAddress);
            }

            if (addresses.isEmpty()) {
                System.out.println("Cannot find Address element fot address: " + rawAddress);
                return null;
            }
        }

        return addresses.get(0);
    }

    private static Address resolveAddressWithDistrict(String rawAddress) {
        rawAddress = formatString(rawAddress);
        rawAddress = rawAddress.replaceAll("[^a-zA-Z]", "");

        final String finalRawAddress = rawAddress;
        DistrictEnum districtEnum = Arrays.asList(DistrictEnum.values()).stream()
                .filter(district -> StringUtils.containsIgnoreCase(finalRawAddress, district.getName().replaceAll("[^a-zA-Z]", "")))
                .findAny()
                .orElse(null);

        if (districtEnum == null) {
            if (StringUtils.containsIgnoreCase(rawAddress, "Kazimierz")) {
                districtEnum = DistrictEnum.STARE_MIASTO;
            } else if (StringUtils.containsIgnoreCase(rawAddress, "Azory")) {
                districtEnum = DistrictEnum.PRADNIK_BIALY;
            } else if (StringUtils.containsIgnoreCase(rawAddress, "Krowodrza")) {
                districtEnum = DistrictEnum.LOBZOW;
            } else if (StringUtils.containsIgnoreCase(rawAddress, "Ruczaj")) {
                districtEnum = DistrictEnum.DEBNIKI;
            } else if (StringUtils.containsIgnoreCase(rawAddress, "Kurdwanow")) {
                districtEnum = DistrictEnum.WOLA_DUCHACKA;
            } else if (StringUtils.containsIgnoreCase(rawAddress, "Olsza")) {
                districtEnum = DistrictEnum.PRADNIK_CZERWONY;
            } else if (StringUtils.containsIgnoreCase(rawAddress, "Salwator")) {
                districtEnum = DistrictEnum.ZWIERZYNIEC;
            } else if (StringUtils.containsIgnoreCase(rawAddress, "Biezanow")) {
                districtEnum = DistrictEnum.ZWIERZYNIEC;
            }
        }

        if (districtEnum != null) {
            Address address = new Address();
            address.setDistrict(districtEnum);
            return address;
        }
        return null;
    }

    private static List<Address> resolveAddressWithStreet(String rawAddress) {
        String rawStreet = getRawStreet(rawAddress);

        if (rawStreet != null) {
            List<Address> matchedAddresses = addresses.stream()
                    .filter(address -> findAddress(rawStreet, address))
                    .collect(Collectors.toList());

            if (matchedAddresses.isEmpty()) {
                System.out.println("Cannot find addres element for street: " + rawStreet);
                return new ArrayList<>();
            }
            return matchedAddresses;
        }

        return new ArrayList<>();
    }

    private static List<Address> resolveAddressWithFullAddress(String rawAddress) {
        rawAddress = formatString(rawAddress);
        final String finalRawAddress = rawAddress;
        List<Address> matchedAddresses = addresses.stream()
                .filter(address -> findAddress(finalRawAddress, address))
                .collect(Collectors.toList());

        if (matchedAddresses.isEmpty()) {
            return new ArrayList<>();
        }

        return matchedAddresses;
    }

    private static boolean findAddress(String rawAddress, Address address) {
        return address.getStreet().contains(rawAddress.toUpperCase())
                || rawAddress.toUpperCase().contains(address.getStreet());
    }

    protected static String getRawStreet(String rawAddress) {
        rawAddress = formatString(rawAddress);

        Pattern pattern = Pattern.compile("(.*), (.*), ([A-Za-z -.]*)");
        Matcher matcher = pattern.matcher(rawAddress);

        if (matcher.matches()) {
            return matcher.group(3);
        }
        return null;
    }

    private static String formatString(String rawAddress) {
        rawAddress = rawAddress.toUpperCase();
        rawAddress = rawAddress.replace("Ą", "A")
                .replace("Ć", "C")
                .replace("Ę", "E")
                .replace("Ł", "L")
                .replace("Ń", "N")
                .replace("Ó", "O")
                .replace("Ś", "s")
                .replace("Ż", "Z")
                .replace("Ź", "Z");
        return rawAddress;
    }

    private static Market getMarket(TextNode marketNode) {
        if (marketNode.text().contains("pierwotny")) {
            return Market.PRIMARY_MARKET;
        } else if (marketNode.text().contains("wtórny")) {
            return Market.AFTER_MARKET;
        }
        return null;
    }

    private static double getTotalPrice(Element price) {
        return convertToDouble(((TextNode) price.childNode(1).childNode(0).childNode(0)).text());
    }

    private static boolean isCorrectPriceElement(Element price) {
        if (price.childNodes().size() == 3) {
            return (price.childNode(0) instanceof TextNode && ((TextNode) price.childNode(0)).text().equals("cena ")
                    && price.childNode(2) instanceof TextNode && ((TextNode) price.childNode(2)).text().contains("zł/m²"));
        }
        return false;
    }

    private static Double convertToDouble(String text) {
        String stringDouble = text.replaceAll("[^\\d,]", "").replace(",", ".");
        return stringDouble.isEmpty() ? null : Double.valueOf(stringDouble);
    }

    private static Integer convertToInt(String text) {
        String stringInteger = text.replaceAll("[^\\d,]", "").replace(",", ".");
        return stringInteger.isEmpty() ? null : Integer.valueOf(stringInteger);
    }

    private static Short convertToShort(String text) {
        String stringShort = text.replaceAll("[^\\d,]", "").replace(",", ".");
        return stringShort.isEmpty() ? 0 : Short.valueOf(stringShort);
    }
}
