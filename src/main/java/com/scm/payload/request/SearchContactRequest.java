package com.scm.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SearchContactRequest {
    private String field;
    private String key;
}
