# -*- coding: utf-8 -*-
"""Morning (31) and evening (30) athkar, in the exact order, wording and repeat counts of
islambook.com/azkar/1 and islambook.com/azkar/2. Shared blocks live in athkar_shared.py.
"""

from athkar_shared import (
    KURSI, BAQARAH_LAST, IKHLAS, FALAQ, NAS,
    SAYYIDUL_ISTIGHFAR, RADITU, HASBI, BISMILLAH_LA_YADUR, SUBHAN_ADAD,
    AAFINI, KUFR_FAQR, AFW_AAFIYA, YA_HAYY, ALIM_GHAYB, KALIMAT_TAMMAT,
    SALAWAT, SHIRK, HAMM_HAZAN, ISTIGHFAR_AZEEM, YA_RABB_HAMD, TAWAKKALTU,
    LA_ILAHA_100, SUBHAN_100, ISTIGHFAR_100,
)


# ══════════════════════════ morning-only items (verbatim) ══════════════════════════

M_ASBAHNA_MULK = {
    "repeat": 1,
    "ar": "أَصْـبَحْنا وَأَصْـبَحَ المُـلْكُ لله وَالحَمدُ لله ، لا إلهَ إلاّ اللّهُ وَحدَهُ لا شَريكَ لهُ، لهُ المُـلكُ ولهُ الحَمْـد، وهُوَ على كلّ شَيءٍ قدير ، رَبِّ أسْـأَلُـكَ خَـيرَ ما في هـذا اليوم وَخَـيرَ ما بَعْـدَه ، وَأَعـوذُ بِكَ مِنْ شَـرِّ ما في هـذا اليوم وَشَرِّ ما بَعْـدَه، رَبِّ أَعـوذُبِكَ مِنَ الْكَسَـلِ وَسـوءِ الْكِـبَر ، رَبِّ أَعـوذُ بِكَ مِنْ عَـذابٍ في النّـارِ وَعَـذابٍ في القَـبْر.",
    "en": "We have entered the morning and the dominion belongs to Allah, and all praise is due to Allah. There is no deity but Allah alone, without any partner. To Him belongs the dominion and to Him belongs the praise, and He has power over all things. My Lord, I ask You for the good of this day and the good of what comes after it, and I seek refuge in You from the evil of this day and the evil of what comes after it. My Lord, I seek refuge in You from laziness and the evils of old age. My Lord, I seek refuge in You from punishment in the Fire and punishment in the grave.",
    "tr": "Asbahna wa asbahal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamd, wa Huwa 'ala kulli shay'in qadir. Rabbi as'aluka khaira ma fi hadhal-yawmi wa khaira ma ba'dah, wa a'udhu bika min sharri ma fi hadhal-yawmi wa sharri ma ba'dah. Rabbi a'udhu bika minal-kasali wa su'il-kibar, Rabbi a'udhu bika min 'adhabin fin-nari wa 'adhabin fil-qabr.",
    "ref_ar": "[مسلم]",
    "ref_en": "[Muslim]",
    "virtue_ar": "",
    "virtue_en": "",
}

M_ASHHADUK = {
    "repeat": 4,
    "ar": "اللّهُـمَّ إِنِّـي أَصْبَـحْتُ أُشْـهِدُك ، وَأُشْـهِدُ حَمَلَـةَ عَـرْشِـك ، وَمَلَائِكَتَكَ ، وَجَمـيعَ خَلْـقِك ، أَنَّـكَ أَنْـتَ اللهُ لا إلهَ إلاّ أَنْـتَ وَحْـدَكَ لا شَريكَ لَـك ، وَأَنَّ ُ مُحَمّـداً عَبْـدُكَ وَرَسـولُـك.",
    "en": "O Allah, I have entered the morning asking You to bear witness, and asking the bearers of Your Throne, Your angels and all of Your creation to bear witness, that You are Allah — there is no deity but You, alone without any partner — and that Muhammad is Your servant and Your Messenger.",
    "tr": "Allahumma inni asbahtu ush-hiduka, wa ush-hidu hamalata 'arshika, wa mala'ikataka, wa jami'a khalqika, annaka Antallahu la ilaha illa Anta wahdaka la sharika lak, wa anna Muhammadan 'abduka wa rasuluk.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "من قالها أعتقه الله من النار.",
    "virtue_en": "Whoever says it, Allah sets him free from the Fire.",
}

M_NIMAH = {
    "repeat": 1,
    "ar": "اللّهُـمَّ ما أَصْبَـَحَ بي مِـنْ نِعْـمَةٍ أَو بِأَحَـدٍ مِـنْ خَلْـقِك ، فَمِـنْكَ وَحْـدَكَ لا شريكَ لَـك ، فَلَـكَ الْحَمْـدُ وَلَـكَ الشُّكْـر.",
    "en": "O Allah, whatever favour has come to me or to any of Your creation this morning, it is from You alone, without any partner. So to You belongs all praise and to You belongs all thanks.",
    "tr": "Allahumma ma asbaha bi min ni'matin aw bi ahadin min khalqika, fa minka wahdaka la sharika lak, fa lakal-hamdu wa lakash-shukr.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "من قالها حين يصبح أدى شكر يومه.",
    "virtue_en": "Whoever says it in the morning has fulfilled the gratitude of his day.",
}

M_BIKA_ASBAHNA = {
    "repeat": 1,
    "ar": "اللّهُـمَّ بِكَ أَصْـبَحْنا وَبِكَ أَمْسَـينا ، وَبِكَ نَحْـيا وَبِكَ نَمُـوتُ وَإِلَـيْكَ النُّـشُور.",
    "en": "O Allah, by You we have entered the morning and by You we have entered the evening, by You we live and by You we die, and to You is the resurrection.",
    "tr": "Allahumma bika asbahna wa bika amsayna, wa bika nahya wa bika namut, wa ilaykan-nushur.",
    "ref_ar": "[الترمذي]",
    "ref_en": "[At-Tirmidhi]",
    "virtue_ar": "",
    "virtue_en": "",
}

M_FITRAH = {
    "repeat": 1,
    "ar": "أَصْبَـحْـنا عَلَى فِطْرَةِ الإسْلاَمِ، وَعَلَى كَلِمَةِ الإِخْلاَصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ، وَعَلَى مِلَّةِ أَبِينَا إبْرَاهِيمَ حَنِيفاً مُسْلِماً وَمَا كَانَ مِنَ المُشْرِكِينَ.",
    "en": "We have entered the morning upon the natural disposition of Islam, upon the word of pure sincerity, upon the religion of our Prophet Muhammad, peace and blessings be upon him, and upon the way of our father Ibrahim, upright and submitting — and he was not of those who associate others with Allah.",
    "tr": "Asbahna 'ala fitratil-Islam, wa 'ala kalimatil-ikhlas, wa 'ala dini nabiyyina Muhammadin sallallahu 'alayhi wa sallam, wa 'ala millati abina Ibrahima hanifan musliman, wa ma kana minal-mushrikin.",
    "ref_ar": "[أحمد]",
    "ref_en": "[Ahmad]",
    "virtue_ar": "",
    "virtue_en": "",
}

M_ASBAHNA_RABB_ALAMIN = {
    "repeat": 1,
    "ar": "أَصْبَـحْـنا وَأَصْبَـحْ المُـلكُ للهِ رَبِّ العـالَمـين ، اللّهُـمَّ إِنِّـي أسْـأَلُـكَ خَـيْرَ هـذا الـيَوْم ، فَـتْحَهُ ، وَنَصْـرَهُ ، وَنـورَهُ وَبَـرَكَتَـهُ ، وَهُـداهُ ، وَأَعـوذُ بِـكَ مِـنْ شَـرِّ ما فـيهِ وَشَـرِّ ما بَعْـدَه.",
    "en": "We have entered the morning and the dominion belongs to Allah, Lord of all the worlds. O Allah, I ask You for the good of this day: its victory, its help, its light, its blessing and its guidance; and I seek refuge in You from the evil of what is in it and the evil of what comes after it.",
    "tr": "Asbahna wa asbahal-mulku lillahi Rabbil-'alamin. Allahumma inni as'aluka khaira hadhal-yawm: fathahu, wa nasrahu, wa nurahu wa barakatahu, wa hudahu, wa a'udhu bika min sharri ma fihi wa sharri ma ba'dah.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "",
    "virtue_en": "",
}

M_ILM_NAFI = {
    "repeat": 1,
    "ar": "اللَّهُمَّ إِنِّي أَسْأَلُكَ عِلْمًا نَافِعًا، وَرِزْقًا طَيِّبًا، وَعَمَلًا مُتَقَبَّلًا.",
    "en": "O Allah, I ask You for beneficial knowledge, good provision and deeds that are accepted.",
    "tr": "Allahumma inni as'aluka 'ilman nafi'an, wa rizqan tayyiban, wa 'amalan mutaqabbalan.",
    "ref_ar": "[ابن ماجه]",
    "ref_en": "[Ibn Majah]",
    "virtue_ar": "",
    "virtue_en": "",
}

MORNING = [
    KURSI, IKHLAS, FALAQ, NAS, M_ASBAHNA_MULK, SAYYIDUL_ISTIGHFAR, RADITU,
    M_ASHHADUK, M_NIMAH, HASBI, BISMILLAH_LA_YADUR, M_BIKA_ASBAHNA, M_FITRAH,
    SUBHAN_ADAD, AAFINI, KUFR_FAQR, AFW_AAFIYA, YA_HAYY, M_ASBAHNA_RABB_ALAMIN,
    ALIM_GHAYB, KALIMAT_TAMMAT, SALAWAT, SHIRK, HAMM_HAZAN, ISTIGHFAR_AZEEM,
    YA_RABB_HAMD, M_ILM_NAFI, TAWAKKALTU, LA_ILAHA_100, SUBHAN_100, ISTIGHFAR_100,
]


# ══════════════════════════ evening-only items (verbatim) ══════════════════════════

E_AMSAYNA_MULK = {
    "repeat": 1,
    "ar": "أَمْسَيْـنا وَأَمْسـى المـلكُ لله وَالحَمدُ لله ، لا إلهَ إلاّ اللّهُ وَحدَهُ لا شَريكَ لهُ، لهُ المُـلكُ ولهُ الحَمْـد، وهُوَ على كلّ شَيءٍ قدير ، رَبِّ أسْـأَلُـكَ خَـيرَ ما في هـذهِ اللَّـيْلَةِ وَخَـيرَ ما بَعْـدَهـا ، وَأَعـوذُ بِكَ مِنْ شَـرِّ ما في هـذهِ اللَّـيْلةِ وَشَرِّ ما بَعْـدَهـا ، رَبِّ أَعـوذُبِكَ مِنَ الْكَسَـلِ وَسـوءِ الْكِـبَر ، رَبِّ أَعـوذُ بِكَ مِنْ عَـذابٍ في النّـارِ وَعَـذابٍ في القَـبْر.",
    "en": "We have entered the evening and the dominion belongs to Allah, and all praise is due to Allah. There is no deity but Allah alone, without any partner. To Him belongs the dominion and to Him belongs the praise, and He has power over all things. My Lord, I ask You for the good of this night and the good of what comes after it, and I seek refuge in You from the evil of this night and the evil of what comes after it. My Lord, I seek refuge in You from laziness and the evils of old age. My Lord, I seek refuge in You from punishment in the Fire and punishment in the grave.",
    "tr": "Amsayna wa amsal-mulku lillah, wal-hamdu lillah, la ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamd, wa Huwa 'ala kulli shay'in qadir. Rabbi as'aluka khaira ma fi hadhihil-laylati wa khaira ma ba'daha, wa a'udhu bika min sharri ma fi hadhihil-laylati wa sharri ma ba'daha. Rabbi a'udhu bika minal-kasali wa su'il-kibar, Rabbi a'udhu bika min 'adhabin fin-nari wa 'adhabin fil-qabr.",
    "ref_ar": "[مسلم]",
    "ref_en": "[Muslim]",
    "virtue_ar": "",
    "virtue_en": "",
}

E_AMSAYTU_USHHIDUK = {
    "repeat": 4,
    "ar": "اللّهُـمَّ إِنِّـي أَمسيتُ أُشْـهِدُك ، وَأُشْـهِدُ حَمَلَـةَ عَـرْشِـك ، وَمَلَائِكَتَكَ ، وَجَمـيعَ خَلْـقِك ، أَنَّـكَ أَنْـتَ اللهُ لا إلهَ إلاّ أَنْـتَ وَحْـدَكَ لا شَريكَ لَـك ، وَأَنَّ ُ مُحَمّـداً عَبْـدُكَ وَرَسـولُـك.",
    "en": "O Allah, I have entered the evening asking You to bear witness, and asking the bearers of Your Throne, Your angels and all of Your creation to bear witness, that You are Allah — there is no deity but You, alone without any partner — and that Muhammad is Your servant and Your Messenger.",
    "tr": "Allahumma inni amsaytu ush-hiduka, wa ush-hidu hamalata 'arshika, wa mala'ikataka, wa jami'a khalqika, annaka Antallahu la ilaha illa Anta wahdaka la sharika lak, wa anna Muhammadan 'abduka wa rasuluk.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "من قالها أعتقه الله من النار.",
    "virtue_en": "Whoever says it, Allah sets him free from the Fire.",
}

E_NIMAH = {
    "repeat": 1,
    "ar": "اللّهُـمَّ ما أَمسى بي مِـنْ نِعْـمَةٍ أَو بِأَحَـدٍ مِـنْ خَلْـقِك ، فَمِـنْكَ وَحْـدَكَ لا شريكَ لَـك ، فَلَـكَ الْحَمْـدُ وَلَـكَ الشُّكْـر.",
    "en": "O Allah, whatever favour has come to me or to any of Your creation this evening, it is from You alone, without any partner. So to You belongs all praise and to You belongs all thanks.",
    "tr": "Allahumma ma amsa bi min ni'matin aw bi ahadin min khalqika, fa minka wahdaka la sharika lak, fa lakal-hamdu wa lakash-shukr.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "من قالها حين يمسى أدى شكر يومه.",
    "virtue_en": "Whoever says it in the evening has fulfilled the gratitude of his day.",
}

E_BIKA_AMSAYNA = {
    "repeat": 1,
    "ar": "اللّهُـمَّ بِكَ أَمْسَـينا وَبِكَ أَصْـبَحْنا، وَبِكَ نَحْـيا وَبِكَ نَمُـوتُ وَإِلَـيْكَ الْمَصِيرُ.",
    "en": "O Allah, by You we have entered the evening and by You we have entered the morning, by You we live and by You we die, and to You is the final destination.",
    "tr": "Allahumma bika amsayna wa bika asbahna, wa bika nahya wa bika namut, wa ilaykal-masir.",
    "ref_ar": "[الترمذي]",
    "ref_en": "[At-Tirmidhi]",
    "virtue_ar": "",
    "virtue_en": "",
}

E_FITRAH = {
    "repeat": 1,
    "ar": "أَمْسَيْنَا عَلَى فِطْرَةِ الإسْلاَمِ، وَعَلَى كَلِمَةِ الإِخْلاَصِ، وَعَلَى دِينِ نَبِيِّنَا مُحَمَّدٍ صَلَّى اللهُ عَلَيْهِ وَسَلَّمَ، وَعَلَى مِلَّةِ أَبِينَا إبْرَاهِيمَ حَنِيفاً مُسْلِماً وَمَا كَانَ مِنَ المُشْرِكِينَ.",
    "en": "We have entered the evening upon the natural disposition of Islam, upon the word of pure sincerity, upon the religion of our Prophet Muhammad, peace and blessings be upon him, and upon the way of our father Ibrahim, upright and submitting — and he was not of those who associate others with Allah.",
    "tr": "Amsayna 'ala fitratil-Islam, wa 'ala kalimatil-ikhlas, wa 'ala dini nabiyyina Muhammadin sallallahu 'alayhi wa sallam, wa 'ala millati abina Ibrahima hanifan musliman, wa ma kana minal-mushrikin.",
    "ref_ar": "[أحمد]",
    "ref_en": "[Ahmad]",
    "virtue_ar": "",
    "virtue_en": "",
}

E_AMSAYNA_RABB_ALAMIN = {
    "repeat": 1,
    "ar": "أَمْسَيْنا وَأَمْسَى الْمُلْكُ للهِ رَبِّ الْعَالَمَيْنِ، اللَّهُمَّ إِنَّي أسْأَلُكَ خَيْرَ هَذَه اللَّيْلَةِ فَتْحَهَا ونَصْرَهَا، ونُوْرَهَا وبَرَكَتهَا، وَهُدَاهَا، وَأَعُوذُ بِكَ مِنْ شَرِّ مَا فيهِا وَشَرَّ مَا بَعْدَهَا.",
    "en": "We have entered the evening and the dominion belongs to Allah, Lord of all the worlds. O Allah, I ask You for the good of this night: its victory, its help, its light, its blessing and its guidance; and I seek refuge in You from the evil of what is in it and the evil of what comes after it.",
    "tr": "Amsayna wa amsal-mulku lillahi Rabbil-'alamin. Allahumma inni as'aluka khaira hadhihil-laylah: fathaha wa nasraha, wa nuraha wa barakataha, wa hudaha, wa a'udhu bika min sharri ma fiha wa sharri ma ba'daha.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "",
    "virtue_en": "",
}

EVENING = [
    KURSI, BAQARAH_LAST, IKHLAS, FALAQ, NAS, E_AMSAYNA_MULK, SAYYIDUL_ISTIGHFAR,
    RADITU, E_AMSAYTU_USHHIDUK, E_NIMAH, HASBI, BISMILLAH_LA_YADUR, E_BIKA_AMSAYNA,
    E_FITRAH, SUBHAN_ADAD, AAFINI, KUFR_FAQR, AFW_AAFIYA, YA_HAYY, E_AMSAYNA_RABB_ALAMIN,
    ALIM_GHAYB, KALIMAT_TAMMAT, SALAWAT, SHIRK, HAMM_HAZAN, ISTIGHFAR_AZEEM,
    YA_RABB_HAMD, LA_ILAHA_100, TAWAKKALTU, SUBHAN_100,
]
