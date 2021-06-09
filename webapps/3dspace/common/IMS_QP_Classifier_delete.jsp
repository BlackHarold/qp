<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.ContextUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.List" %>
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
    String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");
    Map objectMap = UIUtil.parseRelAndObjectIds(context, tableIDs, false);
    tableIDs = (String[]) objectMap.get("objectIds");

    HashMap args = new HashMap();
    args.put("emxTableRowId", tableIDs);

    try {
        Map map = JPO.invoke(context, "IMS_QP_Classifier", new String[]{}, "deleteClassifiers", JPO.packArgs(args), HashMap.class);
        map.remove("message");
        if (!map.isEmpty()) {
            out.print("<center>");
            List<String> list = (List) map.get("array");
            out.print("<span class=\"header\">An error, check states and try again<br><br></span></header>");
            out.print("Details: <br>");
            for (String name : list) {
                out.print("<br>" + name + " has an \'Approved\' task");
            }
            out.print("<p><a class=\"button\" onclick=\"window.close();\">It's my fault</a><p>");
            out.print("</center>");
        } else {
%>
<script language="javascript">
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
