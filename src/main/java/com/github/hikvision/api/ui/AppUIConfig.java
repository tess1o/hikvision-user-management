package com.github.hikvision.api.ui;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
public class AppUIConfig {
    private String appTitleText;
    private String buttonText;
    private boolean isFullScreen;
    private int addUserButtonWidth;
    private int addUserButtonHeight;
    private int addUserButtonPositionX;
    private int addUserButtonPositionY;
    private String addUserButtonFontName;
    private int addUserButtonFontSize;
    private String addUserButtonFontColor;
    private boolean addUserButtonShowBackground;
    private int modalWindowTimeoutSeconds;
}
