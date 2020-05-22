import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.Relationship;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QualityPlanBase_mxJPO extends DomainObject {

    private static Logger LOG = Logger.getLogger("blackLogger");

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
        selects.add("from[IMS_QP_DEPSubStage2DEPTask]");
        selects.add("name");

        MapList objectsInfo = null;
        try {
            objectsInfo = DomainObject.getInfo(context, substagesIDs, selects);
        } catch (FrameworkException e) {
            LOG.info("error getting info: " + e.getMessage());
            e.printStackTrace();
        }

        //check if substage has some subtask
        List<String> deletingIDs = new ArrayList();

        StringBuffer buffer = new StringBuffer("\nCouldn't to delete next stages: \n");
        List<String> badNames = new ArrayList<>();
        boolean flag = false;
        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            if ("TRUE".equals(map.get("from[IMS_QP_DEPSubStage2DEPTask]"))) {
                buffer.append(map.get("name") + "\n");
                badNames.add((String) map.get("name"));
                flag = true;
            } else {
                deletingIDs.add((String) map.get("id"));
            }
        }

//        String bufferMessage = "";
        if (flag) {
            LOG.info("buffer: " + buffer.toString());
//            bufferMessage = buffer.toString();
        }
        Map mapMessage = new HashMap();
        if (flag) {
//            mapMessage.put("message", bufferMessage);
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
//            emxContextUtil_mxJPO.mqlWarning(context, buffer.toString());
        } catch (Exception e) {
            LOG.info("delete error: " + e.getMessage());
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
                /*type*/"IMS_QP_DEPSubStage",
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
        LOG.info("args: " + Arrays.deepToString(new Map[]{argsMap}));

        Map requestMap = (Map) argsMap.get("requestMap");
        Map paramMap = (Map) argsMap.get("paramMap");

        //all required params
        String projectStageID = (String) requestMap.get("project_stage");
        String parentOID = (String) requestMap.get("parentOID");
        String objectId = (String) requestMap.get("objectId");
        //sub_stage params
        String relId = (String) paramMap.get("relId");
        String newObjectId = (String) paramMap.get("newObjectId");
        String baselineID = (String) requestMap.get("baseline");
        baselineID = baselineID.substring(0, baselineID.indexOf("_")) != null ? baselineID.substring(0, baselineID.indexOf("_")) : "";

        //log all required initial params
        LOG.info("projectStageID: " + projectStageID);
        LOG.info("parentID: " + objectId);
        LOG.info("relID: " + relId);
        LOG.info("parentOID: " + parentOID);
        LOG.info("baselineID: " + baselineID);

        //parent
        DomainObject parent = new DomainObject(parentOID);
        DomainObject substage = new DomainObject(newObjectId);
        LOG.info("substage: " + substage + " id: " + newObjectId);

        StringList selects = new StringList();
        selects.add("id");

        String where = "from[IMS_QP_ProjectStage2DEPProjectStage].to.to[IMS_QP_DEP2DEPProjectStage].from.id==" + parentOID;
        LOG.info("where: " + where);

        MapList result = parent.findObjects(context,
                /*type*/ "IMS_ProjectStage",
                /*?*/ "*",
                /*where*/ where,
                selects
        );

        String allProjectStagesConnectingToDEP = "";
        for (Object temp : result) {
            Map map = (Map) temp;
            allProjectStagesConnectingToDEP += map.get("id") + "|";
        }


        LOG.info("allProjectStagesConnectingToDEP: " + allProjectStagesConnectingToDEP);
        LOG.info(parent.getInfo(context, "name") + " prepare parent stage: " + parent.getInfo(context, "from[IMS_QP_DEP2DEPProjectStage].to.to[IMS_QP_ProjectStage2DEPProjectStage].from.id"));

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
                uniqueNameSubStage = setUniqueFieldName(context, parent, newDEPProjectStage);
                LOG.info("if uniqueNameSubStage: " + uniqueNameSubStage);

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
                LOG.info("where: " + where);

                result = parent.findObjects(context,
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
                uniqueNameSubStage = setUniqueFieldName(context, parent, depProjectStage);
                LOG.info("else uniqueNameSubStage: " + uniqueNameSubStage);

                //connect new IMS_QP_SubStage <- IMS_QP_DEPProjectStage <- projectStageID.
                LOG.info("depProjectStage: " + depProjectStage);
                LOG.info("substage: " + substage);
                DomainRelationship.connect(context, depProjectStage, "IMS_QP_DEPProjectStage2DEPSubStage", substage);
                LOG.info(depProjectStageID + "->IMS_QP_DEPProjectStage2DEPSubStage->" + objectId);
            }

            //connect new IMS_QP_SubStage <- IMS_QP_Baseline2DEPSubStage <- baselineID
            LOG.info(baselineID + "<- IMS_QP_Baseline2DEPSubStage <- " + newObjectId);
            if (!baselineID.equals("")) {
                DomainRelationship.connect(context, new DomainObject(baselineID), "IMS_QP_Baseline2DEPSubStage", substage);
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
        LOG.info(argsMap);

        Map requestMap = (Map) argsMap.get("requestMap");
        Map paramMap = (Map) argsMap.get("paramMap");

        String parentOID = (String) requestMap.get("parentOID");
        String systemID = (String) requestMap.get("systemOID");
        String depID = (String) requestMap.get("dep");
        String newObjectId = (String) paramMap.get("newObjectId");
        String relId = (String) paramMap.get("relId");


        //log all needed params
        LOG.info("parent: " + parentOID + "->relId: " + relId + "->newObjectId: " + newObjectId + " from DEP: " + depID + " to system: " + systemID);

        //create domainobjects
        DomainObject system = new DomainObject(systemID);
//        DomainObject dep = new DomainObject(depID);
        DomainObject newObject = new DomainObject(newObjectId);

        try {
            //start transactional
            ContextUtil.startTransaction(context, true);
            LOG.info("before set name: " + newObject.getName());

            String systemName = system.getInfo(context, DomainObject.SELECT_NAME);
            LOG.info("systemName: " + systemName);

            //change name for new object mask: QP-PBS /QP-10FAL/
            newObject.setName(context, "QP-" + systemName);
            LOG.info("new name: " + newObject.getName());

            //connect relations
            DomainRelationship.connect(context, new DomainObject(depID), "IMS_QP_DEP2QPlan", newObject);
            LOG.info("connect " + depID + " -> IMS_QP_DEP2QPlan ->" + newObject.getName());

            DomainRelationship.connect(context, newObject, "IMS_QP_QPlan2Object", new DomainObject(system));
            LOG.info("connect new object " + newObjectId + " -> IMS_QP_QPlan2Object ->" + system.getName());

            //commit transaction
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOG.error("transaction aborted: " + e.getMessage());
        }
    }

    public String setUniqueFieldName(Context context, DomainObject parent, DomainObject depProjectStage) throws Exception {
        //set unique name for the new substage
//        String indexParent = parent.getName().substring(4, 5);
        String indexParent = parent.getInfo(context, "to[IMS_QP_Discipline2DEP].from.attribute[IMS_ShortName]") != null ?
                parent.getInfo(context, "to[IMS_QP_Discipline2DEP].from.attribute[IMS_ShortName]") : "_";
        String depProjectStageName = depProjectStage.getInfo(context, "name");

        //count all substages at this DEP ProjectStage

//        String allSubStages = parent.getInfo(context, "from[IMS_QP_DEP2DEPProjectStage].to.id");
        StringList selects = new StringList();
        selects.add("id");

        String where = "to[IMS_QP_DEPProjectStage2DEPSubStage].from.id==" + depProjectStage.getInfo(context, "id");
        LOG.info("where: " + where);

        MapList result = parent.findObjects(context,
                /*type*/ "IMS_QP_DEPSubStage",
                /*?*/ "*",
                /*where*/ where,
                selects
        );

        int count = 1 + result.size();
        String counter = count < 10 ? "0" + count : "" + count;
        LOG.info("set unique name counter: " + counter);

        //concate unique name
        String uniqueName = indexParent + depProjectStageName + "-" + counter;

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

        String baselineID = (String) paramMap.get("Baseline");
        String subStageID = (String) paramMap.get("objectId");
        LOG.info("baselineID: " + baselineID + " subStageID: " + subStageID);

        DomainObject substage = new DomainObject(subStageID);
        String substageBaseline = UIUtil.isNotNullAndNotEmpty(substage.getInfo(context, "to[IMS_QP_BaseLine2DEPSubStage].from.id")) ?
                substage.getInfo(context, "to[IMS_QP_BaseLine2DEPSubStage].from.id") : "";
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
     * Method to edit current IMS_QP_DEPSubStage
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
        String depID = (String) paramMap.get("dep");
        String systemID = UIUtil.isNotNullAndNotEmpty((String) paramMap.get("systemOID")) ? (String) paramMap.get("systemOID") : "empty";

        LOG.info("objectId: " + objectId + " depID: " + depID + " systemID: " + systemID);

        DomainObject qPlan = new DomainObject(objectId);

        //change description
        String currentDescription = qPlan.getDescription(context);
        if (!currentDescription.equals(description)) {
            if (!currentDescription.equals(description)) qPlan.setDescription(context, description);
            LOG.info("description: " + qPlan.getDescription(context));
        }
        LOG.info("description changed: " + currentDescription + " -> " + description);

        //disconnect current system
        String currentDEPID = UIUtil.isNotNullAndNotEmpty(qPlan.getInfo(context, "to[IMS_QP_DEP2QPlan].from.id"))
                ? qPlan.getInfo(context, "to[IMS_QP_DEP2QPlan].from.id") : "empty";
        String currentSystemID = UIUtil.isNotNullAndNotEmpty(qPlan.getInfo(context, "from[IMS_QP_QPlan2Object].to.id"))
                ? qPlan.getInfo(context, "from[IMS_QP_QPlan2Object].to.id") : "empty";
        LOG.info("currentRelationDEPId: " + currentDEPID);
        LOG.info("currentRelationSystemID: " + currentSystemID);

        //connect new system
        try {
            ContextUtil.startTransaction(context, true);

            if (!currentSystemID.equals(systemID)) {
                if (!currentSystemID.equals("empty")) {
                    LOG.info("currentSystem: " + qPlan.getInfo(context, "name") + ": " + qPlan.getInfo(context, "from[IMS_QP_QPlan2Object].id"));
                    DomainRelationship.disconnect(context, qPlan.getInfo(context, "from[IMS_QP_QPlan2Object].id"));
                    LOG.info("currentSystem: " + currentSystemID + " disconnected");
                }
                if (!systemID.equals("empty")) {
                    LOG.info("systemID: " + systemID);
                    DomainRelationship.connect(context, qPlan, "IMS_QP_QPlan2Object", new DomainObject(systemID));
                    LOG.info("from " + objectId + " ->IMS_QP_QPlan2Object-> " + systemID);
                }
            }

            if (!currentDEPID.equals(depID)) {
                if (!currentDEPID.equals("empty")) {
                    LOG.info("currentDEP: " + qPlan.getId(context) + ": " + qPlan.getInfo(context, "to[IMS_QP_DEP2QPlan].from.name"));
                    DomainRelationship.disconnect(context, qPlan.getInfo(context, "to[IMS_QP_DEP2QPlan].id"));
                    LOG.info("currentDEPID: " + currentDEPID + " disconnected");
                }
                if (!depID.equals("")) {
                    DomainRelationship.connect(context, new DomainObject(depID), "IMS_QP_DEP2QPlan", qPlan);
                    LOG.info("from " + depID + " ->IMS_QP_DEP2QPlan-> " + objectId);
                }
            }

            ContextUtil.commitTransaction(context);
            LOG.info("transaction commited");

        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOG.error("transaction aborted: " + e.getMessage());
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

        String typePattern = "IMS_QP_DEP";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("from[IMS_QP_DEP2QPlan].to.id");

        MapList allDEPs = DomainObject.findObjects(context, typePattern, "eService Production", null, objectSelects);

        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();

        for (Object rawStage : allDEPs) {
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

            String typePattern = "IMS_PBSSystem,IMS_GBSBuilding,IMS_PBSFunctionalArea";

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
    public Object getBaseLineNames(Context context, String[] args) throws FrameworkException {

        String typePattern = "IMS_BaseLine";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("to[IMS_ProjectStage2CB].from.id");

        MapList allBaselineNames = DomainObject.findObjects(context, typePattern, "eService Production", null, objectSelects);

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

        String typePattern = "IMS_BaseLine";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("to[IMS_ProjectStage2CB].from.id");

        String where = "to[IMS_ProjectStage2CB].from.from[IMS_QP_ProjectStage2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.id==" + objectId;

        MapList allBaselineNames = DomainObject.findObjects(context, typePattern, "eService Production", where, objectSelects);

        LOG.info("allBaselineNames: " + allBaselineNames);

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
}
