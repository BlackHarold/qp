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
import java.util.*;

public class IMS_QP_DEP_Report_mxJPO {
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

        /**dep report generator*/
        BusinessObjectWithSelectList reportData =
                new IMS_QP_ListObjectsReportGenerator_mxJPO().reportGeneration(ctx, "DEP", cleanedIDs);

        createReportUnit(ctx, reportData, "DEP");

        return new HashMap();
    }

    public void createReportUnit(Context ctx, BusinessObjectWithSelectList reportData, String reportType) {

        //Create object of ReportUnit type
        String objectId = null;
        String reportName = null;
        String vault = ctx.getVault().getName();
        DomainObject reportsContainerObject = null, reportObject = null;
        try {
            MapList reportsByType = DomainObject.findObjects(ctx,
                    IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit,
                    IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    "name smatch *DEP*",
                    new StringList(DomainConstants.SELECT_ID));
            int reportsCount = reportsByType.size();

            BusinessObject boReportContainerObject = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_Reports, "Reports", "-", ctx.getVault().getName());
            reportsContainerObject = new DomainObject(boReportContainerObject);

            BusinessObject boReportUnit;
            do {
                reportName = "dep_report_" + ++reportsCount;
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

    Map<List<String>, List<String>> tasksInfoMap = new LinkedHashMap<>();
    Map<String, CellStyle> styles;

    /**
     * Creating report by type with using a report business object data
     * *
     * @param ctx
     * @param reportData
     * @param sheetName
     * @return
     */

    private Workbook createReport(Context ctx, BusinessObjectWithSelectList reportData, String sheetName) {

        Workbook wb = null;

        try {
            wb = new XSSFWorkbook(IMS_QP_Constants_mxJPO.DEP_REPORT_TEMPLATE_PATH);
            styles = IMS_QP_ExcelUtil_mxJPO.getStyle(wb);
        } catch (IOException ioException) {
            LOG.error("IO exception: " + ioException.getMessage());
            ioException.printStackTrace();
        }

        Sheet sheet = wb.getSheet(sheetName);
        int lastRowCount = sheet.getLastRowNum();
        int pointCounter = 1;

        counter = 0;
        greenCounter = 0;

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

            /*DEP sheet specified zone*/
            RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);
            List<String> tasksInfo = getQPTaskList(businessObject.getSelectData(DomainObject.SELECT_ID), relItr);
            Object[] tasksInfoRaw = tasksInfo.toArray();
            String[] plansCleanIDs = new String[tasksInfoRaw.length];
            for (int i = 0; i < tasksInfoRaw.length; i++) {
                plansCleanIDs[i] = (String) tasksInfoRaw[i];
            }

            getInfoTasks(ctx, tasksInfo);
            Map<List<String>, List<String>> tasksInfoMap =
                    (Map<List<String>, List<String>>) new IMS_QP_PreparationStatement_Report_mxJPO()
                            .getPrepare(ctx, "DEP", plansCleanIDs);
            pushCellsValuesDEP(wb, row, tasksInfoMap);

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

    private Map<List<String>, List<String>> getInfoTasks(Context ctx, List<String> tasksInfo) {
        Map<List<String>, List<String>> tasksInfoMap = new HashMap<>();
        List<String> taskCodes = new ArrayList<>();
        List<String> taskNames = new ArrayList<>();

        for (String taskId : tasksInfo) {

            // Instantiate the BusinessObject.
            StringList selectBusStmts = new StringList(4);
            selectBusStmts.add("name");

            StringList selectRelStmts = new StringList(3);
            selectRelStmts.add("name");
            selectRelStmts.add("to");
            selectRelStmts.add("to.id");
            selectRelStmts.add("to.type");
            selectRelStmts.add("to.type");
            selectRelStmts.add("to.name");
            selectRelStmts.add("to.attribute[IMS_NameRu]");
            selectRelStmts.add("to.attribute[IMS_QP_CloseStatus]");
            selectRelStmts.add("to.from[IMS_QP_QPTask2Fact].to.name");
            selectRelStmts.add("to.from[IMS_QP_QPTask2Fact].to.current");
            selectRelStmts.add("to.from[IMS_QP_QPTask2Fact].to.type"); //check CL & VTZ
            selectRelStmts.add("to.from[IMS_QP_QPTask2Fact].to.attribute[IMS_ProjDocStatus]");

            BusinessObject taskObject = null;
            try {
                taskObject = new BusinessObject(taskId);
                taskObject.open(ctx);
            } catch (MatrixException matrixException) {
                matrixException.printStackTrace();
            }

            // Getting the expansion
            ExpansionWithSelect expansion = null;
            try {
                expansion = taskObject.expandSelect
                        (ctx, "*", "*", selectBusStmts, selectRelStmts, true, true, (short) 1);
            } catch (MatrixException matrixException) {
                matrixException.printStackTrace();
            }

            // Getting Relationships.
            RelationshipWithSelectList _relSelectList =
                    expansion.getRelationships();
            RelationshipWithSelectItr relItr = new
                    RelationshipWithSelectItr(_relSelectList);

            while (relItr.next()) {
                RelationshipWithSelect relSelect = relItr.obj();

                if (relSelect.getSelectData("name").equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask)) {

                    //Increase task counter
                    counter++;
                    boolean redFlag = true;

                    if ("Full".equals(relSelect.getSelectData("to.attribute[IMS_QP_CloseStatus]"))) {
                        greenCounter++;
                        redFlag = false;
                    }

                    if (redFlag) {
                        taskCodes.add("" + relSelect.getSelectData("to.name"));
                        taskNames.add("" + relSelect.getSelectData("to.attribute[IMS_NameRu]"));
                    }
                }
            }
        }

        tasksInfoMap.put(taskCodes, taskNames);

        return tasksInfoMap;
    }

    private void pushCellsValuesDEP(Workbook wb, Row row, Map<List<String>, List<String>> tasksInfo) {
        Cell cell;
        StringBuilder codeCellValueBuilder = new StringBuilder();
        StringBuilder nameCellValueBuilder = new StringBuilder();
        for (Map.Entry<List<String>, List<String>> entry : tasksInfo.entrySet()) {
            for (String code : entry.getKey()) {
                codeCellValueBuilder.append(code).append("\n");
            }
            for (String name : entry.getValue()) {
                name = UIUtil.isNotNullAndNotEmpty(name) ? name : " - ";
                nameCellValueBuilder.append(name).append("\n");
            }
        }

        /*percentage counting cell*/
        cell = row.createCell(2);
        counter = counter > 0 ? counter : 1;
        float mathResult = 100.0f * greenCounter / counter;
        cell.setCellStyle(
                Float.compare(mathResult, 100.0f) == 0 ? styles.get("green11left") : styles.get("red11left"));
        cell.setCellValue(String.format("%.1f", mathResult) + "%");

        if (tasksInfo != null && !tasksInfo.isEmpty()) {

            //Code task cell
            cell = row.createCell(3);
            cell.setCellStyle(styles.get("red11left"));
            cell.setCellValue(codeCellValueBuilder.length() > 0 ? codeCellValueBuilder.toString() : "-");

            //Name task cell
            cell = row.createCell(4);
            cell.setCellStyle(styles.get("red11left"));
            cell.setCellValue(nameCellValueBuilder.toString());
        }
    }

    private List<String> getQPTaskList(String taskId, RelationshipWithSelectItr relItr) {

        List<String> relatedTasks = new ArrayList<>();
        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();
            String relationshipType = relSelect.getSelectData("name");
            String relationshipFromId = relSelect.getSelectData("from.id");
            String relationshipToId = relSelect.getSelectData("to.id");
            String relationshipToName = relSelect.getSelectData("to.name");

            boolean from = relationshipFromId.equals(taskId);
            if (relationshipType.equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan) && from) {
                relatedTasks.add(relationshipToId);
            }
        }

        return relatedTasks;
    }

    private List<String> getRelationshipGroupReportSelects() {
        StringList selectRelStmts = new StringList();
        //Relationships
        selectRelStmts.addElement(String.format("to.from[%s].to.from[%s]",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));

        //Ids
        selectRelStmts.addElement(String.format("to.from[%s].to.id",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask));
        //Codes
        selectRelStmts.addElement(String.format("to.from[%s].to.name",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask));
        //States projDocStatus
        selectRelStmts.addElement(String.format("to.from[%s].to.from[%s].to.attribute[IMS_ProjDocStatus]",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        //Current states
        selectRelStmts.addElement(String.format("to.from[%s].to.from[%s].to.current",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        //Names (RU)
        selectRelStmts.addElement(String.format("to.from[%s].to.attribute[IMS_NameRu]",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask));

        return selectRelStmts;
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

        ///Group report select
        selectRelStmts.addAll(getRelationshipGroupReportSelects());

        selectRelStmts.addElement(
                String.format("to.from[%s].to.attribute[IMS_ProjDocStatus]",
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        selectRelStmts.addElement(
                String.format("from.from[%s].to.attribute[IMS_ProjDocStatus]",
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2Fact));
        short recurse = 1;

        ExpansionWithSelect expansion = null;
        try {
            expansion = businessObject.expandSelect(ctx,
                    "*", "*", selectBusStmts, selectRelStmts, true, true, recurse);
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
