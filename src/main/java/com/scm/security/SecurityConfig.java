package com.scm.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;


import com.scm.service.impl.CustomUserDetailsService;


@Configuration
public class SecurityConfig {
    @Autowired
    private CustomUserDetailsService customUserDetailsService;
    @Autowired
    private Oauth2AuthenticationSuccessHandler handler;
    @Autowired
    private FormLoginSuccessHandler formLoginSuccessHandler;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    // Filter chain custom configuration
    @Bean
    public SecurityFilterChain defaultSecurityConfig(HttpSecurity httpSecurity) throws Exception {

        // Url request permit configuration
        httpSecurity.authorizeHttpRequests(request -> {
            request.requestMatchers("/user/**","/api/**","/error/**").authenticated();
            request.anyRequest().permitAll();
        });

        // Form based login configuration
        httpSecurity.formLogin(formLogin -> {
            formLogin.loginPage("/login")
                    .loginProcessingUrl("/authenticate")
                    // .successForwardUrl("/user/dashboard")
                    // .failureForwardUrl("/login?error=true") // Method ="Post" required
                    .usernameParameter("email")
                    .passwordParameter("password");

            formLogin.successHandler(formLoginSuccessHandler);
        });

        httpSecurity.csrf(csrf -> csrf.disable());

        httpSecurity.logout(logoutForm -> {
            logoutForm.logoutUrl("/logout")
                    .logoutSuccessUrl("/login?logout=true");
        });

        httpSecurity.oauth2Login(oauthLogin -> {
            oauthLogin.loginPage("/login");
            oauthLogin.successHandler(handler);
        });

        return httpSecurity.build();
    }

    // Authentication Provider Configuration
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

}
