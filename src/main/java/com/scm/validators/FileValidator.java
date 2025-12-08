package com.scm.validators;

import java.util.Arrays;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class FileValidator implements ConstraintValidator<ValidFile, MultipartFile> {
    private final List<String> ALLOWED_TYPES = Arrays.asList("image/jpeg", "image/png", "image/jpg");
    private final long MAX_SIZE = 2 * 1024 * 1024; // 2 MB

    @Override
    public boolean isValid(MultipartFile file, ConstraintValidatorContext context) {
        //Validate only when file present
        if (file != null && !file.isEmpty()) {
            context.disableDefaultConstraintViolation();
            // 1 Validate file size
            if (file.getSize() > MAX_SIZE) {
                context.buildConstraintViolationWithTemplate("File size must not exceed 2MB.").addConstraintViolation();
                return false;
            }

            // 2 Validate File Type

            if (!ALLOWED_TYPES.contains(file.getContentType())) {
                context.buildConstraintViolationWithTemplate("Only JPG, JPEG, PNG files are allowed.")
                        .addConstraintViolation();

                return false;
            }
        }

        return true;

    }

}
