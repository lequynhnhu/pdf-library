package nl.mad.pdflibrary.syntax;

import static org.junit.Assert.assertEquals;

import java.io.UnsupportedEncodingException;

import nl.mad.pdflibrary.api.BaseFont;
import nl.mad.pdflibrary.api.BaseParagraph;
import nl.mad.pdflibrary.api.BaseText;
import nl.mad.pdflibrary.model.Paragraph;
import nl.mad.pdflibrary.model.Text;
import nl.mad.pdflibrary.pdf.syntax.PdfFont;
import nl.mad.pdflibrary.pdf.syntax.PdfIndirectObject;
import nl.mad.pdflibrary.pdf.syntax.PdfObjectType;
import nl.mad.pdflibrary.pdf.syntax.PdfPage;
import nl.mad.pdflibrary.pdf.syntax.PdfText;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class PdfTextTest {
    private PdfText pdfText;
    private static PdfIndirectObject fontReference;
    private static PdfPage page;

    @BeforeClass
    public static void setUpTestObjects() throws Exception {
        fontReference = new PdfIndirectObject(1, 0, new PdfFont(new BaseFont()), true);
        fontReference.getReference().setResourceReference("R1");
        page = new PdfPage(300, 300);
        page.setMargins(10, 0, 0, 11);
    }

    @Before
    public void setUp() throws Exception {
        pdfText = new PdfText();
    }

    @Test
    public void testCreation() {
        assertEquals(PdfObjectType.TEXT, pdfText.getType());
    }

    @Test
    public void testTextAdding() throws UnsupportedEncodingException {
        Text text = new BaseText("Test").size(10).on(20, 20);

        //expected result for font adding, matrix adding and text adding
        String expectedTotalResult = "/R1 10 Tf\n" + "1.0 0.0 0.0 1.0 20 20 Tm\n" + "[(T) 70 (est)] TJ\n";
        pdfText.addText(text, fontReference);
        assertEquals(expectedTotalResult, new String(pdfText.getByteRepresentation(), "UTF-8"));
    }

    @Test
    public void testParagraphTextAdding() throws UnsupportedEncodingException {
        pdfText = new PdfText();
        Paragraph p = new BaseParagraph().addText(new BaseText("Test Test Test")).addText(new BaseText("Test2"));
        for (int i = 0; i < p.getTextCollection().size(); ++i) {
            pdfText.addText(p.getTextCollection().get(i), fontReference);
        }
        String expectedResult = "/R1 12 Tf\n1.0 0.0 0.0 1.0 -1 -1 Tm\n[(T) 70 (est)] TJ\n[(T) 70 (est)] TJ\n"
                + "[(T) 70 (est)] TJ\n/R1 12 Tf\n1.0 0.0 0.0 1.0 -1 -1 Tm\n[(T) 70 (est2)] TJ\n";
        assertEquals(expectedResult, new String(pdfText.getByteRepresentation(), "UTF-8"));
    }
}
