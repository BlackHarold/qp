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

    String query = String.format("temp query bus IMS_QP_ReportUnit * * where 'owner==%s' select attribute[IMS_QP_FileCheckinStatus] dump |", context.getUser());
    String checkUndoneReport = MqlUtil.mqlCommand(context, query);

    try {
        if (!checkUndoneReport.contains("Not ready yet")) {
            synchronized (this) {
                String objectId = request.getParameter("objectId");
                String user = context.getUser();

                String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");

                HashMap args = new HashMap();
                args.put("currentUser", user);
                args.put("objectId", objectId);
                args.put("emxTableRowId", tableIDs);

                JPO.invoke(context, "IMS_QP_DQP_Report", new String[]{}, "getMultiPageReport", JPO.packArgs(args), HashMap.class);
            }
        }

        response.sendRedirect("../common/emxTree.jsp?objectId=" + id);
        return;

    } catch (Exception ex) {
        ex.printStackTrace();
        emxNavErrorObject.addMessage(ex.toString());
    }
%>

