import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.util.MatrixException;
import matrix.util.SelectList;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_QPTaskRelatedTasks_mxJPO {
    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    private MapList getRelatedTasks(Context ctx, boolean getTo, String... args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        String objectId = (String) argsMap.get("objectId");
        DomainObject object = new DomainObject(objectId);

        MapList relatedTasks;
        String relationships = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask;
        String types = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;
        StringList selects = new StringList(DomainConstants.SELECT_ID);

        StringBuilder whereBuilder = new StringBuilder(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS).append("!='Rejected'");
        if (IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, args)) {
            whereBuilder.setLength(0);
        }

        if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(object.getType(ctx))) {
            relatedTasks = object.getRelatedObjects(ctx,
                    /*relationship*/relationships,
                    /*type*/types,
                    /*object attributes*/ selects,
                    /*relationship selects*/ null,
                    /*getTo*/ getTo, /*getFrom*/ !getTo,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ null,
                    /*relationship where*/ whereBuilder.toString(),
                    /*limit*/ 0);
        } else {
            return new MapList();
        }

        return relatedTasks;
    }

    public MapList getRelatedTaskInput(Context ctx, String... args) throws Exception {
        return getRelatedTasks(ctx, true, args);
    }

    public MapList getRelatedTaskOutput(Context ctx, String... args) throws Exception {
        return getRelatedTasks(ctx, false, args);
    }

    public Vector getTaskInput(Context ctx, String[] args) {
        return getQPTaskRelatedObjects(ctx, args, true);
    }

    public Vector getTaskOutput(Context ctx, String[] args) {
        return getQPTaskRelatedObjects(ctx, args, false);
    }

    public Vector getQPTaskRelatedObjects(Context ctx, String[] args, boolean in) {
        Vector result = new Vector();
        try {
            Map argsMap = JPO.unpackArgs(args);
            Map paramList = (Map) argsMap.get("paramList");
            String parentOID = (String) paramList.get("objectId");
            /*top level Codes by items*/
            DomainObject parent = new DomainObject(parentOID);
            StringList selects = new StringList(DomainConstants.SELECT_ID);
            StringList relSelects = new StringList(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS);

            //where with check from direction of relationship
            StringBuilder selectWhereBuilder = new StringBuilder("");
            if (in) {
                selectWhereBuilder
                        .append("from[")
                        .append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask)
                        .append("].to.id==");
            } else {
                selectWhereBuilder
                        .append("to[")
                        .append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask)
                        .append("].from.id==");

            }

            selectWhereBuilder.append("'").append(parentOID).append("'");

            //drop 'Rejected' states if user hasn't admin or superuser roles
            StringBuilder whereBuilder = new StringBuilder(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS).append("!='Rejected'");
            if (IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, parentOID)) {
                whereBuilder.setLength(0);
            }

            MapList taskStates = parent.getRelatedObjects(ctx,
                    /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    /*object attributes*/ selects,
                    /*relationship selects*/ relSelects,
                    /*getTo*/ in, /*getFrom*/ !in,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ selectWhereBuilder.toString(),
                    /*relationship where*/ whereBuilder.toString(),
                    /*limit*/ 0);

            Map<String, String> states = new HashMap();
            for (Object o : taskStates) {
                Map map = (Map) o;
                states.put((String) map.get("id"), (String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS));
            }

            MapList objectList = (MapList) argsMap.get("objectList");
            for (Object object : objectList) {
                Map map = (Map) object;
                String objectId = (String) map.get(DomainConstants.SELECT_ID);
                String state = states.get(objectId);
                // result without coloring
                result.addElement(state);
            }

        } catch (Exception e) {
            try {
                emxContextUtil_mxJPO.mqlWarning(ctx, e.toString());
                LOG.error("result link: " + result);
                LOG.error("error getting url string: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    private String getColor(String state) {
        switch (state) {
            case "Draft":
                return "#3264C8";
            case "Approved":
                return "green";
            case "Rejected":
                return "red";
            default:
                return "";
        }
    }

    //    Confirmation procedure
    public HashMap approve(Context ctx, String... args) {
        return agreementProcess(ctx, "Approved", args);
    }

    public HashMap reject(Context ctx, String... args) {
        return agreementProcess(ctx, "Rejected", args);
    }

    private HashMap agreementProcess(Context ctx, String action, String... args) {
        Map mapMessage = new HashMap();
        //get all ids
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");
        String road = (String) argsMap.get("road");
        boolean roadTo = road.equals("input");

        String planIdFromTask = String.format("to[%s].from.%s",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                DomainConstants.SELECT_ID);
        String depIdFromTask = String.format("to[%s].from.to[%s].from.%s",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan,
                DomainConstants.SELECT_ID);

        String[] taskIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            rowIDs[i] = rowIDs[i].substring(rowIDs[i].indexOf("|"), rowIDs[i].lastIndexOf("|"));
            taskIDs[i] = rowIDs[i].substring(1, rowIDs[i].lastIndexOf("|"));
        }

        String[] rawString = rowIDs[0].split("\\|");
        String parentID = rawString[2];

        DomainObject parent = null;
        try {
            parent = new DomainObject(parentID);
        } catch (Exception e) {
            LOG.error("error getting parent object: " + e.getMessage());
        }

//            task selects
        StringList selects = new StringList(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(planIdFromTask);
        selects.add(depIdFromTask);

        String where = String.format("%s[%s].%s.id==%s",
                roadTo ? "from" : "to", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                roadTo ? "to" : "from", parentID);

        MapList taskStates = null;
        if (parent != null) {
            try {
                taskStates = parent.getRelatedObjects(ctx,
                        /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                        /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/ roadTo, /*getFrom*/ !roadTo,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ where,
                        /*relationship where*/ null,
                        /*limit*/ 0);
                LOG.info("taskStates: " + taskStates);
            } catch (FrameworkException fe) {
                LOG.error("error getting related states: " + fe.getMessage());
            }
        }
//            rel selects
        StringList relSelects = new StringList(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
        relSelects.add("id");
        MapList relIdStates = null;
        try {
            relIdStates = parent.getRelatedObjects(ctx,
                    /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    /*object attributes*/ selects,
                    /*relationship selects*/ relSelects,
                    /*getTo*/ roadTo, /*getFrom*/ !roadTo,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ where,
                    /*relationship where*/ null,
                    /*limit*/ 0);
            LOG.info("relIdStates: " + relIdStates);
        } catch (FrameworkException fe) {
            LOG.error("error getting relation states: " + fe.getMessage());
        }

        String parentQPlanId = "", parentDEPId = "";
        try {
            parentQPlanId = parent.getInfo(ctx, planIdFromTask);
            parentDEPId = parent.getInfo(ctx, depIdFromTask);
        } catch (FrameworkException fe) {
            LOG.error("error getting parent info: " + fe.getMessage());
        }

        Map<String, Map> processMap = new HashMap();

        Map<String, Map> innerQP_taskIDs = new HashMap<>();
        Map<String, Map> brotherDEP_taskIDs = new HashMap<>();
        Map<String, Map> differenceDEP_taskIDs = new HashMap<>();

        if (taskStates != null && relIdStates != null)
            for (int i = 0; i < taskStates.size(); i++) {
                Map taskMap = (Map) taskStates.get(i);
                Map relMap = null;
                for (Object o : relIdStates) {
                    Map map = (Map) o;
                    if (map.get(DomainConstants.SELECT_NAME).equals(taskMap.get(DomainConstants.SELECT_NAME)))
                        relMap = map;
                }
//                relMap.put("name", taskMap.get("name"));

                if (relMap != null) {

                    //inner check plan
                    if (taskMap.get("to[IMS_QP_QPlan2QPTask].from.id").equals(parentQPlanId)) {
                        innerQP_taskIDs.put((String) taskMap.get(DomainConstants.SELECT_ID), relMap);
                    }

                    //brother check DEP
                    else if (!taskMap.get(planIdFromTask).equals(parentQPlanId) && taskMap.get(depIdFromTask).equals(parentDEPId)) {
                        brotherDEP_taskIDs.put((String) taskMap.get(DomainConstants.SELECT_ID), relMap);

                        //different check DEP
                    } else if (!taskMap.get(depIdFromTask).equals(parentDEPId)) {
                        differenceDEP_taskIDs.put((String) taskMap.get(DomainConstants.SELECT_ID), relMap);
                    }

                    processMap.put((String) taskMap.get(DomainConstants.SELECT_ID), relMap);
                }
            }
        LOG.info("processMap: " + processMap);

//            check table row ids
        for (String taskID : taskIDs) {
            if (processMap.containsKey(taskID)) {
                Map taskRelationship = processMap.get(taskID);
                String taskState = (String) taskRelationship.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
                String taskName = (String) taskRelationship.get(DomainConstants.SELECT_NAME);

                if (innerQP_taskIDs.containsKey(taskID)) {
                    //check if user is DEP or PBS owner
                    String ownerNames = "";
                    StringBuilder command = new StringBuilder("print bus ").append(taskID).append(" select ")
                            .append("to[IMS_QP_QPlan2QPTask].from.to[IMS_QP_DEP2QPlan].from.from[IMS_QP_DEP2Owner].to.name")
                            .append(" ")
                            .append("to[IMS_QP_QPlan2QPTask].from.from[IMS_QP_QPlan2Object].to.from[IMS_PBS2Owner].to.name")
                            .append(" dump |");
                    try {
                        ownerNames = MQLCommand.exec(ctx, command.toString());
                    } catch (MatrixException e) {
                        LOG.error("matrix error: " + e.getMessage());
                        e.printStackTrace();
                    }
                    LOG.info("id: " + taskID + " command: " + command);
                    LOG.info("ownerNames: " + ownerNames + " user: " + ctx.getUser());
                    if (!ownerNames.contains(ctx.getUser())) {
                        mapMessage.put(taskName, " can't change the state of inner tasks who don't own PBS or DEP at all");
                        LOG.info(ctx.getUser() + " isn't owner!");
                        continue;
                    }
                }

                if (brotherDEP_taskIDs.containsKey(taskID) && !IMS_QP_Security_mxJPO.isUserAdmin(ctx) && !IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx)) {
                    //
                    if (IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(ctx, parentID) && !IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, parentID)) {
                        if (roadTo && !"Draft".equals(taskState)) {
                            mapMessage.put(taskName, String.format("for Quality plan owner has a wrong state: %s", taskState));
                            continue;
                        }
                        if (!roadTo) {
                            mapMessage.put(taskName, " Quality plan owners can't change output state brother tasks");
                            continue;
                        }
                    }
                }

                if (differenceDEP_taskIDs.containsKey(taskID) && !IMS_QP_Security_mxJPO.isUserAdmin(ctx) && !IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx)) {
                    //
                    if (IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(ctx, parentID) && !IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, parentID)) {
                        if (!roadTo) {
                            mapMessage.put(taskName, " Quality plan owners can't change output state different tasks");
                            continue;
                        }
                        if (roadTo && !"Draft".equals(taskState)) {
                            mapMessage.put(taskName, String.format(" has a wrong state: %s", taskState));
                            continue;
                        }
                    }

                    if (IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, parentID)) {
                        if (!roadTo) {
                            mapMessage.put(taskName, " Quality plan owners can't change output state different tasks");
                            continue;
                        }
                    }
                }
                String relId = (String) taskRelationship.get(DomainConstants.SELECT_ID);
                boolean isStateChanged = changeState(ctx, relId, action);

                if (!isStateChanged)
                    mapMessage.put(taskName, String.format(" wrong result of changing state: %s", taskState));
            }
        }

        if (mapMessage.isEmpty()) {
            mapMessage.put("message", "status OK code 200");
        }

        return (HashMap) mapMessage;
    }


    private boolean changeState(Context ctx, String relId, String action) {
        DomainRelationship relationship = new DomainRelationship(relId);
        try {
            relationship.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.attribute_IMS_QP_DEPTaskStatus, action);
        } catch (FrameworkException frameworkException) {
            frameworkException.printStackTrace();
            return false;
        }
        return true;
    }
}
