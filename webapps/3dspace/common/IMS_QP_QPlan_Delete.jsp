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

<%
	String objectId = request.getParameter("objectId");
    String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");
    Map objectMap = UIUtil.parseRelAndObjectIds(context, tableIDs, false);
    tableIDs = (String[]) objectMap.get("objectIds");
	String message = "";
    HashMap args = new HashMap();
    args.put("emxTableRowId", tableIDs);
	args.put("objectId", objectId);

    try {
        Map map = JPO.invoke(context, "IMS_QP", new String[]{}, "deleteQPlan", JPO.packArgs(args), HashMap.class);
            if (map != null) {
                if (!"".equals(map.get("message"))) {
                   message = (String) map.get("message");
                }
            }
    } catch (Exception ex) {
        ex.printStackTrace();
        emxNavErrorObject.addMessage(ex.toString());
    }
%>

<!DOCTYPE>
<html>
<body>
<%@include file="emxNavigatorBottomErrorInclude.inc" %>
<%@include file="../emxUICommonEndOfPageInclude.inc" %>
<script>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
	window.opener.location.reload();
	window.close();
</script>
</body>
</html>
