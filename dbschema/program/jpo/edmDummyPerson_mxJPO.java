import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Vector;

/**
 * Created by 20906648 on 26/06/19.
 *  * !!!!!!!!!!!This program execute User Agent!!!!!!!!!!!!!!
 */
public class edmDummyPerson_mxJPO {

    private static final String PARENT_PERSON_REL_NAME = "edmDummyPersonParent";

    //###################
    //TRIGGER
    //###################
    public int checkCreate(Context context, String[] args) throws Exception {
        try {
            String personName = MQLCommand.exec(context, "get env $1", "NAME");
            String objectType = MQLCommand.exec(context, "get env $1", "TYPE");

            Query queryPerson = new Query();
            queryPerson.setBusinessObjectType(DomainObject.TYPE_PERSON + "," + objectType);
            queryPerson.setWhereExpression(DomainObject.SELECT_NAME + " smatch \"" + personName  + "\"");
            BusinessObjectList personBOList = queryPerson.evaluate(context);

            if (personBOList.size()>0) {
                throw new Exception("Person " + personName + " already exists. Use another person name.");
            }

            return 0;
        } catch (Exception ex) {
            throw ex;
        }

    }
    public void createPerson(Context context, String[] args) throws Exception {
        try {
            ContextUtil.startTransaction(context, true);

            DomainObject dummyPersonDO = getDummyPersonDO(context);
            DomainObject parentPersonDO = getParentPersonDO(context, dummyPersonDO);
            DomainObject newPersonDO = copyPerson(context, dummyPersonDO, parentPersonDO);

            updatePassword(context, newPersonDO);
            updateConnection(context, newPersonDO, parentPersonDO, dummyPersonDO);
            updateProduct(context, parentPersonDO, newPersonDO);
            activateNewPerson(context, newPersonDO);

            ContextUtil.commitTransaction(context);
        } catch (Exception ex) {
            ContextUtil.abortTransaction(context);
            throw ex;
        }
    }

    //###################
    //UI
    //###################
    public void connectParentPerson(Context context, String[] args) throws Exception {
        try {
            HashMap programMap = JPO.unpackArgs(args);

            HashMap paramMap = (HashMap) programMap.get("paramMap");

            String createdObjectId = (String) paramMap.get("objectId");
            String selectedObjectIds = (String) paramMap.get("New OID");

            if (!UIUtil.isNullOrEmpty(selectedObjectIds)) {
                DomainRelationship.connect(
                        context,
                        new DomainObject(selectedObjectIds),
                        PARENT_PERSON_REL_NAME,
                        new DomainObject(createdObjectId));
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
    public Vector getParentPersonRoles(Context context, String[] args) throws Exception {
        Vector<String> vector = new Vector<>();

        HashMap programMap = JPO.unpackArgs(args);
        MapList objectList = (MapList) programMap.get("objectList");

        for (Object o : objectList) {
            try {
                Map map = (Map) o;
                String objectId = (String) map.get(DomainObject.SELECT_ID);

                DomainObject domainObject = new DomainObject(objectId);
                String fromPersonName = domainObject.getInfo(
                        context,
                        "to["+ PARENT_PERSON_REL_NAME +"].from.name");

                Person parentPerson = new Person(fromPersonName);
                parentPerson.open(context);

                UserList assignments = parentPerson.getAssignments(context);
                vector.add(assignments.join(", "));
            } catch (Exception ex) {
                vector.add(null);
            }
        }

        return vector;
    }

    //###################
    //HELPER
    //###################
    private static DomainObject getDummyPersonDO(Context context) throws Exception {
        try {
            String dummyPersonId = MQLCommand.exec(context, "get env $1", "OBJECTID");

            return new DomainObject(dummyPersonId);
        } catch (Exception ex) {
            throw ex;
        }
    }
    private static DomainObject getParentPersonDO(Context context, DomainObject dummyPersonDO) throws Exception {
        try {
            String fromPersonObjectId = dummyPersonDO.getInfo(context, "to[" + PARENT_PERSON_REL_NAME + "].from.id");
            if (UIUtil.isNullOrEmpty(fromPersonObjectId)) {
                throw new Exception("Parent person does not exists. Add connect parent person.");
            }

            DomainObject parentPersonDO = new DomainObject(fromPersonObjectId);

            checkBusiness(context, parentPersonDO);

            return parentPersonDO;
        } catch (Exception ex) {
            throw ex;
        }
    }
    private static DomainObject copyPerson(Context context, DomainObject dummyPersonDO, DomainObject parentPersonDO) throws Exception {
        try {
            String dummyPersonName = dummyPersonDO.getInfo(context, DomainObject.SELECT_NAME);
            dummyPersonName = dummyPersonName.toLowerCase();

            String parentPersonName = parentPersonDO.getInfo(context, DomainObject.SELECT_NAME);

            MQLCommand.exec(
                    context,
                    "copy person $1 $2 type inactive",
                    parentPersonName, dummyPersonName);

            Person dummyPerson = new Person(dummyPersonName);
            dummyPerson.open(context);
            UserList assignList = dummyPerson.getAssignments(context);
            for (User user : assignList) {
                String assignName = user.getName();
                if (assignName.contains(parentPersonName + "_PRJ")) {
                    MQLCommand.exec(
                            context,
                            "mod person $1 remove assign role $2",
                            dummyPersonName, assignName);
                }
            }

            DomainObject newPersonDO = new DomainObject(parentPersonDO.cloneObject(context, dummyPersonName));

            AttributeList dummyAttributeList = dummyPersonDO.getAttributeValues(context);
            newPersonDO.setAttributes(context, dummyAttributeList);
            newPersonDO.setOwner(context, dummyPersonName);

            return newPersonDO;
        } catch (Exception ex) {
            throw ex;
        }
    }
    private static void updatePassword(Context context, DomainObject newPersonDO) throws Exception {
        try {
            String personName = newPersonDO.getInfo(context, DomainObject.SELECT_NAME);
            String newPassword = passwordGenerator(context);

            MQLCommand.exec(
                    context,
                    "mod person $1 password $2",
                    personName, newPassword);

        } catch (Exception ex) {
            throw ex;
        }
    }
    private static void updateConnection(Context context, DomainObject newPersonDO, DomainObject parentPersonDO, DomainObject dummyPersonDO) throws Exception {
        try {
            String parentPersonEmployee = parentPersonDO.getInfo(context, "to["+DomainRelationship.RELATIONSHIP_EMPLOYEE+"].from.id");

            DomainRelationship.connect(
                    context,
                    parentPersonEmployee,
                    DomainRelationship.RELATIONSHIP_EMPLOYEE,
                    newPersonDO.getObjectId(),
                    false);

            /*DomainRelationship.connect(
                    context,
                    dummyPersonDO,
                    PARENT_PERSON_REL_NAME,
                    newPersonDO);*/
        } catch (Exception ex) {
            throw ex;
        }

    }
    private static void updateProduct(Context context, DomainObject parentPersonDO, DomainObject newPersonDO) throws Exception {
        String parentPersonName = parentPersonDO.getInfo(context, DomainObject.SELECT_NAME);
        String newPersonName = newPersonDO.getInfo(context, DomainObject.SELECT_NAME);

        String sProduct = MQLCommand.exec(context, "print person $1 select $2 dump", parentPersonName, "product");

        if (UIUtil.isNotNullAndNotEmpty(sProduct)) {
            String[] products = sProduct.split(",");
            for (String productName : products) {
                MQLCommand.exec(
                        context,
                        "mod product $1 add person $2",
                        productName,
                        newPersonName);
            }
        }
    }
    private static void activateNewPerson(Context context, DomainObject newPersonDomainObject) throws Exception {
        newPersonDomainObject.promote(context);
    }
    private static void checkBusiness(Context context, DomainObject parentPersonDO) throws Exception {
        try {
            String parentPersonName = parentPersonDO.getInfo(context, DomainObject.SELECT_NAME);

            Person parentPerson = new Person(parentPersonName);
            parentPerson.open(context);
            if (parentPerson.isSystemAdmin() || parentPerson.isBusinessAdmin()) {
                Person contextPerson =  new Person(context.getUser());
                contextPerson.open(context);
                if (contextPerson.isSystemAdmin() || contextPerson.isBusinessAdmin()) {
                    //continue
                } else {
                    throw new Exception("Parent person " + parentPersonName + " denied. Is system or business admin.");
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }
    private static String passwordGenerator(Context context) throws Exception {
        try {
            String allowSymbols = "23456789abdefhkmstvxyzABDEHKLMRSTVXYZ#!@()";
            String sMinLenght = "8";

            StringBuilder pwdBuilder = new StringBuilder();

            String pwdSettings = MQLCommand.exec(context, "print password");
            String[] pwdSettingsArray = pwdSettings.split("\n");

            for (String s : pwdSettingsArray) {
                if (s.startsWith("minimum length")) {
                    sMinLenght = s.split(" = ")[1];
                    break;
                }
            }

            int minLenght = Integer.parseInt(sMinLenght);

            Random random = new SecureRandom();
            for (int i = 0; i < minLenght; i++) {
                int index = random.nextInt(allowSymbols.length());
                pwdBuilder.append(allowSymbols.charAt(index));
            }

            return pwdBuilder.toString();
        } catch (Exception ex) {
            throw ex;
        }
    }
}
