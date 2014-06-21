var DEBUG = "PDFFragment";
console.log(DEBUG + " Loaded viewer.html");
function parseQueryString(query) {
    var parts = query.split('&');
    var params = {};
    for (var i = 0, ii = parts.length; i < ii; ++i) {
        var param = parts[i].split('=');
        var key = param[0];
        var value = param.length > 1 ? param[1] : null;
        params[decodeURIComponent(key)] = decodeURIComponent(value);
    }
    return params;
}

var PAGES_KEY = "pages";
var params = parseQueryString(document.location.search.substring(1));
var url = params.file;
var scale = params.scale;
debug = params.debug;
var pagesInMemory = 3;

var pageNum = 1;
var pageCount = 0;
var pdfDocument;

function getPage(index, callback) {
    if(debug)
        console.log(DEBUG + " Getting page " + index);
    if(index < 0 || index > pageCount) return;
    if(typeof pdfDocument == 'undefined') {
        window.setTimeout(function(index, callback){ if(debug) console.log(DEBUG + " Retrying page " + index); getPage(index, callback); }, 1000);
        return;
    }

    var pages = sessionStorage.getItem(PAGES_KEY);
    if(pages)
        for(var i=0; i< pages.length; i++) {
            var page = pages[i];
            if(page[0] == ("" + index)) {
                callback(index, page[1], true);
                return;
            }
        }

    pdfDocument.getPage(index).then(function(pdfPage) {
        var viewport = pdfPage.getViewport(scale); // Caution: high the scale, could cause memory issues.
        var canvas = document.createElement('canvas');
        var context = canvas.getContext('2d');
        canvas.height = viewport.height;
        canvas.width = viewport.width;
        var renderContext = {
            canvasContext: context,
            viewport: viewport
        };

        pdfPage.render(renderContext).then(function(){
            var dataUrl = canvas.toDataURL("image/jpeg");
            if(debug)
                console.log(DEBUG + " Setting page " + index + " : " + dataUrl);
            var pages = sessionStorage.getItem(PAGES_KEY);
            if(pages) {
                if(pages.length == 3)
                    pages.shift();
            } else {
                pages = [];
            }
            pages.push(["" + index, dataUrl]);
            callback(index, dataUrl, true);
        });
    });
}

function loadAllPages(callback) {
    var loaded = sessionStorage.getItem("arePagesLoaded");
    if(loaded == '1') {
        callback();
        return;
    }

    pageNum = 1;
    getPages(callback);
}

function getPages(callback) {
    getPage(pageNum, function(index, page) {
        if(pageNum == pageCount) {
            sessionStorage.setItem("arePagesLoaded", "1");
            if(debug)
                console.log(DEBUG + " Loaded all pages.");
            callback();
        } else {
            pageNum = pageNum + 1;
            getPages(callback);
        }
    });
}

function init(callback) {
    PDFJS.disableWorker = true;
    PDFJS.getDocument(url).then(function(pdf) {
        pdfDocument = pdf;
        pageCount = pdfDocument.numPages;
        if(debug)
            console.log(DEBUG + " Num of Pages : " + pageCount);
        PDFAND.setNumOfPages(pageCount);
        callback();
    });
}

init(function() {});