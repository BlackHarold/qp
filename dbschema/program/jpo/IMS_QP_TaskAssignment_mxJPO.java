import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.Set;
import java.util.*;

public class IMS_QP_TaskAssignment_mxJPO {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public static final String SELECT_RELATED_EXPECTED_RES = String.format(
            "to[%s].from.to[%s].from.type",
            IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2DEPTask,
            IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult);

    public static final String SELECT_RELATED_DEP_ID = String.format(
            "to[IMS_QP_DEPTask2QPTask].from.id",
            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask);

    public static final String SELECT_RELATED_RESULT_TYPE = String.format(
            "from[%s].to.to[%s].from.id",
            IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask,
            IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult);

    public static final String SELECT_RELATED_EXPECTED_RESULT = String.format(
            "from[%s].to.id",
            IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask);

    public static final String SELECT_INPUT_TASKS = String.format(
            "to[%s].from.id",
            IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask);
    public static final String SELECT_SYSTEM = String.format(
            "from[%s].to.name",
            IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object);

    public static final String TYPE_IMS_QP_RESULTTYPE = IMS_QP_Constants_mxJPO.type_IMS_QP_ResultType
            + "," + IMS_QP_Constants_mxJPO.type_IMS_Family;
    public static final String FIELD_IMS_CODE = "IMS_CODE";
    public static final String FIELD_IMS_DEP_ID = "IMS_DEP_ID";
    public static final String FIELD_IMS_EXP_ID = "IMS_EXP_ID";
    public static final String FIELD_TASK_NAME = "TASKNAME";
    public static final String FIELD_IMS_RES_ID = "IMS_RES_ID";
    public static final String FIELD_IMS_PARENT_ID = "id[parent]";

    public MapList getAvailableTasks(Context context, String[] args) throws Exception {

        Map programMap = JPO.unpackArgs(args);

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
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask, IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask, select, new StringList(),
                    true, true, (short) 1, "", null, 0);

            Set<String> existedTasksNames = new HashSet<String>();
            if (existedTasks != null || existedTasks.size() > 0) {
                for (Iterator it = existedTasks.iterator(); it.hasNext(); ) {
                    Map mapInfo = (Map) it.next();
                    existedTasksNames.add((String) mapInfo.get(DomainConstants.SELECT_NAME));
                }
            }

            //Get related tasks from DEP
            StringList depTaskList = JPO.invoke(context, jpoName, null, methodName, args1, StringList.class);

            //Create task info for each task
            for (Object taskIdObj : depTaskList) {
                String depTaskId = (String) taskIdObj;
                DomainObject depTask = new DomainObject(depTaskId);
                String taskName = depTask.getInfo(context, DomainConstants.SELECT_NAME);
                String IMS_Name = depTask.getInfo(context, DomainObject.getAttributeSelect(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_Name));
                String IMS_NameRu = depTask.getInfo(context, DomainObject.getAttributeSelect(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu));
                String IMS_DescriptionEn = depTask.getInfo(context, DomainObject.getAttributeSelect(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionEn));
                String IMS_DescriptionRu = depTask.getInfo(context, DomainObject.getAttributeSelect(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionRu));
                String DEP_ID = depTask.getInfo(context, DomainConstants.SELECT_ID);


                StringList busSelects = new StringList();
                busSelects.add(DomainConstants.SELECT_ID);
                busSelects.add(DomainConstants.SELECT_NAME);
                final MapList expectedResObjects = depTask.getRelatedObjects(context,
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2DEPTask, IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult, busSelects,
                        new StringList(), false, true, (short) 1, "", null, 0);

                //Go through all expected results
                for (Iterator it = expectedResObjects.iterator(); it.hasNext(); ) {
                    Map mapInfo = (Map) it.next();
                    String expResId = (String) mapInfo.get(DomainConstants.SELECT_ID);

                    DomainObject expRes = new DomainObject(expResId);
                    final MapList resultObjects = expRes.getRelatedObjects(context,
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult, TYPE_IMS_QP_RESULTTYPE, busSelects,
                            new StringList(), true, true, (short) 1, "", null, 0);
                    //Go through all result types
                    for (Iterator it2 = resultObjects.iterator(); it2.hasNext(); ) {
                        Map mapInfo2 = (Map) it2.next();
                        String artName = (String) mapInfo2.get(DomainConstants.SELECT_NAME);
                        String documentCodeFromExpectedResult = expRes.getAttributeValue(context, "IMS_QP_DocumentCode");

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
//                        if (!existedTasksNames.contains(newName)) {
                        String RES_ID = (String) mapInfo2.get(DomainConstants.SELECT_ID);
                        Map mapQPTask = new HashMap();
                        mapQPTask.put(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionEn, IMS_DescriptionEn);
                        mapQPTask.put(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionRu, IMS_DescriptionRu);
                        mapQPTask.put(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu, IMS_NameRu);
                        mapQPTask.put(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_Name, IMS_Name);
                        mapQPTask.put(FIELD_IMS_CODE, newName);
                        mapQPTask.put(FIELD_TASK_NAME, taskName);
                        mapQPTask.put(FIELD_IMS_DEP_ID, DEP_ID);
                        mapQPTask.put(FIELD_IMS_EXP_ID, expResId);
                        mapQPTask.put(FIELD_IMS_RES_ID, RES_ID);
                        mapQPTask.put(DomainConstants.SELECT_ID, UUID.randomUUID().toString());
                        mapQPTask.put("doc_code", documentCodeFromExpectedResult);
                        result.add(mapQPTask);
//                        }
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

    public Vector getDocCodeColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, "doc_code");
    }

    public Vector getTaskName(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, FIELD_TASK_NAME);
    }

    public Vector getNameColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_Name);
    }

    public Vector getNameRUColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu);
    }

    public Vector getDescriptionENColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionEn);
    }

    public Vector getDescriptionRUColumn(Context context, String[] args) throws Exception {
        return getVectorValues(context, args, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionRu);
    }

    private Vector getVectorValues(Context context, String[] args, String key) throws Exception {
        try {
            // Create result vector
            Vector vecResult = new Vector();

            HashMap programMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) programMap.get("objectList");

            Map mapObjectInfo;
            String value;

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
        String IMS_Name = (String) objInfo.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_Name);
        String IMS_NameRu = (String) objInfo.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu);
        String IMS_DescriptionEn = (String) objInfo.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionEn);
        String IMS_DescriptionRu = (String) objInfo.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionRu);
        String IMS_DEP_ID = (String) objInfo.get(FIELD_IMS_DEP_ID);
        String IMS_EXP_ID = (String) objInfo.get(FIELD_IMS_EXP_ID);
        String IMS_RES_ID = (String) objInfo.get(FIELD_IMS_RES_ID);

        ContextUtil.startTransaction(context, true);

        DomainObject taskObj = new DomainObject(
                new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask, name, "", context.getVault().getName()));

        if (!taskObj.exists(context)) {
            taskObj.create(context, IMS_QP_Constants_mxJPO.policy_IMS_QP_QPTask);

        } else {
            int counter;
            MapList tasksByName = DomainObject.findObjects(context,
                    /*types*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    /*vault*/ IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    /*where*/"name smatch '" + taskObj.getName() + "*'",
                    /*selects*/new StringList("id"));
            boolean isUniqueName = false;
            counter = tasksByName.size();

            while (!isUniqueName) {
                counter++;
                name = new StringBuilder(name).append((counter < 10 ? "0" + counter : counter)).toString();

                DomainObject taskObjectWithCounter = new DomainObject(
                        new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask, name, "", context.getVault().getName()));
                if (!taskObjectWithCounter.exists(context)) {
                    taskObjectWithCounter.create(context, IMS_QP_Constants_mxJPO.policy_IMS_QP_QPTask);
                    isUniqueName = true;
                    taskObj = taskObjectWithCounter;
                }
            }
        }

        taskObj.setAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_Name, IMS_Name);
        taskObj.setAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu, IMS_NameRu);
        taskObj.setAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionEn, IMS_DescriptionEn);
        taskObj.setAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_DescriptionRu, IMS_DescriptionRu);
        DomainRelationship.connect(context,/*from*/ new DomainObject(qpID), IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask, /*to*/taskObj);

        //connect to dep task
        DomainRelationship.connect(context, new DomainObject(IMS_DEP_ID), IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask, taskObj);
        taskObj.setAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_FACT_EXP, "1");

        //create expected result
        DomainObject expObj = new DomainObject(
                new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult, name, "", context.getVault().getName()));
        if (UIUtil.isNotNullAndNotEmpty(name)) {

            if (!expObj.exists(context)) {
                expObj.create(context, IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult);
            } else {
                int counter;
                MapList expectedResultsByName = DomainObject.findObjects(context,
                        /*types*/IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                        /*vault*/ IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                        /*where*/"name smatch '" + expObj.getName() + "*'",
                        /*selects*/new StringList("id"));
                boolean isUniqueName = false;
                counter = expectedResultsByName.size();

                while (!isUniqueName) {
                    counter++;
                    name = new StringBuilder(name).append((counter < 10 ? "0" + counter : counter)).toString();

                    DomainObject expectedResultObjectWithCounter = new DomainObject(
                            new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult, name, "", context.getVault().getName()));
                    if (!expectedResultObjectWithCounter.exists(context)) {
                        expectedResultObjectWithCounter.create(context, IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult);
                        isUniqueName = true;
                        expObj = expectedResultObjectWithCounter;
                    }
                }
            }

        } else {
            LOG.info("temp name: " + name);
            throw new MatrixException("error getting temp name for expected result: " + name);
        }

        //coping all attributes
        DomainObject expectedResult = new DomainObject(IMS_EXP_ID);
        copyAttributes(context, expectedResult, expObj);

        //connect expected result to task
        //TODO check that relationship arrow only from task -> to expected result (copy only output expected result)
        DomainRelationship.connect(context, taskObj, IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask, expObj);
        //connect expected result to  type
        DomainRelationship.connect(context, new DomainObject(IMS_RES_ID), IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult, expObj);

        try {
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            LOG.error("exception: " + e.getMessage());
        }
    }

    /**
     * @param context usual parameter
     * @param object1 DomainObject copied from
     * @param object2 DomainObject copied to
     * @throws MatrixException throwable Matrix database exception throwable
     */
    private static void copyAttributes(Context context, DomainObject object1, DomainObject object2) throws MatrixException {
        object1.open(context);
        object2.open(context);

        BusinessObjectAttributes businessObjectAttributes = object1.getAttributes(context);
        AttributeList attributes = businessObjectAttributes.getAttributes();
        LOG.info("task assignments copied attributes: " + attributes);
        object2.setAttributes(context, attributes);
        object2.update(context);

        object1.close(context);
        object2.close(context);
    }

    /**
     * Deprecated since 17.11.2021 This method is no longer acceptable
     * <p> Use {@link IMS_QualityPlanBase_mxJPO#copyPlan(Context, String...)} instead
     * </p>
     * createQPlan(context, args);
     *
     * @param ctx
     * @param args
     * @throws Exception
     */
    @Deprecated
    public void copyTasks(Context ctx, String[] args) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        Map requestMap = (Map) programMap.get("requestMap");

        //from IMS_QP
        String parentId = (String) requestMap.get("parentOID");
        DomainObject qpParentObject = new DomainObject(parentId);
        String parentName = qpParentObject.getName(ctx);

        //from IMS_QP_QPlan
//        String createFromId = (String) requestMap.get("rowIds");

        String objectId = (String) requestMap.get("objectId");
        String description = (String) requestMap.get("description");

        boolean useConnection = Boolean.valueOf((String) requestMap.get("useconnection"));

        //getting Main plan & dep info from
        DomainObject fromObject;
        String fromMainSystemToDepID;
        try {
            fromObject = new DomainObject(objectId);
            fromMainSystemToDepID = fromObject.getInfo(ctx, "to[IMS_QP_DEP2QPlan].from.id");
            LOG.info("parent plan object " + fromObject.getInfo(ctx, "name") + " from Main System to Dep IDs: " + fromMainSystemToDepID);
        } catch (Exception e) {
            LOG.error("error get object from main plan dep: " + e.getMessage());
            e.printStackTrace();
        }


        //ll systems from field System/Building
        String systemFieldKey = getSystemFieldFromForm(parentName);
        String systems = (String) requestMap.get(systemFieldKey);
        StringList systemArray = FrameworkUtil.split(systems, "|");

        LOG.info("selected systems/buildings/FA: " + systemArray);
    }

    private String getSystemFieldFromForm(String parsedHeader) {
        if (UIUtil.isNotNullAndNotEmpty(parsedHeader) && parsedHeader.contains("AQP")) {
            return "system2OID";
        } else {
            return "system1OID";
        }

    }

    public static void copyQPTasks(Context context, DomainObject fromPlan, DomainObject toPlan, boolean useconnection) {
        String examPlanName, fromSystem = "", targetPlanName, systemName = "";
        MapList existedTasks = null;
        try {
            examPlanName = fromPlan.getInfo(context, DomainConstants.SELECT_NAME);
            fromSystem = fromPlan.getInfo(context, SELECT_SYSTEM);
            targetPlanName = toPlan.getInfo(context, DomainConstants.SELECT_NAME);
            systemName = toPlan.getInfo(context, SELECT_SYSTEM);
            LOG.info("from plan info: " + examPlanName + "|" + fromSystem + "|" + targetPlanName + "|" + systemName);

            //Create list of existed task
            StringList select = new StringList();
            select.add(DomainConstants.SELECT_NAME);
            select.add(DomainConstants.SELECT_ID);
            select.add(SELECT_RELATED_DEP_ID);
            select.add(SELECT_RELATED_RESULT_TYPE);
            select.add(SELECT_INPUT_TASKS);
            select.add(SELECT_RELATED_EXPECTED_RESULT);

            existedTasks = fromPlan.getRelatedObjects(context,
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                    IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    select,
                    new StringList(),
                    true,
                    true,
                    (short) 1,
                    DomainConstants.EMPTY_STRING,
                    null,
                    0);
        } catch (FrameworkException e) {
            LOG.error("error getting info: " + e.getMessage());
            e.printStackTrace();
        }

        Map<String, String> existedTasksNames = new HashMap<>();
        StringList newIds = new StringList();

        if (existedTasks != null || existedTasks.size() > 0) {
            Iterator it = existedTasks.iterator();
            while (it.hasNext()) {
                Map mapInfo = (Map) it.next();
                String examTaskId = (String) mapInfo.get(DomainConstants.SELECT_ID);
                String examTaskName = (String) mapInfo.get(DomainConstants.SELECT_NAME);
                String depId = (String) mapInfo.get(SELECT_RELATED_DEP_ID);
                String resTypeId = (String) mapInfo.get(SELECT_RELATED_RESULT_TYPE);

                String targetTaskName = systemName + examTaskName.substring(fromSystem.length());

                DomainObject examTask;
                DomainObject targetTask = null;
                try {
                    examTask = new DomainObject(examTaskId);
                    targetTask = new DomainObject(
                            examTask.cloneObject(context, targetTaskName, "", context.getVault().getName(), false));
                } catch (Exception e) {
                    LOG.error("error cloning objects: " + e.getMessage());
                    e.printStackTrace();
                }

                //connect task to plan
                try {
                    DomainRelationship.connect(context, toPlan, IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask, targetTask);
                    //connect to dep task
                    DomainRelationship.connect(context, new DomainObject(depId), IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask, targetTask);
                } catch (Exception e) {
                    LOG.error("error connecting objects: " + e.getMessage());
                    e.printStackTrace();
                }


                //create expected result
                DomainObject expObj = new DomainObject();
                try {
                    expObj.createObject(context, IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                            targetTaskName, "", IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult, context.getVault().getName());

                    //copy attributes
                    DomainObject expectedResult = new DomainObject((String) mapInfo.get(SELECT_RELATED_EXPECTED_RESULT));
                    copyAttributes(context, expectedResult, expObj);

                    //connect expected result to task
                    DomainRelationship.connect(context, targetTask, IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask, expObj);

                    //connect expected result to type
                    DomainRelationship.connect(context,
                            new DomainObject(resTypeId), IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult, expObj);

                    existedTasksNames.put(examTaskName, targetTask.getId(context));
                    targetTask.setAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_FACT_EXP, "1");

                    newIds.add(targetTask.getId(context));
                } catch (Exception e) {
                    LOG.error("other connecting errors: " + e.getMessage());
                    e.printStackTrace();
                }
            }
            //connect all tasks between each other
            if (useconnection) {
                Iterator taskIterator = existedTasks.iterator();
                for (Object newId : newIds) {
                    Map mapInfo = (Map) taskIterator.next();
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
                            try {
                                DomainObject task = new DomainObject(taskId);
                                String name = task.getInfo(context, DomainConstants.SELECT_NAME);
                                String temp = existedTasksNames.get(name);

                                String id = (String) newId;
                                if (UIUtil.isNotNullAndNotEmpty(temp)) {
                                    DomainRelationship rel = DomainRelationship.connect(context,
                                            new DomainObject(temp),
                                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                                            new DomainObject(id));
                                    rel.setAttributeValue(context,
                                            IMS_QP_Constants_mxJPO.attribute_IMS_QP_DEPTaskStatus,
                                            IMS_QP_Constants_mxJPO.APPROVED);
                                } else {
                                    DomainRelationship.connect(context,
                                            new DomainObject(taskId),
                                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                                            new DomainObject(id));
                                }
                            } catch (Exception e) {
                                LOG.error("other errors: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
    }

    public static String connectToDep(Context ctx, DomainObject fromPlan, DomainObject toPlan) throws Exception {
        LOG.info("target plan: " + fromPlan.getName(ctx) + "|" + fromPlan.getId(ctx));
        Map depMap = fromPlan.getRelatedObject(
                ctx,
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan, false,
                new StringList(new String[]{
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_NAME,
                }),
                null);
        String depId = (String) depMap.get(DomainConstants.SELECT_ID);
        DomainObject depObject = new DomainObject(depId);
        DomainRelationship.connect(ctx, depObject, IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan, toPlan);

        return depObject.getName(ctx);
    }

    public static Object getCheckboxUseConnection(Context context, String[] args) throws Exception {
        Map<String, Object> tempMap = new HashMap();
        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();
        fieldDisplayRangeValues.addElement(
                EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.label.Yes", context.getLocale()));
        fieldDisplayRangeValues.addElement(
                EnoviaResourceBundle.getFrameworkStringResourceProperty(context, "emxFramework.label.No", context.getLocale()));

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

            String color = "";

            //1 if attribute of task IMS_QP_SelectDocument has any values
            if (UIUtil.isNotNullAndNotEmpty(object.getInfo(context, IMS_QP_Constants_mxJPO.attribute_IMS_QP_SelectDocument)))
                color = "IMS_QP_Purple";

            //2 if attribute of expected result IMS_QP_DocumentCode contains value 'Wrong'
            String wrongCodeField = object.getInfo(context, String.format("from[%s].to.%s",
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask, IMS_QP_Constants_mxJPO.attribute_IMS_QP_DocumentCode));
            if (UIUtil.isNotNullAndNotEmpty(wrongCodeField) && wrongCodeField.contains("Wrong code"))
                color = "IMS_QP_Orange";

            //3 if the task has more than one expected result in direction 'Output'
            String moreThanOneExpectedRelations = MqlUtil.mqlCommand(context,
                    String.format("print bus %s select from[IMS_QP_ExpectedResult2QPTask].to.id dump |", object.getId(context)));
            if (UIUtil.isNotNullAndNotEmpty(moreThanOneExpectedRelations) && moreThanOneExpectedRelations.contains("|"))
                color = "IMS_QP_Yellow";

            String checkNoFact = object.getInfo(context,
                    String.format("from[%s]", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));

            //4 if the task is 'Another' type
            String resultType = object.getInfo(context,
                    "from[IMS_QP_ExpectedResult2QPTask].to.to[IMS_QP_ResultType2ExpectedResult].from.to[IMS_QP_ResultType2Family].from.name");
            boolean anotherTypeAndNoFact = IMS_QP_Constants_mxJPO.ANOTHER_PLAN_TYPES.equals(resultType) && "FALSE".equals(checkNoFact);

            //5 if the task is 'VTZ' type and attribute of expected result IMS_DocumentCode is empty value
            boolean vtzTypeAndNoFact = IMS_QP_Constants_mxJPO.VTZ_PLAN_TYPES.equals(resultType) && "FALSE".equals(checkNoFact);

            if (anotherTypeAndNoFact || vtzTypeAndNoFact) color = "IMS_QP_Blue";

            LOG.info(object.getName(context) + " color: " + color);
            IMS_QP_DEPTask_mxJPO.getColor(returnList, factExp, factGot, color);
        }
        return returnList;
    }

    public boolean isQPlanDraft(Context context, String... args) throws MatrixException {
        Map argsMap = null;
        String objectId = "";
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args: " + e.getMessage());
            e.printStackTrace();
        }

        objectId = (String) argsMap.get("objectId");
        DomainObject object;
        String qpState = "";
        try {
            object = new DomainObject(objectId);
            String select = (object != null && object.getType(context).equals(IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan)) ?
                    DomainConstants.SELECT_CURRENT :
                    String.format("to[%s].from.current", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask);
            qpState = object.getInfo(context, select);
        } catch (Exception e) {
            LOG.error("error getting info from object: " + e.getMessage());
            e.printStackTrace();
        }

        return "Draft".equals(qpState) && !IMS_QP_Security_mxJPO.isUserViewer(context);
    }
}
