package com.video.core.utils;

import com.video.core.config.MinioConfig;
import io.minio.*;
import io.minio.errors.MinioException;
import io.minio.messages.Item;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.compress.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Nonnull;
import javax.annotation.Resource;
import java.io.*;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@Slf4j
public class MinioUtils {

    @Autowired
    private MinioConfig minioConfig;

    @Resource
    private MinioClient minioClient;

    @Value("${directory.chunk}")
    private String CHUNK_DIRECTORY;

    public String uploadImage(@Nonnull MultipartFile file, @Nonnull String type) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String ext = "." + FileNameUtils.getExtension(originalFilename);
        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ext;

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        String filePathName = date + "/img/" + type + "/" + fileName;

        try {
            InputStream inputStream = file.getInputStream();

            PutObjectArgs objectArgs = PutObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .object(filePathName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .build();

            minioClient.putObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return minioConfig.getMinioEndpoint() + "/" + minioConfig.getMinioBucketName() + "/" + filePathName;
    }

    public void deleteFiles(@Nonnull String prefix) {
        if (prefix.equals("")) {
            log.warn("你正试图删除整个bucket，已拒绝该危险操作");
            return;
        }

        try {
            Iterable<Result<Item>> results = minioClient.listObjects(
                    ListObjectsArgs.builder().bucket(minioConfig.getMinioBucketName()).prefix(prefix).build()
            );
            List<String> keys = new ArrayList<>();

            for (Result<Item> result : results) {
                Item item = result.get();
                keys.add(item.objectName());
            }
            if (!keys.isEmpty()) {
                for (String key : keys) {
                    minioClient.removeObject(
                        RemoveObjectArgs.builder()
                            .bucket(minioConfig.getMinioBucketName())
                            .object(key)
                            .build()
                    );
                    try {
                        String deleteObj = URLDecoder.decode(key, "UTF-8");
                        log.info("删除文件：" + deleteObj);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (MinioException e) {
            log.error("MinIO出错了:" + e.getMessage());
        } catch (Exception e) {
            log.error("操作出错了:" + e.getMessage());
        }
    }

    public String appendUploadVideo(@NonNull String hash) throws IOException, InvalidKeyException, NoSuchAlgorithmException, IllegalArgumentException, MinioException {
        String uuid = System.currentTimeMillis() + UUID.randomUUID().toString().replace("-", "");
        String fileName = uuid + ".mp4";
        // 完整路径名
        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date()).replace("-", "");
        String filePathName = date + "/video/" + fileName;

        int chunkIndex = 0;
        List<ComposeSource> sources = new ArrayList<>();
        while (true) {
            File chunkFile = new File(CHUNK_DIRECTORY + hash + "-" + chunkIndex);
            if (!chunkFile.exists()) {
                if (chunkIndex == 0) {
                    log.error("没找到任何相关分片文件");
                    return null;
                }
                break;
            }
            // 读取分片数据
            try (FileInputStream fis = new FileInputStream(chunkFile)) {
                byte[] buffer = new byte[(int) chunkFile.length()];
                fis.read(buffer);

                // 上传分片数据
                String chunkObjectName = filePathName + ".part" + chunkIndex;
                minioClient.putObject(
                    PutObjectArgs.builder()
                        .bucket(minioConfig.getMinioBucketName())
                        .object(chunkObjectName)
                        .stream(new ByteArrayInputStream(buffer), buffer.length, -1)
                        .contentType("video/mp4")
                        .build()
                );
                sources.add(ComposeSource.builder().bucket(minioConfig.getMinioBucketName()).object(chunkObjectName).build());
            } catch (MinioException e) {
                log.error("MinIO出错了:" + e.getMessage());
                throw e;
            }
            chunkFile.delete(); // 上传完后删除分片
            chunkIndex++;
        }

        // 合并分片
        try {
            minioClient.composeObject(
                ComposeObjectArgs.builder()
                    .bucket(minioConfig.getMinioBucketName())
                    .object(filePathName)
                    .sources(sources)
                    .build()
            );

            for (ComposeSource source : sources) {
                minioClient.removeObject(
                    RemoveObjectArgs.builder()
                        .bucket(source.bucket())
                        .object(source.object())
                        .build()
                );
            }
            
        } catch (MinioException e) {
            log.error("MinIO合并出错了:" + e.getMessage());
            throw e;
        }

        return minioConfig.getMinioEndpoint() + "/" + minioConfig.getMinioBucketName() + "/" + filePathName;
    }
}
