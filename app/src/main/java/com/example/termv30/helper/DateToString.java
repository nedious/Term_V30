package com.example.termv30.helper;

import androidx.room.TypeConverter;

import java.time.LocalDate;

public class DateToString {

    @TypeConverter
    public static LocalDate toDate(String dateString) {
        return dateString == null ? null : LocalDate.parse(dateString);
    }

    @TypeConverter
    public static String toDateString(LocalDate date) {
        return date == null ? null : date.toString();
    }
}