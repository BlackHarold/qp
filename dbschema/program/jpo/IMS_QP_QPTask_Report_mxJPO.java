import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Feature #55924
 */
public class IMS_QP_QPTask_Report_mxJPO extends IMS_QP_ActualPlanSearch_mxJPO {
    private static final Logger LOG = Logger.getLogger("reportLogger");
    private final int ROW_LIMIT = 1048575;

    private String getTimeStamp() {
        return new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(Calendar.getInstance().getTime());
    }

    public void getReport(Context ctx, String... args) {

        /**task report generator*/
        LOG.info("start report timing : " + getTimeStamp());
        BusinessObjectWithSelectList reportData = getAllQPTaskList(ctx);
        createReportUnit(ctx, reportData, "QPTask");
    }

    public synchronized void createReportUnit(Context ctx, BusinessObjectWithSelectList reportData, String reportType) {

        String objectId = null;
        String reportName = null;
        String vault = ctx.getVault().getName();

        DomainObject reportsContainerObject = null, reportObject = null;

        try {
            MapList reportsByType = DomainObject.findObjects(ctx,
                    IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit,
                    IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    "name smatch *QPTask*",
                    new StringList(DomainConstants.SELECT_ID));
            int reportsCount = reportsByType.size();

            BusinessObject boReportContainerObject = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_Reports, "Reports", "-", ctx.getVault().getName());
            reportsContainerObject = new DomainObject(boReportContainerObject);

            BusinessObject boReportUnit;
            do {
                reportName = "qp_task_report_" + ++reportsCount;
                boReportUnit = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit, reportName, "-", vault);
            } while (boReportUnit.exists(ctx));

            reportObject = DomainObject.newInstance(ctx);
            reportObject.createObject(ctx,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit,
                    /*name*/reportName,
                    /*revision*/"-",
                    /*policy*/IMS_QP_Constants_mxJPO.policy_IMS_QP_ReportUnit,
                    /*vault*/ vault);
        } catch (FrameworkException frameworkException) {
            LOG.error("Framework exception: " + frameworkException.getMessage());
            frameworkException.printStackTrace();
        } catch (MatrixException matrixException) {
            LOG.error("Matrix exception: " + matrixException.getMessage());
            matrixException.printStackTrace();
        }

        try {
            if (reportsContainerObject != null && reportObject != null) {
                IMS_KDD_mxJPO.connectIfNotConnected(ctx, IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit, reportsContainerObject, reportObject);
            }
            objectId = reportObject.getId(ctx);
            reportObject.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_FILE_CHECKIN_STATUS, "Not ready yet");
        } catch (Exception e) {
            LOG.error("error connecting: " + IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit + "|" + e.getMessage());
        }

        //Get report
        SXSSFWorkbook workbook = createReport(ctx, reportData, reportType);

        //Write report to the file and upload file to the Business object
        if (UIUtil.isNotNullAndNotEmpty(reportName) && UIUtil.isNotNullAndNotEmpty(objectId)) {

            IMS_QP_CheckInOutFiles_mxJPO checkInOutFiles = new IMS_QP_CheckInOutFiles_mxJPO();
            File file = checkInOutFiles.writeToFile(ctx, reportName, workbook);
            workbook.dispose();

            boolean checkin = checkInOutFiles.checkIn(ctx, objectId, file);
            LOG.info(objectId + "checkin result: " + checkin);

            try {
                reportObject.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_FILE_CHECKIN_STATUS, "Ready");
            } catch (FrameworkException e) {
                LOG.error("error: " + e.getMessage());
                e.printStackTrace();
            }
        }
        LOG.info("finished report timing: " + getTimeStamp());
    }

    private String getWorkspacePath(Context ctx) {
        String workspace = "";
        try {
            workspace = ctx.createWorkspace();
        } catch (MatrixException matrixException) {
            matrixException.printStackTrace();
        }
        return workspace;
    }

    private SXSSFWorkbook createReport(Context ctx, BusinessObjectWithSelectList reportData, String sheetName) {

        SXSSFWorkbook wb = null;
        try {
            wb = new SXSSFWorkbook(new XSSFWorkbook(), 100000);
        } catch (Exception e) {
            LOG.error("IO exception: " + e.getMessage());
            e.printStackTrace();
        }

        Sheet sheet = null;
        int pointCounter = 1;
        for (Object o : reportData) {
            BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;

            Map<String, Map<String, String>> taskMap = new HashMap<>();
            try {
                businessObject.open(ctx);
                if (businessObject.isOpen()) {
                    RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);
                    taskMap = getQPTaskList(
                            businessObject.getSelectData(DomainObject.SELECT_ID),
                            businessObject.getSelectData(DomainObject.SELECT_NAME),
                            relItr);
                }

            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }

            if (sheet == null) {
                sheet = wb.createSheet(businessObject.getSelectData(DomainObject.SELECT_NAME));
            }

            if (!taskMap.isEmpty()) {
                sheet = pushCellsValues(pointCounter, wb, sheet, taskMap);
            }

            try {
                businessObject.close(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }

            pointCounter++;
        }

        return wb;
    }

    private Sheet pushCellsValues(int pointCounter, Workbook wb, Sheet sheet, Map<String, Map<String, String>> taskMap) {

        int rowNum = sheet.getLastRowNum();
        if (rowNum < 2) rowNum = 2;
        LOG.info("rowNum: " + rowNum);
        Row row = sheet.createRow(rowNum);

        //set number of parent task
        Cell cell = row.createCell(0);
        cell.setCellValue(pointCounter);

        for (Object o : taskMap.entrySet()) {
            Map.Entry<String, Map<String, String>> entry = (Map.Entry) o;
            String taskName = entry.getKey();

            Map<String, String> relatedTasksMap = entry.getValue();
            for (Object relatedMap : relatedTasksMap.entrySet()) {

                //set task name
                cell = row.createCell(1);
                cell.setCellValue(taskName);

                Map.Entry<String, String> tempMap = (Map.Entry<String, String>) relatedMap;

                //set to.name task
                cell = row.createCell(2);
                cell.setCellValue(tempMap.getKey());

                //set attribute status relationship
                cell = row.createCell(3);
                cell.setCellValue(tempMap.getValue().substring(0, tempMap.getValue().indexOf("|")));

                //set from dep
                cell = row.createCell(4);
                cell.setCellValue(tempMap.getValue().substring(tempMap.getValue().indexOf("|") + 1));

                ++rowNum;

                //check limit rows
                if (rowNum >= ROW_LIMIT) {
                    sheet = wb.createSheet(taskName);
                    rowNum = 2;
                }

                row = sheet.createRow(rowNum);
            }
        }

        return sheet;
    }

    private CellStyle getStyle(Workbook wb, String styleParam) {
        CellStyle cellStyle = wb.createCellStyle();
        Font font;
        switch (styleParam) {
            case "wrap":
                cellStyle.setWrapText(true);
                break;
            case "wrap8":
                cellStyle.setWrapText(true);
                font = wb.createFont();
                font.setFontHeightInPoints((short) 8);
                cellStyle.setFont(font);
                break;
            case "wrap8red":
                cellStyle.setWrapText(true);
                font = wb.createFont();
                font.setFontHeightInPoints((short) 8);
                font.setColor((short) 10);
                cellStyle.setFont(font);
                break;
        }
        return cellStyle;
    }

    private Map<String, Map<String, String>> getQPTaskList(String taskId, String taskName, RelationshipWithSelectItr relItr) {
        Map<String, Map<String, String>> map = new HashMap<>();
        Map<String, String> taskInfo = new HashMap<>();

        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();
            if (relSelect.getSelectData("name").equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask) &&
                    taskId.equals(relSelect.getSelectData("from.id"))) {

                taskInfo.put(
                        relSelect.getSelectData("to.name"),
                        String.format("%s|%s",
                                relSelect.getSelectData("attribute[IMS_QP_DEPTaskStatus]"),
                                relSelect.getSelectData("to.to[IMS_QP_QPlan2QPTask].from.to[IMS_QP_DEP2QPlan].from.name")));
            }
        }
        map.put(taskName, taskInfo);
        return map;
    }

    private RelationshipWithSelectItr getRelationshipsWithItr(Context ctx, BusinessObjectWithSelect businessObject) {

        // Instantiating the BusinessObject
        StringList selectBusStmts = new StringList();
        selectBusStmts.addElement("id");
        selectBusStmts.addElement("type");
        selectBusStmts.addElement("name");

        StringList selectRelStmts = new StringList();
        selectRelStmts.addElement("name");
        selectRelStmts.addElement("from.id");
        selectRelStmts.addElement("from.name");
        selectRelStmts.addElement("to.id");
        selectRelStmts.addElement("to.name");
        selectRelStmts.addElement("attribute[IMS_QP_DEPTaskStatus]");
        selectRelStmts.addElement("to.to[IMS_QP_QPlan2QPTask].from.to[IMS_QP_DEP2QPlan].from.name");

        ExpansionWithSelect expansion = null;
        try {
            expansion = businessObject.expandSelect(ctx,
                    "*", "*", selectBusStmts, selectRelStmts, true, true, (short) 1);
            // Getting the expansion
            //--------------------------------------------------------------
            //  _object.expandSelect(_context, - Java context object
            //  "*",                           - relationship Pattern
            //  "*",                           - type Pattern
            //  selectBusStmts,                - selects for Business Objects
            //  selectRelStmts,                - selects for Relationships
            //  true,                          - get To relationships
            //  true,                          - get From relationships
            //  recurse);                      - recursion level (0 = all)
            //--------------------------------------------------------------

        } catch (MatrixException e) {
            LOG.error("matrix error: " + e.getMessage());
            e.printStackTrace();
        }
        // Getting Relationships
        RelationshipWithSelectList relationshipWithSelectList = expansion.getRelationships();
        RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(relationshipWithSelectList);
        return relItr;
    }
}
