package de.quinscape.automaton.runtime.config;

import de.quinscape.automaton.model.domain.DecimalPrecision;
import de.quinscape.domainql.DomainQL;
import de.quinscape.domainql.OutputType;
import de.quinscape.domainql.meta.DomainQLMeta;
import de.quinscape.domainql.meta.DomainQLTypeMeta;
import de.quinscape.domainql.meta.MetadataProvider;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.info.JSONClassInfo;
import org.svenson.info.JSONPropertyInfo;

import javax.persistence.Column;
import javax.validation.constraints.Size;
import java.math.BigDecimal;
import java.math.BigInteger;

/**
 * Provides the extended automaton domain configuration 
 */
public class AutomatonMetadataProvider
    implements MetadataProvider
{
    private final static String DECIMAL_PRECISION = "decimalPrecision";
    private final static String MAX_LENGTH = "maxLength";

    @Override
    public void provideMetaData(DomainQL domainQL, DomainQLMeta meta)
    {
        for (OutputType outputType : domainQL.getTypeRegistry().getOutputTypes())
        {
            final Class<?> pojoType = outputType.getJavaType();

            if (Enum.class.isAssignableFrom(pojoType))
            {
                continue;
            }

            final String typeName = outputType.getName();

            final DomainQLTypeMeta typeMeta = meta.getTypeMeta(typeName);


            final JSONClassInfo classInfo = JSONUtil.getClassInfo(pojoType);

            for (JSONPropertyInfo propertyInfo : classInfo.getPropertyInfos())
            {
                final Class<Object> type = propertyInfo.getType();
                if (propertyInfo.isReadable())
                {
                    if (type.equals(BigDecimal.class) || type.equals(BigInteger.class))
                    {
                        final Column columnAnno = JSONUtil.findAnnotation(propertyInfo, Column.class);
                        if (columnAnno != null)
                        {
                            typeMeta.setFieldMeta(propertyInfo.getJsonName(), DECIMAL_PRECISION,
                                new DecimalPrecision(
                                    columnAnno.precision(),
                                    columnAnno.scale()
                                )
                            );
                        }
                    }
                    else if (type.equals(String.class))
                    {
                        final Size sizeAnno = JSONUtil.findAnnotation(propertyInfo, Size.class);
                        if (sizeAnno != null)
                        {
                            typeMeta.setFieldMeta(propertyInfo.getJsonName(), MAX_LENGTH, sizeAnno.max());
                        }
                    }
                }
            }
        }
    }
}
