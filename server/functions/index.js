const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Data structures keys
const USERS_KEY = 'users';
const REQUESTS_KEY = 'requests';
const REQUESTS_LOCATIONS_KEY = 'requests_locations';

admin.initializeApp();

// Auth listeners
exports.createUser = functions.auth.user().onCreate(async (user) => {
    await setUser(new DbUser(user.uid, user.email, '', 0))
});

exports.deleteUser = functions.auth.user().onDelete(async (user) => {
  await admin.database().ref(USERS_KEY + '/' + user.uid).remove()
  .then(function() {
    console.log('Removing user: ' + user.email + ' succeeded.');
  })
  .catch(function(error) {
    console.log('Removing user failed: ' + error.message);
  });
});

// Database listeners
exports.deleteRequest = functions.database.ref(REQUESTS_KEY).onDelete(async (snapshot, context) => {
    const deletedRequestUid = snapshot.key;
    await admin.database().ref(REQUESTS_LOCATIONS_KEY + '/' + deletedRequestUid).remove()
    .then(function() {
      console.log('Removing request: ' + deletedRequestUid + ' location succeeded.');
    })
    .catch(function(error) {
      console.log('Removing request location failed: ' + error.message);
    });
});

// Methods
async function setUser(dbUser)
{
    await admin.database().ref(USERS_KEY + '/' + dbUser.uid).set({
        email: dbUser.email,
        phoneNumber: dbUser.phoneNumber,
        reputation: dbUser.reputation
    })
    .then(function() {
      console.log('Adding/updating user: ' + dbUser.email + ' succeeded.');
    })
    .catch(function(error) {
      console.log('Adding/updating failed: ' + error.message);
    });
}

// Database structures constructors
function DbUser(uid, email, phoneNumber, reputation)
{
    this.uid = uid;
    this.email = email;
    this.phoneNumber = phoneNumber;
    this.reputation = reputation;
}
