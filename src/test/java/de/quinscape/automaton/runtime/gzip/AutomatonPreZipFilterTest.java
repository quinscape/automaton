package de.quinscape.automaton.runtime.gzip;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.io.File;

import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.Matchers.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AutomatonPreZipFilterTest
{
    private final static Logger log = LoggerFactory.getLogger(AutomatonPreZipFilterTest.class);


    @ParameterizedTest
    @ValueSource(booleans = { true, false})
    void testFileCompression(boolean clientSupportsGzip) throws Exception
    {
        final AutomatonPreZipFilter preZipFilter = new AutomatonPreZipFilter("");
        final MockMvc mvc = MockMvcBuilders.standaloneSetup(
            new TestResourceController()
        )
            .addFilter( preZipFilter, "/de/*")
        .build();

        mvc.perform(
            get("/de/quinscape/automaton/runtime/gzip/test-resource.js")
                .header(HttpHeaders.ACCEPT_ENCODING,clientSupportsGzip ? "gzip, deflate, br" : "identity")

            )
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_ENCODING, clientSupportsGzip ? "gzip" : null))
            .andExpect(result -> {
                String source;
                if (clientSupportsGzip)
                {
                    source = AutomatonCompressionFilterTest.decompress(result.getResponse()
                        .getContentAsByteArray());
                }
                else
                {
                    source = result.getResponse().getContentAsString();
                }
                assertThat(source, containsString("window.str = `Lorem ipsum dolor sit amet"));
                assertThat(source.length(), is(2341));

            })
        ;

    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false})
    void testFileCompressionWithSendFile(boolean clientSupportsGzip) throws Exception
    {
        final AutomatonPreZipFilter preZipFilter = new AutomatonPreZipFilter("");
        final MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new TestResourceController()
            )
            .addFilter( preZipFilter, "/de/*")
            .build();

        mvc.perform(
                get("/de/quinscape/automaton/runtime/gzip/test-resource.js")
                    .header(HttpHeaders.ACCEPT_ENCODING,clientSupportsGzip ? "gzip, deflate, br" : "identity")
                    .with(req -> {
                        req.setAttribute(AutomatonPreZipFilter.SENDFILE_SUPPORT, Boolean.TRUE);
                        return req;
                    })

            )
            .andExpect(status().isOk())
            .andExpect(header().string(HttpHeaders.CONTENT_ENCODING, clientSupportsGzip ? "gzip" : null))
            .andExpect(result -> {
                String source;
                if (clientSupportsGzip)
                {
                    final MockHttpServletRequest req = result.getRequest();

                    final String fileName = (String) req.getAttribute(AutomatonPreZipFilter.SENDFILE_FILENAME);

                    assertThat(fileName,is(notNullValue()));
                    byte[] data = FileUtils.readFileToByteArray(new File(fileName));

                    final Long start = (Long) req.getAttribute(AutomatonPreZipFilter.SENDFILE_START);
                    final Long end = (Long) req.getAttribute(AutomatonPreZipFilter.SENDFILE_END);
                    assertThat(start,is(0L));
                    assertThat(end,is((long)data.length));

                    source = AutomatonCompressionFilterTest.decompress(data);
                }
                else
                {
                    // response is not influenced by filter
                    source = result.getResponse().getContentAsString();
                }
                assertThat(source, containsString("window.str = `Lorem ipsum dolor sit amet"));
                assertThat(source.length(), is(2341));
            })
        ;

    }

    @Test
    void testError() throws Exception
    {
        final AutomatonPreZipFilter preZipFilter = new AutomatonPreZipFilter("");
        final MockMvc mvc = MockMvcBuilders.standaloneSetup(
                new TestResourceController()
            )
            .addFilter( preZipFilter, "/de/*")
            .build();

        mvc.perform(
                get("/de/quinscape/automaton/runtime/gzip/fake.js")
                    .header(HttpHeaders.ACCEPT_ENCODING, "gzip, deflate, br")

            )
            .andExpect(header().string(HttpHeaders.CONTENT_ENCODING, (String)null))
            .andExpect(status().isNotFound())
        ;

    }
}
