package me.kyrobi;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

public class BufferReaderUtils {
    // Buffer reading functions
    public static boolean readBool(DataInputStream dis) throws IOException{
        return dis.readBoolean();
    }

    public static int readUByte(DataInputStream dis) throws IOException{
        return dis.readUnsignedByte();
    }

    public static int readUShort(DataInputStream dis) throws IOException{
        return Short.reverseBytes(dis.readShort()) & 0xFFFF;
    }

    public static long readUInt(DataInputStream dis) throws IOException{
        return Integer.reverseBytes(dis.readInt()) & 0xFFFFFFFFL;
    }

    public static int readInt(DataInputStream dis) throws IOException{
        return Integer.reverseBytes(dis.readInt());
    }

    public static long readLong(DataInputStream dis) throws IOException{
        return Long.reverseBytes(dis.readLong());
    }

    public static long readULong(DataInputStream dis) throws IOException{
        return Long.reverseBytes(dis.readLong());
    }

    public static String readString(DataInputStream dis) throws IOException {
        int strFlag = readUByte(dis);
        if (strFlag == 0x0b) {
            int strlen = 0;
            int shift = 0;
            while (true) {
                int b = readUByte(dis);
                strlen |= ((b & 0x7F) << shift);
                if ((b & (1 << 7)) == 0) {
                    break;
                }
                shift += 7;
            }

            if (strlen == 0) {
                return "";
            }

            byte[] stringBytes = new byte[strlen];
            dis.readFully(stringBytes);
            return new String(stringBytes, "UTF-8");
        }
        return "";
    }

    public static LocalDateTime readDateTime(DataInputStream dis) throws IOException{
        long ticks = readULong(dis);

        if (ticks == 0) {
            return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        }

        try {
            long unixTimestamp = (ticks / 10000000L) - 11644473600L;

            if (unixTimestamp < 0) {
                return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
            }
            if (unixTimestamp > 253402300799L) {
                return LocalDateTime.of(9999, 12, 31, 23, 59, 59);
            }

            return LocalDateTime.ofInstant(Instant.ofEpochSecond(unixTimestamp), ZoneId.systemDefault());
        } catch (Exception e) {
            return LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        }
    }
}
