import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_Task_mxJPO {

    private static final Logger LOG = Logger.getLogger("blackLogger");

    private static final String TYPE_IMS_QP_DEP = "IMS_QP_DEP";
    private static final String TYPE_IMS_QP_DEPTask = "IMS_QP_DEPTask";
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

    private static String getIconUrl(String type) {
        return IMS_KDD_mxJPO.COMMON_IMAGES + type + "_16x16.png";
    }

    private Vector getDEPTaskRelatedObjects(Context context, String[] args, boolean in) {

        Vector result = new Vector();
        try {
            String virtualRelationship = in ? "in" : "out";
            boolean getTo = in;
            boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);

            Map argsMap = JPO.unpackArgs(args);
            LOG.info("argsMap: " + argsMap);

            MapList argsList = (MapList) argsMap.get("objectList");

            List<Map> items = new ArrayList<>();
            for (Object o : argsList) {
                items.add((Map) o);
            }

            /*top level Codes by items*/
            StringBuilder stringBuilder = new StringBuilder();
            int counter = 1;
            for (Map map : items) {
                stringBuilder.setLength(0);


                String mainTaskID = (String) map.get("id");
                String mainLevel = (String) map.get("id[level]");
                LOG.info("id[level]=" + map.get("id[level]"));
                DomainObject objectMainTask = new DomainObject(mainTaskID);

//                boolean currentUserIsDEPOwner = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, depTaskObject);
                //TODO uncomment after test
                boolean currentUserIsDEPOwner = true;

                StringList selects = new StringList();
                selects.add("id");
                selects.add("name");
                selects.add(SELECT_DEP_ID);
                selects.add(SELECT_DEP_NAME);
                selects.add(SELECT_DEP_IMS_NAME);
                selects.add(SELECT_DEP_IMS_NAME_RU);
                selects.add("to[IMS_QP_DEPSubStage2DEPTask].from.id");
                selects.add("attribute[IMS_NameRu]");
                selects.add("attribute[IMS_Name]");
                /*INPUT getTo*/
                selects.add("to[IMS_QP_DEPTask2DEPTask].from.id");
                selects.add("to[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]");
                /*OUTPUT getFrom*/
                selects.add("from[IMS_QP_DEPTask2DEPTask].to.id");
                selects.add("from[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]");

                MapList relatedTasks = objectMainTask.getRelatedObjects(context,
                        /*relationship*/"IMS_QP_DEPTask2DEPTask,IMS_QP_DEPTask2DEP",
                        /*type*/"IMS_QP_DEP,IMS_QP_DEPTask",
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/ getTo, /*getFrom*/ !getTo,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/ 0);

                String id, name = "", rawLink = "";

                /*get all states for related tasks*/
                Map<String, String> taskStates = new HashMap<>();

                for (Object object : relatedTasks) {
                    Map relatedMap = (Map) object;
                    id = (String) relatedMap.get("id");

                    /*getting list of all related tasks with states*/
                    List<String> toId, toState, fromId, fromState;
                    if (!getTo) {
                        toId = getStringList(relatedMap.get("to[IMS_QP_DEPTask2DEPTask].from.id"));
                        toState = getStringList(relatedMap.get("to[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]"));

                        for (int i = 0; i < toId.size(); i++) {
                            taskStates.put(toId.get(i), toState.get(i));
                        }
                    }

                    if (getTo) {
                        fromId = getStringList(relatedMap.get("from[IMS_QP_DEPTask2DEPTask].to.id"));
                        fromState = getStringList(relatedMap.get("from[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]"));
                        for (int i = 0; i < fromId.size(); i++) {
                            taskStates.put(fromId.get(i), fromState.get(i));
                        }
                    }

                    /*rotate all related tasks and generating links*/
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append("<br/>");
                    }

                    name = (String) relatedMap.get(isRuLocale ? "attribute[IMS_NameRu]" : "attribute[IMS_Name]");
                    name = UIUtil.isNotNullAndNotEmpty(name) ? name : "error";

                    if (getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEPTask))
                        rawLink = getLinkHTML(relatedMap, SOURCE_DEPTask, getIconUrl(TYPE_IMS_QP_DEPTask), name);
                    else if (getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEP))
                        rawLink = getLinkHTML(relatedMap, SOURCE_DEP, getIconUrl(TYPE_IMS_QP_DEP), name);

                    stringBuilder.append(rawLink);

                    if (currentUserIsDEPOwner) {
                        //TODO brush the links red&green colors by states
                        String state = UIUtil.isNotNullAndNotEmpty(taskStates.get(mainTaskID)) ? taskStates.get(mainTaskID) : "";
                        LOG.info("[id " + id + " name " + new DomainObject(id).getName(context) + "] | [main id " + mainTaskID + " main name " + new DomainObject(mainTaskID).getName(context) + "] | state: " + state);
                        if (state.equals("Draft") || state.equals("")) {
                            if (getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEPTask)) {
//                                stringBuilder.append(" " + getCheckLinkHTML("IMS_QP_Task", "approveConnection", /*main task*/mainTaskID, id, virtualRelationship, "Accept", "function(){emxEditableTable.refreshRowByRowId(" + mainLevel + ");}"));
                                stringBuilder.append(" " + getCheckLinkHTML("IMS_QP_Task", "approveConnection", /*main task*/mainTaskID, id, virtualRelationship, "Accept", mainLevel));
                            } else if (getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEP)) {
                                stringBuilder.append(" " + getCheckLinkHTML("IMS_QP_Task", "distributionArrow", /*main task*/mainTaskID, id, virtualRelationship, "Arrow", mainLevel));
                            }
                            stringBuilder.append(" " + getCheckLinkHTML("IMS_QP_Task", "rejectConnection", /*main task*/mainTaskID, id, virtualRelationship, "Reject", mainLevel));
                        }
                    }
                }

                result.addElement(stringBuilder.toString());
            }

        } catch (Exception e) {
            try {
                emxContextUtil_mxJPO.mqlWarning(context, e.toString());
                LOG.info("result link: " + result);
                LOG.info("error getting url string: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public StringList getStringList(Object object) {
        StringList list = new StringList();
        try {
            if (object instanceof String) {
                list = new StringList((String) object);
            }
            if (object instanceof StringList) {
                list = (StringList) object;
            }
        } catch (ClassCastException cce) {
            LOG.info("error getting StringList: " + object.getClass().getCanonicalName() + " message: " + cce.getMessage());
        }
        return list;
    }

    public String getLinkHTML(Map objectMap, String source, String imageURL, String title) throws Exception {
        String fontSize = "12px";
        String id = (String) objectMap.get("id");
        String name = (String) objectMap.get("name");

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("<div style=\"display:none;\">%1$s</div>", name));

        StringBuilder onDragStart = new StringBuilder();

        onDragStart.append(String.format(" ondragstart=\"event.dataTransfer.setData('objectId', '%s');", id));

        if (source != null)
            onDragStart.append(String.format("event.dataTransfer.setData('source', '%s');\"", HtmlEscapers.htmlEscaper().escape(source)));

        if (imageURL != null)
            sb.append(String.format("<img src='%s'%s/>", HtmlEscapers.htmlEscaper().escape(imageURL), onDragStart));

        String style = fontSize != null ? String.format(" style=\"font-size: %s\"", fontSize) : "";
        String titleHTML = title != null ? String.format(" title=\"%s\"", HtmlEscapers.htmlEscaper().escape(title.replace("&#10;", "|")).replace("|", "&#10;")) : "";

        sb.append(String.format("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=%2$s')\"%3$s%4$s%5$s>%1$s</a>",
                HtmlEscapers.htmlEscaper().escape(name), id, onDragStart, style, titleHTML));

        return sb.toString();
    }

    public String approveConnection(Context context, String[] args) {
        LOG.info("in approve connection");
        setAttributeDepTaskStaus(context, "Approved", args);
        //TODO return reload that row
        return "";
    }

    public String rejectConnection(Context context, String[] args) {
        LOG.info("in reject connection");
        setAttributeDepTaskStaus(context, "Rejected", args);
        //TODO return reload that row
        return "";
    }

    private void setAttributeDepTaskStaus(Context context, String check, String... args) {

        String mainTaskID = args[0];
        String taskID = args[1];
        String route = args[2];

        Map currentRelationship = null;
        String relationshipID = "";

        try {
            DomainObject mainTask = new DomainObject(mainTaskID);

            StringList relselects = new StringList("id");
            relselects.add("attribute[IMS_QP_DEPTaskStatus]");

            route = route.equals("in") ? "from" : "to";
            boolean getTo = route.equals("from");

            /*getting relationship between main task & current task*/
            MapList currentTask = mainTask.getRelatedObjects(context,
                    /*relationship*/"IMS_QP_DEPTask2DEPTask",
                    /*type*/"IMS_QP_DEPTask",
                    /*object attributes*/ null,
                    /*relationship selects*/ relselects,
                    /*getTo*/ getTo, /*getFrom*/ !getTo,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ /*"from[IMS_QP_DEPTask2DEPTask].from.id==" + taskID*/null,
                    /*relationship where*/ route + ".id==" + taskID,
                    /*limit*/ 0);
            currentRelationship = (Map) currentTask.get(0);

            /*getting relationship ID and set to the attribute*/
            relationshipID = UIUtil.isNotNullAndNotEmpty((String) currentRelationship.get("id")) ? (String) currentRelationship.get("id") : "";
            LOG.info(relationshipID + " getting to: " + check);

            if (relationshipID.equals("")) throw new FrameworkException("error: relationship id is empty");

            DomainRelationship relationship = new DomainRelationship(relationshipID);
            LOG.info("before: " + relationship.getAttributeValue(context, "IMS_QP_DEPTaskStatus"));
            relationship.setAttributeValue(context, "IMS_QP_DEPTaskStatus", check);
            LOG.info("after: " + relationship.getAttributeValue(context, "IMS_QP_DEPTaskStatus"));

        } catch (FrameworkException fe) {
            LOG.error(check + "operation caused error: " + fe.getMessage());
        } catch (Exception e) {
            LOG.error("error getting domain object with ID: " + mainTaskID + " message: " + e.getMessage());
        }
    }

    public String distributionArrow(Context context, String[] args) throws Exception {
        LOG.error("distributionArrow(): " + Arrays.deepToString(args));
        return "false";
    }

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
    public String getCheckLinkHTML(String program, String function, String fromId, String toId, String
            relationships, String title, String idLevel) {
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

        String result = getCheckLinkHTML(program, function, fromId, toId, checkTitle, relationships, FUGUE_16x16 + icon, title, idLevel);

        return result;
    }

    public String getCheckLinkHTML(String program, String function, String fromId, String toId,
                                   boolean escapeToId, String relationships, String imageUrl, /*8*/String title, String onDisconnected) {

        String linkId = UUID.randomUUID().toString();
        String spinnerId = UUID.randomUUID().toString();

//        LOG.info("toId: " + toId + " ToId: " + escapeToId + " fromId: " + fromId);

        String link = String.format(
                "<img id=\"%s\" src=\"%sspinner_16x16.png\" style=\"display:none;\" />",
                HtmlEscapers.htmlEscaper().escape(spinnerId), COMMON_IMAGES) +
                String.format(
                        "<a id=\"%s\" href=\"javascript:IMS_CheckTask('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')\">" +
                                "<img src=\"%s\" title=\"%s\"/></a>",
                        /*"<a id=\"%s\" href=\"javascript:IMS_CheckTask('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')\">" +
                                "<img src=\"%s\" title=\"%s\" style=\"border: 1px solid #992211;\"/></a>",*///          <- with borders
                        HtmlEscapers.htmlEscaper().escape(linkId), getEcma(program), getEcma(function), getEcma(fromId),
                        escapeToId ? getEcma(toId) : toId, getEcma(relationships), getEcma(linkId), getEcma(spinnerId),
                        onDisconnected != null && !onDisconnected.isEmpty() ? onDisconnected : "", imageUrl,
                        HtmlEscapers.htmlEscaper().escape(title));

//        LOG.info("link: " + link);

        return link;
    }

    private String getEcma(String string) {
        return StringEscapeUtils.escapeEcmaScript(string);
    }

    private String getTypeFromMap(Object obj) {
        return obj != null ? (String) ((Map) obj).get(DomainConstants.SELECT_TYPE) : null;
    }
}
