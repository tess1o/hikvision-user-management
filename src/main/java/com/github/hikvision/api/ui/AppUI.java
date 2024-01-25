package com.github.hikvision.api.ui;

import com.github.hikvision.api.exceptions.EmployeeAlreadyExist;
import com.github.hikvision.api.exceptions.FaceDetectFailed;
import com.github.hikvision.api.model.UserInfo;
import com.github.hikvision.api.services.HikVisionService;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static javax.swing.JOptionPane.showMessageDialog;

@Slf4j
public class AppUI extends JFrame {
    private final Webcam webcam;
    private final HikVisionService service;

    public AppUI(Webcam webcam, HikVisionService service, AppUIConfig appConfig) {

        super(appConfig.getAppTitleText());
        this.webcam = webcam;
        this.service = service;

        Dimension size = WebcamResolution.VGA.getSize();
        webcam.setViewSize(size);
        WebcamPanel panel = new WebcamPanel(webcam, size, false);
        panel.setFPSDisplayed(false);
        panel.setFillArea(true);
        panel.start();

        JButton addUserButton = new JButton(new AddUserButtonAction(appConfig.getButtonText()));
        addUserButton.setEnabled(true);
        addUserButton.setPreferredSize(new Dimension(200, 100));

        setLayout(new FlowLayout());
        add(panel);
        add(addUserButton);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (appConfig.isFullScreen()) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    private class AddUserButtonAction extends AbstractAction {

        public AddUserButtonAction(String buttonName) {
            super(buttonName);
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                String employeeNo = service.findLatestEmployeeCode();
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(webcam.getImage(), "jpg", baos);
                byte[] photo = baos.toByteArray();
                service.addUser(new UserInfo(employeeNo), photo);
                showMessageDialog(null, "User with employeeNo '" + employeeNo + "' was added");
            } catch (EmployeeAlreadyExist | FaceDetectFailed e1) {
                showMessageDialog(null, e1.getMessage());
            } catch (IOException e3) {
                log.error("Unexpected error happened, check the logs", e3);
            }
        }
    }
}
