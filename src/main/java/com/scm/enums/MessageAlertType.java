package com.scm.enums;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import com.scm.utils.constants.ResponseMessage;

public enum MessageAlertType {
    green(ResponseMessage.SUCCESS),
    red(ResponseMessage.FAILED),
    yellow(ResponseMessage.CONFLICT),
    blue("DEFAULT");

    private final String code;

    MessageAlertType(String code) {
        this.code = code;
    }

    private static final Map<String,MessageAlertType> LOOKUP=
    Arrays.stream(values()).collect(Collectors.toMap(type->type.code,type->type));

    public static MessageAlertType fromCode(String code){
        return LOOKUP.getOrDefault(code, blue);
    }
}
