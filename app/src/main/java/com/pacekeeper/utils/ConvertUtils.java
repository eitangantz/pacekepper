package com.pacekeeper.utils;

public class ConvertUtils {
    public static double metersToConfigured(double meters) {
        return meters * (PrefUtils.getInstance().getMeasurement().equals("Miles") ? 0.621371 : 1) / 1000;
    }

    public static double kilometersToConfigured(double meters) {
        return meters * (PrefUtils.getInstance().getMeasurement().equals("Miles") ? 0.621371 : 1);
    }

    public static double getMeters(double distance) {
        if (PrefUtils.getInstance().getMeasurement().equals("Miles")) {
            return distance / 0.0006213711;
        } else {
            return distance * 1000;
        }
    }
}
