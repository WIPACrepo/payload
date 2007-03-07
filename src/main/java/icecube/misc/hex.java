package icecube.misc;
/**
 *  A Utility Class for printing values in hex.
 */
public class hex {
    // Converts a string of hex digits into a byte array of those digits
    static public byte[] toByteArr(String no)
        {
        byte[] number = new byte[no.length()/2];
        int i;
        for (i=0; i<no.length(); i+=2)
            {
            int j = Integer.parseInt(no.substring(i,i+2), 16);
            number[i/2] = (byte)(j & 0x000000ff);
            }
        return number;
        }

    static public void printHex(byte[] b)   {printHex(b, b.length);}

    static public void printHex(short[] b)  {printHex(b, b.length);}

    static public void printHex(int[] b)    {printHex(b, b.length);}


    static public void printHex(String label, byte[] b)  {printHex(label, b, b.length);}

    static public void printHex(String label, short[] b) {printHex(label, b, b.length);}

    static public void printHex(String label, int[] b)   {printHex(label, b, b.length);}


    static public String toHexF(String label, byte[] b)  {return toHexF(label, b, b.length);}

    static public String toHexF(String label, short[] b) {return toHexF(label, b, b.length);}

    static public String toHexF(String label, int[] b)   {return toHexF(label, b, b.length);}


    static public String toHexF(int[] b)   {return toHexF(b, b.length);}

    static public String toHexF(short[] b) {return toHexF(b, b.length);}
    static public String toShortF(short[] b) {return toShortF(b, b.length);}

    static public String toHexF(byte[] b)  {return toHexF(b, b.length);}


    static public String toHex(byte[] b)  {return toHex(b, b.length);}

    static public String toHex(short[] b) {return toHex(b, b.length);}

    static public String toHex(int[] b)   {return toHex(b, b.length);}
    static public void printHex(String label, byte[] b, int len)
        {
        System.out.println(label);
        printHex(b, len);
        }

    static public void printHex(String label, short[] b, int len)
        {
        System.out.println(label);
        printHex(b, len);
        }

    static public void printHex(String label, int[] b, int len)
        {
        System.out.println(label);
        printHex(b, len);
        }


    static public void printHex(byte[] b, int len)   {System.out.print(toHexF(b, len));}

    static public void printHex(short[] b, int len)  {System.out.print(toHexF(b, len));}

    static public void printHex(int[] b, int len)    {System.out.print(toHexF(b, len));}


    static public String toHexF(String label, int[] b, int len)
        {
        return label + "\n" + toHexF(b, len);
        }

    static public String toHexF(String label, short[] b, int len)
        {
        return label + "\n" + toHexF(b, len);
        }

    static public String toHexF(String label, byte[] b, int len)
        {
        return label + "\n" + toHexF(b, len);
        }

    static public String toHexF(byte[] b, int len)
        {
        return toHexF(b,0,len);
        }

    static public String toHexF(byte[] b, int start, int len)
        {
        StringBuffer s = new StringBuffer("");
        int i;

        if (b==null) return "<null>";

        for (i=start; i<len; i++)
            {
            //s.append(" " + toHex(b[i]));
            s.append(toHex(b[i]));
            if      (i%16 == 15) s.append("\n");
            //else if (i% 8 ==  7) s.append(" ");
            // else if (i% 4 ==  3) s.append(" ");
            else if (i% 2 ==  1) s.append(" ");
            }
        if (i%16 != 0) s.append("\n");

        return s.toString();
        }

    static public String toHexF(short[] b, int len)
        {
        StringBuffer s = new StringBuffer("");
        int i;

        if (b==null) return "<null>";

        for (i=0; i<len; i++)
            {
            s.append(" " + toHex(b[i]));
            if      (i%16 ==  7) s.append("\n");
            else if (i% 4 ==  3) s.append(" ");
            }
        if (i%8 != 0) s.append("\n");

        return s.toString();
        }

    static public String toShortF(short[] b, int len)
        {
        StringBuffer s = new StringBuffer("");
        int i;

        if (b==null) return "<null>";

        for (i=0; i<len; i++) {
            s.append(" " + b[i]);
            if ( i%10 == 9 || i==9 ) s.append("\n");
        }
        return s.toString();
        }

    static public String toHexF(int[] b, int len)
        {
        StringBuffer s = new StringBuffer("");
        int i;

        if (b==null) return "<null>";

        for (i=0; i<len; i++)
            {
            s.append(" " + toHex(b[i]));
            if (i%4 == 3) s.append("\n");
            }
        if (i%4 != 0) s.append("\n");
        return s.toString();
        }


    static public String toHex(int[] b, int len)
        {
        if (b==null) return "";
        StringBuffer s = new StringBuffer("");
        int i;
        for (i=0; i<len; i++)
            s.append(toHex(b[i]));
        return s.toString();
        }

    static public String toHex(short[] b, int len)
        {
        if (b==null) return "";
        StringBuffer s = new StringBuffer("");
        int i;
        for (i=0; i<len; i++)
            s.append(toHex(b[i]));
        return s.toString();
        }

    static public String toHex(byte[] b, int start, int len)
        {
        if (b==null) return "";
        StringBuffer s = new StringBuffer("");
        int i;
        for (i=start; i<(start + len); i++)
            s.append(toHex(b[i]));
        return s.toString();
        }

    static public String toHex(byte[] b, int len)
        {
        if (b==null) return "";
        StringBuffer s = new StringBuffer("");
        int i;
        for (i=0; i<len; i++)
            s.append(toHex(b[i]));
        return s.toString();
        }


    static public String toHex(byte b)
        {
        Integer I = new Integer((((int)b) << 24) >>> 24);
        int i = I.intValue();

        if ( i < (byte)16 )
            return "0"+Integer.toString(i, 16);
        else
            return     Integer.toString(i, 16);
        }

    static public String toHex(short i)
        {
        byte b[] = new byte[2];
        b[0] = (byte)((i & 0xff00) >>>  8);
        b[1] = (byte)((i & 0x00ff)       );

        return toHex(b[0])+toHex(b[1]);
        }

    static public String toHex(int i)
        {
        byte b[] = new byte[4];
        b[0] = (byte)((i & 0xff000000) >>> 24);
        b[1] = (byte)((i & 0x00ff0000) >>> 16);
        b[2] = (byte)((i & 0x0000ff00) >>>  8);
        b[3] = (byte)((i & 0x000000ff)       );

        return toHex(b[0])+toHex(b[1])+toHex(b[2])+toHex(b[3]);
        }
    static public String toHex(long l)
        {
        byte b[] = new byte[8];
        b[0] = (byte)((l & 0xff00000000000000L) >>> 56);
        b[1] = (byte)((l & 0x00ff000000000000L) >>> 48);
        b[2] = (byte)((l & 0x0000ff0000000000L) >>> 40);
        b[3] = (byte)((l & 0x000000ff00000000L) >>> 32);
        b[4] = (byte)((l & 0x00000000ff000000L) >>> 24);
        b[5] = (byte)((l & 0x0000000000ff0000L) >>> 16);
        b[6] = (byte)((l & 0x000000000000ff00L) >>>  8);
        b[7] = (byte)((l & 0x00000000000000ffL)       );

        return toHex(b[0])+toHex(b[1])+toHex(b[2])+toHex(b[3])+
            toHex(b[4])+toHex(b[5])+toHex(b[6])+toHex(b[7]);
        }
}
