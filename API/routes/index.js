const express = require('express');
const router = express.Router();

// this is the index route, works as a place holder 

router.get('/', async (req, res) => {
    try{
        res.send("Changes will happen here."); // place holder for the index
    } catch (err){
        res.json({message: err});
    }
});

router.post('/', async (req, res) => {
    try {
        console.log("index post");
    } catch (err) {
        res.json({message: err});
    }
});

module.exports = router;