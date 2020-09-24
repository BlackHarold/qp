import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.StringList;

import javax.management.relation.RelationType;
import java.util.*;

public class IMS_QP_AllTask_mxJPO extends IMS_QP_Task_mxJPO {

    public Vector getDEPTaskRelatedObjects(Context context, String[] args, boolean in) {

        Vector result = new Vector();

        try {
            boolean getTo = in;
            boolean isRuLocale = IMS_KDD_mxJPO.isRuLocale(args);

            Map argsMap = JPO.unpackArgs(args);
            MapList argsList = (MapList) argsMap.get("objectList");

            /*top level Codes by items*/
            StringBuilder stringBuilder = new StringBuilder();
            for (Object o : argsList) {
                Map map = (Map) o;
                stringBuilder.setLength(0);

                String mainTaskID = (String) map.get("id");
                DomainObject objectMainTask = new DomainObject(mainTaskID);

                StringList selects = new StringList();
                selects.add("id");
                selects.add("name");
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_ID);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_NAME);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_IMS_NAME);
                selects.add(IMS_QP_Constants_mxJPO.SELECT_DEP_IMS_NAME_RU);
                selects.add(IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_ID);
                selects.add(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU);
                selects.add(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME);
                /*INPUT getTo*/
                selects.add(IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPTASK_2_DEPTASK_FROM_ID);
                selects.add(IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPTASK_2_DEPTASK_ATTRIBUTE_IMS_QP_DEPTASK_STATUS);
                /*OUTPUT getFrom*/
                selects.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPTASK_2_DEPTASK_TO_ID);
                selects.add(IMS_QP_Constants_mxJPO.FROM_IMS_QP_DEPTASK_2_DEPTASK_ATTRIBUTE_IMS_QP_DEPTASK_STATUS);

                MapList relatedTasks = objectMainTask.getRelatedObjects(context,
                        /*relationship*/"IMS_QP_DEPTask2DEPTask",
                        /*type*/"IMS_QP_DEPTask",
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/ getTo, /*getFrom*/ !getTo,
                        /*recurse to level*/ (short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/ 0);

                String name, rawLink = "";

                for (Object object : relatedTasks) {
                    Map relatedMap = (Map) object;
                    /*rotate all related tasks and generating links*/
                    if (stringBuilder.length() > 0) {
                        stringBuilder.append("<br/>");
                    }

                    name = (String) relatedMap.get(isRuLocale ? "attribute[IMS_NameRu]" : "attribute[IMS_Name]");
                    name = UIUtil.isNotNullAndNotEmpty(name) ? name : "error";

                    if (getTypeFromMap(relatedMap).equals(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask))
                        rawLink = getLinkHTML(relatedMap, IMS_QP_Constants_mxJPO.SOURCE_DEPTask, /*image url*/null, name, "");

                    stringBuilder.append(rawLink);
                }

                result.addElement(stringBuilder.toString());
            }

        } catch (Exception e) {
            try {
                emxContextUtil_mxJPO.mqlWarning(context, e.toString());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
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
                                "<img src=\"%s\" title=\"%s\" style=\"border: 1px solid #992211;\"/></a>",*///          <- with borders
                        HtmlEscapers.htmlEscaper().escape(linkId), getEcma(program), getEcma(function), getEcma(fromId),
                        escapeToId ? getEcma(toId) : toId, getEcma(relationships), getEcma(linkId), getEcma(spinnerId),
                        onDisconnected != null && !onDisconnected.isEmpty() ? onDisconnected : "", imageUrl,
                        HtmlEscapers.htmlEscaper().escape(title));

        return link;
    }

    /**
     * Method distributes relationship from Task
     * disconnecting to DEP and then connecting to selected rows
     */
    public Map distributeTaskConnection(Context context, String[] args) {

        //get all table ids
        HashMap<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            argsMap.put("message", "error getting arguments: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");
        if (rowIDs.length == 0) {
            return null;
        }

        String[] taskIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            taskIDs[i] = rowIDs[i].substring(0, rowIDs[i].indexOf("|"));
        }

        String depTaskID = (String) argsMap.get("parentOID"), currentTaskID = (String) argsMap.get("objectId");

        String relationshipID = getRelationshipId(context, depTaskID, "IMS_QP_DEPTask2DEP", currentTaskID);
        if (relationshipID != null) {
            StringBuilder stringBuilder = new StringBuilder();
            try {
                ContextUtil.startTransaction(context, true);
                DomainRelationship relationship = new DomainRelationship(relationshipID);

                stringBuilder.append("get relationship: " + relationshipID + " rel type: " + relationship.getAttributeDetails(context));
                relationship.remove(context);

                int counter = 1;
                for (int i = 0; i < taskIDs.length; i++) {
                    DomainObject depTask = new DomainObject(depTaskID), currentTask = new DomainObject(currentTaskID), task = new DomainObject(taskIDs[i]);
                    stringBuilder.append("\n act " + counter++ + " - connect from " + depTask.getName(context) + " id " + depTaskID +
                            "\n relationship: RelationshipType(\"IMS_QP_DEPTask2DEP\") \nto " + task.getName(context) + " id: " + taskIDs[i]);
                    relationship = DomainRelationship.connect(context, depTask, new RelationshipType("IMS_QP_DEPTask2DEPTask"), task);
                    relationship.setAttributeValue(context, "IMS_QP_DEPTaskStatus", "Approved");
                    argsMap.put("message", stringBuilder.toString());
                }
                ContextUtil.commitTransaction(context);
            } catch (Exception e) {
                LOG.error("error distribute relationship: " + e.getMessage());
                argsMap.put("message", "error distribute relationship: " + e.getMessage());
                ContextUtil.abortTransaction(context);
            }
        }

        return argsMap;
    }

    private String getRelationshipId(Context context, String fromObjectId, String relationshipType, String toObjectId) {
        String result;

        try {
            DomainObject fromObject = new DomainObject(fromObjectId);
            DomainObject toObject = new DomainObject(toObjectId);
            StringList fromObjectIDs = fromObject.getInfoList(context, "from[" + relationshipType + "].id");
            StringList toObjectIDs = toObject.getInfoList(context, "to[" + relationshipType + "].id");

            List<String> retainID = retainCollection(fromObjectIDs, toObjectIDs);

            if (retainID.size() > 1) {
                throw new Exception("DEP has nore than one relations with task, check this. from object: " +
                        fromObjectIDs + " / to object: " +
                        toObjectIDs + " retain result: " + retainID);
            }

            result = retainID.get(0);

        } catch (Exception ex) {
            LOG.error("error getting relationship id: " + ex.getMessage());
            return null;
        }
        return result;
    }

    private List retainCollection(List l1, List l2) {
        l1.retainAll(l2);
        return l1;
    }
}
