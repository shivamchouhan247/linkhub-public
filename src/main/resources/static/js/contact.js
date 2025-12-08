const baseUrl = "http://localhost:8080";
//set the modal element
const viewContactDetails = document.getElementById('view_contact_modal');

// options with default values
const options = {
    placement: 'bottom-right',
    backdrop: 'dynamic',
    backdropClasses:
        'bg-gray-900/50 dark:bg-gray-900/80 fixed inset-0 z-40',
    closable: true,
    onHide: () => {
        console.log('modal is hidden');
    },
    onShow: () => {
        console.log('modal is shown');
    },
    onToggle: () => {
        console.log('modal has been toggled');
    },
};

// instance options object
const instanceOptions = {
    id: 'view_contact_modal',
    override: true
};

//creating modal
const viewContact = new Modal(viewContactDetails, options, instanceOptions);


function openContactDetails() {
    viewContact.show();
}

async function loadContactDetails(id) {
    try {
        const url = `${baseUrl}/api/contact/${id}`;
        const response = await fetch(url);
        const contact = await response.json();
        // console.log("Contact Details: ", contact)

        // Populate modal fields
        document.getElementById('contact_name').textContent = contact.name;
        document.getElementById('contact_email').textContent = contact.email || 'Not provided';
        document.getElementById('contact_phone').textContent = contact.phoneNumber || 'Not provided';
        document.getElementById('contact_address').textContent = contact.address || 'Not provided';
        document.getElementById('contact_description').textContent = contact.description || 'No Additional Information available';
        document.getElementById('contact_favorite').textContent = contact.favorite ? 'Yes ❤️' : 'No';
        document.getElementById('contact_picture').src = contact.picture || '/images/default-profile.png';

        // Populate social links
        const linksContainer = document.getElementById('contact_social_links');
        linksContainer.innerHTML = '';
        if (contact.socialLinks && contact.socialLinks.length > 0) {
            contact.socialLinks.forEach(link => {
                const li = document.createElement('li');
                li.innerHTML = `<a href="${link.link}" target="_blank"
                            class="text-blue-600 dark:text-blue-400 hover:underline">${link.link}</a>`;
                linksContainer.appendChild(li);
            });
        } else {
            linksContainer.innerHTML = '<p class="text-gray-500 dark:text-gray-400">No social links added.</p>';
        }


    } catch (error) {
        console.error("Error fetching contact: ", error)
    }

    openContactDetails();

}


function openDeleteConfirmation(contactId) {
    // Detect Tailwind dark mode
    const isDarkMode = document.documentElement.classList.contains('dark');

    Swal.fire({
        title: 'Are you sure?',
        text: "You won't be able to revert this!",
        icon: 'warning',
        showCancelButton: true,
        confirmButtonText: 'Yes, delete it!',
        cancelButtonText: 'Cancel',
        buttonsStyling: false,
        customClass: {
            popup: isDarkMode 
                ? 'bg-gray-800 text-white rounded-2xl p-6 shadow-xl' 
                : 'bg-white text-gray-900 rounded-2xl p-6 shadow-xl',
            title: 'text-lg font-bold',
            confirmButton: 'px-4 py-2 bg-red-600 hover:bg-red-700 text-white rounded-lg',
            cancelButton: isDarkMode
                ? 'px-4 py-2 bg-gray-700 hover:bg-gray-600 text-white rounded-lg'
                : 'px-4 py-2 bg-gray-200 hover:bg-gray-300 text-gray-800 rounded-lg'
        }
    }).then((result) => {
        if (result.isConfirmed) {
            // Call your delete API here
            fetch(`http://localhost:8080/api/contact/delete/${contactId}`, {
                method: 'DELETE'
            })
            .then(res => {
                if(res.ok) {
                    Swal.fire({
                        icon: 'success',
                        title: 'Deleted!',
                        text: 'Your contact has been deleted.',
                        buttonsStyling: false,
                        customClass: {
                            popup: isDarkMode 
                                ? 'bg-gray-800 text-white rounded-2xl p-6 shadow-xl' 
                                : 'bg-white text-gray-900 rounded-2xl p-6 shadow-xl',
                            confirmButton: 'px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg'
                        }
                    });
                    // Optionally remove row from table
                    const row = document.getElementById(`row-${contactId}`);
                    if (row) row.remove();
                } else {
                    throw new Error('Failed to delete');
                }
            })
            .catch(err => {
                Swal.fire({
                    icon: 'error',
                    title: 'Oops!',
                    text: 'Failed to delete contact.',
                    buttonsStyling: false,
                    customClass: {
                        popup: isDarkMode 
                            ? 'bg-gray-800 text-white rounded-2xl p-6 shadow-xl' 
                            : 'bg-white text-gray-900 rounded-2xl p-6 shadow-xl',
                        confirmButton: 'px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg'
                    }
                });
                console.error(err);
            });
        }
    });
}
