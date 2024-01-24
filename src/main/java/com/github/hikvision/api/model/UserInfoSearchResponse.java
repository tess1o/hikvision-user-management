package com.github.hikvision.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.Data;

import java.util.ArrayList;

@Data
@JsonRootName(value = "UserInfoSearch")
public class UserInfoSearchResponse {
    public String searchID;
    public String responseStatusStrg;
    public int numOfMatches;
    public int totalMatches;
    @JsonProperty("UserInfo")
    public ArrayList<UserInfo> userInfo;
}