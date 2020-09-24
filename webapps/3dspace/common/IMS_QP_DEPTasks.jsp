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

<div style="width: 100%; height: 100%">

    <div id="portal" style="width: 100%; height: 100%; float: left;">
        <iframe id="portalFrame" name="portalFrame" src="emxPortalDisplay.jsp?portal=IMS_QP_DEPTaskPortal&objectId=<%=request.getParameter("objectId")%>" width="100%" frameborder="0" style="height: calc(100% - 0px)"></iframe>
    </div>
</div>
