package de.quinscape.automaton.runtime.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public final class Base32
{

    private final static Logger log = LoggerFactory.getLogger(Base32.class);


    public final static char[] BASE32_ALPHABET = "0123456789abcdefghijklmnopqrstuv".toCharArray();

    public static String encode(long value)
    {
        StringBuilder sb = new StringBuilder();
        do
        {
            value = dump32(sb, value);
        } while (value != 0);
        return sb.reverse().toString();
    }


    private static long dump32(StringBuilder sb, long value)
    {
        int lowerBits = (int) (value & 31);
        sb.append(BASE32_ALPHABET[lowerBits]);
        value >>>= 5;
        return value;
    }


    /**
     * Returns a base32 representation of a 128-bit value split into two longs.
     *
     * @param lo        lower 64 bits
     * @param hi        upper 64 bits
     * @return  base32 string
     */
    public static String uuid(long lo, long hi)
    {
        StringBuilder sb = new StringBuilder(26);

        long value = lo;
        for (int i=0; i < 12; i++)
        {
            value = dump32(sb, value);
        }

        // upper 4 bits of lo (unsigned) and lower bit of hi
        //noinspection NumericOverflow
        final int carry = (int) (((hi & 1) << 4) + (lo >>> 60));

        sb.append(BASE32_ALPHABET[carry]);

        value = hi >>> 1;
        for (int i=0; i < 13; i++)
        {
            value = dump32(sb, value);
        }
        return sb.reverse().toString();
    }


    public static String uuid()
    {
        final UUID uuid = UUID.randomUUID();
        return uuid(uuid.getLeastSignificantBits(), uuid.getMostSignificantBits());
    }
}



