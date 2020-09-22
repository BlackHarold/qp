import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QualityPlanBase_mxJPO extends DomainObject {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    /**
     * Method to deleting IMS_QP_DEPSubStages if their hasn't any SubTasks
     *
     * @param context
     * @param args
     */
    public Map deleteSubStages(Context context, String[] args) {

        //get all ids
        HashMap<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");
        String[] substagesIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            substagesIDs[i] = rowIDs[i].substring(0, rowIDs[i].indexOf("|"));
        }

        //selects all subtasks  with their subtasks
        StringList selects = new StringList();
        selects.add("id");
        selects.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPSUB_STAGE_2_DEPTASK);
        selects.add("name");

        MapList objectsInfo = null;
        try {
            objectsInfo = DomainObject.getInfo(context, substagesIDs, selects);
        } catch (FrameworkException e) {
            LOG.error("error getting info: " + e.getMessage());
            e.printStackTrace();
        }

        //check if substage has some subtask
        List<String> deletingIDs = new ArrayList();

        StringBuffer buffer = new StringBuffer("\nCouldn't to delete next stages: \n");
        List<String> badNames = new ArrayList<>();
        boolean flag = false;
        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            if ("TRUE".equals(map.get(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPSUB_STAGE_2_DEPTASK))) {
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

        //delete substages
        try {
            if (var1 != null && var1.length > 0)
                DomainObject.deleteObjects(context, var1);
            LOG.info("objects deleted: " + Arrays.deepToString(var1));
        } catch (Exception e) {
            LOG.error("delete error: " + e.getMessage());
            e.printStackTrace();
        }

        LOG.info("return map: " + mapMessage);
        return mapMessage;
    }

    /**
     * Method to show the table of IMS_SubStage elements
     */
    public MapList getAllSubStagesForTable(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add("id");

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        MapList result = parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/IMS_QP_Constants_mxJPO.IMS_QP_DEP_SUB_STAGE,
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 2,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);

        return result;
    }

    /**
     * Method to show the table of IMS_PBSSystem elements
     */
    public MapList getAllPBSSystemsForTable(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add("id");

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        MapList result = parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_PBSSystem",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 2,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);

        return result;
    }

    /**
     * Method to show the table of IMS_GBSBuilding elements
     */
    public MapList getAllGBSBuildingsForTable(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add("id");

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        MapList result = parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_GBSBuilding",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 2,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);

        return result;
    }

    /**
     * Method to show the table of IMS_PBSFunctionalArea elements
     */
    public MapList getAllFunctionalAreasForTable(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add("id");

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        MapList result = parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_PBSFunctionalArea",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 2,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);

        return result;
    }

    /**
     * Method to create new IMS_QP_DEPSubStage
     *
     * @param context
     * @param args
     * @throws Exception
     */

    public void createPostProcess(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        Map requestMap = (Map) argsMap.get("requestMap");
        Map paramMap = (Map) argsMap.get("paramMap");

        //all required params
        String projectStageID = (String) requestMap.get("project_stage");
        String parentOID = (String) requestMap.get("parentOID");
        String objectId = (String) requestMap.get("objectId");
        //sub_stage params
        String relId = (String) paramMap.get("relId");
        String newObjectId = (String) paramMap.get("newObjectId");

        //log all required initial params
        LOG.info("projectStageID: " + projectStageID);
        LOG.info("parentID: " + objectId);
        LOG.info("parentOID: " + parentOID);

        String baselineID = (String) requestMap.get("baseline");
        LOG.info("baselineID: " + baselineID);
        if (UIUtil.isNotNullAndNotEmpty(baselineID))
            baselineID = baselineID.substring(0, baselineID.indexOf("_")) != null ? baselineID.substring(0, baselineID.indexOf("_")) : "";
        else baselineID = "";

        String stage = (String) requestMap.get("stage");

        //parent
        DomainObject parent = new DomainObject(parentOID);
        DomainObject substage = new DomainObject(newObjectId);
        LOG.info("substage: " + substage + " id: " + newObjectId);

        StringList selects = new StringList();
        selects.add("id");

        String where = IMS_QP_Constants_mxJPO.FROM_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_TO_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID + parentOID;
//        print bus 59479.11672.23687.9771 select from[IMS_QP_DEP2DEPProjectStage].to.id;
//        print bus 59479.11672.23687.9771 select id from[IMS_QP_DEP2DEPProjectStage].to.to[IMS_QP_ProjectStage2DEPProjectStage].from.name;
        LOG.info("where: " + where);

        MapList result = findObjects(context,
                /*type*/ "IMS_ProjectStage",
                /*?*/ "*",
                /*where*/ where,
                selects
        );

        LOG.info(parentOID + " all IMS_ProjectStage: " + result);

        String allProjectStagesConnectingToDEP = "";
        for (Object temp : result) {
            Map map = (Map) temp;
            allProjectStagesConnectingToDEP += map.get("id") + "|";
        }


        LOG.info("allProjectStagesConnectingToDEP: " + allProjectStagesConnectingToDEP);
        LOG.info(parent.getInfo(context, "name") + " prepare parent stage: " + parent.getInfo(context, IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEP_2_DEPPROJECT_STAGE_TO_TO_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_FROM_ID));

        DomainObject newDEPProjectStage;
        try {
            ContextUtil.startTransaction(context, true);
            DomainObject projectStage = new DomainObject(projectStageID);

            String uniqueNameSubStage;
            if (!allProjectStagesConnectingToDEP.contains(projectStageID)) {

                //get name project_stage
                String nameProjectStage = projectStage.getInfo(context, "name");

                //create object T:IMS_QP_DEPProjectStage
                newDEPProjectStage = DomainObject.newInstance(context);
                //String revision = "DEP" + "-" + "nameProjectStage";
                newDEPProjectStage.createObject(context,
                        /*type*/"IMS_QP_DEPProjectStage",
                        /*name*/nameProjectStage,
                        /*revision*/parent.getName() + "-" + nameProjectStage,
                        /*policy*/"IMS_QP_DEPProjectStage",
                        /*vault*/ context.getVault().getName());
                LOG.info("new object created: " + newDEPProjectStage.getName() + ": " + newDEPProjectStage.getId(context));

                //set unique name
                uniqueNameSubStage = setUniqueFieldName(context, parent, stage, newDEPProjectStage);

                //connect parent -> object
                LOG.info(parent.getId(context) + "->IMS_QP_DEP2DEPProjectStage->" + newDEPProjectStage.getId(context));
                DomainRelationship.connect(context, parent, "IMS_QP_DEP2DEPProjectStage", newDEPProjectStage);

                //connect object -> new IMS_QP_DEPSubStage [objectID]
                LOG.info(newDEPProjectStage.getId(context) + "->IMS_QP_DEPProjectStage2DEPSubStage->" + substage);
                DomainRelationship.connect(context, newDEPProjectStage, "IMS_QP_DEPProjectStage2DEPSubStage", substage);

                //connect object <- IMS_ProjectStage [projectStageID]
                LOG.info(projectStage.getId(context) + "->IMS_QP_ProjectStage2DEPProjectStage->" + newDEPProjectStage.getId(context));
                DomainRelationship.connect(context, projectStage, "IMS_QP_ProjectStage2DEPProjectStage", newDEPProjectStage);

            } else {

                selects = new StringList();
                selects.add("id");

                where = "name smatch '*" + projectStage.getInfo(context, "name") + "*' && to[IMS_QP_DEP2DEPProjectStage].from.id==" + parentOID;
                LOG.info("else where: " + where);

                result = findObjects(context,
                        /*type*/ "IMS_QP_DEPProjectStage",
                        /*?*/ "*",
                        /*where*/ where,
                        selects
                );
                LOG.info("else result: " + result);

                Map map = (Map) result.get(0);
                String depProjectStageID = (String) map.get("id");
                LOG.info("depProjectStageID: " + depProjectStageID);

                //set unique name
                DomainObject depProjectStage = new DomainObject(depProjectStageID);
                uniqueNameSubStage = setUniqueFieldName(context, parent, stage, depProjectStage);

                //connect new IMS_QP_SubStage <- IMS_QP_DEPProjectStage <- projectStageID.
                LOG.info("depProjectStage: " + depProjectStage);
                LOG.info("substage: " + substage);
                DomainRelationship.connect(context, depProjectStage, "IMS_QP_DEPProjectStage2DEPSubStage", substage);
                LOG.info(depProjectStageID + "->IMS_QP_DEPProjectStage2DEPSubStage->" + objectId);
            }

            //connect new IMS_QP_SubStage <- IMS_QP_Baseline2DEPSubStage <- baselineID
            LOG.info(baselineID + "<- IMS_QP_BaseLine2DEPSubStage <- " + newObjectId);
            if (!baselineID.equals("")) {
                DomainRelationship.connect(context, new DomainObject(baselineID), "IMS_QP_BaseLine2DEPSubStage", substage);
            }

            LOG.info("set name to substage: " + substage);
            substage.setName(context, uniqueNameSubStage);
            LOG.info("set name to substage: " + substage.getId() + " " + substage.getName());

            ContextUtil.commitTransaction(context);
            LOG.info("transaction commited");
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOG.error("transaction aborted: " + e.getMessage());
        }
    }

    public void createQPlanPostProcess(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        Map requestMap = (Map) argsMap.get("requestMap");
        Map paramMap = (Map) argsMap.get("paramMap");

        String parentID = (String) requestMap.get("parentOID");

        String systemID = (String) requestMap.get("systemOID");
        String systemName = (String) requestMap.get("systemDisplay");

        String depFieldFromForm = (String) requestMap.get("dep");
        String cleanDepID = UIUtil.isNotNullAndNotEmpty(depFieldFromForm) ? depFieldFromForm.substring(0, depFieldFromForm.indexOf("_")) : "";

        String newObjectId = (String) paramMap.get("newObjectId");
        DomainObject newObject = new DomainObject(newObjectId);
        DomainObject objectPBS = new DomainObject();
        DomainObject objectDEP = new DomainObject(cleanDepID);
        String plantID = objectDEP.getInfo(context, "from[IMS_PBS2DEP].to.id");
        String qpPlanName = "";
        String relId = (String) paramMap.get("relId");
        LOG.info("systemID:" + systemID);
        if (systemName.isEmpty() && depFieldFromForm.endsWith("_FALSE")) {
            throw new Exception("You have to select a System!");
        } else if (!systemName.equals("empty") && depFieldFromForm.endsWith("_FALSE")) {
            DomainObject system = new DomainObject(systemID);
            String systemType = system.getType(context);

            /*check the owner of system*/
            String owners = system.getInfo(context, "from[IMS_PBS2Owner].to.name");
            LOG.info("owners: " + owners);
            String userName = context.getUser();
            if (owners == null || !owners.contains(userName)) {
                throw new MatrixException(String.format("%s is not QPlan owner of %s %s ", userName, systemType, systemName));
            }

            /*check the dep of system*/
            String systemDEPs = system.getInfo(context, "from[IMS_PBS2DEP].to.id");
            LOG.info(system.getName(context) + " system deps: " + systemDEPs + " clean dep: " + cleanDepID);
            if (systemDEPs == null || !systemDEPs.contains(cleanDepID)) {
                throw new MatrixException(String.format("%s does not include the specified dep", systemName));
            }

            DomainObject depObject = new DomainObject(cleanDepID);
            String depState = depObject.getInfo(context, "current");
            LOG.info("depState: " + depState);
            if (!depState.equals("Done")) {
                throw new MatrixException(String.format("%s is not finished yet and has status \'%s\'", depObject.getName(context), depState));
            }

            //check if system type is functional area
            boolean arrow = systemType.equals("IMS_PBSFunctionalArea");
            String forkConnection = arrow ? "from[IMS_PBSFunctionalArea2" + systemType + "].to" : "to[IMS_PBSFunctionalArea2" + systemType + "].from";
            LOG.info("fork arrow: " + forkConnection);

            //getting info about other QPlan connections
            String relationshipFromQPlan = UIUtil.isNotNullAndNotEmpty(system.getInfo(context, forkConnection + ".to[IMS_QP_QPlan2Object]")) ?
                    system.getInfo(context, forkConnection + ".to[IMS_QP_QPlan2Object]") : "";
            LOG.info("functional area to Q plan: " + relationshipFromQPlan);

            if (relationshipFromQPlan.equals("TRUE")) {
                String message = "this " + systemType.substring(systemType.lastIndexOf("_") + 1) + " is part of Functional Area";
                throw new MatrixException(message);
            }
            objectPBS.setId(systemID);
            qpPlanName = objectPBS.getName(context);
        } else if (depFieldFromForm.endsWith("_TRUE")) {
            if (UIUtil.isNotNullAndNotEmpty(plantID))
                objectPBS.setId(plantID);
            qpPlanName = "Plant_" + objectDEP.getName(context);
        }

        //log all needs params
        LOG.info("parent: " + parentID + "->relId: " + relId + "->newObjectId: " + newObjectId + " from DEP: " + cleanDepID + " to system: " + systemID);

        //create domain objects

        try {
            //start transactional
            ContextUtil.startTransaction(context, true);
            LOG.info("before set name: " + newObject.getName());

            //change name for new object mask: QP-PBS /QP-10FAL/
            newObject.setName(context, "QP-" + qpPlanName);
            LOG.info("new name: " + newObject.getName());

            //connect relations
            DomainRelationship.connect(context, objectDEP, "IMS_QP_DEP2QPlan", newObject);
            LOG.info("connect " + cleanDepID + " -> IMS_QP_DEP2QPlan ->" + newObject.getName());

            if (objectPBS.exists(context)) {
                DomainRelationship.connect(context, newObject, "IMS_QP_QPlan2Object", objectPBS);
                LOG.info("connect new object " + newObjectId + " -> IMS_QP_QPlan2Object ->" + objectPBS.getName());
            }

            //commit transaction
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOG.error("transaction aborted: " + e.getMessage());
        }
    }

    public String setUniqueFieldName(Context context, DomainObject parent, String stage, DomainObject depProjectStage) throws Exception {

        //set unique name for the new substage
        String indexParent = parent.getInfo(context, "attribute[IMS_QP_DEPShortCode]") != null ?
                parent.getInfo(context, "attribute[IMS_QP_DEPShortCode]") : "";
        String depProjectStageName = depProjectStage.getInfo(context, "name");

        //count all substages at this DEP ProjectStage
        StringList selects = new StringList();
        selects.add("id");

        String nameTail = !stage.equals("") ?
                "&&name smatch *" + depProjectStageName + stage + "*" :                                                 //if stage 1 or 2
                "&&name nsmatch *" + depProjectStageName + "1*&&name nsmatch *" + depProjectStageName + "2*";           //if stage is empty
        String where = IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_ID +
                depProjectStage.getInfo(context, "id") + nameTail;

        MapList result = findObjects(context,
                /*type*/ "IMS_QP_DEPSubStage",
                /*?*/ "*",
                /*where*/ where,
                selects
        );

        int count = 1 + result.size();
        String counter = count < 10 ? "0" + count : "" + count;
        LOG.info("set unique name counter: " + counter);

        //concate unique name
        String uniqueName = indexParent + depProjectStageName + stage + "-" + counter;

        LOG.info("unique name to return: " + uniqueName);
        //return new name to the field
        return uniqueName;
    }

    /**
     * Method to edit current IMS_QP_DEPSubStage
     *
     * @param context
     * @param args
     * @throws Exception
     */
    public void editPostProcess(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);
        LOG.info("args: " + Arrays.deepToString(new Map[]{argsMap}));

        Map paramMap = (Map) argsMap.get("paramMap");

        String subStageID = (String) paramMap.get("objectId");

        String baselineName = UIUtil.isNotNullAndNotEmpty((String) paramMap.get("baseline")) ? (String) paramMap.get("baseline") : "";
        String baselineID = "";

        if (!baselineName.equals("") && baselineName.contains("_")) {
            baselineName = baselineName.substring(0, baselineName.indexOf("_"));
            LOG.info("baselineName: " + baselineName);
            baselineID = baselineName;
        } else if (!baselineName.equals("")) {
            String where = "name smatch '" + baselineName + "'";
            MapList currentBaseline = DomainObject.findObjects(context, "IMS_Baseline", "eService Production", where, new StringList("id"));
            Map id = (Map) currentBaseline.get(0);
            baselineID = (String) id.get("id");
            LOG.info("baselineID: " + currentBaseline + " subStageID: " + subStageID);
        }

        DomainObject substage = new DomainObject(subStageID);
        String substageBaseline = UIUtil.isNotNullAndNotEmpty(substage.getInfo(context, IMS_QP_Constants_mxJPO.TO_IMS_QP_BASE_LINE_2_DEPSUB_STAGE_FROM_ID)) ?
                substage.getInfo(context, IMS_QP_Constants_mxJPO.TO_IMS_QP_BASE_LINE_2_DEPSUB_STAGE_FROM_ID) : "";
        LOG.info("substageBaseline: " + substageBaseline);

        //check if baselines is equals -> return
        if (substageBaseline.equals(baselineID)) {
            LOG.info("baselines equals");
            return;
        }
        //check if baselines not equals ->
        else {
            LOG.info("substage baseline - " + substageBaseline + " not equals baseline id - " + baselineID);
            String relId = substage.getInfo(context, "to[IMS_QP_BaseLine2DEPSubStage].id");

            try {
                ContextUtil.startTransaction(context, true);

                //the stage is not connected earlier
                if (substageBaseline.equals("")) {
                    DomainRelationship.connect(context, new DomainObject(baselineID), "IMS_QP_BaseLine2DEPSubStage", substage);
                    //the field of baseline has status <empty>
                } else if (baselineID.equals(""))
                    DomainRelationship.disconnect(context, relId);
                    //baseline of stage and field of baseline is not equals
                else {
                    LOG.info("relId: " + relId);
                    DomainRelationship.disconnect(context, relId);
                    DomainRelationship.connect(context, new DomainObject(baselineID), "IMS_QP_BaseLine2DEPSubStage", substage);
                }

                ContextUtil.commitTransaction(context);
                LOG.info("transaction commited");

            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                LOG.error("transaction aborted: " + e.getMessage());
            }
        }
    }

    /**
     * Method to edit current IMS_QP_QPlan
     *
     * @param context
     * @param args
     * @throws Exception
     */
    public void editQPlanPostProcess(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);
        Map paramMap = (Map) argsMap.get("paramMap");

        String objectId = (String) paramMap.get("objectId");
        String description = (String) paramMap.get("description");

        DomainObject qPlan = new DomainObject(objectId);
        //change description
        String currentDescription = qPlan.getDescription(context);
        if (!currentDescription.equals(description)) {
            qPlan.setDescription(context, description);
            LOG.info("description changed: " + currentDescription + " -> " + description);
        }
    }

    /**
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public Object getObjectNames(Context context, String[] args) throws FrameworkException {

        String typePattern = "IMS_ProjectStage";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);

        MapList allStages = DomainObject.findObjects(context, typePattern, "eService Production", null, objectSelects);

        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();

        for (Object rawStage : allStages) {
            Map<String, String> map = (Map<String, String>) rawStage;
            fieldRangeValues.add(map.get("id"));
            fieldDisplayRangeValues.add(map.get("name"));
        }

        HashMap tempMap = new HashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    /**
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public Object getQPDEPNames(Context context, String[] args) throws FrameworkException {

        LOG.info("getQPDEPNames");

        String typePattern = "IMS_QP_DEP";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("from[IMS_QP_DEP2QPlan].to.id");
        objectSelects.add("attribute[IMS_QP_InterdisciplinaryDEP]");
        objectSelects.add("to[IMS_PBS2DEP].from.id");

        MapList allDEPs = DomainObject.findObjects(context, typePattern, "eService Production", null, objectSelects);

        //initialize with empty string first value
        StringList fieldRangeValues = new StringList("");
        StringList fieldDisplayRangeValues = new StringList("");

        for (Object rawStage : allDEPs) {
            Map<String, String> map = (Map<String, String>) rawStage;

            String pbsArray = UIUtil.isNotNullAndNotEmpty(map.get("to[IMS_PBS2DEP].from.id")) ? map.get("to[IMS_PBS2DEP].from.id") : "";
            String rangeValue = map.get("id") + "_" + pbsArray + "_" + map.get("attribute[IMS_QP_InterdisciplinaryDEP]");
            rangeValue = rangeValue.replaceAll(IMS_QP_Constants_mxJPO.BELL_DELIMITER, ",");
            fieldRangeValues.add(rangeValue);
            fieldDisplayRangeValues.add(map.get("name"));
        }

        LOG.info("fieldRangeValues: " + fieldRangeValues);


        HashMap tempMap = new HashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    /**
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public Object getSystemNames(Context context, String[] args) throws FrameworkException {

        String typePattern = "IMS_PBSSystem,IMS_GBSBuilding,IMS_PBSFunctionalArea";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("from[IMS_QP_DEP2QPlan].to.id");

        MapList allSystems = DomainObject.findObjects(context, /*types*/typePattern, /*vault*/ "eService Production", /*where*/"", /*selects*/objectSelects);

        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();

        for (Object rawStage : allSystems) {
            Map<String, String> map = (Map<String, String>) rawStage;
            fieldRangeValues.add(map.get("id"));
            fieldDisplayRangeValues.add(map.get("name"));
        }


        HashMap tempMap = new HashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    public StringList getSearchSystemsIds(Context context, String[] args) throws Exception {

        if (args.length == 0) throw new IllegalArgumentException();

        Map paramMap = JPO.unpackArgs(args);
        StringList returnIDs = new StringList();

        try {
            SelectList selectList = new SelectList();
            selectList.add(SELECT_ID);

            String typePattern = IMS_QP_Constants_mxJPO.SYSTEM_TYPES;

            MapList allSystems = DomainObject.findObjects(context, typePattern, "eService Production", null, selectList);
            LOG.info("allSystems: " + allSystems);
            for (Object o : allSystems) {
                Map tempMap = (Map) o;
                String id = (String) tempMap.get(SELECT_ID);
                returnIDs.add(id);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return returnIDs;
    }

    /**
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public Object getProjectStageNames(Context context, String[] args) throws FrameworkException {

        String typePattern = "IMS_ProjectStage";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);

        MapList allStages = DomainObject.findObjects(context, typePattern, "eService Production", null, objectSelects);

        Collections.sort(allStages, new Comparator<Map>() {
            @Override
            public int compare(Map o1, Map o2) {
                String str1 = (String) o1.get("name");
                String str2 = (String) o2.get("name");
                LOG.info("str1: " + str1 + " comparing str2:" + str2);
                return str1.compareTo(str2);
            }
        });

        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();

        for (Object rawStage : allStages) {
            Map<String, String> map = (Map<String, String>) rawStage;
            fieldRangeValues.add(map.get("id"));
            fieldDisplayRangeValues.add(map.get("name"));
        }

        Map tempMap = new HashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    public Object getStages(Context context, String[] args) throws FrameworkException {

        StringList fieldRangeValues = new StringList("");
        StringList fieldDisplayRangeValues = new StringList("<empty value>");

        fieldRangeValues.add("1");
        fieldRangeValues.add("2");
        fieldDisplayRangeValues.add("1");
        fieldDisplayRangeValues.add("2");


        Map tempMap = new LinkedHashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    /**
     * @param context
     * @param args
     * @return
     * @throws FrameworkException
     */
    public Object getBaseLineNames(Context context, String[] args) throws FrameworkException {

        String typePattern = "IMS_Baseline";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("to[IMS_ProjectStage2CB].from.id");

        MapList allBaselineNames = DomainObject.findObjects(context, typePattern, "eService Production", null, objectSelects);
        return getBaselines(allBaselineNames);
    }

    /**
     * Method to showing in combo the projectstage baselines only
     *
     * @param context
     * @param args
     * @return
     * @throws Exception
     */
    public Object getBaseLineNamesByStage(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        Map paramMap = (Map) argsMap.get("paramMap");
        String objectId = (String) paramMap.get("objectId");

        String typePattern = "IMS_Baseline";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("to[IMS_ProjectStage2CB].from.id");

        String where = IMS_QP_Constants_mxJPO.BASELINES_BY_STAGE + ".id==" + objectId;

        MapList allBaselineNames = DomainObject.findObjects(context, typePattern, "eService Production", where, objectSelects);
        return getBaselines(allBaselineNames);
    }

    private Object getBaselines(MapList allBaselineNames) {

        StringList fieldRangeValues = new StringList("");
        StringList fieldDisplayRangeValues = new StringList("<empty value>");

        for (Object rawStage : allBaselineNames) {
            Map<String, String> map = (Map<String, String>) rawStage;
            fieldRangeValues.add(map.get("id") + "_" + map.get("to[IMS_ProjectStage2CB].from.id"));
            fieldDisplayRangeValues.add(map.get("name"));
        }

        HashMap tempMap = new HashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    public Object updateBaselineCombo(Context context, String[] args) throws Exception {
        Map map = new HashMap();
        return map;
    }

    /**
     * Method to deleting IMS_QP_QPTask if their hasn't any approved related tasks
     *
     * @param context
     * @param args
     */
    public Map deleteQPTasks(Context context, String[] args) {

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

        //selects all tasks  with their subtasks
        StringList selects = new StringList("id");
        selects.add("name");

        MapList objectsInfo = null;
        try {
            objectsInfo = DomainObject.getInfo(context, taskIDs, selects);
        } catch (FrameworkException e) {
            LOG.error("error getting info: " + e.getMessage());
            e.printStackTrace();
        }

        //check if substage has some subtask
        List<String> deletingIDs = new ArrayList();

        StringBuffer buffer = new StringBuffer("\nCouldn't to delete next stages: \n");
        List<String> badNames = new ArrayList<>();
        boolean flag = false;

        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            if (!IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(context, (String) map.get("id"))) {
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
            LOG.info("objects deleted: " + Arrays.deepToString(var1));
        } catch (Exception e) {
            LOG.error("delete error: " + e.getMessage());
            e.printStackTrace();
        }

        LOG.info("return map: " + mapMessage);
        return mapMessage;
    }
}
