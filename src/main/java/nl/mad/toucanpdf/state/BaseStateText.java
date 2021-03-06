package nl.mad.toucanpdf.state;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import nl.mad.toucanpdf.model.DocumentPart;
import nl.mad.toucanpdf.model.FontMetrics;
import nl.mad.toucanpdf.model.Page;
import nl.mad.toucanpdf.model.Position;
import nl.mad.toucanpdf.model.Text;
import nl.mad.toucanpdf.model.state.StatePage;
import nl.mad.toucanpdf.model.state.StateText;
import nl.mad.toucanpdf.utility.FloatEqualityTester;

/**
 * Base implementation of the StateText interface. This class offers the same functionality as the BaseText class.
 * On top of that this class offers methods for calculating the position, determining new lines, handling overflow and processing cutoff.
 * 
 * @author Dylan de Wolff
 */
public class BaseStateText extends AbstractStateText implements StateText {
    private DocumentPart originalObject;

    /**
     * Creates a new instance of BaseStateText.
     */
    BaseStateText() {
        this("");
    }

    /**
     * Creates a new instance of BaseStateText.
     * @param text Text this object will represent.
     */
    public BaseStateText(String text) {
        super(text);
    }

    /**
     * Creates a copy of the given text object.
     * @param part Text to copy from.
     */
    public BaseStateText(Text part) {
        super(part);
    }

    @Override
    public StateText processContentSize(StatePage page, double positionX, boolean fixedPosition) {
        ArrayList<String> strings = new ArrayList<String>(Arrays.asList(getText().split(" ")));
        double leading = page.getLeading() + getRequiredSpaceBelowLine();
        this.textSplit = new LinkedHashMap<Position, String>();
        int i = 0;
        boolean stringsProcessed = false;
        Position pos = new Position(this.getPosition());
        StateText overflowText = null;
        if(positionX == 0) {
        	positionX = this.getRequiredSpaceLeft();
        }

        while (!stringsProcessed && i < strings.size()) {
            List<int[]> openSpaces = getOpenSpaces(pos, page, fixedPosition);
            if(openSpaces.size() != 0) {
	            i += splitText(openSpaces, strings.subList(i, strings.size()), pos, page);
	            boolean isLast = (i == (strings.size() - 1));
	            pos = handleTextAddition(page, leading, pos, positionX, fixedPosition, isLast);
	            if (pos == null) {
	                overflowText = handleOverflow(i, strings);
	                stringsProcessed = true;
	            }
            } else {
            	pos.adjustY(-leading);
            }
        }
        return overflowText;
    }

    /**
     * Returns the open spaces on the given position and page.
     * @param pos Position to check.
     * @param page Page the text will be on.
     * @param fixedPosition Whether the text has a fixed position.
     * @return List of int arrays. Each int array contains two values specifying the x-value of the start and end of a single open space.
     */
    private List<int[]> getOpenSpaces(Position pos, StatePage page, boolean fixedPosition) {
        List<int[]> openSpaces;
        if (!fixedPosition) {
            openSpaces = page.getOpenSpacesOn(pos, true, getRequiredSpaceAboveLine(), getRequiredSpaceBelowLine(), this);
        } else {
            openSpaces = new ArrayList<int[]>();
            openSpaces.add(new int[] { (int) pos.getX(), (int) (page.getWidth() - page.getMarginRight() - getRequiredSpaceRight()) });
        }
        return openSpaces;
    }

    /**
     * Splits the given text over the given open spaces.
     * @param openSpaces Open spaces that can be filled.
     * @param strings Strings to fill the open spaces with.
     * @param pos Position of the text.
     * @param page The page to add the text to.
     * @return index determining how many strings have been processed.
     */
    private int splitText(List<int[]> openSpaces, List<String> strings, Position pos, Page page) {
        List<String> stringsCopy = new LinkedList<String>(strings);
        stringsCopy.add("");
        StringBuilder currentLine = new StringBuilder();
        int[] openSpace = openSpaces.get(0);
        int i = 0;
        int lastAdditionIndex = 0;
        int additions = 0;
        int cutOffAdditions = 0;
        int textSize = getTextSize();
        double width = 0;
        boolean openSpacesFilled = false;
        FontMetrics metrics = getFont().getMetrics();
        int openSpaceIndex = 0;

        while (!openSpacesFilled && i < stringsCopy.size()) {
            String s = stringsCopy.get(i);
            double oldWidth = width;
            if (i != (stringsCopy.size() - 1)) {
                width += metrics.getWidthPointOfString(s, textSize, true) + (metrics.getWidthPoint("space") * textSize);
            }
            if ((width > openSpace[1] - openSpace[0] || i == (stringsCopy.size() - 1)) && oldWidth < openSpace[1] - openSpace[0]) {
                String cutOffLine = processCutOff(openSpace[0] + oldWidth, openSpace[1], currentLine.toString(), stringsCopy, i, page);
                if (!cutOffLine.equals(currentLine.toString())) {
                    strings.add(i + 1, stringsCopy.get(i + 1));
                    ++cutOffAdditions;
                    currentLine = new StringBuilder(cutOffLine);
                }
                if (!currentLine.toString().isEmpty()) {
                    lastAdditionIndex = i;
                    ++additions;
                    Position position = new Position(openSpace[0], pos.getY());
                    width = metrics.getWidthPointOfString(currentLine.toString(), textSize, true) + (metrics.getWidthPoint("space") * textSize);
                    position = processAlignment(currentLine.toString(), position, width, openSpace[1] - openSpace[0]);
                    addTextSplitEntry(position, currentLine.toString());
                    currentLine = new StringBuilder();
                }
                if (openSpaceIndex != (openSpaces.size() - 1)) {
                    openSpace = openSpaces.get(openSpaceIndex + 1);
                    width = metrics.getWidthPointOfString(stringsCopy.get(i), textSize, true) - (metrics.getWidthPoint("space") * textSize);
                    ++openSpaceIndex;
                } else {
                    openSpacesFilled = true;
                }
            }
            currentLine.append(stringsCopy.get(i));
            currentLine.append(' ');
            ++i;
        }
        //if we only do a single loop we should still skip the current text if it was added.
        if (lastAdditionIndex == 0 && additions > 0) {
            ++lastAdditionIndex;
        }
        return lastAdditionIndex + cutOffAdditions;
    }

    /**
     * Adds a new line to the textSplit map.
     * @param position Position the line is on.
     * @param string The line itself.
     */
    private void addTextSplitEntry(Position position, String string) {
        if (textSplit.isEmpty()) {
            this.setPosition(new Position(position.getX(), position.getY()));
        }
        textSplit.put(position, string);
    }

    /**
     * Processes text cutoff. Determines whether or not cutting off text is required and does so if it is.
     * @param width Width of the current line.
     * @param widthLimit Width of the open space being filled.
     * @param currentLine The current line being processed.
     * @param text The text list being processed.
     * @param currentTextIndex The current index being processed of the text list.
     * @param page The page the text will be added to.
     * @return
     */
    private String processCutOff(double width, int widthLimit, String currentLine, List<String> text, int currentTextIndex,
            Page page) {
        if (FloatEqualityTester.greaterThan((widthLimit - width), ((page.getWidth() - page.getMarginRight()) * (1.0 - Page.CUT_OFF_POINT_PERCENTAGE)))) {
            int textSize = getTextSize();
            double currentWidth = width;
            StringBuilder currentString = new StringBuilder(currentLine);
            FontMetrics metrics = getFont().getMetrics();
            char[] charArray = text.get(currentTextIndex).toCharArray();
            int i = 0;
            double dashWidth = metrics.getWidthPoint("endash") * textSize;
            while (currentWidth < (widthLimit - dashWidth) && i != charArray.length) {
                char c = charArray[i];
                double characterSize = 0.0;
                if (i + 1 != charArray.length) {
                    characterSize = (((metrics.getWidth(c) - metrics.getKerning(c, charArray[i + 1])) * textSize) * metrics.getConversionToPointsValue());
                } else {
                    characterSize = metrics.getWidthPoint(c) * textSize;
                }
                currentWidth += characterSize;
                if (currentWidth < widthLimit - dashWidth) {
                    currentString.append(c);
                }
                ++i;
            }
            if (i != 0) {
                currentString.append('-');
                text.set(currentTextIndex, "");
                String stringToAdd = String.valueOf(charArray).substring(i - 1);
                if (text.size() > currentTextIndex + 1) {
                    text.set(currentTextIndex + 1, stringToAdd);
                } else {
                    text.add(stringToAdd);
                }
            }
            return currentString.toString();
        }
        return currentLine;
    }

    /**
     * Processes the addition of text by adjusting the filled height of the page and calculating a new position for the upcoming text.
     * @param page Page the text has been added to.
     * @param leading Leading between each line.
     * @param pos Position the text has been added on.
     * @param positionX Default X offset of this text.
     * @param fixedPosition Whether the text has a fixed position.
     * @return
     */
    private Position handleTextAddition(StatePage page, double leading, Position pos, double positionX, boolean fixedPosition, boolean isLast) {
        Position newPos;
        if (!fixedPosition) {
            newPos = page.getOpenPosition(positionX, pos.getY() - leading - this.getRequiredSpaceAboveLine(), this.getRequiredSpaceAboveLine(),
                    this.getRequiredSpaceBelowLine(), this, 0);
            double heightDifference = (page.getHeight() - page.getFilledHeight()) - pos.getY();
            double spaceBelow = this.getRequiredSpaceBelowLine();
            if (isLast) {
                spaceBelow = this.getRequiredSpaceBelow();
            }
            page.setFilledHeight(page.getFilledHeight() + heightDifference + page.getLeading() + spaceBelow);

        } else {
            newPos = new Position(positionX, pos.getY() - leading - this.getRequiredSpaceAboveLine());
        }
        return newPos;
    }

    /**
     * Processes the overflow by removing all overflowing content and adding it to another text object.
     * @param overflowStart Index determining which string caused the overflow.
     * @param strings The text split into a list of strings.
     * @return a new StateText object that contains all the overflowing content of this object.
     */
    private StateText handleOverflow(int overflowStart, List<String> strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : textSplit.values()) {
            if (!"\n".equals(s)) {
                sb.append(s);
                sb.append(" ");
            }
        }
        this.text(sb.toString());

        sb = new StringBuilder();
        for (int i = overflowStart; i < strings.size(); ++i) {
            sb.append(strings.get(i));
            sb.append(" ");
        }
        StateText overflowText = new BaseStateText(this);
        overflowText.setOriginalObject(this.getOriginalObject());
        overflowText.text(sb.toString()).on(new Position());
        return overflowText;
    }

    @Override
    public double getContentHeight(Page page) {
        double lowestHeight = this.getPosition().getY();
        double highestHeight = 0;
        for (Entry<Position, String> entry : textSplit.entrySet()) {
            Double posY = entry.getKey().getY();
            highestHeight = Math.max(highestHeight, posY);
            lowestHeight = Math.min(lowestHeight, posY);
        }
        return highestHeight - lowestHeight + this.getRequiredSpaceAboveLine() + this.getRequiredSpaceBelowLine();
    }

    @Override
    public double getContentHeightUnderBaseLine(Page page) {
        return this.getContentHeight(page) - getRequiredSpaceAboveLine();
    }

    @Override
    public double getContentWidth(Page page, Position position) {
        FontMetrics metrics = getFont().getMetrics();
        List<Entry<Position, String>> entries = this.getEntriesAtHeight(position.getY());
        int width = 0;
        for (Entry<Position, String> entry : entries) {
            width += (int) (metrics.getWidthPointOfString(entry.getValue(), getTextSize(), true) + (metrics.getWidthPoint("space") * getTextSize()));
        }
        return width;
    }

    @Override
    public int[] getPositionAt(double height) {
        List<Entry<Position, String>> entries = this.getEntriesAtHeight(height);
        int[] positions = new int[0];
        if (!entries.isEmpty()) {
            positions = new int[entries.size()];
            for (int i = 0; i < entries.size(); ++i) {
                positions[i] = (int) entries.get(i).getKey().getX();
            }
        }
        return positions;
    }

    /**
     * Returns all entries from the textSplit map that are positioned at the given height.
     * @param height Height to check.
     * @return List of entries that are positioned at the given height.
     */
    private List<Entry<Position, String>> getEntriesAtHeight(double height) {
        List<Entry<Position, String>> entries = new LinkedList<Entry<Position, String>>();
        int i = 0;
        for (Entry<Position, String> entry : textSplit.entrySet()) {
            Position linePos = entry.getKey();
            double requiredSpaceAbove = this.getRequiredSpaceAboveLine();
            double requiredSpaceBelow = this.getRequiredSpaceBelowLine();
            if (i == 0) {
                requiredSpaceAbove = this.getRequiredSpaceAbove();
            }
            if (i == (textSplit.size() - 1)) {
                requiredSpaceBelow = this.getRequiredSpaceBelow();
            }
            if (FloatEqualityTester.lessThanOrEqualTo(height, linePos.getY() + requiredSpaceAbove)
                    && FloatEqualityTester.greaterThanOrEqualTo(height, linePos.getY() - requiredSpaceBelow)) {
                entries.add(entry);
            }
            ++i;
        }
        return entries;
    }

    @Override
    public List<int[]> getUsedSpaces(double height, int pageWidth) {
        List<int[]> spaces = new LinkedList<int[]>();
        List<Entry<Position, String>> entries = getEntriesAtHeight(height);
        FontMetrics metrics = getFont().getMetrics();
        for (int i = 0; i < entries.size(); ++i) {
            Entry<Position, String> entry = entries.get(i);
            double stringWidth = metrics.getWidthPointOfString(entry.getValue(), getTextSize(), true);
            if ((entry.equals(getFirstTextSplitEntry()) && this.marginTop > 0) || (entry.equals(getLastTextSplitEntry()) && this.getMarginBottom() > 0)) {
                spaces.add(new int[] { (int) 0, pageWidth });
            } else {
                spaces.add(new int[] { (int) entry.getKey().getX(), (int) (entry.getKey().getX() + stringWidth) });
            }
        }
        return spaces;
    }

    private Entry<Position, String> getFirstTextSplitEntry() {
        return textSplit.entrySet().iterator().next();
    }

    private Entry<Position, String> getLastTextSplitEntry() {
        Iterator<Entry<Position, String>> iterator = textSplit.entrySet().iterator();
        Entry<Position, String> lastEntry = null;
        while (iterator.hasNext()) {
            lastEntry = iterator.next();
        }
        return lastEntry;
    }

    @Override
    public void setOriginalObject(DocumentPart originalObject) {
        if (this.originalObject == null) {
            this.originalObject = originalObject;
        }
    }

    @Override
    public DocumentPart getOriginalObject() {
        return this.originalObject;
    }
}
