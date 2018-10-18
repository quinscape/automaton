package de.quinscape.automaton.model.view;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.svenson.JSONParser;
import org.svenson.SvensonRuntimeException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    @Test
    public void testView()
    {
        final View view = new View();
        view.setName("ExampleView");
        List<ViewDeclaration> decls = new ArrayList<>();
        decls.add(new ViewDeclaration("canAccess", "authentication.id === values.ownerId || hasRole(\"ROLE_ADMIN\")"));

        view.setDeclarations(decls);

        view.setRoot(
            component("React.Fragment", null,
                component("GlobalErrors"),
                component("Field", attrs("name", "\"name\"")),
                component("TextArea", attrs("name", "\"description\"")),
                component("Field", attrs("name", "\"num\"")),
                component("div", null,

                    component("button", attrs(
                        "type", "\"reset\"",
                        "className", "\"btn btn-secondary\""

                        ),
                        component("Icon", attrs(
                            "className", "fa-recycle"
                        )),
                        component("[String]", attrs(
                            "value", "Reset"
                        ))
                    ),
                    component("button", attrs(
                        "type", "\"submit\"",
                        "className", "{\n" +
                            "                            cx(\n" +
                            "                                \"btn\",\n" +
                            "                                canAccess ? \"btn-success\" : \"btn-danger\"\n" +
                            "                            )\n" +
                            "                        }"

                        ),
                        component("Icon", attrs(
                            "className", "fa-save"
                        )),
                        component("[String]", attrs(
                            "value", "Save"
                        ))
                    )
                )
            )

        );

        log.info(JSONUtil.formatJSON(
            JSONUtil.DEFAULT_GENERATOR.forValue(view)
        ));

    }


    private Component component(String name)
    {
        return component(name, null, null);
    }


    public static Component component(String name, Map<String, Object> attrs)
    {
        return component(name, attrs, null);
    }


    public static Component component(String name, Map<String, Object> attrs, Component... kids)
    {
        final Component component = new Component();
        component.setName(name);
        if (attrs != null)
        {
            component.setAttrs(attrs);
        }
        if (kids != null)
        {
            component.setKids(Arrays.asList(kids));

        }
        return component;

    }


    public static Map<String, Object> attrs(String... keyValues)
    {
        final Map<String, Object> map = new HashMap<>();

        for (int i = 0; i < keyValues.length; i += 2)
        {
            String key = keyValues[i];
            String value = keyValues[i + 1];
            map.put(key, value);
        }

        return map;

    }


}
