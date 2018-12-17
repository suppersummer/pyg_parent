package com.pyg.shop.controller;


import entity.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import util.FastDFSClient;
@RestController
public class UploadController {

    @Value ("${FILE_SERVER_URL}")
    private String FILE_SERVER_URL;
    @RequestMapping("uploadFile")
    public Result upload(MultipartFile file){
        //1.获取文件的扩展名
        String originalFilename = file.getOriginalFilename ();
        //截取到文件的尾
        String extName = originalFilename.substring (originalFilename.lastIndexOf (".") + 1);
        try {
            //创建一个fastDFS客户端，把配置文件传进去
            FastDFSClient fastDFSClient = new FastDFSClient ("classpath:config/fdfs_client.conf");
            //执行上传处理
            String path = fastDFSClient.uploadFile (file.getBytes (), extName);
        //拼接ip地址和返回的url，拼装成完整的url
            String url=FILE_SERVER_URL+path;
            return new Result (true,url);
        } catch (Exception e) {
            e.printStackTrace ();
            return new Result (false,"上传失败");
        }



    }
}
