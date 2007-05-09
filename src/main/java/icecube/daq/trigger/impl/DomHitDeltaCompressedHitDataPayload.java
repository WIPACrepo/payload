package icecube.daq.trigger.impl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.zip.DataFormatException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import icecube.daq.payload.IDOMID;
import icecube.daq.payload.IUTCTime;
import icecube.daq.payload.PayloadDestination;
import icecube.daq.payload.PayloadRegistry;
import icecube.daq.payload.impl.UTCTime8B;
import icecube.daq.payload.splicer.Payload;
import icecube.daq.splicer.Spliceable;
import icecube.daq.trigger.impl.DOMID8B;
import icecube.util.Poolable;

/**
 * This object represents a Delta Compressed Hit from a DOM and includes
 * the waveform data. It carries both the header information and the data 
 * from the dom and is constructed after the data comes to the surface in the hub. 
 * The mux header is included in the header of each payload because they are 
 * seperated by time and not by dom-id after reaching the surface and being 
 * concentrated and sorted.
 *
 * FORMAT
 *   PayloadEnvelope (16)
 *   DomHitDeltaCompressedFormatRecord (22)
 *   WORD3 - WORDN (variable length) waveform data
 *
 * @author dwharton
 */

