<%@include file = "emxNavigatorTopErrorInclude.inc"%>
<%@include file = "emxNavigatorInclude.inc"%>
<%@page trimDirectiveWhitespaces="true" %>
<%@ page import="java.util.*" %>

<%
String sMessage="Relationships were created.";
try
{
	
	String qpPlanId = request.getParameter("objectId");
    String[] emxTableRowIds = emxGetParameterValues(request, "emxTableRowId");
    Map objectMap = UIUtil.parseRelAndObjectIds(context, emxTableRowIds, false);
    emxTableRowIds = (String[]) objectMap.get("objectIds");

    HashMap args = new HashMap();
    args.put("emxTableRowId", emxTableRowIds);
    args.put("objectId", qpPlanId);
	
	Map map = (Map)JPO.invoke(context, "IMS_QP_Task", new String[]{}, "generateRelIN_OUT", JPO.packArgs(args), HashMap.class);
	if(map.containsKey("message"))
	{
		sMessage = (String) map.get("message");
	}	
}
catch(Exception ex)
{
	ex.printStackTrace();
	emxNavErrorObject.addMessage(ex.toString());
}
%>
<script language="javascript" type="text/javascript" >
   	  alert("<%=XSSUtil.encodeForJavaScript(context, sMessage)%>");

	    window.opener.location.reload(true);
    window.close();
</script>

