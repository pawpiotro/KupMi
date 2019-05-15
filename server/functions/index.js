const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Data structures keys
const USERS_KEY = 'users';

const REQUESTS_KEY = 'requests';
const ACTIVE_KEY = 'active';
const ACCEPTED_KEY = 'accepted';
const DONE_KEY = 'done';
const UNDONE_KEY = 'undone';

const REQUESTS_LOCATIONS_KEY = 'requests_locations';

const TAGS_KEY = 'tags';
const DELIVERY_KEY = 'delivery';
const LOAN_KEY = 'loan';
const REPAIR_KEY = 'repair';
const ACTIVITY_KEY = 'activity';

admin.initializeApp();

// Auth listeners
// TODO: Problem with displayName
exports.createUser = functions.auth.user().onCreate(async (user) => {
    await setUser(new DbUser(user.uid, user.email, '', '', 0))
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
exports.changeRequestTags = functions.database.ref(REQUESTS_KEY + '/{state}/{requestUid}/tag')
.onWrite(async (change, context) => {
    const requestUid = context.params.requestUid;
    const state = context.params.state;

    if (!change.before.exists())
    {
        // In case of change tags function
    }
    else if (change.before.exists() && change.after.exists())
    {
        // In case of change tags function
    }
    else if (!change.after.exists())
    {
        const deletedTag = change.before.val();
        if (deletedTag != null && typeof deletedTag == 'string')
        {
            await admin.database().ref(TAGS_KEY + '/' + deletedTag + '/' +
            state + '/' + requestUid).remove()
            .then(function() {
              console.log('Removing request: ' + requestUid + ' from tag: ' + deletedTag + ' succeeded.');
            })
            .catch(function(error) {
              console.log('Removing request from tag: ' + deletedTag + ' failed: ' + error.message);
            });
        }
        else
        {
            console.log('Wrong deletedTag value.');
        }
    }
});

exports.deleteRequest = functions.database.ref(REQUESTS_KEY + '/{state}/{requestUid}').onDelete(async (snapshot, context) => {
    const deletedRequestUid = context.params.requestUid;
    const state = context.params.state;
    await admin.database().ref(REQUESTS_LOCATIONS_KEY + '/' + state + '/' + deletedRequestUid).remove()
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
        name: dbUser.name,
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
function DbUser(uid, email, name, phoneNumber, reputation)
{
    this.uid = uid;
    this.email = email;
    this.name = name;
    this.phoneNumber = phoneNumber;
    this.reputation = reputation;
}
