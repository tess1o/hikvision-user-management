package com.github.hikvision.api.exceptions;

public class FaceDetectFailed extends RuntimeException {
    public FaceDetectFailed(String message) {
        super(message);
    }

    public FaceDetectFailed(String message, Throwable cause) {
        super(message, cause);
    }
}
