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
<%@include file = "../common/emxNavigatorInclude.inc"%>

<%
    // get the timeStamp from the incoming HttpRequest
    String timeStamp = (String) emxGetParameter(request,"timeStamp");

    // define the table bean
%>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<%
    System.out.println("START IMS_QP_CreateQPTask.jsp");
    HashMap tableDataMap = null;
    String strAlertMessage   = null;
    String[] selectedRows = ComponentsUIUtil.getSplitTableRowIds(emxGetParameterValues(request, "emxTableRowId"));

    try {
        // get the tableDataMap and the requestMap from the table bean
        tableDataMap = (HashMap) indentedTableBean.getTableData(timeStamp);
        MapList objectList = (MapList) indentedTableBean.getObjectList(tableDataMap);

        Map mapCurrObject        = null;

        for (String id : selectedRows) {
            for (Iterator itrObjectList = objectList.iterator(); itrObjectList.hasNext();) {
                mapCurrObject = (Map)itrObjectList.next();
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
	System.out.println("END IMS_QP_CreateQPTask.jsp");
%>
<script src="../common/scripts/emxUICore.js"></script>
<script language="javascript" type="text/javascript" >
console.log("start " + "<%=UIUtil.isNotNullAndNotEmpty(strAlertMessage)%>");
if("<%=UIUtil.isNotNullAndNotEmpty(strAlertMessage)%>" == "true") {
    alert("<%=XSSUtil.encodeForJavaScript(context, strAlertMessage)%>");
} else {
    getTopWindow().opener.location.href=getTopWindow().opener.location.href;
    getTopWindow().close();
}
</script>
