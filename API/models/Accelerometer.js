const mongoose = require('mongoose');

// Schema of the Accelerometer data
const AccelerometerSchema = mongoose.Schema({
    key: {
        type: String,
        required: true
    },
    X: {
        type: Number, 
        required: true
    },
    Y: {
        type: Number, 
        required: true
    },
    Z: {
        type: Number, 
        required: true
    },
    time: {
        type : Date,
        default: Date.now 
    }
});

module.exports = mongoose.model('Accelerometer', AccelerometerSchema);