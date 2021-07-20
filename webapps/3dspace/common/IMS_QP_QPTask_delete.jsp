<%@ page import="java.util.Map" %>
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
        color: #005584;
    }

    p {
        margin-top: 60px;
        font-size: 16px;
    }

    .loader {
        display: none;
        top: 50%;
        left: 50%;
        position: absolute;
        transform: translate(-50%, -50%);
    }

    .loading {
        border: 3px solid #ccc;
        width: 80px;
        height: 80px;
        border-radius: 50%;
        border-top-color: #005584;
        border-left-color: #005584;
        animation: spin 1s infinite ease-in;
    }

    @keyframes spin {
        0% {
            transform: rotate(0deg);
        }

        100% {
            transform: rotate(360deg);
        }
    }
</style>

<%
    String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");
    Map objectMap = UIUtil.parseRelAndObjectIds(context, tableIDs, false);
    tableIDs = (String[]) objectMap.get("objectIds");

    Map args = new HashMap();
    args.put("emxTableRowId", tableIDs);

    String cleanedIds = "";
    for (int i = 0; i < tableIDs.length; i++) {
        cleanedIds += tableIDs[i].substring(0, tableIDs[i].indexOf("|"));
        cleanedIds += "|";
    }

    int cleanedIdsArray = cleanedIds.split("\\|").length;

    String url = "\"IMS_QP_PureDelete.jsp?emxTableRowId=" + cleanedIds + "\"";

    try {
        String message = JPO.invoke(context, "IMS_QualityPlanBase", new String[]{}, "getTimeInfoAboutDeleteTasks", JPO.packArgs(args), String.class);
        out.print("<script>\n" +
                "function IMS_QP_CloseWindow() {\n" +
                "    window.opener.location.reload();\n" +
                "    window.close();" +
                "}\n" +
                "function IMS_QP_DeletingTasksCommand(ids) {\n" +
                "   document.getElementsByClassName(\"loader\")[0].style.display = \"block\";\n" +
                "   document.getElementsByClassName(\"button\")[0].style.display = \"none\";\n" +
                "   document.getElementsByClassName(\"button\")[1].style.display = \"none\";\n" +
                "    let xhr = new XMLHttpRequest();\n" +
                "    xhr.onloadend = function() {\n" +
                "    window.opener.location.reload();\n" +
                "    window.close();\n" +
                " };\n" +
                "    let url = " + url + ";\n" +
                "    xhr.open('GET', " + url + ");\n" +
                "    xhr.send();\n" +
                "}\n" +
                "</script>");
        out.print("<center><b>" + message+"</b>");
        out.print("<p>" + cleanedIdsArray + " task" + (cleanedIdsArray > 1 ? "s" : "") + " selected</p>");
        out.print("<p><a class=\"button\" onclick=\"IMS_QP_DeletingTasksCommand('" + cleanedIds + "');\">Do it!</a></p>");
        out.print("<p><a class=\"button\" onclick=\"window.close()\">Cancel</a></p>");
        out.print("<p><div class=\"loader\"><div class=\"loading\"></div><div></p>");
        out.print("</center>");
    } catch (Exception ex) {
        ex.printStackTrace();
        emxNavErrorObject.addMessage(ex.toString());
    }

%>
<%@include file="../common/emxNavigatorBottomErrorInclude.inc" %>
<%@include file="../emxUICommonEndOfPageInclude.inc" %>
</body>
</html>
