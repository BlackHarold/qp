import com.google.common.html.HtmlEscapers;
import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.DomainRelationship;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.FrameworkUtil;
import com.matrixone.apps.domain.util.MapList;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.*;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;

public class IMS_QP_ExpectedResult_mxJPO {
    private static final Logger LOG = LogManager.getLogger("IMS_QP_DEP");

    public HashMap deleteExpectedResults(Context ctx, String... args) {
        HashMap mapMessage = new HashMap();

        //get all ids
        Map<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = new String[0];
        if (argsMap.get("emxTableRowId") != null) {
            rowIDs = (String[]) argsMap.get("emxTableRowId");
        }

        String[] expectedResultIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            rowIDs[i] = rowIDs[i].substring(rowIDs[i].indexOf("|"), rowIDs[i].lastIndexOf("|"));
            expectedResultIDs[i] = rowIDs[i].substring(1, rowIDs[i].lastIndexOf("|"));
        }

        LOG.info("deleteExpectedResults array ids: " + Arrays.asList(expectedResultIDs));

        StringList commands = new StringList();
        for (int i = 0; i < expectedResultIDs.length; i++) {
            String expectedResultId = expectedResultIDs[i];

//            self delete command
            commands.add(String.format("delete bus %s", expectedResultId));

            try {
                DomainObject expectedResultObject = new DomainObject(expectedResultId);
                LOG.info(expectedResultId + ": " + expectedResultObject.getType(ctx) + " " + expectedResultObject.getName(ctx) + " " + expectedResultObject.getRevision(ctx));
                String expectedResultName = expectedResultObject.getName(ctx);
                MapList sameNamedTasks = DomainObject.findObjects(ctx,
                        /*type*/ IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask
                                + "," + IMS_QP_Constants_mxJPO.type_IMS_QP_DEPTask,
                        /*vault*/IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                        /*where*/"name=='" + expectedResultName + "'",
                        /*selects*/ new StringList(DomainConstants.SELECT_ID));
                if (sameNamedTasks != null && sameNamedTasks.size() > 0) {

                    //relates same named task delete command
                    for (Object o : sameNamedTasks) {
                        Map map = (Map) o;
                        DomainObject object = new DomainObject((String) map.get(DomainConstants.SELECT_ID));
                        LOG.info("same named task: " + map.get(DomainConstants.SELECT_ID) + ": " + object.getType(ctx) + " " + object.getName(ctx) + " " + object.getRevision(ctx));
                        String command = String.format("delete bus %s", map.get(DomainConstants.SELECT_ID));
                        commands.add(command);
                    }
                }

            } catch (FrameworkException e) {
                LOG.error("framework error: " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                LOG.error("error getting object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        //execute
        LOG.info("executed command: " + commands.size() + " " + commands);
        for (int j = 0; j < commands.size(); j++) {
            try {
                MQLCommand.exec(ctx, (String) commands.get(j));
            } catch (MatrixException e) {
                LOG.error("error executing command: " + commands.get(j) + " message: " + e.getMessage());
                e.printStackTrace();
            }
        }

        if (mapMessage.isEmpty()) {
            mapMessage.put("message", "status OK code 200");
        }

        return mapMessage;
    }

    public HashMap deleteExpectedResultsQP(Context ctx, String... args) {
        HashMap mapMessage = new HashMap();

        //get all ids
        Map<String, Object> argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error: " + e.getMessage());
            e.printStackTrace();
        }

        String[] rowIDs = (String[]) argsMap.get("emxTableRowId");

        String[] expectedResultIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            rowIDs[i] = rowIDs[i].substring(rowIDs[i].indexOf("|"), rowIDs[i].lastIndexOf("|"));
            expectedResultIDs[i] = rowIDs[i].substring(1, rowIDs[i].lastIndexOf("|"));
        }

        String[] rawString = rowIDs[0].split("\\|");
        String parentID = rawString[2];

        try {
            DomainObject parent = new DomainObject(parentID);

//            task selects
            StringList selects = new StringList(DomainConstants.SELECT_ID);
            selects.add(DomainConstants.SELECT_NAME);
            MapList relatedResults = parent.getRelatedObjects(ctx,
                    /*relationship*/IMS_QP_Constants_mxJPO.relationship_IMS_QP_ExpectedResult2QPTask,
                    /*type*/ IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult,
                    /*object attributes*/ selects,
                    /*relationship selects*/ null,
                    /*getTo*/ true, /*getFrom*/ true,
                    /*recurse to level*/ (short) 1,
                    /*object where*/ null,
                    /*relationship where*/ null,
                    /*limit*/ 0);
            Map<String, String> relatedExpectedResultIDs = new HashMap<>();

            //old rules highlighting alone output. it's still working
            Map<String, Object> mapLonelyResults = new HashMap<>();
            for (int i = 0; i < relatedResults.size(); i++) {
                Map expectedResult = (Map) relatedResults.get(i);
                String expectedResultId = (String) expectedResult.get(DomainConstants.SELECT_ID);
                DomainObject domainObject = new DomainObject(expectedResultId);
                boolean isAloneOutput = domainObject.getInfoList(ctx, "to[IMS_QP_ExpectedResult2QPTask].from.id").size() == 1;

                if (!isAloneOutput) {
                    relatedExpectedResultIDs.put(
                            (String) expectedResult.get(DomainConstants.SELECT_ID),
                            (String) expectedResult.get(DomainConstants.SELECT_NAME));
                } else {
                    mapLonelyResults.put(domainObject.getName(ctx), "You can't delete the alone 'output' for " + domainObject.getInfo(ctx, "to[IMS_QP_ExpectedResult2QPTask].from.name"));
                }
            }

            //check table row ids and then delete
            for (int i = 0; i < expectedResultIDs.length; i++) {
                DomainObject candidateToRemove = new DomainObject(expectedResultIDs[i]);
                String candidateToRemoveName = candidateToRemove.getName(ctx);

                StringList planNames = candidateToRemove.getInfoList(ctx, "to[IMS_QP_ExpectedResult2QPTask].from.to[IMS_QP_QPlan2QPTask].from.name");
                boolean isExternalInitialData = false;
                for (Object o : planNames) {
                    String s = (String) o;
                    if (s.contains("ExternalInitialData")) isExternalInitialData = true;
                    MapList mapList = DomainObject.findObjects(ctx,
                            IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                            candidateToRemoveName,
                            "",
                            new StringList(DomainConstants.SELECT_ID)
                    );

                    if (mapList != null && mapList.size() > 0) {
                        Map map = (Map) mapList.get(0);
                        String sameNamedTaskId = (String) map.get(DomainConstants.SELECT_ID);
                        MqlUtil.mqlCommand(ctx, String.format("delete bus %s", sameNamedTaskId));
                    }
                }

                if (!isExternalInitialData && mapLonelyResults.containsKey(candidateToRemoveName)) {
                    mapMessage.put(candidateToRemoveName, mapLonelyResults.get(candidateToRemoveName));
                } else if (isExternalInitialData || relatedExpectedResultIDs.containsKey(expectedResultIDs[i])) {
                    //removing by trigger
//                    MqlUtil.mqlCommand(ctx, String.format("delete bus %s", expectedResultIDs[i]));
                } else {
                    if (!mapMessage.containsKey(candidateToRemoveName)) {
                        mapMessage.put(candidateToRemoveName, "You can't delete the expected result of another owners.");
                        LOG.info(candidateToRemoveName + " drop message: " + "You can't delete the expected result of another owners.");
                    }
                }
            }

        } catch (Exception e) {
            LOG.error("error getting domain object: " + e.getMessage());
        }

        if (mapMessage.isEmpty()) {
            mapMessage.put("message", "status OK code 200");
        }

        return mapMessage;
    }

    //'true' for all who use this method. added by meeting 08.09. version 1.3.1
    public boolean checkAccess(Context ctx, String... args) {
        boolean permission = false;

        String parentTypeQP = "", parentTypeDEP = "", key = "";
        try {
            Map programMap = JPO.unpackArgs(args);
            Map settings = (Map) programMap.get("SETTINGS");
            String objectId = (String) programMap.get("objectId");

            DomainObject parentObject = new DomainObject(objectId);
            parentTypeQP = parentObject.getInfo(ctx, "relationship[IMS_QP_ExpectedResult2QPTask]");
            parentTypeDEP = parentObject.getInfo(ctx, "relationship[IMS_QP_ExpectedResult2DEPTask]");

            key = (String) settings.get("key");

        } catch (Exception e) {
            LOG.error("error unpacking arguments: " + e.getMessage());
            e.printStackTrace();
        }

        //for IMS_QP_DEPTask field 'Document code' is not editable
        if ("qp".equals(key)) {
            //added after meeting 08.09. version 1.3.1
            permission = true;
        }

        return permission;
    }

    public boolean checkMenuAccess(Context ctx, String... args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        String objectId = (String) argsMap.get("objectId");
        DomainObject domainObject = new DomainObject(objectId);
        String type = domainObject.getType(ctx);

        String from = "";
        if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(type)) {
            String currentState = domainObject.getInfo(ctx, "to[IMS_QP_QPlan2QPTask].from.current");
            from = domainObject.getInfo(ctx, "to[IMS_QP_QPlan2QPTask].from.to[IMS_QP_QP2QPlan].from.name");
            if (IMS_QP_Security_mxJPO.STATE_DONE.equals(currentState)) {
                return false;
            }
        }

        boolean aspectAccess = IMS_QP_Constants_mxJPO.AQP.equals(from) ?
                IMS_QP_Security_mxJPO.isOwnerQPlanFromTask(ctx, args)
                        || IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, args)
                :
                IMS_QP_Security_mxJPO.isOwnerQPlanFromTask(ctx, args);

        return aspectAccess;
    }

    public boolean checkOwnerAccess(Context ctx, String... args) {
        Person person = new Person(ctx.getUser());

        String ROLE_IMS_QP_Viewer = "IMS_QP_Viewer";
        String ROLE_IMS_QP_Supervisor = "IMS_QP_Supervisor";

        boolean granted = false;

        try {
            if (person.isAssigned(ctx, ROLE_IMS_QP_Viewer)) {
                granted = false;
            }

            Map argsMap = JPO.unpackArgs(args);
            String objectId = UIUtil.isNotNullAndNotEmpty((String) argsMap.get("objectId")) ?
                    (String) argsMap.get("objectId") : (String) argsMap.get("parentOID");
            DomainObject domainObject = new DomainObject(objectId);
            String type = domainObject.getType(ctx);
            String toId = "";

            if (IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult.equals(type)) {
                toId = domainObject.getInfo(ctx, "from[IMS_QP_ExpectedResult2QPTask].to.id");

                if (UIUtil.isNullOrEmpty(toId)) {
                    toId = domainObject.getInfo(ctx, "to[IMS_QP_ExpectedResult2QPTask].from.id");
                }

                if (UIUtil.isNullOrEmpty(toId)) {
                    toId = domainObject.getInfo(ctx, "from[IMS_QP_ExpectedResult2DEPTask].to.id");
                }

                if (UIUtil.isNullOrEmpty(toId)) {
                    toId = domainObject.getInfo(ctx, "to[IMS_QP_ExpectedResult2DEPTask].from.id");
                }

                DomainObject toTask = new DomainObject(toId);
                String toIdType = toTask.getType(ctx);
                String from = "";
                if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(toIdType)) {
                    String qPlanId = toTask.getInfo(ctx, "to[IMS_QP_QPlan2QPTask].from.id");
                    from = new DomainObject(qPlanId).getInfo(ctx, "to[IMS_QP_QP2QPlan].from");
                    if (UIUtil.isNotNullAndNotEmpty(qPlanId)) {
                        if (IMS_QP_Constants_mxJPO.AQP.equals(from)) {
                            granted = IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, qPlanId) || IMS_QP_Security_mxJPO.isOwnerDepFromQPPlan(ctx, qPlanId);
                        }

                        if (IMS_QP_Constants_mxJPO.SQP.equals(from)) {
                            granted = IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, qPlanId);
                        }

                    }
                }

                argsMap.put("objectId", objectId);
                argsMap.put("parentOID", toId);

            } else if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(type)) {
                String qPlanId = new DomainObject(objectId).getInfo(ctx, "to[IMS_QP_QPlan2QPTask].from.id");
                if (UIUtil.isNotNullAndNotEmpty(qPlanId)) {
                    granted = IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, qPlanId) || IMS_QP_Security_mxJPO.isOwnerDepFromQPPlan(ctx, qPlanId);
                }
            }

            args = JPO.packArgs(argsMap);

            if (granted) {
                return true;
            }

            if (IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, args)) {
                return true;
            }

            if (IMS_QP_Security_mxJPO.isOwnerQPlan(ctx, args)
                    || IMS_QP_Security_mxJPO.isOwnerDepFromQPTask(ctx, args)
                    || IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(ctx, toId)
                    || IMS_QP_Security_mxJPO.currentUserIsDEPOwner(ctx, new DomainObject(toId))) {
                granted = true;
            }

            if (IMS_QP_Security_mxJPO.isUserAdminOrSuper(ctx)) {
                granted = true;
            }

        } catch (MatrixException e) {
            LOG.error("error when checking Person: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOG.error("error when checking Person is dep owner: " + e.getMessage());
            e.printStackTrace();
        }


        return granted;
    }

    public boolean checkStateAndOwnerOfExpectedResult(Context context, String... args) {
        Map argsMap;

        boolean result = false;
        try {
            argsMap = JPO.unpackArgs(args);
            String id = (String) argsMap.get("objectId");
            DomainObject object = new DomainObject(id);

            String type = object.getType(context);
            String state = "";

            // IMS_QP_DEPTask
            if (IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask.equals(type)) {
                result = IMS_QP_Security_mxJPO.currentUserIsDEPOwner(context, object) ||
                        IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(context);
                state = object.getInfo(context, String.format("to[%s].from.to[%s].from.to[%s].from.current",
                        IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
                        IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
                        IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEP2DEPProjectStage));
            }

            // IMS_QP_QPTask
            else if ("IMS_QP_QPTask".equals(type)) {
                result = IMS_QP_Security_mxJPO.isOwnerQPlanFromTaskID(context, id) ||
                        IMS_QP_Security_mxJPO.currentUserIsQPSuperUser(context);
                state = object.getInfo(context, String.format("to[%s].from.current",
                        IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask));
            }

            if (!"Draft".equals(state)) {
                return false;
            }

        } catch (Exception e) {
            LOG.error("security exception: " + e.getMessage());
        }

        return result;
    }

    public String getConnectedExternalDocumentSetHTML(Context context, String[] args) throws Exception {
        Map argsMap = JPO.unpackArgs(args);
        Map paramsMap = (Map) argsMap.get("paramMap");
        String objectId = (String) paramsMap.get("objectId");
        LOG.info("getConnectedExternalDocumentSetHTML objectId: " + objectId);
        DomainObject domainObject = new DomainObject(objectId);
        final String RELATIONSHIP_IMS_QP_ExpectedResult2Fact = "IMS_QP_ExpectedResult2Fact";
        final String PROGRAM_IMS_QP_DEPSubStageDEPTasks = "IMS_QP_DEPSubStageDEPTasks";

        //TODO 1.Rewrite this to getRelatedObjects with sort method!
        //TODO 2.After that get the method to all developers
        List<Map> externalDocumentSetMaps = IMS_KDD_mxJPO.getRelatedObjectMaps(
                context, domainObject,
                RELATIONSHIP_IMS_QP_ExpectedResult2Fact,
                true,
                Arrays.asList(
                        DomainConstants.SELECT_ID,
                        DomainConstants.SELECT_NAME,
                        String.format("from[%s].to.", IMS_ExternalSystem_mxJPO.RELATIONSHIP_IMS_Object2ExternalSystem)
                ),
                null, null, false);

        StringBuilder sb = new StringBuilder();

        if (externalDocumentSetMaps.size() > 0) {
            for (Map externalDocumentSetMap : externalDocumentSetMaps) {
                if (sb.length() > 0) {
                    sb.append("<br />");
                }

                String externalSystemName = (String) externalDocumentSetMap.get(
                        String.format("from[%s].to.", IMS_ExternalSystem_mxJPO.RELATIONSHIP_IMS_Object2ExternalSystem));
                LOG.info("getConnectedExternalDocumentSetHTML externalSystemName: " + externalSystemName);
                String externalDocumentId = (String) externalDocumentSetMap.get(DomainConstants.SELECT_ID);
                String externalDocumentName = (String) externalDocumentSetMap.get(DomainConstants.SELECT_NAME);

                //TODO Rewrite this to disconnecting object method
                sb.append(IMS_KDD_mxJPO.getDisconnectLinkHTML(
                        PROGRAM_IMS_QP_DEPSubStageDEPTasks, "disconnectExternalDocumentSet",
                        domainObject.getId(context), externalDocumentId,
                        RELATIONSHIP_IMS_QP_ExpectedResult2Fact,
                        "Disconnect",
                        "function(){window.location.href = window.location.href;}"));

                sb.append(String.format(
                        "<a href=\"javascript:%s\"><img src=\"%s\" />%s</a>",
                        String.format(
                                "emxTableColumnLinkClick('../common/emxForm.jsp?objectId=%s&form=type_IMS_ExternalDocumentSet&%s=%s')",
                                externalDocumentId,
                                IMS_ExternalSystem_mxJPO.ATTRIBUTE_IMS_ExternalSystemName,
                                HtmlEscapers.htmlEscaper().escape(externalSystemName != null ? externalSystemName : "")),
                        IMS_KDD_mxJPO.FUGUE_16x16 + "document.png",
                        HtmlEscapers.htmlEscaper().escape(externalDocumentName)));
            }
        } else {
            sb.append(String.format(
                    "<a href=\"javascript:%s\"><img src=\"%s\" title=\"%s\" /></a>",
                    String.format(
                            "window.open('IMS_ExternalSearch.jsp?table=IMS_ExternalDocumentSet&IMS_ExternalSystemName=%s&relationship=%s&from=%s&objectId=%s&IMS_SearchHint=%s', '_blank', 'height=800,width=1200,toolbar=0,location=0,menubar=0')",
                            "97",
                            RELATIONSHIP_IMS_QP_ExpectedResult2Fact,
                            true,
                            domainObject.getId(context),
                            HtmlEscapers.htmlEscaper().escape("Search for Document Sets")),
                    "../common/images/fugue/16x16/plus.png",
                    "Connect Document Set"));
        }
        return sb.toString();
    }

    public String directionInputLink(Context ctx, String[] args) {
        String value = "<p style=\"color: #841000; font-style: italic;\"><input checked='checked' id='radio_button' type='radio'/> Input</p>";
        return value;
    }

    public Map createExpectedResult(Context ctx, String[] args) throws Exception {
        Map errorMessageMap = new HashMap();

        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("error getting arguments: " + e.getMessage());
        }

        Map paramMap = (Map) argsMap.get("paramMap");
        Map requestMap = (Map) argsMap.get("requestMap");

        String objectId = (String) paramMap.get("objectId");
        String arrow = (String) requestMap.get("fromto");

        String deviation = (String) requestMap.get("deviation");
        String resultType = (String) requestMap.get("resultType");
        String family = (String) requestMap.get("family");
        family = UIUtil.isNotNullAndNotEmpty(family) && family.equals("none") ? "" : family;
        String projectStage = (String) requestMap.get("project_stage");
        String stage = (String) requestMap.get("stage");
        String baseline = (String) requestMap.get("baseline");
        String fromDep = (String) requestMap.get("from_dep");

        if (IMS_QP_DEPTask_mxJPO.FROM.equals(arrow)) {
            if (UIUtil.isNullOrEmpty(fromDep)) {
                checkRequiredParameter(ctx, "family", family);
                checkRequiredParameter(ctx, "project stage", projectStage);
                checkRequiredParameter(ctx, "baseline", baseline);
            }
        }

        if (UIUtil.isNotNullAndNotEmpty(resultType)) {
            checkRequiredParameter(ctx, "family", family);
        }

        String parentOID = "";
        if (requestMap != null && !requestMap.isEmpty()) {
            parentOID = (String) requestMap.get("parentOID");
        }

        String type = null;
        try {
            //IMS_QP_DEPTask or IMS_QP_QPlan
            type = new DomainObject(parentOID).getType(ctx);
        } catch (Exception e) {
            LOG.error("error getting object: " + e.getMessage());
            e.printStackTrace();
        }

        if (IMS_QP_Constants_mxJPO.TYPE_IMS_QP_DEPTask.equals(type)) {
            errorMessageMap.putAll(createExpectedResultFromDEPTask(ctx, args));
        } else if (IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask.equals(type) || IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan.equals(type)) {
            errorMessageMap.putAll(createExpectedResultFromQPTask(ctx, args));
        }

        LOG.info("errorMessageMap: " + errorMessageMap);
        return errorMessageMap;
    }

    public void checkRequiredParameter(Context ctx, String field, String parameter) throws Exception {
        if (UIUtil.isNullOrEmpty(parameter)) {
            Exception e = new FrameworkException("Required parameter is empty: check field: " + field);

            LOG.error("error: " + e.getMessage());
            emxContextUtil_mxJPO.mqlWarning(ctx, e.toString());
            throw e;
        }
    }

    public Map createExpectedResultFromQPTask(Context ctx, String[] args) {
        Map returnMap = new HashMap();
        try {
            Map programMap = JPO.unpackArgs(args);
            Map requestMap = (Map) programMap.get("requestMap");
            Map paramMap = (Map) programMap.get("paramMap");

            String parentOID = (String) requestMap.get("parentOID");
            DomainObject parent = new DomainObject(parentOID);

            String objectId = (String) requestMap.get("objectId");
            DomainObject qpTask = new DomainObject(objectId);

            String newObjectId = (String) paramMap.get("objectId");

            String arrow = (String) requestMap.get("fromto");
            String relationship = IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK;

            DomainObject newObject;
            arrow = UIUtil.isNullOrEmpty(arrow) ? IMS_QP_DEPTask_mxJPO.FROM : arrow;
            if (arrow.equals(IMS_QP_DEPTask_mxJPO.FROM)) {
                newObject = getExternalExpectedResultQP(ctx, args);
                DomainRelationship.connect(ctx, newObject, relationship, qpTask);

                //create ext qptask & connect to QP Plan QP-DEP_ExternallInitialData
                addExternalQpTaskAndConnectingTo(ctx, qpTask, newObject);
            }

        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
            LOG.error("error creating new object types of Expected result: " + returnMap);
        }
        return returnMap;
    }

    private DomainObject getExternalExpectedResultQP(Context ctx, String... args) {
        Map argsMap = null;
        try {
            argsMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("could not get arguments: " + e.getMessage());
            e.printStackTrace();
        }

        Map paramMap = (Map) argsMap.get("paramMap");
        Map requestMap = (Map) argsMap.get("requestMap");
        String fromDep = (String) requestMap.get("from_dep");

        String vault = ctx.getVault().getName();
        BusinessObject boRelatedDepExpectedResultObject = null;
        DomainObject doRelatedDepExpectedResultObject = null;
        try {
            boRelatedDepExpectedResultObject = new BusinessObject(
                    IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult, fromDep, "", vault);
            doRelatedDepExpectedResultObject = new DomainObject(boRelatedDepExpectedResultObject);
        } catch (MatrixException e) {
            LOG.error("error getting external DEP");
            e.printStackTrace();
        }

        //get new objects id
        String objectId = (String) paramMap.get("objectId");

        String parentOID = (String) requestMap.get("parentOID");

        //create domain object for manipulating
        DomainObject newObject = null;
        try {
            newObject = new DomainObject(objectId);
        } catch (Exception e) {
            LOG.error("error getting object: " + objectId + " " + e.getMessage());
            e.printStackTrace();
        }

        //set attribute values
        String dynamicFieldString = "";
        if (doRelatedDepExpectedResultObject != null) {
            try {
                dynamicFieldString = dynamicFormFields(ctx, new String[]{doRelatedDepExpectedResultObject.getId(ctx)});
            } catch (FrameworkException e) {
                LOG.error("error getting field values: " + e.getMessage());
                e.printStackTrace();
            }
        }

        try {
            copyAttributes(ctx,/*from*/doRelatedDepExpectedResultObject, /*to*/newObject);

//            additional attributes filling
            String docCode = (String) requestMap.get("DocCode");
            String nameEN = (String) requestMap.get("NameEn");
            String nameRU = (String) requestMap.get("NameRu");
            String descriptionEN = (String) requestMap.get("DescriptionEn");
            String descriptionRU = (String) requestMap.get("DescriptionRu");

            newObject.setAttributeValue(ctx, "IMS_QP_DocumentCode", docCode);
            newObject.setAttributeValue(ctx, "IMS_Name", nameEN);
            newObject.setAttributeValue(ctx, "IMS_NameRu", nameRU);
            newObject.setAttributeValue(ctx, "IMS_DescriptionEn", descriptionEN);
            newObject.setAttributeValue(ctx, "IMS_DescriptionRu", descriptionRU);

            List<String> paramList = Arrays.asList(dynamicFieldString.split("\\|"));

            //by issue #65454 comment 12 doc 1.4.2
            boolean relatedFromFamily = relateFromFamily(ctx, paramList.get(9), newObject);

        } catch (MatrixException e) {
            LOG.error("error copying attributes: " + e.getMessage());
            e.printStackTrace();
        }

        //combine name by issue #65454
        StringBuilder builder = new StringBuilder();

        //prefix sector task name
        DomainObject parent = null;
        try {
            parent = new DomainObject(parentOID);
            String parentName = "";
            if ("IMS_QP_QPTask".equals(parent.getType(ctx))) {
                parentName = parent.getInfo(ctx, "to[IMS_QP_QPlan2QPTask].from.name");

            } else {
                parentName = parent.getName(ctx);
            }

            if (parentName.startsWith("QP-")) {
                parentName = parentName.substring(3);
                LOG.info("embedded name: " + parentName);
            }

            builder
                    .append(parentName)
                    .append("_");
        } catch (Exception e) {
            LOG.error("error getting object: " + parentOID + " " + e.getMessage());
            e.printStackTrace();
        }

        //from sector
        builder
                .append(fromDep)
                .append("_");

        try {
            LOG.info("name before: " + newObject.getName(ctx));
            String index = getUniqueIndex(ctx, builder.toString(), ""/*, IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult*/);
            newObject.setName(ctx, builder + index);
            LOG.info("name after: " + newObject.getName(ctx));

        } catch (FrameworkException e) {
            LOG.error("error setting name to new object: " + builder + " " + e.getMessage());
            e.printStackTrace();
        }

        return newObject;
    }

    public Map createExpectedResultFromDEPTask(Context ctx, String... args) {
        Map returnMap = new HashMap();

        try {
            Map argsMap = JPO.unpackArgs(args);
            Map paramMap = (Map) argsMap.get("paramMap");
            Map requestMap = (Map) argsMap.get("requestMap");
            String objectId = (String) paramMap.get("objectId");
            String parentOID = (String) requestMap.get("parentOID");
            String depExpectedOID = (String) requestMap.get("DEPexpectedOID");
            String arrow = (String) requestMap.get("fromto");

            DomainObject parent = new DomainObject(parentOID);

            DomainObject newObject = null;
            if (arrow.equals(IMS_QP_DEPTask_mxJPO.FROM)) {
                newObject = getExternalExpectedResult(ctx, args);

                //connect to ExternalInitialData
                addExternalDepTaskAndConnectingTo(ctx, parent, newObject);

            } else if (arrow.equals(IMS_QP_DEPTask_mxJPO.TO)) {
                MapList depTaskQP = getRelatedMapList(ctx,
                        parent, IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK, IMS_QP_DEPTask_mxJPO.TYPE_IMS_QP_EXPECTED_RESULT,
                        true, true, (short) 1, "", null, 0);
                MapList depTaskDEP = getRelatedMapList(ctx,
                        parent, IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK, IMS_QP_DEPTask_mxJPO.TYPE_IMS_QP_EXPECTED_RESULT,
                        true, true, (short) 1, "", null, 0);
                String family = UIUtil.isNotNullAndNotEmpty((String) requestMap.get("family")) ?
                        (String) requestMap.get("family") : "family_error";

                depTaskQP.addAll(depTaskDEP);
                newObject = IMS_QP_DEPTask_mxJPO.getNextName(ctx, objectId, parent, depTaskQP, family, 0, 0);
            }

            if (newObject == null) {
                Exception e = new FrameworkException("Required parameter is empty: check fields");

                LOG.error("error: " + e.getMessage());
                emxContextUtil_mxJPO.mqlWarning(ctx, e.toString());
                throw e;
            }

            if (UIUtil.isNotNullAndNotEmpty(depExpectedOID)) {
                DomainObject depTaskObject = new DomainObject(depExpectedOID);

                DomainRelationship.connect(ctx, depTaskObject, IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_DEPResult2QPResult, newObject);
                String relatedTypeQP = newObject.getInfo(ctx, "to[" + IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.id");
                if (relatedTypeQP == null || relatedTypeQP.equals("")) {
                    String relatedType = depTaskObject.getInfo(ctx, "to[" + IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT + "].from.id");
                    if (relatedType != null && !relatedType.equals("")) {
                        DomainRelationship domainRelationship = DomainRelationship.connect(ctx,
                                new DomainObject(relatedType), IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_RESULT_TYPE_2_EXPECTED_RESULT, newObject);
                    }
                }

                LOG.info("attribues copied to new object");
                copyAttributes(ctx, depTaskObject, newObject);
            }

            String relationship = IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK;
            LOG.info("connecting: " + relationship);
            if (arrow.equals(IMS_QP_DEPTask_mxJPO.FROM)) {
                DomainRelationship.connect(ctx, newObject, relationship, parent);
                LOG.info("related " + newObject.getName(ctx) + " -> " + relationship + " -> " + parent.getName(ctx));
            } else {
                DomainRelationship.connect(ctx, parent, relationship, newObject);
                LOG.info("related " + parent.getName(ctx) + " -> " + relationship + " -> " + newObject.getName(ctx));
            }

//            //by issue #65454 comment 12 doc 1.4.2
//            String family = (String) requestMap.get("family");
//            relateFromFamily(ctx, family, newObject);


        } catch (Exception ex) {
            returnMap.put("Message", ex.toString());
            returnMap.put("Action", "STOP");
        }

        LOG.info("return result: " + returnMap);
        return returnMap;
    }

    private boolean relateFromFamily(Context ctx, String family, DomainObject object) {

        boolean related = false;
        LOG.info("family: " + family);
        String familyId = null;
        if (!family.equals("") && family.contains("_")) {
            familyId = family.substring(0, family.lastIndexOf("_"));
        } else {
            familyId = family;
        }

        LOG.info("familyId: " + familyId);
        String relationshipFromFamily = IMS_QP_Constants_mxJPO.relationship_IMS_QP_ResultType2ExpectedResult;
        if (UIUtil.isNotNullAndNotEmpty(familyId)) {
            try {
                DomainRelationship relationship = DomainRelationship.connect(ctx, new DomainObject(familyId), relationshipFromFamily, object);
                if (relationship != null) {
                    LOG.info("objects related: " + relationship.getName());
                    related = !related;
                }
            } catch (Exception e) {
                LOG.info("error relating objects: " + e.getMessage());
                e.printStackTrace();
            }
        }

        return related;
    }

    private DomainObject getExternalExpectedResult(Context ctx, String... args) {
        Map programMap = null;
        try {
            programMap = JPO.unpackArgs(args);
        } catch (Exception e) {
            LOG.error("could not get arguments: " + e.getMessage());
            e.printStackTrace();
        }

        Map paramMap = (Map) programMap.get("paramMap");
        Map requestMap = (Map) programMap.get("requestMap");

        //get new objects id
        String objectId = (String) paramMap.get("objectId");

        String parentOID = (String) requestMap.get("parentOID");
        String depExpectedOID = (String) requestMap.get("DEPexpectedOID");
        String arrow = (String) requestMap.get("fromto");

        //create domain object for manipulating
        DomainObject newObject = null;
        try {
            newObject = new DomainObject(objectId);
        } catch (Exception e) {
            LOG.error("error getting object: " + objectId + " " + e.getMessage());
            e.printStackTrace();
        }

        //set attribute values
        String resultType = (String) requestMap.get("resultType");
        String family = (String) requestMap.get("family");

        String projectStageId = (String) requestMap.get("project_stage");
        String stage = (String) requestMap.get("stage");
        String baseline = (String) requestMap.get("baseline");

        String baselineId = (String) requestMap.get("baseline");
        if (UIUtil.isNotNullAndNotEmpty(baselineId)) {
            baselineId = !baselineId.substring(0, baselineId.indexOf("_")).isEmpty() ? baselineId.substring(0, baselineId.indexOf("_")) : "";
        } else baselineId = "";

        String projectStageName = "", baselineName;
        try {
            projectStageName = UIUtil.isNotNullAndNotEmpty(projectStageId) ?
                    new DomainObject(projectStageId).getName(ctx) : "";
            newObject.setAttributeValue(ctx, "IMS_QP_ProjectStage", projectStageName);

            newObject.setAttributeValue(ctx, "IMS_QP_Stage", stage);

            baselineName = UIUtil.isNotNullAndNotEmpty(baselineId) ?
                    new DomainObject(baselineId).getName(ctx) : "";
            newObject.setAttributeValue(ctx, "IMS_QP_Baseline", baselineName);

            LOG.info("attributes set: " + projectStageName + " " + stage + " " + baselineName);
        } catch (FrameworkException e) {
            LOG.error("error setting attributes "
                    + "IMS_QP_ProjectStage: " + projectStageName
                    + " IMS_QP_Stage: " + stage
                    + " IMS_QP_Baseline: " + baseline
                    + " message: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            LOG.error("error getting object from id: " + projectStageName
                    + " message: " + e.getMessage());
            e.printStackTrace();
        }

        //combine name by issue #65454
        StringBuilder builder = new StringBuilder();

        //prefix sector task name
        DomainObject parent = null;
        try {
            parent = new DomainObject(parentOID);
            builder
                    .append(parent.getName(ctx));
        } catch (Exception e) {
            LOG.error("error getting object: " + parentOID + " " + e.getMessage());
            e.printStackTrace();
        }

        //ExIn sector
        builder
                .append("_ExIn");

        //project stage sector
        builder
                .append(projectStageName);

        //stage sector
        builder
                .append(stage)
                .append("-");

        //index sector *unique number from ExternalInitialData
        //result type sector
        String familyName = null;
        String familyId;

        if (!family.equals("")) {
            try {
                familyId = family.substring(0, family.lastIndexOf("_"));
                familyName = new DomainObject(familyId).getName(ctx);
                LOG.info("execute family name: " + familyName);
            } catch (Exception e) {
                LOG.error("error getting family info: " + e.getMessage());
                e.printStackTrace();
            }
        }

        String index = getUniqueIndex(ctx, builder.toString(), familyName/*, IMS_QP_Constants_mxJPO.type_IMS_QP_ExpectedResult*/);
        builder
                .append(index)
                .append("_");

        builder
                .append(IMS_QP_Constants_mxJPO.IDP_FROM_OWNER.equals(familyName) ? "OwnIn" : familyName);

        try {
            LOG.info("name before: " + newObject.getName(ctx));
            newObject.setName(ctx, builder.toString());
            LOG.info("name after: " + newObject.getName(ctx));

        } catch (FrameworkException e) {
            LOG.error("error setting name to new object: " + builder + " " + e.getMessage());
            e.printStackTrace();
        }

        return newObject;
    }

    /**
     * Adding external Dep task object and connecting with parent task, new ER object & external initial data object
     *
     * @param ctx
     * @param parent
     * @param newObject
     */
    private void addExternalQpTaskAndConnectingTo(Context ctx, DomainObject parent, DomainObject newObject) {
        LOG.info("addExternalQpTaskAndConnectingTo");
        String vault = ctx.getVault().getName();
        DomainObject externalQpTask = null;
        try {
            String rawName = newObject.getName(ctx);
            externalQpTask = DomainObject.newInstance(ctx);
            externalQpTask.createObject(ctx,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    /*name*/rawName,
                    /*revision*/"-",
                    /*policy*/IMS_QP_Constants_mxJPO.type_IMS_QP_QPTask,
                    /*vault*/ vault);

        } catch (MatrixException e) {
            e.printStackTrace();
        }

        //relate ER from QpTask
        String relationshipToTask = IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_QP_TASK;

        //relate QpTask from DepTask
        String relationshipFromExternalTask = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPTask2QPTask;

        //relate Qp to DepTask
        BusinessObject boQPlan = null;
        try {
            boQPlan = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan, "QP-DEP_ExternalInitialData", "", vault);
        } catch (MatrixException e) {
            LOG.error("error getting external DEP");
            e.printStackTrace();
        }

        DomainObject qpObject = null;
        try {
            if (externalQpTask != null) {
                DomainRelationship.connect(ctx, /*from*/externalQpTask, relationshipToTask, /*to*/newObject);
                LOG.info("related " + externalQpTask.getName(ctx) + " -> " + relationshipToTask + " -> " + newObject.getName(ctx));

                DomainRelationship externalRelationship = DomainRelationship.connect(ctx, /*from*/externalQpTask, relationshipFromExternalTask, /*to*/parent);
                //external relationship going to Approve
                externalRelationship.setAttributeValue(ctx, "IMS_QP_DEPTaskStatus", "Approved");
                LOG.info("related and approved: " + externalQpTask.getName(ctx) + " -> " + relationshipFromExternalTask + " -> " + parent.getName(ctx));

                qpObject = new DomainObject(boQPlan);
                if (qpObject != null) {
//                    qpObject.open(ctx);
                    LOG.info("ext plan object found");
                    String relationshipFromQPlan = IMS_QP_Constants_mxJPO.relationship_IMS_QP_QPlan2QPTask;
                    DomainRelationship.connect(ctx, /*from*/qpObject, relationshipFromQPlan, /*to*/externalQpTask);
                    LOG.info("related " + qpObject.getName(ctx) + " -> " + relationshipFromQPlan + " -> " + externalQpTask.getName(ctx));
                }
            } else {
                LOG.error("error: " + externalQpTask + " | " + qpObject);
                throw new FrameworkException("could not create objects");
            }

        } catch (FrameworkException fe) {
            LOG.error("error connecting: " + fe.getMessage());
            fe.printStackTrace();
        }

        //finally
        try {
            copyAttributes(ctx, newObject, externalQpTask);
        } catch (MatrixException e) {
            LOG.error("error occurred copying attributes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Adding external Dep task object and connecting with parent task, new ER object & external initial data object
     *
     * @param ctx
     * @param parent
     * @param newObject
     */
    private void addExternalDepTaskAndConnectingTo(Context ctx, DomainObject parent, DomainObject newObject) {
        LOG.info("addExternalDepTaskAndConnectingTo");
        String vault = ctx.getVault().getName();
        DomainObject externalDepTask = null;
        try {
            String rawName = newObject.getName(ctx);
            externalDepTask = DomainObject.newInstance(ctx);
            externalDepTask.createObject(ctx,
                    /*type*/IMS_QP_Constants_mxJPO.type_IMS_QP_DEPTask,
                    /*name*/rawName,
                    /*revision*/"-",
                    /*policy*/IMS_QP_Constants_mxJPO.type_IMS_QP_DEPTask,
                    /*vault*/ vault);

        } catch (MatrixException e) {
            e.printStackTrace();
        }

        //relate ER from DepTask
        String relationshipToTask = IMS_QP_DEPTask_mxJPO.RELATIONSHIP_IMS_QP_EXPECTED_RESULT_2_DEP_TASK;

        //relate DepTask from DepTask
        String relationshipToExternalTask = IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPTask2DEPTask;

        //relate Dep from DepTask
        BusinessObject boDep = null;
        try {
            boDep = new BusinessObject(IMS_QP_Constants_mxJPO.type_IMS_QP_DEP, "DEP_ExternalInitialData", "", vault);
        } catch (MatrixException e) {
            LOG.error("error getting external DEP");
            e.printStackTrace();
        }

        DomainObject externalDepObject = null;
        try {
            if (externalDepTask != null) {
                DomainRelationship.connect(ctx, /*from*/externalDepTask, relationshipToTask, /*to*/newObject);
                LOG.info("related " + externalDepTask.getName(ctx) + " -> " + relationshipToTask + " -> " + newObject.getName(ctx));
                DomainRelationship externalRelationship = DomainRelationship.connect(ctx, /*from*/externalDepTask, relationshipToExternalTask, /*to*/parent);
                //external relationship going to Approve
                externalRelationship.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.attribute_IMS_QP_DEPTaskStatus, IMS_QP_Constants_mxJPO.APPROVED);
                LOG.info("related " + externalDepTask.getName(ctx) + " -> " + relationshipToExternalTask + " -> " + parent.getName(ctx));

                externalDepObject = new DomainObject(boDep);
                if (externalDepObject != null) {
                    String relationshipToDep = IMS_QP_Constants_mxJPO.RELATIONSHIP_IMS_QP_DEPTask2DEP;
                    DomainRelationship externalRelationshipToDep = DomainRelationship.connect(ctx, /*from*/externalDepTask, relationshipToDep, /*to*/externalDepObject);
                    externalRelationshipToDep.setAttributeValue(ctx, IMS_QP_Constants_mxJPO.attribute_IMS_QP_DEPTaskStatus, IMS_QP_Constants_mxJPO.APPROVED);
                    LOG.info("related " + externalDepTask.getName(ctx) + " -> " + relationshipToDep + " -> " + externalDepObject.getName(ctx));
                }
            } else {
                LOG.error("error: " + externalDepTask + " | " + externalDepObject);
                throw new FrameworkException("could not create objects");
            }

        } catch (FrameworkException fe) {
            LOG.error("error connecting: " + fe.getMessage());
            fe.printStackTrace();
        }

        //finally
        try {
            copyAttributes(ctx, newObject, externalDepTask);
        } catch (MatrixException e) {
            LOG.error("error occurred copying attributes: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private String getUniqueIndex(Context ctx, String prefix, String postfix) {
        String vault = ctx.getVault().getName();

        int counter = 1;
        String index = "";

        try {
            boolean candidate = false;
            do {
                index = counter >= 10 ? "" + counter : "0" + counter;
                postfix = UIUtil.isNotNullAndNotEmpty(postfix) ? "*" + postfix : "";
                String rawName = prefix + index;

                String where = "name smatch '*" + rawName + "*'";
                MapList allObjectsWithName = DomainObject.findObjects(ctx,
                        /*types*/"*",
                        /*vault*/    vault,
                        /*where*/ where,
                        /*select*/   new StringList(DomainConstants.SELECT_ID));

                if (allObjectsWithName != null && allObjectsWithName.size() == 0) {
                    candidate = true;
                } else {
                    ++counter;
                }

            } while (!candidate);

        } catch (FrameworkException frameworkException) {
            LOG.error("Framework exception: " + frameworkException.getMessage());
            frameworkException.printStackTrace();
        }

        return index;
    }

    private MapList getRelatedMapList(Context context,
                                      DomainObject object, String relationship, String type,
                                      boolean from, boolean to, short level, String expressionObject,
                                      String expressionRelationship, int i) throws FrameworkException {

        StringList QAGbusSelects = new StringList();  // Object
        QAGbusSelects.add(DomainConstants.SELECT_ID);
        QAGbusSelects.add(DomainConstants.SELECT_NAME);
        QAGbusSelects.add(DomainConstants.SELECT_ORIGINATED);
        QAGbusSelects.add("attribute[IMS_SortOrder]");
        StringList QAGrelSelects = new StringList();  // Rel
        QAGrelSelects.add(DomainConstants.SELECT_RELATIONSHIP_ID);

        MapList result = object.getRelatedObjects(context, relationship, type, QAGbusSelects, QAGrelSelects, from, to, level, expressionObject, expressionRelationship, i);
        return result;
    }

    /**
     * @param ctx     usual parameter
     * @param object1 DomainObject copied from
     * @param object2 DomainObject copied to
     * @throws MatrixException throwable Matrix database exception throwable
     */
    private void copyAttributes(Context ctx, DomainObject object1, DomainObject object2) throws MatrixException {
        object1.open(ctx);
        object2.open(ctx);

        Map attributesMap2 = object2.getAttributeMap(ctx);

        BusinessObjectAttributes businessObjectAttributes = object1.getAttributes(ctx);
        Map attributesMap1 = object1.getAttributeMap(ctx);

        Map copiedAttributes = new HashMap();
        for (Object o : attributesMap2.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            if (attributesMap1.containsKey(entry.getKey())) {
                copiedAttributes.put(entry.getKey(), attributesMap1.get(entry.getKey()));
            }
        }

        object2.setAttributes(ctx, FrameworkUtil.toAttributeList(copiedAttributes));
        object2.update(ctx);

        //finally
        object1.close(ctx);
        object2.close(ctx);
    }

    /**
     * It's retrieve all ProjectStages
     */
    public Object getProjectStages(Context ctx, String... args) {
        StringList fieldRangeValues = new StringList("");
        StringList fieldDisplayRangeValues = new StringList("");

        //get list of objects by type
        BusinessObjectWithSelectList businessObjectList = getByType(ctx,
                IMS_QP_Constants_mxJPO.type_IMS_ProjectStage);

        for (Object o : businessObjectList) {
            BusinessObjectWithSelect bows = (BusinessObjectWithSelect) o;

            try {
                bows.open(ctx);
                fieldRangeValues.add(bows.getSelectData(DomainConstants.SELECT_NAME));
                fieldDisplayRangeValues.add(bows.getSelectData(DomainObject.SELECT_NAME));
                bows.close(ctx);
            } catch (MatrixException e) {
                LOG.error("error caused by business object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Map result = new HashMap();
        result.put("field_choices", fieldRangeValues);
        result.put("field_display_choices", fieldDisplayRangeValues);

        return result;
    }

    public Object getStages(Context ctx, String... args) throws FrameworkException {

        StringList fieldRangeValues = new StringList("");
        StringList fieldDisplayRangeValues = new StringList("");

        fieldRangeValues.add("1");
        fieldRangeValues.add("2");
        fieldDisplayRangeValues.add("1");
        fieldDisplayRangeValues.add("2");


        Map result = new LinkedHashMap();
        result.put("field_choices", fieldRangeValues);
        result.put("field_display_choices", fieldDisplayRangeValues);
        return result;
    }

    /**
     * It's retrieve all Baselines
     */
    public Object getBaselines(Context ctx, String... args) {
        StringList fieldRangeValues = new StringList("");
        StringList fieldDisplayRangeValues = new StringList("");

        //get list of objects by type
        BusinessObjectWithSelectList businessObjectList = getByType(ctx,
                IMS_QP_Constants_mxJPO.type_IMS_Baseline);

        for (Object o : businessObjectList) {
            BusinessObjectWithSelect bows = (BusinessObjectWithSelect) o;

            try {
                bows.open(ctx);
                fieldRangeValues.add(bows.getSelectData(DomainConstants.SELECT_NAME));
                fieldDisplayRangeValues.add(bows.getSelectData(DomainObject.SELECT_NAME));
                bows.close(ctx);
            } catch (MatrixException e) {
                LOG.error("error caused by business object: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Map result = new HashMap();
        result.put("field_choices", fieldRangeValues);
        result.put("field_display_choices", fieldDisplayRangeValues);

        return result;
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
        query.setVaultPattern("*");
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

        busSelect.addElement(DomainConstants.SELECT_TYPE);
        busSelect.addElement(DomainConstants.SELECT_NAME);
        busSelect.addElement(DomainConstants.SELECT_REVISION);
        busSelect.addElement(DomainConstants.SELECT_ID);

        return busSelect;
    }

    /**
     * get form field values by DEP
     * let from = document.getElementsByName("fromto");                                                                //[0]
     * let doccode = document.getElementById("DocCode");                                                               //[1]
     * let name = document.getElementById("NameEn");                                                                   //[2]
     * let nameRu = document.getElementById("NameRu");                                                                 //[3]
     * let description = document.getElementById("DescriptionEn");                                                     //[4]
     * let descriptionru = document.getElementById("DescriptionRu");                                                   //[5]
     * <p>
     * let project_stage = document.getElementById("calc_project_stage");                                              //[6]
     * let stage = document.getElementById("calc_stage");                                                              //[7]
     * let baseline = document.getElementById("calc_baseline");                                                        //[8]
     * <p>
     * let resultTypeCombobox = document.getElementById("resultTypeId");                                               //[9-10]
     * let familyCombobox = document.getElementById("familyId");                                                       //[11-12]
     */
    public String dynamicFormFields(Context ctx, String... args) {

        StringList selects = new StringList();
        selects.addElement("from[IMS_QP_ExpectedResult2DEPTask]");
        selects.addElement("attribute[IMS_QP_DocumentCode]");
        selects.addElement("attribute[IMS_Name]");
        selects.addElement("attribute[IMS_NameRu]");
        selects.addElement("attribute[IMS_DescriptionRu]");
        selects.addElement("attribute[IMS_DescriptionEn]");
        selects.addElement("attribute[IMS_QP_ProjectStage]");
        selects.addElement("attribute[IMS_QP_Stage]");
        selects.addElement("attribute[IMS_QP_Baseline]");
        selects.addElement("to[IMS_QP_ResultType2ExpectedResult].from.id");
        selects.addElement("to[IMS_QP_ResultType2ExpectedResult].from.name");

        Map objectMap;
        DomainObject object;

        try {
            String objectId = args[0];
            object = new DomainObject(objectId);
            objectMap = object.getInfo(ctx, selects);

        } catch (Exception e) {
            return e.getMessage();
        }

        StringList typeSelects = new StringList();
        typeSelects.addElement("to[IMS_QP_ResultType2Family].from.id");
        typeSelects.addElement("to[IMS_QP_ResultType2Family].from.name");

        Map typeMap = null;

        String typeId = (String) objectMap.get("to[IMS_QP_ResultType2ExpectedResult].from.id");
        if (UIUtil.isNotNullAndNotEmpty(typeId)) {
            try {
                DomainObject type = new DomainObject(typeId);
                typeMap = type.getInfo(ctx, typeSelects);
            } catch (Exception e) {
                return e.getMessage();
            }

        }

        StringBuilder builder = new StringBuilder();
        if (objectMap != null && !objectMap.isEmpty()) {
            builder.
                    append("TRUE".equals(objectMap.get("from[IMS_QP_ExpectedResult2DEPTask]")) ? "from" : "to").append("|").    //[0]
                    append(objectMap.get("attribute[IMS_QP_DocumentCode]")).append("|").                                        //[1]
                    append(objectMap.get("attribute[IMS_Name]")).append("|").                                                   //[2]
                    append(objectMap.get("attribute[IMS_NameRu]")).append("|").                                                 //[3]
                    append(objectMap.get("attribute[IMS_DescriptionEn]")).append("|").                                          //[4]
                    append(objectMap.get("attribute[IMS_DescriptionRu]")).append("|").                                          //[5]
                    append(objectMap.get("attribute[IMS_QP_ProjectStage]")).append("|").                                        //[6]
                    append(objectMap.get("attribute[IMS_QP_Stage]")).append("|").                                               //[7]
                    append(objectMap.get("attribute[IMS_QP_Baseline]")).append("|").                                            //[8]
                    append(objectMap.get("to[IMS_QP_ResultType2ExpectedResult].from.id")).append("|").                          //[9]
                    append(objectMap.get("to[IMS_QP_ResultType2ExpectedResult].from.name")).append("|");                        //[10]
        }
        if (typeMap != null && !typeMap.isEmpty()) {
            builder.
                    append(typeMap.get("to[IMS_QP_ResultType2Family].from.id")).append("|").                                    //[11]
                    append(typeMap.get("to[IMS_QP_ResultType2Family].from.name"));                                              //[12]
        }

        LOG.info("builder: " + builder);
        return builder.toString();
    }

}
