import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.*;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Set;

@SuppressWarnings("unchecked")
public class IMS_KDD_mxJPO {

    public interface Filter<T> {
        boolean matches(T obj) throws Exception;
    }

    public interface NameResolver {
        String findName(Context context, Map map) throws Exception;
    }

    public interface ObjectIdPredicate {
        boolean evaluate(String objectId) throws Exception;
    }

    interface SimpleLogger {
        void log(String message);
        void log(String message, Throwable t);
        void log(Throwable t);
    }

    public interface Action {
        void run() throws Exception;
    }

    public interface Func<TOut> {
        TOut run() throws Exception;
    }

    public interface Func1<TIn, TOut> {
        TOut run(TIn arg) throws Exception;
    }

    static class SimpleLoggerImpl implements SimpleLogger {

        private final String loggerName;

        public SimpleLoggerImpl(String loggerName) {
            this.loggerName = loggerName;
        }

        public void log(String message) {
            IMS_KDD_mxJPO.log(loggerName, message);
        }

        public void log(String message, Throwable t) {
            IMS_KDD_mxJPO.log(loggerName, message, t);
        }

        public void log(Throwable t) {
            IMS_KDD_mxJPO.log(loggerName, t);
        }
    }

    public static class ConnectionInfo {
        public final String Id;
        public final String Relationship;
        public final String FromId;
        public final String ToId;
        public final String Owner;
        public final String Originated;

        public ConnectionInfo(String id, String relationship, String fromId, String toId, String owner, String originated) {
            Id = id;
            Relationship = relationship;
            FromId = fromId;
            ToId = toId;
            Owner = owner;
            Originated = originated;
        }

        public ConnectionInfo(ConnectionInfo connection) {
            Id = connection.Id;
            Relationship = connection.Relationship;
            FromId = connection.FromId;
            ToId = connection.ToId;
            Owner = connection.Owner;
            Originated = connection.Originated;
        }
    }

    @SuppressWarnings("unused")
    public IMS_KDD_mxJPO(Context context, String[] args) throws Exception {
    }

    public static final String UTF8 = "UTF-8";

    public static final String ROW_ID_KEY = "id[level]";

    public static class Messages {
        public static final String AccessDenied = "Access denied";
    }

    public static class Attributes {
        public static final String First_Name = "First Name";
        public static final String Last_Name = "Last Name";
        public static final String IMS_Gender = "IMS_Gender";
        public static final String Email_Address = "Email Address";
        public static final String IMS_FullName = "IMS_FullName";
    }

    public static class Types {
        public static final String Person = "Person";
        public static final String Department = "Department";
        public static final String Company = "Company";
        public static final String IMS_Task = "IMS_Task";
        public static final String IMS_DocumentSet = "IMS_DocumentSet";
    }

    public static class Vaults {
        public static final String eService_Production = "eService Production";
    }

    public static final String COMMON_IMAGES = "../common/images/";
    public static final String FUGUE_16x16 = COMMON_IMAGES + "fugue/16x16/";

    public static final String E_SERVICE_PRODUCTION = "eService Production";

    public static void log(String loggerName, String message) {
        Logger.getLogger(loggerName).debug(message);
    }

    public static void log(String loggerName, String message, Throwable t) {
        t.printStackTrace();
        Logger.getLogger(loggerName).error(message, t);
    }

    public static void log(String loggerName, Throwable t) {
        t.printStackTrace();
        Logger.getLogger(loggerName).error(t);
    }

    public static String getString(Context context, String name, String language) throws FrameworkException {
        return EnoviaResourceBundle.getFrameworkStringResourceProperty(context, name, new Locale(language));
    }

    public static String getString(Context context, String name) throws FrameworkException {
        return getString(context, name, context.getSession().getLanguage());
    }

    public static DomainObject getObjectFromProgramMap(Context context, HashMap programMap) throws Exception {
        return new DomainObject((String) programMap.get("objectId"));
    }

    public static HashMap getProgramMap(String[] args) throws Exception {
        return JPO.unpackArgs(args);
    }

    public static DomainObject getObjectFromProgramMap(Context context, String[] args) throws Exception {
        return getObjectFromProgramMap(context, getProgramMap(args));
    }

    public static MapList getObjectMapList(String[] args) throws Exception {
        return (MapList) getProgramMap(args).get("objectList");
    }

    public static List<Map> getObjectListMaps(String[] args) throws Exception {
        List<Map> maps = new ArrayList<>();
        for (Object item : getObjectMapList(args)) {
            maps.add((Map) item);
        }
        return maps;
    }

    public static void printProgramMap(String loggerName, String[] args) throws Exception {
        log(loggerName, "----------------------------------------");
        for (Object obj : getProgramMap(args).entrySet()) {
            Map.Entry entry = (Map.Entry) obj;
            log(loggerName, entry.getKey() + " : " + entry.getValue());
        }
        log(loggerName, "----------------------------------------");
    }

    public static boolean isRuLocale(String locale) throws Exception {
        return locale != null && locale.toLowerCase().startsWith("ru");
    }

    public static Locale getLocale(String[] args) throws Exception {
        return  (Locale) getParamMap(args).get("localeObj");
    }

    public static boolean isRuLocale(String[] args) throws Exception {
        Locale locale = getLocale(args);
        return locale != null && isRuLocale(locale.toString());
    }

    public static Map getParamMap(String[] args) throws Exception {
        return (Map) ((HashMap) JPO.unpackArgs(args)).get("paramList");
    }

    public static String getIdFromMap(Object obj) {
        return obj != null ? (String) ((Map) obj).get(DomainConstants.SELECT_ID) : null;
    }

    public static String getNameFromMap(Object obj) {
        return obj != null ? (String) ((Map) obj).get(DomainConstants.SELECT_NAME) : null;
    }

    public static String getRevisionFromMap(Object obj) {
        return obj != null ? (String) ((Map) obj).get(DomainConstants.SELECT_REVISION) : null;
    }

    public static String getTypeFromMap(Object obj) {
        return obj != null ? (String) ((Map) obj).get(DomainConstants.SELECT_TYPE) : null;
    }

    public static String getOriginatedFromMap(Object obj) {
        return obj != null ? (String) ((Map) obj).get(DomainConstants.SELECT_ORIGINATED) : null;
    }

    public static DomainObject idToObject(Context context, String id) throws Exception {
        return new DomainObject(id);
    }

    public static DomainObject mapToObject(Context context, Object obj) throws Exception {
        return idToObject(context, getIdFromMap(obj));
    }

    public static StringList getIdTypeNameSelects() {
        StringList busSelects = new StringList();
        busSelects.addElement(DomainConstants.SELECT_ID);
        busSelects.addElement(DomainConstants.SELECT_TYPE);
        busSelects.addElement(DomainConstants.SELECT_NAME);
        return busSelects;
    }

    private static StringList getIdTypeNameRevisionSelects() {
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_TYPE);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(DomainConstants.SELECT_REVISION);
        return selects;
    }

    public static List<Map> getRelatedObjectMaps(
            final Context context, DomainObject domainObject, String relationships, boolean from,
            String typePattern,
            List<String> busSelects, List<String> relSelects, String busWhere, String relWhere, boolean sort)
            throws MatrixException {

        StringList busSelectList = getIdTypeNameRevisionSelects();
        if (busSelects != null) {
            for (String select : busSelects) {
                if (!busSelectList.contains(select)) {
                    busSelectList.add(select);
                }
            }
        }

        StringList relSelectList = new StringList();
        if (relSelects != null) {
            for (String select : relSelects) {
                if (!relSelectList.contains(select)) {
                    relSelectList.add(select);
                }
            }
        }

        MapList mapList = domainObject.getRelatedObjects(
                context, relationships, typePattern != null ? typePattern : "*",
                busSelectList,
                relSelectList,
                !from, from, (short) 1, busWhere, relWhere);

        List<Map> maps = new ArrayList<>();
        if (mapList != null) {
            for (Object obj : mapList) {
                maps.add((Map) obj);
            }
        }

        if (sort) {
            Collections.sort(maps, new Comparator<Map>() {
                @Override
                public int compare(Map map1, Map map2) {
                    try {
                        return getName(context, map1).compareTo(getName(context, map2));
                    } catch (Exception e) {
                        return 0;
                    }
                }
            });
        }

        return maps;
    }

    public static List<Map> getRelatedObjectMaps(
            final Context context, DomainObject domainObject, String relationships, boolean from,
            List<String> busSelects, List<String> relSelects, String busWhere, String relWhere, boolean sort)
            throws MatrixException {

        return getRelatedObjectMaps(context, domainObject, relationships, from, null, busSelects, relSelects, busWhere, relWhere, sort);
    }

    public static List<Map> getRelatedObjectMaps(
            final Context context, DomainObject domainObject, String relationships, boolean from,
            List<String> busSelects, String busWhere, String relWhere, boolean sort)
            throws MatrixException {

        return getRelatedObjectMaps(context, domainObject, relationships, from, null, busSelects, null, busWhere, relWhere, sort);
    }

    public static List<Map> getRelatedObjectMaps(
            Context context, DomainObject domainObject, String relationships, boolean from, boolean sort)
            throws MatrixException {

        return getRelatedObjectMaps(context, domainObject, relationships, from, null, null, null, sort);
    }

    public static List<Map> getRelatedObjectMaps(
            Context context, DomainObject domainObject, String relationships, boolean from)
            throws MatrixException {

        return getRelatedObjectMaps(context, domainObject, relationships, from, null, null, null, false);
    }

    public static Map getRelatedObjectMap(
            Context context, DomainObject domainObject, String relationships, boolean from)
            throws MatrixException {

        return domainObject.getRelatedObject(
                context, relationships, from,
                new StringList(new String[] {DomainConstants.SELECT_ID, DomainConstants.SELECT_TYPE, DomainConstants.SELECT_NAME}),
                new StringList());
    }

    public static String getEditLinkHTML(
            Context context, String editId, String objectId, String attributeName, String rowId, String program, String function,
            String imageUrl, String title) throws FrameworkException {

        return String.format(
                "<a id=\"edit-%s-link-%s\" href=\"javascript:IMS_KDD_editStart('%s', '%s', '%s', '%s', '%s', '%s')\"><img src=\"%s\" title=\"%s\" /></a>",
                HtmlEscapers.htmlEscaper().escape(editId),
                HtmlEscapers.htmlEscaper().escape(objectId),
                StringEscapeUtils.escapeEcmaScript(editId),
                StringEscapeUtils.escapeEcmaScript(objectId),
                StringEscapeUtils.escapeEcmaScript(attributeName),
                StringEscapeUtils.escapeEcmaScript(rowId),
                HtmlEscapers.htmlEscaper().escape(program),
                HtmlEscapers.htmlEscaper().escape(function),
                imageUrl,
                HtmlEscapers.htmlEscaper().escape(title));
    }

    public static String getEditLinkHTML(
            Context context, String editId, String objectId, String attributeName, String rowId, String program, String function) throws FrameworkException {

        return getEditLinkHTML(
                context, editId, objectId, attributeName, rowId, program, function,
                FUGUE_16x16 + "pencil-small.png",
                getString(context, "emxFramework.Label.KDD_Edit"));
    }

    public static String getConnectLinkHTML(
            String connectProgram, String connectFunction,
            String fromId, String toId, boolean escapeToId, String relationship, String imageURL, String title,
            String onConnected) {

        String linkId = UUID.randomUUID().toString();
        String spinnerId = UUID.randomUUID().toString();

        return String.format(
                "<img id=\"%s\" src=\"%sspinner_16x16.png\" style=\"display:none;\" />",
                HtmlEscapers.htmlEscaper().escape(spinnerId), COMMON_IMAGES) +
                String.format(
                        "<a id=\"%s\" href=\"javascript:IMS_KDD_connect('%s', '%s', '%s', %s, '%s', '%s', '%s', %s)\"><img src=\"%s\" title=\"%s\" /></a>",
                        HtmlEscapers.htmlEscaper().escape(linkId),
                        StringEscapeUtils.escapeEcmaScript(connectProgram),
                        StringEscapeUtils.escapeEcmaScript(connectFunction),
                        StringEscapeUtils.escapeEcmaScript(fromId),
                        escapeToId ? "'" + StringEscapeUtils.escapeEcmaScript(toId) + "'" : toId,
                        StringEscapeUtils.escapeEcmaScript(relationship),
                        StringEscapeUtils.escapeEcmaScript(linkId),
                        StringEscapeUtils.escapeEcmaScript(spinnerId),
                        onConnected != null && !onConnected.isEmpty() ? onConnected : "",
                        imageURL,
                        HtmlEscapers.htmlEscaper().escape(title.replace("&#10;", "|")).replace("|", "&#10;"));
    }

    public static String getDisconnectLinkHTML(
            String disconnectProgram, String disconnectFunction,
            String fromId, String toId, boolean escapeToId, String relationships,
            String imageUrl, String title,
            String onDisconnected) {

        String linkId = UUID.randomUUID().toString();
        String spinnerId = UUID.randomUUID().toString();

        return String.format(
                "<img id=\"%s\" src=\"%sspinner_16x16.png\" style=\"display:none;\" />",
                HtmlEscapers.htmlEscaper().escape(spinnerId), COMMON_IMAGES) +
                String.format(
                        "<a id=\"%s\" href=\"javascript:IMS_KDD_disconnect('%s', '%s', '%s', %s, '%s', '%s', '%s', %s)\"><img src=\"%s\" title=\"%s\" /></a>",
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
    }

    public static String getDisconnectLinkHTML(
            String disconnectProgram, String disconnectFunction,
            String fromId, String toId, String relationships, String title,
            String onDisconnected) {

        return getDisconnectLinkHTML(disconnectProgram, disconnectFunction, fromId, toId, true, relationships, FUGUE_16x16 + "cross.png", title, onDisconnected);
    }

    public static String getDisconnectLinkHTML(
            String disconnectProgram, String disconnectFunction,
            String fromId, String toId, String relationships, String imageUrl, String title,
            String onDisconnected) {

        return getDisconnectLinkHTML(disconnectProgram, disconnectFunction, fromId, toId, true, relationships, imageUrl, title, onDisconnected);
    }

    public static String getImageURL(Map map, String otherPersonName) {
        String type = getTypeFromMap(map);
        String name = getNameFromMap(map);
        if (type != null) {
            switch (type) {
                case Types.Company:
                    return FUGUE_16x16 + "building.png";
                case Types.Department:
                    return COMMON_IMAGES + "iconSmallDeparment.gif";
                case Types.Person:
                    boolean isFemale = "Female".equals(map.get(DomainObject.getAttributeSelect(Attributes.IMS_Gender)));
                    return FUGUE_16x16 + (name.equals(otherPersonName) ? "user-silhouette-question.png" : isFemale ? "user-female.png" : "user.png");
                case Types.IMS_Task:
                    return COMMON_IMAGES + "iconSmallTaskGray.gif";
                case Types.IMS_DocumentSet:
                    return COMMON_IMAGES + "iconSmallAttachmentFolder.gif";
            }
        }
        return null;
    }

    public static String getPersonName(Context context, DomainObject personObject) throws FrameworkException {
        String lastName = personObject.getAttributeValue(context, Attributes.Last_Name);
        String firstName = personObject.getAttributeValue(context, Attributes.First_Name);
        StringBuilder sb = new StringBuilder();
        if (!StringUtils.isBlank(lastName)) {
            sb.append(lastName);
        }
        if (!StringUtils.isBlank(firstName)) {
            if (sb.length() > 0) {
                sb.append(" ");
            }
            sb.append(firstName);
        }
        return sb.toString();
    }

    public static String getName(Context context, Map objectMap) throws Exception {
        String type = getTypeFromMap(objectMap);
        if (type != null) {
            switch (getTypeFromMap(objectMap)) {
                case Types.Person:
                    DomainObject personObject = mapToObject(context, objectMap);
                    return getPersonName(context, personObject);
            }
        }
        return getNameFromMap(objectMap);
    }

    public static String getLinkHTML(
            Context context, Map objectMap, String source, String name, String imageURL, String fontSize, String title,
            NameResolver nameResolver,
            boolean enableDragNDrop, boolean dragCheckedMode, String dragCheckedParentId, boolean isIndentedTable,
            String otherPersonName, boolean forceNameAlert,
            String dragSource)
            throws Exception {

        StringBuilder sb = new StringBuilder();
        String id = getIdFromMap(objectMap);

        if (name == null) {
            if (nameResolver != null) {
                name = nameResolver.findName(context, objectMap);
            }
            if (name == null) {
                name = getName(context, objectMap);
            }
        }
        if (name == null) {
            name = "";
        }

        String mapName = getNameFromMap(objectMap);

        sb.append(String.format(
                "<div style=\"display:none;\">%1$s</div>",
                HtmlEscapers.htmlEscaper().escape(mapName != null && mapName.equals(otherPersonName) ? "_" + name : name)));

        StringBuilder onDragStart = new StringBuilder();
        if (enableDragNDrop) {
            onDragStart.append(" ondragstart=\"");
            if (dragCheckedMode) {
                onDragStart.append(String.format("IMS_DragNDrop_onDragStart(event, '%s', '%s', %s);", id, dragCheckedParentId, isIndentedTable));
            }
            else {
                onDragStart.append(String.format("event.dataTransfer.setData('objectId', '%s');", id));
            }

            if (source != null) {
                onDragStart.append(String.format("event.dataTransfer.setData('source', '%s');", HtmlEscapers.htmlEscaper().escape(source)));
            }
            if (!StringUtils.isBlank(dragSource)) {
                onDragStart.append(String.format("event.dataTransfer.setData('dragSource', '%s');", HtmlEscapers.htmlEscaper().escape(dragSource)));
            }

            onDragStart.append("\"");
        }

        if (imageURL == null) {
            imageURL = getImageURL(objectMap, otherPersonName);
        }
        if (imageURL != null) {
            sb.append(String.format(
                    "<img src='%s'%s/>",
                    HtmlEscapers.htmlEscaper().escape(imageURL), onDragStart));
        }

        String style = fontSize != null ? String.format(" style=\"font-size: %s\"", fontSize): "";
        String titleHTML = title != null ? String.format(" title=\"%s\"", HtmlEscapers.htmlEscaper().escape(title.replace("&#10;", "|")).replace("|", "&#10;")) : "";

        sb.append(forceNameAlert || objectMap == null || mapName != null && mapName.equals(otherPersonName) ?
                String.format(
                        "<a href=\"javascript:alert('%1$s')\"%2$s%3$s%4$s>%1$s</a>",
                        HtmlEscapers.htmlEscaper().escape(name), onDragStart, style, titleHTML) :
                String.format(
                        "<a href=\"javascript:emxTableColumnLinkClick('../common/emxTree.jsp?objectId=%2$s')\"%3$s%4$s%5$s>%1$s</a>",
                        HtmlEscapers.htmlEscaper().escape(name), id, onDragStart, style, titleHTML));

        return sb.toString();
    }

    public static String getLinkHTML(
            Context context, Map objectMap, String source, String name, String imageURL, String fontSize, String title,
            NameResolver nameResolver,
            boolean enableDragNDrop, boolean dragCheckedMode, String dragCheckedParentId, boolean isIndentedTable,
            String otherPersonName, boolean forceNameAlert)
            throws Exception {

        return getLinkHTML(
                context, objectMap, source, name, imageURL, fontSize, title,
                nameResolver,
                enableDragNDrop, dragCheckedMode, dragCheckedParentId, isIndentedTable,
                otherPersonName, forceNameAlert,
                null);
    }

    public static String getRowId(Map map) {
        return (String) map.get(ROW_ID_KEY);
    }

    public static List<Map> extractRelatedMaps(Map map, String prefix) {
        int relatedCount = 0;
        Map<String, List<String>> prefixedValuesByTrimmedKey = new HashMap<>();

        for (Object entryObject : map.entrySet()) {
            Map.Entry<String, String> entry = (Map.Entry<String, String>) entryObject;
            if (entry.getKey().startsWith(prefix)) {
                List<String> parts = split(entry.getValue());
                relatedCount = parts.size();
                prefixedValuesByTrimmedKey.put(entry.getKey().substring(prefix.length()), parts);
            }
        }

        List<Map> relatedMaps = new ArrayList<>();
        for (int i = 0; i < relatedCount; i++) {
            Map<String, String> relatedMap = new HashMap<>();
            for (Map.Entry<String, List<String>> entry : prefixedValuesByTrimmedKey.entrySet()) {
                relatedMap.put(entry.getKey(), entry.getValue().get(i));
            }
            relatedMaps.add(relatedMap);
        }
        return relatedMaps;
    }

    public static List<Map> extractRelatedMaps(Map map, String relationship, boolean from) {
        String prefix = from ? getToSelectPrefix(relationship) : getFromSelectPrefix(relationship);
        return extractRelatedMaps(map, prefix);
    }

    public String toggleConnection(Context context, String fromId, String toName, String toType, String relationship) throws Exception {
        DomainObject fromObject = DomainObject.newInstance(context, fromId);
        Map connectedMap = null;
        for (Map map : getRelatedObjectMaps(context, fromObject, relationship, true)) {
            if (getNameFromMap(map).equals(toName)) {
                connectedMap = map;
                break;
            }
        }
        if (connectedMap != null) {
            fromObject.disconnect(context, new RelationshipType(relationship), true, idToObject(context, getIdFromMap(connectedMap)));
        }
        else {
            DomainObject toObject = DomainObject.newInstance(context, new BusinessObject(toType, toName, "", Vaults.eService_Production));
            if (toObject.exists(context)) {
                fromObject.connect(context, new RelationshipType(relationship), true, toObject);
            }
        }
        return "";
    }

    @SuppressWarnings("unused")
    public String toggleConnection(Context context, String[] args) throws Exception {
        return toggleConnection(context, args[0], args[1], args[2], args[3]);
    }

    private static DomainObject getPersonObject(Context context, String name) throws MatrixException {
        return "creator".equals(name) ?
                null :
                new DomainObject(new BusinessObject(Types.Person, name, "-", Vaults.eService_Production));
    }

    public static DomainObject findExistingPersonObject(Context context, String name) {
        try {
            DomainObject personObject = getPersonObject(context, name);
            if (personObject != null && personObject.exists(context)) {
                personObject.open(context);
                return personObject;
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static DomainObject getCompanyObject(Context context, String name) throws MatrixException {
        return DomainObject.newInstance(context, new BusinessObject(Types.Company, name, "-", Vaults.eService_Production));
    }

    public static boolean isConnected(Context context, String relationship, DomainObject fromObject, DomainObject toObject) throws FrameworkException {
        return fromObject.getRelatedObjects(
                context, relationship, toObject.getType(context),
                null, null, false, true, (short) 1, "id==" + toObject.getId(context), null)
                .size() > 0;
    }

    public static final String BELL = "\u0007";

    public static List<String> split(Object obj) {
        if (obj instanceof StringList) {
            return ((StringList) obj).toList();
        }
        if (obj instanceof String) {
            String s = (String) obj;
            return s.contains(BELL) ? Arrays.asList(s.split(BELL, -1)) : Collections.singletonList(s);
        }
        return new ArrayList<>();
    }

    public static long getElapsedMilliseconds(long startTime) {
        return (System.nanoTime() - startTime) / 1000000;
    }

    public static String getFromConnectionIdSelect(String relationship) {
        return String.format("from[%s].id", relationship);
    }

    public static String getToSelectPrefix(String relationship) {
        return String.format("from[%s].to.", relationship);
    }

    public static String getToIdSelect(String relationship) {
        return getToSelectPrefix(relationship) + DomainConstants.SELECT_ID;
    }

    public static String getToNameSelect(String relationship) {
        return getToSelectPrefix(relationship) + DomainConstants.SELECT_NAME;
    }

    public static String getToTypeSelect(String relationship) {
        return getToSelectPrefix(relationship) + DomainConstants.SELECT_TYPE;
    }

    public static String getToDescriptionSelect(String relationship) {
        return getToSelectPrefix(relationship) + DomainConstants.SELECT_DESCRIPTION;
    }

    public static String getToOwnerSelect(String relationship) {
        return getToSelectPrefix(relationship) + DomainConstants.SELECT_OWNER;
    }

    public static String getToOriginatedSelect(String relationship) {
        return getToSelectPrefix(relationship) + DomainConstants.SELECT_ORIGINATED;
    }

    public static String getToModifiedSelect(String relationship) {
        return getToSelectPrefix(relationship) + DomainConstants.SELECT_MODIFIED;
    }

    public static String getToAttributeSelect(String relationship, String attributeName) {
        return getToSelectPrefix(relationship) + DomainObject.getAttributeSelect(attributeName);
    }

    public static String getToConnectionIdSelect(String relationship) {
        return String.format("to[%s].id", relationship);
    }

    public static String getFromSelectPrefix(String relationship) {
        return String.format("to[%s].from.", relationship);
    }

    public static String getFromIdSelect(String relationship) {
        return getFromSelectPrefix(relationship) + "id";
    }

    public static String getFromNameSelect(String relationship) {
        return getFromSelectPrefix(relationship) + "name";
    }

    public static String getFromTypeSelect(String relationship) {
        return getFromSelectPrefix(relationship) + "type";
    }

    public static String getFromConnectionOwnerSelect(String relationship) {
        return String.format("from[%s].owner", relationship);
    }

    public static String getFromConnectionOriginatedSelect(String relationship) {
        return String.format("from[%s].originated", relationship);
    }

    public static String getFromConnectionAttributeSelect(String relationship, String attribute) {
        return String.format("from[%s].%s", relationship, DomainObject.getAttributeSelect(attribute));
    }

    public static String smartTrim(String value) {
        if (value != null) {
            value = value.trim().replace(" / ", "/");
            while (value.contains("  ")) {
                value = value.replace("  ", " ");
            }
        }
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class ConnectResult {

        private DomainRelationship connection;

        private boolean connected;

        public DomainRelationship getConnection() {
            return connection;
        }

        public boolean isConnected() {
            return connected;
        }

        public ConnectResult(DomainRelationship connection, boolean connected) {
            this.connection = connection;
            this.connected = connected;
        }
    }

    public static String findConnectionId(
            Context context, String relationship, String fromObjectId, String toObjectId) throws Exception {

//        String result = MqlUtil.mqlCommand(
//                context,
//                "query connection relationship $1 where $2 select $3 dump $4;",
//                relationship,
//                String.format("from.id==\"%s\" && to.id==\"%s\"", fromObjectId, toObjectId),
//                "id", "|");
//
//        if (!StringUtils.isBlank(result)) {
//            return result.split("\\|")[1];
//        }
//        return null;
        String result = MqlUtil.mqlCommand(
                context,
                String.format(
                        "print bus %s select from[%s|to.id==%s].id dump |;",
                        fromObjectId, relationship, toObjectId));

        return !StringUtils.isBlank(result) ? result : null;
    }

    public static String findConnectionId(
            Context context, String relationship, DomainObject fromObject, DomainObject toObject) throws Exception {

        return findConnectionId(context, relationship, fromObject.getId(context), toObject.getId(context));
    }

    public static DomainRelationship findConnection(
            Context context, String relationship, DomainObject fromObject, DomainObject toObject) throws Exception {

        String id = findConnectionId(context, relationship, fromObject.getId(context), toObject.getId(context));
        return id != null ? DomainRelationship.newInstance(context, id) : null;
    }

    public static String findRelToTypeConnectionId(
            Context context, String relationship, String fromConnectionId, DomainObject toObject) throws Exception {

//        String result = MqlUtil.mqlCommand(
//                context,
//                "query connection relationship $1 where $2 select $3 dump $4;",
//                relationship,
//                String.format("fromrel.id==\"%s\" && to.id==\"%s\"", fromConnectionId, toObject.getId(context)),
//                "id", "|");
//
//        if (!StringUtils.isBlank(result)) {
//            return result.split("\\|")[1];
//        }
//        return null;

        String result = MqlUtil.mqlCommand(
                context,
                String.format(
                        "print connection %s select frommid[%s|to.id==%s].id dump |;",
                        fromConnectionId, relationship, toObject.getId(context)));

        return !StringUtils.isBlank(result) ? result : null;
    }

    public static List<String> getRelToTypeConnectionIds(
            Context context, String relationship, String fromConnectionId) throws Exception {

        String result = MqlUtil.mqlCommand(
                context,
                String.format(
                        "print connection %s select frommid[%s].id dump |;",
                        fromConnectionId, relationship));

        List<String> ids = new ArrayList<>();
        if (StringUtils.isNotBlank(result)) {
            ids.addAll(Arrays.asList(result.split("\\|")));
        }
        return ids;
    }

    public static String findTypeToRelConnectionId(
            Context context, String relationship, DomainObject fromObject, String toConnectionId) throws Exception {

//        String result = MqlUtil.mqlCommand(
//                context,
//                "query connection relationship $1 where $2 select $3 dump $4;",
//                relationship,
//                String.format("from.id==\"%s\" && torel.id==\"%s\"", fromObjectId.getId(context), toConnectionId),
//                "id", "|");
//
//        if (!StringUtils.isBlank(result)) {
//            return result.split("\\|")[1];
//        }
//        return null;

        String result = MqlUtil.mqlCommand(
                context,
                String.format(
                        "print connection %s select tomid[%s|from.id==%s].id dump |;",
                        toConnectionId, relationship, fromObject.getId(context)));

        return !StringUtils.isBlank(result) ? result : null;
    }

    public static ConnectionInfo connectAndSetConnectionOwner(
            Context context, String relationship, DomainObject fromObject, DomainObject toObject,
            String connectionOwner)
            throws Exception {

        DomainRelationship connection = new DomainRelationship(
                fromObject.connect(context, new RelationshipType(relationship), true, toObject));

        String connectionId = getConnectionId(context, connection);

        if (!StringUtils.isBlank(connectionOwner)) {
            MqlUtil.mqlCommand(context, "modify connection $1 owner \"$2\"", connectionId, connectionOwner);
        }

        return new ConnectionInfo(
                connectionId, relationship, fromObject.getId(context), toObject.getId(context),
                !StringUtils.isBlank(connectionOwner) ? connectionOwner : context.getUser(),
                ""); // TODO originated
    }

    public static ConnectionInfo connectConnectionToObjectAndSetConnectionOwner(
            Context context, String relationship, String fromConnectionId, String toObjectId,
            String connectionOwner)
            throws Exception {

        if (!StringUtils.isBlank(connectionOwner)) {
            String[] results = MqlUtil.mqlCommand(context, "add connection $1 fromrel $2 to $3 owner \"$4\" select $5 $6 $7 dump $8;",
                    relationship, fromConnectionId, toObjectId, connectionOwner, "id", "owner", "originated", "|")
                    .split("\\|");

            return new ConnectionInfo(results[0], relationship, fromConnectionId, toObjectId, results[1], results[2]);
        }

        String[] results = MqlUtil.mqlCommand(context, "add connection $1 fromrel $2 to $3 select $4 $5 $6 dump $7",
                relationship, fromConnectionId, toObjectId, "id", "owner", "originated", "|")
                .split("\\|");

        return new ConnectionInfo(results[0], relationship, fromConnectionId, toObjectId, results[1], results[2]);
    }

    public static ConnectionInfo connectObjectToConnectionAndSetConnectionOwner(
            Context context, String relationship, String fromObjectId, String toConnectionId,
            String connectionOwner)
            throws Exception {

        if (!StringUtils.isBlank(connectionOwner)) {
            String[] results = MqlUtil.mqlCommand(context, "add connection $1 from $2 torel $3 owner \"$4\" select $5 $6 $7 dump $8;",
                    relationship, fromObjectId, toConnectionId, connectionOwner, "id", "owner", "originated", "|")
                    .split("\\|");

            return new ConnectionInfo(results[0], relationship, fromObjectId, toConnectionId, results[1], results[2]);
        }

        String[] results = MqlUtil.mqlCommand(context, "add connection $1 from $2 torel $3 select $4 $5 $6 dump $7",
                relationship, fromObjectId, toConnectionId, "id", "owner", "originated", "|")
                .split("\\|");

        return new ConnectionInfo(results[0], relationship, fromObjectId, toConnectionId, results[1], results[2]);
    }

    public static ConnectResult connectObjectsIfNotConnected(
            Context context, String relationship, DomainObject fromObject, DomainObject toObject) throws Exception {

        String connectionId = findConnectionId(context, relationship, fromObject, toObject);
        if (!StringUtils.isBlank(connectionId)) {
            return new ConnectResult(DomainRelationship.newInstance(context, connectionId), false);
        }
        return new ConnectResult(new DomainRelationship(fromObject.connect(context, new RelationshipType(relationship), true, toObject)), true);
    }

    public static DomainRelationship connectIfNotConnected(
            Context context, String relationship, DomainObject fromObject, DomainObject toObject) throws Exception {

        ConnectResult result = connectObjectsIfNotConnected(context, relationship, fromObject, toObject);
        return result.getConnection();
    }

    public static String getConnectionId(Context context, DomainRelationship connection) throws Exception {
        return (String) ((StringList) connection.getRelationshipData(context, new StringList(new String[]{DomainConstants.SELECT_RELATIONSHIP_ID}))
                .get(DomainConstants.SELECT_RELATIONSHIP_ID))
                .get(0);
    }

    public static ConnectResult connectConnectionToObjectIfNotConnected(
            Context context, String relationship, DomainRelationship fromConnection, DomainObject toObject) throws Exception {

        String fromConnectionId = getConnectionId(context, fromConnection);
        String connectionId = findRelToTypeConnectionId(context, relationship, fromConnectionId, toObject);
        if (!StringUtils.isBlank(connectionId)) {
            return new ConnectResult(DomainRelationship.newInstance(context, connectionId), false);
        }

        ConnectionInfo connectionInfo = connectConnectionToObjectAndSetConnectionOwner(
                context, relationship, fromConnectionId, toObject.getId(context), null);

        return new ConnectResult(DomainRelationship.newInstance(context, connectionInfo.Id), true);
    }

    public static DomainRelationship connectIfNotConnected(
            Context context, String relationship, DomainRelationship fromConnection, DomainObject toObject) throws Exception {

        ConnectResult result = connectConnectionToObjectIfNotConnected(context, relationship, fromConnection, toObject);
        return result.getConnection();
    }

    public static ConnectResult connectObjectToConnectionIfNotConnected(
            Context context, String relationship, DomainObject fromObject, DomainRelationship toConnection) throws Exception {

        String toConnectionId = getConnectionId(context, toConnection);
        String connectionId = findTypeToRelConnectionId(context, relationship, fromObject, toConnectionId);
        if (!StringUtils.isBlank(connectionId)) {
            return new ConnectResult(DomainRelationship.newInstance(context, connectionId), false);
        }

        ConnectionInfo connectionInfo = connectObjectToConnectionAndSetConnectionOwner(
                context, relationship, fromObject.getId(context), toConnectionId, null);

        return new ConnectResult(DomainRelationship.newInstance(context, connectionInfo.Id), true);
    }

    public static DomainRelationship connectIfNotConnected(
            Context context, String relationship, DomainObject fromObject, DomainRelationship toConnection) throws Exception {

        ConnectResult result = connectObjectToConnectionIfNotConnected(context, relationship, fromObject, toConnection);
        return result.getConnection();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getCellStringValue(Row row, int colNum) {
        if (colNum >= row.getFirstCellNum() && colNum < row.getLastCellNum()) {
            Cell cell = row.getCell(colNum);
            if (cell != null) {

                if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC ||
                        cell.getCellType() == Cell.CELL_TYPE_FORMULA && cell.getCachedFormulaResultType() == Cell.CELL_TYPE_NUMERIC) {

                    //return String.valueOf(cell.getNumericCellValue());
                    DecimalFormat decimalFormat = new DecimalFormat("0.#");
                    return decimalFormat.format(cell.getNumericCellValue());
                }
                else {
                    try {
                        return cell.getStringCellValue();
                    }
                    catch (Exception ignored) {
                        return "";
                    }
                }
            }
        }
        return null;
    }

    public static boolean isBlankRow(Row row) {
        if (row != null) {
            for (int i = row.getFirstCellNum(); i <= row.getLastCellNum(); i++) {
                if (!StringUtils.isBlank(getCellStringValue(row, i))) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean isTestMode(String[] args) {
        return args != null && args.length > 0 && "test".equalsIgnoreCase(args[args.length - 1]);
    }

    public static void runInTransaction(Context context, boolean testMode, Action action) throws Exception {
        if (testMode) {
            System.out.println("TEST MODE");
        }
        context.start(true);
        try {
            action.run();
        }
        catch (Exception e) {
            e.printStackTrace();
            System.out.println("ABORT");
            try {
                context.abort();
            }
            catch (Exception abortE) {
                abortE.printStackTrace();
            }
            throw e;
        }
        finally {
            if (context.isTransactionActive()) {
                if (!testMode) {
                    System.out.println("COMMIT");
                    context.commit();
                }
                else {
                    System.out.println("ABORT");
                    try {
                        context.abort();
                    }
                    catch (Exception abortE) {
                        abortE.printStackTrace();
                    }
                }
            }
        }
    }

    public static void runInTransaction(Context context, Action action) throws Exception {
        runInTransaction(context, false, action);
    }

    public static Double parseDouble(String s) {
        Double sortNumber = 0d;
        try {
            sortNumber = Double.parseDouble(s);
        }
        catch (Exception ignored) {
        }
        return sortNumber;
    }

    public static List<Map> sortMapsByName(List<Map> maps) {
        if (maps != null) {
            Collections.sort(maps, new Comparator<Map>() {
                @Override
                public int compare(Map map1, Map map2) {
                    String name1 = getNameFromMap(map1);
                    String name2 = getNameFromMap(map2);
                    if (name1 == null) name1 = "";
                    if (name2 == null) name2 = "";
                    return name1.compareTo(name2);
                }
            });
        }
        return maps;
    }

    public static List<Map> mapListToList(MapList mapList) {
        List<Map> maps = new ArrayList<>();
        for (Object item : mapList) {
            maps.add((Map) item);
        }
        return maps;
    }

    public static String getNonBlankArg(String[] args, int index, String argDescription) {
        if (args != null && args.length > index && !org.apache.commons.lang.StringUtils.isBlank(args[index])) {
            return args[index];
        }
        throw new IllegalArgumentException(String.format("Argument %s (%s) must be specified.", index + 1, argDescription));
    }

    public static boolean isInitialGetColumnValuesCall(String[] args) throws Exception {
        return !getParamMap(args).containsKey("firstTime");
    }

    public static DomainObject getObject(Context context, String type, String name, String revision) throws MatrixException {
        return DomainObject.newInstance(context, new BusinessObject(type, name, revision, Vaults.eService_Production));
    }

    public static String getCheckBoxHTML(String column, String rowId, String id) {
        return String.format(
                "<input type=\"checkbox\" id=\"checkbox-%s-%s-%s\" class=\"IMS_KDD_CB\" onclick=\"IMS_KDD_handleCheckboxClick(this);\" />",
                column, rowId, id);
    }

    private static List<Map> findConnections(String result, String fromSelect, String toSelect) {
        List<Map> maps = new ArrayList<>();
        for (String line : result.split("\n")) {
            if (!StringUtils.isBlank(line)) {
                Map map = new HashMap();
                String[] parts = line.split("\\|");
                map.put("id", parts[1]);
                map.put(fromSelect, parts[2]);
                map.put(toSelect, parts[3]);
                map.put("owner", parts[4]);
                map.put("originated", parts[5]);
                maps.add(map);
            }
        }
        return maps;
    }

    private static List<Map> findTypeToTypeConnections(String result) {
        return findConnections(result, "from.id", "to.id");
    }

    private static List<Map> findRelToTypeConnections(String result) {
        return findConnections(result, "fromrel.id", "to.id");
    }

    public static List<Map> findTypeToTypeConnections(Context context, String typeList, String where) throws FrameworkException {
        // TODO get rid of 'where...'
        return findTypeToTypeConnections(MqlUtil.mqlCommand(
                context,
                "query connection type $1 where $8 select $2 $3 $4 $5 $6 dump $7;",
                typeList, "id", "from.id", "to.id", "owner", "originated", "|", where));
    }

    public static List<Map> findTypeToTypeConnections(Context context, String typeList) throws FrameworkException {
        return findTypeToTypeConnections(MqlUtil.mqlCommand(
                context,
                "query connection type $1 select $2 $3 $4 $5 $6 dump $7;",
                typeList, "id", "from.id", "to.id", "owner", "originated", "|"));
    }

    public static List<Map> findRelToTypeConnections(Context context, String typeList) throws FrameworkException {
        return findRelToTypeConnections(MqlUtil.mqlCommand(
                context,
                "query connection type $1 select $2 $3 $4 $5 $6 dump $7;",
                typeList, "id", "fromrel.id", "to.id", "owner", "originated", "|"));
    }

    public static List<String> getConnectionBusinessInterfaceNames(Context context, String connectionId) throws FrameworkException {

        String result = MqlUtil.mqlCommand(
                context,
                "print connection $1 select $2 dump $3;",
                connectionId, "interface", "|");

        List<String> names = new ArrayList<>();
        for (String part : result.split("\\|")) {
            if (!StringUtils.isBlank(part) && !names.contains(part)) {
                names.add(part);
            }
        }
        return names;
    }

    public static void runAsSuperUser(Context context, Action action) throws Exception {
        ContextUtil.pushContext(context);
        try {
            action.run();
        }
        finally {
            ContextUtil.popContext(context);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String ENOVIA_DATE_FORMAT = "MM/dd/yyyy hh:mm:ss aaa";
    public static final String DISPLAY_DATE_FORMAT = "dd.MM.yyyy HH:mm:ss";
    public static final String SORTABLE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    public static final String FILE_DATE_FORMAT = "yyyy-MM-dd_HH-mm-ss-SSS";

    public static Date parseEnoviaDate(String enoviaDate) {
        if (!StringUtils.isBlank(enoviaDate)) {
            try {
                return new SimpleDateFormat(ENOVIA_DATE_FORMAT).parse(enoviaDate);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public static String toDisplayDate(String enoviaDate) {
        if (StringUtils.isBlank(enoviaDate)) {
            return "";
        }
        try {
            final DateFormat dateFormat = new SimpleDateFormat(ENOVIA_DATE_FORMAT);
            final DateFormat displayDateFormat = new SimpleDateFormat(DISPLAY_DATE_FORMAT);
            return displayDateFormat.format(dateFormat.parse(enoviaDate));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return enoviaDate;
    }

    public static int compareEnoviaDates(String enoviaDate1, String enoviaDate2) {
        Date date1 = parseEnoviaDate(enoviaDate1);
        if (date1 == null) {
            return -1;
        }
        Date date2 = parseEnoviaDate(enoviaDate2);
        if (date2 == null) {
            return 1;
        }
        return date1.compareTo(date2);
    }

    public static String toEnoviaDate(Date date) {
        return new SimpleDateFormat(ENOVIA_DATE_FORMAT).format(date);
    }

    public static String formatAsSortableDate(Date date) {
        return new SimpleDateFormat(SORTABLE_DATE_FORMAT).format(date);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class LogInfo {
        public final long StartTime;
        public final String MethodName;
        public final String User;

        public LogInfo(Context context, String methodName) {
            StartTime = System.nanoTime();
            MethodName = methodName;
            User = context.getUser();
        }
    }

    public static LogInfo logMethodStart(SimpleLogger logger, Context context, String name) {
        LogInfo info = new LogInfo(context, name);
        logger.log(String.format("--> %s (%s)", info.MethodName, info.User));
        return info;
    }

    public static void logMethodFinish(SimpleLogger logger, LogInfo info) {
        logger.log(String.format("<-- %s (%s) : %s ms", info.MethodName, info.User, getElapsedMilliseconds(info.StartTime)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class Task<T> {

        private final Thread thread;
        private T result;

        public T getResult() {
            return result;
        }

        public Task(final Func<T> func, boolean start) {
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        result = func.run();
                    }
                    catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });

            if (start) {
                thread.start();
            }
        }

        public void start() {
            thread.start();
        }

        public void waitFor() {
            try {
                thread.join();
            }
            catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static class MapsLoader {
        private final Context context;
        private final SimpleLogger logger;
        private final int maxThreads;
        private final int maxIdsPerQuery;

        public MapsLoader(Context context, SimpleLogger logger, int maxThreads, int maxIdsPerQuery) {
            this.context = context;
            this.logger = logger;
            this.maxThreads = maxThreads;
            this.maxIdsPerQuery = maxIdsPerQuery;
        }

        private List<Map> loadMaps(final String type, final String where, final StringList selects, final List<String> ids)
                throws Exception {

            //logger.log("Thread IDs count: " + ids.size());
            Context newContext = context.getFrameContext(UUID.randomUUID().toString());
            try {
                String idsWhere = String.format("id matchlist '%s' ','", StringUtils.join(ids, ","));

                List<Map> maps = DomainObject.findObjects(
                        newContext, type, "*", "*", "*", newContext.getVault().getName(),
                        !StringUtils.isBlank(where) ? String.format("(%s) && (%s)", where, idsWhere) : idsWhere,
                        true, selects);

                //logger.log("Thread maps found: " + maps.size());
                return maps;
            }
            finally {
                newContext.shutdown();
            }
        }

        public List<Map> loadMaps(final String type, final String where, final StringList busSelects) throws Exception {
            LogInfo logInfo = logMethodStart(logger, context, "loadMaps");
            final List<Map> maps = new ArrayList<>();
            try {
                MapList idMapList = DomainObject.findObjects(
                        context, type, "*", "*", "*", context.getVault().getName(),
                        where,
                        true, new StringList(DomainConstants.SELECT_ID));

                logger.log("Number of IDs: " + idMapList.size());

                final int maxIdsPerThread = Math.max(
                        1,
                        Math.max(
                                maxIdsPerQuery,
                                idMapList.size() / (maxThreads < 2 || idMapList.size() % maxThreads == 0 ? maxThreads : maxThreads - 1)));

                logger.log("IDs per thread: " + maxIdsPerThread);
                int idIndex = 0;
                List<Thread> threads = new ArrayList<>();

                while (idIndex < idMapList.size()) {
                    final List<String> threadIds = new ArrayList<>(maxIdsPerThread);

                    while (idIndex < idMapList.size() && threadIds.size() < maxIdsPerThread) {
                        threadIds.add(getIdFromMap(idMapList.get(idIndex)));
                        idIndex++;
                    }

                    if (threadIds.size() > 0) {

                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                int threadIdIndex = 0;
                                while (threadIdIndex < threadIds.size()) {
                                    List<String> queryIds = new ArrayList<>(maxIdsPerQuery);

                                    while (threadIdIndex < threadIds.size() && queryIds.size() < maxIdsPerQuery) {
                                        queryIds.add(threadIds.get(threadIdIndex));
                                        threadIdIndex++;
                                    }

                                    if (queryIds.size() > 0) {
                                        try {
                                            List<Map> queryMaps = loadMaps(type, where, busSelects, queryIds);
                                            synchronized (maps) {
                                                maps.addAll(queryMaps);
                                            }
                                        }
                                        catch (Exception e) {
                                            throw new RuntimeException(e);
                                        }
                                    }
                                }
                            }
                        });

                        threads.add(thread);
                    }
                }
                logger.log("Number of threads: " + threads.size());

                for (Thread thread : threads) {
                    thread.start();
                }
                for (Thread thread : threads) {
                    thread.join();
                }
            }
            finally {
                logMethodFinish(logger, logInfo);
            }
            logger.log("Maps found: " + maps.size());
            return maps;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static <T extends Comparable<? super T>> List<T> sort(List<T> list) {
        List<T> newList = new ArrayList<>(list);
        Collections.sort(newList);
        return newList;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static boolean matches(String value, String pattern) {
        if (value == null) {
            value = "";
        }
        if (pattern == null)
        {
            pattern = "";
        }
        pattern = pattern.trim();
        if (pattern.length() > 2 && pattern.startsWith("\"") && pattern.endsWith("\"")) {
            return value.equals(pattern.substring(1, pattern.length() - 1));
        }
        return value.toLowerCase().contains(pattern.toLowerCase());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getRangeString(Context context, String attributeName, String item, String language) throws FrameworkException {
        return StringUtils.isNotBlank(item) ?
                getString(context, String.format("emxFramework.Range.%s.%s", attributeName, item), language) :
                "";
    }

    public static String getRangeString(Context context, String attributeName, String item) throws FrameworkException {
        return getRangeString(context, attributeName, item, context.getSession().getLanguage());
    }

    public static String getAttributeString(Context context, String attributeName, String language) throws FrameworkException {
        return getString(context, String.format("emxFramework.Attribute.%s", attributeName), language);
    }

    public static String getAttributeString(Context context, String attributeName) throws FrameworkException {
        return getAttributeString(context, attributeName, context.getSession().getLanguage());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static Map getMap(Context context, String id, StringList selects) throws FrameworkException {
        MapList mapList = DomainObject.findObjects(context, "*", "*", "id==" + id, selects);
        return (Map) mapList.get(0);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static final String MAIL_SMTP_HOST = "mail.smtp.host";
    private static final String MAIL_SMTP_PORT = "mail.smtp.port";
    private static final String MAIL_SMTP_AUTH = "mail.smtp.auth";
    private static final String MAIL_SMTP_STARTTLS_ENABLE = "mail.smtp.starttls.enable";

    public static void sendLetter(
            Context context,
            String smtpHost, String smtpPort,
            String from, List<String> recipients,
            String subject, String body, boolean isHtml, String attachmentFilePath) throws Exception {

        Properties properties = new Properties();
        properties.put(MAIL_SMTP_HOST, smtpHost);
        properties.put(MAIL_SMTP_PORT, smtpPort);
        properties.put(MAIL_SMTP_AUTH, "false");
        properties.put(MAIL_SMTP_STARTTLS_ENABLE, "false");

        Set<String> addressSet = new HashSet<String>(recipients);
        Session session = Session.getInstance(properties);
        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(from));
        message.setSubject(subject);
        InternetAddress[] recipientAddress = new InternetAddress[addressSet.size()];
        int i = 0;
        for (Object address : addressSet) {
            recipientAddress[i] = new InternetAddress((String) address);
            i++;
        }
        message.setRecipients(Message.RecipientType.TO, recipientAddress);
        message.setSentDate(new Date());

        MimeMultipart multipart = new MimeMultipart();
        BodyPart messageBodyPart = new MimeBodyPart();

        messageBodyPart.setContent(body, "text/" + (isHtml ? "html" : "plain") + "; charset=UTF-8");
        multipart.addBodyPart(messageBodyPart);

        if (StringUtils.isNotBlank(attachmentFilePath)) {
            BodyPart attachment = new MimeBodyPart();
            attachment.setDataHandler(new DataHandler(new FileDataSource(attachmentFilePath)));
            attachment.setFileName(new File(attachmentFilePath).getName());
            multipart.addBodyPart(attachment);
        }

        message.setContent(multipart);
        Transport.send(message);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static <T> Marshaller createMarshaller(T object) throws JAXBException {
        JAXBContext jc = JAXBContext.newInstance(object.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        return marshaller;
    }

    public static <T> void saveToFile(T object, String filePath) throws Exception {
        Marshaller marshaller = createMarshaller(object);
        marshaller.marshal(object, new File(filePath));
    }

    public static <T> void saveToPage(Context context, T object, String name) throws Exception {
        Marshaller marshaller = createMarshaller(object);
        StringWriter stringWriter = new StringWriter();
        marshaller.marshal(object, stringWriter);

        Page page = new Page(name);
        page.open(context);
        page.setContents(context, stringWriter.toString());
        page.update(context);
    }

    private static <T> Unmarshaller createUnmarshaller(Class<T> type) throws JAXBException {
        return JAXBContext.newInstance(type).createUnmarshaller();
    }

    public static <T> T loadFromFile(Class<T> type, String filePath) throws Exception {
        return (T) createUnmarshaller(type).unmarshal(new File(filePath));
    }

    public static <T> T loadFromString(Class<T> type, String s) throws Exception {
        return (T) createUnmarshaller(type).unmarshal(new StringReader(s));
    }

    public static <T> T loadFromPage(Context context, Class<T> type, String name) throws Exception {
        Page page = new Page(name);
        if (page.exists(context)) {
            page.open(context);
            String contents = page.getContents(context);
            if (contents != null) {
                contents = contents.trim();
            }
            return loadFromString(type, contents);
        }
        throw new Exception(String.format("Page '%s' not found", name));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public interface Connector {

        String connect(
                Context context,
                String sourceId, String targetId, String relationship) throws Exception;
    }

    public interface Disconnector {

        String disconnect(
                Context context,
                String from, String to, String relationship) throws Exception;
    }

    public static String connect(
            Context context,
            String from, String to, String relationships,
            String connectId, String rowIdToRefresh,
            IMS_KDD_mxJPO.Connector connector,
            String toSplitRegex)
            throws Exception {

        boolean searchMode = false;

        if (from != null && to != null && relationships != null) {
            String[] fromItems = from.split(",");

            List<String> toItems;
            if (to.contains("|")) {
                searchMode = true;
                toItems = new ArrayList<>();
                for (String part : to.split(";")) {
                    toItems.add(part.split("\\|")[1]);
                }
            }
            else {
                toItems = toSplitRegex != null ? Arrays.asList(to.split(toSplitRegex)) : Collections.singletonList(to);
            }

            for (String relationship : relationships.split(",")) {
                if (!StringUtils.isBlank(relationship)) {
                    int specifierIndex = relationship.indexOf('\\');
                    if (specifierIndex > 0) {
                        relationship = relationship.substring(0, specifierIndex);
                    }
                    for (String fromItem : fromItems) {
                        if (!StringUtils.isBlank(fromItem)) {
                            for (String toItem : toItems) {
                                if (!org.apache.commons.lang.StringUtils.isBlank(toItem)) {

                                    String result = connector.connect(
                                            context,
                                            fromItem, toItem, relationship
                                    );

                                    if (!StringUtils.isEmpty(result)) {
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return searchMode ?
                String.format(
                        "<script>parent.IMS_KDD_closeSearchWindowAndRefreshRow('%s','%s');</script>",
                        StringEscapeUtils.escapeEcmaScript(connectId),
                        StringEscapeUtils.escapeEcmaScript(rowIdToRefresh)) :
                "";
    }

    private static String connectInTransaction(
            final Context context,
            final String from, final String to, final String relationships,
            final String connectId, final String rowIdToRefresh,
            final IMS_KDD_mxJPO.Connector connector,
            final String toSplitRegex)
            throws Exception {

        final String[] results = new String[1];

        runInTransaction(context, new Action() {
            @Override
            public void run() throws Exception {
                results[0] = connect(
                        context,
                        from, to, relationships,
                        connectId, rowIdToRefresh,
                        connector,
                        toSplitRegex);
            }
        });

        return results[0];
    }

    public static String connect(Context context, String[] args, String toSplitRegex, Connector connector) {
        try {
            return connectInTransaction(
                    context,
                    args[0], args[1], args[2],
                    args.length > 3 ? args[3] : null, args.length > 4 ? args[4] : null,
                    connector,
                    toSplitRegex);
        }
        catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    public static String connect(Context context, String[] args, Connector connector) {
        return connect(context, args, ",", connector);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    private static String disconnect(
            Context context, String from, String to, String relationships,
            Disconnector disconnector)
            throws Exception {

        if (from != null && to != null) {
            for (String fromItem : from.split(",")) {
                for (String toItem : to.split(",")) {
                    if (StringUtils.isNotBlank(fromItem) && StringUtils.isNotBlank(toItem)) {
                        fromItem = fromItem.trim();
                        toItem = toItem.trim();
                        if (relationships != null) {
                            for (String relationship : StringUtils.split(relationships, ',')) {
                                if (StringUtils.isNotBlank(relationship)) {
                                    relationship = relationship.trim();
                                    String result = disconnector.disconnect(context, fromItem, toItem, relationship);
                                    if (StringUtils.isNotEmpty(result)) {
                                        return result;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return "";
    }

    public static String disconnectInTransaction(
            final Context context,
            final String from, final String to, final String relationships,
            final Disconnector disconnector)
            throws Exception {

        final String[] results = new String[1];

        runInTransaction(context, new Action() {
            @Override
            public void run() throws Exception {
                results[0] = disconnect(context, from, to, relationships, disconnector);
            }
        });

        return results[0];
    }

    public static String disconnect(Context context, String[] args, IMS_KDD_mxJPO.Disconnector disconnector) {
        try {
            return IMS_KDD_mxJPO.disconnectInTransaction(
                    context,
                    args[0], args[1], args[2],
                    disconnector);
        }
        catch (Exception e) {
            return "ERROR: " + e.getMessage();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    public static String getRefreshRowFunction(String rowId) {
        return String.format("function(){emxEditableTable.refreshRowByRowId('%s');}", rowId);
    }

    public static String getRefreshAllRowsFunction() {
        return "function(){refreshRows();}";
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
}