Another PDF Viewer library for Android devices
==============================================
This library provides your application a capability of reading/viewing PDF's. It uses PDFJS and HTML5 at its core. It is processor independent and is very light on resource usage. Supports SDK level 8 and above.

Usage
-----
`PDFFragment fragment = PDFFragment.newInstance(url, scale, debugJs);`

URL - url of the file to open. Currently only supports `file://` protocol.


Scale - How much zooming you are looking at, directly proportional to the size of the bitmap. Ideally 2 is good enough.


debugJs - Either to print the debug logs or not within JS.


Add the above fragment using your FragmentManager to your activity.


Credits
-------
PDFJS - Mozilla (http://mozilla.github.io/pdf.js/)


VerticalViewPager - Castorflex (https://github.com/castorflex/VerticalViewPager/blob/master/library/src/main/java/fr/castorflex/android/verticalviewpager/VerticalViewPager.java)

