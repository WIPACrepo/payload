package icecube.daq.payload.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * DomHitDeltaCompressedFormatUtillity
 *
 * This record is meant to provide access to the Delta Compressed data from a DOM.
 * This includes the muxed format as well as the individual records containing
 * compressed data.
 *
 * This object is a container for the DomHit Delta Compressed Format Data which
 * is enveloped inside a Payload. This format is detailed in the document
 * by Josh Sopher and Dawn Williams: Version 1.0 Dec 12, 2005
 *
 * COMPRESSION HEADER:
 * FORMAT:                      bit-position            num-bits
 * WORD0:   Compr Flag          D31                     1
 *
 *              { 1 = compressed data, 0 = uncompressed data }
 *
 *          TriggerWord         D30..D18, D30=msb       13
 *
 *              { lower 13bits of the raw data Trigger Word }
 *
 *          LC                  D17..D16, D17=msb       2
 *              {
 *                  D[17..16]=01 LC tag came from below
 *                  D[17..16]=10 LC tag came from above
 *                  D[17..16]=11 LC tag came from below *and* above
 *              }
 *
 *          fADC Avail          D15                     1
 *
 *              { Used to calculate if 256 fADC words are recorded. If fADC data
 *                is not recorded, then ATWD data is also not recorded.
 *                1=true, 0=false. If false, ATWD Available will = 0
 *              }
 *
 *          ATWD Avail          D14                     1
 *
 *              { 1=true, 0=false }
 *
 *          ATWD Size           D13..D12, D13=msb       2
 *
 *              {
 *                  Used to calculate the number of 128 10-bit words recorded per channel.
 *                  D[13..12] = 00  ch0 only
 *                  D[13..12] = 01  ch0 and ch1
 *                  D[13..12] = 10  ch0, ch1, and ch2
 *                  D[13..12] = 11  ch0,ch1,ch2, and ch3
 *              }
 *
 *          ATWD_AB             D11                     1
 *
 *              { 0 = ATWD A, 1 = ATWD B }
 *
 *          Hit Size            D10..D0, D10=msb        11
 *
 *              { Used to tell when yo uget to the end of the hit data. }
 *
 * WORD1:   Time Stamp          D31..D0, D31=msb        32  (32 1.s. bits)
 *
 *              { lowest 32 bits of the 48 bit (full) Time stamp
 *                which rolls-over every 1.789 minutes
 *              }
 *
 * WORD2:   Peak Range          D31                     1
 *
 *              { 0 = Lower 9 bits, 1 = Higher 9 bits }
 *
 *          Peak Sample         D30..D27,D30=msb        4
 *
 *              { Sample number of the peak count. The first sample number is 0. }
 *
 *          PrePeak Count       D26..D18, D26=msb       9
 *
 *              { Count of the fADC output of the sample preciding the peak sample. }
 *
 *          Peak Count          D17..D9, D17=msb        9
 *
 *              { Count of the fADC output of the peak sample. }
 *
 *          PostPeak Count      D8..D0, D8=msb          9
 *
 *              { Count of the fADC output of the sample following the peak sample.
 *                If the peak does not occur within the range of 0 to 15 samples, the post-peak
 *                count will exceed the peak count.
 *              }
 *
 * WORD3 - WORDN: Compressed Data
 *          WORDN is given by the HitSize as described above
 *          Data is obtained from different data sources (fADC, ATWD channels), depending
 *          on the various flag values. For instance, if fADC Available = 0, only the header is
 *          recorded.
 *          Compressed Data is read out of memory in the following order:
 *          fADC is first
 *          ATWD Ch0 is next, followed by
 *               Ch1
 *               Ch2
 *               Ch3
 *
 * (note: here WORD0 is the first element in the DAQ record. This corresponds to WORD1 in the
 *        DOM record).
 *
 * NOTE: According to the pDAQ_trunk/StringHub/src/main/java/icecube/dat/domap/
 *       DataCollector.java code (revision 666) I am reverse engineering the code.
 * Prior to this code: (assuming BIG_ENDIAN?)
 *
 *    private void dataProcess(ByteBuffer in) throws IOException
 *  {
 *      // TODO - I created a number of less-than-elegant hacks to
 *      // keep performance at acceptable level such as minimal
 *      // decoding of hit records.  This should be cleaned up.
 *
 *      int buffer_limit = in.limit();
 *
 *      // create records from aggregrate message data returned from DOMApp
 *      while (in.remaining() > 0)
 *      {
 *          int pos = in.position();
 *          short len = in.getShort();
 *          short fmt = in.getShort();
 ************************************************************************************
 * NOTE: dbw-I am using the above definition of the position and endianness (BIG_ENDIAN)
 *       for these two fields.  This is all the documentation I have on this at this time
 *       along with the description of the format identifier data.
 ************************************************************************************
 *          if (hitsSink != null)
 *          {
 *              long domClock;
 *              switch (fmt)
 *      ...removed indent for clarity..
 *        case 144: // Delta compressed data
 *             // It gets weird here - FPGA data written LITTLE_ENDIAN
 *             // Also must handle unpacking and applying clock context
 *             // to delta hits compressed in data block starting here.
 *             in.order(ByteOrder.LITTLE_ENDIAN);
 *             int clkMSB = in.getShort();
 *             logger.debug("clkMSB: " + clkMSB);
 *             in.getShort();
 *             while (in.remaining() > 0)
 *             {
 *                 in.mark();
 *                 int hitSize = in.getInt() & 0x7ff;
 *                 int clkLSB = in.getInt();
 ************************************************************************************
 * NOTE: dbw-according to the formating provided by the DeltaCompression document,
 *       the above reconstruction of the clock (taking the endianess conversion)
 *       will not work.  If there is some other document which mitagates this handling
 *       of the data (incorrectly) - I will defer to it. However, until that time I will
 *       be working from the Spec in Docushare and email communications from John Jacobsen
 *       as to the format.
 ************************************************************************************
 *                 logger.debug("hitsize: " + hitSize + " clkLSB: " + clkLSB);
 *                 domClock = (((long) clkMSB) << 32) | (((long) clkLSB) & 0xffffffffL);
 *                 in.reset();
 *                 in.limit(in.position() + hitSize);
 *                 numHits++;
 *                 genericDataDispatch(hitSize, 3, domClock, in, hitsSink);
 *                 in.limit(buffer_limit);
 *             }
 *             in.order(ByteOrder.BIG_ENDIAN);
 *             break;
 *
 ************************************************************************************
 * The code above was authored by 'krokodil' (unsure who that is)
 ************************************************************************************
 * @author dwharton
 */
public class DomHitDeltaCompressedFormatUtility {

    /**
     * extractDomClock_MSB16
     * Extracts the 16 most significant bits of the dom clock from the header of a muxed
     * compression block sent to the domhub from a dom.
     *
     * @return long the lower 16bits are filled the the 16 most significant bits of the
     *              DOM clock.
     */
    public static final long extractDomClock_MSB16(int iMUXHeaderOffset, ByteBuffer tBuffer) throws IOException {
        //-exctract the current order to save
        ByteOrder tSaveOrder = tBuffer.order();
        //-change to LITTLE_ENDIAN for reading the MSB short
        tBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //-pull out the 16bits and kill any sign extension that may have come through
        // when casting to a long.
        long l_MSB16 = ((long) tBuffer.getShort(iMUXHeaderOffset) & 0x00000000FFFFFFFFL);
        //-restore the entry order
        tBuffer.order(tSaveOrder);
        return l_MSB16;
    }

    /**
     * extractTriggerAndLC_15bits
     * This function extracts the Trigger bits upper 13bits and the
     * LC bits (lower 2 bits)
     *
     *
     * @param iWORDOffset Offset of the word0 in the deltacompressed
     *                    record.
     * @param tBuffer
     *
     * @return int which is formated as followed
     *
     * WORD0:   Compr Flag          D31                     1
     *
     *              { 1 = compressed data, 0 = uncompressed data }
     *
     *          TriggerWord         D30..D18, D30=msb       13
     *
     *              { lower 13bits of the raw data Trigger Word }
     *
     *          LC                  D17..D16, D17=msb       2
     *              {
     *                  D[17..16]=01 LC tag came from below
     *                  D[17..16]=10 LC tag came from above
     *                  D[17..16]=11 LC tag came from below *and* above
     *              }
     *
     */
    public static final int extractTriggerAndLC_15bits(int iWORDOffset, ByteBuffer tBuffer) throws IOException {
        return (tBuffer.getShort(iWORDOffset) & 0x7FFF);
    }

    /**
     * Extracts the least significant 32 bits of the dom clock given the offset of
     * an individual delta compressed record.
     * @param iWORD0Offset offset in the Bytebuffer of WORD0 as received by
     * @param tBuffer ByteBuffer from which to construct the record.
     * @exception IOException if errors are detected reading the record
     *
     * @return the value of the DomClock stored in a long
     */
    public static final long extractDomClock_LSB32(int iWORD0Offset, ByteBuffer tBuffer) throws IOException {
        long ldomClock = 0L;
        //-save the byte order and pull out the data
        ByteOrder tSaveOrder = tBuffer.order();
        tBuffer.order(ByteOrder.BIG_ENDIAN);
        ldomClock = ( 0x00000000FFFFFFFFL & ((long) tBuffer.getInt(iWORD0Offset)));
        tBuffer.order(tSaveOrder);
        return ldomClock;
    }
}

