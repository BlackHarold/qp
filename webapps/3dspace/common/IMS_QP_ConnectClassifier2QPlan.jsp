<%@page import="matrix.db.*,
                matrix.util.*,
                com.matrixone.util.*,
                com.matrixone.servlet.*,
                com.matrixone.apps.common.util.ComponentsUIUtil,
                com.matrixone.apps.framework.ui.*,
                com.matrixone.apps.domain.util.*,
                com.matrixone.apps.domain.*,
                matrix.db.*,
                matrix.util.*,
                com.matrixone.servlet.*,
                com.matrixone.apps.domain.util.*,
                java.util.*,
                java.io.*"
%>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.List" %>
<%@include file="../common/emxNavigatorInclude.inc" %>

<%
    // get the timeStamp from the incoming HttpRequest
    String timeStamp = (String) emxGetParameter(request, "timeStamp");
%>
<%--define the table bean--%>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<%
    String alertMessage = null;
    String[] selectedRows = ComponentsUIUtil.getSplitTableRowIds(emxGetParameterValues(request, "emxTableRowId"));

    // get the tableDataMap and the requestMap from the table bean
    HashMap tableDataMap = indentedTableBean.getTableData(timeStamp);

    List<String> listIds = Arrays.asList(selectedRows);
    String objectId = request.getParameter("objectId");

    HashMap argsMap = new HashMap();
    argsMap.put("objectList", listIds);
    argsMap.put("objectId", objectId);
    try {
        String[] args = JPO.packArgs(argsMap);
        String message = JPO.invoke(context, "IMS_QP_Classifier", new String[]{}, "connectPlans", JPO.packArgs(argsMap), String.class);

        if (!message.contains("200")) {
            alertMessage = message;
        }
    } catch (Exception ex) {
        alertMessage = ex.getMessage();
    }
%>
<script src="../common/scripts/emxUICore.js"></script>
<script language="javascript" type="text/javascript">
    if ("<%=UIUtil.isNotNullAndNotEmpty(alertMessage)%>" === "true") {
        alert("<%=XSSUtil.encodeForJavaScript(context, alertMessage)%>");
    } else {
        getTopWindow().opener.location.href = getTopWindow().opener.location.href;
        getTopWindow().close();
    }
</script>
