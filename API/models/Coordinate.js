const mongoose = require('mongoose');

// Schema of the Coordinates data
const CoordinatesSchema = mongoose.Schema({
    key: {
        type: String,
        required: true
    },
    lat: {
        type: Number, 
        required: true
    },
    lon: {
        type: Number, 
        required: true
    },
    adress: {
        type: String, 
        required: true
    },
    time: {
        type : Date,
        default: Date.now 
    }
});

module.exports = mongoose.model('Coordinate', CoordinatesSchema);