<%@ page language="java" contentType="text/html; charset=ISO-8859-1"
	pageEncoding="ISO-8859-1"%>
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
			<li><a href="#security">Security</a></li>
			<li><a href="#mail-manager">Mail manager</a></li>
		</ul>
		<div id="filter-options">
			<form id="filter-form" action="" method="post">
				<div id="save-status"></div>
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
		<div id="security">
			<p>Security here!</p>
		</div>
		<div id="mail-manager">
			<p>Mail manager here!</p>
		</div>
	</div>

	<script type="text/javascript">
		URL = "mail";
		bindFormOnAJAX("#filter-form", onFilterComplete);
	</script>
</body>
</html>