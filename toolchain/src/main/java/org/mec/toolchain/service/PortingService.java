/*
 *  Copyright 2020 Huawei Technologies Co., Ltd.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.mec.toolchain.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spencerwi.either.Either;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.core.Response.Status;
import org.mec.toolchain.config.PortingParamConfig;
import org.mec.toolchain.model.dto.FormatRespDto;
import org.mec.toolchain.model.porting.BaseResponse;
import org.mec.toolchain.model.porting.BaseTask;
import org.mec.toolchain.model.porting.BaseTaskInfo;
import org.mec.toolchain.model.porting.GetSrcResponse;
import org.mec.toolchain.model.porting.LoginRequest;
import org.mec.toolchain.model.porting.LoginResponse;
import org.mec.toolchain.model.porting.TaskResponse;
import org.mec.toolchain.model.porting.TaskStatus;
import org.mec.toolchain.model.porting.TaskStatusAnalysis;
import org.mec.toolchain.model.porting.TasksData;
import org.mec.toolchain.model.porting.TasksResponse;
import org.mec.toolchain.model.porting.UploadSrcResponse;
import org.mec.toolchain.util.FileUtil;
import org.mec.toolchain.util.HttpUtil;
import org.mec.toolchain.util.PackageChecker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service("porting")
public class PortingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PortingService.class);

    private static final String CONTENT_TYPE = "Content-type";

    private static final String CHARSET = "application/json;charset=UTF-8";

    private static final String AUTHORIZATION = "Authorization";

    private static final String CREAETE_TASK_FAIL = "Create task failed";

    private static final String GET_TASK_FAIL = "Get task info failed";

    private static final String REPORT = "report";

    private Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().create();

    @Autowired
    private HttpUtil httpUtil;

    @Autowired
    private PortingParamConfig portingParamConfig;

    /**
     * upload source code to toolchain service.
     *
     * @param userId user id
     * @param sourceCode source code .tar.gz
     * @return UploadSrcResponse
     */
    public Either<FormatRespDto, UploadSrcResponse> uploadSourceCode(String userId, MultipartFile sourceCode) {
        LOGGER.info("Begin to upload source code");
        String fileName = sourceCode.getOriginalFilename();

        String srcPath = new StringBuffer(portingParamConfig.getSrcPath()).append(transToUsername(userId))
            .append(File.separator).append("src").append(File.separator).toString();
        String filePath = srcPath + fileName;

        File src = null;
        try {
            src = FileUtil.createNewDir(filePath);
        } catch (IOException e) {
            LOGGER.error("Create new Dir IOException");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Create src dir failed"));
        }
        File tgz = new File(src, "source.tar.gz");
        try {
            boolean res = tgz.createNewFile();
            if (!res) {
                LOGGER.error("Create tgz file error.");
            }
            sourceCode.transferTo(tgz);

            // to bomb check
            PackageChecker checker = new PackageChecker();
            if (!checker.bombCheckGzip(tgz)) {
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Tar.gz decompress failed."));
            }

            FileUtil.deCompressTgz(filePath, tgz);
        } catch (IOException e) {
            LOGGER.error("Create new file IOException");
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Tar.gz decompress failed"));
        }

        LOGGER.info("Upload source code success");
        UploadSrcResponse response = new UploadSrcResponse(
            transToUsername(userId) + File.separator + "src" + File.separator);
        return Either.right(response);
    }

    /**
     * get source code by user id.
     *
     * @param userId user id
     * @return GetSrcResponse
     */
    public Either<FormatRespDto, GetSrcResponse> getSourceCode(String userId) {
        LOGGER.info("Search source code");
        String srcPath = new StringBuffer(portingParamConfig.getSrcPath()).append(transToUsername(userId))
            .append(File.separator).append("src").toString();
        File src = new File(srcPath);
        if (!src.exists() || !src.isDirectory()) {
            return Either.right(new GetSrcResponse("", false));
        }
        File[] files = src.listFiles();
        if (files == null || files.length < 1) {
            return Either.right(new GetSrcResponse("", false));
        }
        return Either.right(new GetSrcResponse(files[0].getName(), true));
    }

    /**
     * delete source code by user id.
     *
     * @param userId user id
     * @return BaseResponse
     */
    public Either<FormatRespDto, BaseResponse> deleteSourceCode(String userId) {
        LOGGER.info("Begin delete source code");
        String srcPath = new StringBuilder(portingParamConfig.getSrcPath()).append(transToUsername(userId))
            .append(File.separator).append("src").toString();
        try {
            FileUtil.deleteDir(new File(srcPath));
        } catch (IOException e) {
            LOGGER.error("Delete source code IOException");
            return Either.right(new BaseResponse(1, "Delete failed"));
        }

        LOGGER.info("Delete source code success");
        return Either.right(new BaseResponse(0, "Delete success"));
    }

    /**
     * create analysis task.
     *
     * @param userId user id
     * @param taskInfo analysis task info
     * @return TaskResponse
     */
    public synchronized Either<FormatRespDto, TaskResponse> createTask(String userId, BaseTask taskInfo) {
        LOGGER.info("Begin create analysis task");
        String token = getToken(
            loginPorting(portingParamConfig.getAdminUsername(), portingParamConfig.getAdminPassword()));
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, CHARSET);
        headers.put(AUTHORIZATION, token);

        String srcPath = new StringBuilder(portingParamConfig.getSrcPath()).append(transToUsername(userId))
            .append(File.separator).append("src").append(File.separator).toString();
        taskInfo.setSourceDir(srcPath);
        BaseTaskInfo task = new BaseTaskInfo(taskInfo);
        String url = portingParamConfig.getTasksUrl();
        String httpResponse = httpUtil.httpsPost(url, headers, gson.toJson(task));

        if (httpResponse == null) {
            LOGGER.error(CREAETE_TASK_FAIL);
            logoutPorting(token);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, CREAETE_TASK_FAIL));
        }
        BaseResponse baseResponse = gson.fromJson(httpResponse, BaseResponse.class);
        if (baseResponse.getStatus() != 0) {
            LOGGER.error(CREAETE_TASK_FAIL + " : " + baseResponse.getInfo());
            logoutPorting(token);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, CREAETE_TASK_FAIL));
        }
        LOGGER.info("Create task success");
        TaskResponse taskResponse = gson.fromJson(httpResponse, TaskResponse.class);
        String taskId = taskResponse.getData().getId();
        if (taskId == null) {
            LOGGER.error("Task id is empty");
            logoutPorting(token);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, CREAETE_TASK_FAIL));
        }
        LOGGER.info("Begin search task result");

        // search task result and wait task end
        waitTaskEnd(taskId, token);
        logoutPorting(token);
        moveReport(taskId, userId);
        try {
            FileUtil.deleteDir(new File(srcPath));
        } catch (IOException e) {
            LOGGER.error("Delete resource code failed");
            return Either.left(new FormatRespDto(Status.INTERNAL_SERVER_ERROR));
        }
        return Either.right(taskResponse);
    }

    private void waitTaskEnd(String taskId, String token) {
        while (!isTaskAnalysisEnd(taskId, token)) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error("Thread is interrupted.");
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    /**
     * whether task is end.
     *
     * @param taskId task id
     * @param token token
     * @return boolean
     */
    private boolean isTaskAnalysisEnd(String taskId, String token) {
        String getTaskStatusUrl = portingParamConfig.getTasksUrl() + taskId + "/";
        Map<String, String> taskHeaders = new HashMap<>();
        taskHeaders.put(AUTHORIZATION, token);
        String getTaskStatusResponse = httpUtil.httpsGet(getTaskStatusUrl, taskHeaders);
        if (getTaskStatusResponse == null) {
            LOGGER.error(GET_TASK_FAIL);
            return true;
        }
        TaskStatusAnalysis taskStatus = gson.fromJson(getTaskStatusResponse, TaskStatusAnalysis.class);
        if (taskStatus.getData().getStatus() == 2) {
            LOGGER.info("task analysis end");
            return true;
        }
        if (taskStatus.getData().getStatus() == 3) {
            LOGGER.error("task analysis error");
            return true;
        }
        LOGGER.warn("task analysising " + taskStatus.getData().getProgress() + "/100");
        return false;
    }

    /**
     * get task list by project id.
     *
     * @param userId user id
     * @return TasksResponse
     */
    public Either<FormatRespDto, TasksResponse> getTaskList(String userId) {
        LOGGER.info("Begin get tasks list");
        TasksResponse response = new TasksResponse();
        TasksData tasksData = new TasksData();
        try {
            File report = new File(
                portingParamConfig.getSrcPath() + transToUsername(userId) + File.separator + REPORT + File.separator);
            if (report.exists() && report.isDirectory()) {
                String[] reportName = report.list();
                if (reportName != null) {
                    tasksData.setTotalCount(reportName.length);
                    tasksData.setTaskList(Arrays.asList(reportName));
                }
            } else {
                tasksData.setTotalCount(0);
            }
            response.setStatus(0);
            response.setInfo("get tasks list success");
            response.setData(tasksData);
            LOGGER.info("Get tasks list success");
        } catch (Exception e) {
            LOGGER.error("Get tasks list failed " + e);
            response.setStatus(1);
            response.setInfo("get tasks list failed");
        }

        return Either.right(response);
    }

    /**
     * get a task by project id and task id.
     *
     * @param userId user id
     * @param taskId task id
     * @return TaskStatus
     */
    public Either<FormatRespDto, TaskStatus> getTask(String userId, String taskId) {
        String token = getToken(
            loginPorting(portingParamConfig.getAdminUsername(), portingParamConfig.getAdminPassword()));
        String url = portingParamConfig.getTasksUrl() + taskId + "/";
        Map<String, String> headers = new HashMap<>();
        headers.put(AUTHORIZATION, token);

        String httpResponse = httpUtil.httpsGet(url, headers);
        logoutPorting(token);
        if (httpResponse == null) {
            LOGGER.error(GET_TASK_FAIL);
            return Either.left(new FormatRespDto(Status.BAD_REQUEST, GET_TASK_FAIL));
        }
        String src = new StringBuilder(portingParamConfig.getSrcPath()).append(transToUsername(userId))
            .append(File.separator).append("src").toString();
        TaskStatus response = gson.fromJson(httpResponse.replaceAll(src, ""), TaskStatus.class);
        return Either.right(response);
    }

    /**
     * download task by project id and task id.
     *
     * @param userId user id
     * @param taskId task id
     */
    public void downloadTask(String userId, String taskId, HttpServletResponse response) {
        LOGGER.info("Begin download task report");
        String taskPath = new StringBuilder(portingParamConfig.getSrcPath()).append(transToUsername(userId))
            .append(File.separator).append(REPORT).append(File.separator).append(taskId).append(File.separator)
            .append("porting-advisor.csv").toString();
        File task = new File(taskPath);
        downFile(response, task, userId);
    }

    private void downFile(HttpServletResponse response, File file, String userId) {
        InputStream is = null;
        BufferedInputStream bs = null;
        OutputStream os = null;
        try {
            if (file.exists()) {
                //Set upHeaders
                response.setHeader("Content-Type", "application/octet-stream");
                //Set the name of the downloaded file-This method has solved the Chinese garbled problem
                response.setHeader("Content-Disposition", "attachment;filename=porting-advisor.csv");
                String src = new StringBuilder(portingParamConfig.getSrcPath()).append(transToUsername(userId))
                    .append(File.separator).append("src").toString();
                is = new ByteArrayInputStream(
                    FileUtil.readFileToString(file).replaceAll(src, "").getBytes(StandardCharsets.UTF_8));
                bs = new BufferedInputStream(is);
                os = response.getOutputStream();
                byte[] buffer = new byte[1024];
                int len = 0;
                while ((len = bs.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
            } else {
                LOGGER.error("task file is not exist");
                response.sendError(404, "no found resource");
            }
        } catch (IOException e) {
            LOGGER.error("download file,occur exception {}", e.getMessage());
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
                if (bs != null) {
                    bs.close();
                }
                if (os != null) {
                    os.flush();
                    os.close();
                }
            } catch (IOException e) {
                LOGGER.error("close stream,occur exception {}", e.getMessage());
            }
        }

    }

    /**
     * delete task by project id and task id.
     *
     * @param userId user id
     * @param taskId task id
     * @return BaseResponse
     */
    public Either<FormatRespDto, BaseResponse> deleteTask(String userId, String taskId) {
        LOGGER.info("Begin delete task report");
        String projectTaskPath = new StringBuilder(portingParamConfig.getSrcPath()).append(transToUsername(userId))
            .append(File.separator).append(REPORT).append(File.separator).append(taskId).append(File.separator)
            .toString();
        File projectTask = new File(projectTaskPath);
        File adminTask = new File(portingParamConfig.getReportPath() + taskId + File.separator);
        if (projectTask.exists() && projectTask.isDirectory()) {
            try {
                FileUtil.deleteDir(projectTask);
                FileUtil.deleteDir(adminTask);
                BaseResponse baseResponse = new BaseResponse();
                baseResponse.setStatus(0);
                baseResponse.setInfo("delete task success");
                LOGGER.info("Delete task success");
                return Either.right(baseResponse);
            } catch (IOException e) {
                LOGGER.error("IOException :delete task failed");
                return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Delete task IOException"));
            }
        }
        LOGGER.error("Delete task failed, task not exist or is not a directory");
        return Either.left(new FormatRespDto(Status.BAD_REQUEST, "Delete task failed"));
    }

    /**
     * login porting advisor.
     *
     * @param username username
     * @param verifyCode verify code of login.
     * @return login response
     */
    private String loginPorting(String username, String verifyCode) {
        LOGGER.info("Login porting advisor");
        String url = portingParamConfig.getUserUrl() + "login/";
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, CHARSET);
        LoginRequest loginRequest = new LoginRequest(username, verifyCode);
        return httpUtil.httpsPost(url, headers, gson.toJson(loginRequest));
    }

    /**
     * logout porting advisor.
     *
     * @param token token
     */
    private void logoutPorting(String token) {
        LOGGER.info("Logout porting advisor");
        if (token.length() < 1) {
            LOGGER.error("Token is empty");
            return;
        }
        Map<String, String> headers = new HashMap<>();
        headers.put(CONTENT_TYPE, CHARSET);
        headers.put(AUTHORIZATION, token);
        String url = portingParamConfig.getUserUrl() + "logout/";
        BaseResponse logoutResponse = gson.fromJson(httpUtil.httpsPost(url, headers, null), BaseResponse.class);
        if (logoutResponse != null) {
            LOGGER.info(logoutResponse.getInfo());
        }
    }

    /**
     * get token from login response.
     *
     * @param json login response json
     * @return token
     */
    private String getToken(String json) {
        BaseResponse baseResponse = gson.fromJson(json, BaseResponse.class);
        if (json == null || json.length() < 1) {
            return "";
        }
        // verify code error.
        if (baseResponse.getStatus() == 1) {
            LOGGER.info("Password error, need reset password");
            resetPassword();
            return getToken(loginPorting(portingParamConfig.getAdminUsername(), portingParamConfig.getAdminPassword()));
        }

        if (baseResponse.getStatus() == 0) {
            LOGGER.info("Login success, get token");
            LoginResponse response = gson.fromJson(json, LoginResponse.class);
            return response.getData().getToken();
        }
        LOGGER.error("Login failed");
        return "";
    }

    // trans project id to username
    private String transToUsername(String projectId) {
        return projectId;
    }

    /**
     * when the login porting advisor firstly, the verify code need to reset.
     */
    private void resetPassword() {
        String loginRes = loginPorting(portingParamConfig.getAdminUsername(), portingParamConfig.getAdminOldPassword());
        BaseResponse baseResponse = gson.fromJson(loginRes, BaseResponse.class);
        // first login, need reset verify code
        if (baseResponse.getStatus() == 2) {
            LOGGER.info("Login success, reset verify code.");
            LoginResponse response = gson.fromJson(loginRes, LoginResponse.class);
            String token = response.getData().getToken();
            Map<String, String> headers = new HashMap<>();
            headers.put(CONTENT_TYPE, CHARSET);
            headers.put(AUTHORIZATION, token);
            Map<String, String> bodyMap = new HashMap<>();
            bodyMap.put("old_password", portingParamConfig.getAdminOldPassword());
            bodyMap.put("new_password", portingParamConfig.getAdminPassword());
            bodyMap.put("confirm_password", portingParamConfig.getAdminPassword());
            String url = portingParamConfig.getUserUrl() + "1/resetpassword/";
            httpUtil.httpsPost(url, headers, gson.toJson(bodyMap));
            logoutPorting(token);
        }
    }

    /**
     * move report from admin to project dir.
     *
     * @param reportId report id
     * @param userId user id
     */
    private void moveReport(String reportId, String userId) {
        File projectDir = new File(
            portingParamConfig.getSrcPath() + transToUsername(userId) + File.separator + REPORT + File.separator);
        if (!projectDir.exists() || !projectDir.isDirectory()) {
            boolean res = projectDir.mkdirs();
            if (!res) {
                LOGGER.error("create project directory error.");
            }
        }

        String reportPath = portingParamConfig.getReportPath() + reportId + File.separator;
        File reportDir = new File(reportPath);
        if (reportDir.exists() && reportDir.isDirectory()) {
            try {
                FileUtil.copyDirToDir(reportDir, projectDir);
            } catch (IOException e) {
                LOGGER.error("copy report dir failed");
            }
        }
    }
}
