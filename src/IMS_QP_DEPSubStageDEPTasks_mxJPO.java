import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.Context;
import matrix.db.RelationshipType;
import matrix.util.StringList;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class IMS_QP_DEPSubStageDEPTasks_mxJPO {

    private static final String PROGRAM_IMS_QP_DEPSubStageDEPTasks = "IMS_QP_DEPSubStageDEPTasks";

    private static final String TYPE_IMS_QP = "IMS_QP";
    private static final String TYPE_IMS_QP_DEP = "IMS_QP_DEP";
    private static final String TYPE_IMS_QP_DEPProjectStage = "IMS_QP_DEPProjectStage";
    private static final String TYPE_IMS_QP_DEPSubStage = "IMS_QP_DEPSubStage";
    private static final String TYPE_IMS_QP_DEPTask = "IMS_QP_DEPTask";

    private static final String RELATIONSHIP_IMS_QP_QP2DEP = "IMS_QP_QP2DEP";
    private static final String RELATIONSHIP_IMS_QP_DEP2DEPProjectStage = "IMS_QP_DEP2DEPProjectStage";
    private static final String RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage = "IMS_QP_DEPProjectStage2DEPSubStage";
    private static final String RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask = "IMS_QP_DEPSubStage2DEPTask";
    private static final String RELATIONSHIP_IMS_QP_DEPTask2DEPTask = "IMS_QP_DEPTask2DEPTask";
    private static final String RELATIONSHIP_IMS_QP_DEPTask2DEP = "IMS_QP_DEPTask2DEP";

    private static final String ATTRIBUTE_IMS_Name = "IMS_Name";
    private static final String ATTRIBUTE_IMS_NameRu = "IMS_NameRu";

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

    private static final String SOURCE_DEP = "D_E_P"; // Because source lists are checked using indexOf
    private static final String SOURCE_DEPTask = "DEPTask";

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
                    context, TYPE_IMS_QP, "*", "*", "*", "*",
                    null,true,
                    getQPTreeSelects());

            IMS_KDD_mxJPO.sortMapsByName(mapList);
            return mapList;
        }
        catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.getMessage());
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
                                    RELATIONSHIP_IMS_QP_QP2DEP,
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
                    null, null, false);

            IMS_KDD_mxJPO.sortMapsByName(maps);
            return new MapList(maps);
        }
        catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.getMessage());
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
        }
        catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.getMessage());
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
        }
        catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.getMessage());
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

    private Vector getRelatedDEPTasks(Context context, String[] args, boolean in) throws Exception {
        try {
            String virtualRelationship = in ? "in" : "out";
            boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);

            Vector results = new Vector();

            for (Map map : IMS_KDD_mxJPO.getObjectListMaps(args)) {
                StringBuilder sb = new StringBuilder();
                String rowId = IMS_KDD_mxJPO.getRowId(map);
                String id = IMS_KDD_mxJPO.getIdFromMap(map);

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

                List<Map> relatedMaps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                        context, IMS_KDD_mxJPO.idToObject(context, id),
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

                    sb.append(IMS_KDD_mxJPO.getDisconnectLinkHTML(
                            PROGRAM_IMS_QP_DEPSubStageDEPTasks, "disconnectDEPTask",
                            id, IMS_KDD_mxJPO.getIdFromMap(relatedMap),
                            virtualRelationship,
                            "Disconnect",
                            IMS_KDD_mxJPO.getRefreshAllRowsFunction()));

                    if (IMS_KDD_mxJPO.getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEPTask)) {
                        Map depMap = new HashMap();
                        depMap.put(DomainConstants.SELECT_ID, relatedMap.get(SELECT_DEP_ID));
                        depMap.put(DomainConstants.SELECT_NAME, relatedMap.get(SELECT_DEP_NAME));

                        sb.append(IMS_KDD_mxJPO.getLinkHTML(
                                context, depMap, SOURCE_DEP, null,
                                getIconUrl(TYPE_IMS_QP_DEP),
                                "12px",
                                /*(String) relatedMap.get(
                                        isRuLocale ?
                                                SELECT_DEP_IMS_NAME_RU :
                                                SELECT_DEP_IMS_NAME)*/"",
                                null, true, false, null, true, null, false));

                        sb.append(String.format(
                                "&#160;&#160;<img src=\"%s\" />&#160;",
                                IMS_KDD_mxJPO.FUGUE_16x16 + "arrow.png"));

                        sb.append(IMS_KDD_mxJPO.getLinkHTML(
                                context, relatedMap, SOURCE_DEPTask, null,
                                getIconUrl(TYPE_IMS_QP_DEPTask),
                                "12px",
                                /*(String) relatedMap.get(DomainObject.getAttributeSelect(
                                        isRuLocale ?
                                                ATTRIBUTE_IMS_NameRu :
                                                ATTRIBUTE_IMS_Name))*/"",
                                null, true, false, null, true, null, false));
                    }
                    else if (IMS_KDD_mxJPO.getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEP)) {
                        sb.append(IMS_KDD_mxJPO.getLinkHTML(
                                context, relatedMap, SOURCE_DEP, null,
                                getIconUrl(TYPE_IMS_QP_DEP),
                                "12px",
                                /*(String) relatedMap.get(DomainObject.getAttributeSelect(
                                        isRuLocale ?
                                                ATTRIBUTE_IMS_NameRu :
                                                ATTRIBUTE_IMS_Name))*/"",
                                null, true, false, null, true, null, false));
                    }
                }

                results.addElement(sb.toString());
            }

            return results;
        }
        catch (Exception e) {
            emxContextUtil_mxJPO.mqlWarning(context, e.getMessage());
            throw e;
        }
    }

    @SuppressWarnings("unused")
    public Vector getDEPTaskInDEPTasks(Context context, String[] args) throws Exception {
        return getRelatedDEPTasks(context, args, true);
    }

    @SuppressWarnings("unused")
    public Vector getDEPTaskOutDEPTasks(Context context, String[] args) throws Exception {
        return getRelatedDEPTasks(context, args, false);
    }

    @SuppressWarnings("unused")
    public String connectDEPTask(Context context, String[] args) throws Exception {
        return IMS_KDD_mxJPO.connect(context, args, new IMS_KDD_mxJPO.Connector() {
            @Override
            public String connect(Context context, String from, String to, String relationship) throws Exception {

                DomainObject toObject = new DomainObject(to);

                IMS_KDD_mxJPO.connectIfNotConnected(
                        context,
                        toObject.getType(context).equals(TYPE_IMS_QP_DEPTask) ?
                                RELATIONSHIP_IMS_QP_DEPTask2DEPTask :
                                RELATIONSHIP_IMS_QP_DEPTask2DEP,
                        new DomainObject(from),
                        toObject);

                return "";
            }
        });
    }

    @SuppressWarnings("unused")
    public String disconnectDEPTask(Context context, String[] args) throws Exception {
        return IMS_KDD_mxJPO.disconnect(context, args, new IMS_KDD_mxJPO.Disconnector() {
            @Override
            public String disconnect(Context context, String from, String to, String relationship) throws Exception {

                DomainObject toObject = new DomainObject(to);

                new DomainObject(from).disconnect(
                        context,
                        new RelationshipType(toObject.getType(context).equals(TYPE_IMS_QP_DEPTask) ?
                                RELATIONSHIP_IMS_QP_DEPTask2DEPTask :
                                RELATIONSHIP_IMS_QP_DEPTask2DEP),
                        !relationship.equals("in"),
                        toObject);

                return "";
            }
        });
    }
}