package io.github.shazaanashraff.apiforge.modules.schemaparser;

import java.util.List;
import java.util.Map;
import org.springframework.http.HttpMethod;

/**
 * Normalised representation of a single API endpoint.
 *
 * <p>This is the contract between the schema parser and every downstream module (test generator,
 * executor, reporter). Changes here propagate to all consumers — update carefully.
 *
 * @param path path template, e.g. {@code /pets/{petId}}
 * @param method HTTP method
 * @param operationId unique operation identifier from the spec, or null
 * @param summary short human-readable description
 * @param parameters all path, query, header, and cookie parameters
 * @param requestBody request body schema, or null if the endpoint has no body
 * @param responses declared responses keyed by HTTP status code
 * @param authRequirement detected authentication requirement
 * @param paginationHint detected pagination pattern
 * @param payloadSizeHint detected payload size constraint
 * @param slaHint declared response-time SLA from {@code x-response-time-sla}
 * @param idFormatHint detected ID format for path/query ID parameters
 * @param tags spec tags used to categorise the endpoint
 */
public record Endpoint(
    String path,
    HttpMethod method,
    String operationId,
    String summary,
    List<Parameter> parameters,
    RequestBodySchema requestBody,
    Map<Integer, ResponseSchema> responses,
    AuthRequirement authRequirement,
    PaginationHint paginationHint,
    PayloadSizeHint payloadSizeHint,
    SlaHint slaHint,
    IdFormatHint idFormatHint,
    List<String> tags) {}
