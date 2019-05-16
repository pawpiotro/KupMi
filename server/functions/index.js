const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Data structures keys
const USERS_KEY = 'users';

const REQUESTS_KEY = 'requests';
const REQUESTER_KEY = "requester";
const SUPPLIER_KEY = "supplier";

const REQUESTS_DETAILS_KEY = "requests_details";

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
/*exports.changeRequestTags = functions.database.ref(REQUESTS_KEY +
    '/{userKind}/{userUID}/{requestUID}/tag')
.onWrite(async (change, context) => {
    const userKind = context.params.userKind;
    const userUID = context.params.userUID;
    const requestUID = context.params.requestUID;

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
        const deletedTag = ;
        if (deletedTag != null && typeof deletedTag == 'string')
        {
            await admin.database().ref(TAGS_KEY + '/' + deletedTag + '/' +
            state + '/' + requestUID).remove()
            .then(function() {
              console.log('Removing request: ' + requestUID + ' from tag: ' + deletedTag + ' succeeded.');
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
});*/

exports.deleteRequest = functions.database.ref(REQUESTS_KEY +
    '/{userKind}/{userUID}/{requestUID}').onDelete(async (snapshot, context) => {
    const userKind = context.params.userKind;
    const otherUserKind = getOtherUserKind(userKind);
    const userUID = context.params.userUID;
    const requestUID = context.params.requestUID;

    const request = snapshot.val()

    if (request != null)
    {
        const otherUserUID = request.UserUID;
        const stateName = getStateName(request.state);
        const tag = request.tag;

        if (otherUserUID)
        {
            await admin.database().ref(REQUESTS_KEY + '/' + otherUserKind + '/' +
            otherUserUID + '/' + requestUID).remove()
            .then(function() {
              console.log('Removing request: ' + requestUID + ' from ' + otherUserKind + ' succeeded.');
            })
            .catch(function(error) {
              console.log('Removing request from ' + otherUserKind + ' failed: ' + error.message);
            })
        }

        await admin.database().ref(REQUESTS_DETAILS_KEY + '/' + requestUID).remove()
        .then(function() {
          console.log('Removing request: ' + requestUID + ' details succeeded.');
        })
        .catch(function(error) {
          console.log('Removing request location failed: ' + error.message);
        });

        if (stateName)
        {
            await admin.database().ref(REQUESTS_LOCATIONS_KEY + '/' + stateName + '/' + requestUID).remove()
            .then(function() {
              console.log('Removing request: ' + requestUID + ' location succeeded.');
            })
            .catch(function(error) {
              console.log('Removing request location failed: ' + error.message);
            });

            if (tag)
            {
                await admin.database().ref(TAGS_KEY + '/' + tag + '/' + stateName + '/' + requestUID).remove()
                .then(function() {
                  console.log('Removing request: ' + requestUID + ' from tag: ' + tag + ' succeeded.');
                })
                .catch(function(error) {
                  console.log('Removing request from tag: ' + tag + ' failed: ' + error.message);
                });
            }
        }
    }
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

function getStateName(state)
{
    switch (state)
    {
        case 0:
            return ACTIVE_KEY;
        case 1:
            return ACCEPTED_KEY;
        case 2:
            return DONE_KEY;
        case 3:
            return UNDONE_KEY;
        default:
            return "";
    }
}

function getOtherUserKind(userKind)
{
    if (userKind == REQUESTER_KEY)
        return SUPPLIER_KEY;
    else
        return REQUESTER_KEY;
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
