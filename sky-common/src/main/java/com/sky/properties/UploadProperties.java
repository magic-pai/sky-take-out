package com.sky.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sky.upload")
@Data
public class UploadProperties {

    /**
     * 本地文件存储目录，相对路径以应用工作目录为基准
     */
    private String path = "uploads";

}
