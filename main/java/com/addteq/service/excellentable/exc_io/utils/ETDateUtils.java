/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addteq.service.excellentable.exc_io.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
/**
 *
 * @author vikashkumar
 */
public class ETDateUtils {  
    public static Date currentTime() {
        Date date = new Date(System.currentTimeMillis());
        return date;
    }
    
    public static String getFormattedDate(Date date) {
        if(date != null){
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, h:mm a");
            return sdf.format(date);
        }
        return null;
    }
    /**
     * Change a string date to Date object.
     * @param stringToDate The string value representing a date.
     * @return A Date object corresponding to the given string date.
     * @throws ParseException 
     */
    public static Date stringToDate(String stringToDate) throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat("EEEE, dd/MM/yyyy/hh:mm:ss");
        Date parsedDate = formatter.parse(stringToDate);
        return parsedDate;
    }
    
    /* 
    * Referred: http://stackoverflow.com/questions/23670516/what-is-the-equivalent-of-datetime-fromoadate-in-java-double-to-datetime-in-j
    * And modified as per the requirement.
    * i.e IF condition is there in the POST but we removed that 
    * because if the date double value is negative then it return 29 Dec 1899 date object.
    * which is not required in our case hence removed this IF condition.
    */
    public static Date fromDoubleToDateTime(double OADate) {
        long num = (long) ((OADate * 86400000.0) + ((OADate >= 0.0) ? 0.5 : -0.5));
        num += 0x3680b5e1fc00L;
        num -= 62135596800000L;

        return new Date(num);
    }
}