import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.EnoviaResourceBundle;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.*;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * It is the utility class whose methods help to obtain additional capabilities for extracting data from the database,
 * comparing and normalizing the provided information. see method descriptions for more details.
 */
public class IMS_QP_CheckRelations_mxJPO {

    Logger LOG = Logger.getLogger("reportLogger");

    /**
     * The method checks all objects of the IMS_QP_DEPTask type in the database and gets information about the relationships.
     * Then each object is checked for the presence of "steps" among themselves and their status.
     * Condition: all steps related to each other are listed as "internal" and the links between them must go to the 'Approved' status.
     *
     * @param context Basic parameter
     * @param args    Basic parameter
     * @return String 'success' if all stages are successful. This message will appear in the calling JSP.
     */
    public String checkRelations(Context context, String[] args) {

        StringList selects = new StringList(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(IMS_QP_Constants_mxJPO.DEP_ID_FOR_TASK);
        selects.add("to[IMS_QP_DEPTask2DEPTask].from.id");
        selects.add("to[IMS_QP_DEPTask2DEPTask].id");
        selects.add("from[IMS_QP_DEPTask2DEPTask].to.id");
        selects.add("from[IMS_QP_DEPTask2DEPTask].id");

        MapList allTasks;

        try {
            allTasks = DomainObject.findObjects(context,
                    /*type*/ IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask,
                    DomainConstants.QUERY_WILDCARD,
                    DomainConstants.EMPTY_STRING, selects);

            Map<String, List<String>> allDEPs = getAllDEPs(context);

            for (Object o : allTasks) {
                Map map = (Map) o;
                String id = (String) map.get(DomainConstants.SELECT_ID);

                String depId = UIUtil.isNotNullAndNotEmpty((String) map.get(IMS_QP_Constants_mxJPO.DEP_ID_FOR_TASK)) ?
                        (String) map.get(IMS_QP_Constants_mxJPO.DEP_ID_FOR_TASK) : "not found";
                depId = depId.contains(IMS_QP_Constants_mxJPO.BELL_DELIMITER) ?
                        depId.substring(0, depId.indexOf(IMS_QP_Constants_mxJPO.BELL_DELIMITER)) : depId;

                if (map.containsKey("to[IMS_QP_DEPTask2DEPTask].from.id")) {
                    String rawToId = (String) map.get("to[IMS_QP_DEPTask2DEPTask].from.id");
                    String rawToRelationshipId = (String) map.get("to[IMS_QP_DEPTask2DEPTask].id");
                    String[] rawToIdArray = rawToId.split(IMS_QP_Constants_mxJPO.BELL_DELIMITER);
                    String[] rawToRelationshipIdArray = rawToRelationshipId.split(IMS_QP_Constants_mxJPO.BELL_DELIMITER);
                    List<String> listToId = Arrays.asList(rawToIdArray);
                    List<String> listRelToId = Arrays.asList(rawToRelationshipIdArray);

                    for (int i = 0; i < listToId.size(); i++) {
                        try {
                            //search task in dep map
                            List<String> depContainTasks = allDEPs.get(depId) != null ? allDEPs.get(depId) : new ArrayList();
                            //search by id of related task they rel id
                            try {
                                if (!depId.equals("not found") && depContainTasks.contains(listToId.get(i))) {
                                    //get id related task
                                    String relationshipID = listRelToId.get(i);
                                    //change status on to "Approved"
                                    DomainRelationship relationship = new DomainRelationship(relationshipID);
                                    relationship.setAttributeValue(context,
                                            IMS_QP_Constants_mxJPO.attribute_IMS_QP_DEPTaskStatus,
                                            IMS_QP_Constants_mxJPO.APPROVED);
                                }


                            } catch (Exception e) {
                                LOG.error("error scenario: " + depId
                                        + "|" + id
                                        + "|" + listToId.get(i)
                                        + "|result: " + depContainTasks.contains(listToId.get(i)));
                                e.printStackTrace();
                            }
                        } catch (Exception e) {
                            LOG.error("err: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                }
            }
        } catch (FrameworkException e) {
            e.printStackTrace();
            return "error in method";
        }
        return "success";
    }

    /**
     * The helper method used in the checkRelations method provides information about all associated "steps" in relation
     * to objects of type IMS_QP_DEP
     *
     * @param context Base parameter
     * @return
     * @throws FrameworkException
     */
    private Map getAllDEPs(Context context) throws FrameworkException {
        Map<String, List<String>> deps = new HashMap<>();

        StringList selects = new StringList(DomainConstants.SELECT_ID);
        selects.add("from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.from[IMS_QP_DEPSubStage2DEPTask].to.id");
        MapList listDeps = DomainObject.findObjects(context, "IMS_QP_DEP", "*", "", selects);

        for (Object o : listDeps) {
            Map map = (Map) o;
            String id = (String) map.get(DomainConstants.SELECT_ID);

            if (map.containsKey("from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.from[IMS_QP_DEPSubStage2DEPTask].to.id")) {
                String rawAllRelatedTasks = (String) map.get("from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.from[IMS_QP_DEPSubStage2DEPTask].to.id");

                String[] rawAllRelatedTasksArray = rawAllRelatedTasks.split(IMS_QP_Constants_mxJPO.BELL_DELIMITER);
                List<String> allRelatedTasks = Arrays.asList(rawAllRelatedTasksArray);
                deps.put(id, allRelatedTasks);
            }
        }
        return deps;
    }


    /**
     * The method allows you to output information about all objects of the IMS_QP_DEP type to the log file.
     * Used as a reference when you need to check for the presence of well-organized links in the database.
     *
     * @param context Base parameter
     * @param args    Base parameter
     * @return
     */
    public String checkDepRelations(Context context, String[] args) {

        String id = DomainConstants.SELECT_ID;
        String name = DomainConstants.SELECT_NAME;
        String qp = "to[IMS_QP_QP2DEP].from.name";
        String discipline = "to[IMS_QP_Discipline2DEP].from.name";
        String projectStage = "from[IMS_QP_DEP2DEPProjectStage].to.name";
        String subStage = "from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.name";
        String tasks = "from[IMS_QP_DEP2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to.from[IMS_QP_DEPSubStage2DEPTask].to.name";
        String depTask = "to[IMS_QP_DEP2DEPTask].from.name";
        String depProjectStage = "from[IMS_QP_DEP2DEPProjectStage].to.to[IMS_QP_ProjectStage2DEPProjectStage].from.name";

        StringList selects = new StringList(id);
        selects.add(name);
        selects.add(qp);
        selects.add(discipline);
        selects.add(projectStage);
        selects.add(subStage);
        selects.add(tasks);
        selects.add(depTask);
        selects.add(depProjectStage);

        MapList listDeps = new MapList();
        try {
            listDeps = DomainObject.findObjects(context, "IMS_QP_DEP", "*", "", selects);
        } catch (FrameworkException fe) {
            LOG.error("framework exception: " + fe.getMessage());
        }

        if (!listDeps.isEmpty()) {

            StringBuilder sb = new StringBuilder();
            sb.append("id|name|qp|discipline|project stage|substages|all tasks|dep tasks|project directory");
            LOG.info(sb.toString());

            for (Object object : listDeps) {
                Map map = (Map) object;
                sb.setLength(0);
                sb.append(map.get(id));
                sb.append("|" + map.get(name));
                sb.append("|" + map.get(qp));
                sb.append("|" + map.get(discipline));
                sb.append("|" + map.get(projectStage));
                sb.append("|" + map.get(subStage));
                sb.append("|" + map.get(tasks));
                sb.append("|" + map.get(depTask));
                sb.append("|" + map.get(depProjectStage));

                String result = sb.toString();
                result = result.replace(IMS_QP_Constants_mxJPO.BELL_DELIMITER, ",");
                LOG.info(result);
            }


        }
        return EnoviaResourceBundle.getProperty(context,
                "IMS_QP_FrameworkStringMessages",
                context.getLocale(),
                "IMS_QP_Framework.Message.resultCheckRelations");
    }


    /**
     * It takes command lines from the presented file, then each of the lines executes them using MQL commands.
     * An unlimited number of commands can be transmitted, for each command, in case of failure,
     * meaningful information is displayed in the log file.
     *
     * @param context Base parameter
     * @param args    Base parameter
     * @return
     */
    public String connectPBSToSystems(Context context, String... args) {

        FileReader fileReader;
        BufferedReader bufferedReader = null;

        try {
            fileReader = new FileReader(EnoviaResourceBundle.getProperty(context,
                    "IMS_QP_FrameworkStringMessages",
                    context.getLocale(),
                    "IMS_QP_Framework.Message.resultCheckRelations"));
            bufferedReader = new BufferedReader(fileReader);
            String command = bufferedReader.readLine();
            while (command != null) {
                LOG.info(command);
                try {
                    /*String result = */
                    MqlUtil.mqlCommand(context, command);
                } catch (FrameworkException e) {
                    LOG.error("FrameworkException: " + e.getMessage());
                    e.printStackTrace();
                }
                command = bufferedReader.readLine();
            }

        } catch (IOException e) {
            LOG.error("exception: " + e.getMessage());
            return EnoviaResourceBundle.getProperty(context,
                    "IMS_QP_FrameworkStringMessages",
                    context.getLocale(),
                    "IMS_QP_Framework.Message.internalServerError");
        } finally {
            if (bufferedReader != null)
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    LOG.error("buffer error: " + e.getMessage());
                    e.printStackTrace();
                }
        }

        return EnoviaResourceBundle.getProperty(context,
                "IMS_QP_FrameworkStringMessages",
                context.getLocale(),
                "IMS_QP_Framework.Message.passedOK");
    }

    public void getAllPBS(Context context, String[] args) {
        System.out.println("getAllPBS starting");

        //get objectID
        StringList selects = new StringList();
        selects.add(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_TYPE);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add("revision");

        MapList rawResult = null;
        final String types = new StringBuilder()
                .append(IMS_QP_Constants_mxJPO.type_IMS_PBSSystem).append(",")
                .append(IMS_QP_Constants_mxJPO.type_IMS_GBSBuilding).append(",")
                .append(IMS_QP_Constants_mxJPO.type_IMS_PBSFunctionalArea)
                .toString();
        try {
            rawResult = DomainObject.findObjects(context,
                    types,
                    DomainConstants.QUERY_WILDCARD,
                    DomainConstants.EMPTY_STRING,
                    selects);
        } catch (FrameworkException e) {
            System.out.println("error when getting system types objects: " + e.getMessage());
            e.printStackTrace();
        }

        MapList typoSystems = new MapList();
        MapList typoBuildings = new MapList();
        MapList typoAreas = new MapList();

        System.out.println("raw result size: " + rawResult.size());
        if (rawResult != null)
            for (Object o : rawResult) {
                Map map = (Map) o;


                //check type that non identical with same names but another types
                switch ((String) map.get(DomainConstants.SELECT_TYPE)) {
                    case IMS_QP_Constants_mxJPO.type_IMS_PBSSystem:
                        typoSystems.add(map);
                        break;
                    case IMS_QP_Constants_mxJPO.type_IMS_GBSBuilding:
                        typoBuildings.add(map);
                        break;
                    case IMS_QP_Constants_mxJPO.type_IMS_PBSFunctionalArea:
                        typoAreas.add(map);
                        break;
                }
            }

        sortMap(typoSystems);
        sortMap(typoBuildings);
        sortMap(typoAreas);

        try {
            FileWriter fileWriter = new FileWriter(new File("C:\\Temp\\revise_commands.txt"), false);

            fileWriter
                    .append("================================= IMS_PBSSystem =================================\n");
            reviseByType(typoSystems, fileWriter);

            fileWriter
                    .append("================================= IMS_GBSBuildings =================================\n");
            reviseByType(typoBuildings, fileWriter);

            fileWriter
                    .append("================================= IMS_PBSFunctionalArea =================================\n");
            reviseByType(typoAreas, fileWriter);

            fileWriter.close();

        } catch (IOException e) {
            System.out.println("error when revising: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private MapList sortMap(MapList mapList) {
        Collections.sort(mapList, new Comparator<Map>() {
            @Override
            public int compare(Map map1, Map map2) {
                Integer revision1 = Integer.parseInt((String) map1.get(DomainConstants.SELECT_REVISION));
                Integer revision2 = Integer.parseInt((String) map2.get(DomainConstants.SELECT_REVISION));
                return revision1.compareTo(revision2);
            }
        });

        return mapList;
    }

    private void reviseByType(MapList mapList, FileWriter fileWriter) {
        for (Object o : mapList) {
            Map map1 = (Map) o;
            for (Object o1 : mapList) {

                Map map2 = (Map) o1;
                if (map1.get(DomainConstants.SELECT_NAME).equals(map2.get(DomainConstants.SELECT_NAME))) {
                    Integer revisionMap1 = Integer.parseInt((String) map1.get(DomainConstants.SELECT_REVISION));
                    Integer revisionMap2 = Integer.parseInt((String) map2.get(DomainConstants.SELECT_REVISION));

                    if (revisionMap1 < revisionMap2) {
                        String map1ID = (String) map1.get(DomainConstants.SELECT_ID);
                        String map1Type = (String) map1.get(DomainConstants.SELECT_TYPE);
                        String map1Name = (String) map1.get(DomainConstants.SELECT_NAME);
                        String map1Description = String.format("%s|%s|%s", map1Type, map1Name, revisionMap1);

                        String map2ID = (String) map2.get(DomainConstants.SELECT_ID);
                        String map2Type = (String) map2.get(DomainConstants.SELECT_TYPE);
                        String map2Name = (String) map2.get(DomainConstants.SELECT_NAME);
                        String map2Description = String.format("%s|%s|%s", map2Type, map2Name, revisionMap2);

                        try {
                            fileWriter.write(String.format("revise bus %s bus %s;",
                                    map1ID, map2ID) + "|" + map1Description + "|" + map2Description + "\n");
                        } catch (IOException e) {
                            System.out.println("error when writing into file: " + e.getMessage());
                            e.printStackTrace();
                        }
                        map1 = map2;
                    }
                }
            }
        }

        try {
            fileWriter.flush();
        } catch (IOException e) {
            System.out.println("error when flushing: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * by issue #63784.1 report without bother Resource
     *
     * @param ctx
     * @param args if it needs all parameters
     * [0] - the plan name,
     * [1] - path to directory of report,
     * [2] - name of report template
     * or using default values
     */
    Workbook wb = null;
    Map globalTasksMap = null;

    public void getPlanRelatedFullReport(Context ctx, String... args) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMM_HHmm");

        String planName = "QP-10KAB";
        String absolutePath = "C:\\Temp\\63784\\";
        String fileName = "kab_report.xlsx";

        if (args != null && args.length > 0) {
            planName = args[0];
            absolutePath = args[1];
            fileName = args[2];

        }

        String type = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;
        String where = "to[IMS_QP_QPlan2QPTask].from.name=='" + planName + "'";

        //get all tasks FROM IMS_QP_QPlan
        MapList items = findObjectsByTypeWhere(ctx, type, where);
        System.out.println("start to reporting at " + formatter.format(date)
                + "\nfound items size: " + items.size());

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(absolutePath + fileName);
            if (fis != null) {
                wb = new XSSFWorkbook(fis);
            }
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
            e.printStackTrace();
        }

        //counters
        int globalCounter = 0;
        int inputRowCount = 1;
        int outputRowCount = 1;

        globalTasksMap = new HashMap();
        for (Object mainTaskObject : items) {
            System.out.println("items left: " + (items.size() - globalCounter));
            globalCounter++;

            Map mainMap = (Map) mainTaskObject;
            String taskId = (String) mainMap.get(DomainConstants.SELECT_ID);

            //rotate all tasks & get input tasks with attributes
            MapList allInputTasks = new MapList();

            allInputTasks.addAll(
                    getRelatedById(ctx, taskId, type,
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                            false, true));
            System.out.println("allInputTasks: " + allInputTasks.size());
            //fill input sheet
            inputRowCount = fillSheetData(0, inputRowCount, mainMap, allInputTasks);
            System.out.println("input row counter: " + inputRowCount);

            //rotate all tasks & get output tasks with attributes
            MapList allOutputTasks = new MapList();
            allOutputTasks.addAll(
                    getRelatedById(ctx, taskId, type,
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                            true, false));
            System.out.println("allOutputTasks: " + allOutputTasks.size());
            //fill output sheet
            outputRowCount = fillSheetData(1, outputRowCount, mainMap, allOutputTasks);
            System.out.println("output row counter: " + outputRowCount);

            //add all tasks to global sheet
        }

        //1. write book to new file
        try {
            date = new Date();
            String toFileName = absolutePath + "plan_report_" + planName + "_" + formatter.format(date) + ".xlsx";
            System.out.println("finished to reporting at " + formatter.format(date) + "\nwith new file name: " + toFileName);
            FileOutputStream fos = new FileOutputStream(toFileName);
            wb.write(fos);

            //close streams
            if (fis != null) {
                fis.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //2. add global sheet and rotate all tasks & get all in/out tasks
        System.out.println("global size: " + globalTasksMap.size());
//        writeGlobalToTxtFile(ctx, absolutePath, globalTasksList);
    }

    /**
     * by issue #63784.2 report without bother Resource
     *
     * @param ctx
     * @param args if it needs all parameters
     *             [1] - path to directory of report,
     *             [2] - name of report template
     *             or using default values
     */
    public void getDepTasksRelatedFullReport(Context ctx, String... args) {
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMM_HHmm");

        String absolutePath = "C:\\Temp\\63784\\";
        String fileName = "dep_report.xlsx";

        if (args != null && args.length > 0) {
            absolutePath = args[0];
            fileName = args[1];

        }

        String type = IMS_QP_Constants_mxJPO.type_IMS_QP_DEPTask;

        //get all dep tasks FROM IMS_QP_QPlan
        BusinessObjectWithSelectList bowsDepTasksList = getByType(ctx,
                IMS_QP_Constants_mxJPO.type_IMS_QP_DEPTask);
        System.out.println("start to reporting at " + formatter.format(date)
                + "\nfound items size: " + bowsDepTasksList.size());

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(absolutePath + fileName);
            if (fis != null) {
                wb = new XSSFWorkbook(fis);
            }
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
            e.printStackTrace();
        }

        //counters
        int globalCounter = 0;
        int inputRowCount = 1;
        int outputRowCount = 1;

        globalTasksMap = new HashMap();
        for (Object o : bowsDepTasksList) {
            BusinessObjectWithSelect boMainTask = (BusinessObjectWithSelect) o;
            System.out.println("items left: " + (bowsDepTasksList.size() - globalCounter));
            globalCounter++;

            String taskId = boMainTask.getSelectData(DomainConstants.SELECT_ID);

            //rotate all tasks & get input tasks with attributes
            MapList allInputTasks = new MapList();

            allInputTasks.addAll(
                    getRelatedById(ctx, taskId, type,
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2DEPTask,
                            false, true));
            System.out.println("allInputTasks: " + allInputTasks.size());
            //fill input sheet
            inputRowCount = fillSheetData(0, inputRowCount, boMainTask, allInputTasks);
            System.out.println("input row counter: " + inputRowCount);

            //rotate all tasks & get output tasks with attributes
            MapList allOutputTasks = new MapList();
            allOutputTasks.addAll(
                    getRelatedById(ctx, taskId, type,
                            IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2DEPTask,
                            true, false));
            System.out.println("allOutputTasks: " + allOutputTasks.size());
            //fill output sheet
            outputRowCount = fillSheetData(1, outputRowCount, boMainTask, allOutputTasks);
            System.out.println("output row counter: " + outputRowCount);

            //add all tasks to global sheet
        }

        //1. write book to new file
        try {
            date = new Date();
            fileName = "dep_report";
            String toFileName = absolutePath + fileName + "_" + formatter.format(date) + ".xlsx";
            System.out.println("finished to reporting at " + formatter.format(date) + "\nwith new file name: " + toFileName);
            FileOutputStream fos = new FileOutputStream(toFileName);
            wb.write(fos);

            //close streams
            if (fis != null) {
                fis.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        //2. add global sheet and rotate all tasks & get all in/out tasks
        System.out.println("global size: " + globalTasksMap.size());
//        writeGlobalToTxtFile(ctx, absolutePath, globalTasksList);
    }

    /**
     * Utility method for filling rows in indexed sheets for report by issue #63784
     *
     * @param sheetIndex
     * @param rowIndex
     * @param mainMap
     * @param data
     * @return
     */
    private int fillSheetData(int sheetIndex, int rowIndex,
                              Map mainMap, MapList data) {

        int index = rowIndex;
        Sheet sheet = wb.getSheetAt(sheetIndex);
        Row row;
        Cell cell;
        for (Object o : data) {
            row = sheet.createRow(index);
            index++;

            Map map = (Map) o;

            //Code
            cell = row.createCell(0);
            cell.setCellValue((String) mainMap.get(DomainConstants.SELECT_NAME));

            //Name
            cell = row.createCell(1);
            cell.setCellValue((String) mainMap.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU));

            //Input task code
            cell = row.createCell(2);
            cell.setCellValue((String) map.get(DomainConstants.SELECT_NAME));

            //Input task name
            cell = row.createCell(3);
            cell.setCellValue((String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU));

            //Task from plan name
            cell = row.createCell(4);
            cell.setCellValue(UIUtil.isNotNullAndNotEmpty((String) map.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.name")) ?
                    (String) map.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.name") : "-");

            cell = row.createCell(5);
            cell.setCellValue(UIUtil.isNotNullAndNotEmpty((String) map.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.description")) ?
                    (String) map.get("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.description") : "-");

            if (!globalTasksMap.containsKey(map.get(DomainConstants.SELECT_NAME))) {
                globalTasksMap.put(map.get(DomainConstants.SELECT_NAME), map.get(DomainConstants.SELECT_ID));
            }
        }
        return index;
    }

    /**
     * Utility method for filling rows in indexed sheets for report by issue #63784
     *
     * @param sheetIndex
     * @param rowIndex
     * @param mainTask   Business object with select - TDO object
     * @param data
     * @return
     */

    private final String relToDep = new StringBuilder()
            .append("from[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2DEP).append("].to.")
            .append(DomainConstants.SELECT_NAME).toString();
    private final String relToDepDocDescription = new StringBuilder()
            .append("from[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPTask2DEP).append("].to.")
            .append("from[IMS_QP_DEP2Doc].to.")
            .append("description").toString();
    private final String relFromSubStageFromProjectStageToDep = new StringBuilder()
            .append("to[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPSubStage2DEPTask).append("].from.")
            .append("to[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPProjectStage2DEPSubStage).append("].from.")
            .append("to[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2DEPProjectStage).append("].from.")
            .append(DomainConstants.SELECT_NAME).toString();
    private final String relFromSubStageFromProjectStageToDepDocDescription = new StringBuilder()
            .append("to[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPSubStage2DEPTask).append("].from.")
            .append("to[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEPProjectStage2DEPSubStage).append("].from.")
            .append("to[").append(IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2DEPProjectStage).append("].from.")
            .append("from[IMS_QP_DEP2Doc].to.")
            .append("description").toString();

    private int fillSheetData(int sheetIndex, int rowIndex,
                              BusinessObjectWithSelect mainTask, MapList data) {

        int index = rowIndex;
        Sheet sheet = wb.getSheetAt(sheetIndex);
        Row row;
        Cell cell;
        for (Object o : data) {
            row = sheet.createRow(index);
            index++;

            Map map = (Map) o;

            //Code
            cell = row.createCell(0);
            cell.setCellValue(mainTask.getSelectData(DomainConstants.SELECT_NAME));

            //Name
            cell = row.createCell(1);
            cell.setCellValue(mainTask.getSelectData(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU));

            //Input task code
            cell = row.createCell(2);
            cell.setCellValue((String) map.get(DomainConstants.SELECT_NAME));

            //Input task name
            cell = row.createCell(3);
            cell.setCellValue((String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU));

            //Parent dep name
            cell = row.createCell(4);
            cell.setCellValue(UIUtil.isNotNullAndNotEmpty(mainTask.getSelectData(relToDep)) ?
                    mainTask.getSelectData(relToDep) :
                    UIUtil.isNotNullAndNotEmpty(mainTask.getSelectData(relFromSubStageFromProjectStageToDep)) ?
                            mainTask.getSelectData(relFromSubStageFromProjectStageToDep) : "-");

            //Parent deps doc description
            cell = row.createCell(5);
            cell.setCellValue(UIUtil.isNotNullAndNotEmpty(mainTask.getSelectData(relToDepDocDescription)) ?
                    mainTask.getSelectData(relToDepDocDescription) :
                    UIUtil.isNotNullAndNotEmpty(mainTask.getSelectData(relFromSubStageFromProjectStageToDepDocDescription)) ?
                            mainTask.getSelectData(relFromSubStageFromProjectStageToDepDocDescription) : "-");

            if (!globalTasksMap.containsKey(map.get(DomainConstants.SELECT_NAME))) {
                globalTasksMap.put(map.get(DomainConstants.SELECT_NAME), map.get(DomainConstants.SELECT_ID));
            }
        }
        return index;
    }

    private MapList getRelatedById(Context ctx, String id, String type,
                                   String relationship,
            /*getTo*/ boolean to, /*getFrom*/ boolean from) {
        StringList selects = new StringList(DomainConstants.SELECT_ID);
        selects.add(DomainConstants.SELECT_NAME);
        selects.add(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU);
        selects.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.name");
        selects.add("to[" + IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask + "].from.description");
        MapList related = new MapList();
        try {
            DomainObject object = new DomainObject(id);
            if (object != null) {
                related = object.getRelatedObjects(ctx,
                        /*relationship*/ relationship,
                        /*type*/ type,
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/to,/*getFrom*/ from,
                        /*recurse level*/(short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/0);
                object.close(ctx);
            }
        } catch (Exception e) {
            System.out.println("error getting object: " + e.getMessage());
            e.printStackTrace();
        }

        return related;
    }

    private void writeGlobalToTxtFile(Context ctx, String absolutePath, MapList globalTasksList) {
        String fileName = "kab_report.txt";
        FileWriter fileWriter = null;
        try {
            fileWriter = new FileWriter(absolutePath + fileName, false);

            StringList selects = new StringList();
            selects.add(DomainConstants.SELECT_ID);
            selects.add(DomainConstants.SELECT_NAME);

            for (Object o : globalTasksList) {
                Map map = (Map) o;
                fileWriter.append("===" + map.get(DomainConstants.SELECT_NAME) + "===\n");

                DomainObject object = new DomainObject((String) map.get(DomainConstants.SELECT_ID));

                MapList relatedInputs = object.getRelatedObjects(ctx,
                        /*relationship*/ IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                        /*type*/ IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/true,/*getFrom*/ false,
                        /*recurse level*/(short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/0);

                fileWriter.append("\tINPUTS:\n");
                for (Object o1 : relatedInputs) {
                    Map map1 = (Map) o1;
                    fileWriter.append("\t\t\t" + map1.get(DomainConstants.SELECT_NAME) + "\n");
                }

                fileWriter.flush();

                fileWriter.append("\tOUTPUTS:\n");
                MapList relatedOutputs = object.getRelatedObjects(ctx,
                        /*relationship*/ IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask,
                        /*type*/ IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                        /*object attributes*/ selects,
                        /*relationship selects*/ null,
                        /*getTo*/false,/*getFrom*/ true,
                        /*recurse level*/(short) 1,
                        /*object where*/ null,
                        /*relationship where*/ null,
                        /*limit*/0);
                for (Object o1 : relatedOutputs) {
                    Map map1 = (Map) o1;
                    fileWriter.append("\t\t\t" + map1.get(DomainConstants.SELECT_NAME) + "\n");
                }

                fileWriter.flush();
            }

        } catch (IOException e) {
            System.out.println("error when writing: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.out.println("error getting object: " + e.getMessage());
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    System.out.println("error closing file writer stream: " + e.getMessage());
                    e.printStackTrace();
                }

            }
        }
    }

    private MapList findObjectsByTypeWhere(Context ctx, String type, String where) {
        StringList selects = new StringList();
        selects.addElement(DomainConstants.SELECT_ID);
        selects.addElement(DomainConstants.SELECT_NAME);

        selects.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME);
        selects.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU);

        MapList items = new MapList();
        try {
            items = DomainObject.findObjects(ctx, type, IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION, where, selects);
        } catch (FrameworkException e) {
            LOG.error("error getting Tasks: " + e.getMessage());
            e.printStackTrace();
        }

        return items;
    }

    public Map getVTZReport(Context ctx, String... args) {

        String type = IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask;
        String where = new StringBuilder()
                .append("from[IMS_QP_ExpectedResult2QPTask].to.")
                .append("to[IMS_QP_ResultType2ExpectedResult].from.")
                .append("to[IMS_QP_ResultType2Family].from.name==")
                .append("'\u0422\u0438\u043f\u044b\u0020\u0412\u0422\u0417'")
                .toString();

        StringList selects = new StringList();
        selects.addElement(DomainConstants.SELECT_ID);
        selects.addElement("to[IMS_QP_QPlan2QPTask].from.name");
        selects.addElement(DomainConstants.SELECT_NAME);
        selects.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME);
        selects.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU);
        selects.addElement("from[IMS_QP_QPTask2Fact].to.name");
        selects.addElement("attribute[IMS_QP_SelectDocument]");
        selects.addElement("attribute[IMS_QP_AdditionalInfo]");

        MapList items = new MapList();
        try {
            items = DomainObject.findObjects(ctx, type, IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION, where, selects);
        } catch (FrameworkException e) {
            LOG.error("error getting Tasks: " + e.getMessage());
            e.printStackTrace();
        }

        String absolutePath = "C:\\Temp\\";
        String fileName = "qp_task_vtz_report.txt";
        try {
            FileWriter fileWriter = new FileWriter(absolutePath + fileName, false);
            fileWriter.append("QPlan\tQPTask\tName\tNameRu\tDocument Set\tSelect document\tAdditional information\n");
            for (Object o : items) {
                Map map = (Map) o;
                String qPlanName = UIUtil.isNotNullAndNotEmpty(
                        (String) map.get("to[IMS_QP_QPlan2QPTask].from.name")) ?
                        (String) map.get("to[IMS_QP_QPlan2QPTask].from.name") : "-";
                String name = UIUtil.isNotNullAndNotEmpty(
                        (String) map.get(DomainConstants.SELECT_NAME)) ?
                        (String) map.get(DomainConstants.SELECT_NAME) : "-";
                String imsName = UIUtil.isNotNullAndNotEmpty(
                        (String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME)) ?
                        (String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME) : "-";
                String imsNameRu = UIUtil.isNotNullAndNotEmpty(
                        (String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU)) ?
                        (String) map.get(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU) : "-";
                String docCode = UIUtil.isNotNullAndNotEmpty(
                        (String) map.get("from[IMS_QP_QPTask2Fact].to.name")) ? (String) map.get("from[IMS_QP_QPTask2Fact].to.name") : "-";
                String selection = UIUtil.isNotNullAndNotEmpty(
                        (String) map.get(IMS_QP_Constants_mxJPO.attribute_IMS_QP_SelectDocument)) ?
                        (String) map.get(IMS_QP_Constants_mxJPO.attribute_IMS_QP_SelectDocument) : "-";
                String additionalInfo = UIUtil.isNotNullAndNotEmpty(
                        (String) map.get(IMS_QP_Constants_mxJPO.attribute_IMS_QP_ADDITIONAL_INFO)) ?
                        (String) map.get(IMS_QP_Constants_mxJPO.attribute_IMS_QP_ADDITIONAL_INFO) : "-";
                //without Fact-* quality plans
                if (UIUtil.isNotNullAndNotEmpty(qPlanName) && qPlanName.contains("Fact")) {
                    continue;
                }
                fileWriter.append(qPlanName + "\t" + name + "\t" + imsName + "\t" + imsNameRu + "\t" + docCode + "\t" + selection + "\t" + additionalInfo + "\n");
                fileWriter.flush();
            }
            fileWriter.close();

        } catch (IOException e) {
            LOG.error("error when revising: " + e.getMessage());
            e.printStackTrace();
        }

        /*send to the JSP*/
        Map map = new HashMap();
        map.put("fileName", fileName);
        map.put("byteArray", getOutArray(absolutePath + fileName));
//        FileUtils.deleteQuietly(new File(absolutePath + fileName));

        return map;
    }

    private byte[] getOutArray(String fileName) {
        File file = new File(fileName);
        byte[] outArray = new byte[0];

        try {
            outArray = Files.readAllBytes(file.toPath());
            return outArray;
        } catch (IOException var5) {
            var5.printStackTrace();
            LOG.error("error getting fle: " + var5.getMessage());
            return outArray;
        }
    }

    /**
     * Getting all objects by type
     *
     * @param ctx  usual parameter
     * @param type type of objects
     * @return List of business objects
     */
    public BusinessObjectWithSelectList getByType(Context ctx, String type) {

        //Do the query
        BusinessObjectWithSelectList businessObjectList = new BusinessObjectWithSelectList();
        try {
            Query query = getQueryByType(type);
            businessObjectList = query.selectTmp(ctx, getBusinessSelect());
        } catch (MatrixException e) {
            System.out.println("matrix error: " + e.getMessage());
            e.printStackTrace();
        }
        return businessObjectList;
    }

    /**
     * Method take type and set parameters for query
     *
     * @param type of object for searching
     * @return matrix.db.Query
     */
    private Query getQueryByType(String type) {

        //Prepare temp query
        Query query = new Query();
        query.setBusinessObjectType(type);
        query.setOwnerPattern("*");
        query.setVaultPattern(IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION);
        query.setWhereExpression("");

        return query;
    }

    /**
     * Getting selections needed
     *
     * @return list of selection strings
     */
    private StringList getBusinessSelect() {
        StringList busSelect = new StringList();

        busSelect.addElement(DomainConstants.SELECT_ID);
        busSelect.addElement(DomainConstants.SELECT_NAME);
        busSelect.addElement(DomainConstants.SELECT_TYPE);

        busSelect.addElement(relToDep);
        busSelect.addElement(relFromSubStageFromProjectStageToDep);
        busSelect.addElement(relToDepDocDescription);
        busSelect.addElement(relFromSubStageFromProjectStageToDepDocDescription);

        busSelect.addElement(IMS_QP_Constants_mxJPO.ATTRIBUTE_IMS_NAME_RU);

        return busSelect;
    }

    /**
     * report by issue #66675
     *
     * @param ctx
     * @param args if it needs all parameters
     *             [1] - path to directory of report,
     *             [2] - name of report template
     *             or using default values
     */
    public void getInitialDesignReport(Context ctx, String... args) {
        int cellCountRequired = 12;
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("ddMM_HHmm");

        String dataFilePath = "C:\\Temp\\66675\\initial_design_data.txt";

        String absolutePath = "C:\\Temp\\66675\\";
        String fileName = "initial_report.xlsx";

        if (args != null && args.length > 0) {
            absolutePath = args[0];
            fileName = args[1];
        }

        FileInputStream fis = null;
        try {
            System.out.println(absolutePath + fileName);
            fis = new FileInputStream(absolutePath + fileName);

            if (fis != null) {
                wb = new XSSFWorkbook(fis);
            }
        } catch (IOException e) {
            System.out.println("IO error: " + e.getMessage());
            e.printStackTrace();
        }

        int rowCount = 1;

        //get next row from txt file
        File file = new File(dataFilePath);

        FileReader fileReader = null;
        Reader reader = null;
        try {
            fileReader = new FileReader(file);
            reader = new InputStreamReader(new FileInputStream(file), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        BufferedReader bufferedReader = new BufferedReader(reader);
        List<String> lineArray = null;

        try {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                lineArray = new ArrayList<>();
                if (line != null) {
                    String[] array = line.split("\\|\\|\\|");
                    for (String s : array) {
                        lineArray.add(s);
                    }

                    if (lineArray != null && lineArray.size() > 0) {
                        lineArray.remove(0);

                        //find task name related document by name revision&version
                        BusinessObject businessObject = null;
                        try {

                            String name = lineArray.get(0);
                            String revision = lineArray.get(1);
                            System.out.println("check document by name: " + name + " revision: " + revision);
                            businessObject = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_ExternalDocumentSet, name, revision, IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION);

                            if (businessObject != null && businessObject.exists(ctx)) {
                                System.out.print("document " + name + " exist ");
                                DomainObject domainObject = new DomainObject(businessObject);
                                StringList relatedTasks = domainObject.getInfoList(ctx, "to[IMS_QP_QPTask2Fact].from.name");
                                System.out.println(revision + " relatedTasks: " + relatedTasks.size() + ": " + relatedTasks);
                                StringBuilder builder = new StringBuilder("");
                                if (relatedTasks != null & relatedTasks.size() > 0) {
                                    for (int i = 0; i < relatedTasks.size(); i++) {
                                        builder.append(relatedTasks.get(i));
                                        if (relatedTasks.size() - i > 1) {
                                            builder.append(",");
                                        }
                                    }
                                }
                                lineArray.add(1, UIUtil.isNotNullAndNotEmpty(builder.toString())?builder.toString():"not applicable");
                            } else {
                                lineArray.add(1, "-");
                            }
                        } catch (MatrixException e) {
                            System.out.println("matrix error: " + e.getMessage());
                            e.printStackTrace();

                        } catch (IndexOutOfBoundsException ioe) {
                            System.out.println("empty list: " + lineArray);
                            ioe.printStackTrace();
                        }

                        if (lineArray.size() < cellCountRequired) {
                            lineArray.add("empty description");
                        }
                        if (lineArray.size() == cellCountRequired) {
                            fillSheet(0, rowCount, lineArray);
                            rowCount++;
                        } else {
                            System.out.println("bad doc: " + lineArray);
                        }
                    }
                }
            }
            bufferedReader.close();

        } catch (IOException e) {
            System.out.println("IO exception " + e.getMessage());
            e.printStackTrace();
        }
        //write book to new file
        try {
            date = new Date();
            fileName = "initial_report";
            String toFileName = absolutePath + fileName + "_" + formatter.format(date) + ".xlsx";
            System.out.println("finished to reporting at " + formatter.format(date) + "\nwith new file name: " + toFileName);
            FileOutputStream fos = new FileOutputStream(toFileName);
            wb.write(fos);

            //close streams
            if (fis != null) {
                fis.close();
                fos.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Utility method for filling rows in indexed sheets for report by issue #63784
     *
     * @param sheetIndex
     * @param rowIndex
     * @return
     */
    private void fillSheet(int sheetIndex, int rowIndex, List<String> dataRow) {
        Sheet sheet = wb.getSheetAt(sheetIndex);
        Row row;
        Cell cell;

        row = sheet.createRow(rowIndex);
        System.out.println(rowIndex + ": " + dataRow);
//        [0]  FH1.B.P000.1.030233.01&&&&.011.CA.0001.E
//        [1]  task_name
//        [2]  fd7e382d-ebd8-4572-bc68-d4d604201f51
//        [3]  64032.5386.10544.50562
//        [4]  PreReview_RS
//        [5]  01 _ [6] 1
//        [7]  FALSE  attribute[IMS_Frozen]
//        [8]  TRUE attribute[IMS_IsLast]
//        [9]  Preliminary  attribute[IMS_ProjDocStatus]
//        [10] Is Finalized
//        [11] Description

        cell = row.createCell(0);
        cell.setCellValue(dataRow.get(0));                          //doc name

        cell = row.createCell(1);
        cell.setCellValue(dataRow.get(1));                          //task name

        cell = row.createCell(2);
        cell.setCellValue(dataRow.get(2));                          //revision

        cell = row.createCell(3);
        cell.setCellValue(dataRow.get(3));                          //id

        cell = row.createCell(4);
        cell.setCellValue(dataRow.get(4));                          //current state

        cell = row.createCell(5);
        cell.setCellValue(dataRow.get(5) + "_" + dataRow.get(6));   //rev_ver

        cell = row.createCell(7);
        cell.setCellValue(dataRow.get(7));                          //frozen

        cell = row.createCell(8);
        cell.setCellValue(dataRow.get(8));                          //last

        cell = row.createCell(9);
        cell.setCellValue(dataRow.get(9));                          //doc status

        cell = row.createCell(10);
        cell.setCellValue(dataRow.get(10));                          //is finalized

        cell = row.createCell(11);
        cell.setCellValue(dataRow.get(11));                          //description
    }
}
