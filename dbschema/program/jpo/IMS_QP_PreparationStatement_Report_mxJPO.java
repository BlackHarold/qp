import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_PreparationStatement_Report_mxJPO {
    private static final Logger LOG = Logger.getLogger("reportLogger");

    public Map<?, ?> getPrepare(Context ctx, String type, String... args) {
        String[] cleanedIDs = args; //QP-00SNA
        BusinessObjectWithSelectList data = getByTypeAndSelectedIds(ctx, IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan, cleanedIDs);
        LOG.info("data: " + data);
        Collections.sort(data, new Comparator<BusinessObjectWithSelect>() {
            @Override
            public int compare(BusinessObjectWithSelect bos1, BusinessObjectWithSelect bos2) {

                String bosA = bos1.getSelectData(DomainConstants.SELECT_NAME);
                String bosB = bos2.getSelectData(DomainConstants.SELECT_NAME);
                return bosA.compareTo(bosB);
            }
        });

        Map<String, String> taskMap = new LinkedHashMap<>();
        LOG.info("sorted business object data: " + data);
        for (Object o : data) {
            BusinessObjectWithSelect businessObject = (BusinessObjectWithSelect) o;

            try {
                businessObject.open(ctx);
            } catch (MatrixException e) {
                LOG.error("error opening business object: " + e.getMessage());
                e.printStackTrace();
            }

            RelationshipWithSelectList relationshipWithSelectList = getRelationshipWithSelectList(ctx, businessObject);
            LOG.info("relationships list: " + relationshipWithSelectList);
            RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(relationshipWithSelectList);
            taskMap.putAll(getQPTaskList(businessObject.getSelectData(DomainObject.SELECT_ID), relItr));
        }

        LOG.info("type: " + type + " return taskMap: " + taskMap + " or return getInfoMap: " + getInfoMap(taskMap));
        if (type.equals("SQP")) {
            return taskMap;

        } else {
            return getInfoMap(taskMap);
        }
    }

    private Map<List<String>, List<String>> getInfoMap(Map<String, String> taskMap) {
        Map<List<String>, List<String>> tasksInfoMap = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>();
        List<String> values = new ArrayList<>();
        for (Map.Entry<String, String> entry : taskMap.entrySet()) {
            keys.add(entry.getKey());
            values.add(entry.getValue());
        }
        tasksInfoMap.put(keys, values);
        return tasksInfoMap;
    }

    private BusinessObjectWithSelectList getByTypeAndSelectedIds(Context ctx, String type, String... ids) {
        //Do the query
        BusinessObjectWithSelectList businessObjectList = new BusinessObjectWithSelectList();
        QueryList queryList = new QueryList(); // extends List<Query>
        try {
            for (String id : ids) {
                Query query = getQueryByTypeAndIds(type, id);
                queryList.add(query);
            }

            for (Object o : queryList) {
                Query query = (Query) o;
                businessObjectList.addAll(query.selectTmp(ctx, getBusinessSelect()));
            }
        } catch (MatrixException e) {
            LOG.error("matrix error: " + e.getMessage());
            e.printStackTrace();
        }
        return businessObjectList;
    }

    private Query getQueryByTypeAndIds(String type, String id) {

        //Prepare temp query
        Query query = new Query();
        query.setBusinessObjectType(type);
        query.setOwnerPattern("*");
        query.setVaultPattern(IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION);
        query.setWhereExpression(whereInitiate(id));

        return query;
    }

    private Map<String, String> getQPTaskList(String planId, RelationshipWithSelectItr relItr) {
        Map<String, RelationshipWithSelect> map = new HashMap<>();
        Map<String, RelationshipWithSelect> sortedMap = new LinkedHashMap<>();
        List<String> taskList = new ArrayList<>();

        List<RelationshipWithSelect> relationshipWithSelectList = new ArrayList<>();
        while (relItr.next()) {
            RelationshipWithSelect relSelect = relItr.obj();
            String toName = relSelect.getSelectData(DomainConstants.SELECT_TO_NAME);
            taskList.add(toName);
            map.put(toName, relSelect);
            relationshipWithSelectList.add(relSelect);
        }

        Collections.sort(relationshipWithSelectList, new Comparator<RelationshipWithSelect>() {
            @Override
            public int compare(RelationshipWithSelect t1, RelationshipWithSelect t2) {

                int sortOrder_A = Integer.parseInt(t1.getSelectData("to.attribute[IMS_SortOrder]"));
                int sortOrder_B = Integer.parseInt(t2.getSelectData("to.attribute[IMS_SortOrder]"));
                if (sortOrder_A < sortOrder_B) {
                    return 1;
                } else if (sortOrder_A > sortOrder_B) {
                    return -1;
                }
                return 0;
            }
        });

        Collections.sort(relationshipWithSelectList, new Comparator<RelationshipWithSelect>() {
            @Override
            public int compare(RelationshipWithSelect t1, RelationshipWithSelect t2) {

                int sortOrder_A = Integer.parseInt(t1.getSelectData("to.attribute[IMS_QP_SortLevel]"));
                int sortOrder_B = Integer.parseInt(t2.getSelectData("to.attribute[IMS_QP_SortLevel]"));
                if (sortOrder_A < sortOrder_B) {
                    return 1;
                } else if (sortOrder_A > sortOrder_B) {
                    return -1;
                }
                return 0;
            }
        });

        Collections.reverse(relationshipWithSelectList);
        for (RelationshipWithSelect r : relationshipWithSelectList) {
            String taskName = r.getSelectData("to.name");
            sortedMap.put(taskName, map.get(taskName));
        }

        Map<String, String> badTasks = new LinkedHashMap<>();
        String full = "Full", no = "No";
        for (Map.Entry entry : sortedMap.entrySet()) {
            RelationshipWithSelect relToTask = (RelationshipWithSelect) entry.getValue();

            String toId = relToTask.getSelectData(DomainConstants.SELECT_TO_ID);
            String toName = relToTask.getSelectData(DomainConstants.SELECT_TO_NAME);
            String to_IMSName = UIUtil.isNotNullAndNotEmpty(relToTask.getSelectData("to.attribute[IMS_NameRu]")) ?
                    relToTask.getSelectData("to.attribute[IMS_NameRu]") : "-";
            String toState = relToTask.getSelectData("to.attribute[IMS_QP_CloseStatus]");
            int sortLevel = Integer.parseInt(relToTask.getSelectData("to.attribute[IMS_QP_SortLevel]"));

            if (toState.equals("Full")) {
                LOG.info(toName + " has state Full. Additional info: " + toState + " sort level: " + sortLevel);
                continue;

            } else {
                List<String> toTasksId = Arrays.asList(
                        relToTask.getSelectData("to.to[IMS_QP_QPTask2QPTask].from.id")
                                .split(IMS_QP_Constants_mxJPO.BELL_DELIMITER));
                List<String> toTasksNames = Arrays.asList(
                        relToTask.getSelectData("to.to[IMS_QP_QPTask2QPTask].from.name")
                                .split(IMS_QP_Constants_mxJPO.BELL_DELIMITER));
                List<String> toTasksStates = Arrays.asList(
                        relToTask.getSelectData("to.to[IMS_QP_QPTask2QPTask].from.attribute[IMS_QP_CloseStatus]")
                                .split(IMS_QP_Constants_mxJPO.BELL_DELIMITER));
                List<String> toTasksSortLevel = Arrays.asList(
                        relToTask.getSelectData("to.to[IMS_QP_QPTask2QPTask].from.attribute[IMS_QP_SortLevel]")
                                .split(IMS_QP_Constants_mxJPO.BELL_DELIMITER));

                for (int i = 0; i < toTasksId.size(); i++) {

                    //if only one task without relationships by issue 59788 / comment #9 / point 3.
                    if (toTasksId.size() == 1) {
                        LOG.info("only one task without relationships by issue comment #9 point 3: " + toName);
                        badTasks.put(toName, to_IMSName);
                        continue;
                    }

                    if (sortedMap.containsKey(toTasksNames.get(i))) {
                        int parentTaskSortLevel = Integer.parseInt(toTasksSortLevel.get(i));
                        LOG.info(toName + "->" + toTasksNames.get(i) + "|" + toTasksStates.get(i) + "|"
                                + parentTaskSortLevel +
                                (parentTaskSortLevel == sortLevel ? " = " : parentTaskSortLevel > sortLevel ? " > " : " < ")
                                + sortLevel + " -checkup!");

                        //task has relationship from upper task whose has been state is 'Full'
                        if (parentTaskSortLevel < sortLevel && toTasksStates.get(i).equals("Full")) {
                            LOG.info("task has relationship from upper task whose has been state is 'Full': " + toName);
                            badTasks.put(toName, to_IMSName);
                        }

                        //task has no relationship from upper task & has a state
                    } else if (UIUtil.isNullOrEmpty(toTasksSortLevel.get(i)) && toState.equals("No")) {
                        LOG.info(toName + "->" + toTasksNames.get(i) + "|" + toTasksSortLevel.get(i) + " -skip!");
                        LOG.info("task has no relationship from upper task & has a state: " + toName);
                        badTasks.put(toName, to_IMSName);
                    }
                }

                LOG.info("bad tasks: " + badTasks);
            }
        }

        return badTasks;
    }

    private RelationshipWithSelectList getRelationshipWithSelectList(Context ctx, BusinessObjectWithSelect businessObject) {

        // Instantiating the BusinessObject
        StringList selectBusStmts = new StringList();
        selectBusStmts.addElement(DomainConstants.SELECT_ID);
        selectBusStmts.addElement(DomainConstants.SELECT_TYPE);
        selectBusStmts.addElement(DomainConstants.SELECT_NAME);

        StringList selectRelStmts = new StringList();
        selectRelStmts.addElement(DomainConstants.SELECT_NAME);
        selectRelStmts.addElement(DomainConstants.SELECT_FROM_ID);
        selectRelStmts.addElement(DomainConstants.SELECT_FROM_NAME);
        selectRelStmts.addElement(DomainConstants.SELECT_TO_ID);
        selectRelStmts.addElement(DomainConstants.SELECT_TO_NAME);
        selectRelStmts.addElement("from.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.attribute[IMS_QP_CloseStatus]");
        selectRelStmts.addElement("to.attribute[IMS_QP_SortLevel]");
        selectRelStmts.addElement("to.attribute[IMS_SortOrder]");
        selectRelStmts.addElement("to.attribute[IMS_QP_SortInfo]");
        selectRelStmts.addElement("to.to[IMS_QP_QPTask2QPTask].from.name");
        selectRelStmts.addElement("to.to[IMS_QP_QPTask2QPTask].from.id");
        selectRelStmts.addElement("to.to[IMS_QP_QPTask2QPTask].from.attribute[IMS_QP_CloseStatus]");
        selectRelStmts.addElement("to.to[IMS_QP_QPTask2QPTask].from.attribute[IMS_QP_SortLevel]]");
        selectRelStmts.addElement("to.attribute[IMS_Name]");
        selectRelStmts.addElement("to.attribute[IMS_NameRu]");
        selectRelStmts.addElement("to.to[IMS_QP_QPTask2QPTask].from.attribute[IMS_QP_CloseStatus]");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.name");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.current");
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.type"); //check CL & VTZ
        selectRelStmts.addElement("to.from[IMS_QP_QPTask2Fact].to.attribute[IMS_ProjDocStatus]");

        ExpansionWithSelect expansion = null;
        try {
            expansion = businessObject.expandSelect(ctx,
                    IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask,
                    DomainConstants.QUERY_WILDCARD,
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
//        RelationshipWithSelectItr relItr = new RelationshipWithSelectItr(relationshipWithSelectList);
        return relationshipWithSelectList;
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

        //DEP
        busSelect.addElement(String.format("to[%s].from.name",
                IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan));
        //Classifier
        busSelect.addElement(String.format("from[%s].to.name", IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2Classifier));
        busSelect.addElement(String.format("to[%s].from.name", IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2Classifier));
        busSelect.addElement(String.format("to[%s].from.name", IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan));
        busSelect.addElement(String.format("from[%s].to.name", IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan));

        return busSelect;
    }

    private String whereInitiate(String id) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id==" + id);

        return stringBuilder.toString();
    }
}
