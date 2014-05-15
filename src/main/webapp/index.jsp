<html>
<head>
    <meta charset="utf-8">
    <script type="text/javascript" src="web/jquery.min.js"></script>
    <!--
    <script src="http://ajax.googleapis.com/ajax/libs/jquery/1.8/jquery.js"></script>
    -->
    <script>

       function convertFile() {

               var file = $('input[name="file"]')[0].files[0];

                var submitData = new FormData();
                submitData.append('file', file);

                 $.ajax({
                    url: 'rest/PostDOCViewerRESTService',
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
                        else if (window.confirm("Do you want to view the doc in the html?")) {
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