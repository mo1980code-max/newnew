# تشغيل المشروع في Android Studio — دليل كامل

> المشروع جاهز للفتح والبناء كما هو. لا يحتاج أي تعديل مسبق ليعمل،
> ولا يحتاج إنترنت إلا مرة واحدة أثناء أول مزامنة (تنزيل Gradle والتبعيات).

---

## 1) المتطلبات

| المكوّن | المطلوب | ملاحظات |
|---|---|---|
| Android Studio | **Narwhal 3 Feature Drop (2025.1.3) أو أحدث** | Otter / Panda / Quail كلها تدعم AGP 8.13 |
| Android Gradle Plugin | 8.13.2 | مثبّت مسبقًا في `build.gradle` الجذر |
| Gradle | 8.13 | الـ wrapper ينزّله تلقائيًا، لا تثبّته يدويًا |
| JDK | **17** | مدمج في Android Studio (Embedded JDK) — لا تحتاج تثبيت جافا |
| Android SDK Platform | **36** (Android 16) | `compileSdk` و`targetSdk` = 36 |
| Build-Tools | 36.x | تُثبَّت تلقائيًا مع المنصة |
| جهاز/محاكي | **API 23 فأعلى** (Android 6.0+) | `minSdk 23` |

إن ظهر «Install missing platform(s)» في أول مزامنة → اضغط الرابط نفسه وثبّت SDK 36.

---

## 2) الحصول على المشروع

**الطريقة أ — تنزيل ZIP مباشرة من المستودع** (الأسهل، 9.2 م.ب):
```
https://github.com/mo1980code-max/newnew/raw/refs/heads/arena/01a0970d-newnew/Allah-Clock-Live-Wallpaper-android-studio.zip
```
أو من واجهة GitHub: افتح المستودع على الفرع `arena/01a0970d-newnew` → اضغط الملف
`Allah-Clock-Live-Wallpaper-android-studio.zip` → **Download raw file**. ثم فك الضغط وافتح المجلد الناتج.

> الحزمة محفوظة داخل المستودع نفسه (وليست مرفقًا في Releases) لأن بيئة البناء لا تصل إلى
> `uploads.github.com`. تُعاد بنائها في أي وقت بالأمر:
> ```bash
> python3 tools/build_package.py        # تُنشئ الحزمة في جذر المستودع
> ```
> وهي تستثني تلقائيًا: `.git`، كل `build/` و`.gradle/`، `local.properties`،
> وأصول المعاينة المولَّدة `tools/preview/assets/`. وتحفظ صلاحية التنفيذ لـ `gradlew`.

**الطريقة ب — git**:
```bash
git clone -b arena/01a0970d-newnew https://github.com/mo1980code-max/newnew.git
```

---

## 3) الفتح والمزامنة

1. Android Studio → **File → Open** → اختر **المجلد الجذر** (الذي فيه `settings.gradle`) — وليس مجلد `app`.
2. انتظر **Gradle Sync** الأول: ينزّل Gradle 8.13 وAGP والتبعيات (~3–8 دقائق حسب الاتصال).
3. إن سألك عن «Trust project» → **Trust**.
4. `local.properties` يُنشأ تلقائيًا بمسار SDK عندك (وهو متجاهَل في git — لا ترفعه).

**التشغيل**: اختر جهازًا من القائمة العلوية ثم ▶ (Run 'app').
أول بناء debug يستغرق دقائق؛ بعدها يصير سريعًا بفضل التخزين المؤقت المفعّل.

---

## 4) بناء ملف قابل للتثبيت

| الهدف | الطريق |
|---|---|
| APK للتجربة على جهازك | **Build → Build App Bundle(s) / APK(s) → Build APK(s)** ثم `locate` |
| APK من سطر الأوامر | `./gradlew assembleDebug` (ويندوز: `gradlew.bat assembleDebug`) |
| AAB موقّع للنشر | **Build → Generate Signed App Bundle / APK** → أنشئ keystore → `release` |

بناء `release` يفعّل **R8 + تقليص الموارد** (`minifyEnabled true` و`shrinkResources true`)،
وقواعد الحفظ في `app/proguard-rules.pro` تمنع كسر Gson والتخطيطات.

---

## 5) قبل النشر على Google Play — 4 خطوات إلزامية

1. **معرّفات الإعلانات**: المشروع يستخدم **معرّفات جوجل الاختبارية** عن قصد (لا إيرادات ولا
   خطر حظر). استبدلها بمعرفاتك في مكانين:
   - `app/src/main/java/org/Allah_Clock_Live_Wallpaper/ads/AdConfig.java`
     (App ID + Banner + Interstitial + Rewarded + App Open + Native)
   - `app/src/main/AndroidManifest.xml` → `com.google.android.gms.ads.APPLICATION_ID`
   ⚠ لا تنقر أبدًا على إعلاناتك الحقيقية بحسابك — AdMob يحظر الحساب.
2. **`versionCode`**: ارفعه فوق ما هو منشور حاليًا في Play Console (`app/build.gradle`).
3. **keystore**: احتفظ بنسخة احتياطية منه ومن كلماته — بدونه لا يمكنك تحديث التطبيق.
4. **نموذج Data Safety**: التطبيق يُعلن `com.google.android.gms.permission.AD_ID` ويستخدم
   AdMob + UMP، فصرّح بجمع «Advertising ID» و«App interactions»، وبأن تدفق الموافقة
   (UMP) معطّل لجمع البيانات قبل الموافقة.

---

## 6) قارئ القرآن الكريم

تظهر بطاقة **«القرآن الكريم»** في الشاشة الرئيسية. القارئ يعمل دون إنترنت ويضم:

- فهرس السور الـ114؛
- بحثًا في اسم السورة أو نص الآيات (ويقبل مرجعًا مثل `2:255`)؛
- حفظ أول آية مرئية لاستئناف القراءة؛
- علامات محفوظة لكل آية يمكن فتحها أو حذفها من شاشة **العلامات**.

أيقونة العلامة بجانب الآية تحفظها أو تزيلها؛ والنقر المطوّل على بطاقة الآية يفعل الشيء نفسه.
الموضع والعلامات محلية على الجهاز ولا تُرسل إلى الشبكة. تفاصيل مصدر النص العثماني وترخيصه
وفحص سلامته موجودة في [`QURAN_TEXT_ATTRIBUTION.md`](QURAN_TEXT_ATTRIBUTION.md).

---

## 7) حل المشكلات الشائعة

| العَرَض | السبب والحل |
|---|---|
| `Android Gradle plugin requires Java 17` | Settings → Build, Execution, Deployment → Build Tools → Gradle → **Gradle JDK = 17 (Embedded)** |
| تعذّر تنزيل `com.github.QuadFlask:colorpicker` | المستودع `jitpack.io` معلن في **`settings.gradle`** (نمط `PREFER_SETTINGS`، أي أن مستودعات `build.gradle` الجذر تُتجاهل) — تحقق من اتصالك أو البروكسي |
| تحذير من `enableJetifier` | مقصود: مكتبتان (colorpicker وokdownload القديمة) مبنيتان على `android.support` |
| إعلانات لا تظهر | طبيعية: المعرّفات اختبارية، وقد يتأخر أول إعلان. الإصدار `debug` لا يقلّص الكود |
| الخلفية الحية لا تعمل فورًا | فعّلها من الجهاز: ضغط مطوّل على الرئيسية → **Wallpaper → Live wallpaper**، أو من داخل التطبيق |
| السبحة العائمة لا تظهر | تحتاج إذن **«Display over other apps»** — التطبيق يطلبه عند التفعيل فقط |
| شارة الأذكار غير ظاهرة | مقصود: تظهر **داخل نافذتها فقط** (الصباح: الفجر→الشروق، المساء: العصر→المغرب). غيّر الأوقات من ⚙ داخل القارئ للتجربة |
| تحذير lint عن `LockedOrientationActivity` / `DiscouragedApi` | مقصود ومُعلَّم بـ `tools:ignore`، وlint لا يوقف البناء (`abortOnError false`) |
| تحذير `AllowBackup` بلا قواعد استخراج بيانات | الاحتياط الافتراضي يشمل تفضيلات التطبيق فقط (TinyDB) — لا بيانات حساسة |

---

## 8) أدوات التحقق داخل المستودع (بلا JDK)

```bash
python3 tools/verify_resources.py        # مدقّق الموارد: 8 فحوص — يُفترض «NO ERRORS»
python3 tools/athkar/build_athkar.py     # إعادة بناء res/raw/athkar.json (61 ذكرًا) مع فحوصه
python3 tools/preview/build_assets.py    # توليد أصول المعاينة من res/
node tools/preview/server.js 8080        # معاينة التطبيق في المتصفح (12 شاشة)
node tools/preview/smoke.js              # 22 فحصًا لمنطق المعاينة بلا متصفح
```

---

## 9) نتيجة الفحص الكامل للمشروع (16 سبتمبر 2026)

| البند | النتيجة |
|---|---|
| ملفات XML في `res/` (108) | كلها تُحلَّل بلا خطأ |
| النصوص: `values` مقابل `values-ar` | **144 / 144** متطابقة، ووسائط التنسيق (`%1$d`) متماثلة، وشرطات الاقتباس مُهرَّبة |
| المصفوفات | 7 في الافتراضي و6 في العربية، وكل المشتركة بنفس عدد العناصر (`adhkar` عربية عن قصد في اللغتين) |
| مراجع `R.*` في 75 ملف جافا | كلها مُعلنة (نصوص، معرّفات، مصفوفات، رسومات، ألوان، `raw`) |
| مراجع `@type/name` داخل التخطيطات والرسومات | كلها تُحلّ إلى مورد موجود |
| `findViewById(R.id.x)` | كل هدف له `@+id/x` في تخطيط ما |
| توازن أقواس جافا (ماسح أحرف لا تعبير نمطي) | 75/75 سليمة |
| مكونات Manifest (20) | كل صنف موجود، و`android:exported` معرّف على كل مكوّن (مطلوب من targetSdk 31+) |
| حزم جافا مقابل مسارات المجلدات | 75/75 مطابقة |
| أسماء الموارد المكررة داخل المجلد الواحد | لا يوجد |
| الخطوط في `assets` (16) المستعملة بـ `createFromAsset` | كلها موجودة |
| `res/xml` (5) و`res/font/myfont.ttf` وأيقونة الإقلاع | موجودة ومطابقة لمراجع Manifest |
| بقايا الحزمة القديمة `com.clock.livewallpaper` | لا يوجد anywhere |
| `java.time` أو APIs أعلى من minSdk 23 بلا حماية | لا يوجد |
| AdMob/UMP | المعرّفات اختبارية ومتسقة بين `AdConfig` وManifest، و`MobileAds.initialize` **بعد** تدفق الموافقة |
| التبعيات | appcompat 1.8.0 · recyclerview 1.4.0 · cardview 1.0.0 · play-services-ads 25.4.0 · UMP 4.0.0 · glide 4.16.0 · gson 2.14.0 · colorpicker (jitpack) |
| بيانات القرآن | **114 سورة / 6,236 آية**؛ مصدر Tanzil Uthmani 1.1، البصمة والنسبة مُتحققتان |

### ثلاثة أمور صُحّحت أثناء هذا الفحص
1. **`bundle.language.enableSplit` كان `true`** → مع مبدّل اللغة داخل التطبيق، كان Play قد
   يسلّم APK بلا `values-ar` على جهاز لغته إنجليزية، فيفشل التبديل إلى العربية بصمت.
   صار `false` (وتقسيم الكثافة وABI ما زالا مفعّلين).
2. **`gradlew` لم يكن قابلًا للتنفيذ** (644) → `./gradlew` يفشل على macOS/Linux. صار 755.
3. **تعليق في Manifest** يصف الودجت فوق خدمة السبحة، ووعدٌ بأن `SCREEN_ON` يحدّث الودجت
   وهو لا يصل إلى مستقبلات Manifest في API 26+ → صُحّح الشرح: التحديث يعتمد على منبّه
   `AlarmManager` (يعمل على كل المستويات)، و`SCREEN_ON` باقية لأجهزة API 23–25 فقط.

### ملاحظات اختيارية (ليست أخطاء)
- أيقونة الإقلاع `drawable/icon200.png` بدقة 256×256 وليست أيقونة تكيّفية (Adaptive Icon).
  تعمل على كل المستويات، لكن API 26+ يعرضها داخل قناع؛ إن أردت الشكل الأمثل أضف
  `mipmap-anydpi-v26/ic_launcher.xml` بطبقتين (أمامية/خلفية) وأيقونة 512×512 لمتجر Play.
- `assets/` فيها 7.3 م.ب خطوطًا (16 ملفًا) تستعملها تصاميم الساعة. يمكن تصغيرها لاحقًا
  (subsetting) لتخفيض حجم APK — لم تُنفَّذ لأن أدوات تقليم الخطوط غير متوفرة هنا.
- `versionCode 2 / versionName 1.1` — ارفع `versionCode` قبل أي رفع جديد إلى Play.
