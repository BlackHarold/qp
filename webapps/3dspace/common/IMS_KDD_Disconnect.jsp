<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%=JPO.invoke(
        Framework.getContext(session),
        request.getParameter("program"),
        new String[] {},
        request.getParameter("function"),
        new String[] {
            request.getParameter("fromId"),
            request.getParameter("toId"),
            request.getParameter("relationship")
        },
        String.class)%>