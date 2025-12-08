package com.scm.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInsight {
    @Builder.Default
    private int totalContact = 0;
    @Builder.Default
    private int favoriteCount = 0;
    @Builder.Default
    private int socialLinkCount = 0;
    @Builder.Default
    private int monthlyContacts = 0;
}
