import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.Context;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_DEPSubStageDEPTasks_mxJPO {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    private static final String PROGRAM_IMS_QP_DEPSubStageDEPTasks = "IMS_QP_DEPSubStageDEPTasks";

    private static final String TYPE_IMS_QP_Project = "IMS_QP_Project";
    private static final String TYPE_IMS_QP = "IMS_QP";
    private static final String TYPE_IMS_QP_DEP = "IMS_QP_DEP";
    private static final String TYPE_IMS_QP_DEPProjectStage = "IMS_QP_DEPProjectStage";
    private static final String TYPE_IMS_QP_DEPSubStage = "IMS_QP_DEPSubStage";
    private static final String TYPE_IMS_QP_DEPTask = "IMS_QP_DEPTask";

    private static final String TYPE_IMS_QP_QPlan = "IMS_QP_QPlan";
    private static final String TYPE_IMS_QP_QPTask = "IMS_QP_QPTask";

    private static final String RELATIONSHIP_IMS_QP_QP2DEP = "IMS_QP_QP2DEP";
    private static final String RELATIONSHIP_IMS_QP_DEP2DEPProjectStage = "IMS_QP_DEP2DEPProjectStage";
    private static final String RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage = "IMS_QP_DEPProjectStage2DEPSubStage";
    private static final String RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask = "IMS_QP_DEPSubStage2DEPTask";
    private static final String RELATIONSHIP_IMS_QP_DEPTask2DEPTask = "IMS_QP_DEPTask2DEPTask";
    private static final String RELATIONSHIP_IMS_QP_DEPTask2DEP = "IMS_QP_DEPTask2DEP";

    private static final String RELATIONSHIP_IMS_QP_Project2QP = "IMS_QP_Project2QP";
    private static final String RELATIONSHIP_IMS_QP_QP2QPlan = "IMS_QP_QP2QPlan";
    private static final String RELATIONSHIP_IMS_QP_QPlan2QPTask = "IMS_QP_QPlan2QPTask";
    private static final String RELATIONSHIP_IMS_QP_QPTask2QPTask = "IMS_QP_QPTask2QPTask";
    private static final String RELATIONSHIP_IMS_QP_DEPTask2QPTask = "IMS_QP_DEPTask2QPTask";

    private static final String RELATIONSHIP_IMS_QP_ExpectedResult2QPTask = "IMS_QP_ExpectedResult2QPTask";
    private static final String ATTRIBUTE_IMS_QP_DocumentCode = "IMS_QP_DocumentCode";

    private static final String ATTRIBUTE_IMS_Name = "IMS_Name";
    private static final String ATTRIBUTE_IMS_NameRu = "IMS_NameRu";

    private static final String RELATIONSHIP_IMS_QP_QPTask2Fact = "IMS_QP_QPTask2Fact";

    private static final String SELECT_DEP_ID = String.format(
            "to[%s].from.to[%s].from.to[%s].from.id",
            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage);

    private static final String SELECT_DEP_NAME = String.format(
            "to[%s].from.to[%s].from.to[%s].from.name",
            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage);

    private static final String SELECT_DEP_IMS_NAME = String.format(
            "to[%s].from.to[%s].from.to[%s].from.%s",
            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
            DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Name));

    private static final String SELECT_DEP_IMS_NAME_RU = String.format(
            "to[%s].from.to[%s].from.to[%s].from.%s",
            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
            DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu));

    private static final String SELECT_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID = String.format(
            "to[%s].from.to[%s].from.id",
            RELATIONSHIP_IMS_QP_DEPTask2QPTask,
            RELATIONSHIP_IMS_QP_DEPTask2DEPTask);

    private static final String SELECT_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID = String.format(
            "to[%s].from.from[%s].to.id",
            RELATIONSHIP_IMS_QP_DEPTask2QPTask,
            RELATIONSHIP_IMS_QP_DEPTask2DEPTask);

    private static final String SELECT_QPLAN_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID = String.format(
            "from[%s].from.%s",
            RELATIONSHIP_IMS_QP_QPlan2QPTask,
            SELECT_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID);

    private static final String SELECT_QPLAN_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID = String.format(
            "from[%s].from.%s",
            RELATIONSHIP_IMS_QP_QPlan2QPTask,
            SELECT_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID);

    private static final String SOURCE_DEP = "D_E_P"; // Because source lists are checked using indexOf
    private static final String SOURCE_DEPTask = "DEPTask";

    private static final String SOURCE_QPTask = "QPTask";

    public IMS_QP_DEPSubStageDEPTasks_mxJPO(Context context, String[] args) throws Exception {
    }

    private static String getIconUrl(String type) {
        return IMS_KDD_mxJPO.COMMON_IMAGES + type + "_16x16.png";
    }

    private static StringList getQPTreeSelects() {
        return new StringList(new String[]{
                DomainConstants.SELECT_ID,
                DomainConstants.SELECT_NAME,
                DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Name),
                DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu)
        });
    }

    private static String getCurrentDEPId(Context context, String[] args) throws Exception {
        String depSubStageId = (String) IMS_KDD_mxJPO.getProgramMap(args).get("parentOID");

        return new DomainObject(depSubStageId).getInfo(context, String.format(
                "to[%s].from.to[%s].from.id",
                RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                RELATIONSHIP_IMS_QP_DEP2DEPProjectStage));
    }

    @SuppressWarnings("unused")
    public MapList getQPTreeRootObjects(Context context, String[] args) throws Exception {
        try {
            MapList mapList = DomainObject.findObjects(
                    context, TYPE_IMS_QP_Project, "*", "*", "*", "*",
                    null, true,
                    getQPTreeSelects());

            IMS_KDD_mxJPO.sortMapsByName(mapList);
            return mapList;
        } catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public MapList expandQPTreeObject(Context context, String[] args) throws Exception {
        try {
            DomainObject parentObject = IMS_KDD_mxJPO.getObjectFromProgramMap(context, IMS_KDD_mxJPO.getProgramMap(args));

            List<Map> maps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                    context, parentObject,
                    StringUtils.join(
                            Arrays.asList(
                                    RELATIONSHIP_IMS_QP_Project2QP,

                                    RELATIONSHIP_IMS_QP_QP2DEP,
                                    RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                                    RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                                    RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,

                                    RELATIONSHIP_IMS_QP_QP2QPlan,
                                    RELATIONSHIP_IMS_QP_QPlan2QPTask
                            ),
                            ','),
                    true,
                    Arrays.asList(
                            DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Name),
                            DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu)
                    ),
                    null, null, false);

            IMS_KDD_mxJPO.sortMapsByName(maps);
            return new MapList(maps);
        } catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public MapList getOtherDEPsRootObjects(Context context, String[] args) throws Exception {
        try {
            MapList mapList = DomainObject.findObjects(
                    context, TYPE_IMS_QP_DEP, "*", "*", "*", "*",
                    //String.format("id!=%s", getCurrentDEPId(context, args)),
                    String.format(
                            "from[%s].to.from[%s].to.from[%s].to.from[%s|to.id==%s]==True",
                            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                            RELATIONSHIP_IMS_QP_DEPTask2DEP,
                            getCurrentDEPId(context, args)
                    ),
                    true,
                    getQPTreeSelects());

            IMS_KDD_mxJPO.sortMapsByName(mapList);
            return mapList;
        } catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public MapList expandOtherDEPsObject(Context context, String[] args) throws Exception {
        try {
            DomainObject parentObject = IMS_KDD_mxJPO.getObjectFromProgramMap(context, IMS_KDD_mxJPO.getProgramMap(args));
            String currentDEPId = getCurrentDEPId(context, args);

            List<Map> maps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                    context, parentObject,
                    StringUtils.join(
                            Arrays.asList(
                                    RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                                    RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                                    RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask
                            ),
                            ','),
                    true,
                    Arrays.asList(
                            DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Name),
                            DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu)
                    ),
                    String.format(
                            "from[%s|to.id==%s]==True ||" +
                                    "from[%s].to.from[%s|to.id==%s]==True ||" +
                                    "from[%s].to.from[%s].to.from[%s|to.id==%s]==True ||" +
                                    "from[%s].to.from[%s].to.from[%s].to.from[%s|to.id==%s]==True",
                            RELATIONSHIP_IMS_QP_DEPTask2DEP,
                            currentDEPId,

                            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                            RELATIONSHIP_IMS_QP_DEPTask2DEP,
                            currentDEPId,

                            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                            RELATIONSHIP_IMS_QP_DEPTask2DEP,
                            currentDEPId,

                            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                            RELATIONSHIP_IMS_QP_DEPTask2DEP,
                            currentDEPId
                    )
                    /*String.format(
                            "type!=%s || from[%s|to.id==%s]==True",
                            TYPE_IMS_QP_DEPTask,
                            RELATIONSHIP_IMS_QP_DEPTask2DEP,
                            currentDEPId)*/,
                    null, false);

            IMS_KDD_mxJPO.sortMapsByName(maps);
            return new MapList(maps);
        } catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public MapList getCurrentDEPTreeRootObjects(Context context, String[] args) throws Exception {
        try {
            MapList mapList = DomainObject.findObjects(
                    context, TYPE_IMS_QP_DEPProjectStage, "*", "*", "*", "*",
                    String.format("to[%s].from.id==%s", RELATIONSHIP_IMS_QP_DEP2DEPProjectStage, getCurrentDEPId(context, args)),
                    true,
                    getQPTreeSelects());

            IMS_KDD_mxJPO.sortMapsByName(mapList);
            return mapList;
        } catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public MapList expandQPlanQPTreeObject(Context context, String[] args) throws Exception {
        try {
            String qPlanId = (String) IMS_KDD_mxJPO.getProgramMap(args).get("parentOID");
            DomainObject qPlanObject = new DomainObject(qPlanId);
            StringList leftInputDEPTaskIds = qPlanObject.getInfoList(context, SELECT_QPLAN_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID);
            StringList leftOutputDEPTaskIds = qPlanObject.getInfoList(context, SELECT_QPLAN_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID);

            DomainObject parentObject = IMS_KDD_mxJPO.getObjectFromProgramMap(context, IMS_KDD_mxJPO.getProgramMap(args));
            boolean parentIsQPlan = parentObject.getType(context).equals(TYPE_IMS_QP_QPlan);

            List<String> selects = new ArrayList<>(Arrays.asList(
                    DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Name),
                    DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu)));

            if (parentIsQPlan) {
                selects.add(SELECT_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID);
                selects.add(SELECT_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID);
            }

            List<Map> maps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                    context, parentObject,
                    StringUtils.join(
                            Arrays.asList(
                                    RELATIONSHIP_IMS_QP_Project2QP,
                                    RELATIONSHIP_IMS_QP_QP2QPlan,
                                    RELATIONSHIP_IMS_QP_QPlan2QPTask
                            ),
                            ','),
                    true,
                    selects,
                    null, null, false);

            List<Map> filteredMaps;
            if (parentIsQPlan) {
                filteredMaps = new ArrayList<>();
                for (Map map : maps) {
                    List<String> rightInputDEPTaskIds = IMS_KDD_mxJPO.split(map.get(SELECT_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID));
                    List<String> rightOutputDEPTaskIds = IMS_KDD_mxJPO.split(map.get(SELECT_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID));
                    boolean add = false;
                    for (Object leftInputDEPTaskId : leftInputDEPTaskIds) {
                        if (rightOutputDEPTaskIds.contains(leftInputDEPTaskId)) {
                            add = true;
                            break;
                        }
                    }
                    if (!add) {
                        for (Object leftOutputDEPTaskId : leftOutputDEPTaskIds) {
                            if (rightInputDEPTaskIds.contains(leftOutputDEPTaskId)) {
                                add = true;
                                break;
                            }
                        }
                    }
                    if (add) {
                        filteredMaps.add(map);
                    }
                }
            } else {
                filteredMaps = maps;
            }

            IMS_KDD_mxJPO.sortMapsByName(filteredMaps);
            return new MapList(filteredMaps);
        } catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public Vector getQPTreeObjectCode(Context context, String[] args) throws Exception {
        boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);
        Vector results = new Vector();
        for (Map map : IMS_KDD_mxJPO.getObjectListMaps(args)) {
            String type = IMS_KDD_mxJPO.getTypeFromMap(map);

            String source = "";
            String image = getIconUrl(type);

            switch (type) {
                case TYPE_IMS_QP_DEP:
                    source = SOURCE_DEP;
                    break;

                case TYPE_IMS_QP_DEPTask:
                    source = SOURCE_DEPTask;
                    break;

                case TYPE_IMS_QP_QPTask:
                    source = SOURCE_QPTask;
                    break;
            }

            results.addElement(IMS_KDD_mxJPO.getLinkHTML(
                    context, map,
                    source,
                    null,
                    image,
                    null, null, null,
                    true, true, "treeBodyTable",
                    true,
                    null, false));
        }
        return results;
    }

    @SuppressWarnings("unused")
    public Vector getQPTreeObjectName(Context context, String[] args) throws Exception {
        boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);
        Vector results = new Vector();
        for (Map map : IMS_KDD_mxJPO.getObjectListMaps(args)) {
            results.addElement(map.get(DomainObject.getAttributeSelect(isRuLocale ?
                    ATTRIBUTE_IMS_NameRu :
                    ATTRIBUTE_IMS_Name)));
        }
        return results;
    }

    private Vector getDEPTaskRelatedObjects(Context context, String[] args, boolean in) throws Exception {
        try {
            String virtualRelationship = in ? "in" : "out";
            boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);

            Vector results = new Vector();

            for (Map map : IMS_KDD_mxJPO.getObjectListMaps(args)) {
                StringBuilder sb = new StringBuilder();
                String rowId = IMS_KDD_mxJPO.getRowId(map);
                String id = IMS_KDD_mxJPO.getIdFromMap(map);
                DomainObject depTaskObject = IMS_KDD_mxJPO.idToObject(context, id);
                boolean currentUserIsRowDEPOwner = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, depTaskObject);

                List<Map> relatedMaps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                        context, depTaskObject,
                        StringUtils.join(
                                Arrays.asList(RELATIONSHIP_IMS_QP_DEPTask2DEPTask, RELATIONSHIP_IMS_QP_DEPTask2DEP),
                                ','),
                        !in,
                        Arrays.asList(
                                DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu),
                                SELECT_DEP_ID, SELECT_DEP_NAME, SELECT_DEP_IMS_NAME, SELECT_DEP_IMS_NAME_RU),
                        null, null, false);

                for (Map relatedMap : relatedMaps) {
                    if (sb.length() > 0) {
                        sb.append("<br />");
                    }

                    if (currentUserIsRowDEPOwner ||
                            IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, IMS_KDD_mxJPO.getIdFromMap(relatedMap))) {

                        sb.append(IMS_KDD_mxJPO.getDisconnectLinkHTML(
                                PROGRAM_IMS_QP_DEPSubStageDEPTasks, "disconnectDEPTask",
                                id, IMS_KDD_mxJPO.getIdFromMap(relatedMap),
                                virtualRelationship,
                                "Disconnect",
                                IMS_KDD_mxJPO.getRefreshAllRowsFunction()));
                    }

                    if (IMS_KDD_mxJPO.getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEPTask)) {
//                        Map depMap = new HashMap();
//                        depMap.put(DomainConstants.SELECT_ID, relatedMap.get(SELECT_DEP_ID));
//                        depMap.put(DomainConstants.SELECT_NAME, relatedMap.get(SELECT_DEP_NAME));
//
//                        sb.append(IMS_KDD_mxJPO.getLinkHTML(
//                                context, depMap, SOURCE_DEP, null,
//                                getIconUrl(TYPE_IMS_QP_DEP),
//                                "12px",
//                                (String) relatedMap.get(
//                                        isRuLocale ?
//                                                SELECT_DEP_IMS_NAME_RU :
//                                                SELECT_DEP_IMS_NAME),
//                                null, true, false, null, true, null, false));
//
//                        sb.append(String.format(
//                                "&#160;&#160;<img src=\"%s\" />&#160;",
//                                IMS_KDD_mxJPO.FUGUE_16x16 + "arrow.png"));

                        sb.append(IMS_KDD_mxJPO.getLinkHTML(
                                context, relatedMap, SOURCE_DEPTask, null,
                                getIconUrl(TYPE_IMS_QP_DEPTask),
                                "12px",
                                (String) relatedMap.get(DomainObject.getAttributeSelect(
                                        isRuLocale ?
                                                ATTRIBUTE_IMS_NameRu :
                                                ATTRIBUTE_IMS_Name)),
                                null, true, false, null, true, null, false));
                    } else if (IMS_KDD_mxJPO.getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEP)) {
                        sb.append(IMS_KDD_mxJPO.getLinkHTML(
                                context, relatedMap, SOURCE_DEP, null,
                                getIconUrl(TYPE_IMS_QP_DEP),
                                "12px",
                                (String) relatedMap.get(DomainObject.getAttributeSelect(
                                        isRuLocale ?
                                                ATTRIBUTE_IMS_NameRu :
                                                ATTRIBUTE_IMS_Name)),
                                null, true, false, null, true, null, false));
                    }
                }

                sb.append(IMS_DragNDrop_mxJPO.getConnectDropAreaHTML(
                        PROGRAM_IMS_QP_DEPSubStageDEPTasks, "connectDEPTask",
                        virtualRelationship, !in,
                        rowId, id,
                        IMS_KDD_mxJPO.getRefreshAllRowsFunction(),
                        in ?
                                SOURCE_DEPTask :
                                StringUtils.join(Arrays.asList(SOURCE_DEPTask, SOURCE_DEP), ','),
                        String.format(
                                "Drop %s %s here",
                                in ? "input" : "output",
                                in ? "DEP Task" : "DEP Task or DEP"),
                        "26px", "10px"));

                results.addElement(sb.toString());
            }

            return results;
        } catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public Vector getDEPTaskInput(Context context, String[] args) throws Exception {
        return getDEPTaskRelatedObjects(context, args, true);
    }

    @SuppressWarnings("unused")
    public Vector getDEPTaskOutput(Context context, String[] args) throws Exception {
        return getDEPTaskRelatedObjects(context, args, false);
    }

    private Vector getQPTaskRelatedObjects(Context context, String[] args, boolean in) throws Exception {
        try {
            String virtualRelationship = in ? "in" : "out";
            boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);

            Vector results = new Vector();

            for (Map rowQPTaskMap : IMS_KDD_mxJPO.getObjectListMaps(args)) {
                StringBuilder sb = new StringBuilder();
                String rowId = IMS_KDD_mxJPO.getRowId(rowQPTaskMap);
                String rowQPTaskId = IMS_KDD_mxJPO.getIdFromMap(rowQPTaskMap);

                DomainObject rowQPTask = new DomainObject(rowQPTaskId);
                StringList rowQPTaskDEPTaskInputDEPTaskIds = rowQPTask.getInfoList(context, SELECT_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID);
                StringList rowQPTaskDEPTaskOutputDEPTaskIds = rowQPTask.getInfoList(context, SELECT_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID);

                List<Map> relatedMaps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                        context, IMS_KDD_mxJPO.idToObject(context, rowQPTaskId),
                        RELATIONSHIP_IMS_QP_QPTask2QPTask,
                        !in,
                        Arrays.asList(
                                DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu),
                                SELECT_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID,
                                SELECT_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID),
                        null, null, false);

                for (Map relatedMap : relatedMaps) {
                    if (sb.length() > 0) {
                        sb.append("<br />");
                    }

                    sb.append(IMS_KDD_mxJPO.getDisconnectLinkHTML(
                            PROGRAM_IMS_QP_DEPSubStageDEPTasks, "disconnectQPTask",
                            rowQPTaskId, IMS_KDD_mxJPO.getIdFromMap(relatedMap),
                            virtualRelationship,
                            "Disconnect",
                            IMS_KDD_mxJPO.getRefreshAllRowsFunction()));

                    boolean thereAreConnectedQPTaskDEPTasks = false;

                    List<String> relatedQPTaskDEPTaskInputDEPTaskIds = IMS_KDD_mxJPO.split(relatedMap.get(SELECT_QPTASK_DEP_TASK_INPUT_DEP_TASK_ID));
                    List<String> relatedQPTaskDEPTaskOutputDEPTaskIds = IMS_KDD_mxJPO.split(relatedMap.get(SELECT_QPTASK_DEP_TASK_OUTPUT_DEP_TASK_ID));

                    if (in) {
                        for (Object rowQPTaskDEPTaskInputDEPTaskId : rowQPTaskDEPTaskInputDEPTaskIds) {
                            if (relatedQPTaskDEPTaskOutputDEPTaskIds.contains(rowQPTaskDEPTaskInputDEPTaskId)) {
                                thereAreConnectedQPTaskDEPTasks = true;
                                break;
                            }
                        }
                    } else {
                        for (Object rowQPTaskDEPTaskOutputDEPTaskId : rowQPTaskDEPTaskOutputDEPTaskIds) {
                            if (relatedQPTaskDEPTaskInputDEPTaskIds.contains(rowQPTaskDEPTaskOutputDEPTaskId)) {
                                thereAreConnectedQPTaskDEPTasks = true;
                                break;
                            }
                        }
                    }

                    if (!thereAreConnectedQPTaskDEPTasks) {
                        sb.append(String.format(
                                "&#160;<img src=\"%s\" title=\"%s\" />",
                                IMS_KDD_mxJPO.FUGUE_16x16 + "exclamation.png",
                                HtmlEscapers.htmlEscaper().escape("Not DEPTask-based link"))); // TODO
                    }

                    sb.append(IMS_KDD_mxJPO.getLinkHTML(
                            context, relatedMap, SOURCE_QPTask, null,
                            getIconUrl(TYPE_IMS_QP_QPTask),
                            "12px",
                            (String) relatedMap.get(DomainObject.getAttributeSelect(
                                    isRuLocale ?
                                            ATTRIBUTE_IMS_NameRu :
                                            ATTRIBUTE_IMS_Name)),
                            null, true, false, null, true, null, false));
                }

                sb.append(IMS_DragNDrop_mxJPO.getConnectDropAreaHTML(
                        PROGRAM_IMS_QP_DEPSubStageDEPTasks, "connectQPTask",
                        virtualRelationship, !in,
                        rowId, rowQPTaskId,
                        IMS_KDD_mxJPO.getRefreshAllRowsFunction(),
                        SOURCE_QPTask,
                        "Drop QP Task here",
                        "26px", "10px"));

                results.addElement(sb.toString());
            }

            return results;
        } catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public Vector getQPTaskInput(Context context, String[] args) throws Exception {
        return getQPTaskRelatedObjects(context, args, true);
    }

    @SuppressWarnings("unused")
    public Vector getQPTaskOutput(Context context, String[] args) throws Exception {
        return getQPTaskRelatedObjects(context, args, false);
    }

    @SuppressWarnings("unused")
    public String connectDEPTask(Context context, String[] args) {
        return IMS_KDD_mxJPO.connect(context, args, new IMS_KDD_mxJPO.Connector() {
            @Override
            public String connect(Context context, String from, String to, String relationship) throws Exception {

                DomainObject fromObject = new DomainObject(from);
                DomainObject toObject = new DomainObject(to);

                if (IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, fromObject) ||
                        IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, toObject)) {


                    String depIdFromObject = fromObject.getInfo(context, IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID);
                    String depIdToObject = toObject.getInfo(context, IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID);
                    LOG.info("depFromId:" + depIdFromObject + " equals depToId: " + depIdToObject + " relationship IMS_QP_DEPTaskStatus state will be \'Approved\'");

                    DomainRelationship domainRelationship = IMS_KDD_mxJPO.connectIfNotConnected(context, toObject.getType(context).equals(TYPE_IMS_QP_DEPTask) ?
                            RELATIONSHIP_IMS_QP_DEPTask2DEPTask : RELATIONSHIP_IMS_QP_DEPTask2DEP, fromObject, toObject);

                    String oldStatus = domainRelationship.getAttributeValue(context, "IMS_QP_DEPTaskStatus");
                    domainRelationship.setAttributeValue(context, "IMS_QP_DEPTaskStatus", "Approved");
                    LOG.info("relation status was: " + oldStatus + " now: " + domainRelationship.getAttributeValue(context, "IMS_QP_DEPTaskStatus"));

                    return "";
                }
                return "Access denied.";
            }
        });
    }

    @SuppressWarnings("unused")
    public String disconnectDEPTask(Context context, String[] args) throws Exception {
        return IMS_KDD_mxJPO.disconnect(context, args, new IMS_KDD_mxJPO.Disconnector() {
            @Override
            public String disconnect(Context context, String from, String to, String relationship) throws Exception {

                DomainObject fromObject = new DomainObject(from);
                DomainObject toObject = new DomainObject(to);

                if (IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, fromObject) ||
                        IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, toObject)) {

                    fromObject.disconnect(
                            context,
                            new RelationshipType(toObject.getType(context).equals(TYPE_IMS_QP_DEPTask) ?
                                    RELATIONSHIP_IMS_QP_DEPTask2DEPTask :
                                    RELATIONSHIP_IMS_QP_DEPTask2DEP),
                            !relationship.equals("in"),
                            toObject);

                    return "";
                }
                return "Access denied.";
            }
        });
    }

    @SuppressWarnings("unused")
    public String connectQPTask(Context context, String[] args) throws Exception {
        return IMS_KDD_mxJPO.connect(context, args, new IMS_KDD_mxJPO.Connector() {
            @Override
            public String connect(Context context, String from, String to, String relationship) throws Exception {

                IMS_KDD_mxJPO.connectIfNotConnected(
                        context,
                        RELATIONSHIP_IMS_QP_QPTask2QPTask,
                        new DomainObject(from),
                        new DomainObject(to));

                return "";
            }
        });
    }

    @SuppressWarnings("unused")
    public String disconnectQPTask(Context context, String[] args) throws Exception {
        return IMS_KDD_mxJPO.disconnect(context, args, new IMS_KDD_mxJPO.Disconnector() {
            @Override
            public String disconnect(Context context, String from, String to, String relationship) throws Exception {

                DomainObject toObject = new DomainObject(to);

                new DomainObject(from).disconnect(
                        context,
                        new RelationshipType(RELATIONSHIP_IMS_QP_QPTask2QPTask),
                        !relationship.equals("in"),
                        toObject);

                return "";
            }
        });
    }

    @SuppressWarnings("unused")
    public static StringList getDEPTaskInputCellStyle(Context context, String[] args) throws Exception {
        StringList styles = new StringList();
        for (Map ignored : IMS_KDD_mxJPO.getObjectListMaps(args)) {
            styles.add("IMS_QP_DEPTaskInput");
        }
        return styles;
    }

    @SuppressWarnings("unused")
    public static StringList getDEPTaskOutputCellStyle(Context context, String[] args) throws Exception {
        StringList styles = new StringList();
        for (Map ignored : IMS_KDD_mxJPO.getObjectListMaps(args)) {
            styles.add("IMS_QP_DEPTaskOutput");
        }
        return styles;
    }

    @SuppressWarnings("unused")
    public String getConnectedExternalDocumentSetHTML(Context context, String[] args) throws Exception {
        DomainObject qpTaskObject = IMS_KDD_mxJPO.getObjectFromParamMap(args);

        List<Map> externalDocumentSetMaps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                context, qpTaskObject,
                RELATIONSHIP_IMS_QP_QPTask2Fact,
                true,
                Arrays.asList(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_NAME,
                        IMS_KDD_mxJPO.getToNameSelect(IMS_ExternalSystem_mxJPO.RELATIONSHIP_IMS_Object2ExternalSystem)
                ),
                null, null, false);

        StringBuilder sb = new StringBuilder();

        if (externalDocumentSetMaps.size() > 0) {
            for (Map externalDocumentSetMap : externalDocumentSetMaps) {
                if (sb.length() > 0) {
                    sb.append("<br />");
                }

                String externalSystemName = (String) externalDocumentSetMap.get(
                        IMS_KDD_mxJPO.getToNameSelect(IMS_ExternalSystem_mxJPO.RELATIONSHIP_IMS_Object2ExternalSystem));

                sb.append(IMS_KDD_mxJPO.getDisconnectLinkHTML(
                        PROGRAM_IMS_QP_DEPSubStageDEPTasks, "disconnectExternalDocumentSet",
                        qpTaskObject.getId(context), IMS_KDD_mxJPO.getIdFromMap(externalDocumentSetMap),
                        RELATIONSHIP_IMS_QP_QPTask2Fact,
                        "Disconnect",
                        IMS_KDD_mxJPO.getRefreshWindowFunction()));

                sb.append(String.format(
                        "<a href=\"javascript:%s\"><img src=\"%s\" />%s</a>",
                        String.format(
                                "emxTableColumnLinkClick('../common/emxForm.jsp?objectId=%s&form=type_IMS_ExternalDocumentSet&%s=%s')",
                                IMS_KDD_mxJPO.getIdFromMap(externalDocumentSetMap),
                                IMS_ExternalSystem_mxJPO.ATTRIBUTE_IMS_ExternalSystemName,
                                HtmlEscapers.htmlEscaper().escape(externalSystemName != null ? externalSystemName : "")),
                        IMS_KDD_mxJPO.FUGUE_16x16 + "document.png",
                        HtmlEscapers.htmlEscaper().escape(IMS_KDD_mxJPO.getNameFromMap(externalDocumentSetMap))));
            }
        } else {
            sb.append(String.format(
                    "<a href=\"javascript:%s\"><img src=\"%s\" title=\"%s\" /></a>",
                    String.format(
                            "window.open('IMS_ExternalSearch.jsp?table=IMS_ExternalDocumentSet&IMS_ExternalSystemName=%s&relationship=%s&from=%s&objectId=%s&IMS_SearchHint=%s', '_blank', 'height=800,width=1200,toolbar=0,location=0,menubar=0')",
                            "97",
                            RELATIONSHIP_IMS_QP_QPTask2Fact,
                            true,
                            qpTaskObject.getId(context),
                            HtmlEscapers.htmlEscaper().escape("Search for Document Sets")),
                    IMS_KDD_mxJPO.FUGUE_16x16 + "plus.png",
                    "Connect Document Set"));
        }

        return sb.toString();
    }

    @SuppressWarnings("unused")
    public String disconnectExternalDocumentSet(Context context, String[] args) throws Exception {
        return IMS_KDD_mxJPO.disconnect(context, args, new IMS_KDD_mxJPO.Disconnector() {
            @Override
            public String disconnect(Context context, String from, String to, String relationship) throws Exception {

                new DomainObject(from).disconnect(
                        context,
                        new RelationshipType(relationship),
                        true,
                        new DomainObject(to));

                return "";
            }
        });
    }

    public static void linkQPExpectedResultDocs(Context context, String externalSystemName, boolean testMode) throws Exception {
        String docCodeSelect = String.format("to[%s].from.attribute[%s]", RELATIONSHIP_IMS_QP_ExpectedResult2QPTask, ATTRIBUTE_IMS_QP_DocumentCode);

        MapList qpTaskMaps = DomainObject.findObjects(
                context,
                TYPE_IMS_QP_QPTask,
                "*",
                String.format("%s!=''", docCodeSelect),
                new StringList(new String[]{
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_NAME,
                        docCodeSelect
                }));

        for (Object item : qpTaskMaps) {
            Map map = (Map) item;
            String qpTaskId = (String) map.get(DomainConstants.SELECT_ID);
            String docCode = (String) map.get(docCodeSelect);
            System.out.println(String.format("%s --> %s", map.get(DomainConstants.SELECT_NAME), docCode));

            MapList externalDocMapList = IMS_ExternalSystem_mxJPO.findObjects(context, externalSystemName, docCode);

            if (externalDocMapList.size() == 1) {
                System.out.println("  Connecting...");

                String result = IMS_ExternalSystem_mxJPO.connectExternalObject(
                        context, externalSystemName,
                        RELATIONSHIP_IMS_QP_QPTask2Fact, true,
                        qpTaskId,
                        (String) ((Map) externalDocMapList.get(0)).get(DomainConstants.SELECT_ID),
                        testMode);

                if (StringUtils.isNotBlank(result)) {
                    System.out.println("    " + result);
                }
            } else if (externalDocMapList.size() == 0) {
                System.out.println("  No external documents found");
            } else {
                System.out.println("  Multiple external documents found");
            }
        }
    }

    public static void linkQPExpectedResultDocs(final Context context, final String[] args) throws Exception {
        linkQPExpectedResultDocs(context, args[0], IMS_KDD_mxJPO.isTestMode(args));
    }
}
