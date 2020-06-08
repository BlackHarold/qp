import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import java.util.*;


public class IMS_QP_mxJPO extends DomainObject {

    static final String type_IMS_QP = "IMS_QP";
    static final String type_IMS_QP_Directory = "IMS_QP_Directory";
    static final String type_IMS_QP_ResultType = "IMS_QP_ResultType";
    static final String type_IMS_Baseline = "IMS_Baseline";
    static final String type_IMS_ProjectStage = "IMS_ProjectStage";
    static final String type_IMS_DisciplineCode = "IMS_DisciplineCode";
    static final String type_IMS_QP_DEP = "IMS_QP_DEP";
    static final String type_IMS_QP_QPlan = "IMS_QP_QPlan";
    static final String type_IMS_QP_DEPProjectStage = "IMS_QP_DEPProjectStage";
    static final String type_IMS_QP_DEPSubStage = "IMS_QP_DEPSubStage";
    static final String type_IMS_QP_DEPTask = "IMS_QP_DEPTask";
    static final String type_IMS_Family = "IMS_Family";
    static final String type_IMS_PBSSystem = "IMS_PBSSystem";
    static final String type_IMS_GBSBuilding = "IMS_GBSBuilding";
    static final String type_IMS_PBSFunctionalArea = "IMS_PBSFunctionalArea";

    static final String relationship_IMS_QP_Project2QP = "IMS_QP_Project2QP";
    static final String relationship_IMS_QP_Project2Directory = "IMS_QP_Project2Directory";
    static final String relationship_IMS_QP_Directory2ResultType = "IMS_QP_Directory2ResultType";
    static final String relationship_IMS_QP_Directory2ProjectStage = "IMS_QP_Directory2ProjectStage";
    static final String relationship_IMS_QP_Directory2Baseline = "IMS_QP_Directory2Baseline";
    static final String relationship_IMS_QP_Directory2DisciplineCode = "IMS_QP_Directory2DisciplineCode";
    static final String relationship_IMS_QP_Directory2Directory = "IMS_QP_Directory2Directory";
    static final String relationship_IMS_QP_Directory2PBSSystem = "IMS_QP_Directory2PBSSystem";
    static final String relationship_IMS_QP_Directory2GBSBuilding = "IMS_QP_Directory2GBSBuilding";
    static final String relationship_IMS_QP_Directory2PBSFunctionalArea = "IMS_QP_Directory2PBSFunctionalArea";
    static final String relationship_IMS_QP_QP2DEP = "IMS_QP_QP2DEP";
    static final String relationship_IMS_QP_DEP2DEPProjectStage = "IMS_QP_DEP2DEPProjectStage";
    static final String relationship_IMS_QP_DEPProjectStage2DEPSubStage = "IMS_QP_DEPProjectStage2DEPSubStage";
    static final String relationship_IMS_QP_DEPSubStage2DEPTask = "IMS_QP_DEPSubStage2DEPTask";
    static final String relationship_IMS_QP_ResultType2Family = "IMS_QP_ResultType2Family";
    static final String relationship_IMS_QP_QP2QPlan = "IMS_QP_QP2QPlan";
    static final String relationship_IMS_QP_QPlan2QPTask = "IMS_QP_QPlan2QPTask";

    public IMS_QP_mxJPO(Context context, String[] args) throws Exception {
        super();
        if (args != null && args.length > 0) {
            setId(args[0]);
        }
    }

    public static MapList getStructureList(Context context,
                                           String[] args
    ) throws FrameworkException {
        MapList componentsList = null;
        try {
            HashMap programMap = JPO.unpackArgs(args);
            HashMap paramMap = (HashMap) programMap.get("paramMap");
            String objectId = (String) paramMap.get("objectId");

            StringBuffer sbType = new StringBuffer().
                    append(type_IMS_QP).append(",").
                    append(type_IMS_QP_DEP).append(",").
                    append(type_IMS_Baseline).append(",").
                    append(type_IMS_ProjectStage).append(",").
                    append(type_IMS_QP_Directory).append(",").
                    append(type_IMS_QP_ResultType).append(",").
                    append(type_IMS_QP_QPlan).append(",").
                    append(type_IMS_Family).append(",").
                    append(type_IMS_DisciplineCode).append(",").
                    append(type_IMS_QP_DEPProjectStage).append(",").
                    append(type_IMS_QP_DEPSubStage).append(",").
                    append(type_IMS_QP_DEPTask);

            StringBuffer sbRel = new StringBuffer().
                    append(relationship_IMS_QP_Project2QP).append(",").
                    append(relationship_IMS_QP_Project2Directory).append(",").
                    append(relationship_IMS_QP_Directory2ResultType).append(",").
                    append(relationship_IMS_QP_ResultType2Family).append(",").
                    append(relationship_IMS_QP_Directory2Baseline).append(",").
                    append(relationship_IMS_QP_Directory2ProjectStage).append(",").
                    append(relationship_IMS_QP_Directory2DisciplineCode).append(",").
                    append(relationship_IMS_QP_Directory2Directory).append(",").
                    append(relationship_IMS_QP_QP2DEP).append(",").
                    append(relationship_IMS_QP_DEP2DEPProjectStage).append(",").
                    append(relationship_IMS_QP_DEPProjectStage2DEPSubStage).append(",").
                    append(relationship_IMS_QP_DEPSubStage2DEPTask);

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
    public String getDisplayNameForNavigator(Context context,
                                             String[] args
    ) throws Exception {

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


    public void postProcess_IMS_QP(Context context,
                                   String[] args
    ) throws Exception {

        Map argsMap = JPO.unpackArgs(args);
        Map requestMap = (Map) argsMap.get("requestMap");
        Map paramMap = (Map) argsMap.get("paramMap");

        String disciplineCodeID = (String) requestMap.get("DisciplineCode");
        String qpId = (String) requestMap.get("objectId");
        String depId = (String) paramMap.get("newObjectId");

        DomainObject disciplineCodeObj = new DomainObject(disciplineCodeID);
        DomainObject depObj = new DomainObject(depId);
        DomainObject qpObj = new DomainObject(qpId);
        String depNewName = qpObj.getName(context) + "-" + disciplineCodeObj.getInfo(context, "attribute[IMS_ShortName]");
        depObj.setName(context, depNewName);

        DomainRelationship.connect(context, disciplineCodeObj, "IMS_QP_Discipline2DEP", depObj);
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
    public Map deleteDEP(Context context,
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
        selects.add("from[" + relationship_IMS_QP_DEP2DEPProjectStage + "].to." +
                "from[" + relationship_IMS_QP_DEPProjectStage2DEPSubStage + "]");

        MapList objectsInfo = DomainObject.getInfo(context, ids, selects);

        ArrayList<String> delObjs = new ArrayList();
        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            String subStage = (String) (map.get("from[" + relationship_IMS_QP_DEP2DEPProjectStage + "].to." +
                    "from[" + relationship_IMS_QP_DEPProjectStage2DEPSubStage + "]"));
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
        selects.add("from[" + relationship_IMS_QP_QPlan2QPTask + "]");

        MapList objectsInfo = DomainObject.getInfo(context, ids, selects);

        ArrayList<String> delObjs = new ArrayList();
        for (int i = 0; i < objectsInfo.size(); i++) {
            Map map = (Map) objectsInfo.get(i);
            String tasks = (String) (map.get("from[" + relationship_IMS_QP_QPlan2QPTask + "]"));
            if (tasks != null && (tasks).contains("TRUE")) {
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

        String user = context.getContext().get_user();

        Map programMap = JPO.unpackArgs(args);
        String objId = (String) programMap.get("objectId");

        IMS_QP_Security_mxJPO security = new IMS_QP_Security_mxJPO(context, args);

        return security.personNameIsDEPOwner(context, user, objId);
    }
}