apiURL = require("./index.js").apiURL;

/**
 * Handles a single request.
 * @param {*} req The request object.
 * @param {*} res The response object.
 * @param {*} pageID The page type that is requested.
 */
function process(req, res, pageID) {
    var data = {};
    data.session = {};
    data.error = false;

    // libraries for async request handling and UUID generation
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

    // check which page to render
    switch (pageID) {
        case 'about':
            data.title = 'About the Leipzig University Search Engine Project';
            render(res, pageID, data);
            break;
        case "search":
            data.title = "Search";
            data.query = (req.query.q != null) ? req.query.q.trim() : '';
            data.page = (req.query.p != null) ? parseInt(req.query.p.trim()) : 1;
            if (data.page < 1) data.page = 1;
            requestPromise({ uri: apiURL+"search", qs: { q: data.query, p: data.page }, headers: { 'Cookie': 'client_id=' + data.session.client_id + ';', 'Content-Type': 'application/json' }, json: true })
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

/**
 * Returns the search form as a string.
 * @param {*} query The optional query string.
 */
function getSearchForm(query = '') {
    return '<form class="search_form" id="search_form" action="search" onsubmit="event.preventDefault(); LunseRouter.search();">\n' +
        '<input type="text" id="q" name="q" autocomplete="off" value="' + query + '" />\n<input type="submit" value=" " />\n<div id="search_suggestions"></div></form>\n';
}

/**
 * Removes "http://" from URLs and decodes them.
 * @param {*} url The encoded URL to parse.
 */
function getDisplayURL(url) {
    var decoded = decodeURIComponent(url);
    return (decoded.startsWith("http://") ? decoded.substring(7) : decoded);
}

/**
 * renders pagination links according to API data
 * @param {*} res The response object.
 * @param {*} result JSON object with result data from API.
 */
function renderPagination(res, result) {
    res.write('<div class="content_pagination">');

    if (result.previousPage) {
        res.write(getPaginationLink(result.previousPage));
    }
    if (result.firstPageLink) {
        res.write(getPaginationLink(result.firstPageLink));
    }
    if (result.namedPageLinksBefore.length > 0) {
        for (var i = 0; i < result.namedPageLinksBefore.length; i++) {
            res.write(getPaginationLink(result.namedPageLinksBefore[i]));
        }
    }
    res.write('<span>' + result.page + '</span>')
    if (result.namedPageLinksAfter.length > 0) {
        for (var i = 0; i < result.namedPageLinksAfter.length; i++) {
            res.write(getPaginationLink(result.namedPageLinksAfter[i]));
        }
    }
    if (result.nextPage) {
        res.write(getPaginationLink(result.nextPage));
    }

    res.write('</div>')
}

/**
 * Creates a HTML link from JSON.
 * @param {*} link The link object with href and rel attributes.
 */
function getPaginationLink(link) {
    return ('<a href="' + link.href.replace("api/v1/", "") + '" onclick="return LunseRouter.goTo(\'' + link.href + '\');">' + link.rel + '</a>');
}

/**
 * Outputs performance metrics for a request.
 * @param {*} res The response object.
 * @param {*} apiData JSON object with result data from API.
 */
function renderPerformance(res, apiData) {
    res.write('<div class="content_note">Found ' + apiData.totalHits + ' results within ' + apiData.durationInMilliseconds + 'ms</div>'); 
}

/**
 * Outputs code for a single website result.
 * @param {*} res The response object.
 * @param {*} result JSON object with result data from API.
 */
function renderResultWebsite(res, result) {
    res.write('<div class="content_result_website"><a href="' + result.targetUrl.href + '">');
    if (result.title) {
        res.write(result.title);
    } else {
        res.write(result.snippet.substring(0, 40));
    }
    res.write('</a><div>' + getDisplayURL(result.targetUrl.displayLink) + '</div><p>' + result.snippet + '</p>');

    // add duplicates-links
    if (result.linksToDuplicates.length > 0) {
        res.write('<ul><li>Similar results:</li>');
        for (var i = 0; i < result.linksToDuplicates.length; i++) {
            res.write('<li><a href="' + result.linksToDuplicates[i].href + '">' + getDisplayURL(result.linksToDuplicates[i].displayLink) + '</a></li>');
        }
        res.write('</ul>');
    }
    res.write('</div>');
}

/**
 * Combines template and data and outputs it.
 * @param {*} res The response object.
 * @param {*} pageID The page type.
 * @param {*} data The data object to use to fill the template.
 */
function render(res, pageID, data) {
    console.log('LunseUI - rendering ' + pageID + ' with data: ' + JSON.stringify(data));

    // Add a notice if cookies aren't allowed yet
    var cookieNote = '';
    if (!data.cookiesAllowed) {
        cookieNote = '<div id="cookie_wrapper"><div class="cookie_center">This website works best with cookies.' +
            '<a href="Javascript:void(0);" onclick="LunseGUI.acceptCookies();">Allow us to use them!</a></div></div>\n';
    }

    // adjust page title on SERPs
    if (pageID === 'search') {
        data.title = data.apiData.originalQuery + " - Leipzig University Search (unofficial)";
    }

    // head
    res.write('<!DOCTYPE html>\n<html lang="en"><head>\n');
    res.write('<meta charset="utf-8">\n<meta http-equiv="x-ua-compatible" content="ie=edge">\n<meta name="viewport" content="width=device-width, initial-scale=1">');
    res.write('<link rel="stylesheet" href="./style.css">\n<link rel="icon" href="./favicon.png">\n<script src="./lunsekit.js"></script>\n<title>' + data.title + '</title>\n</head>');

    // body
    res.write('<body onload="lunseInit()">\n<header class="header_wrapper">\n<div class="header_center" id="header_center">\n');
    if (pageID === 'search') {
        res.write(getSearchForm(data.query));
    }
    res.write('</div>\n</header>\n<div class="content_wrapper">\n<div class="content_center" id="content_center">\n');

    // add page specific contents
    switch (pageID) {
        case 'home':
            res.write('<div class="content_mid"><h1>Leipzig University Search</h1></div>\n');
            if (pageID === 'home') {
                res.write(getSearchForm(data.query));
            }
            break;
        case 'about':
            res.write('<div class="content_link"><a href="Javascript:void(0);" onclick="window.history.back();">&lt;&lt; back</a></div>');
            res.write('<div class="content_article"><h1>About the Leipzig University Search Engine Project</h1><p>This website is the result of the information retrieval internship (October &apos;17 - March &apos;18). Students were tasked to learn about how search engines generally work and apply their gained knowledge in projects like this one. A <a href="https://github.com/mam10eks/search-homepage-of-university-leipzig/blob/master/ausarbeitung/output/document.pdf">summary report</a> as well as a <a href="/presentation/index.html">final presentation</a> are available online.</p></div>');
            break;
        case 'search':
            if (data.apiData != null) {
                // render all the results
                for (var i = 0; i < data.apiData.resultsOnPage.length; i++) {
                    renderResultWebsite(res, data.apiData.resultsOnPage[i]);
                }
                renderPagination(res, data.apiData);
                renderPerformance(res, data.apiData);
            }
            break;
    }

    // footer
    res.write('</div>\n</div>\n<footer class="footer_wrapper">\n<div class="footer_center" id="footer_center">\n&copy; 2018 - ' +
        'This website is an unofficial project by students of Leipzig University - ' +
        '<a href="about" onclick="return LunseRouter.goTo(\'about\');">Learn more</a>' +
        '</div>\n</footer>\n' + cookieNote + '</body>\n</html>');
    res.end();
}

module.exports = {
    process: process
};
