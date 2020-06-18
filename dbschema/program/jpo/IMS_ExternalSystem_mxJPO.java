import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.*;
import matrix.util.StringList;

import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class IMS_ExternalSystem_mxJPO {

    public static final String ATTRIBUTE_IMS_ExternalSystemName = "IMS_ExternalSystemName";
    private static final String ATTRIBUTE_IMS_ExternalSystemUrl = "IMS_ExternalSystemUrl";
    private static final String ATTRIBUTE_IMS_ExternalSystemServiceUrl = "IMS_ExternalSystemServiceUrl";
    private static final String ATTRIBUTE_IMS_ExternalSystemUser = "IMS_ExternalSystemUser";
    private static final String ATTRIBUTE_IMS_ExternalSystemPassword = "IMS_ExternalSystemPassword";
    private static final String ATTRIBUTE_IMS_ExternalSystemProgram = "IMS_ExternalSystemProgram";

    private static final String TYPE_IMS_ExternalSystem = "IMS_ExternalSystem";

    public static final String RELATIONSHIP_IMS_Object2ExternalSystem = "IMS_Object2ExternalSystem";

    private static final String METHOD_buildExternalObjectLink = "buildExternalObjectLink";
    private static final String METHOD_buildExternalObjectLinkForProxyObject = "buildExternalObjectLinkForProxyObject";
    private static final String METHOD_findExternalObjects = "findExternalObjects";
    private static final String METHOD_findExternalObjectMap = "findExternalObjectMap";
    private static final String METHOD_ensureProxyObject = "ensureProxyObject";

    private static final Object LOCK = new Object();

    public IMS_ExternalSystem_mxJPO(Context context, String[] args) throws Exception {
    }

    public static final class BuildLinkResult {

        private final String url;
        private final String name;

        public String getUrl() {
            return url;
        }

        public String getName() {
            return name;
        }

        public BuildLinkResult(String url, String name) {
            this.url = url;
            this.name = name;
        }
    }

    public interface ExternalSystemJPO {

        BuildLinkResult buildExternalObjectLink(Context context, String[] args) throws Exception;

        BuildLinkResult buildExternalObjectLinkForProxyObject(Context context, String[] args) throws Exception;

        MapList findExternalObjects(Context context, String[] args) throws Exception;

        Map findExternalObjectMap(Context context, String[] args) throws Exception;

        DomainObject ensureProxyObject(Context context, String[] args) throws Exception;
    }

    public static class ExternalSystem {

        private final DomainObject object;
        private String description;
        private String url;
        private String serviceUrl;
        private final String user;
        private final String password;
        private final String program;

        public DomainObject getObject() {
            return object;
        }

        public String getDescription() {
            return description;
        }

        public String getUrl() {
            return url;
        }

        public String getServiceUrl() {
            return serviceUrl;
        }

        public String getUser() {
            return user;
        }

        public String getPassword() {
            return password;
        }

        public String getProgram() {
            return program;
        }

        public ExternalSystem(Context context, String name) throws Exception {
            object = new DomainObject(new BusinessObject(TYPE_IMS_ExternalSystem, name, "", context.getVault().getName()));
            description = object.getDescription(context);

            url = object.getAttributeValue(context, ATTRIBUTE_IMS_ExternalSystemUrl);
            if (url.endsWith("/")) {
                url = url.substring(0, url.length() - 1);
            }

            serviceUrl = object.getAttributeValue(context, ATTRIBUTE_IMS_ExternalSystemServiceUrl);
            if (serviceUrl.endsWith("/")) {
                serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 1);
            }

            user = object.getAttributeValue(context, ATTRIBUTE_IMS_ExternalSystemUser);
            password = FrameworkUtil.decrypt(object.getAttributeValue(context, ATTRIBUTE_IMS_ExternalSystemPassword));
            program = object.getAttributeValue(context, ATTRIBUTE_IMS_ExternalSystemProgram);
        }
    }

    public static MapList findObjects(Context context, String externalSystemName, String query) throws Exception {
        ExternalSystem externalSystem = new ExternalSystem(context, externalSystemName);

        return JPO.invoke(
                context,
                externalSystem.getProgram(),
                null,
                METHOD_findExternalObjects,
                new String[]{ externalSystem.getServiceUrl(), externalSystem.getUser(), externalSystem.getPassword(), query },
                MapList.class);
    }

    public static MapList findObjects(Context context, String[] args) throws Exception {
        Map programMap = IMS_KDD_mxJPO.getProgramMap(args);

        return findObjects(
                context,
                (String) programMap.get(ATTRIBUTE_IMS_ExternalSystemName),
                (String) programMap.get("IMS_ExternalSystemQuery"));
    }

    private BuildLinkResult getBuildLinkResult(Context context, ExternalSystem externalSystem, Map objectMap) throws Exception {
        Map buildLinkMap = new HashMap();
        buildLinkMap.put("url", externalSystem.getUrl());
        buildLinkMap.put("objectMap", objectMap);

        return JPO.invoke(
                context,
                externalSystem.getProgram(),
                null,
                METHOD_buildExternalObjectLink,
                JPO.packArgs(buildLinkMap),
                BuildLinkResult.class);
    }

    @SuppressWarnings("unused")
    public Vector getExternalObjectLinkTableHTML(Context context, String[] args) throws Exception {
        String externalSystemName = (String) IMS_KDD_mxJPO.getParamListMap(args).get(ATTRIBUTE_IMS_ExternalSystemName);
        ExternalSystem externalSystem = new ExternalSystem(context, externalSystemName);

        Vector results = new Vector();
        for (Object objectMapObject : IMS_KDD_mxJPO.getObjectMapList(args)) {
            Map objectMap = (Map) objectMapObject;
            BuildLinkResult buildLinkResult = getBuildLinkResult(context, externalSystem, objectMap);

            results.addElement(String.format(
                    "<a target=\"_blank\" href=\"%s\">%s</a>",
                    HtmlEscapers.htmlEscaper().escape(buildLinkResult.getUrl()),
                    HtmlEscapers.htmlEscaper().escape(buildLinkResult.getName())));
        }
        return results;
    }

    @SuppressWarnings("unused")
    public String getExternalObjectLinkFormHTML(Context context, String[] args) throws Exception {
        String formObjectId = IMS_KDD_mxJPO.getObjectIdFromParamMap(args);
        String externalSystemName = (String) IMS_KDD_mxJPO.getRequestMap(args).get(ATTRIBUTE_IMS_ExternalSystemName);
        ExternalSystem externalSystem = new ExternalSystem(context, externalSystemName);

        BuildLinkResult buildLinkResult = JPO.invoke(
                context,
                externalSystem.getProgram(),
                null,
                METHOD_buildExternalObjectLinkForProxyObject,
                new String[]{ externalSystem.getUrl(), formObjectId },
                BuildLinkResult.class);

        return buildLinkResult != null ?
                String.format(
                        "<a target=\"_blank\" href=\"%s\"><img src=\"%s\" />%s</a><br /><span style=\"font-size: 10px; color: gray\">You must be logged in to '%s' to open the link.</span>",
                        HtmlEscapers.htmlEscaper().escape(buildLinkResult.getUrl()),
                        IMS_KDD_mxJPO.FUGUE_16x16 + "document--arrow.png",
                        HtmlEscapers.htmlEscaper().escape(buildLinkResult.getName()),
                        HtmlEscapers.htmlEscaper().escape(externalSystem.getDescription())) :
                "";
    }

    public Vector getObjectMapValue(Context context, String[] args) throws Exception {
        String key = (String) ((Map) ((Map) IMS_KDD_mxJPO.getProgramMap(args).get("columnMap")).get("settings")).get("objectMapKey");
        Vector results = new Vector();
        for (Object objectMapObject : IMS_KDD_mxJPO.getObjectMapList(args)) {
            results.addElement(((Map) objectMapObject).get(key));
        }
        return results;
    }

    public static String encrypt(Context context, String[] args) throws Exception {
        String result = FrameworkUtil.encrypt(args[0]);
        System.out.println(result);
        return result;
    }

    public static String decrypt(Context context, String[] args) throws Exception {
        String result = FrameworkUtil.decrypt(args[0]);
        System.out.println(result);
        return result;
    }

    @SuppressWarnings("unused")
    public String connectExternalObject(final Context context, String[] args) throws Exception {
        final ExternalSystem externalSystem = new ExternalSystem(context, args[0]);
        final String relationship = args[1];
        final boolean from = "true".equalsIgnoreCase(args[2]);
        final DomainObject object = new DomainObject(args[3]);
        final String externalObjectId = args[4];

        final Map externalObjectMap = JPO.invoke(
                context,
                externalSystem.getProgram(),
                null,
                METHOD_findExternalObjectMap,
                new String[]{ externalSystem.getServiceUrl(), externalSystem.getUser(), externalSystem.getPassword(), externalObjectId },
                Map.class);

        if (externalObjectMap != null) {
            synchronized (LOCK) {

                IMS_KDD_mxJPO.runInTransaction(context, new IMS_KDD_mxJPO.Action() {
                    @Override
                    public void run() throws Exception {

                        Map connectedMap = object.getRelatedObject(
                                context,
                                relationship, from,
                                new StringList(new String[]{ DomainConstants.SELECT_ID }),
                                null);

                        if (connectedMap != null) {
                            object.disconnect(context, new RelationshipType(relationship), from, IMS_KDD_mxJPO.mapToObject(context, connectedMap));
                        }

                        DomainObject proxyObject = JPO.invoke(
                                context,
                                externalSystem.getProgram(),
                                null,
                                METHOD_ensureProxyObject,
                                JPO.packArgs(externalObjectMap),
                                DomainObject.class);

                        object.connect(context, new RelationshipType(relationship), from, proxyObject);

                        IMS_KDD_mxJPO.connectIfNotConnected(context, RELATIONSHIP_IMS_Object2ExternalSystem, proxyObject, externalSystem.getObject());
                    }
                });

                return "";
            }
        }
        return "External object not found.";
    }
}