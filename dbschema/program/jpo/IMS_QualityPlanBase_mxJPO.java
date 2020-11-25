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
import matrix.db.Person;
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
        selects.add("id");
        selects.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPSUB_STAGE_2_DEPTASK);
        selects.add("name");

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
                badNames.add((String) map.get("name"));
                flag = true;
            } else {
                deletingIDs.add((String) map.get("id"));
            }
        }

        Map mapMessage = getMessage(badNames, flag);
        deleteObjects(context, deletingIDs);

        return mapMessage;
    }

    private void getArrayIDs(List<String> deletingIDs, String[] var1) {
        for (int i = 0; i < deletingIDs.size(); i++) {
            var1[i] = deletingIDs.get(i);
        }
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
        selects.add("id");

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
        selects.add("id");

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
        selects.add("id");

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
            allProjectStagesConnectingToDEP += map.get("id") + "|";
        }

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
                selects.add("id");

                where = "name smatch '*" + projectStage.getInfo(context, "name") + "*' && to[IMS_QP_DEP2DEPProjectStage].from.id==" + parentOID;

                result = findObjects(context,
                        /*type*/ "IMS_QP_DEPProjectStage",
                        /*?*/ "*",
                        /*where*/ where,
                        selects
                );

                Map map = (Map) result.get(0);
                String depProjectStageID = (String) map.get("id");

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

            ContextUtil.commitTransaction(context);
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

        String systemID = UIUtil.isNotNullAndNotEmpty((String) requestMap.get("systemOID")) ? (String) requestMap.get("systemOID") : "";
        String systemName = (String) requestMap.get("systemDisplay");

        String groupOID = (String) requestMap.get("groupOID");

        String depFieldFromForm = (String) requestMap.get("dep");
        String cleanDepID = UIUtil.isNotNullAndNotEmpty(depFieldFromForm) ? depFieldFromForm.substring(0, depFieldFromForm.indexOf("_")) : "";

        //set needed objects
        String newObjectId = (String) paramMap.get("newObjectId");
        DomainObject newObject = new DomainObject(newObjectId);
        DomainObject objectDEP = new DomainObject(cleanDepID);
        DomainObject objectPBS = new DomainObject();

        /*check if the DEP is Done status*/
        String depState = objectDEP.getInfo(context, "current");
        if (!depState.equals("Done")) {
            throw new MatrixException(String.format("%s is not finished yet and has status '%s'", objectDEP.getName(context), depState));
        }

        String qpPlanName = "";

        String relId = (String) paramMap.get("relId");

        List<String> listOwners = new ArrayList<>();

        if (systemName.isEmpty() && depFieldFromForm.endsWith("_FALSE")) {
            throw new Exception("You have to select a System!");
        } else if (!systemID.isEmpty() && !systemName.equals("empty") && depFieldFromForm.endsWith("_FALSE")) {
            //TODO objectPBS equals system rewrite
            DomainObject system = new DomainObject(systemID);
            String systemType = system.getType(context);

            /*check the owner of system*/
            String owners = MqlUtil.mqlCommand(context, String.format("print bus %s select %s", systemID, "from[IMS_PBS2Owner].to.name"));
            String userName = context.getUser();
            if (owners == null || !owners.contains(userName)) {
                throw new MatrixException(String.format("%s is not QPlan owner of %s %s ", userName, systemType, systemName));
            }

            /*check the dep of system*/
            String systemDEPs = MqlUtil.mqlCommand(context, String.format("print bus %s select %s dump |", systemID, "from[IMS_PBS2DEP].to.id"));
            if (systemDEPs == null || !systemDEPs.contains(cleanDepID)) {
                throw new MatrixException(String.format("%s does not include the specified dep", systemName));
            }

            //check if system type is functional area
            boolean isFunctionalArea = systemType.equals("IMS_PBSFunctionalArea");
            String forkConnection = isFunctionalArea ? "isFunctionalArea" : "to[IMS_PBSFunctionalArea2" + systemType + "].from";

            //getting info about other QPlan connections & throw errors if that is
            String relationshipFromQPlan;

            if (!forkConnection.equals("isFunctionalArea")) {
                relationshipFromQPlan = UIUtil.isNotNullAndNotEmpty(system.getInfo(context, forkConnection + ".to[IMS_QP_QPlan2Object]")) ?
                        system.getInfo(context, forkConnection + ".to[IMS_QP_QPlan2Object]") : "";
            } else {
                String systemQPlanConnection = MqlUtil.mqlCommand(context, String.format("print bus %s select from[IMS_PBSFunctionalArea2IMS_PBSSystem].to.to[IMS_QP_QPlan2Object] dump |", systemID));

                String buildingQPlanConnection = MqlUtil.mqlCommand(context, String.format("print bus %s select from[IMS_PBSFunctionalArea2IMS_GBSBuilding].to.to[IMS_QP_QPlan2Object] dump |", systemID));

                relationshipFromQPlan = systemQPlanConnection + buildingQPlanConnection;
            }

            if (relationshipFromQPlan.contains("TRUE")) {
                String message = !isFunctionalArea ? "this " + systemType.substring(systemType.lastIndexOf("_") + 1) + " is part of Functional Area" :
                        "this functional area has systems or building having some QPlans";
                throw new MatrixException(message);
            }
            objectPBS.setId(systemID);
            qpPlanName = objectPBS.getName(context);
        } else if (depFieldFromForm.endsWith("_TRUE")) {
            qpPlanName = objectDEP.getName(context);
        }

        MapList depOwners = DomainObject.getInfo(context, new String[]{cleanDepID}, new StringList("from[IMS_QP_DEP2Owner].to.id"));
        try {
            Map owners = (Map) depOwners.get(0);
            String rawOwners = (String) owners.get("from[IMS_QP_DEP2Owner].to.id");
            String[] arrayOwners = rawOwners.split(IMS_QP_Constants_mxJPO.BELL_DELIMITER);
            listOwners = Arrays.asList(arrayOwners);
        } catch (NullPointerException npe) {
            throw new MatrixException("error check owners of the DEP");
        }

        //create domain objects
        try {
            //start transactional
            ContextUtil.startTransaction(context, true);

            //change name for new object mask: QP-PBS /QP-10FAL/
            if (!qpPlanName.equals("")) {
                newObject.setName(context, "QP-" + qpPlanName);
            } else throw new MatrixException("unable to initialize correct name for the QPlan object");

            //connect relations
            DomainRelationship.connect(context, objectDEP, "IMS_QP_DEP2QPlan", newObject);

            if (UIUtil.isNotNullAndNotEmpty(groupOID)) {
                DomainObject objectClassifier = new DomainObject(groupOID);
                String dep2ClassifierId = MqlUtil.mqlCommand(context,
                        String.format("print bus %s select from[IMS_QP_DEP2Classifier].to.id dump |", objectDEP.getId(context)));
                //objectDEP.getInfo(context, "from[IMS_QP_DEP2Classifier].to.id");
                LOG.info("dep2ClassifierId: " + dep2ClassifierId + "|groupOID: " + groupOID);
                if (UIUtil.isNullOrEmpty(dep2ClassifierId)) {
                    throw new Exception("Selected DEP hasn't relation to any classifier");
                }
                if (!dep2ClassifierId.contains(groupOID)) {
                    throw new Exception("Selected DEP isn't related to the specified classifier");
                }
                DomainRelationship.connect(context, objectClassifier, "IMS_QP_Classifier2QPlan", newObject);
            }

            for (String ownerName : listOwners) {
                DomainObject personObject = new DomainObject(ownerName);
                DomainRelationship.connect(context, newObject, "IMS_QP_QPlan2Owner", personObject);

                Person person = new Person(personObject.getName(context));
                if (!person.isAssigned(context, "IMS_QP_QPOwner")) {
                    try {
                        ContextUtil.pushContext(context);
                    } catch (FrameworkException e) {
                        LOG.error("push context error: " + e.getMessage());
                    }
                    MqlUtil.mqlCommand(context, "mod person $1 assign role $2", person.getName(), "IMS_QP_QPOwner");
                    LOG.info(String.format("mod person %s assign role %s", person.getName(), "IMS_QP_QPOwner"));
                    try {
                        ContextUtil.popContext(context);
                    } catch (FrameworkException e) {
                        LOG.error("pop context error: " + e.getMessage());
                    }
                }
            }

            if (objectPBS != null && objectPBS.exists(context)) {
                DomainRelationship.connect(context, newObject, "IMS_QP_QPlan2Object", objectPBS);
            }

            //commit transaction
            ContextUtil.commitTransaction(context);
        } catch (Exception e) {
            ContextUtil.abortTransaction(context);
            LOG.error("transaction aborted: " + e.getMessage());
            throw e;
        }
    }

    public String setUniqueFieldName(Context context, DomainObject parent, String stage, DomainObject depProjectStage) throws Exception {

        //set unique name for the new substage
        String indexParent = parent.getInfo(context, "attribute[IMS_QP_DEPShortCode]") != null ?
                parent.getInfo(context, "attribute[IMS_QP_DEPShortCode]") : "";
        String depProjectStageName = depProjectStage.getInfo(context, "name");

        //count all substages at this DEP ProjectStage
        StringList selects = new StringList(DomainConstants.SELECT_NAME);
        selects.add("id");
        selects.add("attribute[IMS_SortOrder]");

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
            MapList currentBaseline = DomainObject.findObjects(context, "IMS_Baseline", "eService Production", where, new StringList("id"));
            Map id = (Map) currentBaseline.get(0);
            baselineID = (String) id.get("id");
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
            fieldRangeValues.add(map.get("id"));
            fieldDisplayRangeValues.add(map.get("name"));
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
    public Object getQPDEPNames(Context context, String[] args) throws FrameworkException {

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
                String str1 = (String) o1.get("name");
                String str2 = (String) o2.get("name");
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

        List<String> badNames = new ArrayList<>();
        boolean flag = false;

        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            if (!IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(context, (String) map.get("id"))) {
                badNames.add((String) map.get("name"));
                flag = true;
            } else {
                deletingIDs.add((String) map.get("id"));
            }
        }

        Map mapMessage = getMessage(badNames, flag);
        deleteObjects(context, deletingIDs);

        return mapMessage;
    }

    private Map getMessage(List<String> badNames, boolean flag) {
        Map mapMessage = new HashMap();
        if (flag) {
            mapMessage.put("array", badNames);
        }
        return mapMessage;
    }

    private void deleteObjects(Context context, List<String> deletingIDs) {
        String[] var1 = new String[deletingIDs.size()];
        getArrayIDs(deletingIDs, var1);

        //delete tasks
        try {
            if (var1.length > 0)
                DomainObject.deleteObjects(context, var1);
        } catch (Exception e) {
            LOG.error("delete error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MapList getFindObject(Context context, String type, String name, String revision, String expression) throws
            Exception {
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
