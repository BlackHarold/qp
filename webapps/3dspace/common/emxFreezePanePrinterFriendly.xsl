<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="html" version="1.0" encoding="UTF-8" indent="yes"/>
    <xsl:variable name="split" select="/mxRoot/setting[@name='split']"/>
    <xsl:variable name="compare" select="/mxRoot/requestMap/setting[@name='IsStructureCompare']"/>
    <xsl:variable name="sbMode" select="/mxRoot/setting[@name='sbMode']"/>

    <xsl:variable name="showDiffCodeIcon" select="/mxRoot/requestMap/setting[@name='diffCodeIcons']"/>
    <xsl:variable name="showSummaryIcons" select="/mxRoot/requestMap/setting[@name='summaryIcons']"/>

    <xsl:variable name="reportType">
        <xsl:if test="/mxRoot/requestMap/setting[@name='IsStructureCompare'] = 'TRUE'">
            <xsl:value-of select="/mxRoot/requestMap/setting[@name='reportType']"/>
        </xsl:if>
    </xsl:variable>

    <xsl:variable name="isRenderPdf" select="/mxRoot/requestMap/setting[@name='IsRenderPdf']"/>

    <!--
 ! root template
 !-->
    <xsl:template match="mxRoot">
        <html>
            <head>
                <title>Print report</title>
                <meta http-equiv="imagetoolbar" content="no"/>
                <meta http-equiv="pragma" content="no-cache"/>
                <meta http-equiv="content-type" content="text/plain; charset=UTF-8"/>
                <link rel="stylesheet" href="styles/emxUIDefaultPF.css"/>
                <link rel="stylesheet" href="styles/emxUIListPF.css"/>
                <script type="text/javascript" src="../common/scripts/emxUIConstants.js">
                    /* Bug 368799 If a script element is 'self-closed' , the validators will not pick it considering its
                    not well formed or it is not validly placed */
                </script>
                <script type="text/javascript" src="../common/scripts/emxUICore.js"/>
                <script>
                    function printData() {
                    var divToPrint = document.getElementById("html_to_print");
                    newWin = window.open("");
                    newWin.document.write(divToPrint.outerHTML);
                    window.close();
                    newWin.print();
                    newWin.close();
                    }

                    tableToExcel = (function () {
                    var uri = 'data:application/vnd.ms-excel;base64,',
                    template = '{table}',
                    base64 = function (s) {
                    return window.btoa(unescape(encodeURIComponent(s)))
                    }
                    , format = function (s, c) {
                    return s.replace(/{(\w+)}/g, function (m, p) {
                    return c[p];
                    })
                    }

                    downloadURI = function (uri, name) {
                    var link = document.createElement("a");
                    link.download = name;
                    link.href = uri;
                    link.click();
                    }

                    return function (table, name, fileName) {
                    if (!table.nodeType) table = document.getElementById(table)
                    var ctx = {worksheet: name || 'Worksheet', table: table.innerHTML}
                    var resuri = uri + base64(format(template, ctx))
                    downloadURI(resuri, fileName);
                    }
                    })();
                </script>
                <style>
                    .button13 {
                    display: inline-block;
                    width: 15em;
                    font-size: 80%;
                    color: rgba(255,255,255,.9);
                    text-shadow: #2e7ebd 0 1px 2px;
                    text-decoration: none;
                    text-align: center;
                    line-height: 1.1;
                    white-space: pre-line;
                    padding: .7em 5em;
                    border: 1px solid;
                    border-color: #60a3d8 #2970a9 #2970a9 #60a3d8;
                    border-radius: 6px;
                    outline: none;
                    background: #60a3d8 linear-gradient(#89bbe2, #60a3d8 50%, #378bce);
                    box-shadow: inset rgba(255,255,255,.5) 1px 1px;
                    }
                    .button13:first-line{
                    font-size: 170%;
                    font-weight: 700;
                    }
                    .button13:hover {
                    color: rgb(255,255,255);
                    background-image: linear-gradient(#9dc7e7, #74afdd 50%, #378bce);
                    }
                    .button13:active {
                    color: rgb(255,255,255);
                    border-color: #2970a9;
                    background-image: linear-gradient(#5796c8, #6aa2ce);
                    box-shadow: none;
                    }

                    input{
                    margin-right: 20px
                    }
                </style>
            </head>
            <body>
                <input type="button" class="button13" value="Print report" onclick="printData()"/>
                <input type="button" class="button13" value="Export to XLS" onclick="tableToExcel('html_to_print', 'Report table', 'report.xls')"/>
                <hr noshade="noshade"/>
                <div id="html_to_print">
                    <table width="100%">
                        <tr class="page-head">
                            <td width="60%" class="pageHeader">&#160;<xsl:value-of
                                    select="//setting[@name='PageHeader']"/>
                                <xsl:if test="/mxRoot/tableControlMap/setting[@name='subHeaderDisplay']">
                                    <br/>
                                    <span class="pageSubTitle">&#160;<xsl:value-of
                                            select="/mxRoot/tableControlMap/setting[@name='subHeaderDisplay']"/>
                                    </span>
                                </xsl:if>
                            </td>
                            <td width="39%" align="right">&#160;</td>
                            <td nowrap="nowrap">&#160;</td>
                        </tr>
                    </table>
                    <hr noshade="noshade"/>
                    <table width="100%">
                        <xsl:apply-templates select="columns"/>
                        <xsl:apply-templates select="rows"/>
                    </table>
                </div>
            </body>
        </html>
    </xsl:template>

    <!--
 ! columns template
 !-->
    <xsl:template match="columns">
        <xsl:variable name="isIndentedView" select="//setting[@name='isIndentedView']"/>
        <thead>
            <xsl:if test="//setting/@name='Col Span'">
                <tr>
                    <!-- <td>&#160;</td> -->
                    <xsl:if test="normalize-space($isIndentedView) = 'true'">
                        <td>&#160;</td>
                    </xsl:if>
                    <xsl:if test="$reportType = 'Complete_Summary_Report' and /mxRoot/tableControlMap/setting[@name='HasDiffCodeColumn']/value = 'true'">
                        <td>&#160;</td>
                    </xsl:if>
                    <xsl:apply-templates select="column" mode="group-header"/>
                </tr>
            </xsl:if>
            <xsl:if test="$reportType = 'Complete_Summary_Report' and /mxRoot/tableControlMap/setting[@name='HasDiffCodeColumn']/value = 'true'">
                <tr>
                    <!--  <th rowspan="2"><xsl:value-of select="//setting[@name='Labels']//item[@name='DiffCode']/value"/></th>-->
                    <th rowspan="2">
                        <table>
                            <tr>
                                <xsl:choose>
                                    <xsl:when test="$showDiffCodeIcon = 'true' or $showSummaryIcons = 'true'">
                                        <td class="colHeadIcon">
                                            <img src="images/iconColHeadChangeDiff.png"/>
                                        </td>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <td>
                                            <span>
                                                <xsl:value-of
                                                        select="//setting[@name='Labels']//item[@name='DiffCode']/value"/>
                                            </span>
                                        </td>
                                    </xsl:otherwise>
                                </xsl:choose>

                            </tr>
                        </table>
                    </th>
                </tr>
            </xsl:if>
            <tr>
                <xsl:if test="normalize-space($isIndentedView) = 'true'">
                    <xsl:choose>
                        <xsl:when test="/mxRoot/requestMap/setting[@name='HasMergedCell']/value = 'true'">
                            <th rowspan="2">
                                <xsl:value-of select="//setting[@name='Labels']//item[@name='Level']/value"/>
                            </th>
                        </xsl:when>
                        <xsl:otherwise>
                            <th>
                                <xsl:value-of select="//setting[@name='Labels']//item[@name='Level']/value"/>
                            </th>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
                <xsl:apply-templates select="column">
                    <xsl:with-param name="rowNumber" select="'1'"/>
                </xsl:apply-templates>
            </tr>

            <xsl:if test="/mxRoot/requestMap/setting[@name='HasMergedCell']/value = 'true'">
                <tr>
                    <xsl:apply-templates select="column">
                        <xsl:with-param name="rowNumber" select="'2'"/>
                    </xsl:apply-templates>
                </tr>
            </xsl:if>
        </thead>
    </xsl:template>

    <!--
 ! column template
 !-->
    <xsl:template match="column">
        <xsl:param name="rowNumber" select="."/>
        <xsl:variable name="compareBy" select="string(@compareBy)"/>
        <xsl:variable name="varFieldType" select="settings/setting[@name = 'Column Type']"/>
        <xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
        <xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
        <xsl:variable name="Field_Type" select="translate($varFieldType,$ucletters,$lcletters)"/>
        <xsl:variable name="settingRowNumber" select="settings/setting[@name='Row Number']"/>
        <xsl:variable name="settingRowSpan" select="settings/setting[@name='Row Span']"/>
        <xsl:variable name="settingColumnSpan" select="settings/setting[@name='Column Span']"/>
        <xsl:if test="$rowNumber = '2' and settings/setting[@name='BlankCell'] = 'true'">
            <th>
                <xsl:attribute name="style">border-top: 2px solid white;</xsl:attribute>
                &#160;
            </th>
        </xsl:if>
        <xsl:variable name="displayMode">
            <xsl:choose>
                <xsl:when test="settings/setting[@name = 'Display Mode']">
                    <xsl:value-of select="settings/setting[@name='Display Mode']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>Both</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="($displayMode = 'Both' or ($displayMode = $sbMode)) and not(settings/setting[@name='Printer Friendly'] = 'false')">
            <xsl:if test="($settingRowNumber/text() = $rowNumber)">
                <xsl:choose>
                    <!-- image for the column header-->
                    <xsl:when test="contains(@label,'img') and contains(@label,'src')">
                        <th nowrap="nowrap">
                            <xsl:if test="number($settingRowSpan) &gt; 1">
                                <xsl:attribute name="rowspan">
                                    <xsl:value-of select="$settingRowSpan"/>
                                </xsl:attribute>
                            </xsl:if>
                            <xsl:if test="number(($settingColumnSpan + 1) div 2) &gt; 1">
                                <xsl:attribute name="colspan">
                                    <xsl:value-of select="($settingColumnSpan + 1) div 2"/>
                                </xsl:attribute>
                            </xsl:if>
                            <xsl:if test="settings/setting[@name='Row Number']/text() = '2'">
                                <xsl:attribute name="style">border-top: 1px solid white; height: 27;</xsl:attribute>
                            </xsl:if>

                            <xsl:if test="$compareBy = 'true'">
                                <xsl:attribute name="class">mx_compare-column</xsl:attribute>
                            </xsl:if>
                            <xsl:copy-of select="imageHeader/node()"/>
                        </th>
                    </xsl:when>
                    <xsl:when test="@type='separator'">
                        <td class="separator" width="10" style="background-color:white">
                            <xsl:attribute name="rowspan">
                                <xsl:if test="/mxRoot/requestMap/setting[@name='HasMergedCell']/value = 'true'">
                                    <xsl:value-of select="2"/>
                                </xsl:if>
                            </xsl:attribute>
                            &#160;
                        </td>
                    </xsl:when>
                    <xsl:when test="contains(@label,'nbsp')">
                        <th>
                            <xsl:if test="number($settingRowSpan) &gt; 1">
                                <xsl:attribute name="rowspan">
                                    <xsl:value-of select="$settingRowSpan"/>
                                </xsl:attribute>
                            </xsl:if>

                            <xsl:if test="number(($settingColumnSpan + 1) div 2) &gt; 1">
                                <xsl:attribute name="colspan">
                                    <xsl:value-of select="($settingColumnSpan + 1) div 2"/>
                                </xsl:attribute>
                            </xsl:if>

                            <xsl:if test="settings/setting[@name='Row Number']/text() = '2'">
                                <xsl:attribute name="style">border-top: 1px solid white; height: 27;</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="$compareBy = 'true'">
                                <xsl:attribute name="class">mx_compare-column</xsl:attribute>
                            </xsl:if>
                            &#160;
                        </th>
                    </xsl:when>

                    <xsl:when test="$Field_Type = 'checkbox'">
                        <th nowrap="nowrap">

                        </th>
                    </xsl:when>
                    <xsl:when test="$Field_Type = 'image'">
                        <th nowrap="nowrap">
                            <xsl:if test="number($settingRowSpan) &gt; 1">
                                <xsl:attribute name="rowspan">
                                    <xsl:value-of select="$settingRowSpan"/>
                                </xsl:attribute>
                            </xsl:if>

                            <xsl:if test="number(($settingColumnSpan + 1) div 2) &gt; 1">
                                <xsl:attribute name="colspan">
                                    <xsl:value-of select="($settingColumnSpan + 1) div 2"/>
                                </xsl:attribute>
                            </xsl:if>

                            <xsl:if test="settings/setting[@name='Row Number']/text() = '2'">
                                <xsl:attribute name="style">border-top: 1px solid white; height: 27;</xsl:attribute>
                            </xsl:if>

                            <xsl:if test="$compareBy = 'true'">
                                <xsl:attribute name="class">mx_compare-column</xsl:attribute>
                            </xsl:if>
                            <img src='images/iconColHeadImage.gif' align='absmiddle' border='0'/>
                        </th>
                    </xsl:when>
                    <xsl:when test="@type='File'">
                        <th nowrap="nowrap">
                            <xsl:if test="number($settingRowSpan) &gt; 1">
                                <xsl:attribute name="rowspan">
                                    <xsl:value-of select="$settingRowSpan"/>
                                </xsl:attribute>
                            </xsl:if>

                            <xsl:if test="number(($settingColumnSpan + 1) div 2) &gt; 1">
                                <xsl:attribute name="colspan">
                                    <xsl:value-of select="($settingColumnSpan + 1) div 2"/>
                                </xsl:attribute>
                            </xsl:if>

                            <xsl:if test="settings/setting[@name='Row Number']/text() = '2'">
                                <xsl:attribute name="style">border-top: 1px solid white; height: 27;</xsl:attribute>
                            </xsl:if>

                            <xsl:if test="$compareBy = 'true'">
                                <xsl:attribute name="class">mx_compare-column</xsl:attribute>
                            </xsl:if>
                            <img src='images/iconSmallPaperclipVertical.gif' border='0'/>
                        </th>
                    </xsl:when>
                    <xsl:otherwise>
                        <th>
                            <xsl:if test="number($settingRowSpan) &gt; 1">
                                <xsl:attribute name="rowspan">
                                    <xsl:value-of select="$settingRowSpan"/>
                                </xsl:attribute>
                            </xsl:if>

                            <xsl:if test="number(($settingColumnSpan + 1) div 2) &gt; 1">
                                <xsl:attribute name="colspan">
                                    <xsl:value-of select="($settingColumnSpan + 1) div 2"/>
                                </xsl:attribute>
                            </xsl:if>

                            <xsl:if test="settings/setting[@name='Row Number']/text() = '2'">
                                <xsl:attribute name="style">border-top: 1px solid white; height: 27;</xsl:attribute>
                            </xsl:if>
                            <xsl:if test="$compareBy = 'true'">
                                <xsl:attribute name="class">mx_compare-column</xsl:attribute>
                            </xsl:if>
                            <xsl:value-of select="@label"/>
                        </th>
                    </xsl:otherwise>
                </xsl:choose>
                <xsl:if test="position() = $split and $compare = 'TRUE'">
                    <td class="separator" width="10">&#160;</td>
                </xsl:if>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <!--
 ! column template (for group headers)
 !-->
    <xsl:template match="column" mode="group-header">
        <xsl:if test="not($reportType = '') and position() = $split">
            <td>&#160;</td>
        </xsl:if>
        <xsl:variable name="displayMode">
            <xsl:choose>
                <xsl:when test="settings/setting[@name = 'Display Mode']">
                    <xsl:value-of select="settings/setting[@name='Display Mode']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>Both</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="($displayMode = 'Both' or ($displayMode = $sbMode)) and not(settings/setting[@name='Printer Friendly'] = 'false')">
            <xsl:if test="settings/setting[@name='Row Number'] = '1'">
                <xsl:variable name="currentText" select="settings/setting[@name='Group Header']/text()"/>
                <xsl:choose>
                    <xsl:when test="settings/setting/@name='Col Span'">
                        <xsl:if test="settings/setting/@name='Group Header' and settings/setting[@name='Col Span']/text() != '0'">
                            <th class="mx_group-header">
                                <xsl:if test="number(settings/setting[@name='Col Span']/text()) &gt; 1">
                                    <xsl:attribute name="colspan">
                                        <xsl:value-of select="settings/setting[@name='Col Span']/text()"/>
                                    </xsl:attribute>
                                </xsl:if>
                                <xsl:value-of select="$currentText"/>
                            </th>
                            <!-- <th width="6"/> -->
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <td>
                            <xsl:value-of select="'&#160;'"/>
                        </td>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <!--
 ! rows template
 !-->
    <xsl:template match="rows">
        <tbody>
            <xsl:apply-templates select="*"/>
        </tbody>
    </xsl:template>

    <!--
 ! row template
 !-->
    <xsl:template match="r">
        <xsl:variable name="isIndentedView" select="//setting[@name='isIndentedView']"/>
        <xsl:variable name="display" select="string(@display)"/>
        <xsl:variable name="filter" select="string(@filter)"/>
        <xsl:variable name="displayRow" select="string(@displayRow)"/>
        <xsl:variable name="diffcode" select="string(@diffcode)"/>
        <xsl:variable name="rg" select="string(@rg)"/>
        <xsl:variable name="level" select="string(@level)"/>

        <xsl:variable name="class">
            <xsl:choose>
                <xsl:when test="@status='cut'">
                    <xsl:text>mx_cut</xsl:text>
                </xsl:when>
                <xsl:when test="@status='add'">
                    <xsl:text>mx_add</xsl:text>
                </xsl:when>
                <xsl:when test="@status='changed'">
                    <xsl:text>mx_change</xsl:text>
                </xsl:when>
                <xsl:when test="@status='resequence'">
                    <xsl:text>mx_resequence</xsl:text>
                </xsl:when>
                <xsl:when test="$isRenderPdf='true'">
                    <xsl:text>sb</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>listCell</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="not($filter = 'true')">
            <xsl:if test="not($displayRow = 'false')">
                <tr>
                    <xsl:if test="$rg">
                        <xsl:attribute name="class">
                            <xsl:value-of select="$rg"/> heading
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="$reportType = 'Complete_Summary_Report' and /mxRoot/tableControlMap/setting[@name='HasDiffCodeColumn']/value = 'true'">
                        <xsl:choose>
                            <xsl:when test="count(ancestor-or-self::r) = '1'">
                                <td>&#160;</td>
                            </xsl:when>
                            <xsl:when test="/mxRoot/requestMap/setting[@name='HasMergedCell']/value = 'true'">
                                <xsl:if test="$rg">
                                    <td class="listCell"></td>
                                </xsl:if>
                                <xsl:if test="not($rg)">
                                    <!-- <td class="listCell" rowspan="2"><xsl:value-of select="$diffcode"/></td> -->
                                    <td class="listCell" rowspan="2">

                                        <xsl:choose>
                                            <xsl:when test="$diffcode = 'SUMMOD' and $showSummaryIcons = 'true'">
                                                <a href="javascript:;">
                                                    <img src="images/iconSmallPreviousCostClass.png"/>
                                                </a>
                                            </xsl:when>
                                            <xsl:when test="$diffcode = 'ADD' and $showDiffCodeIcon = 'true'">
                                                <a href="javascript:;">
                                                    <img src="images/iconStatusAdded.gif"/>
                                                </a>
                                            </xsl:when>
                                            <xsl:when test="$diffcode = 'DEL' and $showDiffCodeIcon = 'true'">
                                                <a href="javascript:;">
                                                    <img src="images/iconStatusRemoved.gif"/>
                                                </a>
                                            </xsl:when>
                                            <xsl:when test="$diffcode = 'MOD' and $showDiffCodeIcon = 'true'">
                                                <a href="javascript:;">
                                                    <img src="images/iconStatusChanged.gif"/>
                                                </a>
                                            </xsl:when>
                                            <xsl:when test="$diffcode = 'RESEQ' and $showDiffCodeIcon = 'true'">
                                                <a href="javascript:;">
                                                    <img src="images/iconStatusResequenced.gif"/>
                                                </a>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$diffcode"/>
                                            </xsl:otherwise>

                                        </xsl:choose>

                                    </td>

                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <!--  <td class="listCell"><xsl:value-of select="$diffcode"/></td>-->
                                <td class="listCell">
                                    <xsl:choose>
                                        <xsl:when test="$diffcode = 'SUMMOD' and $showSummaryIcons = 'true'">
                                            <a href="javascript:;">
                                                <img src="images/iconSmallPreviousCostClass.png"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:when test="$diffcode = 'ADD' and $showDiffCodeIcon = 'true'">
                                            <a href="javascript:;">
                                                <img src="images/iconStatusAdded.gif"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:when test="$diffcode = 'DEL' and $showDiffCodeIcon = 'true'">
                                            <a href="javascript:;">
                                                <img src="images/iconStatusRemoved.gif"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:when test="$diffcode = 'MOD' and $showDiffCodeIcon = 'true'">
                                            <a href="javascript:;">
                                                <img src="images/iconStatusChanged.gif"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:when test="$diffcode = 'RESEQ' and $showDiffCodeIcon = 'true'">
                                            <a href="javascript:;">
                                                <img src="images/iconStatusResequenced.gif"/>
                                            </a>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$diffcode"/>
                                        </xsl:otherwise>

                                    </xsl:choose>
                                </td>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                    <!-- row indicating level -->
                    <xsl:if test="normalize-space($isIndentedView) = 'true'">
                        <xsl:choose>
                            <xsl:when test="/mxRoot/requestMap/setting[@name='HasMergedCell']/value = 'true'">
                                <xsl:if test="$rg">
                                    <td class="{$class}"></td>
                                </xsl:if>
                                <xsl:if test="not($rg)">
                                    <td class="{$class}" rowspan="2">
                                        <xsl:value-of select="$level+1"/>
                                    </td>
                                </xsl:if>
                            </xsl:when>
                            <xsl:when test="$rg">
                                <td></td>
                            </xsl:when>
                            <xsl:when test="not($rg)">
                                <td class="listCell">
                                    <xsl:value-of select="$level+1"/>
                                </td>
                            </xsl:when>
                        </xsl:choose>
                    </xsl:if>
                    <!-- other table cells -->
                    <xsl:apply-templates select="c">
                        <xsl:with-param name="rowNumber" select="'1'"/>
                        <xsl:with-param name="class" select="$class"/>
                    </xsl:apply-templates>
                </tr>

                <xsl:if test="/mxRoot/requestMap/setting[@name='HasMergedCell']/value = 'true'">
                    <xsl:if test="$rg">
                        <tr height='0'></tr>
                    </xsl:if>
                    <xsl:if test="not($rg)">
                        <tr>
                            <!-- other table cells -->
                            <xsl:apply-templates select="c">
                                <xsl:with-param name="rowNumber" select="'2'"/>
                                <xsl:with-param name="class" select="$class"/>
                            </xsl:apply-templates>
                        </tr>
                    </xsl:if>
                </xsl:if>
            </xsl:if>
            <!-- any child rows -->
            <xsl:if test="$display = 'block'">
                <xsl:apply-templates select="r"/>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <!--
 ! cell template
 !-->
    <xsl:template match="c">
        <xsl:param name="rowNumber" select="."/>
        <xsl:param name="class" select="."/>

        <xsl:variable name="cellIndex" select="position()"/>
        <xsl:variable name="matchresult" select="string(parent::r/@matchresult)"/>
        <xsl:variable name="compareResult" select="string(@compareResult)"/>
        <xsl:variable name="currentColumn" select="/mxRoot/columns/column[$cellIndex]"/>
        <xsl:variable name="varFieldType" select="$currentColumn/settings/setting[@name = 'Column Type']"/>
        <xsl:variable name="lcletters">abcdefghijklmnopqrstuvwxyz</xsl:variable>
        <xsl:variable name="ucletters">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>
        <xsl:variable name="Field_Type" select="translate($varFieldType,$ucletters,$lcletters)"/>
        <xsl:variable name="settingRowNumber" select="$currentColumn/settings/setting[@name='Row Number']"/>
        <xsl:variable name="iFH" select="string(@iFH) = 'true'"/>
        <xsl:variable name="isRTE" select="string(@rte) = 'true'"/>
        <xsl:variable name="rg" select="parent::r/@rg"/>
        <xsl:variable name="count" select="parent::r/@count"/>
        <xsl:variable name="preserveSpacesCss">
            <xsl:choose>
                <xsl:when
                        test="$currentColumn/settings/setting[@name='Preserve Spaces']='true' or $currentColumn/settings/setting[@name='Preserve Spaces']='TRUE'">
                    <xsl:text>verbatim</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text></xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="displayMode">
            <xsl:choose>
                <xsl:when test="$currentColumn/settings/setting[@name = 'Display Mode']">
                    <xsl:value-of select="$currentColumn/settings/setting[@name='Display Mode']"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>Both</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="tdclass">
            <xsl:choose>
                <xsl:when test="$class != 'listCell'">
                    <xsl:value-of select="$class"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text></xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:variable name="istatus">
            <xsl:choose>
                <xsl:when test="parent::r/@status = 'cut'">
                    <xsl:text>images/iconStatusRemoved.gif</xsl:text>
                </xsl:when>
                <xsl:when test="parent::r/@status = 'add'">
                    <xsl:text>images/iconStatusAdded.gif</xsl:text>
                </xsl:when>
                <xsl:when test="parent::r/@status = 'resequence'">
                    <xsl:text>images/iconStatusResequenced.gif</xsl:text>
                </xsl:when>
                <xsl:when test="parent::r/@status = 'changed'">
                    <xsl:text>images/iconStatusChanged.gif</xsl:text>
                </xsl:when>
                <xsl:when test="parent::r/@status = 'new'">
                    <xsl:text>images/iconActionCreate.gif</xsl:text>
                </xsl:when>
                <xsl:when test="parent::r/@status = 'lookup'">
                    <xsl:text>images/iconActionAdd.gif</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>none</xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>


        <xsl:variable name="align">
            <xsl:choose>
                <xsl:when test="$isRenderPdf='true'">
                    <xsl:text>center</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text></xsl:text>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>

        <xsl:if test="($displayMode = 'Both' or ($displayMode = $sbMode)) and not($currentColumn/settings/setting[@name='Printer Friendly'] = 'false')">
            <xsl:if test="$settingRowNumber = $rowNumber">


                <td class="{$class}" align="{$align}">

                    <xsl:if test="not($preserveSpacesCss = '')">
                        <xsl:attribute name="class">
                            <xsl:value-of select="concat($class, ' ', $preserveSpacesCss)"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="/mxRoot/requestMap/setting[@name='HasMergedCell']/value = 'true' and not($rg)">
                        <xsl:if test="number($currentColumn/settings/setting[@name='Row Span']) &gt; 1">
                            <xsl:attribute name="rowspan">
                                <xsl:value-of select="$currentColumn/settings/setting[@name='Row Span']"/>
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:if test="number(($currentColumn/settings/setting[@name='Column Span'] + 1) div 2) &gt; 1">
                            <xsl:attribute name="colspan">
                                <xsl:value-of
                                        select="($currentColumn/settings/setting[@name='Column Span'] + 1) div 2"/>
                            </xsl:attribute>
                        </xsl:if>
                    </xsl:if>

                    <xsl:if test="$matchresult='left' and $cellIndex = 1">
                        <xsl:attribute name="class">mx_unique-row
                            <xsl:value-of select="$preserveSpacesCss"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$matchresult='right' and $cellIndex = $split+1">
                        <xsl:attribute name="class">mx_unique-row
                            <xsl:value-of select="$preserveSpacesCss"/>
                        </xsl:attribute>
                    </xsl:if>
                    <xsl:if test="$compareResult='different'">
                        <xsl:attribute name="class">mx_common-attribute-change
                            <xsl:value-of select="$preserveSpacesCss"/>
                        </xsl:attribute>
                    </xsl:if>

                    <xsl:if test="$cellIndex = '1' and not(@i) and not($istatus = 'none')">
                        <img src="{$istatus}"/>
                    </xsl:if>

                    <xsl:choose>
                        <xsl:when test="$Field_Type = 'checkbox'">
                            <!-- do nothing -->
                        </xsl:when>

                        <xsl:when test="@icon">
                            <table border="0">
                                <tr>
                                    <td>
                                        <img src="{@icon}" alt="*" width="16" height="16"/>
                                    </td>
                                    <td>
                                        <span class="object">
                                            <xsl:value-of select="."/>
                                        </span>
                                    </td>
                                </tr>
                            </table>
                        </xsl:when>
                        <xsl:when test="@i and not($rg)">

                            <table border="0">
                                <tr>
                                    <td>
                                        <img src="{@i}"/>
                                    </td>

                                    <xsl:if test="$cellIndex = '1' and not($istatus = 'none')">
                                        <td>
                                            <img src="{$istatus}"/>
                                        </td>
                                    </xsl:if>

                                    <td class="{$tdclass}">
                                        <xsl:if test="not($preserveSpacesCss = '')">
                                            <xsl:attribute name="class">
                                                <xsl:value-of select="concat($tdclass, ' ', $preserveSpacesCss)"/>
                                            </xsl:attribute>
                                        </xsl:if>
                                        <xsl:choose>
                                            <xsl:when test="@FPRootCol = 'true'">
                                                <xsl:choose>
                                                    <xsl:when test="$currentColumn/setting[@name = 'Root Label']">
                                                        <xsl:value-of
                                                                select="/mxRoot/tableControlMap/setting[@name='ObjectInfo']/items/item[@name='name']/value"/>
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="."/>
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="."/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </td>
                                </tr>
                            </table>

                        </xsl:when>
                        <xsl:when test="contains(text(),'nbsp')">&#160;</xsl:when>
                        <xsl:when test="$isRTE = 'true'">
                            <span>
                                <xsl:choose>
                                    <xsl:when test="./mxLink">
                                        <xsl:for-each select="./mxLink/node()">
                                            <xsl:copy-of select="."/>
                                        </xsl:for-each>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:for-each select="./node()">
                                            <xsl:copy-of select="."/>
                                        </xsl:for-each>
                                    </xsl:otherwise>
                                </xsl:choose>
                                &#160;
                            </span>
                        </xsl:when>
                        <xsl:when test="$currentColumn/@type='programHTMLOutput' or $iFH = 'true' ">
                            <xsl:choose>
                                <xsl:when test="./node()">
                                    <xsl:for-each select="./node()">
                                        <span><xsl:copy-of select="."/>&#160;
                                        </span>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:copy-of select="."/>&#160;
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:when>
                        <xsl:when test="./mxLink">
                            <xsl:for-each select="./mxLink/node()">
                                <xsl:value-of select="."/>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:when test="text() !=''">
                            <xsl:choose>
                                <xsl:when test="$currentColumn/settings/setting[@name='isMultiVal'] = 'true'">
                                    <table class="multi-attr">
                                        <xsl:variable name="strData" select="."/>
                                        <xsl:call-template name="splitByDelimiter">
                                            <xsl:with-param name="str" select="$strData"/>
                                        </xsl:call-template>
                                    </table>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="."/>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:if test="$rg">
                                <span>&#160;(<xsl:value-of select="$count"/>)
                                </span>
                            </xsl:if>
                        </xsl:when>
                        <xsl:when test="./node()">&#160;
                            <xsl:for-each select="./node()">
                                <xsl:copy-of select="."/>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>&#160;</xsl:otherwise>
                    </xsl:choose>

                    <xsl:if test="@edited ='true'">
                        <xsl:choose>
                            <xsl:when test="@d">
                                &#160;<span class="original-value" style="text-decoration: line-through; color: #F30;">
                                <xsl:value-of select="@d"/>
                            </span>
                            </xsl:when>
                            <xsl:otherwise>
                                &#160;<span class="original-value" style="text-decoration: line-through; color: #F30;">
                                <xsl:value-of select="@a"/>
                            </span>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>

                </td>
                <xsl:if test="position() = $split and $compare = 'TRUE'">
                    <td class="separator" width="10">&#160;</td>
                </xsl:if>
            </xsl:if>
            <xsl:if test="$rowNumber = '2' and $currentColumn/settings/setting[@name='BlankCell'] = 'true'">
                <td class="listCell">&#160;</td>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <!-- To diplay Mutli-value attribute in SB Printer Friendly -->
    <xsl:template name="splitByDelimiter">
        <xsl:param name="str"/>
        <xsl:choose>
            <xsl:when test="contains($str,'0x08')">
                <tr>
                    <td>
                        <xsl:value-of select="substring-before($str,'0x08')"/>
                    </td>
                </tr>
                <xsl:call-template name="splitByDelimiter">
                    <xsl:with-param name="str" select="substring-after($str,'0x08')"/>
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <tr>
                    <td>
                        <xsl:value-of select="$str"/>
                    </td>
                </tr>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

</xsl:stylesheet>
