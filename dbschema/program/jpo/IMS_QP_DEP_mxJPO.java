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
            objectId = (String) requestMap.get(IMS_QP_Constants_mxJPO.OBJECT_ID);
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
        String objectId = (String) paramMap.get(IMS_QP_Constants_mxJPO.OBJECT_ID);

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
                if (IMS_QP_Security_mxJPO.currentUserIsDEPOwner(ctx, objectId) || IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(ctx)) {
                    sb.append(getDisconnectLinkHTML(IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEP, "disconnectExternalDocumentSet",
                            objectId, docId, true, "IMS_QP_DEP2Doc", "../common/images/fugue/16x16/cross.png", "Disconnect", getRefreshWindowFunction()));
                }
                String emxTableLinkClick = String.format("emxTableColumnLinkClick('../common/emxForm.jsp?objectId=%s&form=type_IMS_ExternalDocumentSet')", docId);
                sb.append(String.format("<a href=\"javascript:%s\"><img src=\"%s\" />%s</a>", emxTableLinkClick, "../common/images/fugue/16x16/document.png", HtmlEscapers.htmlEscaper().escape(docName)));
            }
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

    public MapList getRelatedSystems(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args: " + e.getMessage());
            e.printStackTrace();
        }

        //get objectID
        String objectId = (String) argsMap.get(IMS_QP_Constants_mxJPO.OBJECT_ID);
        StringList selects = new StringList();
        selects.add("id");

        DomainObject parent = null;
        try {
            parent = new DomainObject(objectId);
        } catch (Exception e) {
            LOG.error("error getting domain object: " + objectId + ": " + e.getMessage());
            e.printStackTrace();
        }

        MapList listObjects = new MapList();
        try {
            listObjects = parent.getRelatedObjects(ctx,
                    /*relationship*/"IMS_PBS2DEP",
                    /*type*/DomainConstants.QUERY_WILDCARD,
                    /*object attributes*/ selects,
                    /*relationship selects*/ null,
                    /*getTo*/ true, /*getFrom*/ false,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ null,
                    /*relationship where*/ null,
                    /*limit*/ 0);
        } catch (FrameworkException e) {
            LOG.error("error getting related objects from: " + objectId + "  with message: " + e.getMessage());
            e.printStackTrace();
        }

        return listObjects;
    }

    public String disconnectSystem(Context ctx, String... args) {

        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");
        if (rowIDs.length == 0) {
            return "no selected items";
        }

        String objectId = (String) argsMap.get(IMS_QP_Constants_mxJPO.OBJECT_ID);
        DomainObject domainObject = null;
        try {
            domainObject = new DomainObject(objectId);
        } catch (Exception e) {
            LOG.error("error getting domain object: " + e.getMessage());
            e.printStackTrace();
        }
        String[] cleanedIDs = new String[rowIDs.length];
        if (domainObject != null) {
            RelationshipType relationshipType = new RelationshipType("IMS_PBS2DEP");
            for (int i = 0; i < rowIDs.length; i++) {
                String[] rowIdArray = rowIDs[i].split("\\|");
                cleanedIDs[i] = rowIdArray[1];
                try {
                    domainObject.disconnect(ctx, relationshipType, false, new DomainObject(rowIdArray[1]));
                } catch (Exception e) {
                    LOG.error("error disconnect relationship from " + objectId + " from " + rowIdArray[1]);
                    e.printStackTrace();
                }
            }
        }
        return "all ok";
    }

    public MapList findSystemByName(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting args: " + e.getMessage());
            e.printStackTrace();
        }

        String partWhere = "";
        String externalSystemQuery = (String) argsMap.get("IMS_ExternalSystemQuery");
        if (UIUtil.isNotNullAndNotEmpty(externalSystemQuery) && externalSystemQuery.contains(",")) {
            externalSystemQuery = externalSystemQuery.replaceAll("\\s+", "");
            String[] rawQueryParts = externalSystemQuery.split(",");
            for (int i = 0; i < rawQueryParts.length; i++) {
                partWhere += "name ~~'*" + rawQueryParts[i] + "*'";
                if ((rawQueryParts.length - i) > 1) {
                    partWhere += "||";
                }
            }
        } else {
            partWhere = "name ~~'*" + externalSystemQuery + "*'";
        }

        //get objectID
        String objectId = (String) argsMap.get(IMS_QP_Constants_mxJPO.OBJECT_ID);

        MapList listObjects = new MapList();
        try {
            listObjects = DomainObject.findObjects(ctx,
                    /*var1 type*/IMS_QP_Constants_mxJPO.SYSTEM_TYPES,
                    /*var2 name*/DomainConstants.QUERY_WILDCARD,
                    /*var3 revision*/DomainConstants.QUERY_WILDCARD,
                    /*var4 owner*/DomainConstants.QUERY_WILDCARD,
                    /*var5 vault*/ ctx.getVault().getName(),
                    /*var6 where*/ partWhere +
                            "&&from[IMS_PBS2DEP]==false",
                    /*var8 expand type*/false,
                    /*var14*/new StringList("id")
            );
        } catch (FrameworkException e) {
            LOG.error("error getting related objects from: " + objectId + "  with message: " + e.getMessage());
            e.printStackTrace();
        }

        return listObjects;
    }

    public void connectSystem(Context ctx, String... args) {
        String depId = args[0];
        DomainObject depObject = null;
        try {
            depObject = new DomainObject(depId);
        } catch (Exception e) {
            LOG.error("error getting domain object: " + e.getMessage());
            e.printStackTrace();
        }
        String systemId = args[1];
        systemId = systemId.replace("[", "").replace("]", "");
        String[] systemIds = systemId.split(",");

        for (int i = 0; i < systemIds.length; i++) {
            try {
                DomainRelationship relationship = DomainRelationship.connect(ctx, /*from*/ new DomainObject(systemIds[i]), "IMS_PBS2DEP", /*to*/depObject);
                LOG.info("system Id: " + systemIds[i]);
            } catch (Exception e) {
                LOG.error("error creating connection: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}