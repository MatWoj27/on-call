package com.mattech.on_call.utils;

import android.support.annotation.NonNull;

import java.util.regex.Pattern;

public class WebUtil {
    private static final String ZERO_TO_255 = "([01]?[0-9]{1,2}|2[0-4][0-9]|25[0-5])";
    private static final String IPV4_REGEX = ZERO_TO_255 + "\\." + ZERO_TO_255 + "\\." +
            ZERO_TO_255 + "\\." + ZERO_TO_255;
    private static final String DECIMAL_5_DIGITS = "^[0-9]{0,5}$";
    private static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
    private static final Pattern DECIMAL_5_DIGITS_PATTERN = Pattern.compile(DECIMAL_5_DIGITS);

    public static boolean isValidIPv4(@NonNull String address) {
        return IPV4_PATTERN.matcher(address).matches();
    }

    public static boolean isValidPortNumber(@NonNull String portNumber) {
        return DECIMAL_5_DIGITS_PATTERN.matcher(portNumber).matches() && (portNumber.isEmpty() || Integer.parseInt(portNumber) <= 65535);
    }

    @NonNull
    public static String removeIPv4LeadingZeros(@NonNull String address) {
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

    @NonNull
    public static String removeLeadingZeros(@NonNull String number) {
        return number.replaceFirst("^0+(?!$)", "");
    }
}
