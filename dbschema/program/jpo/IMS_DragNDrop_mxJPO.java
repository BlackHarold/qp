import com.matrixone.apps.domain.util.FrameworkException;
import matrix.db.Context;
import matrix.util.StringList;
import org.apache.commons.lang3.StringEscapeUtils;

import java.util.Map;

@SuppressWarnings("unchecked")
public class IMS_DragNDrop_mxJPO {

    @SuppressWarnings("unused")
    public IMS_DragNDrop_mxJPO(Context context, String[] args) throws Exception {
    }

    public static String getConnectDropAreaHTML(
            String connectProgram, String connectFunction,
            String relationship, boolean isFromTarget,
            String rowId, String targetId,
            String onConnected, String allowedSources,
            String checkboxesColumn,
            String dropMessageHTML, String width, String height, String margin, String fontSize)
            throws FrameworkException {

        String areaId = String.format("dropArea-%s-%s-%s", relationship, rowId, targetId);
        String spinnerId = String.format("spinner-%s-%s-%s", relationship, rowId, targetId);

        return String.format(
                "<div id=\"%s\" style=\"width: %s; height: %s; text-align: center; margin: %s; display: none;\">" +
                        "<div style=\"display: table-cell; vertical-align: middle;\">" +
                        "<div>" + "<img src=\"%sspinner_26x26.png\" />" + "</div>" +
                        "</div>" +
                        "</div>",
                StringEscapeUtils.escapeHtml4(spinnerId),
                StringEscapeUtils.escapeHtml4(width),
                StringEscapeUtils.escapeHtml4(height),
                StringEscapeUtils.escapeHtml4(margin),
                IMS_KDD_mxJPO.COMMON_IMAGES) +
                //"<br />" +
                String.format(
                        "<div id=\"%1$s\" class=\"dropArea\" style=\"width: %14$s; height: %8$s; margin: %15$s; display: table; border-collapse: separate;\" " +
                                "ondragover=\"IMS_DragNDrop_onDrag(event, '%6$s', '%13$s', '%5$s', '%11$s')\" " +
                                "ondragleave=\"IMS_DragNDrop_onDrag(event, '%6$s', '%13$s', '%5$s', '%11$s')\" " +
                                "ondrop=\"IMS_DragNDrop_connect('%3$s', '%4$s', '%5$s', event, '%6$s', %7$s, '%13$s', %9$s, '%10$s', '%11$s')\">" +
                                "<div style=\"display: table-cell; vertical-align: middle; line-height: inherit; font: inherit; color: inherit;\">" +
                                "<div style=\"line-height: inherit; font: inherit; color: inherit; font-size: %12$s !important\">" + dropMessageHTML + "</div>" +
                                "</div>" +
                                "</div>",
                        /*01*/StringEscapeUtils.escapeHtml4(areaId),
                        /*02*/StringEscapeUtils.escapeEcmaScript(areaId),
                        /*03*/StringEscapeUtils.escapeEcmaScript(connectProgram),
                        /*04*/StringEscapeUtils.escapeEcmaScript(connectFunction),
                        /*05*/StringEscapeUtils.escapeEcmaScript(targetId),
                        /*06*/StringEscapeUtils.escapeEcmaScript(relationship),
                        /*07*/isFromTarget,
                        /*08*/StringEscapeUtils.escapeHtml4(height),
                        /*09*/onConnected != null && !onConnected.isEmpty() ? onConnected : "",
                        /*10*/allowedSources != null && !allowedSources.isEmpty() ? allowedSources : "",
                        /*11*/checkboxesColumn != null && !checkboxesColumn.isEmpty() ? StringEscapeUtils.escapeEcmaScript(checkboxesColumn) : "",
                        /*12*/fontSize,
                        /*13*/StringEscapeUtils.escapeEcmaScript(rowId),
                        /*14*/StringEscapeUtils.escapeHtml4(width),
                        /*15*/StringEscapeUtils.escapeHtml4(margin));
    }

    public static String getConnectDropAreaHTML(
            String connectProgram, String connectFunction,
            String relationship, boolean isFromTarget,
            String rowId, String targetId,
            String onConnected, String allowedSources,
            String checkboxesColumn,
            String dropMessageHTML, String height, String fontSize)
            throws FrameworkException {

        return getConnectDropAreaHTML(
                connectProgram, connectFunction,
                relationship, isFromTarget,
                rowId, targetId,
                onConnected, allowedSources,
                checkboxesColumn,
                dropMessageHTML, "98%", height, "auto", fontSize);
    }

    public static String getConnectDropAreaHTML(
            String connectProgram, String connectFunction,
            String relationship, boolean isFromTarget,
            String rowId, String targetId,
            String onConnected, String allowedSources,
            String dropMessageHTML, String height, String fontSize)
            throws FrameworkException {

        return getConnectDropAreaHTML(
                connectProgram, connectFunction,
                relationship, isFromTarget,
                rowId, targetId,
                onConnected, allowedSources,
                "",
                dropMessageHTML, height, fontSize);
    }

    public static String getConnectDropAreaHTML(
            String connectProgram, String connectFunction,
            String relationship, boolean isFromTarget,
            String rowId, String targetId,
            String allowedSources,
            String checkboxesColumn,
            String dropMessageHTML)
            throws FrameworkException {

        return getConnectDropAreaHTML(
                connectProgram, connectFunction,
                relationship, isFromTarget,
                rowId, targetId,
                String.format("function(){emxEditableTable.refreshRowByRowId('%s');}", rowId), allowedSources, checkboxesColumn,
                dropMessageHTML, "26px", "9px");
    }

    public static String getConnectDropAreaHTML(
            String connectProgram, String connectFunction,
            String relationship, boolean isFromTarget,
            String rowId, String targetId,
            String allowedSources,
            String dropMessageHTML)
            throws FrameworkException {

        return getConnectDropAreaHTML(
                connectProgram, connectFunction,
                relationship, isFromTarget,
                rowId, targetId,
                allowedSources,
                "",
                dropMessageHTML);
    }

    public static String getConnectDropAreaHTML(
            String connectProgram, String connectFunction,
            String relationship, boolean isFromTarget,
            String rowId, String targetId,
            String dropMessageHTML)
            throws FrameworkException {

        return getConnectDropAreaHTML(
                connectProgram, connectFunction,
                relationship, isFromTarget,
                rowId, targetId,
                "",
                dropMessageHTML);
    }

    @SuppressWarnings("unused")
    public static StringList getDropAreaCellStyle(Context context, String[] args) throws Exception {
        StringList styles = new StringList();
        for (Map ignored : IMS_KDD_mxJPO.getObjectListMaps(args)) {
            styles.add("IMS_VerticalAlignTop");
        }
        return styles;
    }
}