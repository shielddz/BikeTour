const express = require('express');
const router = express.Router();

// Import the model of this route
const User = require('../models/User');


// Check all users
router.get('/', async (req, res) => {
    try{
        // get all the users from the User's collection
        const users = await User.find();
        //map through them to create a json object
        const context = {
            users: users.map(user => {
                return {
                    key: user.key,
                    firstName: user.firstName,
                    lastName: user.lastName
                }
            })
        };
        //return the users as a json response
        res.json(context);
    } catch (err){
        res.json({message: err});
    }
});

// Add a User
router.post('/', async (req, res) => {
    // Create a new user
    const user = new User({
        key: req.body.key,
        firstName: req.body.firstName,
        lastName: req.body.lastName
    });

    // Check if adding it is possible or not
    try {
        const savedUser = await user.save();
        res.json(savedUser);
    } catch (err) {
        res.json({message: err});
    }
});

// export the route
module.exports = router;