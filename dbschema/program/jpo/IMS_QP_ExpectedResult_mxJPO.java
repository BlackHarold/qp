import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_ExpectedResult_mxJPO {
    private static final Logger LOG = Logger.getLogger("blackLogger");

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

        LOG.info("parentID: " + parentID);
        LOG.info("rawString: " + Arrays.deepToString(expectedResultIDs));

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
                relatedExpectedResultIDs.put(
                        (String) expectedResult.get(DomainConstants.SELECT_ID),
                        (String) expectedResult.get(DomainConstants.SELECT_NAME)
                );
            }

//            check table row ids
            String message = "you can't delete expected result another owners.";
            for (int i = 0; i < expectedResultIDs.length; i++) {
                if (relatedExpectedResultIDs.containsKey(expectedResultIDs[i])) {
                    LOG.info("MqlUtil.mqlCommand(ctx, " + String.format("delete bus %s", expectedResultIDs[i]) + ");");
                    MqlUtil.mqlCommand(ctx, String.format("delete bus %s", expectedResultIDs[i]));
                } else {
                    DomainObject expectedResultObject = new DomainObject(expectedResultIDs[i]);
                    LOG.info("er id: " + expectedResultIDs[i]);
                    mapMessage.put(expectedResultObject.getName(ctx),message);
                }
            }

        } catch (Exception e) {
            LOG.error("error getting domain object: " + e.getMessage());
        }

        if (mapMessage.isEmpty()) {
            mapMessage.put("message", "status OK code 200");
        }

        LOG.info("map message: " + mapMessage);
        return mapMessage;
    }
}
