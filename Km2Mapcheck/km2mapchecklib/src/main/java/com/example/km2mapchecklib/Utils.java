package com.example.km2mapchecklib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

public class Utils{
    //byte[]转String 并去掉无效字符
    public static String ByteToString(byte[] bytes)
    {
        StringBuilder strBuilder = new StringBuilder();
        for (int i = 0; i <bytes.length ; i++) {
            if (bytes[i]!=0){
                strBuilder.append((char)bytes[i]);
            }else {
                break;
            }
        }
        return strBuilder.toString();
    }

    // byte[]转char[]
    public static char[] getChars (byte[] bytes) {
        Charset cs = Charset.forName ("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate (bytes.length);
        bb.put (bytes);
        bb.flip ();
        CharBuffer cb = cs.decode (bb);

        return cb.array();
    }

    //byte[]转类对象
    public static Object ByteToObject(byte[] bytes)
    {
        Object obj = null;
        try
        {
            ByteArrayInputStream byteArrayInStream = new ByteArrayInputStream(bytes);
            ObjectInputStream objectInStream = new ObjectInputStream(byteArrayInStream);
            obj = objectInStream.readObject();
            byteArrayInStream.close();
            objectInStream.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return obj;
    }

    //类对象转byte[]
    public static byte[] ObjectToByte(Object obj)
    {
        byte[] bytes = null;
        try
        {
            ByteArrayOutputStream byteArrayOutStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(byteArrayOutStream);
            objectOutputStream.writeObject(obj);
            bytes = byteArrayOutStream.toByteArray();
            byteArrayOutStream.close();
            objectOutputStream.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }

        return(bytes);
    }

    //截取指定字节数组
    public static  byte[] subBytes(byte[] src, int begin, int count) {
        byte[] bs = new byte[count];
        for (int i=begin;i<begin+count; i++)
            bs[i-begin] = src[i];

        return bs;
    }

    //byte[]转double
    public static double bytesArrayToDouble(byte[] arr) {
        long value = 0;
        for (int i = 0; i < 8; i++) {
            value |= ((long) (arr[i] & 0xff)) << (8 * i);
        }
        return Double.longBitsToDouble(value);
    }

    //byte[]转int
    public static int byteArrayToInt(byte[] b) {
        return   b[0] & 0xFF |
                (b[1] & 0xFF) << 8 |
                (b[2] & 0xFF) << 16 |
                (b[3] & 0xFF) << 24;
    }

    //int转byte[]
    // 未检查是否有效
    public static byte[] intToByteArray(int a) {
        return new byte[] {
                (byte) ((a >> 24) & 0xFF),
                (byte) ((a >> 16) & 0xFF),
                (byte) ((a >> 8) & 0xFF),
                (byte) (a & 0xFF)
        };
    }

    public static double byteArrayToDouble(byte[] Array, int Pos) {
        long accum = 0;
        accum = Array[Pos + 0] & 0xFF;
        accum |= (long) (Array[Pos + 1] & 0xFF) << 8;
        accum |= (long) (Array[Pos + 2] & 0xFF) << 16;
        accum |= (long) (Array[Pos + 3] & 0xFF) << 24;
        accum |= (long) (Array[Pos + 4] & 0xFF) << 32;
        accum |= (long) (Array[Pos + 5] & 0xFF) << 40;
        accum |= (long) (Array[Pos + 6] & 0xFF) << 48;
        accum |= (long) (Array[Pos + 7] & 0xFF) << 56;
        return Double.longBitsToDouble(accum);
    }
}
