package com.github.hikvision.api;

import com.github.hikvision.api.services.CleanupTrigger;
import com.github.hikvision.api.services.HikVisionService;
import com.github.hikvision.api.services.HikVisionServiceImpl;
import com.github.hikvision.api.ui.AppUI;
import com.github.hikvision.api.ui.AppUIConfig;
import com.github.sarxos.webcam.Webcam;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

@Slf4j
public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        // Webcam.setDriver(new NativeDriver());
        Config conf = ConfigFactory.load();
        String serverUrl = conf.getString("hikvision.server.url");
        String username = conf.getString("hikvision.server.username");
        String password = conf.getString("hikvision.server.password");
        String cleanupCronExpression = conf.getString("hikvision.cleanup.cron");

        AppUIConfig appUIConfig = AppUIConfig
                .builder()
                .isFullScreen(conf.getBoolean("ui.app.full.screen"))
                .appTitleText(conf.getString("ui.app.title.text"))
                .buttonText(conf.getString("ui.add.user.button.text"))
                .addUserButtonWidth(conf.getInt("ui.add.user.button.width"))
                .addUserButtonHeight(conf.getInt("ui.add.user.button.height"))
                .addUserButtonPositionX(conf.getInt("ui.add.user.button.position.x"))
                .addUserButtonPositionY(conf.getInt("ui.add.user.button.position.y"))
                .addUserButtonFontName(conf.getString("ui.add.user.button.font.name"))
                .addUserButtonFontSize(conf.getInt("ui.add.user.button.font.size"))
                .addUserButtonFontColor(conf.getString("ui.add.user.button.font.color"))
                .addUserButtonShowBackground(conf.getBoolean("ui.add.user.button.showBackground"))
                .build();

        HikVisionService service = new HikVisionServiceImpl(serverUrl, username, password);
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = sf.getScheduler();
        scheduler.start();
        new CleanupTrigger().scheduleCleanupJob(scheduler, cleanupCronExpression, service);
        new AppUI(Webcam.getDefault(), service, appUIConfig);
    }
}