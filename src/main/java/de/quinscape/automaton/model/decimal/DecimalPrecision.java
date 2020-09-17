package de.quinscape.automaton.model.decimal;

/**
 * Contains the decimal precision for one decimal field within the domain.
 */
public class DecimalPrecision
{

    private String domainType;

    private String fieldName;

    private int precision;

    private int scale;

    public DecimalPrecision()
    {
        this(null, null, 0, 0);
    }

    public DecimalPrecision(
        String domainType,
        String fieldName,
        int precision,
        int scale
    )
    {
        this.domainType = domainType;
        this.fieldName = fieldName;
        this.precision = precision;
        this.scale = scale;
    }


    /**
     * Returns the name of the corresponding table
     *
     * @return name of the corresponding table
     */
    public String getDomainType()
    {
        return domainType;
    }


    public void setDomainType(String domainType)
    {
        this.domainType = domainType;
    }


    /**
     * Returns the column name
     *
     * @return column name
     */
    public String getFieldName()
    {
        return fieldName;
    }


    public void setFieldName(String fieldName)
    {
        this.fieldName = fieldName;
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
            + domainType + "." + fieldName + ":"
            + " precision = " + precision
            + ", scale = " + scale
            ;
    }
}
