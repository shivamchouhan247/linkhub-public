package com.scm.utils.constants;

import java.util.*;

import org.springframework.stereotype.Component;

@Component
public class CommonConfigLogic {

    public Map<String, Object> buildResponse(String code, String status, String description) {
        Map<String, Object> response = new HashMap<>();
        response.put(ResponseMessage.CODE, code);
        response.put(ResponseMessage.STATUS, status);
        response.put(ResponseMessage.DESCRIPTION, description);
        return response;
    }
}
