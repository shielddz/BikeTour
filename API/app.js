const express = require('express');
const app = express();
const mongoose = require('mongoose');
require('dotenv/config');
const bodyParser = require('body-parser');
const cors = require('cors');

//middleware
app.use('/superviseur', express.static(__dirname+'/public'));
app.use(cors());
app.use(bodyParser.json());

// Import Routes
const accelerometerRoute = require('./routes/accelerometers');
const coordinatesRoute = require('./routes/coordinates');
const usersRoute = require('./routes/users');
const indexRoute = require('./routes/index');

// using the routes
app.use('/accelerometers', accelerometerRoute);
app.use('/coordinates', coordinatesRoute);
app.use('/users', usersRoute);
app.use('/', indexRoute);

// Establish DB connection
mongoose.connect(
    process.env.DB_CONNECTION, 
    {   useNewUrlParser: true, 
        useUnifiedTopology: true 
    }, 
    () => console.log('connected to database')
);

// starting the server on port 1337
PORT = process.env.PORT || 1337
app.listen(PORT, () => {
    console.log('server started on port', PORT);
});