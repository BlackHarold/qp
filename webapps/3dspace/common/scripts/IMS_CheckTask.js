function IMS_CheckTask(program, programFunction, fromId, toId, relationship, linkId, spinnerId, rowId) {

    if (programFunction == "distributionArrow") {
        var url = "IMS_IndentedTable.jsp?selection=multiple&parentOID=" +
            toId + "&program=IMS_QP_Task:getAllRelatedTasksForDistributionButton&table=IMS_QP_DEPAllTasks&&objectId=" +
            fromId + "&submitLabel=Distribute&submitURL=IMS_DistributeTaskConnection.jsp";
        showPopup(url, "Distribute relationships", window, window.screen.width * 0.4, window.screen.height * 0.3);

    } else {
        $("#" + linkId).css("display", "none");
        $("#" + spinnerId).css("display", "");
        $.get("IMS_CheckTask.jsp?program=" + program + "&function=" + programFunction + "&fromId=" + fromId + "&toId=" + toId + "&relationship=" + relationship, function () {
            $("#" + spinnerId).css("display", "none");
            $("#" + linkId).css("display", "");

            emxEditableTable.refreshRowByRowId(rowId);

        });
    }
}
