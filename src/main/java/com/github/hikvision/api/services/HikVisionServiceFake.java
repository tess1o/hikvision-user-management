package com.github.hikvision.api.services;

import com.github.hikvision.api.model.UserInfo;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class HikVisionServiceFake implements HikVisionService {
    @Override
    public List<UserInfo> findAll() {
        log.info("Find all users");
        return List.of();
    }

    @Override
    public void addUser(UserInfo user, byte[] photo) {
        log.info("Add user {}", user);
    }

    @Override
    public HttpResponseWrapper remove(List<String> ids) {
        log.info("Remove users: {}", ids);
        return new HttpResponseWrapper("OK", 200);
    }

    @Override
    public String findLatestEmployeeCode() {
        return "6";
    }
}
