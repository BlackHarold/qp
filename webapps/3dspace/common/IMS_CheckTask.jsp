<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>

<%=JPO.invoke(Framework.getContext(session), request.getParameter("program"), new String[]{}, request.getParameter("function"),
        new String[]{request.getParameter("fromId"), request.getParameter("toId"), request.getParameter("relationship")},
        String.class)
%>
