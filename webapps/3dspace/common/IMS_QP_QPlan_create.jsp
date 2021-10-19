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
    LOG.info("create from: " + objectId + "|" + type + "|" + name + "|" + revision);

    //string values
    String url = "IMS_QP_" + name + "_create.jsp";
    String form = "IMS_QP_" + name + "_create";
    String relationship = "relationship_IMS_QP_QP2QPlan";

    type = "type_IMS_QP_QPlan";
    String policy = "policy_IMS_QP_QPlan";

    //post process program:method
    String program = "IMS_QualityPlanBase";
    String method = "createQPlanPostProcess";

    //boolean values
    String typeChooser = "false";

    //other parameters
    String submitAction = "refreshCaller";
    String nameField = "autoName";

    //get redirection url
    StringBuilder redirectUrl = new StringBuilder();
    redirectUrl.append(url).append("?")
            .append("type=" + type).append("&")
            .append("form=" + form).append("&")
            .append("relationship=" + relationship).append("&")
            .append("policy=" + policy).append("&")
            .append("postProcessJPO=" + program + ":" + method).append("&")
            .append("typeChooser=" + typeChooser).append("&")
            .append("submitAction=" + submitAction).append("&")
            .append("nameField=" + nameField).append("&")
            .append("objectId=" + objectId)
    ;

    //do redirect
    LOG.info("url: " + redirectUrl);
    response.sendRedirect(redirectUrl.toString());
%>
