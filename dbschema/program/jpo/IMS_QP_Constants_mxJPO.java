public class IMS_QP_Constants_mxJPO {

    public static final String FROM_IMS_QP_DEPSUB_STAGE_2_DEPTASK = "from[IMS_QP_DEPSubStage2DEPTask]";
    public static final String TO_IMS_QP_BASE_LINE_2_DEPSUB_STAGE_FROM_ID = "to[IMS_QP_BaseLine2DEPSubStage].from.id";
    public static final String TO_IMS_QP_DEPPROJECT_STAGE_2_DEPSUB_STAGE_FROM_ID = "to[IMS_QP_DEPProjectStage2DEPSubStage].from.id==";
    public static final String FROM_IMS_QP_DEP_2_DEPPROJECT_STAGE_TO_TO_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_FROM_ID = "from[IMS_QP_DEP2DEPProjectStage].to.to[IMS_QP_ProjectStage2DEPProjectStage].from.id";
    public static final String FROM_IMS_QP_PROJECT_STAGE_2_DEPPROJECT_STAGE_TO_TO_IMS_QP_DEP_2_DEPPROJECT_STAGE_FROM_ID = "from[IMS_QP_ProjectStage2DEPProjectStage].to.to[IMS_QP_DEP2DEPProjectStage].from.id==";
    public static final String IMS_QP_DEP_SUB_STAGE = "IMS_QP_DEPSubStage";

    private IMS_QP_Constants_mxJPO() {
    }

    public static final String BELL_DELIMITER = "\u0007";

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
    public static final String type_IMS_Family = "IMS_Family";
    public static final String type_IMS_PBSSystem = "IMS_PBSSystem";
    public static final String type_IMS_GBSBuilding = "IMS_GBSBuilding";
    public static final String type_IMS_PBSFunctionalArea = "IMS_PBSFunctionalArea";

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

    public static final String relationship_IMS_QP_DEPTask2QPTask = "IMS_QP_DEPTask2QPTask";
    public static final String relationshipIMS_QP_DEPTask2DEPTask = "IMS_QP_DEPTask2DEPTask";
    public static final String relationship_IMS_QP_DEPTaskStatus = "IMS_QP_DEPTaskStatus";
    public static final String relationship_IMS_QP_DEPTask2DEPTask = "IMS_QP_DEPTask2DEPTask";
    public static final String relationship_IMS_QP_QPTask2QPTask = "IMS_QP_QPTask2QPTask";
}
