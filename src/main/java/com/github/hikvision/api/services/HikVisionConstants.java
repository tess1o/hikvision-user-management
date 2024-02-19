package com.github.hikvision.api.services;

public interface HikVisionConstants {

    interface HikVisionErrors {
        String EMPLOYEE_NO_ALREADY_EXIST_ERROR_MESSAGE = "employeeNoAlreadyExist";
        String FACE_DETECT_FAILED_ERROR_MESSAGE = "faceDetectFailed";
    }

    interface HikVisionUrls {
        String ADD_USER_URL = "/ISAPI/AccessControl/UserInfo/Record?format=json";
        String SEARCH_REQUEST_URL = "/ISAPI/AccessControl/UserInfo/Search?format=json";
        String DELETE_USER_URL = "/ISAPI/AccessControl/UserInfo/Delete?format=json";
        String ADD_PHOTO_URL = "/ISAPI/Intelligent/FDLib/FDSetUp?format=json";
    }

    int SEARCH_MAX_RESULTS = 30;
    String APPLICATION_X_WWW_FORM_URLENCODED_CHARSET_UTF_8 = "application/x-www-form-urlencoded; charset=UTF-8";
}
