import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.UUID;

public class IMS_QP_DEP_mxJPO {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public String getInterdisciplinaryCheck(Context context, String... args) {

        //get objectId object args
        String objectId;
        boolean interdisciplinary = false;

        try {
            Map argsMap = JPO.unpackArgs(args);
            Map requestMap = (Map) argsMap.get("requestMap");
            objectId = (String) requestMap.get("objectId");
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                DomainObject depObject = new DomainObject(objectId);

                //getting attribute IMS_QP_InterdisciplinaryDEP state, default value is 'false'
                interdisciplinary = depObject.getInfo(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_INTERDISCIPLINARY_DEP).equals("TRUE");
            }
        } catch (Exception ex) {
            LOG.error("exception getting program map: " + ex.getMessage());
            ex.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        // showing icons iconLicenseAvailable.gif or iconLicenseUnavailable.gif
        String available = interdisciplinary ? "Available" : "Unavailable";
        sb.append("<img src=\"../common/images/iconLicenseBox").append(available).append(".png\">");

        return sb.toString();
    }

    public String getConnectedExternalDocumentSetHTML(Context ctx, String... args) throws Exception {
        StringBuilder sb = new StringBuilder();

        Map argsMap = JPO.unpackArgs(args);
        Map paramMap = (Map) argsMap.get("paramMap");
        String objectId = (String) paramMap.get("objectId");

        DomainObject objectClassifier = new DomainObject(objectId);
        StringList selects = new StringList("id");
        selects.add("name");

        MapList externalDocuments = objectClassifier.getRelatedObjects(ctx,
                /*relationship*/"IMS_QP_DEP2Doc",
                /*type*/"IMS_ExternalDocumentSet",
                /*object attributes*/ selects,
                /*relationship selects*/ null,
                /*getTo*/ false, /*getFrom*/ true,
                /*recurse to level*/ (short) 1,
                /*object where*/ null,
                /*relationship where*/ null,
                /*limit*/ 0);

        if (externalDocuments.size() > 0) {
            for (Object o : externalDocuments) {
                Map externalDocument = (Map) o;
                String docId = (String) externalDocument.get("id");
                String docName = (String) externalDocument.get("name");
                LOG.info(objectId + " is DEP owner: " + IMS_QP_Security_mxJPO.currentUserIsDEPOwner(ctx, objectId));
                if (IMS_QP_Security_mxJPO.currentUserIsDEPOwner(ctx, objectId) || IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx)) {
                    sb.append(getDisconnectLinkHTML(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP, "disconnectExternalDocumentSet",
                            objectId, docId, true, "IMS_QP_DEP2Doc", "../common/images/fugue/16x16/cross.png", "Disconnect", getRefreshWindowFunction()));
                }
                String emxTableLinkClick = String.format("emxTableColumnLinkClick('../common/emxForm.jsp?objectId=%s&form=type_IMS_ExternalDocumentSet')", docId);
                sb.append(String.format("<a href=\"javascript:%s\"><img src=\"%s\" />%s</a>", emxTableLinkClick, "../common/images/fugue/16x16/document.png", HtmlEscapers.htmlEscaper().escape(docName)));
            }
            LOG.info("has doc link: " + sb.toString());
        } else {
            String tableParameter = String.format("table=%s", IMS_QP_Constants_mxJPO.EXTERNAL_DOCUMENT);
            String systemNameParameter = String.format("IMS_ExternalSystemName=%s", IMS_QP_Constants_mxJPO.HNH_PRODUCTION_SRV);
            String relationshipParameter = String.format("relationship=%s", "IMS_QP_DEP2Doc");

            String windowOpen = String.format(
                    "window.open('IMS_ExternalSearch.jsp?%s&%s&%s&from=%s&objectId=%s&IMS_SearchHint=%s', '_blank', 'height=800, width=1200, toolbar=0, location=0, menubar=0')",
                    tableParameter, systemNameParameter, relationshipParameter, true, objectId, HtmlEscapers.htmlEscaper().escape("Search for Document Sets"));
            if (IMS_QP_Security_mxJPO.currentUserIsDEPOwner(ctx, objectId) || IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx)) {
                String imageUrl = "../common/images/fugue/16x16/plus.png";
                sb.append(String.format(
                        "<a href=\"javascript:%s\"><img src=\"%s\" title=\"%s\" /></a>", windowOpen, imageUrl, "Connect set of document"));
            }
            LOG.info("hasn't doc link: " + sb.toString());
        }

        return sb.toString();
    }

    private String getDisconnectLinkHTML(
            String disconnectProgram, String disconnectFunction,
            String fromId, String toId, boolean escapeToId, String relationships,
            String imageUrl, String title,
            String onDisconnected) {

        String linkId = UUID.randomUUID().toString();
        String spinnerId = UUID.randomUUID().toString();

        String result = String.format(
                "<img id=\"%s\" src=\"%spinner_16x16.png\" style=\"display:none;\" />",
                HtmlEscapers.htmlEscaper().escape(spinnerId), "../common/images/") + String.format(
                "<a id=\"%s\" href=\"javascript:disconnect('%s', '%s', '%s', %s, '%s', '%s', '%s', %s)\"><img src=\"%s\" title=\"%s\" /></a>",
                HtmlEscapers.htmlEscaper().escape(linkId),
                StringEscapeUtils.escapeEcmaScript(disconnectProgram),
                StringEscapeUtils.escapeEcmaScript(disconnectFunction),
                StringEscapeUtils.escapeEcmaScript(fromId),
                escapeToId ? "'" + StringEscapeUtils.escapeEcmaScript(toId) + "'" : toId,
                StringEscapeUtils.escapeEcmaScript(relationships),
                StringEscapeUtils.escapeEcmaScript(linkId),
                StringEscapeUtils.escapeEcmaScript(spinnerId),
                onDisconnected != null && !onDisconnected.isEmpty() ? onDisconnected : "",
                imageUrl,
                HtmlEscapers.htmlEscaper().escape(title));
        return result;
    }

    private String getRefreshWindowFunction() {
        return "function(){window.location.href = window.location.href;}";
    }

    public String disconnectExternalDocumentSet(Context ctx, String... args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);
        String fromId = (String) argsMap.get("fromId");
        String relationship = (String) argsMap.get("relationship");
        String toId = (String) argsMap.get("toId");
        DomainObject objectDEP = new DomainObject(fromId);
        DomainObject objectDoc = new DomainObject(toId);

        try {
            objectDEP.disconnect(ctx, new RelationshipType(relationship), true, objectDoc);
        } catch (MatrixException me) {
            LOG.error("matrix exception: " + me.getMessage());
            throw (me);
        }
        return "{\"status code\": 200," +
                "\"message\": \"sucess\"}";
    }
}
