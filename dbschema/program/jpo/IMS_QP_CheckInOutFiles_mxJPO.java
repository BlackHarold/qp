import matrix.db.BusinessObject;
import matrix.db.Context;
import matrix.db.FileList;
import matrix.db.JPO;
import matrix.util.MatrixException;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class IMS_QP_CheckInOutFiles_mxJPO {
    private static final Logger LOG = Logger.getLogger("reportLogger");

    public File writeToFile(Context ctx, String reportName, Workbook workbook) {
        String workspace = getWorkspacePath(ctx);
        String fileName = reportName + ".xlsx";

        File file = new File(workspace + "\\" + fileName);

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            workbook.write(fos);

        } catch (FileNotFoundException e) {
            LOG.error("FileNotFoundException: " + e.getMessage());
            e.printStackTrace();

        } catch (IOException ioException) {
            LOG.error("IO error: " + ioException.getMessage());
            ioException.printStackTrace();

        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException ioException) {
                    LOG.error("IO error: " + ioException.getMessage());
                    ioException.printStackTrace();
                }
            }

            return file;
        }
    }

    public boolean checkIn(Context ctx, String objectId, File file) {

        String fileName = file.getName();
        String filePath = file.getParent();

        BusinessObject bo;
        try {
            bo = new BusinessObject(objectId);
            ctx.connect();
            bo.open(ctx);
        } catch (Exception e) {
            System.out.println("business object error: " + e.getMessage());
            LOG.error("business object error: " + e.getMessage());
            return false;
        }

        /*checkin method*/
        try {
            bo.checkinFile(ctx,
                    true, // false to lock file
                    false, // true to append file
                    "", // different host
                    "generic", // format
                    fileName, //file
                    filePath); //path
            bo.close(ctx);

        } catch (MatrixException me) {
            LOG.error("checkin: " + me.getMessage());
            return false;
        }

        FileUtils.deleteQuietly(new File(filePath));

        return true;
    }

    private String getWorkspacePath(Context ctx) {
        String workspace = "";
        try {
            workspace = ctx.createWorkspace();
        } catch (MatrixException matrixException) {
            matrixException.printStackTrace();
        }
        return workspace;
    }

    public Map<String, Object> checkout(Context ctx, String... args) throws Exception {
        Map<String, Object> map = new HashMap();

        Map programMap = JPO.unpackArgs(args);

        String[] rowIDs = (String[]) programMap.get("emxTableRowId");
        if (rowIDs.length == 0) {
            return null;
        }

        String[] cleanedIDs = new String[rowIDs.length];
        for (int i = 0; i < rowIDs.length; i++) {
            if (rowIDs[i].contains("|")) {
                String[] rowIdArray = rowIDs[i].split("\\|");
                cleanedIDs[i] = rowIdArray[1];
            } else {
                cleanedIDs[i] = rowIDs[i];
            }
        }

        String objectId = cleanedIDs[0];

        BusinessObject bo = null;
        try {
            bo = new BusinessObject(objectId);
            ctx.connect();
            bo.open(ctx);
        } catch (Exception e) {
            LOG.error("business object error: " + e.getMessage());
        }

        String workspace = getWorkspacePath(ctx);
        FileList files = bo.getFiles(ctx);
        if (files.size() > 1) {
            return null;
        }
        matrix.db.File mxFile = (matrix.db.File) files.get(0);
        String fileName = mxFile.getName();

        /*checkout method*/
        try {
            ctx.connect();
            ctx.createWorkspace();

            bo.checkoutFile(ctx,
                    false, //true to lock file
                    "generic", //format
                    fileName,
                    workspace);
            bo.close(ctx);
        } catch (MatrixException me) {
            LOG.error("matrix exception: " + me.getMessage());
            System.out.println("an error occurred");
        }

        /*read'n'write to workbook*/
        String absolutePath = workspace + "\\" + fileName;
        LOG.info("absolute path: " + absolutePath);

        /*send to the JSP*/
        map.put("fileName", fileName);
        map.put("byteArray", getOutArray(absolutePath));

        FileUtils.deleteQuietly(new File(workspace));

        return map;
    }

    private byte[] getOutArray(String fileName) {
        File file = new File(fileName);
        byte[] outArray = new byte[0];

        try {
            outArray = Files.readAllBytes(file.toPath());
            return outArray;
        } catch (IOException var5) {
            var5.printStackTrace();
            return outArray;
        }
    }
}
