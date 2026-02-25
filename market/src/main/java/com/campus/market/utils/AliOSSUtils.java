package com.campus.market.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.campus.market.config.AliOSSConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云 OSS 工具类
 */

@Component
public class AliOSSUtils {

    @Autowired // 将OSS的配置注入进来，后面需要用到
    private AliOSSConfig aliOSSConfig;

    /**
     * 以下是一个实现上传文件到OSS的方法
     */

    public String upload(MultipartFile file) throws IOException {

        // 获取阿里云OSS参数
        String endpoint = aliOSSConfig.getEndpoint();
        String accessKeyId = aliOSSConfig.getAccessKeyId();
        String accessKeySecret = aliOSSConfig.getAccessKeySecret();
        String bucketName = aliOSSConfig.getBucketName();

        // 获取上传的文件的输入流
        InputStream inputStream = file.getInputStream();

        // 避免文件覆盖，需要使用UUID将文件重命名
        String originalFilename = file.getOriginalFilename();
        String fileName = null;
        if (originalFilename != null) {
            fileName = UUID.randomUUID() + originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // 上传文件到 OSS
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);
        ossClient.putObject(bucketName, fileName, inputStream);

        // 文件访问路径
        String domain = endpoint;
        if (endpoint.contains("://")) {
            domain = endpoint.split("://")[1];
        }
        String url = "https://" + bucketName + "." + domain + "/" + fileName;

        // 关闭ossClient
        ossClient.shutdown();

        // 把上传到oss的路径返回
        return url;

    }
}
