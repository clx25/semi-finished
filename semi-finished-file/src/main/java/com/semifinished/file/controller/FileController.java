package com.semifinished.file.controller;


import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.utils.Assert;
import com.semifinished.core.utils.MapUtils;
import com.semifinished.file.pojo.FileInfo;
import com.semifinished.file.service.FileService;
import lombok.AllArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 文件相关操作
 */

@CrossOrigin
@RestController
@AllArgsConstructor
public class FileController {

    private final FileService fileService;


    /**
     * 图片上传
     *
     * @param file 文件数据
     * @return 操作结果与文件名
     * @throws IOException 文件保存失败
     */
    @PostMapping("upload/image")
    private Result uploadImage(MultipartFile file) throws IOException {
        Assert.isNull(file, () -> new ParamsException("缺少文件"));
        return fileService.uploadImage(file);
    }

    /**
     * 文件上传
     *
     * @param file 文件数据
     * @return 操作结果与文件名
     * @throws IOException 文件保存失败
     */
    @PostMapping("upload")
    private Result upload(MultipartFile file) throws IOException {
        Assert.isNull(file, () -> new ParamsException("缺少文件"));
        return fileService.upload(file);
    }


    /**
     * 分片上传文件
     *
     * @param file 文件内容
     * @param info 文件信息
     * @return 操作结果
     * @throws IOException 文件保存失败
     */
    @PostMapping("uploadChunk")
    public Result uploadChunk(MultipartFile file, @ModelAttribute @Validated(FileInfo.uploadChunk.class) FileInfo info) throws IOException {
        Assert.isNull(file, () -> new ParamsException("缺少文件"));
        String fileName = fileService.uploadChunk(file, info);
        return Result.success(fileName);
    }

    /**
     * 检查分片是否上传
     *
     * @param info 文件信息
     * @return 分片是否上传
     */
    @PostMapping("checkChunk")
    public Result checkChunk(@RequestBody FileInfo info) {
        return fileService.checkChunk(info);
    }

    /**
     * 判断文件是否存在
     *
     * @param info 文件信息
     * @return 文件是否上传
     */
    @PostMapping("checkUpload")
    public Result checkUpload(@RequestBody @Validated(FileInfo.checkUpload.class) FileInfo info) {
        return fileService.checkUpload(info);
    }

    /**
     * 检查文件完整性，并合并文件，如果文件不完整，则返回不完整的分片序号
     * todo 添加校验注解
     *
     * @param info 文件信息
     * @return 操作成功，或者不完整文件的分片序号
     */
    @PostMapping("mergeFile")
    public Result mergeFile(@RequestBody @Validated(FileInfo.mergeFile.class) FileInfo info) {
        return fileService.mergeFile(info);
    }


    /**
     * 获取文件
     *
     * @param fileName 文件名
     * @return 文件数据
     * @throws IOException 文件操作异常
     */
    @GetMapping(value = "/file/{fileName}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public FileSystemResource file(@PathVariable String fileName, HttpServletResponse response) {
        response.setHeader(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"");

        return fileService.file(fileName);
    }


    @PostMapping("/edit")
    public Map<String, Integer> get(@RequestBody String msg) {
        return MapUtils.of("error", 0);
    }

}
