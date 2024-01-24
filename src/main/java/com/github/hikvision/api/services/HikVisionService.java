package com.github.hikvision.api.services;

import com.github.hikvision.api.model.UserInfo;

import java.io.IOException;
import java.util.List;

public interface HikVisionService {
    List<UserInfo> findAll() throws IOException;

    void addUser(UserInfo user, byte[] photo) throws IOException;

    HttpResponseWrapper remove(String id) throws IOException;
    void remove(List<String> ids) throws IOException;

    void removeAll() throws IOException;

}
