import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QualityPlanBase_mxJPO extends DomainObject {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    /**
     * Method to deleting IMS_QP_DEPSubStages if their hasn't any SubTasks
     *
     * @param context usual parameter
     * @param args    usual parameter
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

        String[] rowIDs = (String[]) Objects.requireNonNull(argsMap).get("emxTableRowId");
        String[] substagesIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            substagesIDs[i] = rowIDs[i].substring(0, rowIDs[i].indexOf("|"));
        }

        //selects all subtasks  with their subtasks
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPSUB_STAGE_2_DEPTASK);
        selects.add(DomainConstants.SELECT_NAME);

        MapList objectsInfo = new MapList();
        try {
            objectsInfo = DomainObject.getInfo(context, substagesIDs, selects);
        } catch (FrameworkException e) {
            LOG.error("error getting info: " + e.getMessage());
            e.printStackTrace();
        }

        //check if substage has some subtask
        List<String> deletingIDs = new ArrayList<>();

        List<String> badNames = new ArrayList<>();
        boolean flag = false;
        for (Object o : Objects.requireNonNull(objectsInfo)) {
            Map map = (Map) o;
            if ("TRUE".equals(map.get(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPSUB_STAGE_2_DEPTASK))) {
                badNames.add((String) map.get(DomainConstants.SELECT_NAME));
                flag = true;
            } else {
                deletingIDs.add((String) map.get(DomainConstants.SELECT_ID));
            }
        }

        Map mapMessage = getMessage(badNames, flag);
        deleteObjects(context, deletingIDs);

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
        selects.add(DomainConstants.SELECT_ID);

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        return parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/IMS_QP_Constants_mxJPO.IMS_QP_DEP_SUB_STAGE,
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 2,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);
    }

    /**
     * Method to show the table of IMS_PBSSystem elements
     */
    public MapList getAllPBSSystemsForTable(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        return parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_PBSSystem",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 2,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);
    }

    public void copyPlan(Context ctx, String... args) throws Exception {
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
        DomainObject fromObject = null;
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
        String systems = (String) requestMap.get("systemOID"), systemName = "";
        StringList systemArray = FrameworkUtil.split(systems, "|");
        List<String> errorsBuffer = new ArrayList();

        //lookup systems
        for (String s : systemArray) {
            DomainObject systemObject = new DomainObject(s);
            systemName = systemObject.getInfo(ctx, DomainObject.SELECT_NAME);

            //check the system owner
            String systemOwners = null;
            try {
                systemOwners = MqlUtil.mqlCommand(ctx, "print bus " + s + " select from[IMS_PBS2Owner].to.name dump |");
            } catch (FrameworkException e) {
                LOG.error("error executing mql commend: " + e.getMessage());
                e.printStackTrace();
            }

            systemOwners = UIUtil.isNotNullAndNotEmpty(systemOwners) ? systemOwners : "";
            if (!parentName.contains("AQP")) {
                if (!systemOwners.contains(ctx.getUser()) && !"admin_platform".equals(ctx.getUser())) {
                    errorsBuffer.add("" +
                            "user " + ctx.getUser() + " is not from the system owners " + systemOwners +
                            "");
                    continue;
                }
            }

            Map<String, Object> objectMap = new HashMap<>();
            objectMap.put("systemName", systemName);
            objectMap.put("description", description);
            objectMap.put("qpParentObject", qpParentObject);
            objectMap.put("system", systemObject);
            objectMap.put("fromObject", fromObject);
            objectMap.put("parentName", parentName);
            objectMap.put("useConnection", useConnection);
            continueCopyingProcedure(ctx, objectMap);
        }

        if (errorsBuffer.size() > 0) {
            StringBuilder builder = new StringBuilder("errors: ");
            for (int i = 0; i < errorsBuffer.size(); i++) {
                builder.append("user " + ctx.getUser() + " is not owner of the system names" + systemName);
            }

            emxContextUtil_mxJPO.mqlWarning(ctx, builder.toString());
        }
    }

    private void continueCopyingProcedure(Context ctx, Map<String, Object> objectMap) {
        //start transactional
        try {
            ContextUtil.startTransaction(ctx, true);
        } catch (FrameworkException e) {
            LOG.error("can not starting transaction: " + e.getMessage());
            e.printStackTrace();
            ContextUtil.abortTransaction(ctx);
        }

        //create object
        DomainObject planObject = new DomainObject();
        String newObjectName = "QP-" + objectMap.get("systemName");
        try {
            planObject.createObject(ctx,
                    IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan,
                    newObjectName,
                    "",
                    IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan,
                    ctx.getVault().getName());
            if (UIUtil.isNotNullAndNotEmpty((String) objectMap.get("description"))) {
                planObject.setDescription(ctx, (String) objectMap.get("description"));
            }
        } catch (FrameworkException e) {
            LOG.error("error creating a copy plan object: " + e.getMessage());
            e.printStackTrace();
            ContextUtil.abortTransaction(ctx);
        }

        try {
            DomainRelationship.connect(ctx, (DomainObject) objectMap.get("qpParentObject"), IMS_QP_Constants_mxJPO.relationship_IMS_QP_QP2QPlan, planObject);
            LOG.info("connected from " + ((DomainObject) objectMap.get("qpParentObject")).getName(ctx)
                    + " related " + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QP2QPlan
                    + " to " + planObject.getName(ctx));

            //Connect with system and dep
            DomainRelationship.connect(ctx, planObject, IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object, (DomainObject) objectMap.get("system"));
            LOG.info("connected from " + planObject.getName(ctx)
                    + " related " + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object
                    + " to " + ((DomainObject) objectMap.get("system")).getName(ctx));

            String depName = IMS_QP_TaskAssignment_mxJPO.connectToDep(ctx, (DomainObject) objectMap.get("fromObject"), planObject);
            if (((String) objectMap.get("parentName")).contains("AQP")) {
                planObject.setName(ctx, newObjectName + "-" + depName);
            }

        } catch (Exception e) {
            LOG.error("error connecting objects: " + e.getMessage());
            e.printStackTrace();
        }

        //Copy tasks
        try {
            IMS_QP_TaskAssignment_mxJPO.copyQPTasks(ctx,
                    (DomainObject) objectMap.get("fromObject"),
                    planObject, (Boolean) objectMap.get("useConnection"));
            LOG.info("plan copied to " + planObject.getName(ctx) + "|" + planObject.getId(ctx));
        } catch (Exception e) {
            LOG.error("error coping tasks connections: " + e.getMessage());
            e.printStackTrace();
            ContextUtil.abortTransaction(ctx);
        }

        try {
            ContextUtil.commitTransaction(ctx);
        } catch (FrameworkException e) {
            LOG.error("can not finishing transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Method to show the table of IMS_PBSSystem, IMS_GBSBuildings, IMS_PBSFunctionalAreas elements
     */

    public StringList includeIDs(Context ctx, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);
        String objectId = "";
        if (argsMap.containsKey("objectId")) {
            objectId = (String) argsMap.get("objectId");
        }

        //getting Main plan & dep info from
        DomainObject fromObject, fromDep;
        String fromMainSystemToDepID;
        StringList systemIDs = new StringList();
        try {
            fromObject = new DomainObject(objectId);
            fromMainSystemToDepID = fromObject.getInfo(ctx, "to[IMS_QP_DEP2QPlan].from.id");
            fromDep = new DomainObject(fromMainSystemToDepID);
            systemIDs = fromDep.getInfoList(ctx, "to[IMS_PBS2DEP].from.id");
        } catch (Exception e) {
            LOG.error("error get object from main plan dep: " + e.getMessage());
            e.printStackTrace();
        }

        return systemIDs;
    }

    /**
     * Method to show the table of IMS_GBSBuilding elements
     */
    public MapList getAllGBSBuildingsForTable(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        return parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_GBSBuilding",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 2,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);
    }

    /**
     * Method to show the table of IMS_PBSFunctionalArea elements
     */
    public MapList getAllFunctionalAreasForTable(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        return parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_PBSFunctionalArea",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 2,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);
    }

    /**
     * Method to create new IMS_QP_DEPSubStage
     *
     * @param context usualy parameter
     * @param args    usualy parameter
     * @throws Exception if has some errors throw common mistake
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
        String newObjectId = (String) paramMap.get("newObjectId");

        String baselineID = (String) requestMap.get("baseline");
        if (UIUtil.isNotNullAndNotEmpty(baselineID)) {
            baselineID = !baselineID.substring(0, baselineID.indexOf("_")).isEmpty() ? baselineID.substring(0, baselineID.indexOf("_")) : "";
        } else baselineID = "";

        String stage = (String) requestMap.get("stage");

        //parent
        DomainObject parent = new DomainObject(parentOID);
        DomainObject substage = new DomainObject(newObjectId);

        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);

        String where = IMS_QP_Constants_mxJPO.FROM_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_TO_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID + parentOID;

        MapList result = findObjects(context,
                /*type*/ "IMS_ProjectStage",
                /*?*/ "*",
                /*where*/ where,
                selects
        );

        String allProjectStagesConnectingToDEP = "";
        for (Object temp : result) {
            Map map = (Map) temp;
            allProjectStagesConnectingToDEP += map.get(DomainConstants.SELECT_ID) + "|";
        }

        DomainObject newDEPProjectStage;
        try {
            ContextUtil.startTransaction(context, true);
            DomainObject projectStage = new DomainObject(projectStageID);

            String uniqueNameSubStage;
            if (!allProjectStagesConnectingToDEP.contains(projectStageID)) {

                //get name project_stage
                String nameProjectStage = projectStage.getInfo(context, DomainConstants.SELECT_NAME);

                //create object T:IMS_QP_DEPProjectStage
                newDEPProjectStage = DomainObject.newInstance(context);
                //String revision = "DEP" + "-" + "nameProjectStage";
                newDEPProjectStage.createObject(context,
                        /*type*/"IMS_QP_DEPProjectStage",
                        /*name*/nameProjectStage,
                        /*revision*/parent.getName() + "-" + nameProjectStage,
                        /*policy*/"IMS_QP_DEPProjectStage",
                        /*vault*/ context.getVault().getName());

                //set unique name
                uniqueNameSubStage = setUniqueFieldName(context, parent, stage, newDEPProjectStage);

                //connect parent -> object
                DomainRelationship.connect(context, parent, "IMS_QP_DEP2DEPProjectStage", newDEPProjectStage);

                //connect object -> new IMS_QP_DEPSubStage [objectID]
                DomainRelationship.connect(context, newDEPProjectStage, "IMS_QP_DEPProjectStage2DEPSubStage", substage);

                //connect object <- IMS_ProjectStage [projectStageID]
                DomainRelationship.connect(context, projectStage, "IMS_QP_ProjectStage2DEPProjectStage", newDEPProjectStage);

            } else {

                selects = new StringList();
                selects.add(DomainConstants.SELECT_ID);

                where = "name smatch '*" + projectStage.getInfo(context, DomainConstants.SELECT_NAME) + "*' && to[IMS_QP_DEP2DEPProjectStage].from.id==" + parentOID;

                result = findObjects(context,
                        /*type*/ "IMS_QP_DEPProjectStage",
                        /*?*/ "*",
                        /*where*/ where,
                        selects
                );

                Map map = (Map) result.get(0);
                String depProjectStageID = (String) map.get(DomainConstants.SELECT_ID);

                //set unique name
                DomainObject depProjectStage = new DomainObject(depProjectStageID);
                uniqueNameSubStage = setUniqueFieldName(context, parent, stage, depProjectStage);

                //connect new IMS_QP_SubStage <- IMS_QP_DEPProjectStage <- projectStageID.
                DomainRelationship.connect(context, depProjectStage, "IMS_QP_DEPProjectStage2DEPSubStage", substage);
            }

            //connect new IMS_QP_SubStage <- IMS_QP_Baseline2DEPSubStage <- baselineID
            if (!baselineID.equals("")) {
                DomainRelationship.connect(context, new DomainObject(baselineID), "IMS_QP_BaseLine2DEPSubStage", substage);
            }

            substage.setName(context, uniqueNameSubStage);
            substage.setAttributeValue(context, "IMS_QP_Stage", stage);

            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOG.error("transaction aborted: " + e.getMessage());
        }
    }

    public void createQPlanPostProcess(Context ctx, String... args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        Map requestMap = (Map) argsMap.get("requestMap");
        Map paramMap = (Map) argsMap.get("paramMap");

        String form = UIUtil.isNotNullAndNotEmpty((String) requestMap.get("form")) ? (String) requestMap.get("form") : "";

        //system field
        String systemID = UIUtil.isNotNullAndNotEmpty((String) requestMap.get("systemOID")) ? (String) requestMap.get("systemOID") : "";
        String systemName = (String) requestMap.get("systemDisplay");

        //dep field
        String depFieldFromForm = (String) requestMap.get("dep");
        String cleanDepID = UIUtil.isNotNullAndNotEmpty(depFieldFromForm) ? depFieldFromForm.substring(0, depFieldFromForm.indexOf("_")) : "";
        boolean isInterdisciplinaryDep = depFieldFromForm.endsWith("_TRUE");

        //group field
        String groupOID = (String) requestMap.get("groupOID");

        //set objects
        String newObjectId = (String) paramMap.get("newObjectId");
        DomainObject depObject = new DomainObject(cleanDepID);

        //1. check if the DEP is 'Done' status
        String depState = depObject.getInfo(ctx, "current");
        if (!depState.equals("Done")) {
            throw new MatrixException(String.format("%s is not finished yet and has status '%s'", depObject.getName(ctx), depState));
        }

        //2. check if the system isn't selected and the dep isn't 'Interdisciplinary'
        DomainObject system = null;
        String systemType = "";
        if (StringUtils.isEmpty(systemID) && !isInterdisciplinaryDep) {
            throw new Exception("You have to select a System!");
        } else if (!StringUtils.isEmpty(systemID)) {
            system = new DomainObject(systemID);
            systemType = system.getType(ctx);

            //3, check owners of system
            String printOwners = MqlUtil.mqlCommand(ctx, String.format("print bus %s select %s", systemID, "from[IMS_PBS2Owner].to.name"));
            String userName = ctx.getUser();

            if (!IMS_QP_Security_mxJPO.isUserAdmin(ctx)) {
                if (!form.contains("AQP") && (printOwners == null || !printOwners.contains(userName))) {
                    LOG.error("throw error: " + String.format("%s does not QPlan owner of %s %s ", userName, systemType, systemName));
                    throw new MatrixException(String.format("%s does not QPlan owner of %s %s ", userName, systemType, systemName));
                }
            }

            //4. check the dep of system
            String systemDEPs = MqlUtil.mqlCommand(ctx, String.format("print bus %s select %s dump |", systemID, "from[IMS_PBS2DEP].to.id"));
            if (systemDEPs == null || !systemDEPs.contains(cleanDepID)) {
                throw new MatrixException(String.format("%s does not include the specified dep", systemName));
            }
        }

        //5. check this MAIN RULE only if that's system is not null
        if (!StringUtils.isEmpty(systemID) && system != null) {

            //5.1 check type of the system
            boolean isFunctionalArea = systemType.equals("IMS_PBSFunctionalArea");
            String forkConnection = isFunctionalArea ? "isFunctionalArea" : "to[IMS_PBSFunctionalArea2" + systemType + "].from";

            //5.2. getting info about other QPlans connections & throw errors if that is
            mainCheckRule(ctx, form, systemID, cleanDepID, systemType, isFunctionalArea, forkConnection);
        }

        //6. check the dep owners
        StringList depOwners = depObject.getInfoList(ctx, "from[IMS_QP_DEP2Owner].to.id");

        //start creating the QPlan object with relationships
        try {
            ContextUtil.startTransaction(ctx, true);

            //change name for new object mask: QP-PBS /QP-10FAL/ or QP-PBS-DEP /QP-10FAK-DEP-3D/
            String qpPlanName = UIUtil.isNotNullAndNotEmpty(systemName) ? systemName : "";
            if (form.contains("AQP")) {
                qpPlanName += "-" + depObject.getName(ctx);
            }

            if (form.contains("SQP") && isInterdisciplinaryDep) {
                qpPlanName = depObject.getName(ctx);
            }

            DomainObject newObject;
            if (!qpPlanName.equals("")) {
                newObject = new DomainObject(newObjectId);
                newObject.setName(ctx, "QP-" + qpPlanName);
            } else throw new MatrixException("unable to initialize correct name for the QPlan object");

            LOG.info("qpPlanName: " + qpPlanName);

            //connect relations

            //from dep
            DomainRelationship.connect(ctx, depObject, "IMS_QP_DEP2QPlan", newObject);

            //from classifier
            if (UIUtil.isNotNullAndNotEmpty(groupOID)) {
                DomainObject objectClassifier = new DomainObject(groupOID);
                String dep2ClassifierId = MqlUtil.mqlCommand(ctx,
                        String.format("print bus %s select from[IMS_QP_DEP2Classifier].to.id dump |", depObject.getId(ctx)));

                LOG.info("dep2ClassifierId: " + dep2ClassifierId + "|groupOID: " + groupOID);
                if (UIUtil.isNullOrEmpty(dep2ClassifierId)) {
                    throw new Exception("Selected DEP hasn't relation to any classifier");
                }
                if (!dep2ClassifierId.contains(groupOID)) {
                    throw new Exception("Selected DEP isn't related to the specified classifier");
                }
                DomainRelationship.connect(ctx, objectClassifier, "IMS_QP_Classifier2QPlan", newObject);
            }

            //to system
            if (system != null && system.exists(ctx)) {
                DomainRelationship.connect(ctx, newObject, "IMS_QP_QPlan2Object", system);
            }

            ContextUtil.commitTransaction(ctx);
            LOG.info("qplan created: " + qpPlanName + "|" + newObjectId);
        } catch (Exception e) {
            ContextUtil.abortTransaction(ctx);
            LOG.error("transaction aborted: " + e.getMessage());
            throw e;
        }
    }

    private void mainCheckRule(Context ctx, String form, String systemID, String cleanDepID, String systemType, boolean isFunctionalArea, String forkConnection) throws MatrixException {
        String relationshipFromQPlan, relationshipFromQPlanInterdisciplinaryAttributes, relationshipFromQPlanIds;
        if (!forkConnection.equals("isFunctionalArea")) {
            //is it has any plans
            relationshipFromQPlan = MqlUtil.mqlCommand(ctx, String.format("print bus %s select %s.to[IMS_QP_QPlan2Object] dump |", systemID, forkConnection));

            //ids all related DEPs
            relationshipFromQPlanIds = relationshipFromQPlan.contains("TRUE") ?
                    MqlUtil.mqlCommand(ctx, String.format("print bus %s select %s.to[IMS_QP_QPlan2Object].from.to[IMS_QP_DEP2QPlan].from.id dump |", systemID, forkConnection)) : "";

            //states all related DEPs
            relationshipFromQPlanInterdisciplinaryAttributes = relationshipFromQPlan.contains("TRUE") ?
                    MqlUtil.mqlCommand(ctx, String.format("print bus %s select %s.to[IMS_QP_QPlan2Object].from.to[IMS_QP_DEP2QPlan].from.attribute[IMS_QP_InterdisciplinaryDEP] dump |", systemID, forkConnection)) : "";

            LOG.info("has rels from plans: " + relationshipFromQPlan + " dep ids: " + relationshipFromQPlanIds + " deps states ID: " + relationshipFromQPlanInterdisciplinaryAttributes);

        } else {

            String systemQPlanConnected = MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_PBSFunctionalArea2IMS_PBSSystem].to.to[IMS_QP_QPlan2Object] dump |", systemID));
            String systemQPlanConnectedIds = MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_PBSFunctionalArea2IMS_PBSSystem].to.to[IMS_QP_QPlan2Object].from.to[IMS_QP_DEP2QPlan].from.id dump |", systemID));
            String systemQPlanConnectedInterdisciplinaryAttributes = MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_PBSFunctionalArea2IMS_PBSSystem].to.to[IMS_QP_QPlan2Object].from.to[IMS_QP_DEP2QPlan].from.attribute[IMS_QP_InterdisciplinaryDEP] dump |", systemID));

            String buildingQPlanConnected = MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_PBSFunctionalArea2IMS_GBSBuilding].to.to[IMS_QP_QPlan2Object] dump |", systemID));
            String buildingQPlanConnectedIds = MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_PBSFunctionalArea2IMS_GBSBuilding].to.to[IMS_QP_QPlan2Object].from.to[IMS_QP_DEP2QPlan].from.id dump |", systemID));
            String buildingQPlanConnectedInterdisciplinaryAttributes = MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_PBSFunctionalArea2IMS_GBSBuilding].to.to[IMS_QP_QPlan2Object].from.to[IMS_QP_DEP2QPlan].from.attribute[IMS_QP_InterdisciplinaryDEP] dump |", systemID));

            relationshipFromQPlan = systemQPlanConnected + buildingQPlanConnected;
            relationshipFromQPlanIds = systemQPlanConnectedIds + buildingQPlanConnectedIds;
            relationshipFromQPlanInterdisciplinaryAttributes = systemQPlanConnectedInterdisciplinaryAttributes + buildingQPlanConnectedInterdisciplinaryAttributes;

            LOG.info("has rels from plans: " + relationshipFromQPlan + " dep ids: " + relationshipFromQPlanIds + " deps states ID: " + relationshipFromQPlanInterdisciplinaryAttributes);
        }

        if (form.contains("AQP") && relationshipFromQPlan.contains("TRUE")) {
            //check if related plan constains DEP name from form
            LOG.info("rel from plan: " + UIUtil.isNotNullAndNotEmpty(relationshipFromQPlanIds) + " ids: " + relationshipFromQPlanIds + " dep id: " + cleanDepID + " | ");
            if (UIUtil.isNotNullAndNotEmpty(relationshipFromQPlanIds) && relationshipFromQPlanIds.contains(cleanDepID)) {
                LOG.error("drop AQP exception!");
                String message = !isFunctionalArea ? "this " + systemType.substring(systemType.lastIndexOf("_") + 1) + " has already used DEP for this Functional Area" :
                        "this functional area has systems or building having some QPlans used this DEP";
                throw new MatrixException(message);
            }

        } else if (relationshipFromQPlan.contains("TRUE")) {
            if (relationshipFromQPlanInterdisciplinaryAttributes.contains("FALSE")) {
                LOG.error("drop SQP exception!");
                String message = !isFunctionalArea ? "this " + systemType.substring(systemType.lastIndexOf("_") + 1) + " is part of Functional Area" :
                        "this functional area has systems or building having some QPlans";
                LOG.info("plan doesn't created!");
                throw new MatrixException(message);
            }
        }
    }

    public String setUniqueFieldName(Context context, DomainObject parent, String stage, DomainObject
            depProjectStage) throws Exception {

        //set unique name for the new substage
        String indexParent = parent.getInfo(context, "attribute[IMS_QP_DEPShortCode]") != null ?
                parent.getInfo(context, "attribute[IMS_QP_DEPShortCode]") : "";
        String depProjectStageName = depProjectStage.getInfo(context, DomainConstants.SELECT_NAME);

        //count all substages at this DEP ProjectStage
        StringList selects = new StringList(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_ID);
        selects.add("attribute[IMS_SortOrder]");

        String nameTail = !stage.equals("") ?
                "&&name smatch *" + depProjectStageName + stage + "*" :                                                 //if stage 1 or 2
                "&&name nsmatch *" + depProjectStageName + "1*&&name nsmatch *" + depProjectStageName + "2*";           //if stage is empty
        String where = IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_ID +
                depProjectStage.getInfo(context, DomainConstants.SELECT_ID) + nameTail;

        MapList result = findObjects(context,
                /*type*/ "IMS_QP_DEPSubStage",
                /*?*/ "*",
                /*where*/ where,
                selects
        );
        result.addSortKey("attribute[IMS_SortOrder]", "descending", "integer");
        result.sort();

        int numberInt = 1;
        if (result.size() > 0) {
            String name = (String) ((Map) result.get(0)).get(DomainConstants.SELECT_NAME);
            numberInt = Integer.parseInt(name.substring(name.lastIndexOf("-") + 1)) + 1;
        }
        String counter = (numberInt < 10) ? "0" + numberInt : Integer.toString(numberInt);

        //concate unique name
        String uniqueName = indexParent + depProjectStageName + stage + "-" + counter;

        //return new name to the field
        return uniqueName;
    }

    /**
     * Method to edit current IMS_QP_DEPSubStage
     *
     * @param context usualy parameter
     * @param args    usualy parameter
     * @throws Exception if has some errors throw common mistake
     */
    public void editPostProcess(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        Map paramMap = (Map) argsMap.get("paramMap");

        String subStageID = (String) paramMap.get("objectId");

        String baselineName = UIUtil.isNotNullAndNotEmpty((String) paramMap.get("baseline")) ? (String) paramMap.get("baseline") : "";
        String baselineID = "";

        if (!baselineName.equals("") && baselineName.contains("_")) {
            baselineName = baselineName.substring(0, baselineName.indexOf("_"));
            baselineID = baselineName;
        } else if (!baselineName.equals("")) {
            String where = "name smatch '" + baselineName + "'";
            MapList currentBaseline = DomainObject.findObjects(context, "IMS_Baseline", "eService Production", where, new StringList(DomainConstants.SELECT_ID));
            Map id = (Map) currentBaseline.get(0);
            baselineID = (String) id.get(DomainConstants.SELECT_ID);
        }

        DomainObject substage = new DomainObject(subStageID);
        String substageBaseline = UIUtil.isNotNullAndNotEmpty(substage.getInfo(context, IMS_QP_Constants_mxJPO.TO_IMS_QP_BASE_LINE_2_DEPSUB_STAGE_FROM_ID)) ?
                substage.getInfo(context, IMS_QP_Constants_mxJPO.TO_IMS_QP_BASE_LINE_2_DEPSUB_STAGE_FROM_ID) : "";

        //check if baselines is equals -> return
        if (substageBaseline.equals(baselineID)) {
            LOG.info("baselines equals");
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
                    DomainRelationship.disconnect(context, relId);
                    DomainRelationship.connect(context, new DomainObject(baselineID), "IMS_QP_BaseLine2DEPSubStage", substage);
                }

                ContextUtil.commitTransaction(context);

            } catch (Exception e) {
                ContextUtil.abortTransaction(context);
                LOG.error("transaction aborted: " + e.getMessage());
            }
        }
    }

    /**
     * Method to edit current IMS_QP_QPlan
     *
     * @param context usualy parameter
     * @param args    usualy parameter
     * @throws Exception if has some errors throw common mistake
     */
    public void editQPlanPostProcess(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);
        Map paramMap = (Map) argsMap.get("paramMap");

        String objectId = (String) paramMap.get("objectId");
        String description = (String) paramMap.get("description");
        String groupOID = (String) paramMap.get("groupOID");

        DomainObject qPlan = new DomainObject(objectId);
        //change description
        String currentDescription = qPlan.getDescription(context);
        if (!currentDescription.equals(description)) {
            qPlan.setDescription(context, description);
        }

        try {
            String classifierToQPlanRelationshipId = qPlan.getInfo(context, "to[IMS_QP_Classifier2QPlan].id");
            if (UIUtil.isNotNullAndNotEmpty(groupOID)) {
                DomainObject objectClassifier = new DomainObject(groupOID);

                //disconnect to QPlan
                if (UIUtil.isNotNullAndNotEmpty(classifierToQPlanRelationshipId)) {
                    String classifierToQPlanId = qPlan.getInfo(context, "to[IMS_QP_Classifier2QPlan].from.id");

                    if (classifierToQPlanId.equals(groupOID)) return;

                    DomainRelationship.disconnect(context, classifierToQPlanRelationshipId);
                }

                //connect to QPlan
                DomainRelationship.connect(context, objectClassifier, "IMS_QP_Classifier2QPlan", qPlan);

            } else {
                if (UIUtil.isNotNullAndNotEmpty(classifierToQPlanRelationshipId)) {
                    DomainRelationship.disconnect(context, classifierToQPlanRelationshipId);
                }
            }

        } catch (MatrixException matrixException) {
            LOG.info("Matrix exception: " + matrixException.getMessage());
        }
    }

    /**
     * @param context usualy parameter
     * @param args    usualy parameter
     * @return map of object names
     * @throws FrameworkException if has some errors throw common mistake
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
            fieldRangeValues.add(map.get(DomainConstants.SELECT_ID));
            fieldDisplayRangeValues.add(map.get(DomainConstants.SELECT_NAME));
        }

        HashMap tempMap = new HashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    /**
     * @param context usualy parameter
     * @param args    usualy parameter
     * @return raw map of dep names
     * @throws FrameworkException if has some errors throw common mistake
     */
    public Object getQPDEPNames(Context context, String[] args) {

        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error unpacking arguments: " + e.getMessage());
            e.printStackTrace();
        }

        String form = "";
        if (argsMap != null && !argsMap.isEmpty()) {
            Map requestMap = (Map) argsMap.get("requestMap");
            form = (String) requestMap.get("form");
        }

        String typePattern = IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP;

        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("from[IMS_QP_DEP2QPlan].to.id");
        objectSelects.add("attribute[IMS_QP_InterdisciplinaryDEP]");
        objectSelects.add("to[IMS_PBS2DEP].from.id");

        StringBuilder whereBuilder = new StringBuilder();

        if (UIUtil.isNotNullAndNotEmpty(form) && form.contains("AQP")) {
            whereBuilder.append("attribute[IMS_QP_InterdisciplinaryDEP]==TRUE&&name nsmatch '*ExternalInitialData*'");
        }

        if (UIUtil.isNotNullAndNotEmpty(form) && form.contains("SQP")) {
            whereBuilder.append("attribute[IMS_QP_InterdisciplinaryDEP]==FALSE||context.user==from[IMS_QP_DEP2Owner].to.name");
        }


        LOG.info("where: " + whereBuilder);
        MapList allDEPs = null;
        try {
            allDEPs = DomainObject.findObjects(context,
                    /*types*/ typePattern,
                    /*vault*/ "eService Production",
                    /*where*/ whereBuilder.toString(),
                    /*selects*/ objectSelects
            );
        } catch (FrameworkException e) {
            LOG.error("error find objects: " + e.getMessage());
            e.printStackTrace();
        }

        //initialize with empty string first value
        StringList fieldRangeValues = new StringList("");
        StringList fieldDisplayRangeValues = new StringList("");

        LOG.info("allDEPs: " + allDEPs);
        for (Object rawStage : allDEPs) {
            Map<String, String> map = (Map<String, String>) rawStage;

            String pbsArray = UIUtil.isNotNullAndNotEmpty(map.get("to[IMS_PBS2DEP].from.id")) ?
                    map.get("to[IMS_PBS2DEP].from.id") : "";
            String rangeValue = map.get(DomainConstants.SELECT_ID) + "_" + pbsArray + "_" + map.get("attribute[IMS_QP_InterdisciplinaryDEP]");
            rangeValue = rangeValue.replaceAll(IMS_QP_Constants_mxJPO.BELL_DELIMITER, ",");
            fieldRangeValues.add(rangeValue);
            fieldDisplayRangeValues.add(map.get(DomainConstants.SELECT_NAME));
        }


        Map resultMap = new HashMap();
        resultMap.put("field_choices", fieldRangeValues);
        resultMap.put("field_display_choices", fieldDisplayRangeValues);

        return resultMap;
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
            fieldRangeValues.add(map.get(DomainConstants.SELECT_ID));
            fieldDisplayRangeValues.add(map.get(DomainConstants.SELECT_NAME));
        }


        HashMap tempMap = new HashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    public StringList getSearchSystemsIds(Context context, String[] args) {
        StringList returnIDs = new StringList();

        try {
            SelectList selectList = new SelectList();
            selectList.add(SELECT_ID);

            String typePattern = IMS_QP_Constants_mxJPO.SYSTEM_TYPES;

            MapList allSystems = DomainObject.findObjects(context, typePattern, "eService Production", null, selectList);
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
                String str1 = (String) o1.get(DomainConstants.SELECT_NAME);
                String str2 = (String) o2.get(DomainConstants.SELECT_NAME);
                return str1.compareTo(str2);
            }
        });

        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();

        for (Object rawStage : allStages) {
            Map<String, String> map = (Map<String, String>) rawStage;
            fieldRangeValues.add(map.get(DomainConstants.SELECT_ID));
            fieldDisplayRangeValues.add(map.get(DomainConstants.SELECT_NAME));
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
            fieldRangeValues.add(map.get(DomainConstants.SELECT_ID) + "_" + map.get("to[IMS_ProjectStage2CB].from.id"));
            fieldDisplayRangeValues.add(map.get(DomainConstants.SELECT_NAME));
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
     * @param ctx
     * @param args
     * @return
     */
    private boolean isDraft(Context ctx, String type, String... args) {

        Map argsMap = null;
        String objectId;
        DomainObject object;
        String currentState = "";


        try {
            argsMap = JPO.unpackArgs(args);
            objectId = (String) argsMap.get("objectId");
            if (!StringUtils.isEmpty(objectId)) {
                object = new DomainObject(objectId);
                if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan.equals(type)) {
                    currentState = object.getInfo(ctx, DomainConstants.SELECT_CURRENT);
                }
                if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(type)) {
                    currentState = object.getInfo(ctx, "to[IMS_QP_QPlan2QPTask].from.current");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return currentState.equals("Draft");
    }

    public boolean checkDeleteQPlan(Context ctx, String... args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);
        String objectId = (String) argsMap.get("objectId");
        if (!IMS_QP_Security_mxJPO.isUserAdminOrSuper(ctx) && "SQP".equals(new DomainObject(objectId).getName(ctx))) {
            return false;
        }

        return IMS_QP_Security_mxJPO.isOwner(ctx, args);
    }

    public boolean checkAccess(Context ctx, String... args) throws Exception {
        if (IMS_QP_Security_mxJPO.isUserAdmin(ctx)) {
            return true;
        }

        Map argsMap = JPO.unpackArgs(args);
        Map settings = (Map) argsMap.get("SETTINGS");
        LOG.info("argsMap: " + argsMap);
        String key = (String) settings.get("key");
        LOG.info("key: " + key);

        /**
         * rule for admins
         */
        if (UIUtil.isNotNullAndNotEmpty(key) && "IMS_QP_QPlan_edit".equals(key)) {
            if (IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx)) {
                return true;
            }
        }

        /**
         * rule for viewer
         */
        if (IMS_QP_Security_mxJPO.isUserViewer(ctx)) {
            LOG.info("viewer: access non-granted");
            return false;
        }

        /**
         * In `Confirmation process` when toggle selected takes task Ids
         */
        String objectId = (String) argsMap.get("objectId");
        DomainObject object = new DomainObject(objectId);
        String type = object.getType(ctx);

        /**
         * check is current state `Draft` & Security returned `true`
         */
        if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan.equals(type)) {
            return isDraft(ctx, type, args) && IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, args);
        }

        /**
         * check is current state `Draft` & User the task Owner
         */
        if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(type)) {
            return (isDraft(ctx, type, args) && IMS_QP_Security_mxJPO.isOwnerQPlanFromTask(ctx, args));
        }

        /**
         * another stories
         */
        return false;
    }

    /**
     * Method to deleting IMS_QP_QPTask if their hasn't any approved related tasks
     *
     * @param ctx
     * @param args
     */
    public Map deleteQPTasks(Context ctx, String[] args) {

        //get all ids
        Map<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }
        LOG.info(argsMap.get("objectMap").getClass().getSimpleName());
        Map objectMap = null;
        if (argsMap.get("objectMap") instanceof Map) {
            objectMap = (Map) argsMap.get("objectMap");
        }

        String rawString = "";
        List<String> rawList = null;
        if (argsMap.get("objectMap") instanceof String) {
            if (((String) argsMap.get("objectMap")).contains("objectIds=[")) {
                rawString = (String) argsMap.get("objectMap");
                rawString = rawString.substring(rawString.lastIndexOf("[") + 1, rawString.lastIndexOf("]"));
                rawList = Arrays.asList(rawString.split(" "));
                LOG.info("rawList: " + rawList.size());
                for (String s : rawList) {
                    LOG.info("s " + s);
                }
            }
        }

        StringList rawIds = new StringList();
        if (objectMap != null) {
            rawIds = (StringList) objectMap.get("objectIds");
        } else if (rawList != null) {
            rawIds.addAll(rawList);
        }

        String[] rowIDs = new String[rawIds.size()];
        for (int i = 0; i < rawIds.size(); i++) {
            rowIDs[i] = rawIds.get(i).substring(0, rawIds.get(i).indexOf("|"));
        }
        List<String> deletingIDs = Arrays.asList(rowIDs);
        LOG.info("deletingIDs: " + deletingIDs);
        List<String> badNames = new ArrayList<>();
        boolean flag = false;

        //#62908_II
        for (String s : deletingIDs) {
            DomainObject domainObject = null;
            MapList relatedTasks = new MapList();
            String mainPlan = "";
            try {
                domainObject = new DomainObject(s);
                mainPlan = domainObject.getInfo(ctx, IMS_QP_Constants_mxJPO.PLAN_TO_TASK);

                StringList selects = new StringList();
                selects.addElement(DomainConstants.SELECT_ID);
                selects.addElement(DomainConstants.SELECT_NAME);
                selects.addElement(IMS_QP_Constants_mxJPO.PLAN_TO_TASK);

                SelectList relSelect = new SelectList();
                relSelect.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS);

                String where = "";

                relatedTasks = domainObject.getRelatedObjects(ctx,
                        /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                        /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                        /*object attributes*/ selects,
                        /*relationship selects*/ relSelect,
                        /*getTo*/ true, /*getFrom*/ true,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ where,
                        /*relationship where*/ null,
                        /*limit*/ 0);
//                LOG.info(domainObject.getName(ctx) + " related with " + relatedTasks);
            } catch (Exception e) {
                e.printStackTrace();
            }

            for (Object o : relatedTasks) {
                Map map = (Map) o;
                if (!map.get(IMS_QP_Constants_mxJPO.PLAN_TO_TASK).equals(mainPlan) && map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS).equals(IMS_QP_Constants_mxJPO.APPROVED)) {
                    badNames.add((String) map.get(DomainConstants.SELECT_NAME));
                    flag = true;
                }
            }

        }

        Map mapMessage = getMessage(badNames, flag);
        if (mapMessage.isEmpty()) {
            String operation = (String) argsMap.get("operation");
            LOG.info("operation: " + operation);
            if ("delete".equals(operation)) {
                LOG.info("deleted: " + deletingIDs);
                deleteObjects(ctx, deletingIDs);
            }
        }

        return mapMessage;
    }

    public String getTimeInfoAboutDeleteTasks(Context ctx, String... args) {
        Map<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

//        LOG.info("argsMap: " + argsMap);
        Map objectMap = (Map) argsMap.get("objectMap");
        StringList rawIds = (StringList) objectMap.get("objectIds");
        String[] rowIDs = new String[rawIds.size()];
        for (int i = 0; i < rawIds.size(); i++) {
//            LOG.info("index of: " + rawId.indexOf("|") + " clean id: " + rawIds.get(i).substring(0, rawIds.get(i).indexOf("|")));
            rowIDs[i] = rawIds.get(i).substring(0, rawIds.get(i).indexOf("|"));
        }

        int relationCounter = 0;
        try {
            BusinessObjectWithSelectList businessObjectWithSelectList
                    = BusinessObject.getSelectBusinessObjectData(ctx,
                    rowIDs, new StringList(DomainConstants.SELECT_ID));
            StringList selectBusStmts = new StringList(DomainConstants.SELECT_ID);
            StringList selectRelStmts = new StringList(DomainConstants.SELECT_ID);
            short recurse = 1;
            for (Object o : businessObjectWithSelectList) {
                BusinessObjectWithSelect bows = (BusinessObjectWithSelect) o;
                bows.open(ctx);
                ExpansionWithSelect expansion = bows.expandSelect(ctx,
                        /*rel type*/ "*", /*obj type*/"*",
                        selectBusStmts, selectRelStmts, /*getTo*/true,/*getFrom*/true, recurse);
                RelationshipWithSelectList relationshipWithSelectList = expansion.getRelationships();

                relationCounter += relationshipWithSelectList.size();
            }

        } catch (MatrixException matrixException) {
            matrixException.printStackTrace();
        }

        double timeCostMinutes = relationCounter * 0.10 / 60;
        String timeMessage = (
                timeCostMinutes > 1 ?
                        " This may take a long time (about " + ((timeCostMinutes > 60) ? (int) timeCostMinutes / 60 + " hour(s))" : (int) timeCostMinutes + " minute(s))")
                        : " This may take less than a minute");
        return "Trying to terminate more than " + relationCounter + " connections. " + timeMessage;
    }

    private Map getMessage(List<String> badNames, boolean flag) {
        Map mapMessage = new HashMap();
        if (flag) {
            mapMessage.put("array", badNames);
        }
        return mapMessage;
    }

    private void deleteObjects(Context context, List<String> deletingIDs) {
        for (int i = 0; i < deletingIDs.size(); i++) {
            try {
                MqlUtil.mqlCommand(context, "delete bus $1", new String[]{deletingIDs.get(i)});
            } catch (Exception e) {
                LOG.error("delete error id:" + i + ": " + deletingIDs.get(i) + " message: " + e.getMessage());
                for (StackTraceElement trace : e.getStackTrace()) {
                    LOG.error(deletingIDs.get(i) + ": " + trace.toString());
                }
            }
        }
    }

    public MapList getFindObject(Context context, String type, String name, String revision, String expression)
            throws Exception {
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        MapList resultType = DomainObject.findObjects(context, type, name,
                revision, "*", "*", expression, true, objectSelects);
        return resultType;
    }

    public void setCounterForDEPProjectStage(Context context, String[] args) throws Exception {

        try {
            MapList resultTasks = getFindObject(context,
                    "IMS_QP_DEPSubStage", "*", "*", "");

            for (Object resultObject : resultTasks) {
                Map objTemp = (Map) resultObject;
                String name = ((String) objTemp.get(DomainConstants.SELECT_NAME));
                try {
                    if (name.contains("-")) {
                        int numberInt = Integer.parseInt(name.substring(name.lastIndexOf("-") + 1));
                        new DomainObject((String) objTemp.get(DomainConstants.SELECT_ID)).setAttributeValue(context, "IMS_SortOrder", String.valueOf(numberInt));
                    } else {
                        new DomainObject((String) objTemp.get(DomainConstants.SELECT_ID)).setAttributeValue(context, "IMS_SortOrder", "0");
                    }
                } catch (Exception e) {
                    new DomainObject((String) objTemp.get(DomainConstants.SELECT_ID)).setAttributeValue(context, "IMS_SortOrder", "0");
                    System.out.println(e.fillInStackTrace());
                }
            }

        } catch (Exception ex) {
            LOG.error(ex.fillInStackTrace());
            throw ex;
        }
    }
}
