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
}
