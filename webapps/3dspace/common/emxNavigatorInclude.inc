<%--  emxNavigatorInclude.inc

   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxNavigatorInclude.inc.rca 1.22 Tue Oct 28 18:55:05 2008 przemek Experimental przemek $
--%>

<%@ page import = "matrix.db.*, matrix.util.*,
				   com.matrixone.util.*,
				   com.matrixone.servlet.*,
				   com.matrixone.apps.framework.ui.*,
				   com.matrixone.apps.domain.util.*, 
				   com.matrixone.apps.domain.*, 
				   java.util.*, 
				   java.io.*, 
				   com.matrixone.jsystem.util.*"
				   errorPage="../common/emxNavigatorErrorPage.jsp"%>
				   
<%@include file = "emxNavigatorComponentSideDoorInclude.inc"%>
<!DOCTYPE html>
<%@include file = "../emxContentTypeInclude.inc"%>
<%@include file = "emxNavigatorBaseInclude.inc"%>


<!-- ESAPI -->
<script type="text/javascript" language="JavaScript" src="../plugins/esapi4js/esapi-min.js"></script>
<script type="text/javascript" language="JavaScript" src="../plugins/esapi4js/resources/i18n/ESAPI_Standard_en_US.properties.js"></script>
<script type="text/javascript" language="JavaScript" src="../plugins/esapi4js/resources/Base.esapi.properties.js"></script>
<script type="text/javascript" language="JavaScript">
	org.owasp.esapi.ESAPI.initialize();
</script>

<script src="scripts/IMS_KDD.js?version=2020-04-27"></script>
<script src="scripts/IMS_DragNDrop.js?version=2020-04-27"></script>