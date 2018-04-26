/* CONFIG */
var port = 7531;

exports.apiURL = 'http://localhost:7777/api/v1/';
/* CONFIG END */

// load dependencies
var express = require('express');
var cookieParser = require('cookie-parser');
var lunseUI = require('./lunseui.js');
var app = express();
app.use(cookieParser());

// disable header
app.disable('x-powered-by');

// serve style.css, lunsekit.js, logo.svg, favicon.png
app.use(express.static('static'));

// defined all (intended) endpoints
app.get('/search', (req, res) => {
    lunseUI.process(req, res, "search");
});
app.get('/about', (req, res) => {
    lunseUI.process(req, res, "about");
});
app.get('/', (req, res) => {
    lunseUI.process(req, res, "home");
});

// default handler for HTTP 404
app.use(function (req, res, next) {
    res.status(404).send("404 - Not found!")
})

app.listen(port, () => console.log("LUNSE-FRONTEND started on port %d", port));
