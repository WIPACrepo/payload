package icecube.daq.trigger;
/*
 * class: IReadoutSPRequestElement
 *
 * Version $Id: IReadoutRequestElement.java,v 1.3 2005/01/25 18:11:25 dwharton Exp $
 *
 * Date: November 5 2004
 *
 * (c) 2004 IceCube Collaboration
 */

import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.ISourceID;
import icecube.daq.payload.IDOMID;

/**
 * This interface describes a single readout element.
 * It can be either a single Module, a whole String
 * or the whole Detector in a timeframe from t1 to t2.
 * NOTE: This is integral to the trigger system so it has been
 *       moved to the trigger package.
 *
 * @version $Id: IReadoutRequestElement.java,v 1.3 2005/01/25 18:11:25 dwharton Exp $
 * @author hellwig,dwharton
 */
public interface IReadoutRequestElement {
    /**
     * Definition of Readout Types
     */
    int READOUT_TYPE_GLOBAL = 0; // Readout of both all InIce and all IceTop
    int READOUT_TYPE_IT_GLOBAL = READOUT_TYPE_GLOBAL + 1; //Readout of all IceTop
    int READOUT_TYPE_II_GLOBAL = READOUT_TYPE_IT_GLOBAL + 1;             // Readout of InIce
    // -- NOTE: if StringProcessor gets a GLOBAL request it will read out the whole string as if if where a string request
    int READOUT_TYPE_II_STRING = READOUT_TYPE_II_GLOBAL + 1; // Readout of complete String
    int READOUT_TYPE_II_MODULE = READOUT_TYPE_II_STRING + 1; // Readout of a single Module for InIce
    int READOUT_TYPE_IT_MODULE = READOUT_TYPE_II_MODULE + 1; // Readout of a single Module for IceTop



    /**
     * getReadoutType()
     * @return int Type of Readout
     */
    int getReadoutType();

    /**
     * getDomID()
     * @return IDOMID ...identifies module to readout.
     *                   IDOMID object if request is for single DOM
     *                   null if request is not specific to a single DOM.
     */
    IDOMID getDomID();

    /**
     * getSourceID()
     * @return ISourceID ....the component from which to get data (typically a StringProcessor)
     */
    ISourceID getSourceID();

    /**
     * returns start time of interval
     */
    IUTCTime getFirstTimeUTC();

    /**
     * returns end time of interval
     */
    IUTCTime getLastTimeUTC();

}
