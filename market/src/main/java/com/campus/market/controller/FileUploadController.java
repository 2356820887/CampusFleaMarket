package com.campus.market.controller;

import com.campus.market.common.Result;
import com.campus.market.utils.AliOSSUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
public class FileUploadController {
    @Autowired
    private AliOSSUtils aliOSSUtils;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        //调用阿里云OSS工具类进行文件上传
        String url = aliOSSUtils.upload(file);
        return Result.success(url);
    }
}
