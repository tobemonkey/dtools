package com.dtools.bootstrap.logging.sanitize;

import com.dtools.common.log.LogSensitive;
import com.dtools.common.log.SensitiveStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 失败请求参数快照构造器，合并 query 和 body 用于 DB 诊断日志
 * @author: yesterday'jam
 * @date: 2026/06/10
 * @注意: 异常诊断模式下仅 HIDDEN 字段隐藏，其余注解字段保留原值
 */
@Component
public class RequestErrorParamSnapshotBuilder {

    private static final String HIDDEN_VALUE = "[HIDDEN]";

    private final ObjectMapper objectMapper;

    public RequestErrorParamSnapshotBuilder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * @description: 构造失败请求参数 JSON 快照
     * @author: yesterday'jam
     * @date: 2026/06/10
     * @注意: query 和 body 均为空时返回 null，避免写入无意义空对象
     */
    public String build(String queryString, String body, Class<?> bodyType, int maxLength) {
        try {
            ObjectNode root = objectMapper.createObjectNode();
            ObjectNode query = parseQuery(queryString);
            if (!query.isEmpty()) {
                root.set("query", query);
            }
            JsonNode bodyNode = parseBody(body, bodyType);
            if (bodyNode != null && !bodyNode.isNull()) {
                root.set("body", bodyNode);
            }
            if (root.isEmpty()) {
                return null;
            }
            return limit(objectMapper.writeValueAsString(root), maxLength);
        } catch (Exception exception) {
            return null;
        }
    }

    private ObjectNode parseQuery(String queryString) {
        ObjectNode query = objectMapper.createObjectNode();
        if (queryString == null || queryString.isBlank()) {
            return query;
        }
        String[] pairs = queryString.split("&");
        for (String pair : pairs) {
            if (pair.isBlank()) {
                continue;
            }
            int equalsIndex = pair.indexOf('=');
            String name = equalsIndex >= 0 ? pair.substring(0, equalsIndex) : pair;
            String value = equalsIndex >= 0 ? pair.substring(equalsIndex + 1) : "";
            query.put(decode(name), decode(value));
        }
        return query;
    }

    private JsonNode parseBody(String body, Class<?> bodyType) throws Exception {
        if (body == null || body.isBlank()) {
            return null;
        }
        JsonNode root = objectMapper.readTree(body);
        if (bodyType != null) {
            hideOnlyHiddenFields(root, bodyType);
        }
        return root;
    }

    private void hideOnlyHiddenFields(JsonNode node, Class<?> nodeType) {
        if (node instanceof ObjectNode objectNode) {
            hideObjectNode(objectNode, nodeType);
            return;
        }
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode item : arrayNode) {
                hideOnlyHiddenFields(item, nodeType);
            }
        }
    }

    private void hideObjectNode(ObjectNode objectNode, Class<?> nodeType) {
        Map<String, Field> fields = fieldsByName(nodeType);
        objectNode.fieldNames().forEachRemaining(fieldName -> {
            Field field = fields.get(fieldName);
            if (field == null) {
                return;
            }
            LogSensitive annotation = field.getAnnotation(LogSensitive.class);
            if (annotation != null && SensitiveStrategy.HIDDEN.equals(annotation.strategy())) {
                objectNode.set(fieldName, TextNode.valueOf(HIDDEN_VALUE));
                return;
            }
            hideOnlyHiddenFields(objectNode.get(fieldName), field.getType());
        });
    }

    private Map<String, Field> fieldsByName(Class<?> type) {
        Map<String, Field> fields = new HashMap<>();
        Class<?> current = type;
        while (current != null && current != Object.class) {
            for (Field field : current.getDeclaredFields()) {
                fields.putIfAbsent(field.getName(), field);
            }
            current = current.getSuperclass();
        }
        return fields;
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private String limit(String value, int maxLength) {
        if (maxLength <= 0 || value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}

