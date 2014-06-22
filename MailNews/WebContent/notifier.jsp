<%@page import="org.apache.jasper.tagplugins.jstl.core.ForEach"%>
<%@page import="java.util.Set"%>
<%@page import="org.mailnews.servlet.EmailContentServlet"%>
<%@page import="java.util.Map"%>
<%@page import="java.net.InetAddress"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="css/style.css" rel="stylesheet">
<script type="text/javascript" src="js/requester.js"></script>
<title>Notifier page</title>
<%
    String url =
            "http://" + InetAddress.getLocalHost().getHostAddress() + ":8080"
                    + getServletContext().getContextPath() + "/mail";
    Set<String> names = EmailContentServlet.getSessions().keySet();
%>
<script type="text/javascript">
var url = "<%=url%>";
var name;
var notydiv;
function onSelectionComplete()
{
	//document.getElementById("client-chooser").setAttribute("style", "display: none");
	//document.getElementById("notifier").setAttribute("style", "");
	var select = document.getElementById("client-chooser-select");
	document.getElementById("client-name-id").setAttribute("value", 
			select.options[select.selectedIndex].text);
	name = select.options[select.selectedIndex].text;
}
function startWithCommand(command)
{
	startRequest("notifier-command=" + command + "&client-name="+name, url, defaultHandler);
}
function setNormalSize( div )
{
	var w = div.offsetWidth;
	var h = div.offsetHeight;
	if ( w < h )
	{
		div.setAttribute("style","width: 100%;");
		div.setAttribute("style","width: 100%; height: "+div.offsetWidth+"px;");
	} 
	else
	{
		div.setAttribute("style","height: 100%;");
		div.setAttribute("style","height: 100%; width: " + div.offsetHeight +"px;");
	}
}
</script>
<style type="text/css">
#notifier {
	width: 100%; 
	height: 100%;
}
</style>
</head>
<body style="width: 100%; height: 100%;">

	<div id="client-chooser" >
		<select id="client-chooser-select" onchange="onSelectionComplete()">
			<%
			if (names.size() == 0)
			{
			    %>
			   		<option> NONE </option>
			   <%
			}else
			    for (String name : names)
			    {
			%>
					<option><%=name%></option>
			<%
			    }
			%>
		</select>
		
	</div>
	
	<div id="notifier" style="width: 100%;" >
	<input type="hidden" id="client-name-id" value="" />
		<div>
			<table>
				<tr>
					<td><button class="button-stop"
							onclick="startWithCommand('notifier-stop')">
						</button></td>
					<td><button class="button-play"
							onclick="startWithCommand('notifier-play')">
						</button></td>
				</tr>
				<tr>
					<td><button class="button-prev"
							onclick="startWithCommand('notifier-prev')">
						</button></td>
					<td><button class="button-next"
							onclick="startWithCommand('notifier-next')">
						</button></td>
				</tr>
			</table>
		</div>
	</div>
<script type="text/javascript">
	
	notydiv = document.getElementById("notifier");
	setNormalSize(notydiv);
	window.onresize = function()
	{
		setNormalSize(notydiv);
	}
	onSelectionComplete();
</script>
</body>
</html>