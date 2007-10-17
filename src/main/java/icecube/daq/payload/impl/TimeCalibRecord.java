/*************************************************  120 columns wide   ************************************************

 Class:      TimeCalibRecord

 @author     Jim Braun, Dan Wharton
 @author     jbraun@amanda.wisc.edu, dwharton@icecube.wisc.edu

 ICECUBE Project
 University of Wisconsin - Madison

 **********************************************************************************************************************/

package icecube.daq.payload.impl;
//-dbw:this file was takend from V07-01-07 of testdaq-collector
// with consultation from Dima.
//-dbw: this is the old package
// package icecube.testdaq.datacollector;

/**
 * class TimeCalibRecord represents the data contained within a binary tcalib record from a DOMHubApp byte stream
 */
public interface TimeCalibRecord {
    /**
     * @return the transmit DOM timestamp
     */
    long getDomTXTime();

    /**
     * @return the receive DOM timestamp
     */
    long getDomRXTime();

    /**
     * @return the transmit DOR timestamp
     */
    long getDorTXTime();

    /**
     * @return the receive DOR timestamp
     */
    long getDorRXTime();

    /**
     * @return the waveform as measured by the DOM
     */
    int[] getDomWaveform();

    /**
     * @return the waveform as measured by the DOR card
     */
    int[] getDorWaveform();

    /**
     * @return DOM id
     */
    String getDomId();

    /**
     * @return  the count of seconds represented by the GPS UTC string
     */
    int getGpsSeconds();

    /**
     * @return byte indicating the quality of the 1 PPS signal from GPS
     */
    byte getGpsQualityByte();

    /**
     * @return the Dor count at the PGS time string - 1 count = 50 ns
     */
    long getDorGpsSyncTime();

}
