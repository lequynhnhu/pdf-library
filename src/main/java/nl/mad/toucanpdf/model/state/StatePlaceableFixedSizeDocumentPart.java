package nl.mad.toucanpdf.model.state;

/**
 * This interface specifies the methods all fixed size document parts in the preview state should implement.
 * @author Dylan de Wolff
 *
 */
public interface StatePlaceableFixedSizeDocumentPart extends StatePlaceableDocumentPart {

    /**
     * Processes the size of the part and positions it accordingly.
     * @param page The page to add the part to.
     */
    boolean processContentSize(StatePage page);

    /**
     * Processes the size of the part and positions it accordingly.
     * @param page The page to add the part to.
     * @param wrapping Whether wrapping around the part should be allowed.
     * @param processAlignment Whether alignment should be applied.
     */
    boolean processContentSize(StatePage page, boolean wrapping, boolean processAlignment);

}
