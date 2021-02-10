import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IMS_QP_ActualPlanSearchReport_mxJPO {
    private static final Logger LOG = Logger.getLogger("plan_search");

    public void main(Context ctx, Map reportData, String... args) {

        /*get report*/
        Workbook workbook = createReport(ctx, reportData);

        /*write report to the file*/
        File file = writeToFile(ctx, workbook);

        /*upload file to the Business object*/
        boolean checkin = checkIn(ctx, file);
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

    private Workbook createReport(Context ctx, Map reportData) {

        Workbook wb = null;
        try {
            wb = new XSSFWorkbook(IMS_QP_Constants_mxJPO.PLAN_SEARCH_REPORT_TEMPLATE_PATH);
        } catch (IOException ioException) {
            System.out.println("IO exception: " + ioException.getMessage());
            ioException.printStackTrace();
        }

        for (Object element : reportData.entrySet()) {
            Map.Entry entry = (Map.Entry) element;
            String key = (String) entry.getKey();
            BusinessObjectWithSelectList list = (BusinessObjectWithSelectList) entry.getValue();

            Sheet sheet = wb.getSheet(key);
            int lastRowCount = sheet.getLastRowNum();
            int pointCounter = 1;
            System.out.println("sheet: " + sheet.getSheetName());

            for (Object o : list) {
                BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;

                try {
                    businessObject.open(ctx);
                } catch (MatrixException e) {
                    System.out.println("error opening business object: " + e.getMessage());
                    e.printStackTrace();
                }

                Row row = sheet.createRow(lastRowCount);
                row.createCell(0).setCellValue(pointCounter);
                row.createCell(1).setCellValue(businessObject.getSelectData("name"));

                counter = 0;
                greenCounter = 0;

                /*SQP sheet specified zone*/
                if ("SQP".equals(key)) {
                    row.createCell(2).setCellValue(businessObject.getSelectData(
                            String.format("to[%s].from.name",
                                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan)));
                    row.createCell(3).setCellValue(businessObject.getSelectData(
                            String.format("to[%s].from.name",
                                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan)));

                    RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);
                    Map<String, String> taskMap = getQPTaskList(businessObject.getSelectData(DomainObject.SELECT_ID), relItr);
                    pushCellsValuesSQP(wb, row, taskMap);
                }

                /*GRP sheet specified zone*/
                if ("Group SQP,BQPS".equals(key)) {
                    String group = businessObject.getSelectData(String.format("to[%s].from.name",
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2Classifier));
                    row.createCell(2).setCellValue(UIUtil.isNotNullAndNotEmpty(group) ?
                            group : IMS_QP_Constants_mxJPO.OUT_OF_GROUP);

                    RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);
                    List<String> tasksInfo = getQPTaskListByRelationshipType(
                            businessObject.getSelectData(DomainObject.SELECT_ID),
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan,
                            relItr);
                    Map<List<String>, List<String>> tasksInfoMap = getInfoTasks(ctx, tasksInfo);
                    pushCellsValuesGROUP(wb, row, tasksInfoMap);
                }

                /*DEP sheet specified zone*/
                if ("DEP".equals(key)) {
                    RelationshipWithSelectItr relItr = getRelationshipsWithItr(ctx, businessObject);
                    List<String> tasksInfo = getQPTaskListByRelationshipType(businessObject.getSelectData(DomainObject.SELECT_ID),
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan,
                            relItr);
                    Map<List<String>, List<String>> tasksInfoMap = getInfoTasks(ctx, tasksInfo);
                    pushCellsValuesDEP(wb, row, tasksInfoMap);
                }

                lastRowCount++;
                pointCounter++;

                try {
                    businessObject.close(ctx);
                } catch (MatrixException e) {
                    System.out.println("error opening business object: " + e.getMessage());
                    e.printStackTrace();
                }
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
        cell.setCellValue(String.format("%.1f", mathResult) + "%");

        /*code task cell*/
        cell = row.createCell(5);
        cell.setCellStyle(getStyle(wb, "wrap10red"));
        cell.setCellValue(codeCellValueBuilder.toString());
        /*name task cell*/
        cell = row.createCell(6);
        cell.setCellStyle(getStyle(wb, "wrap10red"));
        cell.setCellValue(nameCellValueBuilder.toString());
    }

    private void pushCellsValuesGROUP(Workbook wb, Row row, Map<List<String>, List<String>> tasksInfo) {
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
        cell = row.createCell(3);
        counter = counter > 0 ? counter : 1;
        float mathResult = 100.0f * greenCounter / counter;
        cell.setCellValue(String.format("%.1f", mathResult) + "%");

        if (tasksInfo != null && !tasksInfo.isEmpty()) {

            //Code task cell
            cell = row.createCell(4);
            cell.setCellStyle(getStyle(wb, "wrap10red"));
            cell.setCellValue(codeCellValueBuilder.length() > 0 ? codeCellValueBuilder.toString() : "-");

            //Name task cell
            cell = row.createCell(5);
            cell.setCellStyle(getStyle(wb, "wrap10red"));
            cell.setCellValue(nameCellValueBuilder.toString());
        }
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
        cell.setCellValue(String.format("%.1f", mathResult) + "%");

        if (tasksInfo != null && !tasksInfo.isEmpty()) {

            //Code task cell
            cell = row.createCell(3);
            cell.setCellStyle(getStyle(wb, "wrap10red"));
            cell.setCellValue(codeCellValueBuilder.length() > 0 ? codeCellValueBuilder.toString() : "-");

            //Name task cell
            cell = row.createCell(4);
            cell.setCellStyle(getStyle(wb, "wrap10red"));
            cell.setCellValue(nameCellValueBuilder.toString());
        }
    }

    /**
     * @param ctx
     * @param tasksInfo
     * @return all needed information about task for report
     */
    private Map<List<String>, List<String>> getInfoTasks(Context ctx, List<String> tasksInfo) {
        Map<List<String>, List<String>> tasksInfoMap = new HashMap<>();
        List<String> taskCodes = new ArrayList<>();
        List<String> taskNames = new ArrayList<>();

        for (String taskId : tasksInfo) {

            // Instantiate the BusinessObject.
            StringList selectBusStmts = new StringList();
            selectBusStmts.addElement("name");

            StringList selectRelStmts = new StringList();
            selectRelStmts.addElement("name");
            selectRelStmts.addElement("to");
            selectRelStmts.addElement("to.id");
            selectRelStmts.addElement("to.type");
            selectRelStmts.addElement("to.type");
            selectRelStmts.addElement("to.name");
            selectRelStmts.addElement("to.attribute[IMS_NameRu]");
            selectRelStmts.addElement("to.attribute[IMS_QP_CloseStatus]");
            selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.name");
            selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.current");
            selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.type"); //check CL & VTZ
            selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.attribute[IMS_ProjDocStatus]");

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

    private CellStyle getStyle(Workbook wb, String styleParam) {
        CellStyle cellStyle = wb.createCellStyle();
        Font font;
        switch (styleParam) {
            case "wrap":
                cellStyle.setWrapText(true);
                break;
            case "wrap10":
                cellStyle.setWrapText(true);
                font = wb.createFont();
                font.setFontHeightInPoints((short) 8);
                cellStyle.setFont(font);
                break;
            case "wrap10red":
                cellStyle.setWrapText(true);
                font = wb.createFont();
                font.setFontHeightInPoints((short) 8);
                font.setColor((short) 10);
                cellStyle.setFont(font);
                break;
        }
        return cellStyle;
    }

    private int counter;
    private int greenCounter;

    private Map<String, String> getQPTaskList(String taskId, RelationshipWithSelectItr relItr) {
        Map<String, String> map = new HashMap<>();

        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();

            if (relSelect.getSelectData("name").equals(IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask)) {
                //Increase counter
                counter++;
                boolean redFlag = true;

                if ("Full".equals(relSelect.getSelectData("to.attribute[IMS_QP_CloseStatus]"))) {
                    redFlag = false;
                    greenCounter++;
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

    private List<String> getQPTaskListByRelationshipType(String taskId, String relationship, RelationshipWithSelectItr relItr) {

        List<String> relatedTasks = new ArrayList<>();
        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();
            String relationshipType = relSelect.getSelectData("name");
            String relationshipFromId = relSelect.getSelectData("from.id");
            String relationshipToId = relSelect.getSelectData("to.id");

            boolean from = relationshipFromId.equals(taskId);
            if (relationshipType.equals(relationship) && from) {
                relatedTasks.add(relationshipToId);
            }
        }

        return relatedTasks;
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

        ///Group report select
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
            LOG.info("matrix error: " + e.getMessage());
            e.printStackTrace();
        }

        // Getting Relationships
        RelationshipWithSelectList relationshipWithSelectList = expansion.getRelationships();
        RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(relationshipWithSelectList);
        return relItr;
    }

    private File writeToFile(Context ctx, Workbook workbook) {
        String workspace = getWorkspacePath(ctx);
        String fileName = "actual_search_report.xlsx";

        File file = new File(workspace + "\\" + fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            workbook.write(fos);

        } catch (FileNotFoundException e) {
            System.out.println("FileNotFoundException: " + e.getMessage());
            e.printStackTrace();

        } catch (IOException ioException) {
            System.out.println("IO error: " + ioException.getMessage());
            ioException.printStackTrace();

        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioException) {
                    System.out.println("IO error: " + ioException.getMessage());
                    ioException.printStackTrace();
                }
            }

            return file;
        }
    }

    private boolean checkIn(Context ctx, File file) {

        String fileName = file.getName();
        String filePath = file.getParent();

        BusinessObject bo;
        try {
            bo = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_Reports,
                    "Reports",
                    "-",
                    ctx.getVault().getName());
            ctx.connect();
            bo.open(ctx);
        } catch (Exception e) {
            System.out.println("business object error: " + e.getMessage());
            return false;
        }

        /*checkin method*/
        try {
            bo.checkinFile(ctx,
                    true, // false to lock file
                    false, // true to append file
                    "", // different host
                    "generic", // format
                    fileName, //file
                    filePath); //path
            bo.close(ctx);

        } catch (MatrixException me) {
            System.out.println("checkin: " + me.getMessage());
            return false;
        }

        FileUtils.deleteQuietly(new File(filePath));

        return true;
    }

    public Map<String, Object> checkout(Context ctx, String... args) throws Exception {
        Map<String, Object> map = new HashMap();

        Map programMap = JPO.unpackArgs(args);
        String objectId = (String) programMap.get("objectId");
        BusinessObject bo = null;
        try {
            bo = new BusinessObject(objectId);
            ctx.connect();
            bo.open(ctx);
        } catch (Exception e) {
            System.out.println("business object error: " + e.getMessage());
        }

        String workspace = getWorkspacePath(ctx);
        matrix.db.FileList files = bo.getFiles(ctx);
        if (files.size() > 1) {
            return null;
        }
        matrix.db.File mxFile = (matrix.db.File) files.get(0);
        String fileName = mxFile.getName();

        /*checkout method*/
        try {
            ctx.connect();
            ctx.createWorkspace();

            bo.checkoutFile(ctx,
                    false, //true to lock file
                    "generic", //format
                    fileName,
                    workspace);
            bo.close(ctx);
        } catch (MatrixException me) {
            System.out.println("matrix exception: " + me.getMessage());
        }

        /*read'n'write to workbook*/
        String absolutePath = workspace + "\\" + fileName;
        LOG.info("absolute path: " + absolutePath);

        /*send to the JSP*/
        map.put("fileName", fileName);
        map.put("byteArray", getOutArray(absolutePath));

        FileUtils.deleteQuietly(new File(workspace));

        return map;
    }

    private byte[] getOutArray(String fileName) {
        File file = new File(fileName);
        byte[] outArray = new byte[0];

        try {
            outArray = Files.readAllBytes(file.toPath());
            return outArray;
        } catch (IOException ioe) {
            ioe.printStackTrace();
            return outArray;
        }
    }
}
