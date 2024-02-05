package com.github.hikvision.api.ui;

import com.github.hikvision.api.exceptions.EmployeeAlreadyExist;
import com.github.hikvision.api.exceptions.FaceDetectFailed;
import com.github.hikvision.api.model.UserInfo;
import com.github.hikvision.api.services.HikVisionService;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamResolution;
import lombok.SneakyThrows;
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
    public static final String BACKGROUND_FILE = "background.jpg";
    private final Webcam webcam;
    private final HikVisionService service;

    @SneakyThrows
    public AppUI(Webcam webcam, HikVisionService service, AppUIConfig appConfig) {
        super(appConfig.getAppTitleText());
        this.webcam = webcam;
        this.service = service;

        Dimension size = WebcamResolution.VGA.getSize();
        webcam.setViewSize(size);

        JButton addUserButton = new JButton(new AddUserButtonAction(appConfig.getButtonText()));
        addUserButton.setEnabled(true);
        addUserButton.setBounds(appConfig.getAddUserButtonPositionX(), appConfig.getAddUserButtonPositionY(),
                appConfig.getAddUserButtonWidth(), appConfig.getAddUserButtonHeight());
        addUserButton.setOpaque(false);
        addUserButton.setFocusPainted(false);
        addUserButton.setBorderPainted(false);
        addUserButton.setContentAreaFilled(appConfig.isAddUserButtonShowBackground());
        addUserButton.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0)); // Especially important
        addUserButton.setFont(new Font(appConfig.getAddUserButtonFontName(), Font.BOLD, appConfig.getAddUserButtonFontSize()));
        addUserButton.setForeground(getButtonTextColor(appConfig.getAddUserButtonFontColor()));

        setLayout(new FlowLayout());
        JLabel background = new JLabel(new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(BACKGROUND_FILE))));
        add(background);
        background.setLayout(null);
        addUserButton.setLayout(null);
        background.add(addUserButton);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (appConfig.isFullScreen()) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    private Color getButtonTextColor(String hexColorFromConfig) {
        String hexCode = hexColorFromConfig.startsWith("#") ? hexColorFromConfig.replace("#", "") : hexColorFromConfig;
        int red = Integer.valueOf(hexCode.substring(0, 2), 16);
        int green = Integer.valueOf(hexCode.substring(2, 4), 16);
        int blue = Integer.valueOf(hexCode.substring(4, 6), 16);
        return new Color(red, green, blue);
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
