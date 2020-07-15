<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<link href="styles/emxUIExtendedHeader.css" rel="stylesheet"></link>

<style>
    .gutter {
        background-color: #eee;
        background-repeat: no-repeat;
        background-position: 50%;
    }

    .gutter.gutter-horizontal {
        background-image: url('images/grip_vertical.png');
        cursor: ew-resize;
    }

    .split.split-horizontal, .gutter.gutter-horizontal {
        height: 100%;
        float: left;
    }
</style>

<script src="scripts/jquery-latest.js"></script>

<div style="height: 100%">

    <%--    <div id="leftDiv" style="float: left;">--%>
    <%--        <iframe id="leftFrame" name="leftFrame" src="emxForm.jsp?form=type_IMS_QP_DEP&toolbar=IMS_QP_DEP_Toolbar&formHeader='Quality DEP'&submitAction=refreshCaller&objectId=<%=request.getParameter("objectId")%>" frameborder="0" style="width: 100%; height: 100%;)"></iframe>--%>
    <%--    </div>--%>
    <div id="leftDiv" style="float: left;">
        <iframe id="leftFrame" name="leftFrame"
                src="emxPortalDisplay.jsp?portal=IMS_QP_DEPLeftPortal&objectId=<%=request.getParameter("objectId")%>"
                frameborder="0" style="width: 100%; height:100%;"></iframe>
    </div>

    <div id="rightDiv" style="float: right;">
        <iframe id="rightFrame"
                src="emxPortalDisplay.jsp?portal=IMS_QP_DEPRightPortal&objectId=<%=request.getParameter("objectId")%>"
                frameborder="0" style="width: 100%; height:100%;"></iframe>
    </div>
</div>

<script src="scripts/split.js"></script>
<script>
    Split(['#leftDiv', '#rightDiv'], {
        sizes: [65, 35],
        minSize: 200
    });
</script>
