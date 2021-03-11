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

    private final List<String> BASELINES = Collections.unmodifiableList(Arrays.asList(
            "CB0", "CB0.01", "Initial FH1 design", "CB01",
            "CB02", "CB02.1", "CB02.1.1", "CB02.2",
            "CB03", "CB03.1",
            "CB1"));

    private final List<String> IMS_PHASE_RANGE = Collections.unmodifiableList(Arrays.asList(
            "B", "C", "L", "M", "P"));                                                                                  //full list of values: look at #56811

    /**
     * Entering point of service
     *
     * @param ctx  usual parameter
     * @param args usual parameter
     */
    public void searchProcess(Context ctx, String... args) {                                                            //0 entry point

        BusinessObjectWithSelectList businessObjectList = getAllQPTaskList(ctx);                                        //1 get all QP Tasks

        if (businessObjectList != null) {
            getLoopProcess(ctx, businessObjectList);                                                                    //2 loop searching actual plan
        }

//        report generator
        Map<String, BusinessObjectWithSelectList> reportData =
                new IMS_QP_ActualPlanSearchGenerator_mxJPO().reportGeneration(ctx);

//        report write to xlsx
        new IMS_QP_ActualPlanSearchReport_mxJPO().main(ctx, reportData, args);
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
     * Getting selections needed for Task
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
        busSelect.add(IMS_QP_Constants_mxJPO.SYSTEM_TO_FUNCTIONAL_AREA);
        busSelect.add(IMS_QP_Constants_mxJPO.BUILDING_TO_FUNCTIONAL_AREA);
        busSelect.add(IMS_QP_Constants_mxJPO.STRAIGHT_SYSTEM_TO_FUNCTIONAL_AREA);
        busSelect.add(IMS_QP_Constants_mxJPO.STRAIGHT_BUILDING_TO_FUNCTIONAL_AREA);
        busSelect.add(IMS_QP_Constants_mxJPO.PBS_TYPE_TO_QPTASK);
        busSelect.add(IMS_QP_Constants_mxJPO.PBS_TO_QPTASK);                                   //pbs
        busSelect.add(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);                              //baseline
        busSelect.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM);                       //system
        busSelect.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM_TYPE);                  //system type
        busSelect.add(IMS_QP_Constants_mxJPO.PLAN_TO_TASK);                                    //quality plan
        busSelect.add(IMS_QP_Constants_mxJPO.STAGE_TO_TASK_ID);                                //stage_id
        busSelect.add(IMS_QP_Constants_mxJPO.STAGE_TO_TASK);                                   //stage_name
        busSelect.add(IMS_QP_Constants_mxJPO.STAGE_TO_TASK_TYPE);                              //stage_type
        busSelect.add(IMS_QP_Constants_mxJPO.ATTRIBUTE_STAGE_TO_TASK);                         //stage_level

        return busSelect;
    }

    /**
     * Getting selections needed for Expected Result
     *
     * @return list of selection strings
     */
    private StringList getExpectedResultSelects() {
        StringList selectStmts = new StringList();
        selectStmts.add(DomainConstants.SELECT_NAME);
        selectStmts.add(IMS_QP_Constants_mxJPO.RESULT_TYPE_TO_EXPECTED_RESULT);
        selectStmts.add(IMS_QP_Constants_mxJPO.attribute_IMS_QP_DocumentCode);
        selectStmts.add(IMS_QP_Constants_mxJPO.FAMILY_TO_EXPECTED_RESULT);
        return selectStmts;
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

        for (Object o : businessObjectList) {
            BusinessObjectWithSelect bowsTask = (BusinessObjectWithSelect) o;

            try {
                bowsTask.open(ctx);
            } catch (MatrixException e) {
                System.out.println("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }

            System.out.println("\n=== " + bowsTask.getSelectData(DomainConstants.SELECT_NAME) + " ===");

            // Instantiating the BusinessObject
            StringList selectBusStmts = new StringList();
            selectBusStmts.add(DomainConstants.SELECT_ID);
            selectBusStmts.add(DomainConstants.SELECT_TYPE);
            selectBusStmts.add(DomainConstants.SELECT_NAME);

            StringList selectRelStmts = new StringList();
            selectRelStmts.add(DomainConstants.SELECT_NAME);
            selectRelStmts.add(DomainConstants.SELECT_FROM_ID);
            selectRelStmts.add(DomainConstants.SELECT_FROM_NAME);
            selectRelStmts.add(DomainConstants.SELECT_TO_ID);
            selectRelStmts.add(DomainConstants.SELECT_TO_NAME);
            short recurse = 1;

            ExpansionWithSelect expansion = null;
            try {
                expansion = bowsTask.expandSelect(ctx,
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

            // Get each relationship of type IMS_QP_ExpectedResult2QPTask and direction output (from task to expected result)
            int counter = 0;
            String expectedResultId = null;

            String id = bowsTask.getSelectData(DomainConstants.SELECT_ID);
            while (relItr.next()) {
                RelationshipWithSelect relSelect = relItr.obj();
                String relationshipType = relSelect.getSelectData(DomainConstants.SELECT_NAME);
                String relationshipFromId = relSelect.getSelectData(DomainConstants.SELECT_FROM_ID);
                String relationshipToId = relSelect.getSelectData(DomainConstants.SELECT_TO_ID);

                //if expected result type and output direct (from)
                boolean from = relationshipFromId.equals(id);
                if (relationshipType
                        .equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask) && from) {            //3 search IMS_QP_ExpectedResult2QPTask with direction output
                    counter++;
                    expectedResultId = relationshipToId;
                }
            }

            //drop any attribute values of the additional information task to the empty string
            setValueByAttribute(ctx, bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO, "");

            if (counter != 1) {
                setValueByAttribute(ctx,
                        bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO, "4.1 Task has error");
                continue;
            }

            BusinessObjectWithSelect bowsExpectedResult = null;
            try {
                bowsExpectedResult = new BusinessObject(expectedResultId).select(ctx, getExpectedResultSelects());
                bowsExpectedResult.open(ctx);
            } catch (Exception e) {
                System.out.println("error getting business object: " + expectedResultId);
                e.printStackTrace();
            }

            boolean checkType = false;
            if (bowsExpectedResult != null) {
                checkType = checkResultType(ctx, bowsTask, bowsExpectedResult);                                         //5
            }

            boolean needsCheckDoc = false;
            if (checkType) {
                initializeCommonParameters(bowsTask, bowsExpectedResult);
                needsCheckDoc = searchPreparation(ctx, bowsTask, bowsExpectedResult);                                   //6
            }

            boolean docFounded = false;
            if (needsCheckDoc) {
                if (UIUtil.isNotNullAndNotEmpty(documentCode)) {
                    docFounded = findDocumentByCode(ctx, bowsTask, bowsExpectedResult);                                 //8
                }
                if (UIUtil.isNullOrEmpty(documentCode)) {
                    docFounded = findDocumentEmptyCode(ctx, bowsTask, bowsExpectedResult);                              //9
                }
            }

            System.out.println("Searching result: " + (docFounded ? "Found" : "Not found"));

            try {
                bowsTask.close(ctx);
                bowsExpectedResult.close(ctx);
            } catch (MatrixException e) {
                System.out.println("error close bo: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return result;                                                                                                  //11 end of loop
    }

    /**
     * @param ctx       usual parameter
     * @param bowsTask  task object
     * @param attribute name of attribute
     * @param value     value of attribute
     */
    private void setValueByAttribute(Context ctx, BusinessObjectWithSelect bowsTask, String attribute, String value) {
        try {
            bowsTask.setAttributeValue(ctx, attribute, value);
        } catch (MatrixException e) {
            System.out.println("error setting attribute: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private String documentCode;
    private String documentBaseline;
    private String documentStage;
    private String documentStageLevel;
    private String resultType;
    private String family;
    private boolean isVTZType;

    private void initializeCommonParameters(BusinessObjectWithSelect bowsTask,
                                            BusinessObjectWithSelect bowsExpectedResult) {

        documentCode = bowsExpectedResult.getSelectData(IMS_QP_Constants_mxJPO.attribute_IMS_QP_DocumentCode);
        resultType = bowsExpectedResult.getSelectData(IMS_QP_Constants_mxJPO.RESULT_TYPE_TO_EXPECTED_RESULT);
        family = bowsExpectedResult.getSelectData(IMS_QP_Constants_mxJPO.FAMILY_TO_EXPECTED_RESULT);

        documentBaseline = bowsTask.getSelectData(IMS_QP_Constants_mxJPO.BASELINE_TO_QPTASK);
        documentStage = bowsTask.getSelectData(IMS_QP_Constants_mxJPO.STAGE_TO_TASK);
        documentStageLevel = bowsTask.getSelectData(IMS_QP_Constants_mxJPO.ATTRIBUTE_STAGE_TO_TASK);

        isVTZType = IMS_QP_Constants_mxJPO.VTZ_PLAN_TYPES.equals(resultType);

        System.out.println("code: " + documentCode +
                " result type: " + resultType +
                " family: " + family +
                " CB: " + documentBaseline +
                " stage: " + documentStage +
                " stage lvl: " + documentStageLevel +
                " is vtz type: " + isVTZType
        );
    }

    /**
     * @param ctx
     * @param bowsTask
     * @param bowsExpectedResult
     * @return
     */
    private boolean checkResultType(Context ctx,
                                    BusinessObjectWithSelect bowsTask,
                                    BusinessObjectWithSelect bowsExpectedResult) {
        String resultType = bowsExpectedResult.getSelectData(IMS_QP_Constants_mxJPO.RESULT_TYPE_TO_EXPECTED_RESULT);

        boolean hasConnectedDocument =
                "TRUE".equals(bowsTask.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_QPTASK_2_FACT));

        if (!hasConnectedDocument) {
            if (IMS_QP_Constants_mxJPO.ANOTHER_PLAN_TYPES.equals(resultType)) {                                         //5.1.1
                return false;

            } else if (IMS_QP_Constants_mxJPO.FAMILY_CL.equals(resultType)) {                                           //5.2
                DomainObject objectTask = new DomainObject(bowsTask);
                try {
                    ContextUtil.startTransaction(ctx, true);
                    connectCheckList(ctx, objectTask);                                                                  //5.2.2
                    ContextUtil.commitTransaction(ctx);
                } catch (FrameworkException e) {
                    e.printStackTrace();
                }
                return false;

            }
        } else if (hasConnectedDocument && IMS_QP_Constants_mxJPO.FAMILY_CL.equals(resultType)) {
            return false;
        }
        return true;
    }

    /**
     * @param ctx                usual parameter
     * @param bowsTask           the Task object from loop for getting parameters
     * @param bowsExpectedResult the Expected Result object to check in the method
     * @return preliminary check result
     */
    private boolean searchPreparation(Context ctx,
                                      BusinessObjectWithSelect bowsTask,
                                      BusinessObjectWithSelect bowsExpectedResult) {
        if (UIUtil.isNullOrEmpty(documentBaseline)) {
            setValueByAttribute(ctx, bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO, "6.2.1 Error getting the baseline");
            return false;
        }

        if (!isVTZType && UIUtil.isNotNullAndNotEmpty(documentStage) && "BD".equals(documentStage)) {
            if (UIUtil.isNullOrEmpty(documentStageLevel)) {
                setValueByAttribute(ctx,
                        bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO,
                        "6.2.1 Document is BD stage but level is 'empty'"
                );
                return false;
            }
        }

        boolean isCorrectDocCode = true;
        if (UIUtil.isNotNullAndNotEmpty(documentCode)) {
            isCorrectDocCode = checkDocumentCode(documentCode);
        }

        if (!isCorrectDocCode) {
            String errorDocCode = "Wrong code: " + documentCode.replaceAll("Wrong code: ", "");
            try {
                bowsExpectedResult.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_DocumentCode, errorDocCode);
            } catch (MatrixException e) {
                e.printStackTrace();
            }
        }
        return isCorrectDocCode;                                                                                        //6.2
    }

    /**
     * @param documentCode
     * @return result of check
     */
    private boolean checkDocumentCode(String documentCode) {

        if (UIUtil.isNotNullAndNotEmpty(documentCode)) {                                                                //5.2 if task has Document Code?
            if (!documentCode.matches("[F][H][1][\\w.&]+")) {                                                    //5.2.0 check correct code
                return false;
            }
        }
        return true;
    }

    /**
     * @param bowsTask           BusinessObjectWithSelect Task
     * @param bowsExpectedResult BusinessObjectWithSelect Expected Result
     * @return result of found and connect document
     */
    private boolean findDocumentByCode(Context ctx, BusinessObjectWithSelect bowsTask,
                                       BusinessObjectWithSelect bowsExpectedResult) {

        StringBuilder builder = new StringBuilder();

        builder.append(String.format("name smatch '%s*'", documentCode));

        if (!isVTZType) {                                                                                               //8.2.2
            builder.append(getBaselineValue(documentBaseline));
            builder.append(getStageValue(documentStage, documentStageLevel));
        }

        if (isVTZType) {
            if (!checkBaselineContainer(documentBaseline)) {                                                            //8.2.1
                setValueByAttribute(ctx,                                                                                //8.2.1.1
                        bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO,
                        "8.2.1.1 Wrong baseline for VTZ: " + documentBaseline);
                return false;
            }

            if ("CB1".equals(documentBaseline)) {
                if (UIUtil.isNotNullAndNotEmpty(documentStage) && "BD".equals(documentStage)) {                         //8.2.1.2
                    if (UIUtil.isNullOrEmpty(documentStageLevel)) {
                        setValueByAttribute(ctx,                                                                        //8.2.1.2.1
                                bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO,
                                "8.2.1.2.1 Document is BD stage but level is 'empty'"
                        );
                        return false;
                    }
                    builder.append(getITAStageValue(documentStageLevel));
                }
            }
        }

        System.out.println("vtz: " + isVTZType + "|findDocumentByCode builder: " + builder);
        return documentService(ctx, bowsTask, bowsExpectedResult, builder.toString());
    }

    /**
     * @param bowsTask           the Business object for document searching
     * @param bowsExpectedResult himself Expected result object
     * @return result of found and connect document
     */
    private boolean findDocumentEmptyCode(Context ctx, BusinessObjectWithSelect bowsTask,
                                          BusinessObjectWithSelect bowsExpectedResult) {

        String documentPBS = UIUtil.isNotNullAndNotEmpty(
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM)) ?
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM) :
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.PBS_TO_QPTASK);

        String documentPBSType = UIUtil.isNotNullAndNotEmpty(
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM_TYPE)) ?
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.FROM_IMS_QP_TASK_2_SYSTEM_TYPE) :
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.PBS_TYPE_TO_QPTASK);

        StringBuilder builder = new StringBuilder();

        if (!isVTZType) {                                                                                               //9.1.1
            builder.append(String.format("name smatch 'FH1*.%s.*'", family));
            builder.append(getBaselineValue(documentBaseline));
            builder.append(getStageValue(documentStage, documentStageLevel));
            builder.append(checkRelationshipToObject(documentPBSType, documentPBS));
        }

        if (isVTZType) {
            if (checkRelationshipToObject(documentPBSType, documentPBS).contains("Architecture level") ||
                    documentPBSType.equals(IMS_QP_Constants_mxJPO.type_IMS_PBSFunctionalArea)) {
                builder.append(checkRelationshipToObject(documentPBSType, documentPBS));
            }

            if (!builder.toString().contains("Architecture level")) {
                if (!documentPBSType.equals(IMS_QP_Constants_mxJPO.type_IMS_PBSFunctionalArea)) {
                    if (UIUtil.isNotNullAndNotEmpty(searchFunctionalAreaFromPBS(bowsTask))) {
                        documentPBS = searchFunctionalAreaFromPBS(bowsTask);
                        documentPBSType = IMS_QP_Constants_mxJPO.type_IMS_PBSFunctionalArea;
                        builder.append(checkRelationshipToObject(documentPBSType, documentPBS));
                    } else {
                        setValueByAttribute(ctx,                                                                        //9.1.2.4
                                bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO,
                                "9.1.2.4 Can't found functional area for KKS"
                        );
                        return false;
                    }
                }
            }

            if (BASELINES.contains(documentBaseline)) {
                builder.append(getPhaseValue());                                                                        //9.1.2.5.2
            } else {
                setValueByAttribute(ctx,
                        bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO,
                        "9.1.2.5.1 Wrong baseline for VTZ: '" + documentBaseline + "'"
                );
                return false;
            }

            if ("CB1".equals(documentBaseline)) {                                                                       //9.1.2.5.3
                if (UIUtil.isNotNullAndNotEmpty(documentStage) && "BD".equals(documentStage)) {
                    if (UIUtil.isNullOrEmpty(documentStageLevel)) {
                        setValueByAttribute(ctx,                                                                        //9.1.2.5.3.1
                                bowsTask, IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO,
                                "9.1.2.5.3.1 Document is BD but level is 'empty'"
                        );
                        return false;
                    }
                    builder.append(getITAStageValue(documentStageLevel));                                               //9.1.2.5.3.2
                }
            }

            builder.append(                                                                                             //9.1.2.5.3.3
                    String.format(
                            "&&to[IMS_ListDocument2DocSet].from.to[IMS_DocumentClass2ListDoc].from.name=='%s'",
                            family));
            builder.insert(0, "name smatch 'FH1.*.MF.*'");
        }

        System.out.println("vtz: " + isVTZType + " builder: " + builder);
        return documentService(ctx, bowsTask, bowsExpectedResult, builder.toString());
    }

    private String getStageValue(String documentStage, String documentStageLevel) {                                     //6.1
        if (UIUtil.isNotNullAndNotEmpty(documentStage) && "BD".equals(documentStage)) {
            if (UIUtil.isNullOrEmpty(documentStageLevel)) {
                return "";
            }
        }
        return String.format("&&'%s'=='%s'",
                "to[Task Deliverable].from.attribute[IMS_BDStage]", documentStageLevel);
    }

    private String searchFunctionalAreaFromPBS(BusinessObjectWithSelect bowsTask) {

        String functionalAreaFromTask = UIUtil.isNotNullAndNotEmpty(
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.STRAIGHT_SYSTEM_TO_FUNCTIONAL_AREA)) ?
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.STRAIGHT_SYSTEM_TO_FUNCTIONAL_AREA) :
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.STRAIGHT_BUILDING_TO_FUNCTIONAL_AREA);
        String functionalAreaFromPlan = UIUtil.isNotNullAndNotEmpty(
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.SYSTEM_TO_FUNCTIONAL_AREA)) ?
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.SYSTEM_TO_FUNCTIONAL_AREA) :
                bowsTask.getSelectData(IMS_QP_Constants_mxJPO.BUILDING_TO_FUNCTIONAL_AREA);

        if (UIUtil.isNotNullAndNotEmpty(functionalAreaFromTask)) return functionalAreaFromTask;
        if (UIUtil.isNotNullAndNotEmpty(functionalAreaFromPlan)) return functionalAreaFromPlan;
        return "";
    }


    private String checkRelationshipToObject(String documentPBSType, String documentPBS) {
        if (UIUtil.isNullOrEmpty(documentPBS)) {
            return String.format("&&('%s'=='%s'||'%s'=='%s')",
                    "to[IMS_PBS2DocSet].from.name", "Architecture level",
                    "to[IMS_PBS2DocSet].from.name", "Plant level");

        } else {
            String relationship = IMS_QP_Constants_mxJPO.type_IMS_PBSFunctionalArea.equals(documentPBSType) ?
                    IMS_QP_Constants_mxJPO.TO_IMS_PBS_2_DOC_SET_FROM + ".name"
                    : IMS_QP_Constants_mxJPO.TO_IMS_REF_CI_2_DOC_FROM + ".name";
            return String.format("&&'%s'=='%s'", relationship, documentPBS);
        }
    }

    boolean checkBaselineContainer(String baseline) {
        return BASELINES.contains(baseline);
    }

    private String getBaselineValue(String documentBaseline) {
        return String.format(
                "&&'%s'=='%s'", IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name", documentBaseline);               //5.2.1
    }

    private String getPhaseValue() {
        StringBuilder builder = new StringBuilder();
        builder.append("&&(");
        for (int i = 0; i < IMS_PHASE_RANGE.size(); i++) {                                                              //5.6.3 search by second section
            builder.append(String.format("attribute[IMS_Phase]==%s", IMS_PHASE_RANGE.get(i)));
            if (IMS_PHASE_RANGE.size() - i > 1) {
                builder.append("||");
            } else {
                builder.append(")");
            }
        }
        return builder.toString();
    }

    private String getITAStageValue(String stageLevel) {
        String attributeITAStage = "&&attribute[IMS_ITA_Stage]=='%s'";
        String stageQuery = String.format(attributeITAStage, "2".equals(stageLevel) ? "Finalized DA" : "Preliminary DA");
        return stageQuery;
    }

    private String getColoredString(String color, String text) {
        text = text.replaceAll("&", "&amp;");
        return new StringBuilder()
                .append("style=\"background-color:" + color + "\">")
                .append(text)
                .toString();
    }

    /**
     * @param bowsTask           the Business object for getting data about himself
     * @param bowsExpectedResult
     * @return map with stage, result type, where parameters
     * @throws FrameworkException
     */
    private String getVTZWhereDocument(BusinessObjectWithSelect bowsTask,
                                       BusinessObjectWithSelect bowsExpectedResult) {
        return "";
    }

    private String getVTZWhereDocument(String functionalAreaName) {
        return String.format("'%s'=='%s'", "to[IMS_PBS2DocSet].from.name", functionalAreaName);
    }

    private MapList sortMapListByRevisionAndVersion(MapList filteredMapList) {
        Map<String, Map> tempMap = new HashMap();

        for (Object o : filteredMapList) {
            Map map1 = (Map) o;
            String name = (String) map1.get("name");

            if (tempMap.containsKey(name)) {
                Map map2 = tempMap.get(name);
                String sR1 = ((String) map1.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_SPFMajorRevision)).replaceAll("[^0-9]", "");
                String sR2 = ((String) map2.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_SPFMajorRevision)).replaceAll("[^0-9]", "");
                int r1 = Integer.parseInt(sR1);
                int r2 = Integer.parseInt(sR2);
                if (r1 > r2) {
                    tempMap.put(name, map1);
                    continue;
                }

                if (r1 == r2) {
                    String sV1 = ((String) map1.get(IMS_QP_Constants_mxJPO.ATTIBUTE_IMS_SPFDocVersion)).replaceAll("[^0-9]", "");
                    String sV2 = ((String) map2.get(IMS_QP_Constants_mxJPO.ATTIBUTE_IMS_SPFDocVersion)).replaceAll("[^0-9]", "");
                    int v1 = Integer.parseInt(sV1);
                    int v2 = Integer.parseInt(sV2);
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

    /**
     * @param ctx        usual parameter
     * @param objectTask Mock object while during the formation of the algo
     * @return result of connecting
     */
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
                relationship = IMS_KDD_mxJPO.connectIfNotConnected(ctx,
                        externalObjectRelationship, objectTask, objectDoc);
            }
        } catch (Exception e) {
            System.out.println("error connecting: " + e.getMessage());
            e.printStackTrace();
        }

        return "checklist " + objectDoc.getName() + "  connected " + relationship;
    }

    /**
     * @param ctx                usual parameter
     * @param bowsTask           the Business object for manipulations
     * @param bowsExpectedResult the related Expected Result object
     * @param where              the query where
     */
    private boolean documentService(Context ctx,                                                                        //10.1
                                    BusinessObjectWithSelect bowsTask,
                                    BusinessObjectWithSelect bowsExpectedResult,
                                    String where) {

        HttpURLConnection connection = null;
        MapList contentMapList = new MapList();
        String taskName = bowsTask.getSelectData(DomainConstants.SELECT_NAME);

        DomainObject objectTask = new DomainObject(bowsTask);

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
        boolean docFounded;
        if (contentMapList.size() > 0) {
            docFounded = true;

            //filtering by language type                                                                                //10.2.1.1
            filteredMapList.addAll(getLanguageList(contentMapList));
            filteredMapList = sortMapListByRevisionAndVersion(filteredMapList);

            if (filteredMapList.size() > 1) {                                                                           //10.2.1.2.1
                String selectResult = fillSelect(filteredMapList, bowsTask.getSelectData(DomainConstants.SELECT_ID));

                try {
                    if ("FALSE".equals(objectTask.getInfo(ctx, IMS_QP_Constants_mxJPO.FROM_IMS_QP_QPTASK_2_FACT))) {
                        bowsTask.setAttributeValue(ctx, "IMS_QP_SelectDocument", selectResult);
                    }
                } catch (FrameworkException fe) {
                    System.out.println("error setting attribute IMS_QP_SelectDocument for " + taskName);
                } catch (MatrixException e) {
                    System.out.println("error setting attribute IMS_QP_SelectDocument for " + taskName);
                    e.printStackTrace();
                }

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

                    if (UIUtil.isNotNullAndNotEmpty(existedConnectionToExternalDocumentRevision)) {
                        //check equals document revisions
                        equalDocument = existedConnectionToExternalDocumentRevision.equals(map.get(DomainConstants.SELECT_REVISION));

                        //delete connection if existed same
                        if (!equalDocument) {
                            DomainRelationship.disconnect(ctx, existedConnectionToExternalDocumentId);
                        }
                    }
                } catch (FrameworkException frameworkException) {
                    System.out.println("framework exception: " + frameworkException.getMessage());
                    frameworkException.printStackTrace();
                }

                DomainObject externalDocument = ensureObject(ctx, map);
                try {
                    Relationship relationship = IMS_KDD_mxJPO.connectIfNotConnected(ctx,                                //10.2.1.3
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact, objectTask, externalDocument);

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
                    bowsExpectedResult.setAttributeValue(ctx,
                            "IMS_QP_DocumentCode", docName.substring(0, docName.lastIndexOf(".")));
                } catch (FrameworkException fe) {
                    System.out.println("framework exception: " + fe.getMessage());
                } catch (MatrixException matrixException) {
                    System.out.println("matrix exception: " + matrixException.getMessage());
                    matrixException.printStackTrace();
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
            }
        } else {
            docFounded = false;
        }

        if (connection != null) {
            connection.disconnect();
        }
        return docFounded;
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

        //TODO delete next replace
        url = url.replace("97", "106");

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

        OutputStream os = connection.getOutputStream();
        byte[] input = jsonInputString.getBytes("utf-8");
        os.flush();
        os.write(input, 0, input.length);
        os.close();

        //Read the response from input stream
        String content = "";
        if (HttpURLConnection.HTTP_OK == connection.getResponseCode()) {
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

            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_ExternalObjectType, (String) map.get(DomainConstants.SELECT_TYPE));
            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_ExternalObjectPolicy, (String) map.get(DomainConstants.SELECT_POLICY));
            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_ExternalObjectState, (String) map.get(DomainConstants.SELECT_CURRENT));

            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_SPFMajorRevision,
                    (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFMajorRevision)));
            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_SPFDocVersion,
                    (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFDocVersion)));
            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_ProjDocStatus,
                    (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_ProjDocStatus)));
            object.setAttributeValue(ctx,
                    ATTRIBUTE_IMS_Frozen,
                    (String) map.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Frozen)));

            object.setAttributeValue(ctx, "IMS_BBSMajor", (String) map.get(IMS_QP_Constants_mxJPO.TO_IMS_BBS_2_CI_FROM + ".name"));

            System.out.println("ensure object id: " + object.getAttributeValue(ctx, ATTRIBUTE_IMS_ExternalObjectId));
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
