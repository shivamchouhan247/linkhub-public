console.log("script loaded");

let currentTheme = getTheme();
setTheme(currentTheme);
changeTheme(); 
console.log(getTheme());

// Mobile button fix
const changeThemeButtonMobile = document.querySelector('#theme_toggle_mobile');
if (changeThemeButtonMobile) {
    changeThemeButtonMobile.addEventListener('click', (event) => {
        console.log("mobile change theme button clicked");

        let newTheme = getTheme() == "dark" ? "light" : "dark";
        document.querySelector('html').classList.remove(getTheme());
        document.querySelector('html').classList.add(newTheme);
        setTheme(newTheme);

        // update desktop button text
        const changeThemeButton = document.querySelector('#theme_toggle');
        if(changeThemeButton) changeThemeButton.querySelector('span').textContent = (newTheme == "light" ? "Dark" : "Light");

        // update mobile button text
        changeThemeButtonMobile.querySelector('span').textContent = (newTheme == "light" ? "Dark" : "Light");
    });
}

// Set to webpage
function changeTheme() {
    // This sets the initial theme
    document.querySelector('html').classList.add(currentTheme);

    // changing button theme
    const changeThemeButton = document.querySelector('#theme_toggle');
    changeThemeButton.addEventListener('click', (event) => {
        console.log("change theme button clicked");
        // You can add logic here to toggle themes
        let newTheme = getTheme() == "dark" ? "light" : "dark";
        document.querySelector('html').classList.remove(getTheme()); //step 1 -> remove oldTheme and add new Theme
        document.querySelector('html').classList.add(newTheme); 
        setTheme(newTheme); //step 2 -> update localStorage

        // step 3 -> change button content 
        changeThemeButton.querySelector('span').textContent = (newTheme == "light" ? "Dark" : "Light");

        // also update mobile button text if exists
        const changeThemeButtonMobile = document.querySelector('#theme_toggle_mobile');
        if(changeThemeButtonMobile) changeThemeButtonMobile.querySelector('span').textContent = (newTheme == "light" ? "Dark" : "Light");
    });
}

// Set theme
function setTheme(theme) {
    localStorage.setItem("theme", theme);
}

// Get theme
function getTheme() {
    let theme = localStorage.getItem("theme");
    return theme ? theme : "dark";
}
