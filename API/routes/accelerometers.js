const express = require('express');
const router = express.Router();

// Import the model of this route
const Accelerometer = require('../models/Accelerometer');

//Import the user model for the helper function
const User = require('../models/User');

// Check all collected data of the accelerometer
router.get('/', async (req, res) => {
    try{
        // get all the accelerometers stored
        const accelerometers = await Accelerometer.find();
        //map through them to create a json object
        const context = {
            accelerometers: accelerometers.map(accelerometer => {
                return {
                    key: accelerometer.key,
                    X: accelerometer.X,
                    Y: accelerometer.Y,
                    Z: accelerometer.Z,
                    time: accelerometer.time
                }
            })
        };
        //return the data as a json response
        res.json(context);
    } catch (err){
        res.json({message: err});
    }
});

// Add a collected accelerometer data 
router.post('/', async (req, res) => {
    // check for user existance 
    let userExists = await checkKeyAdd(req.body.key);

    if(userExists){
        // Create a new accelerometer data
        const accelerometer = new Accelerometer({
            key: req.body.key,
            X: req.body.X,
            Y: req.body.Y,
            Z: req.body.Z,
            time: req.body.time
        });
        try {
            // Check if adding it is possible or not
            const savedAccelerometer = await accelerometer.save();
            res.json(savedAccelerometer);
        } catch (err) {
            res.json({message: err});
        }
    }else{
        res.json({message: "No matching key found"});
    }
});

// Helper function to check the existence of the key in the users collection
function checkKeyAdd(key) {
    // Create the query
    let query = { key: key };
    // return a promise with the answer (true or false)
    return new Promise(resolve => {
        User.findOne(query, (err, user) => {
            if (err) return resolve(false);
            if (user) return resolve(true);
            return resolve(false);
        });
    });
}

// export the route
module.exports = router;