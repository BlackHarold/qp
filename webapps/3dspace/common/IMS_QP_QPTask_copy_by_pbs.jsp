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
<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>

<%
    Logger LOG = Logger.getLogger("IMS_QP_DEP");
    String objectId = request.getParameter("objectId");
    String[] parentIDs = new String[0], tableIDs = new String[0];
    try {
        parentIDs = emxGetParameterValues(request, "emxParentIds");
    } catch (Exception e) {
        LOG.error("error getting parent ids: " + e.getMessage());
    }

    try {
        tableIDs = emxGetParameterValues(request, "emxTableRowId");
    } catch (Exception e) {
        LOG.error("error getting row ids: " + e.getMessage());
    }


    String message = "";
    List<String> warnList;
    Map args = new HashMap();
    args.put("emxParentIds", parentIDs);
    args.put("emxTableRowId", tableIDs);
    args.put("objectId", objectId);
    LOG.info("emxParentIds: " + parentIDs);
    LOG.info("emxTableRowId: " + tableIDs);

    try {
        Map map = JPO.invoke(context, "IMS_QP", new String[]{}, "copyQPTask", JPO.packArgs(args), HashMap.class);

        LOG.info("map: " + map);
        message = (String) map.get("message");
        warnList = (List<String>) map.get("warning");

        if (warnList != null && warnList.size() > 0) {
            for (String s : warnList) {
                if (!message.contains(s)) {
                    message += "\n" + s;
                }

            }
            message += "\nalready has KKS/PBS";
        }
    } catch (Exception e) {
        LOG.error("error: " + e.getMessage());
    }
%>

<script>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");

    window.parent.parent.opener.location.reload(); //for copy by PBS
    window.parent.parent.close(); //for copy by PBS
</script>
