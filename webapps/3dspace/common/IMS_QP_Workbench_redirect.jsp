<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="com.matrixone.apps.domain.DomainObject" %>
<%@ page import="matrix.db.Context" %>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%
    Logger LOG = Logger.getLogger("IMS_QP_DEP");
    Context ctx = Framework.getContext(session);
    String objectId = request.getParameter("objectId");
    DomainObject requestObject = DomainObject.getObject(ctx, objectId);
    String type = requestObject.getType(ctx);
    String name = requestObject.getName(ctx);
    String revision = requestObject.getRevision(ctx);
    LOG.info(objectId + " " + type + " " + name + " " + revision);


    //string values
    String table = "IMS_QP_Workbench_table";
    String toolbar = "";
    String disableSorting = "false";
    String program = "IMS_QP_Workbench";
    String method =
            type.equals("IMS_QP") && name.equals("DEP") ? "findAllDepsForBenchTable"
                    : type.equals("IMS_QP") && name.equals("SQP") ? "findAllQPsForBenchTable"
                    : type.equals("IMS_QP_DEP") ? "findDepTasksForBenchTableInit"
                    : type.equals("IMS_QP_QPlan") ? "findQPTasksForBenchTableInit"
                    : "";
    String sortColumnName = "sort_order";
    String sortDirection = "ascending";
    String groupColumnName = "";
    String selection = "";

    //boolean values
    String parallelLoading = "true";
    String showPageURLIcon = "true";
    String showClipboard = "true";

    //integer values
    String pageSize = "50";

    //get redirection url
    StringBuilder redirectUrl = new StringBuilder();
    redirectUrl.append("emxIndentedTable.jsp").append("?")
            .append("table=" + table).append("&")
            .append("toolbar=" + toolbar).append("&")
            .append("disableSorting=" + disableSorting).append("&")
            .append("sortColumnName=" + sortColumnName).append("&")
            .append("sortDirection=" + sortDirection).append("&")
            .append("program=" + program + ":" + method).append("&")
            .append("rowGroupingColumnNames=" + groupColumnName).append("&")
            .append("selection=" + selection).append("&")
            .append("parallelLoading=" + parallelLoading).append("&")
            .append("showPageURLIcon=" + showPageURLIcon).append("&")
            .append("showClipboard=" + showClipboard).append("&")
            .append("pageSize=" + pageSize).append("&")
            .append("objectId=" + objectId)
    ;

    //do redirect
    LOG.info("url: " + redirectUrl);
    response.sendRedirect(redirectUrl.toString());
%>
