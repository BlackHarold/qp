import com.matrixone.apps.framework.ui.UIUtil;
import matrix.db.Context;
import matrix.util.MatrixException;

import java.io.*;

public class IMS_QP_StaticUtilities_mxJPO {

    public static void writeToLog(Context ctx, String path, String text) {
        String fileName = "actual_search.log";
        writeToFile(ctx, path, fileName, text);
    }

    private static void writeToFile(Context ctx, String path, String fileName, String text) {
        String fullFilePath = (UIUtil.isNotNullAndNotEmpty(path) ? path : getWorkspacePath(ctx)) + "\\" + fileName;

        File file = new File(fullFilePath);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file, true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        PrintWriter printWriter = null;
        try {
            printWriter = new PrintWriter(new OutputStreamWriter(fileOutputStream, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        printWriter.println(text);

        if (printWriter != null) {
            printWriter.close();
        }
    }

    private static String getWorkspacePath(Context ctx) {
        String workspace = "";
        try {
            workspace = ctx.createWorkspace();
        } catch (MatrixException matrixException) {
            matrixException.printStackTrace();
        }
        return workspace;
    }
}
