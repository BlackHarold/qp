import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
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
import java.nio.charset.StandardCharsets;
import java.util.*;

public class IMS_QP_ActualPlanSearch_mxJPO {
    private static final Logger LOG = Logger.getLogger("plan_search");
    private Map<String, String> result;

    /**
     * Entering point of service
     *
     * @param ctx usual parameter
     * @return Map of results
     */
    public void searchProcess(Context ctx, String... args) {                                                            //0 entry point

        BusinessObjectWithSelectList businessObjectList = getAllQPTaskList(ctx);                                        //1 get all QP Tasks
        if (businessObjectList != null) {
            Map<String, String> rawMap = getLoopProcess(ctx, businessObjectList);                                       //2 loop searching actual plan
            StringBuilder stringBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : rawMap.entrySet()) {
                stringBuilder
                        .append("<p ")
                        .append(entry.getKey())
                        .append(" : ")
                        .append(entry.getValue())
                        .append("</p>");
            }
            System.out.println(stringBuilder.toString());
        }

        /**report generator*/
        Map<String, BusinessObjectWithSelectList> reportData =
                new IMS_QP_ActualPlanSearchGenerator_mxJPO().reportGeneration(ctx);

        /**report write to xlsx*/
        new IMS_QP_ActualPlanSearchReport_mxJPO().main(ctx, reportData, args);
    }

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
        busSelect.addElement(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM);                                         //system
        busSelect.addElement(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM_TYPE);                                    //system type
        busSelect.addElement(IMS_QP_Constants_mxJPO.PLAN_TO_TASK);                                                      //quality plan
        busSelect.addElement(IMS_QP_Constants_mxJPO.STAGE_TO_TASK_ID);                                                  //stage_id
        busSelect.addElement(IMS_QP_Constants_mxJPO.STAGE_TO_TASK);                                                     //stage_name
        busSelect.addElement(IMS_QP_Constants_mxJPO.STAGE_TO_TASK_TYPE);                                                //stage_type
        busSelect.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_STAGE_TO_TASK);                                           //stage_level

        return busSelect;
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
            System.out.println("matrix error: " + e.getMessage());
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
        result = new HashMap<>();
        String name;

        for (Object o : businessObjectList) {
            BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;

            try {
                businessObject.open(ctx);
            } catch (MatrixException e) {
                System.out.println("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }

            String id = businessObject.getSelectData("id");
            name = businessObject.getSelectData("name");
            System.out.println("=== " + name + " ===");

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
            selectRelStmts.addElement("to.name");
            short recurse = 1;

            ExpansionWithSelect expansion = null;
            try {
                expansion = businessObject.expandSelect(ctx,
                        "*", "*", selectBusStmts, selectRelStmts, true, true, recurse);
                // Getting the expansion
                //--------------------------------------------------------------
                //  _object.expandSelect(_ctx, - Java ctx object
                //  "*",                           - relationship Pattern
                //  "*",                           - type Pattern
                //  selectBusStmts,                - selects for Business Objects
                //  selectRelStmts,                - selects for Relationships
                //  true,                          - get To relationships
                //  true,                          - get From relationships
                //  recurse);                      - recursion level (0 = all)
                //--------------------------------------------------------------

            } catch (MatrixException e) {
                System.out.println("matrix error: " + e.getMessage());
                e.printStackTrace();
            }
            // Getting Relationships
            RelationshipWithSelectList relationshipWithSelectList = expansion.getRelationships();
            RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(relationshipWithSelectList);

            // Get each relationship of type IMS_QP_ExpectedResult2QPTask and direction output (from task to ER)
            int counter = 0;
            String expectedResultId = null;
            String straightToSystemId = "empty_system_id", parentQualityPlanId = "empty_system_id";

            while (relItr.next()) {
                RelationshipWithSelect relSelect = relItr.obj();
                String relationshipType = relSelect.getSelectData("name");
                String relationshipFromId = relSelect.getSelectData("from.id");
                String relationshipToId = relSelect.getSelectData("to.id");

                //if expected result type
                boolean from = relationshipFromId.equals(id);
                if (relationshipType.equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask) && from) {//3 search IMS_QP_ExpectedResult2QPTask with direction output
                    counter++;
                    expectedResultId = relationshipToId;
                }
            }

            if (counter > 1) {
                result.put(getColoredString("''", name), "Task has more than one expected result (code 5.1)");
                continue;                                                                                               //5.1.1 (4.1) if more than one object show error
            }

            if (counter < 1) {
                result.put(getColoredString("''", name), "Task has no any expected results");                     //5.1.1 (4.1) if more than one object show error
                continue;
            }

            DomainObject expectedResult = null;
            try {
                expectedResult = new DomainObject(expectedResultId);
            } catch (Exception e) {
                System.out.println("error getting domain object: " + expectedResultId);
                e.printStackTrace();
            }

            checkResultTypeAndFoundDocument(ctx, businessObject, expectedResult);                                                       //5 check points 5.3 5.4.1 5.5.1

            try {
                businessObject.close(ctx);
            } catch (MatrixException e) {
                System.out.println("error close bo: " + e.getMessage());
                e.printStackTrace();
            }
        }                                                                                                               //11 end of loop
        return result;
    }

    /**
     * @param ctx            usual parameter
     * @param businessObject the Task object from loop for getting parameters
     * @param expectedResult the Expected Result object to chek in the method
     */
    private void checkResultTypeAndFoundDocument(Context ctx, BusinessObjectWithSelect businessObject, DomainObject expectedResult) {
        try {
            String resultType = expectedResult.getInfo(ctx, IMS_QP_Constants_mxJPO.RESULT_TYPE_TO_EXPECTED_RESULT);
            String taskName = businessObject.getSelectData("name");
            String documentName = expectedResult.getAttributeValue(ctx, "IMS_QP_DocumentCode");
            System.out.println("check result type encoding: " + resultType + " document code: |" + documentName + "|");
            boolean needsCheckDoc = true;

            boolean hasConnectedDocument = "TRUE".equals(businessObject.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_QPTASK_2_FACT));
            boolean hasDocumentCode = UIUtil.isNotNullAndNotEmpty(documentName);
            System.out.println("task2fact: " + hasConnectedDocument + " doc name: " + hasDocumentCode);

            if (!hasConnectedDocument &&
                    IMS_QP_Constants_mxJPO.ANOTHER_PLAN_TYPES.equals(resultType)) {                                     //5.3 (6) check if type is 'Другое'
                result.put(getColoredString("''", taskName), String.format(
                        "Inconsistent type: %s, needs to select a plan fact manually (code 5.3)",
                        IMS_QP_Constants_mxJPO.ANOTHER_PLAN_TYPES));

                needsCheckDoc = false;
                System.out.println("5.3 Another plan type: no needs check doc");

            } else if (IMS_QP_Constants_mxJPO.FAMILY_CL.equals(resultType)) {                                           //5.4.1 (7.1) create check-list
                if (!hasConnectedDocument) {
                    DomainObject objectTask = new DomainObject(businessObject);
                    ContextUtil.startTransaction(ctx, true);
                    String connectionState = connectCheckList(ctx, objectTask);
                    ContextUtil.commitTransaction(ctx);
                    result.put(getColoredString("#8cfab4", taskName), connectionState);
                }

                needsCheckDoc = false;
                System.out.println("5.4.1 CL Family: no needs check doc");
            }

            //Main method to find documents
            boolean docFounded = false;
            if (needsCheckDoc) {
                docFounded = checkDocumentCode(ctx, businessObject, documentName, expectedResult);
            }

            if (!docFounded && !hasDocumentCode && !hasConnectedDocument
                    && (IMS_QP_Constants_mxJPO.VTZ_PLAN_TYPES.equals(resultType))) {                                    //5.5.1 create VTZ
                DomainObject objectTask = new DomainObject(businessObject);
                ContextUtil.startTransaction(ctx, true);
                String connectionState = connectVTZType(ctx, objectTask);
                ContextUtil.commitTransaction(ctx);
                result.put(getColoredString("#8cfab4", taskName), connectionState);
                System.out.println("VTZ family: " + connectionState);
            }

        } catch (FrameworkException fe) {
            System.out.println("method checkResultType has framework error: " + fe.getMessage());
            fe.printStackTrace();
        }
    }

    /**
     * @param ctx            usual parameter
     * @param businessObject the Business object for document searching
     * @param documentName   the Document name from related Expected Result object attribute
     * @param expectedResult himself Expected result object
     * @return result of found and connect document
     */
    private boolean checkDocumentCode(Context ctx,
                                      BusinessObjectWithSelect businessObject,
                                      String documentName,
                                      DomainObject expectedResult) {

        String taskId = businessObject.getSelectData("id");
        String taskName = businessObject.getSelectData("name");
        String taskType = businessObject.getSelectData(IMS_QP_Constants_mxJPO.PBS_TYPE_TO_QPTASK);
        String documentPBS = businessObject.getSelectData(IMS_QP_Constants_mxJPO.PBS_TO_QPTASK);
        String documentBaseline = businessObject.getSelectData(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);
        String documentStage = businessObject.getSelectData(IMS_QP_Constants_mxJPO.STAGE_TO_TASK);
        String documentStageLevel = businessObject.getSelectData(IMS_QP_Constants_mxJPO.ATTRIBUTE_STAGE_TO_TASK);
        String where;
        boolean docFounded = false;

        try {
            System.out.println("params {id=" + taskId + ", name=" + taskName + ", document_bbs=" + documentBaseline
                    + ", doc_name=" + documentName);

            if (UIUtil.isNotNullAndNotEmpty(documentName) && !documentName.matches("[F][H][1][\\w.&]+")) {
                String errorDocCode = expectedResult.getAttributeValue(ctx, "IMS_QP_DocumentCode");
                errorDocCode = "Wrong code: " + errorDocCode.replaceAll("Wrong code: ", "");
                expectedResult.setAttributeValue(ctx, "IMS_QP_DocumentCode", errorDocCode);
                result.put(getColoredString("''", taskName), errorDocCode);
                return false;

            } else if (UIUtil.isNotNullAndNotEmpty(documentName) && documentName.matches("[F][H][1][\\w.&]+")) {  //5.2 (5) if task has Document Code?

                if (UIUtil.isNotNullAndNotEmpty(documentBaseline)) {
                    StringBuilder builder = new StringBuilder();

                    builder.append(String.format("escape name ~~ const'%s*'", documentName));

                    builder.append(String.format("&&'%s'=='%s'",
                            IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name", documentBaseline));

                    if (UIUtil.isNotNullAndNotEmpty(documentStage) && "BD".equals(documentStage)) {                       //6.1 Stage
                        builder.append(String.format("&&'%s'=='%s'",
                                "to[Task Deliverable].from.attribute[IMS_BDStage]", documentStageLevel));
                    }

                    where = builder.toString();                                                                         //5.2.1 (5.1) search document by code & baseline

                } else {
                    result.put(getColoredString("''", taskName), "Error getting the baseline of '"
                            + documentName.replaceAll("&+", "*") + "' (5.2.1)");
                    return false;
                }

            } else {
                Map<String, String> infoMap = null;
                try {
                    infoMap = getWhereDocument(ctx, businessObject, expectedResult);                                    //4 & 6
                } catch (FrameworkException frameworkException) {
                    frameworkException.printStackTrace();
                }

                if (infoMap.isEmpty()) {
                    result.put(getColoredString("''", taskName), "Error occurred while getting some information about the object: "
                            + (UIUtil.isNotNullAndNotEmpty(documentBaseline) ? "baseline " : "")
                            + (UIUtil.isNotNullAndNotEmpty(documentPBS) ? "pbs " : "")
                            + (UIUtil.isNotNullAndNotEmpty(taskType) ? "type " : "")
                            + (UIUtil.isNotNullAndNotEmpty(documentStage) ? "stage" : "")
                    );
                    return false;
                }
                where = infoMap.get("where");
            }

            if (UIUtil.isNotNullAndNotEmpty(where)) {
                System.out.println(taskName + " where: " + where);
                docFounded = documentService(ctx, businessObject, where, expectedResult);                               //7 (8)
            }

        } catch (Exception e) {
            System.out.println("transaction aborted: " + e.getMessage());
            e.printStackTrace();
        }
        return docFounded;
    }

    /**
     * @param ctx            usual parameter
     * @param businessObject the Business object for manipulations
     * @param where          the query where
     * @param expectedResult the related Expected Result object
     */
    private boolean documentService(Context ctx,
                                    BusinessObjectWithSelect businessObject,
                                    String where,
                                    DomainObject expectedResult) {

        HttpURLConnection connection = null;
        MapList contentMapList = new MapList();
        String taskName = businessObject.getSelectData("name");

        DomainObject objectTask = new DomainObject(businessObject);

        //Http POST request
        String content = null;
        try {
            Map authorizationParameters = getAuthorizeParameters(ctx);
            connection = (HttpURLConnection) getConnection(authorizationParameters);

        } catch (Exception e) {
            System.out.println("io error: " + e.getMessage());
            e.printStackTrace();
        }
        try {
            content = findExternalObjects(connection, where);
        } catch (IOException ioe) {
            System.out.println("IO error: " + ioe.getMessage());
            ioe.printStackTrace();
        }
        if (UIUtil.isNotNullAndNotEmpty(content)) {
            try {
                contentMapList = jsonArrayToMapList(new JSONArray(content));
            } catch (MatrixException matrixException) {
                System.out.println("matrix exception: " + matrixException.getMessage());
                matrixException.printStackTrace();
            }
        }

        MapList filteredMapList = new MapList();
        boolean docFounded = false;
        if (contentMapList.size() > 0) {
            docFounded = true;

            //filtering by language type                                                                                //8.1 8.1.1 8.1.2
            filteredMapList.addAll(getLanguageList(contentMapList));
            filteredMapList = sortMapListByRevisionAndVersion(filteredMapList);
            System.out.println("8.1: " + taskName + "|" + filteredMapList);

            if (filteredMapList.size() > 1) {                                                                           //8 (9) if task has more than one actual plan more than one - select document for connect from QPtask
                String taskId = "";
                try {
                    taskId = objectTask.getId(ctx);
                    System.out.println("setting attribute IMS_QP_SelectDocument for " + taskName);
                    if ("FALSE".equals(objectTask.getInfo(ctx, "from[IMS_QP_QPTask2Fact]"))) {
                        objectTask.setAttributeValue(ctx,
                                "IMS_QP_SelectDocument", fillSelect(filteredMapList, taskId));
                    }
                } catch (FrameworkException fe) {
                    System.out.println("error setting attribute IMS_QP_SelectDocument for " + taskName);
                }
                String rawResult = fillSelect(filteredMapList, taskId);
                System.out.println("8: " + taskName + "|" + rawResult);

                result.put(
                        getColoredString("#fff0dc", taskName), rawResult);


            } else if (filteredMapList.size() == 1) {

                Map map = (Map) filteredMapList.get(0);

                //check if task has same connection to the document
                try {
                    ContextUtil.startTransaction(ctx, true);
                } catch (FrameworkException fe) {
                    System.out.println("start transaction error: " + fe.getMessage());
                }

                boolean equalDocument;
                try {
                    String existedConnectionToExternalDocumentId = objectTask.getInfo(ctx,
                            "from[IMS_QP_QPTask2Fact].id");
                    String existedConnectionToExternalDocumentRevision = objectTask.getInfo(ctx,
                            "from[IMS_QP_QPTask2Fact].to.revision");

                    System.out.println(
                            "existedConnectionToExternalDocumentRevision: " + existedConnectionToExternalDocumentRevision);

                    if (UIUtil.isNotNullAndNotEmpty(existedConnectionToExternalDocumentRevision)) {
                        //check equals document revisions
                        equalDocument = existedConnectionToExternalDocumentRevision.equals(map.get("revision"));
                        System.out.println("is documents similar and already connected? " + equalDocument);

                        //delete connection if existed same
                        if (!equalDocument) {
//                            MqlUtil.mqlCommand(ctx, "delete connection " + existedConnectionToExternalDocumentId);
                            DomainRelationship.disconnect(ctx, existedConnectionToExternalDocumentId);
                            System.out.println("documents are not similar: '"
                                    + existedConnectionToExternalDocumentRevision + "' ! '" + map.get("revision") + "'");
                        }
                    }
                } catch (FrameworkException frameworkException) {
                    System.out.println("framework exception: " + frameworkException.getMessage());
                    frameworkException.printStackTrace();
                }

                DomainObject externalDocument = ensureObject(ctx, map);                                                 //9.1 create document
                System.out.println("externalDocument: " + externalDocument);

                try {
                    Relationship relationship = IMS_KDD_mxJPO.connectIfNotConnected(ctx,                                //9.2 connect external doc with task
                            "IMS_QP_QPTask2Fact", objectTask, externalDocument);

                    if (relationship != null) {
                        System.out.println("new connection between: " + objectTask.getName(ctx)
                                + " -> " + relationship.getTypeName()
                                + " -> " + externalDocument.getName(ctx));
                    }
                } catch (Exception e) {
                    System.out.println("connect object error: " + e.getMessage());
                    e.printStackTrace();
                }

                try {
                    String docName = externalDocument.getName(ctx);
                    expectedResult.setAttributeValue(ctx,                                                               //9.3 write the document code to the attribute
                            "IMS_QP_DocumentCode", docName.substring(0, docName.lastIndexOf(".")));
                    System.out.println("9.1: " + externalDocument.getName(ctx) + "| 9.2: connected "
                            + objectTask.getName(ctx) + "| 9.3: code written " + expectedResult.getName(ctx));
                } catch (FrameworkException fe) {
                    System.out.println("framework exception: " + fe.getMessage());
                }

                try {
                    final IMS_ExternalSystem_mxJPO.ExternalSystem externalSystem =
                            new IMS_ExternalSystem_mxJPO.ExternalSystem(ctx, "97");
                    IMS_KDD_mxJPO.connectIfNotConnected(ctx,
                            IMS_ExternalSystem_mxJPO.RELATIONSHIP_IMS_Object2ExternalSystem,
                            externalDocument,
                            externalSystem.getObject());
                } catch (Exception e) {
                    System.out.println("external systems error: " + e.getMessage());
                }

                try {
                    ContextUtil.commitTransaction(ctx);
                } catch (FrameworkException fe) {
                    System.out.println("commit transaction error: " + fe.getMessage());
                }

                result.put(getColoredString("#8cfab4", taskName), map.get("name") + " connected");                //10 one element connect from QPtask
            }
        } else {
            result.put(getColoredString("#faaaaa", taskName), "Document not found: where " + where);
        }

        if (connection != null) {
            connection.disconnect();
        }
        return docFounded;
    }

    /**
     * @param ctx                  usual parameter
     * @param bo                   the Business object for getting data about himself
     * @param expectedResultObject
     * @return map with stage, result type, where parameters
     * @throws FrameworkException
     */
    private Map<String, String> getWhereDocument(Context ctx,
                                                 BusinessObjectWithSelect bo,
                                                 DomainObject expectedResultObject) throws FrameworkException {

        String documentBaseline = bo.getSelectData(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);

        String documentPBS = UIUtil.isNotNullAndNotEmpty(
                bo.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM)) ?
                bo.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM) :                                    //4.1-4.2
                bo.getSelectData(IMS_QP_Constants_mxJPO.PBS_TO_QPTASK);

        if (UIUtil.isNullOrEmpty(documentPBS)) {
            documentPBS = "";
        }

        String documentPBSType = UIUtil.isNotNullAndNotEmpty(
                bo.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM_TYPE)) ?
                bo.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM_TYPE) :
                bo.getSelectData(IMS_QP_Constants_mxJPO.PBS_TYPE_TO_QPTASK);

        documentPBSType = documentPBSType.equals("IMS_PBSFunctionalArea") ?
                IMS_QP_Constants_mxJPO.TO_IMS_PBS_2_DOC_SET_FROM + ".name"
                : IMS_QP_Constants_mxJPO.TO_IMS_REF_CI_2_DOC_FROM + ".name";

        String family = expectedResultObject.getInfo(ctx, IMS_QP_Constants_mxJPO.FAMILY_TO_EXPECTED_RESULT);
        String documentType = expectedResultObject.getInfo(ctx, IMS_QP_Constants_mxJPO.RESULT_TYPE_TO_EXPECTED_RESULT);

        String stage = bo.getSelectData(IMS_QP_Constants_mxJPO.STAGE_TO_TASK);
        String stageLevel = bo.getSelectData(IMS_QP_Constants_mxJPO.ATTRIBUTE_STAGE_TO_TASK);

        String where = "";

        //pbs is empty
        if (UIUtil.isNotNullAndNotEmpty(documentBaseline) && UIUtil.isNullOrEmpty(documentPBS)) {
            where = String.format("escape name ~~ const'FH1%s'&&'%s'=='%s'&&('%s'=='%s'||'%s'=='%s')",
                    "*." + family + ".*",
                    IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name", documentBaseline,                            //BBS
                    "to[IMS_PBS2DocSet].from.name", "Architecture level",                                               //PSB 4.2.1
                    "to[IMS_PBS2DocSet].from.name", "Plant level"
            );

        } else if (UIUtil.isNotNullAndNotEmpty(documentBaseline) && UIUtil.isNotNullAndNotEmpty(documentPBS)) {
            where = String.format("escape name ~~ const'FH1%s'&&'%s'=='%s'&&'%s'=='%s'",
                    "*." + family + ".*",
                    documentPBSType, documentPBS,                                                                       //PSB 4.2.2
                    IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name", documentBaseline                             //BBS
            );
        }

        StringBuilder whereBuilder = new StringBuilder(where);
        if (UIUtil.isNotNullAndNotEmpty(stage) && "BD".equals(stage)) {                                                 //6.1, 6.1.2 If stage equals 'BD', the stage level in used
            whereBuilder.append(String.format("&&'%s'=='%s'",
                    "to[Task Deliverable].from.attribute[IMS_BDStage]", stageLevel));
        }

        Map map = new HashMap();
        map.put("where", whereBuilder.toString());
        map.put("resultType", documentType);
        map.put("stage", stageLevel);

        System.out.println("documentBaseline: " + documentBaseline
                + ", documentPBS: " + documentPBS
                + ", documentType: " + documentType
                + ", documentStage: " + stage
                + ", level: " + stageLevel
                + ", result type: " + documentType);

        return map;
    }

    private MapList sortMapListByRevisionAndVersion(MapList filteredMapList) {
        Map<String, Map> tempMap = new HashMap();

        for (Object o : filteredMapList) {
            Map map1 = (Map) o;
            String name = (String) map1.get("name");

            if (tempMap.containsKey(name)) {
                Map map2 = tempMap.get(name);
                int r1 = Integer.parseInt((String) map1.get("attribute[IMS_SPFMajorRevision]"));
                int r2 = Integer.parseInt((String) map2.get("attribute[IMS_SPFMajorRevision]"));
                if (r1 > r2) {
                    tempMap.put(name, map1);
                    continue;
                }

                if (r1 == r2) {
                    int v1 = Integer.parseInt((String) map1.get("attribute[IMS_SPFDocVersion]"));
                    int v2 = Integer.parseInt((String) map2.get("attribute[IMS_SPFDocVersion]"));
                    if (v1 > v2) {
                        tempMap.put(name, map1);
                    }
                    continue;
                }
            } else {
                tempMap.put(name, map1);
            }
        }

        filteredMapList.clear();
        for (Map.Entry entry : tempMap.entrySet()) {
            filteredMapList.add(entry.getValue());
        }

        return filteredMapList;
    }

    private String connectCheckList(Context ctx, DomainObject objectTask) {
        String checkList = IMS_QP_Constants_mxJPO.type_IMS_QP_CheckList;
        String externalObjectRelationship = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact;

        DomainObject objectDoc = null;
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
            } else {
                objectDoc = new DomainObject(boDocument);
                objectDoc.setState(ctx, "Approved");
            }
        } catch (FrameworkException frameworkException) {
            System.out.println("framework exception: " + frameworkException.getMessage());
            frameworkException.printStackTrace();
        } catch (MatrixException matrixException) {
            System.out.println("matrix exception: " + matrixException.getMessage());
            matrixException.printStackTrace();
        }

        try {
            if (objectDoc != null) {
                relationship = IMS_KDD_mxJPO.connectIfNotConnected(ctx, externalObjectRelationship, objectTask, objectDoc);
            }
        } catch (Exception e) {
            System.out.println("error connecting: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("objects connected: " + objectTask.getName()
                + "-> " + relationship.getTypeName()
                + " ->" + objectDoc.getName());

        return "checklist " + objectDoc.getName() + "  connected";
    }

    private String connectVTZType(Context ctx, DomainObject objectTask) {
        String docType = IMS_QP_Constants_mxJPO.type_IMS_ExternalDocumentSet;
        String docPolicy = IMS_QP_Constants_mxJPO.type_IMS_ExternalObject;
        String relationship_qpTask2Fact = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact;

        DomainObject objectDoc;
        Relationship relationship = null;

        try {
            BusinessObject boDocument = new BusinessObject(docType, "VTZ", "-", ctx.getVault().getName());
            if (!boDocument.exists(ctx)) {
                objectDoc = DomainObject.newInstance(ctx);
                objectDoc.createObject(ctx,
                        /*type*/docType,
                        /*name*/"VTZ",
                        /*revision*/"-",
                        /*policy*/docPolicy,
                        /*vault*/ ctx.getVault().getName());

                objectDoc.setState(ctx, "Approved");
                objectDoc.setAttributeValue(ctx, "IMS_ProjDocStatus", "Finalized");
                objectDoc.setDescription(ctx, "Redmine #52277");

            } else {
                objectDoc = new DomainObject(boDocument);
            }

            objectDoc.setState(ctx, "Approved");
            objectDoc.setAttributeValue(ctx, "IMS_ProjDocStatus", "Finalized");
            objectDoc.setDescription(ctx, "Redmine #52277");

            System.out.print(objectDoc.getInfo(ctx, "current"));
            System.out.print("|" + objectDoc.getAttributeValue(ctx, "IMS_ProjDocStatus"));
            System.out.println("|" + objectDoc.getDescription(ctx));

            relationship = IMS_KDD_mxJPO.connectIfNotConnected(ctx, relationship_qpTask2Fact, /*from*/ objectTask, /*to*/ objectDoc);
            System.out.println("objects connected: " + objectTask.getName()
                    + " -> " + relationship.getTypeName()
                    + " -> " + objectDoc.getName());

        } catch (Exception e) {
            System.out.println("error connecting: " + relationship + "|" + e.getMessage());
            return "Error connecting: " + relationship + " message: " + e.getMessage();
        }
        return objectDoc.getName() + "  connected";
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

    private String fillSelect(MapList filteredMapList, String taskId) {

        String dropDownDocket =
                "<form>" +
                        "<select  style=\"background-color:#fff0dc\" >";

        for (Object o : filteredMapList) {
            Map map = (Map) o;

            dropDownDocket += new StringBuilder()
                    .append("<option style=\"background-color:#fff0dc\" value=\"")
                    .append(map.get("id"))
                    .append("\">")

                    .append(map.get("name"))
                    .append(" baseline: ").append(map.get(IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name"))
                    .append(" rev.: ").append(map.get("attribute[IMS_SPFMajorRevision]"))
                    .append("_").append(map.get("attribute[IMS_SPFDocVersion]"))
                    .append("</option>")
                    .toString();
        }

        dropDownDocket = dropDownDocket.replaceAll("&+", "*");
        dropDownDocket += "</select>" +
                "   <input type=\"submit\" value=\"OK\" onClick=\"junction('" + taskId + "');\">" +
                "</form>";

        return dropDownDocket;
    }

    public String junctionAction(Context ctx, String... args) {

        HttpURLConnection connection = null;
        try {
            Map argsMap = JPO.unpackArgs(args);
            LOG.info("argsMap junctionAction: " + argsMap);
            DomainObject objectTask = new DomainObject((String) argsMap.get("objectId"));

            MapList contentMapList = null;

            String where = String.format("id==%s", argsMap.get("docId"));
            String content = null;


            try {
                Map authorizationParameters = getAuthorizeParameters(ctx);
                connection = (HttpURLConnection) getConnection(authorizationParameters);
                content = findExternalObjects(connection, where);
            } catch (IOException ioe) {
                LOG.error("IO error: " + ioe.getMessage());
                ioe.printStackTrace();
            }

            if (UIUtil.isNotNullAndNotEmpty(content)) {
                contentMapList = jsonArrayToMapList(new JSONArray(content));
            }
            LOG.info("contentMapList: " + contentMapList);

            try {
                ContextUtil.startTransaction(ctx, true);
            } catch (FrameworkException fe) {
                LOG.error("starting transaction error: " + fe.getMessage());
                fe.printStackTrace();
            }

            DomainObject externalDocument = ensureObject(ctx, (Map) contentMapList.get(0));

            try {
                IMS_KDD_mxJPO.connectIfNotConnected(ctx, "IMS_QP_QPTask2Fact", objectTask, externalDocument);
            } catch (Exception e) {
                LOG.error("connect object error: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                String docName = externalDocument.getName(ctx);
                String expectedResultId = objectTask.getInfo(ctx, "from[IMS_QP_ExpectedResult2QPTask].to.id");
                DomainObject expectedResult = new DomainObject(expectedResultId);
                expectedResult.setAttributeValue(ctx, "IMS_QP_DocumentCode", docName.substring(0, docName.lastIndexOf(".")));
            } catch (FrameworkException fe) {
                LOG.error("framework exception: " + fe.getMessage());
                fe.printStackTrace();
            }

            try {
                final IMS_ExternalSystem_mxJPO.ExternalSystem externalSystem = new IMS_ExternalSystem_mxJPO.ExternalSystem(ctx, "97");
                IMS_KDD_mxJPO.connectIfNotConnected(ctx,
                        IMS_ExternalSystem_mxJPO.RELATIONSHIP_IMS_Object2ExternalSystem, externalDocument, externalSystem.getObject());
            } catch (Exception e) {
                LOG.error("external systems error: " + e.getMessage());
                e.printStackTrace();
            }

            objectTask.setAttributeValue(ctx, "IMS_QP_SelectDocument", "");

            ContextUtil.commitTransaction(ctx);

        } catch (Exception e) {
            LOG.error("any errors: " + e.getMessage());
            e.printStackTrace();
        }

        if (connection != null) {
            connection.disconnect();
        }

        return "200";
    }

    private String getColoredString(String color, String text) {
        text = text.replaceAll("&", "&amp;");
        return new StringBuilder()
                .append("style=\"background-color:" + color + "\">")
                .append(text)
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
                        DomainObject.getAttributeSelect("IMS_IsLast"),
                        DomainObject.getAttributeSelect("IMS_IsLastVersion"),
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
     * @param ctx usual parameter
     * @param map contains all needs attributes for filling the instance
     * @return Domain Object for connecting to QP plan
     */
    private DomainObject ensureObject(Context ctx, Map map) {
        System.out.println("new ensureObject: " + map);

        String INTERFACE_IMS_ExternalObject = "IMS_ExternalObject";

        String ATTRIBUTE_IMS_ExternalObjectId = "IMS_ExternalObjectId";
        String ATTRIBUTE_IMS_ExternalObjectPolicy = "IMS_ExternalObjectPolicy";
        String ATTRIBUTE_IMS_ExternalObjectState = "IMS_ExternalObjectState";
        String ATTRIBUTE_IMS_ExternalObjectType = "IMS_ExternalObjectType";

        String TYPE_IMS_ExternalDocumentSet = "IMS_ExternalDocumentSet";
        String POLICY_IMS_ExternalObject = "IMS_ExternalObject";
        String ATTRIBUTE_IMS_SPFMajorRevision = "IMS_SPFMajorRevision";
        String ATTRIBUTE_IMS_SPFDocVersion = "IMS_SPFDocVersion";
        String ATTRIBUTE_IMS_ProjDocStatus = "IMS_ProjDocStatus";
        String ATTRIBUTE_IMS_Frozen = "IMS_Frozen";

        DomainObject object = null;

        String name = (String) map.get(DomainConstants.SELECT_NAME),
                revision = (String) map.get(DomainConstants.SELECT_REVISION);

        try {
            BusinessObject boDocument = new BusinessObject(TYPE_IMS_ExternalDocumentSet, name, revision,
                    ctx.getVault().getName());

            object = new DomainObject(boDocument);

            if (!object.exists(ctx)) {
                object.create(ctx, POLICY_IMS_ExternalObject);
                object.addBusinessInterface(ctx, new BusinessInterface(INTERFACE_IMS_ExternalObject, ctx.getVault()));
            }

        } catch (Exception e) {
            System.out.println("creating business object error: " + e.getMessage());
        }

        try {
            object.setDescription(ctx, (String) map.get(DomainConstants.SELECT_DESCRIPTION));

            object.setAttributeValue(ctx, ATTRIBUTE_IMS_ExternalObjectId, (String) map.get(DomainConstants.SELECT_ID));
            System.out.println("ensure object id: " + object.getAttributeValue(ctx, ATTRIBUTE_IMS_ExternalObjectId));

            object.setAttributeValue(ctx, ATTRIBUTE_IMS_ExternalObjectType, (String) map.get(DomainConstants.SELECT_TYPE));
            object.setAttributeValue(ctx, ATTRIBUTE_IMS_ExternalObjectPolicy, (String) map.get(DomainConstants.SELECT_POLICY));
            object.setAttributeValue(ctx, ATTRIBUTE_IMS_ExternalObjectState, (String) map.get(DomainConstants.SELECT_CURRENT));

            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_SPFMajorRevision, (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFMajorRevision)));
            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_SPFDocVersion, (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFDocVersion)));
            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_ProjDocStatus, (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_ProjDocStatus)));
            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_Frozen, (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Frozen)));

            object.setAttributeValue(ctx, "IMS_BBSMajor", (String) map.get(IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name"));

            System.out.println("ensure object id: " + object.getAttributeValue(ctx, "IMS_ExternalObjectId"));
        } catch (FrameworkException fe) {
            System.out.println("setting attributes error: " + fe.getMessage());
        }

        return object;
    }

    public MapList getAllTasksForTable(Context ctx, String... args) {
        MapList allTasks = new MapList();
        try {
            allTasks = DomainObject.findObjects(ctx,
                    /*type*/ IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    /*vault*/IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    /*where*/null,
                    /*selects*/ new StringList("id"));
        } catch (FrameworkException fe) {
            System.out.println("error getting tasks: " + fe.getMessage());
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
        if (result != null) {
            map = result;
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
        if (result != null) {
            map = result;
        }

        return fillVector(map, "value");
    }
}
