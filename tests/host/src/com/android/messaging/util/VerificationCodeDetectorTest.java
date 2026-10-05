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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public final class VerificationCodeDetectorTest {
    private static final String[][] LOCALIZED_CODE_MESSAGES = {
        { "af", "Jou bevestigingskode is 482913." },
        { "am", "የማረጋገጫ ኮድ፡ 482913" },
        { "ar", "رمز التحقق هو 482913" },
        { "az", "Təsdiq kodu: 482913" },
        { "bg", "Код за потвърждение: 482913" },
        { "bn", "যাচাইকরণ কোড: 482913" },
        { "ca", "Codi de verificació: 482913" },
        { "cs", "Ověřovací kód: 482913" },
        { "da", "Bekræftelseskode: 482913" },
        { "de", "Bestätigungscode: 482913" },
        { "el", "Κωδικός επαλήθευσης: 482913" },
        { "en", "Your verification code is 482913" },
        { "es", "Código de verificación: 482913" },
        { "et", "Kinnituskood: 482913" },
        { "eu", "Egiaztapen-kodea: 482913" },
        { "fa", "کد تأیید: 482913" },
        { "fi", "Vahvistuskoodi: 482913" },
        { "fr", "Code de vérification : 482913" },
        { "gl", "Código de verificación: 482913" },
        { "gu", "ચકાસણી કોડ: 482913" },
        { "hi", "सत्यापन कोड: 482913" },
        { "hr", "Verifikacijski kod: 482913" },
        { "hu", "Ellenőrző kód: 482913" },
        { "hy", "Հաստատման կոդ՝ 482913" },
        { "in", "Kode verifikasi: 482913" },
        { "is", "Staðfestingarkóði: 482913" },
        { "it", "Codice di verifica: 482913" },
        { "iw", "קוד אימות: 482913" },
        { "ja", "認証コード: 482913" },
        { "ka", "დადასტურების კოდი: 482913" },
        { "kk", "Растау коды: 482913" },
        { "km", "លេខកូដផ្ទៀងផ្ទាត់: 482913" },
        { "kn", "ಪರಿಶೀಲನೆ ಕೋಡ್: 482913" },
        { "ko", "인증 코드: 482913" },
        { "ky", "Ырастоо коду: 482913" },
        { "lo", "ລະຫັດຢືນຢັນ: 482913" },
        { "lt", "Patvirtinimo kodas: 482913" },
        { "lv", "Apstiprinājuma kods: 482913" },
        { "mk", "Код за потврда: 482913" },
        { "ml", "സ്ഥിരീകരണ കോഡ്: 482913" },
        { "mn", "Баталгаажуулах код: 482913" },
        { "mr", "पडताळणी कोड: 482913" },
        { "ms", "Kod pengesahan: 482913" },
        { "my", "အတည်ပြုကုဒ်: 482913" },
        { "nb", "Bekreftelseskode: 482913" },
        { "ne", "प्रमाणीकरण कोड: 482913" },
        { "nl", "Verificatiecode: 482913" },
        { "pa", "ਤਸਦੀਕ ਕੋਡ: 482913" },
        { "pl", "Kod weryfikacyjny: 482913" },
        { "pt", "Código de verificação: 482913" },
        { "ro", "Cod de verificare: 482913" },
        { "ru", "Код подтверждения: 482913" },
        { "si", "සත්‍යාපන කේතය: 482913" },
        { "sk", "Overovací kód: 482913" },
        { "sl", "Potrditvena koda: 482913" },
        { "sq", "Kodi i verifikimit: 482913" },
        { "sr-Latn", "Verifikacioni kod: 482913" },
        { "sr-Cyrl", "Верификациони код: 482913" },
        { "sv", "Verifieringskod: 482913" },
        { "sw", "Msimbo wa uthibitisho: 482913" },
        { "ta", "சரிபார்ப்பு குறியீடு: 482913" },
        { "te", "ధృవీకరణ కోడ్: 482913" },
        { "th", "รหัสยืนยัน: 482913" },
        { "tl", "Kodigo ng pagpapatunay: 482913" },
        { "tr", "Doğrulama kodu: 482913" },
        { "uk", "Код підтвердження: 482913" },
        { "ur", "تصدیقی کوڈ: 482913" },
        { "uz", "Tasdiqlash kodi: 482913" },
        { "vi", "Mã xác minh: 482913" },
        { "zh-Hans", "验证码：482913" },
        { "zh-Hant", "驗證碼：482913" },
        { "zu", "Ikhodi yokuqinisekisa: 482913" },
    };

    @Test
    public void findsNumericCodeWithKeywordBeforeOrAfter() {
        assertEquals("482913", VerificationCodeDetector.findCode(
                "Your verification code is 482913. Do not share it."));
        assertEquals("482913", VerificationCodeDetector.findCode(
                "482913 is your one-time password."));
    }

    @Test
    public void findsAlphanumericCode() {
        assertEquals("A1B2C3", VerificationCodeDetector.findCode(
                "Use code A1B2C3 to verify your account."));
    }

    @Test
    public void detectsCodeUsingEveryMessagingLocaleKeywordSet() {
        for (String[] localizedMessage : LOCALIZED_CODE_MESSAGES) {
            assertEquals("Locale " + localizedMessage[0], "482913",
                    VerificationCodeDetector.findCode(localizedMessage[1]));
        }
    }

    @Test
    public void supportsUnicodeDigitsAndGroupedDigits() {
        assertEquals("١٢٣٤٥٦", VerificationCodeDetector.findCode("رمز التحقق: ١٢٣٤٥٦"));
        assertEquals("१२३४५६", VerificationCodeDetector.findCode("सत्यापन कोड: १२३४५६"));
        assertEquals("123456", VerificationCodeDetector.findCode("Your OTP is 123 456"));
    }

    @Test
    public void choosesCodeNearestToKeywordInsteadOfExpiryYear() {
        assertEquals("482913", VerificationCodeDetector.findCode(
                "Verification code: 482913. This code expires in 2028."));
    }

    @Test
    public void ignoresNumbersWithoutVerificationWording() {
        assertNull(VerificationCodeDetector.findCode(
                "Call 5551234567 about order 482913."));
    }

    @Test
    public void rejectsShortAndLongNumericTokens() {
        assertNull(VerificationCodeDetector.findCode("Your code is 123."));
        assertNull(VerificationCodeDetector.findCode("Your code is 1234567890."));
    }

    @Test
    public void rejectsDateAndPhoneNumberNearCodeWording() {
        assertNull(VerificationCodeDetector.findCode("Verification code expired on 01/12/2026."));
        assertNull(VerificationCodeDetector.findCode(
                "For the security code call 555-123-4567."));
    }

    @Test
    public void rejectsAmbiguousEquallyNearCodes() {
        assertNull(VerificationCodeDetector.findCode("1234 code 5678"));
    }

    @Test
    public void acceptsDuplicateOccurrenceOfSameCode() {
        assertEquals("123456", VerificationCodeDetector.findCode(
                "Code 123456; repeat the code 123456 if asked."));
    }

    @Test
    public void rejectsNullEmptyAndCodeWithoutDigits() {
        assertNull(VerificationCodeDetector.findCode(null));
        assertNull(VerificationCodeDetector.findCode(""));
        assertNull(VerificationCodeDetector.findCode("Your verification code is ABCDEF."));
        assertFalse(VerificationCodeDetector.isValidCode("ABCD"));
        assertTrue(VerificationCodeDetector.isValidCode("A1B2"));
    }
}
