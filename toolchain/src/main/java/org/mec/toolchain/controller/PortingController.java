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

package org.mec.toolchain.controller;

import com.spencerwi.either.Either;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.File;
import javax.servlet.http.HttpServletResponse;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.eclipse.jetty.http.HttpStatus;
import org.mec.toolchain.model.dto.ErrorRespDto;
import org.mec.toolchain.model.dto.FormatRespDto;
import org.mec.toolchain.model.porting.BaseResponse;
import org.mec.toolchain.model.porting.BaseTask;
import org.mec.toolchain.model.porting.GetSrcResponse;
import org.mec.toolchain.model.porting.TaskResponse;
import org.mec.toolchain.model.porting.TaskStatus;
import org.mec.toolchain.model.porting.TasksResponse;
import org.mec.toolchain.model.porting.UploadSrcResponse;
import org.mec.toolchain.service.PortingService;
import org.mec.toolchain.util.ResponseUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

@RestSchema(schemaId = "porting")
@RequestMapping("/mec/toolchain/v1/porting")
@Controller
public class PortingController {

    @Autowired
    private PortingService portingService;

    @ApiOperation(value = "upload src code", response = UploadSrcResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = HttpStatus.OK_200, message = "OK", response = UploadSrcResponse.class),
        @ApiResponse(code = HttpStatus.BAD_REQUEST_400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{userId}", method = RequestMethod.POST, consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UploadSrcResponse> uploadSourceCode(
        @ApiParam(value = "file", required = true) @RequestPart("file") MultipartFile sourceCode,
        @ApiParam(value = "userId", required = true) @PathVariable("userId") String userId) {
        Either<FormatRespDto, UploadSrcResponse> either = portingService.uploadSourceCode(userId, sourceCode);
        return ResponseUtil.buildResponse(either);
    }

    @ApiOperation(value = "get src code", response = GetSrcResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = GetSrcResponse.class),
        @ApiResponse(code = HttpStatus.BAD_REQUEST_400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<GetSrcResponse> getSourceCode(
        @ApiParam(value = "userId", required = true) @PathVariable("userId") String userId) {
        Either<FormatRespDto, GetSrcResponse> either = portingService.getSourceCode(userId);
        return ResponseUtil.buildResponse(either);
    }

    @ApiOperation(value = "delete src code", response = BaseResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = BaseResponse.class),
        @ApiResponse(code = HttpStatus.BAD_REQUEST_400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{userId}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<BaseResponse> deleteSourceCode(
        @ApiParam(value = "userId", required = true) @PathVariable("userId") String userId) {
        Either<FormatRespDto, BaseResponse> either = portingService.deleteSourceCode(userId);
        return ResponseUtil.buildResponse(either);
    }

    @ApiOperation(value = "create task", response = TaskResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = TaskResponse.class),
        @ApiResponse(code = HttpStatus.BAD_REQUEST_400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{userId}/tasks", method = RequestMethod.POST,
        consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<TaskResponse> createTask(
        @ApiParam(value = "taskInfo", required = true) @RequestBody BaseTask taskInfo,
        @ApiParam(value = "userId", required = true) @PathVariable("userId") String userId) {
        Either<FormatRespDto, TaskResponse> either = portingService.createTask(userId, taskInfo);
        return ResponseUtil.buildResponse(either);
    }

    @ApiOperation(value = "get task list", response = TasksResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = TasksResponse.class),
        @ApiResponse(code = HttpStatus.BAD_REQUEST_400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{userId}/tasks", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<TasksResponse> getTaskList(
        @ApiParam(value = "userId", required = true) @PathVariable("userId") String userId) {
        Either<FormatRespDto, TasksResponse> either = portingService.getTaskList(userId);
        return ResponseUtil.buildResponse(either);
    }

    @ApiOperation(value = "get task", response = TaskStatus.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = TaskStatus.class),
        @ApiResponse(code = HttpStatus.BAD_REQUEST_400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{userId}/tasks/{id}", method = RequestMethod.GET,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<TaskStatus> getTask(
        @ApiParam(value = "userId", required = true) @PathVariable("userId") String userId,
        @ApiParam(value = "id", required = true) @PathVariable("id") String taskId) {
        Either<FormatRespDto, TaskStatus> either = portingService.getTask(userId, taskId);
        return ResponseUtil.buildResponse(either);
    }

    /**
     * down load file.
     */
    @ApiOperation(value = "download task", response = File.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = File.class)
    })
    @RequestMapping(value = "/{userId}/tasks/{id}/download", method = RequestMethod.GET)
    public void downloadTask(@ApiParam(value = "userId", required = true) @PathVariable("userId") String userId,
        @ApiParam(value = "id", required = true) @PathVariable("id") String taskId) {
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder
            .getRequestAttributes();
        if (servletRequestAttributes != null) {
            HttpServletResponse response = servletRequestAttributes.getResponse();
            if (response != null) {
                portingService.downloadTask(userId, taskId, response);
            }
        }
    }

    @ApiOperation(value = "delete task", response = BaseResponse.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = BaseResponse.class),
        @ApiResponse(code = HttpStatus.BAD_REQUEST_400, message = "Bad Request", response = ErrorRespDto.class)
    })
    @RequestMapping(value = "/{userId}/tasks/{id}", method = RequestMethod.DELETE,
        produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<BaseResponse> deleteTask(
        @ApiParam(value = "userId", required = true) @PathVariable("userId") String userId,
        @ApiParam(value = "id", required = true) @PathVariable("id") String taskId) {
        Either<FormatRespDto, BaseResponse> either = portingService.deleteTask(userId, taskId);
        return ResponseUtil.buildResponse(either);
    }

}