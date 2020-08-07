<%--  emxNavigatorTopErrorInclude.jsp

   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxNavigatorTopErrorInclude.inc.rca 1.5 Wed Oct 22 15:48:39 2008 przemek Experimental przemek $
--%>
<%-- Instantiate the bean for Error Object to handle the error messages --%>
<jsp:useBean id="emxNavErrorObject" class="com.matrixone.apps.domain.util.FrameworkException" scope="request"/>
