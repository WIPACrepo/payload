/*
 * class: IHitDataPayload
 *
 *
 */

package icecube.daq.trigger;

import java.util.zip.DataFormatException;

/**
 * Interface of a payload describing a single hit and its associated engineering format
 * data.
 *
 * @author dwharton
 */
public interface IHitDataPayload extends IHitPayload {
    /**
     * Get access to the underlying data for an engineering hit
     */
    IHitDataRecord getHitRecord() throws DataFormatException;
}
