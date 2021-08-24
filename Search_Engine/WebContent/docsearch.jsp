<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@  page import=" java.util.ArrayList"%>
<%@  page import=" java.sql.*"%>
<%@  page import=" backend.*"%>
<%@  page import=" backend.QueryResults"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Query Result</title>
    <script src="http://code.jquery.com/jquery-1.5.js"></script>
   <script>
    $(document).ready(function() {
   $("a").click(function() {
   $.post("db_search", { "urld" : window.location.href},
   function(data) {
   });
   });
 });
    </script>
</head>
<body>
	<form name="Buttons" action="ChangeMode" method="get">
		<a href="LandingPageImages.html" id="images"> Image Search</a>&nbsp;
		<a href="LandingPageMulti.html" id="multi"> Multi Search</a>&nbsp;
		<a href="addingads.html" id="ads">Add Ads</a>&nbsp;
		 <a href="config.html" id="config">Config</a>
	</form>
<div class='wrapper'>
<center> <img src="css/searchengines.jpg" class="user"  style="width:300px"> 
	 <form action="docsearch" >
		<input type="text" name ="query"><br>
		<input type="submit" value= "Search Websites">
	</form>

	</center>
</div>
<center>
<%
String error = (String) request.getAttribute("error");
if(error != null) out.println(error);%>

<table border="1">
<tr>
<th>Document ID</th>
<th>Document Score</th>
<th>Document Rank</th>
<th>Url</th>
<th>Snippet Score</th>
</tr>
<%

// Iterating through subjectList
 	ArrayList<QueryResults> qresultss = (ArrayList<QueryResults>) request.getAttribute("documents");

	for(QueryResults SS : qresultss) {%>

<tr>	
<td><%=SS.getDocId()%></td>
<td><%=SS.getScore()%></td>
<td><%=SS.getRank()%></td>
<td> 
 <% if(SS.getAds()){ %>
<i><b>ads</b></i>
 <%} %>
  <% String currentUrl = ""+ request.getRequestURL(); %>
    <%  currentUrl = currentUrl.replaceAll("/docsearch.jsp",""); %>
  <% String url = (currentUrl+"/red?url="+SS.getUrl()); %>
<a href=<%=url%>> <%=SS.getUrl()%>
</a>
<br>
 <%=SS.getSnippet()%>
</br>
 <% if(SS.getAds()){ %>
<br>

<a href= <%=SS.getImagelink()%>> <img alt="Qries"
src= <%=SS.getImagelink()%> width=150 " height="70">
</a>

</br>
<%} %>
</td>
<td> <%=SS.getSnippetScore()%></td>
</tr>
<%}%>

</table>
</center>
</body>
</html>