import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_Task_mxJPO {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public static String getIconUrl(String type) {
        return IMS_KDD_mxJPO.COMMON_IMAGES + type + "_16x16.png";
    }

    /**
     * Method to show the table of related with DEP-object IMS_QP_DEPTask elements
     */
    public MapList getAllRelatedTasksForDistributionButton(Context context, String[] args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");

        StringList selects = new StringList("id");

        //get all tasks
        MapList result = DomainObject.findObjects(context,
                /*type*/"IMS_QP_DEPTask",
                "eService Production",
                /*where*/"to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_DEPProjectStage2DEPSubStage].from.to[IMS_QP_DEP2DEPProjectStage].from.id==" + objectId,
                /*selects*/ selects);

        return result;
    }

    public Vector getDEPTaskRelatedObjects(Context context, String[] args, boolean in) {

        Vector result = new Vector();
        try {
            String virtualRelationship = in ? "in" : "out";
            boolean getTo = in;
            boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);

            Map argsMap = JPO.unpackArgs(args);
            MapList argsList = (MapList) argsMap.get("objectList");

            List<Map> items = new ArrayList<>();
            List<String> mainTaskIDs = new ArrayList<>();
            for (Object o : argsList) {
                items.add((Map) o);

                Map rawMap = (Map) o;
                mainTaskIDs.add((String) rawMap.get("id"));
            }

            /*top level Codes by items*/
            StringBuilder stringBuilder = new StringBuilder();

            //upper DEPs
            for (Map map : items) {
                stringBuilder.setLength(0);


                String mainTaskID = (String) map.get("id");
                String mainLevel = (String) map.get("id[level]");
                DomainObject objectMainTask = new DomainObject(mainTaskID);

                boolean currentUserIsDEPOwner = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, objectMainTask);

                StringList selects = new StringList();
                selects.add("id");
                selects.add("name");
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_ID);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_NAME);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_IMS_NAME);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_IMS_NAME_RU);
                selects.add("to[IMS_QP_DEPSubStage2DEPTask].from.id");
                selects.add("attribute[IMS_NameRu]");
                selects.add("attribute[IMS_Name]");
                /*INPUT getTo*/
                selects.add("to[IMS_QP_DEPTask2DEPTask].from.id");
                selects.add("to[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]");
                selects.add("to[IMS_QP_DEPTask2DEPTask].owner");
                /*OUTPUT getFrom*/
                selects.add("from[IMS_QP_DEPTask2DEPTask].to.id");
                selects.add("from[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]");
                selects.add("from[IMS_QP_DEPTask2DEPTask].owner");
                /*OUTPUT getDepFrom*/
                selects.add("from[IMS_QP_DEPTask2DEP].to.id");
                selects.add("from[IMS_QP_DEPTask2DEP].attribute[IMS_QP_DEPTaskStatus]");

                String relationships = getTo ? "IMS_QP_DEPTask2DEPTask,IMS_QP_DEPTask2DEP" : "IMS_QP_DEPTask2DEPTask";
                String types = getTo ? "IMS_QP_DEP,IMS_QP_DEPTask" : "IMS_QP_DEPTask";

                MapList relatedTasks = objectMainTask.getRelatedObjects(context,
                        /*relationship*/relationships,
                        /*type*/types,
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/ getTo, /*getFrom*/ !getTo,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/ 0);

                String id, name, rawLink = "";

                /*get all states for related tasks*/
                Map<String, String> taskStates = new HashMap<>();
                Map<String, String> relationshipOwners = new HashMap<>();

                //in-out DEPTask
                for (Object object : relatedTasks) {
                    Map relatedMap = (Map) object;
                    id = (String) relatedMap.get("id");

                    /*getting list of all related tasks with states*/
                    List<String> toId, toState, toRelationshipOwner, fromId, fromState, fromRelationshipOwner, fromDepId, fromDepState;
                    if (!getTo) {

                        toId = getStringList(relatedMap.get("to[IMS_QP_DEPTask2DEPTask].from.id"));
                        toState = getStringList(relatedMap.get("to[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]"));
                        toRelationshipOwner = getStringList(relatedMap.get("to[IMS_QP_DEPTask2DEPTask].owner"));

                        for (int i = 0; i < toId.size(); i++) {
                            taskStates.put(toId.get(i), toState.get(i));
                            relationshipOwners.put(toId.get(i), toRelationshipOwner.get(i));
                        }
                    }

                    if (getTo) {

                        fromId = getStringList(relatedMap.get("from[IMS_QP_DEPTask2DEPTask].to.id"));
                        fromState = getStringList(relatedMap.get("from[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]"));
                        fromRelationshipOwner = getStringList(relatedMap.get("from[IMS_QP_DEPTask2DEPTask].owner"));

                        for (int i = 0; i < fromId.size(); i++) {
                            taskStates.put(fromId.get(i), fromState.get(i));
                            relationshipOwners.put(fromId.get(i), fromRelationshipOwner.get(i));
                        }

                        fromDepId = getStringList(relatedMap.get("from[IMS_QP_DEPTask2DEP].to.id"));
                        fromDepState = getStringList(relatedMap.get("from[IMS_QP_DEPTask2DEP].attribute[IMS_QP_DEPTaskStatus]"));

                        for (int i = 0; i < fromDepId.size(); i++) {
                            taskStates.put(fromDepId.get(i), fromDepState.get(i));
                        }
                    }

                    String state = UIUtil.isNotNullAndNotEmpty(taskStates.get(mainTaskID)) ? taskStates.get(mainTaskID) : "";
                    String hidden = state.equals("Rejected") ? " hidden=\"\"" : "";
                    stringBuilder.append("<div" + hidden + ">");

                    name = (String) relatedMap.get(isRuLocale ? "attribute[IMS_NameRu]" : "attribute[IMS_Name]");
                    name = UIUtil.isNotNullAndNotEmpty(name) ? name : "error";

                    if (getTypeFromMap(relatedMap).equals(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask))
                        rawLink = getLinkHTML(relatedMap, IMS_QP_Constants_mxJPO.SOURCE_DEPTask, getIconUrl(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask), name, state);
                    else if (getTypeFromMap(relatedMap).equals(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP))
                        rawLink = getLinkHTML(relatedMap, IMS_QP_Constants_mxJPO.SOURCE_DEP, getIconUrl(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP), name, state);

                    stringBuilder.append(rawLink);

                    /* check if the current user is the dep owner, if owner/originator of the task*/

                    //1. main task is mine
                    boolean currentUserIsMainTaskOwner = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, objectMainTask);

                    //2. task is not mine
                    DomainObject taskObject = new DomainObject(id);
                    boolean currentUserIsTaskOwner = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, taskObject);

                    //3. main task owner is not me
                    String relationshipOwner = UIUtil.isNotNullAndNotEmpty(relationshipOwners.get(mainTaskID)) ? relationshipOwners.get(mainTaskID) : "";
                    String contextUser = context.getUser();
                    boolean currentUserContextIsDEPTaskOwner = contextUser.equals(relationshipOwner);

                    LOG.info("context user:" + contextUser + "|" +
                            objectMainTask.getName(context) +
                            " is owner:" + currentUserIsMainTaskOwner + "|" +
                            taskObject.getName(context) +
                            " is task owner:" + currentUserIsTaskOwner + "|" +
                            relationshipOwner +
                            " is relation owner: " + currentUserContextIsDEPTaskOwner);

                    if (currentUserIsMainTaskOwner && !currentUserIsTaskOwner && !currentUserContextIsDEPTaskOwner) {
                        if (!mainTaskIDs.contains(id) && (state.equals("Draft") || state.equals(""))) {
                            if (getTypeFromMap(relatedMap).equals(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask) && !map.get("type").equals("IMS_QP_DEP")) {
                                stringBuilder.append(" " + getCheckLinkHTML("IMS_QP_Task", "approveConnection", /*main task*/mainTaskID, id, virtualRelationship, "Accept", mainLevel));
                            } else {
                                stringBuilder.append(" " + getDistributeHTMLLink(getCheckLinkHTML(null, "distributionArrow", /*main task*/mainTaskID, id, null, "Distribute task", null)));
                            }
                            stringBuilder.append(" " + getCheckLinkHTML("IMS_QP_Task", "rejectConnection", /*main task*/mainTaskID, id, virtualRelationship, "Reject", mainLevel));
                        }
                    }
                    stringBuilder.append("</div>");
                }
                String element = FrameworkUtil.findAndReplace(stringBuilder.toString(), "&", "&amp;");
                result.addElement(element);
            }

        } catch (Exception e) {
            try {
                emxContextUtil_mxJPO.mqlWarning(context, e.toString());
                LOG.error("result link: " + result);
                LOG.error("error getting url string: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public String getDistributeHTMLLink(String rawLink) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(rawLink);
        return stringBuilder.toString();
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
            LOG.error("error getting StringList: " + object.getClass().getCanonicalName() + " message: " + cce.getMessage());
        }
        return list;
    }

    public String getLinkHTML(Map objectMap, String source, String imageURL, String title, String state) {
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

        String fontSize = "12px";
        String textDecoration = !state.equals("Rejected") ? "none" : "line-through";
        String color = state.equals("Approved") ? "darkgreen" : "";
        color = state.equals("Rejected") ? "grey" : color;
        String style = String.format(" style=\"font-size: %s; text-decoration: %s; color: %s;\"", fontSize, textDecoration, color);

        String titleHTML = title != null ? String.format(" title=\"%s\"", HtmlEscapers.htmlEscaper().escape(title.replace("&#10;", "|")).replace("|", "&#10;")) : "";

        sb.append(String.format("<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=%2$s')\"%3$s%4$s%5$s>%1$s</a>",
                HtmlEscapers.htmlEscaper().escape(name), id, onDragStart, style, titleHTML));

        return sb.toString();
    }

    public String approveConnection(Context context, String[] args) {
        setAttributeDepTaskStatus(context, "Approved", args);
        return "";
    }

    public String rejectConnection(Context context, String[] args) {
        setAttributeDepTaskStatus(context, "Rejected", args);
        return "";
    }

    public void setAttributeDepTaskStatus(Context context, String check, String... args) {

        String mainTaskID = args[0];
        String taskID = args[1];
        String route = args[2];

        /*change attribute state*/
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
                    /*relationship*/"IMS_QP_DEPTask2DEPTask,IMS_QP_DEPTask2DEP",
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

            if (relationshipID.equals("")) throw new FrameworkException("error: relationship id is empty");

            DomainRelationship relationship = new DomainRelationship(relationshipID);
            relationship.setAttributeValue(context, "IMS_QP_DEPTaskStatus", check);

        } catch (FrameworkException fe) {
            LOG.error(check + "operation caused error: " + fe.getMessage());
        } catch (Exception e) {
            LOG.error("error getting domain object with ID: " + mainTaskID + " message: " + e.getMessage());
        }

        /*add history comment*/
        try {

            String login = context.getUser();
            ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),
                    DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

            /*history to task*/
            String exec = String.format("mod bus %s add history modify comment %s", taskID,
                    "\"attribute task status setting: \'" + check + "\' for relationship to the object named: " +
                            new DomainObject(mainTaskID).getName(context) + " for route \'" + route + "\' by user: " + login + "\"");
            MQLCommand.exec(context, exec);

            /*history to main task*/
            exec = String.format("mod bus %s add history modify comment %s", mainTaskID,
                    "\"attribute task status setting: \'" + check + "\' for relationship to the object named: " +
                            new DomainObject(taskID).getName(context) + " for route \'" + route + "\' by user: " + login + "\"");
            MQLCommand.exec(context, exec);

            LOG.info("Coordination event - : \'" + new DomainObject(taskID).getName(context) +
                    "\' task status: \'" + check + "\' route: \'" + route + "\' related task: \'" +
                    new DomainObject(mainTaskID).getName(context) + "\' by user: " + login + "");

            ContextUtil.popContext(context);

        } catch (MatrixException me) {
            LOG.error("framework exception: " + me.getMessage());
            me.printStackTrace();
        } catch (Exception e) {
            LOG.error("error DomainObject initialisation: " + e.getMessage());
            e.printStackTrace();
        }
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
            case "Distribute task":
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

        String link = String.format(
                "<img id=\"%s\" src=\"%sspinner_16x16.png\" style=\"display:none;\" />",
                HtmlEscapers.htmlEscaper().escape(spinnerId), COMMON_IMAGES) +
                String.format(
                        "<a id=\"%s\" href=\"javascript:IMS_CheckTask('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')\">" +
                                "<img src=\"%s\" title=\"%s\"/></a>",
                        //with borders ->
                        /*"<a id=\"%s\" href=\"javascript:IMS_CheckTask('%s', '%s', '%s', '%s', '%s', '%s', '%s', '%s')\">" +
                                "<img src=\"%s\" title=\"%s\" style=\"border: 1px solid #992211;\"/></a>",*/
                        HtmlEscapers.htmlEscaper().escape(linkId), getEcma(program), getEcma(function), getEcma(fromId),
                        escapeToId ? getEcma(toId) : toId, getEcma(relationships), getEcma(linkId), getEcma(spinnerId),
                        onDisconnected != null && !onDisconnected.isEmpty() ? onDisconnected : "", imageUrl,
                        HtmlEscapers.htmlEscaper().escape(title));

        return link;
    }

    public String getEcma(String string) {
        return StringEscapeUtils.escapeEcmaScript(string);
    }

    public String getTypeFromMap(Object obj) {
        return obj != null ? (String) ((Map) obj).get(DomainConstants.SELECT_TYPE) : null;
    }

    /**
     * use for creation relationship.
     *
     * @param context
     * @param args
     * @return
     */
    public HashMap generateRelIN_OUT(Context context,
                                     String[] args
    ) {
        HashMap returnMap = new HashMap();
        StringBuilder message = new StringBuilder();

        try {
            HashMap<String, Object> jpoArgs = JPO.unpackArgs(args);
            String[] qpTaskIds = (String[]) jpoArgs.get("emxTableRowId");
            String objectId = (String) jpoArgs.get("objectId");
            Hashtable idTasks = new DomainObject(objectId).getBusinessObjectData(context, new StringList("from[IMS_QP_QPlan2QPTask].to.id"));
            StringList qpTaskIdsList = (StringList) idTasks.get("from[IMS_QP_QPlan2QPTask].to.id");

            StringList select = new StringList();
            select.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.name");
            select.add(DomainObject.SELECT_ID);
            select.add(DomainObject.SELECT_NAME);

            select.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask + "]." +
                    "from.to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2DEPTask + "|attribute[IMS_QP_DEPTaskStatus]=='Approved']." +
                    "from.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask + "].to.id");
            select.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask + "]." +
                    "from.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2DEPTask + "|attribute[IMS_QP_DEPTaskStatus]=='Approved']." +
                    "to.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask + "].to.id");

            StringList selectQP = new StringList();
            selectQP.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.name");
            selectQP.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].from.id");
            selectQP.add("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].to.id");
            selectQP.add(DomainObject.SELECT_ID);
            selectQP.add(DomainObject.SELECT_NAME);

            MapList mapParent = DomainObject.getInfo(context, qpTaskIds, select);

            for (Object obj : mapParent) {
                Map objMap = (Map) obj;
                String idQPTaskIN = (String) objMap.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask + "]." +
                        "from.to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2DEPTask + "]." +
                        "from.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask + "].to.id");
                String idQPTaskOUT = (String) objMap.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask + "]." +
                        "from.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2DEPTask + "]." +
                        "to.from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask + "].to.id");

                String[] idQPTaskINArray = (idQPTaskIN != null && !idQPTaskIN.isEmpty()) ? idQPTaskIN.split(IMS_QP_Constants_mxJPO.BELL_DELIMITER) : new String[0];
                String[] idQPTaskOUTArray = (idQPTaskOUT != null && !idQPTaskOUT.isEmpty()) ? idQPTaskOUT.split(IMS_QP_Constants_mxJPO.BELL_DELIMITER) : new String[0];

                String nameQP = (String) objMap.get(DomainObject.SELECT_NAME);
                String idQPTask = (String) objMap.get(DomainObject.SELECT_ID);

                MapList mapQPTaskIN = DomainObject.getInfo(context, idQPTaskINArray, selectQP);
                MapList mapQPTaskOUT = DomainObject.getInfo(context, idQPTaskOUTArray, selectQP);

                setRelations(context, qpTaskIdsList, mapQPTaskIN, nameQP, idQPTask, message, "IN");
                setRelations(context, qpTaskIdsList, mapQPTaskOUT, nameQP, idQPTask, message, "OUT");
            }

            if (message.length() == 0) {
                message.append("IN & OUT tasks were not found!");
            }

            returnMap.put("message", message.toString());
        } catch (Exception ex) {
            returnMap.put("message", ex.getMessage());
        }
        return returnMap;
    }

    /**
     * sets rel between 2 steps of QP
     *
     * @param context
     * @param mapQPTask
     * @param nameQP
     * @param idQPTask
     * @param message
     * @param line
     * @throws Exception
     */
    private void setRelations(Context context,
                              StringList qpTaskIdsList,
                              MapList mapQPTask,
                              String nameQP,
                              String idQPTask,
                              StringBuilder message,
                              String line
    ) throws Exception {
        for (Object objQPTask : mapQPTask) {
            Map objMapQPTask = (Map) objQPTask;
            String qpPlanName = (String) objMapQPTask.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.name");
            String idTask = (String) objMapQPTask.get(DomainObject.SELECT_ID);
            String nameTask = (String) objMapQPTask.get(DomainObject.SELECT_NAME);
            String inQPTaskId = (String) objMapQPTask.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].from.id");
            String outQPTaskId = (String) objMapQPTask.get("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].to.id");

            if (!nameQP.equals(qpPlanName)) {
                if ("IN".equals(line) && !(UIUtil.isNotNullAndNotEmpty(outQPTaskId) && outQPTaskId.contains(idQPTask))) {
                    DomainRelationship inRel = DomainRelationship.connect(context, new DomainObject(idTask), IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask, new DomainObject(idQPTask));
                    if (qpTaskIdsList.contains(idTask))
                        inRel.setAttributeValue(context, "IMS_QP_DEPTaskStatus", "Approved");
                    message.append("from " + nameTask + " to " + nameQP + " connection was build \n");
                } else if ("OUT".equals(line) && !(UIUtil.isNotNullAndNotEmpty(inQPTaskId) && inQPTaskId.contains(idQPTask))) {
                    DomainRelationship outRel = DomainRelationship.connect(context, new DomainObject(idQPTask), IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask, new DomainObject(idTask));
                    if (qpTaskIdsList.contains(idTask))
                        outRel.setAttributeValue(context, "IMS_QP_DEPTaskStatus", "Approved");
                    message.append("from " + nameQP + " to " + nameTask + " connection was build \n");
                }
            }
        }
    }

    public Vector getQPTaskInput(Context context, String[] args) throws Exception {
        return getQPTaskRelatedObjects(context, args, true);
    }

    public Vector getQPTaskOutput(Context context, String[] args) throws Exception {
        return getQPTaskRelatedObjects(context, args, false);
    }


    private int counterGeneralIn = 0, counterGeneralOut = 0;
    private int counterDraftIn = 0, counterDraftOut = 0;
    private int counterApprovedIn = 0, counterApprovedOut = 0;
    private int counterRejectedIn = 0, counterRejectedOut = 0;
    public Vector getQPTaskRelatedObjects(Context context, String[] args, boolean in) {

        Vector result = new Vector();
        try {

            Map argsMap = JPO.unpackArgs(args);
            MapList argsList = (MapList) argsMap.get("objectList");

            List<Map> items = new ArrayList<>();
            for (Object o : argsList) {
                items.add((Map) o);
            }

            /*top level Codes by items*/
            StringBuilder stringBuilder = new StringBuilder();
            for (Map map : items) {
                counterGeneralIn = 0;
                counterGeneralOut = 0;
                counterDraftIn = 0;
                counterDraftOut = 0;
                counterApprovedIn = 0;
                counterApprovedOut = 0;
                counterRejectedIn = 0;
                counterRejectedOut = 0;
                stringBuilder.setLength(0);

                String mainTaskID = (String) map.get("id");
                DomainObject objectMainTask = new DomainObject(mainTaskID);

                boolean currentUserIsQPlanOwner = IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(context, mainTaskID);

                StringList selects = new StringList();
                selects.add("id");
                selects.add("name");
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_ID);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_NAME);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_IMS_NAME);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_IMS_NAME_RU);
                selects.add("to[IMS_QP_DEPSubStage2DEPTask].from.id");
                /*INPUT getTo*/
                selects.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].from.id");
                selects.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].attribute[IMS_QP_DEPTaskStatus]");
                /*OUTPUT getFrom*/
                selects.add("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].to.id");
                selects.add("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].attribute[IMS_QP_DEPTaskStatus]");


                String relationships = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask;
                String types = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;

                MapList relatedTasks = objectMainTask.getRelatedObjects(context,
                        /*relationship*/relationships,
                        /*type*/types,
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/ in, /*getFrom*/ !in,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/ 0);

                String id, name, rawLink = "";

                /*get all states for related tasks*/
                Map<String, String> taskStates = new HashMap<>();

                for (Object object : relatedTasks) {
                    Map relatedMap = (Map) object;
                    id = (String) relatedMap.get("id");

                    /*getting list of all related tasks with states*/
                    List<String> toId, toState, fromId, fromState;
                    if (!in) {
                        toId = getStringList(relatedMap.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].from.id"));
                        toState = getStringList(relatedMap.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].attribute[IMS_QP_DEPTaskStatus]"));
                        for (int i = 0; i < toId.size(); i++) {
                            taskStates.put(toId.get(i), toState.get(i));
                        }
                    }

                    if (in) {
                        fromId = getStringList(relatedMap.get("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].to.id"));
                        fromState = getStringList(relatedMap.get("from[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask + "].attribute[IMS_QP_DEPTaskStatus]"));
                        for (int i = 0; i < fromId.size(); i++) {
                            taskStates.put(fromId.get(i), fromState.get(i));
                        }
                    }

                    String state = UIUtil.isNotNullAndNotEmpty(taskStates.get(mainTaskID)) ? taskStates.get(mainTaskID) : "";

                    name = "";

                    if (getTypeFromMap(relatedMap).equals(IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask)) {
                        incrementCounter(state, in);
                    } else if (getTypeFromMap(relatedMap).equals(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP)) {
                        rawLink = getLinkHTML(relatedMap, IMS_QP_Constants_mxJPO.SOURCE_DEP, getIconUrl(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP), name, state);
                    }

                    stringBuilder.append(rawLink);
                }

                stringBuilder.append("<div align=\"right\" style=\"color: grey; font-size: .8em;\">" + (in ? counterGeneralIn : counterGeneralOut) + "</div>");
                stringBuilder.append("<div align=\"center\">");
                stringBuilder.append("<span style=\"color: green\">" + (in ? counterApprovedIn : counterApprovedOut) + "</span>");
                stringBuilder.append("<span style=\"color: #3264C8; padding: 0 .5em 0 .5em; font-size: 1.5em\"> " + (in ? counterDraftIn : counterDraftOut) + " </span>");
                stringBuilder.append("<span style=\"color: red;\">" + (in ? counterRejectedIn : counterRejectedOut) + "</span>");
                stringBuilder.append("</div>");

                result.addElement(FrameworkUtil.findAndReplace(stringBuilder.toString(), "&", "&amp;"));
            }

        } catch (Exception e) {
            try {
                emxContextUtil_mxJPO.mqlWarning(context, e.toString());
                LOG.error("result link: " + result);
                LOG.error("error getting url string: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    private void incrementCounter(String state, boolean in) {
        if (in) {
            switch (state) {
                case "Draft":
                    counterDraftIn++;
                    break;
                case "Approved":
                    counterApprovedIn++;
                    break;
                case "Rejected":
                    counterRejectedIn++;
                    break;
            }
            counterGeneralIn++;
        }

        if (!in) {
            switch (state) {
                case "Draft":
                    counterDraftOut++;
                    break;
                case "Approved":
                    counterApprovedOut++;
                    break;
                case "Rejected":
                    counterRejectedOut++;
                    break;
            }
            counterGeneralOut++;
        }
    }

    public String approveConnectionQP(Context context, String[] args) {
        setAttributeDepTaskStatusforQP(context, "Approved", args);
        return "";
    }

    public String rejectConnectionQP(Context context, String[] args) {
        setAttributeDepTaskStatusforQP(context, "Rejected", args);
        return "";
    }

    public void setAttributeDepTaskStatusforQP(Context context, String check, String... args) {

        String mainTaskID = args[0];
        String taskID = args[1];
        String route = args[2];

        /*change attribute state*/
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
                    /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
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

            if (relationshipID.equals("")) throw new FrameworkException("error: relationship id is empty");

            DomainRelationship relationship = new DomainRelationship(relationshipID);
            relationship.setAttributeValue(context, "IMS_QP_DEPTaskStatus", check);

        } catch (FrameworkException fe) {
            LOG.error(check + "operation caused error: " + fe.getMessage());
        } catch (Exception e) {
            LOG.error("error getting domain object with ID: " + mainTaskID + " message: " + e.getMessage());
        }

        /*add history comment*/
        try {

            String login = context.getUser();
            ContextUtil.pushContext(context, PropertyUtil.getSchemaProperty(context, "person_UserAgent"),
                    DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

            /*history to task*/
            String exec = String.format("mod bus %s add history modify comment %s", taskID,
                    "\"attribute task status setting: \'" + check + "\' for relationship to the object named: " +
                            new DomainObject(mainTaskID).getName(context) + " for route \'" + route + "\' by user: " + login + "\"");
            MQLCommand.exec(context, exec);

            /*history to main task*/
            exec = String.format("mod bus %s add history modify comment %s", mainTaskID,
                    "\"attribute task status setting: \'" + check + "\' for relationship to the object named: " +
                            new DomainObject(taskID).getName(context) + " for route \'" + route + "\' by user: " + login + "\"");
            MQLCommand.exec(context, exec);

            LOG.info("Coordination event - : \'" + new DomainObject(taskID).getName(context) + "\' task status: \'" +
                    check + "\' route: \'" + route + "\' related task: \'" + new DomainObject(mainTaskID).getName(context) + "\' by user: " + login + "");

            ContextUtil.popContext(context);

        } catch (MatrixException me) {
            LOG.error("framework exception: " + me.getMessage());
            me.printStackTrace();
        } catch (Exception e) {
            LOG.error("error DomainObject initialisation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void editPostProcess(Context context, String... args) {

        String objectID = "", systemID = "";
        try {
            Map programMap = JPO.unpackArgs(args);
            Map paramMap = (Map) programMap.get("paramMap");

            objectID = (String) paramMap.get("objectId");
            systemID = (String) paramMap.get("systemOID");
        } catch (Exception e) {
            LOG.error("error unpacking arguments: " + e);
        }

        //initialize qptask object
        DomainObject object = null;
        try {
            object = new DomainObject(objectID);
        } catch (Exception e) {
            LOG.error("error when initializing object: " + e.getMessage());
            e.printStackTrace();
        }

        DomainObject system = null;
        if (UIUtil.isNotNullAndNotEmpty(systemID)) {

            //disconnect qPlan from all systems
            try {
                MapList systemsRelated = object.getRelatedObjects(context,
                        /*relationship*/"IMS_QP_QPlan2Object",
                        /*type*/IMS_QP_Constants_mxJPO.SYSTEM_TYPES,
                        /*object attributes*/ new StringList("id"),
                        /*relationship selects*/ null,
                        /*getTo*/ false, /*getFrom*/ true,
                        /*recurse to level*/ (short) 1,
                        /*object where*/null,
                        /*relationship where*/ null,
                        /*limit*/ 0);

                for (Object o : systemsRelated) {
                    Map map = (Map) o;
                    DomainObject disconnectingSystemObject = new DomainObject((String) map.get("id"));
                    object.disconnect(context, new RelationshipType("IMS_QP_QPlan2Object"), true, disconnectingSystemObject);
                    LOG.info("system " + disconnectingSystemObject.getName(context) + " disconnected");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //connect to new one system and change name
            try {

                system = new DomainObject(systemID);
                object.connect(context, new RelationshipType("IMS_QP_QPlan2Object"),/*from*/true, new DomainObject(systemID));

                String prefix = system.getName(context) + "_", objectName = object.getName(context);
                LOG.info("qptask name " + objectName + " has changed to: " + prefix + objectName);
                object.setName(context, prefix + objectName);

            } catch (Exception e) {
                try {
                    LOG.error("error when connecting " + system.getType(context) + " " + system.getName(context));
                } catch (FrameworkException frameworkException) {
                    LOG.error("error getting type&name of the system id: " + systemID);
                    frameworkException.printStackTrace();
                }
                e.printStackTrace();
            }
        }
    }

    /**
     * It's retrieve all Systems&Buildings from current Functional Area
     */
    public Object getSystems(Context context, String... args) {

        String parentID = "", objectID = "";
        try {
            Map programMap = JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            parentID = (String) requestMap.get("parentOID");
            objectID = (String) requestMap.get("objectId");
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
            LOG.error("error getting info from qpTask id " + objectID + " parentOID: " + parentID + " with message: " + e.getMessage());
            e.printStackTrace();
        }

        StringList objectSelects = new StringList();
        objectSelects.add("id");
        objectSelects.add("name");

        MapList rawData = null;
        if (functionalAreaObject != null) {
            try {
                rawData = functionalAreaObject.getRelatedObjects(context,
                        /*relationship*/ null,
                        /*type*/ "IMS_PBSSystem,IMS_GBSBuilding",
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
        }

        StringList fieldRangeValues = new StringList();
        StringList fieldDisplayRangeValues = new StringList();
        if (rawData != null)
            for (Object o : rawData) {
                Map<String, String> map = (Map<String, String>) o;
                fieldRangeValues.add(map.get("id"));
                fieldDisplayRangeValues.add(map.get("name"));
            }

        Map result = new HashMap();
        result.put("field_choices", fieldRangeValues);
        result.put("field_display_choices", fieldDisplayRangeValues);

        return result;
    }
}
