import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.db.RelationshipType;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class IMS_QP_Classifier_mxJPO {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    private final String TYPE_CLASSIFIER = "IMS_QP_Classifier";
    private final String RELATIONSHIP_DEP_2_CLASSIFIER = "IMS_QP_DEP2Classifier";

    public MapList findAll(Context ctx, String... args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        String where = null;
        if (argsMap.containsKey("depId")) {
            String depId = (String) argsMap.get("depId");
            where = String.format("to[" + RELATIONSHIP_DEP_2_CLASSIFIER + "].from.id==%s", depId);
        }

        MapList allClassifiers = new MapList();
        try {
            String VAULT = IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION;
            allClassifiers = DomainObject.findObjects(ctx,
                    /*type*/ TYPE_CLASSIFIER,/*vault*/VAULT, /*where*/where,/*selects*/ new StringList(DomainConstants.SELECT_ID));
        } catch (FrameworkException fe) {
            LOG.error("error getting tasks: " + fe.getMessage());
            fe.printStackTrace();
        }
        return allClassifiers;
    }

    public MapList findAllFiltered(Context ctx, String... args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        String qPlanId = (String) argsMap.get("objectId");

        MapList relatedClassifiers = new MapList();
        try {
            DomainObject objectQPlan = new DomainObject(qPlanId);
            String dep2QPlan = "IMS_QP_DEP2QPlan";
            String depId = objectQPlan.getInfo(ctx, "to[" + dep2QPlan + "].from.id");
            DomainObject objectDEP = new DomainObject(depId);

            relatedClassifiers = objectDEP.getRelatedObjects(ctx,
                    /*relationship*/RELATIONSHIP_DEP_2_CLASSIFIER,
                    /*type*/TYPE_CLASSIFIER,
                    /*object attributes*/ new StringList(DomainConstants.SELECT_ID),
                    /*relationship selects*/ null,
                    /*getTo*/ false, /*getFrom*/ true,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ null,
                    /*relationship where*/ null,
                    /*limit*/ 0);

        } catch (FrameworkException fe) {
            LOG.error("error getting tasks: " + fe.getMessage());
            fe.printStackTrace();
        }

        LOG.info("related classifiers: " + relatedClassifiers);
        return relatedClassifiers;
    }

    public void createPostProcess(Context ctx, String... args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);

        Map requestMap = (Map) argsMap.get("requestMap");
        String depOID = (String) requestMap.get("depOID");

        if (UIUtil.isNotNullAndNotEmpty(depOID)) {
            Map paramMap = (Map) argsMap.get("paramMap");
            String newObjectId = (String) paramMap.get("newObjectId");
            DomainObject objectDep = new DomainObject(depOID);
            DomainObject objectClassifier = new DomainObject(newObjectId);
            DomainRelationship.connect(ctx, objectDep, RELATIONSHIP_DEP_2_CLASSIFIER, objectClassifier);
        }
    }

    public Map deleteClassifiers(Context ctx, String... args) {

        String[] rowIDs = new String[0];

        try {
            Map paramMap = JPO.unpackArgs(args);
            rowIDs = (String[]) paramMap.get("emxTableRowId");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String[] ids = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            ids[i] = rowIDs[i].substring(0, rowIDs[i].indexOf("|"));
        }

        if (ids.length > 0) {
            try {
                DomainObject.deleteObjects(ctx, ids);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return new HashMap();
    }

    public String getConnectedExternalDocumentSetHTML(Context ctx, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();

        Map argsMap = JPO.unpackArgs(args);
        Map paramMap = (Map) argsMap.get("paramMap");
        String objectId = (String) paramMap.get("objectId");

        DomainObject objectClassifier = new DomainObject(objectId);
        StringList selects = new StringList("id");
        selects.add("name");

        String classifier2Doc = "IMS_QP_Classifier2Doc";
        String externalDocumentSet = "IMS_ExternalDocumentSet";
        MapList externalDocuments = objectClassifier.getRelatedObjects(ctx,
                /*relationship*/classifier2Doc,
                /*type*/externalDocumentSet,
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

                if (IMS_QP_Security_mxJPO.isUserAdmin(ctx) || IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx)) {
                    sb.append(getDisconnectLinkHTML(TYPE_CLASSIFIER, "disconnectExternalDocumentSet",
                            objectId, docId, true, classifier2Doc, "../common/images/fugue/16x16/cross.png", "Disconnect", getRefreshWindowFunction()));
                }
                String emxTableLinkClick = String.format("emxTableColumnLinkClick('../common/emxForm.jsp?objectId=%s&form=type_IMS_ExternalDocumentSet')", docId);
                sb.append(String.format("<a href=\"javascript:%s\"><img src=\"%s\" />%s</a>", emxTableLinkClick, "../common/images/fugue/16x16/document.png", HtmlEscapers.htmlEscaper().escape(docName)));
            }
            LOG.info("has doc link: " + sb.toString());

        } else {
            String tableParameter = String.format("table=%s", externalDocumentSet);
            String systemNameParameter = String.format("IMS_ExternalSystemName=%s", IMS_QP_Constants_mxJPO.HNH_PRODUCTION_SRV);
            String relationshipParameter = String.format("relationship=%s", classifier2Doc);

            String windowOpen = String.format(
                    "window.open('IMS_ExternalSearch.jsp?%s&%s&%s&from=%s&objectId=%s&IMS_SearchHint=%s', '_blank', 'height=800, width=1200, toolbar=0, location=0, menubar=0')",
                    tableParameter, systemNameParameter, relationshipParameter, true, objectId, HtmlEscapers.htmlEscaper().escape("Search for Document Sets"));
            if (IMS_QP_Security_mxJPO.isUserAdmin(ctx) || IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx)) {
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
        DomainObject objectClassifier = new DomainObject(fromId);
        DomainObject objectDoc = new DomainObject(toId);

        try {
            objectClassifier.disconnect(ctx, new RelationshipType(relationship), true, objectDoc);
        } catch (MatrixException me) {
            LOG.error("matrix exception: " + me.getMessage());
            throw (me);
        }
        return "{\"status code\": 200," +
                "\"message\": \"success\"}";
    }
}
