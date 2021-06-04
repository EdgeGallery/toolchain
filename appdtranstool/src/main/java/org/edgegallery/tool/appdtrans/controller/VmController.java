/*
 *  Copyright 2021 Huawei Technologies Co., Ltd.
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

package org.edgegallery.tool.appdtrans.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import java.io.FileNotFoundException;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.fileupload.servlet.ServletFileUpload;
import org.edgegallery.tool.appdtrans.controller.dto.request.Chunk;
import org.edgegallery.tool.appdtrans.controller.dto.request.TransVmPkgReqDto;
import org.edgegallery.tool.appdtrans.controller.dto.response.ErrorRespDto;
import org.edgegallery.tool.appdtrans.controller.dto.response.ResponseObject;
import org.edgegallery.tool.appdtrans.facade.VmServiceFacade;
import org.edgegallery.tool.appdtrans.service.VmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api(value = "appdtranstool api system")
@Validated
@RequestMapping("/mec/appdtranstool/v1/vm")
@RestController
public class VmController {
    @Autowired
    private VmServiceFacade vmServiceFacade;

    /**
     * get appd templates types.
     *
     */
    @GetMapping(value = "/templates", produces = MediaType.APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = ResponseObject.class)
    })
    public ResponseEntity<List<String>> getVmTemplates() {
        return ResponseEntity.ok(vmServiceFacade.getVmTemplates());
    }

    /**
     * upload file.
     */
    @ApiOperation(value = "upload image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = ResponseObject.class)
    })
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<ResponseObject> uploadFile(HttpServletRequest request, Chunk chunk) {
        return vmServiceFacade.uploadFile(ServletFileUpload.isMultipartContent(request), chunk);
    }

    /**
     * merge file.
     */
    @ApiOperation(value = "merge image", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = ResponseObject.class)
    })
    @RequestMapping(value = "/apps/merge", method = RequestMethod.GET)
    public ResponseEntity<ResponseObject> merge(
        @RequestParam(value = "fileName") String fileName,
        @RequestParam(value = "guid") String guid,
        @RequestParam(value = "fileType") String fileType) {
        return vmServiceFacade.merge(fileName, guid, fileType);
    }

    /**
     * transform app package.
     */
    @ApiOperation(value = "transform app package", response = ResponseEntity.class)
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "OK", response = ResponseEntity.class),
        @ApiResponse(code = 400, message = "Bad Request", response = ErrorRespDto.class),
        @ApiResponse(code = 500, message = "resource grant " + "error", response = ResponseObject.class)
    })
    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity<InputStreamResource> transVmPackage(@RequestBody TransVmPkgReqDto dto) {
        return vmServiceFacade.transVmPackage(dto);
    }
}
