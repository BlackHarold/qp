import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.*;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.MQLCommand;
import matrix.db.Person;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.Vector;

//Deviation service
public class IMS_QP_ER_Deviation_mxJPO {

    private final static Logger LOG = LogManager.getLogger("IMS_QP_DEP");

    private String logInfo(Context ctx, String objectId) {

        String result = "name: ";
        try {
            result += new DomainObject(objectId).getName(ctx);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public void create(Context ctx, String... args) {
        Map map = null;
        try {
            map = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting argumets: " + e.getMessage());
            e.printStackTrace();
        }

        Map requestMap = (Map) map.get("requestMap");
        String objectId = (String) requestMap.get("objectId");
        String parentOID = (String) requestMap.get("parentOID");
        LOG.info(logInfo(ctx, objectId));

        Map paramMap = (Map) map.get("paramMap");
        String newObjectId = (String) paramMap.get("newObjectId");

        //check if it has related deviation
        if (UIUtil.isNotNullAndNotEmpty(objectId) && UIUtil.isNotNullAndNotEmpty(newObjectId)) {
            try {
                hasRelatedDeviations(ctx, objectId);
            } catch (Exception e) {
                LOG.error("error checking related deviations: " + e.getMessage());
                e.printStackTrace();
                try {
                    throw new Exception("Something wrong: " + e.getMessage());
                } catch (Exception ex) {
                    LOG.error("got an error: " + ex.getMessage());
                    ex.printStackTrace();
                }
            }

            //get relation between tasks
            MapList relatedTasks = getRelatedToMidObjects(ctx,/*objectId*/ parentOID, /*childId*/ objectId);
            LOG.info("related tasks by  deviation : " + relatedTasks);
            Object o = relatedTasks.get(0);
            Map rawMap = (Map) o;
            String relationshipId = (String) rawMap.get("id");

            // relate with task relation
            DomainObject deviationObject = null, parentObject;
            try {
                deviationObject = new DomainObject(newObjectId);
                parentObject = new DomainObject(parentOID);
                String deviationName = deviationObject.getName(ctx);
                String objectName = UIUtil.isNotNullAndNotEmpty(parentObject.getName(ctx)) ? parentObject.getName(ctx) : "Uncommon";
                deviationName = objectName + "_" + deviationName;
                deviationObject.setName(ctx, deviationName);
            } catch (FrameworkException e) {
                LOG.error("has an error: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                LOG.error("error getting object: " + e.getMessage());
                e.printStackTrace();
            }

            String relationship = "IMS_QP_Deviation_QPTask2QPTask";
            String command = "add connection $1 from $2 torel $3";
            try {
                MqlUtil.mqlCommand(ctx, command, new String[]{relationship, newObjectId, relationshipId});
            } catch (FrameworkException e) {
                LOG.error("error connecting new object to relationship: " + e.getMessage());
                e.printStackTrace();
            }

            try {
                String comment = String.format("deviation object %s linked", deviationObject.getName(ctx));
                setHistory(ctx, objectId, "modify", comment);
            } catch (FrameworkException e) {
                LOG.error("Adding history comment cause an error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public Map delete(Context ctx, String... args) {
        Map map = null;
        try {
            map = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting argumets: " + e.getMessage());
            e.printStackTrace();
        }

        if (map != null && map.containsKey("request")) {
            Object o = map.get("request");
            HttpServletRequest request = (HttpServletRequest) o;

            String objectId = request.getParameter("objectId");
            String tableRowIds = request.getParameter("emxTableRowId");
            String[] objectIds = tableRowIds.split("\\|");
            String childId = objectIds[1];

            LOG.info("objectId: " + objectId + " childId: " + childId);
            boolean result = deleteShadowDeviation(ctx, objectId, childId);
            if (result == false) {
                map.put("message", "something went wrong");
            }
        }

        return map;
    }

    /**
     * @param objectId Upper task who wants to deviate
     * @param childId  Lower current task
     * @return
     */
    private boolean deleteShadowDeviation(Context ctx, String objectId, String childId) {

        //trying to get all related deviations
        MapList relatedDeviations = getRelatedToMidObjects(ctx, objectId, childId);

        String deviationId = "";
        if (relatedDeviations != null && relatedDeviations.size() == 1) {
            Map map = (Map) relatedDeviations.get(0);
            deviationId = (String) map.get("tomid[IMS_QP_Deviation_QPTask2QPTask].from.id");
            LOG.info("dev id: " + deviationId);
        } else {
            return false;
        }

        DomainObject deviationObject = null;
        try {
            deviationObject = new DomainObject(deviationId);
        } catch (Exception e) {
            LOG.error("error getting domain object: " + e.getMessage());
            e.printStackTrace();
        }


        String deviationName = null, deviationProtocol = null, deviationProtocolDescription = null;
        if (UIUtil.isNotNullAndNotEmpty(deviationId)) {
            try {
                deviationName = deviationObject.getName(ctx);
                deviationProtocol = deviationObject.getAttributeValue(ctx, "IMS_QP_Protocol");
                deviationProtocolDescription = deviationObject.getAttributeValue(ctx, "IMS_QP_ProtocolDescription");
            } catch (FrameworkException e) {
                LOG.error("to get deviation name cause an error: " + deviationId + ": " + e.getMessage());
                e.printStackTrace();
            }
            //delete deviation
            try {
                LOG.info("delete object: " + deviationId);
                deleteObject(ctx, deviationId);
            } catch (Exception e) {
                LOG.error("error deleting object: " + e.getMessage());
                e.printStackTrace();
                return false;
            }

            //set history comment
            String historyComment = String.format("deviation object %s deleted. Protocol: %s, description: %s", deviationName, deviationProtocol, deviationProtocolDescription);
            LOG.info("history comment: " + historyComment);
            setHistory(ctx, objectId, "modify", historyComment);
            setHistory(ctx, childId, "modify", historyComment);
        }

        return true;
    }

    private void deleteObject(Context ctx, String id) throws Exception {
        LOG.info("deviation id deleted: " + id);
        new DomainObject(id).deleteObject(ctx, false);
    }

    private MapList getRelatedToMidObjects(Context ctx, String objectId, String childId) {
        DomainObject childObject = null;
        try {
            childObject = new DomainObject(childId);
        } catch (Exception e) {
            LOG.error("error getting domain object: " + e.getMessage());
            e.printStackTrace();
        }

        MapList relatedMIddleDeviations = new MapList();
        if (childObject != null) {
            StringList relationSelects = new StringList();
            relationSelects.addElement(/* rel id*/DomainConstants.SELECT_ID);
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.id");
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.type");
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.name");
            relationSelects.addElement("tomid[IMS_QP_Deviation_QPTask2QPTask].from.revision");

            String relationshipWhere = String.format("to.id==%s&&from.id==%s", objectId, childId);
            String objectWhere = String.format(
                    "to[IMS_QP_QPTask2QPTask].from.id==%s",
                    childId);

            try {
                relatedMIddleDeviations = childObject.getRelatedObjects(ctx,
                        /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                        /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                        /*object selects*/ null,
                        /*relationship selects*/ relationSelects,
                        /*getTo*/ true, /*getFrom*/ true,
                        /*recurse to level*/ (short) 1,
                        /*object where*//*objectWhere*/ null,
                        /*relationship where*/ relationshipWhere,
                        /*limit*/ 0);
            } catch (FrameworkException e) {
                LOG.error("error getting info from domain object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return relatedMIddleDeviations;
    }

    public boolean hasRelatedDeviations(Context ctx, String objectId) throws Exception {

        //trying to get all related deviations
        MapList relatedDeviations = getRelatedToMidObjects(ctx, /*objectId*/ null, /*childId*/ objectId);
        LOG.info("print bus " + objectId + "; hasRelatedDeviations: " + relatedDeviations);

        //throw exception if task has any deviations
        if (!relatedDeviations.isEmpty()) {
            StringBuilder builderMessage = new StringBuilder("Task has deviation(s) already: ");
            boolean inFlag = false;
            for (int i = 0; i < relatedDeviations.size(); i++) {
                Map deviationMap = (Map) relatedDeviations.get(i);

                String deviationName = (String) deviationMap.get("tomid[IMS_QP_Deviation_QPTask2QPTask].from.name");
                if (UIUtil.isNotNullAndNotEmpty(deviationName)) {
                    builderMessage.append(deviationName);
                    if (relatedDeviations.size() - i > 1) {
                        builderMessage.append(", ");
                    }
                    inFlag = true;
                }
            }

            if (inFlag) {
                LOG.error("should be throwing an error: " + builderMessage);
                throw new MatrixException(builderMessage.toString());
            }

        }

        LOG.info("relatedDeviations: " + relatedDeviations.isEmpty());
        return false;
    }

    /**
     * Add history comment
     *
     * @param ctx
     * @param objectId
     */
    private void setHistory(Context ctx, String objectId, String type, String comment) {
        String username = ctx.getUser();

        try {
            ContextUtil.pushContext(ctx, PropertyUtil.getSchemaProperty(ctx, "person_UserAgent"),
                    DomainConstants.EMPTY_STRING, DomainConstants.EMPTY_STRING);

            String command = String.format("mod bus %s add history \'%s\' comment \'%s, by user: %s\'",
                    /*to object*/       objectId,
                    /*type of comment*/ type,
                    /*comment text*/    comment,
                    /*by user*/         username);

            LOG.info("command: " + command);
            MQLCommand.exec(ctx, command);

            ContextUtil.popContext(ctx);

        } catch (FrameworkException e) {
            LOG.error(objectId + " context operation cause error: " + e.getMessage());
            e.printStackTrace();
        } catch (MatrixException e) {
            LOG.error(objectId + "execute mql command cause an error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public MapList getRelatedTaskInput(Context ctx, String... args) throws Exception {
        return getRelatedTasks(ctx, true, args);
    }

    private MapList getRelatedTasks(Context ctx, boolean getTo, String... args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        String objectId = (String) argsMap.get("objectId");
        DomainObject object = new DomainObject(objectId);

        MapList relatedTasks;
        String relationships = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask;
        String types = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;
        StringList selects = new StringList(DomainConstants.SELECT_ID);

        StringBuilder whereBuilder = new StringBuilder();
        whereBuilder
                .append(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_DEPTASK_STATUS).append("=='Approved'");

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


    public Vector getDocumentCode(Context ctx, String... args) {
        Vector result = new Vector();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Map argsMap = null;
            try {
                argsMap = JPO.unpackArgs(args);
            } catch (Exception e) {
                LOG.error("error: " + e.getMessage());
                e.printStackTrace();
            }

            MapList objectList = (MapList) argsMap.get("objectList");
            for (Object o : objectList) {
                stringBuilder.setLength(0);
                Map map = (Map) o;
                String objectId = (String) map.get(DomainObject.SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                    DomainObject taskObject = new DomainObject(objectId);
                    String documentId = objectId;
                    String documentCode = taskObject.getInfo(ctx, "from[IMS_QP_QPTask2Fact].to.name");
                    documentCode = UIUtil.isNotNullAndNotEmpty(documentCode) ? documentCode :
                            taskObject.getInfo(ctx, "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_QP_DocumentCode]");
                    documentCode = UIUtil.isNotNullAndNotEmpty(documentCode) ? documentCode : "";
                    if (UIUtil.isNotNullAndNotEmpty(documentCode)) {
                        stringBuilder.append(getLinkHTML(objectId, documentCode, 14));
                    }
                    result.addElement(stringBuilder.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }


    public String getLinkHTML(String documentId, String documentName, int fontSize) {
        if (UIUtil.isNotNullAndNotEmpty(documentName)) {
            documentName = documentName.replaceAll("&", "&amp;");
        } else {
            documentName = "-";
        }

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("<div style=\"display:none;\">"
                + documentName + "</div>" + "<img src=''/>"
                + "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?" +
                "objectId=" + documentId + "')\" style=\"font-size: "
                + fontSize + "px\" title=\""
                + documentName + "\">"
                + documentName + "</a>");

        return stringBuilder.toString();
    }

    /**
     * Getting access to deviation tab and create delete commands
     *
     * @param ctx  usual parameter
     * @param args usual parameter
     * @return bool access mark
     */
    public boolean checkAccess(Context ctx, String... args) {
        Person person = new Person(ctx.getUser());

        String ROLE_IMS_QP_Viewer = "IMS_QP_Viewer";
        String ROLE_IMS_QP_Supervisor = "IMS_QP_Supervisor";

        boolean granted = false;

        try {
            LOG.info("viewer: " + person.isAssigned(ctx, ROLE_IMS_QP_Viewer));
            if (person.isAssigned(ctx, ROLE_IMS_QP_Viewer)) {
                granted = false;
            }

            Map map = JPO.unpackArgs(args);
            String objectId = UIUtil.isNotNullAndNotEmpty((String) map.get("objectId")) ?
                    (String) map.get("objectId") : (String) map.get("parentOID");
            LOG.info("admin or su: " + IMS_QP_Security_mxJPO.isUserAdminOrSuper(ctx)
                    + " qp plan owner: " + IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, args)
                    + " dep owner: " + IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, args)
                    + " task owner: " + IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(ctx, objectId));

            if (IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, args) || IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, args) || IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(ctx, objectId)) {
                if (person.isAssigned(ctx, ROLE_IMS_QP_Supervisor)) {
                    granted = true;
                }
            }

            if (IMS_QP_Security_mxJPO.isUserAdminOrSuper(ctx)) {
                granted = true;
            }

        } catch (MatrixException e) {
            LOG.error("error when checking Person: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOG.error("error when checking Person is dep owner: " + e.getMessage());
            e.printStackTrace();
        }


        LOG.info("access: " + granted);
        return granted;
    }
}
