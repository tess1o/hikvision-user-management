package com.github.hikvision.api.model;

import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Builder;
import lombok.Data;

@Data
@JsonRootName("UserInfoSearchCond")
@Builder
public class UserInfoSearchRequest {
    public String searchID;
    public int maxResults;
    public int searchResultPosition;
}