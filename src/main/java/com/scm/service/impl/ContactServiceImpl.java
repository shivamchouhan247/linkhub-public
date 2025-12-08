package com.scm.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.scm.entities.Contact;
import com.scm.entities.SocialLink;
import com.scm.entities.User;
import com.scm.exception.ResourceNotFoundException;
import com.scm.payload.request.AddContactRequest;
import com.scm.repo.ContactRepository;
import com.scm.service.ContactService;
import com.scm.service.S3Service;
import com.scm.service.UserService;
import com.scm.utils.constants.CommonConfigLogic;
import com.scm.utils.constants.ResponseMessage;
import com.scm.utils.helper.UserHelper;

@Service
public class ContactServiceImpl implements ContactService {
    private CommonConfigLogic commonConfigLogic;
    private ContactRepository contactRepository;
    private UserService userService;
    private S3Service s3Service;

    public ContactServiceImpl(CommonConfigLogic commonConfigLogic, ContactRepository contactRepository,
            UserService userService, S3Service s3Service) {
        this.commonConfigLogic = commonConfigLogic;
        this.contactRepository = contactRepository;
        this.userService = userService;
        this.s3Service = s3Service;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(ContactServiceImpl.class);

    @Override
    public Map<String, Object> saveContact(AddContactRequest contactForm, Authentication authentication)
            throws Exception {
        // Fetch and map to Contact Entity
        Map<String, Object> map = new HashMap<>();
        try {
            LOGGER.info(" 2 Inside saveContact :");

            // 1 Fetching user
            String userEmail = UserHelper.getLoggedInUserEmail(authentication);
            User user = userService.getUserByEmail(userEmail);

            // 2 Processing image
            MultipartFile contactImage = contactForm.getContactImage();
            LOGGER.info(" 2 contactImage Details : {}", contactImage.getOriginalFilename());
            String pictureKey = null;
            if (contactImage != null && !contactImage.isEmpty()) {
                pictureKey = s3Service.uploadFile(contactImage);
                LOGGER.info("s3URL :{}", s3Service.generatePresignedUrl(pictureKey));
            }

            // 3 Map contact entity
            String contactId = UUID.randomUUID().toString();
            LOGGER.info(" 3 contactId: {}", contactId);

            Contact contact = Contact.builder()
                    .contactId(contactId)
                    .name(contactForm.getName())
                    .email(contactForm.getEmail())
                    .phoneNumber(contactForm.getPhoneNumber())
                    .address(contactForm.getAddress())
                    .description(contactForm.getDescription())
                    .favorite(contactForm.isFavorite())
                    .picture(pictureKey)
                    .user(user)
                    .build();

            List<SocialLink> socialLinks = contactForm.getSocialLinks().stream()
                    .peek(sl -> sl.setContact(contact))
                    .collect(Collectors.toList());

            contact.setSocialLinks(socialLinks);

            // 4 save contact
            Contact savedContact = contactRepository.save(contact);

            map = commonConfigLogic.buildResponse(ResponseMessage.SUCCESS, ResponseMessage.STATUS_SUCCESS,
                    "Contact Saved Successfully");
            map.put("contact", savedContact);

        } catch (Exception e) {
            LOGGER.error("Unexpected error during add new contact: {}", contactForm.getName(), e);
            return commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                    e.getMessage());
        }
        return map;
    }

    @Override
    public Page<Contact> getUserSavedContacts(int pageSize, int pageNumber, String sortBy, String direction,
            Authentication authentication) {
        Page<Contact> contacts = null;
        try {
            // Fetch LoggedIn User Email Then Load User From DB
            String email = UserHelper.getLoggedInUserEmail(authentication);
            User user = userService.getUserByEmail(email);

            // Get User Saved Contacts Page Using User
            Sort sort = direction.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);
            contacts = contactRepository.findByUser(user, pageable);

            // Processing Picture Presigned URl from S3 Bucket Due to Private Bucket
            String defaultProfile = "/images/default-profile.png";

            contacts.forEach(contact -> {
                String pictureKey = contact.getPicture();

                if (pictureKey == null || pictureKey.isBlank()) {
                    contact.setPicture(defaultProfile);
                    return;
                }

                try {
                    String pictureUrl = s3Service.generatePresignedUrl(pictureKey);
                    contact.setPicture(pictureUrl.isBlank() ? defaultProfile : pictureUrl);
                } catch (Exception e) {
                    contact.setPicture(defaultProfile);
                    LOGGER.warn("Error generating presigned URL for contact picture: {}", e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            LOGGER.error("Unexpected error during get all user saved contacts: {}", e);
        }
        return contacts;
    }

    @Override
    public Page<Contact> getSearchedUserContacts(String field, String keyword, int pageSize, int pageNumber,
            String sortBy, String direction, Authentication authentication) {
        LOGGER.info("Inside getSearchedUserContacts Imp :");
        LOGGER.info("field: {},keyword: {},pageSize: {},pageNumber: {},sortBy: {},direction: {}", field, keyword,
                pageSize, pageNumber, sortBy, direction);
        Page<Contact> contacts = null;
        try {
            User user = userService.getUserByEmail(UserHelper.getLoggedInUserEmail(authentication));
            LOGGER.info("user :{}", user.getEmail());

            Sort sort = direction.equals("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
            Pageable pageable = PageRequest.of(pageNumber, pageSize, sort);

            switch (field.toLowerCase()) {
                case "name":
                    contacts = contactRepository.findByUserAndNameContaining(user, keyword, pageable);
                    break;
                case "phone":
                    contacts = contactRepository.findByUserAndPhoneNumberContaining(user, keyword, pageable);
                    break;
                case "email":
                    contacts = contactRepository.findByUserAndEmailContaining(user, keyword, pageable);
                    break;
                default:
                    break;
            }

            // Processing Picture Presigned URl from S3 Bucket Due to Private Bucket
            String defaultProfile = "/images/default-profile.png";

            contacts.forEach(contact -> {
                String pictureKey = contact.getPicture();

                if (pictureKey == null || pictureKey.isBlank()) {
                    contact.setPicture(defaultProfile);
                    return;
                }

                try {
                    String pictureUrl = s3Service.generatePresignedUrl(pictureKey);
                    contact.setPicture(pictureUrl.isBlank() ? defaultProfile : pictureUrl);
                } catch (Exception e) {
                    contact.setPicture(defaultProfile);
                    LOGGER.warn("Error generating presigned URL for contact picture: {}", e.getMessage(), e);
                }
            });

        } catch (Exception e) {
            LOGGER.error("Unexpected error during search user saved contacts: {}", e);
        }
        return contacts;
    }

    @Override
    public Contact getContactById(String contactId) {
        Contact contact = null;
        try {
            if (contactId == null || contactId.isEmpty()) {
                return contact;
            }
            contact = contactRepository.findById(contactId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contact Not found with contactId :" + contactId));

            String pictureUrl = s3Service.generatePresignedUrl(contact.getPicture());
            contact.setPicture(pictureUrl.isEmpty() ? "/images/default-profile.png" : pictureUrl);
        } catch (Exception e) {
            LOGGER.warn("Unexpected error during get contact details by contact Id: {}, {}", contactId, e);

        }
        return contact;

    }

    @Override
    public ResponseEntity<Boolean> deleteContactById(String contactId) {
        LOGGER.info("inside delete contact id: {}", contactId);
        try {
            if (contactRepository.existsById(contactId)) {
                contactRepository.deleteById(contactId);
                return ResponseEntity.ok(true);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(true);
            }

        } catch (Exception e) {
            LOGGER.warn("Unexpected error during delete contact by Id: {}, {}", contactId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(false);
        }
    }

    @Override
    public boolean isValidContactId(String contactId) {
        if (contactId == null || contactId.isEmpty()) {
            return false;
        }
        return contactRepository.existsById(contactId);
    }

    @Override
    public Contact getRawContactById(String contactId) {
        Contact contact = null;
        try {
            if (contactId == null || contactId.isEmpty()) {
                return contact;
            }
            contact = contactRepository.findById(contactId)
                    .orElseThrow(() -> new ResourceNotFoundException("Contact Not found with contactId :" + contactId));

        } catch (Exception e) {
            LOGGER.warn("Unexpected error during get contact details by contact Id: {}, {}", contactId, e);

        }
        return contact;

    }

    @Override
    public Map<String, Object> updateContactDetails(AddContactRequest contactForm)
            throws Exception {
        // Fetch and map to Contact Entity
        Map<String, Object> map = new HashMap<>();
        try {
            LOGGER.info("Inside updateContact: {}", contactForm.getContactId());

            // 1 Fetch Saved Contact
            Contact savedContact = getRawContactById(contactForm.getContactId());

            if (savedContact == null) {
                return commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                        "Contact not found with the given contactId: " + contactForm.getContactId());
            }

            // 2 Processing image
            MultipartFile contactImage = contactForm.getContactImage();
            // LOGGER.info(" contactImage Details : {}",
            // contactImage.getOriginalFilename());
            String pictureKey = savedContact.getPicture();
            if (contactImage != null && !contactImage.isEmpty()) {
                pictureKey = s3Service.uploadFile(contactImage);
                // LOGGER.info("s3URL :{}", s3Service.generatePresignedUrl(pictureKey));
            }

            // 3 Map contact entity
            Contact contact = savedContact;
            contact.setContactId(contactForm.getContactId());
            contact.setName(contactForm.getName());
            contact.setEmail(contactForm.getEmail());
            contact.setPhoneNumber(contactForm.getPhoneNumber());
            contact.setAddress(contactForm.getAddress());
            contact.setDescription(contactForm.getDescription());
            contact.setFavorite(contactForm.isFavorite());
            contact.setPicture(pictureKey);

            contact.getSocialLinks().clear();
            List<SocialLink> socialLinks = contactForm.getSocialLinks().stream()
                    .peek(sl -> sl.setContact(contact))
                    .collect(Collectors.toList());
            contact.getSocialLinks().addAll(socialLinks);
            contact.getSocialLinks().removeIf(link -> link.getLink() == null || link.getLink().trim().isEmpty());

            // 4 Update contact
            // LOGGER.info("updating Contact: {}", contact.toString());
            Contact updatedContact = contactRepository.save(contact);

            map = commonConfigLogic.buildResponse(ResponseMessage.SUCCESS, ResponseMessage.STATUS_SUCCESS,
                    "Contact Updated Successfully");
            map.put("contact", updatedContact);

        } catch (Exception e) {
            LOGGER.error("Unexpected error during update contact: {}", contactForm.getName(), e);
            return commonConfigLogic.buildResponse(ResponseMessage.FAILED, ResponseMessage.STATUS_FAILED,
                    "Something Went Wrong");
        }
        return map;
    }

    @Override
    public List<Contact> getUserSavedContacts(String userId) {
        List<Contact> contacts = new ArrayList<>();
        try {
            contacts = contactRepository.findByUser_UserId(userId);
        } catch (Exception e) {

        }

        return contacts;
    }
}
