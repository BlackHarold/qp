import com.matrixone.apps.domain.DomainConstants;
import matrix.db.BusinessObjectWithSelectList;
import matrix.db.Context;
import matrix.db.Query;
import matrix.util.MatrixException;
import matrix.util.StringList;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class IMS_QP_ActualPlanSearchGenerator_mxJPO {

    public Map<String, BusinessObjectWithSelectList> reportGeneration(Context ctx) {

        Map map = new HashMap();

        BusinessObjectWithSelectList sqpList = getByType(ctx,
                IMS_QP_Constants_mxJPO.type_IMS_QP_QPlan);
        map.put("SQP", sqpList);

        BusinessObjectWithSelectList depList = getByType(ctx,
                IMS_QP_Constants_mxJPO.type_IMS_QP_DEP);
        map.put("DEP", depList);

        BusinessObjectWithSelectList groupList = getByType(ctx,
                IMS_QP_Constants_mxJPO.type_IMS_QP_Classifier);
        map.put("Group SQP,BQPS", groupList);

        return map;
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
}
