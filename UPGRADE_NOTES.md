# ملاحظات الترقية — Allah Clock Live Wallpaper

تحديث شامل للمشروع: SDK 36 + الإعلانات (Native / Rewarded / Interstitial / Banner) + تنظيف الكود.

---

## 1) خطوة يدوية واحدة مطلوبة قبل أول بناء

ملف `gradle/wrapper/gradle-wrapper.jar` الموجود في المستودع قديم جدًا (من Gradle **2.10** سنة 2015)،
بينما `gradle-wrapper.properties` يشير الآن إلى Gradle **8.13**. قد لا يستطيع الـ jar القديم تحميل
التوزيع الجديد.

**الحل (أي واحدة تكفي):**

- افتح المشروع في **Android Studio** (Giraffe أو أحدث) → عند طلب المزامنة اختر
  *"Upgrade Gradle wrapper"* أو دعه يعيد توليد الـ wrapper تلقائيًا، **أو**
- نفّذ من الطرفية إذا كان Gradle مثبّتًا عندك:

  ```bash
  gradle wrapper --gradle-version 8.13 --distribution-type bin
  ```

بعد ذلك التزم بالملفات الثلاثة المولّدة:
`gradlew`, `gradlew.bat`, `gradle/wrapper/gradle-wrapper.jar`.

> ملاحظة: الـ JDK المطلوب هو **17** (إلزامي مع AGP 8.x). Android Studio الحديث يأتي معه
> (JBR 17/21) ولا يحتاج تثبيتًا منفصلًا.

---

## 2) قبل النشر على Google Play — مهم جدًا

| البند | المكان | ما يجب فعله |
|---|---|---|
| **أرقام الإعلانات** | `ads/AdConfig.java` + `AndroidManifest.xml` | القيم الحالية هي **أرقام اختبار رسمية من Google**. استبدل الخمسة (App ID + Banner + Interstitial + Rewarded + Native) بأرقامك من [apps.admob.com](https://apps.admob.com). رقم التطبيق يجب أن يتطابق في الملفين. |
| **versionCode** | `app/build.gradle` | حاليًا `2` / `1.1`. ارفعه فوق آخر رقم منشور على Play Console وإلا سيُرفض الرفع. |
| **أجهزة الاختبار** | `AdConfig.TEST_DEVICE_HASHED_IDS` | بعد استخدام أرقام حقيقية، أضف الـ hashed IDs لهواتفك (تظهر في Logcat بوسم `Ads`) حتى لا تنقر على إعلان حي بالخطأ — هذا يُعتبر مخالفة سياسة. |
| **نموذج Data safety** | Play Console | أعلن عن: Advertising ID, Device/Other IDs, Approximate location, Crash/Diagnostics. واذكر مشاركة البيانات مع Google (AdMob). |
| **إعلانات مخصّصة** | Play Console → *App content → Ads* | اختر "يحتوي على إعلانات". |
| **UMP / موافقة المستخدم** | مفعّل مسبقًا في `AdManager` | أضف رابطًا لـ *privacy policy* في إعدادات AdMob حتى تظهر رسالة الموافقة لمستخدمي EEA/UK/CH. |
| **توقيع الإصدار** | — | `minifyEnabled true` + `shrinkResources true` مفعّلان في `release`. جرّب بناء release ونصّبه على جهاز حقيقي قبل الرفع. |

---

## 3) ما تغيّر في المشروع

### البناء والإصدارات
- `compileSdk 36` / `targetSdk 36` (متطلب Play لتحديثات التطبيقات منذ 31 آب 2026).
- `minSdk 23` — الحد الأدنى لإعلانات Google Mobile Ads 25.x ومكتبات AndroidX الحالية.
- AGP `8.13.2` + Gradle `8.13` + Java `17`.
- حذف `jcenter()` (مغلق نهائيًا) وإضافة `jitpack` (لمكتبة colorpicker).
- إزالة مكتبات غير مستخدمة: material، constraintlayout، volley، picasso، simplecropview،
  filedownloader، connector، smarttablayout، spinkit.

### الإعلانات (كلها تمر عبر `ads/AdManager.java`)
1. **Native** — بطاقة إعلانية في **منتصف** قوائم الساعات والخلفيات
   (`NativeAdListAdapter` يغلف الـ adapter ويُدخل بطاقة واحدة عند نسبة `0.5` بعرض كامل).
2. **Rewarded** — يفتح الساعات الذهبية (آخر **3** عناصر من كل قائمة: Analog / Digital / Smart).
   الفتح **للجلسة الحالية فقط** ويعود مقفولًا بعد إغلاق التطبيق.
3. **Interstitial** — يُعرض **فقط بعد نجاح تطبيق الخلفية**، ولا يُعرض أبدًا عند الإقلاع أو الخروج.
4. **Banner** — adaptive banner في **أسفل شاشات الإعدادات الفرعية فقط**
   (ClockFuntion، ClockCard، Wallpaper، WallpaperCategory). لا بانر في الشاشة الرئيسية ولا في شاشة المحرر/التطبيق.

### سياسة Google Play
- **UMP consent** قبل تهيئة SDK وقبل أي طلب إعلان.
- زر/أيقونة **Privacy options** تظهر فقط عندما تفرضها Google، وتعيد فتح نموذج الموافقة.
- الإعلانات الأصلية داخل `NativeAdView` مع أيقونة AdChoices وشارة "Ad" واضحة.
- لا إعلانات قبل أن يستطيع المستخدم التفاعل، ولا إعلانات مفاجئة عند الخروج.
- `UNLOCK_WHEN_REWARDED_UNAVAILABLE = true`: إذا فشل تحميل الإعلان المكافئ لا يبقى المستخدم عالقًا.
- حُذفت صلاحيات التخزين غير الضرورية (`READ/WRITE_EXTERNAL_STORAGE`) — الصور الملتقطة من المعرض
  تُنسخ إلى مساحة التطبيق الخاصة عبر `ContentResolver`، والخلفيات تُحمّل إلى `getExternalCacheDir()`.
- حُذف `AD_MANAGER_APP` من الـ manifest (كان يمنع عمل وحدات AdMob العادية).
- كل النصوص في `strings.xml`، وكل عنصر أيقوني له `contentDescription` (متطلب إمكانية الوصول).

### تنظيف الكود
- حذف ملفات غير مستخدمة: `AdAdmob.java`, `utils/RealPathUtil.java`,
  `rvadapter/RecyclerViewAdapterWrapper.java`, `res/anim/bottom_animation.xml`, `res/values/arrays.xml`.
- استبدال الواجهات المهجورة: `ProgressDialog` → `UiCompat.showLoading`،
  `startActivityForResult` → `ActivityResultLauncher`،
  `setSystemUiVisibility` → `WindowInsetsControllerCompat`،
  `new Handler()` → `new Handler(Looper.getMainLooper())`،
  `InputDeviceCompat.SOURCE_ANY` → `Color.WHITE`،
  `switch (view.getId())` → `if/else` (ثوابت R لم تعد compile-time constants).
- **Edge-to-edge** مفروض الآن من النظام في targetSdk 35+؛ تمت معالجته عبر `UiCompat.applyEdgeToEdge`
  و `applyImmersive` حتى لا يختفي المحتوى تحت أشرطة النظام.

---

## 4) هيكل مجلد الإعلانات

```
ads/
├── AdConfig.java             كل الثوابت (أرقام الوحدات، عدد العناصر الذهبية، موضع الـ Native)
├── AdManager.java            UMP + تهيئة SDK + Interstitial + Rewarded + Native
├── BannerAdController.java   بانر adaptive مع إعادة المحاولة عند onResume
├── NativeAdListAdapter.java  RecyclerView.Adapter wrapper يُدخل بطاقة Native في المنتصف
├── NativeAdViewHolder.java   ربط NativeAdView (MediaView + AdChoices + شارة Ad)
├── PremiumBadge.java         شارة القفل الذهبي + بوابة الفتح عند النقر
├── PremiumManager.java       حالة الفتح (جلسة/مؤقت) + تحديد العناصر المميزة
└── RewardedUnlockHelper.java حوار التوضيح ثم تشغيل الإعلان المكافئ
```

---

## 5) الخلفيات: محلية 100% — بلا أي مصدر خارجي

تم حذف الكتالوج البعيد بالكامل (`wallpaper.json` من dopewalls و `wallpapernew.json` من Flickr)
لأنه كان: (أ) روابط صور لجهات أخرى بلا ترخيص = خطر سياسة الملكية الفكرية، (ب) معتمدًا على خوادم
قد تختفي أو يتغير محتواها في أي لحظة.

**البنية الجديدة:**

| المكوّن | الدور |
|---|---|
| `res/drawable-nodpi/wp_*.jpg` | صور الخلفيات نفسها، مضمّنة داخل الـ APK (لا تُقاس بكثافة الشاشة) |
| `utils/WallpaperCatalog.java` | المصدر الوحيد للحقيقة: الفئات + ترتيبها + صورها |
| `model/WallpaperCategory.java` / `WallpaperItem.java` | نموذج بسيط بلا Gson ولا Parcelable |
| `SetWallpaperActivity` | يفكّ الرسم إلى ملف JPEG خاص بالتطبيق ثم يطبّقه/يشاركه — بلا تنزيل |

**لإضافة خلفية:** ضع jpg في `res/drawable-nodpi` وأضف `R.drawable.xxx` إلى المصفوفة المناسبة
في `WallpaperCatalog`.
**لإضافة فئة:** أضف عنوانًا في `strings.xml` وسطر `category(...)` واحدًا في `build()`.

**مكتبات حُذفت مع هذه الخطوة:** `okdownload`، `okhttp` (لا تنزيل بعد الآن).
بقي `gson` لأنه يُستخدم فقط في `TinyDB` لحفظ نموذج الساعة المختارة.

> **تنويه مهم:** صور الأماكن المقدسة الحالية **رسومات فنية مولّدة بالذكاء الاصطناعي** وليست
> صورًا فوتوغرافية حقيقية، وُضعت بأسلوب ليلي/سيلويت محترم بلا أشخاص وبلا نصوص. يمكنك استبدال
> أي منها بصور تملك حقوقها بوضع الملف بنفس الاسم داخل `drawable-nodpi`.

---

## 6) الميزات الإسلامية الثلاث + اكتمال العشرين خلفية

### التاريخ الهجري
- `utils/HijriDate.java`: على API 24+ يستخدم `android.icu` بحساب **أم القرى** الرسمي،
  وعلى API 23 حساب تبوّلي احتياطي (قد يختلف يومًا واحدًا — مقبول لسطر زخرفي).
- يظهر أعلى الخلفية الحية عندما يفعّله المستخدم من المحرر.

### الأذكار المتغيرة تلقائيًا
- `res/values/islamic_content.xml`: مصفوفة `adhkar` (14 ذكرًا صحيحًا قصيرًا).
- `WallpaperOverlayView` يختار الذكر تلقائيًا بمعادلة الوقت (يتغير كل 3 ساعات) —
  بلا مؤقّتات وبلا بطارية إضافية، ويتغيّر حتى لو فُتح الجهاز بعد غياب.
- الخط.used هو خط النظام حتى تُشكَّل العربية صحيحًا على كل الأجهزة.

### بوصلة القبلة
- `activity/QiblaActivity` + `viewUtils/CompassView` + `utils/QiblaUtil`.
- **بلا صلاحية موقع**: جدول 73 مدينة مضمّن (`QiblaUtil.CITY_COORDS`)، والميل يُحسب
  بمعادلة الدائرة العظمى إلى الكعبة (21.422487, 39.826206)، والاتجاه من حسّاس
  `ROTATION_VECTOR` مع تنعيم. تعمل بدون إنترنت تمامًا.
- تُعرض المسافة بالكيلومترات، وال_city_ المختار يُحفظ في التفضيلات.
- شاشة فرعية → فيها بانر أسفلها مثل بقية الشاشات الفرعية.

### التحكم
- زر جديد (منزلقات) في شريط المحرر السفلي يفتح `dialog_wallpaper_options`:
  تشغيل/إيقاف التاريخ الهجري والأذكار. المفاتيح تُحفظ في `TinyDB`
  (`showHijri` / `showDhikr`) وتقرأها الخلفية الحية والمحرر مباشرة (معاينة فورية).

### الخلفيات
اكتملت العشرون: 5 الكعبة والحرم المكي + 5 الحرم المدني + 5 الأقصى والقدس +
5 مساجد ومعالم = **5 فئات / 27 خلفية** كلها داخل `res/drawable-nodpi` (~2.9 MB).

---

## 7) ودجت الشاشة الرئيسية (2×2 و 4×2)

- `widget/ClockWidgetProvider` (أساس) + `ClockWidgetSquare` (2×2) + `ClockWidgetWide` (4×2).
- **نفس تصاميم الساعات حرفيًا**: `utils/WidgetClockRenderer` ينشئ نفس الـ view الذي تستخدمه
  الخلفية الحية (`AnalogClock` / `SmartClockPreview` / `TextClockPreview`) ويضبطه بنفس
  الإعدادات (`setPosition` / `setClockSize` / `config`) ثم يرسمه على Bitmap —
  فلا يوجد أي تكرار للتصاميم، وأي نمط ساعة جديد يصل للودجت تلقائيًا.
- الودجت يقرأ اختيار المستخدم الحالي من التفضيلات (`clockType` / `textClockPosition` /
  `clocks` / الألوان)، والنقر عليه يفتح شاشة اختيار الساعات.
- الودجت العريض يعرض أيضًا **التاريخ الهجري + الذكر الحالي** (نفس دالة
  `WallpaperOverlayView.currentDhikr` المستخدمة في الخلفية → نفس الذكر دائمًا في المكانين).
- **قابل للتغيير الحر**: `resizeMode="horizontal|vertical"` والرسم يتبع المقاس الفعلي من
  `AppWidgetManager.getAppWidgetOptions`، لذا 2×2 و4×2 مجرد نقطتي بداية وليس حدًا.
- **نموذج البطارية**: إعادة رسم **مرة كل دقيقة** عبر `AlarmManager.set` واحد غير مُوقِظ
  (يُؤجَّل أثناء Doze حين لا يرى أحد الشاشة)، + تحديث فوري عند: تشغيل الشاشة، تغيّر الوقت،
  تغيّر المنطقة الزمنية. أرخص بأضعاف من إعادة رسم الخلفية الحية كل ثانية.
- **بلا أي صلاحيات جديدة.**

### عن شاشة القفل — بصراحة
أندرويد الخام منذ 5.0 **أزال ودجات شاشة القفل**؛ لا توجد واجهة برمجية لها في النسخة الحديثة.
لذلك:
- أعلنّا `widgetCategory="home_screen|keyguard"` حتى تتيح الشركات المصنّعة (OEM) التي ما زالت
  تدعم الوضع على شاشة القفل استخدامه.
- البديل الحقيقي الذي يعمل على شاشة القفل في أندرويد الحديث هو **الخلفية الحية نفسها**
  أو وضع AOD، وكلاهما موجود/مدعوم في التطبيق.

---

## 8) محدّد الإطارات (FPS Limiter) + إعلانات فتح التطبيق

### وضع توفير الطاقة
- `utils/FrameRate.java` + مفتاح `powerSaver` في حوار خيارات المحرر (الافتراضي = مفعّل).
- **موفّر (الافتراضي)**: إعادة رسم الخلفية مرة واحدة كل ثانية وعقرب الثواني يقفز كل ثانية —
  وهو نفس سلوك التطبيق المنشور اليوم تمامًا، فلا يشعر المستخدمون القدامى بأي تغيير.
- **انسيابي**: عقرب ثواني يلتف باستمرار بمعدل ~30 إطارًا/ثانية (opt-in لمن يفضل الجمال).
- المحرك هو الذي يقود الإطارات وحده: أُلغيت حلقة الـ800ms الداخلية في `AnalogClock` داخل
  الخلفية (`setAutoUpdate(false)` + `setTime()` كل إطار) حتى لا تتعارض حلقتان.
- الساعات الرقمية والذكية لا تتأثر بالوضع الانسيابي (الدقيقة فقط تتغير) — توفير تلقائي.

### App Open Ads — داخل حدود السياسة حرفيًا
- `ads/AppOpenAdController.java` + رقم اختبار جوجل الرسمي في `AdConfig.APP_OPEN_UNIT_ID`.
- يُعرض **فقط** عند عودة التطبيق للمقدمة (أو إقلاع بارد) **وفقط على الشاشة الرئيسية** —
  لا يظهر فوق مهمة جارية (تطبيق خلفية مثلًا)، ولا عند الخروج، ولا فوق نموذج موافقة UMP.
- تتبع المقدمة/الخلفية عبر `ActivityLifecycleCallbacks` في `AppClass` بدون أي مكتبة إضافية.
- **تبريد 4 ساعات** محفوظ في التفضيلات + عدم التكديس: علم `FULL_SCREEN_ACTIVE` في `AdManager`
  يمنع تزامنه مع interstitial أو rewarded.
- يُحمَّل مسبقًا في الخلفية بعد تهيئة الـ SDK؛ إن لم يكن جاهزًا عند الفتح لا يُعرض شيء إطلاقًا
  ويبدأ تحميل للزيارة التالية — المستخدم لا ينتظر إعلانًا أبدًا.

---

## 9) السبحة الإلكترونية العائمة (Floating Tasbeeh)

- `service/FloatingTasbeehService.java` + `res/layout/view_floating_tasbeeh.xml`.
- **تشغيل اختياري 100%** من أيقونة السبحة في الشاشة الرئيسية؛ التطبيق لا يطلب
  `SYSTEM_ALERT_WINDOW` ولا يبدأ الخدمة من نفسه أبدًا (سياسة Play للصلاحية الخاصة).
- عدّاد دائري شبه شفاف + زر تصفير صغير؛ النقر يعدّ، **والضغط المطوّل 0.7ث على زر التصفير**
  يصفّر (لأن النقر المفرد Zufällig يفقد العدّ)، والضغط المطوّل **1.5ث على الدائرة** يقفل العدّ.
- السحب حر، وعند الإفلات قرب الحافة يلتصق بأقرب حافة بحركة انسيابية (ValueAnimator)،
  ويتلاشى إلى **35%** بعد 4 ثوانٍ سكون.
- اهتزاز `EFFECT_TICK` قصير ولطيف (وبدلًا منه oneShot 15ms على الأقدم).

### حماية الجيب والبطارية (ثلاث طبقات)
1. **مستشعر التقارب**: عند التغطية (جيب/وجه لأسفل) تُرفض كل اللمسات ويُوقف الاهتزاز فورًا.
2. **SCREEN_OFF**: إلغاء تسجيل المستشعر + حذف كل أعمال الـ Handler + إلغاء الاهتزاز →
   لا شيء يعمل ولا شيء يوقظ المعالج؛ و**USER_PRESENT** يعيد كل شيء.
3. **قفل يدوي** بالضغط المطوّل 1.5 ثانية.

### قرار معماري موثّق
خدمة **started عادية وليست Foreground**: على targetSdk 36 كانت Foreground ستفرض نوع خدمة
(`specialUse` = تبرير في Play Console) + إشعارًا دائمًا + صلاحية إشعارات على 13+.
الثمن المقبول: بعض واجهات OEM العدوانية (MIUI/EMUI) قد تقتلها — يُنصح المستخدم بتفعيل
"auto-start" إن أراد بقاءها دائمًا. لا wake lock ولا أي حلقة تشغيل: العمل المجدول الوحيد
هو مؤقّت تلاشي واحد مدته 4 ثوانٍ.
