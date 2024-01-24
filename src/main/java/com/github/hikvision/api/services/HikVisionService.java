package com.github.hikvision.api.services;

import com.github.hikvision.api.model.UserInfo;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public interface HikVisionService {
    List<UserInfo> findAll() throws IOException;

    void addUser(UserInfo user, byte[] photo) throws IOException;

    HttpResponseWrapper remove(List<String> ids) throws IOException;

    default HttpResponseWrapper remove(String id) throws IOException {
        return remove(List.of(id));
    }
    default HttpResponseWrapper removeAll() throws IOException {
        List<String> users = findAll().stream()
                .map(UserInfo::getEmployeeNo)
                .collect(Collectors.toList());
        return remove(users);
    }
}
