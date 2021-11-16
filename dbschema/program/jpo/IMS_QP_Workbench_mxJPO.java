import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_Workbench_mxJPO {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public MapList findDEPsForBenchTable(Context ctx, String... args) throws MatrixException {

        StringBuilder whereStringBuilder = new StringBuilder("");
        if (ctx.isAssigned(IMS_QP_Security_mxJPO.ROLE_IMS_QP_Viewer)) {
            if (whereStringBuilder.length() > 0) {
                whereStringBuilder.append("&&");
            }

            //tree without external initial objects
            whereStringBuilder
                    .append("name nsmatch '*ExternalInitialData*'");
        }

        MapList items = new MapList();
        String type = "IMS_QP_DEP";
        //commented by issue #65455:5 doc 1.2.2 p: 11
//        String where = "from[IMS_QP_DEP2Owner].to.name==" + ctx.getUser();

        StringList selects = new StringList(DomainConstants.SELECT_ID);
        try {
            items = DomainObject.findObjects(ctx,
                    /*type*/ type,
                    /*vault*/ IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    /*where*/ whereStringBuilder.toString(),
                    /*selects*/ selects);
        } catch (FrameworkException e) {
            LOG.error("error getting DEP Tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public MapList findSQPsForBenchTable(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args map: " + e.getMessage());
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
            e.printStackTrace();
        }

        String objectType = null;
        String objectName = null;
        try {
            objectType = parent.getType(ctx);
            objectName = parent.getName(ctx);
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

            if (IMS_QP_Constants_mxJPO.AQP.equals(objectName)) {
                whereStringBuilder
                        .append("owner==" + ctx.getUser());
            }

            if (IMS_QP_Constants_mxJPO.SQP.equals(objectName)) {
                whereStringBuilder
                        .append("(to[IMS_QP_DEP2QPlan].from.attribute[IMS_QP_InterdisciplinaryDEP]==FALSE")
                        .append("&&from[IMS_QP_QPlan2Object].to.from[IMS_PBS2Owner].to.name==" + ctx.getUser() + ")")
                        .append("||")
                        .append("(to[IMS_QP_DEP2QPlan].from.attribute[IMS_QP_InterdisciplinaryDEP]==TRUE")
                        .append("&&to[IMS_QP_DEP2QPlan].from.from[IMS_QP_DEP2Owner].to.name==" + ctx.getUser() + ")");
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


        MapList items = new MapList();
        try {
            items = parent.getRelatedObjects(ctx,
                    /*relationship*/null,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan,
                    /*object attributes*/ selects,
                    /*relationship selects*/ null,
                    /*getTo*/ false, /*getFrom*/ true,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ whereStringBuilder.toString(),
                    /*relationship where*/ null,
                    /*limit*/ 0);
        } catch (FrameworkException e) {
            e.printStackTrace();
        }

        return items;
    }

    public MapList findAQPsForBenchTable(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args map: " + e.getMessage());
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
            e.printStackTrace();
        }

        String where = "";
        if (!IMS_QP_Security_mxJPO.isUserViewerWithChild(ctx)) {
            where = IMS_QP_Security_mxJPO.isUserAdminOrSuper(ctx) ? null : "owner==" + ctx.getUser()
                    + "||to[IMS_QP_DEP2QPlan].from.from[IMS_QP_DEP2Owner].to.name==" + ctx.getUser();
        }

        MapList items = new MapList();
        try {
            items = parent.getRelatedObjects(ctx,
                    /*relationship*/null,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan,
                    /*object attributes*/ selects,
                    /*relationship selects*/ null,
                    /*getTo*/ false, /*getFrom*/ true,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ where,
                    /*relationship where*/ null,
                    /*limit*/ 0);
        } catch (FrameworkException e) {
            e.printStackTrace();
        }

        return items;
    }

    public MapList findDepTasksForBenchTableInit(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args map: " + e.getMessage());
            e.printStackTrace();
        }
        String objectId = (String) argsMap.get("objectId");
        MapList items = new MapList();
        String type = IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask;
        String where = "from[IMS_QP_DEPTask2DEP].to.id==" + objectId;
        StringList selects = new StringList(DomainConstants.SELECT_ID);
        try {
            items = DomainObject.findObjects(ctx, type, IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION, where, selects);
        } catch (FrameworkException e) {
            LOG.error("error getting DEP Tasks: " + e.getMessage());
            e.printStackTrace();
        }
        return items;
    }

    public MapList findQPTasksForBenchTableInit(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args map: " + e.getMessage());
            e.printStackTrace();
        }
        String objectId = (String) argsMap.get("objectId");
        MapList items = new MapList();
        String type = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;
        String where = "to[IMS_QP_QPlan2QPTask].from.id==" + objectId;
        StringList selects = new StringList(DomainConstants.SELECT_ID);
        try {
            items = DomainObject.findObjects(ctx, type, IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION, where, selects);
        } catch (FrameworkException e) {
            LOG.error("error getting DEP Tasks: " + e.getMessage());
            e.printStackTrace();
        }

        items.addSortKey("attribute[IMS_SortOrder]", "descending", "integer");
        items.sort();

        return items;
    }

    public Vector getColumnPercentage(Context ctx, String[] args) {
        LOG.info("getColumnPercentage start time");
        Vector result = new Vector();
        Map argsMap = new HashMap();

        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        MapList argsList = (MapList) argsMap.get("objectList");

        String[] cleanedIds = new String[argsList.size()];
        for (int i = 0; i < argsList.size(); i++) {
            Map map = (Map) argsList.get(i);
            String id = (String) map.get(DomainConstants.SELECT_ID);
            cleanedIds[i] = id;
        }

        Map paramList = (Map) argsMap.get("paramList");
        String objectId = (String) paramList.get("objectId");
        String key = "";
        try {
            DomainObject domainObject = new DomainObject(objectId);
            key = domainObject.getName(ctx);
        } catch (Exception e) {
            e.printStackTrace();
        }
        BusinessObjectWithSelectList businessObjectWithSelectList = getReportData(ctx, key, cleanedIds);
        LOG.info("key: " + key + " result -> " + businessObjectWithSelectList);

        List percentList = createReport(ctx, businessObjectWithSelectList);

        /*top level Codes by items*/
        for (Object o : percentList) {
            result.addElement(o);
        }

        LOG.info("getColumnPercentage finished time");
        return result;
    }

    public Vector getColumnCheck(Context ctx, String... args) {
        LOG.info("getColumnCheck start time");
        Vector result = new Vector();
        Map argsMap = new HashMap();

        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        MapList argsList = (MapList) argsMap.get("objectList");

        List<Boolean> checkList = new ArrayList<>();
        for (int i = 0; i < argsList.size(); i++) {
            List<String> objectIds = new ArrayList<>(); //truncate ids each turn
            Map map = (Map) argsList.get(i);
            String objectId = (String) map.get(DomainConstants.SELECT_ID);
            String type = "", name = "";
            DomainObject domainObject = null;
            try {
                domainObject = new DomainObject(objectId);
                type = domainObject.getType(ctx);
                name = domainObject.getName(ctx);
            } catch (Exception e) {
                LOG.error("error getting type: " + e.getMessage());
                e.printStackTrace();
            }

            //add all ids to the list of plans
            if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan.equals(type)) {
                objectIds.add(objectId);
            } else if (IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP.equals(type)) {
                try {

                    objectIds.addAll(
                            domainObject
                                    .getInfoList(ctx, "from[IMS_QP_DEP2QPlan].to.id"));

                } catch (Exception e) {
                    LOG.error("error getting list of QPlan ids");
                    e.printStackTrace();
                }
            }

            //convert ids to array
            String[] cleanedIds = new String[objectIds.size()];
            for (int j = 0; j < cleanedIds.length; j++) {
                cleanedIds[j] = objectIds.get(j);
            }

            BusinessObjectWithSelectList businessObjectWithSelectList = getReportData(ctx, IMS_QP_Constants_mxJPO.SQP, cleanedIds);

            /*Main check process*/
            boolean checkResult = createCheckReport(ctx, businessObjectWithSelectList);
            checkList.add(checkResult);
        }

        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < argsList.size(); i++) {
            if (checkList != null && checkList.get(i)) {
                stringBuilder.setLength(0);
                String pictureName = "exclamation";
                stringBuilder
                        .append("<p align=\"center\">")
                        .append("<img src=\"")
                        .append("../common/images/fugue/16x16/").append(pictureName).append(".png")
                        .append("\"/>")
                        .append("</p>");
                result.addElement(stringBuilder.toString());
            } else result.addElement("");
        }
        LOG.info("getColumnCheck finished time");
        return result;
    }

    public Vector getColumnRedCode(Context ctx, String... args) {
        LOG.info("getColumnRedCode start time");
        Vector result = new Vector();
        Map argsMap = new HashMap();

        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        MapList argsList = (MapList) argsMap.get("objectList");

        /*top level Codes by items*/
        for (int i = 0; i < argsList.size(); i++) {
            Map map = (Map) argsList.get(i);
            String objectId = (String) map.get(DomainConstants.SELECT_ID);
            String type = "";
            try {
                type = new DomainObject(objectId).getType(ctx);
            } catch (Exception e) {
                LOG.error("error getting type: " + e.getMessage());
                e.printStackTrace();
            }

            //add all ids to the list of plans
            List<String> objectIds = new ArrayList<>();
            if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan.equals(type)) {
                objectIds.add(objectId);
            } else if (IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP.equals(type)) {
                try {
                    DomainObject object = new DomainObject(objectId);
                    StringList listQPlanIds = object.getInfoList(ctx, "from[IMS_QP_DEP2QPlan].to.id");
                    objectIds.addAll(listQPlanIds);
                } catch (Exception e) {
                    LOG.error("error getting list of QPlan ids");
                    e.printStackTrace();
                }
            }

            //rotate list of plans and add into resulting vector
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j < objectIds.size(); j++) {
                LinkedHashMap<String, String> taskMap =
                        (LinkedHashMap<String, String>) new IMS_QP_PreparationStatement_Report_mxJPO()
                                .getPrepare(ctx, /*always SQP*/ IMS_QP_Constants_mxJPO.SQP, new String[]{objectIds.get(j)});

                String currentTask = "";
                if (taskMap != null && !taskMap.isEmpty()) {
                    Map.Entry entry = taskMap.entrySet().iterator().next();

                    currentTask = entry.getKey() + ": " + entry.getValue();
                    builder.append(currentTask);
                    if (builder.length() > 0 && objectIds.size() - j > 1) {
                        builder.append("; ");
                    }
                }
            }

            if (builder.toString().endsWith("; ")) {
                builder.setLength(builder.length() - 2);
            }

            result.addElement(builder.toString());
        }

        LOG.info("getColumnRedCode finished time");
        return result;
    }

    public Vector getColumnExpectedResultByKey(Context ctx, String... args) {
        Vector result = new Vector();
        Map argsMap = new HashMap();

        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error(e.getMessage());
        }

        MapList argsList = (MapList) argsMap.get("objectList");
        String key = (String) ((Map) ((Map) argsMap.get("columnMap")).get("settings")).get("key");

        /*top level Codes by items*/
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : argsList) {
            stringBuilder.setLength(0);
            Map map = (Map) o;
            String objectId = (String) map.get(DomainConstants.SELECT_ID);

            DomainObject domainObject = null;
            String type = "";
            try {
                domainObject = new DomainObject(objectId);
                type = domainObject.getType(ctx);

            } catch (Exception e) {
                e.printStackTrace();
            }

            StringList selects = getSelects();
            StringList relSelect = new StringList("id[connection]");
            relSelect.addElement("attribute[IMS_QP_DEPTaskStatus]");

            MapList relatedObjects = new MapList();
            if (domainObject != null) {
                String relationshipWhere = "attribute[IMS_QP_DEPTaskStatus]==Approved";
                try {
                    if (type.equals(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask)) {
                        relatedObjects = domainObject.getRelatedObjects(ctx,
                                /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2DEPTask,
                                /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                                /*object attributes*/ selects,
                                /*relationship selects*/ relSelect,
                                /*getTo*/ true, /*getFrom*/ false,
                                /*recurse to level*/ (short) 1,
                                /*object where*/ null,
                                /*relationship where*/ null,
                                /*limit*/ 0);
                    } else {
                        MapList relatedInputApprovedTasks = domainObject.getRelatedObjects(ctx,
                                /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                                /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                                /*object attributes*/ selects,
                                /*relationship selects*/ relSelect,
                                /*getTo*/ true, /*getFrom*/ false,
                                /*recurse to level*/ (short) 1,
                                /*object where*/ null,
                                /*relationship where*/ relationshipWhere,
                                /*limit*/ 0);
                        if (relatedInputApprovedTasks != null) {
                            for (Object tempRelatedTask : relatedInputApprovedTasks) {
                                Map tempMap = (Map) tempRelatedTask;
                                String tempId = (String) tempMap.get(DomainConstants.SELECT_ID);
                                DomainObject tempObject = new DomainObject(tempId);
                                relatedObjects.addAll(
                                        tempObject.getRelatedObjects(ctx,
                                                /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask,
                                                /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                                                /*object attributes*/ selects,
                                                /*relationship selects*/ relSelect,
                                                /*getTo*/ false, /*getFrom*/ true,
                                                /*recurse to level*/ (short) 1,
                                                /*object where*/ null,
                                                /*relationship where*/ null,
                                                /*limit*/ 0)
                                );
                            }
                        }
                    }
                } catch (FrameworkException e) {
                    LOG.error("error getting map of related objects: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    LOG.error("exception e: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < relatedObjects.size(); i++) {
                Map relatedObjectsMap = (Map) relatedObjects.get(i);

                if (stringBuilder.length() > 0) {
                    stringBuilder.append("<br />");
                }

                if (DomainConstants.SELECT_NAME.equals(key)) {
                    String linkHTML = null;
                    try {
                        linkHTML = getLinkHTML(relatedObjectsMap, UIUtil.isNotNullAndNotEmpty(key) ?
                                key : DomainConstants.SELECT_NAME, /*font size*/14);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    stringBuilder.append(linkHTML);
                } else {
                    String valueByKey = (String) relatedObjectsMap.get(UIUtil.isNotNullAndNotEmpty(key) ?
                            key : "");
                    stringBuilder.append(UIUtil.isNotNullAndNotEmpty(valueByKey) ? valueByKey : "-");
                }
            }

            result.addElement(stringBuilder.toString());
        }
        return result;
    }

    private StringList getSelects() {
        SelectList selects = new SelectList();
        selects.addElement(DomainConstants.SELECT_ID);
        selects.addElement(DomainConstants.SELECT_NAME);
        selects.addElement(DomainConstants.SELECT_CURRENT);
        selects.addElement(IMS_QP_Constants_mxJPO.attribute_IMS_QP_DocumentCode);
        selects.addElement("attribute[IMS_SortOrder]");
        selects.addElement("attribute[IMS_QP_Protocol]");
        selects.addElement("attribute[IMS_QP_ProtocolDescription]");
        selects.addElement("from[IMS_QP_ExpectedResult2QPTask].to.id");
        return selects;
    }

    public String getLinkHTML(Map objectMap, String key, int fontSize) {
        String id = (String) objectMap.get(DomainConstants.SELECT_ID);
        String name = (String) objectMap.get(key);
        if (UIUtil.isNotNullAndNotEmpty(name)) {
            name = name.replaceAll("&", "&amp;");
        } else {
            name = "-";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div style=\"display:none;\">"
                + name + "</div>" + "<img src=''/>"
                + "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?" +
                "objectId=" + id + "')\" style=\"font-size: "
                + fontSize + "px\" title=\""
                + name + "\">"
                + name + "</a>");

        return stringBuilder.toString();
    }

    /**
     * Method who get all data for count percent column
     *
     * @param ctx    Usual parameter
     * @param ids    ids of all DEPs
     * @param qpType String type of IMS_QP elements in array
     * @return business objects list with selection data
     */
    private BusinessObjectWithSelectList getReportData(Context ctx, String qpType, String... ids) {
        BusinessObjectWithSelectList reportData =
                new IMS_QP_ListObjectsReportGenerator_mxJPO().reportGeneration(ctx, qpType, ids);

        return reportData;
    }

    int counter;
    int greenCounter;

    private List createReport(Context ctx, BusinessObjectWithSelectList reportData) {

        List mathResultList = new MapList();
        for (Object o : reportData) {
            counter = 0;
            greenCounter = 0;
            BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;

            try {
                businessObject.open(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }

            /*DEP sheet specified zone*/
            RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);
            List<String> tasksInfo = getQPTaskList(businessObject.getSelectData(DomainObject.SELECT_ID), relItr);
            Object[] tasksInfoRaw = tasksInfo.toArray();
            String[] plansCleanIDs = new String[tasksInfoRaw.length];
            for (int i = 0; i < tasksInfoRaw.length; i++) {
                plansCleanIDs[i] = (String) tasksInfoRaw[i];
            }

            //count percentage
            getInfoTasks(ctx, tasksInfo);

            counter = counter > 0 ? counter : 1;
            float mathResult = 100.0f * greenCounter / counter;
            Map<String, String> resultItem = new HashMap<>();
            String value = String.format("%.1f", mathResult);
            mathResultList.add(value);

            try {
                businessObject.close(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }
        }


        return mathResultList;
    }

    private boolean createCheckReport(Context ctx, BusinessObjectWithSelectList reportData) {

        try {
            for (Object o : reportData) {
                BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;
                businessObject.open(ctx);

                /*DEP sheet specified zone*/
                RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);
                List<String> tasksInfo = getQPTaskList(businessObject.getSelectData(DomainObject.SELECT_ID), relItr);

                //check tasks
                boolean checkFlag = getTaskColorFlags(ctx, tasksInfo);

                businessObject.close(ctx);

                if (checkFlag) {
                    return true;
                }
            }
        } catch (MatrixException e) {
            e.printStackTrace();
        }

        return false;
    }

    private RelationshipWithSelectItr getRelationshipsWithItr(Context ctx, BusinessObjectWithSelect businessObject) {

        // Instantiating the BusinessObject
        StringList selectBusStmts = new StringList();
        selectBusStmts.addElement(DomainConstants.SELECT_ID);
        selectBusStmts.addElement(DomainConstants.SELECT_TYPE);
        selectBusStmts.addElement(DomainConstants.SELECT_NAME);

        StringList selectRelStmts = new StringList();
        selectRelStmts.addElement(DomainConstants.SELECT_NAME);
        selectRelStmts.addElement(DomainConstants.SELECT_FROM_ID);
        selectRelStmts.addElement(DomainConstants.SELECT_FROM_NAME);
        selectRelStmts.addElement(DomainConstants.SELECT_TO_ID);
        selectRelStmts.addElement(DomainConstants.SELECT_TO_NAME);
        selectRelStmts.addElement("from.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.attribute[IMS_NameRu]");

        ///Group report select
        selectRelStmts.addAll(getRelationshipGroupReportSelects());

        selectRelStmts.addElement(
                String.format("to.from[%s].to.attribute[IMS_ProjDocStatus]",
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        selectRelStmts.addElement(
                String.format("from.from[%s].to.attribute[IMS_ProjDocStatus]",
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        short recurse = 1;

        ExpansionWithSelect expansion = null;
        try {
            expansion = businessObject.expandSelect(ctx,
                    "*", "*", selectBusStmts, selectRelStmts, true, true, recurse);
            // Getting the expansion
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

        } catch (MatrixException e) {
            LOG.error("matrix error: " + e.getMessage());
            e.printStackTrace();
        }
        // Getting Relationships
        RelationshipWithSelectList relationshipWithSelectList = expansion.getRelationships();
        RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(relationshipWithSelectList);
        return relItr;
    }

    private List<String> getQPTaskList(String taskId, RelationshipWithSelectItr relItr) {
        List<String> relatedTasks = new ArrayList<>();
        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();
            String relationshipType = relSelect.getSelectData(DomainConstants.SELECT_NAME);
            String relationshipFromId = relSelect.getSelectData(DomainConstants.SELECT_FROM_ID);
            String relationshipToId = relSelect.getSelectData(DomainConstants.SELECT_TO_ID);
            String relationshipToName = relSelect.getSelectData(DomainConstants.SELECT_TO_NAME);

            boolean from = relationshipFromId.equals(taskId);
            if ((relationshipType.equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan)
                    || relationshipType.equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask)) && from) {
                relatedTasks.add(relationshipToId);
            }
        }

        return relatedTasks;
    }

    private void getInfoTasks(Context ctx, List<String> tasksInfo) {
        for (String taskId : tasksInfo) {

            // Instantiate the BusinessObject.
            StringList selectBusStmts = new StringList(4);
            selectBusStmts.add(DomainConstants.SELECT_NAME);

            StringList selectRelStmts = new StringList(3);
            selectRelStmts.add(DomainConstants.SELECT_NAME);
            selectRelStmts.add(DomainConstants.SELECT_TO_ID);
            selectRelStmts.add(DomainConstants.SELECT_TO_TYPE);
            selectRelStmts.add(DomainConstants.SELECT_TO_NAME);
            selectRelStmts.add("to");
            selectRelStmts.add("to.attribute[IMS_NameRu]");
            selectRelStmts.add("to.attribute[IMS_QP_CloseStatus]");
            selectRelStmts.add("to.from[IMS_QP_QPTask2Fact].to.name");
            selectRelStmts.add("to.from[IMS_QP_QPTask2Fact].to.current");
            selectRelStmts.add("to.from[IMS_QP_QPTask2Fact].to.type"); //check CL & VTZ
            selectRelStmts.add("to.from[IMS_QP_QPTask2Fact].to.attribute[IMS_ProjDocStatus]");

            BusinessObject taskObject = null;
            try {
                taskObject = new BusinessObject(taskId);
                taskObject.open(ctx);
            } catch (MatrixException matrixException) {
                matrixException.printStackTrace();
            }

            // Getting the expansion
            ExpansionWithSelect expansion = null;
            try {
                expansion = taskObject.expandSelect(ctx,
                        DomainConstants.QUERY_WILDCARD,
                        DomainConstants.QUERY_WILDCARD,
                        selectBusStmts,
                        selectRelStmts,
                        true,
                        true,
                        (short) 1);
            } catch (MatrixException matrixException) {
                matrixException.printStackTrace();
            }

            // Getting Relationships.
            RelationshipWithSelectList _relSelectList =
                    expansion.getRelationships();
            RelationshipWithSelectItr relItr = new
                    RelationshipWithSelectItr(_relSelectList);

            while (relItr.next()) {
                RelationshipWithSelect relSelect = relItr.obj();
                if (relSelect.getSelectData(DomainConstants.SELECT_NAME).equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask)) {

                    //Increase task counter
                    counter++;

                    if ("Full".equals(relSelect.getSelectData("to.attribute[IMS_QP_CloseStatus]"))) {
                        greenCounter++;
                    }
                }
            }
        }
    }

    private boolean getTaskColorFlags(Context ctx, List<String> tasksInfo) {
        StringList resultList = new StringList();

        MapList allPlansToMap = new MapList();
        for (String taskId : tasksInfo) {
            Map<String, String> map = new LinkedHashMap();
            map.put("id", taskId);
            allPlansToMap.add(map);
        }

        Map argsMap = new HashMap();
        argsMap.put("objectList", allPlansToMap);
        try {
            resultList = IMS_QP_DEPTask_mxJPO.getFactColumnStyle(ctx, JPO.packArgs(argsMap));
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (String color : resultList) {
            if ("IMS_QP_Purple".equals(color) || "IMS_QP_Yellow".equals(color) || "IMS_QP_Orange".equals(color) || "IMS_QP_Blue".equals(color)) {
                return true;
            }
        }

        return false;
    }

    private List<String> getRelationshipGroupReportSelects() {
        StringList selectRelStmts = new StringList();
        //Relationships
        selectRelStmts.addElement(String.format("to.from[%s].to.from[%s]",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));

        //Ids
        selectRelStmts.addElement(String.format("to.from[%s].to.id",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask));
        //Codes
        selectRelStmts.addElement(String.format("to.from[%s].to.name",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask));
        //States projDocStatus
        selectRelStmts.addElement(String.format("to.from[%s].to.from[%s].to.attribute[IMS_ProjDocStatus]",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        //Current states
        selectRelStmts.addElement(String.format("to.from[%s].to.from[%s].to.current",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        //Names (RU)
        selectRelStmts.addElement(String.format("to.from[%s].to.attribute[IMS_NameRu]",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask));

        return selectRelStmts;
    }

    /**
     * @param context usual parameter
     * @param args    usual parameter
     * @return list of style names for row od object in to the table
     * @throws Exception
     */
    public static StringList getColumnStyleByKey(Context context, String[] args) {
        Map programMap = null;
        try {
            programMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args map: " + e.getMessage());
            e.printStackTrace();
        }

        MapList objectList = (MapList) programMap.get("objectList");

        StringList listStyles = new StringList();
        for (int i = 0; i < objectList.size(); i++) {
            listStyles.add("IMS_QP_ExpectedResultToWorkbench");
        }

        return listStyles;
    }

    public int changeStatusTrigger(Context ctx, String... args) {
        //TODO
        return 0;
    }

    public int changeAttributeProtocolTrigger(Context ctx, String[] args) throws Exception {
        String objectId = args[0];
        String oldValue = args[1];
        String newValue = args[2];

        boolean check = false;
        if (UIUtil.isNullOrEmpty(oldValue) && UIUtil.isNotNullAndNotEmpty(newValue)) {
            check = checkValues(ctx, objectId, "Preready");
        }

        if (UIUtil.isNotNullAndNotEmpty(oldValue) && UIUtil.isNullOrEmpty(newValue)) {
            check = checkValues(ctx, objectId, "Missed");
        }

        return check ? 0 : 1;
    }

    public boolean checkAccess(Context ctx, String... args) {
        Person person = new Person(ctx.getUser());

        if (IMS_QP_Security_mxJPO.isUserAdminOrSuper(ctx)) {
            return true;
        }

        boolean granted = false;

        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error unpacking args: " + e.getMessage());
            e.printStackTrace();
        }

        String objectId = (String) argsMap.get("objectId");

        String type = "", name = "", from = "";
        try {
            DomainObject object = new DomainObject(objectId);
            type = object.getInfo(ctx, DomainConstants.SELECT_TYPE);
            name = object.getInfo(ctx, DomainConstants.SELECT_NAME);
            from = object.getInfo(ctx, "to[IMS_QP_QP2QPlan].from.name");
        } catch (Exception e) {
            LOG.error("error getting object: " + e.getMessage());
            e.printStackTrace();
        }

        boolean isPlanType = IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan.equals(type);
        if (isPlanType && IMS_QP_Constants_mxJPO.AQP.equals(from)) {
            return true;
        }

        try {

            if (/*IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, objectId) ||*/ IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, args)) {
                if (person.isAssigned(ctx, IMS_QP_Security_mxJPO.ROLE_IMS_QP_Supervisor)) {
                    granted = true;
                }
            }

            if (IMS_QP_Security_mxJPO.isOwnerDepFromQPlan(ctx, objectId) || IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(ctx, objectId)) {
                granted = true;
            }

            if (IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, objectId)) {
                granted = true;
            }

        } catch (MatrixException e) {
            LOG.error("error when checking Person: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOG.error("error when checking Person is dep owner: " + e.getMessage());
            e.printStackTrace();
        }


        if (type.equals(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP)) {
            granted = false;
        }

        if (type.equals(IMS_QP_Constants_mxJPO.type_IMS_QP)) {
            granted = true;
        }

        try {
            if (type.equals(IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan) &&
                    !person.isAssigned(ctx, IMS_QP_Security_mxJPO.ROLE_IMS_QP_Supervisor)) {
                granted = false;
            }
        } catch (MatrixException e) {
            e.printStackTrace();
        }

        return granted;
    }

    private boolean checkValues(Context ctx, String objectId, String toState) throws Exception {

        DomainObject domainObject = null;
        //clear attribute protocol description if protocol is empty
        try {
            domainObject = new DomainObject(objectId);

            if ("Missed".equals(toState))
                domainObject.setAttributeValue(ctx, "IMS_QP_ProtocolDescription", "");
        } catch (Exception ex) {
            LOG.error(String.format(
                    "error to change attribute protocol description for  %s: %s",
                    domainObject.getInfo(ctx, DomainConstants.SELECT_NAME),
                    ex.getMessage()));
            emxContextUtil_mxJPO.mqlWarning(ctx, ex.getMessage());
            return false;
        }

        //change state
        try {
            String mqlQuery = String.format("mod bus %s current %s", objectId, toState);
            String mqlExecutionResult = MqlUtil.mqlCommand(ctx, mqlQuery);
            return true;
        } catch (FrameworkException e) {
            LOG.error("error the state change: " + e.getMessage());
            emxContextUtil_mxJPO.mqlWarning(ctx, e.getMessage());
        }

        return false;
    }

    public Vector getColumnDeviationByKey(Context ctx, String... args) {
        String empty = "-";

        Vector result = new Vector();
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error unpack args: " + e.getMessage());
            e.printStackTrace();
        }

        MapList objectList = (MapList) argsMap.get("objectList");
        String key = (String) ((Map) ((Map) argsMap.get("columnMap")).get("settings")).get("key");

        /*top level Codes by items*/
        StringBuilder stringBuilder = new StringBuilder();
        for (Object o : objectList) {
            stringBuilder.setLength(0);
            Map map = (Map) o;
            String objectId = (String) map.get(DomainConstants.SELECT_ID);

            DomainObject domainObject = null;
            try {
                domainObject = new DomainObject(objectId);
            } catch (Exception e) {
                LOG.error("error getting object: " + e.getMessage());
                e.printStackTrace();
            }

            StringList selects = getSelects();

            StringList relationSelects = new StringList();
            relationSelects.addElement(/* rel id*/DomainConstants.SELECT_ID);
            relationSelects.addElement(DomainConstants.SELECT_FROM_ID);
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.id");
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.type");
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.name");
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.revision");
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.attribute[IMS_QP_Protocol]");
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.attribute[IMS_QP_ProtocolDescription]");

            MapList relatedObjects = new MapList();
            if (domainObject != null) {
                String relationshipWhere = "attribute[IMS_QP_DEPTaskStatus]==Approved";
                try {
                    relatedObjects = domainObject.getRelatedObjects(ctx,
                            /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                            /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                            /*object attributes*/ selects,
                            /*relationship selects*/ relationSelects,
                            /*getTo*/ true, /*getFrom*/ false,
                            /*recurse to level*/ (short) 1,
                            /*object where*/ null,
                            /*relationship where*/ relationshipWhere,
                            /*limit*/ 0);

                } catch (FrameworkException e) {
                    LOG.error("error getting map of related objects: " + e.getMessage());
                    e.printStackTrace();
                } catch (Exception e) {
                    LOG.error("exception e: " + e.getMessage());
                    e.printStackTrace();
                }
            }

            for (int i = 0; i < relatedObjects.size(); i++) {
                Map objectMap = (Map) relatedObjects.get(i);

                if (stringBuilder.length() > 0) {
                    stringBuilder.append("<br />");
                }

                if ("initial".equals(key)) {
                    String value = getEmbeddedColor(ctx, domainObject, objectMap);
                    stringBuilder.append(UIUtil.isNotNullAndNotEmpty(value) ? value : empty);
                } else if ("protocol".equals(key)) {
                    String value = (String) objectMap.get("tomid[IMS_QP_Deviation_QPTask2QPTask].from.attribute[IMS_QP_Protocol]");
                    stringBuilder.append(UIUtil.isNotNullAndNotEmpty(value) ? value : empty);
                } else if ("p_description".equals(key)) {
                    String value = (String) objectMap.get("tomid[IMS_QP_Deviation_QPTask2QPTask].from.attribute[IMS_QP_ProtocolDescription]");
                    stringBuilder.append(UIUtil.isNotNullAndNotEmpty(value) ? value : empty);
                } else {
                    stringBuilder.append(empty);
                }
            }

            result.addElement(stringBuilder.toString());
        }
        return result;
    }

    private String getEmbeddedColor(Context ctx, DomainObject object1, Map objectMap) {

        DomainObject object = null;
        try {
            object = new DomainObject((String) objectMap.get(DomainConstants.SELECT_FROM_ID));
        } catch (Exception e) {
            e.printStackTrace();
        }

        String color = "";
        //1 if the task is 'Another' type
        String hasFact = this.getInfo(ctx,
                String.format("from[%s]", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact),
                object
        );

        String resultType = this.getInfo(ctx,
                "from[IMS_QP_ExpectedResult2QPTask].to.to[IMS_QP_ResultType2ExpectedResult].from.to[IMS_QP_ResultType2Family].from.name", object);
        if (IMS_QP_Constants_mxJPO.ANOTHER_PLAN_TYPES.equals(resultType) && "FALSE".equals(hasFact)) {
            color = "IMS_QP_Blue";
        }

        //2 if attribute of task IMS_QP_SelectDocument has any values
        if (UIUtil.isNotNullAndNotEmpty(this.getInfo(ctx, IMS_QP_Constants_mxJPO.attribute_IMS_QP_SelectDocument, object))) {
            color = "IMS_QP_Purple";
        }

        //3 if task has relationship to fact but that fact isn't in 'Finalized' state or 'Approved' for CheckList type
        String factStatus = null, factState = null;
        try {
            factStatus = object.getInfo(ctx, String.format("from[%s].to.attribute[IMS_ProjDocStatus]", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
            factState = object.getInfo(ctx, String.format("from[%s].to.current", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        } catch (FrameworkException e) {
            LOG.error("error getting info: " + e.getMessage());
            e.printStackTrace();
        }
        boolean checkFactStatus = UIUtil.isNotNullAndNotEmpty(factStatus) && factStatus.equals("Finalized") ||
                UIUtil.isNotNullAndNotEmpty(factState) && factState.contains("Approved");

        if ("TRUE".equals(hasFact) && !checkFactStatus) {
            color = "IMS_QP_Red";
        }

        //4 if attribute of expected result IMS_QP_DocumentCode contains value 'Wrong'
        String wrongCodeField = this.getInfo(ctx, String.format("from[%s].to.%s",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask,
                IMS_QP_Constants_mxJPO.attribute_IMS_QP_DocumentCode), object);
        String errorCodeField = this.getInfo(ctx, String.format(
                "attribute[%s]", IMS_QP_Constants_mxJPO.IMS_QP_ADDITIONAL_INFO), object);
        if (UIUtil.isNotNullAndNotEmpty(wrongCodeField) && wrongCodeField.contains("Wrong code")
                || UIUtil.isNotNullAndNotEmpty(errorCodeField)) {
            color = "IMS_QP_Orange";
        }

        //5 if the task has more than one expected result in direction 'Output'
        String id, moreThanOneExpectedRelations = null;
        try {
            id = object.getId(ctx);
            moreThanOneExpectedRelations = MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_QP_ExpectedResult2QPTask].to.id dump |", id));
        } catch (FrameworkException e) {
            LOG.error("error getting id: " + e.getMessage());
            e.printStackTrace();
        }

        if (UIUtil.isNotNullAndNotEmpty(moreThanOneExpectedRelations) && moreThanOneExpectedRelations.contains("|") || errorCodeField.contains("4.1")) {
            color = "IMS_QP_Yellow";
        }

        String deviation = (String) objectMap.get("tomid[IMS_QP_Deviation_QPTask2QPTask].from.attribute[IMS_QP_Protocol]");
        if (UIUtil.isNotNullAndNotEmpty(deviation)) {
            color = "IMS_QP_White";
        }

        int compareResult = this.getInfo(ctx, "attribute[IMS_QP_FactExp]", object).compareTo(this.getInfo(ctx, "attribute[IMS_QP_FactGot]", object));

        return getPhrase(compareResult, color);

    }

    static String getPhrase(int compareResult, String color) {


        if (compareResult == 0) {
            return "Frozen Initial Data";
        } else if ("IMS_QP_White".equals(color)) {
            return "Initial Data has been replaced";
        } else if ("IMS_QP_Orange".equals(color)
                || "IMS_QP_Yellow".equals(color)
                || "IMS_QP_Purple".equals(color)) {
            return "Planning of Initial Data in progress";
        } else if (UIUtil.isNullOrEmpty(color) && compareResult == 1 || "IMS_QP_Red".equals(color)) {
            return "Missed Initial Data";
        }

        return "Planning of Initial Data in progress";
    }

    private String getInfo(Context ctx, String key, DomainObject object) {
        if (key == null || object == null) {
            try {
                return "error statement: " + key + "|" + object.getName(ctx);
            } catch (FrameworkException e) {
                LOG.error("error getting name" + e.getMessage());
                e.printStackTrace();
            }
        }

        if (DomainConstants.SELECT_ID.equals(key)) {
            try {
                return object.getId(ctx);
            } catch (FrameworkException e) {
                LOG.error("error getting id: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (DomainConstants.SELECT_NAME.equals(key)) {
            try {
                return object.getName(ctx);
            } catch (FrameworkException e) {
                LOG.error("error getting name: " + e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            return object.getInfo(ctx, key);
        } catch (FrameworkException e) {
            LOG.error("error getting info: " + e.getMessage());
            e.printStackTrace();
        }

        return "*NULL*";
    }
}
