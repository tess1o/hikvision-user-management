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

@Slf4j
public class AppUI extends JFrame {

    public static final String BACKGROUND_MAIN = "background_main.jpg";
    public static final String BACKGROUND_INSTRUCTION = "background_instruction.jpg";
    private final Webcam webcam;
    private final HikVisionService service;

    private final JLabel mainBackground; // main image
    private final Icon mainBackgroundIcon; // main image icon
    private final Icon instructionIcon; //image with instructions
    private final JButton addUserButton; //add new user button


    @SneakyThrows
    public AppUI(Webcam webcam, HikVisionService service, AppUIConfig appConfig) {
        super(appConfig.getAppTitleText());
        this.webcam = webcam;
        this.service = service;

        Dimension size = WebcamResolution.VGA.getSize();
        this.webcam.setViewSize(size);
        this.webcam.open();

        addUserButton = configureAddButton(appConfig);
        setLayout(new FlowLayout());

        mainBackgroundIcon = getImageIcon(BACKGROUND_MAIN);
        instructionIcon = getImageIcon(BACKGROUND_INSTRUCTION);

        mainBackground = new JLabel(mainBackgroundIcon);
        add(mainBackground);
        mainBackground.setLayout(null);
        addUserButton.setLayout(null);
        mainBackground.add(addUserButton);
        pack();
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        if (appConfig.isFullScreen()) {
            setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        }
    }

    private static ImageIcon getImageIcon(String path) throws IOException {
        return new ImageIcon(ImageIO.read(Thread.currentThread().getContextClassLoader().getResourceAsStream(path)));
    }

    private JButton configureAddButton(AppUIConfig appConfig) {
        final JButton addUserButton;
        addUserButton = new JButton(new AddUserButtonAction(appConfig.getButtonText(), appConfig.getModalWindowTimeoutSeconds()));
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
        return addUserButton;
    }

    private Color getButtonTextColor(String hexColorFromConfig) {
        String hexCode = hexColorFromConfig.startsWith("#") ? hexColorFromConfig.replace("#", "") : hexColorFromConfig;
        int red = Integer.valueOf(hexCode.substring(0, 2), 16);
        int green = Integer.valueOf(hexCode.substring(2, 4), 16);
        int blue = Integer.valueOf(hexCode.substring(4, 6), 16);
        return new Color(red, green, blue);
    }

    private class AddUserButtonAction extends AbstractAction {

        private final int modalWindowTimeout;

        public AddUserButtonAction(String buttonName, int modalWindowTimeout) {
            super(buttonName);
            this.modalWindowTimeout = modalWindowTimeout;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            try {
                String employeeNo = service.findLatestEmployeeCode();
                log.info("Will use employeeCode for this user: {}", employeeNo);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(webcam.getImage(), "jpg", baos);
                byte[] photo = baos.toByteArray();
                service.addUser(new UserInfo(employeeNo), photo);
                handleSuccess(modalWindowTimeout);
            } catch (EmployeeAlreadyExist | FaceDetectFailed e1) {
                showMessage(e1.getMessage(), "Cannot add a new user", JOptionPane.ERROR_MESSAGE, modalWindowTimeout);
            } catch (IOException e3) {
                log.error("Unexpected error happened, check the logs", e3);
                showMessage("Unexpected error happened, check the logs", "Error", JOptionPane.ERROR_MESSAGE, modalWindowTimeout);
            }
        }

        private void showMessage(String message, String title, int messageType, int timeoutSeconds) {
            JOptionPane pane = new JOptionPane(message, messageType);
            JDialog dialog = pane.createDialog(null, title);
            dialog.setModal(false);
            dialog.setVisible(true);

            if (timeoutSeconds > 0) {
                new Timer(timeoutSeconds * 1000, e -> {
                    dialog.setVisible(false);
                }).start();
            }
        }
    }

    /**
     * Temporary show image with instructions (and disable addUserButton). After timeout change the image back to main and enable the button
     * @param timeoutSeconds how long to show the instruction
     */
    @SneakyThrows
    private void handleSuccess(int timeoutSeconds) {
        log.info("The user was added to HikVision, changing background to an image with instructions");
        mainBackground.setIcon(instructionIcon);
        addUserButton.setEnabled(false);

        if (timeoutSeconds > 0) {
            new Timer(timeoutSeconds * 1000, e -> {
                log.info("Changing background back to the main");
                mainBackground.setIcon(mainBackgroundIcon);
                addUserButton.setEnabled(true);
            }).start();
        }
    }
}
