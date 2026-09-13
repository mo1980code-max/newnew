package com.clock.livewallpaper.quran.utils;

import com.clock.livewallpaper.quran.model.JuzItem;
import com.clock.livewallpaper.quran.model.SurahItem;

import java.util.ArrayList;
import java.util.List;

public class QuranDataProvider {

    public static final int TOTAL_PAGES = 604;
    public static final int TOTAL_SURAHS = 114;
    public static final int TOTAL_JUZ = 30;

    private static List<SurahItem> surahsList;
    private static List<JuzItem> juzList;

    public static synchronized List<SurahItem> getSurahs() {
        if (surahsList != null) {
            return surahsList;
        }
        surahsList = new ArrayList<SurahItem>();
        surahsList.add(new SurahItem(1, "الفاتحة", "Al-Fatihah", true, 7, 1));
        surahsList.add(new SurahItem(2, "البقرة", "Al-Baqarah", false, 286, 2));
        surahsList.add(new SurahItem(3, "آل عمران", "Ali 'Imran", false, 200, 50));
        surahsList.add(new SurahItem(4, "النساء", "An-Nisa", false, 176, 77));
        surahsList.add(new SurahItem(5, "المائدة", "Al-Ma'idah", false, 120, 106));
        surahsList.add(new SurahItem(6, "الأنعام", "Al-An'am", true, 165, 128));
        surahsList.add(new SurahItem(7, "الأعراف", "Al-A'raf", true, 206, 151));
        surahsList.add(new SurahItem(8, "الأنفال", "Al-Anfal", false, 75, 177));
        surahsList.add(new SurahItem(9, "التوبة", "At-Tawbah", false, 129, 187));
        surahsList.add(new SurahItem(10, "يونس", "Yunus", true, 109, 208));
        surahsList.add(new SurahItem(11, "هود", "Hud", true, 123, 221));
        surahsList.add(new SurahItem(12, "يوسف", "Yusuf", true, 111, 235));
        surahsList.add(new SurahItem(13, "الرعد", "Ar-Ra'd", false, 43, 249));
        surahsList.add(new SurahItem(14, "إبراهيم", "Ibrahim", true, 52, 255));
        surahsList.add(new SurahItem(15, "الحجر", "Al-Hijr", true, 99, 262));
        surahsList.add(new SurahItem(16, "النحل", "An-Nahl", true, 128, 267));
        surahsList.add(new SurahItem(17, "الإسراء", "Al-Isra", true, 111, 282));
        surahsList.add(new SurahItem(18, "الكهف", "Al-Kahf", true, 110, 293));
        surahsList.add(new SurahItem(19, "مريم", "Maryam", true, 98, 305));
        surahsList.add(new SurahItem(20, "طه", "Ta-Ha", true, 135, 312));
        surahsList.add(new SurahItem(21, "الأنبياء", "Al-Anbiya", true, 112, 322));
        surahsList.add(new SurahItem(22, "الحج", "Al-Hajj", false, 78, 332));
        surahsList.add(new SurahItem(23, "المؤمنون", "Al-Mu'minun", true, 118, 342));
        surahsList.add(new SurahItem(24, "النور", "An-Nur", false, 64, 350));
        surahsList.add(new SurahItem(25, "الفرقان", "Al-Furqan", true, 77, 359));
        surahsList.add(new SurahItem(26, "الشعراء", "Ash-Shu'ara", true, 227, 367));
        surahsList.add(new SurahItem(27, "النمل", "An-Naml", true, 93, 377));
        surahsList.add(new SurahItem(28, "القصص", "Al-Qasas", true, 88, 385));
        surahsList.add(new SurahItem(29, "العنكبوت", "Al-'Ankabut", true, 69, 396));
        surahsList.add(new SurahItem(30, "الروم", "Ar-Rum", true, 60, 404));
        surahsList.add(new SurahItem(31, "لقمان", "Luqman", true, 34, 411));
        surahsList.add(new SurahItem(32, "السجدة", "As-Sajdah", true, 30, 415));
        surahsList.add(new SurahItem(33, "الأحزاب", "Al-Ahzab", false, 73, 418));
        surahsList.add(new SurahItem(34, "سبأ", "Saba", true, 54, 428));
        surahsList.add(new SurahItem(35, "فاطر", "Fatir", true, 45, 434));
        surahsList.add(new SurahItem(36, "يس", "Ya-Sin", true, 83, 440));
        surahsList.add(new SurahItem(37, "الصافات", "As-Saffat", true, 182, 446));
        surahsList.add(new SurahItem(38, "ص", "Sad", true, 88, 453));
        surahsList.add(new SurahItem(39, "الزمر", "Az-Zumar", true, 75, 458));
        surahsList.add(new SurahItem(40, "غافر", "Ghafir", true, 85, 467));
        surahsList.add(new SurahItem(41, "فصلت", "Fussilat", true, 54, 477));
        surahsList.add(new SurahItem(42, "الشورى", "Ash-Shura", true, 53, 483));
        surahsList.add(new SurahItem(43, "الزخرف", "Az-Zukhruf", true, 89, 489));
        surahsList.add(new SurahItem(44, "الدخان", "Ad-Dukhan", true, 59, 496));
        surahsList.add(new SurahItem(45, "الجاثية", "Al-Jathiyah", true, 37, 499));
        surahsList.add(new SurahItem(46, "الأحقاف", "Al-Ahqaf", true, 35, 502));
        surahsList.add(new SurahItem(47, "محمد", "Muhammad", false, 38, 507));
        surahsList.add(new SurahItem(48, "الفتح", "Al-Fath", false, 29, 511));
        surahsList.add(new SurahItem(49, "الحجرات", "Al-Hujurat", false, 18, 515));
        surahsList.add(new SurahItem(50, "ق", "Qaf", true, 45, 518));
        surahsList.add(new SurahItem(51, "الذاريات", "Adh-Dhariyat", true, 60, 520));
        surahsList.add(new SurahItem(52, "الطور", "At-Tur", true, 49, 523));
        surahsList.add(new SurahItem(53, "النجم", "An-Najm", true, 62, 526));
        surahsList.add(new SurahItem(54, "القمر", "Al-Qamar", true, 55, 528));
        surahsList.add(new SurahItem(55, "الرحمن", "Ar-Rahman", false, 78, 531));
        surahsList.add(new SurahItem(56, "الواقعة", "Al-Waqi'ah", true, 96, 534));
        surahsList.add(new SurahItem(57, "الحديد", "Al-Hadid", false, 29, 537));
        surahsList.add(new SurahItem(58, "المجادلة", "Al-Mujadila", false, 22, 542));
        surahsList.add(new SurahItem(59, "الحشر", "Al-Hashr", false, 24, 545));
        surahsList.add(new SurahItem(60, "الممتحنة", "Al-Mumtahanah", false, 13, 549));
        surahsList.add(new SurahItem(61, "الصف", "As-Saff", false, 14, 551));
        surahsList.add(new SurahItem(62, "الجمعة", "Al-Jumu'ah", false, 11, 553));
        surahsList.add(new SurahItem(63, "المنافقون", "Al-Munafiqun", false, 11, 554));
        surahsList.add(new SurahItem(64, "التغابن", "At-Taghabun", false, 18, 556));
        surahsList.add(new SurahItem(65, "الطلاق", "At-Talaq", false, 12, 558));
        surahsList.add(new SurahItem(66, "التحريم", "At-Tahrim", false, 12, 560));
        surahsList.add(new SurahItem(67, "الملك", "Al-Mulk", true, 30, 562));
        surahsList.add(new SurahItem(68, "القلم", "Al-Qalam", true, 52, 564));
        surahsList.add(new SurahItem(69, "الحاقة", "Al-Haqqah", true, 52, 566));
        surahsList.add(new SurahItem(70, "المعارج", "Al-Ma'arij", true, 44, 568));
        surahsList.add(new SurahItem(71, "نوح", "Nuh", true, 28, 570));
        surahsList.add(new SurahItem(72, "الجن", "Al-Jinn", true, 28, 572));
        surahsList.add(new SurahItem(73, "المزمل", "Al-Muzzammil", true, 20, 574));
        surahsList.add(new SurahItem(74, "المدثر", "Al-Muddaththir", true, 56, 575));
        surahsList.add(new SurahItem(75, "القيامة", "Al-Qiyamah", true, 40, 577));
        surahsList.add(new SurahItem(76, "الإنسان", "Al-Insan", false, 31, 578));
        surahsList.add(new SurahItem(77, "المرسلات", "Al-Mursalat", true, 50, 580));
        surahsList.add(new SurahItem(78, "النبأ", "An-Naba", true, 40, 582));
        surahsList.add(new SurahItem(79, "النازعات", "An-Nazi'at", true, 46, 583));
        surahsList.add(new SurahItem(80, "عبس", "'Abasa", true, 42, 585));
        surahsList.add(new SurahItem(81, "التكوير", "At-Takwir", true, 29, 586));
        surahsList.add(new SurahItem(82, "الانفطار", "Al-Infitar", true, 19, 587));
        surahsList.add(new SurahItem(83, "المطففين", "Al-Mutaffifin", true, 36, 587));
        surahsList.add(new SurahItem(84, "الانشقاق", "Al-Inshiqaq", true, 25, 589));
        surahsList.add(new SurahItem(85, "البروج", "Al-Buruj", true, 22, 590));
        surahsList.add(new SurahItem(86, "الطارق", "At-Tariq", true, 17, 591));
        surahsList.add(new SurahItem(87, "الأعلى", "Al-A'la", true, 19, 591));
        surahsList.add(new SurahItem(88, "الغاشية", "Al-Ghashiyah", true, 26, 592));
        surahsList.add(new SurahItem(89, "الفجر", "Al-Fajr", true, 30, 593));
        surahsList.add(new SurahItem(90, "البلد", "Al-Balad", true, 20, 594));
        surahsList.add(new SurahItem(91, "الشمس", "Ash-Shams", true, 15, 595));
        surahsList.add(new SurahItem(92, "الليل", "Al-Layl", true, 21, 595));
        surahsList.add(new SurahItem(93, "الضحى", "Ad-Duha", true, 11, 596));
        surahsList.add(new SurahItem(94, "الشرح", "Ash-Sharh", true, 8, 596));
        surahsList.add(new SurahItem(95, "التين", "At-Tin", true, 8, 597));
        surahsList.add(new SurahItem(96, "العلق", "Al-'Alaq", true, 19, 597));
        surahsList.add(new SurahItem(97, "القدر", "Al-Qadr", true, 5, 598));
        surahsList.add(new SurahItem(98, "البينة", "Al-Bayyinah", false, 8, 598));
        surahsList.add(new SurahItem(99, "الزلزلة", "Az-Zalzalah", false, 8, 599));
        surahsList.add(new SurahItem(100, "العاديات", "Al-'Adiyat", true, 11, 599));
        surahsList.add(new SurahItem(101, "القارعة", "Al-Qari'ah", true, 11, 600));
        surahsList.add(new SurahItem(102, "التكاثر", "At-Takathur", true, 8, 600));
        surahsList.add(new SurahItem(103, "العصر", "Al-'Asr", true, 3, 601));
        surahsList.add(new SurahItem(104, "الهمزة", "Al-Humazah", true, 9, 601));
        surahsList.add(new SurahItem(105, "الفيل", "Al-Fil", true, 5, 601));
        surahsList.add(new SurahItem(106, "قريش", "Quraysh", true, 4, 602));
        surahsList.add(new SurahItem(107, "الماعون", "Al-Ma'un", true, 7, 602));
        surahsList.add(new SurahItem(108, "الكوثر", "Al-Kawthar", true, 3, 602));
        surahsList.add(new SurahItem(109, "الكافرون", "Al-Kafirun", true, 6, 603));
        surahsList.add(new SurahItem(110, "النصر", "An-Nasr", false, 3, 603));
        surahsList.add(new SurahItem(111, "المسد", "Al-Masad", true, 5, 603));
        surahsList.add(new SurahItem(112, "الإخلاص", "Al-Ikhlas", true, 4, 604));
        surahsList.add(new SurahItem(113, "الفلق", "Al-Falaq", true, 5, 604));
        surahsList.add(new SurahItem(114, "الناس", "An-Nas", true, 6, 604));

        return surahsList;
    }

    public static synchronized List<JuzItem> getJuzList() {
        if (juzList != null) {
            return juzList;
        }
        juzList = new ArrayList<JuzItem>();
        juzList.add(new JuzItem(1, "الجزء الأول", "Juz 1", "الفاتحة", "Al-Fatihah", "الم", "Alif-Lam-Mim", 1));
        juzList.add(new JuzItem(2, "الجزء الثاني", "Juz 2", "البقرة", "Al-Baqarah", "سَيَقُولُ السُّفَهَاءُ", "Sayakul us-sufaha'", 22));
        juzList.add(new JuzItem(3, "الجزء الثالث", "Juz 3", "البقرة", "Al-Baqarah", "تِلْكَ الرُّسُلُ", "Tilka r-rusul", 42));
        juzList.add(new JuzItem(4, "الجزء الرابع", "Juz 4", "آل عمران", "Ali 'Imran", "لَنْ تَنَالُوا الْبِرَّ", "Lan tanalu l-birr", 62));
        juzList.add(new JuzItem(5, "الجزء الخامس", "Juz 5", "النساء", "An-Nisa", "وَالْمُحْصَنَاتُ", "Wal-muhsanat", 82));
        juzList.add(new JuzItem(6, "الجزء السادس", "Juz 6", "النساء", "An-Nisa", "لَا يُحِبُّ اللَّهُ", "La yuhibbu llah", 102));
        juzList.add(new JuzItem(7, "الجزء السابع", "Juz 7", "المائدة", "Al-Ma'idah", "وَإِذَا سَمِعُوا", "Wa idha sami'u", 122));
        juzList.add(new JuzItem(8, "الجزء الثامن", "Juz 8", "الأنعام", "Al-An'am", "وَلَوْ أَنَّنَا نَزَّلْنَا", "Wa law annana nazzalna", 142));
        juzList.add(new JuzItem(9, "الجزء التاسع", "Juz 9", "الأعراف", "Al-A'raf", "قَالَ الْمَلَأُ", "Qal al-mala'", 162));
        juzList.add(new JuzItem(10, "الجزء العاشر", "Juz 10", "الأنفال", "Al-Anfal", "وَاعْلَمُوا أَنَّمَا غَنِمْتُمْ", "Wa'lamu annama ghanimtum", 182));
        juzList.add(new JuzItem(11, "الجزء الحادي عشر", "Juz 11", "التوبة", "At-Tawbah", "يَعْتَذِرُونَ إِلَيْكُمْ", "Ya'tadhiruna ilaykum", 202));
        juzList.add(new JuzItem(12, "الجزء الثاني عشر", "Juz 12", "هود", "Hud", "وَمَا مِنْ دَابَّةٍ", "Wa ma min dabbah", 222));
        juzList.add(new JuzItem(13, "الجزء الثالث عشر", "Juz 13", "يوسف", "Yusuf", "وَمَا أُبَرِّئُ نَفْسِي", "Wa ma ubarri'u nafsi", 242));
        juzList.add(new JuzItem(14, "الجزء الرابع عشر", "Juz 14", "الحجر", "Al-Hijr", "الر تِلْكَ آيَاتُ", "Alif-Lam-Ra tilka ayat", 262));
        juzList.add(new JuzItem(15, "الجزء الخامس عشر", "Juz 15", "الإسراء", "Al-Isra", "سُبْحَانَ الَّذِي أَسْرَىٰ", "Subhan alladhi asra", 282));
        juzList.add(new JuzItem(16, "الجزء السادس عشر", "Juz 16", "الكهف", "Al-Kahf", "قَالَ أَلَمْ أَقُلْ لَكَ", "Qala alam aqul laka", 302));
        juzList.add(new JuzItem(17, "الجزء السابع عشر", "Juz 17", "الأنبياء", "Al-Anbiya", "اقْتَرَبَ لِلنَّاسِ", "Iqtaraba lin-nas", 322));
        juzList.add(new JuzItem(18, "الجزء الثامن عشر", "Juz 18", "المؤمنون", "Al-Mu'minun", "قَدْ أَفْلَحَ الْمُؤْمِنُونَ", "Qad aflaha l-mu'minun", 342));
        juzList.add(new JuzItem(19, "الجزء التاسع عشر", "Juz 19", "الفرقان", "Al-Furqan", "وَقَالَ الَّذِينَ لَا يَرْجُونَ", "Wa qal alladhina la yarjun", 362));
        juzList.add(new JuzItem(20, "الجزء العشرون", "Juz 20", "النمل", "An-Naml", "فَمَا كَانَ جَوَابَ قَوْمِهِ", "Fama kana jawaba qawmih", 382));
        juzList.add(new JuzItem(21, "الجزء الحادي والعشرون", "Juz 21", "العنكبوت", "Al-'Ankabut", "وَلَا تُجَادِلُوا", "Wa la tujadilu", 402));
        juzList.add(new JuzItem(22, "الجزء الثاني والعشرون", "Juz 22", "الأحزاب", "Al-Ahzab", "وَمَنْ يَقْنُتْ مِنْكُنَّ", "Wa man yaqnut minkunna", 422));
        juzList.add(new JuzItem(23, "الجزء الثالث والعشرون", "Juz 23", "يس", "Ya-Sin", "وَمَا أَنْزَلْنَا عَلَىٰ قَوْمِهِ", "Wa ma anzalna 'ala qawmih", 442));
        juzList.add(new JuzItem(24, "الجزء الرابع والعشرون", "Juz 24", "الزمر", "Az-Zumar", "فَمَنْ أَظْلَمُ", "Faman adhlam", 462));
        juzList.add(new JuzItem(25, "الجزء الخامس والعشرون", "Juz 25", "فصلت", "Fussilat", "إِلَيْهِ يُرَدُّ عِلْمُ السَّاعَةِ", "Ilayhi yuraddu 'ilmu s-sa'ah", 482));
        juzList.add(new JuzItem(26, "الجزء السادس والعشرون", "Juz 26", "الأحقاف", "Al-Ahqaf", "حم تَنْزِيلُ الْكِتَابِ", "Ha-Mim tanzilu l-kitab", 502));
        juzList.add(new JuzItem(27, "الجزء السابع والعشرون", "Juz 27", "الذاريات", "Adh-Dhariyat", "قَالَ فَمَا خَطْبُكُمْ", "Qala fama khatbukum", 522));
        juzList.add(new JuzItem(28, "الجزء الثامن والعشرون", "Juz 28", "المجادلة", "Al-Mujadila", "قَدْ سَمِعَ اللَّهُ", "Qad sami'a llah", 542));
        juzList.add(new JuzItem(29, "الجزء التاسع والعشرون", "Juz 29", "الملك", "Al-Mulk", "تَبَارَكَ الَّذِي بِيَدِهِ الْمُلْكُ", "Tabarak alladhi biyadihi l-mulk", 562));
        juzList.add(new JuzItem(30, "الجزء الثلاثون", "Juz 30", "النبأ", "An-Naba", "عَمَّ يَتَسَاءَلُونَ", "'Amma yatasa'alun", 582));

        return juzList;
    }

    public static SurahItem getSurahForPage(int page) {
        List<SurahItem> list = getSurahs();
        SurahItem match = list.get(0);
        for (SurahItem item : list) {
            if (item.getStartPage() <= page) {
                match = item;
            } else {
                break;
            }
        }
        return match;
    }

    public static int getJuzNumberForPage(int page) {
        List<JuzItem> list = getJuzList();
        int j = 1;
        for (JuzItem item : list) {
            if (item.getStartPage() <= page) {
                j = item.getJuzNumber();
            } else {
                break;
            }
        }
        return j;
    }

    public static String getPageHeaderInfo(int page, boolean isArabic) {
        SurahItem surah = getSurahForPage(page);
        int juz = getJuzNumberForPage(page);
        if (isArabic) {
            return "سورة " + surah.getNameAr() + " • الجزء " + juz;
        } else {
            return "Surah " + surah.getNameEn() + " • Juz " + juz;
        }
    }
}
