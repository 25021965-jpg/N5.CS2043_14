package client.util;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class TextUtilsTest {

    // ==================== toTitleCase ====================

    @Test
    void toTitleCase_null_returnsEmDash() {
        assertEquals("—", TextUtils.toTitleCase(null));
    }

    @Test
    void toTitleCase_blank_returnsEmDash() {
        assertEquals("—", TextUtils.toTitleCase("   "));
    }

    @Test
    void toTitleCase_singleWord_capitalized() {
        assertEquals("Hello", TextUtils.toTitleCase("hello"));
    }

    @Test
    void toTitleCase_multipleWords_eachCapitalized() {
        assertEquals("Hello World", TextUtils.toTitleCase("hello world"));
    }

    @Test
    void toTitleCase_underscoreInput_convertedToSpaceAndCapitalized() {
        assertEquals("Hello World", TextUtils.toTitleCase("hello_world"));
    }

    @Test
    void toTitleCase_allUpperCase_loweredThenCapitalized() {
        assertEquals("Abc Def", TextUtils.toTitleCase("ABC DEF"));
    }

    @Test
    void toTitleCase_extraSpaces_filteredOut() {
        assertEquals("A B", TextUtils.toTitleCase("  a   b  "));
    }

    @Test
    void toTitleCase_singleChar_capitalized() {
        assertEquals("A", TextUtils.toTitleCase("a"));
    }

    // ==================== fallback ====================

    @Test
    void fallback_nullValue_returnsFallback() {
        assertEquals("default", TextUtils.fallback(null, "default"));
    }

    @Test
    void fallback_blankValue_returnsFallback() {
        assertEquals("default", TextUtils.fallback("   ", "default"));
    }

    @Test
    void fallback_validValue_returnsTrimmedValue() {
        assertEquals("hello", TextUtils.fallback("  hello  ", "default"));
    }

    // ==================== safeText ====================

    @Test
    void safeText_null_returnsEmDash() {
        assertEquals("—", TextUtils.safeText(null));
    }

    @Test
    void safeText_blank_returnsEmDash() {
        assertEquals("—", TextUtils.safeText(""));
    }

    @Test
    void safeText_valid_returnsTrimmed() {
        assertEquals("value", TextUtils.safeText("  value  "));
    }

    // ==================== truncate ====================

    @Test
    void truncate_null_returnsEmpty() {
        assertEquals("", TextUtils.truncate(null, 10));
    }

    @Test
    void truncate_blank_returnsEmpty() {
        assertEquals("", TextUtils.truncate("   ", 10));
    }

    @Test
    void truncate_shorterThanMax_returnsOriginal() {
        assertEquals("Hello", TextUtils.truncate("Hello", 10));
    }

    @Test
    void truncate_exactMax_returnsOriginal() {
        assertEquals("Hello", TextUtils.truncate("Hello", 5));
    }

    @Test
    void truncate_longerThanMax_appendsEllipsis() {
        assertEquals("Hello...", TextUtils.truncate("Hello World", 5));
    }

    @Test
    void truncate_maxZero_returnsOnlyEllipsis() {
        assertEquals("...", TextUtils.truncate("Hello", 0));
    }

    // ==================== formatUsername ====================

    @Test
    void formatUsername_null_returnsAtUnknown() {
        assertEquals("@unknown", TextUtils.formatUsername(null));
    }

    @Test
    void formatUsername_blank_returnsAtUnknown() {
        assertEquals("@unknown", TextUtils.formatUsername("   "));
    }

    @Test
    void formatUsername_withoutAt_prependsAt() {
        assertEquals("@alice", TextUtils.formatUsername("alice"));
    }

    @Test
    void formatUsername_alreadyHasAt_noDoubleAt() {
        assertEquals("@alice", TextUtils.formatUsername("@alice"));
    }

    // ==================== hideEmail ====================

    @Test
    void hideEmail_null_returnsEmDash() {
        assertEquals("—", TextUtils.hideEmail(null));
    }

    @Test
    void hideEmail_noAtSign_returnsEmDash() {
        assertEquals("—", TextUtils.hideEmail("notanemail"));
    }

    @Test
    void hideEmail_shortLocalPart_masksAll() {
        assertEquals("***@example.com", TextUtils.hideEmail("ab@example.com"));
    }

    @Test
    void hideEmail_singleCharLocalPart_masksAll() {
        assertEquals("***@example.com", TextUtils.hideEmail("a@example.com"));
    }

    @Test
    void hideEmail_longLocalPart_showsTwoCharsAndMasks() {
        assertEquals("al****@example.com", TextUtils.hideEmail("alice@example.com"));
    }

    @Test
    void hideEmail_multipleAtSigns_returnsEmDash() {
        // split("@") gives 3 parts — length != 2
        assertEquals("—", TextUtils.hideEmail("a@b@c.com"));
    }

    // ==================== capitalizeFirst ====================

    @Test
    void capitalizeFirst_null_returnsEmpty() {
        assertEquals("", TextUtils.capitalizeFirst(null));
    }

    @Test
    void capitalizeFirst_blank_returnsEmpty() {
        assertEquals("", TextUtils.capitalizeFirst("   "));
    }

    @Test
    void capitalizeFirst_upperCaseInput_lowersRest() {
        assertEquals("Hello", TextUtils.capitalizeFirst("HELLO"));
    }

    @Test
    void capitalizeFirst_lowerCaseInput_capitalizesFirst() {
        assertEquals("Hello", TextUtils.capitalizeFirst("hello"));
    }

    @Test
    void capitalizeFirst_singleChar_capitalized() {
        assertEquals("A", TextUtils.capitalizeFirst("a"));
    }

    // ==================== normalizeSpaces ====================

    @Test
    void normalizeSpaces_null_returnsEmpty() {
        assertEquals("", TextUtils.normalizeSpaces(null));
    }

    @Test
    void normalizeSpaces_leadingTrailing_trimmed() {
        assertEquals("hello world", TextUtils.normalizeSpaces("  hello  world  "));
    }

    @Test
    void normalizeSpaces_multipleInternalSpaces_collapsed() {
        assertEquals("a b c", TextUtils.normalizeSpaces("a   b   c"));
    }

    @Test
    void normalizeSpaces_normalInput_unchanged() {
        assertEquals("hello world", TextUtils.normalizeSpaces("hello world"));
    }

    // ==================== formatCurrency ====================

    @Test
    void formatCurrency_null_returnsZero() {
        assertEquals("$0.00", TextUtils.formatCurrency(null));
    }

    @Test
    void formatCurrency_zero_formatsCorrectly() {
        String result = TextUtils.formatCurrency(BigDecimal.ZERO);
        assertTrue(result.contains("0"));
    }

    @Test
    void formatCurrency_positiveAmount_includesDollarSign() {
        String result = TextUtils.formatCurrency(new BigDecimal("1500.50"));
        assertTrue(result.startsWith("$"));
    }

    @Test
    void formatCurrency_largeAmount_includesThousandSeparator() {
        String result = TextUtils.formatCurrency(new BigDecimal("1000000"));
        assertTrue(result.contains(","));
    }

    // ==================== formatDateTime ====================

    @Test
    void formatDateTime_null_returnsEmDash() {
        assertEquals("—", TextUtils.formatDateTime(null));
    }

    @Test
    void formatDateTime_validDate_formattedCorrectly() {
        LocalDateTime dt = LocalDateTime.of(2024, 12, 31, 23, 59);
        assertEquals("31/12/2024 23:59", TextUtils.formatDateTime(dt));
    }

    // ==================== formatRemainingTime ====================

    @Test
    void formatRemainingTime_null_returnsEnded() {
        assertEquals("Ended", TextUtils.formatRemainingTime(null));
    }

    @Test
    void formatRemainingTime_negativeDuration_returnsEnded() {
        assertEquals("Ended", TextUtils.formatRemainingTime(Duration.ofSeconds(-1)));
    }

    @Test
    void formatRemainingTime_zeroDuration_returnsZeros() {
        assertEquals("00h 00m 00s", TextUtils.formatRemainingTime(Duration.ZERO));
    }

    @Test
    void formatRemainingTime_exactHour_formattedCorrectly() {
        assertEquals("01h 00m 00s", TextUtils.formatRemainingTime(Duration.ofHours(1)));
    }

    @Test
    void formatRemainingTime_complex_formattedCorrectly() {
        Duration d = Duration.ofHours(2).plusMinutes(30).plusSeconds(15);
        assertEquals("02h 30m 15s", TextUtils.formatRemainingTime(d));
    }

    // ==================== formatBidIncrement ====================

    @Test
    void formatBidIncrement_amount_startWithPlus() {
        String result = TextUtils.formatBidIncrement(new BigDecimal("100"));
        assertTrue(result.startsWith("+ "));
    }

    @Test
    void formatBidIncrement_null_handledByFormatCurrency() {
        String result = TextUtils.formatBidIncrement(null);
        assertEquals("+ $0.00", result);
    }
}