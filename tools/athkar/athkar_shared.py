# -*- coding: utf-8 -*-
"""Athkar content, transcribed from islambook.com/azkar/1 (morning, 31 items) and
islambook.com/azkar/2 (evening, 30 items): same texts, same order, same repeat counts.

Blocks shared between the two lists are written once here so the wording, the translation
and the transliteration cannot drift apart between morning and evening.
"""

# ══════════════════════════════════════ shared blocks ══════════════════════════════════════

ISTIAADHA = "أَعُوذُ بِاللهِ مِنْ الشَّيْطَانِ الرَّجِيمِ"
BISMILLAH = "بِسْمِ اللهِ الرَّحْمنِ الرَّحِيم"

KURSI = {
    "repeat": 1,
    "ar": ISTIAADHA + "\n\n" + "اللّهُ لاَ إِلَـهَ إِلاَّ هُوَ الْحَيُّ الْقَيُّومُ لاَ تَأْخُذُهُ سِنَةٌ وَلاَ نَوْمٌ لَّهُ مَا فِي السَّمَاوَاتِ وَمَا فِي الأَرْضِ مَن ذَا الَّذِي يَشْفَعُ عِنْدَهُ إِلاَّ بِإِذْنِهِ يَعْلَمُ مَا بَيْنَ أَيْدِيهِمْ وَمَا خَلْفَهُمْ وَلاَ يُحِيطُونَ بِشَيْءٍ مِّنْ عِلْمِهِ إِلاَّ بِمَا شَاء وَسِعَ كُرْسِيُّهُ السَّمَاوَاتِ وَالأَرْضَ وَلاَ يَؤُودُهُ حِفْظُهُمَا وَهُوَ الْعَلِيُّ الْعَظِيمُ.",
    "en": "I seek refuge in Allah from Satan the outcast.\n\nAllah! There is no deity save Him, the Ever-Living, the Sustainer of all existence. Neither drowsiness overtakes Him nor sleep. To Him belongs whatever is in the heavens and whatever is on the earth. Who is it that can intercede with Him except by His permission? He knows what is before them and what will be after them, and they encompass not a thing of His knowledge except for what He wills. His Kursi extends over the heavens and the earth, and their preservation tires Him not. And He is the Most High, the Most Great.",
    "tr": "A'udhu billahi minash-shaytanir-rajim.\n\nAllahu la ilaha illa Huwa, al-Hayyul-Qayyum, la ta'khudhuhu sinatun wa la nawm, lahu ma fis-samawati wa ma fil-ard, man dhalladhi yashfa'u 'indahu illa bi-idhnih, ya'lamu ma bayna aydihim wa ma khalfahum, wa la yuhituna bi shay'in min 'ilmihi illa bima sha', wasi'a kursiyyuhus-samawati wal-ard, wa la ya'uduhu hifzuhuma, wa Huwal-'Aliyyul-'Azim.",
    "ref_ar": "[آية الكرسى - البقرة 255]",
    "ref_en": "[Ayat al-Kursi — Al-Baqarah 255]",
    "virtue_ar": "من قالها حين يصبح أجير من الجن حتى يمسى ومن قالها حين يمسى أجير من الجن حتى يصبح.",
    "virtue_en": "Whoever recites it in the morning is protected from the jinn until evening, and whoever recites it in the evening is protected from them until morning.",
}

BAQARAH_LAST = {
    "repeat": 1,
    "ar": ISTIAADHA + "\n\n" + "آمَنَ الرَّسُولُ بِمَا أُنْزِلَ إِلَيْهِ مِنْ رَبِّهِ وَالْمُؤْمِنُونَ ۚ كُلٌّ آمَنَ بِاللَّهِ وَمَلَائِكَتِهِ وَكُتُبِهِ وَرُسُلِهِ لَا نُفَرِّقُ بَيْنَ أَحَدٍ مِنْ رُسُلِهِ ۚ وَقَالُوا سَمِعْنَا وَأَطَعْنَا ۖ غُفْرَانَكَ رَبَّنَا وَإِلَيْكَ الْمَصِيرُ. لَا يُكَلِّفُ اللَّهُ نَفْسًا إِلَّا وُسْعَهَا لَهَا مَا كَسَبَتْ وَعَلَيْهَا مَا اكْتَسَبَتْ رَبَّنَا لَا تُؤَاخِذْنَا إِنْ نَّسِينَآ أَوْ أَخْطَأْنَا رَبَّنَا وَلَا تَحْمِلْ عَلَيْنَا إِصْرًا كَمَا حَمَلْتَهُ عَلَى الَّذِينَ مِنْ قَبْلِنَا رَبَّنَا وَلَا تُحَمِّلْنَا مَا لَا طَاقَةَ لَنَا بِهِ وَاعْفُ عَنَّا وَاغْفِرْ لَنَا وَارْحَمْنَا أَنْتَ مَوْلَانَا فَانْصُرْنَا عَلَى الْقَوْمِ الْكَافِرِينَ.",
    "en": "I seek refuge in Allah from Satan the outcast.\n\nThe Messenger has believed in what was revealed to him from his Lord, and so have the believers. All of them have believed in Allah and His angels and His books and His messengers, saying: We make no distinction between any of His messengers. And they say: We hear and we obey. Your forgiveness, our Lord, and to You is the final destination. Allah does not charge a soul except with that within its capacity. It will have the consequence of what good it has gained, and it will bear the consequence of what evil it has earned. Our Lord, do not take us to task if we forget or make mistakes. Our Lord, do not lay upon us a burden such as You laid upon those before us. Our Lord, do not burden us with what we have no strength to bear. Pardon us, forgive us, and have mercy on us. You are our Protector, so grant us victory over the disbelieving people.",
    "tr": "Amana-rasulu bima unzila ilayhi min rabbihi wal-mu'minun, kullun amana billahi wa mala'ikatihi wa kutubihi wa rusulih, la nufarriqu bayna ahadin min rusulih, wa qalu sami'na wa ata'na ghufrana-ka rabbana wa ilaykal-masir. La yukallifullahu nafsan illa wus'aha, laha ma kasabat wa 'alayha ma-ktasabat, rabbana la tu'akhidhna in nasina aw akhta'na, rabbana wa la tahmil 'alayna isran kama hamaltahu 'alalladhina min qablina, rabbana wa la tuhammilna ma la taqata lana bih, wa'fu 'anna waghfir lana warhamna, anta mawlana fansurna 'alal-qawmil-kafirin.",
    "ref_ar": "[البقرة 285 - 286]",
    "ref_en": "[Al-Baqarah 285-286]",
    "virtue_ar": "من قرأ آيتين من آخر سورة البقرة في ليلة كفتاه.",
    "virtue_en": "Whoever recites these two verses from the end of Surah al-Baqarah in a night, they will suffice him.",
}

IKHLAS = {
    "repeat": 3,
    "ar": BISMILLAH + "\n\n" + "قُلْ هُوَ ٱللَّهُ أَحَدٌ، ٱللَّهُ ٱلصَّمَدُ، لَمْ يَلِدْ وَلَمْ يُولَدْ، وَلَمْ يَكُن لَّهُۥ كُفُوًا أَحَدٌۢ.",
    "en": "In the name of Allah, the Most Gracious, the Most Merciful.\n\nSay: He is Allah, the One. Allah, the Eternal Refuge. He neither begets nor is born, nor is there to Him any equivalent.",
    "tr": "Bismillahir-Rahmanir-Rahim.\n\nQul Huwallahu Ahad, Allahus-Samad, lam yalid wa lam yulad, wa lam yakul-lahu kufuwan ahad.",
    "ref_ar": "[سورة الإخلاص]",
    "ref_en": "[Surat al-Ikhlas]",
    "virtue_ar": "من قالها حين يصبح وحين يمسى كفته من كل شىء (الإخلاص والمعوذتين).",
    "virtue_en": "Whoever recites them in the morning and in the evening, they will suffice him against everything (al-Ikhlas and the two Mu'awwidhatayn).",
}

FALAQ = {
    "repeat": 3,
    "ar": BISMILLAH + "\n\n" + "قُلْ أَعُوذُ بِرَبِّ ٱلْفَلَقِ، مِن شَرِّ مَا خَلَقَ، وَمِن شَرِّ غَاسِقٍ إِذَا وَقَبَ، وَمِن شَرِّ ٱلنَّفَّٰثَٰتِ فِى ٱلْعُقَدِ، وَمِن شَرِّ حَاسِدٍ إِذَا حَسَدَ.",
    "en": "In the name of Allah, the Most Gracious, the Most Merciful.\n\nSay: I seek refuge in the Lord of daybreak, from the evil of that which He created, and from the evil of darkness when it settles, and from the evil of the blowers in knots, and from the evil of an envier when he envies.",
    "tr": "Bismillahir-Rahmanir-Rahim.\n\nQul a'udhu bi rabbil-falaq, min sharri ma khalaq, wa min sharri ghasiqin idha waqab, wa min sharrin-naffathati fil-'uqad, wa min sharri hasidin idha hasad.",
    "ref_ar": "[سورة الفلق]",
    "ref_en": "[Surat al-Falaq]",
    "virtue_ar": "من قالها حين يصبح وحين يمسى كفته من كل شىء (الإخلاص والمعوذتين).",
    "virtue_en": "Whoever recites them in the morning and in the evening, they will suffice him against everything (al-Ikhlas and the two Mu'awwidhatayn).",
}

NAS = {
    "repeat": 3,
    "ar": BISMILLAH + "\n\n" + "قُلْ أَعُوذُ بِرَبِّ ٱلنَّاسِ، مَلِكِ ٱلنَّاسِ، إِلَٰهِ ٱلنَّاسِ، مِن شَرِّ ٱلْوَسْوَاسِ ٱلْخَنَّاسِ، ٱلَّذِى يُوَسْوِسُ فِى صُدُورِ ٱلنَّاسِ، مِنَ ٱلْجِنَّةِ وَٱلنَّاسِ.",
    "en": "In the name of Allah, the Most Gracious, the Most Merciful.\n\nSay: I seek refuge in the Lord of mankind, the Sovereign of mankind, the God of mankind, from the evil of the retreating whisperer, who whispers into the breasts of mankind, from among the jinn and mankind.",
    "tr": "Bismillahir-Rahmanir-Rahim.\n\nQul a'udhu bi rabbin-nas, Malikin-nas, Ilahin-nas, min sharril-waswasil-khannas, alladhi yuwaswisu fi sudurin-nas, minal-jinnati wan-nas.",
    "ref_ar": "[سورة الناس]",
    "ref_en": "[Surat an-Nas]",
    "virtue_ar": "من قالها حين يصبح وحين يمسى كفته من كل شىء (الإخلاص والمعوذتين).",
    "virtue_en": "Whoever recites them in the morning and in the evening, they will suffice him against everything (al-Ikhlas and the two Mu'awwidhatayn).",
}

SAYYIDUL_ISTIGHFAR = {
    "repeat": 1,
    "ar": "اللّهـمَّ أَنْتَ رَبِّـي لا إلهَ إلاّ أَنْتَ ، خَلَقْتَنـي وَأَنا عَبْـدُك ، وَأَنا عَلـى عَهْـدِكَ وَوَعْـدِكَ ما اسْتَـطَعْـت ، أَعـوذُبِكَ مِنْ شَـرِّ ما صَنَـعْت ، أَبـوءُ لَـكَ بِنِعْـمَتِـكَ عَلَـيَّ وَأَبـوءُ بِذَنْـبي فَاغْفـِرْ لي فَإِنَّـهُ لا يَغْـفِرُ الذُّنـوبَ إِلاّ أَنْتَ .",
    "en": "O Allah, You are my Lord. There is no deity but You. You created me and I am Your servant, and I am upon Your covenant and promise as much as I am able. I seek refuge in You from the evil of what I have done. I acknowledge Your favour upon me and I acknowledge my sin, so forgive me, for none forgives sins except You.",
    "tr": "Allahumma anta Rabbi, la ilaha illa Anta, khalaqtani wa ana 'abduka, wa ana 'ala 'ahdika wa wa'dika mastata't, a'udhu bika min sharri ma sana't, abu'u laka bi ni'matika 'alayya wa abu'u bi dhanbi faghfir li, fa innahu la yaghfirudh-dhunuba illa Anta.",
    "ref_ar": "[البخاري]",
    "ref_en": "[Al-Bukhari]",
    "virtue_ar": "من قالها موقنا بها حين يمسى ومات من ليلته دخل الجنة وكذلك حين يصبح.",
    "virtue_en": "Whoever says it with certainty in the evening and dies that night enters Paradise, and likewise whoever says it in the morning.",
}

RADITU = {
    "repeat": 3,
    "ar": "رَضيـتُ بِاللهِ رَبَّـاً وَبِالإسْلامِ ديـناً وَبِمُحَـمَّدٍ صلى الله عليه وسلم نَبِيّـاً.",
    "en": "I am pleased with Allah as my Lord, with Islam as my religion and with Muhammad, peace and blessings be upon him, as my Prophet.",
    "tr": "Raditu billahi Rabba, wa bil-Islami dina, wa bi Muhammadin sallallahu 'alayhi wa sallama nabiyya.",
    "ref_ar": "[أبو داود والترمذي]",
    "ref_en": "[Abu Dawud and at-Tirmidhi]",
    "virtue_ar": "من قالها حين يصبح وحين يمسى كان حقا على الله أن يرضيه يوم القيامة.",
    "virtue_en": "Whoever says it in the morning and in the evening, it is a duty upon Allah to please him on the Day of Resurrection.",
}

HASBI = {
    "repeat": 7,
    "ar": "حَسْبِـيَ اللّهُ لا إلهَ إلاّ هُوَ عَلَـيهِ تَوَكَّـلتُ وَهُوَ رَبُّ العَرْشِ العَظـيم.",
    "en": "Allah is sufficient for me. There is no deity but Him. In Him I have put my trust, and He is the Lord of the Mighty Throne.",
    "tr": "Hasbiyallahu la ilaha illa Huwa, 'alayhi tawakkaltu wa Huwa Rabbul-'arshil-'azim.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "من قالها كفاه الله ما أهمه من أمر الدنيا والأخرة.",
    "virtue_en": "Whoever says it, Allah will suffice him in whatever concerns him of this world and the Hereafter.",
}

BISMILLAH_LA_YADUR = {
    "repeat": 3,
    "ar": "بِسـمِ اللهِ الذي لا يَضُـرُّ مَعَ اسمِـهِ شَيءٌ في الأرْضِ وَلا في السّمـاءِ وَهـوَ السّمـيعُ العَلـيم.",
    "en": "In the name of Allah, with whose name nothing on earth or in heaven can cause harm, and He is the All-Hearing, the All-Knowing.",
    "tr": "Bismillahilladhi la yadurru ma'asmihi shay'un fil-ardi wa la fis-sama'i wa Huwas-Sami'ul-'Alim.",
    "ref_ar": "[أبو داود والترمذي]",
    "ref_en": "[Abu Dawud and at-Tirmidhi]",
    "virtue_ar": "لم يضره من الله شيء.",
    "virtue_en": "Nothing will harm him by Allah's leave.",
}

SUBHAN_ADAD = {
    "repeat": 3,
    "ar": "سُبْحـانَ اللهِ وَبِحَمْـدِهِ عَدَدَ خَلْـقِه ، وَرِضـا نَفْسِـه ، وَزِنَـةَ عَـرْشِـه ، وَمِـدادَ كَلِمـاتِـه.",
    "en": "Glory is to Allah and praise is to Him, as many as His creation, as much as pleases Him, as much as the weight of His Throne and as much as the ink of His words.",
    "tr": "Subhanallahi wa bihamdihi, 'adada khalqihi wa rida nafsihi wa zinata 'arshihi wa midada kalimatih.",
    "ref_ar": "[مسلم]",
    "ref_en": "[Muslim]",
    "virtue_ar": "",
    "virtue_en": "",
}

AAFINI = {
    "repeat": 3,
    "ar": "اللّهُـمَّ عافِـني في بَدَنـي ، اللّهُـمَّ عافِـني في سَمْـعي ، اللّهُـمَّ عافِـني في بَصَـري ، لا إلهَ إلاّ أَنْـتَ.",
    "en": "O Allah, grant health to my body. O Allah, grant health to my hearing. O Allah, grant health to my sight. There is no deity but You.",
    "tr": "Allahumma 'afini fi badani, Allahumma 'afini fi sam'i, Allahumma 'afini fi basari, la ilaha illa Anta.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "",
    "virtue_en": "",
}

KUFR_FAQR = {
    "repeat": 3,
    "ar": "اللّهُـمَّ إِنّـي أَعـوذُ بِكَ مِنَ الْكُـفر ، وَالفَـقْر ، وَأَعـوذُ بِكَ مِنْ عَذابِ القَـبْر ، لا إلهَ إلاّ أَنْـتَ.",
    "en": "O Allah, I seek refuge in You from disbelief and poverty, and I seek refuge in You from the punishment of the grave. There is no deity but You.",
    "tr": "Allahumma inni a'udhu bika minal-kufri wal-faqr, wa a'udhu bika min 'adhabil-qabr, la ilaha illa Anta.",
    "ref_ar": "[أبو داود]",
    "ref_en": "[Abu Dawud]",
    "virtue_ar": "",
    "virtue_en": "",
}

AFW_AAFIYA = {
    "repeat": 1,
    "ar": "اللّهُـمَّ إِنِّـي أسْـأَلُـكَ العَـفْوَ وَالعـافِـيةَ في الدُّنْـيا وَالآخِـرَة ، اللّهُـمَّ إِنِّـي أسْـأَلُـكَ العَـفْوَ وَالعـافِـيةَ في ديني وَدُنْـيايَ وَأهْـلي وَمالـي ، اللّهُـمَّ اسْتُـرْ عـوْراتي وَآمِـنْ رَوْعاتـي ، اللّهُـمَّ احْفَظْـني مِن بَـينِ يَدَيَّ وَمِن خَلْفـي وَعَن يَمـيني وَعَن شِمـالي ، وَمِن فَوْقـي ، وَأَعـوذُ بِعَظَمَـتِكَ أَن أُغْـتالَ مِن تَحْتـي.",
    "en": "O Allah, I ask You for pardon and well-being in this world and the Hereafter. O Allah, I ask You for pardon and well-being in my religion, my worldly affairs, my family and my wealth. O Allah, conceal my faults and calm my fears. O Allah, protect me from before me, from behind me, from my right, from my left and from above me, and I seek refuge in Your greatness from being struck down from beneath me.",
    "tr": "Allahumma inni as'alukal-'afwa wal-'afiyata fid-dunya wal-akhirah, Allahumma inni as'alukal-'afwa wal-'afiyata fi dini wa dunyaya wa ahli wa mali, Allahumma-stur 'awrati wa amin raw'ati, Allahumma-hfazni min bayni yadayya wa min khalfi wa 'an yamini wa 'an shimali wa min fawqi, wa a'udhu bi 'azamatika an ughtala min tahti.",
    "ref_ar": "[أبو داود وابن ماجه]",
    "ref_en": "[Abu Dawud and Ibn Majah]",
    "virtue_ar": "",
    "virtue_en": "",
}

YA_HAYY = {
    "repeat": 3,
    "ar": "يَا حَيُّ يَا قيُّومُ بِرَحْمَتِكَ أسْتَغِيثُ أصْلِحْ لِي شَأنِي كُلَّهُ وَلاَ تَكِلْنِي إلَى نَفْسِي طَـرْفَةَ عَيْنٍ.",
    "en": "O Ever-Living One, O Sustainer of all existence, by Your mercy I seek help: rectify for me all of my affairs and do not leave me to myself for even the blink of an eye.",
    "tr": "Ya Hayyu Ya Qayyum, bi rahmatika astaghith, aslih li sha'ni kullahu wa la takilni ila nafsi tarfata 'ayn.",
    "ref_ar": "[الحاكم والبيهقي]",
    "ref_en": "[Al-Hakim and al-Bayhaqi]",
    "virtue_ar": "",
    "virtue_en": "",
}

ALIM_GHAYB = {
    "repeat": 1,
    "ar": "اللّهُـمَّ عالِـمَ الغَـيْبِ وَالشّـهادَةِ فاطِـرَ السّماواتِ وَالأرْضِ رَبَّ كـلِّ شَـيءٍ وَمَليـكَه ، أَشْهَـدُ أَنْ لا إِلـهَ إِلاّ أَنْت ، أَعـوذُ بِكَ مِن شَـرِّ نَفْسـي وَمِن شَـرِّ الشَّيْـطانِ وَشِرْكِهِ ، وَأَنْ أَقْتَـرِفَ عَلـى نَفْسـي سوءاً أَوْ أَجُـرَّهُ إِلـى مُسْـلِم.",
    "en": "O Allah, Knower of the unseen and the seen, Creator of the heavens and the earth, Lord and Sovereign of all things, I bear witness that there is no deity but You. I seek refuge in You from the evil of my own soul, from the evil of Satan and his call to polytheism, and from bringing harm upon myself or dragging it upon a Muslim.",
    "tr": "Allahumma 'alimal-ghaybi wash-shahadah, fatiras-samawati wal-ard, Rabba kulli shay'in wa malikah, ash-hadu an la ilaha illa Anta, a'udhu bika min sharri nafsi wa min sharrish-shaytani wa shirkih, wa an aqtarifa 'ala nafsi su'an aw ajurrahu ila muslim.",
    "ref_ar": "[أبو داود والترمذي]",
    "ref_en": "[Abu Dawud and at-Tirmidhi]",
    "virtue_ar": "",
    "virtue_en": "",
}

KALIMAT_TAMMAT = {
    "repeat": 3,
    "ar": "أَعـوذُ بِكَلِمـاتِ اللّهِ التّـامّـاتِ مِنْ شَـرِّ ما خَلَـق.",
    "en": "I seek refuge in the perfect words of Allah from the evil of what He has created.",
    "tr": "A'udhu bi kalimatillahit-tammati min sharri ma khalaq.",
    "ref_ar": "[مسلم]",
    "ref_en": "[Muslim]",
    "virtue_ar": "",
    "virtue_en": "",
}

SALAWAT = {
    "repeat": 10,
    "ar": "اللَّهُمَّ صَلِّ وَسَلِّمْ وَبَارِكْ على نَبِيِّنَا مُحمَّد.",
    "en": "O Allah, send prayers and peace and blessings upon our Prophet Muhammad.",
    "tr": "Allahumma salli wa sallim wa barik 'ala nabiyyina Muhammad.",
    "ref_ar": "[الطبراني]",
    "ref_en": "[At-Tabarani]",
    "virtue_ar": "من صلى على حين يصبح وحين يمسى ادركته شفاعتى يوم القيامة.",
    "virtue_en": "Whoever sends prayers upon me in the morning and in the evening will receive my intercession on the Day of Resurrection.",
}

SHIRK = {
    "repeat": 3,
    "ar": "اللَّهُمَّ إِنَّا نَعُوذُ بِكَ مِنْ أَنْ نُشْرِكَ بِكَ شَيْئًا نَعْلَمُهُ ، وَنَسْتَغْفِرُكَ لِمَا لَا نَعْلَمُهُ.",
    "en": "O Allah, we seek refuge in You from associating anything with You knowingly, and we ask Your forgiveness for what we do unknowingly.",
    "tr": "Allahumma inna na'udhu bika min an nushrika bika shay'an na'lamuh, wa nastaghfiruka lima la na'lamuh.",
    "ref_ar": "[أحمد]",
    "ref_en": "[Ahmad]",
    "virtue_ar": "",
    "virtue_en": "",
}

HAMM_HAZAN = {
    "repeat": 3,
    "ar": "اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ الْهَمِّ وَالْحَزَنِ، وَأَعُوذُ بِكَ مِنْ الْعَجْزِ وَالْكَسَلِ، وَأَعُوذُ بِكَ مِنْ الْجُبْنِ وَالْبُخْلِ، وَأَعُوذُ بِكَ مِنْ غَلَبَةِ الدَّيْنِ، وَقَهْرِ الرِّجَالِ.",
    "en": "O Allah, I seek refuge in You from anxiety and sorrow, from weakness and laziness, from cowardice and miserliness, from being overcome by debt and from being overpowered by men.",
    "tr": "Allahumma inni a'udhu bika minal-hammi wal-hazan, wa a'udhu bika minal-'ajzi wal-kasal, wa a'udhu bika minal-jubni wal-bukhl, wa a'udhu bika min ghalabatid-dayni wa qahrir-rijal.",
    "ref_ar": "[البخاري]",
    "ref_en": "[Al-Bukhari]",
    "virtue_ar": "",
    "virtue_en": "",
}

ISTIGHFAR_AZEEM = {
    "repeat": 3,
    "ar": "أسْتَغْفِرُ اللهَ العَظِيمَ الَّذِي لاَ إلَهَ إلاَّ هُوَ، الحَيُّ القَيُّومُ، وَأتُوبُ إلَيهِ.",
    "en": "I seek the forgiveness of Allah the Almighty, besides whom none has the right to be worshipped, the Ever-Living, the Self-Subsisting, and I turn to Him in repentance.",
    "tr": "Astaghfirullahal-'Azim, alladhi la ilaha illa Huwa, al-Hayyul-Qayyum, wa atubu ilayh.",
    "ref_ar": "[أبو داود والترمذي]",
    "ref_en": "[Abu Dawud and at-Tirmidhi]",
    "virtue_ar": "",
    "virtue_en": "",
}

YA_RABB_HAMD = {
    "repeat": 3,
    "ar": "يَا رَبِّ , لَكَ الْحَمْدُ كَمَا يَنْبَغِي لِجَلَالِ وَجْهِكَ , وَلِعَظِيمِ سُلْطَانِكَ.",
    "en": "O my Lord, all praise is due to You as befits the majesty of Your Face and the greatness of Your sovereignty.",
    "tr": "Ya Rabbi, lakal-hamdu kama yanbaghi li jalali wajhika wa li 'azimi sultanik.",
    "ref_ar": "[النسائي في الكبرى]",
    "ref_en": "[An-Nasa'i in al-Kubra]",
    "virtue_ar": "",
    "virtue_en": "",
}

TAWAKKALTU = {
    "repeat": 1,
    "ar": "اللَّهُمَّ أَنْتَ رَبِّي لا إِلَهَ إِلا أَنْتَ ، عَلَيْكَ تَوَكَّلْتُ ، وَأَنْتَ رَبُّ الْعَرْشِ الْعَظِيمِ , مَا شَاءَ اللَّهُ كَانَ ، وَمَا لَمْ يَشَأْ لَمْ يَكُنْ ، وَلا حَوْلَ وَلا قُوَّةَ إِلا بِاللَّهِ الْعَلِيِّ الْعَظِيمِ , أَعْلَمُ أَنَّ اللَّهَ عَلَى كُلِّ شَيْءٍ قَدِيرٌ ، وَأَنَّ اللَّهَ قَدْ أَحَاطَ بِكُلِّ شَيْءٍ عِلْمًا , اللَّهُمَّ إِنِّي أَعُوذُ بِكَ مِنْ شَرِّ نَفْسِي ، وَمِنْ شَرِّ كُلِّ دَابَّةٍ أَنْتَ آخِذٌ بِنَاصِيَتِهَا ، إِنَّ رَبِّي عَلَى صِرَاطٍ مُسْتَقِيمٍ.",
    "en": "O Allah, You are my Lord; there is no deity but You. In You I have placed my trust, and You are the Lord of the Mighty Throne. Whatever Allah wills happens, and whatever He does not will does not happen. There is no power and no strength except with Allah, the Most High, the Most Great. I know that Allah has power over all things and that Allah has encompassed all things in knowledge. O Allah, I seek refuge in You from the evil of my own soul and from the evil of every moving creature whose forelock You hold. Indeed, my Lord is upon a straight path.",
    "tr": "Allahumma anta Rabbi la ilaha illa Anta, 'alayka tawakkaltu wa anta Rabbul-'arshil-'azim, ma sha'allahu kan, wa ma lam yasha' lam yakun, wa la hawla wa la quwwata illa billahil-'aliyyil-'azim, a'lamu annallaha 'ala kulli shay'in qadir, wa annallaha qad ahata bi kulli shay'in 'ilma, Allahumma inni a'udhu bika min sharri nafsi wa min sharri kulli dabbatin anta akhidhun bi nasiyatiha, inna Rabbi 'ala siratin mustaqim.",
    "ref_ar": "[ابن السني]",
    "ref_en": "[Ibn as-Sunni]",
    "virtue_ar": "ذكر طيب.",
    "virtue_en": "A good remembrance.",
}

LA_ILAHA_100 = {
    "repeat": 100,
    "ar": "لَا إلَه إلّا اللهُ وَحْدَهُ لَا شَرِيكَ لَهُ، لَهُ الْمُلْكُ وَلَهُ الْحَمْدُ وَهُوَ عَلَى كُلِّ شَيْءِ قَدِيرِ.",
    "en": "There is no deity but Allah alone, without any partner. His is the dominion and His is the praise, and He has power over all things.",
    "tr": "La ilaha illallahu wahdahu la sharika lah, lahul-mulku wa lahul-hamdu wa Huwa 'ala kulli shay'in qadir.",
    "ref_ar": "[متفق عليه]",
    "ref_en": "[Agreed upon]",
    "virtue_ar": "كانت له عدل عشر رقاب، وكتبت له مئة حسنة، ومحيت عنه مئة سيئة، وكانت له حرزا من الشيطان.",
    "virtue_en": "It is equal to freeing ten slaves, one hundred good deeds are written for him, one hundred sins are erased from him, and it is a shield for him against Satan.",
}

SUBHAN_100 = {
    "repeat": 100,
    "ar": "سُبْحـانَ اللهِ وَبِحَمْـدِهِ.",
    "en": "Glory is to Allah and praise is to Him.",
    "tr": "Subhanallahi wa bihamdih.",
    "ref_ar": "[متفق عليه]",
    "ref_en": "[Agreed upon]",
    "virtue_ar": "حُطَّتْ خَطَايَاهُ وَإِنْ كَانَتْ مِثْلَ زَبَدِ الْبَحْرِ. لَمْ يَأْتِ أَحَدٌ يَوْمَ الْقِيَامَةِ بِأَفْضَلَ مِمَّا جَاءَ بِهِ إِلَّا أَحَدٌ قَالَ مِثْلَ مَا قَالَ أَوْ زَادَ عَلَيْهِ.",
    "virtue_en": "His sins are forgiven even if they were like the foam of the sea, and no one will come on the Day of Resurrection with anything better than what he brought, except one who said the same or more.",
}

ISTIGHFAR_100 = {
    "repeat": 100,
    "ar": "أسْتَغْفِرُ اللهَ وَأتُوبُ إلَيْهِ",
    "en": "I seek the forgiveness of Allah and I turn to Him in repentance.",
    "tr": "Astaghfirullaha wa atubu ilayh.",
    "ref_ar": "[متفق عليه]",
    "ref_en": "[Agreed upon]",
    "virtue_ar": "مائة حسنة، ومُحيت عنه مائة سيئة، وكانت له حرزاً من الشيطان حتى يمسى.",
    "virtue_en": "One hundred good deeds are written for him, one hundred sins are erased from him, and it is a shield for him against Satan until the evening.",
}
