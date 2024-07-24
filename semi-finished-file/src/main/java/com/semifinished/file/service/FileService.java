package com.semifinished.file.service;

import com.semifinished.core.exception.ParamsException;
import com.semifinished.core.pojo.Result;
import com.semifinished.core.utils.Assert;
import com.semifinished.file.config.FileProperties;
import com.semifinished.file.exception.FileUploadException;
import com.semifinished.file.pojo.FileInfo;
import com.semifinished.file.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileService {

    private final FileProperties fileProperties;

    /**
     * 上传图片
     *
     * @param file 图片文件
     * @return 操作结果，包含保存的文件名
     * @throws IOException 文件保存失败
     */
    public Result uploadImage(MultipartFile file) throws IOException {
        //对图片类型进行校验，对限制支持的图片类型进行校验
        Tika tika = new Tika();

        String detect = tika.detect(file.getInputStream());

        Assert.isFalse(detect.contains("image/"), () -> new ParamsException("上传的文件不是图片类型"));

        List<String> imageType = fileProperties.getImageType();
        String type = detect.replace("image/", "");

        Assert.isTrue(imageType != null && !imageType.isEmpty() && imageType.stream().noneMatch(s -> s.equals(type)), () -> new ParamsException("仅支持以下图片类型：" + String.join(",", imageType)));
        String fileName = save(file, type);

        return Result.success(fileName);
    }

    /**
     * 上传文件
     *
     * @param file 文件
     * @return 操作结果，包含保存的文件名
     * @throws FileUploadException 没有该路径，并且创建失败
     * @throws IOException         文件保存失败
     */
    public Result upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = null;
        if (StringUtils.hasText(originalFilename)) {
            String[] name = originalFilename.split("\\.");
            type = name[name.length - 1];
        }

        String fileName = save(file, type);
        return Result.success(fileName);
    }

    /**
     * 保存文件
     *
     * @param file 文件
     * @param type 文件类型（后缀）
     * @return 文件名称（包含类型）
     * @throws IOException         文件保存失败
     * @throws FileUploadException 没有文件路径，并且创建失败
     */
    public String save(MultipartFile file, String type) throws IOException {
        String md5 = FileUtil.md5(file.getBytes());

        String fileName = md5 + (StringUtils.hasText(type) ? ("." + type) : "");
        String filePath = getPath() + File.separator + fileName;
        File f = new File(filePath);
        if (FileUtil.fileExists(f)) {
            file.transferTo(f);
        }
        return fileName;
    }


    /**
     * 获取文件
     *
     * @param fileName 文件名
     * @return 文件
     */
    public FileSystemResource file(String fileName) throws IOException {
        String path = getPath();
        File file = new File(path + File.separator + fileName);
        if (!file.exists()) {
            throw new ParamsException("文件不存在");
        }
        return new FileSystemResource(file);
    }

    /**
     * 获取文件保存路径，如果没有配置路径，那么默认使用jar包所在路径
     *
     * @return 文件保存路径
     */
    private String getPath() {
        String path = fileProperties.getFilePath();
        if (!StringUtils.hasText(path)) {
            path = FileUtil.getDefaultPath();
        }
        return path + File.separator;
    }

    /**
     * 分片上传
     *
     * @param file 文件片段
     * @param info 文件信息
     * @return 上传成功的分片名称
     * @throws IOException 上传失败
     */
    public String uploadChunk(MultipartFile file, FileInfo info) throws IOException {

        String hash = info.getHash();
        String path = getPath();
        String fileName = hash + ".part" + info.getChunkNum();
        File partFile = new File(path + File.separator + fileName);
        if (partFile.exists()) {
            if (partFile.length() == info.getChunkSize()) {
                return fileName;
            }
            Assert.isFalse(partFile.delete(), () -> new FileUploadException("文件上传失败"));
        }

        file.transferTo(partFile);

        return fileName;
    }


    /**
     * 检查文件完整性，并合并文件，如果文件不完整，则返回不完整的分片序号
     *
     * @param info 文件信息
     * @return 操作成功，或者不完整文件的分片序号
     */
    public Result mergeFile(FileInfo info) {
        List<Integer> incomplete = new ArrayList<>();
        List<File> readyFile = new ArrayList<>();

        String path = getPath();
        String hash = info.getHash();

        long[] chunksSize = info.getChunksSize();
        for (int i = 1; i <= chunksSize.length; i++) {
            File partFile = new File(path, hash + ".part" + i);
            readyFile.add(partFile);
            if (!partFile.exists()) {
                incomplete.add(i);
                continue;
            }
            if (partFile.length() != chunksSize[i - 1]) {
                incomplete.add(i);
                Assert.isFalse(partFile.delete(), () -> new FileUploadException("文件删除失败"));
            }
        }
        if (incomplete.isEmpty()) {
            mergeChunks(path, hash, info.getType(), readyFile);
            return Result.success();
        }

        return Result.success(incomplete);
    }

    /**
     * 合并分片
     *
     * @param path       文件路径
     * @param hash       文件哈希
     * @param type       文件类型
     * @param readyFiles 准备好合并的文件
     */
    private void mergeChunks(String path, String hash, String type, List<File> readyFiles) {
        File targetFile = new File(path, hash + "." + type);
        try (FileOutputStream fos = new FileOutputStream(targetFile, true)) {
            for (File readyFile : readyFiles) {
                Files.copy(readyFile.toPath(), fos);
                Assert.isFalse(readyFile.delete(), () -> new FileUploadException("文件删除失败：" + readyFile.getAbsolutePath()));
            }
        } catch (IOException e) {
            throw new FileUploadException("文件合并失败", e);
        }
    }

    /**
     * 检查是否上传
     *
     * @param info 文件信息
     * @return 文件是否上传
     */
    public Result checkUpload(FileInfo info) {
        String path = getPath();
        File file = new File(path, info.getHash() + "." + info.getType());
        return Result.success(file.exists());
    }
}
