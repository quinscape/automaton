package de.quinscape.automaton.runtime.attachment;

import de.quinscape.automaton.model.attachment.Attachment;

/**
 * Repository interface for attachment meta data
 */
public interface AttachmentRepository
{
    Attachment getAttachment(String attachmentId);
    void store(Attachment attachment, byte[] data);
    void delete(String attachmentId);

    AttachmentContentRepository getContentRepository();
}
