import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import java.util.UUID;

import java.util.*;

public class IMS_QP_TaskAssignment_mxJPO {
    public static final String TYPE_IMS_QP_EXPECTED_RESULT = ${CLASS:IMS_QP_Constants}.type_IMS_QP_ExpectedResult;
    public static final String TYPE_IMS_QP_QPTask = ${CLASS:IMS_QP_Constants}.type_IMS_QP_QPTask;
    public static final String POLICY_IMS_QP_QPTask = ${CLASS:IMS_QP_Constants}.policy_IMS_QP_QPTask;
    public static final String REL_IMS_QP_ExpectedResult2DEPTask =  ${CLASS:IMS_QP_Constants}.relationship_IMS_QP_ExpectedResult2DEPTask;
    public static final String REL_IMS_QP_ResultType2ExpectedResult = ${CLASS:IMS_QP_Constants}.relationship_IMS_QP_ResultType2ExpectedResult;
    public static final String REL_IMS_QP_ExpectedResult2QPTask = ${CLASS:IMS_QP_Constants}.relationship_IMS_QP_ExpectedResult2QPTask;
    public static final String REL_IMS_QP_QPlan2QPTask = ${CLASS:IMS_QP_Constants}.relationship_IMS_QP_QPlan2QPTask;
    public static final String REL_IMS_QP_DEPTask2QPTask  = ${CLASS:IMS_QP_Constants}.relationship_IMS_QP_DEPTask2QPTask;
    public static final String ATT_IMS_Name = ${CLASS:IMS_QP_Constants}.ATTRIBUTE_IMS_Name;
    public static final String ATT_IMS_NameRu = ${CLASS:IMS_QP_Constants}.ATTRIBUTE_IMS_NameRu;
    public static final String ATT_IMS_DescriptionEn = ${CLASS:IMS_QP_Constants}.ATTRIBUTE_IMS_DescriptionEn;
    public static final String ATT_IMS_DescriptionRu = ${CLASS:IMS_QP_Constants}.ATTRIBUTE_IMS_DescriptionRu;

    public static final String SELECT_RELATED_EXPECTED_RES =  String.format(
                                                            "to[%s].from.to[%s].from.type",
                                                            REL_IMS_QP_ExpectedResult2DEPTask,
                                                            REL_IMS_QP_ResultType2ExpectedResult);
    public static final String TYPE_IMS_QP_RESULTTYPE = ${CLASS:IMS_QP_Constants}.type_IMS_QP_ResultType 
                                                            + "," + ${CLASS:IMS_QP_Constants}.type_IMS_Family;
    public static final String FIELD_IMS_CODE       = "IMS_CODE";
    public static final String FIELD_IMS_DEP_ID     = "IMS_DEP_ID";
    public static final String FIELD_IMS_EXP_ID     = "IMS_EXP_ID";
    public static final String FIELD_TASKNAME       = "TASKNAME";
    public static final String FIELD_IMS_RES_ID     = "IMS_RES_ID";
    public static final String FIELD_IMS_PARENT_ID  = "id[parent]";

    public  MapList getAvailableTasks(Context context, String[] args) throws Exception {

        Map programMap = (Map) JPO.unpackArgs(args);

        // Get object id
        String objectId = (String) programMap.get("objectId");
        MapList result = new MapList();
        try {
            // find the configurable parameters
            DomainObject taskOID = new DomainObject(objectId);
            String planName = taskOID.getInfo(context, DomainConstants.SELECT_NAME);
            final String jpoName = "IMS_QP_DEPTask";
            final String methodName = "includeSearchDEP";
            HashMap uploadParamsMap = new HashMap();
            uploadParamsMap.put("objectId", objectId);

            String[] args1 = JPO.packArgs(uploadParamsMap);

            //Create list of existed task
            StringList select = new StringList();
            select.add(DomainConstants.SELECT_NAME);

            final MapList existedTasks = taskOID.getRelatedObjects(context,
                            REL_IMS_QP_QPlan2QPTask,
                            TYPE_IMS_QP_QPTask,
                            select,
                            new StringList(),
                            true,
                            true,
                            (short) 1,
                            "",
                            null,
                            0);

            Set<String> existedTasksNames = new HashSet<String>();
            if (existedTasks != null || existedTasks.size() > 0) {
                for (Iterator it = existedTasks.iterator(); it.hasNext(); ) {
                        Map mapInfo = (Map) it.next();
                        existedTasksNames.add((String)mapInfo.get(DomainConstants.SELECT_NAME));
                }

            }

            //Get relateded tasks from DEP
            StringList depTaskList = JPO.invoke(context, jpoName, null, methodName, args1, StringList.class);

            //Create task info for each task
            for (Object taskIdObj : depTaskList) {
                String depTaskId = (String) taskIdObj;
                DomainObject depTask = new DomainObject(depTaskId);
                String taskName = depTask.getInfo(context, DomainConstants.SELECT_NAME);
                String IMS_Name = depTask.getInfo(context, DomainObject.getAttributeSelect(ATT_IMS_Name));
                String IMS_NameRu = depTask.getInfo(context, DomainObject.getAttributeSelect(ATT_IMS_NameRu));
                String IMS_DescriptionEn = depTask.getInfo(context, DomainObject.getAttributeSelect(ATT_IMS_DescriptionEn));
                String IMS_DescriptionRu = depTask.getInfo(context, DomainObject.getAttributeSelect(ATT_IMS_DescriptionRu));
                String DEP_ID = depTask.getInfo(context, DomainConstants.SELECT_ID);


                StringList busSelects = new StringList();  // Object
                busSelects.add(DomainConstants.SELECT_ID);
                busSelects.add(DomainConstants.SELECT_NAME);
                final MapList expectedResObjects = depTask.getRelatedObjects(context,
                        REL_IMS_QP_ExpectedResult2DEPTask,
                        TYPE_IMS_QP_EXPECTED_RESULT,
                        busSelects,
                        new StringList(),
                        false,
                        true,
                        (short) 1,
                        "",
                        null,
                        0);

                //Go through all expected results
                for (Iterator it = expectedResObjects.iterator(); it.hasNext(); ) {
                    Map mapInfo = (Map) it.next();
                    String expResId = (String) mapInfo.get(DomainConstants.SELECT_ID);

                    DomainObject expRes = new DomainObject(expResId);

                    final MapList resultObjects = expRes.getRelatedObjects(context,
                        REL_IMS_QP_ResultType2ExpectedResult,
                        TYPE_IMS_QP_RESULTTYPE,
                        busSelects,
                        new StringList(),
                        true,
                        true,
                        (short) 1,
                        "",
                        null,
                        0);
                    //Go through all expected results
                    for (Iterator it2 = resultObjects.iterator(); it2.hasNext(); ) {
                        Map mapInfo2 = (Map) it2.next();
                        String artName = (String) mapInfo2.get(DomainConstants.SELECT_NAME);
                        //System.out.println("artName " + artName);

                        String newName = planName.split("-")[1] + "_" + taskName + "_" + artName;
                        if (!existedTasksNames.contains(newName)) {
                            String RES_ID = (String) mapInfo2.get(DomainConstants.SELECT_ID);
                            Map mapQPTask = new HashMap();
                            mapQPTask.put(ATT_IMS_DescriptionEn, IMS_DescriptionEn);
                            mapQPTask.put(ATT_IMS_DescriptionRu, IMS_DescriptionRu);
                            mapQPTask.put(ATT_IMS_NameRu, IMS_NameRu);
                            mapQPTask.put(ATT_IMS_Name, IMS_Name);
                            mapQPTask.put(FIELD_IMS_CODE, newName);
                            mapQPTask.put(FIELD_TASKNAME, taskName);
                            mapQPTask.put(FIELD_IMS_DEP_ID, DEP_ID);
                            mapQPTask.put(FIELD_IMS_EXP_ID, expResId);
                            mapQPTask.put(FIELD_IMS_RES_ID, RES_ID);
                            mapQPTask.put(DomainConstants.SELECT_ID, UUID.randomUUID().toString());
                            result.add(mapQPTask);
                        }
                    }
                }
            }
        } catch (Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
        return result;
    }

    public  Vector getCodeColumn (Context context, String[] args) throws Exception {
        return getVectorValues(context, args, FIELD_IMS_CODE);
    }
    public  Vector getTaskName (Context context, String[] args) throws Exception {
        return getVectorValues(context, args, FIELD_TASKNAME);
    }
    public  Vector getNameColumn (Context context, String[] args) throws Exception {
        return getVectorValues(context, args, ATT_IMS_Name);
    }
    public  Vector getNameRUColumn (Context context, String[] args) throws Exception {
        return getVectorValues(context, args, ATT_IMS_NameRu);
    }
    public  Vector getDescriptionENColumn (Context context, String[] args) throws Exception {
        return getVectorValues(context, args, ATT_IMS_DescriptionEn);
    }
    public  Vector getDescriptionRUColumn (Context context, String[] args) throws Exception {
        return getVectorValues(context, args, ATT_IMS_DescriptionRu);
    }

    private Vector getVectorValues(Context context, String[] args, String key) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList)programMap.get("objectList");
            Map paramList = (Map)programMap.get("paramList");
            String languageStr = (String)paramList.get("languageStr");

            Map mapObjectInfo = null;
            String value = null;

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext(); ) {
                mapObjectInfo = (Map) itrObjects.next();

                value = (String)mapObjectInfo.get(key);

                // Add to result vector
                vecResult.add(value);
            }

            return vecResult;
        }
        catch(Exception exp) {
            exp.printStackTrace();
            throw exp;
        }
    }

    public void createAndConnectTask(Context context, String[] args) throws Exception {
        Map objInfo = JPO.unpackArgs(args);
        String name = (String) objInfo.get(FIELD_IMS_CODE);
        String qpID = (String) objInfo.get(FIELD_IMS_PARENT_ID);
        String IMS_Name = (String) objInfo.get(ATT_IMS_Name);
        String IMS_NameRu = (String) objInfo.get(ATT_IMS_NameRu);
        String IMS_DescriptionEn = (String) objInfo.get(ATT_IMS_DescriptionEn);
        String IMS_DescriptionRu = (String) objInfo.get(ATT_IMS_DescriptionRu);
        String IMS_DEP_ID = (String) objInfo.get(FIELD_IMS_DEP_ID);
        String IMS_EXP_ID = (String) objInfo.get(FIELD_IMS_EXP_ID);
        String IMS_RES_ID = (String) objInfo.get(FIELD_IMS_RES_ID);

        ContextUtil.startTransaction(context, true);
        DomainObject taskObj = new DomainObject();
        taskObj.createObject(context,
                TYPE_IMS_QP_QPTask,
                name,
                "",
                POLICY_IMS_QP_QPTask,
                context.getVault().getName());

        taskObj.setAttributeValue(context, ATT_IMS_Name, IMS_Name);
        taskObj.setAttributeValue(context, ATT_IMS_NameRu, IMS_NameRu);
        taskObj.setAttributeValue(context, ATT_IMS_DescriptionEn, IMS_DescriptionEn);
        taskObj.setAttributeValue(context, ATT_IMS_DescriptionRu, IMS_DescriptionRu);
        DomainRelationship.connect(context, new DomainObject(qpID), REL_IMS_QP_QPlan2QPTask, taskObj);

        //connect to dep task
        DomainRelationship.connect(context, new DomainObject(IMS_DEP_ID), REL_IMS_QP_DEPTask2QPTask, taskObj);

        //create expected result
        String expName = name;
        DomainObject expObj = new DomainObject();
        expObj.createObject(context,
                TYPE_IMS_QP_EXPECTED_RESULT,
                expName,
                "",
                TYPE_IMS_QP_EXPECTED_RESULT,
                context.getVault().getName());

        //connect expected resulte to task
        DomainRelationship.connect(context, taskObj, REL_IMS_QP_ExpectedResult2QPTask, expObj);

        //connect expected result to  type
        DomainRelationship.connect(context, new DomainObject(IMS_RES_ID), REL_IMS_QP_ResultType2ExpectedResult, expObj);

        ContextUtil.commitTransaction(context);
    }
}
