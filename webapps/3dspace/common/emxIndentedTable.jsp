<%--  emxIndentedTable.jsp
   Copyright (c) 1992-2018 Dassault Systemes.
   All Rights Reserved.
--%>

<%@page import="com.matrixone.apps.framework.ui.UIUtil"%>
<%@page import="com.matrixone.apps.domain.util.EnoviaBrowserUtility.Browsers"%>
<%@page import="javax.json.JsonArrayBuilder"%>
<%@page import="javax.json.JsonObject,javax.json.Json"%>
<%@page import="javax.json.JsonObjectBuilder"%>
<%@ page import="java.util.*,java.io.*,
                com.matrixone.jdom.*,
                com.matrixone.jdom.output.*,
                com.matrixone.jdom.transform.*,
                com.matrixone.apps.framework.ui.UINavigatorUtil,
				com.matrixone.apps.domain.util.FrameworkUtil,
				com.matrixone.apps.domain.util.BPSJsonObjectBuilder"%>

<%@include file = "emxNavigatorNoDocTypeInclude.inc"%>
<%@include file = "emxNavigatorTopErrorInclude.inc"%>
<%@include file = "emxNavigatorCheckReadAccess.inc"%>
<jsp:useBean id="indentedTableBean" class="com.matrixone.apps.framework.ui.UITableIndented" scope="session"/>
<jsp:useBean id="structureCompareBean" class="com.matrixone.apps.framework.ui.UIStructureCompare" scope="session"/>
<%
  HashMap requestMap = UINavigatorUtil.getRequestParameterMap(pageContext);
  JsonObject freezePaneLayoutData = null;
  JsonObjectBuilder freezePaneLayoutDataBuilder = BPSJsonObjectBuilder.createJsonObjectBuilder(Json.createObjectBuilder());
  JsonArrayBuilder customValidationFiles = Json.createArrayBuilder();
  String  fPLayoutDataString= "";

  requestMap = UINavigatorUtil.appendURLParams(context, requestMap, "StructureBrowser");
  String isStructureCompare = (String) requestMap.get("IsStructureCompare");
  String logPerformance = emxGetParameter(request, "logPerformance");
  boolean isLoggingEnabled=false;
  long serverTimeLocal = 0L;
  boolean isStructure = Boolean.parseBoolean(isStructureCompare);
  requestMap.put("IsStructureCompare", String.valueOf(isStructure).toUpperCase());

  String portalMode = (String) requestMap.get("portalMode"); 
  boolean isPortalMode = Boolean.parseBoolean(portalMode);
  requestMap.put("portalMode", String.valueOf(isPortalMode));
  
  String isFromInContextSearch = (String) requestMap.get("fromCtxSearch");
  if(isFromInContextSearch == null){
	  requestMap.put("fromCtxSearch","false");
  }
  if ("TRUE".equalsIgnoreCase(isStructureCompare)){
    indentedTableBean = (com.matrixone.apps.framework.ui.UIStructureCompare)structureCompareBean;
  }
  String persist = emxGetParameter(request, "persist");
  String timeStampOld = emxGetParameter(request, "timeStamp");
  String timeStamp = "";
  // timeStamp to handle the business objectList
  if(!"true".equalsIgnoreCase(persist)){
  	 timeStamp = indentedTableBean.getTimeStamp();
  }else{
	  timeStamp = timeStampOld;
  }
  
  if(UIUtil.isNotNullAndNotEmpty(logPerformance) && "true".equalsIgnoreCase(logPerformance)) {
	  serverTimeLocal = System.currentTimeMillis();
	  isLoggingEnabled=true;
  }
  Vector userRoleList    = PersonUtil.getAssignments(context);
  String suiteKey        = emxGetParameter(request, "suiteKey");
  String strCustomize    = emxGetParameter(request, "customize");
  String isDynamicGridTable = emxGetParameter(request, "gridTable");
  String massUpdateTCL = emxGetParameter(request, "massUpdateTCL");
  String strCustomizationEnabled = "enable";

  // Check for transaction type passed in (needed to save ".finder" during a query)
  String transactionType = emxGetParameter(request, "TransactionType");
  boolean updateTransaction = (transactionType != null && transactionType.equalsIgnoreCase("update"));

  //add showStructure param
  String preProcessJPO = emxGetParameter(request, "preProcessJPO");
  String preProcessURL = emxGetParameter(request, "preProcessURL");

  String postProcessJPO = emxGetParameter(request, "postProcessJPO");
  String postProcessURL= emxGetParameter(request, "postProcessURL");

  String cancelProcessJPO = emxGetParameter(request, "cancelProcessJPO");
  String cancelProcessURL = emxGetParameter(request, "cancelProcessURL");
  
  String persistSort = emxGetParameter(request, "persistSort");

  String split = "";

  String languageStr = request.getHeader("Accept-Language");
  String objectString   = UINavigatorUtil.getI18nString("emxFramework.Common.object",
                                "emxFrameworkStringResource",
                                languageStr);
  String objectsString  = UINavigatorUtil.getI18nString("emxFramework.Common.objects",
                                "emxFrameworkStringResource",
                                languageStr);
  String selectedString   = UINavigatorUtil.getI18nString("emxFramework.Common.selected",
                                "emxFrameworkStringResource",
                                languageStr);
  String enterValueString = UINavigatorUtil.getI18nString("emxFramework.FreezePane.MoreLevel.EnterValue",
                            "emxFrameworkStringResource",
                            languageStr);
  String clearString      = UINavigatorUtil.getI18nString("emxFramework.Common.Clear",
                            "emxFrameworkStringResource",
                            languageStr);

  String xsltFile = "../webapps/ENOAEFStructureBrowser/assets/xslt/emxFreezePaneLayout.xsl";
  String level = emxGetParameter(request, "level");
  String ua = request.getHeader("user-agent");
  String timeZone=(String)session.getAttribute("timeZone");
  String isUnix = (ua.toLowerCase().indexOf("x11") > -1)?"true":"false";
  String isIE = EnoviaBrowserUtility.is(request,Browsers.IE)+"";
  String isMobile = String.valueOf(UINavigatorUtil.isMobile(context));
 	String isPCTouch = String.valueOf(UINavigatorUtil.isPCTouch(context));	 
  String submitMethod = request.getMethod();
  String displayView = null;

  try {
    ContextUtil.startTransaction(context, updateTransaction);
    //HashMap requestMap = UINavigatorUtil.getRequestParameterMap(pageContext);
    // Added for Custom Table
    strCustomizationEnabled = EnoviaResourceBundle.getProperty(context, "emxFramework.UITable.Customization");
    //Added for Structure Browser - Dynamic Columns

    if(!requestMap.containsKey("displayView")){
	    try{
	       displayView = EnoviaResourceBundle.getProperty(context, "emxFramework.Freezepane.view");
	       requestMap.put("displayView",displayView.trim());
	    }catch(Exception ex){
	    }
    }
    //SpellChecker
    String spellCheckerURL = EnoviaResourceBundle.getProperty(context,"emxFramework.spellchecker.URL");
    requestMap.put("spellCheckerURL",spellCheckerURL.trim());
    requestMap.put("spellCheckerLang",UIUtil.getLanguageForSpellChecker(request));
    requestMap.put("massUpdateTCL",massUpdateTCL);

    requestMap.put("uiType","structureBrowser");
    String isFullSearch = (String)requestMap.get("fullTextSearch");
    if(!"true".equalsIgnoreCase(isFullSearch)){
        requestMap.put("submitMethod",request.getMethod());
    }
	/* Start: IR-048208V6R2011x: If the URL contains the japanese characters, it must be decoded before further processing. */
    if("Shift_JIS".equals(request.getCharacterEncoding()) && "true".equalsIgnoreCase(isFullSearch)){
		String ftsFilters=(String) requestMap.get("ftsFilters");
		ftsFilters = com.matrixone.apps.domain.util.XSSUtil.decodeFromURL(ftsFilters);
		requestMap.put("ftsFilters",ftsFilters);
	}
	/*End: IR-048208V6R2011x */
    //Ended for Structure Browser - Dynamic Columns

    String[] arrayTableRowIds = emxGetParameterValues(request, "emxTableRowId");
    if (arrayTableRowIds != null && arrayTableRowIds.length > 0) {
        String sParentIds = "";
        for (int itr = 0; itr < arrayTableRowIds.length; itr++) {
            if (sParentIds.length() > 0) {
                sParentIds = sParentIds + "~" + arrayTableRowIds[itr];
            }
            else {
                sParentIds = arrayTableRowIds[itr];
            }
        }
        requestMap.put("emxParentIds", sParentIds);
    }
    if (requestMap.containsKey("level")) {
        requestMap.put("level", null);
    }

//  added for consolidated search
    HashMap requestValuesMap = UINavigatorUtil.getRequestParameterValuesMap(request);
    requestMap.put("RequestValuesMap", requestValuesMap);
  // ended for consolidated search


    indentedTableBean.init(context, pageContext, requestMap, timeStamp, userRoleList);
		  
    Map tableControlMap = indentedTableBean.getControlMap(timeStamp);
    
    indentedTableBean.updateRequestAndTableParamsInJson(context, freezePaneLayoutDataBuilder, requestMap, tableControlMap);

	boolean isUserTable = (Boolean) requestMap.get("userTable");
	java.util.List derivedTableNamesList = null;

    if(!isUserTable){
    	String passedTable = (String)requestMap.get("selectedTable");
    	derivedTableNamesList = UITableCustom.getDerivedTableNames(context,passedTable);

    	String accessUsers = MqlUtil.mqlCommand(context, "print table $1 system select $2 dump", passedTable, "property[AccessUsers].value");
    	if(UIUtil.isNotNullAndNotEmpty(accessUsers)) {
    		if(!PersonUtil.hasAnyAssignment(context, accessUsers)) {
    		    return;
    		}
    	}
    }

    requestMap.remove("RootLabel");
    split = (String)requestMap.get("split");
    Vector contentVector = new Vector();
    Element root = new Element("mxRoot");

    //set the root element
    contentVector.add(root);
    Document doc = new Document(contentVector);

    root.addContent(indentedTableBean.createSetting("timeStamp",timeStamp));
    if(derivedTableNamesList!=null && derivedTableNamesList.size()>0) {
    	StringBuffer sb = new StringBuffer();
    	for(int i = 0; i < derivedTableNamesList.size(); i++) {
    		String table = (String)derivedTableNamesList.get(i);
    		if (sb.length() == 0) {
    			sb.append(table);
    		} else {
    			sb.append(",");
    			sb.append(table);
    		}
    	}
    	root.addContent(indentedTableBean.createSetting("derivedTables",sb.toString()));
    }
    root.addContent(indentedTableBean.createSetting("split",split));
    root.addContent(indentedTableBean.createSetting("isUnix",isUnix));
    root.addContent(indentedTableBean.createSetting("isMobile",isMobile));
    root.addContent(indentedTableBean.createSetting("isPCTouch",isPCTouch));

    root.addContent(indentedTableBean.createSetting("isIE",isIE));
    root.addContent(indentedTableBean.createSetting("preProcessJPO",preProcessJPO));
    root.addContent(indentedTableBean.createSetting("preProcessURL",preProcessURL));
    root.addContent(indentedTableBean.createSetting("postProcessJPO",postProcessJPO));
    root.addContent(indentedTableBean.createSetting("postProcessURL",postProcessURL));
    root.addContent(indentedTableBean.createSetting("cancelProcessJPO",cancelProcessJPO));
    root.addContent(indentedTableBean.createSetting("cancelProcessURL",cancelProcessURL));
    root.addContent(indentedTableBean.createSetting("objectString",objectString));
    root.addContent(indentedTableBean.createSetting("objectsString",objectsString));
    root.addContent(indentedTableBean.createSetting("selectedString",selectedString));
    root.addContent(indentedTableBean.createSetting("enterValueString",enterValueString));
    root.addContent(indentedTableBean.createSetting("clearString",clearString));

    //get emxToolbarJavaScript.jsp response here to avoid server side unnecesary round trip
    CharArrayWriterResponse customResponse  = new CharArrayWriterResponse(response);
    StringBuffer toolbarURL = new StringBuffer("emxToolbarJavaScript.jsp?uiType=structureBrowser&timeStamp=");
    
    String  objectId= (String)requestMap.get("objectId");
    toolbarURL.append(timeStamp);
    toolbarURL.append("&AutoFilter=");
    toolbarURL.append(String.valueOf((Boolean)tableControlMap.get("HasAutoFilter")));
    toolbarURL.append("&cellwrap=");
    toolbarURL.append((String)requestMap.get("cellWrap"));
    toolbarURL.append("&IsStructureCompare=");
    toolbarURL.append((String)requestMap.get("IsStructureCompare"));
    toolbarURL.append("&isLayoutRequest=true");
    toolbarURL.append("&targetLocation=");
    toolbarURL.append((String)requestMap.get("targetLocation"));
    toolbarURL.append("&parentOID=");
    toolbarURL.append(objectId);
	toolbarURL.append("&logPerformance=");
    toolbarURL.append(logPerformance);

    request.getRequestDispatcher(toolbarURL.toString()).include(request, customResponse);
    Element toolbarCode = new Element("toolbarCode");
    toolbarCode.setContent(new CDATA(customResponse.toString()));
    root.addContent(toolbarCode);


    //Get properties for thumbnail view
    String thumbnailFieldCount = EnoviaResourceBundle.getProperty(context, "emxFramework.ThumbnailView.fieldCount");
    String tileFieldCount = EnoviaResourceBundle.getProperty(context, "emxFramework.TileView.fieldCount");
    String noImageFile = EnoviaResourceBundle.getProperty(context, "emxFramework.ThumbnailView.NoImageFile");
    String formatThumbnailImage = null;
    String strFormatThumbnailImage = null;
    strFormatThumbnailImage = emxGetParameter(request, "thumbnailFormat");

    try{
    	if(strFormatThumbnailImage == null){
    		strFormatThumbnailImage = EnoviaResourceBundle.getProperty(context, "emxFramework.ThumbnailView.ImageSize");
    	}
    	requestMap.put("thumbnailFormat", strFormatThumbnailImage);

    	formatThumbnailImage = PropertyUtil.getSchemaProperty(context,strFormatThumbnailImage);
    	if("".equals(formatThumbnailImage)) {
    		formatThumbnailImage = "mxMedium Image";
    	}
    }catch(Exception ex){
    	formatThumbnailImage = "mxMedium Image";
    	requestMap.put("thumbnailFormat", "format_mxMediumImage");
    }

    String strImageHeight = PropertyUtil.getAdminProperty(context, "format", formatThumbnailImage, "mxImageSize");

    root.addContent(indentedTableBean.createSetting("thumbnailFieldCount",thumbnailFieldCount));
    root.addContent(indentedTableBean.createSetting("tileFieldCount",tileFieldCount));
    root.addContent(indentedTableBean.createSetting("noImageFile",noImageFile));
    root.addContent(indentedTableBean.createSetting("thumbnailImageHeight",strImageHeight));
    root.addContent(indentedTableBean.createSetting("maxCellsToDraw",indentedTableBean.getMaxCellsToDraw(context, requestMap)));
    root.addContent(indentedTableBean.createSetting("scrollPageSize",indentedTableBean.getScrollPageSize(context, requestMap)));

    //right now assuming the height and width of image are the same.
    root.addContent(indentedTableBean.createSetting("thumbnailImageWidth",strImageHeight));

    //add requestMap
    indentedTableBean.getRequestMap(root, timeStamp);
    //add tableControlMap
    indentedTableBean.getTableControlMap(root, timeStamp);
    //get columns
    MapList columns = indentedTableBean.getColumns(context, root, timeStamp);

    int iRowHeight = 16;
    String rowHeight = (String)requestMap.get("rowHeight");
    if(UIUtil.isNotNullAndNotEmpty(rowHeight)){
        iRowHeight = Integer.parseInt(rowHeight);
    }     
    //root.addContent(indentedTableBean.createSetting("rowHeight",new Integer(iRowHeight + 4).toString()));
  	
    freezePaneLayoutDataBuilder.add("rowHeight",new Integer(iRowHeight).toString());


  StringList incFileList = UITableIndented.getJSValidationFileList(context, suiteKey);
  if ("TRUE".equalsIgnoreCase(isDynamicGridTable))
  {
    StringList incFileGridList = UITableIndented.getJSValidationFileList(context, "Framework", "UIGrid");
    incFileList.addAll(incFileGridList);
  }
  StringList backFileList = UITableIndented.getCallBackFileList(context, suiteKey);

  if(backFileList != null && backFileList.size() > 0)
  {
    StringItr keyItr = new StringItr(backFileList);
    while(keyItr.next())
    {
      String callBackFile = keyItr.obj();
      incFileList.addElement(callBackFile);
    }
  }

  if(incFileList != null && incFileList.size() > 0){
      //add Custom clientside validation includes
      Element custVal = new Element("CustomValidation");
    String fileTok = "";
    for(StringItr keyItr = new StringItr(incFileList); keyItr.next();)
    {
      custVal.addContent(indentedTableBean.createSetting("file",keyItr.obj()));
      customValidationFiles.add(keyItr.obj());
    }
    root.addContent(custVal);
  }


    //add empty row
    root.addContent(new Element("r"));

    ContextUtil.commitTransaction(context);

  	freezePaneLayoutDataBuilder.add("columns", indentedTableBean.getColumnsConfigurationData(columns));

    StringBuilder tempURLParameters = new StringBuilder("uiType=structureBrowser").append("&amp;");
    tempURLParameters.append("timeStamp=").append(timeStamp).append("&amp;");
    tempURLParameters.append("AutoFilter=").append(((Boolean)tableControlMap.get("HasAutoFilter")).toString()).append("&amp;");
    tempURLParameters.append("rowBuffer=").append((String)requestMap.get("rowBuffer")).append("&amp;");

    StringList tempParams = new StringList();
    tempParams.add("header");
    tempParams.add("subHeader");
    tempParams.add("timeStamp");
    tempParams.add("ftsFilters");
    tempParams.add("postDataXML");
    tempParams.add("fieldValues");
    tempParams.add("columnMap");
    tempParams.add("treeLabel");
    
    Iterator itr = requestMap.entrySet().iterator();
    while(itr.hasNext()){
    	Map.Entry keyValuePair = (Map.Entry)itr.next();
    	String paramName = (String)keyValuePair.getKey();
    	if(!tempParams.contains(paramName)){
        	Object objValue = keyValuePair.getValue();
        	if(objValue instanceof String){
    			tempURLParameters.append(paramName).append("=").append(XSSUtil.encodeForURL(context, (String)objValue)).append("&amp;");
        	}
    	}
    }
    tempURLParameters.replace(tempURLParameters.lastIndexOf("&amp;"),tempURLParameters.length(),"");
    String urlParameters =  tempURLParameters.toString();
 	
 	
    freezePaneLayoutDataBuilder.add("isMobile", isMobile);
    freezePaneLayoutDataBuilder.add("isPCTouch", isPCTouch);
    freezePaneLayoutDataBuilder.add("isUnix", isUnix);    
    freezePaneLayoutDataBuilder.add("isIE", isIE);
    freezePaneLayoutDataBuilder.add("timeStamp", timeStamp+"");
    freezePaneLayoutDataBuilder.add("thumbnailFieldCount",thumbnailFieldCount);
    freezePaneLayoutDataBuilder.add("tileFieldCount",tileFieldCount);    
    freezePaneLayoutDataBuilder.add("noImageFile",noImageFile);
    freezePaneLayoutDataBuilder.add("totalRows", "");    
    freezePaneLayoutDataBuilder.add("urlParameters", urlParameters);
    freezePaneLayoutDataBuilder.add("parentOID", XSSUtil.encodeForJavaScript(context, objectId));
    freezePaneLayoutDataBuilder.add("split", XSSUtil.encodeForJavaScript(context, split));
    freezePaneLayoutDataBuilder.add("maxCellsToDraw",indentedTableBean.getMaxCellsToDraw(context, requestMap));
    freezePaneLayoutDataBuilder.add("scrollPageSize",indentedTableBean.getScrollPageSize(context, requestMap));    
    freezePaneLayoutDataBuilder.add("preProcessJPO", XSSUtil.encodeForURL(context,preProcessJPO));
    freezePaneLayoutDataBuilder.add("preProcessURL", XSSUtil.encodeForURL(context,preProcessURL));
    freezePaneLayoutDataBuilder.add("postProcessJPO", XSSUtil.encodeForURL(context,postProcessJPO));
    freezePaneLayoutDataBuilder.add("cancelProcessJPO", XSSUtil.encodeForURL(context,cancelProcessJPO));
    freezePaneLayoutDataBuilder.add("cancelProcessURL", XSSUtil.encodeForURL(context,cancelProcessURL));
    freezePaneLayoutDataBuilder.add("cancelProcessURL", XSSUtil.encodeForURL(context,cancelProcessURL));  
    freezePaneLayoutDataBuilder.add("objectString",objectString);
    freezePaneLayoutDataBuilder.add("objectsString",objectsString);
    freezePaneLayoutDataBuilder.add("selectedString",selectedString);
    freezePaneLayoutDataBuilder.add("enterValueString",enterValueString);
    freezePaneLayoutDataBuilder.add("clearString",clearString);    
    freezePaneLayoutDataBuilder.add("toolbarCode", customResponse.toString());
    freezePaneLayoutDataBuilder.add("logPerformance", logPerformance);
    freezePaneLayoutDataBuilder.add("customValidationFiles", customValidationFiles.build()); 
   
	//JsonObject freezePaneLayoutData2 = freezePaneLayoutDataBuilder.build();
    // fPLayoutDataString = UIUtil.getJSONString(freezePaneLayoutData);
    //JsonObject mergedfreezePaneLayoutData = FrameworkUtil.mergeJsonObject(freezePaneLayoutData,freezePaneLayoutData2); 
    freezePaneLayoutData = freezePaneLayoutDataBuilder.build();
	fPLayoutDataString = UIUtil.getUnescapedJsonString(freezePaneLayoutData);
	
} catch (Exception ex) {
    ContextUtil.abortTransaction(context);
    if (ex.toString() != null && (ex.toString().trim()).length() > 0){
        emxNavErrorObject.addMessage(ex.toString().trim());
        %>
        <%@include file = "emxNavigatorBottomErrorInclude.inc"%>
    <%
    }
}

if((strCustomize!=null && "true".equalsIgnoreCase(strCustomize)) || (strCustomize==null && "enable".equalsIgnoreCase(strCustomizationEnabled))||("".equalsIgnoreCase(strCustomize) && "enable".equalsIgnoreCase(strCustomizationEnabled)))
{
  HashMap hmpRequest = indentedTableBean.getRequestMap(timeStamp);
  String strTableName =(String) hmpRequest.get("selectedTable");
  indentedTableBean.setCurrentTable(context,strTableName);
}

if(isLoggingEnabled) {
	serverTimeLocal=System.currentTimeMillis()-serverTimeLocal;
	HashMap tableData = indentedTableBean.getTableData(timeStamp);
	if(tableData!=null && tableData.get("totalServerTime") != null) {
		serverTimeLocal+=(Long)tableData.get("totalServerTime");
		tableData.put("totalServerTime",serverTimeLocal);
	}
	else {
	   if(tableData!=null) {
		tableData.put("totalServerTime",serverTimeLocal);
	   }			
	}
}
%>
<%-- After this javascript/html code --%>
<%@include  file="emxFreezePaneLayout.inc" %>
