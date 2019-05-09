const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendWelcomeEmail = functions.auth.user().onCreate(async (user) => {
    await setUser(new DbUser(user.uid, user.email, "", 0))
});

// Methods
async function setUser(dbUser)
{
    await admin.database().ref(USERS_KEY + '/' + dbUser.uid).set({
        email: dbUser.email,
        phoneNumber: dbUser.phoneNumber,
        reputation: dbUser.reputation
    });
}

// Data structures keys
const USERS_KEY = 'users';
const REQUESTS_KEY = 'requests';
const REQUESTS_LOCATIONS_KEYS = 'requests_locations';

// Database structures constructors
function DbUser(uid, email, phoneNumber, reputation)
{
    this.uid = uid;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.reputation = reputation;
}
