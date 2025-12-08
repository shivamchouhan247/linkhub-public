package com.scm.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {

    @NotBlank(message = "Please enter a username")
    @Pattern(regexp = "^[a-zA-Z][a-zA-Z0-9._\\- ]{4,19}$", message = "Username must start with a letter, 5-20 characters, [characters, letters, numbers, spaces, '.', '-', '_' allowed]")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Min 6 characters is required")
    private String password;

    @NotBlank(message = "About is required")
    @Size(min = 5, max = 200, message = "About must be between 5 and 200 characters")
    private String about;

    @NotBlank(message = "Please check the Terms and condition")
    private String terms;

}
