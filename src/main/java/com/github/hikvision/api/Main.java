package com.github.hikvision.api;

import com.github.eduramiba.webcamcapture.drivers.NativeDriver;
import com.github.hikvision.api.services.HikVisionService;
import com.github.hikvision.api.services.HikVisionServiceImpl;
import com.github.sarxos.webcam.Webcam;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

@Slf4j
public class Main extends JFrame {

    public static void main(String[] args) {
        Webcam.setDriver(new NativeDriver());
        Config conf = ConfigFactory.load();
        String serverUrl = conf.getString("hikvision.server.url");
        String username = conf.getString("hikvision.server.username");
        String password = conf.getString("hikvision.server.password");

        HikVisionService service = new HikVisionServiceImpl(serverUrl, username, password);

        new AppUI(Webcam.getDefault(), service);
    }
}