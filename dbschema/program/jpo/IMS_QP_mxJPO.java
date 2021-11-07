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
import java.util.stream.Collectors;
import java.util.stream.Stream;


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
                    append(IMS_QP_Constants_mxJPO.type_IMS_QP_DEPTask).append(",")

                    //by issue #51753
                    .append(IMS_QP_Constants_mxJPO.type_IMS_QP_Reports).append(",");

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
                    append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPSubStage2DEPTask).append(",")

                    //by issue #51753
                    .append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_Project2Reports).append(",");

            DomainObject domainObject = new DomainObject(objectId);

            StringList objectSelects = new StringList(3);
            objectSelects.add(DomainConstants.SELECT_ID);
            objectSelects.add(DomainConstants.SELECT_TYPE);
            objectSelects.add(DomainConstants.SELECT_NAME);

            StringList relSelects = new StringList(
                    DomainConstants.SELECT_RELATIONSHIP_ID);

            componentsList = domainObject.getRelatedObjects(
                    context, // matrix context
                    sbRel.toString(), // all relationships to expand
                    sbType.toString(), // all types required from the expand
                    objectSelects,// object selects
                    relSelects, // relationship selects
                    false, // to direction
                    true, // from direction
                    (short) 1, // recursion level
                    getWhere(context, domainObject), // object where clause
                    "", // relationship where clause
                    0);

        } catch (Exception ex) {
            throw new FrameworkException(ex.toString());
        }

        for (Object o : componentsList) {
            Map map = (Map) o;
            String name = (String) map.get(DomainConstants.SELECT_NAME);
            if (name.equals("Reports")) {
                map.put(DomainConstants.SELECT_TYPE, "");
            }
        }

        return componentsList;
    }

    private MapList getFilteredMapListByOwner(Context context, MapList mapList) {

        if (isAdminOrSuperOrViewer(context)) return mapList;

        MapList filteredByOwnerSQPs = new MapList();
        for (Object o : mapList) {
            Map map = (Map) o;
            boolean isOwner = false;
            try {
                isOwner = IMS_QP_Security_mxJPO.isOwnerQPlan(context, (String) map.get(DomainConstants.SELECT_ID)) ||
                        IMS_QP_Security_mxJPO.isOwnerDepFromQPlan(context, (String) map.get(DomainConstants.SELECT_ID));
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
        Map map = JPO.unpackArgs(args);
        Map paramMap = (Map) map.get("paramMap");
        String objectId = (String) paramMap.get("objectId");
        StringList select = new StringList(DomainConstants.SELECT_REVISION);
        select.add(DomainConstants.SELECT_TYPE);
        select.add(DomainConstants.SELECT_NAME);
        Map mapInfo = (new DomainObject(objectId)).getInfo(context, select);
        return mapInfo.get(DomainConstants.SELECT_NAME).toString();
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
                bArr[i] = (String) ((Map) ObjectList.get(i)).get(DomainConstants.SELECT_ID);
            }

            // Get the required information for the objects.
            BusinessObjectWithSelectList bwsl = BusinessObject.getSelectBusinessObjectData(context, bArr, bSel);

            for (int i = 0; i < ObjectList.size(); i++) {
                String currentObjectid = (String) ((Map) ObjectList.get(i)).get(DomainConstants.SELECT_ID);
                // get the current state value
                String sPolicyClassification = bwsl.getElement(i).getSelectData("policy.property[PolicyClassification].value");
                String type = bwsl.getElement(i).getSelectData(DomainConstants.SELECT_TYPE);

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

        String typePattern = IMS_QP_Constants_mxJPO.type_IMS_DisciplineCode;
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
            fieldRangeValues.add(map.get(DomainConstants.SELECT_ID));
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

        String typePattern = IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP;
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

        StringBuilder whereStringBuilder = new StringBuilder("");
        if (context.isAssigned(IMS_QP_Security_mxJPO.ROLE_IMS_QP_Viewer)) {
            if (whereStringBuilder.length() > 0) {
                whereStringBuilder.append("&&");
            }

            //tree without external initial objects
            whereStringBuilder
                    .append("name nsmatch '*ExternalInitialData*'");
        }

        MapList allDEPs = new MapList();
        try {
            allDEPs = DomainObject.findObjects(context,
                    /*type*/ IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP,
                    /*vault*/IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    /*where*/whereStringBuilder.toString(),
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

        MapList result = new MapList();
        try {
//            commented by issue #66293
//            result = findObjects(context, IMS_QP_Constants_mxJPO.SYSTEM_TYPES, "*", "revision==last",
            result = findObjects(context, IMS_QP_Constants_mxJPO.SYSTEM_TYPES, "*", null,
                    new StringList(DomainConstants.SELECT_ID));
        } catch (FrameworkException frameworkException) {
            frameworkException.printStackTrace();
        }
        return result;
    }

    /**
     * It's retrieve KKS and PBS for table DEP KKS PBS
     */
    public MapList getPBSByQPlan(Context context, String[] args) {
        String parentID = "";
        try {
            Map argsMap = JPO.unpackArgs(args);
            parentID = (String) argsMap.get("parentOID");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String functionalAreaID = "";
        DomainObject functionalAreaObject = null;
        try {
            DomainObject qpPlan = new DomainObject(parentID);
            functionalAreaID = qpPlan.getInfo(context, "from[IMS_QP_QPlan2Object].to.id");
            functionalAreaObject = new DomainObject(functionalAreaID);
        } catch (Exception e) {
            LOG.error("error getting info from qpTask " + parentID + ": " + e.getMessage());
            e.printStackTrace();
        }

        StringList objectSelects = new StringList();
        objectSelects.add(DomainConstants.SELECT_ID);
        objectSelects.add(DomainConstants.SELECT_NAME);

        MapList result = new MapList();
        if (UIUtil.isNotNullAndNotEmpty(functionalAreaID)) {
            try {
                String types = new StringBuilder()
                        .append(IMS_QP_Constants_mxJPO.type_IMS_PBSSystem)
                        .append(",")
                        .append(IMS_QP_Constants_mxJPO.type_IMS_GBSBuilding)
                        .toString();

                result = functionalAreaObject.getRelatedObjects(context,
                        /*relationship*/ null,
                        /*type*/ types,
                        /*object attributes*/ objectSelects,
                        /*relationship selects*/ null,
                        /*getTo*/false,/*getFrom*/ true,
                        /*recurse level*/(short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/0);
            } catch (FrameworkException frameworkException) {
                LOG.error("Framework exception: " + frameworkException.getMessage());
                frameworkException.printStackTrace();
            }
        } else {
            result = getAllPBS(context, null);
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

                String pbsID = (String) map.get(DomainConstants.SELECT_ID);
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
            e.printStackTrace();
            LOG.error("error getting url string: " + e.getMessage());
        }

        return result;
    }

    public Vector getQP(Context context, String... args) {
        Vector result = new Vector();
        StringBuilder stringBuilder = new StringBuilder();

        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception exception) {
            exception.printStackTrace();
            LOG.error("error unpack args: " + exception.getMessage());
        }
        MapList objectList = (MapList) argsMap.get("objectList");

        List<Map> items = new ArrayList<>();
        for (Object o : objectList) {
            items.add((Map) o);
        }

        String[] ODI = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            Map map = items.get(i);
            ODI[i] = (String) map.get(DomainConstants.SELECT_ID);
        }

        StringList selectBusStmts = new StringList(4);
        selectBusStmts.add(DomainConstants.SELECT_TYPE);
        selectBusStmts.add(DomainConstants.SELECT_NAME);
        selectBusStmts.add(DomainConstants.SELECT_REVISION);
        selectBusStmts.add(DomainConstants.SELECT_ID);
        selectBusStmts.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object + "]");

        StringList selectRelStmts = new StringList();
        selectRelStmts.add(DomainConstants.SELECT_NAME);
        selectRelStmts.add(DomainConstants.SELECT_FROM_TYPE);
        selectRelStmts.add("from.current");
        selectRelStmts.add("from.to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask
                + "].from.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object + "]");
        selectRelStmts.add("from.to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.current");
        short recurse = 1;

        BusinessObjectWithSelectList businessObjectWithSelectList = null;
        try {
            businessObjectWithSelectList = BusinessObject.getSelectBusinessObjectData(context, ODI, selectBusStmts);
        } catch (MatrixException matrixException) {
            matrixException.printStackTrace();
            LOG.error("error getting business objects list: " + matrixException.getMessage());
        }

        for (Object o : businessObjectWithSelectList) {
            stringBuilder.setLength(0);
            BusinessObjectWithSelect bowsSystem = (BusinessObjectWithSelect) o;
            try {
                bowsSystem.open(context);
            } catch (MatrixException e) {
                System.out.println("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }

            boolean foundPlan = false;
            String hasQPlan = bowsSystem.getSelectData("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object + "]");

            if ("TRUE".equals(hasQPlan)) {
                ExpansionWithSelect expansion = null;
                try {
                    expansion = bowsSystem.expandSelect(context,
                            /*rel type*/ IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object, /*obj type*/"*",
                            selectBusStmts, selectRelStmts, /*getTo*/true,/*getFrom*/false, recurse);
                } catch (MatrixException e) {
                    System.out.println("matrix error: " + e.getMessage());
                    e.printStackTrace();
                }

                // getting relationships
                RelationshipWithSelectList relationshipWithSelectList = expansion.getRelationships();
                RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(relationshipWithSelectList);

                while (relItr.next()) {
                    RelationshipWithSelect relSelect = relItr.obj();
                    String relationshipName = relSelect.getSelectData(DomainConstants.SELECT_NAME);
                    String relationshipFromType = relSelect.getSelectData(DomainConstants.SELECT_FROM_TYPE);
                    String relationshipStraightFromCurrent = relSelect.getSelectData("from.current");
                    String relationshipFromInterdisciplinaryTask = relSelect.getSelectData(
                            "from.to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask
                                    + "].from.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object + "]");
                    String relationshipFromTaskCurrent = relSelect.getSelectData("from.to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.current");

                    if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan.equals(relationshipFromType)) {
                        result.addElement(relationshipStraightFromCurrent);
                        foundPlan = true;
                        break;

                    } else if (foundPlan &&
                            IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(relationshipFromType)) {
                        if ("TRUE".equals(relationshipFromInterdisciplinaryTask)) {
                            result.addElement(relationshipFromTaskCurrent);
                            foundPlan = true;
                            break;
                        }
                    }
                }
            }
            if (!foundPlan) {
                result.addElement("No plan");
            }
        }

        return result;
    }

    public String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * Method to show the table of related with DEP-object IMS_QP_DEPTask elements
     */
    public MapList getAllRelatedTasksForTable(Context context, String[] args) throws Exception {

        MapList result = new MapList();
        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");

        StringList selects = new StringList(DomainConstants.SELECT_ID);

        //get all tasks
        Map depMap = new HashMap<>();
        depMap.put(DomainConstants.SELECT_TYPE, IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP);
        depMap.put(DomainConstants.SELECT_ID, objectId);
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
        selects.add(DomainConstants.SELECT_ID);

        DomainObject parent = new DomainObject(objectId);

        MapList listSQPs = parent.getRelatedObjects(context,
                /*relationship*/null,
                /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan,
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 1,
                /*object where*/ getWhere(context, parent),
                /*relationship where*/ null,
                /*limit*/ 0);

        //get all QP plans filtered by owner
//        listSQPs = getFilteredMapListByOwner(context, listSQPs);

        return listSQPs;
    }

    public MapList getAllRelatedObjects(Context context, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args: " + e.getMessage());
            e.printStackTrace();
        }

        //get objectID
        String objectId = (String) argsMap.get("objectId");
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);

        DomainObject parent = null;
        try {
            parent = new DomainObject(objectId);
        } catch (Exception e) {
            LOG.error("error getting domain object: " + objectId + ": " + e.getMessage());
            e.printStackTrace();
        }

        MapList listObjects = new MapList();
        try {
            listObjects = parent.getRelatedObjects(context,
                    /*relationship*/DomainConstants.QUERY_WILDCARD,
                    /*type*/DomainConstants.QUERY_WILDCARD,
                    /*object attributes*/ selects,
                    /*relationship selects*/ null,
                    /*getTo*/ true, /*getFrom*/ true,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ null,
                    /*relationship where*/ null,
                    /*limit*/ 0);
        } catch (FrameworkException e) {
            LOG.error("error getting related objects from: " + objectId + "  with message: " + e.getMessage());
            e.printStackTrace();
        }
        return listObjects;
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
        Map argsMap = null;
        List<String> warnMessageList = new ArrayList<>();

        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error when unpacking arguments: " + e.getMessage());
            e.printStackTrace();
        }

        String[] emxParentIds = null, emxTableRowId = null;
        try {
            emxParentIds = (String[]) argsMap.get("emxParentIds");
        } catch (NullPointerException nullPointerException) {
            LOG.error("error: " + nullPointerException.getMessage());
            nullPointerException.printStackTrace();
        }
        try {
            emxTableRowId = (String[]) argsMap.get("emxTableRowId");
        } catch (NullPointerException nullPointerException) {
            LOG.error("error: " + nullPointerException.getMessage());
        }

        boolean copyBySystemFlag = false;
        if (emxParentIds != null) {
            emxParentIds = emxParentIds[0].split("~");
            copyBySystemFlag = true;
        }

        List<String> cleanSystemIds = new ArrayList<>(), cleanTaskIds = new ArrayList<>();
        if (copyBySystemFlag) {
            cleanTaskIds.addAll(cleanIdsUtility(emxParentIds));
            cleanSystemIds.addAll(cleanIdsUtility(emxTableRowId));
        } else {
            String rawTableRowId = emxTableRowId[0];
            emxTableRowId = rawTableRowId.split("\",\"");
            cleanTaskIds.addAll(cleanIdsUtility(emxTableRowId));
        }

        //copying all ids of objects with new names and not including files
        for (int i = 0; i < (cleanSystemIds.size() > 0 ? cleanSystemIds.size() : 1); i++) {
            for (String id : cleanTaskIds) {
                try {
                    DomainObject parentTask = new DomainObject(id);
                    String name = parentTask.getName(context), type = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;
                    MapList mapList = DomainObject.findObjects(context,
                            /*type*/ type,
                            /*vault*/ IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                            /*where*/"name smatch '*" + name + "*'",
                            /*selects*/new StringList(DomainConstants.SELECT_ID));

                    //mapList size isn't smaller than 1
                    int counter = mapList.size();
                    String prefix = copyBySystemFlag ?
                            new DomainObject(cleanSystemIds.get(i)).getInfo(context, DomainConstants.SELECT_NAME) + "_" : "";
                    String postfix = counter < 10 ? "_0" + counter : "_" + counter;
                    String rename = String.format("%s%s", name, postfix);

                    //Transactional
                    ContextUtil.startTransaction(context, true);
                    boolean commitingFlag = true;

                    DomainObject targetTask = new DomainObject();
                    targetTask.createObject(context,
                            /*type*/type,
                            /*name*/rename,
                            /*revision*/"",
                            /*policy*/type,
                            /*vault*/ context.getVault().getName());
                    targetTask.setAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_Name,
                            parentTask.getAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_Name));
                    targetTask.setAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu,
                            parentTask.getAttributeValue(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NameRu));

                    boolean hasSystemRelationship = copyRelationshipsOfTask(context, id, targetTask, prefix, postfix);
                    if (copyBySystemFlag && !hasSystemRelationship) {
                        DomainRelationship.connect(context,/*from*/targetTask, "IMS_QP_QPlan2Object",/*to*/new DomainObject(cleanSystemIds.get(i)));
                        targetTask.setName(context, prefix + rename);
                    } else if (copyBySystemFlag && hasSystemRelationship) {
                        ContextUtil.abortTransaction(context);
                        warnMessageList.add(rename);
                        LOG.info("transaction aborted: rel detected!");
                        commitingFlag = false;
                    }

                    if (commitingFlag) {
                        ContextUtil.commitTransaction(context);
                        LOG.info("new task is name: " + rename + " commited now");
                    }

                } catch (Exception e) {
                    LOG.error("error when coping: " + e.getMessage());
                    ContextUtil.abortTransaction(context);
                }
            }
        }

        mapMessage.put("warning", warnMessageList);
        mapMessage.put("message", warnMessageList.size() > 0 ?
                "Warning! Tasks doesn't copied: " : cleanSystemIds.size() > 1 ?
                "Objects copied" : "Object copied");
        return mapMessage;
    }

    /**
     * Method to cleaning strings of rel id|kind object id|related object|row id[0,0]
     * simplify to object id only
     *
     * @param rawIds raw ids array
     * @return cleaned list of ids
     */
    private List<String> cleanIdsUtility(String[] rawIds) {
        List<String> cleanIds = new ArrayList<>();
        for (String s : rawIds) {
            s = s.substring(s.indexOf("|") + 1, s.lastIndexOf("|"));
            s = s.substring(0, s.lastIndexOf("|"));
            cleanIds.add(s);
        }

        return cleanIds;
    }

    /**
     * @param context      usual parameter
     * @param id           is parent object id
     * @param targetObject copied object
     * @param postfix      last piece of name for copying object name
     * @throws Exception any errors thrown by the method
     */
    private boolean copyRelationshipsOfTask(Context context, String id, DomainObject targetObject,
                                            String prefix, String postfix) {
        boolean hasSystemRelationship = false;

        // Instantiate the BusinessObject.
        StringList selectBusStmts = new StringList();
        selectBusStmts.addElement(DomainConstants.SELECT_ID);
        selectBusStmts.addElement(DomainConstants.SELECT_TYPE);
        selectBusStmts.addElement(DomainConstants.SELECT_NAME);

        StringList selectRelStmts = new StringList();
        selectRelStmts.addElement(DomainConstants.SELECT_NAME);
        selectRelStmts.addElement(DomainConstants.SELECT_TO_ID);
        selectRelStmts.addElement(DomainConstants.SELECT_FROM_ID);
        short recurse = 1;

        BusinessObject parentObject = null;
        try {
            parentObject = new BusinessObject(id);
            parentObject.open(context);
        } catch (MatrixException matrixException) {
            LOG.error("error when opening Business Object: " + matrixException.getMessage());
            matrixException.printStackTrace();
        }

        // Getting the expansion
        ExpansionWithSelect expansion = null;
        try {
            expansion = parentObject.expandSelect(context, "*", "*", selectBusStmts, selectRelStmts, true, true, recurse);
            parentObject.close(context);
        } catch (MatrixException matrixException) {
            LOG.error("error getting expansion and closing Business Object: " + matrixException.getMessage());
            matrixException.printStackTrace();
        }
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
            String relationshipType = relSelect.getSelectData(DomainConstants.SELECT_NAME);
            if (relationshipType.equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object)) {
                hasSystemRelationship = true;
                prefix = "";
            }

            //ignoring type of QPTask2QPTask relationships
            if (relationshipType.equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask)) continue;

            //if expected result type
            boolean from = relSelect.getSelectData(DomainConstants.SELECT_FROM_ID).equals(id) ? true : false;

            if (relationshipType.equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask)) {
                DomainObject expectedResult = null, clonedExpectedResult = null;

                try {
                    expectedResult = new DomainObject(relSelect.getSelectData((from ? "to" : "from") + ".id"));
                    String expectedResultRename = String.format("%S%S%S", prefix, expectedResult.getName(context), postfix);

                    //copied expected result
                    clonedExpectedResult = new DomainObject();
                    clonedExpectedResult.createObject(context,
                            /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                            /*name*/expectedResultRename,
                            /*revision*/"",
                            /*policy*/IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                            /*vault*/ context.getVault().getName());
                } catch (Exception e) {
                    LOG.error("error creating Domain Object: " + e.getMessage());
                }

                //coping all attributes
                try {
                    copyAttributes(context, expectedResult, clonedExpectedResult);
                } catch (MatrixException matrixException) {
                    LOG.error("error copying attributes: " + matrixException.getMessage());
                }

                String resultTypeID = null;
                DomainObject resultType = null;
                try {
                    resultTypeID = expectedResult.getInfo(context, "to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult + "].from.id");
                    resultType = new DomainObject(resultTypeID);
                } catch (Exception e) {
                    LOG.error("error getting Domain Object: " + resultTypeID + " message: " + e.getMessage());
                }

                try {
                    if (IMS_QP_Constants_mxJPO.type_IMS_Family.equals(resultType.getType(context))) {
                        clonedExpectedResult.connect(context, new RelationshipType(IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult), /*from clone*/ false, resultType);
                    } else {
                        emxContextUtil_mxJPO.mqlWarning(context, "Expected result " + expectedResult.getName(context) + " doesn't have connection from IMS_Family. Check this!");
                        throw new FrameworkException("Expected result doesn't have connection from IMS_Family. Check this!");
                    }
                } catch (FrameworkException frameworkException) {
                    LOG.error("error getting attribute IMS_Family: " + resultTypeID + " message: " + frameworkException.getMessage());
                } catch (MatrixException matrixException) {
                    LOG.error("error connecting expected result to type: " + clonedExpectedResult + " to " + resultTypeID + " message: " + matrixException.getMessage());
                } catch (Exception e) {
                    LOG.error("other error: " + e.getMessage());
                }

                //connect copied Expected result from/to targetObject
                try {
                    clonedExpectedResult.connect(context, new RelationshipType(relationshipType), /*from clone*/ !from, targetObject);
                } catch (MatrixException matrixException) {
                    LOG.error("error connecting " + clonedExpectedResult + " !from: " + !from + " " + targetObject);
                }
            } else {
                //another connections
                if (IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact.equals(relationshipType)) continue;
                String relationshipRoute = DomainConstants.SELECT_FROM_ID;
                if (IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2Object.equals(relationshipType)) {
                    relationshipRoute = DomainConstants.SELECT_TO_ID;
                }
                try {
                    DomainObject anotherObject = new DomainObject(relSelect.getSelectData(relationshipRoute));
                    anotherObject.connect(context, new RelationshipType(relationshipType), !from, targetObject);
                } catch (FrameworkException frameworkException) {
                    LOG.error("error getting attribute IMS_Family: " + relSelect.getSelectData(DomainConstants.SELECT_FROM_ID) + " to " + targetObject + "| message: " + frameworkException.getMessage());
                } catch (Exception e) {
                    LOG.error("other error: " + e.getMessage());
                }
            }
        }
        return hasSystemRelationship;
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
    public Map deleteQPlan(Context ctx, String[] args) throws Exception {

        StringBuffer message = new StringBuffer();
        StringBuffer messageDel = new StringBuffer();
        Map mapMessage = new HashMap();

        Map argsMap = JPO.unpackArgs(args);
        LOG.info("deleteQPlanargsMap: " + argsMap);
        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");

        StringList selects = new StringList();
        selects.add(DomainObject.SELECT_ID);
        selects.add(DomainObject.SELECT_NAME);
        selects.add(DomainObject.SELECT_OWNER);
        selects.add("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "]");

        MapList objectsInfo = DomainObject.getInfo(ctx, rowIDs, selects);

        String objectId = (String) argsMap.get("objectId");
        String type = "", name = "";
        if (UIUtil.isNotNullAndNotEmpty(objectId)) {
            DomainObject object = new DomainObject(objectId);
            type = object.getType(ctx);
            name = object.getName(ctx);
        }

        StringBuilder aqpOwnerMessage = new StringBuilder();
        if (IMS_QP_Constants_mxJPO.type_IMS_QP.equals(type) && "AQP".equals(name)) {
            MapList filteredByOwnerForAQP = new MapList();

            objectsInfo.forEach((o) -> {
                Map map = (Map) o;
                LOG.info("map: " + map);
                boolean isDepOwner = false;
                try {
                    isDepOwner = IMS_QP_Security_mxJPO.isOwnerDepFromQPlan(ctx, (String) map.get(DomainConstants.SELECT_ID));
                } catch (FrameworkException e) {
                    e.printStackTrace();
                }
                if (!isDepOwner && !IMS_QP_Security_mxJPO.isUserAdminOrSuper(ctx) && ctx.getUser().equals(map.get(DomainObject.SELECT_OWNER))) {
                    aqpOwnerMessage.append("\nAQP detected: originator can't delete its own plan: " + map.get(DomainObject.SELECT_NAME));
                } else {
                    filteredByOwnerForAQP.add(map);
                }
            });
            LOG.info("objectInfo before: " + objectsInfo.size());
            objectsInfo = filteredByOwnerForAQP;
            LOG.info("objectInfo after: " + objectsInfo.size());
        }

        List<String> delObjs = new ArrayList<>();
        objectsInfo.forEach((o) -> {
            Map map = (Map) o;
            String tasks = (String) (map.get("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "]"));
            getMessage(message, messageDel, delObjs, map, tasks);
        });

        String[] deletion = new String[delObjs.size()];
        for (int i = 0; i < delObjs.size(); i++) {
            deletion[i] = delObjs.get(i);
        }

        if (deletion.length > 0)
            DomainObject.deleteObjects(ctx, deletion);

        if (message.length() > 0)
            message.append(" have a task!\n");

        if (messageDel.length() > 0)
            message.append(messageDel).append(" was deleted!");


        message.append(aqpOwnerMessage);
        mapMessage.put("message", message.toString());
        return mapMessage;
    }

    public Boolean checkAccess(Context context,
                               String[] args
    ) throws Exception {
        Map programMap = JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");

        return IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, objectId);
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
        DomainObject domainObjectTo = new DomainObject(new BusinessObject(IMS_QP_Security_mxJPO.TYPE_Person, context.getUser(), "-", context.getVault().getName()));
        boolean isConnectedOwner = IMS_KDD_mxJPO.isConnected(context, IMS_QP_Security_mxJPO.RELATIONSHIP_IMS_QP_DEP2Owner, /*from*/ domainObjectFrom, /*to*/ domainObjectTo);
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
                message.append(EnoviaResourceBundle.getProperty(context,
                        "IMS_QP_FrameworkStringMessages",
                        context.getLocale(),
                        "IMS_QP_Framework.Message.hasAcceptedDEP"));
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
                message.append(EnoviaResourceBundle.getProperty(context,
                        "IMS_QP_FrameworkStringMessages",
                        context.getLocale(),
                        "IMS_QP_Framework.Message.haveNoApproved"));
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
        if (IMS_KDD_mxJPO.isConnected(context, IMS_QP_Security_mxJPO.RELATIONSHIP_IMS_QP_DEP2Owner,
                new DomainObject(args[0]), new DomainObject(new BusinessObject(IMS_QP_Security_mxJPO.TYPE_Person, context.getUser(), "-", context.getVault().getName())))) {
            emxContextUtil_mxJPO.mqlWarning(context, EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.noPermissions"));
            return 1;
        }
        return 0;
    }

    private boolean isAdminOrSuperOrViewer(Context context) {
        boolean result = false;
        try {
            result = context.isAssigned(IMS_QP_Security_mxJPO.ROLE_IMS_Admin)
                    || context.isAssigned(IMS_QP_Security_mxJPO.ROLE_IMS_QP_SuperUser)
                    || context.isAssigned(IMS_QP_Security_mxJPO.ROLE_IMS_QP_Viewer);
        } catch (MatrixException e) {
            e.printStackTrace();
        }
        return result;
    }


    private String getWhere(Context ctx, DomainObject domainObject) {
        String objectType = null;
        String objectName = null;
        try {
            objectType = domainObject.getType(ctx);
            objectName = domainObject.getName(ctx);
        } catch (FrameworkException e) {
            LOG.error("framework exception: " + e.getMessage());
            e.printStackTrace();
        }

        boolean isViewer = IMS_QP_Security_mxJPO.isUserViewerWithChild(ctx);

        StringBuilder whereStringBuilder = new StringBuilder("");

        /**
         * For `AQP` OR `SQP` level
         * To show all objects of type `IMS_QP_QPlan` only if user has roles: `IMS_Admin` or `IMS_QP_SuperUser` or `IMS_QP_Viewer`
         * To show those objects of type `IMS_QP_QPlan` whose owner are
         */
        if (!isViewer && !IMS_QP_Security_mxJPO.isUserAdminOrSuper(ctx) && IMS_QP_Constants_mxJPO.type_IMS_QP.equals(objectType)) {

            if ("AQP".equals(objectName)) {
                whereStringBuilder
                        .append("owner==" + ctx.getUser())
                        .append("||")
                        .append("to[IMS_QP_DEP2QPlan].from.")
                        .append("from[IMS_QP_DEP2Owner].to.name==" + ctx.getUser());
            }

            if ("SQP".equals(objectName)) {
                whereStringBuilder
                        //не интердисциплинари: pbs2owner тоже
                        .append("(to[IMS_QP_DEP2QPlan].from.attribute[IMS_QP_InterdisciplinaryDEP]==FALSE")
                        .append("&&")
                        .append("(from[IMS_QP_QPlan2Object].to.from[IMS_PBS2Owner].to.name==").append(ctx.getUser())
                        .append("||")
                        .append("to[IMS_QP_DEP2QPlan].from.from[IMS_QP_DEP2Owner].to.name==").append(ctx.getUser())
                        .append("))")
                        .append("||")
                        .append("(").append("to[IMS_QP_DEP2QPlan].from.attribute[IMS_QP_InterdisciplinaryDEP]==TRUE")
                        .append("&&").append("to[IMS_QP_DEP2QPlan].from.from[IMS_QP_DEP2Owner].to.name==").append(ctx.getUser()).append(")");
            }
        }

        /**
         * Users who have `IMS_QP_Viewer` type roles can not see `ExternalInitialData` names objects
         */

        if (isViewer &&
                IMS_QP_Constants_mxJPO.type_IMS_QP.equals(objectType)) {
            if (whereStringBuilder.length() > 0) {
                whereStringBuilder.append("&&");
            }

            //tree without external initial objects
            whereStringBuilder
                    .append("name nsmatch '*ExternalInitialData*'");
        }

        return whereStringBuilder.toString();
    }
}
