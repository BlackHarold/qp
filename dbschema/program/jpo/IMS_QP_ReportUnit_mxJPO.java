import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import org.apache.log4j.Logger;

import java.util.Map;
import java.util.Vector;

public class IMS_QP_ReportUnit_mxJPO {

    private static final Logger LOG = Logger.getLogger("reportLogger");

    public Object getFileCheckinStatus(Context context, String... args) {
        Vector result = new Vector();
        StringBuilder stringBuilder = new StringBuilder();
        try {
            Map argsMap = null;
            try {
                argsMap = JPO.unpackArgs(args);
            } catch (Exception e) {
                LOG.error("error: " + e.getMessage());
                e.printStackTrace();
            }

            MapList objectList = (MapList) argsMap.get("objectList");
            boolean checkinStatus;
            String statusBar;
            for (Object o : objectList) {
                stringBuilder.setLength(0);
                Map map = (Map) o;
                String objectId = (String) map.get(DomainObject.SELECT_ID);
                if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                    DomainObject reportUnit = new DomainObject(objectId);
                    checkinStatus = "Ready".equals(reportUnit.getInfo(
                            context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_FILE_CHECKIN_STATUS));
                    statusBar = checkinStatus ? "100Green" : "010Red";
                    stringBuilder.append("<p align=\"center\"><img src=\"").append("../common/images/progressBar").append(statusBar).append(".gif\"/></p>");
                    result.addElement(stringBuilder.toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
}
