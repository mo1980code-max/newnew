package org.Allah_Clock_Live_Wallpaper.utils;

/**
 * Qibla maths plus the bundled city table.
 *
 * <p>The city table exists on purpose: asking for {@code ACCESS_FINE_LOCATION} just to point
 * at the Kaaba would add a runtime permission, a Play Console data-safety declaration and a
 * battery cost for nothing. Picking a city works offline, on every device, forever.</p>
 */
public final class QiblaUtil {

    /** Kaaba, Masjid al-Haram, Makkah. */
    public static final double KAABA_LAT = 21.422487;
    public static final double KAABA_LON = 39.826206;

    private static final double EARTH_RADIUS_KM = 6371.0;

    private QiblaUtil() {
    }

    /**
     * Initial great-circle bearing from {@code (lat, lon)} to the Kaaba,
     * in degrees clockwise from true north, normalised to [0, 360).
     */
    public static float bearingFromNorth(double lat, double lon) {
        double latRad = Math.toRadians(lat);
        double dLonRad = Math.toRadians(KAABA_LON - lon);
        double y = Math.sin(dLonRad);
        double x = Math.cos(latRad) * Math.tan(Math.toRadians(KAABA_LAT))
                - Math.sin(latRad) * Math.cos(dLonRad);
        double bearing = Math.toDegrees(Math.atan2(y, x));
        return (float) ((bearing + 360.0) % 360.0);
    }

    /** Great-circle distance to the Kaaba in whole kilometres. */
    public static int distanceKm(double lat, double lon) {
        double dLat = Math.toRadians(KAABA_LAT - lat);
        double dLon = Math.toRadians(KAABA_LON - lon);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat)) * Math.cos(Math.toRadians(KAABA_LAT))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(EARTH_RADIUS_KM * c);
    }

    /** Proper nouns on purpose: they read the same in every language. */
    public static final String[] CITY_NAMES = {
            "Amman", "Jerusalem", "Gaza", "Makkah", "Madinah", "Riyadh", "Jeddah",
            "Cairo", "Alexandria", "Khartoum", "Tripoli", "Tunis", "Algiers",
            "Casablanca", "Rabat", "Nouakchott", "Dakar", "Lagos", "Kano",
            "Mogadishu", "Djibouti", "Sana'a", "Aden", "Muscat", "Dubai",
            "Abu Dhabi", "Doha", "Manama", "Kuwait City", "Baghdad", "Basra",
            "Erbil", "Aleppo", "Damascus", "Beirut", "Tehran", "Mashhad",
            "Kabul", "Karachi", "Lahore", "Islamabad", "Peshawar", "Dhaka",
            "Chittagong", "Malé", "Colombo", "Kuala Lumpur", "Jakarta",
            "Surabaya", "Bandar Seri Begawan", "Manila", "Istanbul", "Ankara",
            "Konya", "Baku", "Tashkent", "Almaty", "Bishkek", "Dushanbe",
            "Ashgabat", "Kazan", "Sarajevo", "Tirana", "Pristina", "London",
            "Paris", "Berlin", "Madrid", "Rome", "New York", "Toronto",
            "Sydney", "Johannesburg",
    };

    /** Parallel to {@link #CITY_NAMES}: {@code {latitude, longitude}}. */
    public static final double[][] CITY_COORDS = {
            {31.9539, 35.9106}, {31.7683, 35.2137}, {31.5017, 34.4668},
            {21.4225, 39.8262}, {24.4672, 39.6111}, {24.7136, 46.6753},
            {21.4858, 39.1925}, {30.0444, 31.2357}, {31.2001, 29.9187},
            {15.5007, 32.5599}, {32.8872, 13.1913}, {36.8065, 10.1815},
            {36.7538, 3.0588}, {33.5731, -7.5898}, {34.0209, -6.8416},
            {18.0735, -15.9582}, {14.7167, -17.4677}, {6.5244, 3.3792},
            {12.0022, 8.5920}, {2.0469, 45.3182}, {11.8251, 42.5903},
            {15.3694, 44.1910}, {12.7855, 45.0187}, {23.5880, 58.3829},
            {25.2048, 55.2708}, {24.4539, 54.3773}, {25.2854, 51.5310},
            {26.2285, 50.5860}, {29.3759, 47.9774}, {33.3152, 44.3661},
            {30.5085, 47.7804}, {36.1901, 44.0092}, {36.2021, 37.1343},
            {33.5138, 36.2765}, {33.8938, 35.5018}, {35.6892, 51.3890},
            {36.2605, 59.6168}, {34.5553, 69.2075}, {24.8607, 67.0011},
            {31.5204, 74.3587}, {33.6844, 73.0479}, {34.0151, 71.5249},
            {23.8103, 90.4125}, {22.3569, 91.7832}, {4.1755, 73.5093},
            {6.9271, 79.8612}, {3.1390, 101.6869}, {-6.2088, 106.8456},
            {-7.2575, 112.7521}, {4.9031, 114.9398}, {14.5995, 120.9842},
            {41.0082, 28.9784}, {39.9334, 32.8597}, {37.8746, 32.4932},
            {40.4093, 49.8671}, {41.2995, 69.2401}, {43.2220, 76.8512},
            {42.8746, 74.5698}, {38.5598, 68.7870}, {37.9771, 58.3286},
            {55.7963, 49.1088}, {43.8563, 18.4131}, {41.3275, 19.8187},
            {42.6629, 21.1655}, {51.5074, -0.1278}, {48.8566, 2.3522},
            {52.5200, 13.4050}, {40.4168, -3.7038}, {41.9028, 12.4964},
            {40.7128, -74.0060}, {43.6532, -79.3832}, {-33.8688, 151.2093},
            {-26.2041, 28.0473},
    };
}
