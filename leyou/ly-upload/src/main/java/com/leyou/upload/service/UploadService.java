package com.leyou.upload.service;

import com.github.tobato.fastdfs.domain.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import com.leyou.upload.config.UploadProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Service
@Slf4j
@EnableConfigurationProperties(UploadProperties.class)
public class UploadService {
    @Autowired
    private FastFileStorageClient storageClient;

    @Autowired
    private UploadProperties props;

    //1.定义一个支持的文件类型集合
//    private static final List<String> suffixes = Arrays.asList("image/png", "image/jpeg","image/bmp");

    /**
     * 上传文件
     * @param file
     * @return
     */
    public String uploadImage(MultipartFile file) {

        //一、校验文件
        //2.校验文件类型
            //得到请求类型
        try{
            String contentType = file.getContentType();
            //判断在定义的类型里面是否包含:不包含直接抛出异常
            System.out.println(props.getAllowTypes());
            System.out.println(contentType);
            if(!props.getAllowTypes().contains(contentType)){
                log.error("上传失败，文件类型不匹配",contentType);
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }
            //3.校验文件内容
            //读取文件流
            BufferedImage read = ImageIO.read(file.getInputStream());
            //判断读取是否为空：为空抛出异常
            if(read == null){
                //失效异常
                log.error("上传失败，文件内容不符合要求");
                throw new LyException(ExceptionEnum.INVALID_FILE_TYPE);
            }

        //二、上传到FastDFC
            //截取后缀名
            String extension = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
            StorePath storePath = storageClient.uploadFile(file.getInputStream(), file.getSize(), extension, null);
            //3.返回路径:回写
            return props.getBaseUrl()+storePath.getFullPath();
        } catch (IOException e) {
            //若上传失败 打印日志
            log.error("文件上传失败",e);
            throw new LyException(ExceptionEnum.FILE_UPLOAD_ERROR);
        }
    }
}
