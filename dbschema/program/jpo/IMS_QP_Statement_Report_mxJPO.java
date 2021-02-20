import com.matrixone.apps.common.Person;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class IMS_QP_Statement_Report_mxJPO {
    private static final Logger LOG = Logger.getLogger("reportLogger");
    private final int ROW_LIMIT = 1048575;

    Map contentListDocumentMap;
    Map contentDocumentSetMap;

    public void getReport(Context ctx, String... args) {

        /**statement report generator*/
        //create report
        final DomainObject reportObject = createReportUnit(ctx);

        //get document contents
        systemService(ctx, args);

        //get tasks data
        BusinessObjectWithSelectList reportData = getAllQPTaskList(ctx);

        createBook(ctx, reportData, "stmt_report", reportObject);

    }

    String objectId;
    String reportName;

    public DomainObject createReportUnit(Context ctx) {

        String vault = ctx.getVault().getName();

        DomainObject reportsContainerObject = null, reportObject = null;

        try {
            MapList reportsByType = DomainObject.findObjects(ctx,
                    IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit,
                    IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    "name smatch *stmt*",
                    new StringList(DomainConstants.SELECT_ID));
            int reportsCount = reportsByType.size();

            BusinessObject boReportContainerObject = new BusinessObject(
                    IMS_QP_Constants_mxJPO.type_IMS_QP_Reports, "Reports", "-", ctx.getVault().getName());
            reportsContainerObject = new DomainObject(boReportContainerObject);

            BusinessObject boReportUnit;
            do {
                reportName = "stmt_report_" + ++reportsCount;
                boReportUnit = new BusinessObject(
                        IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit, reportName, "-", vault);
            } while (boReportUnit.exists(ctx));

            reportObject = DomainObject.newInstance(ctx);
            reportObject.createObject(ctx,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit,
                    /*name*/reportName,
                    /*revision*/"-",
                    /*policy*/IMS_QP_Constants_mxJPO.policy_IMS_QP_ReportUnit,
                    /*vault*/ vault);
        } catch (FrameworkException frameworkException) {
            LOG.error("Framework exception: " + frameworkException.getMessage());
            frameworkException.printStackTrace();
        } catch (MatrixException matrixException) {
            LOG.error("Matrix exception: " + matrixException.getMessage());
            matrixException.printStackTrace();
        }

        try {
            if (reportsContainerObject != null && reportObject != null) {
                IMS_KDD_mxJPO.connectIfNotConnected(ctx,
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit, reportsContainerObject, reportObject);
            }
            objectId = reportObject.getId(ctx);
            reportObject.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_FILE_CHECKIN_STATUS, "Not ready yet");
        } catch (Exception e) {
            LOG.error("error connecting: "
                    + IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit + "|" + e.getMessage());
        }

        return reportObject;
    }

    /**
     * @param ctx  usual parameter
     * @param args usual parameter
     */
    public void systemService(Context ctx, String... args) {

        HttpURLConnection connection = null;


        //Http POST request
        String contentListDocument = "";
        String contentDocumentSet = "";
        try {
            Map authorizationParameters = getAuthorizeParameters(ctx);

            //Get all IMS_ListDocuments
            connection = (HttpURLConnection) getConnection(authorizationParameters);
            contentListDocument = findAllListDocuments(connection);
            if (connection != null) {
                connection.disconnect();
            }

            //Get all DocumentSets
            connection = (HttpURLConnection) getConnection(authorizationParameters);
            contentDocumentSet = findAllDocumentSets(connection);
            if (connection != null) {
                connection.disconnect();
            }

        } catch (IOException ioe) {
            LOG.error("IO exception: " + ioe.getMessage());
            ioe.printStackTrace();
        } catch (MatrixException me) {
            LOG.error("Matrix exception: " + me.getMessage());
            me.printStackTrace();
        } catch (Exception e) {
            LOG.error("Exception: " + e.getMessage());
            e.printStackTrace();
        }


        if (UIUtil.isNotNullAndNotEmpty(contentListDocument)) {
            try {
                contentListDocumentMap = jsonArrayToMapMapsByName(new JSONArray(contentListDocument));
                contentListDocumentMap = getLastIndexFiltered(contentListDocumentMap);
            } catch (MatrixException matrixException) {
                LOG.error("matrix exception: " + matrixException.getMessage());
                matrixException.printStackTrace();
            }
        }

        if (UIUtil.isNotNullAndNotEmpty(contentDocumentSet)) {
            try {
                contentDocumentSetMap = jsonArrayToMapMapsById(new JSONArray(contentDocumentSet));
                contentDocumentSetMap = getLanguageFiltered(contentDocumentSetMap);
            } catch (MatrixException matrixException) {
                LOG.error("matrix exception: " + matrixException.getMessage());
                matrixException.printStackTrace();
            }
        }
    }

    private Map<String, Map> getLastIndexFiltered(Map contentListDocumentMap) {
        Map<String, Map> namesMap = new HashMap<>();

        for (Object o : contentListDocumentMap.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) o;
            String docName = entry.getKey();
            if (docName.matches("[.+\\w&]+E$")
                    || docName.matches("[.+\\w&]+S$")
                    || docName.matches("[.+\\w&]+R$")) {
                namesMap.put(docName.substring(0, docName.lastIndexOf(".")), entry.getValue());
            } else {
                namesMap.put(docName, entry.getValue());
            }
        }

        return namesMap;
    }

    private Map<String, Map> getLanguageFiltered(Map contentMap) {

        Map<String, Map> namesMap = new HashMap<>();

        for (Object o : contentMap.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) o;
            Map tempMap = entry.getValue();
            String docName = (String) tempMap.get("name");

            if (docName.matches("[.+\\w&]+E$")) {
                namesMap.put(docName.substring(0, docName.lastIndexOf(".")), tempMap);
            }
        }

        for (Object o : contentMap.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) o;
            Map tempMap = entry.getValue();
            String docName = (String) tempMap.get("name");
            if (docName.matches("[.+\\w&]+S$") &&
                    !namesMap.containsKey(docName.substring(0, docName.lastIndexOf(".")))) {
                namesMap.put(docName.substring(0, docName.lastIndexOf(".")), tempMap);
            }
        }

        for (Object o : contentMap.entrySet()) {
            Map.Entry<String, Map> entry = (Map.Entry<String, Map>) o;
            Map tempMap = entry.getValue();
            String docName = (String) tempMap.get("name");
            if (docName.matches("[.+\\w&]+R$") &&
                    !namesMap.containsKey(docName.substring(0, docName.lastIndexOf(".")))) {
                namesMap.put(docName.substring(0, docName.lastIndexOf(".")), tempMap);
            }
        }

        return namesMap;
    }

    /**
     * Getting all objects type of IMS_QP_QPTask
     *
     * @param ctx usual parameter
     * @return List of business objects
     */
    public BusinessObjectWithSelectList getAllQPTaskList(Context ctx) {

        //Do the query
        BusinessObjectWithSelectList businessObjectList = new BusinessObjectWithSelectList();
        try {
            Query query = getQueryByType(IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask);
            businessObjectList = query.selectTmp(ctx, getBusinessSelect());
        } catch (MatrixException e) {
            LOG.error("Matrix exception: " + e.getMessage());
            e.printStackTrace();
        }
        return businessObjectList;
    }

    /**
     * Method take type and set parameters for query
     *
     * @param type of object for searching
     * @return matrix.db.Query
     */
    private Query getQueryByType(String type) {

        //Prepare temp query
        Query query = new Query();
        query.setBusinessObjectType(type);
        query.setOwnerPattern("*");
        query.setVaultPattern("*");
        query.setWhereExpression("");

        return query;
    }

    /**
     * Getting selections needed
     *
     * @return list of selection strings
     */
    private StringList getBusinessSelect() {
        StringList busSelect = new StringList();
        busSelect.add(DomainConstants.SELECT_ID);
        busSelect.add(DomainConstants.SELECT_NAME);
        busSelect.add(DomainConstants.SELECT_TYPE);
        busSelect.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_QPTASK_2_FACT + ".to.name");
        busSelect.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_QPTASK_2_FACT);
        busSelect.add(IMS_QP_Constants_mxJPO.ATTRIBUTE_DOC_CODE_TO_EXPECTED_RESULT);
        busSelect.add(IMS_QP_Constants_mxJPO.PBS_TYPE_TO_QPTASK);
        busSelect.add(IMS_QP_Constants_mxJPO.PBS_TO_QPTASK);                                                            //pbs
        busSelect.add(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);                                                       //baseline
        busSelect.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM);                                                //system
        busSelect.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM_TYPE);                                           //system type
        busSelect.add(IMS_QP_Constants_mxJPO.PLAN_TO_TASK);                                                             //quality plan
        busSelect.add(IMS_QP_Constants_mxJPO.STAGE_TO_TASK_ID);                                                         //stage_id
        busSelect.add(IMS_QP_Constants_mxJPO.STAGE_TO_TASK);                                                            //stage_name
        busSelect.add(IMS_QP_Constants_mxJPO.STAGE_TO_TASK_TYPE);                                                       //stage_type
        busSelect.add(IMS_QP_Constants_mxJPO.ATTRIBUTE_STAGE_TO_TASK);                                                  //stage_level
        busSelect.add(DomainObject.getAttributeSelect(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu));
        busSelect.add(DomainConstants.SELECT_OWNER);
        busSelect.add("to[IMS_QP_QPlan2QPTask].from.from[IMS_QP_QPlan2Owner].to.name");
        busSelect.add(IMS_QP_Constants_mxJPO.NAME_DEP_TASK_FROM_QP_TASK);

        return busSelect;
    }

    public synchronized void createBook(Context ctx,
                                        BusinessObjectWithSelectList reportData,
                                        String reportType, DomainObject reportObject) {

        //Get report
        SXSSFWorkbook workbook = createReport(ctx, reportData, reportType);

        //Write report to the file and upload file to the Business object
        if (UIUtil.isNotNullAndNotEmpty(reportName) && UIUtil.isNotNullAndNotEmpty(objectId)) {

            IMS_QP_CheckInOutFiles_mxJPO checkInOutFiles = new IMS_QP_CheckInOutFiles_mxJPO();
            File file = checkInOutFiles.writeToFile(ctx, reportName, workbook);
            workbook.dispose();

            boolean checkin = checkInOutFiles.checkIn(ctx, objectId, file);

            try {
                reportObject.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_FILE_CHECKIN_STATUS, "Ready");
            } catch (FrameworkException e) {
                LOG.error("error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private SXSSFWorkbook createReport(Context ctx, BusinessObjectWithSelectList reportData, String sheetName) {

        Map tasksInfo = getTasksObjectsInfo(ctx, reportData);

        SXSSFWorkbook wb = null;
        try {
            wb = new SXSSFWorkbook(new XSSFWorkbook(
                    IMS_QP_Constants_mxJPO.STATEMENT_REPORT_TEMPLATE_PATH), 10000);
        } catch (Exception e) {
            LOG.error("IO exception: " + e.getMessage());
            e.printStackTrace();
        }

        Sheet sheet = wb.getSheet(sheetName);
        int rowNum = sheet.getLastRowNum();
        if (rowNum < 2) rowNum = 2;

        int pointCounter = 1;
        Row row;
        Cell cell;
        for (Object o : contentListDocumentMap.entrySet()) {
            Map.Entry<String, Map<String, String>> entry = (Map.Entry<String, Map<String, String>>) o;
            row = sheet.createRow(rowNum);

            //set number of list document
            cell = row.createCell(0);
            cell.setCellValue(pointCounter);
            pointCounter++;

            //set list doc name
            cell = row.createCell(1);
            cell.setCellValue(entry.getKey());

            Map map2 = (Map) contentDocumentSetMap.get(entry.getKey());
            if (map2 != null) {
                //set doc description
                cell = row.createCell(2);
                cell.setCellStyle(getStyle(wb, "wrap"));
                cell.setCellValue(
                        UIUtil.isNotNullAndNotEmpty((String) map2.get(DomainConstants.SELECT_DESCRIPTION)) ?
                                (String) map2.get(DomainConstants.SELECT_DESCRIPTION) : "-");
                //set task code
                cell = row.createCell(6);
                cell.setCellValue(
                        UIUtil.isNotNullAndNotEmpty((String) map2.get(
                                "to[Task Deliverable].from.attribute[ims_TaskCode]")) ?
                                (String) map2.get("to[Task Deliverable].from.attribute[ims_TaskCode]") : "-");
            }

            //set task names
            MapList mapList = (MapList) tasksInfo.get(entry.getKey());
            if (mapList != null) {

                StringBuilder sbNames = new StringBuilder();
                StringBuilder sbDescriptions = new StringBuilder();
                StringBuilder sbOwners = new StringBuilder();
                StringBuilder sbDepTasks = new StringBuilder();

                for (int i = 0; i < mapList.size(); i++) {
                    Map map = (Map) mapList.get(i);

                    sbNames.append(map.get(DomainConstants.SELECT_NAME));
                    sbDescriptions.append(map.get(DomainConstants.SELECT_DESCRIPTION));

                    String owners = (String) map.get(DomainConstants.SELECT_OWNER);
                    String[] splittedNames = null;
                    if (UIUtil.isNotNullAndNotEmpty(owners)) {
                        splittedNames = owners.split("\u0007");
                    }
                    try {
                        if (splittedNames != null) {
                            for (int j = 0; j < splittedNames.length; j++) {
                                DomainObject personObject = null;
                                if (splittedNames[j] != null) {
                                    personObject = new DomainObject(
                                            new BusinessObject(
                                                    "Person", splittedNames[j], "-", ctx.getVault().getName()));
                                }
                                if (personObject != null) {
                                    String firstName = personObject.getAttributeValue(ctx, "First Name");
                                    String lastName = personObject.getAttributeValue(ctx, "Last Name");
                                    if (!sbOwners.toString().contains(firstName + " " + lastName)) {
                                        sbOwners.append(firstName + " " + lastName);
                                        if (splittedNames.length - j != 1) {
                                            sbOwners.append(",\n");
                                        }
                                    }
                                }
                            }
                        }
                    } catch (FrameworkException frameworkException) {
                        LOG.error("framework exception getting info from Person: " + frameworkException.getMessage());
                        frameworkException.printStackTrace();
                    } catch (MatrixException matrixException) {
                        LOG.error("error getting info from Person: " + matrixException.getMessage());
                        matrixException.printStackTrace();
                    }
                    sbDepTasks.append(map.get("dep"));

                    if (mapList.size() - i != 1) {
                        sbNames.append(",\n");
                        sbDepTasks.append(",\n");
                        sbDescriptions.append(",\n");
                    }
                }

                //set code task
                cell = row.createCell(3);
                cell.setCellStyle(getStyle(wb, "wrap"));
                cell.setCellValue(UIUtil.isNotNullAndNotEmpty(sbNames.toString()) ? sbNames.toString() : "");
                //set dep task code
                cell = row.createCell(4);
                cell.setCellStyle(getStyle(wb, "wrap"));
                cell.setCellValue(UIUtil.isNotNullAndNotEmpty(sbDepTasks.toString()) ? sbDepTasks.toString() : "");
                //set task description
                cell = row.createCell(5);
                cell.setCellStyle(getStyle(wb, "wrap"));
                cell.setCellValue(UIUtil.isNotNullAndNotEmpty(sbDescriptions.toString()) ? sbDescriptions.toString() : "");
                //set owner
                cell = row.createCell(7);
                cell.setCellStyle(getStyle(wb, "wrap"));
                cell.setCellValue(UIUtil.isNotNullAndNotEmpty(sbOwners.toString()) ? sbOwners.toString() : "");
            }

            rowNum++;
        }

        return wb;
    }

    private Map getTasksObjectsInfo(Context ctx, BusinessObjectWithSelectList reportData) {
        Map<String, MapList> tasksInfoByDocCode = new HashMap<>();

        for (Object o : reportData) {
            BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;

            try {
                businessObject.open(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }


            String taskName = businessObject.getSelectData(
                    DomainObject.SELECT_NAME);
            String docNameFromFact = businessObject.getSelectData(
                    IMS_QP_Constants_mxJPO.FROM_IMS_QP_QPTASK_2_FACT + ".to.name");
            String descriptionRu = businessObject.getSelectData(
                    DomainObject.getAttributeSelect(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu));
            String docNameFromExpectedResult = businessObject.getSelectData(
                    IMS_QP_Constants_mxJPO.ATTRIBUTE_DOC_CODE_TO_EXPECTED_RESULT);
            String depTaskName = businessObject.getSelectData(
                    IMS_QP_Constants_mxJPO.NAME_DEP_TASK_FROM_QP_TASK);
            String owner = businessObject.getSelectData(
                    "to[IMS_QP_QPlan2QPTask].from.from[IMS_QP_QPlan2Owner].to.name");

            Map<String, String> tempMap = new HashMap();
            {
                tempMap.put("name", taskName);
                tempMap.put("docFactName", docNameFromFact);
                tempMap.put("description", descriptionRu);
                tempMap.put("owner", owner);
                tempMap.put("dep", depTaskName);
            }

            MapList mapList;
            //if has doc code from expected result
            if (UIUtil.isNotNullAndNotEmpty(docNameFromExpectedResult)) {

                if (tasksInfoByDocCode.containsKey(docNameFromExpectedResult)) {
                    mapList = tasksInfoByDocCode.get(docNameFromExpectedResult);
                } else {
                    mapList = new MapList();
                }

                mapList.add(tempMap);
                tasksInfoByDocCode.put(docNameFromExpectedResult, mapList);
            }

            try {
                businessObject.close(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return tasksInfoByDocCode;
    }

    private CellStyle getStyle(Workbook wb, String styleParam) {
        CellStyle cellStyle = wb.createCellStyle();
        Font font;
        switch (styleParam) {
            case "wrap":
                cellStyle.setWrapText(true);
                break;
            case "wrap8":
                cellStyle.setWrapText(true);
                font = wb.createFont();
                font.setFontHeightInPoints((short) 8);
                cellStyle.setFont(font);
                break;
            case "wrap8red":
                cellStyle.setWrapText(true);
                font = wb.createFont();
                font.setFontHeightInPoints((short) 8);
                font.setColor((short) 10);
                cellStyle.setFont(font);
                break;
        }
        return cellStyle;
    }

    private Map getAuthorizeParameters(Context ctx) throws MatrixException {
        DomainObject domainObject = new DomainObject(
                new BusinessObject("IMS_ExternalSystem", "97", "", ctx.getVault().getName()));
        StringList selectBusStmts = new StringList();
        selectBusStmts.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_EXTERNAL_SYSTEM_URL);
        selectBusStmts.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_EXTERNAL_SYSTEM_USER);
        selectBusStmts.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_EXTERNAL_SYSTEM_PASSWORD);
        return domainObject.getInfo(ctx, selectBusStmts);
    }

    private URLConnection getConnection(Map map) throws Exception {
        String user = (String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_EXTERNAL_SYSTEM_USER);
        String password = FrameworkUtil.decrypt((String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_EXTERNAL_SYSTEM_PASSWORD));

        String url = (String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_EXTERNAL_SYSTEM_URL);
        url = url.replace("https", "http");

        //        Create a URL object
        URL searchUrl = new URL(url + "/remote/businessobject/search");
        //        Open a connection
        HttpURLConnection connection = (HttpURLConnection) searchUrl.openConnection();

        //        Set the request method
        connection.setRequestMethod("POST");
        //        Set the request content-type header parameter
        connection.setRequestProperty("Content-Type", "application/json");

        //        Encode the authorization basic parameters
        String auth = user + ":" + password;
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + new String(encodedAuth));

        //        Set response format type
        connection.setRequestProperty("Accept", "application/json");

        //        Ensure the connection will be used to send content
        connection.setDoOutput(true);

        return connection;
    }

    /**
     * Method POST: request JSON first & JSON response take next
     *
     * @param connection
     * @return string of content response
     * @throws IOException
     */
    private String findAllListDocuments(HttpURLConnection connection) throws IOException {

        //        Create the request body
        String jsonInputString = buildRequestBody(
                "IMS_ListDocument",
                "",
                Arrays.asList(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_TYPE,
                        DomainConstants.SELECT_NAME,
                        DomainConstants.SELECT_REVISION,
                        DomainConstants.SELECT_CURRENT,
                        DomainConstants.SELECT_DESCRIPTION
                ));

        return returnResponse(connection, jsonInputString);
    }

    /**
     * Method POST: request JSON first & JSON response take next
     *
     * @param connection
     * @return string of content response
     * @throws IOException
     */
    private String findAllDocumentSets(HttpURLConnection connection) throws IOException {

        //        Create the request body
        String jsonInputString = buildRequestBody(
                "IMS_DocumentSet",
                "attribute[IMS_IsLast]==true",
                Arrays.asList(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_TYPE,
                        DomainConstants.SELECT_NAME,
                        DomainConstants.SELECT_REVISION,
                        DomainConstants.SELECT_CURRENT,
                        DomainConstants.SELECT_DESCRIPTION,
                        IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_SPF_MASTER_ID,
                        IMS_QP_Constants_mxJPO.TO_IMS_ListDocument2DocSet_FROM_ID,
                        "to[Task Deliverable].from.attribute[ims_TaskCode]"
                ));

        return returnResponse(connection, jsonInputString);
    }

    private String returnResponse(HttpURLConnection connection, String jsonInputString) throws IOException {
        //        write it
        OutputStream os = connection.getOutputStream();
        byte[] input = jsonInputString.getBytes("utf-8");
        os.flush();
        os.write(input, 0, input.length);
        os.close();

        //        Read the response from input stream
        int responseCode = connection.getResponseCode();

        String content = "";
        if (responseCode == 200) {
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = bufferedReader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            bufferedReader.close();
            content = response.toString();
        } else {
            content = "bad response code: " + responseCode;
        }

        return content;
    }

    /**
     * Converter from plain matrix JSON Array to Maps of Map<id, data>
     *
     * @param jsonArray
     * @return list of maps
     * @throws MatrixException
     * @see #jsonObjectToMap
     */
    private Map jsonArrayToMapMapsById(JSONArray jsonArray) throws MatrixException {
        Map<String, Map> map = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Map tempMap = jsonObjectToMap(jsonArray.getJSONObject(i));
            map.put((String) tempMap.get(DomainConstants.SELECT_ID), tempMap);
        }
        return map;
    }

    /**
     * Converter from plain matrix JSON Array to Maps of Map<id, data>
     *
     * @param jsonArray
     * @return list of maps
     * @throws MatrixException
     * @see #jsonObjectToMap
     */
    private Map jsonArrayToMapMapsByName(JSONArray jsonArray) throws MatrixException {
        Map<String, Map> map = new HashMap<>();
        for (int i = 0; i < jsonArray.length(); i++) {
            Map tempMap = jsonObjectToMap(jsonArray.getJSONObject(i));
            map.put((String) tempMap.get(DomainConstants.SELECT_NAME), tempMap);
        }
        return map;
    }

    /**
     * Converter from plain matrix JSON-object to java.util.Map
     *
     * @param jsonObject
     * @return java.util.Map type
     * @throws MatrixException
     */
    private Map jsonObjectToMap(JSONObject jsonObject) throws MatrixException {
        Map map = new HashMap();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jsonObject.getString(key));
        }
        return map;
    }

    /**
     * @param type    type of business object
     * @param where   filter for request
     * @param selects selects for request
     * @return query string for request
     * @throws UnsupportedEncodingException
     */
    private String buildRequestBody(String type, String where, List<String> selects)
            throws UnsupportedEncodingException {

        StringBuilder selectBuilder = new StringBuilder();
        for (String select : selects) {
            if (selectBuilder.length() > 0) {
                selectBuilder.append(",");
            }
            selectBuilder.append(String.format("\"%s\"", select));
        }

        return String.format(
                "{" +
                        "   \"type\": \"%s\"," +
                        "   \"where\": \"%s\"," +
                        "   \"select\": [%s]" +
                        "}",
                URLEncoder.encode(type, "UTF-8"),
                where,
                selectBuilder);
    }
}
