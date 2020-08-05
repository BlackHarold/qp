<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.commons.lang.StringUtils" %>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%=JPO.invoke(
        Framework.getContext(session),
        request.getParameter("program"),
        new String[] {},
        request.getParameter("function"),
        new String[] {
                request.getParameter("fromId"),
                StringUtils.isNotBlank(request.getParameter("toId")) ?
                        request.getParameter("toId") :
                        StringUtils.join(request.getParameterValues("emxTableRowId"), ";"),
                request.getParameter("relationship"),
                request.getParameter("connectId"),
                request.getParameter("rowIdToRefresh"),
                request.getParameter("dragSource")
        },
        String.class)%>