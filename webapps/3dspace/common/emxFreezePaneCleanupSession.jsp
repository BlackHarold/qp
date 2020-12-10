<%--  emxFreezePaneCleanupSession.jsp
   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxFreezePaneCleanupSession.jsp.rca 1.6 Wed Oct 22 15:48:16 2008 przemek Experimental przemek $
--%>
<%@ page import="com.matrixone.jdom.*,
    com.matrixone.jdom.Document,
    com.matrixone.jdom.input.*,
    com.matrixone.jdom.output.*" %>

<%@include file = "emxNavigatorNoDocTypeInclude.inc"%>
<%@include file = "emxNavigatorTopErrorInclude.inc"%>

<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>

<%
    // get time stamp
    String timeStamp = com.matrixone.apps.domain.util.Request.getParameter(request, "fpTimeStamp");
	if(indentedTableBean.getTableData(timeStamp) != null){
	HashMap sbRequestMap = indentedTableBean.getRequestMap(timeStamp);
    if(UISearchUtil.isAutonomySearch(context,sbRequestMap) && UISearchUtil.canShowSnippets(context, sbRequestMap)){
    	HashMap hm = new HashMap();
    	hm.put("sbTimestamp", timeStamp);
    	String[] args = JPO.packArgs(hm);
        JPO.invoke(context, UISearchUtil.SNIPPETS_PROGRAM, null, UISearchUtil.SNIPPETS_REMOVE_RECORDS_METHOD, args, null);
		}
    }
    indentedTableBean.removeTableData(timeStamp);
%>

