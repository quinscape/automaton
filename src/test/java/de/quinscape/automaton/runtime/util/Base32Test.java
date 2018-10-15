package de.quinscape.automaton.runtime.util;


import org.junit.Test;

import java.util.regex.Pattern;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class Base32Test
{
    private final Pattern BASE32 = Pattern.compile("^[0-7][0-9a-v]{25}$");


    @Test
    public void testBase32() throws Exception
    {
        assertThat(Base32.BASE32_ALPHABET.length, is(32));

        for (long l = 0; l < 10; l++)
        {
            assertThat(Base32.encode(l), is(String.valueOf((char) ('0' + (char) l))));
        }

        for (long l = 0; l < 22; l++)
        {
            assertThat(Base32.encode(10 + l), is(String.valueOf((char) ('a' + (char) l))));
        }

        assertThat(Base32.encode((2 << 15) + (1 << 10) + (27 << 5) + 4), is("21r4"));

        assertThat(
            Base32.uuid(
                0L,
                0L
            )
            , is("00000000000000000000000000"));
        assertThat(
            Base32.uuid(
                -1L,
                -1L
            )
            // we run out of bits after 25 * 5 = 125 bits, leaving only 3 bits => 7
            , is("7vvvvvvvvvvvvvvvvvvvvvvvvv"));

        // alternating in groups of 5 bits
        assertThat(
            Base32.uuid(
                0b0000111110000011111000001111100000111110000011111000001111100000L,
                0b1110000011111000001111100000111110000011111000001111100000111110L
            )
            , is("70v0v0v0v0v0v0v0v0v0v0v0v0"));

        // only carry-over bits set
        assertThat(
            Base32.uuid(
                0xf000000000000000L,
                1
            )
            , is("0000000000000v000000000000"));


        for (int i = 0; i < 1000; i++)
        {
            assertThat(BASE32.matcher(Base32.uuid()).matches(), is(true));
        }
    }

}
