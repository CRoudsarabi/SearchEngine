<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@  page import=" java.util.ArrayList"%>
<%@  page import=" java.sql.*"%>
<%@  page import=" backend.*"%>
<%@  page import=" backend.QueryResults"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Query Result</title>
</head>
	<form name="Buttons" action="ChangeMode" method="get">
		<a href="LandingPage.html" id="documents">Simple Search</a>&nbsp;
		<a href="LandingPageMulti.html" id="multi"> Multi Search</a>&nbsp;
		<a href="addingads.html" id="ads">Add Ads</a>&nbsp;
		 <a href="config.html" id="config">Config</a>
	</form>
<body>
	<div class='wrapper'>
		<center>
			<img src="css/searchengines.jpg" class="user" style="width: 300px">
			<form action="imagesearch">
				<input type="text" name="query"><br> <input
					type="submit" value="Search Images">
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
				<th>Image ID</th>
				<th>Image Score</th>
				<th>Image Rank</th>
				<th>Url</th>
			</tr>
			<%
				// Iterating through subjectList
				ArrayList<QueryResults> qResults = (ArrayList<QueryResults>) request.getAttribute("images");

				for (QueryResults queryResult : qResults) {
			%>

			<tr>
				<td><%=queryResult.getDocId()%></td>
				<td><%=queryResult.getScore()%></td>
				<td><%=queryResult.getRank()%></td>
				<td><a href=<%=queryResult.getUrl()%>> <img alt="Qries"
						src=<%=queryResult.getUrl()%> width=150 " height="70">
				</a></td>
			</tr>
			<%}%>

		</table>
	</center>
</body>
</html>