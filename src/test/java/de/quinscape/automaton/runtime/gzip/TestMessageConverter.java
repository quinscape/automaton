package de.quinscape.automaton.runtime.gzip;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TestMessageConverter
    implements HttpMessageConverter<Object>
{
    private final static Logger log = LoggerFactory.getLogger(TestMessageConverter.class);


    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType)
    {
        return true;
    }


    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType)
    {
        return true;
    }


    @Override
    public List<MediaType> getSupportedMediaTypes()
    {
        return Collections.singletonList(MediaType.APPLICATION_JSON);
    }


    @Override
    public Object read(
        Class<?> clazz,
        HttpInputMessage inputMessage
    ) throws IOException, HttpMessageNotReadableException
    {

        final String json = IOUtils.toString(inputMessage.getBody(), StandardCharsets.UTF_8);

        return JSONUtil.DEFAULT_PARSER.parse(Map.class, json);
    }


    @Override
    public void write(
        Object map,
        MediaType contentType,
        HttpOutputMessage outputMessage
    ) throws IOException, HttpMessageNotWritableException
    {
        if (map instanceof String)
        {
            IOUtils.write((String)map, outputMessage.getBody(), StandardCharsets.UTF_8);
        }
        else {
            final String json = JSONUtil.forValue(map);
            IOUtils.write(json, outputMessage.getBody(), StandardCharsets.UTF_8);
        }

    }
}
