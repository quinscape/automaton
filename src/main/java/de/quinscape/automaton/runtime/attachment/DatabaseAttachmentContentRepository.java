package de.quinscape.automaton.runtime.attachment;

import de.quinscape.domainql.DomainQL;
import org.jooq.DSLContext;
import org.jooq.Field;
import org.jooq.InsertQuery;
import org.jooq.Table;

import java.util.UUID;

/**
 * Attachment content repository storing attachment data in app_attachment_data.
 */
public class DatabaseAttachmentContentRepository
    implements AttachmentContentRepository
{
    protected final DSLContext dslContext;

    protected final DomainQL domainQL;

    protected final Table<?> table;

    protected final Field<String> idField;

    protected final Field<String> attachmentIdField;

    protected final Field<byte[]> dataField;

    public DatabaseAttachmentContentRepository(
        DSLContext dslContext,
        DomainQL domainQL
    )
    {
        this.dslContext = dslContext;
        this.domainQL = domainQL;


        this.table = domainQL.getJooqTable("AppAttachmentData");
        this.idField = (Field<String>) domainQL.lookupField("AppAttachmentData", "id");
        this.attachmentIdField = (Field<String>) domainQL.lookupField("AppAttachmentData", "attachmentId");
        this.dataField = (Field<byte[]>) domainQL.lookupField("AppAttachmentData", "data");
    }

    @Override
    public void store(String attachmentId, byte[] data)
    {

        final InsertQuery<?> query = dslContext.insertQuery(table);

        query.addValue(idField, UUID.randomUUID().toString());
        query.addValue(attachmentIdField, attachmentId);
        query.addValue(dataField, data);
        query.onDuplicateKeyUpdate(true);
        query.addValueForUpdate(attachmentIdField, attachmentId);
        query.addValueForUpdate(dataField, data);
        query.execute();
    }


    @Override
    public byte[] read(String attachmentId)
    {
        return dslContext.select(
            dataField
        ).from(table).where(
            attachmentIdField.eq(attachmentId)
        ).fetchOne( r -> r.get(dataField));
    }


    @Override
    public void delete(String attachmentId)
    {
        dslContext.deleteFrom(table).where(
            attachmentIdField.eq(attachmentId)
        ).execute();
    }
}
