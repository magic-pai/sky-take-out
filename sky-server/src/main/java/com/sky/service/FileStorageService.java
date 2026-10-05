package com.sky.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 保存上传文件并返回文件名
     * @param file
     * @return
     */
    String store(MultipartFile file);

}
