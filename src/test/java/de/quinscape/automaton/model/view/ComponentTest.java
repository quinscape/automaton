package de.quinscape.automaton.model.view;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;
import org.svenson.SvensonRuntimeException;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

public class ComponentTest
{
    private final static Logger log = LoggerFactory.getLogger(ComponentTest.class);


    private final JSONParser parser = JSONUtil.DEFAULT_PARSER;

    @Test
    public void testComponentParsing()
    {
        final Component component = parser.parse(Component.class, "{\n" +
            "    \"name\" : \"Foo\",\n" +
            "    \"attrs\": {\n" +
            "        \"value\" : \"\\\"bar\\\"\"\n" +
            "    },\n" +
            "    \"kids\": [\n" +
            "        {\n" +
            "            \"name\" : \"Baz\",\n" +
            "            \"attrs\": {\n" +
            "                \"value\" : \"\\\"bar2\\\"\"\n" +
            "            }\n" +
            "        }        \n" +
            "    ]\n" +
            "}");

        log.info(component.toString());

        assertThat(component.getName(), is("Foo"));
        assertThat(component.getAttrs().get("value"), is("\"bar\""));
        assertThat(component.getKids().get(0).getName(), is("Baz"));
        assertThat(component.getKids().get(0).getAttrs().get("value"), is("\"bar2\""));
    }


    @Test
    public void testRenderPropParsing()
    {
        final Component component = parser.parse(Component.class, "{\n" +
            "    \"name\": \"Qux\",\n" +
            "    \"attrs\": {\n" +
            "        \"renderFn\": {\n" +
            "            \"context\": \"context\",\n" +
            "            \"declarations\": [\n" +
            "                {\n" +
            "                    \"name\": \"tmp\",\n" +
            "                    \"code\": \"scope.foo - 12\"\n" +
            "                }\n" +
            "            ],\n" +
            "            \"root\": {\n" +
            "                \"name\": \"ContextComp\"\n" +
            "            }\n" +
            "        }\n" +
            "    }\n" +
            "}");

        log.info(component.toString());

        assertThat(component.getName(), is("Qux"));
        final RenderFunction renderFunction = (RenderFunction) component.getAttrs().get("renderFn");
        assertThat(renderFunction, is(notNullValue()));
        assertThat(renderFunction.getContext(), is("context"));
        assertThat(renderFunction.getDeclarations().get(0).getName(), is("tmp"));
        assertThat(renderFunction.getDeclarations().get(0).getCode(), is("scope.foo - 12"));

    }

    @Test
    public void testRenderKidsFn()
    {
        final Component component = parser.parse(Component.class, "{\n" +
            "    \"name\" : \"Blafasel\",\n" +
            "    \"kidsFn\" : {\n" +
            "        \"context\" : \"formConfig\",\n" +
            "        \"declarations\" : [{\n" +
            "            \"name\": \"formikProps\",\n" +
            "            \"code\": \"formConfig.formikProps\"\n" +
            "        }],\n" +
            "        \"root\": {\n" +
            "            \"name\" : \"ContextComp\"\n" +
            "        }\n" +
            "    }\n" +
            "}");
        log.info(component.toString());

        assertThat(component.getName(), is("Blafasel"));
        final RenderFunction renderFunction = component.getKidsFn();
        assertThat(renderFunction, is(notNullValue()));
        assertThat(renderFunction.getContext(), is("formConfig"));
        assertThat(renderFunction.getDeclarations().get(0).getName(), is("formikProps"));
        assertThat(renderFunction.getDeclarations().get(0).getCode(), is("formConfig.formikProps"));

    }

    @Test(expected = SvensonRuntimeException.class)
    public void testRenderKidsFnAndKids()
    {
        // kids and kidsFn together -> BOOM
        
        parser.parse(Component.class, "{\n" +
            "    \"name\" : \"Blafasel\",\n" +
            "    \"kidsFn\" : {\n" +
            "        \"context\" : \"formConfig\",\n" +
            "        \"declarations\" : [{\n" +
            "            \"name\": \"formikProps\",\n" +
            "            \"code\": \"formConfig.formikProps\"\n" +
            "        }],\n" +
            "        \"root\": {\n" +
            "            \"name\" : \"ContextComp\"\n" +
            "        }\n" +
            "    },\n" +
            "    \"kids\" : []\n" +
            "}");
    }
}
