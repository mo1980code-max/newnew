package com.clock.livewallpaper.utils;

import com.clock.livewallpaper.model.OccasionItem;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class OccasionDhikrManager {

    public static List<OccasionItem> getOccasionsForCalendar(Calendar cal) {
        List<OccasionItem> list = new ArrayList<OccasionItem>();
        int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
        HijriCalendarHelper.HijriDate hijri = HijriCalendarHelper.getHijriDate(cal);

        boolean isFriday = (dayOfWeek == Calendar.FRIDAY);
        boolean isMonday = (dayOfWeek == Calendar.MONDAY);
        boolean isThursday = (dayOfWeek == Calendar.THURSDAY);
        boolean isWhiteDays = hijri.isWhiteDay();
        boolean isRamadan = hijri.isRamadan();
        boolean isArafah = hijri.isDayOfArafah();
        boolean isDhulHijjah10 = hijri.isFirstTenOfDhulHijjah() && !isArafah;
        boolean isAshura = hijri.isAshuraSeason();

        // 1. Friday
        list.add(new OccasionItem(
                "friday",
                "يوم الجمعة المبارك",
                "Blessed Friday",
                "سورة الكهف والصلاة على النبي ﷺ وساعة الإجابة",
                "Surah Al-Kahf & Blessings upon the Prophet ﷺ",
                "يوم الجمعة خير يوم طلعت عليه الشمس. يُستحب فيه الإكثار من الصلاة على النبي ﷺ، وقراءة سورة الكهف، وتحرّي ساعة الاستجابة في آخر النهار.",
                "Friday is the best day upon which the sun has risen. It is recommended to send abundant blessings upon the Prophet ﷺ, recite Surah Al-Kahf, and seek the hour of answered prayers.",
                "قال النبي ﷺ: «من قرأ سورة الكهف في يوم الجمعة أضاء له من النور ما بين الجمعتين». وقال ﷺ: «إن من أفضل أيامكم يوم الجمعة فأكثروا عليّ من الصلاة فيه».",
                "The Prophet ﷺ said: 'Whoever reads Surah Al-Kahf on Friday will have a light shining for him between the two Fridays.'",
                "اللهم صلّ وسلّم على نبينا محمد",
                "Allahumma salli wa sallim 'ala Nabiyyina Muhammad",
                "يوم الجمعة",
                "Friday",
                isFriday
        ));

        // 2. Monday Fasting
        list.add(new OccasionItem(
                "monday_fasting",
                "سُنّة صيام الإثنين",
                "Monday Sunnah Fasting",
                "تُعرض فيه الأعمال على الله تعالى",
                "Deeds are presented to Allah",
                "يُستحب صيام يوم الإثنين اتباعاً لسنة النبي ﷺ واغتناماً لمغفرة الذنوب وعرض الأعمال.",
                "Fasting on Mondays is a confirmed Sunnah of the Prophet ﷺ and a blessed time when deeds are presented to Allah.",
                "قال النبي ﷺ: «تُعرض الأعمال يوم الإثنين والخميس، فأحب أن يُعرض عملي وأنا صائم».",
                "The Prophet ﷺ said: 'Deeds are presented on Monday and Thursday, and I love that my deeds be presented while I am fasting.'",
                "أستغفر الله وأتوب إليه",
                "Astaghfirullah wa atubu ilayh",
                "صيام الإثنين",
                "Monday Fasting",
                isMonday
        ));

        // 3. Thursday Fasting
        list.add(new OccasionItem(
                "thursday_fasting",
                "سُنّة صيام الخميس",
                "Thursday Sunnah Fasting",
                "تُعرض فيه الأعمال على الله تعالى",
                "Deeds are presented to Allah",
                "يُستحب صيام يوم الخميس طلباً للأجر والثواب ورفعة الدرجات.",
                "Fasting on Thursdays is a cherished Sunnah of the Prophet ﷺ.",
                "قال النبي ﷺ: «تُعرض الأعمال يوم الإثنين والخميس، فأحب أن يُعرض عملي وأنا صائم».",
                "The Prophet ﷺ said: 'Deeds are presented on Monday and Thursday, and I love that my deeds be presented while I am fasting.'",
                "سبحان الله وبحمده سبحان الله العظيم",
                "Subhan Allahi wa bihamdihi, Subhan Allahil Azim",
                "صيام الخميس",
                "Thursday Fasting",
                isThursday
        ));

        // 4. The White Days (13, 14, 15 Hijri)
        String whiteTitleAr = "صيام الأيام البيض (" + hijri.day + " " + hijri.getMonthName(true) + ")";
        String whiteTitleEn = "White Days Fasting (" + hijri.day + " " + hijri.getMonthName(false) + ")";
        list.add(new OccasionItem(
                "white_days",
                whiteTitleAr,
                whiteTitleEn,
                "الأيام البيض 13 و 14 و 15 من الشهر الهجري",
                "The 13th, 14th & 15th of the Hijri Month",
                "سُنّة مؤكدة كان النبي ﷺ يوصي بها أصحابه، وصيام ثلاثة أيام من كل شهر يعدل صيام الدهر كله.",
                "A confirmed Sunnah recommended by the Prophet ﷺ; fasting three days each month is like fasting a lifetime.",
                "قال النبي ﷺ: «صم من الشهر ثلاثة أيام، فإن الحسنة بعشر أمثالها، وذلك مثل صيام الدهر».",
                "The Prophet ﷺ said: 'Fast three days each month, for every good deed is multiplied tenfold, and that is like fasting forever.'",
                "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
                "La ilaha illallah wahdahu la sharika lah, lahul-mulku wa lahul-hamd wa huwa 'ala kulli shay'in qadir",
                "الأيام البيض",
                "White Days",
                isWhiteDays
        ));

        // 5. Ramadan
        list.add(new OccasionItem(
                "ramadan",
                "شهر رمضان المبارك",
                "Blessed Ramadan",
                "شهر الصيام والقرآن والرحمة والمغفرة",
                "The Month of Fasting, Quran and Mercy",
                "موسم الخيرات ومضاعفة الحسنات والعتق من النيران والإكثار من الدعاء وقيام الليل.",
                "The season of immense blessings, multiplication of rewards, and sincere repentance.",
                "قال النبي ﷺ: «من صام رمضان إيماناً واحتساباً غُفر له ما تقدم من ذنبه».",
                "The Prophet ﷺ said: 'Whoever fasts Ramadan out of faith and hope for reward, his past sins will be forgiven.'",
                "اللهم إنك عفو تحب العفو فاعفُ عني",
                "Allahumma innaka 'afuwwun tuhibbul-'afwa fa'fu 'anni",
                "رمضان المبارك",
                "Ramadan",
                isRamadan
        ));

        // 6. First Ten of Dhul-Hijjah
        list.add(new OccasionItem(
                "dhul_hijjah_10",
                "عشر ذي الحجة",
                "First Ten of Dhu al-Hijjah",
                "أفضل أيام الدنيا - التكبير والتهليل والتحميد",
                "The Greatest Days - Takbeer, Tahleel & Tahmeed",
                "الأيام الفاضلة التي أقسم الله بها في القرآن، والعمل الصالح فيها أحب إلى الله من أي أيام أخر.",
                "The most virtuous days of the year wherein righteous deeds are most beloved to Allah.",
                "قال النبي ﷺ: «ما من أيام العمل الصالح فيها أحب إلى الله من هذه الأيام، فأكثروا فيهن من التهليل والتكبير والتحميد».",
                "The Prophet ﷺ said: 'There are no days in which righteous deeds are more beloved to Allah than these ten days.'",
                "الله أكبر، الله أكبر، لا إله إلا الله، والله أكبر، الله أكبر، ولله الحمد",
                "Allahu Akbar, Allahu Akbar, La ilaha illallah, Wallahu Akbar, Allahu Akbar, wa lillahil-hamd",
                "عشر ذي الحجة",
                "10 Dhu al-Hijjah",
                isDhulHijjah10
        ));

        // 7. Day of Arafah
        list.add(new OccasionItem(
                "arafah",
                "يوم عرفة المبارك",
                "Blessed Day of Arafah",
                "خير الدعاء وصيام يكفّر ذنوب سنتين",
                "Best Supplication & Fasting Expiates Two Years",
                "أعظم أيام العام، يوم إجابة الدعاء ومباهاة الملائكة وعتق الرقاب من النار.",
                "The pinnacle of the year, a day of answered prayers and divine forgiveness.",
                "قال النبي ﷺ: «صيام يوم عرفة أحتسب على الله أن يكفر السنة التي قبله والسنة التي بعده».",
                "The Prophet ﷺ said: 'Fasting on the Day of Arafah expiates sins of the previous year and the coming year.'",
                "لا إله إلا الله وحده لا شريك له، له الملك وله الحمد وهو على كل شيء قدير",
                "La ilaha illallah wahdahu la sharika lah, lahul-mulku wa lahul-hamd wa huwa 'ala kulli shay'in qadir",
                "يوم عرفة",
                "Arafah",
                isArafah
        ));

        // 8. Ashura & Tasu'a
        list.add(new OccasionItem(
                "ashura",
                "صيام تاسوعاء وعاشوراء",
                "Tasu'a & Ashura Fasting",
                "يوم نجاة موسى عليه السلام وتكفير سنة ماضية",
                "Day of Prophet Moses' Salvation & Sins Expiation",
                "يُستحب صيام التاسع والعاشر من شهر محرم الحرام شكراً لله على نجاة نبي الله موسى والمؤمنين.",
                "It is recommended to fast the 9th and 10th of Muharram in gratitude to Allah for saving Prophet Moses.",
                "قال النبي ﷺ: «صيام يوم عاشوراء أحتسب على الله أن يكفر السنة التي قبله».",
                "The Prophet ﷺ said: 'Fasting the Day of Ashura expiates the sins of the previous year.'",
                "سبحان الله وبحمده، أستغفر الله وأتوب إليه",
                "Subhan Allahi wa bihamdihi, Astaghfirullah wa atubu ilayh",
                "عاشوراء",
                "Ashura",
                isAshura
        ));

        // 9. Daily Perpetual Athkar
        list.add(new OccasionItem(
                "daily_wird",
                "الورد اليومي والاستغفار",
                "Daily Wird & Tasbih",
                "أحب الأعمال إلى الله أدومها وإن قلّ",
                "The most beloved deeds are the most consistent",
                "المداومة على ذكر الله طمأنينة للقلب وحفظ للمسلم في يومه وليلته.",
                "Consistent remembrance of Allah brings serenity to the heart and protection throughout day and night.",
                "قال الله تعالى: ﴿أَلَا بِذِكْرِ اللَّهِ تَطْمَئِنُّ الْقُلُوبُ﴾.",
                "Allah Almighty says: 'Unquestionably, by the remembrance of Allah hearts are assured.'",
                "سبحان الله والحمد لله ولا إله إلا الله والله أكبر",
                "Subhan Allah wal-hamdulillah wa la ilaha illallah wallahu Akbar",
                "الورد الدائم",
                "Daily Wird",
                true
        ));

        return list;
    }

    public static OccasionItem getPrimaryOccasionToday() {
        Calendar cal = Calendar.getInstance();
        List<OccasionItem> list = getOccasionsForCalendar(cal);
        for (OccasionItem item : list) {
            if (item.isActiveToday() && !"daily_wird".equals(item.getId())) {
                return item;
            }
        }
        return list.get(list.size() - 1); // daily wird as fallback
    }

    public static List<OccasionItem> getActiveOccasionsToday() {
        Calendar cal = Calendar.getInstance();
        List<OccasionItem> list = getOccasionsForCalendar(cal);
        List<OccasionItem> active = new ArrayList<OccasionItem>();
        for (OccasionItem item : list) {
            if (item.isActiveToday()) {
                active.add(item);
            }
        }
        return active;
    }
}
