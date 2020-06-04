package de.quinscape.automaton.runtime.util;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * Util class to be instantiated with a concrete maximum bit width. The implementation maintains a data-table of
 * a big integer per bit, so the bit width should be moderate / chosen with care.
 *
 * It is a pretty simple wrapper around BigInteger instances. It is more useful to use in that we can encode the current
 * state of a bit mask in a BigInteger which JOOQ/the databases understand.
 *
 * Note that you need to size your NUMERIC columns right.
 *
 * ( NUMERIC(n,0) with n = BigIntegerMath.log10(BigInteger.valueOf(2).pow(bitsize), RoundingMode.CEILING) )
 */
public final class BitMaskingUtil
{
    private final int bitWidth;

    private final int byteSize;

    private final List<BigInteger> masks;

    public BitMaskingUtil(int bitWidth)
    {
        this.bitWidth = bitWidth;

        // byte size we need to represent all our bits.
        // Every byte stores 8 bits and we need a 1 bit additional because of the two complement +/- bit
        // add an additional 7 to turn the natural integer division rounding down into it rounding up.
        this.byteSize = (bitWidth + 1 + 7)/8;
        masks = new ArrayList<>(bitWidth);

        for (int i = 0; i < bitWidth; i++)
        {
            byte[] bits = new byte[byteSize];
            final int index = i / 8;
            final int bit = 1 << (i - (index * 8));

            bits[byteSize - 1 - index] |= bit;

            final BigInteger bigInt = new BigInteger(bits);
            masks.add(
                bigInt
            );
        }
    }

    public BigInteger getMask(int i)
    {
        if (i < 0)
        {
            throw new IllegalArgumentException("Index must be positive: " + i);
        }

        if (i >= bitWidth)
        {
            throw new IllegalArgumentException("Index is larger than the maximum bitwidth " + bitWidth + ": " + i);
        }

        return masks.get(i);
    }


    public int getBitWidth()
    {
        return bitWidth;
    }


    public int getByteSize()
    {
        return byteSize;
    }


    /**
     * Checks if the given big int has the bit with the given index set
     *
     * @param value     big int mask value
     * @param index     bit index
     *
     * @return true if bit is set
     */
    public boolean check(BigInteger value, int index)
    {
        // AND value with the index from our mask index table and return true if that value is not zero, i.e. a bit is set.
        final BigInteger result = value.and(masks.get(index));
        return !result.equals(BigInteger.ZERO);
    }
}
