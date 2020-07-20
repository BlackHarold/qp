import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_Task_mxJPO {

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

    public IMS_QP_Task_mxJPO(Context context, String[] args) throws Exception {
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

    private Vector getDEPTaskRelatedObjects(Context context, String[] args, boolean in) {

        LOG.info("args: " + Arrays.deepToString(args));

        Vector result = new Vector();
        try {
            String virtualRelationship = in ? "in" : "out";
            boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);


            int counter = 1;
            for (Map map : IMS_KDD_mxJPO.getObjectListMaps(args)) {
                StringBuilder sb = new StringBuilder();
//                String rowId = IMS_KDD_mxJPO.getRowId(map);
                String id = IMS_KDD_mxJPO.getIdFromMap(map);
                LOG.info("id: " + id);

                DomainObject depTaskObject = IMS_KDD_mxJPO.idToObject(context, id);
//                boolean currentUserIsDEPOwner = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, depTaskObject);
                //TODO uncomment after test
                boolean currentUserIsDEPOwner = true;

                List<Map> relatedMaps = IMS_KDD_mxJPO.getRelatedObjectMaps(context, depTaskObject,
                        /*3*/ StringUtils.join(Arrays.asList(RELATIONSHIP_IMS_QP_DEPTask2DEPTask, RELATIONSHIP_IMS_QP_DEPTask2DEP), ','),
                        !in, Arrays.asList(/*DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu),*/
                                SELECT_DEP_ID, SELECT_DEP_NAME, SELECT_DEP_IMS_NAME, SELECT_DEP_IMS_NAME_RU,
                                "to[IMS_QP_DEPSubStage2DEPTask].from.id", "attribute[IMS_NameRu]", "attribute[IMS_Name]"), //generally rel with substage
                        null, null, false);

                LOG.info("relatedMaps: " + relatedMaps);

                String name = "";
                String rawLink = "";
                for (Map relatedMap : relatedMaps) {
                    if (sb.length() > 0) {
                        sb.append("<br/>");
                    }

                    name = (String) relatedMap.get(isRuLocale ? "attribute[IMS_NameRu]" : "attribute[IMS_Name]");
                    name = UIUtil.isNotNullAndNotEmpty(name) ? name : "error";

                    if (IMS_KDD_mxJPO.getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEPTask)) {
                        rawLink = IMS_KDD_mxJPO.getLinkHTML(context, relatedMap, SOURCE_DEPTask, null, getIconUrl(TYPE_IMS_QP_DEPTask),
                                "12px", name, null, true, false, null, true, null, false);
                    } else if (IMS_KDD_mxJPO.getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEP)) {
                        rawLink = IMS_KDD_mxJPO.getLinkHTML(context, relatedMap, SOURCE_DEP, null, getIconUrl(TYPE_IMS_QP_DEP),
                                "12px", name, null, true, false, null, true, null, false);
                    }
                    sb.append(rawLink);

                    if (currentUserIsDEPOwner) {

                        if (IMS_KDD_mxJPO.getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEPTask)) {
                            sb.append(" " + getCheckLinkHTML("IMS_QP_Task", "approveConnection", id, IMS_KDD_mxJPO.getIdFromMap(relatedMap), virtualRelationship, "Accept", IMS_KDD_mxJPO.getRefreshAllRowsFunction()));
                        } else if (IMS_KDD_mxJPO.getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEP)) {
                            sb.append(" " + getCheckLinkHTML("IMS_QP_Task", "distributionArrow", id, IMS_KDD_mxJPO.getIdFromMap(relatedMap), virtualRelationship, "Arrow", IMS_KDD_mxJPO.getRefreshAllRowsFunction()));
                        }
                        sb.append(" " + getCheckLinkHTML("IMS_QP_Task", "rejectConnection", id, IMS_KDD_mxJPO.getIdFromMap(relatedMap), virtualRelationship, "Reject", IMS_KDD_mxJPO.getRefreshAllRowsFunction()));
                    }
                }


                result.addElement(sb.toString());
            }


        } catch (Exception e) {
            try {
                emxContextUtil_mxJPO.mqlWarning(context, e.toString());
                LOG.info("error getting url string: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public String approveConnection(Context context, String[] args) throws Exception {
        LOG.info("approveConnection(): " + Arrays.deepToString(args));
        return "";
    }

    public String rejectConnection(Context context, String[] args) throws Exception {
        LOG.info("rejectConnection(): " + Arrays.deepToString(args));
        return "";
    }

    public String distributionArrow(Context context, String[] args) throws Exception {
        LOG.info("distributionArrow(): " + Arrays.deepToString(args));
        return "";
    }

    Logger LOG = Logger.getLogger("blackLogger");

    public Vector getDEPTaskInput(Context context, String[] args) throws Exception {
        return getDEPTaskRelatedObjects(context, args, true);
    }

    public Vector getDEPTaskOutput(Context context, String[] args) throws Exception {
        return getDEPTaskRelatedObjects(context, args, false);
    }

    final String COMMON_IMAGES = "../common/images/";

    /**
     * In the method can be different values of title (Accept or Reject)
     * The method accept the value of 'title' and sends it to the getCheckLinkHTML method
     * the parameter value of boolean checkTitle depends from title
     */
    public String getCheckLinkHTML(String program, String function, String fromId, String toId, String relationships, String title, String onDisconnected) {
        String FUGUE_16x16 = COMMON_IMAGES + "fugue/16x16/";
        boolean checkTitle = title.equals("Accept") ? true : false;
        String icon = "";

        switch (title) {
            case "Arrow":
                icon = "arrow.png";
                break;
            case "Accept":
                icon = "check.png";
                break;
            case "Reject":
                icon = "cross.png";

        }

        String result = getCheckLinkHTML(program, function, fromId, toId, checkTitle, relationships, FUGUE_16x16 + icon, title, onDisconnected);
        LOG.info("result: " + result);

        return result;
    }

    public String getCheckLinkHTML(String program, String function, String fromId, String toId,
                                   boolean escapeToId, String relationships, String imageUrl, /*8*/String title, String onDisconnected) {

        String linkId = UUID.randomUUID().toString();
        String spinnerId = UUID.randomUUID().toString();

        String link = String.format(
                "<img id=\"%s\" src=\"%sspinner_16x16.png\" style=\"display:none;\" />",
                HtmlEscapers.htmlEscaper().escape(spinnerId), COMMON_IMAGES) +
                String.format(
                        "<a id=\"%s\" href=\"javascript:IMS_CheckTask('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')\">" +
                                "<img src=\"%s\" title=\"%s\"/></a>",
                        /*"<a id=\"%s\" href=\"javascript:IMS_CheckTask('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')\">" +
                                "<img src=\"%s\" title=\"%s\" style=\"border: 1px solid #992211;\"/></a>",*///          <- with border
                        HtmlEscapers.htmlEscaper().escape(linkId), getEcma(program), getEcma(function), getEcma(fromId),
                        escapeToId ? getEcma(toId) : toId, getEcma(relationships), getEcma(linkId), getEcma(spinnerId),
                        onDisconnected != null && !onDisconnected.isEmpty() ? onDisconnected : "", imageUrl,
                        HtmlEscapers.htmlEscaper().escape(title));

        LOG.info("link: " + link);

        return link;
    }

    private String getEcma(String string) {
        return StringEscapeUtils.escapeEcmaScript(string);
    }

    private Vector getQPTaskRelatedObjects(Context context, String[] args, boolean in) throws Exception {
        try {
            String virtualRelationship = in ? "accept" : "reject";
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

    public Vector getQPTaskInput(Context context, String[] args) throws Exception {
        return getQPTaskRelatedObjects(context, args, true);
    }

    public Vector getQPTaskOutput(Context context, String[] args) throws Exception {
        return getQPTaskRelatedObjects(context, args, false);
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

            MapList externalDocMapList = IMS_ExternalSystem_mxJPO.findObjects(
                    context, externalSystemName, String.format(String.format("name==const'%s'", docCode)));

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
