package icecube.daq.oldpayload;

import icecube.daq.payload.IPayloadDestination;
import icecube.daq.payload.IWriteablePayload;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *  This class is meant to be a null implementation
 *  of the PayloadDestination.
 *  NOTE: removed WriteableByteChannel implementation because of conflicting polymorphism with return types
 */
public abstract class PayloadDestination extends DataOutputAdapter implements IPayloadDestination {
    protected boolean mb_doLabel;

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
    public int writePayload(IWriteablePayload tPayload) throws IOException {
        return writePayload(false, tPayload);
    }

    /**
     * This methods proxies the call to write Payload to allow the whole
     * payload to be passed to the payload destination to allow it to
     * be invoke the write method itself, or to pass the payload by refernce
     * to the target.
     *
     * @param bWriteLoaded boolean to indicate if the loaded vs buffered payload should be written.
     * @param tPayload Payload to which to write to this destination
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, IWriteablePayload tPayload) throws IOException {
        return tPayload.writePayload(bWriteLoaded, this);
    }

    /**
     * This methods proxies the call to write Payload to allow the whole
     * payload to be passed to the payload destination to allow it to
     * be invoke the write method itself, or to pass the payload by refernce
     * to the target.
     *
     * @param bWriteLoaded boolean to indicate if the loaded vs buffered payload should be written.
     * @param tPayload Payload to which to write to this destination
     * @param iDestOffset the offset into the destination ByteBuffer at which to start writting the payload
     * @param tDestBuffer the destination ByteBuffer to write the payload to.
     * @return the length in bytes which was written to the ByteBuffer.
     *
     * @throws IOException if an error occurs during the process
     */
    public int writePayload(boolean bWriteLoaded, IWriteablePayload tPayload, int iDestOffset, ByteBuffer tDestBuffer) throws IOException {
        return tPayload.writePayload(bWriteLoaded, iDestOffset, tDestBuffer);
    }
    //
    //-writeablechannel implementation
    // (start)

    /**
     * Writes a sequence of bytes to this channel from the given buffer.
     *
     * <p> An attempt is made to write up to <i>r</i> bytes to the channel,
     * where <i>r</i> is the number of bytes remaining in the buffer, that is,
     * <tt>dst.remaining()</tt>, at the moment this method is invoked.
     *
     * <p> Suppose that a byte sequence of length <i>n</i> is written, where
     * <tt>0</tt>&nbsp;<tt>&lt;=</tt>&nbsp;<i>n</i>&nbsp;<tt>&lt;=</tt>&nbsp;<i>r</i>.
     * This byte sequence will be transferred from the buffer starting at index
     * <i>p</i>, where <i>p</i> is the buffer's position at the moment this
     * method is invoked; the index of the last byte written will be
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>.
     * Upon return the buffer's position will be equal to
     * <i>p</i>&nbsp;<tt>+</tt>&nbsp;<i>n</i>; its limit will not have changed.
     *
     * <p> Unless otherwise specified, a write operation will return only after
     * writing all of the <i>r</i> requested bytes.  Some types of channels,
     * depending upon their state, may write only some of the bytes or possibly
     * none at all.  A socket channel in non-blocking mode, for example, cannot
     * write any more bytes than are free in the socket's output buffer.
     *
     * <p> This method may be invoked at any time.  If another thread has
     * already initiated a write operation upon this channel, however, then an
     * invocation of this method will block until the first operation is
     * complete. </p>
     *
     * @param  src
     *         The buffer from which bytes are to be retrieved
     *
     * @return The number of bytes written, possibly zero
     *
     * @throws  java.nio.channels.NonWritableChannelException
     *          If this channel was not opened for writing
     *
     * @throws  java.nio.channels.ClosedChannelException
     *          If this channel is closed
     *
     * @throws  java.nio.channels.AsynchronousCloseException
     *          If another thread closes this channel
     *          while the write operation is in progress
     *
     * @throws  java.nio.channels.ClosedByInterruptException
     *          If another thread interrupts the current thread
     *          while the write operation is in progress, thereby
     *          closing the channel and setting the current thread's
     *          interrupt status
     *
     * @throws  IOException
     *          If some other I/O error occurs
     */
    public int writeRemaining(ByteBuffer src) throws IOException {
        errorUnimplementedMethod("writeRemaining(ByteBuffer src)");
        return -1;
    }
    /**
     * Tells whether or not this channel is open.
     *
     * @return <tt>true</tt> if, and only if, this channel is open
     */
    public boolean isOpen() {
        return true;
    }
    /**
     * Closes this channel.
     *
     * <p> After a channel is closed, any further attempt to invoke I/O
     * operations upon it will cause a {@link java.nio.channels.ClosedChannelException} to be
     * thrown.
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
    public void close() throws IOException {
        // errorUnimplementedMethod("close()");
    }
    // (end)
    //-writeablechannel implementation
    //

    //
    //-UTILITY METHODS to enhance destination
    //

    /**
     * This method writes bytes from the given offset in the ByteBuffer for a length of iBytes
     * to the destination.
     * @param iOffset the offset in the ByteBuffer to start
     * @param tBuffer ByteBuffer from which to write to destination.
     * @param iBytes  the number of bytes to write to the destination.
     *
     * @throws IOException....if an error occurs either reading the ByteBuffer or writing
     *                        to the destination.
     */
    public void write(int iOffset, ByteBuffer tBuffer, int iBytes) throws IOException {
        errorUnimplementedMethod("write(int iOffset, ByteBuffer tBuffer, int iBytes)");
    }

    /**
     * simple labeling routine which is a stub but is useful for debugging.
     * this is NOT INTENDED to contribute to the output stream.
     * @param sLabel String which indicates some aspect of how the destination is being used
     *                   at a point in the writing of payloads.
     */
    public PayloadDestination label(String sLabel) {
        return this;
    }

    /**
     * Returns boolean to tell if label operation should be performed.
     * this saves objects work if they are using the label feature for
     * PayloadDestinations which do not do labeling.
     * @return boolean true if labeling is on, false if off.
     */
    public boolean doLabel() {
        return mb_doLabel;
    }

    /**
     * adds indentation level for labeling
     */
    public PayloadDestination indent() {
        return this;
    }
    /**
     * removes indentation level for labeling
     */
    public PayloadDestination undent() {
        return this;
    }
    //------NAMED METHODS-------------
    /**
     * See writeRemaining without the sName field.
     */
    public int writeRemaining(String sFieldName, ByteBuffer src) throws IOException {
        return writeRemaining(src);
    }
    /**
     * This method writes bytes from the given offset in the ByteBuffer for a length of iBytes
     * to the destination.
     *
     * @param sFieldName name of the field.
     * @param iOffset the offset in the ByteBuffer to start
     * @param tBuffer ByteBuffer from which to write to destination.
     * @param iBytes the number of bytes to write to the destination.
     *
     * @throws IOException....if an error occurs either reading the ByteBuffer or writing
     *                        to the destination.
     */
    public void write(String sFieldName, int iOffset, ByteBuffer tBuffer, int iBytes) throws IOException {
        write(iOffset, tBuffer, iBytes);
    }
    /**
     * Writes a <code>boolean</code> value to this output stream.
     * If the argument <code>v</code>
     * is <code>true</code>, the value <code>(byte)1</code>
     * is written; if <code>v</code> is <code>false</code>,
     * the  value <code>(byte)0</code> is written.
     * The byte written by this method may
     * be read by the <code>readBoolean</code>
     * method of interface <code>DataInput</code>,
     * which will then return a <code>boolean</code>
     * equal to <code>v</code>.
     *
     * @param      v   the boolean to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeBoolean(String sName, boolean v) throws IOException {
        writeBoolean(v);
    }
    /**
     * Writes to the output stream the eight
     * low-order bits of the argument <code>b</code>.
     * The 24 high-order  bits of <code>b</code>
     * are ignored.
     *
     * @param      b   the byte to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(String sName, int b) throws IOException {
        write(b);
    }
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
    public void write(String sName, String sSpecial, byte[] b) throws IOException {
        write(b);
    }
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
    public void write(String sName, byte[] b) throws IOException {
        write(b);
    }
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
     * @param      v   the <code>short</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeShort(String sName, int v) throws IOException {
        writeShort(v);
    }
    /**
     * Writes <code>len</code> bytes from array
     * <code>b</code>, in order,  to
     * the output stream.  If <code>b</code>
     * is <code>null</code>, a <code>NullPointerException</code>
     * is thrown.  If <code>off</code> is negative,
     * or <code>len</code> is negative, or <code>off+len</code>
     * is greater than the length of the array
     * <code>b</code>, then an <code>IndexOutOfBoundsException</code>
     * is thrown.  If <code>len</code> is zero,
     * then no bytes are written. Otherwise, the
     * byte <code>b[off]</code> is written first,
     * then <code>b[off+1]</code>, and so on; the
     * last byte written is <code>b[off+len-1]</code>.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(String sName, byte[] b, int off, int len) throws IOException {
        write(b, off, len);
    }
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
     * @param      v   the <code>int</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeInt(String sName, int v) throws IOException {
        writeInt(v);
    }
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
     * @param      v   the byte value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeByte(String sName, int v) throws IOException {
        writeByte(v);
    }
    /**
     * Writes a <code>float</code> value,
     * which is comprised of four bytes, to the output stream.
     * It does this as if it first converts this
     * <code>float</code> value to an <code>int</code>
     * in exactly the manner of the <code>Float.floatToIntBits</code>
     * method  and then writes the <code>int</code>
     * value in exactly the manner of the  <code>writeInt</code>
     * method.  The bytes written by this method
     * may be read by the <code>readFloat</code>
     * method of interface <code>DataInput</code>,
     * which will then return a <code>float</code>
     * equal to <code>v</code>.
     *
     * @param      v   the <code>float</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeFloat(String sName, float v) throws IOException {
        writeFloat(v);
    }
    /**
     * Writes a <code>char</code> value, wich
     * is comprised of two bytes, to the
     * output stream.
     * The byte values to be written, in the  order
     * shown, are:
     * <p><pre><code>
     * (byte)(0xff &amp; (v &gt;&gt; 8))
     * (byte)(0xff &amp; v)
     * </code></pre><p>
     * The bytes written by this method may be
     * read by the <code>readChar</code> method
     * of interface <code>DataInput</code> , which
     * will then return a <code>char</code> equal
     * to <code>(char)v</code>.
     *
     * @param      v   the <code>char</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeChar(String sName, int v) throws IOException {
        writeChar(v);
    }
    /**
     * Writes a string to the output stream.
     * For every character in the string
     * <code>s</code>,  taken in order, one byte
     * is written to the output stream.  If
     * <code>s</code> is <code>null</code>, a <code>NullPointerException</code>
     * is thrown.<p>  If <code>s.length</code>
     * is zero, then no bytes are written. Otherwise,
     * the character <code>s[0]</code> is written
     * first, then <code>s[1]</code>, and so on;
     * the last character written is <code>s[s.length-1]</code>.
     * For each character, one byte is written,
     * the low-order byte, in exactly the manner
     * of the <code>writeByte</code> method . The
     * high-order eight bits of each character
     * in the string are ignored.
     *
     * @param      s   the string of bytes to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeBytes(String sName, String s) throws IOException {
        writeBytes(s);
    }
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
     * @param      v   the <code>long</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeLong(String sName, long v) throws IOException {
        writeLong(v);
    }
    /**
     * Writes two bytes of length information
     * to the output stream, followed
     * by the Java modified UTF representation
     * of  every character in the string <code>s</code>.
     * If <code>s</code> is <code>null</code>,
     * a <code>NullPointerException</code> is thrown.
     * Each character in the string <code>s</code>
     * is converted to a group of one, two, or
     * three bytes, depending on the value of the
     * character.<p>
     * If a character <code>c</code>
     * is in the range <code>&#92;u0001</code> through
     * <code>&#92;u007f</code>, it is represented
     * by one byte:<p>
     * <pre>(byte)c </pre>  <p>
     * If a character <code>c</code> is <code>&#92;u0000</code>
     * or is in the range <code>&#92;u0080</code>
     * through <code>&#92;u07ff</code>, then it is
     * represented by two bytes, to be written
     * in the order shown:<p> <pre><code>
     * (byte)(0xc0 | (0x1f &amp; (c &gt;&gt; 6)))
     * (byte)(0x80 | (0x3f &amp; c))
     *  </code></pre>  <p> If a character
     * <code>c</code> is in the range <code>&#92;u0800</code>
     * through <code>uffff</code>, then it is
     * represented by three bytes, to be written
     * in the order shown:<p> <pre><code>
     * (byte)(0xe0 | (0x0f &amp; (c &gt;&gt; 12)))
     * (byte)(0x80 | (0x3f &amp; (c &gt;&gt;  6)))
     * (byte)(0x80 | (0x3f &amp; c))
     *  </code></pre>  <p> First,
     * the total number of bytes needed to represent
     * all the characters of <code>s</code> is
     * calculated. If this number is larger than
     * <code>65535</code>, then a <code>UTFDataFormatException</code>
     * is thrown. Otherwise, this length is written
     * to the output stream in exactly the manner
     * of the <code>writeShort</code> method;
     * after this, the one-, two-, or three-byte
     * representation of each character in the
     * string <code>s</code> is written.<p>  The
     * bytes written by this method may be read
     * by the <code>readUTF</code> method of interface
     * <code>DataInput</code> , which will then
     * return a <code>String</code> equal to <code>s</code>.
     *
     * @param      str   the string value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeUTF(String sName, String str) throws IOException {
        writeUTF(str);
    }
    /**
     * Writes a <code>double</code> value,
     * which is comprised of eight bytes, to the output stream.
     * It does this as if it first converts this
     * <code>double</code> value to a <code>long</code>
     * in exactly the manner of the <code>Double.doubleToLongBits</code>
     * method  and then writes the <code>long</code>
     * value in exactly the manner of the  <code>writeLong</code>
     * method. The bytes written by this method
     * may be read by the <code>readDouble</code>
     * method of interface <code>DataInput</code>,
     * which will then return a <code>double</code>
     * equal to <code>v</code>.
     *
     * @param      v   the <code>double</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeDouble(String sName, double v) throws IOException {
        writeDouble(v);
    }
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
     * @param      s   the string value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeChars(String sName, String s) throws IOException {
        writeChars(s);
    }

    //-convenience methods
    /**
     * Writes out elements of an int array from a source element
     * to a final element.
     *
     * @param sArrayName name of the int array
     * @param iFirst the first element number to write
     * @param iLast the last element number to write
     * @param iaArray array containing the elements to write
     *
     */
    public void writeIntArrayRange(String sArrayName, int iFirst, int iLast, int[] iaArray) throws IOException {
        for (int ii=iFirst; ii <= iLast; ii++) {
            writeInt(iaArray[ii]);
        }
    }
    /**
     * Writes out elements of an short array from a source element
     * to a final element.
     *
     * @param sArrayName name of the int array
     * @param iFirst the first element number to write
     * @param iLast the last element number to write
     * @param iaArray array containing the elements to write
     *
     */
    public void writeShortArrayRange(String sArrayName, int iFirst, int iLast, short[] iaArray) throws IOException {
        for (int ii=iFirst; ii <= iLast; ii++) {
            writeShort(iaArray[ii]);
        }
    }
    /**
     * Writes out elements of an short array from a source element
     * to a final element as bytes.
     *
     * @param sArrayName name of the int array
     * @param iFirst the first element number to write
     * @param iLast the last element number to write
     * @param iaArray array containing the elements to write
     *
     */
    public void writeShortArrayRangeAsBytes(String sArrayName, int iFirst, int iLast, short[] iaArray) throws IOException {
        for (int ii=iFirst; ii <= iLast; ii++) {
            write((int) iaArray[ii]);
        }
    }
}
