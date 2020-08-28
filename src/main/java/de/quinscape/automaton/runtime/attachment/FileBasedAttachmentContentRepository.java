package de.quinscape.automaton.runtime.attachment;

import de.quinscape.automaton.runtime.controller.AttachmentException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Attachment content repository storing attachment data in app_attachment_data.
 */
public class FileBasedAttachmentContentRepository
    implements AttachmentContentRepository
{

    private final static Logger log = LoggerFactory.getLogger(FileBasedAttachmentContentRepository.class);


    private final File directory;


    public FileBasedAttachmentContentRepository(
        File directory
    )
    {

        log.info("Creating FileBasedAttachmentContentRepository (directory = {}", directory);

        if (!directory.exists() || !directory.isDirectory())
        {
            throw new AttachmentException(directory + " is not an existing directory.");
        }

        this.directory = directory;
    }

    @Override
    public void store(String attachmentId, byte[] data)
    {
        try
        {
            FileUtils.writeByteArrayToFile(
                getFile(attachmentId),
                data
            );
        }
        catch (IOException e)
        {
            throw new AttachmentException(e);
        }
    }


    private File getFile(String attachmentId)
    {
        return new File(directory, attachmentId);
    }


    @Override
    public byte[] read(String attachmentId)
    {
        try
        {
            return FileUtils.readFileToByteArray(getFile(attachmentId));
        }
        catch (IOException e)
        {
            throw new AttachmentException(e);
        }
    }


    @Override
    public void delete(String attachmentId)
    {
        final File file = getFile(attachmentId);
        if (!file.delete())
        {
            throw new AttachmentException("Could not delete " + file);
        }
    }
}
