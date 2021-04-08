<%@ page import="static com.matrixone.apps.common.util.JSPUtil.emxGetParameterValues" %>
<%@ page import="matrix.db.BusinessObject" %>
<%@ page import="com.matrixone.apps.domain.util.MqlUtil" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="matrix.db.JPO" %>

<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.Map" %>

<%

    Logger LOG = Logger.getLogger("rihLogger");
    try {

        String objectId = request.getParameter("objectId");
        String[] tableIDs = emxGetParameterValues(request, "emxTableRowId");
        HashMap args = new HashMap();
        args.put("objectId", objectId);
        args.put("emxTableRowId", tableIDs);
		args.put("type", "SQP");

        Map map = (Map) JPO.invoke(context, "IMS_QP_Ordered", new String[]{}, "sort", JPO.packArgs(args), HashMap.class);


    } catch (Exception ex) {
        LOG.error(ex.getMessage());
        ex.printStackTrace();
        emxNavErrorObject.addMessage(ex.toString());
    }
%>
