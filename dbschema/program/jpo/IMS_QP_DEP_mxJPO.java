import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.db.JPO;
import org.apache.log4j.Logger;

import java.util.Map;

public class IMS_QP_DEP_mxJPO {

    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    public String getInterdisciplinaryCheck(Context context, String... args) {

        String objectId;
        boolean interdisciplinary = false;

        try {
            Map argsMap = JPO.unpackArgs(args);
            Map requestMap = (Map) argsMap.get("requestMap");
            objectId = (String) requestMap.get("objectId");
            if (UIUtil.isNotNullAndNotEmpty(objectId)) {
                DomainObject depObject = new DomainObject(objectId);

                //getting attribute IMS_QP_InterdisciplinaryDEP state, default value is 'false'
                interdisciplinary = depObject.getInfo(context, IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_QP_INTERDISCIPLINARY_DEP).equals("TRUE");
            }
        } catch (Exception ex) {
            LOG.error("exception getting program map: " + ex.getMessage());
            ex.printStackTrace();
        }

        StringBuilder sb = new StringBuilder();

        // showing icons iconLicenseAvailable.gif or iconLicenseUnavailable.gif
        String available = interdisciplinary ? "Available" : "Unavailable";
        sb.append("<img src=\"../common/images/iconLicenseBox").append(available).append(".png\">");

        return sb.toString();
    }
}
