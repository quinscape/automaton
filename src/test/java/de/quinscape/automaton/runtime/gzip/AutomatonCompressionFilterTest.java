package de.quinscape.automaton.runtime.gzip;

import de.quinscape.automaton.runtime.controller.GraphQLController;
import de.quinscape.domainql.DomainQL;
import de.quinscape.spring.jsview.util.JSONUtil;
import graphql.GraphQL;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.svenson.util.JSONPathUtil;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AutomatonCompressionFilterTest
{

    private final static Logger log = LoggerFactory.getLogger(AutomatonCompressionFilterTest.class);

    private final JSONPathUtil pathUtil = new JSONPathUtil();

    @ParameterizedTest
    @ValueSource(booleans = { true, false})
    void testGraphQLCompression(boolean clientSupportsGzip) throws Exception
    {
        final AutomatonCompressionFilter filter = new AutomatonCompressionFilter();

        final DomainQL domainQL = setupDomainQL();
        final MockMvc mockMvc =
            MockMvcBuilders.standaloneSetup(
                new GraphQLController(
                    GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build()
                )
            )
            .addFilter(filter, GraphQLController.GRAPHQL_URI)
            .setMessageConverters( new TestMessageConverter())
            .defaultRequest(get("/").contextPath(""))
            .build();


        mockMvc.perform(
            post(
                    GraphQLController.GRAPHQL_URI
                )
                .header(HttpHeaders.ACCEPT_ENCODING,clientSupportsGzip ? "gzip, deflate, br" : "identity")
                .contentType(MediaType.APPLICATION_JSON)
                .characterEncoding("UTF-8")
                .content(
                    // language=JSON
                    "{\"query\": \"query gzipPayload { gzipPayload { value } }\"}"
                )
        )
            //.andDo(print())
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_ENCODING, clientSupportsGzip ? "gzip" : null))
            .andExpect(result -> {

                final String json = clientSupportsGzip ?
                    decompress(result.getResponse().getContentAsByteArray()) :
                    result.getResponse().getContentAsString();
                Map m = JSONUtil.DEFAULT_PARSER.parse(Map.class, json);
                assertThat(pathUtil.getPropertyPath(m, "data.gzipPayload.value"),is(GzipTestLogic.TEST_VALUE));
            })
        ;
    }

    @Test
    void testError() throws Exception
    {
        final AutomatonCompressionFilter filter = new AutomatonCompressionFilter();

        final DomainQL domainQL = setupDomainQL();
        final MockMvc mockMvc =
            MockMvcBuilders.standaloneSetup(
                    new GraphQLController(
                        GraphQL.newGraphQL(domainQL.getGraphQLSchema()).build()
                    )
                )
                .addFilter(filter, GraphQLController.GRAPHQL_URI)
                .setMessageConverters( new TestMessageConverter())
                .defaultRequest(get("/").contextPath(""))
                .build();


        mockMvc.perform(
                post(
                    "/fake"
                )
                    .header(HttpHeaders.ACCEPT_ENCODING,"gzip, deflate, br")
                    .contentType(MediaType.APPLICATION_JSON)
                    .characterEncoding("UTF-8")
                    .content(
                        // language=JSON
                        "{\"query\": \"query gzipPayload { gzipPayload { value } }\"}"
                    )
            )
            //.andDo(print())
            .andExpect(header().string(HttpHeaders.CONTENT_ENCODING, (String)null))
            .andExpect(status().isNotFound())
        ;
    }


    public static String decompress(byte[] data) throws IOException
    {
        try (
            GZIPInputStream in = new GZIPInputStream(new ByteArrayInputStream(data))
        )
        {
            return IOUtils.toString(in, StandardCharsets.UTF_8);
        }
    }


    private DomainQL setupDomainQL()
    {
        return DomainQL.newDomainQL(null)
            .logicBeans(
                new GzipTestLogic()
            )
            .build();
    }
}
