package com.database.loader;

import com.database.model.Address;
import com.database.model.DistrictEnum;
import com.database.model.TaxOffice;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by miras108 on 2016-12-17.
 */
public class AddressLoader {

    public List<Address> loadAddresses(String sourcePatch) throws IOException {
        File fileDirs = new File(sourcePatch);
        BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(fileDirs), "UTF-8"));

        String currentLine;

        List<Address> addresses = new ArrayList<>();
        while ((currentLine = br.readLine()) != null) {
            Address address = parseLine(currentLine);
            if (address != null) {
                addresses.add(address);
            }
        }

        System.out.println("Loaded rows: " + addresses.size());

        return addresses;
    }

    private static Address parseLine(String line) {
        Address address = new Address();
        final String finalLine = line;
        DistrictEnum districtEnum = Arrays.asList(DistrictEnum.values())
                .stream()
                .filter(dis -> isDistrictEqual(finalLine, dis))
                .findFirst()
                .orElse(null);

        if (districtEnum != null) {
            address.setDistrict(districtEnum);
            line = line.replace(" " + districtEnum.getName(), "");
        } else {
            System.out.println("cannot parse district in line: " + line);
            return null;
        }

        Pattern taxOfficePattern = Pattern.compile("(.*) ([A-Z]{2})");
        Matcher taxOfficeMatcher = taxOfficePattern.matcher(line);
        if (taxOfficeMatcher.matches()) {
            String taxOfficeString = taxOfficeMatcher.group(2);
            TaxOffice taxOffice = Arrays.asList(TaxOffice.values()).stream()
                    .filter(office -> office.getName().equals(taxOfficeString))
                    .findFirst()
                    .orElse(null);
            address.setTaxOffice(taxOffice);
            line = line.substring(0, line.length() - 3);
        } else {
            System.out.println("cannot parse tax office in line: " + line);
            return null;
        }


        Pattern streetPattern = Pattern.compile("([A-Z]*) ([A-Z0-9 -.’]*)");
        Matcher streetMatcher = streetPattern.matcher(line);
        if (streetMatcher.matches()) {
            String street = streetMatcher.group(2);
            address.setStreet(street);
        } else {
            System.out.println("cannot parse street in line: " + line);
            return null;
        }

        return address;
    }

    private static boolean isDistrictEqual(String finalLine, DistrictEnum dis) {
        return finalLine.endsWith(dis.getName());
    }
}
