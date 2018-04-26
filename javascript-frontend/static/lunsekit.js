"use strict";

var LunseSearchForm;
var LunseSearchInput;
var LunseSuggestions;
var LunseHeader;
var LunseContent;
var LunseProgressBar = null;

var LunseAPIURL = '/api/v1/';

var autocompleteFocusIndex;

/**
 * Sets up global references and event handlers
 */
function lunseInit() {
    LunseSearchForm = document.getElementById("search_form");
    LunseSuggestions = document.getElementById("search_suggestions");
    LunseSearchInput = LunseSearchForm.querySelector("#q");
    LunseHeader = document.getElementById("header_center");
    LunseContent = document.getElementById("content_center");
    
    // based on https://www.w3schools.com/howto/howto_js_autocomplete.asp
    LunseSearchInput.addEventListener("input", function(e) {
        LunseGUI.hideSuggestions();
        if (!this.value) { return false;}
        autocompleteFocusIndex = -1;
        if (this.value.length > 1) {
            LunseGUI.updateSuggestions();
        } else {
            LunseGUI.hideSuggestions();
        }
    });

    LunseSearchInput.addEventListener("keydown", function(e) {
        var x = LunseSuggestions;
        if (x) x = x.getElementsByTagName("div");
        if (e.keyCode == 40) { // down
            autocompleteFocusIndex++;
            LunseGUI.selectSuggestion(x);
        } else if (e.keyCode == 38) { // up
            autocompleteFocusIndex--;
            LunseGUI.selectSuggestion(x);
        } else if (e.keyCode == 13) {
            // if the enter is pressed, prevent the form from being submitted
            if (x.length > 0) {
                e.preventDefault();
                if (autocompleteFocusIndex > -1) {
                    if (x) x[autocompleteFocusIndex].click(); 
                }
            }
        } else if (e.keyCode == 27) { // escape
            LunseGUI.hideSuggestions();
        }
    });

    document.addEventListener("click", function (e) {
        LunseGUI.hideSuggestions();
    });
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
    
    /**
     * Navigates to a given URL and uses history API if possible.
     * @param {string} url The URL to navigate to.
     * @param {boolean} replace Whether to replace an existing state or create a new one.
     */
    static goTo(url, replace = false) {
        console.log('goTo: ' + url);

        // extract the path
        var urlData = LunseTools.getURLInfo(url);

        var path = url;

		if(url.includes(LunseAPIURL)) {
			path = url.split(LunseAPIURL)[1];
			url = url.replace(LunseAPIURL, "/");
		}

        var state = {};
        switch (path) {
            case 'about':
                // about page
                state.pageID = 'about';
                state.title = 'About the Leipzig University Search Engine Project';
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
                    var pageNum = 1;
                    if (urlData.parameters['p']) {
                        pageNum = urlData.parameters['p'];
                    }
                    request.open('GET', LunseAPIURL + 'search?q=' + urlData.parameters['q'] + '&p=' + pageNum);
                    request.setRequestHeader('Content-Type', 'application/json');
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

    /**
     * Finalizes the navigation process.
     * @param {*} state The page state to use for rendering.
     * @param {*} url The URL to navigate to.
     * @param {*} replace Whether to replace an existing state or create a new one.
     */
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
            LunseRouter.goTo("search?q=" + encodeURIComponent(query));
        }
    }

    /**
     * Renders the specified page given all the data required to do so.
     * @param {*} state The page state with all the data.
     * @param {*} title The title of the page.
     */
    static displayPage(state, title) {
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
                    title = state.apiData.originalQuery + " - Leipzig University Search (unofficial)";

                    // render all the results
                    var frag = document.createDocumentFragment();
                    for (var i = 0; i < state.apiData.resultsOnPage.length; i++) {
                        frag.appendChild(Results.renderWebsite(state.apiData.resultsOnPage[i]));
                    }
                    LunseContent.appendChild(frag);
                    LunseContent.appendChild(LunseGUI.renderPagination(state.apiData));
                    LunseContent.appendChild(LunseGUI.renderBox('content_note', 'Found ' + state.apiData.totalHits + ' results within ' + state.apiData.durationInMilliseconds + 'ms'));
                }
                break;

            case "about":
                LunseContent.appendChild(LunseGUI.renderBox("content_link", '<a href="Javascript:void(0);" onclick="window.history.back();">&lt;&lt; back</a>'));
                LunseContent.appendChild(LunseGUI.renderBox("content_article", "<h1>About the Leipzig University Search Engine Project</h1><p>This website is the result of the information retrieval internship (October '17 - March '18). Students were tasked to learn about how search engines generally work and apply their gained knowledge in projects like this one. A <a href=\"https://github.com/mam10eks/search-homepage-of-university-leipzig/blob/master/ausarbeitung/output/document.pdf\">summary report</a> as well as a <a href=\"/presentation/index.html\">final presentation</a> are available online.</p>"));
                break;

            case "home":
            default:
                LunseContent.appendChild(LunseGUI.renderBox("content_mid", "<h1>Leipzig University Search</h1>"));
                LunseContent.appendChild(LunseSearchForm);
                break;
        }

        // set the title
        document.title = title;
    }

}

class LunseGUI {

    /**
     * Initiates an update of the suggestions.
     */
    static updateSuggestions() {
        console.log();

        // hide if empty
        if (!LunseSearchInput.value) {
            LunseGUI.hideSuggestions();
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
        request.open('GET', LunseAPIURL + 'suggest?query=' + encodeURIComponent(LunseSearchInput.value));
        request.send();
    }

    /**
     * Displays new suggestions in the frontend.
     * @param {*} apiData JSON object with new suggestions.
     */
    static showSuggestions(apiData) {
        var frag = document.createDocumentFragment();

        if (apiData.recommendations.length > 0) {
            for (var i = 0; i < apiData.recommendations.length; i++) {
                var sug = document.createElement('div');
                sug.innerHTML = apiData.recommendations[i].split(LunseSearchInput.value).join('<b>' + LunseSearchInput.value + '</b>') + '<input type="hidden" value="' + apiData.recommendations[i] + '">';
                frag.appendChild(sug);
                sug.addEventListener("click", function(e) {
                    LunseSearchInput.value = this.getElementsByTagName("input")[0].value;
                    LunseGUI.hideSuggestions();
                    LunseRouter.search();
                });
            }

            // show the new results
            LunseSuggestions.innerHTML = "";
            LunseSuggestions.style.display = "block";
            LunseSuggestions.appendChild(frag);
        } else {
            LunseGUI.hideSuggestions();
        }
    }

    /**
     * Hides and clears the suggestions box.
     */
    static hideSuggestions() {
        LunseSuggestions.innerHTML = '';
        LunseSuggestions.style.display = 'none';
    }

    /**
     * Selects a certain suggestion element in the UI.
     * @param {*} x The element to select.
     */
    static selectSuggestion(x) {
        if (!x) return false;
        LunseGUI.unselectSuggestions(x);
        if (autocompleteFocusIndex >= x.length) autocompleteFocusIndex = 0;
        if (autocompleteFocusIndex < 0) autocompleteFocusIndex = (x.length - 1);
        x[autocompleteFocusIndex].classList.add("active");
        if (x[autocompleteFocusIndex]) {
            LunseSearchInput.value = x[autocompleteFocusIndex].getElementsByTagName("input")[0].value;
        }
    }

    /**
     * Deselects all suggestions.
     * @param {*} x Array with all UI-suggestion-elements.
     */
    static unselectSuggestions(x) {
        for (var i = 0; i < x.length; i++) {
            x[i].classList.remove("active");
        }
    }

    /**
     * Returns the value entered in the search form.
     */
    static getQueryString() {
        return LunseSearchForm.querySelector("#q").value.trim();
    }

    /**
     * Allows to use cookies and removes the cookies hint.
     */
    static acceptCookies() {
        document.cookie = 'cookies=accepted';
        var cookieNote = document.getElementById('cookie_wrapper');
        document.getElementsByTagName("BODY")[0].removeChild(cookieNote);
    }

    /**
     * Creates a div box with the given classname(s).
     * @param {*} className The classes to use for this element.
     * @param {*} message The text content of the box.
     */
    static renderBox(className, message) {
        var hint = document.createElement("div");
        hint.setAttribute("class", className);
        hint.innerHTML = message.trim();
        return hint;
    }

    /**
     * Creates a DOM tree with the pagination links.
     * @param {*} result An object representing a single result.
     */
    static renderPagination(result) {
        var container = document.createElement("div");
        container.setAttribute("class", "content_pagination");

        if (result.previousPage) {
            container.appendChild(LunseGUI.getPaginationLink(result.previousPage));
        }
        if (result.firstPageLink) {
            container.appendChild(LunseGUI.getPaginationLink(result.firstPageLink));
        }
        if (result.namedPageLinksBefore.length > 0) {
            for (var i = 0; i < result.namedPageLinksBefore.length; i++) {
                container.appendChild(LunseGUI.getPaginationLink(result.namedPageLinksBefore[i]));
            }
        }
        var currentPage = document.createElement('span');
        currentPage.innerHTML = result.page;
        container.appendChild(currentPage);
        if (result.namedPageLinksAfter.length > 0) {
            for (var i = 0; i < result.namedPageLinksAfter.length; i++) {
                container.appendChild(LunseGUI.getPaginationLink(result.namedPageLinksAfter[i]));
            }
        }
        if (result.nextPage) {
            container.appendChild(LunseGUI.getPaginationLink(result.nextPage));
        }

        return container;
    }

    /**
     * Creates a HTML link from JSON.
     * @param {*} link An object with href and link attribute.
     */
    static getPaginationLink(linkObj) {
        var link = document.createElement('a');
        var url = LunseGUI.apiURLtoFrontend(linkObj.href);
        link.setAttribute('href', url);
        link.addEventListener('click', function(e){ LunseRouter.goTo(linkObj.href); e.preventDefault(); e.stopPropagation();}, false);
        link.innerHTML = linkObj.rel;
        return link;
    }

    static apiURLtoFrontend(url) {
        return url.replace('/api/v1/', '/');
    }
}

class Results {

    /**
     * Returns a DOM tree as the UI representation of this result.
     * @param {*} result An object containing the data of a result. 
     */
    static renderWebsite(result) {
        var container = document.createElement("div");
        container.setAttribute("class", "content_result_website");

        // add title of the document
        var title = document.createElement("a");
        title.setAttribute("href", result.targetUrl.href);
        if (result.title) {
            title.innerHTML = result.title;
        } else {
            // FIXME: can cut through entities, tags
            title.innerHTML = result.snippet.substring(0, 40);
        }
        container.appendChild(title);

        // add the URL display
        var urlfield = document.createElement("div");
        urlfield.innerHTML = Results.getDisplayURL(result.targetUrl.displayLink);
        container.appendChild(urlfield);

        // add the snippet
        var text = document.createElement("p");
        text.innerHTML = result.snippet;
        container.appendChild(text);

        // add duplicates-links
        if (result.linksToDuplicates.length > 0) {
            var duplicatesList = document.createElement("ul");
            var duplicateLabelItem = document.createElement("li");
            duplicateLabelItem.innerHTML = "Similar results:";
            duplicatesList.appendChild(duplicateLabelItem);

            for (var i = 0; i < result.linksToDuplicates.length; i++) {
                var duplicateItem = document.createElement("li");
                var duplicateLink = document.createElement("a");
                duplicateLink.setAttribute("href", result.linksToDuplicates[i].href);
                duplicateLink.innerHTML = Results.getDisplayURL(result.linksToDuplicates[i].displayLink);
                duplicateItem.appendChild(duplicateLink);
                duplicatesList.appendChild(duplicateItem);
            }

            container.appendChild(duplicatesList);
        }

        return container;
    }

    /**
     * Removes "http://" from URLs and decodes them.
     * @param {*} url The encoded URL to parse.
     */
    static getDisplayURL(url) {
        var decoded = decodeURIComponent(url);
        return (decoded.startsWith("http://") ? decoded.substring(7) : decoded);
    }
}

class ProgressBar {
    /**
     * Displays a visual indicator for background activity.
     */
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

    /**
     * Removes the visual indicator.
     */
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
    /**
     * Provides structured info about the given URL.
     * @param {*} url The URL to parse.
     */
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
