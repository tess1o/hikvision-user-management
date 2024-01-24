package com.github.hikvision.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    public String employeeNo;
    public String name;
    public String userType;
    public boolean closeDelayEnabled;
    @JsonProperty("Valid")
    public Valid valid;
    public String belongGroup;
    public String password;
    public String doorRight;
    @JsonProperty("RightPlan")
    public List<RightPlan> rightPlan;
    public int maxOpenDoorTime;
    public int openDoorTime;
    public int roomNumber;
    public int floorNumber;
    public boolean localUIRight;
    public String gender;
    public int numOfCard;
    public int numOfFace;
    @JsonProperty("PersonInfoExtends")
    public List<PersonInfoExtend> personInfoExtends;

    @Data
    private static class PersonInfoExtend {
        public String value;
    }

    public UserInfo(String employeeNo) {
        this.employeeNo = employeeNo;
        this.name = "created by api";
        this.userType = "normal";
        this.gender = "male";
        this.localUIRight = false;
        this.maxOpenDoorTime = 0;
        this.valid = Valid.builder()
                .enable(true)
                .beginTime(LocalDateTime.now())
                .endTime(LocalDateTime.now().plusYears(1))
                .timeType("local")
                .build();
        this.doorRight = "1";
        this.rightPlan = List.of(RightPlan.builder()
                .doorNo(1)
                .planTemplateNo("1")
                .build());
    }
}
