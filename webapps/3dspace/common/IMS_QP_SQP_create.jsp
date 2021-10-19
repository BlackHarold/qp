<%--  emxCreate.jsp
   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxCreate.jsp.rca 1.13.1.2.1.4 Wed Oct 22 15:48:40 2008 przemek Experimental przemek $
--%>
<%@ page import="com.matrixone.apps.framework.ui.UINavigatorUtil,
                 com.matrixone.apps.framework.ui.UIUtil,
                 com.matrixone.jdom.*,
                 com.matrixone.jdom.output.*,
                 com.matrixone.jdom.transform.*,
                 java.util.*" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@include file="emxNavigatorInclude.inc" %>

<%@include file="emxNavigatorTopErrorInclude.inc" %>
<jsp:useBean id="createBean" class="com.matrixone.apps.framework.ui.UIForm" scope="session"/>

<script>
    function doCancel() {
        window.close();
    }

    function onChangeComboboxDep() {
        let depId = document.getElementById("depId");
        let depIndex = depId.selectedIndex;
        let value = depId[depIndex].value;
        let text = depId[depIndex].text;
        let systemInput = document.querySelector("#calc_system > td.createInputField > input[type=\"text\"]:nth-child(1)");
        console.log('systemInput ' + systemInput);

        //check attribute
        if (value.endsWith("_TRUE")) {
            document.getElementById("calc_system").style.display = 'none';
            systemInput.value = "";
            console.log('value: ' + value + " | text: " + text);
        } else {
            document.getElementById("calc_system").style.display = 'table-row';
        }

        let dep = document.querySelector("#calc_group > td.createInputField.disabled > input[type=\"text\"]:nth-child(1)");
        dep.id = value.substring(0, value.indexOf('_'));

        //change script to the group button
        let groupButton = document.querySelector("#calc_group > td.createInputField.disabled > input[type=\"button\"]:nth-child(4)");

        groupButton.setAttribute("onClick",
            "javascript:showChooser('../common/emxIndentedTable.jsp?program=IMS_QP_Classifier:findAll&" +
            "depId=" + dep.id +
            "&table=IMS_QP_Classifier_table&selection=single&targetLocation=popup&mode=chooser&cancelButton=true&cancelLabel=Cancel" +
            "&submitLabel=Select&submitURL=../common/AEFSearchUtil.jsp&fieldNameActual=group&fieldNameDisplay=groupDisplay" +
            "&fieldNameOID=groupOID&suiteKey=Framework','600','600','true','','group')")
    }

    function onChangeTextboxPBS() {

        console.log("onchangeTextboxPBS");
        //dep field
        var dep_field = document.querySelector("#depId");

        console.log("before selected: " + dep_field.selectedIndex);
        // var depId = document.getElementById("depId");
        // var depIndex = depId.selectedIndex;
        // var value = depId[depIndex].value;

        var systemOIDs = document.querySelector("#calc_system > td.createInputField > input[type=\"hidden\"]:nth-child(3)");

        for (var i = 1; i < dep_field.length; i++) {
            console.log("includes: " + dep_field[i].value + " == " + systemOIDs.value + ": " + dep_field[i].value.includes(systemOIDs.value));
            // console.log("index of: " + dep_field[i].value + " == " + systemOIDs.value + ": " + dep_field[i].value.indexOf(systemOIDs.value));

            if (systemOIDs.value==''||dep_field[i].value.includes(systemOIDs.value) && dep_field[i].value.endsWith("_FALSE")) {
                console.log(i + ": " + dep_field[i].value);
                dep_field[i].style.display = 'block';
            } else {
                dep_field[i].style.display = 'none';
            }
        }

        dep_field.selectedIndex = 0;
        console.log("after selected: " + dep_field.selectedIndex);
    }
</script>

<%
    String form = emxGetParameter(request, "form");
    if (UIUtil.isNotNullAndNotEmpty(form)) {
        String accessUsers = MqlUtil.mqlCommand(context, "print form $1 select $2 dump", form, "property[AccessUsers].value");
        if (UIUtil.isNotNullAndNotEmpty(accessUsers)) {
            if (!PersonUtil.hasAnyAssignment(context, accessUsers)) {
                return;
            }
        }
    }

// timeStamp to handle the business objectList
    String timeStamp = createBean.getTimeStamp();
    Vector userRoleList = PersonUtil.getAssignments(context);
    String suiteKey = emxGetParameter(request, "suiteKey");
    String header = emxGetParameter(request, "header");
    String subHeader = emxGetParameter(request, "subHeader");
    String objectId = emxGetParameter(request, "objectId");
    String targetLocation = emxGetParameter(request, "targetLocation");
    String emxTableRowIds = "";
// The objectId , relId from the emxTable.jsp is passd as "emxTableRowId"
    String tableRowIdList[] = Request.getParameterValues(request, "emxTableRowId");
    String mode = emxGetParameter(request, "mode");
    String tableRowId = "";
    if (tableRowIdList != null) {
        tableRowId = tableRowIdList[0];
    }

    if (tableRowId != null && tableRowId.length() > 0) {
        objectId = tableRowId.trim();
        if (objectId.indexOf("|") != -1) {
            StringList objectIdList = FrameworkUtil.split(objectId, "|");
            //objectIdList.size() will be 3 for root node
            if (objectIdList.size() == 3) {
                objectId = (String) objectIdList.get(0);
            } else {
                objectId = (String) objectIdList.get(1);
            }
        }
    }

    if (tableRowIdList != null) {
        String seperator = "";
        for (int i = 0; i < tableRowIdList.length; i++) {
            emxTableRowIds = emxTableRowIds + seperator + tableRowIdList[i].trim();
            seperator = ":";
        }
    }

    String xsltFile = "emxCreatePage.xsl";
    String ua = request.getHeader("user-agent");
    String timeZone = (String) session.getAttribute("timeZone");
//Start : IR-044112V6R2011
    String sRdoTNR = (String) session.getAttribute("rdoTNR");
//End : IR-044112V6R2011
    String isUnix = (ua.toLowerCase().indexOf("x11") > -1) ? "true" : "false";
    boolean flag = false;

    HashMap requestMap = UINavigatorUtil.getRequestParameterMap(pageContext);
    requestMap = UINavigatorUtil.appendURLParams(context, requestMap, "Create");
    emxTableRowIds = emxTableRowIds.equals("undefined") ? "" : emxTableRowIds;
    requestMap.put("emxTableRowIds", emxTableRowIds);
    MapList fields = new MapList();

//Code to remove support to toolbar, editLink and portalMode passed as URL parameters.
//This piece of code can be removed if in future the CreateComponent needs to support the same.
    String toolbar = (String) requestMap.get("toolbar");
    String editLink = (String) requestMap.get("editLink");
    String portalMode = (String) requestMap.get("portalMode");
    String languageStr = (String) requestMap.get("languageStr");
    Locale locale = new Locale(languageStr);
    String settingErrMsg = "";

    if (toolbar != null && !"null".equals(toolbar) && !"".equals(toolbar)) {
        requestMap.remove("toolbar");
    }
    if (editLink != null && !"null".equals(editLink) && !"".equals(editLink)) {
        requestMap.remove("editLink");
    }
    if (portalMode != null && !"null".equals(portalMode) && !"".equals(portalMode)) {
        requestMap.remove("portalMode");
    }
//End
    if (UIComponent.getUIAutomationStatus(context)) {
        requestMap.put("uiAutomation", "true");
    }
    if (objectId != null && objectId.length() > 0) {
        requestMap.put("objectId", objectId);
    }
//Start : IR-044112V6R2011 - This RDO value which is retrieved from the session will be used by EC
//in order to populate wherever the RDO field is used in create forms.
    if (sRdoTNR != null && sRdoTNR.length() > 0) {
        requestMap.put("sRdoTNR", sRdoTNR);
    }
//End : IR-044112V6R2011
    String typeStr = (String) requestMap.get("type");
    String strtypeChooser = (String) requestMap.get("typeChooser");

    if (
            (typeStr == null || "null".equals(typeStr) || "".equals(typeStr)) && (strtypeChooser == null || "null".equals(strtypeChooser) || "".equals(strtypeChooser) || "false".equalsIgnoreCase(strtypeChooser))) {
        if (form != null && form.length() > 0) {
            MapList mapfields = createBean.getFields(context, form, userRoleList);
            Iterator itr = mapfields.iterator();
            while (itr.hasNext()) {
                HashMap field = (HashMap) itr.next();
                if (createBean.isTypeField(field)) {
                    flag = true;
                    break;
                }
            }
        }
    } else {
        flag = true;
    }

    if (flag) {
        try {
            createBean.setCreateFormData(context, timeStamp, requestMap, userRoleList);
            fields = createBean.getFormFields(timeStamp);
            settingErrMsg = createBean.checkForUnspportedSetting(fields);
            String localDateFormat = ((java.text.SimpleDateFormat) java.text.DateFormat.getDateInstance(eMatrixDateFormat.getEMatrixDisplayDateFormat(), request.getLocale())).toLocalizedPattern();
            String allowKeyableDates = "false";
            try {
                allowKeyableDates = EnoviaResourceBundle.getProperty(context, "emxFramework.AllowKeyableDates");
            } catch (Exception e) {
                allowKeyableDates = "false";
            }
            HashMap inputMap = new HashMap();

            if (form == null || form.length() == 0) {
                form = "mxDummyForm";
            }
            inputMap.put("localDateFormat", localDateFormat);
            inputMap.put("allowKeyableDates", allowKeyableDates);
            inputMap.put("timeZone", timeZone);
            inputMap.put("localeObj", request.getLocale());
            inputMap.put("languageStr", (String) requestMap.get("languageStr"));
            inputMap.put("formname", form);
            inputMap.put("pageContext", pageContext);

            Vector contentVector = new Vector();
            Element root = new Element("mxRoot", createBean.XML_NAMESPACE, "http://www.matrixone.com/" + createBean.XML_NAMESPACE);
            Namespace nameSpace = root.getNamespace();
            Map pairs = new HashMap();
            pairs.put("type", "text/xsl");
            pairs.put("href", xsltFile);
            ProcessingInstruction pi = new ProcessingInstruction("xml-stylesheet", pairs);
            contentVector.add(pi);

            //set the root element
            contentVector.add(root);
            Document doc = new Document(contentVector);

            root.addContent(createBean.createSetting("timeStamp", timeStamp, nameSpace));
            root.addContent(createBean.createSetting("isUnix", isUnix, nameSpace));
            root.addContent(createBean.createSetting("isMobile", String.valueOf(UINavigatorUtil.isMobile(context))));
            root.addContent(createBean.createSetting("isPCTouch", String.valueOf(UINavigatorUtil.isPCTouch(context))));

            String i18strClear = "Clear";
            String i18strApply = "Done";
            String i18strDone = "Done";
            String i18strCancel = "Cancel";
            String i18strClose = "Close";
            String i18strNext = "Next";
            String i18strRedFields = "Fields in red italics are required";

            String i18strHeader = "";
            String i18strSubHeader = "";

            try {
                i18strClear = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.FormComponent.Clear");
                i18strDone = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Common.ok");
                i18strCancel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Button.Cancel");
                i18strClose = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Button.Close");
                i18strApply = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Consolidate.Button.Apply");
                i18strNext = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Button.Next");
                i18strRedFields = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Common.FieldsInRed");

                if (header != null && !"null".equalsIgnoreCase(header) && !"".equalsIgnoreCase(header)) {
                    i18strHeader = UIForm.getFormHeaderString(context,
                            pageContext,
                            header,
                            null,
                            suiteKey,
                            languageStr);

                    i18strHeader = UIUtil.getWindowTitleName(context, null, objectId, i18strHeader);
                } else {

                    if (typeStr == null || "null".equals(typeStr) || "".equals(typeStr)) {
                        typeStr = "";
                    } else {
                        StringList typeList = FrameworkUtil.split(typeStr, ",");
                        if (typeList.size() > 1) {
                            typeStr = (String) typeList.get(0);
                        }
                        if (typeStr.startsWith("_selectedType:")) {
                            String ary[] = typeStr.split("_selectedType:");
                            typeStr = ary[1];
                        }
                        String aliasname = typeStr;
                        try {
                            typeStr = PropertyUtil.getSchemaProperty(context, typeStr);
                        } catch (Exception e) {
                            typeStr = aliasname;
                        }
                        if (typeStr == null || "null".equals(typeStr) || "".equals(typeStr)) {
                            typeStr = aliasname;
                        }
                    }
                    String[] messageValues = new String[1];
                    messageValues[0] = UINavigatorUtil.getAdminI18NString("Type", typeStr, languageStr);
                    i18strHeader = MessageUtil.getMessage(context, null,
                            "emxFramework.Create.DefaultHeading",
                            messageValues, null,
                            locale, "emxFrameworkStringResource");
                }
                if (subHeader != null && !"null".equalsIgnoreCase(subHeader) && !"".equalsIgnoreCase(subHeader)) {
                    i18strSubHeader = UIForm.getFormHeaderString(context,
                            pageContext,
                            subHeader,
                            null,
                            suiteKey,
                            languageStr);
                }

            } catch (Exception e) {
                i18strClear = "Clear";
                i18strApply = "Apply";
                i18strDone = "Done";
                i18strCancel = "Cancel";
                i18strClose = "Close";
                i18strNext = "Next";
                i18strRedFields = "Fields in red italics are required";
            }
            root.addContent(createBean.createSetting("clear", i18strClear, nameSpace));
            root.addContent(createBean.createSetting("header", i18strHeader, nameSpace));
            root.addContent(createBean.createSetting("subHeader", i18strSubHeader, nameSpace));
            root.addContent(createBean.createSetting("apply", i18strApply, nameSpace));
            root.addContent(createBean.createSetting("done", i18strDone, nameSpace));
            root.addContent(createBean.createSetting("cancel", i18strCancel, nameSpace));
            root.addContent(createBean.createSetting("close", i18strClose, nameSpace));
            root.addContent(createBean.createSetting("next", i18strNext, nameSpace));
            root.addContent(createBean.createSetting("fieldsinred", i18strRedFields, nameSpace));
            //Remove the basic fields from request, to avoid creation of hidden variables on the form.
            requestMap.remove("owner");
            requestMap.remove("vault");
            //requestMap.remove("policy");
            requestMap.put("localeObj", request.getLocale().toString());
            requestMap.put("submitMethod", request.getMethod());

            //SpellChecker
            String spellCheckerURL = EnoviaResourceBundle.getProperty(context, "emxFramework.spellchecker.URL");
            requestMap.put("spellCheckerURL", spellCheckerURL.trim());
            requestMap.put("spellCheckerLang", UIUtil.getLanguageForSpellChecker(request));

            //JavaScript encoding for requestMap starts

            HashMap requestMapForEncoding = new HashMap();

            Iterator requestMapItr = requestMap.keySet().iterator();
            while (requestMapItr.hasNext()) {
                String key = (String) requestMapItr.next();
                String value = (String) requestMap.get(key);
                key = XSSUtil.encodeForJavaScript(context, key);
                if (UIUtil.isNotNullAndNotEmpty(value)) {
                    value = XSSUtil.encodeForJavaScript(context, value);
                }
                requestMapForEncoding.put(key, value);
            }

            //JavaScript encoding for requestMap ends

            root.addContent(createBean.explodeMap("requestMap", requestMapForEncoding, nameSpace));
            requestMapForEncoding = null;

            //get emxToolbarJavaScript.jsp response here to avoid server side unnecesary round trip
            CharArrayWriterResponse customResponse = new CharArrayWriterResponse(response);

            StringBuffer toolbarURL = new StringBuffer("emxToolbarJavaScript.jsp?uiType=form&printerFriendly=false&export=false&timeStamp=");
            toolbarURL.append(timeStamp);
            Iterator itr = requestMap.keySet().iterator();
            while (itr.hasNext()) {
                String key = (String) itr.next();
                if (key.equals("timeStamp")) {
                    continue;
                }
                String value = "";
                try {
                    value = (String) requestMap.get(key);
                } catch (Exception ex) {
                    String[] values = (String[]) requestMap.get(key);
                    value = FrameworkUtil.join(values, ",");
                }
                toolbarURL.append("&");
                toolbarURL.append(key);
                toolbarURL.append("=");
                toolbarURL.append(value);
            }

            request.getRequestDispatcher(toolbarURL.toString()).include(request, customResponse);
            Element toolbarCode = new Element("toolbarCode");
            toolbarCode.setContent(new CDATA(customResponse.toString()));
            root.addContent(toolbarCode);

            // get emxFormConstantsJavascriptInclude.jsp
            customResponse = new CharArrayWriterResponse(response);
            request.getRequestDispatcher("emxFormConstantsJavascriptInclude.jsp").include(request, customResponse);
            Element formConstantsCode = new Element("formConstants");
            formConstantsCode.setContent(new CDATA(customResponse.toString()));
            root.addContent(formConstantsCode);

            // get emxJSValidation.jsp
            customResponse = new CharArrayWriterResponse(response);
            request.getRequestDispatcher("emxJSValidation.jsp").include(request, customResponse);
            Element JSValidationCode = new Element("JSValidations");
            JSValidationCode.setContent(new CDATA(customResponse.toString()));
            root.addContent(JSValidationCode);

            //get columns
            inputMap.put("timeStamp", timeStamp);
            inputMap.put("uiType", "createForm");
            createBean.addFields(context, root, nameSpace, fields, inputMap);
            if (settingErrMsg != null && !"null".equals(settingErrMsg) && !"".equals(settingErrMsg)) {
                String errorMessage = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Create.FieldTypeErrorMessage");
                settingErrMsg = errorMessage + "\\n " + settingErrMsg;
                Element unsupp = new Element("UnsupportedFieldTypeSetting", root.getNamespace());
                unsupp.addContent(createBean.createSetting("Message", settingErrMsg, nameSpace));
                root.addContent(unsupp);
            }

            StringList incFileList = UIComponent.getCreateJSValidationFileList(context, suiteKey);
            if (incFileList != null && incFileList.size() > 0) {
                //add Custom clientside validation includes
                Element custVal = new Element("CustomValidation", root.getNamespace());
                String fileTok = "";
                for (StringItr keyItr = new StringItr(incFileList); keyItr.next(); ) {
                    custVal.addContent(createBean.createSetting("file", keyItr.obj(), nameSpace));
                }
                root.addContent(custVal);
            }


            //out.clear();
            XSLTransformer transformer = new XSLTransformer(new java.io.File(request.getRealPath("/common"), xsltFile));
            com.matrixone.jdom.output.Format format = com.matrixone.jdom.output.Format.getCompactFormat();
            format.setTextMode(com.matrixone.jdom.output.Format.TextMode.PRESERVE);
            format.setExpandEmptyElements(true);
            format.setOmitDeclaration(true);

            XMLOutputter outputter = new XMLOutputter(format);
            outputter.output(new DocType("html"), out);
            String outString = outputter.outputString(transformer.transform(doc));
            outString = FrameworkUtil.findAndReplace(outString, "&amp;", "&");
            outString = FrameworkUtil.findAndReplace(outString, "&lt;br/&gt;", "<br/>");
            out.print(outString);

    /*response.setContentType("text/xml; charset=UTF-8");
    XMLOutputter  outputter = MxXMLUtils.getOutputter();
    outputter.output(doc, out);*/
            //outputter.output(doc, System.out);
            ContextUtil.commitTransaction(context);
            // End: Get Data
        } catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            if (ex.toString() != null && (ex.toString().trim()).length() > 0) {
                emxNavErrorObject.addMessage(ex.toString().trim());
%>
<%@include file="emxNavigatorBottomErrorInclude.inc" %>
<script>
    if ("slidein" == "<xss:encodeForJavaScript><%=targetLocation%></xss:encodeForJavaScript>") {
        getTopWindow().closeSlideInDialog();
    } else {
        getTopWindow().close();
    }
</script>
<%
        }
    }
} else {
    String errorMessage = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Create.TypeErrorMessage");
%>
<script language="JavaScript">
    alert("<%=errorMessage%>");
    if ("slidein" == "<xss:encodeForJavaScript><%=targetLocation%></xss:encodeForJavaScript>") {
        getTopWindow().closeSlideInDialog();
    } else {
        getTopWindow().close();
    }
</script>
<%
    }
%>

