DOCViewer
=========

Implement Restful service to convert the office document to pdf and then show it in a html page.
JodConverter and PDF.js are used in this project.

1. GET GetDOCViewerRESTService

Returns a pdf file or html page converted from the document located in the URL indicated by the OutputType and reference parameters.
 
 
Resource URL
http://c0043436.itcs.com:8080/DOCViewer/rest/GetDOCViewerRESTService
 
Parameters
Always specify both OutputType and reference when sending the request.
 
OutputType
required
When set to html, a html page will be rendered to show the pdf file converted from the document. When set to pdf, the pdf file converted from the document will be downloaded.

Example Values: html, pdf
reference
required
Specifies the URL where the document to be converted is located. Note the document could be office document like .doc, .docx, .xlsx and .pptx files.

Example Values: http://c0043436.itcs.com:8080/DOCViewer/web/tmp/test.doc
 
Example Request
GET
http://c0043436.itcs.com:8080/DOCViewer/rest/GetDOCViewerRESTService?OutputType=html&reference=http://c0043436.itcs.com:8080/DOCViewer/web/tmp/test.doc
 
 
 
 
 
 
 
 
 
 
 
 
2.  POST PostDOCViewerRESTService

Converts the uploaded document and Returns URL in json data pointing to a html page to show the document. Note this method only expect office documents like .doc, .docx, .xlsx and .pptx files.
 
Resource URL
http://c0043436.itcs.com:8080/DOCViewer/rest/PostDOCViewerRESTService
 
 
Return Values
data.URL
points to a html page to show the document

                                                                                          Example Values: http://c0043436.itcs.com:8080/DOCViewer/web/tmp/1234.pdf

data.error

The error message if only error occurs

Example Values: Exception when using Office Service
 
Example Request
POST
http://c0043436.itcs.com:8080/DOCViewer/rest/PostDOCViewerRESTService
POST Data
data=submitData
 
<html>
<head>
    <meta charset="utf-8">
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8/jquery.js"></script>
  
 
       function convertFile() {
 
               var file = $('input[name="file"]')[0].files[0];
 
                var submitData = new FormData();
                submitData.append('file', file);
 
                 $.ajax({
                    url: '/rest/PostDOCViewerRESTService',
                    data: submitData,
                    cache: false,
                    contentType: false,
                    processData: false,
                    type: 'POST',
                    success: function(data)
                    {
                        if(data.error !=null && data.error.length > 0)
                        {
                            alert(data.error);
                        }
                        else if (window.confirm("Do you want to view the doc in the       html?")) {
                            window.location.href=data.URL;
                        };
                    },
                    error: function(e)
                    {
                       alert(e.message);
                    }
                  });
 
         };
 
    </script>
</head>
<body>
    <h1>Convert documents to HTML</h1>
    <p>
        Select a file : <input type="file" name="file" size="45" />
    </p>
    <input type="button" value="Convert It" onclick="convertFile()"/>
</body>
</html>
