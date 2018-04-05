"use strict";

var LunseSearchForm;
var LunseSearchInput;
var LunseSuggestions;
var LunseHeader;
var LunseContent;
var LunseProgressBar = null;

var LunseAPIURL = 'http://lunse.bitlium.com/api/v1/';
var LunseTrackingURL = 'http://lunse.bitlium.com/r/';
var LunseBaseURL = 'http://lunse.bitlium.com/';


function lunseInit() {
    LunseSearchForm = document.getElementById("search_form");
    LunseSuggestions = document.getElementById("search_suggestions");
    LunseSearchInput = LunseSearchForm.querySelector("#q");
    LunseHeader = document.getElementById("header_center");
    LunseContent = document.getElementById("content_center");
    LunseSearchInput.addEventListener("keyup", LunseGUI.updateSuggestions);
    LunseSearchInput.addEventListener("change", LunseGUI.updateSuggestions);
    LunseSearchInput.addEventListener("focus", LunseGUI.updateSuggestions);
    LunseSearchInput.addEventListener("focusout", LunseGUI.clearSuggestions);
}

// handle history API actions
window.onpopstate = function (event) {
    console.log("Pop: " + JSON.stringify(event.state));
    if (event.state == null) {
        LunseRouter.goTo(window.location, true);
    } else {
        LunseRouter.displayPage(event.state);
    }
};

class LunseRouter {

    static goTo(url, replace = false) {
        console.log('goTo: ' + url);
        // extract the path
        var urlData = LunseTools.getURLInfo(url);
        var path = url.replace(LunseBaseURL, '');

        var state = {};
        switch (path) {
            case 'about':
                // about page
                state.pageID = 'about';
                state.title = 'About the Leipzig University Search Engine Project';
                LunseRouter.goFinish(state, url, replace);
                break;
            case 'statistics':
                // statistics page
                state.pageID = 'statistics';
                state.title = 'Statistics - Leipzig University Search';
                LunseRouter.goFinish(state, url, replace);
                break;
            default:
                if (!path) {
                    // home page
                    state.pageID = 'home';
                    state.title = 'Leipzig University Search (unofficial)';
                    LunseRouter.goFinish(state, url, replace);
                } else if (path.startsWith('search')) {
                    // search result page
                    state.pageID = 'search';
                    var request = new XMLHttpRequest();
                    request.onreadystatechange = function () {
                        if (request.readyState == XMLHttpRequest.DONE) {
                            console.log(request.responseText);
                            if (LunseProgressBar != null) LunseProgressBar.stop();
                            state.apiData = JSON.parse(request.responseText);
                            LunseRouter.goFinish(state, url, replace);
                        }
                    }
                    LunseProgressBar = new ProgressBar();
                    request.open('GET', LunseAPIURL + 'results?q=' + urlData.parameters['q'] + '&p=' + urlData.parameters['p']);
                    request.send();
                } else {
                    // error 404 page
                    state.pageID = 'error404';
                    state.title = 'Page not found - Leipzig University Search (unofficial)';
                    LunseRouter.goFinish(state, url, replace);
                }
        }
        return false;
    }

    static goFinish(state, url, replace) {
        // replace the state if it was a history navigation
        if (replace) {
            history.replaceState(state, state.title, url);
        } else {
            history.pushState(state, state.title, url);
        }

        // set the title and hand off to the render function
        LunseRouter.displayPage(state, state.title);
    }

    /**
     * Starts a search.
     * @param {*} queryString The query to search for. Default (null) will use the form value.
     */
    static search(queryString = null) {
        // get the search string if not provided
        var query;
        if (queryString == null) {
            query = LunseGUI.getQueryString();
        } else {
            query = queryString;
        }
        if (query != "") {
            LunseRouter.goTo(LunseBaseURL + "search?q=" + encodeURIComponent(query));
        }
    }

    /**
     * Renders the specified page given all the data required to do so.
     * @param {*} state 
     * @param {*} title 
     */
    static displayPage(state, title) {
        //console.log("Display: " + JSON.stringify(state));

        // clear page and header content
        while (LunseHeader.firstChild) {
            LunseHeader.removeChild(LunseHeader.firstChild);
        }
        while (LunseContent.firstChild) {
            LunseContent.removeChild(LunseContent.firstChild);
        }

        // render the content
        switch (state.pageID) {
            case "search":
                LunseHeader.appendChild(LunseSearchForm);
                if (state.apiData != null) {
                    title = state.apiData.query + " - Leipzig University Search (unofficial)";

                    // did you mean...
                    if (state.apiData.alternativeQuery != null) {
                        // ONCLICK MISSING
                        LunseContent.appendChild(LunseGUI.renderBox('content_hint', 'Did you mean <i><a href="./search?q=' + state.apiData.alternativeQuery + '">' + decodeURIComponent(state.apiData.alternativeQuery) + '</i></a>?'));
                    }
                    // render all the results
                    var frag = document.createDocumentFragment();
                    for (var i = 0; i < state.apiData.queryResults.length; i++) {
                        switch (state.apiData.queryResults[i].type) {
                            case 'website':
                                frag.appendChild(Results.renderWebsite(state.apiData.queryResults[i]));
                                break;
                            default:
                            // handle different result types
                        }
                    }
                    LunseContent.appendChild(frag);
                    LunseContent.appendChild(LunseGUI.renderPagination(state.apiData.currentPage, state.apiData.pageCount));
                    // performance metrics
                    if (state.apiData.performance != null) {
                        LunseContent.appendChild(LunseGUI.renderBox('content_note', 'Got ' + state.apiData.performance.totalResultCount + ' results within ' + state.apiData.performance.retrievalTime + 'ms.'));
                    }
                }
                break;

            case "about":
                LunseContent.appendChild(LunseGUI.renderBox("content_link", '<a href="Javascript:void(0);" onclick="window.history.back();">&lt;&lt; back</a>'));
                LunseContent.appendChild(LunseGUI.renderBox("content_article", "<h1>About the Leipzig University Search Engine Project</h1><p>Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet. Lorem ipsum dolor sit amet, consetetur sadipscing elitr, sed diam nonumy eirmod tempor invidunt ut labore et dolore magna aliquyam erat, sed diam voluptua. At vero eos et accusam et justo duo dolores et ea rebum. Stet clita kasd gubergren, no sea takimata sanctus est Lorem ipsum dolor sit amet.</p>"));
                break;

            case "statistics":
                LunseContent.appendChild(LunseGUI.renderBox("content_link", '<a href="Javascript:void(0);" onclick="window.history.back();">&lt;&lt; back</a>'));
                LunseContent.appendChild(LunseGUI.renderBox("content_mid", "<h1>Statistics</h1>"));
                LunseContent.appendChild(LunseGUI.renderTable({ "head": ["Metric", "Value"], body: [["Indexed pages", "300.000"]] }));
                break;

            case "home":
            default:
                LunseContent.appendChild(LunseGUI.renderBox("content_mid", "<h1>Leipzig University Search</h1>"));
                LunseContent.appendChild(LunseGUI.renderBox("content_logo", "<img src=\"./static/logo.svg\" />"));
                LunseContent.appendChild(LunseSearchForm);
                break;
        }

        // set the title
        document.title = title;
    }

}

class LunseGUI {

    static updateSuggestions() {
        console.log();

        // hide if empty
        if (!LunseSearchInput.value) {
            LunseGUI.clearSuggestions();
            return;
        }

        // inititate XHR otherwise
        var request = new XMLHttpRequest();
        request.onreadystatechange = function () {
            if (request.readyState == XMLHttpRequest.DONE) {
                console.log(request.responseText);
                LunseGUI.showSuggestions(JSON.parse(request.responseText));
            }
        }
        request.open('GET', LunseAPIURL + 'suggestions?q=' + encodeURIComponent(LunseSearchInput.value));
        request.send();
    }

    static showSuggestions(apiData) {
        var frag = document.createDocumentFragment();

        for (var i = 0; i < apiData.suggestions.length; i++) {
            var sug = document.createElement('a');
            sug.innerHTML = apiData.suggestions[i];
            frag.appendChild(sug);
        }

        // show the new results
        LunseSuggestions.innerHTML = "";
        LunseSuggestions.style.display = "block";
        LunseSuggestions.appendChild(frag);
    }

    static clearSuggestions() {
        LunseSuggestions.innerHTML = '';
        LunseSuggestions.style.display = 'none';
    }

    static getQueryString() {
        return LunseSearchForm.querySelector("#q").value.trim();
    }

    static acceptCookies() {
        document.cookie = 'cookies=accepted';
        var cookieNote = document.getElementById('cookie_wrapper');
        document.getElementsByTagName("BODY")[0].removeChild(cookieNote);
    }

    static renderBox(className, message) {
        var hint = document.createElement("div");
        hint.setAttribute("class", className);
        hint.innerHTML = message.trim();
        return hint;
    }

    static renderPagination(current, max) {
        var container = document.createElement("div");
        container.setAttribute("class", "content_pagination");

        var limit = (current > 10 ? 4 : 11);

        for (var i = 1; i <= max; i++) {
            // only render if we're in the proximity of the result or top10
            if ((i < limit) || (Math.abs(i - current) < 3)) {
                var num;
                if (i == current) {
                    num = document.createElement("span");
                } else {
                    num = document.createElement("a");
                    var pageLink = LunseBaseURL + "search?q=" + encodeURIComponent(LunseGUI.getQueryString()) + "&p=" + i;
                    num.href = pageLink;
                    num.addEventListener('click', function () { event.preventDefault(); return LunseRouter.goTo(this.href); });
                }
                num.innerHTML = i;
                container.appendChild(num);
            } else {
                // append "...""
                var num = document.createElement("span");
                num.innerHTML = "...";
                container.appendChild(num);
                if (i > current) {
                    // we can stop if we're out of top10 and (far) above current
                    return container;
                } else {
                    i = current - 3;
                }
            }
        }

        return container;
    }

    static renderTable(jsonTable) {
        var table = document.createElement('table');
        if (jsonTable.head) {
            var line = document.createElement('tr');
            for (var i = 0; i < jsonTable.head.length; i++) {
                var cell = document.createElement('th');
                cell.innerHTML = jsonTable.head[i];
                line.appendChild(cell);
            }
            table.appendChild(line);
        }
        if (jsonTable.body) {
            for (var j = 0; j < jsonTable.body.length; j++) {
                var line = document.createElement('tr');
                for (var i = 0; i < jsonTable.body[j].length; i++) {
                    var cell = document.createElement('td');
                    cell.innerHTML = jsonTable.body[j][i];
                    line.appendChild(cell);
                }
                table.appendChild(line);
            }
        }
        return table;
    }
}

class Results {
    static renderWebsite(result) {
        var container = document.createElement("div");
        container.setAttribute("class", "content_result_website");

        var title = document.createElement("a");
        title.setAttribute("href", Results.getTrackingURL(result.url));
        title.innerHTML = result.title;
        container.appendChild(title);

        var urlfield = document.createElement("div");
        urlfield.innerHTML = Results.getDisplayURL(result.url);
        if (result.mimeType != null) {
            var tag = document.createElement("div");
            tag.innerHTML = result.mimeType;
            urlfield.insertAdjacentElement('afterbegin', tag);
        }
        container.appendChild(urlfield);

        var text = document.createElement("p");
        text.innerHTML = result.snippet;
        container.appendChild(text);

        return container;
    }

    static getDisplayURL(url) {
        var decoded = decodeURIComponent(url);
        return (decoded.startsWith("http://") ? decoded.substring(7) : decoded);
    }

    static getTrackingURL(url) {
        return (LunseTrackingURL + url);
    }
}

class ProgressBar {
    constructor() {
        this.uiElement = document.createElement('div');
        this.uiElement.setAttribute('class', 'progress_bar');
        document.getElementsByTagName("BODY")[0].appendChild(this.uiElement);
        this.progress = 0;
        this.change = 1;
        var pb = this;
        this.intervalID = setInterval(function () {
            if (pb.uiElement != null) {
                pb.uiElement.setAttribute("style", "background-color: rgb(34, " + (68 + Math.floor(3 * pb.progress)) + ", " + (153 + Math.floor(2 * pb.progress)) + ")");
                pb.progress += pb.change;
                if ((pb.progress > 24) || (pb.progress < 1)) pb.change *= -1;
            }
        }, 50);
    }

    stop() {
        if (this.intervalID != null) {
            clearInterval(this.intervalID);
            delete this.intervalID;
        }
        if ((this.uiElement != null) && (this.uiElement.parentElement == document.getElementsByTagName("BODY")[0])) {
            document.getElementsByTagName("BODY")[0].removeChild(this.uiElement);
            delete this.uiElement;
        }
    }
}

class LunseTools {
    static getURLInfo(url) {
        // create a temporary anchor element
        var anchor = document.createElement('a');
        anchor.href = url;

        // extract query parameters
        var params = {};
        var paramPair;
        var queries = anchor.search.replace(/^\?/, '').split('&');
        for (var i = 0; i < queries.length; i++) {
            paramPair = queries[i].split('=');
            params[paramPair[0]] = paramPair[1];
        }

        return {
            protocol: anchor.protocol,
            hostname: anchor.hostname,
            pathname: anchor.pathname,
            parameters: params,
            hash: anchor.hash
        };
    }
}