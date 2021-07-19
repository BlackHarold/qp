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
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMS_QP_SQP_Report_mxJPO {
    private static final Logger LOG = Logger.getLogger("reportLogger");

    public Map getReport(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");
        if (rowIDs.length == 0) {
            return null;
        }

        String[] cleanedIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            String[] rowIdArray = rowIDs[i].split("\\|");
            cleanedIDs[i] = rowIdArray[1];
        }

        /**sqp report generator*/
        BusinessObjectWithSelectList reportData =
                new IMS_QP_ListObjectsReportGenerator_mxJPO().reportGeneration(ctx, "SQP", cleanedIDs);
        createReportUnit(ctx, reportData, "SQP");

        return new HashMap();
    }

    public void createReportUnit(Context ctx, BusinessObjectWithSelectList reportData, String reportType) {

        String objectId = null;
        String reportName = null;
        String vault = ctx.getVault().getName();

        DomainObject reportsContainerObject = null, reportObject = null;

        try {
            MapList reportsByType = DomainObject.findObjects(ctx,
                    IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit,
                    IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    "name smatch *SQP*",
                    new StringList(DomainConstants.SELECT_ID));
            int reportsCount = reportsByType.size();

            BusinessObject boReportContainerObject = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_Reports, "Reports", "-", vault);
            reportsContainerObject = new DomainObject(boReportContainerObject);

            BusinessObject boReportUnit;
            do {
                reportName = "sqp_report_" + ++reportsCount;
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
                reportObject.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_FILE_CHECKIN_STATUS, "Not ready yet");
                IMS_KDD_mxJPO.connectIfNotConnected(ctx, IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit, reportsContainerObject, reportObject);
            }
            objectId = reportObject.getId(ctx);

        } catch (Exception e) {
            LOG.error("error connecting: " + IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit + "|" + e.getMessage());
        }

        //Get report
        Workbook workbook = createReport(ctx, reportData, reportType);

        //Write report to the file and upload file to the Business object
        if (UIUtil.isNotNullAndNotEmpty(reportName) && UIUtil.isNotNullAndNotEmpty(objectId)) {
            IMS_QP_CheckInOutFiles_mxJPO checkInOutFiles = new IMS_QP_CheckInOutFiles_mxJPO();
            File file = checkInOutFiles.writeToFile(ctx, reportName, workbook);
            boolean checkin = checkInOutFiles.checkIn(ctx, objectId, file);

            try {
                reportObject.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_FILE_CHECKIN_STATUS, "Ready");
            } catch (FrameworkException e) {
                LOG.error("error: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private int counter;
    private int greenCounter;
    Map<String, CellStyle> styles;

    private Workbook createReport(Context ctx, BusinessObjectWithSelectList reportData, String sheetName) {

        Workbook wb = null;
        try {
            wb = new XSSFWorkbook(IMS_QP_Constants_mxJPO.SQP_REPORT_TEMPLATE_PATH);
            styles = IMS_QP_ExcelUtil_mxJPO.getStyle(wb);
        } catch (IOException ioException) {
            LOG.error("IO exception: " + ioException.getMessage());
            ioException.printStackTrace();
        }

        Sheet sheet = wb.getSheet(sheetName);
        int lastRowCount = sheet.getLastRowNum();
        int pointCounter = 1;

        for (Object o : reportData) {
            BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;

            try {
                businessObject.open(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }

            Row row = sheet.createRow(lastRowCount);
            Cell cell;
            cell = row.createCell(0);
            cell.setCellStyle(styles.get("black11"));
            cell.setCellValue(pointCounter);

            cell = row.createCell(1);
            cell.setCellStyle(styles.get("black11left"));
            cell.setCellValue(businessObject.getSelectData("name"));

            /*SQP sheet specified zone*/
            cell = row.createCell(2);
            cell.setCellStyle(styles.get("black11left"));
            cell.setCellValue(businessObject.getSelectData(
                    String.format("to[%s].from.name",
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan)));
            cell = row.createCell(3);
            cell.setCellStyle(styles.get("black11left"));
            cell.setCellValue(businessObject.getSelectData(
                    String.format("to[%s].from.name",
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan)));

            RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);

            String[] plansCleanIDs = new String[]{businessObject.getSelectData(DomainObject.SELECT_ID)};
            getQPTaskList(businessObject.getSelectData(DomainObject.SELECT_ID), relItr);
            Map<String, String> taskMap = (Map<String, String>) new IMS_QP_PreparationStatement_Report_mxJPO()
                    .getPrepare(ctx, "SQP", plansCleanIDs);
            pushCellsValuesSQP(wb, row, taskMap);

            lastRowCount++;
            pointCounter++;

            try {
                businessObject.close(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return wb;
    }

    private void pushCellsValuesSQP(Workbook wb, Row row, Map<String, String> taskMap) {
        Cell cell;
        StringBuilder nameCellValueBuilder = new StringBuilder();
        StringBuilder codeCellValueBuilder = new StringBuilder();
        for (Object o : taskMap.entrySet()) {
            Map.Entry<String, String> entry = (Map.Entry) o;
            codeCellValueBuilder.append(entry.getKey()).append("\n");
            nameCellValueBuilder.append(entry.getValue()).append("\n");
        }

        /*percentage counting cell*/
        cell = row.createCell(4);
        counter = counter > 0 ? counter : 1;
        float mathResult = 100.0f * greenCounter / counter;
        cell.setCellStyle(
                Float.compare(mathResult, 100.0f) == 0 ? styles.get("green11left") : styles.get("red11left"));
        cell.setCellValue(String.format("%.1f", mathResult) + "%");

        /*code task cell*/
        cell = row.createCell(5);
        cell.setCellStyle(styles.get("red11left"));
        cell.setCellValue(codeCellValueBuilder.toString());
        /*name task cell*/
        cell = row.createCell(6);
        cell.setCellStyle(styles.get("red11left"));
        cell.setCellValue(nameCellValueBuilder.toString());
    }

    private Map<String, String> getQPTaskList(String taskId, RelationshipWithSelectItr relItr) {
        Map<String, String> map = new HashMap<>();
        counter = 0;
        greenCounter = 0;

        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();

            if (relSelect.getSelectData("name").equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask)) {
                //Increase counter
                counter++;

                boolean redFlag = true;

                if ("Full".equals(relSelect.getSelectData("to.attribute[IMS_QP_CloseStatus]"))) {
                    greenCounter++;
                    redFlag = false;
                }

                if (redFlag) {
                    map.put(relSelect.getSelectData("to.name"),
                            UIUtil.isNotNullAndNotEmpty(relSelect.getSelectData("to.attribute[IMS_NameRu]")) ?
                                    relSelect.getSelectData("to.attribute[IMS_NameRu]") :
                                    "-");
                }
            }
        }

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
        selectRelStmts.addElement("from.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.id");
        selectRelStmts.addElement("to.name");
        selectRelStmts.addElement("to.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.attribute[IMS_QP_CloseStatus]");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.name");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.current");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.type"); //check CL & VTZ
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.attribute[IMS_ProjDocStatus]");

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

    private RelationshipWithSelectItr getDEPRelationshipsWithIts(Context ctx, BusinessObjectWithSelect businessObject) {

        // Instantiating the BusinessObject
        StringList selectBusStmts = new StringList();
        selectBusStmts.addElement("id");
        selectBusStmts.addElement("type");
        selectBusStmts.addElement("name");

        StringList selectRelStmts = new StringList();
        selectRelStmts.addElement("name");
        selectRelStmts.addElement("from.id");
        selectRelStmts.addElement("from.name");
        selectRelStmts.addElement("from.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.id");
        selectRelStmts.addElement("to.name");
        selectRelStmts.addElement("to.attribute[IMS_NameRu]");
        selectRelStmts.addElement(
                String.format("to.from[%s].to.from[%s].to.attribute[IMS_ProjDocStatus]",
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2QPTask,
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        short recurse = 3;

        ExpansionWithSelect expansion = null;
        try {
            expansion = businessObject.expandSelect(ctx,
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2DEPProjectStage + "," +
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPProjectStage2DEPSubStage + "," +
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPSubStage2DEPTask,
                    "*", selectBusStmts, selectRelStmts, false, true, recurse);
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