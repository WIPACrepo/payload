package icecube.daq.payload;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.IdentityHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This object creates and caches ByteBuffers. This allocates
 * or re-uses from a cache, ByteBuffers of a fixed granularity
 * of size, which are equal to or greater than the size requested
 * by the user of this object.
 *
 * @author dwharton
 */
public class ByteBufferCache implements IByteBufferCache, ByteBufferCacheMBean {

    private static Log mtLog = LogFactory.getLog(ByteBufferCache.class);
    public static final int DEFAULT_GRANULARITY = 128;
    public static final long DEFAULT_MAX_BYTES = (256L * 1024L); //-256K by default
    public static final long DEFAULT_HISTOGRAM_LOG_TIME_INTERVAL =  1000L * 60L; //- once every 60 seconds

    protected long mlMAX_CACHE_BYTES;
    protected long mlMAX_ACQUIRE_BYTES;
    protected boolean mbCACHE_BOUNDED;
    protected boolean mbAQUIRE_BOUNDED;
    private boolean mbOutputWarningOfMaxCachedBytesOnceFlag;
    private static final int DENIED_ACQUIRE_REPORTING_MODULO = 100;
    private static final int NUM_DENIED_ACQUIRES_BEFORE_MODULO = 20;
    private int miNumAcquireDenied;
    private boolean mbLogHistogram = true;
    private long mlTimeOfLastHistogramLog = System.currentTimeMillis();
    private long mlHistogramLogTimeInterval = DEFAULT_HISTOGRAM_LOG_TIME_INTERVAL;

    protected Object mtStatisticsLock = new Object();
    /**
     * The total number of buffers created by the cache
     */
    protected int miTotalBuffersCreated;
    /**
     * The total bytes currently cached.
     */
    protected long mlTotalBytesInCache;
    /**
     * The total number of buffers that have been acquired.
     */
    protected int miTotalBuffersAcquired;
    /**
     * The total number of buffers returned to the cache.
     */
    protected int miTotalBuffersReturned;
    /**
     * The number of buffers which have been acquired
     * but have not yet been returned.
     */
    protected int miCurrentAquiredBuffers;
    /**
     * The current number of bytes which are contained in the buffers
     * which have not yet been returned.
     */
    protected long mlCurrentAquiredBytes;

    /**
     * The main table of elements for the cache in the cache.
     * each element corresponds to an ArrayList of ByteBuffer's of
     * a specific granular size. The key is the Integer(size) object
     * which is used to find a potential list of a specified size.
     */
    protected Hashtable mtGranularBufferTables = new Hashtable();

    /**
     * The histogram that holds the information about each of the
     * elements in the cache.
     */
    private CacheHistogram mtCacheHistogram = new CacheHistogram();
    /**
     * The basic unit of size for computing sizes of cached ByteBuffers.
     * all ByteBuffers which are produced by this cache have capacities which
     * are even multiples of this number.
     */
    protected int miGranularity = DEFAULT_GRANULARITY;

    /**
     * Set's the name of this particular ByteBufferCache
     */
    protected String msCacheName = "";

    // returnBuffer() debugging counters
    private int returnEntry;
    private int returnCount;
    private int returnTime;

    /**
     * Constructor.
     *
     * @param iGranularity         the size unit of which the resulting buffer will be.
     *                             this is checked that is greater than or equal to the default value
     *                             which is statically defined above.
     * @param lMaxNumCachedBytes   long max # of bytes to keep in the cache at any one time
     *                             any buffers which are returned to the cache which would exceed this number
     *                             are ignored. A value of -1 indicates UNBOUNDED for this instance.
     * @param lMaxNumAcquiredBytes long  value which limits the amount of bytes which can be
     *                             Acquired by the byte buffer cache at any one time. A value of -1 indicates
     *                             unbounded.
     */
    public ByteBufferCache(int iGranularity,
                           long lMaxNumCachedBytes,
                           long lMaxNumAcquiredBytes) {
        this(iGranularity, lMaxNumCachedBytes, lMaxNumAcquiredBytes, "");
    }
    /**
     * Constructor.
     *
     * @param iGranularity         the size unit of which the resulting buffer will be.
     *                             this is checked that is greater than or equal to the default value
     *                             which is statically defined above.
     * @param lMaxNumCachedBytes   long max # of bytes to keep in the cache at any one time
     *                             any buffers which are returned to the cache which would exceed this number
     *                             are ignored. A value of -1 indicates UNBOUNDED for this instance.
     * @param lMaxNumAcquiredBytes long  value which limits the amount of bytes which can be
     *                             Acquired by the byte buffer cache at any one time. A value of -1 indicates
     *                             unbounded.
     * @param sCacheName           The name of this cache so it can
     *                             be properly identified in the
     *                             log.
     *
     */
    public ByteBufferCache(int iGranularity,
                           long lMaxNumCachedBytes,
                           long lMaxNumAcquiredBytes,
                           String sCacheName) {
        if (sCacheName != null) {
            setCacheName(sCacheName);
        }
        if (iGranularity >= DEFAULT_GRANULARITY) {
            miGranularity = iGranularity;
        }
        //-Check to see if the cache should have an upper-bound as far
        // as the max number of bytes which should be stored in the cache.
        if (lMaxNumCachedBytes > 0L) {
            mbCACHE_BOUNDED = true;
            mlMAX_CACHE_BYTES = lMaxNumCachedBytes;
        }
        if (lMaxNumAcquiredBytes > 0L) {
            mbAQUIRE_BOUNDED = true;
            mlMAX_ACQUIRE_BYTES = lMaxNumAcquiredBytes;
        }
    }

    /**
     * Constructor.
     *
     * @param iGranularity the size unit of which the resulting buffer will be.
     *                     this is checked that is greater than or equal to the default value
     *                     which is statically defined above.
     * @param long         lMaxNumCachedBytes the bound for the max num bytes to cache,
     *                     if this is -1 then the cache is unbounded.
     * @param sCacheName   name of the cache so it's logging can be
     *                     readily identified.
     */
    public ByteBufferCache(int iGranularity, long lMaxNumCachedBytes, String sCacheName) {
        this(iGranularity, lMaxNumCachedBytes);
        if (sCacheName != null) {
            setCacheName(sCacheName);
        }
    }
    /**
     * Constructor.
     *
     * @param iGranularity the size unit of which the resulting buffer will be.
     *                     this is checked that is greater than or equal to the default value
     *                     which is statically defined above.
     * @param long         lMaxNumCachedBytes the bound for the max num bytes to cache,
     *                     if this is -1 then the cache is unbounded.
     */
    public ByteBufferCache(int iGranularity, long lMaxNumCachedBytes) {
        if (iGranularity >= DEFAULT_GRANULARITY) {
            miGranularity = iGranularity;
        }
        if (lMaxNumCachedBytes > 0L) {
            mlMAX_CACHE_BYTES = lMaxNumCachedBytes;
            mbCACHE_BOUNDED = true;
        }
    }

    /**
     * Constructor.
     *
     * @param iGranularity the size unit of which the resulting buffer will be.
     *                     this is checked that is greater than or equal to the default value
     *                     which is statically defined above.
     */
    public ByteBufferCache(int iGranularity) {
        if (iGranularity >= DEFAULT_GRANULARITY) {
            miGranularity = iGranularity;
        }
    }
    /**
     * Constructor.
     *
     * @param iGranularity the size unit of which the resulting buffer will be.
     *                     this is checked that is greater than or equal to the default value
     *                     which is statically defined above.
     */
    public ByteBufferCache(int iGranularity, String sCacheName) {
        this(iGranularity);
        if (sCacheName != null) {
            setCacheName(sCacheName);
        }
    }

    /**
     * Set's the name of the cache to identifiy it.
     * @param sName
     */
    public void setCacheName(String sName) {
        msCacheName = sName;
    }
    /**
     * Set's the name of the cache to identifiy it.
     * @param sName
     */
    public String getCacheName() {
        return msCacheName;
    }

    /**
     * Simple method to summarize the statistics of the ByteBufferCache.
     *
     * @return String which contains the summary of the stats of the cache.
     */
    public String toString() {
        StringBuffer sbSummary = new StringBuffer();
        sbSummary.append("\n");
        sbSummary.append("ByteBufferCache:"+getCacheName()+"- Summary start -\n");
        sbSummary.append("granularity(bytes)=" + miGranularity + ", \n");
        sbSummary.append("getMaxNumBytesToCache()=" + getMaxNumBytesToCache() + ", \n");
        sbSummary.append("getMaxAquiredBytes()=" + getMaxAquiredBytes() + ", \n");
        sbSummary.append("getIsCacheBounded()=" + getIsCacheBounded() + ", \n");
        sbSummary.append("getCurrentAquiredBuffers()=" + getCurrentAquiredBuffers() + ", \n");
        sbSummary.append("getCurrentAquiredBytes()=" + getCurrentAquiredBytes() + ", \n");
        sbSummary.append("getTotalBuffersCreated()=" + getTotalBuffersCreated() + ", \n");
        sbSummary.append("getTotalBytesInCache()=" + getTotalBytesInCache() + ", \n");
        sbSummary.append("getTotalBuffersAcquired()=" + getTotalBuffersAcquired() + ", \n");
        sbSummary.append("getTotalBuffersReturned()=" + getTotalBuffersReturned() + "  \n");
        sbSummary.append("CacheHistogram (start)\n"+mtCacheHistogram.getHistogramSnapshot());
        sbSummary.append("CacheHistogram (end)\n"+mtCacheHistogram.getHistogramSnapshot());
        sbSummary.append("ByteBufferCache "+getCacheName()+"- Summary end -\n");
        return sbSummary.toString();
    }

    /**
     * Returns the total number of buffers created by the cache
     */
    public int getTotalBuffersCreated() {
        return miTotalBuffersCreated;
    }

    /**
     * Returns the total bytes currently cached.
     */
    public long getTotalBytesInCache() {
        return mlTotalBytesInCache;
    }

    /**
     * Returns the total number of buffers that have been acquired.
     */
    public int getTotalBuffersAcquired() {
        return miTotalBuffersAcquired;
    }

    /**
     * Returns the total number of buffers returned to the cache.
     */
    public int getTotalBuffersReturned() {
        return miTotalBuffersReturned;
    }

    /**
     * Returns the number of buffers which have been acquired
     * but have not yet been returned.
     */
    public int getCurrentAquiredBuffers() {
        return miCurrentAquiredBuffers;
    }

    /**
     * Returns the current number of bytes which are contained in the buffers
     * which have not yet been returned.
     */
    public long getCurrentAquiredBytes() {
        return mlCurrentAquiredBytes;
    }

    /**
     * Returns the current number of bytes which are contained in the buffers
     * which have not yet been returned.
     */
    public long getMaxAquiredBytes() {
        return mlMAX_ACQUIRE_BYTES;
    }

    /**
     * Returns the max number of bytes to store in the
     * cache at any one time.  Anything more will not
     * be stored in the cache.
     *
     * @return long the max number of bytes to store in the cache.
     */
    public long getMaxNumBytesToCache() {
        return mlMAX_CACHE_BYTES;
    }

    /**
     * Returns whether or not the cache is bounded.
     */
    public boolean getIsCacheBounded() {
        return mbCACHE_BOUNDED;
    }

    public int getReturnBufferEntryCount()
    {
        return returnEntry;
    }

    public int getReturnBufferCount()
    {
        return returnCount;
    }

    public long getReturnBufferTime()
    {
        return returnTime;
    }

    /**
     * Records the stats for a buffer that has been
     * successfully acquired by a user of the cache.
     *
     * @param iBufferCapcacity int capacity of the buffer that is being aquired.
     *
     */
    private void recordAquiredBuffer(int iBufferCapcacity) {
        mlTotalBytesInCache -= iBufferCapcacity;
        miTotalBuffersAcquired++;
        miCurrentAquiredBuffers++;
        mlCurrentAquiredBytes += iBufferCapcacity;
    }
    /**
     * Records the stats for a buffer that has been
     * successfully returned by a user of the cache.
     *
     * @param iBufferCapcacity int capacity of the buffer that is being aquired.
     *
     */
    private void recordReturnedBuffer(int iBufferCapcacity) {
        miTotalBuffersReturned++;
        miCurrentAquiredBuffers--;
        mlCurrentAquiredBytes -= iBufferCapcacity;
    }

    /**
     * Records the stats for a buffer that has been
     * successfully returned by a user of the cache.
     *
     * @param iBufferCapcacity int capacity of the buffer that is being aquired.
     *
     */
    private void recordReturnedBufferToCache(int iBufferCapacity) {
        mlTotalBytesInCache += iBufferCapacity;
    }

    /**
     * Have all acquired bytes/buffers been returned and do total buffers
     * acquired match total buffers returned?
     *
     * @return <tt>true</tt> if the statistics are balanced.
     */
    public boolean isBalanced() {
        return (miTotalBuffersAcquired == miTotalBuffersReturned &&
                miCurrentAquiredBuffers == 0 &&
                mlCurrentAquiredBytes == 0);
    }

    /**
     * Get a ByteBuffer of the specified length, either from
     * the cache is created.
     *
     * @param iLength int length of the ByteBuffer
     * @return ByteBuffer whose actual length is >= iLength, or null.
     */
    public synchronized ByteBuffer acquireBuffer(int iLength) {
        boolean bDoAcquire = true;
        ByteBuffer tBuffer = null;
        //-Check to see if logging is needed.
        logHistogramIfNeeded();

        //
        //-compute the granular length of the requested buffer
        //
        int iNewGranularLength = computeGranularSize(miGranularity, iLength);

        //
        //-Find out if we have a buffer which has already been allocated.
        //
        tBuffer = findCachedBuffer(iNewGranularLength);

        if (tBuffer != null) {
            //
            //- If we have found an existing buffer in the cache
            //

            // set the position to 0 and the limit to match the length of the payload
            tBuffer.position(0);
            tBuffer.limit(iLength);

            //-removed cached bytes capacity from total cached bytes
            // in the cache.
            //-update internal statistics
            recordAquiredBuffer(tBuffer.capacity());
            //-update the CacheElementStatsTable to reflect
            // the new buffer being loaned out
            mtCacheHistogram.acquire(new Integer(iNewGranularLength));
        } else {
            //
            //- If we don't have an existing buffer in the cache
            //

            //-check to see if havn't exceeded acquire limit.
            if (mbAQUIRE_BOUNDED && mlMAX_ACQUIRE_BYTES < (mlCurrentAquiredBytes + (long) iNewGranularLength)) {
                //
                //-If we are outside the boundaries of what we are able to acquire then
                // don't allow the allocation of a new buffer to occur.
                //
                bDoAcquire = false;
            }

            if (bDoAcquire) {
                //
                //-Allocate a new buffer
                //

                //-if haven't found it then allocate it in a cavaleer manner.
                tBuffer = ByteBuffer.allocate(iNewGranularLength);
                //-if allocation is successfull, then record the appropriate stats.
                if (tBuffer != null) {
                    //-record the stats for the acquired buffer
                    recordAquiredBuffer(tBuffer.capacity());
                    //-update the CacheElementStatsTable to reflect
                    // the new buffer being loaned out
                    mtCacheHistogram.created(new Integer(iNewGranularLength));
                } else {
                    mtLog.error("ByteBuffer.allocate(" + iNewGranularLength + ") failed!");
                }
            } else {
                //
                //-FAILURE TO ALLOCATE either Cached or New Buffer
                //

                miNumAcquireDenied++;
                if ((miNumAcquireDenied < NUM_DENIED_ACQUIRES_BEFORE_MODULO) ||
                    (
                       (miNumAcquireDenied >= NUM_DENIED_ACQUIRES_BEFORE_MODULO) &&
                       ((miNumAcquireDenied % DENIED_ACQUIRE_REPORTING_MODULO) == 0)
                    )
                )
                {
                    mtLog.warn("Warning: ByteBufferCache Acquire Bound : " + mlMAX_ACQUIRE_BYTES + " bytes");
                    mtLog.warn("Warning: ByteBufferCache Acquire Bound Exceeded: Currently Acquired=" + getCurrentAquiredBytes() +
                            ", request for " + iNewGranularLength + " bytes rejected!");
                    mtLog.warn("Warning: ByteBufferCache has Denied Acquire  : " + miNumAcquireDenied + " times");
                }
            }
        }
        //  if (mtLog.isDebugEnabled()) {
        //      mtLog.debug("Number of buffers allocated = " + miCurrentAquiredBuffers
        //                  + " number of bytes = " + mlCurrentAquiredBytes);
        //  }
        return tBuffer;
    }

    /**
     * Receives a ByteBuffer from a source.
     *
     * @param tBuffer ByteBuffer the new buffer to be processed.
     * @see icecube.daq.payload.IByteBufferReceiver
     */
    public void receiveByteBuffer(ByteBuffer tBuffer) {
        returnBuffer(tBuffer);
    }

    public void destinationClosed() {
        // do nothing
    }
    /**
     * Returns the String which contains the snapshot of the
     * histogram for this Cache.
     *
     * @return String the string representation of the histogram of
     *         the cache at the time this is called.
     */
    public String getHistogramSnapshot() {
        return mtCacheHistogram.getHistogramSnapshot();
    }

    /**
     * This will log the histogram to the info() log if needed.
     */
    private void logHistogramIfNeeded() { }

    //private void logHistogramIfNeeded() {
    //long lNow = System.currentTimeMillis();
    //long lTimeSinceLastLog = lNow - mlTimeOfLastHistogramLog;
    //
    //-Log the histogram summary if enough time has elapsed
    //
    //if (lTimeSinceLastLog >= mlHistogramLogTimeInterval) {
    //  mtLog.info("ByteBufferCache("+getCacheName()+") Histogram:\n" + mtCacheHistogram.getHistogramSnapshot());
    //    mlTimeOfLastHistogramLog = lNow;
    //   }
    //}

    /**
     * Returns the ByteBuffer to the cache.
     *
     * @param tByteBuffer ByteBuffer to be returned.
     */
    public void returnBuffer(ByteBuffer tByteBuffer)
    {
        returnEntry++;
        final long startTime = System.currentTimeMillis();
        returnBufferInternal(tByteBuffer);
        returnTime += System.currentTimeMillis() - startTime;
        returnCount++;
    }
    /**
     * Returns the ByteBuffer to the cache.
     *
     * @param tByteBuffer ByteBuffer to be returned.
     */
    public synchronized void returnBufferInternal(ByteBuffer tByteBuffer) {
        boolean bBufferCached = false;
        //-Check to see if logging is needed.
        //logHistogramIfNeeded();

        if (tByteBuffer == null) {
            return;
        }
        int iReturnedCapacity = tByteBuffer.capacity();
        //-make sure that this is managed by this cache - ie it should be a multiple
        // of the granularity.
        if ((iReturnedCapacity % miGranularity) != 0) {
            //-This condition occures when a ByteBuffer is returned of a size that was not
            // produced by this ByteBuffer cache.
            mtLog.error(getCacheName()+":Unmanaged ByteBuffer of Capacity=" + iReturnedCapacity + " returned, ignoring...");
            //-don't update statistics for this buffer as it was not created with this cache.
            return;
        }

        recordReturnedBuffer( iReturnedCapacity );

        long lProjectedNewCachedBytes = (long) iReturnedCapacity + mlTotalBytesInCache;

        // clear the buffer: position goes to 0 and limit goes to capacity
        tByteBuffer.clear();

        if (mbCACHE_BOUNDED && (lProjectedNewCachedBytes > mlMAX_CACHE_BYTES)) {
            if (!mbOutputWarningOfMaxCachedBytesOnceFlag) {
                if (mtLog.isWarnEnabled()) {
                    mtLog.warn("max number of cached bytes(" + mlMAX_CACHE_BYTES +
                            ") exceeded, disreguarding return of ByteBuffer");
                    mbOutputWarningOfMaxCachedBytesOnceFlag = true;
                }
            }
            mtCacheHistogram.remove(new Integer(iReturnedCapacity) );
            return;
        }
        //-get the key to the correct table from the capacity.
        Integer tKey = new Integer(iReturnedCapacity);
        //synchronized (mtGranularBufferTables) {
        //-get the table (if present which maintains this size
        Object tMapObject = mtGranularBufferTables.get(tKey);
        IdentityHashMap tSingleSizeIMap = null;
        //-if a table object has not been created for this size yet
        // then create one and add to the list.
        if (tMapObject == null) {
            // todo remove this later
            //if (mtLog.isDebugEnabled()) {
            //    mtLog.debug("returnBuffer() did not find map for capacity="+tKey.intValue()+". Creating new map.");
            //}
            tSingleSizeIMap = new IdentityHashMap();
            mtGranularBufferTables.put(tKey, tSingleSizeIMap);
            //-put the object in as keyed to itself to ensure it is unique
            // so that duplicate objects are not placed into the table.
            //synchronized (tSingleSizeIMap) {
            tSingleSizeIMap.put(tByteBuffer, tByteBuffer);
            bBufferCached = true;
            //}
        } else {
            // todo remove this later
            // if (mtLog.isDebugEnabled()) {
            //     mtLog.debug("returnBuffer() FOUND map for capacity="+tKey.intValue());
            // }
            //-have the map, now check for uniqueness (this may be too slow)
            boolean bReturnedBufferUnique = false;
            tSingleSizeIMap = (IdentityHashMap) tMapObject;
            // synchronized (tSingleSizeIMap) {
            if (!tSingleSizeIMap.containsKey(tByteBuffer)) {
                tSingleSizeIMap.put(tByteBuffer, tByteBuffer);
                bReturnedBufferUnique = true;
                bBufferCached = true;
            }
            // }
            //-Make sure that ByteBuffer that was returned is unique!
            // if not then there is a PROBLEM!
            if (!bReturnedBufferUnique) {
                String sMsg = "Error:icecube.daq.payload.ByteBufferCache: detected duplicate return of ByteBuffer!";
                Exception tException = new Exception();
                StringBuffer tSB = new StringBuffer();
                StackTraceElement[] atStackArray = tException.getStackTrace();
                for (int ii = 0; ii < atStackArray.length; ii++) {
                    tSB.append(atStackArray[ii].toString() + "\n");
                }
                mtLog.error(sMsg + "\n StackTrace:\n" + tSB.toString());
            }
        }
        //}

        //
        //-Update the stats if the returned buffer has been cached in the table.
        //
        if (bBufferCached) {
            //-create a key for looking up the cache element as needed.
            Integer tCap = new Integer(tByteBuffer.capacity());
            //
            //- Record stats for a buffer that is returned to the cache
            //  (This can be combined later into a single call for all purposes
            //  but for now this is segregated)
            //
            recordReturnedBufferToCache(tCap.intValue());
            mtCacheHistogram.cache(tCap);
        }
        // if (mtLog.isDebugEnabled()) {
        //     mtLog.debug("Number of buffers allocated = " + miCurrentAquiredBuffers
        //                 + " number of bytes = " + mlCurrentAquiredBytes);
        // }
    }

    /**
     * Flushes all the unused buffers in the cache.
     */
    public void flush() {
        mtGranularBufferTables.clear();
        //-clear the histogram if in use.
        if (mtCacheHistogram != null) {
            mtCacheHistogram.clear();
        }
    }

    /**
     * Finds a cached buffer or creates a new one to satisfy the request.
     *
     * @param iGranularSize the size (to the next nearest granularity size) of the ByteBuffer
     *                      to retrieve.
     * @return ByteBuffer of the specified granular size.
     */
    protected ByteBuffer findCachedBuffer(int iGranularSize) {

        ByteBuffer tRequestedBuffer = null;
        //
        //-Create the key to find a buffer in the hash, Integer's are used as the hashkeys
        // to find the buffer of a particualar size.
        //
        Integer tKey = new Integer(iGranularSize);
        //
        //-Lookup the Hashmap for this particular size.
        // The reason that an IdentityHashMap is used is to prevent a comparison beyon reference
        // which is the default behavior of a ByteBuffer.  That would be crazy! :)
        //
        IdentityHashMap tSingleSizeMap = (IdentityHashMap) mtGranularBufferTables.get(tKey);
        //
        //-If there is an existing IdentityHasMap for this size
        // then try to see if there is a Buffer of this size available.
        //
        if (tSingleSizeMap != null) {
            synchronized (tSingleSizeMap) {
                //
                //-Check to see if the map for this list is empty
                // NOTE: The reason for this being a map is so that we can
                //       check for duplicate returns which of course is a serious error condition
                //       that is checked for in the return method.
                //
                if (!tSingleSizeMap.isEmpty()) {
                    //
                    //-Lock the list and remove an element
                    //-remove the first keyed element in the list to return to caller.
                    // the objects are key'd on themselves to assure unique addition to the list
                    //
                    tRequestedBuffer = (ByteBuffer) tSingleSizeMap.remove(tSingleSizeMap.keySet().iterator().next());
                    // todo remove this later
                    // if (mtLog.isDebugEnabled()) {
                    //     mtLog.debug("FOUND CACHED Buffer for size="+iGranularSize);
                    // }
                }
            }
        }
        return tRequestedBuffer;
    }


    /**
     * Private utility method to compute the size which is a multiple
     * of the granular size which is greater than or equal to the requested
     * size.
     *
     * @param iRequestedSize the requested size
     * @return the granular size based on the input granule size.
     */
    private static int computeGranularSize(int iGranuleSize, int iRequestedSize) {
        int iNewGranularSize = ((iRequestedSize / iGranuleSize) + ((iRequestedSize % iGranuleSize) > 0 ? 1 : 0)) * iGranuleSize;
        return iNewGranularSize;
    }
}

/**
 * Object which records the statistics for a particular
 * ByteBuffer size when cached.
 *
 * @author dwharton
 */
class CacheElementStats implements Comparable {
    /**
     * unit size of the elements tracked by this entry in the cache
     */
    public Integer mtElementUnitSize;
    /**
     * Number of elements of this type currently in the cache, which
     * are ready to be loaned out.
     */
    private int     miNumElementsInCache;
    /**
     * Number of elements of this type that have been loaned-out, in use by the
     * cache.
     */
    private int     miNumElementsAcquired;
    /**
     * Number of times this element has been cached.
     */
    private long    mlTimesCached;
    /**
     * Number of times this element time has been used
     */
    private long    mlTimesAcquired;
    /**
     * Number of times this element type has been cleared.
     */
    private int     miTimesCleared;

    /**
     * Number of Unique elements created.
     */
    private int miNumElementsCreated;


    /**
     * Constructor which defines the size of the element being tracked.
     *
     * @param tElementUnitSize - Integer holding the size of the cache element bin.
     *
     */
    public CacheElementStats(Integer tElementUnitSize) {
        mtElementUnitSize = tElementUnitSize;
    }

    /**
     * Returns a summary of the contribution of this element
     * to the overall histogram of the ByteBufferCache use.
     *
     * @return String formatted histogram entry output
     */
    public String getElementHistogramEntry() {
        StringBuffer sbEntry = new StringBuffer();
        sbEntry.append("buffer-size=" + mtElementUnitSize.intValue() );
        sbEntry.append(", created=" + getElementsCreated() );
        sbEntry.append(", cached="    + getNumElementsInCache() );
        sbEntry.append(", acquired="      + getNumElementsAcquired() );
        sbEntry.append(", bytes="     + getCachedBytes() );
        return sbEntry.toString();
    }

    /**
     * Returns the number of unique elements created.
     *
     * @return the number of elements created.
     */
    public int getElementsCreated() {
        return miNumElementsCreated;
    }
    /**
     * Gets the size in bytes of the cache element.
     *
     * @return int - size of the individual element in bytes (not
     *               the total size of bytes cached only the unit size).
     */
    public int getElementUnitSize() {
        return mtElementUnitSize.intValue();
    }
    /**
     * Gets the number of elements of this type
     * which are currently cached.
     *
     * @return int - number currently in the cache.
     */
    public int getNumElementsInCache() {
        return miNumElementsInCache;
    }
    /**
     * Get the number of elements being used.
     *
     * @return int - the number of elements currently being used of this type.
     */
    public int getNumElementsAcquired() {
        return miNumElementsAcquired;
    }
    /**
     * Computes and returns number of cached Bytes for this element type.
     * @return long - then number of bytes cached
     */
    public long getCachedBytes() {
        return (long) ( (long) miNumElementsInCache * mtElementUnitSize.longValue() );
    }
    /**
     * Accounts for the creation of a new element.
     *
     */
    public void created() {
        miNumElementsCreated++;
    }
    /**
     * Accounts for the case of a single unit of this element
     * being 'cached' in the cache.
     */
    public void cache() {
        miNumElementsInCache++;
        if (miNumElementsAcquired > 0) miNumElementsAcquired--;
        mlTimesCached++;
    }
    /**
     * Accounts for the case of a single unit of this element
     * being 'used' from the cache.
     */
    public void acquire() {
        if (miNumElementsInCache > 0) miNumElementsInCache--;
        miNumElementsAcquired++;
        mlTimesAcquired++;
    }
    /**
     * Removes a unit of this element from the statistics.
     */
    public void remove() {
        if (miNumElementsInCache > 0) miNumElementsInCache--;
    }
    /**
     * Clears the statistics associated with this element.
     */
    public void clear() {
        miNumElementsInCache=0;
        miNumElementsAcquired=0;
        miNumElementsCreated=0;
        miTimesCleared++;
    }
    /**
     * Produces a hashCode based on the element size.
     * @return int - the code produced by the internal element size Integer object.
     */
    public int hashCode() {
        return mtElementUnitSize.hashCode();
    }

    /**
     * Implementation of Comparable.
     */
    public int compareTo(Object tObject) {
        //-compares to the Integer Element Size which is what
        // defines the order (magnitude) of this element.
        // return mtElementUnitSize.compareTo( ( Comparable) tObject);
        //Integer tIntegerObject = (Integer) tObject;
        CacheElementStats tStats = (CacheElementStats) tObject;
        return mtElementUnitSize.compareTo( tStats.mtElementUnitSize );
    }
}

/**
 * This object keeps track of the access characteristics for the ByteBufferCache.
 */
class CacheHistogram {
    /**
     * Table which contains the elements which comprise the histogram.
     */
    private Hashtable mtCacheElementStatsTable = new Hashtable();

    /**
     * Constructor.
     */
    public CacheHistogram() {
    }

    /**
     * Returns a summary of the contribution of this element
     * to the overall histogram of the ByteBufferCache use.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     *
     * @return String formatted histogram entry output
     */
    public String getElementHistogramEntry(Integer tElementUnitSize) {
        String sHistEntry = "error:element-size="+tElementUnitSize.intValue();
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats != null)
            sHistEntry = tStats.getElementHistogramEntry();
        return sHistEntry;
    }

    /**
     * Gets the size in bytes of the cache element.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     *
     * @return int - size of the individual element in bytes (not
     *               the total size of bytes cached only the unit size).
     */
    public int getElementUnitSize(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats != null)
            return tStats.getElementUnitSize();
        else
            return -1;
    }

    /**
     * Gets the number of elements of this type
     * which are currently cached.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     *
     * @return int - number currently in the cache.
     */
    public int getNumElementsInCache(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats != null)
            return tStats.getNumElementsInCache();
        else
            return -1;
    }
    /**
     * Get the number of elements being used.
     *
     * @return int - the number of elements currently being used of this type.
     */
    public int getNumElementsAcquired(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats != null)  {
            return tStats.getNumElementsAcquired();
        } else {
            return -1;
        }
    }
    /**
     * Computes and returns number of cached Bytes for this element type.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     *
     * @return long - then number of bytes cached
     */
    public long getCachedBytes(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats != null)
            return tStats.getCachedBytes();
        else
            return (long) -1;
    }
    /**
     * Accounts for the case of a single unit of this element
     * being 'cached' in the cache.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     */
    public void cache(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats == null) {
            tStats = new CacheElementStats(tElementUnitSize);
            mtCacheElementStatsTable.put(tElementUnitSize, tStats);
            //System.out.println("ByteBufferCache.cache("+tElementUnitSize.intValue()+")");
        }
        tStats.cache();
    }
    /**
     * Accounts for the case of a single unit of this element
     * being 'used' from the cache.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     */
    public void acquire(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats == null) {
            tStats = new CacheElementStats(tElementUnitSize);
            mtCacheElementStatsTable.put(tElementUnitSize, tStats);
            //System.out.println("ByteBufferCache.acquire("+tElementUnitSize.intValue()+")");
        }
        tStats.acquire();
    }
    /**
     * Removes a unit of this element from the statistics.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     */
    public void remove(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats == null) {
            tStats = new CacheElementStats(tElementUnitSize);
            mtCacheElementStatsTable.put(tElementUnitSize, tStats);
            //System.out.println("ByteBufferCache.remove("+tElementUnitSize.intValue()+")");
        }
        tStats.remove();
    }

    /**
     * Clears the statistics associated with this element.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     */
    public void clear(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats == null) {
            tStats = new CacheElementStats(tElementUnitSize);
            mtCacheElementStatsTable.put(tElementUnitSize, tStats);
            //System.out.println("ByteBufferCache.clear("+tElementUnitSize.intValue()+")");
        }
        tStats.clear();
    }
    /**
     * Records the creation the statistics associated with this
     * element.
     *
     * @param tElementUnitSize Integer which hashes to the appropriate element being tracked.
     */
    public void created(Integer tElementUnitSize) {
        CacheElementStats tStats = (CacheElementStats) mtCacheElementStatsTable.get(tElementUnitSize);
        if (tStats == null) {
            tStats = new CacheElementStats(tElementUnitSize);
            mtCacheElementStatsTable.put(tElementUnitSize, tStats);
            //System.out.println("ByteBufferCache.clear("+tElementUnitSize.intValue()+")");
        }
        tStats.created();
    }

    /**
     * Clears the statistics associated with this element.
     */
    public void clear() {
        mtCacheElementStatsTable.clear();
    }

    /**
     * Creates a snapshot of the status of the ByteBufferCache being
     * tracked and produces a summary string representing the sorted list
     * of CacheElementStats.
     *
     * @return String - the multi-line histogram summary of the ByteBufferCache being tracked.
     */
    public synchronized String getHistogramSnapshot() {
        StringBuffer sbHist = new StringBuffer();
        //-pull out the elements
        Enumeration tStatsEnum = mtCacheElementStatsTable.keys();
        //-create a sortable list
        ArrayList tStatsList = new ArrayList();
        while (tStatsEnum.hasMoreElements()) {
            tStatsList.add( mtCacheElementStatsTable.get( tStatsEnum.nextElement() ) );
        }
        //-sort the list according to the Element's
        java.util.Collections.sort(tStatsList);
        //-convert to array
        Object[] taStats = tStatsList.toArray();
        for (int ii=0; ii < taStats.length; ii++) {
            CacheElementStats tElementHistEntry = (CacheElementStats) taStats[ii];
            sbHist.append(tElementHistEntry.getElementHistogramEntry() +"\n");
        }
        return sbHist.toString();
    }

}
