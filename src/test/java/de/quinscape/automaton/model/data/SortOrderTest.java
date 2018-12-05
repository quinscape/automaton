package de.quinscape.automaton.model.data;

import de.quinscape.automaton.runtime.InvalidSortOrderException;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.*;

public class SortOrderTest
{
    /*
            assertThat(SortOrder.ORDER_BY_RE.matcher("abc").matches(), is(true));
        assertThat(SortOrder.ORDER_BY_RE.matcher("!abc").matches(), is(true));
        assertThat(SortOrder.ORDER_BY_RE.matcher("!ab!c").matches(), is(false));
        assertThat(SortOrder.ORDER_BY_RE.matcher("ö").matches(), is(false));
        assertThat(SortOrder.ORDER_BY_RE.matcher("-").matches(), is(false));

     */


    @Test
    public void testSortOrderCreation()
    {
        final SortOrder sortOrder = new SortOrder(Collections.singletonList("abc"));
        assertThat(sortOrder.getFields().size(),is(1));
        assertThat(sortOrder.getFields().get(0),is("abc"));

        final SortOrder sortOrder2 = new SortOrder(Collections.singletonList("!abc"));
        assertThat(sortOrder2.getFields().size(),is(1));
        assertThat(sortOrder2.getFields().get(0),is("!abc"));
    }


    @Test(expected = InvalidSortOrderException.class)
    public void testError()
    {
        new SortOrder(Collections.singletonList("!ab!c"));
    }

    @Test(expected = InvalidSortOrderException.class)
    public void testError2()
    {
        new SortOrder(Collections.singletonList("ö"));
    }

    @Test(expected = InvalidSortOrderException.class)
    public void testError3()
    {
        new SortOrder(Collections.singletonList("-"));
    }
}
