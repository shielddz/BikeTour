const express = require('express');
const router = express.Router();

// Import the model of this route
const Coordinate = require('../models/Coordinate');

//Import the user model for the helper function
const User = require('../models/User');

// Check all collected data of the coordinates
router.get('/', async (req, res) => {
    try{
        // get all the coordinates stored
        const coordinates = await Coordinate.find();
        //map through them to create a json object
        const context = {
            coordinates: coordinates.map(coordinate => {
                return {
                    key: coordinate.key,
                    lat: coordinate.lat,
                    lon: coordinate.lon,
                    adress: coordinate.adress,
                    time: coordinate.time
                }
            })
        };
        //return the data as a json response
        res.json(context);
    } catch (err){
        res.json({message: err});
    }
});

// Add a collected coordinate 
router.post('/', async (req, res) => {
    // check for user existance 
    let userExists = await checkKeyAdd(req.body.key);

    if(userExists){
        // Create a new coordinate
        const coordinate = new Coordinate({
            key: req.body.key,
            lat: req.body.lat,
            lon: req.body.lon,
            adress: req.body.adress,
            time: req.body.time
        });
        // Check if adding it is possible or not
        try {
            const savedCoordinate = await coordinate.save();
            res.json(savedCoordinate);
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