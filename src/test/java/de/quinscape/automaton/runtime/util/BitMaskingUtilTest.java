package de.quinscape.automaton.runtime.util;

import com.google.common.math.BigIntegerMath;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.Arrays;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class BitMaskingUtilTest
{
    private final static Logger log = LoggerFactory.getLogger(BitMaskingUtilTest.class);


    @Test
    public void testMasks()
    {
        final BitMaskingUtil util = new BitMaskingUtil(100);
        final BigInteger mask = util.getMask(42);
        assertThat(mask.toString(), is(BigInteger.valueOf(2).pow(42).toString()));
    }


    @Test
    public void testCheck()
    {

        final BitMaskingUtil util = new BitMaskingUtil(100);

        // create big integer mask value indendent from the util
        final BigInteger value =
            // start with random indizes
            Arrays.asList(20, 43, 63, 42, 69)
                .stream()
                // to big integers via pow
                .map(i -> BigInteger.valueOf(2).pow(i))

                // and them or them all together
                .reduce(BigInteger::or)
                .get();


        // counter check
        assertThat(util.check(value, 20), is(true));
        assertThat(util.check(value, 63), is(true));
        assertThat(util.check(value, 42), is(true));
        assertThat(util.check(value, 69), is(true));
        assertThat(util.check(value, 5), is(false));
        assertThat(util.check(value, 7), is(false));
        assertThat(util.check(value, 0), is(false));
        assertThat(util.check(value, 99), is(false));
    }


    private String hexBytes(byte[] byteArray)
    {
        StringBuilder sb = new StringBuilder(byteArray.length * 3 - 1);
        for (int i = 0; i < byteArray.length; i++)
        {
            if (i > 0)
            {
                sb.append(",");
            }
            byte b = byteArray[i];
            final String s = Integer.toHexString(b);

            if (s.length() < 2)
            {
                sb.append("0");
            }
            sb.append(s);
        }

        return sb.toString();
    }


    @Test
    public void name()
    {
        log.info("{}", BigIntegerMath.log10(BigInteger.valueOf(2).pow(256), RoundingMode.CEILING));
    }
}
