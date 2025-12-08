package com.scm.payload.request;

import org.springframework.web.multipart.MultipartFile;

import com.scm.enums.Providers;
import com.scm.validators.ValidFile;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class UpdateProfileRequest {
    private String userId;
    @NotBlank(message = "Please enter a Full Name")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._\\- ]{4,19}$", message = "Full Name must start with a letter, 5-20 characters, [characters, letters, numbers, spaces, '.', '-', '_' allowed]")
    private String name;

    // @NotBlank(message = "Email is required")
    // @Email(message = "Invalid email address")
    private String email;

    @Pattern(regexp = "^[0-9]{10}$", message = "Phone number must be exactly 10 digits")
    private String phoneNumber;

    // @NotBlank(message = "About is required")
    @Size(min = 5, max = 200, message = "About must be between 5 and 200 characters")
    private String about;

    @Builder.Default
    private Providers provider = Providers.SELF;

    @Builder.Default
    private boolean enabled = false;

    @Builder.Default
    private boolean emailVerified = false;

    @Builder.Default
    private boolean phoneVerified = false;

    private String profilePic;

    @ValidFile
    private MultipartFile imageFile;

}
