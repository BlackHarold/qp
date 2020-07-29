import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;

import java.util.Map;
import java.util.UUID;
import java.util.Vector;

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

                    if (getTypeFromMap(relatedMap).equals(TYPE_IMS_QP_DEPTask))
                        rawLink = getLinkHTML(relatedMap, SOURCE_DEPTask, /*image url*/null, name);

                    stringBuilder.append(rawLink);
                }

                result.addElement(stringBuilder.toString());
            }

            LOG.info("result: " + result);

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

    final String COMMON_IMAGES = "../common/images/";


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
}
