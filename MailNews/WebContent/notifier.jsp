<%@page import="java.net.InetAddress"%>
<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link href="style.css" rel="stylesheet">
<script type="text/javascript" src="requester.js"></script>
<title>Notifier page</title>
<%String url =  "http://"+InetAddress.getLocalHost().getHostAddress() + ":8080" + getServletContext().getContextPath()+"/mail"; %>
</head>
<body style="width: 100%; height: 100%;">
	<div id="notifier">
		<table>
			<tr>
				<td><button class="button-stop"
				onclick="startRequest('ajax=notifier-stop', '<%=url%>', defaultHandler)">
				</button></td>
				<td><button class="button-play"
				onclick="startRequest('ajax=notifier-play', '<%=url %>', defaultHandler)">
				</button></td>
			</tr>
			<tr>
				<td><button class="button-prev"
				onclick="startRequest('ajax=notifier-prev', '<%=url%>', defaultHandler)">
				</button></td>
				<td><button class="button-next"
				onclick="startRequest('ajax=notifier-next', '<%=url%>', defaultHandler)">
				</button></td>
			</tr>
		</table>
	</div>

</body>
</html>