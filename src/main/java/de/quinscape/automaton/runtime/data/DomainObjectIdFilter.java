package de.quinscape.automaton.runtime.data;

import de.quinscape.domainql.config.RelationModel;
import de.quinscape.domainql.generic.DomainObject;
import de.quinscape.spring.jsview.util.JSONUtil;
import org.svenson.util.JSONBeanUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;

public final class DomainObjectIdFilter
    implements Predicate<DomainObject>
{
    private final RelationModel relationModel;

    private final List<Object> keyValues;


    public DomainObjectIdFilter(DomainObject targetObject, RelationModel relationModel)
    {
        this.relationModel = relationModel;
        final List<String> targetFields = relationModel.getTargetFields();

        keyValues = new ArrayList<>(targetFields.size());
        final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;
        for (String targetField : targetFields)
        {
            keyValues.add(
                util.getProperty(targetObject, targetField)
            );
        }
    }


    @Override
    public boolean test(DomainObject source)
    {
        final List<String> sourceFields = relationModel.getSourceFields();

        final JSONBeanUtil util = JSONUtil.DEFAULT_UTIL;

        for (int i = 0; i < sourceFields.size(); i++)
        {
            final String sourceField = sourceFields.get(i);

            final Object lft = keyValues.get(i);
            final Object rgt = util.getProperty(source, sourceField);

            if (!Objects.equals(lft,rgt))
            {
                return false;
            }
        }
        return true;
    }
}
