package com.mattech.on_call.utils;

import java.util.regex.Pattern;

public class IpAddressUtil {
    private static final String ZERO_TO_255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";
    private static final String IPV4_REGEX = ZERO_TO_255 + "\\." + ZERO_TO_255 + "\\." +
            ZERO_TO_255 + "\\." + ZERO_TO_255;
    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);

    public static boolean isValidIPv4(String address) {
        return IPV4_PATTERN.matcher(address).matches();
    }

    public static String removeIPv4LeadingZeros(String address) {
        StringBuilder result = new StringBuilder();
        String[] octets = address.split("\\.");
        for (String octet : octets) {
            if (result.length() > 0) {
                result.append(".");
            }
            result.append(octet.replaceFirst("^0+(?!$)", ""));
        }
        return result.toString();
    }
}
