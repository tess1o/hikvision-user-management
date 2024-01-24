package com.github.hikvision.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@JsonRootName("UserInfoDelCond")
@AllArgsConstructor
@NoArgsConstructor
public class UserDeleteRequest {
    @JsonProperty("EmployeeNoList")
    public List<EmployeeNoList> employees;

    public static UserDeleteRequest of(List<String> ids) {
        List<EmployeeNoList> employees = ids.stream()
                .map(EmployeeNoList::new)
                .collect(Collectors.toList());
        return new UserDeleteRequest(employees);
    }

}
