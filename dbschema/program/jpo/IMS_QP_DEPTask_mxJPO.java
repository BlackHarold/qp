import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import matrix.db.*;
import com.matrixone.apps.domain.util.*;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import java.util.Vector;
import matrix.db.MQLCommand;
import java.io.*;
import java.util.HashMap;
import java.util.Map;



public class IMS_QP_DEPTask_mxJPO
{
    public IMS_QP_DEPTask_mxJPO(Context context, String[] args) throws Exception {}

   public static final String FROM = "From";
    public static final String RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK = "IMS_QP_ExpectedResult2DEPTask";
    public static final String TYPE_IMS_QP_EXPECTED_RESULT = "IMS_QP_ExpectedResult";
    public static final String TO = "To";
    public static final String RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT = "IMS_QP_ResultType2ExpectedResult";
    public static final String RELATIONSHIP_IMS_QP_DEP_SUB_STAGE_2_DEP_TASK = "IMS_QP_DEPSubStage2DEPTask";
    public static final String TYPE_IMS_QP_DEP_TASK = "IMS_QP_DEPTask";
	public static final String RELATIONSHIP_IMS_QPlan2QPTask = "IMS_QP_QPlan2QPTask";
    public static final String TYPE_IMS_QP_QPTASK = "IMS_QP_QPTask";
    public static final String RELATIONSHIP_IMS_DEPTask2QPTask = "IMS_DEPTask2QPTask";
    public static final String RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK = "IMS_QP_ExpectedResult2QPTask";



    public MapList getRelatedIMSDepTask(Context context, String[] args) throws Exception {
        try {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String sOID = (String) programMap.get("objectId");
            DomainObject subStage = DomainObject.newInstance(context, sOID);
            StringList QAGbusSelects = new StringList();  // Object
            QAGbusSelects.add(DomainConstants.SELECT_ID);

            StringList QAGrelSelects = new StringList();  // Rel
            QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            QAGrelSelects.add(DomainConstants.SELECT_NAME);

            MapList depTask = subStage.getRelatedObjects(context, RELATIONSHIP_IMS_QP_DEP_SUB_STAGE_2_DEP_TASK, TYPE_IMS_QP_DEP_TASK, QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);

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
            StringList QAGbusSelects = new StringList();  // Object
            QAGbusSelects.add(DomainConstants.SELECT_ID);
            QAGbusSelects.add(DomainConstants.SELECT_NAME);
            QAGbusSelects.add(DomainConstants.SELECT_ORIGINATED);
            StringList QAGrelSelects = new StringList();  // Rel
            QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            String number = "01";
            MapList depTask = parent.getRelatedObjects(context, RELATIONSHIP_IMS_QP_DEP_SUB_STAGE_2_DEP_TASK, TYPE_IMS_QP_DEP_TASK, QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);

            depTask.addSortKey(DomainConstants.SELECT_ORIGINATED, "descending", "date");
            depTask.sort();
            if (depTask.size() > 1) {
                String name = (String) ((Map) depTask.get(1)).get(DomainConstants.SELECT_NAME);
                int numberInt = Integer.parseInt(name.substring(name.length() - 2)) + 1;
                number = (numberInt < 10) ? "0" + numberInt : Integer.toString(numberInt);
            }
            new DomainObject(objectId).setName(context, parent.getInfo(context, DomainObject.SELECT_NAME) + "-" + number);

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }
        return returnMap;
    }

    public Object getRelationColumn(Context context, String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList mlObList = (MapList) programMap.get("objectList");
        Vector returnList = new Vector();
        for (Object objTemp : mlObList) {
            String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
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
            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String sOID = (String) programMap.get("objectId");
            DomainObject subStage = DomainObject.newInstance(context, sOID);
            StringList QAGbusSelects = new StringList();  // Object
            QAGbusSelects.add(DomainConstants.SELECT_ID);
            QAGbusSelects.add(DomainConstants.SELECT_NAME);

            StringList QAGrelSelects = new StringList();  // Rel
            QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            QAGrelSelects.add(DomainConstants.SELECT_NAME);

            MapList depTask = subStage.getRelatedObjects(context, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK +","+RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, "*", QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);

            return depTask;
        } catch (Exception ex) {
            throw ex;
        }
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
            DomainObject parent = new DomainObject(parentOID);
            StringList QAGbusSelects = new StringList();  // Object
            QAGbusSelects.add(DomainConstants.SELECT_ID);
            QAGbusSelects.add(DomainConstants.SELECT_NAME);
            QAGbusSelects.add(DomainConstants.SELECT_ORIGINATED);
            StringList QAGrelSelects = new StringList();  // Rel
            QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            String number = "01";
            String relationship = parent.getInfo(context,DomainConstants.SELECT_TYPE).equals(TYPE_IMS_QP_QPTASK) ? RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK : RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK;
            MapList depTask = parent.getRelatedObjects(context, relationship, TYPE_IMS_QP_EXPECTED_RESULT, QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);

            depTask.addSortKey(DomainConstants.SELECT_ORIGINATED, "descending", "date");
            depTask.sort();
            if (depTask.size() > 0) {
                String name = (String) ((Map) depTask.get(0)).get(DomainConstants.SELECT_NAME);
                int numberInt = Integer.parseInt(name.substring(name.length() - 2)) + 1;

                number = (numberInt < 10) ? "0" + numberInt : Integer.toString(numberInt);

            }
            DomainObject newObject = new DomainObject(objectId);
            newObject.setName(context, parent.getInfo(context, DomainObject.SELECT_NAME) + "-" + number);
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

    public Object getFromTo(Context context, String[] args) throws Exception {

        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        MapList mlObList = (MapList) programMap.get("objectList");

        Vector returnList = new Vector();
        try {
            for (Object objTemp : mlObList) {
                String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_RELATIONSHIP_ID);
                DomainRelationship relationship = new DomainRelationship(sID);
                relationship.openRelationship(context);
                returnList.add(relationship.getFrom().getTypeName().equals(TYPE_IMS_QP_EXPECTED_RESULT) ? EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.From", context.getLocale().getLanguage()) : EnoviaResourceBundle.getProperty(context, "Framework", "emxFramework.Range.IMS_QP_Direction.To", context.getLocale().getLanguage()));
                relationship.closeRelationship(context,true);
            }

        } catch (Exception e) {
            System.out.println(e.toString());
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

            MapList depExceptedResult = depTask.getRelatedObjects(context, RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK+","+RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, TYPE_IMS_QP_EXPECTED_RESULT, QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);
            for (Object objTemp : depExceptedResult) {
                String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_RELATIONSHIP_ID);
                new DomainObject(sID).deleteObject(context);
            }
        } catch (Exception ex) {
            MQLCommand mc = new MQLCommand();
            mc.executeCommand(context, "error $1", ex.getMessage());
            return 1;
        }
        return 0;
    }

public static Object getRangeResultType( Context context, String[] args) throws Exception {

        Map tempMap = new HashMap();
        StringList fieldRangeValues = new StringList("none");
        StringList fieldDisplayRangeValues = new StringList(" ");

        String typePattern = "IMS_QP_ResultType";
        String namePattern = "*";
        String revPattern = "*";
        String ownerPattern = "*";
        String vaultPattern = "*";
        String whereExpression = "";
        boolean expandType = true;
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        MapList resultType = DomainObject.findObjects(context, typePattern, namePattern,
                revPattern, ownerPattern, vaultPattern, whereExpression, expandType, objectSelects);

        for (Object resultTypObject : resultType) {
            Map objTemp = (HashMap) resultTypObject;
            fieldDisplayRangeValues.add((String) objTemp.get(DomainConstants.SELECT_NAME));
            fieldRangeValues.add((String) objTemp.get(DomainConstants.SELECT_ID));
        }

        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);
        return tempMap;
    }


    public void setResultType(Context context, String[] args) throws Exception {
             PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_RepReqPBS.txt"), true))), true);
        try {
            Map programMap = (Map) JPO.unpackArgs(args);

                   Map paramMap = (Map) programMap.get("paramMap");
        String objectId = (String) paramMap.get("objectId");
        String newValue = (String) paramMap.get("New Value");
        String oldValue = (String) paramMap.get("Old value");
        newValue = newValue.substring(0,newValue.indexOf("_"));

        DomainObject exceptedResult = new DomainObject(objectId);

        StringList QAGbusSelects = new StringList();  // Object
        QAGbusSelects.add(DomainConstants.SELECT_ID);
        StringList QAGrelSelects = new StringList();  // Rel
        QAGbusSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
        RelationshipType resultTypeToExceptedResult = new RelationshipType(RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT);

        MapList depExceptedResult = exceptedResult.getRelatedObjects(context, RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT, "*", QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);
        for (Object objTemp : depExceptedResult) {
            String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
			DomainObject typeRes = new DomainObject(sID);
            typeRes.disconnect(context, resultTypeToExceptedResult, true, new BusinessObject(objectId));

        }

        DomainRelationship.connect(context, new DomainObject(newValue), RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT, exceptedResult);


           
        }
        catch (Exception ex)
        {
            String errorStr = ex.getMessage();
            emxContextUtil_mxJPO.mqlError(context, errorStr);
            throw ex;
        }
    }

    public static Object getRangeFamily( Context context, String[] args) throws Exception {

        HashMap tempMap = new HashMap();
        Map programMap = (Map) JPO.unpackArgs(args);

        StringList fieldRangeValues = new StringList("none");
        StringList fieldDisplayRangeValues = new StringList(" ");
        String typePattern = "IMS_Family";
        String namePattern = "*";
        String revPattern = "*";
        String ownerPattern = "*";
        String vaultPattern = "*";
        String whereExpression = "";
        boolean expandType = true;
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("to[IMS_QP_ResultType2Family].from.id");
        MapList resultType = DomainObject.findObjects(context, typePattern, namePattern,
                revPattern, ownerPattern, vaultPattern, "to[IMS_QP_ResultType2Family]==True", expandType, objectSelects);

        for (Object resultTypObject : resultType) {
            Map objTemp = (HashMap) resultTypObject;
            String name = (String) objTemp.get(DomainConstants.SELECT_ID) +"_" +(String) objTemp.get("to[IMS_QP_ResultType2Family].from.id");
            fieldDisplayRangeValues.add((String) objTemp.get(DomainConstants.SELECT_NAME) );
            fieldRangeValues.add(name);
        }

        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);
        return tempMap;
    }

    public static Object getResultType( Context context, String[] args) throws Exception {
		
        HashMap tempMap = new HashMap();
        Map programMap = (Map) JPO.unpackArgs(args);
        String typePattern = "IMS_QP_ResultType";
        String namePattern = "*";
        String revPattern = "*";
        String ownerPattern = "*";
        String vaultPattern = "*";
        String whereExpression = "";
        boolean expandType = true;
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        MapList resultType = DomainObject.findObjects(context, typePattern, namePattern,
                revPattern, ownerPattern, vaultPattern, whereExpression, expandType, objectSelects);
        return resultType;
    }
	
	public MapList getRelatedQPTask(Context context, String[] args) throws Exception {
        try {

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            String sOID = (String) programMap.get("objectId");
            DomainObject subStage = DomainObject.newInstance(context, sOID);
            StringList QAGbusSelects = new StringList();  // Object
            QAGbusSelects.add(DomainConstants.SELECT_ID);

            StringList QAGrelSelects = new StringList();  // Rel
            QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            QAGrelSelects.add(DomainConstants.SELECT_NAME);

            MapList depTask = subStage.getRelatedObjects(context, RELATIONSHIP_IMS_QPlan2QPTask, TYPE_IMS_QP_QPTASK, QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);

            return depTask;
        } catch (Exception ex) {
            throw ex;
        }
    }
	
	public HashMap createQPTask(Context context, String[] args) throws Exception {
             PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(new java.io.File("c:/temp/IMS_RepReqPBS.txt"), true))), true);
        HashMap returnMap = new HashMap();
        try {

            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            Map requestMap = (Map) programMap.get("requestMap");
            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
			String DEPTaskOID = (String) requestMap.get("DEPTaskOID");
            DomainObject parent = new DomainObject(parentOID);
            StringList QAGbusSelects = new StringList();  // Object
            QAGbusSelects.add(DomainConstants.SELECT_ID);
            QAGbusSelects.add(DomainConstants.SELECT_NAME);
            QAGbusSelects.add(DomainConstants.SELECT_ORIGINATED);
            StringList QAGrelSelects = new StringList();  // Rel
            QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);
            String number = "01";
            MapList depTask = parent.getRelatedObjects(context, RELATIONSHIP_IMS_QPlan2QPTask, TYPE_IMS_QP_QPTASK, QAGbusSelects, QAGrelSelects, /*from*/ true, /*to*/ true, /*details of exan, 1-level*/ (short) 1, "", null, 0);
            if (!DEPTaskOID.equals("") && DEPTaskOID!=null){DomainRelationship.connect(context, new DomainObject(DEPTaskOID), RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT, new DomainObject(objectId));
            }
			depTask.addSortKey(DomainConstants.SELECT_ORIGINATED, "descending", "date");
            depTask.sort();
            if (depTask.size() > 1) {
                String name = (String) ((Map) depTask.get(1)).get(DomainConstants.SELECT_NAME);
                int numberInt = Integer.parseInt(name.substring(name.length() - 2)) + 1;
                number = (numberInt < 10) ? "0" + numberInt : Integer.toString(numberInt);
            }
            new DomainObject(objectId).setName(context, parent.getInfo(context, DomainObject.SELECT_NAME) + "-" + number);

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }
        return returnMap;
    }


}