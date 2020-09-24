import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;


/**
 * It is the utility class whose methods help to obtain additional capabilities for extracting data from the database,
 * comparing and normalizing the provided information. see method descriptions for more details.
 */
public class IMS_QP_CheckRelations_mxJPO {

    Logger LOG = Logger.getLogger("blackLogger");
    public static final String BELL = "\u0007";

    /**
     * The method checks all objects of the IMS_QP_DEPTask type in the database and gets information about the relationships.
     * Then each object is checked for the presence of "steps" among themselves and their status.
     * Condition: all steps related to each other are listed as "internal" and the links between them must go to the 'Approved' status.
     *
     * @param context Basic parameter
     * @param args    Basic parameter
     * @return String 'success' if all stages are successful. This message will appear in the calling JSP.
     */
    public String checkRelations(Context context, String[] args) {

        StringList selects = new StringList("id");
        selects.add("name");
        selects.add(IMS_QP_Constants_mxJPO.DEP_ID_FOR_TASK);
        selects.add("to[IMS_QP_DEPTask2DEPTask].from.id");
        selects.add("to[IMS_QP_DEPTask2DEPTask].id");
        selects.add("from[IMS_QP_DEPTask2DEPTask].to.id");
        selects.add("from[IMS_QP_DEPTask2DEPTask].id");

        MapList allTasks;

        try {
            allTasks = DomainObject.findObjects(context,/*type*/ "IMS_QP_DEPTask", "*", "", selects);

            Map<String, List<String>> allDEPs = getAllDEPs(context);

            for (Object o : allTasks) {
                Map map = (Map) o;
                String id = (String) map.get("id");

                String depId = UIUtil.isNotNullAndNotEmpty((String) map.get(IMS_QP_Constants_mxJPO.DEP_ID_FOR_TASK)) ?
                        (String) map.get(IMS_QP_Constants_mxJPO.DEP_ID_FOR_TASK) : "not found";
                depId = depId.contains(BELL) ? depId.substring(0, depId.indexOf(BELL)) : depId;

                if (map.containsKey("to[IMS_QP_DEPTask2DEPTask].from.id")) {
                    String rawToId = (String) map.get("to[IMS_QP_DEPTask2DEPTask].from.id");
                    String rawToRelationshipId = (String) map.get("to[IMS_QP_DEPTask2DEPTask].id");
                    String[] rawToIdArray = rawToId.split(BELL);
                    String[] rawToRelationshipIdArray = rawToRelationshipId.split(BELL);
                    List<String> listToId = Arrays.asList(rawToIdArray);
                    List<String> listRelToId = Arrays.asList(rawToRelationshipIdArray);

                    for (int i = 0; i < listToId.size(); i++) {
                        try {
                            //search task in dep map
                            List<String> depContainTasks = allDEPs.get(depId) != null ? allDEPs.get(depId) : new ArrayList<>();
                            //search by id of related task they rel id
                            try {
                                if (!depId.equals("not found") && depContainTasks.contains(listToId.get(i))) {
                                    //get id related task
                                    String relationshipID = listRelToId.get(i);
                                    //change status on to "Approved"
                                    DomainRelationship relationship = new DomainRelationship(relationshipID);
                                    relationship.setAttributeValue(context, "IMS_QP_DEPTaskStatus", "Approved");
                                    LOG.info("DEP:" + new DomainObject(depId).getName(context) + "|TASK:" + new DomainObject(id).getName(context) + "|TASK:" + new DomainObject(listToId.get(i)).getName(context) + "|RESULT:" + depContainTasks.contains(listToId.get(i)) + "|COMMENT:status changed to: " + relationship.getAttributes(context));
                                }


                            } catch (Exception e) {
                                LOG.error("error scenario: " + depId + "|" + id + "|" + listToId.get(i) + "|result: " + depContainTasks.contains(listToId.get(i)));
                            }
                        } catch (Exception e) {
                            LOG.error("err: " + e.getMessage());
                        }
                    }
                }
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
            return "error in method";
        }
        return "success";
    }

    /**
     * The helper method used in the checkRelations method provides information about all associated "steps" in relation
     * to objects of type IMS_QP_DEP
     *
     * @param context Base parameter
     * @return
     * @throws FrameworkException
     */
    private Map getAllDEPs(Context context) throws FrameworkException {
        Map<String, List<String>> deps = new HashMap<>();

        StringList selects = new StringList("id");
        selects.add("from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.from[IMS_QP_DEPSubStage2DEPTask].to.id");
        MapList listDeps = DomainObject.findObjects(context, "IMS_QP_DEP", "*", "", selects);

        for (Object o : listDeps) {
            Map map = (Map) o;
            String id = (String) map.get("id");

            if (map.containsKey("from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.from[IMS_QP_DEPSubStage2DEPTask].to.id")) {
                String rawAllRelatedTasks = (String) map.get("from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.from[IMS_QP_DEPSubStage2DEPTask].to.id");

                String[] rawAllRelatedTasksArray = rawAllRelatedTasks.split(BELL);
                List<String> allRelatedTasks = Arrays.asList(rawAllRelatedTasksArray);
                deps.put(id, allRelatedTasks);
            }
        }
        return deps;
    }


    /**
     * The method allows you to output information about all objects of the IMS_QP_DEP type to the log file.
     * Used as a reference when you need to check for the presence of well-organized links in the database.
     *
     * @param context Base parameter
     * @param args    Base parameter
     * @return
     */
    public String checkDepRelations(Context context, String[] args) {

        String id = "id";
        String name = "name";
        String qp = "to[IMS_QP_QP2DEP].from.name";
        String discipline = "to[IMS_QP_Discipline2DEP].from.name";
        String projectStage = "from[IMS_QP_DEP2DEPProjectStage].to.name";
        String subStage = "from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.name";
        String tasks = "from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.from[IMS_QP_DEPSubStage2DEPTask].to.name";
        String depTask = "to[IMS_QP_DEP2DEPTask].from.name";
        String depProjectStage = "from[IMS_QP_DEP2DEPProjectStage].to.to[IMS_QP_ProjectStage2DEPProjectStage].from.name";

        StringList selects = new StringList(id);
        selects.add(name);
        selects.add(qp);
        selects.add(discipline);
        selects.add(projectStage);
        selects.add(subStage);
        selects.add(tasks);
        selects.add(depTask);
        selects.add(depProjectStage);

        MapList listDeps = new MapList();
        try {
            listDeps = DomainObject.findObjects(context, "IMS_QP_DEP", "*", "", selects);
        } catch (FrameworkException fe) {
            LOG.error("framework exception: " + fe.getMessage());
        }

        if (!listDeps.isEmpty()) {

            StringBuilder sb = new StringBuilder();
            sb.append("id|name|qp|discipline|project stage|substages|all tasks|dep tasks|project directory");
            LOG.info(sb.toString());

            for (Object object : listDeps) {
                Map map = (Map) object;
                sb.setLength(0);
                sb.append(map.get(id));
                sb.append("|" + map.get(name));
                sb.append("|" + map.get(qp));
                sb.append("|" + map.get(discipline));
                sb.append("|" + map.get(projectStage));
                sb.append("|" + map.get(subStage));
                sb.append("|" + map.get(tasks));
                sb.append("|" + map.get(depTask));
                sb.append("|" + map.get(depProjectStage));

                String result = sb.toString();
                result = result.replace(BELL, ",");
                LOG.info(result);
            }


        }
        return EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.resultCheckRelations");
    }


    /**
     * TODO develop for the future
     * @param id
     * @return
     */
    private String checkDep(String id) {
        return "";
    }


    /**
     * It takes command lines from the presented file, then each of the lines executes them using MQL commands.
     * An unlimited number of commands can be transmitted, for each command, in case of failure,
     * meaningful information is displayed in the log file.
     *
     * @param context Base parameter
     * @param args    Base parameter
     * @return
     */
    public String connectPBSToSystems(Context context, String... args) {

        FileReader fileReader;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(new File(EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.resultCheckRelations")));
            bufferedReader = new BufferedReader(fileReader);
            String command = bufferedReader.readLine();
            while (command != null) {
                LOG.info(command);
                try {
                    /*String result = */
                    MqlUtil.mqlCommand(context, command);
                } catch (FrameworkException e) {
                    LOG.error("FrameworkException: " + e.getMessage());
                    e.printStackTrace();
                }
                command = bufferedReader.readLine();
            }

        } catch (IOException e) {
            LOG.error("exception: " + e.getMessage());
            return EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.internalServerError");
        } finally {
            if (bufferedReader != null)
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LOG.error("buffer error: " + e.getMessage());
                    e.printStackTrace();
                }
        }

        return EnoviaResourceBundle.getProperty(context, "IMS_QP_FrameworkStringMessages", context.getLocale(), "IMS_QP_Framework.Message.passedOK");
    }
}