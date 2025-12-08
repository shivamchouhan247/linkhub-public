package com.scm.utils.helper;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpSession;

@Component
public class SessionHelper {

    private static final Logger LOGGER=LoggerFactory.getLogger(SessionHelper.class);

    public static void removeMessage(){
       try {
        LOGGER.info("Removing message from Session:");
         HttpSession session= ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getSession();
        session.removeAttribute("message");
       } catch (Exception e) {
        LOGGER.error("Error in SessionHelper: {}",e);
       }
    }

}
