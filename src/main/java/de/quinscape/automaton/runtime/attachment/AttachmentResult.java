package de.quinscape.automaton.runtime.attachment;

public final class AttachmentResult
{
    private final byte[] data;

    private final String url;


    public AttachmentResult(byte[] data)
    {
        this.data = data;
        this.url = null;
    }

    public AttachmentResult(String url)
    {
        this.data = null;
        this.url = url;
    }


    public byte[] getData()
    {
        return data;
    }


    public String getUrl()
    {
        return url;
    }
}
