package com.github.hikvision.api.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.github.hikvision.api.exceptions.EmployeeAlreadyExist;
import com.github.hikvision.api.exceptions.FaceDetectFailed;
import com.github.hikvision.api.model.*;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.classic.methods.HttpPut;
import org.apache.hc.client5.http.entity.mime.ByteArrayBody;
import org.apache.hc.client5.http.entity.mime.HttpMultipartMode;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.entity.mime.StringBody;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.hc.core5.http.io.entity.StringEntity;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

import static com.github.hikvision.api.services.HikVisionConstants.APPLICATION_X_WWW_FORM_URLENCODED_CHARSET_UTF_8;
import static com.github.hikvision.api.services.HikVisionConstants.HikVisionErrors.EMPLOYEE_NO_ALREADY_EXIST_ERROR_MESSAGE;
import static com.github.hikvision.api.services.HikVisionConstants.HikVisionErrors.FACE_DETECT_FAILED_ERROR_MESSAGE;
import static com.github.hikvision.api.services.HikVisionConstants.HikVisionUrls.*;
import static com.github.hikvision.api.services.HikVisionConstants.SEARCH_MAX_RESULTS;

@Slf4j
public class HikVisionServiceImpl implements HikVisionService {

    private final String serverUrl;
    private final String username;
    private final String password;
    private final int employeeCodeStep;
    private final BasicCredentialsProvider credentialsProvider;

    @SneakyThrows
    public HikVisionServiceImpl(String serverUrl, String username, String password, int hikVisionEmployeeCodeStep) {
        this.serverUrl = serverUrl;
        this.username = username;
        this.password = password;
        this.employeeCodeStep = hikVisionEmployeeCodeStep;
        this.credentialsProvider = getCredentialsProvider();
    }

    @Override
    public List<UserInfo> findAll() throws IOException {
        ObjectMapper mapper = ObjectMapperFactory.getObjectMapper();
        List<UserInfo> results = new ArrayList<>();
        try (CloseableHttpClient httpClient = getHttpClient()) {
            int position = 0;
            int matches;
            do {
                UserInfoSearchResponse searchResponse = executeSearchRequest(httpClient, mapper, position);
                matches = searchResponse.getNumOfMatches();
                if (searchResponse.getUserInfo() != null) {
                    results.addAll(searchResponse.getUserInfo());
                }
                position = position + SEARCH_MAX_RESULTS;
            } while (matches != 0);
        }
        log.info("Found {} users in total", results.size());
        return results;
    }

    private UserInfoSearchResponse executeSearchRequest(CloseableHttpClient httpClient, ObjectMapper mapper, int position) throws IOException {
        log.info("Executing search request for start position: {}", position);
        UserInfoSearchRequest searchRequest = UserInfoSearchRequest.builder()
                .searchID(UUID.randomUUID().toString().replace("-", ""))
                .maxResults(SEARCH_MAX_RESULTS)
                .searchResultPosition(position)
                .build();

        String searchRequestString = mapper.writeValueAsString(searchRequest);
        HttpPost httpPost = new HttpPost(serverUrl + SEARCH_REQUEST_URL);
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED_CHARSET_UTF_8);
        httpPost.setEntity(new StringEntity(searchRequestString));
        return httpClient.execute(httpPost,
                response -> {
                    String bodyAsString = EntityUtils.toString(response.getEntity());
                    return mapper.readValue(bodyAsString, UserInfoSearchResponse.class);
                }
        );
    }

    @Override
    public void addUser(UserInfo user, byte[] photo) throws IOException {
        log.info("Trying to add user with employeeCode {}", user.employeeNo);
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpResponseWrapper addUserResponse = addUser(httpClient, user);
            log.info("AddUserResponse: {}", addUserResponse);
            if (addUserResponse.getResponseCode() == 200) {
                log.info("User with employeeNo '{}' was added", user.employeeNo);
                HttpResponseWrapper addPhoto = addPhoto(httpClient, user.getEmployeeNo(), photo);
                log.info("Add photo response = {}", addPhoto);
                if (addPhoto.getResponseCode() == 200) {
                    log.info("Photo is successfully added to user {}", user.getEmployeeNo());
                    return;
                } else {
                    try {
                        HttpResponseWrapper removeResponse = remove(user.getEmployeeNo());
                        if (removeResponse.getResponseCode() != 200) {
                            throw new IOException("Unable to remove user, got response: " + removeResponse.getResponseBody());
                        }
                    } catch (Exception e) {
                        log.error("Tried to remove employee " + user.getEmployeeNo() + " but failed", e);
                    }
                    HikVisionErrorResponse errorResponse = ObjectMapperFactory.getObjectMapperForErrors()
                            .readValue(addPhoto.getResponseBody(), HikVisionErrorResponse.class);
                    if (FACE_DETECT_FAILED_ERROR_MESSAGE.equals(errorResponse.getSubStatusCode())) {
                        throw new FaceDetectFailed("Unable to detect face in image");
                    }
                }
                return;
            }
            // response code is not 200, so we have an issue
            log.error("Unable to add user, received response: {}", addUserResponse.getResponseBody());
            try {
                HikVisionErrorResponse errorResponse = ObjectMapperFactory.getObjectMapperForErrors()
                        .readValue(addUserResponse.getResponseBody(), HikVisionErrorResponse.class);
                if (EMPLOYEE_NO_ALREADY_EXIST_ERROR_MESSAGE.equals(errorResponse.subStatusCode)) {
                    throw new EmployeeAlreadyExist("Unable to add user, employee code " + user.employeeNo + " is already used");
                }
            } catch (MismatchedInputException e) {
                log.error("Unable to cast response to HikVisionErrorResponse, probably received different type of error", e);
            }
            throw new IOException("Unable to add user, reason is unexpected, check the logs");
        }
    }

    private HttpResponseWrapper addUser(CloseableHttpClient client, UserInfo user) throws IOException {
        HttpPost httpPost = new HttpPost(serverUrl + ADD_USER_URL);
        httpPost.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED_CHARSET_UTF_8);
        String addUserRequest = ObjectMapperFactory.getObjectMapper().writeValueAsString(user);
        httpPost.setEntity(new StringEntity(addUserRequest));
        return client.execute(httpPost,
                response -> {
                    String responseString = EntityUtils.toString(response.getEntity());
                    return new HttpResponseWrapper(responseString, response.getCode());
                }
        );
    }

    @SneakyThrows
    private HttpResponseWrapper addPhoto(CloseableHttpClient client, String employeeCode, byte[] photo) {
        HttpPut request = new HttpPut(serverUrl + ADD_PHOTO_URL);

        AddUserWithPhotoRequest faceDataRecord = AddUserWithPhotoRequest.builder()
                .fDID("1")
                .fPID(employeeCode)
                .faceLibType("blackFD")
                .build();
        final MultipartEntityBuilder builder = MultipartEntityBuilder.create();
        builder.setMode(HttpMultipartMode.LEGACY);
        builder.addPart("FaceDataRecord", new StringBody(ObjectMapperFactory.getObjectMapperForErrors().writeValueAsString(faceDataRecord), ContentType.MULTIPART_FORM_DATA));
        builder.addPart("img", new ByteArrayBody(photo, ContentType.IMAGE_JPEG, "photo.jpg"));
        final HttpEntity entity = builder.build();
        request.setEntity(entity);

        return client.execute(request,
                response -> {
                    String responseString = EntityUtils.toString(response.getEntity());
                    return new HttpResponseWrapper(responseString, response.getCode());
                }
        );
    }

    @Override
    public HttpResponseWrapper remove(List<String> ids) throws IOException {
        return removeUsersById(ids);
    }

    @Override
    public String findLatestEmployeeCode() throws IOException {
        final String defaultValue = String.valueOf(employeeCodeStep);
        final String notFound = "-1";
        List<UserInfo> allUsers = findAll();
        String lastValue = allUsers.stream().max(Comparator.comparingInt(o -> Integer.parseInt(o.getEmployeeNo())))
                .map(UserInfo::getEmployeeNo).orElse(notFound);
        if (notFound.equals(lastValue)) {
            log.info("No users found, will use a default value - {}", defaultValue);
            return defaultValue;
        }
        String nextCode = String.valueOf(Integer.parseInt(lastValue) + employeeCodeStep);
        log.info("The next available employeeCode is {}", nextCode);
        return nextCode;
    }

    private HttpResponseWrapper removeUsersById(List<String> ids) throws IOException {
        try (CloseableHttpClient httpClient = getHttpClient()) {
            HttpPut httpPut = new HttpPut(serverUrl + DELETE_USER_URL);
            httpPut.addHeader(HttpHeaders.CONTENT_TYPE, APPLICATION_X_WWW_FORM_URLENCODED_CHARSET_UTF_8);
            String removeRequest = ObjectMapperFactory.getObjectMapper().writeValueAsString(UserDeleteRequest.of(ids));
            httpPut.setEntity(new StringEntity(removeRequest));
            return httpClient.execute(httpPut,
                    response -> {
                        String responseString = EntityUtils.toString(response.getEntity());
                        log.info("Remove user response - {}", responseString);
                        return new HttpResponseWrapper(responseString, response.getCode());
                    }
            );
        }
    }

    private CloseableHttpClient getHttpClient() {
        return HttpClientBuilder.create()
                .setDefaultCredentialsProvider(credentialsProvider)
                .build();
    }

    private BasicCredentialsProvider getCredentialsProvider() throws URISyntaxException {
        HttpHost target = HttpHost.create(serverUrl);
        BasicCredentialsProvider provider = new BasicCredentialsProvider();
        provider.setCredentials(new AuthScope(target),
                new UsernamePasswordCredentials(username, password.toCharArray()));
        return provider;
    }
}
