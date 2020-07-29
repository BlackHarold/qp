function IMS_CheckTask(program, programFunction, fromId, toId, relationship, linkId, spinnerId, rowId) {

    if (programFunction == "distributionArrow") {
        console.log("distributionArrow pressed");
        var url = "IMS_IndentedTable.jsp?selection=multiple&parentOID=" + fromId + "&program=IMS_QP_Task:getAllRelatedTasksForDistributionButton&table=IMS_QP_DEPAllTasks&targetLocation=popup&objectId=" + fromId + "&submitLabel=Distribute&submitURL=''";
        console.log(window.screen.width*0.3);
        showPopup(url, "Distribute relationships", window, window.screen.width*0.4, window.screen.height*0.3);

    } else {
        console.log("IMS_CheckTask function");

        $("#" + linkId).css("display", "none");
        $("#" + spinnerId).css("display", "");
        console.log("program: " + program + " function: " + programFunction);
        $.get("IMS_CheckTask.jsp?program=" + program + "&function=" + programFunction + "&fromId=" + fromId + "&toId=" + toId + "&relationship=" + relationship, function () {
            $("#" + spinnerId).css("display", "none");
            $("#" + linkId).css("display", "");

            emxEditableTable.refreshRowByRowId(rowId);

        });
    }
}
