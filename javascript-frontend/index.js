/* CONFIG */
var port = 7531;

var apiURL = 'http://lunse.bitlium.com/api/v1/';
var redirectURL = 'http://lunse.bitlium.com/r/';
var lunseBackendURL = 'http://127.0.0.1:7532/api/v1/'
/* CONFIG END */

var express = require('express');
var cookieParser = require('cookie-parser');
var lunseUI = require('./lunseui.js');
var app = express();
app.use(cookieParser());

// disable header
app.disable('x-powered-by');

// serve style.css, lunsekit.js, logo.svg, favicon.png
app.use(express.static('static'));

app.get('/search', (req, res) => {
    lunseUI.process(req, res, "search");
});

app.get('/about', (req, res) => {
    lunseUI.process(req, res, "about");
});

app.get('/statistics', (req, res) => {
    lunseUI.process(req, res, "statistics");
});

app.get('/', (req, res) => {
    lunseUI.process(req, res, "home");
});

app.use(function (req, res, next) {
    res.status(404).send("404 - Not found!")
})

app.listen(port, () => console.log("LUNSE-FRONTEND started on port %d", port));