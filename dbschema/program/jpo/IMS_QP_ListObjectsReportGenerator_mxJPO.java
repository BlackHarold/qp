import com.matrixone.apps.domain.DomainConstants;
import com.matrixone.apps.domain.DomainObject;
import com.matrixone.apps.domain.util.FrameworkException;
import com.matrixone.apps.domain.util.MapList;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.Query;
import matrix.db.QueryList;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.Logger;

public class IMS_QP_ListObjectsReportGenerator_mxJPO {
    private static final Logger LOG = Logger.getLogger("reportLogger");

    /**
     * Type report chooser
     * @param ctx usual parameter
     * @param type of report
     * @param ids id objects to molding the report
     * @return list of business objects by type of the report
     */
    public BusinessObjectWithSelectList reportGeneration(Context ctx, String type, String... ids) {

        BusinessObjectWithSelectList boList;

        switch (type) {
            case "SQP":
                boList = getByTypeAndSelectedIds(ctx,
                        IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan, ids);
                break;
            case "DEP":
                boList = getByTypeAndSelectedIds(ctx,
                        IMS_QP_Constants_mxJPO.type_IMS_QP_DEP, ids);
                break;
            case "GROUP":
                boList = getByTypeAndSelectedIds(ctx,
                        IMS_QP_Constants_mxJPO.type_IMS_QP_Classifier, ids);
                break;
            default:
                throw new IllegalStateException("Unexpected value: " + type);
        }

        return boList;
    }

    /**
     * Getting all objects by type
     *
     * @param ctx  usual parameter
     * @param type type of objects
     * @param ids  selected ids
     * @return List of business objects
     */
    public BusinessObjectWithSelectList getByTypeAndSelectedIds(Context ctx, String type, String... ids) {

        //Do the query
        BusinessObjectWithSelectList businessObjectList = new BusinessObjectWithSelectList();
        QueryList queryList = new QueryList();
        try {
            for (String id : ids) {
                Query query = getQueryByTypeAndIds(type, id);
                queryList.add(query);
            }

            for (Query query : queryList) {
                businessObjectList.addAll(query.selectTmp(ctx, getBusinessSelect()));
            }
        } catch (MatrixException e) {
            LOG.error("matrix error: " + e.getMessage());
            e.printStackTrace();
        }
        return businessObjectList;
    }

    /**
     * Method take type and set parameters for query
     *
     * @param type of object for searching
     * @param id   where selected id
     * @return matrix.db.Query
     */
    private Query getQueryByTypeAndIds(String type, String id) {

        //Prepare temp query
        Query query = new Query();
        query.setBusinessObjectType(type);
        query.setOwnerPattern("*");
        query.setVaultPattern(IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION);
        query.setWhereExpression(whereInitiate(id));

        return query;
    }

    private String whereInitiate(String id) {

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("id==" + id);

        return stringBuilder.toString();
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
        busSelect.addElement(DomainConstants.SELECT_CURRENT);
        busSelect.addElement(DomainConstants.SELECT_OWNER);
        busSelect.addElement(DomainConstants.SELECT_ORIGINATED);
        busSelect.addElement("from[IMS_QP_QPlan2Object].to.name");
        busSelect.addElement("from[IMS_QP_QPlan2Object].to.attribute[IMS_DescriptionEn]");
        busSelect.addElement("from[IMS_QP_QPlan2Object].to.attribute[IMS_DescriptionRu]");

        //DEP
        busSelect.addElement(String.format("to[%s].from.name",IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan));
        busSelect.addElement(String.format("to[%s].from.description",IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2QPlan));
        //Classifier
        busSelect.addElement(String.format("from[%s].to.name", IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2Classifier));
        busSelect.addElement(String.format("to[%s].from.name", IMS_QP_Constants_mxJPO.relationship_IMS_QP_DEP2Classifier));
        busSelect.addElement(String.format("to[%s].from.name", IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan));
        busSelect.addElement(String.format("to[%s].from.attribute[IMS_Name]", IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan));
        busSelect.addElement(String.format("to[%s].from.attribute[IMS_NameRu]", IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan));
        busSelect.addElement(String.format("from[%s].to.name", IMS_QP_Constants_mxJPO.relationship_IMS_QP_Classifier2QPlan));

        return busSelect;
    }

    public MapList getReports(Context ctx, String... args) {

        MapList allUnits = new MapList();
        StringList selects = new StringList("id");
        selects.add("owner");

        try {
            allUnits = DomainObject.findObjects(ctx,
                    /*type*/ IMS_QP_Constants_mxJPO.type_IMS_QP_ReportUnit,
                    /*vault*/ IMS_QP_Constants_mxJPO.ESERVICE_PRODUCTION,
                    /*where*/"owner==" + ctx.getUser(),
                    /*select*/ selects);
        } catch (FrameworkException frameworkException) {
            frameworkException.printStackTrace();
        }

        LOG.info("allUnits: " + allUnits);
        return allUnits;
    }
}
