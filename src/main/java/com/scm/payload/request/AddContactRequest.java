package com.scm.payload.request;

import java.util.ArrayList;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.scm.entities.SocialLink;
import com.scm.validators.ValidFile;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddContactRequest {

    @NotBlank(message = "Please enter a full name")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._\\- ]{2,19}$", message = "Full Name must start with a letter, 3-20 characters, [characters, letters, numbers, spaces, '.', '-', '_' allowed]")
    private String name;

    @Email(message = "Please provide a valid email address")
    private String email;
    @NotBlank(message = "Phone number is required")
    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;
    @Size(max = 50, message = "Address must be 50 characters or less")
    private String address;
    @Size(max = 200, message = "Description must be 200 characters or less")
    private String description;
    private String picture;
    private boolean favorite;
    private String contactId;

    @ValidFile
    private MultipartFile contactImage;


    @Builder.Default
    private List<SocialLink> socialLinks = new ArrayList<>();
}
