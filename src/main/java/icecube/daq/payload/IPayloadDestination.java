package icecube.daq.payload;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Objects which implement this interface act to provide
 * a destination to which Payload's may be written. This
 * should provide other destinations besides the standard
 * ByteBuffer, however, it does not provide direct access
 * to the output.
 *
 * @author dwharton
 */
public interface IPayloadDestination
    extends DataOutput
{

    /**
     * Tells whether or not this channel is open.
     *
     * @return <tt>true</tt> if, and only if, this channel is open
     */
    boolean isOpen();

    /**
     * Closes this channel.
     *
     * <p> After a channel is closed, any further attempt to invoke I/O
     * operations upon it will cause a <tt>ClosedChannelException</tt>
     * to be thrown.
     *
     * <p> If this channel is already closed then invoking this method has no
     * effect.
     *
     * <p> This method may be invoked at any time.  If some other thread has
     * already invoked it, however, then another invocation will block until
     * the first invocation is complete, after which it will return without
     * effect. </p>
     *
     * @throws  IOException  If an I/O error occurs
     */
    void close()
        throws IOException;

    /**
     * Returns boolean to tell if label operation should be performed.
     * this saves objects work if they are using the label feature for
     * PayloadDestinations which do not do labeling.
     * @return boolean true if labeling is on, false if off.
     */
    boolean doLabel();

    /**
     * simple labeling routine which is a stub but is useful for debugging.
     * this is NOT INTENDED to contribute to the output stream.
     * @param sLabel String which indicates some aspect of how the destination
     *               is being used at a point in the writing of payloads.
     * @return this object
     */
    IPayloadDestination label(String sLabel);

    /**
     * adds indentation level for labeling
     * @return this object
     */
    IPayloadDestination indent();

    /**
     * removes indentation level for labeling
     * @return this object
     */
    IPayloadDestination undent();

    /**
     * This method writes bytes from the given offset in the ByteBuffer for a
     * length of iBytes to the destination.
     * @param iOffset the offset in the ByteBuffer to start
     * @param tBuffer ByteBuffer from which to write to destination.
     * @param iBytes  the number of bytes to write to the destination.
     *
     * @throws IOException if an error occurs either reading the ByteBuffer or
     *                     writing to the destination.
     */
    void write(int iOffset, ByteBuffer tBuffer, int iBytes)
        throws IOException;

    /**
     * Writes to the output stream the eight
     * low-order bits of the argument <code>b</code>.
     * The 24 high-order  bits of <code>b</code>
     * are ignored.
     *
     * @param sName name of the string
     * @param      b   the byte to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    void write(String sName, int b) throws IOException;

    /**
     * Writes to the output stream all the bytes in array <code>b</code>.
     * If <code>b</code> is <code>null</code>,
     * a <code>NullPointerException</code> is thrown.
     * If <code>b.length</code> is zero, then
     * no bytes are written. Otherwise, the byte
     * <code>b[0]</code> is written first, then
     * <code>b[1]</code>, and so on; the last byte
     * written is <code>b[b.length-1]</code>.
     *
     * @param      sName String the label
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     */
    void write(String sName, byte[] b)
        throws IOException;

    /**
     * Writes to the output stream all the bytes in array <code>b</code>.
     * If <code>b</code> is <code>null</code>,
     * a <code>NullPointerException</code> is thrown.
     * If <code>b.length</code> is zero, then
     * no bytes are written. Otherwise, the byte
     * <code>b[0]</code> is written first, then
     * <code>b[1]</code>, and so on; the last byte
     * written is <code>b[b.length-1]</code>.
     *
     * @param      sName the label
     * @param      sSpecial any special interpretation of this data
     * @param      b   the data.
     * @exception  IOException  if an I/O error occurs.
     */
    void write(String sName, String sSpecial, byte[] b)
        throws IOException;

    /**
     * This method writes bytes from the given offset in the ByteBuffer for a
     * length of iBytes to the destination.
     *
     * @param sFieldName name of the field.
     * @param iOffset the offset in the ByteBuffer to start
     * @param tBuffer ByteBuffer from which to write to destination.
     * @param iBytes the number of bytes to write to the destination.
     *
     * @throws IOException if an error occurs either reading the ByteBuffer or
     *                     writing to the destination.
     */
    void write(String sFieldName, int iOffset, ByteBuffer tBuffer, int iBytes)
        throws IOException;

    /**
     * Writes to the output stream the eight low-
     * order bits of the argument <code>v</code>.
     * The 24 high-order bits of <code>v</code>
     * are ignored. (This means  that <code>writeByte</code>
     * does exactly the same thing as <code>write</code>
     * for an integer argument.) The byte written
     * by this method may be read by the <code>readByte</code>
     * method of interface <code>DataInput</code>,
     * which will then return a <code>byte</code>
     * equal to <code>(byte)v</code>.
     *
     * @param sName name of the string
     * @param      v   the byte value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    void writeByte(String sName, int v)
        throws IOException;

    /**
     * Writes two bytes to the output
     * stream to represent the value of the argument.
     * The byte values to be written, in the  order
     * shown, are: <p>
     * <pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 8))
     * (byte)(0xff &amp; v)
     * </code> </pre> <p>
     * The bytes written by this method may be
     * read by the <code>readShort</code> method
     * of interface <code>DataInput</code> , which
     * will then return a <code>short</code> equal
     * to <code>(short)v</code>.
     *
     * @param sName name of the string
     * @param      v   the <code>short</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    void writeShort(String sName, int v)
        throws IOException;

    /**
     * Writes an <code>int</code> value, which is
     * comprised of four bytes, to the output stream.
     * The byte values to be written, in the  order
     * shown, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt; &#32; &#32;8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be read
     * by the <code>readInt</code> method of interface
     * <code>DataInput</code> , which will then
     * return an <code>int</code> equal to <code>v</code>.
     *
     * @param sName name of the string
     * @param      v   the <code>int</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    void writeInt(String sName, int v)
        throws IOException;

    /**
     * Writes a <code>long</code> value, which is
     * comprised of eight bytes, to the output stream.
     * The byte values to be written, in the  order
     * shown, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 56))
     * (byte)(0xff &amp; (v &gt;&gt; 48))
     * (byte)(0xff &amp; (v &gt;&gt; 40))
     * (byte)(0xff &amp; (v &gt;&gt; 32))
     * (byte)(0xff &amp; (v &gt;&gt; 24))
     * (byte)(0xff &amp; (v &gt;&gt; 16))
     * (byte)(0xff &amp; (v &gt;&gt;  8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be
     * read by the <code>readLong</code> method
     * of interface <code>DataInput</code> , which
     * will then return a <code>long</code> equal
     * to <code>v</code>.
     *
     * @param sName name of the string
     * @param      v   the <code>long</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    void writeLong(String sName, long v)
        throws IOException;

    /**
     * Writes every character in the string <code>s</code>,
     * to the output stream, in order,
     * two bytes per character. If <code>s</code>
     * is <code>null</code>, a <code>NullPointerException</code>
     * is thrown.  If <code>s.length</code>
     * is zero, then no characters are written.
     * Otherwise, the character <code>s[0]</code>
     * is written first, then <code>s[1]</code>,
     * and so on; the last character written is
     * <code>s[s.length-1]</code>. For each character,
     * two bytes are actually written, high-order
     * byte first, in exactly the manner of the
     * <code>writeChar</code> method.
     *
     * @param sName name of the string
     * @param s the string value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    void writeChars(String sName, String s)
        throws IOException;

    /**
     * Writes out elements of an short array from a source element
     * to a final element.
     *
     * @param sArrayName name of the int array
     * @param iFirst the first element number to write
     * @param iLast the last element number to write
     * @param iaArray array containing the elements to write
     *
     * @throws IOException if an error occurs during the process
     */
    void writeShortArrayRange(String sArrayName, int iFirst, int iLast,
                              short[] iaArray)
        throws IOException;

    /**
     * Writes out elements of an short array from a source element
     * to a final element as bytes.
     *
     * @param sArrayName name of the int array
     * @param iFirst the first element number to write
     * @param iLast the last element number to write
     * @param iaArray array containing the elements to write
     *
     * @throws IOException if an error occurs during the process
     */
    void writeShortArrayRangeAsBytes(String sArrayName, int iFirst, int iLast,
                                     short[] iaArray)
        throws IOException;

    /**
     * Writes out elements of an int array from a source element
     * to a final element.
     *
     * @param sArrayName name of the int array
     * @param iFirst the first element number to write
     * @param iLast the last element number to write
     * @param iaArray array containing the elements to write
     *
     * @throws IOException if an error occurs during the process
     */
    void writeIntArrayRange(String sArrayName, int iFirst, int iLast,
                            int[] iaArray)
        throws IOException;

    /**
     * This methods proxies the call to write Payload to allow the whole
     * payload to be passed to the payload destination to allow it to
     * be invoke the write method itself, or to pass the payload by refernce
     * to the target.
     *
     * @param tPayload Payload to which to write to this destination
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    int writePayload(IWriteablePayload tPayload)
        throws IOException;

    /**
     * This methods proxies the call to write Payload to allow the whole
     * payload to be passed to the payload destination to allow it to
     * be invoke the write method itself, or to pass the payload by refernce
     * to the target.
     *
     * @param bWriteLoaded boolean to indicate if the loaded vs buffered
     *                     payload should be written.
     * @param tPayload Payload to which to write to this destination
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    int writePayload(boolean bWriteLoaded, IWriteablePayload tPayload)
        throws IOException;

    /**
     * This methods proxies the call to write Payload to allow the whole
     * payload to be passed to the payload destination to allow it to
     * be invoke the write method itself, or to pass the payload by refernce
     * to the target.
     *
     * @param bWriteLoaded boolean to indicate if the loaded vs buffered
     *                     payload should be written.
     * @param tPayload Payload to which to write to this destination
     * @param iDestOffset the offset into the destination ByteBuffer at which
     *                    to start writting the payload
     * @param tDestBuffer the destination ByteBuffer to write the payload to.
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    int writePayload(boolean bWriteLoaded, IWriteablePayload tPayload,
                     int iDestOffset, ByteBuffer tDestBuffer)
        throws IOException;


}
