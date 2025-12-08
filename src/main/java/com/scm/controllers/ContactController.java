package com.scm.controllers;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import com.scm.entities.Contact;
import com.scm.enums.MessageAlertType;
import com.scm.payload.request.AddContactRequest;
import com.scm.payload.request.SearchContactRequest;
import com.scm.service.ContactService;

import com.scm.utils.constants.CommonConfigLogic;
import com.scm.utils.constants.ResponseMessage;
import com.scm.utils.helper.AlertMessage;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/user/contacts")
public class ContactController {
    @Autowired
    private ContactService contactService;
    @Autowired
    private CommonConfigLogic commonConfigLogic;

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactController.class);

    @RequestMapping(value = "/add")
    public String createContact(Model model) {
        AddContactRequest contactForm = new AddContactRequest();
        model.addAttribute("contactForm", contactForm);
        return "user/add_contact";
    }

    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public String createContactPost(@Valid @ModelAttribute("contactForm") AddContactRequest contactForm,
            BindingResult bindingResult,
            Authentication authentication, HttpSession httpSession) {

        AlertMessage message = null;
        try {
            LOGGER.info(" 1 Add Contact Request: ");

            // 1 Validate Request
            if (bindingResult.hasErrors()) {
                return "user/add_contact";
            }

            // 2 Save Contact
            Map<String, Object> response = contactService.saveContact(contactForm, authentication);
            LOGGER.info(" 4 response :{}", response);

            MessageAlertType type = MessageAlertType.fromCode(response.get(ResponseMessage.CODE).toString());
            message = AlertMessage.builder()
                    .content(response.get(ResponseMessage.DESCRIPTION).toString())
                    .type(type)
                    .build();

        } catch (Exception e) {
            LOGGER.error("Unexpected error during add new contact: {}", contactForm.getName(), e);
            commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                    e.getMessage());

            message = AlertMessage.builder()
                    .content(e.getMessage())
                    .type(MessageAlertType.red)
                    .build();

        }

        httpSession.setAttribute("message", message);
        return "redirect:/user/contacts/add";
    }

    @RequestMapping
    public String allContacts(@RequestParam(value = "size", defaultValue = ResponseMessage.PAGE_SIZE + "") int size,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Model model, Authentication authentication) {
        Page<Contact> contacts = null;
        try {
            // max 10 size is allowed
            size = size > 10 ? 10 : size;
            contacts = contactService.getUserSavedContacts(size, page - 1, sortBy, direction, authentication);

        } catch (Exception e) {
            LOGGER.error("Unexpected error during get all user saved contacts: {}", e);
        }

        // model attribute for search
        model.addAttribute("searchContact", new SearchContactRequest());
        model.addAttribute("pageContact", contacts);
        model.addAttribute("pageSize", ResponseMessage.PAGE_SIZE);
        return "user/contacts";
    }

    @GetMapping("/search")
    public String searchContact(@ModelAttribute("searchContact") SearchContactRequest searchContactRequest,
            @RequestParam(value = "size", defaultValue = ResponseMessage.PAGE_SIZE + "") int size,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "sortBy", defaultValue = "name") String sortBy,
            @RequestParam(value = "direction", defaultValue = "asc") String direction,
            Authentication authentication, Model model) {

        LOGGER.info("Inside search contact:");

        Page<Contact> contacts = null;
        try {
            String field = searchContactRequest.getField();
            String keyword = searchContactRequest.getKey();
            size = size > 10 ? 10 : size;
            contacts = contactService.getSearchedUserContacts(field, keyword, size, page - 1, sortBy, direction,
                    authentication);
        } catch (Exception e) {
            LOGGER.error("Unexpected error during search user saved contacts: {}", e);
        }
        model.addAttribute("searchContact", searchContactRequest);
        model.addAttribute("pageContact", contacts);
        model.addAttribute("pageSize", ResponseMessage.PAGE_SIZE);

        return "user/search_contacts";
    }

    @RequestMapping("/update/{contactId}")
    public String updateContactDetailsPage(@PathVariable("contactId") String contactId, Model model,
            HttpSession session) {
        LOGGER.info("Inside Update Contact Page: {}", contactId);
        AddContactRequest contact = new AddContactRequest();
        AlertMessage alertMessage = null;
        try {
            // validating contactId;
            if (!contactService.isValidContactId(contactId)) {
                alertMessage = AlertMessage.builder()
                        .content("Invalid Contact Id!!")
                        .type(MessageAlertType.red)
                        .build();
            } else {
                Contact savedContact = contactService.getContactById(contactId);
                contact = AddContactRequest.builder()
                        .contactId(savedContact.getContactId())
                        .name(savedContact.getName())
                        .email(savedContact.getEmail())
                        .phoneNumber(savedContact.getPhoneNumber())
                        .address(savedContact.getAddress())
                        .description(savedContact.getDescription())
                        .favorite(savedContact.isFavorite())
                        .socialLinks(savedContact.getSocialLinks())
                        .picture(savedContact.getPicture())
                        .build();
            }

        } catch (Exception e) {
            LOGGER.error("Unexpected error during update contact details : {}", contactId, e);
            alertMessage = AlertMessage.builder()
                    .content("Something Went Wrong")
                    .type(MessageAlertType.red)
                    .build();
        }

        if ((alertMessage != null)) {
            session.setAttribute("message", alertMessage);
        }
        model.addAttribute("contactForm", contact);
        return "user/update_contact";
    }

    @RequestMapping(value = "/update/{contactId}", method = RequestMethod.POST)
    public String updateContactDetails(@PathVariable("contactId") String contactId,
            @Valid @ModelAttribute("contactForm") AddContactRequest contactForm,
            BindingResult bindingResult, HttpSession httpSession) {

        AlertMessage message = null;
        String redirectUrl = "/user/contacts/update/" + contactForm.getContactId();
        try {
            LOGGER.info("Inside Update Contact Request: {}", contactId);

            // 1 Validate Request
            if (bindingResult.hasErrors()) {
                return "user/update_contact";
            }
            if (!contactService.isValidContactId(contactId)) {
                message = AlertMessage.builder()
                        .content("Invalid Contact Id!!")
                        .type(MessageAlertType.red)
                        .build();
                httpSession.setAttribute("message", message);
                return "redirect:" + redirectUrl;

            }

            // 2 Update Contact
            contactForm.setContactId(contactId);
            Map<String, Object> response = contactService.updateContactDetails(contactForm);
            LOGGER.info("response :{}", response);
            

            MessageAlertType type = MessageAlertType.fromCode(response.get(ResponseMessage.CODE).toString());
            message = AlertMessage.builder()
                    .content(response.get(ResponseMessage.DESCRIPTION).toString())
                    .type(type)
                    .build();

        } catch (Exception e) {
            LOGGER.error("Unexpected error during add new contact: {}", contactForm.getName(), e);
            commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                    e.getMessage());

            message = AlertMessage.builder()
                    .content(e.getMessage())
                    .type(MessageAlertType.red)
                    .build();
        }

        httpSession.setAttribute("message", message);
        return "redirect:" + redirectUrl;
    }

}
