package de.quinscape.automaton.model.domain;

/**
 * Encapsulates maximum field length information for a single field of an entity.
 */
public class FieldLength
{

    private String domainType;

    private String fieldName;

    private int length;


    public FieldLength(
        String domainType,
        String fieldName,
        int length
    )
    {
        this.domainType = domainType;
        this.fieldName = fieldName;
        this.length = length;
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
     * Returns the field length for the given domain field. Will only be available for String
     * fields / columns.
     *
     * @return maximal field length
     */
    public int getLength()
    {
        return length;
    }


    public void setLength(int length)
    {
        this.length = length;
    }


    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "domainType = '" + domainType + '\''
            + ", fieldName = '" + fieldName + '\''
            + ", length = " + length
            ;
    }
}
