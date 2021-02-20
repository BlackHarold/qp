<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.BusinessObject" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="org.apache.log4j.Logger" %>

<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>

<%
    Logger LOG = Logger.getLogger("blackLogger");
    BusinessObject boReportContainerObject = new BusinessObject("IMS_QP_Reports", "Reports", "-", context.getVault().getName());
    String id = boReportContainerObject.getObjectId(context);
    LOG.info("id: " + id);
    synchronized (this) {
        try {
            String query = String.format("temp query bus IMS_QP_ReportUnit * * where 'owner==%s' select attribute[IMS_QP_FileCheckinStatus] dump |", context.getUser());
            String checkUndoneReport = MqlUtil.mqlCommand(context, query);
            LOG.info("check undone rep: " + checkUndoneReport);
            if (checkUndoneReport.contains("Not ready yet")) {
                LOG.info("redirect: " + "../common/emxTree.jsp?objectId=" + id);
                response.sendRedirect("../common/emxTree.jsp?objectId=" + id);
            }

            String objectId = request.getParameter("objectId");
            String user = context.getUser();

            String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");

            HashMap args = new HashMap();
            args.put("currentUser", user);
            args.put("objectId", objectId);
            args.put("emxTableRowId", tableIDs);

            JPO.invoke(context, "IMS_QP_SQP_Report", new String[]{}, "getReport", JPO.packArgs(args), HashMap.class);
        } catch (Exception ex) {
            ex.printStackTrace();
            emxNavErrorObject.addMessage(ex.toString());
        } finally {
            LOG.info("finally redirect");
            response.sendRedirect("../common/emxTree.jsp?objectId=" + id);
        }
    }
%>

