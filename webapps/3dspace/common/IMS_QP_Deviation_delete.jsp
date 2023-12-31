﻿<%@ page import="matrix.db.Context" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.*" %>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>

<html>
<body>
<style>

    a.button {
        font-weight: 700;
        color: white;
        text-decoration: none;
        padding: .8em 5em calc(.8em + 3px);
        border-radius: 30px;
        background: rgb(73, 152, 199);
        box-shadow: 0 -3px rgb(81, 134, 199) inset;
        transition: 0.2s;
    }

    a.button:hover {
        background: rgb(75, 123, 167);
    }

    a.button:active {
        background: rgb(75, 123, 167);
        box-shadow: 0 3px rgb(49, 73, 129) inset;
    }

    .header {
        min-width: 640px;
        height: 44px;
        color: #d54c98;
        font-weight: bold;
        font-size: 24px;
        border: none;
    }

    body {
        margin-top: 60px;
        font-family: Arial;
        font-size: 16px;
        color: #243b77;
    }

    p {
        margin-top: 60px;
        font-size: 16px;
    }
</style>

<%
    Map args = new HashMap();
    args.put("request", request);

    try {
        Map map = JPO.invoke(context, "IMS_QP_ER_Deviation", new String[]{}, "delete", JPO.packArgs(args), HashMap.class);
        if (map != null && map.containsKey("message")) {
            out.print(map.get("message"));
        } else {
%>
<script>
    window.opener.location.reload();
    window.close();
</script>
<%
        }
    } catch (Exception ex) {
        ex.printStackTrace();
        emxNavErrorObject.addMessage(ex.toString());
    }

%>
<%@include file="../common/emxNavigatorBottomErrorInclude.inc" %>
<%@include file="../emxUICommonEndOfPageInclude.inc" %>
</body>
</html>
