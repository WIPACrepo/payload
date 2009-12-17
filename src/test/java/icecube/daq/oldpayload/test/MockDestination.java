package icecube.daq.oldpayload.test;

import icecube.daq.oldpayload.PayloadDestination;

import java.nio.ByteBuffer;

public class MockDestination
    extends PayloadDestination
{
    private static final int CHUNKSIZE = 16;

    private ByteBuffer buf;

    public MockDestination()
    {
        buf = ByteBuffer.allocate(CHUNKSIZE);
    }

    public ByteBuffer getByteBuffer()
    {
        return buf;
    }

    private void expandToFit(int bytes)
    {
        int newBytes = 0;
        while (buf.position() + bytes >= buf.limit() + newBytes) {
            newBytes += CHUNKSIZE;
        }

        if (newBytes > 0) {
            ByteBuffer newBuf = ByteBuffer.allocate(buf.limit() + newBytes);
            buf.flip();
            newBuf.put(buf);

            buf = newBuf;
        }
    }

    public void reset()
    {
        buf.position(0);
        buf.limit(buf.capacity());
    }

    public void write(int v)
    {
        expandToFit(1);

        buf.put((byte) (v & 0xff));
    }

    public void write(byte[] b)
    {
        expandToFit(b.length);

        buf.put(b);
    }

    public void write(int offset, ByteBuffer bb, int bytes)
    {
        expandToFit(bytes);

        for (int i = 0; i < bytes; i++) {
            buf.put(bb.get(offset + i));
        }
    }

    public void writeInt(int v)
    {
        expandToFit(4);

        buf.putInt(v);
    }

    public void writeLong(long v)
    {
        expandToFit(8);

        buf.putLong(v);
    }

    public void writeShort(int v)
    {
        expandToFit(2);

        buf.putShort((short) v);
    }

    public String toString()
    {
        return buf.toString();
    }
}
