import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class IMS_QP_QPTaskRelatedTasks_mxJPO {
    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    private MapList getRelatedTasks(Context ctx, boolean getTo, String... args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        String objectId = (String) argsMap.get("objectId");
        DomainObject object = new DomainObject(objectId);

        MapList relatedTasks;
        String relationships = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask;
        String types = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;
        StringList selects = new StringList("id");

        StringBuilder whereBuilder = new StringBuilder(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS).append("!='Rejected'");
        if (IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, args)) {
            whereBuilder.setLength(0);
        }

        if ("IMS_QP_QPTask".equals(object.getType(ctx))) {
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
            StringList selects = new StringList("id");
            StringList relSelects = new StringList(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS);

            //where with check from direction of relationship
            StringBuilder selectWhereBuilder = new StringBuilder("");
            if (in) {
                selectWhereBuilder.append("from[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask).append("].to.id==")
                        .append("'").append(parentOID).append("'");
            } else {
                selectWhereBuilder.append("to[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask).append("].from.id==")
                        .append("'").append(parentOID).append("'");
            }

            //drop 'Rejected' states if user hassn't admin or superuser roles
            StringBuilder whereBuilder = new StringBuilder(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS).append("!='Rejected'");
            if (IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, parentOID)) {
                whereBuilder.setLength(0);
            }

            MapList taskStates = parent.getRelatedObjects(ctx,
                    /*relationship*/"IMS_QP_QPTask2QPTask",
                    /*type*/"IMS_QP_QPTask",
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
                String objectId = (String) map.get("id");
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
        HashMap mapMessage = new HashMap();

        //get all ids
        HashMap<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");
        String road = (String) argsMap.get("road");
        boolean roadTo = road.equals("input");

        String[] taskIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            rowIDs[i] = rowIDs[i].substring(rowIDs[i].indexOf("|"), rowIDs[i].lastIndexOf("|"));
            taskIDs[i] = rowIDs[i].substring(1, rowIDs[i].lastIndexOf("|"));
        }

        String[] rawString = rowIDs[0].split("\\|");
        String parentID = rawString[2];

        try {
            DomainObject parent = new DomainObject(parentID);

//            task selects
            StringList selects = new StringList("id");
            selects.add("name");

            String where = String.format("%s[%s].%s.id==%s", roadTo ? "from" : "to", IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask, roadTo ? "to" : "from", parentID);
            MapList taskStates = parent.getRelatedObjects(ctx,
                    /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    /*object attributes*/ selects,
                    /*relationship selects*/ null,
                    /*getTo*/ roadTo, /*getFrom*/ !roadTo,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ where,
                    /*relationship where*/ null,
                    /*limit*/ 0);

//            rel selects
            StringList relSelects = new StringList(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
            relSelects.add("id");
            MapList relIdStates = parent.getRelatedObjects(ctx,
                    /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    /*object attributes*/ null,
                    /*relationship selects*/ relSelects,
                    /*getTo*/ roadTo, /*getFrom*/ !roadTo,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ where,
                    /*relationship where*/ null,
                    /*limit*/ 0);

            Map<String, Map> processMap = new HashMap();
            for (int i = 0; i < taskStates.size(); i++) {
                Map taskMap = (Map) taskStates.get(i);
                Map relMap = (Map) relIdStates.get(i);
                relMap.put("name", taskMap.get("name"));
                processMap.put((String) taskMap.get("id"), relMap);
            }

//            check table row ids
            for (int i = 0; i < taskIDs.length; i++) {
                if (processMap.containsKey(taskIDs[i])) {
                    Map taskRelationship = processMap.get(taskIDs[i]);
                    String taskState = (String) taskRelationship.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
                    String taskName = (String) taskRelationship.get("name");
                    if (IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, taskIDs[i]) ||
                            "Draft".equals(taskState)) {
                        if (IMS_QP_Security_mxJPO.isUserAdmin(ctx) ||
                                IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx) ||
                                roadTo || !roadTo && !"Draft".equals(taskState)) {
                            String relId = (String) taskRelationship.get("id");
                            DomainRelationship relationship = new DomainRelationship(relId);
                            relationship.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTaskStatus, action);
                        } else {
                            mapMessage.put(taskName, taskState);
                        }
                    } else {
                        mapMessage.put(taskName, taskState);
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("error getting domain object: " + e.getMessage());
        }

        if (mapMessage.isEmpty()) {
            mapMessage.put("message", "status OK code 200");
        }
        return mapMessage;
    }
}
