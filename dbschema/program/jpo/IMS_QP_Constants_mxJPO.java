import com.matrixone.apps.domain.DomainObject;

public class IMS_QP_Constants_mxJPO {

    private IMS_QP_Constants_mxJPO() {
    }

    public static final String ESERVICE_PRODUCTION = "eService Production";
    public static final String ATTRIBUTE_IMS_QP_INTERDISCIPLINARY_DEP = "attribute[IMS_QP_InterdisciplinaryDEP]";

    private static final String TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID = "to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_DEPProjectStage2DEPSubStage].from.to[IMS_QP_DEP2DEPProjectStage].from.id";
    public static final String DEP_ID_FOR_TASK = TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID;
    private static final String TO_IMS_PROJECT_STAGE_2_CB_FROM_FROM_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_TO_FROM_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_TO = "to[IMS_ProjectStage2CB].from.from[IMS_QP_ProjectStage2DEPProjectStage].to.from[IMS_QP_DEPProjectStage2DEPSubStage].to";
    public static final String BASELINES_BY_STAGE = TO_IMS_PROJECT_STAGE_2_CB_FROM_FROM_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_TO_FROM_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_TO;
    private static final String IMS_PBSSYSTEM_IMS_GBSBUILDING_IMS_PBSFUNCTIONAL_AREA = "IMS_PBSSystem,IMS_GBSBuilding,IMS_PBSFunctionalArea";
    public static final String SYSTEM_TYPES = IMS_PBSSYSTEM_IMS_GBSBUILDING_IMS_PBSFUNCTIONAL_AREA;

    public static final String FROM_IMS_QP_DEPSUB_STAGE_2_DEPTASK = "from[IMS_QP_DEPSubStage2DEPTask]";
    public static final String TO_IMS_QP_BASE_LINE_2_DEPSUB_STAGE_FROM_ID = "to[IMS_QP_BaseLine2DEPSubStage].from.id";
    public static final String TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_ID = "to[IMS_QP_DEPProjectStage2DEPSubStage].from.id==";
    public static final String FROM_IMS_QP_DEP_2_DEPPROJECT_STAGE_TO_TO_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_FROM_ID = "from[IMS_QP_DEP2DEPProjectStage].to.to[IMS_QP_ProjectStage2DEPProjectStage].from.id";
    public static final String FROM_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_TO_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID = "from[IMS_QP_ProjectStage2DEPProjectStage].to.to[IMS_QP_DEP2DEPProjectStage].from.id==";
    public static final String IMS_QP_DEP_SUB_STAGE = "IMS_QP_DEPSubStage";

    public static final String TO_IMS_QP_DEPSUB_STAGE_2_DEPTASK_FROM_ID = "to[IMS_QP_DEPSubStage2DEPTask].from.id";
    public static final String ATTRIBUTE_IMS_NAME_RU = "attribute[IMS_NameRu]";
    public static final String ATTRIBUTE_IMS_NAME = "attribute[IMS_Name]";
    public static final String TO_IMS_QP_DEPTASK_2_DEPTASK_FROM_ID = "to[IMS_QP_DEPTask2DEPTask].from.id";
    public static final String TO_IMS_QP_DEPTASK_2_DEPTASK_ATTRIBUTE_IMS_QP_DEPTASK_STATUS = "to[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]";
    public static final String FROM_IMS_QP_DEPTASK_2_DEPTASK_TO_ID = "from[IMS_QP_DEPTask2DEPTask].to.id";
    public static final String FROM_IMS_QP_DEPTASK_2_DEPTASK_ATTRIBUTE_IMS_QP_DEPTASK_STATUS = "from[IMS_QP_DEPTask2DEPTask].attribute[IMS_QP_DEPTaskStatus]";
    public static final String ATTRIBUTE_IMS_QP_DEPTASK_STATUS = "attribute[IMS_QP_DEPTaskStatus]";

    public static final String type_IMS_QP = "IMS_QP";
    public static final String type_IMS_QP_Directory = "IMS_QP_Directory";
    public static final String type_IMS_QP_ResultType = "IMS_QP_ResultType";
    public static final String type_IMS_Baseline = "IMS_Baseline";
    public static final String type_IMS_ProjectStage = "IMS_ProjectStage";
    public static final String type_IMS_DisciplineCode = "IMS_DisciplineCode";
    public static final String type_IMS_QP_DEP = "IMS_QP_DEP";
    public static final String type_IMS_QP_QPlan = "IMS_QP_QPlan";
    public static final String type_IMS_QP_DEPProjectStage = "" + type_IMS_QP_DEP + "ProjectStage";
    public static final String type_IMS_QP_DEPSubStage = "" + type_IMS_QP_DEP + "SubStage";
    public static final String type_IMS_QP_DEPTask = "" + type_IMS_QP_DEP + "Task";
    public static final String type_IMS_QP_ExpectedResult = "IMS_QP_ExpectedResult";
    public static final String type_IMS_QP_QPTask = "IMS_QP_QPTask";
    public static final String type_IMS_Family = "IMS_Family";
    public static final String type_IMS_PBSSystem = "IMS_PBSSystem";
    public static final String type_IMS_GBSBuilding = "IMS_GBSBuilding";
    public static final String type_IMS_PBSFunctionalArea = "IMS_PBSFunctionalArea";
    public static final String type_IMS_QP_Classifier = "IMS_QP_Classifier";
    public static final String type_IMS_QP_CheckList = "IMS_QP_CheckList";
    public static final String type_IMS_ExternalDocumentSet = "IMS_ExternalDocumentSet";
    public static final String type_IMS_ExternalObject = "IMS_ExternalObject";

    //by issue 51753
    public static final String type_IMS_QP_Reports = "IMS_QP_Reports";
    public static final String type_IMS_QP_ReportUnit = "IMS_QP_ReportUnit";

    public static final String policy_IMS_QP_Reports = "IMS_QP_Reports";
    public static final String policy_IMS_QP_ReportUnit = "IMS_QP_ReportUnit";
    public static final String policy_IMS_QP_QPTask = "IMS_QP_QPTask";

    public static final String relationship_IMS_QP_Project2QP = "IMS_QP_Project2QP";
    public static final String relationship_IMS_QP_Project2Directory = "IMS_QP_Project2Directory";
    public static final String relationship_IMS_QP_Directory2ResultType = "IMS_QP_Directory2ResultType";
    public static final String relationship_IMS_QP_Directory2ProjectStage = "IMS_QP_Directory2ProjectStage";
    public static final String relationship_IMS_QP_Directory2Baseline = "IMS_QP_Directory2Baseline";
    public static final String relationship_IMS_QP_Directory2DisciplineCode = "IMS_QP_Directory2DisciplineCode";
    public static final String relationship_IMS_QP_Directory2Directory = "IMS_QP_Directory2Directory";
    public static final String relationship_IMS_QP_Directory2PBSSystem = "IMS_QP_Directory2PBSSystem";
    public static final String relationship_IMS_QP_Directory2GBSBuilding = "IMS_QP_Directory2GBSBuilding";
    public static final String relationship_IMS_QP_Directory2PBSFunctionalArea = "IMS_QP_Directory2PBSFunctionalArea";
    public static final String relationship_IMS_QP_QP2DEP = "IMS_QP_QP2DEP";
    public static final String relationship_IMS_QP_DEP2DEPProjectStage = "" + type_IMS_QP_DEP + "2DEPProjectStage";
    public static final String relationship_IMS_QP_DEPProjectStage2DEPSubStage = "" + type_IMS_QP_DEP + "ProjectStage2DEPSubStage";
    public static final String relationship_IMS_QP_DEPSubStage2DEPTask = "" + type_IMS_QP_DEP + "SubStage2DEPTask";
    public static final String relationship_IMS_QP_ResultType2Family = "IMS_QP_ResultType2Family";
    public static final String relationship_IMS_QP_QP2QPlan = "IMS_QP_QP2QPlan";
    public static final String relationship_IMS_QP_QPlan2QPTask = "IMS_QP_QPlan2QPTask";
    public static final String relationship_IMS_QP_ExpectedResult2DEPTask = "IMS_QP_ExpectedResult2DEPTask";
    public static final String relationship_IMS_QP_ResultType2ExpectedResult = "IMS_QP_ResultType2ExpectedResult";
    public static final String relationship_IMS_QP_ExpectedResult2QPTask = "IMS_QP_ExpectedResult2QPTask";
    public static final String relationship_IMS_QP_QPlan2Object = "IMS_QP_QPlan2Object";
    public static final String relationship_IMS_QP_DEP2QPlan = "IMS_QP_DEP2QPlan";
    public static final String relationship_IMS_QP_Classifier2QPlan = "IMS_QP_Classifier2QPlan";
    public static final String relationship_IMS_QP_DEP2Classifier = "IMS_QP_DEP2Classifier";

    //by issue 51753
    public static final String relationship_IMS_QP_Project2Reports = "IMS_QP_Project2Reports";
    public static final String relationship_IMS_QP_Reports2ReportUnit = "IMS_QP_Reports2ReportUnit";

    static final String ATTRIBUTE_IMS_DescriptionEn = "IMS_DescriptionEn";
    static final String ATTRIBUTE_IMS_DescriptionRu = "IMS_DescriptionRu";
    static final String ATTRIBUTE_IMS_QP_FACT_EXP = "IMS_QP_FactExp";

    static final String TYPE_IMS_QP_DEP = "IMS_QP_DEP";
    static final String TYPE_IMS_QP_DEPTask = "IMS_QP_DEPTask";
    static final String RELATIONSHIP_IMS_QP_DEP2DEPProjectStage = "IMS_QP_DEP2DEPProjectStage";
    static final String RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage = "IMS_QP_DEPProjectStage2DEPSubStage";
    static final String RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask = "IMS_QP_DEPSubStage2DEPTask";
    static final String RELATIONSHIP_IMS_QP_DEPTask2DEPTask = "IMS_QP_DEPTask2DEPTask";
    static final String RELATIONSHIP_IMS_QP_DEPTask2DEP = "IMS_QP_DEPTask2DEP";

    static final String ATTRIBUTE_IMS_Name = "IMS_Name";
    static final String ATTRIBUTE_IMS_NameRu = "IMS_NameRu";

    static final String SELECT_DEP_ID = String.format(
            "to[%s].from.to[%s].from.to[%s].from.id",
            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage);

    static final String SELECT_DEP_NAME = String.format(
            "to[%s].from.to[%s].from.to[%s].from.name",
            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage);

    static final String SELECT_DEP_IMS_NAME = String.format(
            "to[%s].from.to[%s].from.to[%s].from.%s",
            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
            DomainObject.getAttributeSelect(ATTRIBUTE_IMS_Name));

    static final String SELECT_DEP_IMS_NAME_RU = String.format(
            "to[%s].from.to[%s].from.to[%s].from.%s",
            RELATIONSHIP_IMS_QP_DEPSubStage2DEPTask,
            RELATIONSHIP_IMS_QP_DEPProjectStage2DEPSubStage,
            RELATIONSHIP_IMS_QP_DEP2DEPProjectStage,
            DomainObject.getAttributeSelect(ATTRIBUTE_IMS_NameRu));

    static final String SOURCE_DEP = "D_E_P"; // Because source lists are checked using indexOf
    static final String SOURCE_DEPTask = "DEPTask";

    public static final String BELL_DELIMITER = "\u0007";

    public static final String relationship_IMS_QP_DEPTask2DEP = "IMS_QP_DEPTask2DEP";
    public static final String relationship_IMS_QP_DEPTask2QPTask = "IMS_QP_DEPTask2QPTask";
    public static final String relationshipIMS_QP_DEPTask2DEPTask = "IMS_QP_DEPTask2DEPTask";
    public static final String relationship_IMS_QP_DEPTaskStatus = "IMS_QP_DEPTaskStatus";
    public static final String relationship_IMS_QP_DEPTask2DEPTask = "IMS_QP_DEPTask2DEPTask";
    public static final String relationship_IMS_QP_QPTask2QPTask = "IMS_QP_QPTask2QPTask";
    public static final String relationship_IMS_QP_QPTask2Fact = "IMS_QP_QPTask2Fact";

    //    feature 43176 'Actual search plan'
    public static final String ANOTHER_PLAN_TYPES = "\u0414\u0440\u0443\u0433\u043e\u0435";
    public static final String VTZ_PLAN_TYPES = "\u0422\u0438\u043f\u044b\u0020\u0412\u0422\u0417";
    public static final String FAMILY_CL = "\u0427\u0435\u043a\u002d\u043b\u0438\u0441\u0442\u044b";
    public static final String OUT_OF_GROUP = "\u0412\u043d\u0435\u0020\u0433\u0440\u0443\u043f\u043f\u044b";
    public static final String RESULT_TYPE_TO_EXPECTED_RESULT = "to[IMS_QP_ResultType2ExpectedResult].from.to[IMS_QP_ResultType2Family].from.name";
    public static final String FAMILY_TO_EXPECTED_RESULT = "to[IMS_QP_ResultType2ExpectedResult].from.name";
    public static final String TYPE_DOCUMENT_SET = "IMS_DocumentSet";
    public static final String BASELINE_TO_QPTASK = "to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_BaseLine2DEPSubStage].from.name";
    public static final String PBS_TO_QPTASK = "to[IMS_QP_QPlan2QPTask].from.from[IMS_QP_QPlan2Object].to.name";
    public static final String PBS_TYPE_TO_QPTASK = "to[IMS_QP_QPlan2QPTask].from.from[IMS_QP_QPlan2Object].to.type";
    public static final String FROM_IMS_QP_QPTASK_2_FACT = "from[IMS_QP_QPTask2Fact]";
    public static final String TO_IMS_BBS_2_CI_FROM = "to[IMS_BBS2CI].from";
    public static final String TO_IMS_PBS_2_DOC_SET_FROM = "to[IMS_PBS2DocSet].from";
    public static final String TO_IMS_REF_CI_2_DOC_FROM = "to[IMS_RefCI2Doc].from";
    public static final String ATTRIBUTE_IMS_EXTERNAL_SYSTEM_URL = "attribute[IMS_ExternalSystemUrl]";
    public static final String ATTRIBUTE_IMS_EXTERNAL_SYSTEM_USER = "attribute[IMS_ExternalSystemUser]";
    public static final String ATTRIBUTE_IMS_EXTERNAL_SYSTEM_PASSWORD = "attribute[IMS_ExternalSystemPassword]";
    public static final String ATTRIBUTE_IMS_IS_LAST_VERSION = "attribute[IMS_IsLastVersion]";

    //    feature 51753: added relationships to the 'select' of feature 43176 'Actual search plan'
    public static final String FROM_IMS_QP_TASK_2_SYSTEM = "from[IMS_QP_QPlan2Object].to.name";
    public static final String FROM_IMS_QP_TASK_2_SYSTEM_TYPE = "from[IMS_QP_QPlan2Object].to.type";
    public static final String PLAN_TO_TASK = "to[IMS_QP_QPlan2QPTask].from.id";
    public static final String STAGE_TO_TASK = "to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_DEPProjectStage2DEPSubStage].from.name";
    public static final String STAGE_TO_TASK_ID = "to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_DEPProjectStage2DEPSubStage].from.id";
    public static final String STAGE_TO_TASK_TYPE = "to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.to[IMS_QP_DEPProjectStage2DEPSubStage].from.type";
    public static final String ATTRIBUTE_STAGE_TO_TASK = "to[IMS_QP_DEPTask2QPTask].from.to[IMS_QP_DEPSubStage2DEPTask].from.attribute[IMS_QP_Stage]";

    public static final String attribute_IMS_QP_DocumentCode = "attribute[IMS_QP_DocumentCode]";
    public static final String attribute_IMS_QP_SelectDocument = "attribute[IMS_QP_SelectDocument]";

    public static final String EXTERNAL_DOCUMENT = "IMS_ExternalDocumentSet";
    public static final String HNH_PRODUCTION_SRV = "97";

    public static final String PLAN_SEARCH_REPORT_TEMPLATE_PATH = "C:\\R2019X\\3DSpace\\workspace\\templates\\51753.xlsx";
    public static final String SQP_REPORT_TEMPLATE_PATH = "C:\\R2019X\\3DSpace\\workspace\\templates\\sqp_report.xlsx";
    public static final String DEP_REPORT_TEMPLATE_PATH = "C:\\R2019X\\3DSpace\\workspace\\templates\\dep_report.xlsx";
    public static final String GRP_REPORT_TEMPLATE_PATH = "C:\\R2019X\\3DSpace\\workspace\\templates\\grp_report.xlsx";
}
