package com.pacekeeper.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DataManager {
    private static DataManager instance;

    private DataManager() {

    }

    public static DataManager getInstance() {
        if (instance == null) {
            instance = new DataManager();
        }
        return instance;
    }

    public String getDateWithTime(long time) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm aa", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.US);
        Calendar currentDate = Calendar.getInstance();
        int currentDay = currentDate.get(Calendar.DAY_OF_MONTH);
        int currentMonth = currentDate.get(Calendar.MONTH);
        int currentYear = currentDate.get(Calendar.YEAR);

        Calendar chosenDate = Calendar.getInstance();
        chosenDate.setTimeInMillis(time);
        int chosenDay = chosenDate.get(Calendar.DAY_OF_MONTH);
        int chosenMonth = chosenDate.get(Calendar.MONTH);
        int chosenYear = chosenDate.get(Calendar.YEAR);

        long diff = chosenDate.getTimeInMillis() - currentDate.getTimeInMillis();

        float dayCount = (float) diff / (24 * 60 * 60 * 1000);
        if (chosenDay == currentDay && chosenMonth == currentMonth && chosenYear == currentYear) {
            return "Today at " + timeFormat.format(new Date(time));
        } else if (dayCount == 1 && chosenMonth == currentMonth && chosenYear == currentYear) {
            return "Yesterday at " + timeFormat.format(new Date(time));
        } else {
            return dateFormat.format(new Date(time)) + " at " + timeFormat.format(new Date(time));
        }
    }
}
