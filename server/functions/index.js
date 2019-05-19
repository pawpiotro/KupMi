const functions = require('firebase-functions');
const admin = require('firebase-admin');

// Data structures keys
const USERS_KEY = 'users';

const EMAIL_KEY = "email";
const NAME_KEY = "name";
const PHONE_NUMBER_KEY = "phoneNumber";
const REPUTATION_KEY = "reputation";

const REQUESTS_KEY = 'requests';
const REQUESTER_KEY = "requester";
const SUPPLIER_KEY = "supplier";

const USER_UID_KEY = "userUID";
const DEADLINE_KEY = "deadline";
const STATE_KEY = "state";

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
    await setUser(new DbUser(user.uid, user.email, '', '', 0));
});

exports.deleteUser = functions.auth.user().onDelete(async (user) => {
    // Clear user data
    await setUser(new DbUser(user.uid, '', '', '', 0));

    await deleteUserRequests(user.uid, REQUESTER_KEY);
    await deleteUserRequests(user.uid, SUPPLIER_KEY);
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
              console.error('Removing request from tag: ' + deletedTag + ' failed: ' + error.message);
            });
        }
        else
        {
            console.log('Wrong deletedTag value.');
        }
    }
});*/

// Only state and deadline change
exports.changeRequest = functions.database.ref(REQUESTS_KEY +
    '/' + REQUESTER_KEY + '/{userUID}/{requestUID}')
.onWrite(async (change, context) => {

    const userKind = REQUESTS_KEY;
    const otherUserKind = SUPPLIER_KEY;
    const userUID = context.params.userUID;
    const requestUID = context.params.requestsUID;

    if (change.before.exists() && change.after.exists())
    {
        const oldRequest = change.before.val();
        const oldUserUID = oldRequest.userUID;
        const oldState = oldRequest.state;
        const oldStateName = getStateName(oldState);
        const oldTag = oldRequest.tag;

        const request = change.after.val();
        const otherUserUID = request.userUID;
        const deadline = request.deadline;
        const title = request.title;
        const state = request.state;
        const tag = request.tag;
        const stateName = getStateName(state);

        if (otherUserUID && oldStateName && stateName)
        {
            if ((stateName == ACCEPTED_KEY && oldState == state))
            {
                // TODO: Send info to user
                console.log('Other user accepted this request: ' + requestUID);
            }
            else if (stateName == ACCEPTED_KEY && oldState == UNDONE_KEY)
            {
                // TODO: Send info to user
                console.log('Requester cancelled this request: ' + requestUID);
            }
            else if (stateName == UNDONE_KEY && oldState == ACCEPTED_KEY)
            {
                // TODO: Send info to user
                console.log('Other user just accepted this request: ' + requestUID);
            }
            else
            {
                if (state != oldState && stateName == ACTIVE_KEY)
                {
                    await admin.database().ref(REQUESTS_KEY + '/' + userKind + '/' +
                    userUID + '/' + requestUID + '/' + USER_UID_KEY).set('')
                    .then(function() {
                      console.log('Clear supplier UID for request: ' + requestUID + ' succeeded.');
                    })
                    .catch(function(error) {
                      console.error('Clear supplier UID for request: ' + requestUID + ' failed: '
                      + error.message);
                    });

                    await admin.database().ref(REQUESTS_KEY + '/' + otherUserKind + '/' +
                    otherUserUID + '/' + requestUID).remove()
                    .then(function() {
                      console.log('Removing request: ' + requestUID + ' from ' + otherUserKind + ' succeeded.');
                    })
                    .catch(function(error) {
                      console.error('Removing request from ' + otherUserKind + ' failed: ' + error.message);
                    });
                }
                else
                {
                    await admin.database().ref(REQUESTS_KEY + '/' + otherUserKind + '/' +
                    otherUserUID + '/' + requestUID).set(
                        new DbRequest(userUID, deadline, title, tag, state)
                    )
                    .then(function() {
                      console.log('Adding / updating request : ' + requestUID + ' for supplier succeeded.');
                    })
                    .catch(function(error) {
                      console.error('Adding / updating request: ' + requestUID + ' for supplier failed: '
                      + error.message);
                    });
                }

                if (state != oldState)
                {
                    const oldRequestLocationPath = REQUESTS_LOCATIONS_KEY + '/' + oldStateName + '/' + requestUID;
                    const newRequestLocationPath = REQUESTS_LOCATIONS_KEY + '/' + stateName + '/' + requestUID;

                    moveDataToAnotherState(oldRequestLocationPath, newRequestLocationPath,
                    'Removing old location data during moving request: ' + requestUID +
                    ' to another state failed: ',
                    'Setting new location data during moving request: ' + requestUID +
                    ' to another state failed: ',
                    'Moving request: ' + requestUID + ' to another state succedded.');
                }

                if (state != oldState || tag != oldTag)
                {
                    const oldRequestTagPath = REQUESTS_TAG_KEY + '/' + oldTag + '/' +
                    oldStateName + '/' + requestUID;
                    const newRequestTagPath = REQUESTS_TAG_KEY + '/' + tag + '/' + stateName + '/' + requestUID;

                    moveDataToAnotherState(oldRequestTagPath, newRequestTagPath,
                    'Removing old tag data during moving request: ' + requestUID +
                    ' to another state failed: ',
                    'Setting new tag data during moving request: ' + requestUID +
                    ' to another state failed: ',
                    'Moving request: ' + requestUID + ' to another state succedded.');
                }
            }
        }
        else
        {
            console.log('Invalid data: otherUserUID - ' + otherUserUID + ', oldStateName - ' +
            oldStateName + ', stateName - ' + stateName);
        }
    }
});

exports.deleteRequesterRequest = functions.database.ref(REQUESTS_KEY +
    '/' + REQUESTER_KEY + '/{userUID}/{requestUID}').onDelete(async (snapshot, context) => {

    const requestUID = context.params.requestUID;

    const request = snapshot.val();
    if (request != null)
    {
        const stateName = getStateName(request.state);
        const tag = request.tag;

        if (stateName && stateName == ACTIVE_KEY)
        {
            await admin.database().ref(REQUESTS_DETAILS_KEY + '/' + requestUID).remove()
            .then(function() {
              console.log('Removing request: ' + requestUID + ' details succeeded.');
            })
            .catch(function(error) {
              console.error('Removing request details failed: ' + error.message);
            });

            await admin.database().ref(REQUESTS_LOCATIONS_KEY + '/' + stateName + '/' + requestUID).remove()
            .then(function() {
              console.log('Removing request: ' + requestUID + ' location succeeded.');
            })
            .catch(function(error) {
              console.error('Removing request location failed: ' + error.message);
            });

            if (tag)
            {
                await admin.database().ref(TAGS_KEY + '/' + tag + '/' + stateName + '/' + requestUID).remove()
                .then(function() {
                  console.log('Removing request: ' + requestUID + ' from tag: ' + tag + ' succeeded.');
                })
                .catch(function(error) {
                  console.error('Removing request from tag: ' + tag + ' failed: ' + error.message);
                });
            }
        }
    }
});

exports.deleteSupplierRequest = functions.database.ref(REQUESTS_KEY +
    '/' + SUPPLIER_KEY + '/{userUID}/{requestUID}').onDelete(async (snapshot, context) => {

    const requestUID = context.params.requestUID;

    const request = snapshot.val()
    if (request != null)
    {
        const otherUserUID = request.userUID;
        const dbRequest = new DbRequest("", request.deadline, request.title, request.tag, 1);

        const path = REQUESTS_KEY + '/' + REQUESTER_KEY + '/' +
        otherUserUID + '/' + requestUID;

        await admin.database().ref(path).set(dbRequest)
        .then(function() {
          console.log('Removing supplier UID for request: ' + requestUID + ' succeeded.');
        })
        .catch(function(error) {
          console.error('Removing supplier UID for request: ' + requestUID + ' failed: '
          + error.message);
        });
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
      console.error('Adding/updating failed: ' + error.message);
    });
}

async function deleteUserRequests(userUID, userKind)
{
    const path = REQUESTS_KEY + '/' + userKind + '/' + userUID;

    await admin.database().ref(path).remove()
    .then(function() {
      console.log('Removing all requests of user: ' + userUID + ' from ' + userKind + ' branch succeeded.');
    })
    .catch(function(error) {
      console.error('Removing all requests of user: ' + userUID + ' from ' + userKind + ' branch failed: '
      + error.message);
    });
}

function moveDataToAnotherState(oldPath, newPath, removingErrorLog, settingErrorLog,
successLog)
{
    const objectToMove = null;

    admin.database.ref(oldPath)
    .then(function (dataSnapshot)
    {
        objectToMove = dataSnapshot.val();
        if (objectToMove != null)
        {
            admin.database().ref(oldPath).remove()
            .then(function()
            {
                admin.database().ref(newPath).set(objectToMove)
                .then(function()
                {
                    console.log(successLog);
                })
                .catch(function(error)
                {
                  console.error(settingErrorLog + error.message);
                });
            })
            .catch(function(error)
            {
              console.error(removingErrorLog + error.message);
            });
        }
    });
}

function getStateName(state)
{
    switch (state)
    {
        case 0:
            return ACCEPTED_KEY;
        case 1:
            return ACTIVE_KEY;
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

function DbRequest(userUID, deadline, title, tag, state)
{
    this.userUID = userUID;
    this.deadline = deadline;
    this.title = title;
    this.tag = tag;
    this.state = state;
}
