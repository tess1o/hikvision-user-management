package com.github.hikvision.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
@Builder
public class AddUserWithPhotoRequest {
    public String faceLibType;
    @JsonProperty("FDID")
    public String fDID;
    @JsonProperty("FPID")
    public String fPID;
    public Object deleteFP;
    public String name;
    public String gender;
    public String bornTime;
    public String city;
    public String certificateType ;
    public String certificateNumber;
    public String caseInfo;
    public String tag;
    public String address;
    public String customInfo;
    public String modelData;
}
