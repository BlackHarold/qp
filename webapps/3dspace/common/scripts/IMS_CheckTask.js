function IMS_CheckTask(program, programFunction, fromId, toId, relationship, linkId, spinnerId, rowId) {
    console.log("IMS_CheckTask function");

    $("#" + linkId).css("display", "none");
    $("#" + spinnerId).css("display", "");
    console.log("program: " + program + " function: " + programFunction);
    $.get("IMS_CheckTask.jsp?program=" + program + "&function=" + programFunction + "&fromId=" + fromId + "&toId=" + toId + "&relationship=" + relationship, function (data, status) {
        $("#" + spinnerId).css("display", "none");
        $("#" + linkId).css("display", "");

        emxEditableTable.refreshRowByRowId(rowId);

    });
}
