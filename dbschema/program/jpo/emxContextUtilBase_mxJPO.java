/*
**   emxContextUtilBase
**
**   Copyright (c) 1992-2018 Dassault Systemes.
**   All Rights Reserved.
**   This program contains proprietary and trade secret information of MatrixOne,
**   Inc.  Copyright notice is precautionary only
**   and does not evidence any actual or intended publication of such program
**
*/

import matrix.db.Context;
import com.matrixone.apps.domain.util.ContextUtil;
import com.matrixone.apps.domain.util.MessageUtil;
import com.matrixone.apps.domain.util.MqlUtil;
import com.matrixone.apps.domain.util.PropertyUtil;


/**
 * The <code>emxContextUtilBase</code> class contains static methods for managing context.
 *
 * @version AEF 9.5.1.0 - Copyright (c) 2003, MatrixOne, Inc.
 */

public class emxContextUtilBase_mxJPO
{

    /** Create an instance of emxMailUtil JPO. */
    static protected emxMailUtil_mxJPO mailUtil = null;
    /** Declare boolean variable. */
    static protected boolean keepWorkAround = false;

    /**
     * Constructor.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public emxContextUtilBase_mxJPO (Context context, String[] args)
      throws Exception
    {

        /*if (!context.isConnected())
            throw new Exception("not supported on desktop client");
        */
        mailUtil = new emxMailUtil_mxJPO(context, null);
       String propValue = MessageUtil.getMessage(context, "emxFramework.BeanPushPopContextWorkaround", null, null, null, "emxSystem");
        try
        {
            keepWorkAround = Boolean.valueOf(propValue).booleanValue();
        }
        catch (Exception e)
        {
            keepWorkAround = false;
        }

    }

    /**
     * This method gets the super user name.
     *
     * @param context the eMatrix <code>Context</code> object
     * @return a String contains the super user name
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    protected static String getSuperUserName(Context context)
        throws Exception
    {
        return PropertyUtil.getSchemaProperty(context, "person_UserAgent");
    }


    /**
     * This method sets the user context with the user name passed and
     *   if no user is passed, then the context will be set with the shadow agent user.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *        0 - String containing the user name
     *        1 - String containing the user password
     *        2 - String containing the user vault
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int pushContext(Context context,  String[] args)
      throws Exception
    {

        String userName  = null;
        String superName = getSuperUserName(context);
        String password  = null;
        String vault = null;
        if (args != null && args.length > 0 && args[0].length() > 0) {
            userName = args[0];
        } else {
      // If user not passed then default to User Agent
            userName = superName;
        }

        if (args != null && args.length > 1 && args[1].length() > 0) {
            password = args[1];
        } else {
        }

        if (args != null && args.length > 2 && args[2].length() > 0) {
            vault = args[2];
        } else {
            // get context vault
            vault = context.getVault().getName();
        }
        
        ContextUtil.pushContext(context, userName, password, vault);

        return 0;
    }

    /**
     * This method resets the user context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int popContext(Context context, String[] args)
      throws Exception
    {

    	ContextUtil.popContext(context);
        return 0;
    }

    /**
     * This method sets the Bean context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds the following input arguments:
     *         0 - String containing the user name
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int pushBeanContext(Context context, String[] args)
      throws Exception
    {
        if (keepWorkAround)
        {
            ContextUtil.pushContext(context, args[0], null, null);
        }
        return 0;
    }

    /**
     * This method resets Bean context.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param args holds no arguments
     * @return  int 0, status code
     * @throws Exception if the operation fails
     * @since AEF 9.5.1.0
     */

    public int popBeanContext(Context context, String[] args)
      throws Exception
    {
        if (keepWorkAround)
        {
            ContextUtil.popContext(context);
        }
        return 0;
    }

    /**
     * This method displays mql error message.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param error String contains error message
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */

    public static void mqlError(Context context, String error)
        throws Exception
    {
    	MqlUtil.mqlCommand(context, "error $1",error);

    }

    /**
     * This method displays mql notice message.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param notice String containing notice message
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */

    public static void mqlNotice(Context context, String notice)
        throws Exception
    {
        MqlUtil.mqlCommand(context, "notice $1", notice);
    }

    /**
     * This method displays mql warning message.
     *
     * @param context the eMatrix <code>Context</code> object
     * @param warning String containing warning message
     * @throws Exception if the operation fails
     * @since AEF Rossini
     */
    public static void mqlWarning(Context context, String warning)
        throws Exception
    {
        MqlUtil.mqlCommand(context, "warning $1", warning);
    }
}