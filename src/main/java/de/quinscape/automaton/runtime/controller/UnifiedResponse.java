package de.quinscape.automaton.runtime.controller;

import de.quinscape.spring.jsview.util.JSONUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.svenson.JSON;
import org.svenson.util.JSONBuilder;

/**
 * Contains a few helpers to produce responses for the unified response format
 */
public class UnifiedResponse
{
    public static final ResponseEntity<String> OK = successJSON("true");

    private UnifiedResponse()
    {
        // no instances
    }


    /**
     * Produces a response entity with HTTP 200 Ok and the given payload
     * @param payload   Object to be converted to JSON
     * @return  response entity
     * @param <T> any jsonable type
     */
    public static <T> ResponseEntity<String> success(T payload)
    {
        return successJSON(JSONUtil.DEFAULT_GENERATOR.forValue(payload));
    }
    /**
     * Produces a response entity with HTTP 200 Ok and the given payload JSON
     * @param payloadJSON   payload JSON string
     * @return response entity
     */
    public static ResponseEntity<String> successJSON(String payloadJSON)
    {
        final JSON gen = JSONUtil.DEFAULT_GENERATOR;
        return new ResponseEntity<>(
            JSONBuilder.buildObject(gen)
                .includeProperty("data", payloadJSON)
            .output(),
            HttpStatus.OK
        );
    }


    /**
     * Returns an error response.
     *
     * @param httpStatus    HTTP error status to use
     * @param message       varargs of error messages
     * @return error response entity
     */
    public static ResponseEntity<String> errors(HttpStatus httpStatus, String... message)
    {
        final JSONBuilder b = JSONBuilder.buildObject()
            .arrayProperty("errors");

        for (String msg : message)
        {
            b.objectElement()
                .property("message", msg)
                .arrayProperty("path").close()
                .close();
        }

        return new ResponseEntity<>(
            b.output(),
            httpStatus
        );
    }
}
