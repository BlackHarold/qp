<%--  emxCreate.jsp
   Copyright (c) 1992-2015 Dassault Systemes.
   All Rights Reserved.
   This program contains proprietary and trade secret information of MatrixOne,Inc.
   Copyright notice is precautionary only
   and does not evidence any actual or intended publication of such program

   static const char RCSID[] = $Id: emxCreate.jsp.rca 1.13.1.2.1.4 Wed Oct 22 15:48:40 2008 przemek Experimental przemek $
--%>
<%@ page import="java.util.*,
                 java.io.*,
                 com.matrixone.jdom.*,
                 com.matrixone.jdom.output.*,
                 com.matrixone.jdom.transform.*,
                 com.matrixone.apps.framework.ui.UINavigatorUtil" %>
<%@include file="emxNavigatorInclude.inc" %>

<%@include file="emxNavigatorTopErrorInclude.inc" %>
<%@ page import="com.matrixone.util.*,
                 java.util.*,
                 com.matrixone.jdom.*,
                 com.matrixone.jdom.output.*" %>
<%@page import="com.matrixone.apps.framework.ui.UIUtil" %>
<%@ page import="org.apache.log4j.Logger" %>
<%@ page import="matrix.util.StringList" %>
<jsp:useBean id="createBean" class="com.matrixone.apps.framework.ui.UIForm" scope="session"/>
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
            String i18strNext = "Next";
            String i18strRedFields = "Fields in red italics are required";

            String i18strHeader = "";
            String i18strSubHeader = "";

%>
<script>

    window.addEventListener('load', function () {
        let radio = document.getElementsByName('fromto');

        for (let i = 0; i < radio.length; i++) {
            if (radio[i].value == 'To') {
                radio[i].checked = true;
                onchangeRadioDirection();
            }
        }

        let familyCombobox = document.getElementById("familyId");
        familyCombobox.disabled = true;
    });

    function onchangeComboboxStage() {
        let baselineCombobox = document.getElementsByName("baseline");
        let stageCombobox = document.getElementsByName("project_stage");

        for (let i = 1; i < baselineCombobox[0].length; i++) {
            if (baselineCombobox[0][i].value.endsWith("_null") ||
                !baselineCombobox[0][i].value.endsWith("_" + stageCombobox[0].value)) {
                baselineCombobox[0][i].style.display = 'none';
            } else {
                baselineCombobox[0][i].style.display = 'block';
            }
        }

        let selected = document.getElementById("project_stageId");
        let selectedText = selected.options[selected.selectedIndex].text;
        console.log(selectedText);

        if (selectedText != "BD") {
            document.getElementById("calc_stage").style.display = 'none';
        } else {
            document.getElementById("calc_stage").style.display = 'table-row';
        }

        document.getElementById("baselineId").selectedIndex = "0";
        document.getElementById("stageId").selectedIndex = "0";
    }

    function onfocusComboboxBaseline() {
        let baselineCombobox = document.getElementsByName("baseline");
        let stageCombobox = document.getElementsByName("project_stage");

        for (let i = 1; i < baselineCombobox[0].length; i++) {
            if (baselineCombobox[0][i].value.endsWith("_null") ||
                !baselineCombobox[0][i].value.endsWith("_" + stageCombobox[0].value)) {
                baselineCombobox[0][i].style.display = 'none';
            } else {
                baselineCombobox[0][i].style.display = 'block';
            }
        }
    }

    function onchangeComboboxDeviation() {
        let deviationValue = document.getElementById("deviationId").value;

        let typeLevelGroup = document.getElementById("calc_resultType");
        let depExpectedGroup = document.getElementById("calc_from_dep");

        let depElements = document.getElementsByName('from_dep');

        if (deviationValue === 'TRUE') {
            typeLevelGroup.hidden = true;
            depExpectedGroup.hidden = true;
        } else {
            typeLevelGroup.hidden = false
            depExpectedGroup.hidden = false;
        }
    }

    function onfocusComboboxResultType() {
        let familyCombobox = document.getElementById("familyId");
        let resultTypeCombobox = document.getElementById("resultTypeId");
    }

    function onchangeRadioDirection() {

        let radio = document.getElementsByName('fromto');

        let resultTypeCombobox = document.getElementById("resultTypeId");

        let project_stage = document.getElementById("calc_project_stage");
        let stage = document.getElementById("calc_stage");
        let baseline = document.getElementById("calc_baseline");
        let array;

        for (let i = 0; i < radio.length; i++) {
            if (radio[i].checked) {
                array = radio[i].value;
            }
        }

        $('#resultTypeId').prop('selectedIndex', 0);
        $('#familyId').prop('selectedIndex', 0);
        $('#familyId').prop('disabled', true);

        for (let i = 0; i < resultTypeCombobox.options.length; i++) {
            console.log(i + ':' + resultTypeCombobox.options[i].text);
        }

        if (array == 'From') {
            project_stage.setAttribute('style', '');
            stage.setAttribute('style', '');
            baseline.setAttribute('style', '');

            let project_stage_label = document.querySelector('#calc_project_stage > td.createLabel');
            let stage_label = document.querySelector('#calc_stage > td.createLabel');
            let baseline_label = document.querySelector('#calc_baseline > td.createLabel');

            if (project_stage_label != undefined) {
                project_stage_label.setAttribute('class', 'createLabelRequired');
                myValidationRoutines.push(['validateRequiredField', 'project_stage']);

                baseline_label.setAttribute('class', 'createLabelRequired');
                myValidationRoutines.push(['validateRequiredField', 'baseline']);
            }

            for (let i = 0; i < resultTypeCombobox.options.length; i++) {
                console.log('i:' + resultTypeCombobox.options[i].text)
            }
            resultTypeCombobox.options[2].setAttribute('style', 'display:none');
            resultTypeCombobox.options[4].setAttribute('style', 'display:none');
        }

        if (array == 'To') {
            project_stage.setAttribute('style', 'display:none');
            stage.setAttribute('style', 'display:none');
            baseline.setAttribute('style', 'display:none');

            $('#project_stageId').prop('selectedIndex', 0);
            $('#stageId').prop('selectedIndex', 0);
            $('#baselineId').prop('selectedIndex', 0);

            let project_stage_label = document.querySelector('#calc_project_stage > td.createLabelRequired');
            let stage_label = document.querySelector('#calc_stage > td.createLabelRequired');
            let baseline_label = document.querySelector('#calc_baseline > td.createLabelRequired');

            if (project_stage_label != undefined) {
                project_stage_label.setAttribute('class', 'createLabel');

                baseline_label.setAttribute('class', 'createLabel');
                myValidationRoutines.splice(myValidationRoutines.length - 3, myValidationRoutines.length - 1);
            }

            resultTypeCombobox.options[2].setAttribute('style', '');
            resultTypeCombobox.options[4].setAttribute('style', '');
        }
    }

    function onchangeComboboxResultType() {
        let resultTypeCombobox = document.getElementById("resultTypeId");
        let familyCombobox = document.getElementById("familyId");
        $('#familyId').prop('selectedIndex', 0);
        familyCombobox.selectedIndex = 0;

        let selectedText = resultTypeCombobox.options[resultTypeCombobox.selectedIndex].text;
        let selectedValue = resultTypeCombobox.options[resultTypeCombobox.selectedIndex].value;
        console.log(selectedText + '|' + selectedValue);

        if (selectedText == '') {
            familyCombobox.disabled = true;
            familyCombobox.options[familyCombobox.selectedIndex].text = '';
            familyCombobox.options[familyCombobox.selectedIndex].value = '';
        } else {
            familyCombobox.disabled = false;
        }

        for (var i = 1; i < familyCombobox.length; i++) {
            if (familyCombobox.options[i].value.endsWith("_" + resultTypeCombobox.value)) {
                familyCombobox.options[i].setAttribute('style', '');
            } else {

                familyCombobox.options[i].setAttribute('style', 'display:none;');
            }
        }

    }

    function onchangeFormTaskType() {
        let DEP = $("input[name=DEPTaskOID]").val();
        let description = document.getElementById("DescriptionEn");
        let descriptionru = document.getElementById("DescriptionRu");
        let name = document.getElementById("NameEn");
        let nameRu = document.getElementById("NameRu");

        $.post(
            "IMS_JPOInvoke.jsp",
            {
                program: "IMS_QP_DEPTask",
                function: "dynamicFormTask",
                args: [DEP]
            },
            function (data, status) {
                let message = data.split("|");
                name.value = message[0];
                nameRu.value = message[1];
                description.value = message[2];
                descriptionru.value = message[3];


            });
    }

    function onchangeFormExpectedResultType() {
        let DEP = $("input[name=from_depOID]").val();
        let DEPID = $("input[name=objectId]").val();

        let from = document.getElementsByName("fromto");                                                                //[0]
        let doccode = document.getElementById("DocCode");                                                               //[1]
        let name = document.getElementById("NameEn");                                                                   //[2]
        let nameRu = document.getElementById("NameRu");                                                                 //[3]
        let description = document.getElementById("DescriptionEnId");                                                   //[4]
        let description_ru = document.getElementById("DescriptionRuId");                                                //[5]

        let project_stage = document.getElementById("project_stageId");                                                 //[6]
        let stage = document.getElementById("stageId");                                                                 //[7]
        let baseline = document.getElementById("baselineId");                                                           //[8]
        let resultTypeCombobox = document.getElementById("resultTypeId");                                               //[9-10]
        let familyCombobox = document.getElementById("familyId");                                                       //[11-12]

        $.post(
            "IMS_JPOInvoke.jsp",
            {
                program: "IMS_QP_ExpectedResult",
                function: "dynamicFormFields",
                args: [DEP, DEPID]
            },
            function (data, status) {
                const message = data.split("|");

                try {
                    from[0].checked = true;
                } catch (e) {
                    console.log(e);
                }

                doccode.value = checkNull(message[1]);
                name.value = checkNull(message[2]);
                nameRu.value = checkNull(message[3]);
                description.value = checkNull(message[4]);
                description_ru.value = checkNull(message[5]);

                project_stage.value = checkNull(message[6]);
                project_stage.text = checkNull(message[6]);
                stage.value = checkNull(message[7]);
                stage.text = checkNull(message[8]);
                baseline.value = checkNull(message[8]);
                baseline.text = checkNull(message[8]);

                familyCombobox.options[i].value = checkNull(message[9]);
                familyCombobox.options[i].text = checkNull(message[10]);
                resultTypeCombobox.options[i].value = checkNull(message[11]);
                resultTypeCombobox.options[i].text = checkNull(message[12]);

                // doccode.disabled = true;
                // name.disabled = true;
                // nameRu.disabled = true;
                // description.disabled = true;
                // description_ru.disabled = true;

                project_stage.disabled = true;
                stage.disabled = true;
                baseline.disabled = true;

                resultTypeCombobox.disabled = true;
                familyCombobox.disabled = true;
            });
    }


    function checkNull(value) {
        value = (value != null && value != undefined && value != '') ? value : '_';
        return value;
    }
</script>
<%

    try {
        i18strClear = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.FormComponent.Clear");
        i18strDone = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Common.Done");
        i18strCancel = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Button.Cancel");
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
            i18strHeader = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Create.DefaultHeading") + " " + UINavigatorUtil.getAdminI18NString("Type", typeStr, languageStr);
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
        i18strNext = "Next";
        i18strRedFields = "Fields in red italics are required";
    }
    root.addContent(createBean.createSetting("clear", i18strClear, nameSpace));
    root.addContent(createBean.createSetting("header", i18strHeader, nameSpace));
    root.addContent(createBean.createSetting("subHeader", i18strSubHeader, nameSpace));
    root.addContent(createBean.createSetting("apply", i18strApply, nameSpace));
    root.addContent(createBean.createSetting("done", i18strDone, nameSpace));
    root.addContent(createBean.createSetting("cancel", i18strCancel, nameSpace));
    root.addContent(createBean.createSetting("next", i18strNext, nameSpace));
    root.addContent(createBean.createSetting("fieldsinred", i18strRedFields, nameSpace));
    //Remove the basic fields from request, to avoid creation of hidden variables on the form.
    requestMap.remove("owner");
    requestMap.remove("vault");
    requestMap.remove("policy");
    requestMap.put("localeObj", request.getLocale().toString());
    requestMap.put("submitMethod", request.getMethod());

    //SpellChecker
    String spellCheckerURL = EnoviaResourceBundle.getProperty(context, "emxFramework.spellchecker.URL");
    requestMap.put("spellCheckerURL", spellCheckerURL.trim());
    requestMap.put("spellCheckerLang", UIUtil.getLanguageForSpellChecker(request));

    root.addContent(createBean.explodeMap("requestMap", requestMap, nameSpace));

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
        System.out.println(ex.toString().trim());
%>
<%@include file="emxNavigatorBottomErrorInclude.inc" %>
<script>
    getTopWindow().close();
</script>
<%
        }
    }
} else {
    String errorMessage = EnoviaResourceBundle.getProperty(context, "emxFrameworkStringResource", locale, "emxFramework.Create.TypeErrorMessage");
%>
<script language="JavaScript">
    alert("<%=errorMessage%>");
    getTopWindow().close();
</script>
<%
    }
%>
<%
    UIUtil.logConfigurations(context, request, requestMap);
%>
