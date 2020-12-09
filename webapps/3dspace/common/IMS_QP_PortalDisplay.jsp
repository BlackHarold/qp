<%--  emxPortal.jsp
   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxPortalDisplay.jsp.rca 1.16.2.2 Fri Nov 14 07:51:58 2008 ds-arajendiran Experimental $
--%>
<%@include file="emxNavigatorInclude.inc" %>
<%@include file="emxNavigatorTopErrorInclude.inc" %>
<%@include file="emxNavigatorTimerTop.inc" %>

<html>

<%
    String maximumTabLength = EnoviaResourceBundle.getProperty(context, "emxFramework.PowerView.Channel.Label.MaximumLength");
    int tabLength = 17;
    if (maximumTabLength != null && maximumTabLength.trim().length() > 0) {
        try {
            tabLength = Integer.parseInt(maximumTabLength);
        } catch (NumberFormatException e) {
            tabLength = 17;
        }
    }

    String emxSuiteDirectory = emxGetParameter(request, "emxSuiteDirectory");
    String jsTreeID = emxGetParameter(request, "jsTreeID");
    String objectId = emxGetParameter(request, "objectId");
    String portal = emxGetParameter(request, "portal");
    String user = emxGetParameter(request, "user");
    String portalType = emxGetParameter(request, "portalType");
    String strNoMetricsDashboardMsg = "";

    if ((portalType != null) && portalType.equalsIgnoreCase("WorkSpacePortal")) {
        strNoMetricsDashboardMsg = UINavigatorUtil.getI18nString("emxMetrics.label.Dashboard.NoPreferences", "emxMetricsStringResource", request.getHeader("Accept-Language"));
    }

    StringBuffer sfb = new StringBuffer();
    int paramind = 1;
    Enumeration enumParamNames = emxGetParameterNames(request);
    while (enumParamNames.hasMoreElements()) {
        String paramName = (String) enumParamNames.nextElement();
        String paramValue = emxGetParameter(request, paramName);

        if ((paramName != null && !"".equals("emxSuiteDirectory") && !"".equals("objectId") && !"".equals("jsTreeID")) && (paramValue != null && !"null".equals(paramValue) && !"".equals(paramValue))) {
            if (paramind > 1)
                sfb.append("&");

            sfb.append(XSSUtil.encodeForURL(context, paramName));
            sfb.append("=");
            sfb.append(XSSUtil.encodeForURL(context, paramValue));
            paramind++;
        }
    }

    String appendParams = sfb.toString();

%>

<head>
    <title></title>
    <%@include file="emxUIConstantsInclude.inc" %>

    <script>
        addStyleSheet("emxUIDefault");
        addStyleSheet("emxUIMenu");
        addStyleSheet("emxUIPortal");
    </script>

    <%
        MapList rowMapList = new MapList();
        MapList tempMapList = new MapList();
        String strDefaultPortal = "";
        String strSharedWorkspacePortal = "";
        boolean isWorkSpacePortalPresent = false;
        MapList rows = new MapList();

        String workspacePortalOwner = "";
        String strOwner = context.getUser();

        if (user == null || "".equals(user) || "null".equals(user)) {
            user = strOwner;
        }

        if ((portalType != null) && portalType.equalsIgnoreCase("WorkSpacePortal")) {
            if (portal != null) {
                strOwner = user;
                strDefaultPortal = portal;
            } else {
                // check the context user's portal existence
                strDefaultPortal = context.getUser() + ":MetricsDashboard";
                isWorkSpacePortalPresent = UIPortal.checkWorkSpacePortalExistence(context, strDefaultPortal);
            }
        }
        try {
            ContextUtil.startTransaction(context, false);
            if ((portalType != null) && portalType.equalsIgnoreCase("WorkSpacePortal")) {
                rowMapList = UIPortal.getWorkSpacePortal(context, strDefaultPortal, strOwner, UINavigatorUtil.getRequestParameterMap(request), PersonUtil.getAssignments(context), request.getHeader("Accept-Language"));
            } else {
                rowMapList = UIPortal.getPortal(context, portal, UINavigatorUtil.getRequestParameterMap(request), PersonUtil.getAssignments(context), request.getHeader("Accept-Language"));
            }
            if (rowMapList.size() > 0) {
    %>

    <script type="text/javascript" src="scripts/jquery-latest.js"></script>
    <script language="javascript" src="scripts/n-splitter.js"></script>

    <script language="javascript" src="scripts/emxUICoreMenu.js"></script>
    <script language="javascript" src="scripts/emxUITabset.js"></script>
    <script language="javascript" src="scripts/emxUIPortal.js"></script>

    <script type="text/javascript">
        <%
        ///////////////////////////////////////////////////////////
        //Code change for Reload button on portal - Start
        //
        String strEnableCache = emxGetParameter(request,"enableCache");
        if (strEnableCache == null || "".equals(strEnableCache) || "null".equals(strEnableCache)) {
        strEnableCache = "false";
        }
        if ("true".equals(strEnableCache)) {
        %>
        emxUIPortal.ENABLE_CACHE = true;
        <%
        }
        else {
        %>
        emxUIPortal.ENABLE_CACHE = false;
        <%
        }
        //
        //Code change for Reload button on portal - End
        ///////////////////////////////////////////////////////////
        %>

        var objPortal = new emxUIPortal;
        var objRow = null;
        var objContainer = null;
        //XSSOK
        <framework:mapListItr mapList="<%= rowMapList %>" mapName="rowMap">
        //XSSOK
        objRow = objPortal.addRow(new emxUIPortalRow("<%=UIPortal.getRowHeight(rowMap)%>"));
        //XSSOK
        <framework:mapListItr mapList="<%= UIPortal.getChannels(rowMap) %>" mapName="channelMap">
        <%
        String channelName = (String)channelMap.get("name");
        String verticalGroup = "";
        if(channelMap.containsKey("settings")){
            verticalGroup = (String)((HashMap)channelMap.get("settings")).get(DomainConstants.CHANNEL_GROUP_NAME);
        }

        verticalGroup = UIUtil.isNullOrEmpty(verticalGroup)? "": XSSUtil.encodeForJavaScript(context, verticalGroup);
        %>
        objContainer = objRow.addContainer(new emxUITabbedPortalContainer("<%=channelName%>"));
        //XSSOK
        objContainer.verticalGroup = "<%=verticalGroup%>";
        objContainer.portalName = "<%=XSSUtil.encodeForJavaScript(context,portal)%>";
        <framework:mapListItr mapList="<%= UIPortal.getChannelTabs(channelMap) %>" mapName="channelTabMap">
        <%
                                        StringBuffer href = new StringBuffer(100);
                                        href.append((String) UIPortal.getTabHRef(channelTabMap));
                                        if(portalType==null || "".equals(portalType) || "null".equalsIgnoreCase(portalType)){
                                            href.append("&jsTreeID=");
                                            href.append(XSSUtil.encodeForURL(context,jsTreeID));
                                        }
                                        String fullText = UIPortal.getTabLabel(channelTabMap);
                                        //added for bug 346636
                                        String tabName  = UIPortal.getName((HashMap)channelTabMap);
                                        //Modified for Bug 361707
                                        String displayText = UINavigatorUtil.htmlDecode(fullText);
                                        if(displayText.length() > tabLength) {
                                            displayText = displayText.substring(0, tabLength) + "...";
                                        }
                                        displayText = UINavigatorUtil.htmlEncode(displayText);

                                        //Modified for Bug 371025
                                        int index = href.indexOf("?");
                                        String aHref   = href.toString();
                                        String aParams = "";
                                        if(index > 0 && index < href.length())
                                        {
                                            aHref   = href.substring(0,index + 1);
                                            aParams = href.substring(index + 1);
                                        }

                                        /*if(href.indexOf("?") >= 0)
                                        {
                                            href.append("&" + appendParams);
                                        }
                                        else {
                                            href.append("?" + appendParams);
                                        }*/

                                        String params = UIPortal.mergeParams(aParams, appendParams);
                                        aHref += params;
        %>
        //XSSOK
        objContainer.addChannel(new emxUIComplexChannel("<%=displayText%>", "<%=fullText%>", "<%=aHref.toString()%>", "", "<%=tabName%>"));
        </framework:mapListItr>
        </framework:mapListItr>
        </framework:mapListItr>
        <%
                }
            }
            catch (Exception ex)
            {
                ContextUtil.abortTransaction(context);

                    if(ex.toString()!=null && (ex.toString().trim()).length()>0) {
                           emxNavErrorObject.addMessage("emxPortalDisplay:" + ex.toString().trim());
                    }
            }
            finally
            {
                ContextUtil.commitTransaction(context);
            }

        %>

    </script>

    <script type="text/javascript">
        if (getTopWindow().isMobile || getTopWindow().isPCTouch) {
            addStyleSheet("emxUIMobile", '../common/mobile/styles/');
        }
    </script>
</head>


<%
    if (("WorkSpacePortal".equalsIgnoreCase(portalType)) && (isWorkSpacePortalPresent == false) && (portal == null)) {

%>
<body onload="turnOffProgress();">
<form method="post">
    <table border="0" width="100%" cellpadding="0" cellspacing="0">
        <!-- //XSSOK -->
        <tr>
            <td><b><%=strNoMetricsDashboardMsg%>
            </b>
            </td>
        </tr>
    </table>
</form>
    <%  }
    else if(rowMapList == null || rowMapList.size() == 0) {
%>
<body onload="turnOffProgress();">
<form method="post">
    <table border="0" width="100%" cellpadding="0" cellspacing="0">
        <tr>
            <td><emxUtil:i18n localize="i18nId">emxFramework.Portal.Error.NoChannelsDefined</emxUtil:i18n></td>
        </tr>
    </table>
</form>
<%
} else {
%>
<!-- <body onload="objPortal.init(); turnOffProgress();"> -->
<%
    }
%>

</body>

<%@include file="emxNavigatorTimerBottom.inc" %>
<%@include file="emxNavigatorBottomErrorInclude.inc" %>

</html>
