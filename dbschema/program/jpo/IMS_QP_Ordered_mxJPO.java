import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.BusinessObject;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.JPO;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class IMS_QP_Ordered_mxJPO {

    static final String BELL_DELIMITER = "\\u0007";


    public void sort(Context context, String[] args) throws Exception {
        HashMap programMap = (HashMap) JPO.unpackArgs(args);
        String[] selectedItemsList = (String[]) programMap.get("emxTableRowId");
        String type = (String) programMap.get("type");
        Map<String, HashMap<String, ArrayList<String>>> errorList = new HashMap();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
        for (String i:selectedItemsList) {
            String[] tmp = i.split("\\|");
            Map map = new HashMap();
            map.put("objectId", tmp[1]);
            DomainObject dObj = new DomainObject(tmp[1]);
            String eventTime = dateFormat.format(new Date().getTime());
            dObj.setAttributeValue(context, "IMS_QP_SortInfo", "In progress " + eventTime);
        }

        for (String i:selectedItemsList) {
            Map<String, HashMap<String, ArrayList<String>>> baselineGroup = new HashMap();
            ArrayList<String> allIdsTask = new ArrayList<>();
            ArrayList<String> allIdsTaskPermanent = new ArrayList<>();
            Boolean error = false;
            String[] tmp = i.split("\\|");
            Map map = new HashMap();
            map.put("objectId", tmp[1]);
            DomainObject dObj = new DomainObject(tmp[1]);
            String eventTime = dateFormat.format(new Date().getTime());
            dObj.setAttributeValue(context, "IMS_QP_SortInfo", "Start " + eventTime);
            String nameSelectItem = dObj.getInfo(context, DomainObject.SELECT_NAME);
            MapList mapList = new MapList();
            if (type.equals("DEP")) {
                mapList = getAllRelatedTasksForTable(context, JPO.packArgs(map));
            }
            if (type.equals("SQP")) {
                mapList = getRelatedQPTask(context, JPO.packArgs(map));
            }

            for (Object obj : mapList) {
                Map mapDep = (Map) obj;
                String id = (String) mapDep.get(DomainObject.SELECT_ID);
                String name = (String) mapDep.get(DomainObject.SELECT_NAME);
                String baseline = "";
                String ims_sort = "";
                if (type.equals("DEP")) {
                    baseline = (String) mapDep.get("to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.id");
                    ims_sort = (String) mapDep.get("to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.attribute[IMS_Sort]");
                }
                if (type.equals("SQP")) {
                    baseline = (String) mapDep.get("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.id");
                    ims_sort = (String) mapDep.get("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.attribute[IMS_Sort]");
                }

                if (UIUtil.isNotNullAndNotEmpty(baseline)) {
                    if (baseline.split(BELL_DELIMITER).length>1) {
                        error = true;
                        eventTime = dateFormat.format(new Date().getTime());
                        dObj.setAttributeValue(context, "IMS_QP_SortInfo", "Update error " + eventTime);
                        addInfoToErrorList(errorList, nameSelectItem, name, "Has baseline > 1");
                    } else {

                    }
                } else {
                    error = true;
                    eventTime = dateFormat.format(new Date().getTime());
                    dObj.setAttributeValue(context, "IMS_QP_SortInfo", "Update error " + eventTime);
                    addInfoToErrorList(errorList, nameSelectItem, name, "Doesn't have baseline");
                }
                String stage = (String) mapDep.get("to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");
                if (UIUtil.isNullOrEmpty(stage)) {
                    stage = "0";
                }
                String baselineStage = ims_sort + stage + "_" + baseline;
                if (baselineGroup.containsKey(baselineStage)) {
                    HashMap<String, ArrayList<String>> hashMap = baselineGroup.get(baselineStage);
                    ArrayList<String> arrayList = hashMap.get("NotFirst");
                    arrayList.add(id);
                    allIdsTask.add(id + "_" + baselineStage);
                    allIdsTaskPermanent.add(id);
                } else {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(id);
                    allIdsTask.add(id + "_" + baselineStage);
                    allIdsTaskPermanent.add(id);
                    HashMap<String, ArrayList<String>> hashMap = new HashMap<>();
                    hashMap.put("First", new ArrayList<>());
                    hashMap.put("NotFirst", arrayList);
                    baselineGroup.put(baselineStage, hashMap);
                }
            }

            ArrayList<String> allIdsTaskNotPermanent = new ArrayList<>(allIdsTaskPermanent);
            ArrayList<String> finalWithoutDouble = new ArrayList<>();
            if (error==false) {
                SortedSet<String> keys = new TreeSet<String>(baselineGroup.keySet());
                for (String key:keys) {
                    HashMap<String, ArrayList<String>> hashMap = baselineGroup.get(key);
                    ArrayList<String> idsFromKey = hashMap.get("NotFirst");
                    ArrayList<String> firstItem = hashMap.get("First");
                    removeBaselineStage(allIdsTask, idsFromKey, key);
                    removeBaselineStage(allIdsTask, firstItem, key);
                    for (String mbFirstId : idsFromKey) {
                        StringList select = new StringList();
                        if (type.equals("DEP")) {
                            select.add("from[IMS_QP_DEPTask2DEPTask].to.id");
                            select.add("to[IMS_QP_DEPTask2DEPTask].from.id");
                        }
                        if (type.equals("SQP")) {
                            select.add("from[IMS_QP_QPTask2QPTask].to.id");
                            select.add("to[IMS_QP_QPTask2QPTask].from.id");
                        }
                        MapList domObjMapList = DomainObject.getInfo(context, new String[]{mbFirstId}, select);
                        String input = "";
                        String output = "";
                        for (Object domObjObj : domObjMapList) {
                            Map domObj = (Map) domObjObj;
                            if (type.equals("DEP")) {
                                input = (String) domObj.get("to[IMS_QP_DEPTask2DEPTask].from.id");
                                output = (String) domObj.get("from[IMS_QP_DEPTask2DEPTask].to.id");
                            }
                            if (type.equals("SQP")) {
                                input = (String) domObj.get("to[IMS_QP_QPTask2QPTask].from.id");
                                output = (String) domObj.get("from[IMS_QP_QPTask2QPTask].to.id");
                            }
                        }
                        if (!(UIUtil.isNullOrEmpty(output) && UIUtil.isNullOrEmpty(input))) {
                            if (UIUtil.isNotNullAndNotEmpty(input)) {
                                if (!hasInputFrom(input, idsFromKey)) {
                                    if (!hasInAllIdTask(input, key, allIdsTask)) {
                                        if (!input.equals(mbFirstId)) {
                                            if (UIUtil.isNotNullAndNotEmpty(output)) {
                                                if (!output.equals(mbFirstId)) {
                                                    firstItem.add(mbFirstId);
                                                }
                                            }
                                        }

                                    }
                                }
                            } else {
                                firstItem.add(mbFirstId);
                            }
                        }
                    }


                    ArrayList<String> firstLine = new ArrayList<>();
                    ArrayList<ArrayList<String>> matrixResult = new ArrayList<>();
                    matrixResult.add(firstLine);
                    idsFromKey.removeAll(firstItem);

                    ArrayList<String> currentArray = new ArrayList<>();
                    if (!idsFromKey.isEmpty()) {
                        currentArray.addAll(idsFromKey);
                    }
                    if (!firstItem.isEmpty()) {
                        currentArray.addAll(firstItem);
                    }

                    if (firstItem.size()>0) {
                        if (firstItem.size()>1) {
                            sortByArrayList(context, firstLine, firstItem, currentArray, type);
                        } else {
                            firstLine.add(firstItem.get(0));
                        }
                    } else {
                        sortByArrayList(context, firstLine, idsFromKey, currentArray, type);
                        idsFromKey.clear();
                    }

                    allIdsTaskNotPermanent.remove(matrixResult.get(matrixResult.size()-1));
                    ArrayList<String> prevStep = matrixResult.get(matrixResult.size()-1);

                    if (!(checkEndlessCycle(context, new ArrayList<String>(), prevStep, currentArray, type, dObj, errorList, nameSelectItem))) {
                        getOutput(context, matrixResult, prevStep, currentArray, allIdsTaskNotPermanent, idsFromKey, type);
                    } else {
                        error = true;
                        break;
                    }


                    if (!idsFromKey.isEmpty()) {
                        ArrayList<String> currentTempSort = new ArrayList<>();
                        sortByArrayList(context, currentTempSort, idsFromKey, currentArray, type);
                        matrixResult.add(currentTempSort);
                        idsFromKey.clear();
                    }


                    ArrayList<String> tempWithoutDouble = new ArrayList<>();
                    Collections.reverse(matrixResult);
                    for (ArrayList<String> arrLst : matrixResult)  {
                        Collections.reverse(arrLst);
                        for (String str : arrLst) {
                            if (!tempWithoutDouble.contains(str)) {
                                tempWithoutDouble.add(str);
                            }
                        }
                    }

                    Collections.reverse(tempWithoutDouble);

                    for (String idFin : tempWithoutDouble) {
                        finalWithoutDouble.add(idFin);
                    }
                }
                int ims_SortOrder = 1000;
                for (String idItem : finalWithoutDouble) {
                    DomainObject domObj = new DomainObject(idItem);
                    domObj.setAttributeValue(context, "IMS_SortOrder", String.valueOf(ims_SortOrder));
                    ims_SortOrder++;
                }
                if (error==false) {
                    String eventTimeEnd = dateFormat.format(new Date().getTime());
                    dObj.setAttributeValue(context, "IMS_QP_SortInfo", "Updated " + eventTimeEnd);
                }
            }
        }
        if (!errorList.isEmpty()) {
            createReportUnit(context, errorList, type);
        }

    }


    public MapList getAllRelatedTasksForTable(Context context, String[] args) throws Exception {

        MapList result = new MapList();
        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");

        StringList selects = new StringList(DomainObject.SELECT_ID);
        selects.add(DomainObject.SELECT_NAME);
        selects.add("to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.id");
        selects.add("to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.attribute[IMS_Sort]");
        selects.add("to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");

        //get all tasks
        result.addAll(DomainObject.findObjects(context,
                /*type*/"IMS_QP_DEPTask",
                "eService Production",
                /*where*/"to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_DEPProjectStage2DEPSubStage].from.to[IMS_QP_DEP2DEPProjectStage].from.id==" + objectId,
                /*selects*/ selects));
        return result;
    }

    public MapList getRelatedQPTask(Context context, String[] args) throws Exception {
        MapList result = new MapList();
        Map argsMap = JPO.unpackArgs(args);

        //get objectID
        String objectId = (String) argsMap.get("objectId");

        StringList selects = new StringList(DomainObject.SELECT_ID);
        selects.add(DomainObject.SELECT_NAME);
        selects.add("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.id");
        selects.add("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.attribute[IMS_Sort]");
        selects.add("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");

        //get all tasks
        result.addAll(DomainObject.findObjects(context,
                /*type*/"IMS_QP_QPTask",
                "eService Production",
                /*where*/"to[IMS_QP_QPlan2QPTask].from.id==" + objectId,
                /*selects*/ selects));
        return result;
    }

    public boolean hasInputFrom(String input, ArrayList<String> notFirst) {
        boolean result = false;
        String[] inputId = input.split(BELL_DELIMITER);
        ArrayList<String> checkId = new ArrayList<>();
        for (String str : inputId) {
            checkId.add(str);
        }
        int arrSize1 = checkId.size();
        checkId.removeAll(notFirst);
        int arrSize2 = checkId.size();
        if (arrSize1!=arrSize2) {
            result = true;
        }
        return result;
    }

    public void removeBaselineStage (ArrayList<String > allTask, ArrayList<String> id, String key) {
        ArrayList<String> forDelete = new ArrayList<>(id);
        for (String i : id) {
            i = i + "_" + key;
            forDelete.add(i);
        }
        allTask.removeAll(forDelete);
    }

    public boolean hasInAllIdTask (String input, String key, ArrayList<String> allTask) {
        boolean result = false;
        String[] inputId = input.split(BELL_DELIMITER);
        ArrayList<String> checkId = new ArrayList<>();
        for (String str : inputId) {
            checkId.add(str + "_" + key);
        }
        int arrSize1 = checkId.size();
        checkId.removeAll(allTask);
        int arrSize2 = checkId.size();
        if (arrSize1!=arrSize2) {
            result = true;
        }

        return result;
    }

    public void sortByArrayList (Context context, ArrayList<String> forResult, ArrayList<String> sort, ArrayList<String> current, String type) throws FrameworkException {
        Map<String, Map<String, ArrayList<String>>> tempForSort = new HashMap();
        for (String id:sort) {
            StringList select = new StringList();
            if (type.equals("DEP")) {
                select.add("from[IMS_QP_DEPTask2DEPTask].to.id");
                select.add("to[IMS_QP_DEPTask2DEPTask].from.id");
            }
            if (type.equals("SQP")) {
                select.add("from[IMS_QP_QPTask2QPTask].to.id");
                select.add("to[IMS_QP_QPTask2QPTask].from.id");
            }


            MapList domObjMapList = DomainObject.getInfo(context, new String[]{id}, select);
            String input = "";
            for (Object domObjObj : domObjMapList) {
                Map domObj = (Map) domObjObj;
                if (type.equals("DEP")) {
                    input = (String) domObj.get("to[IMS_QP_DEPTask2DEPTask].from.id");
                }
                if (type.equals("SQP")) {
                    input = (String) domObj.get("to[IMS_QP_QPTask2QPTask].from.id");
                }

            }
            if (UIUtil.isNotNullAndNotEmpty(input)) {
                String[] inputId = input.split(BELL_DELIMITER);
                String minBaslineAndStage = "";
                String minNameInput = "";
                for (String str : inputId) {
                    if (current.contains(str)) {
                        StringList selects = new StringList(DomainObject.SELECT_ID);
                        if (type.equals("DEP")) {
                            selects.add("to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.attribute[IMS_Sort]");
                            selects.add("to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");
                        }
                        if (type.equals("SQP")) {
                            selects.add("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.attribute[IMS_Sort]");
                            selects.add("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");
                        }
                        selects.add(DomainObject.SELECT_NAME);
                        MapList domObjMapListInput = DomainObject.getInfo(context, new String[]{str}, selects);
                        for (Object domObjObj : domObjMapListInput) {
                            Map domObj = (Map) domObjObj;
                            String ims_sort = "";
                            String stage = "";
                            String name = "";
                            if (type.equals("DEP")) {
                                ims_sort = (String) domObj.get("to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.attribute[IMS_Sort]");
                                stage = (String) domObj.get("to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");
                                name = (String) domObj.get(DomainObject.SELECT_NAME);
                            }
                            if (type.equals("SQP")) {
                                ims_sort = (String) domObj.get("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.attribute[IMS_Sort]");
                                stage = (String) domObj.get("to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]");
                                String nameTemp = (String) domObj.get(DomainObject.SELECT_NAME);
                                String[] nameArr = nameTemp.split("_");
                                name = nameArr[1];
                            }


                            if (UIUtil.isNullOrEmpty(stage)) {
                                stage = "0";
                            }

                            if (UIUtil.isNotNullAndNotEmpty(ims_sort)) {
                                if (ims_sort.split(BELL_DELIMITER).length>1) {
                                    minBaslineAndStage = "99999.00";
                                    minNameInput = name;
                                } else {
                                    minBaslineAndStage = ims_sort + stage;
                                    minNameInput = name;
                                }
                            } else {
                                minBaslineAndStage = "99999.00";
                                minNameInput = name;
                            }
                        }
                    }
                }


                if (tempForSort.containsKey(minBaslineAndStage)) {
                    Map<String, ArrayList<String>> map = tempForSort.get(minBaslineAndStage);
                    if (map.containsKey(minNameInput)) {
                        ArrayList<String> arrayList = map.get(minNameInput);
                        arrayList.add(id);
                    } else {
                        ArrayList<String> arrayList = new ArrayList<>();
                        arrayList.add(id);
                        map.put(minNameInput, arrayList);
                    }
                } else {
                    Map<String, ArrayList<String>> map = new HashMap<>();
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(id);
                    map.put(minNameInput, arrayList);
                    tempForSort.put(minBaslineAndStage, map);
                }
            } else {
                if (tempForSort.containsKey("99999.00")) {
                    Map<String, ArrayList<String>> map = tempForSort.get("99999.00");
                    ArrayList<String> arrayList = map.get("empty");
                    arrayList.add(id);
                } else {
                    ArrayList<String> arrayList = new ArrayList<>();
                    arrayList.add(id);
                    Map<String, ArrayList<String>> map = new HashMap<>();
                    map.put("empty", arrayList);
                    tempForSort.put("99999.00", map);
                }
            }

        }

        SortedSet<String> keys = new TreeSet<String>(tempForSort.keySet());

        for (String key : keys) {
            Map<String, ArrayList<String>> map = tempForSort.get(key);
            SortedSet<String> keysName = new TreeSet<String>(map.keySet());
            for (String keyName : keysName) {
                ArrayList<String> ids = map.get(keyName);
                Map<String, String> nameId = new HashMap<>();
                if (ids.size()>1) {
                    String[] arr = ids.toArray(new String[ids.size()]);
                    StringList select = new StringList(DomainObject.SELECT_NAME);
                    select.add(DomainObject.SELECT_ID);
                    MapList domObjMapList = DomainObject.getInfo(context, arr, select);
                    for (Object obj : domObjMapList) {
                        Map mapObj = (Map) obj;
                        String idObj = (String) mapObj.get(DomainObject.SELECT_ID);
                        String nameObj = (String) mapObj.get(DomainObject.SELECT_NAME);
                        nameId.put(nameObj, idObj);
                    }
                    SortedSet<String> keysNameObj = new TreeSet<String>(nameId.keySet());
                    for (String k : keysNameObj) {
                        forResult.add(nameId.get(k));
                    }
                } else {
                    forResult.add(ids.get(0));
                }
            }
        }
    }


    public boolean isEmptyArray (Map<String, HashMap<String, ArrayList<String>>> baselineGroup, String key) {

            HashMap<String, ArrayList<String>> hashMap = baselineGroup.get(key);
            ArrayList<String> idsFromKey = hashMap.get("NotFirst");
            if (!idsFromKey.isEmpty()) {
                return false;
            }

        return true;
    }

    public void getOutput (Context context, ArrayList<ArrayList<String>> matrixResult, ArrayList<String> prevStep, ArrayList<String> currentArray, ArrayList<String> allIdsTaskNotPermanent, ArrayList<String> idsFromKey, String type) throws FrameworkException {
        ArrayList<ArrayList<String>> currentTempAll = new ArrayList<>();
        for (String id:prevStep) {
            ArrayList<String> currentTemp = new ArrayList<>();
            currentTempAll.add(currentTemp);

            StringList select = new StringList();
            if (type.equals("DEP")) {
                select.add("from[IMS_QP_DEPTask2DEPTask].to.id");
            }
            if (type.equals("SQP")) {
                select.add("from[IMS_QP_QPTask2QPTask].to.id");
            }
            MapList domObjMapList = DomainObject.getInfo(context, new String[]{id}, select);
            String output = "";
            for (Object domObjObj : domObjMapList) {
                Map domObj = (Map) domObjObj;
                if (type.equals("DEP")) {
                    output = (String) domObj.get("from[IMS_QP_DEPTask2DEPTask].to.id");
                }
                if (type.equals("SQP")) {
                    output = (String) domObj.get("from[IMS_QP_QPTask2QPTask].to.id");
                }


                if (UIUtil.isNotNullAndNotEmpty(output)) {
                    String[] outputId = output.split(BELL_DELIMITER);
                    for (String str : outputId) {
                        if (currentArray.contains(str)&&!(id.equals(str))) {
                            currentTemp.add(str);
                        }
                    }
                }
            }
        }
        boolean isEmptyArr = true;
        for (ArrayList<String> arr : currentTempAll) {
            if (!arr.isEmpty()) {
                isEmptyArr = false;
                break;
            }
        }

        if (!isEmptyArr) {
            ArrayList<String> nextStep = new ArrayList<>();
            for (ArrayList<String> arr : currentTempAll) {
                ArrayList<String> currentTempSort = new ArrayList<>();
                sortByArrayList(context, currentTempSort, arr, currentArray, type);
                matrixResult.add(currentTempSort);
                idsFromKey.removeAll(currentTempSort);
                nextStep.addAll(currentTempSort);
            }
            getOutput(context, matrixResult, nextStep, currentArray, allIdsTaskNotPermanent, idsFromKey, type);
        }
    }

    boolean checkEndlessCycle (Context context, ArrayList<String> checkEndlessCycleArr, ArrayList<String> prevStep,
                               ArrayList<String> currentArray, String type, DomainObject dObj, Map<String, HashMap<String,
                                ArrayList<String>>> errorList, String nameSelectItem) throws FrameworkException {
        for (String id:prevStep) {
            ArrayList<String> currentTemp = new ArrayList<>(checkEndlessCycleArr);
            if (currentTemp.contains(id)) {
                currentTemp.add(id);
                StringList select = new StringList(DomainObject.SELECT_NAME);
                String[] ids = new String[currentTemp.size()];
                int i =0;
                for (String str : currentTemp) {
                    ids[i] = str;
                    i++;
                }
                MapList domObjMapList = DomainObject.getInfo(context, ids, select);
                StringBuilder stringBuilder = new StringBuilder();
                String prefix = "";
                for (Object obj : domObjMapList) {
                    Map map = (Map) obj;
                    String name = (String) map.get(DomainObject.SELECT_NAME);
                    stringBuilder.append(prefix);
                    stringBuilder.append(name);
                    prefix = " -> ";
                }
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm");
                String eventTime = dateFormat.format(new Date().getTime());
                dObj.setAttributeValue(context, "IMS_QP_SortInfo", "Update error " + eventTime);
                addInfoToErrorList(errorList, nameSelectItem, stringBuilder.toString(), "Endless cycle");
                return true;
            }
            currentTemp.add(id);


            StringList select = new StringList();
            if (type.equals("DEP")) {
                select.add("from[IMS_QP_DEPTask2DEPTask].to.id");
            }
            if (type.equals("SQP")) {
                select.add("from[IMS_QP_QPTask2QPTask].to.id");
            }
            MapList domObjMapList = DomainObject.getInfo(context, new String[]{id}, select);
            String output = "";
            for (Object domObjObj : domObjMapList) {
                Map domObj = (Map) domObjObj;
                if (type.equals("DEP")) {
                    output = (String) domObj.get("from[IMS_QP_DEPTask2DEPTask].to.id");
                }
                if (type.equals("SQP")) {
                    output = (String) domObj.get("from[IMS_QP_QPTask2QPTask].to.id");
                }

                ArrayList<String> nextStep = new ArrayList<>();
                if (UIUtil.isNotNullAndNotEmpty(output)) {
                    String[] outputId = output.split(BELL_DELIMITER);
                    for (String str : outputId) {
                        if (currentArray.contains(str)&&!(id.equals(str))) {
                            nextStep.add(str);
                        }
                    }
                }
                boolean result = checkEndlessCycle(context, currentTemp, nextStep, currentArray, type, dObj, errorList, nameSelectItem);
                if (result) {
                    return result;
                }
            }
        }

        return false;
    }

    public void addInfoToErrorList(Map<String, HashMap<String, ArrayList<String>>> errorList, String nameSelectItem,  String name, String reason) {
        if (!errorList.containsKey(nameSelectItem)) {
            ArrayList<String> arrList = new ArrayList<>();
            HashMap<String, ArrayList<String>> hasMap = new HashMap<>();
            arrList.add(name);
            hasMap.put(reason, arrList);
            errorList.put(nameSelectItem, hasMap);
        } else {
            HashMap<String, ArrayList<String>> hasMap = errorList.get(nameSelectItem);
            if (!hasMap.containsKey(reason)) {
                ArrayList<String> arrList = new ArrayList<>();
                arrList.add(name);
                hasMap.put(reason, arrList);
            } else {
                ArrayList<String> arrList = hasMap.get(reason);
                arrList.add(name);
            }
        }
    }


    public void createReportUnit(Context ctx, Map<String, HashMap<String, ArrayList<String>>> errorList, String reportType) {

        //Create object of ReportUnit type
        String objectId = null;
        String reportName = null;
        String vault = ctx.getVault().getName();
        DomainObject reportsContainerObject = null, reportObject = null;
        try {
            MapList reportsByType = DomainObject.findObjects(ctx,
                    IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit,
                    IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    "name smatch *SORT_" + reportType + "*",
                    new StringList(DomainConstants.SELECT_ID));
            int reportsCount = reportsByType.size();

            BusinessObject boReportContainerObject = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_Reports, "Reports", "-", ctx.getVault().getName());
            reportsContainerObject = new DomainObject(boReportContainerObject);

            BusinessObject boReportUnit;
            do {
                reportName = "sort_" + reportType + "_report_" + ++reportsCount;
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
            frameworkException.printStackTrace();
        } catch (MatrixException matrixException) {
            matrixException.printStackTrace();
        }
        try {
            if (reportsContainerObject != null && reportObject != null) {
                reportObject.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_FILE_CHECKIN_STATUS, "Not ready yet");
                IMS_KDD_mxJPO.connectIfNotConnected(ctx, IMS_QP_Constants_mxJPO.relationship_IMS_QP_Reports2ReportUnit, reportsContainerObject, reportObject);
            }
            objectId = reportObject.getId(ctx);

        } catch (Exception e) {
        }

        //Get report
        Workbook workbook = createReport(ctx, errorList, reportType);

        //Write report to the file and upload file to the Business object
        if (UIUtil.isNotNullAndNotEmpty(reportName) && UIUtil.isNotNullAndNotEmpty(objectId)) {
            IMS_QP_CheckInOutFiles_mxJPO checkInOutFiles = new IMS_QP_CheckInOutFiles_mxJPO();
            File file = checkInOutFiles.writeToFile(ctx, reportName, workbook);
            boolean checkin = checkInOutFiles.checkIn(ctx, objectId, file);

            try {
                reportObject.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.IMS_QP_FILE_CHECKIN_STATUS, "Ready");
            } catch (FrameworkException e) {
                e.printStackTrace();
            }
        }
    }

    private Workbook createReport(Context context, Map<String, HashMap<String, ArrayList<String>>> errorList, String reportType) {
        Workbook wb = null;
        try {
            wb = new XSSFWorkbook(IMS_QP_Constants_mxJPO.SORT_REPORT_TEMPLATE_PATH);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        Sheet sheetTemp = wb.getSheet("Name");
        int indextemp = wb.getSheetIndex(sheetTemp);
        SortedSet<String> keys = new TreeSet<String>(errorList.keySet());
        for (String key : keys) {
            Sheet sheet = wb.cloneSheet(indextemp);
            int index = wb.getSheetIndex(sheet);
            wb.setSheetName(index, key);
            HashMap<String, ArrayList<String>> hashMap = errorList.get(key);
            SortedSet<String> keysReason = new TreeSet<String>(hashMap.keySet());
            for (String keyReason : keysReason) {
                ArrayList<String> arrayList = hashMap.get(keyReason);
                int lastRowCount = sheet.getLastRowNum();
                Row row = sheet.createRow(++lastRowCount);
                row.createCell(0).setCellValue(keyReason);
                String prefix = "";
                StringBuilder stringBuilder = new StringBuilder();
                for (String str : arrayList) {
                    stringBuilder.append(prefix);
                    prefix = ", ";
                    stringBuilder.append(str);
                }
                row.createCell(1).setCellValue(stringBuilder.toString());
                CellStyle cellStyle = wb.createCellStyle();
                cellStyle.setWrapText(true);
                row.getCell(1).setCellStyle(cellStyle);
            }
        }
        wb.removeSheetAt(indextemp);
        return wb;
    }

}
