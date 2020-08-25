import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class IMS_QP_Security_mxJPO {

    private static final String TYPE_Person = "Person";

    private static final String ATTRIBUTE_FirstName = "First Name";
    private static final String ATTRIBUTE_LastName = "Last Name";

    private static final String ROLE_IMS_QP_DEPOwner = "IMS_QP_DEPOwner";
    private static final String ROLE_IMS_Admin = "IMS_Admin";
    private static final String ROLE_IMS_QP_SuperUser = "IMS_QP_SuperUser";

    private static final String RELATIONSHIP_IMS_QP_DEP2Owner = "IMS_QP_DEP2Owner";

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

    private static final String STATE_DONE = "Done";

    public IMS_QP_Security_mxJPO(Context context, String[] args) throws Exception {
    }

    private static DomainObject getPersonObject(Context context, String personName) throws MatrixException {
        return new DomainObject(new BusinessObject(TYPE_Person, personName, "-", context.getVault().getName()));
    }

    private static boolean currentUserIsAdmin(Context context) throws MatrixException {
        return context.isAssigned(ROLE_IMS_Admin);
    }

    public static boolean currentUserIsQPSuperUser(Context context) throws MatrixException {
        return context.isAssigned(ROLE_IMS_QP_SuperUser);
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
     * @param object the object to check
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
     * @param args the arguments
     * @return A boolean value indicating if the current user is a DEP owner of the object passed in {@code args}
     * @throws Exception When something goes wrong
     */
    public static boolean currentUserIsDEPOwner(Context context, String[] args) throws Exception {
        return isDEPOwner(context, getPersonObject(context, context.getUser()), new DomainObject(IMS_KDD_mxJPO.getObjectFromProgramMap(context, args)));
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
     * @param context the context
     * @param personObject the person object
     * @param depObject the DEP object
     * @throws Exception When something goes wrong
     */
    public static void addDEPOwner(final Context context, final DomainObject personObject, final DomainObject depObject) throws Exception {
        if (context.isTransactionActive()) {
            addDEPOwnerPrivate(context, personObject, depObject);
        }
        else {
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
     * @param context the context
     * @param personId the ID of the person
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
     * @param context the context
     * @param personName the name of the person
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
     * @param context the context
     * @param personObject the person object
     * @param depObject the DEP object
     * @throws Exception When something goes wrong
     */
    public static void removeDEPOwner(final Context context, final DomainObject personObject, final DomainObject depObject) throws Exception {
        if (context.isTransactionActive()) {
            removeDEPOwnerPrivate(context, depObject, personObject);
        }
        else {
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
     * @param context the context
     * @param personId the ID of the person
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
     * @param context the context
     * @param personName the name of the person
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
                        new String[] {
                                RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
                                RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                                RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                                RELATIONSHIP_IMS_QP_ExpectedResult2DEPTask
                        },
                        ','),
                StringUtils.join(
                        new String[] {
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
     * @param context the context
     * @param personObject the person object
     * @param object the object to check
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
     * @param context the context
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
     * @param context the context
     * @param personName the name of the person
     * @param objectId the ID of the object to check
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
     * @param context the context
     * @param objectId the ID of the object to check
     * @return A boolean value indicating if the current user is a DEP owner of the specified object
     * @throws Exception When something goes wrong
     */
    public static boolean currentUserIsDEPOwner(Context context, String objectId) throws Exception {
        return currentUserIsDEPOwner(context, new DomainObject(objectId));
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

        for (Map relatedMap : relatedMaps) {
            if (sb.length() > 0) {
                sb.append("<br />");
            }

            if (currentUserIsDEPOwner && !context.getUser().equals(IMS_KDD_mxJPO.getNameFromMap(relatedMap))) {

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

        if (currentUserIsDEPOwner) {

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
}
