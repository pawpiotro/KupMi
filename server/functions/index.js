const functions = require('firebase-functions');

// The Firebase Admin SDK to access the Firebase Realtime Database.
const admin = require('firebase-admin');
admin.initializeApp();

//exports.sendWelcomeEmail = functions.auth.user().onCreate(async (user) => {
//    const snapshot = await admin.database().ref('/users').push({});
//});
