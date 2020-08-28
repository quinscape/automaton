package de.quinscape.automaton.runtime.attachment;

import de.quinscape.automaton.model.attachment.Attachment;
import de.quinscape.domainql.DomainQL;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Record;
import org.jooq.Table;

/**
 * Attachment repository handling the attachment storage within app_attachment.
 * <p>
 * Storage is delegated to an {@link AttachmentContentRepository}
 *
 * @see AttachmentContentRepository
 */
public class DefaultAttachmentRepository
    implements AttachmentRepository
{
    protected final DSLContext dslContext;

    protected final DomainQL domainQL;

    protected final Table<?> table;

    protected final Field<String> idField;

    private final AttachmentContentRepository attachmentContentRepository;

    protected final Field<String> descriptionField;

    protected final Field<String> typeField;

    private final Field<String> urlField;


    public DefaultAttachmentRepository(
        DSLContext dslContext,
        DomainQL domainQL,
        AttachmentContentRepository attachmentContentRepository
    )
    {
        this.dslContext = dslContext;
        this.domainQL = domainQL;


        this.table = domainQL.getJooqTable("AppAttachment");
        this.idField = (Field<String>) domainQL.lookupField("AppAttachment", "id");
        this.descriptionField = (Field<String>) domainQL.lookupField("AppAttachment", "description");
        this.typeField = (Field<String>) domainQL.lookupField("AppAttachment", "type");
        this.urlField = (Field<String>) domainQL.lookupField("AppAttachment", "url");
        this.attachmentContentRepository = attachmentContentRepository;
    }


    @Override
    public Attachment getAttachment(String attachmentId)
    {
        return dslContext.select().from(table)
            .where(
                idField.eq(attachmentId)
            )
            .fetchOneInto(Attachment.class);
    }


    @Override
    public void store(Attachment attachment, byte[] data)
    {
        if (data != null && attachment.getUrl() != null)
        {
            throw new IllegalArgumentException("Attachments cannot be URL-based and have binary data");
        }

        final String id = attachment.getId();

        final InsertQuery<?> query = dslContext.insertQuery(table);

        query.addValue(idField, id);
        query.addValue(descriptionField, attachment.getDescription());
        query.addValue(typeField, attachment.getType());
        query.addValue(urlField, attachment.getUrl());
        query.onDuplicateKeyUpdate(true);
        query.addValueForUpdate(descriptionField, attachment.getDescription());
        query.addValueForUpdate(typeField, attachment.getType());
        query.addValueForUpdate(urlField, attachment.getUrl());
        query.execute();

        if (data != null)
        {
            attachmentContentRepository.store(id, data);
        }
    }


    @Override
    public void delete(String attachmentId)
    {
        attachmentContentRepository.delete(attachmentId);

        int count = dslContext.deleteFrom(table)
            .where(
                idField.eq(attachmentId)
            )
            .execute();
    }


    @Override
    public AttachmentContentRepository getContentRepository()
    {
        return attachmentContentRepository;
    }
}
