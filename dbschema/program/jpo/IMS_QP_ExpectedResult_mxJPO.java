import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class IMS_QP_ExpectedResult_mxJPO {
    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public HashMap deleteExpectedResults(Context ctx, String... args) {
        HashMap mapMessage = new HashMap();

        //get all ids
        HashMap<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");

        String[] expectedResultIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            rowIDs[i] = rowIDs[i].substring(rowIDs[i].indexOf("|"), rowIDs[i].lastIndexOf("|"));
            expectedResultIDs[i] = rowIDs[i].substring(1, rowIDs[i].lastIndexOf("|"));
        }

        String[] rawString = rowIDs[0].split("\\|");
        String parentID = rawString[2];

        try {
            DomainObject parent = new DomainObject(parentID);

//            task selects
            StringList selects = new StringList(DomainConstants.SELECT_ID);
            selects.add(DomainConstants.SELECT_NAME);
            MapList relatedResults = parent.getRelatedObjects(ctx,
                    /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask,
                    /*type*/ IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                    /*object attributes*/ selects,
                    /*relationship selects*/ null,
                    /*getTo*/ true, /*getFrom*/ true,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ null,
                    /*relationship where*/ null,
                    /*limit*/ 0);

            Map<String, String> relatedExpectedResultIDs = new HashMap<>();
            for (int i = 0; i < relatedResults.size(); i++) {
                Map expectedResult = (Map) relatedResults.get(i);
                String expectedResultId = (String) expectedResult.get(DomainConstants.SELECT_ID);
                DomainObject domainObject = new DomainObject(expectedResultId);
                boolean isAloneOutput = domainObject.getInfoList(ctx, "to[IMS_QP_ExpectedResult2QPTask].from.id").size() == 1;

                if (!isAloneOutput) {
                    relatedExpectedResultIDs.put(
                            (String) expectedResult.get(DomainConstants.SELECT_ID),
                            (String) expectedResult.get(DomainConstants.SELECT_NAME));
                } else {
                    mapMessage.put(domainObject.getName(ctx), "You can't delete the alone 'output' for " + domainObject.getInfo(ctx, "to[IMS_QP_ExpectedResult2QPTask].from.name"));
                }
            }

            //            check table row ids
            for (int i = 0; i < expectedResultIDs.length; i++) {
                LOG.info("relatedExpectedResultIDs: " + relatedExpectedResultIDs);
                if (relatedExpectedResultIDs.containsKey(expectedResultIDs[i])) {
                    MqlUtil.mqlCommand(ctx, String.format("delete bus %s", expectedResultIDs[i]));
                } else {
                    DomainObject expectedResultObject = new DomainObject(expectedResultIDs[i]);
                    LOG.info("mapMessage: " + !mapMessage.containsKey(expectedResultObject.getName(ctx)) + "|" + mapMessage);
                    if (!mapMessage.containsKey(expectedResultObject.getName(ctx))) {
                        mapMessage.put(expectedResultObject.getName(ctx), "you can't delete the expected result of another owners.");
                        LOG.info("drop message: " + "you can't delete the expected result of another owners.");
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("error getting domain object: " + e.getMessage());
        }

        if (mapMessage.isEmpty()) {
            mapMessage.put("message", "status OK code 200");
        }

        return mapMessage;
    }

    public boolean checkStateAndOwnerOfExpectedResult(Context context, String... args) {
        Map argsMap;

        boolean result = false;
        try {
            argsMap = JPO.unpackArgs(args);
            String id = (String) argsMap.get("objectId");
            DomainObject object = new DomainObject(id);

            String type = object.getType(context);
            String state = "";

            // IMS_QP_DEPTask
            if ("IMS_QP_DEPTask".equals(type)) {
                result = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, object) ||
                        IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(context);
                state = object.getInfo(context, String.format("to[%s].from.to[%s].from.to[%s].from.current",
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPSubStage2DEPTask,
                        IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                        IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEP2DEPProjectStage));
            }

            // IMS_QP_QPTask
            else if ("IMS_QP_QPTask".equals(type)) {
                result = IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(context, id) ||
                        IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(context);
                state = object.getInfo(context, String.format("to[%s].from.current",
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask));
            }

            if (!"Draft".equals(state)) {
                return false;
            }

        } catch (Exception e) {
            LOG.error("security exception: " + e.getMessage());
        }

        return result;
    }
}
