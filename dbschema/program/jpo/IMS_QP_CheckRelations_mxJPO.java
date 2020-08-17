import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.Relationship;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;


public class IMS_QP_CheckRelations_mxJPO {

    Logger LOG = Logger.getLogger("blackLogger");
    public static final String BELL = "\u0007";

    public String checkRelations(Context context, String[] args) {

        StringList selects = new StringList("id");
        selects.add("name");
        selects.add(IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID);
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

                String depId = UIUtil.isNotNullAndNotEmpty((String) map.get(IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID)) ?
                        (String) map.get(IMS_QP_Constants_mxJPO.TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID) : "not found";
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
                                LOG.info("error scenario: " + depId + "|" + id + "|" + listToId.get(i) + "|result: " + depContainTasks.contains(listToId.get(i)));
                            }
                        } catch (Exception e) {
                            LOG.info("err: " + e.getMessage());
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
}
