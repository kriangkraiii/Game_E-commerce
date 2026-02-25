package com.ecom.service;

import org.springframework.web.multipart.MultipartFile;

public interface FileService {

	public Boolean uploadFileS3(MultipartFile file, Integer bucketType);

	public java.io.InputStream downloadFileS3(String key, Integer bucketType);

	public boolean fileExistsS3(String key, Integer bucketType);

}