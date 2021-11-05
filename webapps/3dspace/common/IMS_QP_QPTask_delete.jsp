<%@ page import="matrix.db.Context" %>
<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>

<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.*" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE>
<html>
<head>
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
    <script>
        function IMS_QP_CloseWindow() {
            window.opener.location.reload();
            window.close();
        }

        let ids;
        let operation;

        function IMS_QP_DeletingTasksCommand() {
            document.getElementsByClassName("loader")[0].style.display = "block";
            document.getElementsByClassName("button")[0].style.display = "none";
            document.getElementsByClassName("button")[1].style.display = "none";
            document.getElementById("table").hidden = true;

            let xhr = new XMLHttpRequest();
            // xhr.onreadystatechange = readyState;
            xhr.onloadend = IMS_QP_CloseWindow;
            let url = "IMS_QP_PureDelete.jsp";
            operation = 'delete';
            let params = 'emxTableRowId=' + encodeURIComponent(ids) + '&operation=' + encodeURIComponent(operation);
            console.log(url + '?' + params)
            xhr.open('GET', url + '?' + params, true);
            xhr.send();
        }

        function readyState() {
            window.opener.location.reload();
            window.close();
        }
    </script>
</head>
<body>

<%
    Logger LOG = Logger.getLogger("IMS_QP_DEP");
    Context context = Framework.getContext(session);
    String[] tableIDs = request.getParameterValues("emxTableRowId");
    Map objectMap = UIUtil.parseRelAndObjectIds(context, tableIDs, true);
    Map argsMap = new HashMap();
    argsMap.put("objectMap", objectMap);
    pageContext.setAttribute("emxTableRowId", objectMap);

    argsMap.put("operation", "info");

    String message = "message";
    List<String> badNames = new ArrayList<>();

    LOG.info("objectMap: " + objectMap);
    try {
        message = JPO.invoke(context, "IMS_QualityPlanBase", new String[]{}, "getTimeInfoAboutDeleteTasks", JPO.packArgs(argsMap), String.class);
        Map map = JPO.invoke(context, "IMS_QualityPlanBase", new String[]{}, "deleteQPTasks", JPO.packArgs(argsMap), HashMap.class);
        if (map.containsKey("array")) {
            badNames = (List<String>) map.get("array");
        }
        pageContext.setAttribute("badNames", badNames);
    } catch (Exception ex) {
        LOG.error("error: " + ex.getMessage());
        ex.printStackTrace();
    }

%>
<h3>
    <%=message%>
</h3>
<p class="loader"></p>
<p>
    <a id="action" class="button" onclick="IMS_QP_DeletingTasksCommand();">Do it!</a>
</p>
<script>
    <c:if test="${badNames.size()==0}">
    document.getElementById('action').hidden = false;
    </c:if>
    <c:if test="${badNames.size()>0}">
    document.getElementById('action').hidden = true;
    </c:if>

    ids = "${pageScope.emxTableRowId}";
</script>
<p>
    <a class="button" onclick="IMS_QP_CloseWindow()">Cancel</a>
</p>
<p>
<div class=\"loader\">
    <div class=\"loading\"></div>
</div>
</p>
<table id="table">
    <c:if test="${badNames.size()>0}">
        <c:if test="${badNames.size()>1}">
            <th>
                <h3>Tasks have another plans:</h3>
            </th>
        </c:if>
        <c:if test="${badNames.size()<2}">
            <th>
                <h3>Task has another plan:</h3>
            </th>
        </c:if>
        <c:forEach var="el" items="${badNames}">
            <tr>
                <td>
                    <c:out value="${el}"/>
                </td>
            </tr>
        </c:forEach>
    </c:if>
</table>
</p>
<p id="print">debug info: bad names size is ${badNames.size()}</p>
</body>
</html>
