<%-- emxNavigatorBottomErrorInclude.inc -- Bottom error ipage in emxNavigator

   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,
   Inc.  Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = "$Id: emxNavigatorBottomErrorInclude.inc.rca 1.9 Wed Oct 22 15:48:33 2008 przemek Experimental przemek $";
--%>

<%@include file = "../emxMQLNotice.inc"%>
<%   if(request.getAttribute("useScriptTags") == null){ %>
<script language="JavaScript">
<% } %>
    var DisplayErrorMsg = "";
<% 
    // Get the session error messages and display them
    if (session.getAttribute("error.message") != null)
    {
        String emxSessionErrorMsg = (String)session.getAttribute("error.message");
        session.removeAttribute("error.message");

        if ( (emxSessionErrorMsg != null) && (emxSessionErrorMsg.trim().length() > 0 ) )
        {
	        //Modified for Bug 372874
	        emxSessionErrorMsg = emxSessionErrorMsg.replace("\"", "\\\"");

            // Parse the string for "\n" so that the string can be displayed properly in Javascript
            StringTokenizer token = new StringTokenizer(emxSessionErrorMsg, "\n");
            while (token.hasMoreTokens())
            {
                String strMsg =token.nextToken().trim();
%>
                DisplayErrorMsg += "<%=XSSUtil.encodeForJavaScript(context , strMsg)%>" + "\n";
<%
            }
%>
            alert(DisplayErrorMsg);
            var isFTS = getTopWindow().location.href.indexOf("common/emxFullSearch.jsp?") != -1;
            if(isFTS) {
            	findFrame(getTopWindow(),"structure_browser").setSubmitURLRequestCompleted();
            }
<%
        }
    }

    // get the error object within this request scope
    // Object "emxNavErrorObject" and the display the message.
    if (emxNavErrorObject != null)
    {
        String emxNavigatorErrorMsg = emxNavErrorObject.toString();

        if ( (emxNavigatorErrorMsg != null) && (emxNavigatorErrorMsg.trim().length() > 0 ) )
        {
	   		//Modified for Bug 372874
	  		emxNavigatorErrorMsg = emxNavigatorErrorMsg.replace("\"", "\\\"");

            // Parse the string for "\n" so that the string can be displayed properly in Javascript
            StringTokenizer token = new StringTokenizer(emxNavigatorErrorMsg, "\n");
            while (token.hasMoreTokens())
            {
                String strMsg =token.nextToken().trim();
%>
                DisplayErrorMsg += "<%=XSSUtil.encodeForJavaScript(context , strMsg)%>" + "\n";
<%
            }
%>
            alert(DisplayErrorMsg);
			if(turnOffProgress != 'undefined') {
				turnOffProgress();
			}
<%
        }
    }
      if(request.getAttribute("useScriptTags") == null){
%>
</script>
<% } %>
