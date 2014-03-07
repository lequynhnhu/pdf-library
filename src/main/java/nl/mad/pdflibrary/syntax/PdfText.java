package nl.mad.pdflibrary.syntax;

import java.util.ArrayList;
import java.util.List;

import nl.mad.pdflibrary.model.Font;
import nl.mad.pdflibrary.model.FontMetrics;
import nl.mad.pdflibrary.model.Text;
import nl.mad.pdflibrary.utility.ByteEncoder;

/**
 * PdfText stores the PDF stream version of a Text object. 
 * @author Dylan de Wolff
 * @see PdfStream
 * @see nl.mad.pdflibrary.model.Text
 */
public class PdfText extends AbstractPdfObject {
    private int positionX;
    private boolean inParagraph;

    /**
     * Creates a new instance of PdfText.
     */
    public PdfText() {
        super(PdfObjectType.TEXT);
        inParagraph = false;
    }

    /**
     * Creates a new instance of PdfText. Use this constructor for text objects that are in a paragraph.
     * @param positionX Starting point of the paragraph.
     */
    public PdfText(int positionX) {
        super(PdfObjectType.TEXT);
        this.positionX = positionX;
        inParagraph = true;
    }

    /**
     * Adds the given Text object to the stream.
     * @param text Text object to be added.
     * @param fontReference font for the text
     */
    public String addText(Text text, PdfIndirectObject fontReference, PdfPage page, int leading, boolean ignorePosition) {
        this.addFont(fontReference, text.getTextSize());
        this.addMatrix(text);
        return this.addTextString(text, page, leading, ignorePosition);
    }

    /**
     * Converts the given position values to a text matrix and adds this to the byte representation.
     * This should be done before adding the text.
     * @param text text to add to the document
     */
    public void addMatrix(Text text) {
        String byteRep = text.getScaleX() + " " + text.getShearX() + " " + text.getShearY() + " " + text.getScaleY() + " " + text.getPositionX() + " "
                + text.getPositionY() + " Tm\n";
        this.addToByteRepresentation(ByteEncoder.getBytes(byteRep));
    }

    //TODO: How to avoid hardcoding these strings?
    /**
     * Adds the byte representation for the given font and font size.
     * This should be done before adding the text.
     * @param font IndirectObject containing the font.
     * @param fontSize Size of the font.
     */
    public void addFont(PdfIndirectObject font, int fontSize) {
        String byteRep = "/" + font.getReference().getResourceReference() + " " + fontSize + " Tf\n";
        this.addToByteRepresentation(ByteEncoder.getBytes(byteRep));
    }

    /**
     * Adds the byte representation for the given text string.
     * @param text String that is to be added.
     */
    public String addTextString(Text text, PdfPage page, int leading, boolean ignorePosition) {
        List<String> splitStrings = processNewLines(text, page, leading, ignorePosition);
        StringBuilder sb = new StringBuilder();
        for (String s : splitStrings) {
            sb.append(s);
            sb.append("\n");
        }
        this.addToByteRepresentation(ByteEncoder.getBytes(sb.toString()));
        return "";
    }

    private List<String> processNewLines(Text text, PdfPage page, int leading, boolean ignorePosition) {
        String textString = text.getText();
        int textSize = text.getTextSize();
        Font font = text.getFont();
        String[] strings = textString.split(" ");

        StringBuilder currentLine = new StringBuilder();
        ArrayList<String> processedStrings = new ArrayList<String>();

        double width = page.getFilledWidth();
        //if we aren't adding the given text object behind another text object
        if (!ignorePosition) {
            width += text.getPositionX();
        }

        FontMetrics metrics = font.getBaseFont().getMetricsForStyle(font.getStyle());
        for (int i = 0; i < strings.length; ++i) {
            //add the width of the string
            //TODO: Fix retrieving space width by unicode
            width += metrics.getWidthPointOfString(strings[i], textSize, true) + metrics.getWidthPoint("space");
            if (width > page.getWidth()) {
                currentLine = new StringBuilder(this.processKerning(currentLine.toString(), font, leading));
                currentLine.append(" 0 " + -leading + " TD");
                processedStrings.add(currentLine.toString());
                page.setFilledHeight(page.getFilledHeight() + leading);
                currentLine = new StringBuilder();
                width = positionX;
            }
            //add the string and a space to the current line
            currentLine.append(strings[i]);
            currentLine.append(' ');
            //if we are at the last string
            if ((i + 1) == strings.length) {
                currentLine = new StringBuilder(this.processKerning(currentLine.toString(), font, leading));
                processedStrings.add(currentLine.toString());
            }
        }

        page.setFilledHeight(page.getFilledHeight() + leading);
        if (inParagraph) {
            page.setFilledWidth(width);
        } else {
            page.setFilledWidth(0);
        }

        return processedStrings;
    }

    private String processKerning(String text, Font font, int leading) {
        FontMetrics metrics = font.getBaseFont().getMetricsForStyle(font.getStyle());
        StringBuilder sb = new StringBuilder("[(");
        for (int i = 0; i < text.length(); ++i) {
            sb.append(text.charAt(i));
            if (text.length() != i + 1) {
                int kernWidth = metrics.getKerning((int) text.charAt(i), (int) text.charAt(i + 1));
                if (kernWidth != 0) {
                    sb.append(") ");
                    sb.append(kernWidth);
                    sb.append(" (");
                }
            }
        }
        sb.append(")] TJ");
        return sb.toString();
    }

    public int getPositionX() {
        return this.positionX;
    }
}
