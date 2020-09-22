import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.common.CommonDocument;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import matrix.db.*;
import matrix.util.StringList;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.*;
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
    public static final String SELECT_ATTRIBUTE_IMS_QP_CloseStatus = "attribute[IMS_QP_CloseStatus]";
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

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public MapList getRelatedIMSDepTask(Context context, String[] args) throws Exception {
        try {

            Map programMap = JPO.unpackArgs(args);
            String sOID = (String) programMap.get("objectId");
            DomainObject subStage = DomainObject.newInstance(context, sOID);

            MapList depTask = getRelatedMapList(context, subStage, RELATIONSHIP_IMS_QP_DEP_SUB_STAGE_2_DEP_TASK, TYPE_IMS_QP_DEP_TASK, true, true, (short) 1, "", "", 0);

            return depTask;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public HashMap createDEPTask(Context context, String[] args) throws Exception {

        HashMap returnMap = new HashMap();
        try {

            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");

            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
            DomainObject parent = new DomainObject(parentOID);
            MapList depTask = getRelatedMapList(context, parent, RELATIONSHIP_IMS_QP_DEP_SUB_STAGE_2_DEP_TASK, TYPE_IMS_QP_DEP_TASK, true, true, (short) 1, "", null, 0);

            getNextName(context, objectId, parent, depTask, "", 1, 1);

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }
        return returnMap;
    }

    private DomainObject getNextName(Context context, String objectId, DomainObject parent, MapList mapList, String family, int i, int element) throws Exception {
        DomainObject newObject = new DomainObject(objectId);
        String number = "01";
        mapList.addSortKey(DomainConstants.SELECT_ORIGINATED, "descending", "date");
        mapList.sort();
        if (mapList.size() > i) {
            String name = (String) ((Map) mapList.get(element)).get(DomainConstants.SELECT_NAME);
            int numberInt = Integer.parseInt(name.substring(name.length() - 2)) + 1;
            number = (numberInt < 10) ? "0" + numberInt : Integer.toString(numberInt);
        }

        /*mask name: [Task name]_[Type level 2]_[Count]*/
        String familyPostfix = "";
        if (!family.equals("")) {
            String familyName = new DomainObject(family.substring(0, family.lastIndexOf("_"))).getName(context);
            familyPostfix = "_" + familyName;

        }
        LOG.info("set name: " + parent.getInfo(context, DomainObject.SELECT_NAME) + familyPostfix + "-" + number);
        newObject.setName(context, parent.getInfo(context, DomainObject.SELECT_NAME) + familyPostfix + "-" + number);
        return newObject;
    }

    public Object getRelationColumn(Context context, String[] args) throws Exception {

        Map programMap = JPO.unpackArgs(args);
        MapList mlObList = (MapList) programMap.get("objectList");
        Vector returnList = new Vector();
        for (Object objTemp : mlObList) {
            returnList.add("");
        }
        return returnList;
    }

    public HashMap getRangeRelationshipER(Context context, String[] args) throws Exception {

        HashMap result = new HashMap();
        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();
        fieldRangeValues.add(FROM);
        fieldDisplayRangeValues.add(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.From", context.getLocale().getLanguage()));
        fieldRangeValues.add(TO);
        fieldDisplayRangeValues.add(EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.To", context.getLocale().getLanguage()));
        result.put("field_choices", fieldRangeValues);
        result.put("field_display_choices", fieldDisplayRangeValues);
        return result;
    }


    public MapList getRelatedExpectedResult(Context context, String[] args) throws Exception {
        try {
            Map programMap = JPO.unpackArgs(args);
            String sOID = (String) programMap.get("objectId");
            DomainObject task = DomainObject.newInstance(context, sOID);

            MapList ExpectedREsult = getRelatedMapList(context, task, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK + "," + RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, "*", true, true, (short) 1, "", "", 0);
            MapList inputQPTask = getRelatedMapList(context, task, RELATIONSHIP_IMS_QP_QPTask2QPTask, "*", true, false, (short) 1, "", "", 0);
            for (Object objTemp : inputQPTask) {
                String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
                MapList ExpectedREsultQP = getRelatedMapList(context, new DomainObject(sID), RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, "*", true, true, (short) 1, "", "", 0);
                ExpectedREsult.addAll(ExpectedREsultQP);
            }

            return ExpectedREsult;
        } catch (Exception ex) {
            throw ex;
        }
    }


    public Object getFromTo(Context context, String[] args) throws Exception {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new java.io.File("c:/temp/IMS_121232.txt"), true))), true);
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList mlObList = (MapList) programMap.get("objectList");

        Vector returnList = new Vector();
        try {
            for (Object objTemp : mlObList) {
                String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_RELATIONSHIP_ID);
                pw.println(sID);
                DomainRelationship relationship = new DomainRelationship(sID);
                relationship.openRelationship(context);
                pw.println(relationship.getFrom());
                pw.println(relationship.getFrom().getTypeName());
                returnList.add(relationship.getFrom().getTypeName().equals(TYPE_IMS_QP_EXPECTED_RESULT) ? EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.From", context.getLocale().getLanguage()) : EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.To", context.getLocale().getLanguage()));
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

            MapList depExceptedResult = depTask.getRelatedObjects(context, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK + "," + RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, TYPE_IMS_QP_EXPECTED_RESULT, QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);
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
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_RepReqPBS.txt"), true))), true);

            Map paramMap = (Map) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");
            String newValue = (String) paramMap.get("New Value");
            pw.println("programMap" + programMap.toString());
            if (!newValue.equals("none") && !newValue.equals("")) {
                pw.println("newValue" + newValue);
                newValue = newValue.substring(0, newValue.indexOf("_"));
                DomainObject exceptedResult = new DomainObject(objectId);
                RelationshipType resultTypeToExceptedResult = new RelationshipType(RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT);

                MapList depExceptedResult = getRelatedMapList(context, exceptedResult, RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT, "*", true, true, (short) 1, "", "", 0);
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
        MapList resultType = DomainObject.findObjects(context, IMS_FAMILY, "*", "*", "*", "*", TO_IMS_QP_RESULT_TYPE_2_FAMILY + "==True", true, objectSelects);

        for (Object resultTypObject : resultType) {
            Map objTemp = (HashMap) resultTypObject;
            String name = (String) objTemp.get(DomainConstants.SELECT_ID) + "_" + (String) objTemp.get(SELECT_TO_IMS_QP_RESULT_TYPE_2_FAMILY_FROM_ID);
            fieldDisplayRangeValues.add((String) objTemp.get(DomainConstants.SELECT_NAME));
            fieldRangeValues.add(name);
        }

        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);
        return tempMap;
    }

    public Object getResultType(Context context, String[] args) throws Exception {

        MapList resultType = getFindObject(context, IMS_QP_RESULT_TYPE, "*",
                "*", "");
        return resultType;
    }

    public MapList getRelatedQPTask(Context context, String[] args) throws Exception {
        try {
            Map programMap = JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            DomainObject qPlan = new DomainObject(objectId);

            MapList task = getRelatedMapList(context, qPlan, RELATIONSHIP_IMS_QPlan2QPTask, TYPE_IMS_QP_QPTASK, true, true, (short) 1, "", "", 0);

            return task;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public HashMap createQPTask(Context context, String[] args) throws Exception {

        HashMap returnMap = new HashMap();

        try {
            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");
            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
            String DEPTaskOID = (String) requestMap.get("DEPTaskOID");
            DomainObject parent = new DomainObject(parentOID);
            DomainObject qpTask = new DomainObject(objectId);

            if (!DEPTaskOID.equals("") && DEPTaskOID != null) {
                DomainObject depTask = new DomainObject(DEPTaskOID);
                DomainRelationship.connect(context, new DomainObject(DEPTaskOID), RELATIONSHIP_IMS_DEPTask2QPTask, new DomainObject(objectId));
                addFactExpTree(context, depTask, 1);

            }

            qpTask.setAttributeValue(context, ATTRIBUTE_IMS_QP_FACT_EXP, "1");
            setFactExp(context, parent, 1);

            MapList depTask = getRelatedMapList(context, parent, RELATIONSHIP_IMS_QPlan2QPTask, TYPE_IMS_QP_QPTASK, true, true, (short) 1, "", "", 0);
            getNextName(context, objectId, parent, depTask, "", 1, 1);

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }
        return returnMap;
    }

    private static MapList getRelatedMapList(Context context, DomainObject object, String relationship, String type, boolean from, boolean to, short level, String expressionObject, String expressionRelationship, int i) throws FrameworkException {
        StringList QAGbusSelects = new StringList();  // Object
        QAGbusSelects.add(DomainConstants.SELECT_ID);
        QAGbusSelects.add(DomainConstants.SELECT_NAME);
        QAGbusSelects.add(DomainConstants.SELECT_ORIGINATED);
        StringList QAGrelSelects = new StringList();  // Rel
        QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

        return object.getRelatedObjects(context, relationship, type, QAGbusSelects, QAGrelSelects, from, to, level, expressionObject, expressionRelationship, i);
    }

    public MapList getFindObject(Context context, String type, String name, String revision, String expression) throws Exception {
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        MapList resultType = DomainObject.findObjects(context, type, name,
                revision, "*", "*", expression, true, objectSelects);
        return resultType;
    }

    public StringList includeSearch(Context context, String[] args) throws FrameworkException {
        try {
            Map programMap = JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");

            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_RepReqPBS.txt"), true))), true);

            StringList stringList = new StringList();

            String objectWhere = new DomainObject(objectId).getInfo(context, "to[IMS_QP_DEPTask2QPTask].from.id");
            if (objectWhere != null && !objectWhere.equals("")) {

                MapList mapList = getRelatedMapList(context, new DomainObject(objectWhere), RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK, TYPE_IMS_QP_EXPECTED_RESULT, true, true, (short) 1, "", "", 0);
                pw.println("cdsc2" + mapList.toString());
                pw.println("cdsc2" + mapList.size());
                for (Object resultTypObject : mapList) {
                    Map objTemp = (Map) resultTypObject;
                    pw.println((String) objTemp.get(DomainObject.SELECT_ID));
                    stringList.add((String) objTemp.get(DomainObject.SELECT_ID));
                }
            }
            return stringList;

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    public String dynamicFormTask(final Context context, final String[] args) throws Exception {

        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_DEPTASK.txt"), true))), true);
            String objectId = args[0];
            pw.println(objectId);
            DomainObject object = new DomainObject(objectId);

            return object.getInfo(context, IMS_Name) + "|" + object.getInfo(context, IMS_NameRU) + "|" + object.getInfo(context, IMS_DescriptionEn) + "|" + object.getInfo(context, IMS_DescriptionRu);

        } catch (Exception e) {
            return e.getMessage();
        }

    }

    public String dynamicForm(final Context context, final String[] args) throws Exception {

        try {
            String objectId = args[0];
            DomainObject object = new DomainObject(objectId);
            String typeid = "";
            String resultType = "";
            String nameResult = "";
            String nameFamily = "";
            String idDEP = object.getInfo(context, "from[" + RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK + "]").equals("TRUE") ? "from" : "to";
            typeid = object.getInfo(context, "to[" + RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.id");
            if (typeid != null && !typeid.equals("")) {
                resultType = new DomainObject(typeid).getInfo(context, SELECT_TO_IMS_QP_RESULT_TYPE_2_FAMILY_FROM_ID);
                nameResult = (resultType != null && !resultType.equals("")) ? new DomainObject(resultType).getInfo(context, "name") : "";
                nameFamily = new DomainObject(typeid).getInfo(context, "name");
            }
            return object.getInfo(context, IMS_DocumentCode) + "|" + object.getInfo(context, IMS_Name) + "|" + object.getInfo(context, IMS_NameRU) + "|" + object.getInfo(context, IMS_DescriptionEn) + "|" + object.getInfo(context, IMS_DescriptionRu) + "|" + idDEP + "|" + nameResult + "|" + resultType + "|" + nameFamily + "|" + typeid + "_" + resultType;

        } catch (Exception e) {
            return e.getMessage();
        }

    }

    public HashMap createExpectedResultQP(Context context, String[] args) throws Exception {

        HashMap returnMap = new HashMap();
        try {

            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");
            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
            String DEPexpected = (String) requestMap.get("DEPexpectedOID");
            String relationship = RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK;
            DomainObject parent = new DomainObject(parentOID);
            MapList depTask = getRelatedMapList(context, parent, relationship, TYPE_IMS_QP_EXPECTED_RESULT, true, true, (short) 1, "", null, 0);

            DomainObject newObject = getNextName(context, objectId, parent, depTask, "", 0, 0);

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

                newObject.setAttributeValue(context, "IMS_Name", depTaskObject.getInfo(context, "attribute[IMS_Name]"));
                newObject.setAttributeValue(context, "IMS_NameRu", depTaskObject.getInfo(context, "attribute[IMS_NameRu]"));
                newObject.setAttributeValue(context, "IMS_DescriptionRu", depTaskObject.getInfo(context, "attribute[IMS_DescriptionRu]"));
                newObject.setAttributeValue(context, "IMS_DescriptionEn", depTaskObject.getInfo(context, "attribute[IMS_DescriptionEn]"));
                newObject.setAttributeValue(context, "IMS_QP_DocumentCode", depTaskObject.getInfo(context, "attribute[IMS_QP_DocumentCode]"));

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


    public HashMap createExpectedResult(Context context, String[] args) throws Exception {

        HashMap returnMap = new HashMap();
        try {
            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");
            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
            String fromto = (String) requestMap.get("fromto");
            String family = (String) requestMap.get("family");
            LOG.info("family: " + family);
            DomainObject parent = new DomainObject(parentOID);
            String relationship = RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK;

            MapList depTask = getRelatedMapList(context, parent, relationship, TYPE_IMS_QP_EXPECTED_RESULT, true, true, (short) 1, "", null, 0);

            DomainObject newObject = getNextName(context, objectId, parent, depTask, family, 0, 0);

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
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_RepR44.txt"), true))), true);
            Map programMap = JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            StringList stringList = new StringList();
            MapList depMapList = getRelatedMapList(context, new DomainObject(objectId), "IMS_QP_DEP2QPlan", "*", true, true, (short) 1, "", "", 0);

            for (Object dep : depMapList) {
                Map depMap = (Map) dep;
                String depId = (String) depMap.get(DomainObject.SELECT_ID);
                MapList projectSpace = getRelatedMapList(context, new DomainObject(depId), "IMS_QP_DEP2DEPProjectStage", "*", true, true, (short) 1, "", "", 0);

                for (Object pS : projectSpace) {
                    Map pSMap = (Map) pS;
                    String pSId = (String) pSMap.get(DomainObject.SELECT_ID);
                    MapList subStage = getRelatedMapList(context, new DomainObject(pSId), "IMS_QP_DEPProjectStage2DEPSubStage", "*", true, true, (short) 1, "", "", 0);

                    for (Object sS : subStage) {
                        Map sSMap = (Map) sS;
                        String sSId = (String) sSMap.get(DomainObject.SELECT_ID);
                        MapList depTask = getRelatedMapList(context, new DomainObject(sSId), "IMS_QP_DEPSubStage2DEPTask", "*", true, true, (short) 1, "", "", 0);

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

            MapList subStageMapList = getRelatedMapList(context, depTask, "IMS_QP_DEPSubStage2DEPTask", "*", true, true, (short) 1, "", "", 0);
            setFactExp(context, depTask, count);
            for (Object subStageObject : subStageMapList) {
                Map subStageMap = (Map) subStageObject;
                String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                DomainObject subStage = new DomainObject(subStageId);
                MapList projectSpaceMapList = getRelatedMapList(context, subStage, "IMS_QP_DEPProjectStage2DEPSubStage", "*", true, true, (short) 1, "", "", 0);
                setFactExp(context, subStage, count);
                for (Object projectSpaceObject : projectSpaceMapList) {
                    Map projectSpaceMap = (Map) projectSpaceObject;
                    String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                    DomainObject projectSpace = new DomainObject(projectSpaceId);
                    MapList depMapList = getRelatedMapList(context, projectSpace, "IMS_QP_DEP2DEPProjectStage", "*", true, true, (short) 1, "", "", 0);
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
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_DEP.txt"), true))), true);
            Map programMap = JPO.unpackArgs(args);
            pw.println((String) programMap.get("objectId"));
            return true;

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    //триггер на удаление шага qp
    public int delFact(Context context, String[] args) throws Exception {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_QPTrigger.txt"), true))), true);
            String objectId = args[0];
            DomainObject task = new DomainObject(objectId);
            int factexp = Integer.parseInt(task.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_FACT_EXP));
            int factgot = Integer.parseInt(task.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_FACT_GOT));
            pw.println("fact" + factexp);
            pw.println("got" + factgot);
            MapList depTask = getRelatedMapList(context, task, RELATIONSHIP_IMS_DEPTask2QPTask, "*", true, true, (short) 1, "", "", 0);
            for (Object depObject : depTask) {
                pw.println("dep");

                Map depMap = (Map) depObject;
                String depID = (String) depMap.get(DomainObject.SELECT_ID);
                DomainObject dep = new DomainObject(depID);
                if (factgot == 0) {
                    delFactExpTree(context, dep, -1, 0);
                } else {
                    delFactExpTree(context, dep, -1, -1);
                }
            }

            MapList qpPlan = getRelatedMapList(context, task, RELATIONSHIP_IMS_QPlan2QPTask, "*", true, true, (short) 1, "", "", 0);
            for (Object qpObject : qpPlan) {
                Map qpMap = (Map) qpObject;
                pw.println("qp");

                String qpID = (String) qpMap.get(DomainObject.SELECT_ID);
                DomainObject qp = new DomainObject(qpID);
                setFactExp(context, qp, -1);
                if (factgot == 0) {
                    setFactGot(context, qp, 0);
                } else {
                    setFactGot(context, qp, -1);
                }
                pw.println("no error");
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
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_QPTrigger.txt"), true))), true);
            pw.println("hi");

            MapList subStageMapList = getRelatedMapList(context, depTask, "IMS_QP_DEPSubStage2DEPTask", "*", true, true, (short) 1, "", "", 0);
            setFactExp(context, depTask, count);
            setFactGot(context, depTask, countgot);
            for (Object subStageObject : subStageMapList) {
                Map subStageMap = (Map) subStageObject;
                String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                DomainObject subStage = new DomainObject(subStageId);
                MapList projectSpaceMapList = getRelatedMapList(context, subStage, "IMS_QP_DEPProjectStage2DEPSubStage", "*", true, true, (short) 1, "", "", 0);
                setFactExp(context, subStage, count);
                setFactGot(context, subStage, countgot);
                for (Object projectSpaceObject : projectSpaceMapList) {
                    Map projectSpaceMap = (Map) projectSpaceObject;
                    String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                    DomainObject projectSpace = new DomainObject(projectSpaceId);
                    MapList depMapList = getRelatedMapList(context, projectSpace, "IMS_QP_DEP2DEPProjectStage", "*", true, true, (short) 1, "", "", 0);
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

                MapList subStageMapList = getRelatedMapList(context, depTask, "IMS_QP_DEPSubStage2DEPTask", "*", true, true, (short) 1, "", "", 0);
                setFactGot(context, depTask, count);
                for (Object subStageObject : subStageMapList) {
                    Map subStageMap = (Map) subStageObject;
                    String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                    DomainObject subStage = new DomainObject(subStageId);
                    MapList projectSpaceMapList = getRelatedMapList(context, subStage, "IMS_QP_DEPProjectStage2DEPSubStage", "*", true, true, (short) 1, "", "", 0);
                    setFactGot(context, subStage, count);
                    for (Object projectSpaceObject : projectSpaceMapList) {
                        Map projectSpaceMap = (Map) projectSpaceObject;
                        String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                        DomainObject projectSpace = new DomainObject(projectSpaceId);
                        MapList depMapList = getRelatedMapList(context, projectSpace, "IMS_QP_DEP2DEPProjectStage", "*", true, true, (short) 1, "", "", 0);
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

            MapList qpPlan = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_QPlan2QPTask, "*", true, true, (short) 1, "", "", 0);
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
                task.setAttributeValue(context, "IMS_QP_CloseStatus", "No");
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
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_121232.txt"), true))), true);
            String taskId = args[0];
            pw.println("taskid: " + taskId);
            String factId = args[1];
            pw.println("fact id: " + factId);
            DomainObject fact = new DomainObject(factId);
            if (fact.getInfo(context, DomainObject.SELECT_TYPE).equals("IMS_ExternalDocumentSet")) {
                if (fact.getInfo(context, "attribute[IMS_ProjDocStatus]").equals("Finalized")) {
                    DomainObject task = new DomainObject(taskId);
                    pw.println("doc Finalized");
                    setAttributeClosedTask(context, task);
                }
            }
            //        treeFact(context, task, -1);
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
            if (factExp > 0) {
                if (factExp == factGot) {
                    returnList.add("IMS_QP_Green");
                } else if (factExp > factGot) {
                    returnList.add("IMS_QP_Red");
                } else {
                    returnList.add("");
                }
            } else {
                returnList.add("");
            }
        }
        return returnList;
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

            MapList depTaskMapList = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_DEPTask2QPTask, "*", true, true, (short) 1, "", "", 0);
            for (Object depTaskObject : depTaskMapList) {
                Map depTaskMap = (Map) depTaskObject;
                String depTaskID = (String) depTaskMap.get(DomainObject.SELECT_ID);
                DomainObject depTask = new DomainObject(depTaskID);

                MapList subStageMapList = getRelatedMapList(context, depTask, "IMS_QP_DEPSubStage2DEPTask", "*", true, true, (short) 1, "", "", 0);
                setFactGot(context, depTask, 1);
                for (Object subStageObject : subStageMapList) {
                    Map subStageMap = (Map) subStageObject;
                    String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                    DomainObject subStage = new DomainObject(subStageId);
                    MapList projectSpaceMapList = getRelatedMapList(context, subStage, "IMS_QP_DEPProjectStage2DEPSubStage", "*", true, true, (short) 1, "", "", 0);
                    setFactGot(context, subStage, 1);
                    for (Object projectSpaceObject : projectSpaceMapList) {
                        Map projectSpaceMap = (Map) projectSpaceObject;
                        String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                        DomainObject projectSpace = new DomainObject(projectSpaceId);
                        MapList depMapList = getRelatedMapList(context, projectSpace, "IMS_QP_DEP2DEPProjectStage", "*", true, true, (short) 1, "", "", 0);
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

            MapList qpPlan = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_QPlan2QPTask, "*", true, true, (short) 1, "", "", 0);
            for (Object qpObject : qpPlan) {
                Map qpMap = (Map) qpObject;
                String qpID = (String) qpMap.get(DomainObject.SELECT_ID);
                DomainObject qp = new DomainObject(qpID);
                setFactGot(context, qp, 1);
            }


        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    public void setTreeFactExp(Context context, String[] args) throws Exception {
        try {
            DomainObject qpTask = new DomainObject(args[0]);

            setFactExp(context, qpTask, 1);

            MapList depTaskMapList = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_DEPTask2QPTask, "*", true, true, (short) 1, "", "", 0);
            for (Object depTaskObject : depTaskMapList) {
                Map depTaskMap = (Map) depTaskObject;
                String depTaskID = (String) depTaskMap.get(DomainObject.SELECT_ID);
                DomainObject depTask = new DomainObject(depTaskID);

                MapList subStageMapList = getRelatedMapList(context, depTask, "IMS_QP_DEPSubStage2DEPTask", "*", true, true, (short) 1, "", "", 0);
                setFactExp(context, depTask, 1);
                for (Object subStageObject : subStageMapList) {
                    Map subStageMap = (Map) subStageObject;
                    String subStageId = (String) subStageMap.get(DomainObject.SELECT_ID);
                    DomainObject subStage = new DomainObject(subStageId);
                    MapList projectSpaceMapList = getRelatedMapList(context, subStage, "IMS_QP_DEPProjectStage2DEPSubStage", "*", true, true, (short) 1, "", "", 0);
                    setFactExp(context, subStage, 1);
                    for (Object projectSpaceObject : projectSpaceMapList) {
                        Map projectSpaceMap = (Map) projectSpaceObject;
                        String projectSpaceId = (String) projectSpaceMap.get(DomainObject.SELECT_ID);
                        DomainObject projectSpace = new DomainObject(projectSpaceId);
                        MapList depMapList = getRelatedMapList(context, projectSpace, "IMS_QP_DEP2DEPProjectStage", "*", true, true, (short) 1, "", "", 0);
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

            MapList qpPlan = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_QPlan2QPTask, "*", true, true, (short) 1, "", "", 0);
            for (Object qpObject : qpPlan) {
                Map qpMap = (Map) qpObject;
                String qpID = (String) qpMap.get(DomainObject.SELECT_ID);
                DomainObject qp = new DomainObject(qpID);
                setFactExp(context, qp, 1);
            }


        } catch (Exception ex) {
            throw new FrameworkException(ex);
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

        StringBuilder sb = new StringBuilder();

        if (externalDocumentSetMap != null) {


            sb.append(IMS_KDD_mxJPO.getDisconnectLinkHTML(
                    "IMS_QP_DEPTask", "disconnectCheckList",
                    qpTaskObject.getId(context), IMS_KDD_mxJPO.getIdFromMap(externalDocumentSetMap),
                    RELATIONSHIP_IMS_QP_QPTask2Fact,
                    "Disconnect",
                    IMS_KDD_mxJPO.getRefreshWindowFunction()));

            sb.append(String.format(
                    "<a href=\"javascript:%s\"><img src=\"%s\" />%s</a>",
                    String.format(
                            "emxTableColumnLinkClick('../common/emxForm.jsp?form=type_IMS_QP_CheckList&toolbar=IMS_QP_CheckListToolbar&HelpMarker=emxhelpdocumentproperties&formHeader=emxComponents.Common.PropertiesPageHeading&subHeader=emxComponents.Menu.SubHeaderDocuments&Export=False&&displayCDMFileSummary=true&objectId=%s')",
                            IMS_KDD_mxJPO.getIdFromMap(externalDocumentSetMap)),
                    IMS_KDD_mxJPO.FUGUE_16x16 + "document.png",
                    HtmlEscapers.htmlEscaper().escape(IMS_KDD_mxJPO.getNameFromMap(externalDocumentSetMap))));


        } else {
            sb.append(String.format(
                    "<a href=\"javascript:%s\"><img src=\"%s\" title=\"%s\" /></a>",
                    String.format(
                            "window.open('emxCreate.jsp?type=type_IMS_QP_CheckList&typeChooser=false&form=IMS_QP_Create_CheckList&findMxLink=false&relationship=relationship_IMS_QP_QPTask2Fact&policy=policy_IMS_QP_CheckList&submitAction=refreshCaller&postProcessURL=../common/IMS_PassFilesToJPO.jsp&objectId=%s', '_blank', 'height=800,width=1000,toolbar=0,location=0,menubar=0')", qpTaskObject.getId(context)),
                    IMS_KDD_mxJPO.FUGUE_16x16 + "plus.png",
                    "Create and Connect CheckList"));
        }

        return sb.toString();
    }

    public String disconnectCheckList(Context context, String[] args) throws Exception {
        new DomainObject(args[1]).deleteObject(context);
        return "";

    }

    public static String addExtensionScript(Context context, String[] args) throws Exception {
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new java.io.File("c:/temp/IMS_QPTrigger2.txt"), true))), true);
        pw.println("hiFile2");
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
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new java.io.File("c:/temp/IMS_QPTrigger2.txt"), true))), true);
        Map map = new HashMap();
        HashMap programMap = JPO.unpackArgs(args);
        InputStream inp = (InputStream) programMap.get("inp");
        String sFileName = (String) programMap.get("fileName");
        String sObjectId = (String) programMap.get("objectId");
        map = attachFileGeneral(context, sObjectId, sFileName, inp, args);
        return map;


    }

    public Map attachFileGeneral(Context context, String _objectId, String _fileName, InputStream _inp, String[] args) throws Exception {
        Map map = new HashMap();
        PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(new java.io.File("c:/temp/IMS_QPTrigger2.txt"), true))), true);
        InputStream inp = _inp;
        String sFileName = _fileName;
        String sObjectId = _objectId;
        StringBuffer sb = new StringBuffer();

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
            File file = new File(sPath + "/" + sFileName);
            FileUtils.copyInputStreamToFile(inp1, file);

            HashMap uploadParamsMap = new HashMap();
            uploadParamsMap.put("objectId", sObjectId);
            uploadParamsMap.put("noOfFiles", "1");
            uploadParamsMap.put("fileName0", sFileName);
            uploadParamsMap.put("objectAction", CommonDocument.OBJECT_ACTION_UPDATE_MASTER);
            String[] args1 = JPO.packArgs(uploadParamsMap);

            String jpoName = "emxCommonDocument";
            String methodName = "commonDocumentCheckin";
            Map objectMap = JPO.invoke(context, jpoName, null, methodName, args1, Map.class);
            pw.println(objectMap.toString());

            FileUtils.deleteQuietly(file);
            inp1.close();
            ContextUtil.commitTransaction(context);
            map.put("message", sb.toString());
        } catch (Exception exception) {
            ContextUtil.abortTransaction(context);
            map.put("message", sb.toString() + exception.toString());
        }

        return map;
    }

    public boolean accessCheckList(Context context, String[] args) throws FrameworkException {
        try {

            Map programMap = JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            DomainObject object = new DomainObject(objectId);
            MapList expectedResult = getRelatedMapList(context, object, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, "*", false, true, (short) 1, "to[" + RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.name=='CL-1'", "", 0);
            if (expectedResult.size() != 0) {
                return true;
            }

            return false;

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    public boolean accessDocument(Context context, String[] args) throws FrameworkException {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_121232.txt"), true))), true);

            Map programMap = JPO.unpackArgs(args);
            String objectId = (String) programMap.get("objectId");
            DomainObject object = new DomainObject(objectId);
            MapList expectedResult = getRelatedMapList(context, object, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, "*", false, true, (short) 1, "to[" + RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.name!='CL-1'", "", 0);
            pw.println(expectedResult.size());
            if (expectedResult.size() != 0) {
                return true;
            }

            return false;

        } catch (Exception ex) {
            throw new FrameworkException(ex);
        }
    }

    public void setAttributeClosedTask(Context context, DomainObject qpTask) throws Exception {
        try {
            String full = "Full";
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_121232.txt"), true))), true);
            pw.println();
            MapList outputQPTask = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_QP_QPTask2QPTask, "*", true, false, (short) 1, "", "", 0);

            for (Object objTemp : outputQPTask) {
                String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
                pw.println("output Task: " + sID);
                DomainObject relTask = new DomainObject(sID);
                String closed = relTask.getInfo(context, "attribute[IMS_QP_CloseStatus]");
                pw.println("status Task: " + closed);
                if (!closed.equals("Full")) {
                    full = "No";
                    pw.println("NO ");
                    break;
                }
            }
            if (full.equals("Full")) {
                pw.println("Full");

                qpTask.setAttributeValue(context, "IMS_QP_CloseStatus", "Full");
                pw.println("set Full");
                setAttributeClosedTaskTree(context, qpTask);

            }
            pw.println("end");

        } catch (Exception ex) {
            throw ex;
        }
    }

    public void setAttributeClosedTaskTree(Context context, DomainObject qpTask) throws Exception {
        try {
            String full = "Full";
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_121232.txt"), true))), true);
            pw.println();

            MapList inputQPTask = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_QP_QPTask2QPTask, "*", true, false, (short) 1, "", "", 0);
            for (Object inputMap : inputQPTask) {
                String inputId = (String) ((Map) inputMap).get(DomainObject.SELECT_ID);
                pw.println("input task: " + inputId);

                DomainObject inputObject = new DomainObject(inputId);
                if (isFactFull(context, qpTask)) {
                    MapList outputQPTask = getRelatedMapList(context, inputObject, RELATIONSHIP_IMS_QP_QPTask2QPTask, "*", false, true, (short) 1, "", "", 0);
                    for (Object outputMap : outputQPTask) {
                        String outputId = (String) ((Map) outputMap).get(DomainObject.SELECT_ID);
                        DomainObject outputObject = new DomainObject(outputId);
                        String closed = outputObject.getInfo(context, "attribute[IMS_QP_CloseStatus]");
                        pw.println("output task: " + outputId + "  " + closed);

                        if (!closed.equals("Full")) {
                            full = "No";
                            break;
                        }
                    }
                    if (full.equals("Full")) {
                        inputObject.setAttributeValue(context, "IMS_QP_CloseStatus", "Full");
                        pw.println("Full ");

                        setAttributeClosedTaskTree(context, inputObject);
                    }
                }
            }

        } catch (Exception ex) {
            throw ex;
        }
    }

    public boolean isFactFull(Context context, DomainObject qpTask) throws Exception {
        try {

            MapList fact = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_QP_QPTask2Fact, "*", true, true, (short) 1, "(type==IMS_QP_CheckList AND current==Approved) OR (type==IMS_ExternalDocumentSet AND attribute[IMS_ProjDocStatus]==Finalized)", "", 0);
            if (fact.size() > 0) {
                return true;
            }
            return false;
        } catch (Exception ex) {
            throw ex;
        }
    }

    public int promoteCheckList(Context context, String[] args) throws Exception {
        try {
            String checklistID = args[0];
            DomainObject checkList = new DomainObject(checklistID);
            String qpTaskID = checkList.getInfo(context, "to[IMS_QP_QPTask2Fact].from.id");
            setAttributeClosedTask(context, new DomainObject(qpTaskID));


            //        treeFact(context, task, -1);
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
            String qpTaskID = checkList.getInfo(context, "to[IMS_QP_QPTask2Fact].from.id");
            DomainObject task = new DomainObject(qpTaskID);
            if (task.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_CloseStatus).equals("Full")) {
                task.setAttributeValue(context, "IMS_QP_CloseStatus", "No");
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

    public int modifyAttributeClosedStatus(Context context, String[] args) throws Exception {
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_121232.txt"), true))), true);

            String qpTaskId = args[0];
            String valuenew = args[1];
            String value = args[2];

            pw.println(value + "   " + valuenew);
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


            MapList inputQPTask = getRelatedMapList(context, qpTask, RELATIONSHIP_IMS_QP_QPTask2QPTask, "*", true, false, (short) 1, "", "", 0);
            for (Object inputMap : inputQPTask) {
                String inputId = (String) ((Map) inputMap).get(DomainObject.SELECT_ID);

                DomainObject inputObject = new DomainObject(inputId);
                String status = inputObject.getInfo(context, SELECT_ATTRIBUTE_IMS_QP_CloseStatus);
                if (status.equals("Full")) {
                    inputObject.setAttributeValue(context, "IMS_QP_CloseStatus", "No");

                    setAttributeClosedTaskTreeNo(context, inputObject);
                }
            }


        } catch (Exception ex) {
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
        LOG.info("delete tasks");
        //get all ids
        HashMap<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");
        String[] taskIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            taskIDs[i] = rowIDs[i].substring(0, rowIDs[i].indexOf("|"));
        }

        //selects all tasks
        StringList selects = new StringList();
        selects.add("id");
        selects.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPTASK_2_DEPTASK_ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
        selects.add("to[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]");
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

        StringBuffer buffer = new StringBuffer(EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.couldntDelete"));
        List<String> badNames = new ArrayList<>();
        boolean flag = false;
        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            String taskStates = "" + map.get(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPTASK_2_DEPTASK_ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
            if (taskStates.contains("Approved")) {
                buffer.append(map.get("name") + "\n");
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
            if (var1 != null && var1.length > 0)
                DomainObject.deleteObjects(context, var1);
            LOG.info("objects for deleting: " + Arrays.deepToString(var1));
        } catch (Exception e) {
            LOG.error("deleting error: " + e.getMessage());
            e.printStackTrace();
        }

        LOG.info("return map: " + mapMessage);
        return mapMessage;
    }
}
