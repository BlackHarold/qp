<%@ page import="com.matrixone.apps.domain.util.XSSUtil" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="java.util.HashMap" %>
<%@ page import="java.util.Map" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@include file="../common/emxNavigatorInclude.inc" %>
<%@include file="../common/emxNavigatorTopErrorInclude.inc" %>
<%
    final Logger LOG = Logger.getLogger("reportLogger");

    String objectId = request.getParameter("objectId");
    Map args = new HashMap();
    args.put("objectId", objectId);

    ServletOutputStream stream = null;
    String message = "error message";
    synchronized (this) {

        try {
            Map map = JPO.invoke(context, "IMS_QP_ActualPlanSearchReport", new String[]{}, "checkout", JPO.packArgs(args), HashMap.class);

            if (map != null) {
                String fileName = (String) map.get("fileName");
                byte[] biteArray = (byte[]) map.get("byteArray");

                response.setContentType("application/xls");
                response.setContentLength(biteArray.length);
                response.setHeader("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

                stream = response.getOutputStream();
                stream.write(biteArray);
                stream.flush();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            emxNavErrorObject.addMessage(ex.toString());
        } finally {
            if (stream != null)
                stream.close();
        }
    }
%>

<!DOCTYPE>
<html>
<body>
<%@include file="emxNavigatorBottomErrorInclude.inc" %>
<script>
    alert("<%=XSSUtil.encodeForJavaScript(context, message)%>");
    getTopWindow().close();
</script>
</body>
</html>
