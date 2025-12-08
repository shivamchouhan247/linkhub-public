package com.scm.controllers;

import org.springframework.web.bind.annotation.RestController;

import com.scm.entities.Contact;
import com.scm.service.ContactService;

import org.springframework.web.bind.annotation.RequestMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api")
public class ApiController {

    private ContactService contactService;

    public ApiController(ContactService contactService) {
        this.contactService = contactService;
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(ApiController.class);

    @GetMapping("/contact/{contactId}")
    public Contact getContactDetails(@PathVariable(value = "contactId") String contactId) {
        LOGGER.info("Request for Contact Details with contactId: {}", contactId);
        Contact contact = new Contact();
        try {
            contact = contactService.getContactById(contactId);
        } catch (Exception e) {
            LOGGER.warn("Unexpected error during get contact details by contact Id: {}, {}", contactId, e);
        }
        return contact;
    }

    @DeleteMapping("contact/delete/{contactId}")
    public ResponseEntity<Boolean> deleteContact(@PathVariable("contactId") String contactId) {
        LOGGER.info("contact delete request with id: {}", contactId);
        try {
            if (contactId == null || contactId.isEmpty()) {
                return ResponseEntity.status(HttpStatus.FAILED_DEPENDENCY).body(false);
            }
            ResponseEntity<Boolean> result = contactService.deleteContactById(contactId);
            LOGGER.info("Result: {}", result.toString());
            return result;
        } catch (Exception e) {
            LOGGER.warn("Unexpected error during delete contact by Id: {}, {}", contactId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }

    }
}
