# Voice Control App

Ye ek simple Android app hai jo aapki awaaz (voice) se phone ke basic kaam karwati hai — bina touch kiye.

## Ye kya kar sakta hai (abhi)
- "flashlight on karo" / "torch" → flashlight jala deta hai
- "flash off" → flashlight bujha deta hai
- "call 03001234567" → number bol kar call milata hai
- "message 03001234567" → SMS app khol deta hai
- "whatsapp" → WhatsApp khol deta hai
- "camera" → camera khol deta hai
- "volume up" / "volume down" → awaaz control karta hai
- "search kuch bhi" → web search karta hai

## Setup — SIRF MOBILE SE (PC/laptop ke bina)
Ye poora process phone par hi ho sakta hai. Idea: code GitHub par bhej dein, GitHub apne server par khud APK bana dega, phir wo APK phone par download kar ke install kar lein.

**Cheezein chahiye:** GitHub ka free account, aur **Termux** app (Android).

1. **Termux install karein:** Play Store se nahi (purana version hai), balke [F-Droid](https://f-droid.org/) se F-Droid app install karein, phir F-Droid ke andar se "Termux" install karein.
2. **GitHub account banayein** (agar nahi hai): github.com par phone browser se free account bana lein.
3. **GitHub par khali repository banayein:** GitHub app ya browser se → New Repository → naam `VoiceControl` → Public → Create (README add na karein).
4. **Termux kholein** aur ye commands chalayein (ek-ek kar ke):
   ```
   pkg install git -y
   cd storage/downloads
   ```
   (Agar `storage/downloads` na chale to pehle `termux-setup-storage` chalayein aur permission allow karein, phir dobara try karein.)
5. Jahan aapne zip extract ki thi wahan jayein, jaise:
   ```
   cd VoiceControl/VoiceControl
   ```
   (Apne screenshot ke mutabiq path adjust kar lein.)
6. Git se GitHub par bhejein:
   ```
   git init
   git add .
   git commit -m "first version"
   git branch -M main
   git remote add origin https://github.com/AAPKA-USERNAME/VoiceControl.git
   git push -u origin main
   ```
   (`AAPKA-USERNAME` ki jagah apna GitHub username likhein. Push karte waqt username aur ek **Personal Access Token** maangega — password nahi chalega. Token banane ke liye: GitHub → Settings → Developer settings → Personal access tokens → Generate new token → "repo" permission select karein → token copy kar ke yahan password ki jagah paste karein.)
7. GitHub website par apni repository kholein → **Actions** tab → thodi der (5-10 min) mein build complete ho jayegi (hara ✅ tick dikhega).
8. Us build par click karein → neeche **Artifacts** mein `VoiceControl-debug-apk` milega → download karein (zip aayega, extract karein to andar `app-debug.apk` hogi).
9. Us APK par tap karein phone mein → "Install from unknown source" allow karein → Install ho jayegi.

Is tareeqe se offline model (Vosk) bhi automatically shamil ho jayega bashart aap ne assets/model-en-us folder mein model files pehle se daal rakhi hon (upar wali offline section dekhein) — GitHub push karne se pehle wo files daal lein.

---

## Setup — Computer/Laptop se (agar mil jaye)

1. [Android Studio](https://developer.android.com/studio) install karein (free hai).
2. Is poore `VoiceControl` folder ko Android Studio mein **Open** karein (File → Open).
3. Gradle sync hone dein (pehli dafa internet chahiye hoga, dependencies download hongi).
4. Apna phone USB se connect karein aur **USB debugging** on karein (Settings → Developer Options), ya Android Studio ka emulator use karein.
5. Green **Run ▶** button dabayein — app phone par install ho jayegi.
6. Pehli dafa app kholne par ye microphone, call, aur SMS ki permission maangega — allow kar dein.

## Nayi commands add karni hain?
`MainActivity.kt` file mein `handleCommand()` function ke andar naya `when` condition add karein — jaise:
```kotlin
command.contains("wifi") -> toggleWifi()
```

## "Hamesha sunna" (background mode)
App mein ab ek "Hamesha sunna: OFF/ON" button hai. Ise ek baar ON kar dein to:
- Phone bina uthaye, bina chhue, bolne se commands chalti rahengi.
- Ek chhoti si notification hamesha dikhti rahegi ("Voice Control sun raha hai...") — ye Android ka apna rule hai, kisi bhi app mein mic background mein chale to ye notification chupa nahi sakte (transparency ke liye). Isse app band nahi hoti, bas aapko pata rehta hai ke mic active hai.
- Band karne ke liye dobara button dabayein, ya notification se "Stop" karein.

## Offline — ab guaranteed hai (Vosk)
Ye app ab **Vosk** use karta hai — ek open-source engine jo poori tarah phone ke andar chalta hai. **Internet kabhi nahi lagega**, kisi bhi waqt nahi. Lekin isay kaam karne ke liye ek chhota "model" (language ka data) app ke andar dalna padega — ye file bahut badi (~40-50MB) hoti hai isliye main ise khud aapki jagah download nahi kar saka (mera environment offline hai). Aapko ye ek dafa khud karna hoga:

1. Ye link kholein: https://alphacephei.com/vosk/models
2. **`vosk-model-small-en-us-0.15`** wala model download karein (chhota, fast, ~40MB — English commands ke liye best).
   - Agar Urdu/Hindi mein bolna hai to us page par Hindi (`vosk-model-small-hi`) ya doosri language ka model dhoondein, waisa hi tareeqa follow karein.
3. Downloaded `.zip` ko extract karein — andar ek folder hoga jismein `am`, `conf`, `graph` waghera hongi.
4. Us poore folder ke andar ki cheezein copy karke is project ke `app/src/main/assets/model-en-us/` folder mein paste kar dein (aur `PUT_MODEL_FILES_HERE.txt` file delete kar dein).
5. Android Studio mein Gradle sync karein aur Run karein — bas, ab app 100% offline chalegi.

**Note:** Agar English ke ilawa dusri language chahiye, to model ka naam kuch bhi ho, use `assets/model-en-us` folder ke andar hi rakhein (folder ka naam code mein fix hai) — bas andar ki files sahi language ki honi chahiye.

## Zaroori baat
- Kuch commands (jaise call karna) ke liye phone permissions dena zaroori hai.
- Battery: hamesha sunna mode thoda battery zyada use karega, jaisa kisi bhi voice assistant app mein hota hai.
