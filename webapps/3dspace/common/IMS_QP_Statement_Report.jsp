<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.BusinessObject" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.common.util.JSPUtil" %>
<%@ page import="org.apache.log4j.Logger" %>

<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>

<%
    BusinessObject boReportContainerObject = new BusinessObject("IMS_QP_Reports", "Reports", "-", context.getVault().getName());
    String id = boReportContainerObject.getObjectId(context);

    synchronized (this) {
        try {
            String query = String.format("temp query bus IMS_QP_ReportUnit * * where 'owner==%s' select attribute[IMS_QP_FileCheckinStatus] dump |", context.getUser());
            String checkUndoneReport = MqlUtil.mqlCommand(context, query);
            if (!checkUndoneReport.contains("Not ready yet")) {
                JPO.invoke(context, "IMS_QP_Statement_Report", new String[]{}, "getReport", null);
            }
            response.sendRedirect("../common/emxTree.jsp?objectId=" + id);
            return;

        } catch (Exception ex) {
            ex.printStackTrace();
            emxNavErrorObject.addMessage(ex.toString());
        }
    }
%>
