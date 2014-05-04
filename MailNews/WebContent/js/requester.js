     var xmlHttp;
     var response = "";
     var responseHandler;
     function createXmlHttpRequest()
     {
            if(window.ActiveXObject)
            {
             xmlHttp=new ActiveXObject("Microsoft.XMLHTTP");
           }
         else if(window.XMLHttpRequest)
         {
             xmlHttp=new XMLHttpRequest();
          }
     }
     function handleStateChange()
     {
         if(xmlHttp.readyState==4)
         {
             if(xmlHttp.status==200)
                 {
            	 	responseHandler( xmlHttp.responseText);
                 }
             else
             {
                //alert("Error loading page"+ xmlHttp.status +
     //":"+xmlHttp.statusText);
             }
         }
     }
     function startRequest(reqData,url, handler)
     {
       responseHandler = handler;
       createXmlHttpRequest();  
       xmlHttp.open("POST",url,true);
       xmlHttp.setRequestHeader("content-type","application/x-www-form-urlencoded");
       xmlHttp.onreadystatechange=handleStateChange;
       xmlHttp.send(reqData);
     }
     
     function defaultHandler(text)
     {
    	 
     }