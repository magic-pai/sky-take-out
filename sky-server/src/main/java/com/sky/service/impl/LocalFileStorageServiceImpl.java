package com.sky.service.impl;

import com.sky.constant.MessageConstant;
import com.sky.exception.BaseException;
import com.sky.properties.UploadProperties;
import com.sky.service.FileStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class LocalFileStorageServiceImpl implements FileStorageService {

    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024;

    private static final Set<String> ALLOWED_EXTENSIONS =
            new HashSet<>(Arrays.asList("jpg", "jpeg", "png"));

    @Autowired
    private UploadProperties uploadProperties;

    /**
     * 保存上传文件到本地目录
     *
     * @param file
     * @return
     */
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BaseException("上传文件大小不能超过2M");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (extension == null) {
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }
        extension = extension.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BaseException("仅支持PNG、JPEG、JPG格式的图片");
        }

        Path uploadDirectory = Paths.get(uploadProperties.getPath())
                .toAbsolutePath()
                .normalize();
        String filename = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        Path destination = uploadDirectory.resolve(filename).normalize();

        if (!destination.startsWith(uploadDirectory)) {
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }

        try {
            Files.createDirectories(uploadDirectory);
            file.transferTo(destination);
        } catch (IOException ex) {
            throw new BaseException(MessageConstant.UPLOAD_FAILED);
        }

        return filename;
    }

}
