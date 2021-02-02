import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.List;

public class IMS_QP_Task_Runnable_mxJPO implements Runnable {
    private static final Logger LOG = Logger.getLogger("IMS_QP_DEP");

    private StringBuilder stringBuilder = new StringBuilder();
    private List<String> loopingResult;

    private int counterGeneralIn = 0;
    private int counterDraftIn = 0;
    private int counterApprovedIn = 0;
    private int counterRejectedIn = 0;

    private String mainId;
    private String rawLink;

    public IMS_QP_Task_Runnable_mxJPO(String mainId, List<String> loopingResult) {
        this.mainId = mainId;
        this.loopingResult = loopingResult;
    }

    @Override
    public void run() {
        rawLink = doWork();
    }

    private String doWork() {
        for (String state : loopingResult) {
            incrementCounter(state);
        }

        stringBuilder.setLength(0);
        stringBuilder
                .append("<div align=\"right\" style=\"color: grey; font-size: .8em;\">")
                .append(counterGeneralIn)
                .append("</div>")
                .append("<div align=\"center\">")
                .append("<span style=\"color: green\">")
                .append(counterApprovedIn)
                .append("</span>")
                .append("<span style=\"color: #3264C8; padding: 0 .5em 0 .5em; font-size: 1.5em\"> ")
                .append(counterDraftIn)
                .append(" </span>")
                .append("<span style=\"color: red;\">")
                .append(counterRejectedIn)
                .append("</span>")
                .append("</div>");

        return stringBuilder.toString();

    }

    public String getRawLink() {
        return rawLink;
    }

    public String getMainId() {
        return mainId;
    }

    private void incrementCounter(String state) {
        switch (state) {
            case "Draft":
                counterDraftIn++;
                break;
            case "Approved":
                counterApprovedIn++;
                break;
            case "Rejected":
                counterRejectedIn++;
                break;
        }
        counterGeneralIn++;
    }

    public StringList getStringList(Object object) {
        StringList list = new StringList();
        try {
            if (object instanceof String) {
                list = new StringList((String) object);
            }
            if (object instanceof StringList) {
                list = (StringList) object;
            }
        } catch (ClassCastException cce) {
            LOG.info("error getting StringList: " + object.getClass().getCanonicalName() + " message: " + cce.getMessage());
        }
        return list;
    }
}
