package L03.CNPM.Music.utils;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

@Component
public class ImageFileUtils {

    public File convertMultipartFileToFile(MultipartFile multipartFile) throws Exception {
        File tempFile = File.createTempFile("temp", multipartFile.getOriginalFilename());
        multipartFile.transferTo(tempFile);
        return tempFile;
    }

    public boolean isValidImageFile(String fileType, String fileName) {
        List<String> validMimeTypes = Arrays.asList(
                "image/png", // .png
                "image/jpeg", // .jpeg
                "image/gif", // .gif
                "image/bmp" //.bmp
        );

        List<String> validExtensions = Arrays.asList(
                ".png", ".jpeg", ".gif", ".bmp", ".jpg", ".jfif");

        if (fileType == null || !validMimeTypes.contains(fileType.toLowerCase())) {
            return false;
        }

        if (fileName != null) {
            String fileExtension = getFileExtension(fileName);
            return validExtensions.contains(fileExtension.toLowerCase());
        }

        return false;
    }

    private String getFileExtension(String fileName) {
        int lastIndex = fileName.lastIndexOf('.');
        return (lastIndex != -1) ? fileName.substring(lastIndex) : "";
    }

}
