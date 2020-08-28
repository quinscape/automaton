package de.quinscape.automaton.runtime.attachment;

/**
 * Implemented by classes storing binary attachment data
 */
public interface AttachmentContentRepository
{
    void store(String id, byte[] data);

    byte[] read(String id);

    void delete(String id);
}
