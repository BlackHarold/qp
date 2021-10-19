<%@ page import="java.net.URLEncoder" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<%@ page import="com.google.common.html.HtmlEscapers" %>
<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<link rel="stylesheet" type="text/css" href="../common/styles/emxUIDefault.css">

<script src="scripts/jquery-latest.js"></script>

<script>
    function search(event) {
        if (event === undefined || event.which === 13 || event.keyCode === 13) {
            $('#resultsFrame').attr('src', 'emxTable.jsp' +
                '?program=IMS_QP_DEP%3AfindSystemByName' +
                '&selection=multiple' +
                '&headerRepeat=0' +
                '&pagination=0' +
                '&disableSorting=true' +
                '&table=<%=URLEncoder.encode(request.getParameter("table"), "UTF-8")%>' +
                '&IMS_ExternalSystemQuery=' + encodeURIComponent($("#queryInput").val()) +
                '&objectId=<%=request.getParameter("objectId") != null ? URLEncoder.encode(request.getParameter("objectId"), "UTF-8") : ""%>');
            return false;
        }
        return true;
    }

    function select() {
        var checkboxesChecked = []; // можно в массиве их хранить, если нужно использовать
        let selectedExternalObjectId = $('#resultsFrame').contents()
            .find('iframe[name=\'listDisplay\']').contents()
            .find('input[name=\'emxTableRowId\']:checked')
            .each(function () {
                checkboxesChecked.push(this.value);
            });

        console.log('checkboxesChecked ' + checkboxesChecked);


        if (checkboxesChecked !== undefined) {

            $.post(
                "IMS_JPOInvoke.jsp",
                {
                    program: "IMS_QP_DEP",
                    function: "connectSystem",
                    args: [
                        "<%=request.getParameter("objectId")%>",
                        '['+checkboxesChecked+']'
                    ]
                },
                function (data, status) {
                    window.opener.location.reload(true);
                    window.close();
                });
        } else {
            alert("Please select an object.");
        }
    }
</script>

<style>
    #queryInput {
        background-image: url('images/searchicon.png') !important;
        background-position: 10px 12px;
        background-repeat: no-repeat;
        width: calc(100% - 77px);
        height: 50px;
        font-size: 16px;
        padding: 12px 20px 12px 40px;
        margin-right: 0;
        margin-top: 2px;
    }
</style>

<div style="width: 100%; display: inline-flex">
    <input id="queryInput" type="text" onkeypress="return search(event)" class="sn-search-field" autocomplete="off"
           placeholder="<%=HtmlEscapers.htmlEscaper().escape(StringUtils.isNotBlank(request.getParameter("IMS_SearchHint")) ? request.getParameter("IMS_SearchHint") : "Search")%>">
    <button class="btn-default" style="width: 75px; height: 50px; margin-left: 2px; margin-top: 2px;"
            onclick="search()">Search
    </button>
</div>
<div style="width: 100%; text-align: right">

</div>

<iframe
        id="resultsFrame"
        src="emxTable.jsp?program=IMS_QP_DEP%3AfindSystemByName&selection=multiple&headerRepeat=0&pagination=0&disableSorting=true&table=<%=URLEncoder.encode(request.getParameter("table"), "UTF-8")%>"
        width="100%" frameborder="0" style="height: calc(100% - 103px)">
</iframe>

<br/>
<br/>
<div style="width: 100%; text-align: right">
    <button class="btn-primary" style="width: 75px;" onclick="select()">Select</button>
    <button class="btn-default" style="width: 75px;" onclick="window.close()">Cancel</button>
</div>
