package com.studentbnb.listing_service.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Upload image to Cloudinary
     * @param file MultipartFile to upload
     * @return URL of uploaded image
     * @throws IOException if upload fails
     */
    public String uploadImage(MultipartFile file) throws IOException {
        log.info("Uploading image to Cloudinary: {}", file.getOriginalFilename());

        // Validate file
        validateImage(file);

        // Generate unique public ID for the image
        String publicId = "studentbnb/listings/" + UUID.randomUUID();

        // Upload to Cloudinary with options
        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                "public_id", publicId,
                "folder", "studentbnb/listings",
                "resource_type", "auto",
                "transformation", new com.cloudinary.Transformation()
                        .width(1200)
                        .height(900)
                        .crop("limit")
                        .quality("auto:good")
                        .fetchFormat("auto")
        ));

        String url = (String) uploadResult.get("secure_url");
        log.info("Image uploaded successfully: {}", url);

        return url;
    }

    /**
     * Upload multiple images to Cloudinary
     * @param files Array of MultipartFiles to upload
     * @return Array of URLs of uploaded images
     * @throws IOException if upload fails
     */
    public String[] uploadImages(MultipartFile[] files) throws IOException {
        log.info("Uploading {} images to Cloudinary", files.length);

        String[] urls = new String[files.length];
        for (int i = 0; i < files.length; i++) {
            urls[i] = uploadImage(files[i]);
        }

        return urls;
    }

    /**
     * Delete image from Cloudinary
     * @param imageUrl URL of image to delete
     * @throws IOException if deletion fails
     */
    public void deleteImage(String imageUrl) throws IOException {
        log.info("Deleting image from Cloudinary: {}", imageUrl);

        try {
            // Extract public ID from URL
            String publicId = extractPublicId(imageUrl);

            if (publicId != null) {
                Map result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
                log.info("Image deleted successfully: {}", result);
            } else {
                log.warn("Could not extract public ID from URL: {}", imageUrl);
            }
        } catch (Exception e) {
            log.error("Error deleting image: {}", e.getMessage());
            throw new IOException("Failed to delete image from Cloudinary", e);
        }
    }

    /**
     * Validate image file
     * @param file MultipartFile to validate
     * @throws IllegalArgumentException if file is invalid
     */
    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        // Check file size (max 10MB)
        long maxSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 10MB");
        }

        // Check content type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Check allowed image formats
        String[] allowedFormats = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
        boolean isAllowed = false;
        for (String format : allowedFormats) {
            if (contentType.equalsIgnoreCase(format)) {
                isAllowed = true;
                break;
            }
        }

        if (!isAllowed) {
            throw new IllegalArgumentException("Invalid image format. Allowed: JPEG, PNG, GIF, WebP");
        }
    }

    /**
     * Extract public ID from Cloudinary URL
     * Example: https://res.cloudinary.com/dwjhoilfe/image/upload/v123456/studentbnb/listings/uuid.jpg
     * Returns: studentbnb/listings/uuid
     */
    private String extractPublicId(String imageUrl) {
        try {
            if (imageUrl.contains("/upload/")) {
                String[] parts = imageUrl.split("/upload/");
                if (parts.length > 1) {
                    String pathAfterUpload = parts[1];
                    // Remove version number if present (v1234567/)
                    pathAfterUpload = pathAfterUpload.replaceFirst("v\\d+/", "");
                    // Remove file extension
                    int lastDot = pathAfterUpload.lastIndexOf('.');
                    if (lastDot > 0) {
                        return pathAfterUpload.substring(0, lastDot);
                    }
                    return pathAfterUpload;
                }
            }
        } catch (Exception e) {
            log.error("Error extracting public ID from URL: {}", imageUrl, e);
        }
        return null;
    }
}
