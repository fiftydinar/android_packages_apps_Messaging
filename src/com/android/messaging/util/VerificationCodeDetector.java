/*
 * Copyright (C) 2026 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.android.messaging.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Finds a single verification code only when the message labels it as one. */
public final class VerificationCodeDetector {
    private static final int KEYWORD_PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern CODE_TOKEN = Pattern.compile("[\\p{L}\\p{N}]{4,10}");
    private static final Pattern CODE_CANDIDATE = Pattern.compile(
            "(?<![\\p{L}\\p{N}])([\\p{L}\\p{N}]{4,10}|\\p{Nd}{3}\\s\\p{Nd}{3})"
                    + "(?![\\p{L}\\p{N}])");
    private static final Pattern DATE_CANDIDATE = Pattern.compile(
            "(?<![\\p{L}\\p{N}])(?:\\p{Nd}{1,2}[-/.]\\p{Nd}{1,2}[-/.]"
                    + "(?:\\p{Nd}{2}|\\p{Nd}{4})|\\p{Nd}{4}[-/.]\\p{Nd}{1,2}[-/.]"
                    + "\\p{Nd}{1,2})(?![\\p{L}\\p{N}])");
    private static final Pattern PHONE_CANDIDATE = Pattern.compile(
            "(?<!\\p{N})\\+?\\p{Nd}[\\p{Nd}() .-]{7,}\\p{Nd}(?!\\p{N})");
    private static final Pattern BOUNDARY_CODE_KEYWORDS = compileKeywords(
            new String[] {
                // English and abbreviations shared across many locales.
                "verification code", "verification-code", "verificationcode", "security code",
                "security-code", "one-time password", "one time password", "one-time passcode",
                "one time passcode", "passcode", "pass code", "otp", "pin", "code",
                // Afrikaans, Amharic, and Arabic.
                "verifikasiekode", "bevestigingskode", "veiligheidskode", "kode", "wagwoord",
                "ኮድ", "የማረጋገጫ ኮድ", "የደህንነት ኮድ", "የይለፍ ቃል", "رمز التحقق", "رمز التأكيد",
                "رمز الأمان", "رمز", "كلمة المرور", "كلمة السر",
                // Azerbaijani, Bulgarian, Bengali, Catalan, and Czech.
                "təsdiq kodu", "doğrulama kodu", "təhlükəsizlik kodu", "kod", "şifrə",
                "код за потвърждение", "код за сигурност", "код", "парола", "еднократна парола",
                "যাচাইকরণ কোড", "নিরাপত্তা কোড", "কোড", "পাসওয়ার্ড", "এককালীন",
                "codi de verificació", "codi de seguretat", "codi", "contrasenya", "un sol ús",
                "ověřovací kód", "potvrzovací kód", "bezpečnostní kód", "kód", "heslo",
                "jednorázový",
                // Danish, German, Greek, Spanish, Estonian, and Basque.
                "bekræftelseskode", "sikkerhedskode", "engangskode", "kode", "adgangskode",
                "bestätigungscode", "verifizierungscode", "sicherheitscode", "code", "passwort",
                "einmalcode", "κωδικός επαλήθευσης", "κωδικός επιβεβαίωσης", "κωδικός ασφαλείας",
                "κωδικός", "κωδικός μιας χρήσης", "código de verificación", "código de seguridad",
                "código", "contraseña", "un solo uso", "kinnituskood", "turvakood", "kood",
                "ühekordne parool", "egiaztapen-kodea", "segurtasun-kodea", "kodea", "pasahitza",
                // Persian, Finnish, French, Galician, Gujarati, and Hindi.
                "کد تأیید", "کد تایید", "کد امنیتی", "کد", "رمز عبور", "رمز یکبار مصرف",
                "vahvistuskoodi", "varmistuskoodi", "turvakoodi", "koodi", "kertakäyttöinen salasana",
                "code de vérification", "code de sécurité", "code", "mot de passe", "code à usage unique",
                "código de verificación", "código de seguridade", "código", "contrasinal",
                "ચકાસણી કોડ", "પુષ્ટિ કોડ", "સુરક્ષા કોડ", "કોડ", "પાસવર્ડ",
                "सत्यापन कोड", "पुष्टि कोड", "सुरक्षा कोड", "कोड", "पासवर्ड", "एक बार का",
                // Croatian, Hungarian, Armenian, Indonesian, Icelandic, and Italian.
                "verifikacijski kod", "kod za potvrdu", "sigurnosni kod", "kod", "lozinka",
                "jednokratna lozinka", "ellenőrző kód", "megerősítő kód", "biztonsági kód", "jelszó",
                "egyszer használatos", "հաստատման կոդ", "ստուգման կոդ", "անվտանգության կոդ", "կոդ",
                "գաղտնաբառ", "kode verifikasi", "kode konfirmasi", "kode keamanan", "kode",
                "kata sandi", "sandi", "sekali pakai", "staðfestingarkóði", "öryggiskóði", "kóði",
                "lykilorð", "codice di verifica", "codice di sicurezza", "codice", "password",
                "monouso",
                // Hebrew, Georgian, Kazakh, Kannada, Korean, and Kyrgyz.
                "קוד אימות", "קוד אישור", "קוד אבטחה", "קוד", "סיסמה", "חד פעמי",
                "დადასტურების კოდი", "შემოწმების კოდი", "უსაფრთხოების კოდი", "კოდი", "პაროლი",
                "растау коды", "тексеру коды", "қауіпсіздік коды", "код", "құпиясөз",
                "ಪರಿಶೀಲನೆ ಕೋಡ್", "ದೃಢೀಕರಣ ಕೋಡ್", "ಭದ್ರತಾ ಕೋಡ್", "ಕೋಡ್", "ಪಾಸ್‌ವರ್ಡ್",
                "인증 코드", "인증번호", "확인 코드", "보안 코드", "일회용 비밀번호", "코드",
                "ырастоо коду", "текшерүү коду", "коопсуздук коду", "код", "сырсөз",
                // Lithuanian, Latvian, Macedonian, Malayalam, Mongolian, Marathi, Malay.
                "patvirtinimo kodas", "saugos kodas", "kodas", "slaptažodis", "vienkartinis",
                "apstiprinājuma kods", "drošības kods", "kods", "parole", "vienreizējs",
                "код за потврда", "безбедносен код", "код", "лозинка", "еднократна лозинка",
                "സ്ഥിരീകരണ കോഡ്", "സുരക്ഷാ കോഡ്", "കോഡ്", "പാസ്‌വേഡ്", "баталгаажуулах код",
                "аюулгүй байдлын код", "нууц үг", "нэг удаагийн", "पडताळणी कोड", "सत्यापन कोड",
                "सुरक्षा कोड", "कोड", "पासवर्ड", "kod pengesahan", "kod keselamatan", "kod",
                "kata laluan", "sekali guna",
                // Burmese, Norwegian, Nepali, Dutch, Punjabi, Polish, Portuguese, Romanian.
                "bekreftelseskode", "sikkerhetskode", "engangskode", "kode", "passord",
                "प्रमाणीकरण कोड", "पुष्टि कोड", "सुरक्षा कोड", "कोड", "पासवर्ड",
                "verificatiecode", "bevestigingscode", "beveiligingscode", "code", "wachtwoord",
                "eenmalige", "ਤਸਦੀਕ ਕੋਡ", "ਪੁਸ਼ਟੀਕਰਨ ਕੋਡ", "ਸੁਰੱਖਿਆ ਕੋਡ", "ਕੋਡ", "ਪਾਸਵਰਡ",
                "kod weryfikacyjny", "kod potwierdzający", "kod bezpieczeństwa", "kod", "hasło",
                "jednorazowy", "código de verificação", "código de segurança", "código", "senha",
                "palavra-passe", "uso único", "cod de verificare", "cod de securitate", "cod",
                "parolă", "de unică folosință",
                // Russian, Sinhala, Slovak, Slovenian, Albanian, and Serbian (Latin and Cyrillic).
                "код подтверждения", "проверочный код", "код безопасности", "код", "пароль",
                "одноразовый", "සත්‍යාපන කේතය", "තහවුරු කිරීමේ කේතය", "ආරක්ෂක කේතය", "කේතය",
                "මුරපදය", "overovací kód", "potvrdzovací kód", "bezpečnostný kód", "kód", "heslo",
                "jednorazový", "potrditvena koda", "varnostna koda", "koda", "geslo", "enkratna",
                "kodi i verifikimit", "kodi i konfirmimit", "kodi i sigurisë", "kod", "fjalëkalim",
                "verifikacioni kod", "kod za potvrdu", "bezbednosni kod", "sigurnosni kod", "kod",
                "šifra", "jednokratna lozinka", "верификациони код", "код за потврду",
                "безбедносни код", "код", "шифра", "једнократна лозинка",
                // Swedish, Swahili, Tamil, Telugu, Tagalog, Turkish, Ukrainian, Urdu, Uzbek.
                "verifieringskod", "bekräftelsekod", "säkerhetskod", "engångskod", "kod", "lösenord",
                "msimbo wa uthibitisho", "msimbo wa usalama", "msimbo", "nenosiri", "neno la siri",
                "சரிபார்ப்பு குறியீடு", "உறுதிப்படுத்தல் குறியீடு", "பாதுகாப்பு குறியீடு", "குறியீடு",
                "கடவுச்சொல்", "ధృవీకరణ కోడ్", "నిర్ధారణ కోడ్", "భద్రతా కోడ్", "కోడ్", "పాస్‌వర్డ్",
                "kodigo ng pagpapatunay", "security code", "code", "password", "isang beses",
                "doğrulama kodu", "onay kodu", "güvenlik kodu", "kod", "parola", "tek kullanımlık şifre",
                "код підтвердження", "код перевірки", "код безпеки", "код", "пароль", "одноразовий",
                "تصدیقی کوڈ", "تصدیق کا کوڈ", "سیکیورٹی کوڈ", "کوڈ", "پاس ورڈ", "یک بار",
                "tasdiqlash kodi", "tasdiq kodi", "xavfsizlik kodi", "kod", "parol", "bir martalik",
                // Vietnamese and Zulu.
                "mã xác minh", "mã xác thực", "mã bảo mật", "mã", "mật khẩu", "dùng một lần",
                "ikhodi yokuqinisekisa", "ikhodi yokuphepha", "ikhodi", "iphasiwedi",
            }, true);
    private static final Pattern SUBSTRING_CODE_KEYWORDS = compileKeywords(
            new String[] {
                // Chinese (Simplified and Traditional), Japanese, Thai, Khmer, Lao, and Burmese
                // do not consistently separate words with spaces.
                "验证码", "驗證碼", "确认码", "確認碼", "安全码", "安全碼", "一次性密码", "一次性密碼",
                "代码", "代碼", "認証コード", "確認コード", "検証コード", "ワンタイムパスワード",
                "暗証番号", "セキュリティコード", "コード", "รหัสยืนยัน", "รหัสตรวจสอบ",
                "รหัสความปลอดภัย", "รหัสผ่าน", "លេខកូដផ្ទៀងផ្ទាត់", "លេខកូដសុវត្ថិភាព", "លេខកូដ",
                "ពាក្យសម្ងាត់", "ລະຫັດຢືນຢັນ", "ລະຫັດຄວາມປອດໄພ", "ລະຫັດຜ່ານ", "ລະຫັດ",
                "အတည်ပြုကုဒ်", "အတည်ပြုခြင်းကုဒ်", "လုံခြုံရေးကုဒ်", "ကုဒ်", "စကားဝှက်",
            }, false);

    private static final int MAX_KEYWORD_DISTANCE = 56;
    private static final int MAX_NUMERIC_CODE_LENGTH = 8;

    private VerificationCodeDetector() {
    }

    private static Pattern compileKeywords(final String[] keywords, final boolean wordBoundaries) {
        final StringBuilder expression = new StringBuilder();
        if (wordBoundaries) {
            expression.append("(?<![\\p{L}\\p{N}])");
        }
        expression.append("(?:");
        for (int i = 0; i < keywords.length; i++) {
            if (i > 0) {
                expression.append('|');
            }
            final String[] words = keywords[i].trim().split("\\s+");
            for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
                if (wordIndex > 0) {
                    expression.append("\\s+");
                }
                expression.append(Pattern.quote(words[wordIndex]));
            }
        }
        expression.append(')');
        if (wordBoundaries) {
            expression.append("(?![\\p{L}\\p{N}])");
        }
        return Pattern.compile(expression.toString(), KEYWORD_PATTERN_FLAGS);
    }

    /**
     * Returns the nearest valid code in a message with nearby verification-code wording.
     * Ambiguous equally-near candidates are rejected rather than copying an arbitrary number.
     */
    public static String findCode(final String message) {
        if (message == null || message.isEmpty()) {
            return null;
        }

        String closestCode = null;
        int closestDistance = Integer.MAX_VALUE;
        boolean ambiguous = false;
        final Matcher candidateMatcher = CODE_CANDIDATE.matcher(message);
        while (candidateMatcher.find()) {
            final String candidate = normalizeCode(candidateMatcher.group(1));
            if (!isValidCode(candidate)
                    || overlapsFalsePositive(message, candidateMatcher.start(1),
                            candidateMatcher.end(1))) {
                continue;
            }
            final int distance = distanceToNearestKeyword(message, candidateMatcher.start(1),
                    candidateMatcher.end(1));
            if (distance > MAX_KEYWORD_DISTANCE) {
                continue;
            }
            if (distance < closestDistance) {
                closestCode = candidate;
                closestDistance = distance;
                ambiguous = false;
            } else if (distance == closestDistance && !candidate.equals(closestCode)) {
                ambiguous = true;
            }
        }
        return closestCode == null || ambiguous ? null : closestCode;
    }

    /** Checks that a copied value is a code token, not arbitrary text. */
    public static boolean isValidCode(final String code) {
        if (code == null) {
            return false;
        }
        final String normalizedCode = normalizeCode(code);
        if (!CODE_TOKEN.matcher(normalizedCode).matches()) {
            return false;
        }
        boolean hasDigit = false;
        boolean allDigits = true;
        for (int i = 0; i < normalizedCode.length(); i++) {
            final int codePoint = normalizedCode.codePointAt(i);
            hasDigit |= Character.isDigit(codePoint);
            allDigits &= Character.isDigit(codePoint);
            if (Character.isSupplementaryCodePoint(codePoint)) {
                i++;
            }
        }
        return hasDigit && (!allDigits || normalizedCode.codePointCount(0, normalizedCode.length())
                <= MAX_NUMERIC_CODE_LENGTH);
    }

    private static String normalizeCode(final String code) {
        return code.replaceAll("\\s+", "");
    }

    private static boolean overlapsFalsePositive(final String message, final int start,
            final int end) {
        return overlapsMatch(DATE_CANDIDATE.matcher(message), start, end)
                || overlapsMatch(PHONE_CANDIDATE.matcher(message), start, end);
    }

    private static boolean overlapsMatch(final Matcher matcher, final int start, final int end) {
        while (matcher.find()) {
            if (matcher.start() <= start && matcher.end() >= end) {
                return true;
            }
        }
        return false;
    }

    private static int distanceToNearestKeyword(final String message, final int codeStart,
            final int codeEnd) {
        int distance = Integer.MAX_VALUE;
        for (Pattern pattern : new Pattern[] { BOUNDARY_CODE_KEYWORDS, SUBSTRING_CODE_KEYWORDS }) {
            final Matcher keywordMatcher = pattern.matcher(message);
            while (keywordMatcher.find()) {
                final int keywordDistance;
                if (keywordMatcher.end() <= codeStart) {
                    keywordDistance = codeStart - keywordMatcher.end();
                } else if (keywordMatcher.start() >= codeEnd) {
                    keywordDistance = keywordMatcher.start() - codeEnd;
                } else {
                    keywordDistance = 0;
                }
                distance = Math.min(distance, keywordDistance);
            }
        }
        return distance;
    }
}
