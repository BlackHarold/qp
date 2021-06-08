import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Workbook;

import java.util.HashMap;
import java.util.Map;

public class IMS_QP_ExcelUtil_mxJPO {

    public static Map<String, CellStyle> getStyle(Workbook wb) {

        /***************** Fonts **********************/

        //black color, size 8
        Font font_8_black = wb.createFont();
        font_8_black.setFontHeightInPoints((short) 8);

        //green color, size 8
        Font font_8_green = wb.createFont();
        font_8_green.setFontHeightInPoints((short) 8);
        font_8_green.setColor(HSSFColor.GREEN.index);

        //red color, size 8
        Font font_8_red = wb.createFont();
        font_8_red.setFontHeightInPoints((short) 8);
        font_8_red.setColor(HSSFColor.RED.index);

        //black color, size 11
        Font font_11_black = wb.createFont();
        font_11_black.setFontHeightInPoints((short) 11);

        //green color, size 11
        Font font_11_green = wb.createFont();
        font_11_green.setFontHeightInPoints((short) 11);
        font_11_green.setColor(HSSFColor.GREEN.index);

        //red color, size 11
        Font font_11_red = wb.createFont();
        font_11_red.setFontHeightInPoints((short) 11);
        font_11_red.setColor(HSSFColor.RED.index);

        /***************** Styles **********************/

        CellStyle black8_no_wrap = wb.createCellStyle();
        black8_no_wrap.setAlignment(CellStyle.ALIGN_LEFT);
        black8_no_wrap.setFont(font_8_black);
        black8_no_wrap.setWrapText(false);

        CellStyle black8_align_left = wb.createCellStyle();
        black8_align_left.setAlignment(CellStyle.ALIGN_LEFT);
        black8_align_left.setFont(font_8_black);
        black8_align_left.setWrapText(true);

        CellStyle black8_green_bgd = wb.createCellStyle();
        black8_green_bgd.setAlignment(CellStyle.ALIGN_LEFT);
        black8_green_bgd.setFont(font_8_black);
        black8_green_bgd.setWrapText(false);
        black8_green_bgd.setFillForegroundColor(HSSFColor.LIGHT_GREEN.index);
        black8_green_bgd.setFillPattern(CellStyle.SOLID_FOREGROUND);

        CellStyle black8 = wb.createCellStyle();
        black8.setAlignment(CellStyle.ALIGN_CENTER);
        black8.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        black8.setFont(font_8_black);
        black8.setWrapText(true);

        CellStyle green8 = wb.createCellStyle();
        green8.setAlignment(CellStyle.ALIGN_CENTER);
        green8.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        green8.setFont(font_8_green);
        green8.setWrapText(true);


        //align center, font color red, font size 8
        CellStyle red8 = wb.createCellStyle();
        red8.setAlignment(CellStyle.ALIGN_CENTER);
        red8.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        red8.setFont(font_8_red);
        red8.setWrapText(true);

        //align center, font color black, font size 11
        CellStyle black11 = wb.createCellStyle();
        black11.setAlignment(CellStyle.ALIGN_CENTER);
        black11.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        black11.setFont(font_11_black);
        black11.setWrapText(true);

        //align left, font color black, font size 11
        CellStyle black11left = wb.createCellStyle();
        black11left.setAlignment(CellStyle.ALIGN_LEFT);
        black11left.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        black11left.setFont(font_11_black);
        black11left.setWrapText(true);

        //align center, font color green, font size 11
        CellStyle green11left = wb.createCellStyle();
        green11left.setAlignment(CellStyle.ALIGN_LEFT);
        green11left.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        green11left.setFont(font_11_green);
        green11left.setWrapText(true);

        //align center, font color green, font size 11
        CellStyle red11left = wb.createCellStyle();
        red11left.setAlignment(CellStyle.ALIGN_LEFT);
        red11left.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
        red11left.setFont(font_11_red);
        red11left.setWrapText(true);

        /**
         * All new styles should be added at this map
         */
        Map<String, CellStyle> styles = new HashMap<>();
        styles.put("black8_no_wrap", black8_no_wrap);
        styles.put("black8_align_left", black8_align_left);
        styles.put("black8_green_bgd", black8_green_bgd);
        styles.put("black8", black8);
        styles.put("green8", green8);
        styles.put("black11", black11);
        styles.put("black11left", black11left);
        styles.put("green11left", green11left);
        styles.put("red8", red8);
        styles.put("red11left", red11left);

        return styles;
    }
}