package com.scm.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;

import com.scm.entities.Contact;
import com.scm.entities.User;
import com.scm.payload.request.AddContactRequest;

public interface ContactService {
        public Map<String, Object> saveContact(AddContactRequest contactForm, Authentication authentication)
                        throws Exception;

        public Map<String, Object> updateContactDetails(AddContactRequest contactForm)
                        throws Exception;

        public Page<Contact> getUserSavedContacts(int pageSize, int pageNumber, String sortBy, String direction,
                        Authentication authentication);

        public Page<Contact> getSearchedUserContacts(String field, String keyword, int pageSize, int pageNumber,
                        String sortBy, String direction, Authentication authentication);

        public Contact getContactById(String contactId);

        public ResponseEntity<Boolean> deleteContactById(String contactId);

        public boolean isValidContactId(String contactId);

        public Contact getRawContactById(String contactId);

        public List<Contact> getUserSavedContacts(String userId);

}
