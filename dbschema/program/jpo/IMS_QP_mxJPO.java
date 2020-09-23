import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UINavigatorUtil;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;


public class IMS_QP_mxJPO extends DomainObject {

    public IMS_QP_mxJPO(Context context, String[] args) throws Exception {
        super();
        if (args != null && args.length > 0) {
            setId(args[0]);
        }
    }

    public static MapList getStructureList(Context context, String[] args) throws FrameworkException {
        MapList componentsList = null;
        try {
            HashMap programMap = JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
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

            componentsList = domObj.getRelatedObjects(context, // matrix
                    // context
                    sbRel.toString(), // all relationships to expand
                    sbType.toString(), // all types required from the expand
                    objectSelects,// object selects
                    relSelects, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    "", // object where clause
                    "", 0); // relationship where clause

        } catch (Exception ex) {
            throw new FrameworkException(ex.toString());
        }
        return componentsList;
    }

    /**
     * Label program for  Tree structure
     */
    public String getDisplayNameForNavigator(Context context, String[] args) throws Exception {

        HashMap paramMap = (HashMap) ((HashMap) JPO.unpackArgs(args)).get("paramMap");
        String objectId = (String) paramMap.get("objectId");
        StringList select = new StringList("revision");
        select.add("type");
        select.add("name");
        Map mapInfo = (new DomainObject(objectId)).getInfo(context, select);
        String name = mapInfo.get("name").toString();
        String revision = mapInfo.get("revision").toString();
        String type = mapInfo.get("type").toString();

        String strTreeName = name;
        return strTreeName;
    }

    /**
     * Label program for  Tree structure
     */
    public String getIcons1(Context context, String[] args) {

        String strTreeName = "IMS_QP_QPlan_16x16.png";
        return "";
    }

    public static MapList getIcons(Context context, String[] args) throws Exception {
        MapList iconList = new MapList();
        try {
            // unpack args array to get input map
            Map programMap = (Map) JPO.unpackArgs(args);
            // get object list
            MapList ObjectList = (MapList) programMap.get("objectList");

            String bArr[] = new String[ObjectList.size()];
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
                HashMap alternateMap = new HashMap();
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


    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public static String getIcons(Context context, String typeName, String sPolicyClassification) {
        //TODO rewrite this method
        if (sPolicyClassification.equalsIgnoreCase("Equivalent"))
            return "buttonWizardNextDisabled.gif";
        else {
            String typeIcon = UINavigatorUtil.getTypeIconProperty(context, typeName);
            return "buttonWizardNextDisabled.gif";
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

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add("id");

        DomainObject parent = new DomainObject(objectId);
        //get all substages
        MapList result = parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_QP_DEP",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 1,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);

        return result;
    }

    /**
     * It's retrieve DEPs for table IMS_QP_DEP
     */
    public MapList getAllPBS(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add("id");

        MapList result = findObjects(context, IMS_QP_Constants_mxJPO.SYSTEM_TYPES, "*", "", new StringList("id"));
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
        //get all substages
        MapList result = parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/"IMS_QP_QPlan",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 1,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);

        return result;
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

        MapList objectsInfo = DomainObject.getInfo(context, ids, selects);

        ArrayList<String> delObjs = new ArrayList();
        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            String subStage = (String) (map.get("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2DEPProjectStage + "].to." +
                    "from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPProjectStage2DEPSubStage + "]"));
            getMessage(message, messageDel, delObjs, map, subStage);
        }

        String[] deletion = new String[delObjs.size()];
        for (int i = 0; i < delObjs.size(); i++) {
            deletion[i] = delObjs.get(i);
        }

        if (deletion != null && deletion.length > 0)
            DomainObject.deleteObjects(context, deletion);

        if (message.length() > 0)
            message.append(" have a DEPSubStage!\n");

        if (messageDel.length() > 0)
            message.append(messageDel).append(" were deleted!");

        mapMessage.put("message", message.toString());
        return mapMessage;
    }

    private void getMessage(StringBuffer message, StringBuffer messageDel, ArrayList<String> delObjs, Map map, String subStage) {
        if (subStage != null && (subStage).contains("TRUE")) {
            if (message.length() > 0)
                message.append(", ");
            message.append(map.get(DomainObject.SELECT_NAME));
        } else {
            if (messageDel.length() > 0)
                messageDel.append(", ");
            messageDel.append(map.get(DomainObject.SELECT_NAME));
            delObjs.add((String) map.get(DomainObject.SELECT_ID));
        }
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

        ArrayList<String> delObjs = new ArrayList();
        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            String tasks = (String) (map.get("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "]"));
            getMessage(message, messageDel, delObjs, map, tasks);
        }


        String[] deletion = new String[delObjs.size()];
        for (int i = 0; i < delObjs.size(); i++) {
            deletion[i] = delObjs.get(i);
        }

        if (deletion != null && deletion.length > 0)
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
     * @param context
     * @param args
     * @return Integer
     * @throws Exception
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
                    message.append("\n" + getSetsNameLevelDep.get(i));
                }
            }
            if (message.length() > 0) {
                message.append(EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.hasAcceptedDEP"));
            }

            for (int i = 0; i < getSetsFromStatus.size(); i++) {
                if ("draft".equalsIgnoreCase((String) (getSetsFromStatus.get(i)))) {
                    message.append("\n" + getSetsFromName.get(i));
                    message.append(" - " + getSetsFromStatus.get(i));
                }
            }

            for (int i = 0; i < getSetsToStatus.size(); i++) {
                if ("Draft".equals((getSetsToStatus.get(i)))) {
                    message.append("\n" + getSetsToName.get(i));
                    message.append(" - " + getSetsToStatus.get(i));
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
     * @param context
     * @param args
     * @return
     * @throws Exception
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
