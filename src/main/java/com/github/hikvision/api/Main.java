package com.github.hikvision.api;

import com.github.hikvision.api.services.CleanupTrigger;
import com.github.hikvision.api.services.HikVisionService;
import com.github.hikvision.api.services.HikVisionServiceFake;
import com.github.hikvision.api.services.HikVisionServiceImpl;
import com.github.hikvision.api.ui.AppUI;
import com.github.hikvision.api.ui.AppUIConfig;
import com.github.sarxos.webcam.Webcam;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.impl.StdSchedulerFactory;

@Slf4j
public class Main {

    @SneakyThrows
    public static void main(String[] args) {
        // Webcam.setDriver(new NativeDriver());
        Config conf = ConfigFactory.load(); // load configuration
        AppUIConfig appUIConfig = getAppUIConfig(conf); // get UI config
        HikVisionService service = getHikVisionService(conf); // get HikVision service
        createSchedulerAndStartCleanupJob(conf, service); // start cleanup job
        new AppUI(Webcam.getDefault(), service, appUIConfig); // start UI
    }

    /**
     * Get configuration from application.conf file
     * @param conf
     * @return
     */
    private static AppUIConfig getAppUIConfig(Config conf) {
        return AppUIConfig
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
                .modalWindowTimeoutSeconds(conf.getInt("ui.modal.window.timeout.seconds"))
                .build();
    }

    /**
     * Setup either a real hikvision integration (for production) or a mock for dev tests
     * @param conf - configuration
     * @return if app.hikvision.mock = true then return mock, otherwise return real integration
     */
    private static HikVisionService getHikVisionService(Config conf) {
        HikVisionService service;
        if (conf.hasPath("app.hikvision.mock") && conf.getBoolean("app.hikvision.mock")) {
            log.info("Using mock hikvision service");
            service = new HikVisionServiceFake();
        } else {
            log.info("Using real hikvision service");
            String serverUrl = conf.getString("hikvision.server.url");
            String username = conf.getString("hikvision.server.username");
            String password = conf.getString("hikvision.server.password");
            int hikvisionEmployeeCodeStep = conf.getInt("hikvision.employee.code.step");
            service = new HikVisionServiceImpl(serverUrl, username, password, hikvisionEmployeeCodeStep);
        }
        return service;
    }

    /**
     * Create a scheduler to run jobs and starts a cleanup job to remove all users by the end of the day
     * @param conf - configuration file
     * @param service - hikvision service
     * @throws SchedulerException
     */
    private static void createSchedulerAndStartCleanupJob(Config conf, HikVisionService service) throws SchedulerException {
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = sf.getScheduler();
        scheduler.start();
        String cleanupCronExpression = conf.getString("hikvision.cleanup.cron");
        new CleanupTrigger().scheduleCleanupJob(scheduler, cleanupCronExpression, service);
    }

}