package com.grace.staybooking.service;

import com.grace.staybooking.exception.GCSUploadException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import com.google.cloud.storage.Acl;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;

@Service
public class ImageStorageService {

  @Value("${gcs.bucket}") //会去检查bucket的名字，在application.properties里
  private String bucketName;

  private final Storage storage;//作为一个dependency加入field

  public ImageStorageService(Storage storage) {
    this.storage = storage;
  }

  /*
  Save 文件
  一个文件也分成多个部分发到saver端，memory
   */
  public String save(MultipartFile file) throws GCSUploadException {
    String filename = UUID.randomUUID().toString();
    BlobInfo blobInfo = null;
    try {
      blobInfo = storage.createFrom(
          BlobInfo
              .newBuilder(bucketName, filename)
              .setContentType("image/jpeg")
              //Acl:Access Control List for buckets or blobs.
              .setAcl(new ArrayList<>(Arrays.asList(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER))))
              .build(),
          file.getInputStream());
    } catch (IOException exception) {
      throw new GCSUploadException("Failed to upload file to GCS");
    }

    //存在stayImage里
    return blobInfo.getMediaLink();
  }

}
