package nl.mad.toucanpdf.structure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import mockit.Mock;
import mockit.MockUp;
import nl.mad.toucanpdf.api.BaseCell;
import nl.mad.toucanpdf.api.BaseFont;
import nl.mad.toucanpdf.api.BasePage;
import nl.mad.toucanpdf.api.BaseText;
import nl.mad.toucanpdf.model.DocumentPart;
import nl.mad.toucanpdf.model.Font;
import nl.mad.toucanpdf.model.FontFamilyType;
import nl.mad.toucanpdf.model.FontStyle;
import nl.mad.toucanpdf.model.Image;
import nl.mad.toucanpdf.model.ImageType;
import nl.mad.toucanpdf.model.Table;
import nl.mad.toucanpdf.model.state.StateParagraph;
import nl.mad.toucanpdf.model.state.StateTable;
import nl.mad.toucanpdf.model.state.StateText;
import nl.mad.toucanpdf.pdf.structure.PdfDocument;
import nl.mad.toucanpdf.pdf.syntax.PdfIndirectObject;
import nl.mad.toucanpdf.pdf.syntax.PdfObjectType;
import nl.mad.toucanpdf.pdf.syntax.PdfTable;
import nl.mad.toucanpdf.state.BaseStateImage;
import nl.mad.toucanpdf.state.BaseStateParagraph;
import nl.mad.toucanpdf.state.BaseStateTable;
import nl.mad.toucanpdf.state.BaseStateText;

import org.junit.Before;
import org.junit.Test;

public class PdfDocumentTest {
    private PdfDocument document;

    @Before
    public void setUp() throws Exception {
        document = new PdfDocument();
    }

    @Test
    public void testAddWithEmptyStream() {

    }

    @Test
    public void testAddPage() {
        assertEquals("There should not yet be a current page. ", null, document.getCurrentPage());
        document.addPage(new BasePage(200, 200));
        assertEquals("Page was added and set as current page. ", PdfObjectType.PAGE, document.getCurrentPage().getType());
    }
    
    @Test
    public void testAddMultipleParts() {
    	List<DocumentPart> fontList = new ArrayList<DocumentPart>();
    	Font f1 = new BaseFont(FontFamilyType.HELVETICA, FontStyle.BOLD);
    	fontList.add(f1);
    	Font f2 = new BaseFont(FontFamilyType.HELVETICA, FontStyle.ITALIC);
    	fontList.add(f2);
    	document.add(fontList);
    	assertTrue(document.getPdfFont(f1) != null);
    	assertTrue(document.getPdfFont(f2) != null);    	
    }
    
    @Test
    public void testAddFont() {
    	Font f1 = new BaseFont();
    	document.addFont(f1);
    	PdfIndirectObject ido = document.getPdfFont(f1);
    	assertTrue(ido != null);
    	PdfIndirectObject ido2 = document.addFont(f1);
    	assertTrue(ido.equals(ido2));
    }
    
    @Test
    public void testAddParts() {
    	new MockUp<PdfTable>() {
    		@Mock
    		public void $init(StateTable table) {
    			
    		}
    	};
    	document.addPage(new BasePage(100, 100));
    	StateText text = new BaseStateText("Test");
    	document.add(text);
    	assertEquals(1, document.getCurrentPage().getCurrentStream().getContentSize());
    	StateParagraph par = new BaseStateParagraph();
    	par.addText(new BaseStateText("Test"));
    	par.addText(new BaseStateText("Test"));
    	document.add(par);
    	assertEquals(3, document.getCurrentPage().getCurrentStream().getContentSize());
    	InputStream is = PdfDocumentTest.class.getResourceAsStream("/hammock.jpg");
    	Image i = new BaseStateImage(10, 10, is, ImageType.JPEG);
    	document.add(i);
    	assertEquals(4, document.getCurrentPage().getCurrentStream().getContentSize());
    	Table table = new BaseStateTable(100);
    	table.addCell(new BaseCell(i));
    	table.addCell(new BaseStateText("Test"));
    	document.add(table);
    	assertEquals(7, document.getCurrentPage().getCurrentStream().getContentSize());
    }

}
