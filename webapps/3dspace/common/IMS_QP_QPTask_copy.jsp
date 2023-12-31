﻿<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>

<%
    String objectId = request.getParameter("objectId");
    String[] parentIDs = new String[0], tableIDs = new String[0];
    try {
        parentIDs = emxGetParameterValues(request, "emxParentIds");
    } catch (Exception e) {
        LOG.error("error getting parent ids: " + e.getMessage());
    }

    try {
        tableIDs = emxGetParameterValues(request, "emxTableRowId");
    } catch (Exception e) {
        LOG.error("error getting row ids: " + e.getMessage());
    }


    String message = "";
    Map args = new HashMap();
    args.put("emxParentIds", parentIDs);
    args.put("emxTableRowId", tableIDs);
    args.put("objectId", objectId);

    try {
        Map map = JPO.invoke(context, "IMS_QP", new String[]{}, "copyQPTask", JPO.packArgs(args), HashMap.class);
        if (map != null) {
            if (!"".equals(map.get("message"))) {
                message = (String) map.get("message");
            }
        }

        response.setStatus(200);
    } catch (Exception ex) {
        ex.printStackTrace();
        emxNavErrorObject.addMessage(ex.toString());
    }
%>

<script>
    <%--    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");--%>
    try {
        window.parent.parent.opener.location.reload(); //for copy by PBS
        window.parent.parent.close(); //for copy by PBS

    } catch (e) {
        console.log(e);
    }
</script>
