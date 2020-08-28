package de.quinscape.automaton.model.attachment;

import java.util.UUID;

/**
 * Automaton-internal attachment class. Used as replacement of the yet unknown AppAttachment type within the
 * application.
 */
public class Attachment
{
    private String id;

    private String description;

    private String type;

    private String url;

    public Attachment()
    {
        this(null, null, null, null);
    }


    public Attachment(Attachment value)
    {
        this(value.id, value.description, value.type, value.url);
    }


    public Attachment(
        String id,
        String description,
        String type,
        String url
    )
    {
        this.id = id;
        this.description = description;
        this.type = type;
        this.url = url;
    }


    public String getId()
    {
        return id;
    }


    public void setId(String id)
    {
        this.id = id;
    }


    public String getDescription()
    {
        return description;
    }


    public void setDescription(String description)
    {
        this.description = description;
    }


    /**
     * Returns the content type / media type of the attachment
     */
    public String getType()
    {
        return type;
    }


    public void setType(String type)
    {
        this.type = type;
    }


    public String getUrl()
    {
        return url;
    }


    public void setUrl(String url)
    {
        this.url = url;
    }


    public static Attachment createNew(
        String description,
        String type,
        String url
    )
    {
        return new Attachment(
            UUID.randomUUID().toString(),
            description,
            type,
            url
        );
    }

    @Override
    public String toString()
    {
        return super.toString() + ": "
            + "id = '" + id + '\''
            + ", description = '" + description + '\''
            + ", type = '" + type + '\''
            + ", url = '" + url + '\''
            ;
    }
}
