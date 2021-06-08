<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.BusinessObject" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.Map" %>

<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>

<%
    final Logger LOG = Logger.getLogger("reportLogger");
    String user = context.getUser();
    BusinessObject boReportContainerObject = new BusinessObject("IMS_QP_Reports", "Reports", "-", context.getVault().getName());
    String id = boReportContainerObject.getObjectId(context);

    String query = String.format("temp query bus IMS_QP_ReportUnit * * where 'owner==%s' select attribute[IMS_QP_FileCheckinStatus] dump |", user);
    String checkUndoneReport = MqlUtil.mqlCommand(context, query);
    try {
        if (!checkUndoneReport.contains("Not ready yet")) {
                String objectId = request.getParameter("objectId");

                Map args = new HashMap();
                args.put("currentUser", user);
                args.put("objectId", objectId);

                JPO.invoke(context, "IMS_QP_DQP_Report", new String[]{}, "getReport", JPO.packArgs(args), HashMap.class);
        }

        response.sendRedirect("../common/emxTree.jsp?objectId=" + id);
        return;

    } catch (Exception ex) {
        LOG.error("error: " + ex.getMessage());
        ex.printStackTrace();
        emxNavErrorObject.addMessage(ex.toString());
    }
%>

