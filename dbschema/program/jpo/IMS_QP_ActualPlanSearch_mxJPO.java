import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.net.util.Base64;
import org.apache.log4j.Logger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class IMS_QP_ActualPlanSearch_mxJPO {
    private static final Logger LOG = Logger.getLogger("plan_search");
    private static Map<String, String> staticResult;
    private static Long TIMER = 0L;

    /**
     * Getting selections needed
     *
     * @return list of selection strings
     */
    private StringList getBusinessSelect() {
        StringList busSelect = new StringList();
        busSelect.addElement(DomainConstants.SELECT_ID);
        busSelect.addElement(DomainConstants.SELECT_NAME);
        busSelect.addElement(DomainConstants.SELECT_TYPE);
        busSelect.addElement(IMS_QP_Constants_mxJPO.FROM_IMS_QP_QPTASK_2_FACT + ".to.name");
        busSelect.addElement(IMS_QP_Constants_mxJPO.FROM_IMS_QP_QPTASK_2_FACT);
        busSelect.addElement(IMS_QP_Constants_mxJPO.PBS_TYPE_TO_QPTASK);
        busSelect.addElement(IMS_QP_Constants_mxJPO.PBS_TO_QPTASK);                                                     //pbs
        busSelect.addElement(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);                                                //baseline

        return busSelect;
    }

    /**
     * Entering point of service
     *
     * @param ctx  usual parameter
     * @param args usual parameter
     * @return Map of results
     */
    public Map searchProcess(Context ctx, String... args) {
        if ((System.currentTimeMillis() - TIMER) / 1000L < 300L) {
            return staticResult;
        } else {
            TIMER = System.currentTimeMillis();
            staticResult = new HashMap<>();
            BusinessObjectWithSelectList businessObjectList = getAllQPTaskList(ctx);                                        //1 get all QP Tasks
            if (businessObjectList != null) {
                getLoopProcess(ctx, businessObjectList);                                                                    //2 loop searching actual plan
            }
        }
        return staticResult;
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
            LOG.error("matrix error: " + e.getMessage());
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
     * Loop searching actual plan
     *
     * @param ctx                usual parameter
     * @param businessObjectList list of business objects
     * @return result map
     */
    private Map<String, String> getLoopProcess(Context ctx, BusinessObjectWithSelectList businessObjectList) {

        String name;
        for (Object o : businessObjectList) {
            BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;
            try {
                businessObject.open(ctx);
            } catch (MatrixException e) {
                LOG.error("error open bo");
                e.printStackTrace();
            }
            String id = businessObject.getSelectData("id");
            name = businessObject.getSelectData("name");

            // Instantiating the BusinessObject
            StringList selectBusStmts = new StringList();
            selectBusStmts.addElement("id");
            selectBusStmts.addElement("type");
            selectBusStmts.addElement("name");

            StringList selectRelStmts = new StringList();
            selectRelStmts.addElement("name");
            selectRelStmts.addElement("from.id");
            selectRelStmts.addElement("from.name");
            selectRelStmts.addElement("to.id");
            short recurse = 1;

            ExpansionWithSelect expansion = null;
            try {
                expansion = businessObject.expandSelect(ctx,
                        "*", "*", selectBusStmts, selectRelStmts, true, true, recurse);
                // Getting the expansion
                //--------------------------------------------------------------
                //  _object.expandSelect(_context, - Java context object
                //  "*",                           - relationship Pattern
                //  "*",                           - type Pattern
                //  selectBusStmts,                - selects for Business Objects
                //  selectRelStmts,                - selects for Relationships
                //  true,                          - get To relationships
                //  true,                          - get From relationships
                //  recurse);                      - recursion level (0 = all)
                //--------------------------------------------------------------

            } catch (MatrixException e) {
                LOG.error("matrix error: " + e.getMessage());
                e.printStackTrace();
            }
            // Getting Relationships
            RelationshipWithSelectList relationshipWithSelectList = expansion.getRelationships();                       //3 search IMS_QP_ExpectedResult2QPTask with direction output
            RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(relationshipWithSelectList);

            // Get each relationship of type IMS_QP_ExpectedResult2QPTask and direction output (from task to ER)
            int counter = 0;
            String expectedResultId = null;
            while (relItr.next()) {
                RelationshipWithSelect relSelect = relItr.obj();
                String relationshipType = relSelect.getSelectData("name");
                String relationshipFromId = relSelect.getSelectData("from.id");
                String relationshipToId = relSelect.getSelectData("to.id");

                //if expected result type
                boolean from = relationshipFromId.equals(id) ? true : false;
                if (relationshipType.equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask) && from) {
                    counter++;
                    expectedResultId = relationshipToId;
                }
            }

            if (counter > 1) {
                staticResult.put(name, "Has more than one expected result");
                continue;                                                                                               //4.1 if more than one object show error
            }
            if (counter < 1) {
                staticResult.put(name, "Has no any expected results");
                continue;
            }

            checkDocumentCode(ctx, businessObject, expectedResultId);

            try {
                businessObject.close(ctx);
            } catch (MatrixException e) {
                LOG.error("error close bo: " + e.getMessage());
                e.printStackTrace();
            }
        }                                                                                                               //11 end of loop
        return staticResult;
    }

    private void checkDocumentCode(Context ctx, BusinessObjectWithSelect businessObject, String expectedResultId) {
        String taskId = businessObject.getSelectData("id");
        String taskName = businessObject.getSelectData("name");
        String documentBaseline = businessObject.getSelectData(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);

        DomainObject object = null;
        String documentName = "";

        try {
            object = new DomainObject(expectedResultId);
            documentName = object.getInfo(ctx, "attribute[IMS_QP_DocumentCode]");
            LOG.info("task name: " + taskName
                    + " expected: " + expectedResultId
                    + " doc name: " + documentName
                    + " (empty: " + documentName.isEmpty() + ")");
        } catch (Exception e) {
            LOG.error("error getting domain object");
            e.printStackTrace();
        }

        if (UIUtil.isNotNullAndNotEmpty(documentName) && documentName.matches("[F][H][1][\\w.&]+")) {             //5 if task has Document Code?
            if (UIUtil.isNotNullAndNotEmpty(documentBaseline) && UIUtil.isNotNullAndNotEmpty(documentName)) {
                String where = String.format("escape name ~~ const'%s*'&&%s==%s&&%s==%s&&%s==%s",
                        documentName,
                        IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name", documentBaseline,
                        "attribute[IMS_IsLast]", "TRUE",
                        IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_IS_LAST_VERSION, "TRUE");
                LOG.info(taskName + " where: " + where);
                documentService(ctx, taskId, taskName, where);
            } else {
                staticResult.put(taskName, "Error getting the baseline of '"
                        + documentName.replaceAll("&+", "*") + "'");
                return;
            }

        } else if (expectedResultId != null) {
            try {
                Map<String, String> infoMap = getWhereDocument(ctx, businessObject, object);
                LOG.info("infoMap: " + infoMap);
                if (infoMap == null || infoMap.isEmpty()) {
                    String baseline = businessObject.getSelectData(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);
                    String pbs = businessObject.getSelectData(IMS_QP_Constants_mxJPO.PBS_TO_QPTASK);
                    String type = businessObject.getSelectData(IMS_QP_Constants_mxJPO.PBS_TYPE_TO_QPTASK);
                    staticResult.put(taskName, "Error occurred while getting some information about the object: "
                            + (baseline.isEmpty() ? "baseline " : "")
                            + (pbs.isEmpty() ? "pbs " : "")
                            + (type.isEmpty() ? "type " : ""));
                    return;
                }

                String resultType = UIUtil.isNotNullAndNotEmpty(infoMap.get("resultType")) ?
                        infoMap.get("resultType") : "";
                LOG.info("result type: " + resultType);
                if (new String(IMS_QP_Constants_mxJPO.ACTUAL_PLAN_TYPES.getBytes("windows-1251"),
                        "UTF-8").contains(resultType)) {                                                     //6
                    staticResult.put(taskName, "Inconsistent type You need to select a plan fact manually");
                    return;
                } else if (new String(IMS_QP_Constants_mxJPO.FAMILY_CL.getBytes("windows-1251"),             //7
                        "UTF-8").equals(resultType)) {
                    DomainObject objectTask = new DomainObject(businessObject);
                    ContextUtil.startTransaction(ctx, true);
                    String connectionState = connectCheckList(ctx, objectTask);                                         //7.1
                    ContextUtil.commitTransaction(ctx);
                    staticResult.put(getColoredString("#8cfab4", taskName),
                            getColoredString("#8cfab4", connectionState));
                    return;
                }
                String where = UIUtil.isNotNullAndNotEmpty(infoMap.get("where")) ? infoMap.get("where") : "";           //8
                LOG.info(taskName + " where: " + where);
                if (!where.equals("")) {
                    documentService(ctx, taskId, taskName, where);
                } else {
                    staticResult.put(taskName, "error when getting query for search document - " + where);
                    return;
                }
            } catch (Exception e) {
                LOG.error("transaction aborted, error code: " + e.getMessage());
                e.printStackTrace();
                ContextUtil.abortTransaction(ctx);
            }
        } else {
            staticResult.put(taskName, "Couldn't get expected result");
            return;
        }
    }

    private String connectCheckList(Context ctx, DomainObject objectTask) {
        String checkList = "IMS_QP_CheckList";
        String externalObjectRelationship = "IMS_QP_QPTask2Fact";

        DomainObject objectDoc;
        Relationship relationship = null;

        try {
            BusinessObject boDocument = new BusinessObject(checkList, "CL-1", "-", ctx.getVault().getName());
            if (!boDocument.exists(ctx)) {
                objectDoc = DomainObject.newInstance(ctx);
                objectDoc.createObject(ctx,
                        /*type*/checkList,
                        /*name*/"CL-1",
                        /*revision*/"-",
                        /*policy*/checkList,
                        /*vault*/ ctx.getVault().getName());
                LOG.info("object document created:: " + objectDoc.getName() + ": " + objectDoc.getId(ctx));
            } else {
                objectDoc = new DomainObject(boDocument);
                LOG.info("object document is exist: " + objectDoc.getName(ctx));
            }

            relationship = IMS_KDD_mxJPO.connectIfNotConnected(ctx, externalObjectRelationship, objectTask, objectDoc);
            objectDoc.setState(ctx, "Approved");
            LOG.info("objects connected: " + objectTask.getName() + "-> " + relationship.getTypeName() + " ->" + objectDoc.getName());
        } catch (Exception e) {
            LOG.error("error connecting: " + relationship + "|" + e.getMessage());
            return "Error connecting: " + relationship + " message: " + e.getMessage();
        }
        return "Checklist " + objectDoc.getName() + "  connected";
    }

    private Map<String, String> getWhereDocument(Context ctx, BusinessObjectWithSelect bo, DomainObject expectedResultObject) throws FrameworkException {
        String documentBaseline = bo.getSelectData(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);
        String documentPBS = bo.getSelectData(IMS_QP_Constants_mxJPO.PBS_TO_QPTASK);
        String documentPBSType = bo.getSelectData(IMS_QP_Constants_mxJPO.PBS_TYPE_TO_QPTASK);
        String rawType = expectedResultObject.getInfo(ctx, IMS_QP_Constants_mxJPO.RESULT_TYPE_TO_EXPECTED_RESULT);
        String family = expectedResultObject.getInfo(ctx, IMS_QP_Constants_mxJPO.FAMILY_TO_EXPECTED_RESULT);
        ByteBuffer byteBuffer = StandardCharsets.UTF_8.encode(rawType);
        String documentType = StandardCharsets.UTF_8.decode(byteBuffer).toString();

        LOG.info("documentBaseline " + documentBaseline
                + ", documentPBS " + documentPBS
                + ", documentType " + documentType);

        String where;
        if (UIUtil.isNotNullAndNotEmpty(documentBaseline)
                && UIUtil.isNotNullAndNotEmpty(documentPBS) && UIUtil.isNotNullAndNotEmpty(documentType)) {
            where = String.format("escape name ~~ const'FH1%s'&&'%s'=='%s'&&'%s'=='%s'&&'%s'=='%s'&&'%s'=='%s'",
                    "*." + family + ".*",                                                                               //Name pattern
                    documentPBSType.equals("IMS_PBSFunctionalArea")
                            ? IMS_QP_Constants_mxJPO.TO_IMS_PBS_2_DOC_SET_FROM + ".name"
                            : IMS_QP_Constants_mxJPO.TO_IMS_REF_CI_2_DOC_FROM + ".name", documentPBS,                                               //PSB
                    "attribute[IMS_IsLast]", "TRUE",
                    IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_IS_LAST_VERSION, "TRUE",
                    IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name", documentBaseline);                                                      //BBS
        } else return new HashMap<>();

        Map map = new HashMap();
        map.put("where", where);
        map.put("resultType", documentType);
        return map;
    }

    private void documentService(Context ctx, String taskId, String taskName, String where) {
        long time = System.currentTimeMillis();
        HttpURLConnection connection = null;

        try {
            Map authorizationParameters = getAuthorizeParameters(ctx);
            connection = (HttpURLConnection) getConnection(authorizationParameters);
            String content = "";

            try {
                content = findExternalObjects(connection, where);                                                       //9 if task has more than one actual plan more than one - select document for connect from QPtask
            } catch (IOException ioe) {
                LOG.error("IO error: " + ioe.getMessage());
                throw new IOException("IO error - Service unavailable: " + ioe.getMessage() + ". Try again later");
            }

            MapList contentMapList = null;
            if (UIUtil.isNotNullAndNotEmpty(content)) {
                contentMapList = jsonArrayToMapList(new JSONArray(content));
            }

            MapList filteredMapList = new MapList();
            if (contentMapList.size() > 0) {

                //filtering by language type
                filteredMapList.addAll(getLanguageList(contentMapList));

                if (filteredMapList.size() > 1) {
                    String rawResult = fillSelect(filteredMapList, where);
                    staticResult.put(
                            getColoredString("#fff0dc", taskName),
                            getColoredString("#fff0dc", "Content for "
                                    + (where.contains("RefCI") ? "System document " : "FunctionalArea document ")
                                    + "contain more than one actual plan - select document for connect from 'result' manually:<br />" + rawResult));
                    return;
                } else if (filteredMapList.size() == 1) {
                    Map map = (Map) filteredMapList.get(0);
                    DomainObject objectTask = new DomainObject(taskId);
                    ContextUtil.startTransaction(ctx, true);
                    DomainObject externalDocument = ensureObject(ctx, map);

                    IMS_KDD_mxJPO.connectIfNotConnected(ctx, "IMS_QP_QPTask2Fact", objectTask, externalDocument);

                    final IMS_ExternalSystem_mxJPO.ExternalSystem externalSystem = new IMS_ExternalSystem_mxJPO.ExternalSystem(ctx, "97");
                    IMS_KDD_mxJPO.connectIfNotConnected(ctx, IMS_ExternalSystem_mxJPO.RELATIONSHIP_IMS_Object2ExternalSystem, externalDocument, externalSystem.getObject());

                    ContextUtil.commitTransaction(ctx);
                    staticResult.put(getColoredString("#8cfab4", taskName),
                            getColoredString("#8cfab4", map.get("name") + " connected"));                    //10 one element connect from QPtask
                    return;
                }
            } else {
                staticResult.put(getColoredString("#faaaaa", taskName),
                        getColoredString("#faaaaa", "Document not found: where " + where.replaceAll("&", "&amp;")));
                return;
            }

        } catch (Exception e) {
            LOG.error("transaction aborted, error message: " + e.getMessage());
            e.printStackTrace();
            ContextUtil.abortTransaction(ctx);
        } finally {
            connection.disconnect();
            time = (System.currentTimeMillis() - time) / 1000L;
            LOG.info("connection took time: " + time + " secs");
        }
    }

    private List<Map> getLanguageList(MapList contentMapList) {
        List<Map> eDocuments = new ArrayList<>();
        List<Map> sDocuments = new ArrayList<>();
        List<Map> rDocuments = new ArrayList<>();

        //check last char in doc name
        for (Object o : contentMapList) {
            Map map = (Map) o;
            String docName = (String) map.get("name");

            if (docName.matches("[.+\\w&]+E$")) eDocuments.add(map);
            if (docName.matches("[.+\\w&]+S$")) sDocuments.add(map);
            if (docName.matches("[.+\\w&]+R$")) rDocuments.add(map);
        }

        if (!eDocuments.isEmpty()) return eDocuments;
        else if (!sDocuments.isEmpty()) return sDocuments;
        else return rDocuments;
    }

    private String fillSelect(MapList filteredMapList, String where) {
        String dropDownDocket = "<select  style=\"background-color:#fff0dc\" >";
        for (Object o : filteredMapList) {
            Map map = (Map) o;
            String pbs = (String) map.get(where.contains("RefCI") ?
                    IMS_QP_Constants_mxJPO.TO_IMS_REF_CI_2_DOC_FROM + ".name" : IMS_QP_Constants_mxJPO.TO_IMS_PBS_2_DOC_SET_FROM + ".name");
            short limiter = 30;
            if (pbs.length() > limiter) {
                pbs = pbs.substring(0, limiter) + "...";
            }
            pbs = pbs.replaceAll("\\u0007", ", ");

            dropDownDocket += new StringBuilder()
                    .append("<option style=\"background-color:#fff0dc\" value=\"")
                    .append(map.get("id"))
                    .append("\">")

                    .append(map.get("name"))
                    .append(" baseline: ").append(map.get(IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name"))
                    .append(" pbs: ").append(pbs)
                    .append(" rev.: ").append(map.get("attribute[IMS_SPFMajorRevision]"))
                    .append("_").append(map.get("attribute[IMS_SPFDocVersion]"))
                    .append("</option>")
                    .toString();
        }
        dropDownDocket = dropDownDocket.replaceAll("&+", "*");
        dropDownDocket += "</select>";
        return dropDownDocket;
    }

    private String getColoredString(String color, String text) {
        text = text.replaceAll("&", "&amp;");
        return new StringBuilder()
                .append("<p style=\"background-color:" + color + "\">")
                .append(text)
                .append("</p>")
                .toString();
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
     * @param where      query where excluding unnecessary
     * @return string of content response
     * @throws IOException
     */
    private String findExternalObjects(HttpURLConnection connection, String where) throws IOException {

//        Create the request body
        String jsonInputString = buildRequestBody(
                "IMS_DocumentSet",
                where,
                Arrays.asList(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_TYPE,
                        DomainConstants.SELECT_NAME,
                        DomainConstants.SELECT_REVISION,
                        DomainConstants.SELECT_POLICY,
                        DomainConstants.SELECT_CURRENT,
                        DomainConstants.SELECT_DESCRIPTION,
                        DomainObject.getAttributeSelect("IMS_SPFDocVersion"),
                        DomainObject.getAttributeSelect("IMS_SPFMajorRevision"),
                        DomainObject.getAttributeSelect("IMS_ProjDocStatus"),
                        DomainObject.getAttributeSelect("IMS_Frozen"),
                        IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name",
                        IMS_QP_Constants_mxJPO.TO_IMS_PBS_2_DOC_SET_FROM + ".name",
                        IMS_QP_Constants_mxJPO.TO_IMS_REF_CI_2_DOC_FROM + ".name"
                ));
//        write it
        OutputStream os = connection.getOutputStream();
        byte[] input = jsonInputString.getBytes("utf-8");
        os.flush();
        os.write(input, 0, input.length);
        os.close();

//        Read the response from input stream
        int responseCode = connection.getResponseCode();
        LOG.info("responseCode: " + responseCode);

        String content = "";
        if (responseCode == 200) {
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));

            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = br.readLine()) != null) {
                response.append(responseLine.trim());
            }
            br.close();
            content = response.toString();
        }
        return content;
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

    /**
     * Converter from plain matrix JSON Array to list of Maps
     *
     * @param jsonArray
     * @return list of maps
     * @throws MatrixException
     * @see #jsonObjectToMap
     */
    private MapList jsonArrayToMapList(JSONArray jsonArray) throws MatrixException {
        MapList mapList = new MapList();
        for (int i = 0; i < jsonArray.length(); i++) {
            mapList.add(jsonObjectToMap(jsonArray.getJSONObject(i)));
            if (i == 1000) {
                break;
            }
        }
        return mapList;
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
     * Explains the ensure object
     *
     * @param context usual parameter
     * @param map     contains all needs attributes for filling the instance
     * @return Domain Object for connecting to QP plan
     * @throws Exception
     */
    private DomainObject ensureObject(Context context, Map map) throws Exception {

        String ATTRIBUTE_IMS_ExternalObjectId = "IMS_ExternalObjectId";
        String ATTRIBUTE_IMS_ExternalObjectPolicy = "IMS_ExternalObjectPolicy";
        String ATTRIBUTE_IMS_ExternalObjectState = "IMS_ExternalObjectState";
        String ATTRIBUTE_IMS_ExternalObjectType = "IMS_ExternalObjectType";
        String INTERFACE_IMS_ExternalObject = "IMS_ExternalObject";
        String TYPE_IMS_ExternalDocumentSet = "IMS_ExternalDocumentSet";
        String POLICY_IMS_ExternalObject = "IMS_ExternalObject";
        String ATTRIBUTE_IMS_SPFMajorRevision = "IMS_SPFMajorRevision";
        String ATTRIBUTE_IMS_SPFDocVersion = "IMS_SPFDocVersion";
        String ATTRIBUTE_IMS_ProjDocStatus = "IMS_ProjDocStatus";
        String ATTRIBUTE_IMS_Frozen = "IMS_Frozen";

        DomainObject object = new DomainObject(new BusinessObject(TYPE_IMS_ExternalDocumentSet,
                (String) map.get(DomainConstants.SELECT_NAME),
                (String) map.get(DomainConstants.SELECT_REVISION),
                context.getVault().getName()));

        if (!object.exists(context)) {
            object.create(context, POLICY_IMS_ExternalObject);
            object.addBusinessInterface(context, new BusinessInterface(INTERFACE_IMS_ExternalObject, context.getVault()));
            LOG.info("object document created: " + object.getName(context));
        }

        object.setDescription(context, (String) map.get(DomainConstants.SELECT_DESCRIPTION));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectId, (String) map.get(DomainConstants.SELECT_ID));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectType, (String) map.get(DomainConstants.SELECT_TYPE));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectPolicy, (String) map.get(DomainConstants.SELECT_POLICY));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectState, (String) map.get(DomainConstants.SELECT_CURRENT));

        object.setAttributeValue(context, ATTRIBUTE_IMS_SPFMajorRevision, (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFMajorRevision)));
        object.setAttributeValue(context, ATTRIBUTE_IMS_SPFDocVersion, (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFDocVersion)));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ProjDocStatus, (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_ProjDocStatus)));
        object.setAttributeValue(context, ATTRIBUTE_IMS_Frozen, (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Frozen)));

        object.setAttributeValue(context, "IMS_BBSMajor", (String) map.get(IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name"));

        return object;
    }

    public MapList getAllTasksForTable(Context ctx, String... args) {
        MapList allTasks = new MapList();
        try {
            allTasks = DomainObject.findObjects(ctx,/*type*/ "IMS_QP_QPTask",/*vault*/"eService Production", /*where*/null,/*selects*/ new StringList("id"));
        } catch (FrameworkException fe) {
            LOG.error("error getting tasks: " + fe.getMessage());
            fe.printStackTrace();
        }
        return allTasks;
    }

    /**
     * @param m       set map for filling to the Vector
     * @param trigger showing witch column needs filling
     * @return vector is result of filling
     */
    private Vector fillVector(Map m, String trigger) {
        Vector vResult = new Vector();

        if (m != null && !m.isEmpty())
            for (Object o : m.entrySet()) {
                Map.Entry entry = (Map.Entry) o;

                vResult.add(trigger.equals("key") ?
                        entry.getKey() : entry.getValue());
            }
        return vResult;
    }

    /**
     * @param ctx  usual parameter
     * @param args usual parameter
     * @return list of values for 'Code' column
     */
    public Vector getInfoNameTask(Context ctx, String... args) {
        Map map = new HashMap();
        if (staticResult != null) {
            map = staticResult;
        }

        return fillVector(map, "key");
    }

    /**
     * @param ctx  usual parameter
     * @param args usual parameter
     * @return list of values for 'State' column
     */
    public Vector getInfoStateTask(Context ctx, String... args) {
        Map map = new HashMap();
        if (staticResult != null) {
            map = staticResult;
        }

        return fillVector(map, "value");
    }
}
