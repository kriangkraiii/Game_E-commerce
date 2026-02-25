package com.ecom.service.impl;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.ecom.service.FileService;

@Service
public class FileServiceImpl implements FileService {

	@Autowired
	public AmazonS3 amazonS3;

	@Value("${aws.s3.bucket.category}")
	private String categoryBucket;

	@Value("${aws.s3.bucket.product}")
	private String productBucket;

	@Value("${aws.s3.bucket.profile}")
	private String profileBucket;

	@Value("${aws.s3.bucket.gamefile}")
	private String gamefileBucket;

	@Override
	public Boolean uploadFileS3(MultipartFile file, Integer bucketType) {

		String bucketName = null;

		try {

			if (bucketType == 1) {
				bucketName = categoryBucket;
			} else if (bucketType == 2) {

				bucketName = productBucket;
			} else if (bucketType == 3) {
				bucketName = profileBucket;
			} else if (bucketType == 6) {
				bucketName = gamefileBucket;
			}

			String fileName = file.getOriginalFilename();
			InputStream inputStream = file.getInputStream();
			ObjectMetadata objectMetadata = new ObjectMetadata();
			objectMetadata.setContentType(file.getContentType());
			objectMetadata.setContentLength(file.getSize());

			PutObjectRequest putObjectRequest = new com.amazonaws.services.s3.model.PutObjectRequest(bucketName,
					fileName, inputStream, objectMetadata);
			PutObjectResult saveData = amazonS3.putObject(putObjectRequest);
			if (!ObjectUtils.isEmpty(saveData)) {
				return true;
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		return false;
	}

	@Override
	public InputStream downloadFileS3(String key, Integer bucketType) {
		String bucketName = getBucketNameByType(bucketType);
		if (bucketName == null || key == null)
			return null;

		try {
			if (amazonS3.doesObjectExist(bucketName, key)) {
				return amazonS3.getObject(bucketName, key).getObjectContent();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean fileExistsS3(String key, Integer bucketType) {
		String bucketName = getBucketNameByType(bucketType);
		if (bucketName == null || key == null)
			return false;

		try {
			return amazonS3.doesObjectExist(bucketName, key);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	private String getBucketNameByType(Integer bucketType) {
		if (bucketType == 1)
			return categoryBucket;
		if (bucketType == 2)
			return productBucket;
		if (bucketType == 3)
			return profileBucket;
		if (bucketType == 6)
			return gamefileBucket;
		return null;
	}
}
