<%@ page import="matrix.db.Context" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterNames" %>
<%@ page import="java.util.*" %>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>

<!DOCTYPE>
<html>
<title>Deletion process...</title>
<header></header>
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

    div {
    }
</style>

<%
    String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");

    HashMap args = new HashMap();
    args.put("emxTableRowId", tableIDs);

    try {
        Map map = JPO.invoke(context, "IMS_QP_ExpectedResult", new String[]{}, "deleteExpectedResults", JPO.packArgs(args), HashMap.class);
        map.remove("message");
        if (!map.isEmpty()) {
            out.print("<center>");
            out.print("<div><span class=\"header\">An error, check expected result relations and try again<br><br></span></div>");
            out.print("</center>");
            out.print("Details: <br>");
            for (Object o : map.entrySet()) {
                Map.Entry entry = (Map.Entry) o;
                out.print("<br>" + entry.getKey() + " has another parent: " + entry.getValue());
            }
            out.print("<center>");
            out.print("<p><a class=\"button\" onclick=\"window.close();\">It's my fault</a><p>");
            out.print("</center>");
            out.print("<script>window.opener.location.reload()</script>");
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
