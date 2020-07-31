<%--  emxUICommonEndOfPageInclude.jsp   -
   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxUICommonEndOfPageInclude.inc.rca 1.21 Wed Oct 22 16:10:01 2008 przemek Experimental przemek $
--%>
<script language="javascript">
	var boolcheck = false;
<%
 	String usepg = emxGetParameter(request,"usepg");

 	if(usepg != null && usepg.equalsIgnoreCase("true")) {
%>
  		boolcheck = true;
<%
 	}
%>
 	if(boolcheck && typeof doCheckbox == "function"){
 		doCheckbox();
 	}
</script>

</body>
</html>



