# Allah Clock Live Wallpaper — ساعة الله خلفية حية

تطبيق أندرويد: خلفية حية بساعات إسلامية + خلفيات أماكن مقدسة + قارئ قرآن كريم +
أذكار الصباح والمساء + بوصلة القبلة + سبحة عائمة + ودجت شاشة رئيسية.
يعمل بالعربية والإنجليزية، ويعمل أوفلاين بالكامل بعد التثبيت.

> Android live wallpaper app: Islamic clocks, holy-site wallpapers, an offline Quran
> reader, morning/evening athkar, a Qibla compass, a floating tasbeeh counter and
> home-screen widgets. Arabic + English, fully offline after install.

---

## البناء (Build)

| المتطلب | القيمة |
|---|---|
| Android Studio | Giraffe أو أحدث |
| JDK | **17** (إلزامي مع AGP 8.x — يأتي مدمجًا مع Android Studio) |
| AGP / Gradle | 8.13.2 / 8.13 |
| compileSdk / targetSdk | 36 (أندرويد 16) |
| minSdk | 23 |

```bash
# أول بناء بعد الاستنساخ: الـ wrapper jar القديم قد لا يحمّل Gradle 8.13،
# افتح المشروع في Android Studio ودعه يعيد توليد الـ wrapper، أو:
gradle wrapper --gradle-version 8.13 --distribution-type bin
```

التفاصيل الكاملة خطوة بخطوة في [`docs/ANDROID_STUDIO_SETUP.md`](docs/ANDROID_STUDIO_SETUP.md).

### الحزمة الجاهزة للتنزيل

`Allah-Clock-Live-Wallpaper-android-studio.zip` في جذر المستودع هي المشروع كاملًا كما يريده
Android Studio (بلا `.git` وبلا مجلدات `build/`). تُعاد بناؤها بعد أي تعديل مصدر:

```bash
python3 tools/build_package.py     # 404 ملفات — يجب أن تطابق الشجرة حرفيًا
```

> إن نزّلت الحزمة قديمًا فستبني **نسخة قديمة** من التطبيق؛ تحقّق دائمًا من أنها حديثة
> (`unzip -l` يعرض تاريخ البناء) أو نزّلها من فرع `main` مباشرة.

## الفحص قبل أي Pull Request

```bash
python3 tools/verify_resources.py                  # الموارد والنصوص وبيانات القرآن + شكل Gson
python3 tools/verify_resources.py --self-test      # تأكيد أن مدقق شكل Gson يمسك الخطأ فعلًا
python3 tools/verify_java_symbols.py               # رموز جافا: أصناف، أعضاء، Manifest، تواقيع SDK
python3 tools/verify_java_symbols.py --self-test   # تأكيد أن المدقق يمسك الأخطاء فعلًا
node tools/preview/smoke.js                        # منطق الواجهة (48 فحصًا)
python3 tools/build_package.py                     # إعادة بناء الحزمة بعد أي تعديل مصدر
```

- **مدقق الموارد** يفحص: صحة كل XML، كل مرجع `@string/@drawable/...` و`R.*`، تطابق
  العربية/الإنجليزية (نصوص + مصفوفات)، وسلامة بيانات القرآن (114 سورة، 6236 آية،
  604 صفحة، 30 جزءًا + بصمات SHA-256).
- **مدقق الرموز** يفحص ما يمنع البناء فعلًا: كل `import`، كل `Type.member` على أصناف التطبيق،
  كل `extends/implements`، كل مكوّن في `AndroidManifest.xml`، وتواقيع SDK التي تغيّرت في
  إصدارات جديدة (`AppOpenAd.load` مثلًا).

**النتيجة المطلوبة في كليهما: `NO ERRORS`.**

## قبل النشر على Google Play

1. **أرقام الإعلانات**: القيم الحالية في `ads/AdConfig.java` و`AndroidManifest.xml`
   هي أرقام اختبار رسمية من Google — استبدل الستة (App ID + خمس وحدات) بأرقامك من
   [apps.admob.com](https://apps.admob.com).
2. **versionCode** في `app/build.gradle`: ارفعه فوق آخر رقم منشور.
3. **Data safety** في Play Console: أعلن عن Advertising ID ومشاركة البيانات مع Google.
4. راجع القسم 2 في [`UPGRADE_NOTES.md`](UPGRADE_NOTES.md) للقائمة الكاملة.

## الميزات

- **خلفية حية** بثلاثة أنماط ساعات (تناظرية/رقمية/ذكية) + تاريخ هجري (تقويم أم القرى)
  + أذكار دوّارة + وضع توفير طاقة.
- **قارئ قرآن كريم أوفلاين**: النص العثماني الكامل (مشروع Tanzil، CC-BY 3.0)، فهرس
  السور، بحث فوري، قراءة بالسور أو بصفحات المصحف الـ604، استئناف القراءة، علامات
  مرجعية، حجم نص قابل للتعديل. **بلا أي إعلانات داخل شاشات القراءة.**
  السورة تُقرأ **صفحة واحدة متصلة** كما في المصحف: الآيات بجانب بعضها وكل آية تُختم
  بعلامة ۝ ورقمها، وانقر أي آية لحفظها فيُلوَّن موضعها.
  النسبة والبصمات في [`docs/QURAN_TEXT_ATTRIBUTION.md`](docs/QURAN_TEXT_ATTRIBUTION.md).
- **أذكار الصباح والمساء** (31 + 30 ذكرًا بمحتوى إسلام بوك) تظهر تلقائيًا في وقتها
  بحساب فلكي أوفلاين — بلا إنترنت وبلا تنبيهات خلفية. وفي الشاشة الرئيسية **بطاقة أذكار
  كبيرة دائمة** بحجم بطاقة القرآن، تظهر داخلها شارة ذهبية (صباح/مساء) في وقتها فقط.
- **بوصلة القبلة**: 73 مدينة مضمّنة، بلا GPS وبلا إنترنت.
- **سبحة عائمة** اختيارية + **ودجت** 2×2 و4×2 بنفس تصاميم الساعات.
- **إعلانات ضمن حدود السياسة**: موافقة UMP أولًا، بانر في الشاشات الفرعية فقط،
  Native في منتصف القوائم، Rewarded يفتح الساعات الذهبية، Interstitial بعد تطبيق
  الخلفية فقط، وApp Open عند العودة للمقدمة (تبريد 4 ساعات).

## البنية

```
app/src/main/java/org/Allah_Clock_Live_Wallpaper/
├── activity/     الشاشات (الرئيسية، المحرر، القرآن، الأذكار، القبلة، الخلفيات…)
├── ads/          كل الإعلانات: AdManager + AdConfig + المتحكمات (القسم 4 في UPGRADE_NOTES)
├── adapter/      محوّلات القوائم
├── model/        النماذج (الساعات، الأذكار، سور/آيات/صفحات/أجزاء القرآن)
├── service/      السبحة العائمة
├── utils/        TinyDB، التقويم الهجري، نوافذ الصلاة، مستودع القرآن…
├── viewUtils/    عناصر الرسم (الساعات، البوصلة، طبقة الخلفية)
└── widget/       ودجت الشاشة الرئيسية
tools/
├── verify_resources.py     مدقق الموارد (شغّله قبل كل PR)
├── verify_java_symbols.py  مدقق رموز جافا + حارس تواقيع SDK (شغّله قبل كل PR)
├── build_package.py         يبني حزمة Android Studio القابلة للتنزيل
├── athkar/                 مولّد محتوى الأذكار من المصدر
└── preview/                معاينة ويب تفاعلية للتصميم
docs/
├── ANDROID_STUDIO_SETUP.md
├── QURAN_TEXT_ATTRIBUTION.md
└── TASK_ATHKAR_MORNING_EVENING.md
```

سجل التطوير الكامل قسمًا بقسم في [`UPGRADE_NOTES.md`](UPGRADE_NOTES.md).
