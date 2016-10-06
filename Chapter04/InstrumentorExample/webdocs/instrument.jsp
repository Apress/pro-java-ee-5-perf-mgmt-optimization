<%@page import="java.io.*" %>
<html>
<head>
<title>Instrumentation Management Interface</title>
</head>
<body>

<h2>Instrumentation Management Interface</h2>

<table width="90%" align="center">
<tr><td><i>This interface allows you to control the embedded instrumentation engine</i></td></tr>
<tr><td>Options:

<table width="90%" align="center">
<tr><td><a href="instrument?cmd=start">Start Instrumentation</a></td></tr>
<tr><td><a href="instrument?cmd=stop">Stop Instrumentation</a></td></tr>
<tr><td><a href="instrument?cmd=report">Get Report</a></td></tr>
</table>

</td></tr>

</table>

<br>
<%String instrumentationStatus = ( String )request.getAttribute( "instrumentation-status" );%>
<h3>Instrumentation: <%=instrumentationStatus%>
<br>

<h3>Status</h3>
<pre>
<%String status = ( String )request.getAttribute( "status" );%>
<%=status%>
</pre>

</body>