package de.quinscape.automaton.model.domain;

/**
 * Contains the decimal precision for one decimal field within the domain.
 */
public class DecimalPrecision
{

    private int precision;

    private int scale;


    public DecimalPrecision()
    {
        this(0, 0);
    }


    public DecimalPrecision(
        int precision,
        int scale
    )
    {
        this.precision = precision;
        this.scale = scale;
    }


    /**
     * Returns the numeric precision for the field. (123.45 has a scale of 5)
     *
     * @return the numeric precision for the field
     */
    public int getPrecision()
    {
        return precision;
    }


    public void setPrecision(int precision)
    {
        this.precision = precision;
    }


    /**
     * Returns the numeric scale/number of fractional digits for the field
     *
     * @return numeric scale for the field
     */
    public int getScale()
    {
        return scale;
    }


    public void setScale(int scale)
    {
        this.scale = scale;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + " precision = " + precision
            + ", scale = " + scale
            ;
    }
}
