import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.json.JSONArray;
import com.matrixone.json.JSONObject;
import matrix.db.BusinessInterface;
import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;
import org.apache.commons.net.util.Base64;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class IMS_ExternalDocumentSet_mxJPO implements IMS_ExternalSystem_mxJPO.ExternalSystemJPO {

    private static final String ATTRIBUTE_IMS_ExternalObjectId = "IMS_ExternalObjectId";
    private static final String ATTRIBUTE_IMS_ExternalObjectPolicy = "IMS_ExternalObjectPolicy";
    private static final String ATTRIBUTE_IMS_ExternalObjectState = "IMS_ExternalObjectState";
    private static final String ATTRIBUTE_IMS_ExternalObjectType = "IMS_ExternalObjectType";

    private static final String INTERFACE_IMS_ExternalObject = "IMS_ExternalObject";

    private static final String TYPE_IMS_DocumentSet = "IMS_DocumentSet";
    private static final String TYPE_IMS_ExternalDocumentSet = "IMS_ExternalDocumentSet";

    private static final String POLICY_IMS_ExternalObject = "IMS_ExternalObject";

    private static final String ATTRIBUTE_IMS_SPFMajorRevision = "IMS_SPFMajorRevision";
    private static final String ATTRIBUTE_IMS_SPFDocVersion = "IMS_SPFDocVersion";
    private static final String ATTRIBUTE_IMS_ProjDocStatus = "IMS_ProjDocStatus";
    private static final String ATTRIBUTE_IMS_Frozen = "IMS_Frozen";

    public IMS_ExternalDocumentSet_mxJPO(Context context, String[] args) throws Exception {
    }

    private static IMS_ExternalSystem_mxJPO.BuildLinkResult buildExternalObjectLink(Context context, Object url, Map objectMap) throws Exception {

        return new IMS_ExternalSystem_mxJPO.BuildLinkResult(
                String.format("%s/common/emxTree.jsp?objectId=%s", url, objectMap.get(DomainConstants.SELECT_ID)),
                (String) objectMap.get(DomainConstants.SELECT_NAME));
    }

    @Override
    public IMS_ExternalSystem_mxJPO.BuildLinkResult buildExternalObjectLink(Context context, String[] args) throws Exception {
        Map map = JPO.unpackArgs(args);
        return buildExternalObjectLink(context, map.get("url"), (Map) map.get("objectMap"));
    }

    private static IMS_ExternalSystem_mxJPO.BuildLinkResult buildExternalObjectLinkForProxyObject(Context context, Object url, String proxyObjectId) throws Exception {
        DomainObject proxyObject = new DomainObject(proxyObjectId);

        return new IMS_ExternalSystem_mxJPO.BuildLinkResult(
                String.format("%s/common/emxTree.jsp?objectId=%s", url, proxyObject.getAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectId)),
                proxyObject.getName(context));
    }

    @Override
    public IMS_ExternalSystem_mxJPO.BuildLinkResult buildExternalObjectLinkForProxyObject(Context context, String[] args) throws Exception {
        return buildExternalObjectLinkForProxyObject(context, args[0], args[1]);
    }

    private static Map jsonObjectToMap(JSONObject jsonObject) throws MatrixException {
        Map map = new HashMap();
        Iterator<String> keys = jsonObject.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, jsonObject.getString(key));
        }
        return map;
    }

    private static MapList jsonArrayToMapList(JSONArray array) throws MatrixException {
        MapList mapList = new MapList();
        for (int i = 0; i < array.length(); i++) {
            mapList.add(jsonObjectToMap(array.getJSONObject(i)));
            if (i == 1000) {
                break;
            }
        }
        return mapList;
    }

    private static String buildRequestBody(String type, String where, List<String> selects) throws UnsupportedEncodingException {
        StringBuilder selectBuilder = new StringBuilder();
        for (String select : selects) {
            if (selectBuilder.length() > 0) {
                selectBuilder.append(",");
            }
            selectBuilder.append(String.format("\"%s\"", select));
        }

        return String.format(
                "{" +
                "   \"type\": \"%s\"," +
                "   \"where\": \"%s\"," +
                "   \"select\": [%s]" +
                "}",
                URLEncoder.encode(type, "UTF-8"),
                where,
                selectBuilder);
    }

    private static String findExternalObjects(String url, String user, String password, String where) throws IOException {
        URL searchUrl = new URL(url + "/remote/businessobject/search");
        HttpURLConnection connection = (HttpURLConnection) searchUrl.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Authorization", "Basic " + new String(Base64.encodeBase64(String.format("%s:%s", user, password).getBytes())));

        connection.setDoOutput(true);
        DataOutputStream out = new DataOutputStream(connection.getOutputStream());

        String body = buildRequestBody(
                TYPE_IMS_DocumentSet,
                where,
                Arrays.asList(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_TYPE,
                        DomainConstants.SELECT_NAME,
                        DomainConstants.SELECT_REVISION,
                        DomainConstants.SELECT_POLICY,
                        DomainConstants.SELECT_CURRENT,
                        DomainConstants.SELECT_DESCRIPTION,
                        DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFMajorRevision),
                        DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFDocVersion),
                        DomainObject.getAttributeSelect(ATTRIBUTE_IMS_ProjDocStatus),
                        DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Frozen)
                ));

        out.writeBytes(body);
        out.flush();
        out.close();

        connection.getResponseCode();
        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder contentStringBuilder = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            contentStringBuilder.append(inputLine);
        }
        in.close();

        String content = contentStringBuilder.toString();
        connection.disconnect();
        return content;
    }

    private static MapList findExternalObjects(Context context, String url, String user, String password, String query) throws Exception {
        return jsonArrayToMapList(new JSONArray(findExternalObjects(url, user, password, String.format("escape name ~~ const'%s'", query))));
    }

    @Override
    public MapList findExternalObjects(Context context, String[] args) throws Exception {
        return findExternalObjects(context, args[0], args[1], args[2], args[3]);
    }

    private static Map findExternalObjectMap(Context context, String url, String user, String password, String externalObjectId) throws Exception {
        JSONArray array = new JSONArray(findExternalObjects(url, user, password, String.format("id==%s", externalObjectId)));
        return array.length() == 1 ? jsonObjectToMap(array.getJSONObject(0)) : null;
    }

    @Override
    public Map findExternalObjectMap(Context context, String[] args) throws Exception {
        return findExternalObjectMap(context, args[0], args[1], args[2], args[3]);
    }

    private static DomainObject ensureProxyObject(Context context, Map externalObjectMap) throws Exception {

        DomainObject object = new DomainObject(new BusinessObject(
                TYPE_IMS_ExternalDocumentSet,
                (String) externalObjectMap.get(DomainConstants.SELECT_NAME),
                (String) externalObjectMap.get(DomainConstants.SELECT_REVISION),
                context.getVault().getName()));

        if (!object.exists(context)) {
            object.create(context, POLICY_IMS_ExternalObject);
            object.addBusinessInterface(context, new BusinessInterface(INTERFACE_IMS_ExternalObject, context.getVault()));
        }

        object.setDescription(context, (String) externalObjectMap.get(DomainConstants.SELECT_DESCRIPTION));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectId, (String) externalObjectMap.get(DomainConstants.SELECT_ID));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectType, (String) externalObjectMap.get(DomainConstants.SELECT_TYPE));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectPolicy, (String) externalObjectMap.get(DomainConstants.SELECT_POLICY));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ExternalObjectState, (String) externalObjectMap.get(DomainConstants.SELECT_CURRENT));

        object.setAttributeValue(context, ATTRIBUTE_IMS_SPFMajorRevision, (String) externalObjectMap.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFMajorRevision)));
        object.setAttributeValue(context, ATTRIBUTE_IMS_SPFDocVersion, (String) externalObjectMap.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_SPFDocVersion)));
        object.setAttributeValue(context, ATTRIBUTE_IMS_ProjDocStatus, (String) externalObjectMap.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_ProjDocStatus)));
        object.setAttributeValue(context, ATTRIBUTE_IMS_Frozen, (String) externalObjectMap.get(DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Frozen)));

        return object;
    }

    @Override
    public DomainObject ensureProxyObject(Context context, String[] args) throws Exception {
        return ensureProxyObject(context, (Map) JPO.unpackArgs(args));
    }
}