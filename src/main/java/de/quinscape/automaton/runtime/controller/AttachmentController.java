package de.quinscape.automaton.runtime.controller;

import de.quinscape.automaton.model.attachment.Attachment;
import de.quinscape.automaton.runtime.attachment.AttachmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;

@Controller
public class AttachmentController
{

    private final static Logger log = LoggerFactory.getLogger(AttachmentController.class);

    public static final ResponseEntity<String> OK = new ResponseEntity<>("{\"ok\":true}", HttpStatus.OK);

    private static final String FALLBACK_TYPE = "application/octet-stream";

    private final AttachmentRepository attachmentRepository;

    public AttachmentController(
        @Autowired(required = false) AttachmentRepository attachmentRepository
    )
    {
        log.debug("attachmentRepository = {}", attachmentRepository);

        this.attachmentRepository = attachmentRepository;
    }


    @RequestMapping(value = "/_auto/upload-attachment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> uploadAttachment(
        HttpServletResponse response,
        @RequestBody(required = false) byte[] data,
        @RequestParam("attachmentId") String attachmentId,
        @RequestParam("description") String description,
        @RequestParam("type")String type,
        @RequestParam(value = "url", required = false) String url
    ) throws IOException
    {
        final boolean haveUrl = StringUtils.hasText(url);

        if (data == null && !haveUrl)
        {
            throw new AttachmentException("Invalid attachment: Needs either data or url");
        }

        if (data != null && haveUrl)
        {
            throw new AttachmentException("Invalid attachment: cannot have both data and url");
        }

        // sometimes the file input fails to recognize the type
        if (!StringUtils.hasText(type))
        {
            // give java a chance to guess
            if (data != null)
            {
                InputStream is = new ByteArrayInputStream(data);
                type = URLConnection.guessContentTypeFromStream(is);
            }

            if (!StringUtils.hasText(type))
            {
                type = FALLBACK_TYPE;
            }
        }

        ensureRepositoryInjected();

        attachmentRepository.store(
            new Attachment(attachmentId, description, type, haveUrl ? url : null),
            data
        );

        return OK;
    }


    @RequestMapping(value = "/_auto/remove-attachment", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> removeAttachment(
        @RequestParam("attachmentId") String attachmentId
    ) throws IOException
    {
        ensureRepositoryInjected();

        attachmentRepository.delete(attachmentId);
        return OK;
    }


    private void ensureRepositoryInjected() throws AttachmentNotFoundException
    {
        if (attachmentRepository == null)
        {
            final String message = "No attachment repository configured";
            log.warn(message);
            throw new AttachmentNotFoundException(message);
        }
    }


    @RequestMapping(value = "/_auto/attachment/{attachmentId}", method = RequestMethod.GET, produces = MediaType.ALL_VALUE)
    public void sendAttachment(
        HttpServletResponse response,
        @PathVariable("attachmentId") String attachmentId
    ) throws IOException
    {
        ensureRepositoryInjected();

        final Attachment attachment = attachmentRepository.getAttachment(attachmentId);

        final String url = attachment.getUrl();
        if (url != null)
        {
            response.sendRedirect(url);
        }
        else
        {
            if (StringUtils.hasText(attachment.getDescription()))
            {
                response.setHeader("Content-Disposition", "attachment; filename=\"" + attachment.getDescription() + "\"");
            }

            final byte[] data = attachmentRepository.getContentRepository().read(attachmentId);
            if (data != null)
            {
                response.setContentType(attachment.getType());
                response.setContentLength(data.length);
                response.getOutputStream().write(data);
            }
            else
            {
                response.sendError(HttpServletResponse.SC_NO_CONTENT);

                log.warn("Requested attachment {} has no content.", attachmentId);
            }
        }
    }
}
