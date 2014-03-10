package nl.mad.pdflibrary.api;

import nl.mad.pdflibrary.model.DocumentPartType;
import nl.mad.pdflibrary.model.PlaceableDocumentPart;

/**
 * AbstractPlaceableDocumentPart is an extension of AbstractDocumentPart that allows for positioning of the object.
 * 
 * @author Dylan de Wolff
 * @see AbstractDocumentPart
 */
public abstract class AbstractPlaceableDocumentPart extends AbstractDocumentPart implements PlaceableDocumentPart {
    private Integer positionX;
    private Integer positionY;
    /**
     * Used to determine if the user has specified the positioning of the object.
     */
    private boolean customPositioning = true;

    /**
     * Creates a new instance of AbstractPlaceableDocumentPart.
     * @param type Type of document part.
     */
    public AbstractPlaceableDocumentPart(DocumentPartType type) {
        super(type);
    }

    public int getPositionX() {
        return positionX;
    }

    @Override
    public final void setPositionX(int positionX) {
        this.positionX = positionX;
        this.setCustomPositioning(true);
    }

    public int getPositionY() {
        return positionY;
    }

    @Override
    public final void setPositionY(int positionY) {
        this.positionY = positionY;
        this.setCustomPositioning(true);
    }

    protected final void setCustomPositioning(boolean customPositioning) {
        this.customPositioning = customPositioning;
    }

    public boolean getCustomPositioning() {
        return this.customPositioning;
    }
}