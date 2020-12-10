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
<%@include file="../common/emxNavigatorInclude.inc" %>

<%
    // get the timeStamp from the incoming HttpRequest
    String timeStamp = (String) emxGetParameter(request, "timeStamp");

    // define the table bean
%>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<%
    HashMap tableDataMap;
    String strAlertMessage = null;
    String[] selectedRows = ComponentsUIUtil.getSplitTableRowIds(emxGetParameterValues(request, "emxTableRowId"));

    try {
        // get the tableDataMap and the requestMap from the table bean
        tableDataMap = indentedTableBean.getTableData(timeStamp);
        MapList objectList = indentedTableBean.getObjectList(tableDataMap);

        //TODO JPO.invoke no need to do a loop: one turn with args containing all IDs
        Map mapCurrObject;
        for (String id : selectedRows) {

            Iterator itrObjectList = objectList.iterator();
            while (itrObjectList.hasNext()) {
                mapCurrObject = (Map) itrObjectList.next();
                String rowId = (String) mapCurrObject.get(DomainConstants.SELECT_ID);

                if (id.equals(rowId)) {
                    try {
                        String[] args1 = JPO.packArgs(mapCurrObject);
                        JPO.invoke(context, "IMS_QP_TaskAssignment", null, "createAndConnectTask", args1);
                    } catch (Exception ex) {
                        System.out.println("Can't create task ");
                        strAlertMessage = ex.getMessage();
                    }
                    break;
                }
            }
        }
    } catch (Exception ex) {
        throw ex;
    }
%>
<script src="../common/scripts/emxUICore.js"></script>
<script language="javascript" type="text/javascript">
    console.log("start " + "<%=UIUtil.isNotNullAndNotEmpty(strAlertMessage)%>");
    if ("<%=UIUtil.isNotNullAndNotEmpty(strAlertMessage)%>" === "true") {
        alert("<%=XSSUtil.encodeForJavaScript(context, strAlertMessage)%>");
    } else {
        getTopWindow().opener.location.href = getTopWindow().opener.location.href;
        getTopWindow().close();
    }
</script>
