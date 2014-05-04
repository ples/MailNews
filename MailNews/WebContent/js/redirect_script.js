
var secondsToRedirect = 0;
var stopState = false;
var URL;
var appURL;
var timeStamp;

function nextPage(url,time)
{
	URL = url;
	timeStamp =new Date();
	appURL = url.split("?")[0];
	secondsToRedirect = time*1000;
	startRequest("client-command=client-get&time="+secondsToRedirect, url, handleResponse);
	setTimeout("updateTime()", secondsToRedirect);
}

function handleResponse(text)
{
	if(text.search("url=")!=-1)
	{
		URL = text.substring(4,text.length);
		redirectTo(URL);
	} else
	if(text.search("stop")!=-1)
	{
		stopState = true;
		var now = new Date();
		secondsToRedirect -= (now.getTime() - timeStamp.getTime());
	}
	else
	{
		stopState = false;
		setTimeout("updateTime()", secondsToRedirect);
	}
	startRequest("client-command=client-get&time="+secondsToRedirect, appURL, handleResponse);
}

function redirectTo(url)
{
	window.location = url;
}

function updateTime()
{
	if(!stopState)
	{
		redirectTo(URL);
	} else
		{
		
		}
}