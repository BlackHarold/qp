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
    final Logger LOG = Logger.getLogger("reportLogger");


    String objectId = request.getParameter("objectId");
    String user = context.getUser();

    String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");
//    Map objectMap = UIUtil.parseRelAndObjectIds(context, tableIDs, false);

    HashMap args = new HashMap();
    args.put("currentUser", user);
    args.put("objectId", objectId);
    args.put("emxTableRowId", tableIDs);

    LOG.info("user: " + user);
    LOG.info("objectId: " + objectId);
    LOG.info("table ids: " + Arrays.asList(tableIDs));

    try {
        JPO.invoke(context, "IMS_QP_SQP_Report", new String[]{}, "getReport", JPO.packArgs(args), HashMap.class);
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
    window.close();
</script>
</body>
</html>
