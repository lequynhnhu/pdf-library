package nl.mad.pdflibrary.syntax;

import static org.junit.Assert.assertEquals;
import nl.mad.pdflibrary.api.BaseFont;
import nl.mad.pdflibrary.model.Font;
import nl.mad.pdflibrary.model.FontFamily;
import nl.mad.pdflibrary.model.FontMetrics;
import nl.mad.pdflibrary.model.PdfNameValue;
import nl.mad.pdflibrary.pdf.syntax.PdfArray;
import nl.mad.pdflibrary.pdf.syntax.PdfFont;
import nl.mad.pdflibrary.pdf.syntax.PdfIndirectObjectReference;
import nl.mad.pdflibrary.pdf.syntax.PdfName;
import nl.mad.pdflibrary.pdf.syntax.PdfNumber;
import nl.mad.pdflibrary.utility.FloatEqualityTester;

import org.junit.Before;
import org.junit.Test;

public class PdfFontTest {
    PdfFont pdfFont;
    Font font;

    @Before
    public void setUp() throws Exception {
        font = new BaseFont();
        pdfFont = new PdfFont(font);
    }

    @Test
    public void testFontExtraction() {
        assertEquals(pdfFont.get(PdfNameValue.TYPE), new PdfName(PdfNameValue.FONT));

        FontFamily base = font.getFontFamily();
        FontMetrics metrics = base.getMetricsForStyle(font.getStyle());
        assertEquals(pdfFont.get(PdfNameValue.BASE_FONT), new PdfName(base.getNameOfStyle(font.getStyle())));
        assertEquals(pdfFont.get(PdfNameValue.SUB_TYPE), new PdfName(base.getSubType().getPdfNameValue()));
        assertEquals(((PdfNumber) (pdfFont.get(PdfNameValue.FIRST_CHAR))).getNumber(), metrics.getFirstCharCode(), FloatEqualityTester.EPSILON);
        assertEquals(((PdfNumber) (pdfFont.get(PdfNameValue.LAST_CHAR))).getNumber(), metrics.getLastCharCode(), FloatEqualityTester.EPSILON);
        assertEquals(((PdfArray) (pdfFont.get(PdfNameValue.WIDTHS))).getSize(), metrics.getWidths(metrics.getFirstCharCode(), metrics.getLastCharCode()).size());
    }

    @Test
    public void testFontDescriptor() {
        PdfIndirectObjectReference reference = new PdfIndirectObjectReference(4, 1);
        pdfFont.setFontDescriptorReference(reference);
        assertEquals(reference, pdfFont.get(PdfNameValue.FONT_DESCRIPTOR));
    }

    @Test
    public void testFontEncoding() {
        //default encoding test
        PdfName encoding = new PdfName(PdfNameValue.ASCII_HEX_DECODE);
        pdfFont.setFontEncoding(encoding);
        assertEquals(encoding, pdfFont.get(PdfNameValue.ENCODING));
        //custom encoding test
        PdfIndirectObjectReference reference = new PdfIndirectObjectReference(5, 1);
        pdfFont.setFontEncodingReference(reference);
        assertEquals(reference, pdfFont.get(PdfNameValue.ENCODING));
    }
}
