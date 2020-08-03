<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@include file="emxNavigatorInclude.inc" %>
<%@include file="emxNavigatorTopErrorInclude.inc" %>

    <%
    Logger LOG = Logger.getLogger("blackLogger");

    String objectId = request.getParameter("objectId");
    String parentId = request.getParameter("parentOID");
    String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");
    if (tableIDs==null||tableIDs.length==0) {
            String message = "No one row is selected";
    %>
<script>
    //so this jsp targeted to hidden frame alertify message called for top frame
    top.alert("<%=message%>");
    top.location.reload();
</script>
<%
        return;
    }

    Map objectMap = UIUtil.parseRelAndObjectIds(context, tableIDs, false);
    tableIDs = (String[]) objectMap.get("objectIds");
    String message = "";
    HashMap args = new HashMap();

    LOG.info("table: " + tableIDs.length + " " + Arrays.deepToString(tableIDs));
    try {
        if (tableIDs == null || tableIDs.length == 0) {
            throw new Exception("throw e");
        }

        args.put("objectId", objectId);
        args.put("parentOID", parentId);
        args.put("emxTableRowId", tableIDs);

        Map map = JPO.invoke(context, "IMS_QP_AllTask", new String[]{}, "distributeTaskConnection", JPO.packArgs(args), HashMap.class);
        if (map != null && !"".equals(map.get("message"))) {
            message = (String) map.get("message");
            LOG.info("message: " + message);
        }
%>
<%@include file="emxNavigatorBottomErrorInclude.inc" %>
<script language="javascript">
    window.top.opener.location.reload();
    top.close();
</script>
    <%
    } catch (Exception ex) {
        LOG.error("exception throwed");
        ex.printStackTrace();
        emxNavErrorObject.addMessage(ex.toString());
    }
%>
<body>
<html>
<%@include file="../emxUICommonEndOfPageInclude.inc" %>
