package com.scm.utils.helper;

import com.scm.enums.MessageAlertType;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AlertMessage {
    private String content;
    @Builder.Default
    private MessageAlertType type = MessageAlertType.blue;
}
