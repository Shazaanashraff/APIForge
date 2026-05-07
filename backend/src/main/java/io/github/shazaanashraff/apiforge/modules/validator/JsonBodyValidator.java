package io.github.shazaanashraff.apiforge.modules.validator;

import io.github.shazaanashraff.apiforge.modules.schemaparser.Endpoint;
import io.github.shazaanashraff.apiforge.modules.schemaparser.ResponseSchema;
import java.util.ArrayList;
import java.util.List;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class JsonBodyValidator {

  private JsonBodyValidator() {}

  static List<ValidationViolation> validate(
      int statusCode, String responseBody, Endpoint endpoint) {
    List<ValidationViolation> violations = new ArrayList<>();
    String schemaJson = resolveSchemaJson(statusCode, endpoint);
    if (schemaJson == null || schemaJson.isBlank()) {
      return violations;
    }
    if (responseBody == null || responseBody.isBlank()) {
      return violations;
    }
    try {
      JSONObject rawSchema = new JSONObject(schemaJson);
      Schema schema = SchemaLoader.load(rawSchema);
      String trimmed = responseBody.trim();
      Object subject = trimmed.startsWith("[") ? new JSONArray(trimmed) : new JSONObject(trimmed);
      schema.validate(subject);
    } catch (ValidationException e) {
      for (String msg : e.getAllMessages()) {
        violations.add(new ValidationViolation(ViolationType.RESPONSE_SCHEMA, "body", msg));
      }
    } catch (JSONException e) {
      violations.add(
          new ValidationViolation(
              ViolationType.RESPONSE_SCHEMA, "body", "Response body is not valid JSON"));
    }
    return violations;
  }

  private static String resolveSchemaJson(int statusCode, Endpoint endpoint) {
    if (endpoint.responses() == null) return null;
    ResponseSchema exact = endpoint.responses().get(statusCode);
    if (exact != null) return exact.schemaJson();
    ResponseSchema fallback = endpoint.responses().get(-1);
    return fallback != null ? fallback.schemaJson() : null;
  }
}
