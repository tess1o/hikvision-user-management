package com.github.hikvision.api;

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

    private class SnapMeAction extends AbstractAction {

        public SnapMeAction() {
            super("Скріншот");
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            String employeeNo = "888";
            try {
                //    File file = new File(String.format("test-%s.jpg", LocalDateTime.now().format(FILE_DATE_FORMAT)));
                //ImageIO.write(webcam.getImage(), "JPG", file);
                // log.info("Image for {} saved in {}", webcam.getName(), file);
                //   service.addUser(new UserInfo(employeeNo), Files.readAllBytes(file.toPath()));

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(webcam.getImage(), "jpg", baos);
                byte[] photo = baos.toByteArray();
                service.addUser(new UserInfo(employeeNo), photo);
                showMessageDialog(null, "User with employeeNo '" + employeeNo + "' was added");
            } catch (EmployeeAlreadyExist | FaceDetectFailed e1) {
                showMessageDialog(null, e1.getMessage());
            } catch (IOException e3) {
                log.error("Unable to create screenshot", e3);
            }
        }
    }

    private final Dimension size = WebcamResolution.VGA.getSize();
    private final Webcam webcam;
    private final HikVisionService service;
    private final JButton btSnapMe = new JButton(new AppUI.SnapMeAction());

    public AppUI(Webcam webcam, HikVisionService service) {

        super("Гардероб");
        this.webcam = webcam;
        this.service = service;

        webcam.setViewSize(size);
        WebcamPanel panel = new WebcamPanel(webcam, size, false);
        panel.setFPSDisplayed(false);
        panel.setFillArea(true);
        panel.start();

        btSnapMe.setEnabled(true);

        setLayout(new FlowLayout());
        add(panel);
        add(btSnapMe);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}
