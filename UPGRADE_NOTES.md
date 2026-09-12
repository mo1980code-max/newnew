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
