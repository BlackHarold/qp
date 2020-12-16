import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;


public class IMS_QP_mxJPO extends DomainObject {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public MapList getStructureList(Context context, String[] args) throws FrameworkException {

        MapList componentsList;
        try {
            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");

            StringBuffer sbType = new StringBuffer().
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP_DEP).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_Baseline).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_ProjectStage).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP_Directory).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP_ResultType).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_Family).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_DisciplineCode).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP_DEPProjectStage).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP_DEPSubStage).append(",").
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP_DEPTask);

            StringBuffer sbRel = new StringBuffer().
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_Project2QP).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_Project2Directory).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_Directory2ResultType).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2Family).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_Directory2Baseline).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_Directory2ProjectStage).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_Directory2DisciplineCode).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_Directory2Directory).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QP2DEP).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2DEPProjectStage).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPProjectStage2DEPSubStage).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QP2QPlan).append(",").
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPSubStage2DEPTask);

            DomainObject domObj = newInstance(context, objectId);

            StringList objectSelects = new StringList(3);
            objectSelects.add(DomainConstants.SELECT_ID);
            objectSelects.add(DomainConstants.SELECT_TYPE);
            objectSelects.add(DomainConstants.SELECT_NAME);

            StringList relSelects = new StringList(
                    DomainConstants.SELECT_RELATIONSHIP_ID);

            componentsList = domObj.getRelatedObjects(context, // matrix context
                    sbRel.toString(), // all relationships to expand
                    sbType.toString(), // all types required from the expand
                    objectSelects,// object selects
                    relSelects, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    "", // object where clause
                    "", // relationship where clause
                    0);

            String objectType = new DomainObject(objectId).getType(context);
            String objectName = new DomainObject(objectId).getName(context);
            if ("IMS_QP".equals(objectType) && "SQP/BQP".contains(objectName)) {
                componentsList = getFilteredMapListByOwner(context, componentsList);
            }

        } catch (Exception ex) {
            throw new FrameworkException(ex.toString());
        }
        return componentsList;
    }

    private MapList getFilteredMapListByOwner(Context context, MapList mapList) {

        boolean isSuperUser = false, isAdmin = false;
        isSuperUser = IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(context);
        isAdmin = IMS_QP_Security_mxJPO.isUserAdmin(context);

        if (isSuperUser || isAdmin) return mapList;

        MapList filteredByOwnerSQPs = new MapList();
        for (Object o : mapList) {
            Map map = (Map) o;
            boolean isOwner = false;
            try {
                isOwner = IMS_QP_Security_mxJPO.isOwnerQPlan(context, (String) map.get("id")) ||
                        IMS_QP_Security_mxJPO.isOwnerDepFromQPlan(context, (String) map.get("id"));
            } catch (Exception e) {
                LOG.error("an error: " + e.getMessage());
            }
            if (isOwner) {
                filteredByOwnerSQPs.add(map);
            }
        }

        //get all QP plans filtered by owner
        return filteredByOwnerSQPs;
    }

    /**
     * Label program for Tree structure
     */
    public String getDisplayNameForNavigator(Context context, String[] args) throws Exception {

        HashMap paramMap = (HashMap) ((HashMap) JPO.unpackArgs(args)).get("paramMap");
        String objectId = (String) paramMap.get("objectId");
        StringList select = new StringList("revision");
        select.add("type");
        select.add("name");
        Map mapInfo = (new DomainObject(objectId)).getInfo(context, select);
        return mapInfo.get("name").toString();
    }

    public static MapList getIcons(Context context, String[] args) throws Exception {
        MapList iconList = new MapList();
        try {
            // unpack args array to get input map
            Map programMap = JPO.unpackArgs(args);
            // get object list
            MapList ObjectList = (MapList) programMap.get("objectList");

            String[] bArr = new String[ObjectList.size()];
            StringList bSel = new StringList();
            bSel.add("policy.property[PolicyClassification].value");
            bSel.add(DomainConstants.SELECT_TYPE);

            // Get the object elements - OIDs and RELIDs - if required
            for (int i = 0; i < ObjectList.size(); i++) {
                // Get Business object Id
                bArr[i] = (String) ((Map) ObjectList.get(i)).get("id");
            }

            // Get the required information for the objects.
            BusinessObjectWithSelectList bwsl = BusinessObject.getSelectBusinessObjectData(context, bArr, bSel);

            for (int i = 0; i < ObjectList.size(); i++) {
                String currentObjectid = (String) ((Map) ObjectList.get(i)).get("id");
                // get the current state value
                String sPolicyClassification = bwsl.getElement(i).getSelectData("policy.property[PolicyClassification].value");
                String type = bwsl.getElement(i).getSelectData("type");

                HashMap retMap = new HashMap();
                //TODO rewrite code after consultating with developer of this
                //never used                HashMap alternateMap = new HashMap();
                // Based on the object state add the required icon
                //Pass the required information to this method and get the required icon //name.
                String objectIcon = getIcons(context, type, sPolicyClassification);

                retMap.put(currentObjectid, "buttonWizardNextDisabled.gif");
                // Size of the iconList should be as same as ObjectList.
                iconList.add(retMap);
            }
        } catch (FrameworkException e) {
            throw new FrameworkException(e.toString());
        }
        return iconList;
    }

    //TODO rewrite this method
    public static String getIcons(Context context, String typeName, String sPolicyClassification) {
        if (sPolicyClassification.equalsIgnoreCase("Equivalent"))
            return "buttonWizardNextDisabled.gif";
        else {
            //never used            String typeIcon = UINavigatorUtil.getTypeIconProperty(context, typeName);
            return "";
        }
    }

    public Object getDisciplineCode(Context context,
                                    String[] args
    ) throws Exception {

        String typePattern = "IMS_DisciplineCode";
        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);
        objectSelects.add("attribute[IMS_ShortName]");

        String objectWhere = "attribute[IMS_Level]=='1'";

        MapList AllStages = DomainObject.findObjects(context, typePattern,
                "eService Production", objectWhere, objectSelects);

        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();

        for (Object rawStage : AllStages) {
            Map<String, String> map = (Map<String, String>) rawStage;
            fieldRangeValues.add(map.get("id"));
            fieldDisplayRangeValues.add(map.get(DomainConstants.SELECT_NAME));
        }

        HashMap tempMap = new HashMap();
        tempMap.put("field_choices", fieldRangeValues);
        tempMap.put("field_display_choices", fieldDisplayRangeValues);

        return tempMap;
    }

    public void postProcess_IMS_QP(Context context, String[] args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);

        Map requestMap = (Map) argsMap.get("requestMap");
        String shortCode = (String) requestMap.get("short_code");

        if (checkUniqueShortCodeDEP(context, shortCode) != true) {
            throw new FrameworkException("short name is not unique");

        } else {

            Map paramMap = (Map) argsMap.get("paramMap");
            String depId = (String) paramMap.get("newObjectId");
            String disciplineCodeID = (String) requestMap.get("DisciplineCode");

            DomainObject disciplineCodeObj = new DomainObject(disciplineCodeID);
            DomainObject depObj = new DomainObject(depId);
            DomainRelationship.connect(context, disciplineCodeObj, "IMS_QP_Discipline2DEP", depObj);
        }
    }

    private boolean checkUniqueShortCodeDEP(Context context, String shortCode) {
        boolean check = true;

        String typePattern = "IMS_QP_DEP";
        StringList select = new StringList(DomainConstants.SELECT_ID);
        String where = "attribute[IMS_QP_DEPShortCode]=='" + shortCode + "'";

        MapList sameShortCodes = new MapList();
        try {
            sameShortCodes = DomainObject.findObjects(context, typePattern, "eService Production", where, select);
        } catch (FrameworkException e) {
            e.printStackTrace();
        }

        if (sameShortCodes.size() > 1) check = false;

        return check;
    }

    /**
     * It's retrieve DEPs for table IMS_QP_DEP
     */
    public MapList getAllDEP(Context context, String[] args) throws Exception {

        MapList allDEPs = new MapList();
        try {
            allDEPs = DomainObject.findObjects(context,
                    /*type*/ IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP,
                    /*vault*/IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    /*where*/null,
                    /*selects*/ new StringList(DomainConstants.SELECT_ID));
        } catch (FrameworkException fe) {
            LOG.error("error getting tasks: " + fe.getMessage());
            fe.printStackTrace();
        }

        return allDEPs;
    }

    /**
     * It's retrieve KKS and PBS for table DEP KKS PBS
     */
    public MapList getAllPBS(Context context, String[] args) {

        MapList result = null;
        try {
            result = findObjects(context, IMS_QP_Constants_mxJPO.SYSTEM_TYPES, "*", "revision==last", new StringList("id"));
        } catch (FrameworkException frameworkException) {
            frameworkException.printStackTrace();
        }
        return result;
    }

    public Vector getPBS(Context context, String... args) {

        Vector result = new Vector();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            Map argsMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) argsMap.get("objectList");

            /*getting all systems*/
            List<Map> items = new ArrayList<>();
            for (Object o : objectList) {
                items.add((Map) o);
            }

            /*top level Codes by items*/
            for (Map map : items) {
                stringBuilder.setLength(0);

                String pbsID = (String) map.get("id");
                DomainObject systemObject = new DomainObject(pbsID);
                String systemType = systemObject.getType(context);

                if (pbsID != null) {
                    //get pbs from system
                    String parentPBS = systemObject.getInfo(context, "to[IMS_PBSFunctionalArea2" + systemType + "].from.name");
                    stringBuilder.append(UIUtil.isNotNullAndNotEmpty(parentPBS) ? parentPBS : "No group");
                }

                result.addElement(stringBuilder.toString());
            }
        } catch (Exception e) {
            try {
                LOG.error("error getting url string: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public Vector getQP(Context context, String... args) {
        Vector result = new Vector();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            Map argsMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) argsMap.get("objectList");

            /*getting all systems*/
            List<Map> items = new ArrayList<>();
            for (Object o : objectList) {
                items.add((Map) o);
            }

            /*top level Codes by items*/
            for (Map map : items) {
                stringBuilder.setLength(0);

                String pbsID = (String) map.get("id");
                DomainObject systemObject = new DomainObject(pbsID);

                if (pbsID != null) {
                    String hasQPlan = systemObject.getInfo(context, "to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object + "]");
                    if ("TRUE".equals(hasQPlan)) {
                        String relatedType = systemObject.getInfo(context, "to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object + "].from.type");

                        String relatedQPState = "";
                        switch (relatedType) {
                            case "IMS_QP_QPlan":
                                relatedQPState = systemObject.getInfo(context, "to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object + "].from.current");
                                break;
                            case "IMS_QP_QPTask":
                                String isNotInterdisciplinaryTask = systemObject.getInfo(context,
                                        "to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object
                                                + "].from.to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask
                                                + "].from.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object + "]");

                                if ("TRUE".equals(isNotInterdisciplinaryTask)) {
                                    relatedQPState = systemObject.getInfo(context,
                                            "to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object
                                                    + "].from.to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.current");
                                } else stringBuilder.append("No plan");
                                break;
                        }
                        stringBuilder.append(relatedQPState);
                    } else {
                        stringBuilder.append("No plan");
                    }
                }

                result.addElement(stringBuilder.toString());
            }
        } catch (Exception e) {
            try {
                LOG.error("error getting url string: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }


    /**
     * Method to show the table of related with DEP-object IMS_QP_DEPTask elements
     */
    public MapList getAllRelatedTasksForTable(Context context, String[] args) throws Exception {

        MapList result = new MapList();
        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");

        StringList selects = new StringList("id");

        //get all tasks
        Map depMap = new HashMap<>();
        depMap.put("type", "IMS_QP_DEP");
        depMap.put("id", objectId);
        result.add(depMap);

        result.addAll(DomainObject.findObjects(context,
                /*type*/"IMS_QP_DEPTask",
                "eService Production",
                /*where*/"to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_DEPProjectStage2DEPSubStage].from.to[IMS_QP_DEP2DEPProjectStage].from.id==" + objectId,
                /*selects*/ selects));
        return result;
    }

    /**
     * It's retrieve DEPs for table IMS_QP_DEP
     */
    public MapList getAllSQP(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add("id");

        DomainObject parent = new DomainObject(objectId);

        MapList listSQPs = parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_QP_QPlan",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 1,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);

        //get all QP plans filtered by owner
        listSQPs = getFilteredMapListByOwner(context, listSQPs);

        return listSQPs;
    }

    /**
     * It deletes IMS_DEP in the table IMS_QP_DEP
     */
    public Map deleteDEP(Context context, String[] args) throws Exception {

        StringBuffer message = new StringBuffer();
        StringBuffer messageDel = new StringBuffer();
        Map mapMessage = new HashMap();

        HashMap paramMap = JPO.unpackArgs(args);
        String[] rowIDs = (String[]) paramMap.get("emxTableRowId");
        String[] ids = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            ids[i] = rowIDs[i].substring(0, rowIDs[i].indexOf("|"));
        }

        StringList selects = new StringList();
        selects.add(DomainObject.SELECT_ID);
        selects.add(DomainObject.SELECT_NAME);
        selects.add("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2DEPProjectStage + "].to." +
                "from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPProjectStage2DEPSubStage + "]");
        selects.add("from[IMS_QP_DEP2QPlan]");

        MapList objectsInfo = DomainObject.getInfo(context, ids, selects);

        ArrayList<String> deletedObjects = new ArrayList<>();
        for (Object o : objectsInfo) {
            Map map = (Map) o;
            String subStage = (String) (map.get("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2DEPProjectStage + "].to." +
                    "from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPProjectStage2DEPSubStage + "]"));
            String qPlan = (String) map.get("from[IMS_QP_DEP2QPlan]");
            String concatenated = subStage + qPlan;
            getMessage(message, messageDel, deletedObjects, map, concatenated);
        }

        String[] deletion = new String[deletedObjects.size()];
        for (int i = 0; i < deletedObjects.size(); i++) {
            deletion[i] = deletedObjects.get(i);
        }

        if (deletion.length > 0)
            DomainObject.deleteObjects(context, deletion);

        if (message.length() > 0)
            message.append(" have a DEPSubStage or QPlan!\n");

        if (messageDel.length() > 0)
            message.append(messageDel).append(" were deleted!");

        mapMessage.put("message", message.toString());
        return mapMessage;
    }

    private void getMessage(StringBuffer message, StringBuffer messageDel, List<String> deletedObjects, Map map, String string) {
        if (string != null && string.contains("TRUE")) {
            if (message.length() > 0)
                message.append(", ");
            message.append(map.get(DomainObject.SELECT_NAME));
        } else {
            if (messageDel.length() > 0)
                messageDel.append(", ");
            messageDel.append(map.get(DomainObject.SELECT_NAME));
            deletedObjects.add((String) map.get(DomainObject.SELECT_ID));
        }
    }

    /**
     * @param context usual parameter
     * @param args    usual parameter
     * @return Map which contains message with results of copying
     */
    public Map copyQPTask(Context context, String... args) {
        Map mapMessage = new HashMap();
        StringBuilder stringBuilder = new StringBuilder();
        HashMap paramMap = null;

        try {
            paramMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            stringBuilder.append("error when unpacking arguments: " + e.getMessage() + "\n");
            LOG.error("error when unpacking arguments: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = new String[0];
        if (paramMap != null) {
            rowIDs = (String[]) paramMap.get("emxTableRowId");
        }
        List<String> ids = new ArrayList<>();
        for (String rowID : rowIDs) {
            ids.add(rowID.substring(0, rowID.indexOf("|")));
        }

        //copy all ids objects with new name and not include files
        if (!ids.isEmpty()) {
            for (String id : ids) {
                try {
                    DomainObject parentTask = new DomainObject(id);
                    String name = parentTask.getName(context), type = "IMS_QP_QPTask";
                    MapList mapList = DomainObject.findObjects(context,
                            /*type*/ type,
                            /*vault*/ "eService Production",
                            /*where*/"name smatch '*" + name + "*'",
                            /*selects*/new StringList("id"));

                    //mapList size isn't smaller than 1
                    int counter = mapList.size();
                    String postfix = counter < 10 ? "_0" + counter : "_" + counter;
                    String rename = String.format("%s%s", name, postfix);

                    //transaction
                    ContextUtil.startTransaction(context, true);
                    DomainObject targetTask = new DomainObject();
                    targetTask.createObject(context,
                            /*type*/type,
                            /*name*/rename,
                            /*revision*/"",
                            /*policy*/type,
                            /*vault*/ context.getVault().getName());
                    targetTask.setAttributeValue(context, "IMS_Name", parentTask.getAttributeValue(context, "IMS_Name"));
                    targetTask.setAttributeValue(context, "IMS_NameRu", parentTask.getAttributeValue(context, "IMS_NameRu"));

                    copyRelationshipsOfTask(context, id, targetTask, postfix);
                    ContextUtil.commitTransaction(context);

                } catch (Exception e) {
                    stringBuilder.append("error when coping: " + e.getMessage());
                    LOG.error("error when coping: " + e.getMessage());
                    ContextUtil.abortTransaction(context);
                }
            }
        }

        if (stringBuilder.length() == 0) stringBuilder.append("object copied");
        mapMessage.put("message", stringBuilder.toString());
        return mapMessage;
    }

    /**
     * @param context      usual parameter
     * @param id           is parent object id
     * @param targetObject copied object
     * @param postfix      last piece of name for copying object name
     * @throws Exception any errors thrown by the method
     */
    private void copyRelationshipsOfTask(Context context, String id, DomainObject targetObject, String postfix) throws Exception {

        // Instantiate the BusinessObject.
        StringList selectBusStmts = new StringList();
        selectBusStmts.addElement("id");
        selectBusStmts.addElement("type");
        selectBusStmts.addElement("name");

        StringList selectRelStmts = new StringList();
        selectRelStmts.addElement("name");
        selectRelStmts.addElement("to.id");
        selectRelStmts.addElement("from.id");
        short recurse = 1;

        BusinessObject parentObject = new BusinessObject(id);
        parentObject.open(context);

        // Getting the expansion
        ExpansionWithSelect expansion = parentObject.expandSelect(context, "*", "*", selectBusStmts, selectRelStmts, true, true, recurse);
        parentObject.close(context);
        //--------------------------------------------------------------
        //  _object.expandSelect(_context, - Java context object
        //  "*",                           - relationship Pattern
        //  "*",                           - type Pattern
        //  selectBusStmts,                - selects for Business Objects
        //  selectRelStmts,                - selects for Relationships
        //  true,                          - get To relationships
        //  true,                          - get From relationships
        //  recurse);                      - recursion level (0 = all)
        //--------------------------------------------------------------

        // Getting Relationships.
        RelationshipWithSelectList _relSelectList = expansion.getRelationships();
        RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(_relSelectList);

        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();
            String relationshipType = relSelect.getSelectData("name");
            //ignoring type of QPTask2QPTask relationships
            if (relationshipType.equals("IMS_QP_QPTask2QPTask")) continue;

            //if expected result type
            boolean from = relSelect.getSelectData("from.id").equals(id) ? true : false;
            if (relSelect.getSelectData("name").equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask)) {
                DomainObject expectedResult = new DomainObject(relSelect.getSelectData((from ? "to" : "from") + ".id"));
                String expectedResultRename = expectedResult.getName(context) + postfix;

                //copied expected result
                DomainObject clonedExpectedResult = new DomainObject();
                clonedExpectedResult.createObject(context,
                        /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                        /*name*/expectedResultRename,
                        /*revision*/"",
                        /*policy*/"IMS_QP_ExpectedResult",
                        /*vault*/ context.getVault().getName());

                //coping all attributes
                copyAttributes(context, expectedResult, clonedExpectedResult);

                String resultTypeID = expectedResult.getInfo(context, "to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult + "].from.id");
                DomainObject resultType = new DomainObject(resultTypeID);
                if (resultType.getType(context).equals("IMS_Family")) {
                    clonedExpectedResult.connect(context, new RelationshipType(IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult), /*from clone*/ false, resultType);
                } else {
                    emxContextUtil_mxJPO.mqlWarning(context, "Expected result " + expectedResult.getName(context) + " doesn't have connection from IMS_Family. Check this!");
                    //throw new FrameworkException("Expected result doesn't have connection from IMS_Family. Check this!");
                }
                //connect copied Expected result from/to targetObject
                clonedExpectedResult.connect(context, new RelationshipType(relationshipType), /*from clone*/ !from, targetObject);
            } else {
                //another connections
                if ("IMS_QP_QPTask2Fact".equals(relationshipType)) continue;
                DomainObject anotherObject = new DomainObject(relSelect.getSelectData("from.id"));
                DomainRelationship.connect(context, anotherObject, new RelationshipType(relationshipType), targetObject);
            }
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

    /**
     * It deletes IMS_DEP in the table IMS_QP_QPlan
     */
    public Map deleteQPlan(Context context,
                           String[] args
    ) throws Exception {

        StringBuffer message = new StringBuffer();
        StringBuffer messageDel = new StringBuffer();
        Map mapMessage = new HashMap();

        HashMap paramMap = JPO.unpackArgs(args);
        String[] rowIDs = (String[]) paramMap.get("emxTableRowId");
        String[] ids = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            ids[i] = rowIDs[i].substring(0, rowIDs[i].indexOf("|"));
        }

        StringList selects = new StringList();
        selects.add(DomainObject.SELECT_ID);
        selects.add(DomainObject.SELECT_NAME);
        selects.add("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "]");

        MapList objectsInfo = DomainObject.getInfo(context, ids, selects);

        List<String> delObjs = new ArrayList<>();
        for (Object o : objectsInfo) {
            Map map = (Map) o;
            String tasks = (String) (map.get("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "]"));
            getMessage(message, messageDel, delObjs, map, tasks);
        }


        String[] deletion = new String[delObjs.size()];
        for (int i = 0; i < delObjs.size(); i++) {
            deletion[i] = delObjs.get(i);
        }

        if (deletion.length > 0)
            DomainObject.deleteObjects(context, deletion);

        if (message.length() > 0)
            message.append(" have a task!\n");

        if (messageDel.length() > 0)
            message.append(messageDel).append(" was deleted!");


        mapMessage.put("message", message.toString());
        return mapMessage;
    }

    public Boolean checkAccess(Context context,
                               String[] args
    ) throws Exception {

        Map programMap = JPO.unpackArgs(args);
        String objId = (String) programMap.get("objectId");

        return IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, objId);
    }

    /**
     * works with TRIGGER_IMS_QPDEPPolicyDraftPromoteCheck
     *
     * @param context usualy parameter
     * @param args    usualy parameter
     * @return Integer value. Usualy used in triggers. '1' if something went wrong, '0' if the method passed ok
     * @throws Exception any errors thrown by the method
     */
    public int checkPromoteConditionToDone(Context context, String[] args) throws Exception {
        String id = args[0];
        StringBuilder message = new StringBuilder();

        String taskNameLevelDep = String.format("to[%s].from.name",
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPTask2DEP);
        String taskStatusLevelDep = String.format("to[%s].attribute[IMS_QP_DEPTaskStatus]",
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPTask2DEP);
        String fromTaskStatus = String.format("from[%s].to.from[%s].to.from[%s].to.to[%s].attribute[IMS_QP_DEPTaskStatus]",
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPTask2DEPTask);
        String fromTaskName = String.format("from[%s].to.from[%s].to.from[%s].to.to[%s].from.name",
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPTask2DEPTask);
        String toTaskStatus = String.format("from[%s].to.from[%s].to.from[%s].to.from[%s].attribute[IMS_QP_DEPTaskStatus]",
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPTask2DEPTask);
        String toTaskName = String.format("from[%s].to.from[%s].to.from[%s].to.from[%s].to.name",
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPTask2DEPTask);


        DomainObject domainObjectFrom = new DomainObject(args[0]);
        DomainObject domainObjectTo = new DomainObject(new BusinessObject("Person", context.getUser(), "-", context.getVault().getName()));
        boolean isConnectedOwner = IMS_KDD_mxJPO.isConnected(context, "IMS_QP_DEP2Owner", /*from*/ domainObjectFrom, /*to*/ domainObjectTo);
        boolean isDEPOwner = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, domainObjectTo);

        if (isDEPOwner || isConnectedOwner) {
            StringList select = new StringList();
            select.add(taskNameLevelDep);
            select.add(taskStatusLevelDep);
            select.add(fromTaskName);
            select.add(fromTaskStatus);
            select.add(toTaskName);
            select.add(toTaskStatus);

            Hashtable setOfrelsToTaskApproved = new DomainObject(id).getBusinessObjectData(context, select);

            /*check relationships to DEP*/
            StringList getSetsNameLevelDep = (StringList) setOfrelsToTaskApproved.get(taskNameLevelDep);
            StringList getSetStatusLevelDep = (StringList) setOfrelsToTaskApproved.get(taskStatusLevelDep);
            /*check Draft status from tasks*/
            StringList getSetsFromStatus = (StringList) setOfrelsToTaskApproved.get(fromTaskStatus);
            StringList getSetsFromName = (StringList) setOfrelsToTaskApproved.get(fromTaskName);
            /*check Draft status to tasks*/
            StringList getSetsToStatus = (StringList) setOfrelsToTaskApproved.get(toTaskStatus);
            StringList getSetsToName = (StringList) setOfrelsToTaskApproved.get(toTaskName);

            for (int i = 0; i < getSetStatusLevelDep.size(); i++) {
                if ("draft".equalsIgnoreCase((String) (getSetStatusLevelDep.get(i)))) {
                    message.append("\n").append(getSetsNameLevelDep.get(i));
                }
            }
            if (message.length() > 0) {
                message.append(EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.hasAcceptedDEP"));
            }

            for (int i = 0; i < getSetsFromStatus.size(); i++) {
                if ("draft".equalsIgnoreCase((String) (getSetsFromStatus.get(i)))) {
                    message.append("\n").append(getSetsFromName.get(i));
                    message.append(" - ").append(getSetsFromStatus.get(i));
                }
            }

            for (int i = 0; i < getSetsToStatus.size(); i++) {
                if ("Draft".equals((getSetsToStatus.get(i)))) {
                    message.append("\n").append(getSetsToName.get(i));
                    message.append(" - ").append(getSetsToStatus.get(i));
                }
            }

            if (message.length() > 0) {
                message.append(EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.haveNoApproved"));
                emxContextUtil_mxJPO.mqlWarning(context, message.toString());
                return 1;
            }
        }
        return 0;
    }

    /**
     * works with TRIGGER_IMS_QPDEPPolicyDoneDemoteCheck
     *
     * @param context usualy parameter
     * @param args    usualy parameter
     * @return allways retun zero
     * @throws Exception any errors thrown by the method
     */
    public int checkDemoteConditionToDraft(Context context,
                                           String[] args
    ) throws Exception {
        if (IMS_KDD_mxJPO.isConnected(context, "IMS_QP_DEP2Owner",
                new DomainObject(args[0]), new DomainObject(new BusinessObject("Person", context.getUser(), "-", context.getVault().getName())))) {
            emxContextUtil_mxJPO.mqlWarning(context, EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.noPermissions"));
            return 1;
        }
        return 0;
    }
}
