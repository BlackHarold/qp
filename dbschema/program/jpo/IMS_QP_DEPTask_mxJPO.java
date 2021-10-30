import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.*;

public class IMS_QP_DEPTask_mxJPO {

    public static final String IMS_QP_RESULT_TYPE = "IMS_QP_ResultType";
    public static final String IMS_FAMILY = "IMS_Family";
    public static final String TO_IMS_QP_RESULT_TYPE_2_FAMILY = "to[IMS_QP_ResultType2Family]";
    public static final String SELECT_TO_IMS_QP_RESULT_TYPE_2_FAMILY_FROM_ID = TO_IMS_QP_RESULT_TYPE_2_FAMILY + ".from.id";
    public static final String IMS_NameRU = "attribute[IMS_NameRu]";
    public static final String IMS_DocumentCode = "attribute[IMS_QP_DocumentCode]";
    public static final String IMS_Name = "attribute[IMS_Name]";
    public static final String IMS_DescriptionRu = "attribute[IMS_DescriptionRu]";
    public static final String IMS_DescriptionEn = "attribute[IMS_DescriptionEn]";
    public static final String RELATIONSHIP_IMS_DEPResult2QPResult = "IMS_DEPResult2QPResult";
    public static final String ATTRIBUTE_IMS_QP_FACT_EXP = "IMS_QP_FactExp";
    public static final String SELECT_ATTRIBUTE_IMS_QP_FACT_EXP = "attribute[IMS_QP_FactExp]";
    protected static final String ATTRIBUTE_IMS_QP_CLOSE_STATUS = "IMS_QP_CloseStatus";
    protected static final String SELECT_ATTRIBUTE_IMS_QP_CLOSE_STATUS = "attribute[" + ATTRIBUTE_IMS_QP_CLOSE_STATUS + "]";
    public static final String SELECT_ATTRIBUTE_IMS_QP_CloseStatus = SELECT_ATTRIBUTE_IMS_QP_CLOSE_STATUS;
    public static final String SELECT_ATTRIBUTE_IMS_QP_FACT_GOT = "attribute[IMS_QP_FactGot]";
    public static final String ATTRIBUTE_IMS_QP_FACT_GOT = "IMS_QP_FactGot";
    public static final String RELATIONSHIP_IMS_QP_QPTask2QPTask = "IMS_QP_QPTask2QPTask";
    public static final String RELATIONSHIP_IMS_QP_QPTask2Fact = "IMS_QP_QPTask2Fact";

    public static final String FROM = "From";
    public static final String RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK = "IMS_QP_ExpectedResult2DEPTask";
    public static final String TYPE_IMS_QP_EXPECTED_RESULT = "IMS_QP_ExpectedResult";
    public static final String TO = "To";
    public static final String RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT = "IMS_QP_ResultType2ExpectedResult";
    public static final String RELATIONSHIP_IMS_QP_DEP_SUB_STAGE_2_DEP_TASK = "IMS_QP_DEPSubStage2DEPTask";
    public static final String TYPE_IMS_QP_DEP_TASK = "IMS_QP_DEPTask";
    public static final String RELATIONSHIP_IMS_QPlan2QPTask = "IMS_QP_QPlan2QPTask";
    public static final String TYPE_IMS_QP_QPTASK = "IMS_QP_QPTask";
    public static final String RELATIONSHIP_IMS_DEPTask2QPTask = "IMS_QP_DEPTask2QPTask";
    public static final String RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK = "IMS_QP_ExpectedResult2QPTask";

    protected static final String VALUE_APPROVED = "Approved";
    protected static final String VALUE_NO = "No";
    protected static final String ATTRIBUTE_IMS_SORT_ORDER = "IMS_SortOrder";

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public MapList getRelatedIMSDepTask(Context context, String[] args) throws Exception {
        try {

            Map programMap = JPO.unpackArgs(args);
            String sOID = (String) programMap.get("objectId");
            DomainObject subStage = DomainObject.newInstance(context, sOID);

            return getRelatedMapList(context, subStage,
                    RELATIONSHIP_IMS_QP_DEP_SUB_STAGE_2_DEP_TASK, TYPE_IMS_QP_DEP_TASK,
                    true, true, (short) 1, "", "", 0);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw ex;
        }
    }

    public HashMap createDEPTask(Context ctx, String[] args) {
        HashMap returnMap = new HashMap();
        try {

            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");

            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
            DomainObject parent = new DomainObject(parentOID);
            LOG.info(parent.getType(ctx));
            MapList depTask = getRelatedMapList(ctx, parent, RELATIONSHIP_IMS_QP_DEP_SUB_STAGE_2_DEP_TASK, TYPE_IMS_QP_DEP_TASK, true, true, (short) 1, "", null, 0);
            LOG.info(depTask.size() + ": " + depTask);

            getNextName(ctx, objectId, parent, depTask, "", 1, 0);

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }
        return returnMap;
    }

    public static DomainObject getNextName(Context ctx, String objectId, DomainObject parent, MapList mapList, String family, int i, int element) {
        String vault = ctx.getVault().getName();

        mapList.addSortKey("attribute[IMS_SortOrder]", "descending", "integer");
        mapList.sort();

        int numberInt = mapList.size();
        try {
            if (parent != null && !IMS_QP_Constants_mxJPO.type_IMS_QP_DEPSubStage.equals(parent.getType(ctx))) {
                numberInt += 1;
            }
        } catch (FrameworkException e) {
            LOG.error(e.getMessage());
            e.printStackTrace();
        }

        /*mask name: [Task name]_[Type level 2]_[Count]*/
        String familyPostfix = "";
        if ("DEVIATION".equals(family)) {
            familyPostfix = "_DEVIATION";
        } else if (!family.equals("")) {
            String familyName = "empty";
            if (family.contains("_")) {
                try {
                    LOG.info("family: " + family);
                    familyName = new DomainObject(family.substring(0, family.lastIndexOf("_"))).getName(ctx);
                } catch (Exception e) {
                    LOG.error("checking family name error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            familyPostfix = "_" + familyName;
        }

        String needZero = (numberInt < 10) ? "0" : "";
        String newName = null;
        try {
            newName = parent.getInfo(ctx, DomainObject.SELECT_NAME) + familyPostfix + "-" + needZero;
        } catch (FrameworkException e) {
            LOG.error("getting parent name error: " + e.getMessage());
            e.printStackTrace();
        }

        //check name existing
        BusinessObject boCandidate = null;
        int number = numberInt;
        String nameCandidate = "";
        try {
            do {
                nameCandidate = newName + number;
                boCandidate = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult, nameCandidate, "", vault);
                number++;
            } while (boCandidate.exists(ctx));
        } catch (MatrixException e) {
            LOG.error("check existing name error: " + e.getMessage());
            e.printStackTrace();
        }

        //create new object process
        DomainObject newObject = null;
        try {
            if (boCandidate != null) {
                newObject = new DomainObject(boCandidate);
                if (UIUtil.isNotNullAndNotEmpty(nameCandidate)) {
                    newObject.setId(objectId);
                    newObject.setName(ctx, nameCandidate);
                    newObject.setAttributeValue(ctx, ATTRIBUTE_IMS_SORT_ORDER, String.valueOf(numberInt));
                }
            }

        } catch (FrameworkException e) {
            LOG.error("setting parameters for new object got error: " + e.getMessage());
            e.printStackTrace();
        }

        return newObject;
    }

    //TODO remove method if isn't need
    public Object getRelationColumn(Context context, String[] args) throws Exception {

        Map programMap = JPO.unpackArgs(args);

        MapList mlObList = (MapList) programMap.get("objectList");
        Vector returnList = new Vector();
        for (Object o : mlObList) {
            returnList.add("");
        }

        return returnList;
    }

    public Map getRangeRelationshipER(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);
        Map requestMap = (Map) argsMap.get("requestMap");
        String taskId = (String) requestMap.get("objectId");
        DomainObject domainObject = new DomainObject(taskId);
        Map result = new HashMap();
        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();
        fieldRangeValues.add(FROM);
        fieldDisplayRangeValues.add(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.From", context.getLocale().getLanguage()));
        String objectType = domainObject.getType(context);
        if ("IMS_QP_DEP".equals(objectType) || "IMS_QP_DEPTask".equals(objectType)) {
            fieldRangeValues.add(TO);
            fieldDisplayRangeValues.add(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.To", context.getLocale().getLanguage()));
        }
        result.put("field_choices", fieldRangeValues);
        result.put("field_display_choices", fieldDisplayRangeValues);
        return result;
    }


    public MapList getRelatedExpectedResult(Context context, String[] args) {
        MapList expectedResults = new MapList();

        try {
            Map programMap = JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            DomainObject task = new DomainObject(objectId);

            //output expected result
            expectedResults = getRelatedMapList(context,
                    task, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK + "," + RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK,
                    "*", true, true, (short) 1, "", "", 0);

            //other input expected results with 'Approved' relationship states
            StringBuilder whereBuilder = new StringBuilder(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS).append("=='Approved'");
            MapList inputQPTasks = getRelatedMapList(context,
                    task, RELATIONSHIP_IMS_QP_QPTask2QPTask, "*", true, false, (short) 1, "", whereBuilder.toString(), 0);

            MapList rawList = new MapList();
            MapList expectedResultQP = new MapList();
            for (Object o : inputQPTasks) {
                Map map = (Map) o;
                String sID = (String) map.get(DomainObject.SELECT_ID);
                expectedResultQP.addAll(getRelatedMapList(context,
                        new DomainObject(sID), RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK,
                        "*", true, true, (short) 1, "", "", 0));
            }

            for (Object o1 : expectedResultQP) {
                Map map1 = (Map) o1;
                boolean flag = false;
                for (Object o2 : expectedResults) {
                    Map map2 = (Map) o2;
                    String id1 = (String) map1.get(DomainConstants.SELECT_ID);
                    String name1 = (String) map1.get(DomainConstants.SELECT_NAME);
                    String id2 = (String) map2.get(DomainConstants.SELECT_ID);
                    String name2 = (String) map2.get(DomainConstants.SELECT_NAME);
                    LOG.info(id1 + ":" + name1 + " == " + id2 + ":" + name2);
                    if (id1.equals(id2)) {
                        flag = true;
                        continue;
                    }
                }
                if (!flag) {
                    rawList.add(map1);
                }
            }

            //return map without doubles
            expectedResults.addAll(rawList);
            LOG.info("related expected result without doubles: " + expectedResults);

        } catch (Exception ex) {
            LOG.error("error getting related results: " + ex.getMessage());
            ex.printStackTrace();
        }

        return expectedResults;
    }

    public Object getFromTo(Context context, String[] args) throws Exception {

        Map programMap = JPO.unpackArgs(args);
        Map paramMap = (Map) programMap.get("paramList");
        String sOID = (String) paramMap.get("objectId");
        MapList mlObList = (MapList) programMap.get("objectList");

        Vector returnList = new Vector();
        try {
            for (Object objTemp : mlObList) {
                String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_RELATIONSHIP_ID);
                DomainRelationship relationship = new DomainRelationship(sID);
                relationship.openRelationship(context);
                returnList.add(relationship.getFrom().getTypeName().equals(TYPE_IMS_QP_EXPECTED_RESULT)
                        || (!relationship.getFrom().getObjectId().equals(sOID) && !relationship.getTo().getObjectId().equals(sOID))
                        ? EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.From", context.getLocale().getLanguage())
                        : EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.To", context.getLocale().getLanguage()));
                relationship.closeRelationship(context, true);
            }

        } catch (Exception e) {
            LOG.error("error when getting getFromTo: " + e.getMessage());
        }
        return returnList;
    }

    public int deleteRelatedExceptedResult(Context context, String[] args) throws Exception {
        try {
            String objectId = args[0];
            DomainObject depTask = new DomainObject(objectId);
            StringList QAGbusSelects = new StringList();  // Object
            QAGbusSelects.add(DomainConstants.SELECT_ID);
            StringList QAGrelSelects = new StringList();  // Rel

            MapList depExceptedResult = depTask.getRelatedObjects(context,
                    RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK + "," + RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, TYPE_IMS_QP_EXPECTED_RESULT, QAGbusSelects, QAGrelSelects,
                    /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);
            for (Object objTemp : depExceptedResult) {
                String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
                new DomainObject(sID).deleteObject(context);
            }
        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public Object getRangeResultType(Context context, String[] args) throws Exception {

        Map tempMap = new HashMap();
        StringList fieldRangeValues = new StringList("none");
        StringList fieldDisplayRangeValues = new StringList(" ");
        MapList resultType = getFindObject(context, IMS_QP_RESULT_TYPE, "*",
                "*", "");

        for (Object resultTypObject : resultType) {
            Map objTemp = (Map) resultTypObject;
            fieldDisplayRangeValues.add((String) objTemp.get(DomainConstants.SELECT_NAME));
            fieldRangeValues.add((String) objTemp.get(DomainConstants.SELECT_ID));
        }

        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);
        return tempMap;
    }


    public void setResultType(Context context, String[] args) throws Exception {
        try {
            Map programMap = JPO.unpackArgs(args);

            Map paramMap = (Map) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");
            String newValue = (String) paramMap.get("New Value");
            if (!newValue.equals("none") && !newValue.equals("")) {
                newValue = newValue.substring(0, newValue.indexOf("_"));
                DomainObject exceptedResult = new DomainObject(objectId);
                RelationshipType resultTypeToExceptedResult = new RelationshipType(RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT);

                MapList depExceptedResult = getRelatedMapList(context,
                        exceptedResult, RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT,
                        "*", true, true, (short) 1, "", "", 0);

                for (Object objTemp : depExceptedResult) {
                    String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
                    DomainObject typeRes = new DomainObject(sID);
                    typeRes.disconnect(context, resultTypeToExceptedResult, true, new BusinessObject(objectId));
                }
                DomainRelationship.connect(context, new DomainObject(newValue), RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT, exceptedResult);
            }
        } catch (Exception ex) {
            String errorStr = ex.getMessage();
            emxContextUtil_mxJPO.mqlError(context, errorStr);
            throw ex;
        }
    }

    public static Object getRangeFamily(Context context, String[] args) throws Exception {

        HashMap tempMap = new HashMap();
        StringList fieldRangeValues = new StringList("none");
        StringList fieldDisplayRangeValues = new StringList(" ");
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add(SELECT_TO_IMS_QP_RESULT_TYPE_2_FAMILY_FROM_ID);
        MapList resultType = DomainObject.findObjects(context,
                IMS_FAMILY, "*", "*", "*", "*", TO_IMS_QP_RESULT_TYPE_2_FAMILY + "==True", true, objectSelects);

        resultType.addSortKey("name", "ascending", "string");
        resultType.sort();

        for (Object resultTypObject : resultType) {
            Map objTemp = (HashMap) resultTypObject;
            String name = objTemp.get(DomainConstants.SELECT_ID) + "_" + objTemp.get(SELECT_TO_IMS_QP_RESULT_TYPE_2_FAMILY_FROM_ID);
            fieldDisplayRangeValues.add((String) objTemp.get(DomainConstants.SELECT_NAME));
            fieldRangeValues.add(name);
        }

        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);
        return tempMap;
    }

    public Object getResultType(Context context, String[] args) throws Exception {
        return getFindObject(context,
                IMS_QP_RESULT_TYPE, "*", "*", "");
    }

    public MapList getRelatedQPTask(Context context, String[] args) throws Exception {

        try {
            Map programMap = JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            DomainObject qPlan = new DomainObject(objectId);
            return getRelatedMapList(context, qPlan, RELATIONSHIP_IMS_QPlan2QPTask, TYPE_IMS_QP_QPTASK,
                    true, true, (short) 1, "", "", 0);
        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw ex;
        }
    }

    public HashMap createQPTask(Context context, String[] args) {

        HashMap returnMap = new HashMap();

        try {
            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");
            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
            String depTaskOID = (String) requestMap.get("DEPTaskOID");
            DomainObject parent = new DomainObject(parentOID);
            DomainObject qpTask = new DomainObject(objectId);

            if (UIUtil.isNotNullAndNotEmpty(depTaskOID)) {
                DomainObject depTask = new DomainObject(depTaskOID);
                DomainRelationship.connect(context, new DomainObject(depTaskOID), RELATIONSHIP_IMS_DEPTask2QPTask, new DomainObject(objectId));
            }

            MapList depTask = getRelatedMapList(context, parent, RELATIONSHIP_IMS_QPlan2QPTask, TYPE_IMS_QP_QPTASK, true, true, (short) 1, "", "", 0);
            getNextName(context, objectId, parent, depTask, "", 1, 1);

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }
        return returnMap;
    }

    private static MapList getRelatedMapList(Context context,
                                             DomainObject object, String relationship, String type,
                                             boolean from, boolean to, short level, String expressionObject,
                                             String expressionRelationship, int i) throws FrameworkException {

        StringList QAGbusSelects = new StringList();  // Object
        QAGbusSelects.add(DomainConstants.SELECT_ID);
        QAGbusSelects.add(DomainConstants.SELECT_NAME);
        QAGbusSelects.add(DomainConstants.SELECT_ORIGINATED);
        QAGbusSelects.add("attribute[IMS_SortOrder]");
        StringList QAGrelSelects = new StringList();  // Rel
        QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

        MapList result = object.getRelatedObjects(context, relationship, type, QAGbusSelects, QAGrelSelects, from, to, level, expressionObject, expressionRelationship, i);
        return result;
    }

    public MapList getFindObject(Context context, String type, String name, String revision, String expression) throws Exception {
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        MapList resultType = DomainObject.findObjects(context, type, name,
                revision, "*", "*", expression, true, objectSelects);
        resultType.addSortKey("name", "ascending", "string");
        resultType.sort();
        return resultType;
    }

    public MapList includeSearch(Context context, String[] args) {
        Map programMap = null;
        try {
            programMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error unpacking arguments: " + e.getMessage());
            e.printStackTrace();
        }
        String objectId = (String) programMap.get("objectId");

        String depId = null;
        try {
            depId = new DomainObject(objectId).getInfo(context, "to[IMS_QP_DEPTask2QPTask].from.id");
        } catch (Exception e) {
            LOG.error("error gitting dep id: " + e.getMessage());
            e.printStackTrace();
        }

        MapList relatedObjectList = new MapList();
        if (UIUtil.isNotNullAndNotEmpty(depId)) {

            try {
                relatedObjectList = new DomainObject(depId).getRelatedObjects(context,
                        /*relationship*/RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK,
                        /*type*/TYPE_IMS_QP_EXPECTED_RESULT,
                        /*object attributes*/ new StringList(DomainConstants.SELECT_ID),
                        /*relationship selects*/ null,
                        /*getTo*/ true, /*getFrom*/ false,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ "attribute[IMS_QP_ProjectStage]!=''&&attribute[IMS_QP_Baseline]!=''",
                        /*relationship where*/ null,
                        /*limit*/ 0);
                LOG.info(new DomainObject(objectId).getName(context) + "related with dep: " + new DomainObject(depId).getName(context) + " who's related count " + relatedObjectList.size() + " : " + relatedObjectList);

            } catch (Exception e) {
                LOG.error("error getting list of related tasks from dep: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return relatedObjectList;
    }

    public String dynamicFormTask(final Context context, final String[] args) {

        try {
            String objectId = args[0];
            DomainObject object = new DomainObject(objectId);

            return object.getInfo(context, IMS_Name) + "|" + object.getInfo(context, IMS_NameRU) + "|" + object.getInfo(context, IMS_DescriptionEn) + "|" + object.getInfo(context, IMS_DescriptionRu);

        } catch (Exception e) {
            return e.getMessage();
        }

    }

    public String dynamicForm(final Context context, final String[] args) {

        try {
            String objectId = args[0];
            DomainObject object = new DomainObject(objectId);
            String typeid = "", resultType = "", nameResult = "", nameFamily = "",
                    depId = object.getInfo(context, "from[" + RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK + "]").equals("TRUE") ? "from" : "to";
            typeid = object.getInfo(context, "to[" + RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.id");
            if (typeid != null && !typeid.equals("")) {
                resultType = new DomainObject(typeid).getInfo(context, SELECT_TO_IMS_QP_RESULT_TYPE_2_FAMILY_FROM_ID);
                nameResult = (resultType != null && !resultType.equals("")) ? new DomainObject(resultType).getInfo(context, "name") : "";
                nameFamily = new DomainObject(typeid).getInfo(context, "name");
            }
            return object.getInfo(context, IMS_DocumentCode)
                    + "|" + object.getInfo(context, IMS_Name)
                    + "|" + object.getInfo(context, IMS_NameRU)
                    + "|" + object.getInfo(context, IMS_DescriptionEn)
                    + "|" + object.getInfo(context, IMS_DescriptionRu)
                    + "|" + depId + "|" + nameResult + "|" + resultType
                    + "|" + nameFamily + "|" + typeid + "_" + resultType;

        } catch (Exception e) {
            LOG.error(e.getMessage());
            return e.getMessage();
        }

    }

    /**
     * @param context usual parameter
     * @param object1 DomainObject copied from
     * @param object2 DomainObject copied to
     * @throws MatrixException throwable Matrix database exception throwable
     */
    private void copyAttributes(Context context, DomainObject object1, DomainObject object2) throws MatrixException {
        object1.open(context);
        object2.open(context);

        BusinessObjectAttributes businessObjectAttributes = object1.getAttributes(context);
        AttributeList attributes = businessObjectAttributes.getAttributes();
        object2.setAttributes(context, attributes);
        object2.update(context);

        object1.close(context);
        object2.close(context);
    }

    @Deprecated
    public Map createExpectedResult(Context ctx, String[] args) {
        Map errorMessageMap = new HashMap();

        Map programMap = null;
        try {
            programMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting arguments: " + e.getMessage());
        }

        Map paramMap = (Map) programMap.get("paramMap");
        Map requestMap = (Map) programMap.get("requestMap");

        String deviation = (String) requestMap.get("deviation");
        String family = (String) requestMap.get("family");

        String parentOID = "";
        if (requestMap != null && !requestMap.isEmpty()) {
            parentOID = (String) requestMap.get("parentOID");
        }

        String type = null;
        try {
            //IMS_QP_DEPTask or IMS_QP_QPlan
            type = new DomainObject(parentOID).getType(ctx);
        } catch (Exception e) {
            LOG.error("error getting object: " + e.getMessage());
            e.printStackTrace();
        }

        if ("IMS_QP_DEPTask".equals(type)) {
            createExpectedResultFromDEPTask(ctx, args);
        } else if ("IMS_QP_QPlan".equals(type)) {
            createExpectedResultFromQPTask(ctx, args);
        }

        return errorMessageMap;
    }

    @Deprecated
    public Map createExpectedResultFromQPTask(Context context, String[] args) {
        Map returnMap = new HashMap();

        try {

            Map programMap = JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            Map paramMap = (Map) programMap.get("paramMap");

            String objectId = (String) requestMap.get("objectId");
            String newObjectId = (String) paramMap.get("objectId");
            String arrow = (String) requestMap.get("fromto");
            String family = (String) requestMap.get("family");
            DomainObject qpTask = new DomainObject(objectId);
            String relationship = RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK;

            MapList taskList = getRelatedMapList(context,
                    qpTask, relationship, TYPE_IMS_QP_EXPECTED_RESULT,
                    true, true, (short) 1, "", null, 0);

            DomainObject newObject = getNextName(context, newObjectId, qpTask, taskList, family, 0, 0);

            if (arrow.equals(FROM)) {
                DomainRelationship.connect(context, newObject, relationship, qpTask);
            } else {
                DomainRelationship.connect(context, qpTask, relationship, newObject);
            }

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }
        return returnMap;
    }

    @Deprecated
    public Map createExpectedResultFromDEPTask(Context context, String[] args) {
        Map returnMap = new HashMap();
        LOG.info("createExpectedResultFromDEPTask");
        try {

            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");
            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
            String DEPexpected = (String) requestMap.get("DEPexpectedOID");
            String relationship = RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK;
            DomainObject parent = new DomainObject(parentOID);
            MapList depTaskQP = getRelatedMapList(context,
                    parent, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, TYPE_IMS_QP_EXPECTED_RESULT,
                    true, true, (short) 1, "", null, 0);
            MapList depTaskDEP = getRelatedMapList(context,
                    parent, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK, TYPE_IMS_QP_EXPECTED_RESULT,
                    true, true, (short) 1, "", null, 0);

            depTaskQP.addAll(depTaskDEP);
            DomainObject newObject = getNextName(context, objectId, parent, depTaskQP, "", 0, 0);

            if (DEPexpected != null && !DEPexpected.equals("")) {
                DomainObject depTaskObject = new DomainObject(DEPexpected);

                DomainRelationship.connect(context, depTaskObject, RELATIONSHIP_IMS_DEPResult2QPResult, newObject);
                String relatedTypeQP = newObject.getInfo(context, "to[" + RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.id");
                if (relatedTypeQP == null || relatedTypeQP.equals("")) {
                    String relatedType = depTaskObject.getInfo(context, "to[" + RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.id");
                    if (relatedType != null && !relatedType.equals("")) {
                        DomainRelationship.connect(context, new DomainObject(relatedType), RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT, newObject);
                    }
                }

                copyAttributes(context, depTaskObject, newObject);
            }
            String fromto = (String) requestMap.get("fromto");

            if (fromto.equals(FROM)) {
                DomainRelationship.connect(context, newObject, relationship, parent);
            } else {
                DomainRelationship.connect(context, parent, relationship, newObject);
            }

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }
        return returnMap;
    }

    public StringList includeSearchDEP(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            StringList stringList = new StringList();
            MapList depMapList = getRelatedMapList(context,
                    new DomainObject(objectId), "IMS_QP_DEP2QPlan",
                    "*", true, true, (short) 1, "", "", 0);

            for (Object dep : depMapList) {
                Map depMap = (Map) dep;
                String depId = (String) depMap.get(DomainObject.SELECT_ID);
                MapList projectSpace = getRelatedMapList(context,
                        new DomainObject(depId), "IMS_QP_DEP2DEPProjectStage",
                        "*", true, true, (short) 1, "", "", 0);

                for (Object pS : projectSpace) {
                    Map pSMap = (Map) pS;
                    String pSId = (String) pSMap.get(DomainObject.SELECT_ID);
                    MapList subStage = getRelatedMapList(context,
                            new DomainObject(pSId), "IMS_QP_DEPProjectStage2DEPSubStage",
                            "*", true, true, (short) 1, "", "", 0);

                    for (Object sS : subStage) {
                        Map sSMap = (Map) sS;
                        String sSId = (String) sSMap.get(DomainObject.SELECT_ID);
                        MapList depTask = getRelatedMapList(context,
                                new DomainObject(sSId), "IMS_QP_DEPSubStage2DEPTask",
                                "*", true, true, (short) 1, "", "", 0);

                        for (Object dT : depTask) {
                            Map dTMap = (Map) dT;
                            stringList.add((String) dTMap.get(DomainObject.SELECT_ID));
                        }
                    }
                }
            }
            return stringList;

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    protected static void addFactExpTree(Context context, DomainObject depTask, int count) throws Exception {

        try {
            MapList subStageMapList = getRelatedMapList(context,
                    depTask, "IMS_QP_DEPSubStage2DEPTask",
                    "*", true, true, (short) 1, "", "", 0);
            setFactExp(context, depTask, count);
            for (Object subStageObject : subStageMapList) {
                Map subStageMap = (Map) subStageObject;
                String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                DomainObject subStage = new DomainObject(subStageId);
                MapList projectSpaceMapList = getRelatedMapList(context,
                        subStage, "IMS_QP_DEPProjectStage2DEPSubStage",
                        "*", true, true, (short) 1, "", "", 0);
                setFactExp(context, subStage, count);
                for (Object projectSpaceObject : projectSpaceMapList) {
                    Map projectSpaceMap = (Map) projectSpaceObject;
                    String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                    DomainObject projectSpace = new DomainObject(projectSpaceId);
                    MapList depMapList = getRelatedMapList(context,
                            projectSpace, "IMS_QP_DEP2DEPProjectStage",
                            "*", true, true, (short) 1, "", "", 0);
                    setFactExp(context, projectSpace, count);
                    for (Object depObject : depMapList) {
                        Map depMap = (Map) depObject;
                        String depID = (String) depMap.get(DomainObject.SELECT_ID);
                        DomainObject dep = new DomainObject(depID);
                        setFactExp(context, dep, count);
                    }
                }
            }

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    protected static void setFactExp(Context context, DomainObject object, int count) throws Exception {
        try {

            int oldAttribute = Integer.parseInt(object.getInfo(context, "attribute[" + ATTRIBUTE_IMS_QP_FACT_EXP + "]")) + count;
            object.setAttributeValue(context, ATTRIBUTE_IMS_QP_FACT_EXP, Integer.toString(oldAttribute));


        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    private void setFactGot(Context context, DomainObject object, int count) throws Exception {

        try {
            int oldAttribute = Integer.parseInt(object.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_FACT_GOT)) + count;
            object.setAttributeValue(context, ATTRIBUTE_IMS_QP_FACT_GOT, Integer.toString(oldAttribute));

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    public boolean accessDEPTask(Context context, String[] args) throws FrameworkException {

        try {
            Map programMap = JPO.unpackArgs(args);
            return true;

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    public int delFact(Context context, String[] args) throws Exception {

        try {
            String objectId = args[0];
            DomainObject task = new DomainObject(objectId);
            int factexp = Integer.parseInt(task.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_FACT_EXP));
            int factgot = Integer.parseInt(task.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_FACT_GOT));
            MapList depTask = getRelatedMapList(context,
                    task, RELATIONSHIP_IMS_DEPTask2QPTask,
                    "*", true, true, (short) 1, "", "", 0);
            for (Object depObject : depTask) {

                Map depMap = (Map) depObject;
                String depID = (String) depMap.get(DomainObject.SELECT_ID);
                DomainObject dep = new DomainObject(depID);
                if (factgot == 0) {
                    delFactExpTree(context, dep, -1, 0);
                } else {
                    delFactExpTree(context, dep, -1, -1);
                }
            }

            MapList qpPlan = getRelatedMapList(context,
                    task, RELATIONSHIP_IMS_QPlan2QPTask,
                    "*", true, true, (short) 1, "", "", 0);
            for (Object qpObject : qpPlan) {
                Map qpMap = (Map) qpObject;

                String qpID = (String) qpMap.get(DomainObject.SELECT_ID);
                DomainObject qp = new DomainObject(qpID);
                setFactExp(context, qp, -1);
                if (factgot == 0) {
                    setFactGot(context, qp, 0);
                } else {
                    setFactGot(context, qp, -1);
                }
            }

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    private void delFactExpTree(Context context, DomainObject depTask, int count, int countgot) throws Exception {

        try {
            MapList subStageMapList = getRelatedMapList(context,
                    depTask, "IMS_QP_DEPSubStage2DEPTask",
                    "*", true, true, (short) 1, "", "", 0);
            setFactExp(context, depTask, count);
            setFactGot(context, depTask, countgot);
            for (Object subStageObject : subStageMapList) {
                Map subStageMap = (Map) subStageObject;
                String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                DomainObject subStage = new DomainObject(subStageId);
                MapList projectSpaceMapList = getRelatedMapList(context,
                        subStage, "IMS_QP_DEPProjectStage2DEPSubStage",
                        "*", true, true, (short) 1, "", "", 0);
                setFactExp(context, subStage, count);
                setFactGot(context, subStage, countgot);
                for (Object projectSpaceObject : projectSpaceMapList) {
                    Map projectSpaceMap = (Map) projectSpaceObject;
                    String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                    DomainObject projectSpace = new DomainObject(projectSpaceId);
                    MapList depMapList = getRelatedMapList(context,
                            projectSpace, "IMS_QP_DEP2DEPProjectStage",
                            "*", true, true, (short) 1, "", "", 0);
                    setFactExp(context, projectSpace, count);
                    setFactGot(context, projectSpace, countgot);
                    for (Object depObject : depMapList) {
                        Map depMap = (Map) depObject;
                        String depID = (String) depMap.get(DomainObject.SELECT_ID);
                        DomainObject dep = new DomainObject(depID);
                        setFactExp(context, dep, count);
                        setFactGot(context, dep, countgot);
                    }
                }
            }

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    public void treeFact(Context context, DomainObject qpTask, int count) throws Exception {

        try {
            setFactGot(context, qpTask, count);

            MapList depTaskMapList = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_DEPTask2QPTask, "*", true, true, (short) 1, "", "", 0);
            for (Object depTaskObject : depTaskMapList) {
                Map depTaskMap = (Map) depTaskObject;
                String depTaskID = (String) depTaskMap.get(DomainObject.SELECT_ID);
                DomainObject depTask = new DomainObject(depTaskID);

                MapList subStageMapList = getRelatedMapList(context,
                        depTask, "IMS_QP_DEPSubStage2DEPTask",
                        "*", true, true, (short) 1, "", "", 0);
                setFactGot(context, depTask, count);
                for (Object subStageObject : subStageMapList) {
                    Map subStageMap = (Map) subStageObject;
                    String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                    DomainObject subStage = new DomainObject(subStageId);
                    MapList projectSpaceMapList = getRelatedMapList(context,
                            subStage, "IMS_QP_DEPProjectStage2DEPSubStage",
                            "*", true, true, (short) 1, "", "", 0);
                    setFactGot(context, subStage, count);
                    for (Object projectSpaceObject : projectSpaceMapList) {
                        Map projectSpaceMap = (Map) projectSpaceObject;
                        String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                        DomainObject projectSpace = new DomainObject(projectSpaceId);
                        MapList depMapList = getRelatedMapList(context,
                                projectSpace, "IMS_QP_DEP2DEPProjectStage",
                                "*", true, true, (short) 1, "", "", 0);
                        setFactGot(context, projectSpace, count);
                        for (Object depObject : depMapList) {
                            Map depMap = (Map) depObject;
                            String depID = (String) depMap.get(DomainObject.SELECT_ID);
                            DomainObject dep = new DomainObject(depID);
                            setFactGot(context, dep, count);
                        }
                    }
                }
            }

            MapList qpPlan = getRelatedMapList(context,
                    qpTask, RELATIONSHIP_IMS_QPlan2QPTask,
                    "*", true, true, (short) 1, "", "", 0);
            for (Object qpObject : qpPlan) {
                Map qpMap = (Map) qpObject;
                String qpID = (String) qpMap.get(DomainObject.SELECT_ID);
                DomainObject qp = new DomainObject(qpID);
                setFactGot(context, qp, count);
            }
        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    public int disconnectFact(Context context, String[] args) throws Exception {
        try {
            String taskId = args[0];
            DomainObject task = new DomainObject(taskId);
            if (task.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_CloseStatus).equals("Full")) {
                task.setAttributeValue(context, ATTRIBUTE_IMS_QP_CLOSE_STATUS, VALUE_NO);
                setAttributeClosedTaskTreeNo(context, task);

            }
            //        treeFact(context, task, -1);
        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public int connectFact(Context context, String[] args) throws Exception {

        try {
            String taskId = args[0];
            String factId = args[1];
            DomainObject fact = new DomainObject(factId);
            if (fact.getInfo(context, DomainObject.SELECT_TYPE).equals("IMS_ExternalDocumentSet")) {
                if (fact.getInfo(context, "attribute[IMS_ProjDocStatus]").equals("Finalized")) {
                    setAttributeClosedTask(context, new DomainObject(taskId));
                }
            } else {
                if (fact.getInfo(context, "current").equals("Approved")) {
                    setAttributeClosedTask(context, new DomainObject(taskId));
                }
            }
        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public static StringList getFactColumnStyle(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        MapList mlObList = (MapList) programMap.get("objectList");
        StringList returnList = new StringList();
        for (Object objTemp : mlObList) {
            String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
            DomainObject object = new DomainObject(sID);
            int factExp = Integer.parseInt(object.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_FACT_EXP));
            int factGot = Integer.parseInt(object.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_FACT_GOT));

            String color = "";

            //default priority is 0: no color or fact coloring

            //1 if the task is 'Another' type
            String hasFact = object.getInfo(context,
                    String.format("from[%s]", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
            String resultType = object.getInfo(context,
                    "from[IMS_QP_ExpectedResult2QPTask].to.to[IMS_QP_ResultType2ExpectedResult].from.to[IMS_QP_ResultType2Family].from.name");
            boolean anotherTypeAndNoFact = IMS_QP_Constants_mxJPO.ANOTHER_PLAN_TYPES.equals(resultType) && "FALSE".equals(hasFact);
            if (anotherTypeAndNoFact) color = "IMS_QP_Blue";

            //2 if attribute of task IMS_QP_SelectDocument has any values
            if (UIUtil.isNotNullAndNotEmpty(object.getInfo(context, IMS_QP_Constants_mxJPO.attribute_IMS_QP_SelectDocument)))
                color = "IMS_QP_Purple";

            //3 if task has relationship to fact but that fact isn't in 'Finalized' state or 'Approved' for CheckList type
            String factStatus = object.getInfo(context, String.format("from[%s].to.attribute[IMS_ProjDocStatus]", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
            String factState = object.getInfo(context, String.format("from[%s].to.current", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
            boolean checkFactStatus = UIUtil.isNotNullAndNotEmpty(factStatus) && factStatus.equals("Finalized") ||
                    UIUtil.isNotNullAndNotEmpty(factState) && factState.contains("Approved");

            if ("TRUE".equals(hasFact) && !checkFactStatus) {
                color = "IMS_QP_Red";
            }

            //4 if attribute of expected result IMS_QP_DocumentCode contains value 'Wrong'
            String wrongCodeField = object.getInfo(context, String.format("from[%s].to.%s",
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask,
                    IMS_QP_Constants_mxJPO.attribute_IMS_QP_DocumentCode));

            String errorCodeField = object.getInfo(context, String.format(
                    "attribute[%s]", IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO));
            if (UIUtil.isNotNullAndNotEmpty(wrongCodeField) && wrongCodeField.contains("Wrong code")
                    || UIUtil.isNotNullAndNotEmpty(errorCodeField))
                color = "IMS_QP_Orange";

            //5 if the task has more than one expected result in direction 'Output'
            String moreThanOneExpectedRelations = MqlUtil.mqlCommand(context, String.format("print bus %s select from[IMS_QP_ExpectedResult2QPTask].to.id dump |", object.getId(context)));
            if (UIUtil.isNotNullAndNotEmpty(moreThanOneExpectedRelations) && moreThanOneExpectedRelations.contains("|") || errorCodeField.contains("4.1"))
                color = "IMS_QP_Yellow";
            getColor(returnList, factExp, factGot, color);

        }
        return returnList;
    }

    static void getColor(StringList returnList, int factExp, int factGot, String color) {

        switch (color) {
            case "IMS_QP_Purple":
                returnList.add("IMS_QP_Purple");
                break;
            case "IMS_QP_Yellow":
                returnList.add("IMS_QP_Yellow");
                break;
            case "IMS_QP_Blue":
                returnList.add("IMS_QP_Blue");
                break;
            case "IMS_QP_Orange":
                returnList.add("IMS_QP_Orange");
                break;
            case "IMS_QP_Red":
                returnList.add("IMS_QP_Red");
                break;
            default:
                if (UIUtil.isNullOrEmpty(color)) {
                    if (factExp > 0) {
                        if (factExp == factGot) {
                            returnList.add("IMS_QP_Green");
                        } else if (factExp > factGot) {
                            returnList.add("IMS_QP_Rose");
                        } else {
                            returnList.add("");
                        }
                    } else {
                        returnList.add("");
                    }
                }
        }
    }

    public Object getNameColumn(Context context, String[] args) throws Exception {

        Map programMap = JPO.unpackArgs(args);
        MapList mlObList = (MapList) programMap.get("objectList");
        Vector returnList = new Vector();
        for (Object objTemp : mlObList) {
            String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
            DomainObject object = new DomainObject(sID);
            returnList.add(object.getInfo(context, DomainObject.SELECT_NAME));
        }
        return returnList;
    }

    public void setTreeFactGot(Context context, String[] args) throws Exception {

        try {
            DomainObject qpTask = new DomainObject(args[0]);
            setFactGot(context, qpTask, 1);

            MapList depTaskMapList = getRelatedMapList(context,
                    qpTask, RELATIONSHIP_IMS_DEPTask2QPTask,
                    "*", true, true, (short) 1, "", "", 0);
            for (Object depTaskObject : depTaskMapList) {
                Map depTaskMap = (Map) depTaskObject;
                String depTaskID = (String) depTaskMap.get(DomainObject.SELECT_ID);
                DomainObject depTask = new DomainObject(depTaskID);

                MapList subStageMapList = getRelatedMapList(context,
                        depTask, "IMS_QP_DEPSubStage2DEPTask",
                        "*", true, true, (short) 1, "", "", 0);
                setFactGot(context, depTask, 1);
                for (Object subStageObject : subStageMapList) {
                    Map subStageMap = (Map) subStageObject;
                    String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                    DomainObject subStage = new DomainObject(subStageId);
                    MapList projectSpaceMapList = getRelatedMapList(context,
                            subStage, "IMS_QP_DEPProjectStage2DEPSubStage",
                            "*", true, true, (short) 1, "", "", 0);
                    setFactGot(context, subStage, 1);
                    for (Object projectSpaceObject : projectSpaceMapList) {
                        Map projectSpaceMap = (Map) projectSpaceObject;
                        String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                        DomainObject projectSpace = new DomainObject(projectSpaceId);
                        MapList depMapList = getRelatedMapList(context,
                                projectSpace, "IMS_QP_DEP2DEPProjectStage",
                                "*", true, true, (short) 1, "", "", 0);
                        setFactGot(context, projectSpace, 1);
                        for (Object depObject : depMapList) {
                            Map depMap = (Map) depObject;
                            String depID = (String) depMap.get(DomainObject.SELECT_ID);
                            DomainObject dep = new DomainObject(depID);
                            setFactGot(context, dep, 1);
                        }
                    }
                }
            }

            MapList qpPlan = getRelatedMapList(context,
                    qpTask, RELATIONSHIP_IMS_QPlan2QPTask,
                    "*", true, true, (short) 1, "", "", 0);
            for (Object qpObject : qpPlan) {
                Map qpMap = (Map) qpObject;
                String qpID = (String) qpMap.get(DomainObject.SELECT_ID);
                DomainObject qp = new DomainObject(qpID);
                setFactGot(context, qp, 1);
            }

        } catch (Exception ex) {
            LOG.error("setTreeFactGot: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void setTreeFactExp(Context context, String[] args) throws Exception {

        try {
            DomainObject qpTask = new DomainObject(args[0]);

            setFactExp(context, qpTask, 1);

            MapList depTaskMapList = getRelatedMapList(context,
                    qpTask, RELATIONSHIP_IMS_DEPTask2QPTask,
                    "*", true, true, (short) 1, "", "", 0);
            for (Object depTaskObject : depTaskMapList) {
                Map depTaskMap = (Map) depTaskObject;
                String depTaskID = (String) depTaskMap.get(DomainObject.SELECT_ID);
                DomainObject depTask = new DomainObject(depTaskID);

                MapList subStageMapList = getRelatedMapList(context,
                        depTask, "IMS_QP_DEPSubStage2DEPTask",
                        "*", true, true, (short) 1, "", "", 0);
                setFactExp(context, depTask, 1);
                for (Object subStageObject : subStageMapList) {
                    Map subStageMap = (Map) subStageObject;
                    String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                    DomainObject subStage = new DomainObject(subStageId);
                    MapList projectSpaceMapList = getRelatedMapList(context,
                            subStage, "IMS_QP_DEPProjectStage2DEPSubStage",
                            "*", true, true, (short) 1, "", "", 0);
                    setFactExp(context, subStage, 1);
                    for (Object projectSpaceObject : projectSpaceMapList) {
                        Map projectSpaceMap = (Map) projectSpaceObject;
                        String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                        DomainObject projectSpace = new DomainObject(projectSpaceId);
                        MapList depMapList = getRelatedMapList(context,
                                projectSpace, "IMS_QP_DEP2DEPProjectStage",
                                "*", true, true, (short) 1, "", "", 0);
                        setFactExp(context, projectSpace, 1);
                        for (Object depObject : depMapList) {
                            Map depMap = (Map) depObject;
                            String depID = (String) depMap.get(DomainObject.SELECT_ID);
                            DomainObject dep = new DomainObject(depID);
                            setFactExp(context, dep, 1);
                        }
                    }
                }
            }

            MapList qpPlan = getRelatedMapList(context,
                    qpTask, RELATIONSHIP_IMS_QPlan2QPTask,
                    "*", true, true, (short) 1, "", "", 0);
            for (Object qpObject : qpPlan) {
                Map qpMap = (Map) qpObject;
                String qpID = (String) qpMap.get(DomainObject.SELECT_ID);
                DomainObject qp = new DomainObject(qpID);
                setFactExp(context, qp, 1);
            }

        } catch (Exception ex) {
            LOG.error("setTreeFactExp: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public String getConnectedQPCheckListHTML(Context context, String[] args) throws Exception {
        DomainObject qpTaskObject = IMS_KDD_mxJPO.getObjectFromParamMap(args);

        Map externalDocumentSetMap = qpTaskObject.getRelatedObject(
                context,
                RELATIONSHIP_IMS_QP_QPTask2Fact, true,
                new StringList(new String[]{
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_NAME,
                }),
                null);

        StringBuilder sb = new StringBuilder("");

        if (externalDocumentSetMap != null) {

            sb.append(String.format(
                    "<a href=\"javascript:%s\">%s</a>",
                    String.format(
                            "emxTableColumnLinkClick('../common/emxForm.jsp?form=type_IMS_QP_CheckList&toolbar=IMS_QP_CheckListToolbar&HelpMarker=emxhelpdocumentproperties&formHeader=emxComponents.Common.PropertiesPageHeading&subHeader=emxComponents.Menu.SubHeaderDocuments&Export=False&&displayCDMFileSummary=true&objectId=%s')",
                            IMS_KDD_mxJPO.getIdFromMap(externalDocumentSetMap)),
                    HtmlEscapers.htmlEscaper().escape(IMS_KDD_mxJPO.getNameFromMap(externalDocumentSetMap))));
        } else {
            if (IMS_QP_Security_mxJPO.isUserViewerWithChild(context)) {
                sb.append(String.format(
                        "<a href=\"javascript:%s\"><img src=\"%s\" title=\"%s\" /></a>",
                        String.format(
                                "window.open('emxCreate.jsp?type=type_IMS_QP_CheckList&typeChooser=false&form=IMS_QP_Create_CheckList&findMxLink=false&relationship=relationship_IMS_QP_QPTask2Fact&policy=policy_IMS_QP_CheckList&submitAction=refreshCaller&postProcessURL=../common/IMS_PassFilesToJPO.jsp&objectId=%s', '_blank', 'height=800,width=1000,toolbar=0,location=0,menubar=0')", qpTaskObject.getId(context)),
                        IMS_KDD_mxJPO.FUGUE_16x16 + "plus.png",
                        "Create and Connect CheckList"));
            }
        }

        return sb.toString();
    }

    public String disconnectCheckList(Context context, String[] args) throws Exception {
        new DomainObject(args[1]).deleteObject(context);
        return "";

    }

    public static String addExtensionScript(Context context, String[] args) throws Exception {
        Map fieldMap = ((Map) ((Map) JPO.unpackArgs(args)).get("fieldMap"));
        Map settings = ((Map) fieldMap.get("settings"));
        String regSuite = (String) settings.get("Registered Suite");
        String qpScript = (String) settings.get("qpScript");
        String columnName = (String) fieldMap.get("name");
        String appDir = FrameworkProperties.getProperty(context, "eServiceSuite" + regSuite + ".Directory");
        Calendar cal = Calendar.getInstance();
        String scriptPath = "../" + appDir + "/" + qpScript + "?" + cal.getTimeInMillis();
        return "<div id='" + columnName + "Script'></div><script src=\"" + scriptPath + "\"></script><script>$('#" + columnName + "Script').closest('tr').hide();</script>";
    }

    public Map attachFile(Context context, String[] args) throws Exception {
        HashMap programMap = JPO.unpackArgs(args);
        InputStream inp = (InputStream) programMap.get("inp");
        String sFileName = (String) programMap.get("fileName");
        String sObjectId = (String) programMap.get("objectId");
        return attachFileGeneral(context, sObjectId, sFileName, inp, args);
    }

    public Map attachFileGeneral(Context context, String objectId, String fileName, InputStream inp, String[] args) {
        Map map = new HashMap();

        try {
            ContextUtil.startTransaction(context, true);

            //create two copies of one InputStream: one for checkin, another for parsing
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inp.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }
            baos.flush();
            InputStream inp1 = new ByteArrayInputStream(baos.toByteArray());

            String sPath = context.createWorkspace();
            File file = new File(sPath + "/" + fileName);
            FileUtils.copyInputStreamToFile(inp1, file);

            HashMap uploadParamsMap = new HashMap();
            uploadParamsMap.put("objectId", objectId);
            uploadParamsMap.put("noOfFiles", "1");
            uploadParamsMap.put("fileName0", fileName);
            uploadParamsMap.put("objectAction", CommonDocument.OBJECT_ACTION_UPDATE_MASTER);
            String[] args1 = JPO.packArgs(uploadParamsMap);

            String jpoName = "emxCommonDocument";
            String methodName = "commonDocumentCheckin";

            JPO.invoke(context, jpoName, null, methodName, args1, Map.class);

            FileUtils.deleteQuietly(file);
            inp1.close();
            ContextUtil.commitTransaction(context);
            map.put("message", "");
        } catch (Exception exception) {
            ContextUtil.abortTransaction(context);
            map.put("message", "");
        }

        return map;
    }

    public boolean accessDocument(Context ctx, String[] args) throws FrameworkException {
        try {

            Map argsMap = JPO.unpackArgs(args);
            Map settings = (Map) argsMap.get("SETTINGS");

            String objectId = (String) argsMap.get("objectId");
            DomainObject object = new DomainObject(objectId);
            MapList expectedResult = getRelatedMapList(ctx,
                    object,
                    RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK,
                    "*",
                    false,
                    true,
                    (short) 1,
                    "to[" + RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.name!='CL-1'",
                    "",
                    0
            );
            /*String hasAttributes = object.getAttributeValue(ctx, "IMS_QP_ProjectStage") + object.getAttributeValue(ctx, "IMS_QP_Baseline");*/
            String key = (String) settings.get("key");
            return UIUtil.isNotNullAndNotEmpty(key) && key.equals("doc") && expectedResult.size() != 0/*|| UIUtil.isNotNullAndNotEmpty(hasAttributes)*/;

        } catch (Exception ex) {
            LOG.error("accessDocument error: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public void setAttributeClosedTask(Context context, DomainObject qpTask) throws Exception {
        try {
            String full = "Full";
            if (isFactFull(context, qpTask)) {
                MapList outputQPTask = getRelatedMapList(context,
                        qpTask, RELATIONSHIP_IMS_QP_QPTask2QPTask,
                        "*", true, false, (short) 1, "", "attribute[IMS_QP_DEPTaskStatus]==Approved", 0);

                for (Object objTemp : outputQPTask) {
                    String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
                    DomainObject relTask = new DomainObject(sID);
                    String closed = relTask.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_CLOSE_STATUS);
                    if (!closed.equals("Full")) {
                        full = VALUE_NO;
                        break;
                    }
                }

                if (full.equals("Full")) {
                    qpTask.setAttributeValue(context, ATTRIBUTE_IMS_QP_CLOSE_STATUS, "Full");
                    setAttributeClosedTaskTree(context, qpTask);
                }
            }
        } catch (Exception ex) {
            LOG.error("setAttributeClosedTask: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public void setAttributeClosedTaskTree(Context context, DomainObject qpTask) throws Exception {
        try {
            MapList inputQPTask = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_QP_QPTask2QPTask, "*", false, true, (short) 1, "", "attribute[IMS_QP_DEPTaskStatus]==Approved", 0);
            for (Object inputMap : inputQPTask) {
                String full = "Full";
                String inputId = (String) ((Map) inputMap).get(DomainObject.SELECT_ID);
                DomainObject inputObject = new DomainObject(inputId);
                if (isFactFull(context, inputObject)) {
                    MapList outputQPTask = getRelatedMapList(context, inputObject, RELATIONSHIP_IMS_QP_QPTask2QPTask, "*", true, false, (short) 1, "", "attribute[IMS_QP_DEPTaskStatus]==Approved", 0);
                    for (Object outputMap : outputQPTask) {
                        String outputId = (String) ((Map) outputMap).get(DomainObject.SELECT_ID);
                        DomainObject outputObject = new DomainObject(outputId);
                        String closed = outputObject.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_CLOSE_STATUS);
                        if (!closed.equals("Full")) {
                            full = VALUE_NO;
                            break;
                        }
                    }
                    if (full.equals("Full")) {
                        inputObject.setAttributeValue(context, ATTRIBUTE_IMS_QP_CLOSE_STATUS, "Full");

                        setAttributeClosedTaskTree(context, inputObject);
                    }
                }
            }

        } catch (Exception ex) {
            LOG.error("setAttributeClosedTaskTree: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public boolean isFactFull(Context context, DomainObject qpTask) throws Exception {

        try {
            MapList fact = getRelatedMapList(context,
                    qpTask, RELATIONSHIP_IMS_QP_QPTask2Fact,
                    "*", true, true, (short) 1,
                    "(type==IMS_QP_CheckList AND current==Approved) OR (type==IMS_ExternalDocumentSet AND attribute[IMS_ProjDocStatus]==Finalized)", "", 0);
            return fact.size() > 0;

        } catch (Exception ex) {
            LOG.error("isFactFull: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }

    public int promoteCheckList(Context context, String[] args) throws Exception {

        try {
            String checklistID = args[0];
            DomainObject checkList = new DomainObject(checklistID);
            StringList qpTaskIDs = checkList.getInfoList(context, "to[IMS_QP_QPTask2Fact].from.id");
            for (Object qpTaskID : qpTaskIDs) {
                setAttributeClosedTask(context, new DomainObject((String) qpTaskID));
            }

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public int demoteCheckList(Context context, String[] args) throws Exception {

        try {
            String checklistID = args[0];
            DomainObject checkList = new DomainObject(checklistID);
            StringList qpTaskIDs = checkList.getInfoList(context, "to[IMS_QP_QPTask2Fact].from.id");
            for (Object qpTaskID : qpTaskIDs) {
                DomainObject task = new DomainObject((String) qpTaskID);
                if (task.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_CloseStatus).equals("Full")) {
                    task.setAttributeValue(context, ATTRIBUTE_IMS_QP_CLOSE_STATUS, VALUE_NO);
                    setAttributeClosedTaskTreeNo(context, task);
                }
            }

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public int modifyAttributeClosedStatus(Context context, String[] args) throws Exception {
        try {

            String qpTaskId = args[0];
            String valuenew = args[1];
            String value = args[2];

            DomainObject qpTask = new DomainObject(qpTaskId);
            if (!value.equals("Full") && valuenew.equals("Full")) {
                treeFact(context, qpTask, 1);
            } else if (value.equals("Full") && !valuenew.equals("Full")) {
                treeFact(context, qpTask, -1);
            }
        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public void setAttributeClosedTaskTreeNo(Context context, DomainObject qpTask) throws Exception {

        try {
            MapList outputQPTask = getRelatedMapList(context,
                    qpTask, RELATIONSHIP_IMS_QP_QPTask2QPTask,
                    "*", false, true, (short) 1, "", "attribute[IMS_QP_DEPTaskStatus]==Approved", 0);
            for (Object inputMap : outputQPTask) {
                String inputId = (String) ((Map) inputMap).get(DomainObject.SELECT_ID);

                DomainObject inputObject = new DomainObject(inputId);
                String status = inputObject.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_CloseStatus);
                if (status.equals("Full")) {
                    inputObject.setAttributeValue(context, ATTRIBUTE_IMS_QP_CLOSE_STATUS, VALUE_NO);

                    setAttributeClosedTaskTreeNo(context, inputObject);
                }
            }

        } catch (Exception ex) {
            LOG.error(ex.getMessage());
            throw ex;
        }
    }

    /**
     * Method to deleting IMS_QP_DEPSubStages if their hasn't any SubTasks
     *
     * @param context
     * @param args
     */
    public Map deleteTasks(Context context, String[] args) {

        //get all ids
        Map<String, Object> argsMap = null;

        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (argsMap.get("emxTableRowId") != null) ? (String[]) argsMap.get("emxTableRowId") : new String[0];
        String[] taskIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            taskIDs[i] = rowIDs[i].substring(0, rowIDs[i].indexOf("|"));
        }

        //select all tasks
        StringList selects = new StringList();
        selects.add("id");
        selects.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPTASK_2_DEPTASK_ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
        selects.add("to[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]");
        selects.add("from[IMS_QP_DEPTask2QPTask]");
        selects.add("name");

        MapList objectsInfo = new MapList();
        try {
            objectsInfo = DomainObject.getInfo(context, taskIDs, selects);
            LOG.info("objectsInfo: " + objectsInfo);
        } catch (FrameworkException e) {
            LOG.error("error getting info: " + e.getMessage());
            e.printStackTrace();
        }

        //check if substage has some task in state Approved
        List<String> deletingIDs = new ArrayList();

        StringBuffer buffer = new StringBuffer(EnoviaResourceBundle.getProperty(context,
                "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.couldntDelete"));
        List<String> badNames = new ArrayList<>();
        boolean flag = false;
        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            String taskStates = "" + map.get(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPTASK_2_DEPTASK_ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
            String qpTaskRelation = "" + map.get("from[IMS_QP_DEPTask2QPTask]");
            if (taskStates.contains(VALUE_APPROVED) || qpTaskRelation.equals("TRUE")) {
                buffer.append(map.get("name")).append("\n");
                badNames.add((String) map.get("name"));
                flag = true;
            } else {
                deletingIDs.add((String) map.get("id"));
            }
        }

        Map mapMessage = new HashMap();
        if (flag) {
            mapMessage.put("array", badNames);
        }

        String[] var1 = new String[deletingIDs.size()];
        for (int i = 0; i < deletingIDs.size(); i++) {
            var1[i] = deletingIDs.get(i);
        }

        //delete tasks
        try {
            if (var1.length > 0)
                DomainObject.deleteObjects(context, var1);
            LOG.info("objects for deleting: " + Arrays.deepToString(var1));
        } catch (Exception e) {
            LOG.error("deleting error: " + e.getMessage());
            e.printStackTrace();
        }

        LOG.info("return map: " + mapMessage);
        return mapMessage;
    }

    /**
     * @param context
     * @param args
     * @return
     */
    @com.matrixone.apps.framework.ui.ProgramCallable
    public int setCounterType(Context context, String[] args) {
        try {
            String objID = args[0];
            DomainObject obj = DomainObject.newInstance(context, objID);
            String name = obj.getName(context);
            int numberInt = Integer.parseInt(name.substring(name.lastIndexOf("-") + 1));
            obj.setAttributeValue(context, ATTRIBUTE_IMS_SORT_ORDER, String.valueOf(numberInt));
        } catch (Exception ex) {
            LOG.error("setCounterType Error: " + ex.getMessage());
        }
        return 0;
    }

    public void setCounterForDEPTasksAndExpectedResult(Context context, String[] args) throws Exception {

        try {
            MapList resultTasks = getFindObject(context, "IMS_QP_DEPTask", "*",
                    "*", "");
            System.out.println("I found Tasks " + resultTasks.size());
            MapList resultExpectedResult = getFindObject(context, "IMS_QP_ExpectedResult", "*",
                    "*", "");
            System.out.println("I found ExpectedRes " + resultExpectedResult.size());

            resultTasks.addAll(resultExpectedResult);

            for (Object resultObject : resultTasks) {
                Map objTemp = (Map) resultObject;
                String name = ((String) objTemp.get(DomainConstants.SELECT_NAME));
                try {
                    if (name.contains("-")) {
                        int numberInt = Integer.parseInt(name.substring(name.lastIndexOf("-") + 1));
                        new DomainObject((String) objTemp.get(DomainConstants.SELECT_ID)).setAttributeValue(context, ATTRIBUTE_IMS_SORT_ORDER, String.valueOf(numberInt));
                    } else {
                        new DomainObject((String) objTemp.get(DomainConstants.SELECT_ID)).setAttributeValue(context, ATTRIBUTE_IMS_SORT_ORDER, "0");
                    }
                } catch (Exception e) {
                    new DomainObject((String) objTemp.get(DomainConstants.SELECT_ID)).setAttributeValue(context, ATTRIBUTE_IMS_SORT_ORDER, "0");
                    System.out.println(e.fillInStackTrace());
                }
            }
        } catch (Exception ex) {
            LOG.error(ex.fillInStackTrace());
            throw ex;
        }
    }

    public int addFactTriggerCreateType(Context context, String[] args) throws Exception {

        try {
            String objectId = args[0];
            DomainObject qptask = new DomainObject(objectId);
            LOG.info("before arror: ");
            qptask.setAttributeValue(context, ATTRIBUTE_IMS_QP_FACT_EXP, "1");

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }

        LOG.info("get error");
        return 0;
    }

    public int addFactTriggerConnectDep(Context context, String[] args) throws Exception {

        try {
            String qptaskid = args[0];
            String deptaskid = args[1];
            DomainObject deptask = new DomainObject(deptaskid);
            addFactExpTree(context, deptask, 1);

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public int addFactTriggerConnectQPLan(Context context, String[] args) throws Exception {

        try {
            String qptaskid = args[0];
            String qplanid = args[1];
            DomainObject qplan = new DomainObject(qplanid);
            setFactExp(context, qplan, 1);

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }


    public int modifyAttributeDEPTaskStatus(Context context, String[] args) throws Exception {
        try {

            String qpTaskId = args[0];
            String valuenew = args[1];
            String value = args[2];

            DomainObject qpTask = new DomainObject(qpTaskId);
            if (!value.equals(VALUE_APPROVED) && valuenew.equals(VALUE_APPROVED)) {
                if (isFactFull(context, qpTask)) {
                    setAttributeClosedTask(context, qpTask);
                }
            } else if (value.equals(VALUE_APPROVED) && !valuenew.equals(VALUE_APPROVED)) {
                if (!isFactFull(context, qpTask)) {
                    setAttributeClosedTaskTreeNo(context, qpTask);
                }
            }

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public int modifyAttributeProjDocStatus(Context context, String[] args) throws Exception {
        try {

            String documentId = args[0];
            String valuenew = args[1];
            String value = args[2];

            DomainObject document = new DomainObject(documentId);

            if (!value.equals("Finalized") && valuenew.equals("Finalized")) {
                StringList qpTaskIDs = document.getInfoList(context, "to[IMS_QP_QPTask2Fact].from.id");
                for (Object qpTaskID : qpTaskIDs) {
                    setAttributeClosedTask(context, new DomainObject((String) qpTaskID));
                }
            } else if (value.equals("Finalized") && !valuenew.equals("Finalized")) {
                StringList qpTaskIDs = document.getInfoList(context, "to[IMS_QP_QPTask2Fact].from.id");
                for (Object qpTaskID : qpTaskIDs) {
                    DomainObject qpTask = new DomainObject((String) qpTaskID);
                    qpTask.setAttributeValue(context, ATTRIBUTE_IMS_QP_CLOSE_STATUS, VALUE_NO);
                    setAttributeClosedTaskTreeNo(context, qpTask);
                }
            }

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public void setAttributeClosedTask(Context context, String[] args) throws Exception {
        setAttributeClosedTask(context, new DomainObject(args[0]));
    }

    public int addInputTask(Context context, String[] args) throws Exception {

        try {
            String toTaskID = args[0];
            String relID = args[1];
            String fromTaskID = args[2];
            DomainRelationship rel = new DomainRelationship(relID);
            String attribute = rel.getAttributeValue(context, "IMS_QP_DEPTaskStatus");
            if (attribute.equals("Approved")) {
                DomainObject fromTask = new DomainObject(fromTaskID);
                DomainObject toTask = new DomainObject(toTaskID);
                if ((isFactFull(context, toTask)) && (!isFactFull(context, fromTask))) {
                    toTask.setAttributeValue(context, ATTRIBUTE_IMS_QP_CLOSE_STATUS, VALUE_NO);
                    setAttributeClosedTaskTreeNo(context, toTask);
                }
            }

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

    public int delInputTask(Context context, String[] args) throws Exception {

        try {
            String toTaskID = args[0];
            String relID = args[1];
            String fromTaskID = args[2];
            DomainRelationship rel = new DomainRelationship(relID);
            String attribute = rel.getAttributeValue(context, "IMS_QP_DEPTaskStatus");
            DomainObject toTask = new DomainObject(toTaskID);
            DomainObject fromTask = new DomainObject(fromTaskID);
            if ((attribute.equals("Approved")) && !isFactFull(context, fromTask)) {
                String full = "Full";
                MapList outputQPTask = getRelatedMapList(context,
                        toTask, RELATIONSHIP_IMS_QP_QPTask2QPTask,
                        "*", true, false, (short) 1, "", "(attribute[IMS_QP_DEPTaskStatus]==Approved AND id!=" + relID + ")", 0);

                for (Object objTemp : outputQPTask) {
                    String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
                    DomainObject relTask = new DomainObject(sID);
                    String closed = relTask.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_CLOSE_STATUS);
                    if (!closed.equals("Full")) {
                        full = VALUE_NO;
                        break;
                    }
                }
                if (full.equals("Full")) {
                    toTask.setAttributeValue(context, ATTRIBUTE_IMS_QP_CLOSE_STATUS, "Full");
                    setAttributeClosedTaskTree(context, toTask);
                }

            }

        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }
}
