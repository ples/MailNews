<%@page import="org.mailnews.properties.Constants"%>
<%@page import="org.mailnews.properties.AppProperties"%>
<%@page import="org.mailnews.helper.MessageBean"%>
<%@page import="org.mailnews.helper.MessagesDataSingleton"%>
<%@page import="org.mailnews.helper.MessageData"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%
	if (!AppProperties.isInitialized())
	{
		AppProperties.getInstance().loadFromFile(getServletContext().getRealPath("/") + "/" + Constants.PROPS_FILE);
	}
	MessagesDataSingleton.setPath(getServletContext().getRealPath("/"));
	itsMessageData = MessagesDataSingleton.getInstance().getMessageData();
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1">
<link rel="stylesheet" href="css/jquery-ui-1.10.4.custom.css" />
<link rel="stylesheet" href="css/admin-view-style.css" />
<script type="text/javascript" src="js/jquery/jquery-1.10.2.js"></script>
<script type="text/javascript"
	src="js/jquery/jquery-ui-1.10.4.custom.min.js"></script>
<script type="text/javascript" src="js/admin-helper-script.js"></script>
<script type="text/javascript" src="js/noty/packaged/jquery.noty.packaged.min.js"></script>
<title>Admin view</title>
</head>
<body>
	<script type="text/javascript">
		$(function() {
			$("#tabs").tabs();
		});
	</script>
	<div id="tabs">
		<ul>
			<li><a href="#filter-options">Filter options</a></li>
			<li><a href="#read-speed-options">Read speed options</a></li>
			<li><a href="#mail-manager">Mail manager</a></li>
		</ul>
		<div id="filter-options">
			<form id="filter-form" action="" method="post">
				<div id="filter-save-status"></div>
				<table>
					<tr>
						<td>Time interval:</td>
						<td><input name="mail-life-interval" type="text" size="5" /></td>
						<td>days.</td>
					</tr>
					<tr>
						<td>Refresh interval:</td>
						<td><input name="refresh-interval" type="text" size="5" /></td>
						<td>minutes.</td>
					</tr>
				</table>
				<input name="admin-command" value="filter-save" type="hidden" />
				<button type="submit">Save</button>
			</form>
		</div>
		<div id="read-speed-options">
			<form id="read-speed-form" action="" method="post">
				<div id="speed-save-status"></div>
				<table>
					<tr>
						<td>Symbols per second:</td>
						<td><input name="symb-per-sec" type="text" size="5" /></td>
						<td>symbols</td>
					</tr>
					<tr>
						<td>Image watch time:</td>
						<td><input name="image-watch-time" type="text" size="5" /></td>
						<td>seconds</td>
					</tr>
				</table>
				<input name="admin-command" value="read-speed-save" type="hidden" />
				<button type="submit">Save</button>
			</form>
		</div>
		<div id="mail-manager">
			<div id="mails">
				<ol id="subjects">
					<%
						for(MessageBean eachMessage : itsMessageData.getMessages())
								{
					%>
					<li id="<%=eachMessage.getMsgId()%>" class="ui-widget-content"><%=eachMessage.getSubject()%></li>
					<%
						}
					%>
				</ol>
			</div>
			<div id="mails-buttons">
				<form id="mail-manager-form" action="" method="post">
				<input id="selected-mails-input" type="hidden" name="selected-mails-id">
					<button type="submit" name="spam-mark">Mark as SPAM</button>
					<button type="submit" name="delete-mail">Delete</button>
					<button type="submit" name="spam-mark">Edit</button>
				</form>
			</div>
			<div id="mails-content">
				<%
					for (MessageBean eachMessage : itsMessageData.getMessages()) {
				%>
				<div id="<%=eachMessage.getMsgId()%>-content" style="display: none;"
					class="ui-widget-content">
					<%=eachMessage.getContent()%>
					<%
						if(eachMessage.getAttachments()!=null)
								for (String attachment : eachMessage.getAttachments()) {
					%>
					<img alt="Image" src="<%=attachment%>">
					<%
						}
					%>
				</div>
				<%
					}
				%>
			</div>
		</div>
	</div>
</body>
<script type="text/javascript">
URL = "mail";
bindFormOnAJAX("#filter-form", onFilterComplete);
bindFormOnAJAX("#read-speed-form", onSpeedComplete);
bindFormOnAJAX("#mail-manager-form", onSpeedComplete);
$(function() {
	$( "#subjects" ).selectable({
		stop: function (){
			$("#selected-mails-input").attr("value","");
			var ids = new String();
			$(".ui-selectee").each(
					function(){
					$("#"+$(this).attr("id")+"-content").attr("style","display:none;");
					});
				
			 $(".ui-selected", this).each(function(){
				 $("#" + $(this).attr("id")+"-content").attr("style","display:inline;");
				 ids += $(this).attr("id") + ",";
			 });
				ids = ids.substring(0, ids.length-1);
				$("#selected-mails-input").attr("value",ids);
			}
	});
	});
</script>
</html>
<%!private MessageData itsMessageData;%>