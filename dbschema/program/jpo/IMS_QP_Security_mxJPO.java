import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_Security_mxJPO {
    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public static final String TYPE_Person = "Person";

    public static final String ROLE_IMS_QP_DEPOwner = "IMS_QP_DEPOwner";
    public static final String ROLE_IMS_Admin = "IMS_Admin";
    public static final String ROLE_IMS_QP_SuperUser = "IMS_QP_SuperUser";
    //added by feature #54625
    public static final String ROLE_IMS_QP_Viewer = "IMS_QP_Viewer";
    //added by feature  #65455
    public static final String ROLE_IMS_QP_Supervisor = "IMS_QP_Supervisor";

    public static final String RELATIONSHIP_IMS_QP_DEP2Owner = "IMS_QP_DEP2Owner";
    public static final String RELATIONSHIP_IMS_PBS2Owner = "IMS_PBS2Owner";

    private static final String RELATIONSHIP_IMS_QP_DEP2DEPProjectStage = "IMS_QP_DEP2DEPProjectStage";
    private static final String RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage = "IMS_QP_DEPProjectStage2DEPSubStage";
    private static final String RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask = "IMS_QP_DEPSubStage2DEPTask";
    private static final String RELATIONSHIP_IMS_QP_ExpectedResult2DEPTask = "IMS_QP_ExpectedResult2DEPTask";

    private static final String TYPE_IMS_QP_DEP = "IMS_QP_DEP";
    private static final String TYPE_IMS_QP_DEPProjectStage = "IMS_QP_DEPProjectStage";
    private static final String TYPE_IMS_QP_DEPSubStage = "IMS_QP_DEPSubStage";
    private static final String TYPE_IMS_QP_DEPTask = "IMS_QP_DEPTask";

    private static final String SOURCE_Person = "Person";

    private static final String PROGRAM_IMS_QP_Security = "IMS_QP_Security";

    public static final String STATE_DONE = "Done";

    public IMS_QP_Security_mxJPO(Context context, String[] args) throws Exception {
    }

    public static DomainObject getPersonObject(Context context, String personName) throws MatrixException {
        return new DomainObject(new BusinessObject(TYPE_Person, personName, "-", context.getVault().getName()));
    }

    private static boolean currentUserIsAdmin(Context context) throws MatrixException {
        return context.isAssigned(ROLE_IMS_Admin);
    }

    public static boolean currentUserIsQPSuperUser(Context context) {
        try {
            return context.isAssigned(ROLE_IMS_QP_SuperUser);
        } catch (MatrixException me) {
            LOG.error("error check isAssigned: " + me.getMessage());
            return false;
        }
    }

    private static boolean hasDEPOwnerRole(Context context, String personName) throws MatrixException {
        return new Person(personName).isAssigned(context, ROLE_IMS_QP_DEPOwner);
    }

    private static boolean currentUserHasDEPOwnerRole(Context context) throws MatrixException {
        return hasDEPOwnerRole(context, context.getUser());
    }

    private static void checkHasDEPOwnerRole(Context context, String personName) throws Exception {
        if (!hasDEPOwnerRole(context, personName)) {
            throw new Exception(String.format("Person '%s' must be '%s'", personName, ROLE_IMS_QP_DEPOwner));
        }
    }

    private static void checkCurrentUserHasDEPOwnerRole(Context context) throws Exception {
        if (!currentUserHasDEPOwnerRole(context)) {
            throw new Exception(String.format("Current user must be '%s'", ROLE_IMS_QP_DEPOwner));
        }
    }

    private static void checkCurrentUserIsDEPOwner(Context context, DomainObject depObject) throws Exception {
        if (!currentUserIsDEPOwner(context, depObject)) {
            throw new Exception("Access denied");
        }
    }

    /**
     * Determines if the current user is a DEP owner of the specified object.
     * The object can be a DEP or DEP descendant of any level (IMS_DEPProjectStage, IMS_DEP_SubStage, IMS_DEPTask).
     *
     * @param context the context
     * @param object  the object to check
     * @return A boolean value indicating if the current user is a DEP owner of the specified object
     * @throws Exception When something goes wrong
     */
    public static boolean currentUserIsDEPOwner(Context context, DomainObject object) throws Exception {
        return isDEPOwner(context, getPersonObject(context, context.getUser()), object);
    }

    /**
     * Determines if the current user is a DEP owner of the specified object.
     * The object can be a DEP or DEP descendant of any level (IMS_DEPProjectStage, IMS_DEP_SubStage, IMS_DEPTask).
     *
     * @param context the context
     * @param args    the arguments
     * @return A boolean value indicating if the current user is a DEP owner of the object passed in {@code args}
     * @throws Exception When something goes wrong
     */
    public static boolean currentUserIsDEPOwner(Context context, String[] args) throws Exception {
        boolean isDEPOwner = isDEPOwner(context, getPersonObject(context, context.getUser()), new DomainObject(IMS_KDD_mxJPO.getObjectFromProgramMap(context, args)));
        return isDEPOwner;
    }

    /**
     * @param context  usual parameter
     * @param objectId DEP object id
     * @return boolean `isDEPOwner` from QPTask
     * @throws Exception
     */
    public static boolean isOwnerDepFromQPlan(Context context, String objectId) throws FrameworkException {
        StringBuilder sb = new StringBuilder(String.format("print bus %s select ", objectId))
                .append("to[IMS_QP_DEP2QPlan].from.")
                .append("from[IMS_QP_DEP2Owner].to.name")
                .append(" dump |");

        String objectOwners;
        try {
            objectOwners = MqlUtil.mqlCommand(context, sb.toString());
        } catch (FrameworkException frameworkException) {
            LOG.error("mql command error: " + frameworkException.getMessage());
            frameworkException.printStackTrace();
            throw frameworkException;
        }
        return objectOwners.contains(context.getUser()) || isUserAdminOrSuper(context);
    }

    /**
     * @param context usual parameter
     * @param args    usual parameter
     * @return boolean `isDEPOwner` from QPTask
     * @throws Exception
     */
    public static boolean isOwnerDepFromQPTask(Context context, String[] args) throws Exception {
        Map map = JPO.unpackArgs(args);
        String objectId = UIUtil.isNotNullAndNotEmpty((String) map.get("objectId")) ?
                (String) map.get("objectId") : (String) map.get("parentOID");
        return isOwnerDepFromQPTask(context, objectId) || isUserAdmin(context);
    }

    /**
     * @param context
     * @param objectId
     * @return
     * @throws Exception
     */
    public static boolean isOwnerDepFromQPTask(Context context, String objectId) {
        StringBuilder sb = new StringBuilder(String.format("print bus %s select ", objectId));
        sb.append("to[IMS_QP_DEPTask2QPTask].from.");
        sb.append("to[IMS_QP_DEPSubStage2DEPTask].from.");
        sb.append("to[IMS_QP_DEPProjectStage2DEPSubStage].from.");
        sb.append("to[IMS_QP_DEP2DEPProjectStage].from.");
        sb.append("from[IMS_QP_DEP2Owner].to.name");
        sb.append(" dump |");
        String objectOwners;
        try {
            objectOwners = MqlUtil.mqlCommand(context, sb.toString());
        } catch (FrameworkException frameworkException) {
            LOG.error("mql command error: " + frameworkException.getMessage());
            frameworkException.printStackTrace();
            return false;
        }
        return objectOwners.contains(context.getUser()) || isUserAdminOrSuper(context);
    }

    /**
     * @param context  usual parameter
     * @param objectId id of Quality plan
     * @return boolean `isDEPOwner` from QPlan
     * @throws Exception
     */
    public static boolean isOwnerDepFromQPPlan(Context context, String objectId) throws Exception {
        StringBuilder sb = new StringBuilder(String.format("print bus %s select ", objectId));
        sb
                .append("to[IMS_QP_DEP2QPlan].from")
                .append(".from[IMS_QP_DEP2Owner].to.name")
                .append(" dump |");
        String objectOwners;
        try {
            objectOwners = MqlUtil.mqlCommand(context, sb.toString());
        } catch (FrameworkException frameworkException) {
            LOG.error("mql command error: " + frameworkException.getMessage());
            frameworkException.printStackTrace();
            throw frameworkException;
        }
        return objectOwners.contains(context.getUser()) || isUserAdminOrSuper(context);
    }

    private static void checkAccess(Context context, DomainObject depObject) throws Exception {
        if (currentUserIsAdmin(context)) {
            return;
        }
        checkCurrentUserHasDEPOwnerRole(context);
        checkCurrentUserIsDEPOwner(context, depObject);
    }

    private static void addDEPOwnerPrivate(Context context, DomainObject personObject, DomainObject depObject) throws Exception {
        checkAccess(context, depObject);
        checkHasDEPOwnerRole(context, personObject.getName(context));
        IMS_KDD_mxJPO.connectIfNotConnected(context, RELATIONSHIP_IMS_QP_DEP2Owner, depObject, personObject);
    }

    /**
     * Adds the specified person to the specified DEP object's owners
     *
     * @param context      the context
     * @param personObject the person object
     * @param depObject    the DEP object
     * @throws Exception When something goes wrong
     */
    public static void addDEPOwner(final Context context, final DomainObject personObject, final DomainObject depObject) throws Exception {
        if (context.isTransactionActive()) {
            addDEPOwnerPrivate(context, personObject, depObject);
        } else {
            IMS_KDD_mxJPO.runInTransaction(context, new IMS_KDD_mxJPO.Action() {
                @Override
                public void run() throws Exception {
                    addDEPOwnerPrivate(context, personObject, depObject);
                }
            });
        }
    }

    /**
     * Adds the specified person to the specified DEP object's owners
     *
     * @param context     the context
     * @param personId    the ID of the person
     * @param depObjectId the ID of the DEP object
     * @throws Exception When something goes wrong
     */
    public static void addDEPOwner(final Context context, final String personId, final String depObjectId) throws Exception {
        addDEPOwner(context, new DomainObject(personId), new DomainObject(depObjectId));
    }

    public static void addDEPOwner(Context context, String[] args) throws Exception {
        addDEPOwner(context, args[0], args[1]);
    }

    /**
     * Adds the specified person to the specified DEP object's owners
     *
     * @param context     the context
     * @param personName  the name of the person
     * @param depObjectId the ID of the DEP object
     * @throws Exception When something goes wrong
     */
    public static void addPersonNameDEPOwner(final Context context, final String personName, final String depObjectId) throws Exception {
        addDEPOwner(context, getPersonObject(context, personName), new DomainObject(depObjectId));
    }

    public static void addPersonNameDEPOwner(Context context, String[] args) throws Exception {
        addPersonNameDEPOwner(context, args[0], args[1]);
    }

    private static void removeDEPOwnerPrivate(Context context, DomainObject depObject, DomainObject personObject) throws Exception {
        checkAccess(context, depObject);
        if (!currentUserIsAdmin(context) && context.getUser().equals(personObject.getName(context))) {
            throw new Exception("Cannot remove yourself");
        }
        if (IMS_KDD_mxJPO.isConnected(context, RELATIONSHIP_IMS_QP_DEP2Owner, depObject, personObject)) {

            depObject.disconnect(
                    context,
                    new RelationshipType(RELATIONSHIP_IMS_QP_DEP2Owner),
                    true,
                    personObject);
        }
    }

    /**
     * Removes the specified person from the specified DEP object's owners
     *
     * @param context      the context
     * @param personObject the person object
     * @param depObject    the DEP object
     * @throws Exception When something goes wrong
     */
    public static void removeDEPOwner(final Context context, final DomainObject personObject, final DomainObject depObject) throws Exception {
        if (context.isTransactionActive()) {
            removeDEPOwnerPrivate(context, depObject, personObject);
        } else {
            IMS_KDD_mxJPO.runInTransaction(context, new IMS_KDD_mxJPO.Action() {
                @Override
                public void run() throws Exception {
                    removeDEPOwnerPrivate(context, depObject, personObject);
                }
            });
        }
    }

    /**
     * Removes the specified person from the specified DEP object's owners
     *
     * @param context     the context
     * @param personId    the ID of the person
     * @param depObjectId the ID of the DEP object
     * @throws Exception When something goes wrong
     */
    public static void removeDEPOwner(final Context context, final String personId, final String depObjectId) throws Exception {
        removeDEPOwner(context, new DomainObject(personId), new DomainObject(depObjectId));
    }

    public static void removeDEPOwner(Context context, String[] args) throws Exception {
        removeDEPOwner(context, args[0], args[1]);
    }

    /**
     * Removes the specified person from the specified DEP object's owners
     *
     * @param context     the context
     * @param personName  the name of the person
     * @param depObjectId the ID of the DEP object
     * @throws Exception When something goes wrong
     */
    public static void removePersonNameDEPOwner(final Context context, final String personName, final String depObjectId) throws Exception {
        removeDEPOwner(context, getPersonObject(context, personName), new DomainObject(depObjectId));
    }

    public static void removePersonNameDEPOwner(Context context, String[] args) throws Exception {
        removePersonNameDEPOwner(context, args[0], args[1]);
    }

    private static boolean isDEPOwnerPrivate(Map map) {
        return !STATE_DONE.equals(map.get(DomainConstants.SELECT_CURRENT)) &&
                "TRUE".equalsIgnoreCase((String) map.get(String.format("from[%s]", RELATIONSHIP_IMS_QP_DEP2Owner)));
    }

    private static boolean isDEPOwnerPrivate(Context context, DomainObject personObject, DomainObject object) throws Exception {
        if (currentUserIsAdmin(context)) {
            return true;
        }
        if (!hasDEPOwnerRole(context, personObject.getName(context))) {
            return false;
        }

        if (object.getType(context).equals(TYPE_IMS_QP_DEP)) {
            return !STATE_DONE.equals(object.getCurrentState(context).getName()) &&
                    IMS_KDD_mxJPO.isConnected(context, RELATIONSHIP_IMS_QP_DEP2Owner, object, personObject);
        }

        MapList mapList = object.getRelatedObjects(
                context,
                StringUtils.join(
                        new String[]{
                                RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                                RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                                RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                                RELATIONSHIP_IMS_QP_ExpectedResult2DEPTask
                        },
                        ','),
                StringUtils.join(
                        new String[]{
                                TYPE_IMS_QP_DEP,
                                TYPE_IMS_QP_DEPProjectStage,
                                TYPE_IMS_QP_DEPSubStage,
                                TYPE_IMS_QP_DEPTask
                        },
                        ','),
                new StringList(new String[]{
                        DomainConstants.SELECT_CURRENT,
                        String.format("from[%s|to.id==%s]", RELATIONSHIP_IMS_QP_DEP2Owner, personObject.getId(context))
                }),
                null,
                true, false, (short) 0,
                null,
                null);

        for (Object item : mapList) {
            if (isDEPOwnerPrivate((Map) item)) {
                return true;
            }
        }

        mapList = object.getRelatedObjects(
                context,
                RELATIONSHIP_IMS_QP_ExpectedResult2DEPTask,
                TYPE_IMS_QP_DEPTask,
                new StringList(String.format(
                        "to[%s].from.to[%s].from.to[%s].from[%s|to.id==%s]",
                        RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                        RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                        RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                        RELATIONSHIP_IMS_QP_DEP2Owner,
                        personObject.getId(context))),
                null,
                false, true, (short) 1,
                null,
                null);

        for (Object item : mapList) {
            if (isDEPOwnerPrivate((Map) item)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines if the specified person is a DEP owner of the specified object.
     * The object can be a DEP or DEP descendant of any level (IMS_DEPProjectStage, IMS_DEP_SubStage, IMS_DEPTask).
     *
     * @param context      the context
     * @param personObject the person object
     * @param object       the object to check
     * @return A boolean value indicating if the current user is a DEP owner of the specified object
     * @throws Exception When something goes wrong
     */
    public static boolean isDEPOwner(final Context context, final DomainObject personObject, final DomainObject object) throws Exception {
        if (context.isTransactionActive()) {
            return isDEPOwnerPrivate(context, personObject, object);
        }
        final boolean[] result = new boolean[1];

        IMS_KDD_mxJPO.runInTransaction(context, new IMS_KDD_mxJPO.Action() {
            @Override
            public void run() throws Exception {
                result[0] = isDEPOwnerPrivate(context, personObject, object);
            }
        });

        return result[0];
    }

    /**
     * Determines if the specified person is a DEP owner of the specified object.
     * The object can be a DEP or DEP descendant of any level (IMS_DEPProjectStage, IMS_DEP_SubStage, IMS_DEPTask).
     *
     * @param context  the context
     * @param personId the ID of the person
     * @param objectId the ID of the object to check
     * @return A boolean value indicating if the current user is a DEP owner of the specified object
     * @throws Exception When something goes wrong
     */
    public static boolean isDEPOwner(Context context, String personId, String objectId) throws Exception {
        return isDEPOwner(context, new DomainObject(personId), new DomainObject(objectId));
    }

    public static boolean isDEPOwner(Context context, String[] args) throws Exception {
        boolean result = isDEPOwner(context, args[0], args[1]);
        System.out.println(result);
        return result;
    }

    /**
     * Determines if the specified person is a DEP owner of the specified object.
     * The object can be a DEP or DEP descendant of any level (IMS_DEPProjectStage, IMS_DEP_SubStage, IMS_DEPTask).
     *
     * @param context    the context
     * @param personName the name of the person
     * @param objectId   the ID of the object to check
     * @return A boolean value indicating if the current user is a DEP owner of the specified object
     * @throws Exception When something goes wrong
     */
    public static boolean personNameIsDEPOwner(Context context, String personName, String objectId) throws Exception {
        return isDEPOwner(context, getPersonObject(context, personName), new DomainObject(objectId));
    }

    public static boolean personNameIsDEPOwner(Context context, String[] args) throws Exception {
        boolean result = personNameIsDEPOwner(context, args[0], args[1]);
        System.out.println(result);
        return result;
    }

    /**
     * Determines if the current user is a DEP owner of the specified object.
     * The object can be a DEP or DEP descendant of any level (IMS_DEPProjectStage, IMS_DEP_SubStage, IMS_DEPTask).
     *
     * @param context  the context
     * @param objectId the ID of the object to check
     * @return A boolean value indicating if the current user is a DEP owner of the specified object
     * @throws Exception When something goes wrong
     */
    public static boolean currentUserIsDEPOwner(Context context, String objectId) throws Exception {
        return currentUserIsDEPOwner(context, new DomainObject(objectId));
    }

    public static boolean isAdminOrSuperOrViewer(Context ctx) throws MatrixException {
        Person person = new Person(ctx.getUser());
        return isUserAdminOrSuper(ctx) || person.isAssigned(ctx, ROLE_IMS_QP_Viewer);
    }

    public static boolean isUserSupervisor(Context ctx) {
        Person person = new Person(ctx.getUser());
        try {
            return person.isAssigned(ctx, ROLE_IMS_QP_Supervisor);
        } catch (MatrixException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isUserViewer(Context context) {
        Person person = new Person(context.getUser());

        try {
            return person.isAssigned(context, ROLE_IMS_QP_Viewer) &&
                    !person.isAssigned(context, ROLE_IMS_QP_Supervisor);
        } catch (MatrixException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean isUserViewerWithChild(Context context) {
        Person person = new Person(context.getUser());

        boolean isViewer = false;
        try {
            isViewer = person.isAssigned(context, ROLE_IMS_QP_Viewer);
        } catch (MatrixException e) {
            e.printStackTrace();
        }

        return isViewer;
    }

    @SuppressWarnings("unused")
    public MapList getDEPOwnerRolePersonMaps(Context context, String[] args) throws Exception {
        MapList mapList = new MapList();

        for (Object assignment : new Role(ROLE_IMS_QP_DEPOwner).getAssignments(context)) {
            if (assignment instanceof Person) {
                DomainObject personObject = getPersonObject(context, ((Person) assignment).getName());
                if (personObject.exists(context)) {

                    Map personMap = personObject.getInfo(context, new StringList(new String[]{
                            DomainConstants.SELECT_ID,
                            DomainConstants.SELECT_NAME,
                            DomainConstants.SELECT_TYPE,
                            DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_FIRST_NAME),
                            DomainObject.getAttributeSelect(DomainConstants.ATTRIBUTE_LAST_NAME)
                    }));

                    mapList.add(personMap);
                }
            }
        }

        return mapList;
    }

    @SuppressWarnings("unused")
    public Vector getPersonHTML(Context context, String[] args) throws Exception {
        Vector results = new Vector();
        for (Object objectMapObject : IMS_KDD_mxJPO.getObjectMapList(args)) {
            Map map = (Map) objectMapObject;
            String name = IMS_KDD_mxJPO.getNameFromMap(map);

            results.addElement(IMS_KDD_mxJPO.getLinkHTML(
                    context, map, SOURCE_Person, null,
                    IMS_KDD_mxJPO.FUGUE_16x16 + "user.png",
                    null, null, null,
                    true, true, "mx_divBody",
                    true, null, false));
        }
        return results;
    }

    @SuppressWarnings("unused")
    public String connectDEPOwner(Context context, String[] args) throws Exception {
        return IMS_KDD_mxJPO.connect(context, args, new IMS_KDD_mxJPO.Connector() {
            @Override
            public String connect(Context context, String from, String to, String relationship) throws Exception {

                IMS_KDD_mxJPO.connectIfNotConnected(
                        context,
                        RELATIONSHIP_IMS_QP_DEP2Owner,
                        new DomainObject(from),
                        new DomainObject(to));

                return "";
            }
        });
    }

    @SuppressWarnings("unused")
    public String disconnectDEPOwner(Context context, String[] args) throws Exception {
        return IMS_KDD_mxJPO.disconnect(context, args, new IMS_KDD_mxJPO.Disconnector() {
            @Override
            public String disconnect(Context context, String from, String to, String relationship) throws Exception {

                new DomainObject(from).disconnect(
                        context,
                        new RelationshipType(RELATIONSHIP_IMS_QP_DEP2Owner),
                        true,
                        new DomainObject(to));

                return "";
            }
        });
    }

    @SuppressWarnings("unused")
    public String getDEPOwnersHTML(Context context, String[] args) throws Exception {
        StringBuilder sb = new StringBuilder();
        DomainObject depObject = IMS_KDD_mxJPO.getObjectFromParamMap(args);
        boolean currentUserIsDEPOwner = currentUserIsDEPOwner(context, depObject);

        List<Map> relatedMaps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                context, depObject,
                RELATIONSHIP_IMS_QP_DEP2Owner,
                true,
                Arrays.asList(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_NAME,
                        DomainConstants.SELECT_TYPE,
                        DomainConstants.ATTRIBUTE_FIRST_NAME,
                        DomainConstants.ATTRIBUTE_LAST_NAME),
                null, null, true);

        boolean currentUserIsQPSuperUser = currentUserIsQPSuperUser(context);
        for (Map relatedMap : relatedMaps) {
            if (sb.length() > 0) {
                sb.append("<br />");
            }

            if (currentUserIsQPSuperUser || currentUserIsDEPOwner && !context.getUser().equals(IMS_KDD_mxJPO.getNameFromMap(relatedMap))) {

                sb.append(IMS_KDD_mxJPO.getDisconnectLinkHTML(
                        PROGRAM_IMS_QP_Security, "disconnectDEPOwner",
                        depObject.getId(context), IMS_KDD_mxJPO.getIdFromMap(relatedMap),
                        RELATIONSHIP_IMS_QP_DEP2Owner,
                        "Disconnect",
                        IMS_KDD_mxJPO.getRefreshWindowFunction()));
            }

            sb.append(IMS_KDD_mxJPO.getLinkHTML(
                    context, relatedMap, null, null,
                    IMS_KDD_mxJPO.FUGUE_16x16 + "user.png",
                    "12px",
                    IMS_KDD_mxJPO.getName(context, relatedMap),
                    null, false, false, null, false, null, false));
        }

        if (currentUserIsDEPOwner || currentUserIsQPSuperUser) {

            sb.append(IMS_DragNDrop_mxJPO.getConnectDropAreaHTML(
                    PROGRAM_IMS_QP_Security, "connectDEPOwner",
                    RELATIONSHIP_IMS_QP_DEP2Owner, true,
                    "0", depObject.getId(context),
                    IMS_KDD_mxJPO.getRefreshWindowFunction(),
                    "",
                    SOURCE_Person,
                    "Drop owner here",
                    "100%",
                    "26px", "auto", "12px"));
        }

        return sb.toString();
    }

    public Vector getQPOwnersHTML(Context context, String[] args) {

        Vector result = new Vector();
        StringBuilder stringBuilder = new StringBuilder();

        try {
            Map argsMap = JPO.unpackArgs(args);
            MapList objectList = (MapList) argsMap.get("objectList");

            /*getting all systems*/
            List<Map> items = new ArrayList<>();
            for (Object o : objectList) {
                items.add((Map) o);
            }

            //checking context person assignments
            Person person = new Person(context.getUser());
            boolean isUserAdminOrSuper = isUserAdminOrSuper(context);

            /*top level Codes by items*/
            for (Map map : items) {
                stringBuilder.setLength(0);

                String pbsID = (String) map.get("id");
                DomainObject systemObject = new DomainObject(pbsID);

                List<Map> relatedMaps = new ArrayList<>();

                if (pbsID != null) {
                    relatedMaps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                            context, systemObject, RELATIONSHIP_IMS_PBS2Owner, true,
                            Arrays.asList(
                                    DomainConstants.SELECT_ID,
                                    DomainConstants.SELECT_NAME,
                                    DomainConstants.SELECT_TYPE,
                                    DomainConstants.ATTRIBUTE_FIRST_NAME,
                                    DomainConstants.ATTRIBUTE_LAST_NAME),
                            null, null, true);
                }

                if (!relatedMaps.isEmpty() && systemObject != null)
                    for (Map relatedMap : relatedMaps) {
                        if (stringBuilder.length() > 0) {
                            stringBuilder.append("<br/>");
                        }

                        if (isUserAdminOrSuper)
                            stringBuilder.append(IMS_KDD_mxJPO.getDisconnectLinkHTML(
                                    PROGRAM_IMS_QP_Security, "disconnectQPlanOwner",
                                    pbsID, IMS_KDD_mxJPO.getIdFromMap(relatedMap),
                                    RELATIONSHIP_IMS_PBS2Owner,
                                    "Disconnect",
                                    IMS_KDD_mxJPO.getRefreshRowFunction((String) map.get("id[level]"))));

                        stringBuilder.append(IMS_KDD_mxJPO.getLinkHTML(
                                context, relatedMap, null, null,
                                IMS_KDD_mxJPO.FUGUE_16x16 + "user.png",
                                "12px",
                                IMS_KDD_mxJPO.getName(context, relatedMap),
                                null, false, false, null, false, null, false));
                    }

                if (pbsID != null && isUserAdminOrSuper) {
                    stringBuilder.append(IMS_DragNDrop_mxJPO.getConnectDropAreaHTML(
                            PROGRAM_IMS_QP_Security, "connectQPlanOwner",
                            RELATIONSHIP_IMS_PBS2Owner, true,
                            "0", pbsID,
                            IMS_KDD_mxJPO.getRefreshRowFunction((String) map.get("id[level]")),
                            "",
                            SOURCE_Person,
                            "Drop owner here",
                            "100%",
                            "24px", "auto", "12px"));
                }

                result.addElement(stringBuilder.toString());
            }
        } catch (Exception e) {
            try {
                emxContextUtil_mxJPO.mqlWarning(context, e.toString());
                LOG.error("error getting url string: " + e.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    public String connectQPlanOwner(Context context, String[] args) {
        try {
            ContextUtil.pushContext(context);
        } catch (FrameworkException e) {
            LOG.error("push context error: " + e.getMessage());
        }

        return IMS_KDD_mxJPO.connect(context, args, new IMS_KDD_mxJPO.Connector() {
            @Override
            public String connect(Context context, String from, String to, String relationship) {
                try {
                    LOG.info("connectQPlanOwner: from " + from + "to " + to);

                    DomainObject personObject = new DomainObject(to);
                    Person person = new Person(personObject.getName(context));

                    //if the user connecting to the system is not assigned to IMS_QP_QPOwner
                    if (!person.isAssigned(context, "IMS_QP_QPOwner"))
                        MqlUtil.mqlCommand(context, "mod person $1 assign role $2", person.getName(), "IMS_QP_QPOwner");

                    LOG.info(String.format("connect %s relationship %s to %s", from, RELATIONSHIP_IMS_PBS2Owner, to));

                    DomainRelationship relationshipToOwner = personObject.addRelatedObject(context, new RelationshipType(RELATIONSHIP_IMS_PBS2Owner), true, from);

                    LOG.info("" + relationshipToOwner.getAttributeValues(context));
                } catch (Exception e) {
                    LOG.error("error connecting: " + from + " to " + to + ": " + e.getMessage());
                    for (StackTraceElement er : e.getStackTrace()) {
                        LOG.error(er.toString());
                    }
                }
                try {
                    ContextUtil.popContext(context);
                } catch (FrameworkException e) {
                    LOG.error("pop context error: " + e.getMessage());
                }
                return "";
            }
        });
    }

    public String disconnectQPlanOwner(Context context, String[] args) {
        return IMS_KDD_mxJPO.disconnect(context, args, new IMS_KDD_mxJPO.Disconnector() {
            @Override
            public String disconnect(Context context, String from, String to, String relationship) {

                try {
                    DomainObject owner = new DomainObject(to);
                    Person person = new Person(owner.getName(context));
                    new DomainObject(from).disconnect(context, new RelationshipType(RELATIONSHIP_IMS_PBS2Owner),
                            true, owner);

                    String hasPBS = owner.getInfo(context, "to[IMS_PBS2Owner]");
                    if (hasPBS.equals("FALSE")) {
                        MqlUtil.mqlCommand(context, "mod person $1 remove assign role $2", person.getName(), "IMS_QP_QPOwner");
                    }
                } catch (Exception e) {
                    for (StackTraceElement er : e.getStackTrace()) {
                        LOG.error(er.toString());
                    }
                    LOG.error("error disconnecting: " + to + " from " + from);
                }
                return "";
            }
        });
    }

    public static boolean isUserAdmin(Context context) {
        Person person = new Person(context.getUser());
        try {
            return person.isAssigned(context, ROLE_IMS_Admin);
        } catch (MatrixException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean isUserAdminOrSuper(Context context) {
        Person person = new Person(context.getUser());
        try {
            return person.isAssigned(context, ROLE_IMS_Admin) || person.isAssigned(context, ROLE_IMS_QP_SuperUser);
        } catch (MatrixException e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * @param context
     * @param args
     * @return
     */
    public static boolean isOwner(Context context, String[] args) {

        String ROLE_IMS_QP_QPOwner = "IMS_QP_QPOwner";
        boolean isOwner = false;
        Person person = new Person(context.getUser());

        try {
            isOwner = person.isAssigned(context, ROLE_IMS_QP_QPOwner) || isUserAdmin(context);
        } catch (MatrixException me) {
            LOG.error("error when checking Person: " + me.getMessage());
        }

        return isOwner;
    }

    private static boolean isOwnerInterdisciplinaryQPlan(Context context, String id) throws FrameworkException {
        String personNames = MqlUtil.mqlCommand(context,
                String.format("print bus %s select to[IMS_QP_DEP2QPlan].from.from[IMS_QP_DEP2Owner].to.name dump |", id)
        );
        return UIUtil.isNotNullAndNotEmpty(personNames) && personNames.contains(context.getUser());
    }

    /**
     * @param ctx
     * @param args
     * @return
     */
    public static boolean isOwnerQPlan(Context ctx, String... args) throws Exception {

        Map argsMap = JPO.unpackArgs(args);

        String id = (String) argsMap.get("parentOID");
        DomainObject planObject = new DomainObject(id);
        String type = planObject.getType(ctx);
        String from = planObject.getInfo(ctx, "to[IMS_QP_QP2QPlan].from.name");
        boolean isPlanType = IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan.equals(type);

        /**
         * rule for `AQP` level
         */
        LOG.info("_stage 1_ isPlanType: " + isPlanType + " from " + from + " owner " + planObject.getOwner(ctx));
        if (isPlanType &&
                "AQP".equals(from) &&
                planObject.getOwner(ctx).getName().equals(ctx.getUser())) {
            return true;
        }

        String attributeInterdisciplinary = planObject.getInfo(ctx, "to[IMS_QP_DEP2QPlan].from.attribute[IMS_QP_InterdisciplinaryDEP]");
        boolean isInterdisciplinary = attributeInterdisciplinary.equals("TRUE");

        /**
         * rule for `SQP` & Interdisciplinary `IMS_QP_QPlan`
         * that object has not relationship with KKS/PBS
         */

        LOG.info("_stage 2_ isPlanType: " + isPlanType
                + " from " + from
                + " isInterdisciplinary " + isInterdisciplinary
                + " has no connections to PBS " + planObject.getInfo(ctx, "from[IMS_QP_QPlan2Object]").equals("FALSE")
        );
        if (isPlanType && isInterdisciplinary
                && planObject.getInfo(ctx, "from[IMS_QP_QPlan2Object]").equals("FALSE")) {
            return isOwnerInterdisciplinaryQPlan(ctx, id);
        }

        /**
         * rule for `SQP` the Non-Interdisciplinary `IMS_QP_QPlan`
         * that object has relationship with KKS/PBS
         */

        LOG.info("_stage 3_ isPlanType: " + isPlanType
                + " from " + from
                + " not isInterdisciplinary " + !isInterdisciplinary
                + " has any connections to PBS " + planObject.getInfo(ctx, "from[IMS_QP_QPlan2Object]").equals("TRUE")
                + " owners " + MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_QP_QPlan2Object].to.from[IMS_PBS2Owner].to.name dump |", id))
                + " user: " + ctx.getUser()
        );
        if (isPlanType && !isInterdisciplinary
                && planObject.getInfo(ctx, "from[IMS_QP_QPlan2Object]").equals("TRUE")) {

            String personNames = MqlUtil.mqlCommand(ctx, String.format("print bus %s select from[IMS_QP_QPlan2Object].to.from[IMS_PBS2Owner].to.name dump |", id));
            return UIUtil.isNotNullAndNotEmpty(personNames) && personNames.contains(ctx.getUser());
        }

        return false;
    }

    /**
     * @param context
     * @param id      of IMS_QP_QPlan type object
     * @return boolean
     */
    public static boolean isOwnerQPlan(Context context, String id) {
        boolean isOwnerQPlan = false;

        try {
            Map argsMap = new HashMap();
            argsMap.put("parentOID", id);
            String[] args = JPO.packArgs(argsMap);
            isOwnerQPlan = isOwnerQPlan(context, args);

        } catch (Exception e) {
            LOG.error("error in method isOwnerQPlan: " + e.getMessage());
        }

        return isOwnerQPlan || isUserAdmin(context);
    }

    public static boolean isOwnerQPlanFromTask(Context ctx, String... args) {
        if (IMS_QP_Security_mxJPO.isUserAdmin(ctx)) {
            LOG.info("return true because user is Admin");
            return true;
        }

        Map argsMap = null;

        DomainObject planObject = null;
        String from = null;
        String planType = null;
        String objectId = null;

        try {
            argsMap = JPO.unpackArgs(args);
            objectId = (String) argsMap.get("objectId");
            String parentId = (String) argsMap.get("parentOID");
            if (UIUtil.isNullOrEmpty(objectId)) {
                Map requestMap = (Map) argsMap.get("requestMap");
                objectId = (String) requestMap.get("objectId");
                parentId = (String) requestMap.get("parentOID");
            }
            if (UIUtil.isNullOrEmpty(objectId)) {
                Map paramMap = (Map) argsMap.get("paramMap");
                objectId = (String) paramMap.get("objectId");
                parentId = (String) paramMap.get("parentOID");
            }

            DomainObject domainObject = null;
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                domainObject = new DomainObject(objectId);
            }

            DomainObject object = null;
            if (domainObject != null) {
                LOG.info("domain type: " + domainObject.getType(ctx));
                if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(domainObject.getType(ctx))) {
                    object = domainObject;
                }
            } else {
                object = new DomainObject(parentId);
            }

            //getting target `from` qPlan ID
            String planId = null;
            if (object != null) {
                if (object.getType(ctx).equals(IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask)) {
                    planId = object.getInfo(ctx, "to[IMS_QP_QPlan2QPTask].from.id");
                }
            } else {
                planId = parentId;
            }

            planObject = new DomainObject(planId);
            LOG.info(planId + " is kind of " + object.getType(ctx) + " plan : " + planObject.getName(ctx));

            //before aspect AQP
            if (planObject != null) {
                planType = planObject.getType(ctx);
                from = planObject.getInfo(ctx, "to[IMS_QP_QP2QPlan].from.name");
            } else {
                LOG.error("error getting qplan object from method isOwnerQPlanFromTask with args: " + argsMap);
                return false;
            }

            LOG.info("plan id: " + planId + " plan type: " + planType + " from " + from);
            if (IMS_QP_Constants_mxJPO
                    .type_IMS_QP_QPlan.equals(planType) && "AQP".equals(from)) {
                if (planObject.getOwner(ctx).getName().equals(ctx.getUser())) {
                    return true;
                } else {
                    return false;
                }
            }

            //change ID in args map
            argsMap.put("parentOID", planId);
        } catch (Exception e) {
            LOG.error("error in method: " + e.getMessage());
            e.printStackTrace();
        }

        boolean isOwnerQPlanFromTask = false, isDepOwnerFromQPTask = false;
        try {
            if (planType.equals(IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan)) {
                {
                    argsMap.put("objectId", planObject.getId(ctx));
                    isOwnerQPlanFromTask = isOwnerQPlan(ctx, JPO.packArgs(argsMap));
                }
            }
        } catch (Exception e) {
            try {
                LOG.error("error check owner IMS_Security owner from task for plan: " + planObject.getName(ctx) + " with message: " + e.getMessage());
            } catch (FrameworkException ex) {
                LOG.error("plan identify error: " + ex.getMessage());
                ex.printStackTrace();
            }
        }

        LOG.info(isOwnerQPlanFromTask + "|" + isDepOwnerFromQPTask);
        return isOwnerQPlanFromTask || isDepOwnerFromQPTask;
    }

    /**
     * Method of checking the owner of the task from the received ID object
     * Using in the delete task action
     *
     * @param context
     * @param id
     * @return
     */
    public static boolean isOwnerQPlanFromTaskID(Context context, String id) {
        try {

            Map argsMap = new HashMap();
            argsMap.put("parentOID", id);
            return isOwnerQPlanFromTask(context, JPO.packArgs(argsMap));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }
}
