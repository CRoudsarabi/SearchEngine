<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@  page import=" java.util.ArrayList"%>
<%@  page import=" java.sql.*"%>
<%@  page import=" backend.*"%>
<%@  page import=" backend.MultiSearchResultSet"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Query Result</title>
</head>
	<form name="Buttons" action="ChangeMode" method="get">
		<a href="LandingPage.html" id="documents">Simple Search</a>&nbsp;
		<a href="LandingPageImages.html" id="images"> Image Search</a>&nbsp;
		<a href="addingads.html" id="ads">Add Ads</a>&nbsp;
		 <a href="config.html" id="config">Config</a>
	</form>
<body>
	<div class='wrapper'>
		<center>
			<img src="css/searchengines.jpg" class="user" style="width: 300px">
			<form action="multisearch">
				<input type="text" name="query"><br> <input
					type="submit" value="Multi Search">
			</form>
		</center>
	</div>
	<center>
		<%
			String error = (String) request.getAttribute("error");
			if (error != null)
				out.println(error);
		%>

		<table border="1">
			<tr>
				<th>score</th>
				<th>Url</th>
				<th>VM</th>
			</tr>
			<%
				// Iterating through subjectList
				ArrayList<MultiSearchResultSet> qResults = (ArrayList<MultiSearchResultSet>) request.getAttribute("results");

				for (MultiSearchResultSet queryResult : qResults) {
			%>

			<tr>
				<td><%=queryResult.score%></td>

				<td><a href=<%=queryResult.url%>><%=queryResult.url%>
				</a></td>
				<td><%=queryResult.vm%></td>
			</tr>
			<%}%>

		</table>
	</center>
</body>
</html>