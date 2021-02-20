<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.BusinessObject" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>

<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>

<%
    BusinessObject boReportContainerObject = new BusinessObject("IMS_QP_Reports", "Reports", "-", context.getVault().getName());
    String id = boReportContainerObject.getObjectId(context);

    synchronized (this) {
        try {
            String query = String.format("temp query bus IMS_QP_ReportUnit * * where 'owner==%s' select attribute[IMS_QP_FileChekinStatus] dump |", context.getUser());
            String checkUndoneReport = MqlUtil.mqlCommand(context, query);

            if (checkUndoneReport.contains("Not ready yet")) {
                response.sendRedirect("../common/emxTree.jsp?objectId=" + id);
            }

            JPO.invoke(context, "IMS_QP_QPTask_Report", new String[]{}, "getReport", null);
            response.sendRedirect("../common/emxTree.jsp?objectId=" + id);
        } catch (Exception ex) {
            ex.printStackTrace();
            emxNavErrorObject.addMessage(ex.toString());
        }
    }
%>
