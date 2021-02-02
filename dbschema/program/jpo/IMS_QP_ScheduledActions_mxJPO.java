import com.matrixone.apps.domain.util.FrameworkUtil;
import matrix.db.Context;
import matrix.db.JPO;
import matrix.util.MatrixException;

import java.util.HashMap;
import java.util.Map;

public class IMS_QP_ScheduledActions_mxJPO {

    /**
     * Entry point to the search process
     *
     * @param ctx usual parameter
     */
    public void startPlanSelector(Context ctx, String... args) {
        Map argsMap = new HashMap();

        if (args != null) {
            try {
                argsMap.put("user", args[0]);
                argsMap.put("password", args[1]);
                if (args.length > 2) {
                    argsMap.put("objectId", args[2]);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            try {

                /*set name'n'password*/
                ctx = setContext(ctx, args[0], args[1]);
                if (ctx.isAssigned("IMS_Admin")) {
                    JPO.invoke(ctx, "IMS_QP_ActualPlanSearch", new String[]{}, "searchProcess", JPO.packArgs(argsMap));
                } else {
                    System.out.println("context has need to be assign IMS_Admin role");
                }

            } catch (MatrixException me) {
                me.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Setting new context with arguments username'n'password
     *
     * @param ctx
     * @param user
     * @param password
     * @return
     */
    private Context setContext(Context ctx, String user, String password) {
        Context context = null;

        //disconnect old context
        try {
            ctx.connect();
            ctx.disconnect();

        } catch (MatrixException e) {
            System.out.println("context disconnecting error");
        }

        //set new context
        try {
            context = new Context(/*empty localhost*/"");
            context.setUser(user);
            context.setPassword(FrameworkUtil.decrypt(password));
            context.setVault(ctx.getVault().getName());
            context.connect();

        } catch (MatrixException e) {
            System.out.println("context setting error: " + e.getMessages());
        } catch (Exception e) {
            System.out.println("decrypting error: " + e.getMessage());
        }

        return context;
    }
}
