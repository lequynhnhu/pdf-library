package nl.mad.toucanpdf.syntax;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import nl.mad.toucanpdf.model.PdfNameValue;
import nl.mad.toucanpdf.pdf.syntax.PdfDictionary;
import nl.mad.toucanpdf.pdf.syntax.PdfName;
import nl.mad.toucanpdf.pdf.syntax.PdfNumber;
import nl.mad.toucanpdf.pdf.syntax.PdfObjectType;

import org.junit.Before;
import org.junit.Test;

public class PdfDictionaryTest {
    private PdfDictionary dictionary;

    @Before
    public void setUp() throws Exception {
        dictionary = new PdfDictionary(PdfObjectType.DICTIONARY);
    }

    @Test
    public void testContain() {
        PdfName key = new PdfName(PdfNameValue.ASCENT);
        PdfNumber value = new PdfNumber(1);
        dictionary.put(key, value);
        assertEquals("Method returned incorrect result. ", true, dictionary.containsKey(key));
        assertEquals("Method returned incorrect result. ", true, dictionary.containsValue(value));

    }

    @Test
    public void testWriteToFile() throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        dictionary.put(PdfNameValue.ASCENT, new PdfNumber(1));
        String expectedResult = "<<\n /Ascent 1\n>>";
        dictionary.writeToFile(baos);
        assertEquals("The write output was not as expected.", expectedResult, baos.toString());
    }
}
