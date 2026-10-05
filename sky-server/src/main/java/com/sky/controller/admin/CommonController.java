package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil ;
    /**
     * 文件上传
     * @param file
     * @return
     */
    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file){
        //接收文件
        log.info("文件上传:{}",file);

        //上传文件到服务器
        //给文件命名基本都用uuid自生成，为防止重命名
        //但是光uuid命名还不够，因为还有jpg这样的后缀，所以我们要获取file的名字作为string，然后获取后三位
        //最后拼接到uuid上
        try {
            //原始文件名 xxx.png
            String originalFilename = file.getOriginalFilename();

            //截取后缀
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));

            //uuid
            String objectName =  UUID.randomUUID().toString()+extension ;

            //文件请求路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);

            return Result.success(filePath) ;
        } catch (IOException e) {
            log.info("文件上传失败：{}",e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }



}
