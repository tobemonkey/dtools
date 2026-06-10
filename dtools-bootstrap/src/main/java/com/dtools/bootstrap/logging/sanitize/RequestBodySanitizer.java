package com.dtools.bootstrap.logging.sanitize;

import com.dtools.common.log.LogSensitive;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @description: 请求体日志脱敏器，根据 RequestBody 类型上的 LogSensitive 注解处理 JSON 字段
 * @author: yesterday'jam
 * @date: 2026/06/09
 * @注意: 运行时不根据字段名兜底脱敏，未加注解的字段保持原值
 */
@Component
public class RequestBodySanitizer {

    private final ObjectMapper objectMapper;

    private final SensitiveValueMasker sensitiveValueMasker;

    @Autowired
    public RequestBodySanitizer(ObjectMapper objectMapper) {
        this(objectMapper, new SensitiveValueMasker());
    }

    public RequestBodySanitizer(ObjectMapper objectMapper, SensitiveValueMasker sensitiveValueMasker) {
        this.objectMapper = objectMapper;
        this.sensitiveValueMasker = sensitiveValueMasker;
    }

    /**
     * @description: 脱敏并截断请求体 JSON
     * @author: yesterday'jam
     * @date: 2026/06/09
     * @注意: 解析或脱敏失败时返回截断后的原始 body，调用方不得因此中断请求主链路
     */
    public String sanitize(String body, Class<?> bodyType, int maxLength) {
        if (body == null || body.isBlank()) {
            return body;
        }
        if (bodyType == null) {
            return limit(body, maxLength);
        }
        try {
            JsonNode root = objectMapper.readTree(body);
            sanitizeNode(root, bodyType);
            return limit(objectMapper.writeValueAsString(root), maxLength);
        } catch (RuntimeException exception) {
            return limit(body, maxLength);
        } catch (Exception exception) {
            return limit(body, maxLength);
        }
    }

    private void sanitizeNode(JsonNode node, Class<?> nodeType) {
        if (node instanceof ObjectNode objectNode) {
            sanitizeObjectNode(objectNode, nodeType);
            return;
        }
        if (node instanceof ArrayNode arrayNode) {
            for (JsonNode item : arrayNode) {
                sanitizeNode(item, nodeType);
            }
        }
    }

    private void sanitizeObjectNode(ObjectNode objectNode, Class<?> nodeType) {
        Map<String, Field> fields = fieldsByName(nodeType);
        objectNode.fieldNames().forEachRemaining(fieldName -> {
            Field field = fields.get(fieldName);
            if (field == null) {
                return;
            }
            JsonNode fieldNode = objectNode.get(fieldName);
            LogSensitive annotation = field.getAnnotation(LogSensitive.class);
            if (annotation != null) {
                objectNode.set(fieldName, TextNode.valueOf(sensitiveValueMasker.mask(fieldNode.asText(), annotation.strategy())));
                return;
            }
            sanitizeNode(fieldNode, field.getType());
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

    private String limit(String value, int maxLength) {
        if (maxLength <= 0 || value == null || value.length() <= maxLength) {
            return value;
        }
        return value.substring(0, maxLength) + "...";
    }
}

