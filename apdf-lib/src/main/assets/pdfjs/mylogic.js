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
            var params = parseQueryString(document.location.search.substring(1));
            var url = params.file;
            var scale = params.scale;
            var debug = params.debug;
            var pageNum = 1;
            var pageCount = 0;
            var pdfDocument;
            var pages = [];
            function getPage(index) {
                if(debug)
                    console.log(DEBUG + " Getting page " + index);
                pdfDocument.getPage(index).then(function(pdfPage) {
                    var viewport = pdfPage.getViewport(scale);
                    var canvas = document.createElement('canvas');
                    var context = canvas.getContext('2d');
                    canvas.height = viewport.height;
                    canvas.width = viewport.width;
                    var renderContext = {
                        canvasContext: context,
                        viewport: viewport
                    };
                    pdfPage.render(renderContext).then(function(){
                        if(debug)
                            console.log(DEBUG + " Setting page " + index);
                        PDFAND.setPage(index - 1, canvas.toDataURL());
                    });
                });
            }

            function getAllPages() {
                pageNum = 1;
                getPages();
            }

            function getPages() {
                if(debug)
                    console.log(DEBUG + " Getting page " + pageNum);
                pdfDocument.getPage(pageNum).then(function(pdfPage) {
                    var viewport = pdfPage.getViewport(scale);
                    var canvas = document.createElement('canvas');
                    var context = canvas.getContext('2d');
                    canvas.height = viewport.height;
                    canvas.width = viewport.width;
                    var renderContext = {
                        canvasContext: context,
                        viewport: viewport
                    };
                    pdfPage.render(renderContext).then(function(){
                        pages.push(canvas.toDataURL());
                        if(pageNum == pageCount) {
                            if(debug)
                                console.log(DEBUG + " Setting all pages.");
                            PDFAND.setPages(pages);
                        } else {
                            pageNum = pageNum + 1;
                            getPages();
                        }
                    });
                });
            }

            PDFJS.disableWorker = true;
            PDFJS.getDocument(url).then(function(pdf) {
                pdfDocument = pdf;
                pageCount = pdfDocument.numPages;
                if(debug)
                    console.log(DEBUG + " Num of Pages : " + pageCount);
                PDFAND.setNumOfPages(pageCount);
                getPage(1);
            });