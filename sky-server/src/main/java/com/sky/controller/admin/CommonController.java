package com.sky.controller.admin;

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * 文件上传通用接口
 */
@RestController
@RequestMapping("/admin/common")
@Slf4j
public class CommonController {
    
    @Resource(name = "aliOssUtil")
    private AliOssUtil aliOssUtil;
    
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file){
        log.info("文件上传");
        try {
            //获取原始文件名
            String originalFilename = file.getOriginalFilename();
            //截取扩展名
            if(originalFilename == null){
                log.error("文件名为空");
                return Result.error("文件名为空");
            }
            String extention = originalFilename.substring(originalFilename.lastIndexOf("."));
            //构造文件名
            String objectName = UUID.randomUUID().toString()+extention;
            //上传，接收返回的访问路径
            String filePath = aliOssUtil.upload(file.getBytes(), objectName);
            return Result.success(filePath);
        } catch (Exception e) {
            log.error("文件上传错误：{}",e.getMessage());
            return Result.error(MessageConstant.UPLOAD_FAILED);
        }
    }
}
