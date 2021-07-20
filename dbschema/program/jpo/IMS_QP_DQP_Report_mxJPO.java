import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Feature #52039
 */
public class IMS_QP_DQP_Report_mxJPO {
    private static final Logger LOG = Logger.getLogger("reportLogger");

    public Map getMultiPageReport(Context ctx, String... args) {
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

        argsMap.put("emxTableRowId", cleanedIDs);

        Map report = null;
        try {
            report = getReport(ctx, JPO.packArgs(argsMap));
        } catch (Exception e) {
            LOG.error("exception packing args: " + e.getMessage());
            e.printStackTrace();
        }

        return report != null ? report : new HashMap();
    }

    public Map getReport(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String objectId = (String) argsMap.get("objectId");

        /**dqp report generator*/
        String[] rowIDs = null;
        if (argsMap.containsKey("emxTableRowId")) {
            rowIDs = (String[]) argsMap.get("emxTableRowId");
        }

        BusinessObjectWithSelectList reportData =
                new IMS_QP_ListObjectsReportGenerator_mxJPO().reportGeneration(ctx, "SQP",
                        rowIDs != null ? rowIDs : new String[]{objectId});
        LOG.info("report data: " + reportData);
        createReportUnit(ctx, reportData, rowIDs != null ? "DQP-M" : "DQP");

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
                    "name smatch *DQP*",
                    new StringList(DomainConstants.SELECT_ID));
            int reportsCount = reportsByType.size();

            BusinessObject boReportContainerObject = new BusinessObject(
                    IMS_QP_Constants_mxJPO.type_IMS_QP_Reports, "Reports", "-", vault);
            reportsContainerObject = new DomainObject(boReportContainerObject);

            BusinessObject boReportUnit;
            do {
                reportName = "dqp_report_" + ++reportsCount;
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
                IMS_KDD_mxJPO.connectIfNotConnected(ctx,
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit, reportsContainerObject, reportObject);
            }
            objectId = reportObject.getId(ctx);

        } catch (Exception e) {
            LOG.error(String.format("error connecting: %s message: %s",
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit, e.getMessage()));
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

        XSSFWorkbook wb = null;
        try {
            wb = new XSSFWorkbook(IMS_QP_Constants_mxJPO.DQP_REPORT_TEMPLATE_PATH);
            styles = IMS_QP_ExcelUtil_mxJPO.getStyle(wb);
        } catch (IOException ioException) {
            LOG.error("IO exception: " + ioException.getMessage());
            ioException.printStackTrace();
        }

        Sheet templateSheet = wb.getSheetAt(0);
        int pointCounter = 1;

        final String PASSWORD = "RickAndMortyRTHEBEST";
        String planName;
        for (Object o : reportData) {
            BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;

            try {
                businessObject.open(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }
            Row row;

            //copy template sheet and set name by the task
            Sheet sheet = wb.cloneSheet(0);
            planName = businessObject.getSelectData(DomainObject.SELECT_NAME);
            wb.setSheetName(wb.getSheetIndex(sheet), planName);

            //set password protection
            sheet.protectSheet(PASSWORD);

            row = sheet.getRow(1);

            Cell cell = row.getCell(1);
            cell.setCellStyle(styles.get("black8_align_left"));
            cell.setCellValue(
                    businessObject.getSelectData("from[IMS_QP_QPlan2Object].to.name") +
                            "\n("
                            + businessObject.getSelectData("from[IMS_QP_QPlan2Object].to.attribute[IMS_DescriptionEn]") +
                            "/"
                            + businessObject.getSelectData("from[IMS_QP_QPlan2Object].to.attribute[IMS_DescriptionRu]") +
                            ")"
            );

            //date
            row = sheet.getRow(2);
            cell = row.getCell(1);
            cell.setCellStyle(styles.get("black8_align_left"));
            cell.setCellValue(getTimeStamp());

            //qp name
            row = sheet.getRow(3);
            cell = row.getCell(1);
            cell.setCellStyle(styles.get("black8_align_left"));
            cell.setCellValue(businessObject.getSelectData("name"));

            //owners separated ','
            row = sheet.getRow(4);
            cell = row.getCell(1);
            cell.setCellStyle(styles.get("black8_align_left"));
            cell.setCellValue(getOwners(ctx, businessObject));

            //group classifier name, attributes names en/ru
            row = sheet.getRow(5);
            String classifier = UIUtil.isNotNullAndNotEmpty(
                    businessObject.getSelectData(String.format("to[%s].from.name",
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan))) ?
                    businessObject.getSelectData(String.format("to[%s].from.name",
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan))
                            + ", "
                            + businessObject.getSelectData(
                            String.format("to[%s].from.attribute[IMS_Name]",
                                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan))
                            + "/"
                            + businessObject.getSelectData(
                            String.format("to[%s].from.attribute[IMS_NameRu]",
                                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan))
                    : "No group";
            cell = row.getCell(1);
            cell.setCellStyle(styles.get("black8_align_left"));
            cell.setCellValue(classifier);

            //dep code, description
            row = sheet.getRow(6);
            cell.setCellStyle(styles.get("black8_align_left"));
            row.getCell(1).setCellValue(businessObject.getSelectData(String.format("to[%s].from.name",
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan))
                    + ", "
                    + businessObject.getSelectData(
                    String.format("to[%s].from.description",
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan))
            );

            //current qp state
            row = sheet.getRow(7);
            cell = row.getCell(1);
            cell.setCellStyle(styles.get("black8_align_left"));
            cell.setCellValue(businessObject.getSelectData(DomainConstants.SELECT_CURRENT));

            /*DQP sheet specified zone*/
            RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);
            Map<String, String> taskMap = getQPTaskList(
                    businessObject.getSelectData(DomainObject.SELECT_ID), relItr);
            Map<String, String> sortedMap = new TreeMap<>(taskMap);

            //starting data line
            int lastRowCount = 10;
            pushCellsValuesDQP(wb, sheet, planName, lastRowCount, sortedMap);
            pointCounter++;

            try {
                businessObject.close(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        //remove template sheet
        wb.removeSheetAt(wb.getSheetIndex(templateSheet));
        wb.lockStructure();
        return wb;
    }

    private String getTimeStamp() {
        return new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
    }

    private String getOwners(Context ctx, BusinessObjectWithSelect boWithSelect) {
        String s = "";
        try {
            String owners = MqlUtil.mqlCommand(ctx, String.format("print bus %s select %s dump |",
                    boWithSelect.getSelectData(DomainObject.SELECT_ID), "from[IMS_QP_QPlan2Owner].to.name"));
            List<String> ownersList = Arrays.asList(owners.split("\\|"));


            for (int i = 0; i < ownersList.size(); i++) {
                s += MqlUtil.mqlCommand(ctx, String.format("print person %s select fullname dump |", ownersList.get(i)));
                if (ownersList.size() - i > 1) {
                    s += ",\n";
                }
            }

        } catch (FrameworkException e) {
            e.printStackTrace();
        }
        return s;
    }

    private int pushCellsValuesDQP(Workbook wb, Sheet sheet, String planName, int rowCounter, Map<String, String> taskMap) {
        Row row;
        Cell cell;
        CellStyle style;

        //if tasks are not aligned by sort order
        if (taskMap.isEmpty()) {
            row = sheet.createRow(rowCounter++);
            cell = row.createCell(0);
            cell.setCellStyle(styles.get("red11left"));
            cell.setCellValue("To generate a DQP report, " +
                    "you need to correctly calculate the sequence of steps for " +
                    planName);
            return counter;
        }

        for (Map.Entry<String, String> map : taskMap.entrySet()) {
            String key = map.getKey();
            String value = map.getValue();

            row = sheet.createRow(rowCounter++);

            if (value.contains("\tFull\t")) {
                style = styles.get("black8_green_bgd");
            } else {
                style = styles.get("black8_no_wrap");
            }

            int cellCounter = 0;
            for (String s : value.split("\\t")) {
                cell = row.createCell(cellCounter++);
                cell.setCellStyle(style);
                cell.setCellValue(s);
            }

            //cell with empty document but style and '-' needs
            if (cellCounter == 9) {
                cell = row.createCell(cellCounter);
                cell.setCellStyle(style);
                cell.setCellValue("-");
            }

        }

        return counter;
    }

    private Map<String, String> getQPTaskList(String taskId, RelationshipWithSelectItr relItr) {
        Map<String, String> map = new HashMap<>();
        counter = 0;
        greenCounter = 0;

        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();

            String currentTaskState = relSelect.getSelectData("to.attribute[IMS_QP_CloseStatus]");

            if (relSelect.getSelectData("name").equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask)) {
                //Increase counter
                counter++;

                StringBuilder qpTaskData = new StringBuilder();

                //task code (name)
                qpTaskData.append(relSelect.getSelectData("to.name"))
                        .append("\t");

                //names en/ru
                qpTaskData.append(UIUtil.isNotNullAndNotEmpty(
                        relSelect.getSelectData("to.attribute[IMS_Name]")) ?
                        relSelect.getSelectData("to.attribute[IMS_Name]")
                                + " / " +
                                relSelect.getSelectData("to.attribute[IMS_NameRu]") : "-")
                        .append("\t");

                //descriptions en/ru
                qpTaskData.append(UIUtil.isNotNullAndNotEmpty(
                        relSelect.getSelectData("to.attribute[IMS_DescriptionEn]")) ?
                        relSelect.getSelectData("to.attribute[IMS_DescriptionEn]")
                                + " / "
                                + relSelect.getSelectData("to.attribute[IMS_DescriptionRu]") : "-")
                        .append("\t");

                //project stages
                String stage = relSelect.getSelectData("to." +
                        "to[IMS_QP_DEPTask2QPTask].from." +
                        "to[IMS_QP_DEPSubStage2DEPTask].from." +
                        "to[IMS_QP_DEPProjectStage2DEPSubStage].from." +
                        "to[IMS_QP_ProjectStage2DEPProjectStage].from.name");
                String stageLevel = relSelect.getSelectData("to." +
                        "to[IMS_QP_DEPTask2QPTask].from." +
                        "to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");
                stage += stage.equals("BD") ?
                        (UIUtil.isNotNullAndNotEmpty(stageLevel) ? " Stage " + stageLevel : "") : "";
                qpTaskData.append(stage)
                        .append("\t");

                //doc fact
                String docFactCode = currentTaskState.equals("Full") ?
                        relSelect.getSelectData("to." +
                                "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_QP_DocumentCode]") : "";
                qpTaskData.append(UIUtil.isNotNullAndNotEmpty(docFactCode) ? docFactCode : "-")
                        .append("\t");

                //output name en/ru
                qpTaskData.append(UIUtil.isNotNullAndNotEmpty(
                        relSelect.getSelectData("to." +
                                "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_Name]")) ?
                        relSelect.getSelectData("to." +
                                "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_Name]") : "-")
                        .append(" / ")
                        .append(UIUtil.isNotNullAndNotEmpty(
                                relSelect.getSelectData("to." +
                                        "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_NameRu]")) ?
                                relSelect.getSelectData("to." +
                                        "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_NameRu]") : "-")
                        .append("\t");

                //output description en/ru
                qpTaskData.append(UIUtil.isNotNullAndNotEmpty(
                        relSelect.getSelectData("to." +
                                "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_DescriptionEn]")) ?
                        relSelect.getSelectData("to." +
                                "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_DescriptionEn]") : "-")
                        .append(" / ")
                        .append(UIUtil.isNotNullAndNotEmpty(
                                relSelect.getSelectData("to." +
                                        "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_DescriptionRu]")) ?
                                relSelect.getSelectData("to." +
                                        "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_DescriptionRu]") : "-")
                        .append("\t");

                //expected result family
                String family = relSelect.getSelectData("to." +
                        "from[IMS_QP_ExpectedResult2QPTask].to.to[IMS_QP_ResultType2ExpectedResult].from.name");
                qpTaskData.append(family)
                        .append("\t");

                //task state
                qpTaskData.append(currentTaskState)
                        .append("\t");

                //doc codes inputs tasks
                String taskDocuments = relSelect.getSelectData("to." +
                        "to[IMS_QP_QPTask2QPTask].from." +
                        "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_QP_DocumentCode]");

                List<String> inputDocs;
                if (taskDocuments.contains(IMS_QP_Constants_mxJPO.BELL_DELIMITER)) {
                    inputDocs = Arrays.asList(taskDocuments.split(IMS_QP_Constants_mxJPO.BELL_DELIMITER));
                } else {
                    inputDocs = Arrays.asList(taskDocuments);
                }

                Iterator iterator = new HashSet<>(inputDocs).iterator();
                int count = inputDocs.size();
                while (iterator.hasNext()) {
                    String nextString = (String) iterator.next();
                    qpTaskData.append(UIUtil.isNotNullAndNotEmpty(nextString) ? nextString : "");
                    --count;
                    if (count > 1 && UIUtil.isNotNullAndNotEmpty(nextString)) {
                        qpTaskData.append(", ");
                    }
                }
                if (qpTaskData.substring(qpTaskData.length() - 2).contains(",")) {
                    qpTaskData = new StringBuilder(qpTaskData.substring(0, qpTaskData.length() - 2));
                }

                if (relSelect.getSelectData("to.attribute[IMS_SortOrder]").equals("0")) {
                    return new HashMap<>();
                }
                map.put(relSelect.getSelectData("to.attribute[IMS_SortOrder]"), qpTaskData.toString());
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
        selectBusStmts.addElement("from[IMS_QP_QPlan2Object].to.name");
        selectBusStmts.addElement("from[IMS_QP_QPlan2Object].to.attribute[IMS_DescriptionEn]");
        selectBusStmts.addElement("from[IMS_QP_QPlan2Object].to.attribute[IMS_DescriptionRu]");

        StringList selectRelStmts = new StringList();
        selectRelStmts.addElement("name");
        selectRelStmts.addElement("from.id");
        selectRelStmts.addElement("from.name");
        selectRelStmts.addElement("to.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.attribute[IMS_Name]");
        selectRelStmts.addElement("to.attribute[IMS_DescriptionEn]");
        selectRelStmts.addElement("to.attribute[IMS_DescriptionRu]");
        selectRelStmts.addElement("to.attribute[IMS_SortOrder]");
        selectRelStmts.addElement("to." +
                "to[IMS_QP_DEPTask2QPTask].from." +
                "to[IMS_QP_DEPSubStage2DEPTask].from." +
                "to[IMS_QP_DEPProjectStage2DEPSubStage].from." +
                "to[IMS_QP_ProjectStage2DEPProjectStage].from.name");
        selectRelStmts.addElement("to." +
                "to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");
        selectRelStmts.addElement("to.id");
        selectRelStmts.addElement("to.name");
        selectRelStmts.addElement("to.current");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2QPTask].to.name");
        selectRelStmts.addElement("to.to[IMS_QP_QPTask2QPTask].from.name");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2QPTask].attribute[IMS_QP_DEPTaskStatus]");
        selectRelStmts.addElement("to.to[IMS_QP_QPTask2QPTask].attribute[IMS_QP_DEPTaskStatus]");
        selectRelStmts.addElement("to." +
                "from[IMS_QP_QPTask2QPTask].to." +
                "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_QP_DocumentCode]");
        selectRelStmts.addElement("to." +
                "to[IMS_QP_QPTask2QPTask].from." +
                "from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_QP_DocumentCode]");
        selectRelStmts.addElement("to.from[IMS_QP_ExpectedResult2QPTask].to.name");
        selectRelStmts.addElement("to.to[IMS_QP_ExpectedResult2QPTask].from.attribute[IMS_QP_DocumentCode]");
        selectRelStmts.addElement("to.from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_Name]");
        selectRelStmts.addElement("to.to[IMS_QP_ExpectedResult2QPTask].from.attribute[IMS_Name]");
        selectRelStmts.addElement("to.from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.to[IMS_QP_ExpectedResult2QPTask].from.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_DescriptionEn]");
        selectRelStmts.addElement("to.to[IMS_QP_ExpectedResult2QPTask].from.attribute[IMS_DescriptionEn]");
        selectRelStmts.addElement("to.from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_DescriptionRu]");
        selectRelStmts.addElement("to.to[IMS_QP_ExpectedResult2QPTask].from.attribute[IMS_DescriptionRu]");
        selectRelStmts.addElement("to.from[IMS_QP_ExpectedResult2QPTask].to.attribute[IMS_QP_DocumentCode]");
        selectRelStmts.addElement("to." +
                "from[IMS_QP_ExpectedResult2QPTask].to.to[IMS_QP_ResultType2ExpectedResult].from.name");
        selectRelStmts.addElement("to.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.attribute[IMS_QP_CloseStatus]");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.name");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.current");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.type"); //check CL & VTZ
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.attribute[IMS_ProjDocStatus]");

        ExpansionWithSelect expansion = null;
        try {
            expansion = businessObject.expandSelect(ctx,
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                    IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    selectBusStmts,
                    selectRelStmts,
                    false,
                    true,
                    (short) 1);
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
