import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MqlUtil;
import matrix.db.Context;
import org.apache.log4j.Logger;

public class IMS_QP_Triggers_mxJPO {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    /**
     * @param context usually parameter
     * @param args    usually parameter
     * @return Integer value. Usually used in triggers. '1' if something went wrong, '0' if the method passed ok
     * @throws Exception any errors thrown by the method
     * @see "TRIGGER: IMS_QP_QPlan_policyDraftPromoteCheck"
     */
    public int checkPromoteDone_IMS_QP_QPlan(Context context, String[] args) {
        String id = args[0];

        String relationship_Plan2Task = "IMS_QP_QPlan2QPTask";
        String relationship_Task2Task = "IMS_QP_QPTask2QPTask";
        String state = "IMS_QP_DEPTaskStatus";
        String taskStates;
        try {
            taskStates = MqlUtil.mqlCommand(context, String.format("print bus %s select from[%s].to.relationship[%s].attribute[%s] dump |",
                    id, relationship_Plan2Task, relationship_Task2Task, state));
        } catch (FrameworkException fe) {
            LOG.error("error get info form tasks: " + fe.getMessage());
            return 1;
        }

        boolean isQPOwner = IMS_QP_Security_mxJPO.isOwnerQPlan(context, id);
        String message = "";
        if (!isQPOwner || taskStates.contains("Draft")) {
            if (!isQPOwner) message = "is not qp owner";
            if (taskStates.contains("Draft")) message = "has 'Draft' relationships, check this";
            try {
                emxContextUtil_mxJPO.mqlWarning(context, message);
            } catch (Exception e) {
                LOG.error("error showing warning: " + e.getMessage());
            }
            return 1;
        } else {
            return 0;
        }
    }

    public int checkDemoteDraft_IMS_QP_QPlan(Context context, String[] args) {
        boolean isDEPOwner = false;
        try {
            String id = args[0];
            isDEPOwner = IMS_QP_Security_mxJPO.isOwnerDepFromQPPlan(context, id);
        } catch (Exception e) {
            LOG.error("exception security check: " + e.getMessage());
            return 1;
        }

        String message = "";
        if (!isDEPOwner) {
            try {
                message = "You haven't rights for Demote process, check this";
                emxContextUtil_mxJPO.mqlWarning(context, message);

            } catch (Exception e) {
                LOG.error("error showing warning: " + e.getMessage());
            }
        }

        return isDEPOwner ? 0 : 1;
    }
}
