function process(req, res, pageID) {
    var data = {};
    data.session = {};
    data.error = false;

    // 
    var requestPromise = require('request-promise');
    var uuid = require('node-uuid');

    // response is always HTML
    res.type('text/html');

    // cookie handling
    if (req.cookies.cookies === 'accepted') {
        data.cookiesAllowed = true;
        if (req.cookies.client_id === undefined) {
            data.session.client_id = uuid.v4()
            res.cookie('client_id', data.session.client_id, { maxAge: 30 * 24 * 60 * 60 * 1000, httpOnly: true });
        } else {
            data.session.client_id = req.cookies.client_id;
        }
    } else {
        data.cookiesAllowed = false;
        data.session.client_id = "anonymous";
    }
    console.log('Cookies: ' + JSON.stringify(req.cookies));

    // 
    switch (pageID) {
        case 'about':
            data.title = 'About the Leipzig University Search Engine Project';
            render(res, pageID, data);
            break;
        case "statistics":
            data.title = "Statistics - Leipzig University Search Engine Project";
            render(res, pageID, data);
            break;
        case "search":
            data.title = "Search";
            data.query = (req.query.q != null) ? req.query.q.trim() : '';
            data.page = (req.query.p != null) ? parseInt(req.query.p.trim()) : 1;
            if (data.page < 1) data.page = 1;
            requestPromise({ uri: 'http://lunse.bitlium.com/api/v1/results', qs: { q: data.query, p: data.page }, headers: { 'Cookie': 'client_id=' + data.session.client_id + ';' }, json: true })
                .then((resData) => {
                    data.apiData = resData;
                    render(res, pageID, data);
                })
                .catch((err) => {
                    console.log('LunseUI - error: ' + err)
                    data.error = true;
                    render(res, pageID, data);
                });
            break;
        default:
            data.title = 'Leipzig University Search Engine (unofficial)';
            render(res, pageID, data);
    }
}

// returns just the searchform
function getSearchForm(query = '') {
    return '<form class="search_form" id="search_form" action="search" onsubmit="event.preventDefault(); LunseRouter.search();">\n' +
        '<input type="text" id="q" name="q" autocomplete="off" value="' + query + '" />\n<input type="submit" value=" " />\n<div id="search_suggestions"></div></form>\n';
}

// sends all the HTML put together with the data
function render(res, pageID, data) {
    console.log('LunseUI - rendering ' + pageID + ' with data: ' + JSON.stringify(data));

    /* COOKIES */
    var cookieNote = '';
    if (!data.cookiesAllowed) {
        cookieNote = '<div id="cookie_wrapper"><div class="cookie_center">This website works best with cookies.' +
            '<a href="Javascript:void(0);" onclick="LunseGUI.acceptCookies();">Allow us to use them!</a></div></div>\n';
    }

    // HEAD
    res.write('<!DOCTYPE html>\n<html lang="en"><head>\n');
    res.write('<meta charset="utf-8">\n<meta http-equiv="x-ua-compatible" content="ie=edge">\n<meta name="viewport" content="width=device-width, initial-scale=1">');
    res.write('<link rel="stylesheet" href="./style.css">\n<link rel="icon" href="./favicon.png">\n<script src="./lunsekit.js"></script>\n<title>' + data.title + '</title>\n</head>');

    // BODY
    res.write('<body onload="lunseInit()">\n<header class="header_wrapper">\n<div class="header_center" id="header_center">\n');
    if (pageID === 'search') {
        res.write(getSearchForm(data.query));
    }
    res.write('</div>\n</header>\n<div class="content_wrapper">\n<div class="content_center" id="content_center">\n');

    switch (pageID) {
        case 'home':
            res.write('<div class="content_mid"><h1>Leipzig University Search</h1></div>\n');
            res.write('<div class="content_logo"><img src="./static/logo.svg" /></div>\n');
            if (pageID === 'home') {
                res.write(getSearchForm(data.query));
            }
            break;
        case 'about':
            res.write('<div class="content_link"><a href="Javascript:void(0);" onclick="window.history.back();">&lt;&lt; back</a></div>');
            res.write('<div class="content_article"><h1>About the Leipzig University Search Engine Project</h1><p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p></div>');
            break;
        case 'statistics':
            res.write('<div class="content_link"><a href="Javascript:void(0);" onclick="window.history.back();">&lt;&lt; back</a></div>');
            res.write('<div class="content_article"><h1>Statistics</h1>\n<p>' + JSON.stringify(data.apiData) + '</p></div>\n');
            break;
        case 'search':
            res.write('test');
            break;
    }

    res.write('</div>\n</div>\n<footer class="footer_wrapper">\n<div class="footer_center" id="footer_center">\n&copy; 2018 - ' +
        'This website is an unofficial project by students of Leipzig University - ' +
        '<a href="about" onclick="return LunseRouter.goTo(\'about\');">Learn more</a> - ' +
        '<a href="statistics" onclick="return LunseRouter.goTo(\'statistics\');">Statistics</a>\n</div>\n</footer>\n' + cookieNote + '</body>\n</html>');
    res.end();
}

module.exports = {
    process: process
};