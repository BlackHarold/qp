<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page import="matrix.db.JPO" %>
<%@ page import="com.matrixone.apps.domain.util.EnoviaResourceBundle" %>
<%@ page import="java.util.Locale" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="matrix.db.Context" %>
<%@ page import="org.apache.commons.lang3.StringEscapeUtils" %>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%
    Context context = Framework.getContext(session);

    String[] parameterValues = request.getParameterValues("args[]");
    if (parameterValues == null) {
        parameterValues = new String[0];
    }
    String[] args = new String[parameterValues.length + 1];
    for (int i = 0; i < parameterValues.length; i++) {
        args[i] = parameterValues[i];
    }
    args[parameterValues.length] = request.getParameter("objectId");

    String confirmationText = null;
    String skipConfirmation = request.getParameter("skipConfirmation");
    if (!"true".equalsIgnoreCase(skipConfirmation)) {
        confirmationText = request.getParameter("confirmationText");
        if (!StringUtils.isBlank(confirmationText)) {
            String localizedConfirmationText = EnoviaResourceBundle.getFrameworkStringResourceProperty(context, confirmationText, new Locale(context.getSession().getLanguage()));
            if (!StringUtils.isBlank(localizedConfirmationText)) {
                confirmationText = localizedConfirmationText;
            }
        }
    }

    if (!StringUtils.isBlank(confirmationText)) {
        out.print(String.format(
                "<script>if (confirm('%s')) { window.location.replace(window.location.href + '&skipConfirmation=true'); } else { window.opener = self; window.close(); }</script>",
                StringEscapeUtils.escapeEcmaScript(confirmationText)));
    }
    else {
        out.clear();
        out.print(JPO.invoke(
                Framework.getContext(session),
                request.getParameter("program"),
                new String[]{},
                request.getParameter("function"),
                args,
                String.class));
    }
%>
