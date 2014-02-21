package nl.mad.pdflibrary.pdf.object;

import java.io.IOException;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import nl.mad.pdflibrary.pdf.utility.ByteEncoder;

/**
 * Represents a PDF String object. It contains a single string and is able to write this string to an OutputStream.
 * @author Dylan de Wolff
 */
public class PdfString extends AbstractPdfObject {
    private final static String DATE_PREFIX = "D:";
    private String string;

    /**
     * Creates a new instance of PdfString.
     */
    public PdfString() {
        super(PdfObjectType.STRING);
    }

    /**
     * Creates a new instance of PdfString.
     * @param string 
     */
    public PdfString(String string) {
        this();
        this.setString(string);
    }

    public PdfString(Calendar creationDate) {
        this();
        this.setString(creationDate);
    }

    @Override
    public void writeToFile(OutputStream os) throws IOException {
        os.write('(');
        super.writeToFile(os);
        os.write(')');
    }

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
        this.setByteRepresentation(ByteEncoder.getBytes(string));
    }

    /**
     * Creates a PDF string from the given date.
     * @param date 
     */
    public void setString(Calendar date) {
        String stringDate = DATE_PREFIX;
        DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        stringDate += dateFormat.format(date.getTime());
        this.setString(stringDate);
    }
}
