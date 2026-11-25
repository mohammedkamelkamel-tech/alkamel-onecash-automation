package com.alkamel.onecash;

import org.junit.Test;
import static org.junit.Assert.*;

public class TransferParserTest {
    @Test public void parsesArabicDigitsAndName() {
        TransferParser.Parsed p = TransferParser.parse("استلمت ١٠٠.00\nمن ماجد علي صالح ا\nرصيدك 791.64 ر.ي");
        assertNotNull(p);
        assertEquals(100, p.amount);
        assertEquals("ماجد علي صالح ا", p.payerName);
    }

    @Test public void parsesEnglishDigits() {
        TransferParser.Parsed p = TransferParser.parse("استلمت 250\nمن احمد محمد");
        assertNotNull(p);
        assertEquals(250, p.amount);
    }

    @Test public void rejectsFractionalAmount() {
        assertNull(TransferParser.parse("استلمت 100.50\nمن احمد محمد"));
    }

    @Test public void rejectsMissingNameOrAmount() {
        assertNull(TransferParser.parse("استلمت 100"));
        assertNull(TransferParser.parse("من احمد محمد"));
    }

    @Test public void normalizesNameVariants() {
        assertEquals(TransferParser.normalizeName("أحمد  علي"), TransferParser.normalizeName("احمد علي"));
    }
}
