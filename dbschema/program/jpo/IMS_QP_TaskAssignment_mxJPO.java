import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_TaskAssignment_mxJPO {
    public static final String TYPE_IMS_QP_EXPECTED_RESULT = IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult;
    public static final String TYPE_IMS_QP_QPTask = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;
    public static final String TYPE_IMS_QP_QPlan = IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan;
    public static final String POLICY_IMS_QP_QPTask = IMS_QP_Constants_mxJPO.policy_IMS_QP_QPTask;
    public static final String REL_IMS_QP_ExpectedResult2DEPTask = IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2DEPTask;
    public static final String REL_IMS_QP_ResultType2ExpectedResult = IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult;
    public static final String REL_IMS_QP_ExpectedResult2QPTask = IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask;
    public static final String REL_IMS_QP_QPlan2QPTask = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask;
    public static final String REL_IMS_QP_DEPTask2QPTask = IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask;
    public static final String REL_IMS_QP_QP2QPlan = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QP2QPlan;
    public static final String REL_IMS_QP_QPlan2Object = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object;
    public static final String REL_IMS_QP_DEP2QPlan = IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan;
    public static final String REL_IMS_QP_QPTask2QPTask = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask;
    public static final String REL_IMS_QP_DEPTaskStatus
            = IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTaskStatus;

    public static final String ATT_IMS_Name = IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_Name;
    public static final String ATT_IMS_NameRu = IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu;
    public static final String ATT_IMS_DescriptionEn = IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionEn;
    public static final String ATT_IMS_DescriptionRu = IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionRu;
    public static final String ATT_IMS_QP_FACT_EXP = IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_FACT_EXP;

    public static final String SELECT_RELATED_EXPECTED_RES = String.format(
            "to[%s].from.to[%s].from.type",
            REL_IMS_QP_ExpectedResult2DEPTask,
            REL_IMS_QP_ResultType2ExpectedResult);

    public static final String SELECT_RELATED_DEP_ID = String.format(
            "to[IMS_QP_DEPTask2QPTask].from.id",
            REL_IMS_QP_DEPTask2QPTask);

    public static final String SELECT_RELATED_RESULT_TYPE = String.format(
            "from[%s].to.to[%s].from.id",
            REL_IMS_QP_ExpectedResult2QPTask,
            REL_IMS_QP_ResultType2ExpectedResult);

    public static final String SELECT_INPUT_TASKS = String.format(
            "to[%s].from.id",
            REL_IMS_QP_QPTask2QPTask);
    public static final String SELECT_SYSTEM = String.format(
            "from[%s].to.name",
            REL_IMS_QP_QPlan2Object);

    public static final String TYPE_IMS_QP_RESULTTYPE = IMS_QP_Constants_mxJPO.type_IMS_QP_ResultType
            + "," + IMS_QP_Constants_mxJPO.type_IMS_Family;
    public static final String FIELD_IMS_CODE = "IMS_CODE";
    public static final String FIELD_IMS_DEP_ID = "IMS_DEP_ID";
    public static final String FIELD_IMS_EXP_ID = "IMS_EXP_ID";
    public static final String FIELD_TASKNAME = "TASKNAME";
    public static final String FIELD_IMS_RES_ID = "IMS_RES_ID";
    public static final String FIELD_IMS_PARENT_ID = "id[parent]";

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public MapList getAvailableTasks(Context context, String[] args) throws Exception {

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
                    REL_IMS_QP_QPlan2QPTask, TYPE_IMS_QP_QPTask, select, new StringList(),
                    true, true, (short) 1, "", null, 0);

            Set<String> existedTasksNames = new HashSet<String>();
            if (existedTasks != null || existedTasks.size() > 0) {
                for (Iterator it = existedTasks.iterator(); it.hasNext(); ) {
                    Map mapInfo = (Map) it.next();
                    existedTasksNames.add((String) mapInfo.get(DomainConstants.SELECT_NAME));
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


                StringList busSelects = new StringList();
                busSelects.add(DomainConstants.SELECT_ID);
                busSelects.add(DomainConstants.SELECT_NAME);
                final MapList expectedResObjects = depTask.getRelatedObjects(context,
                        REL_IMS_QP_ExpectedResult2DEPTask, TYPE_IMS_QP_EXPECTED_RESULT, busSelects,
                        new StringList(), false, true, (short) 1, "", null, 0);

                //Go through all expected results
                for (Iterator it = expectedResObjects.iterator(); it.hasNext(); ) {
                    Map mapInfo = (Map) it.next();
                    String expResId = (String) mapInfo.get(DomainConstants.SELECT_ID);

                    DomainObject expRes = new DomainObject(expResId);

                    final MapList resultObjects = expRes.getRelatedObjects(context,
                            REL_IMS_QP_ResultType2ExpectedResult, TYPE_IMS_QP_RESULTTYPE, busSelects,
                            new StringList(), true, true, (short) 1, "", null, 0);
                    //Go through all result types
                    for (Iterator it2 = resultObjects.iterator(); it2.hasNext(); ) {
                        Map mapInfo2 = (Map) it2.next();
                        String artName = (String) mapInfo2.get(DomainConstants.SELECT_NAME);

                        String[] temp = planName.split("-");
                        String suffix = null;
                        if (temp.length > 1) {
                            suffix = temp[1];
                        } else {
                            temp = planName.split("_");
                            if (temp.length > 1) {
                                suffix = temp[1];
                            } else {
                                suffix = planName;
                            }
                        }

                        String newName = suffix + "_" + taskName + "_" + artName;
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

    public Vector getCodeColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, FIELD_IMS_CODE);
    }

    public Vector getTaskName(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, FIELD_TASKNAME);
    }

    public Vector getNameColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, ATT_IMS_Name);
    }

    public Vector getNameRUColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, ATT_IMS_NameRu);
    }

    public Vector getDescriptionENColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, ATT_IMS_DescriptionEn);
    }

    public Vector getDescriptionRUColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, ATT_IMS_DescriptionRu);
    }

    private Vector getVectorValues(Context context, String[] args, String key) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            HashMap programMap = (HashMap) JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");
            Map paramList = (Map) programMap.get("paramList");
            String languageStr = (String) paramList.get("languageStr");

            Map mapObjectInfo = null;
            String value = null;

            for (Iterator itrObjects = objectList.iterator(); itrObjects.hasNext(); ) {
                mapObjectInfo = (Map) itrObjects.next();

                value = (String) mapObjectInfo.get(key);

                // Add to result vector
                vecResult.add(value);
            }

            return vecResult;
        } catch (Exception exp) {
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
        taskObj.createObject(context, TYPE_IMS_QP_QPTask, name, "", POLICY_IMS_QP_QPTask, context.getVault().getName());

        taskObj.setAttributeValue(context, ATT_IMS_Name, IMS_Name);
        taskObj.setAttributeValue(context, ATT_IMS_NameRu, IMS_NameRu);
        taskObj.setAttributeValue(context, ATT_IMS_DescriptionEn, IMS_DescriptionEn);
        taskObj.setAttributeValue(context, ATT_IMS_DescriptionRu, IMS_DescriptionRu);
        DomainRelationship.connect(context, new DomainObject(qpID), REL_IMS_QP_QPlan2QPTask, taskObj);

        //connect to dep task
        DomainRelationship.connect(context, new DomainObject(IMS_DEP_ID), REL_IMS_QP_DEPTask2QPTask, taskObj);
        taskObj.setAttributeValue(context, ATT_IMS_QP_FACT_EXP, "1");
        IMS_QP_DEPTask_mxJPO.addFactExpTree(context, new DomainObject(IMS_DEP_ID), 1);
        IMS_QP_DEPTask_mxJPO.setFactExp(context, new DomainObject(qpID), 1);

        //create expected result
        String expName = name;
        DomainObject expObj = new DomainObject();
        expObj.createObject(context, TYPE_IMS_QP_EXPECTED_RESULT, expName, "", TYPE_IMS_QP_EXPECTED_RESULT, context.getVault().getName());

        //connect expected resulte to task
        DomainRelationship.connect(context, taskObj, REL_IMS_QP_ExpectedResult2QPTask, expObj);

        //connect expected result to  type
        DomainRelationship.connect(context, new DomainObject(IMS_RES_ID), REL_IMS_QP_ResultType2ExpectedResult, expObj);

        ContextUtil.commitTransaction(context);
    }

    /**
     * createQPlan(context, args);
     *
     * @param context
     * @param args
     * @throws Exception
     */
    static public void copyTasks(Context context, String[] args) throws Exception {
        HashMap programMap = JPO.unpackArgs(args);
        HashMap requestMap = (HashMap) programMap.get("requestMap");

        String createFromId = (String) requestMap.get("rowIds");
        String systems = (String) requestMap.get("systemOID");
        String parentId = (String) requestMap.get("parentOID");
        String objectId = (String) requestMap.get("objectId");
        String description = (String) requestMap.get("description");

        boolean useConnection = Boolean.valueOf((String) requestMap.get("useconnection"));

        //getting DEP info from the main task
        DomainObject fromObject = new DomainObject(objectId);
        String fromMainSystemToDepID = fromObject.getInfo(context, "from[IMS_QP_QPlan2Object].to.from[IMS_PBS2DEP].to.id");
        fromMainSystemToDepID = UIUtil.isNotNullAndNotEmpty(fromMainSystemToDepID) ? fromMainSystemToDepID : "empty";

        try {
            StringList systemArray = FrameworkUtil.split(systems, "|");
            for (Object id : systemArray) {
                DomainObject system = new DomainObject((String) id);

                //check equals dep IDs of the main task with the copied Task
                String systemToDepID = system.getInfo(context, "from[IMS_PBS2DEP].to.id");
                systemToDepID = UIUtil.isNotNullAndNotEmpty(systemToDepID) ? systemToDepID : "not equals";
                if (!fromMainSystemToDepID.equals(systemToDepID)) continue;

                //check the system owner
                String systemOwner = system.getInfo(context, "from[IMS_PBS2Owner].to.name");
                systemOwner = UIUtil.isNotNullAndNotEmpty(systemOwner) ? systemOwner : "";
                if (!systemOwner.equals(context.getUser())) continue;

                //get system name
                String systemName = system.getInfo(context, DomainObject.SELECT_NAME);

                //start transactional
                ContextUtil.startTransaction(context, true);
                DomainObject planObj = new DomainObject();
                String newObjName = "QP_" + systemName;
                //create object
                planObj.createObject(context, TYPE_IMS_QP_QPlan, newObjName, "", TYPE_IMS_QP_QPlan, context.getVault().getName());
                if (UIUtil.isNotNullAndNotEmpty(description)) {
                    planObj.setDescription(context, description);
                }

                //Connect with SQP BQP
                DomainRelationship.connect(context, new DomainObject(parentId), REL_IMS_QP_QP2QPlan, planObj);

                //Connect with system and dep
                DomainRelationship.connect(context, planObj, REL_IMS_QP_QPlan2Object, system);
                connectToDep(context, new DomainObject(createFromId), planObj);

                //Copy tasks
                copyQPTasks(context, new DomainObject(createFromId), planObj, useConnection);
                IMS_QP_DEPTask_mxJPO.setFactExp(context, planObj, 1);

                //end transaction
                ContextUtil.commitTransaction(context);
            }
        } catch (Exception e) {
            LOG.error("exception of copied of the tasks: " + e.getMessage());
            e.printStackTrace();
            ContextUtil.abortTransaction(context);
        }

    }

    public static void copyQPTasks(Context context, DomainObject fromPlan, DomainObject toPlan, boolean useconnection) throws Exception {
        String examPlanName = fromPlan.getInfo(context, DomainConstants.SELECT_NAME);
        String fromSystem = fromPlan.getInfo(context, SELECT_SYSTEM);
        String targetPlanName = toPlan.getInfo(context, DomainConstants.SELECT_NAME);
        String systemName = toPlan.getInfo(context, SELECT_SYSTEM);

        //Create list of existed task
        StringList select = new StringList();
        select.add(DomainConstants.SELECT_NAME);
        select.add(DomainConstants.SELECT_ID);
        select.add(SELECT_RELATED_DEP_ID);
        select.add(SELECT_RELATED_RESULT_TYPE);
        select.add(SELECT_INPUT_TASKS);

        final MapList existedTasks = fromPlan.getRelatedObjects(context,
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

        Map<String, String> existedTasksNames = new HashMap<String, String>();
        StringList newIds = new StringList();
        if (existedTasks != null || existedTasks.size() > 0) {
            for (Iterator it = existedTasks.iterator(); it.hasNext(); ) {
                Map mapInfo = (Map) it.next();
                String examTaskId = (String) mapInfo.get(DomainConstants.SELECT_ID);
                String examTaskName = (String) mapInfo.get(DomainConstants.SELECT_NAME);
                String depId = (String) mapInfo.get(SELECT_RELATED_DEP_ID);
                String resTypeId = (String) mapInfo.get(SELECT_RELATED_RESULT_TYPE);

                String targetTaskName = systemName + examTaskName.substring(fromSystem.length(), examTaskName.length());

                DomainObject examTask = new DomainObject(examTaskId);
                DomainObject targetTask = new DomainObject(examTask.cloneObject(context, targetTaskName, "", context.getVault().getName(), false));

                //connect task to plan
                DomainRelationship.connect(context, toPlan, REL_IMS_QP_QPlan2QPTask, targetTask);

                //connect to dep task
                DomainRelationship.connect(context, new DomainObject(depId), REL_IMS_QP_DEPTask2QPTask, targetTask);

                //create expected result
                DomainObject expObj = new DomainObject();
                expObj.createObject(context,
                        TYPE_IMS_QP_EXPECTED_RESULT,
                        targetTaskName,
                        "",
                        TYPE_IMS_QP_EXPECTED_RESULT,
                        context.getVault().getName());

                //connect expected result to task
                DomainRelationship.connect(context, targetTask, REL_IMS_QP_ExpectedResult2QPTask, expObj);

                //connect expected result to type
                DomainRelationship.connect(context, new DomainObject(resTypeId), REL_IMS_QP_ResultType2ExpectedResult, expObj);

                existedTasksNames.put(examTaskName, targetTask.getId(context));

                targetTask.setAttributeValue(context, ATT_IMS_QP_FACT_EXP, "1");

                newIds.add(targetTask.getId(context));
            }
            //connect all tasks between each other
            if (useconnection) {
                Iterator it = existedTasks.iterator();
                for (Object newId : newIds) {
                    Map mapInfo = (Map) it.next();
                    String examTaskName = (String) mapInfo.get(DomainConstants.SELECT_NAME);
                    Object obj = mapInfo.get(SELECT_INPUT_TASKS);
                    StringList inputTasks = null;
                    if (obj instanceof StringList) {
                        inputTasks = (StringList) obj;
                    } else if (obj instanceof String) {
                        inputTasks = new StringList();
                        inputTasks.add((String) obj);
                    }

                    if (inputTasks != null && inputTasks.size() > 0) {
                        for (Object rawId : inputTasks) {
                            String taskId = (String) rawId;
                            DomainObject task = new DomainObject(taskId);
                            String name = task.getInfo(context, DomainConstants.SELECT_NAME);
                            String temp = existedTasksNames.get(name);

                            String id = (String) newId;
                            if (UIUtil.isNotNullAndNotEmpty(temp)) {
                                DomainRelationship rel = DomainRelationship.connect(context, new DomainObject(temp), REL_IMS_QP_QPTask2QPTask, new DomainObject(id));
                                rel.setAttributeValue(context, REL_IMS_QP_DEPTaskStatus, "Approved");
                            } else {
                                DomainRelationship.connect(context, new DomainObject(taskId), REL_IMS_QP_QPTask2QPTask, new DomainObject(id));
                            }
                        }
                    }
                }
            }
        }
    }

    public static void connectToDep(Context context, DomainObject fromPlan, DomainObject toPlan) throws Exception {
        //get target dep
        Map depMap = fromPlan.getRelatedObject(
                context,
                REL_IMS_QP_DEP2QPlan, false,
                new StringList(new String[]{
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_NAME,
                }),
                null);
        String depId = (String) depMap.get(DomainConstants.SELECT_ID);
        DomainRelationship.connect(context, new DomainObject(depId), REL_IMS_QP_DEP2QPlan, toPlan);
        IMS_QP_DEPTask_mxJPO.addFactExpTree(context, new DomainObject(depId), 1);
    }

    public static Object getCheckboxUseConnection(Context context, String[] args) throws Exception {
        Map<String, Object> tempMap = new HashMap();
        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();
        fieldDisplayRangeValues.addElement(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.label.Yes", context.getLocale()));
        fieldDisplayRangeValues.addElement(EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.label.No", context.getLocale()));

        fieldRangeValues.addElement("true");
        fieldRangeValues.addElement("false");
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);
        return tempMap;
    }

    public static StringList getFactColumnStyle(Context context, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        MapList mlObList = (MapList) programMap.get("objectList");
        StringList returnList = new StringList();
        for (Object objTemp : mlObList) {
            String sID = (String) ((Map) objTemp).get(DomainObject.SELECT_ID);
            DomainObject object = new DomainObject(sID);
            int factExp = Integer.parseInt(object.getInfo(context, IMS_QP_DEPTask_mxJPO.SELECT_ATTRIBUTE_IMS_QP_FACT_EXP));
            int factGot = Integer.parseInt(object.getInfo(context, IMS_QP_DEPTask_mxJPO.SELECT_ATTRIBUTE_IMS_QP_FACT_GOT));
            IMS_QP_DEPTask_mxJPO.getColor(returnList, factExp, factGot);
        }
        return returnList;
    }
}
