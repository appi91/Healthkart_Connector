package com.hk.HKConnector.Util;

import com.google.common.base.*;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.util.*;

import javax.servlet.http.*;
import java.util.*;

public class HkUtil {

    public static String getColumnNameFromNumber(int columnNum) {
        String outputColumnName = "";
        int Base = 26;
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

        int TempNumber = columnNum;
        while (TempNumber > 0) {
            int position = TempNumber % Base;
            outputColumnName = (position == 0 ? 'Z' : chars.charAt(position > 0 ? position - 1 : 0)) + outputColumnName;
            TempNumber = (TempNumber - 1) / Base;
        }
        return outputColumnName;
    }

    public static String getRange(Integer startColumnNum, Integer endColumnNum, Integer startRowNum, Integer endRowNum) {
        if(startColumnNum == null || endColumnNum == null || startRowNum == null || endRowNum == null
            || startColumnNum == 0 || endColumnNum == 0 || startRowNum == 0 || endRowNum == 0)  {
            return null;
        }
        StringBuffer range = new StringBuffer(getColumnNameFromNumber(startColumnNum));
        range.append(startRowNum);
        if(!startColumnNum.equals(endColumnNum)) {
            range.append(":").append(getColumnNameFromNumber(endColumnNum))
                    .append(endRowNum);
        }
        return range.toString();
    }

    public static Cookie getCookie(HttpServletRequest httpServletRequest, String cookieName) {
        if (httpServletRequest.getCookies() != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie != null && cookie.getName() != null && cookie.getName().equals(cookieName)) {
                    return cookie;
                }
            }
        }
        return null;
    }

    public static String getBase64EncodedString(String toBeEncoded) {
        return Base64.getEncoder().encodeToString(toBeEncoded.getBytes());
    }

    public static String getDecodedStringEncodedByBase64(String toBeDecoded) {
        return new String(Base64.getDecoder().decode(toBeDecoded));
    }

    public static List<String> getDataTypesAllowedOnBulkConfig() {
        List<String> dataTypes = new LinkedList<>();
        dataTypes.add(Integer.class.getSimpleName());
        dataTypes.add(String.class.getSimpleName());
        dataTypes.add(Long.class.getSimpleName());
        dataTypes.add(Double.class.getSimpleName());
        dataTypes.add(Boolean.class.getSimpleName());
        dataTypes.add(int.class.getSimpleName());
        dataTypes.add(long.class.getSimpleName());
        dataTypes.add(double.class.getSimpleName());
        dataTypes.add(boolean.class.getSimpleName());
        return dataTypes;
    }

    public static <T> T parseInputToGivenClassTypeValue(String input, String className) throws ClassNotFoundException{
        Class c;
        switch (className) {
            case "boolean":
                c = boolean.class;
                if(input == null || input.isBlank()) {
                    return (T) Defaults.defaultValue(c);
                } else {
                    return (T) Boolean.valueOf(input);
                }
            case "int":
                c =  int.class;
                if(input == null || input.isBlank()) {
                    return (T) Defaults.defaultValue(c);
                } else {
                    return (T) Integer.valueOf(input);
                }
            case "long":
                c =  long.class;
                if(input == null || input.isBlank()) {
                    return (T) Defaults.defaultValue(c);
                } else {
                    return (T) Long.valueOf(input);
                }
            case "double":
                c =  double.class;
                if(input == null || input.isBlank()) {
                    return (T) Defaults.defaultValue(c);
                } else {
                    return (T) Double.valueOf(input);
                }
            case "Boolean":
                c = Boolean.class;
                if(input == null || input.isBlank()) {
                    return null;
                } else {
                    return (T) Boolean.valueOf(input);
                }
            case "Integer":
                c =  Integer.class;
                if(input == null || input.isBlank()) {
                    return null;
                } else {
                    return (T) Integer.valueOf(input);
                }
            case "Long":
                c =  Long.class;
                if(input == null || input.isBlank()) {
                    return null;
                } else {
                    return (T) Long.valueOf(input);
                }
            case "Double":
                c =  Double.class;
                if(input == null || input.isBlank()) {
                    return null;
                } else {
                    return (T) Double.valueOf(input);
                }
            case "String":
                c =  String.class;
                if(input == null || input.isBlank()) {
                    return null;
                } else {
                    return (T) input;
                }
            default:
                if(input == null || input.isBlank()) {
                    return null;
                } else {
                    throw new IllegalArgumentException("Class not found Exception.");
                }
        }
    }
}
