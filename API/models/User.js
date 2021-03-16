const mongoose = require('mongoose');

// Schema of the User
const UserSchema = mongoose.Schema({
    key: {
        type: String, //8 digits key
        required: true
    },
    firstName: {
        type: String,
        required: true
    },
    lastName: {
        type: String,
        required: true
    }
});

module.exports = mongoose.model('User', UserSchema);