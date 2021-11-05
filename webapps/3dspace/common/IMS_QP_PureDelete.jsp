<%@ page import="matrix.db.Context" %>
<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.List" %>

<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="java.util.*" %>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE>
<html>
<head></head>
<body>
hi
<%
    Logger LOG = Logger.getLogger("IMS_QP_DEP");
    Context context = Framework.getContext(session);
    LOG.info("objectMap: " + request.getParameter("emxTableRowId"));
    LOG.info("operation: " + request.getParameter("operation"));
    Map argsMap = new HashMap();
    argsMap.put("objectMap", request.getParameter("emxTableRowId"));
    argsMap.put("operation", request.getParameter("operation"));

    try {
        LOG.info("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa argsMap: " + argsMap + " context: " + context.getUser());
//        JPO.invoke(context,)
        Object o = JPO.invoke(context, "IMS_QualityPlanBase", new String[]{}, "deleteQPTasks", JPO.packArgs(argsMap), Object.class);
//        List<String> badNames = (List<String>) map.get("array");
        LOG.info("finally: " + o.getClass().getSimpleName());
    } catch (Exception ex) {
        LOG.error("error: " + ex.getMessage());
        ex.printStackTrace();
    }

//    }
//    String[] tableIDs = request.getParameterValues("emxTableRowId");
//    LOG.info("tableIDs: " + tableIDs);
//
//    String tableIDs = request.getParameter("emxTableRowId");
//
//    HashMap args = new HashMap();
//    args.put("emxTableRowId", tableIDs);

%>
</body>
</html>
