function IMS_KDD_connect(connectProgram, connectFunction, fromId, toId, relationship, linkId, spinnerId, onConnected) {
    $("#" + linkId).css("display", "none");
    $("#" + spinnerId).css("display", "");
    $.get("IMS_KDD_Connect.jsp?program=" + connectProgram + "&function=" + connectFunction + "&fromId=" + fromId + "&toId=" + toId + "&relationship=" + relationship, function (data, status) {
        $("#" + spinnerId).css("display", "none");
        $("#" + linkId).css("display", "");
        var message = data.trim();
        if (message !== "") {
            alert(data);
        } else if (onConnected) {
            onConnected();
        }
    });
}

function IMS_KDD_disconnect(disconnectProgram, disconnectFunction, fromId, toId, relationship, linkId, spinnerId, onDisconnected) {
    $("#" + linkId).css("display", "none");
    $("#" + spinnerId).css("display", "");
    $.get("IMS_KDD_Disconnect.jsp?program=" + disconnectProgram + "&function=" + disconnectFunction + "&fromId=" + fromId + "&toId=" + toId + "&relationship=" + relationship, function (data, status) {
        $("#" + spinnerId).css("display", "none");
        $("#" + linkId).css("display", "");
        var message = data.trim();
        if (message !== "") {
            alert(data);
        } else if (onDisconnected) {
            onDisconnected();
        }
    });
}

function disconnect(disconnectProgram, disconnectFunction, fromId, toId, relationship, linkId, spinnerId, onDisconnected) {
    console.log("disconnect js");
    $("#" + linkId).css("display", "none");
    $("#" + spinnerId).css("display", "");
    $.get("IMS_QP_Disconnect.jsp?program=" + disconnectProgram + "&function=" + disconnectFunction + "&fromId=" + fromId + "&toId=" + toId + "&relationship=" + relationship, function (data, status) {
        $("#" + spinnerId).css("display", "none");
        $("#" + linkId).css("display", "");
        var message = data.trim();
        if (message !== "") {
            alert(data);
        } else if (onDisconnected) {
            onDisconnected();
        }
    });
}

function getViewerElement(editId, objectId) {
    return document.getElementById("edit-" + editId + "-viewer-" + objectId);
}

function getEditorElementId(editId, objectId) {
    return "edit-" + editId + "-editor-" + objectId;
}

function getEditorElement(editId, objectId) {
    return document.getElementById(getEditorElementId(editId, objectId));
}

function getEditLinkId(editId, objectId) {
    return "edit-" + editId + "-link-" + objectId;
}

function getEditLink(editId, objectId) {
    return document.getElementById(getEditLinkId(editId, objectId));
}

function getEditCancelLinkId(editId, objectId) {
    return "edit-" + editId + "-cancel-" + objectId;
}

function getEditCancelLink(editId, objectId) {
    return document.getElementById(getEditCancelLinkId(editId, objectId));
}

function getAddLinkId(connectId, rowId) {
    return "add-" + connectId + "-" + rowId;
}

function getEditFunctionCall(name, editId, objectId, attributeName, rowId, setAttributeProgram, setAttributeFunction) {
    return name + "('" + editId + "', '" + objectId + "', '" + attributeName + "', '" + rowId + "', '" + setAttributeProgram + "', '" + setAttributeFunction + "')";
}

function getEditCancelFunctionCall(editId, objectId, attributeName, rowId, setAttributeProgram, setAttributeFunction, editImageUrl) {
    return "IMS_KDD_editCancel('" + editId + "', '" + objectId + "', '" + attributeName + "', '" + rowId + "', '" + setAttributeProgram + "', '" + setAttributeFunction + "', '" + editImageUrl + "')";
}

function IMS_KDD_editStart(editId, objectId, attributeName, rowId, setAttributeProgram, setAttributeFunction) {
    var viewElement = getViewerElement(editId, objectId);
    viewElement.style.display = "none";

    var textArea = document.createElement("textarea");
    textArea.id = getEditorElementId(editId, objectId);
    textArea.style.width = "100%";
    textArea.rows = 20;
    textArea.value = viewElement.innerText;

    var link = getEditLink(editId, objectId);
    var editImageUrl = link.firstChild.src;
    link.firstChild.src = "images/fugue/16x16/tick.png";
    link.href = "javascript:" + getEditFunctionCall("IMS_KDD_editSave", editId, objectId, attributeName, rowId, setAttributeProgram, setAttributeFunction);
    viewElement.parentElement.insertBefore(textArea, viewElement.nextSibling);

    var cancelLink = document.createElement("a");
    cancelLink.id = getEditCancelLinkId(editId, objectId);
    cancelLink.href = "javascript:" + getEditCancelFunctionCall(editId, objectId, attributeName, rowId, setAttributeProgram, setAttributeFunction, editImageUrl);
    var cancelImage = document.createElement("img");
    cancelImage.src = "images/fugue/16x16/cross.png";
    cancelImage.title = "Отменить";
    cancelLink.appendChild(cancelImage);
    link.parentElement.insertBefore(cancelLink, link.nextSibling);

    textArea.focus();
}

function IMS_KDD_editSave(editId, objectId, attributeName, rowId, setAttributeProgram, setAttributeFunction) {
    getEditCancelLink(editId, objectId).remove();

    var link = getEditLink(editId, objectId);
    link.href = "";
    link.firstChild.src = "images/spinner_16x16.png";

    var editor = getEditorElement(editId, objectId);

    $.post(
        "IMS_KDD_SetAttributeValue.jsp",
        {
            program: setAttributeProgram,
            function: setAttributeFunction,
            objectId: objectId,
            attributeName: attributeName,
            attributeValue: editor.value
        },
        function (data, status) {
            var message = data.trim();
            if (message !== "") {
                alert(data);
            }
            emxEditableTable.refreshRowByRowId(rowId);
        });
}

function IMS_KDD_editCancel(editId, objectId, attributeName, rowId, setAttributeProgram, setAttributeFunction, editImageUrl) {
    var cancelLink = getEditCancelLink(editId, objectId);
    if (cancelLink) {
        cancelLink.remove();
    }

    var editor = getEditorElement(editId, objectId);
    if (editor) {
        editor.remove();
    }

    var viewer = getViewerElement(editId, objectId);
    if (viewer) {
        viewer.style.display = "";
    }

    var link = getEditLink(editId, objectId);
    if (link) {
        link.firstChild.src = editImageUrl;
        link.href = "javascript:" + getEditFunctionCall("IMS_KDD_editStart", editId, objectId, attributeName, rowId, setAttributeProgram, setAttributeFunction);
    }
}

function IMS_KDD_getSearchScript(connectProgram, connectFunction, types, table, tableRelationship, tableRelationshipDirection, selection, connectionRelationship, isTo, fromId, fromIsRel, connectId, rowIdToRefresh, includeOIDProgram) {

    var url = "emxFullSearch.jsp" +
        "?field=TYPES=" + types +
        "&table=" + table +
        (tableRelationship ? "&relationship=" + tableRelationship : "") +
        (tableRelationshipDirection ? "&direction=" + tableRelationshipDirection : "") +
        "&selection=" + selection +
        "&submitAction=doNothing" +
        "&srcDestRelName=" + connectionRelationship +
        "&isTo=" + isTo +
        (!fromIsRel ? "&objectId=" + fromId : "") +
        "&cancelButton=true" +
        "&cancelLabel=Cancel" +
        "&submitLabel=Select" +
        "&submitURL=" + encodeURIComponent(
            "IMS_KDD_Connect.jsp" +
            "?program=" + connectProgram +
            "&function=" + connectFunction +
            "&fromId=" + fromId +
            "&relationship=" + connectionRelationship +
            "&connectId=" + connectId +
            "&rowIdToRefresh=" + rowIdToRefresh);

    if (includeOIDProgram) {
        url = url + "&includeOIDprogram=" + includeOIDProgram;
    }

    window.open(url, "", "width=500,height=400");
}

function IMS_KDD_closeSearchWindowAndRefreshRow(connectId, rowId) {
    var addLink = parent.parent.opener.document.getElementById(getAddLinkId(connectId, rowId));
    if (addLink) {
        addLink.href = "";
        addLink.firstChild.src = "images/spinner_16x16.png";
    }

    parent.parent.opener.emxEditableTable.refreshRowByRowId(rowId);
    parent.parent.window.close();
}

function showPopup(url, title, win, w, h) {
    var y = win.top.outerHeight / 2 + win.top.screenY - (h / 2);
    var x = win.top.outerWidth / 2 + win.top.screenX - (w / 2);
    return win.open(url, title, 'width=' + w + ', height=' + h + ', top=' + y + ', left=' + x);
}

function IMS_KDD_getTableScript(tableProgram, tableFunction, connectProgram, connectFunction, table, selection, sortColumnName, connectionRelationship, objectId, connectId, rowIdToRefresh) {

    var url = "emxIndentedTable.jsp" +
        "?program=" + tableProgram + "%3A" + tableFunction +
        "&table=" + table +
        "&selection=" + selection +
        "&sortColumnName=" + sortColumnName +
        "&sortDirection=ascending" +
        "&cancelButton=true" +
        "&cancelLabel=Cancel" +
        "&submitLabel=Select" +
        "&submitURL=" + encodeURIComponent(
            "IMS_KDD_Connect.jsp" +
            "?program=" + connectProgram +
            "&function=" + connectFunction +
            "&fromId=" + objectId +
            "&relationship=" + connectionRelationship +
            "&connectId=" + connectId +
            "&rowIdToRefresh=" + rowIdToRefresh);

    showPopup(url, "", window, 800, 600);
}

function IMS_KDD_ensure_IMS_KDD_checkedElementIds() {
    if (window.IMS_KDD_checkedElementIds === undefined) {
        window.IMS_KDD_checkedElementIds = [];
    }
}

function IMS_KDD_reset_IMS_KDD_checkedElementIds() {
    window.IMS_KDD_checkedElementIds = [];
}

function IMS_KDD_handleCheckboxClick(cb) {
    IMS_KDD_ensure_IMS_KDD_checkedElementIds();
    if (cb.checked) {
        if (IMS_KDD_checkedElementIds.indexOf(cb.id) === -1) {
            IMS_KDD_checkedElementIds.push(cb.id);
        }
    } else {
        var index = IMS_KDD_checkedElementIds.indexOf(cb.id);
        if (index > -1) {
            IMS_KDD_checkedElementIds.splice(index, 1);
        }
    }
    //alert(IMS_KDD_checkedElementIds);
}

function IMS_KDD_getCheckedElementsRowIdObjectIdPairsOfColumn(column) {
    IMS_KDD_ensure_IMS_KDD_checkedElementIds();
    var ids = [];
    if (column) {
        for (var i = 0; i < IMS_KDD_checkedElementIds.length; i++) {
            var parts = IMS_KDD_checkedElementIds[i].split("-");
            if (parts.length >= 4 && parts[1] == column) {
                ids.push(parts[2] + "-" + parts[3]);
            }
        }
    }
    return ids;
}

function IMS_KDD_getCheckedElementsRowIdsOfColumn(column) {
    IMS_KDD_ensure_IMS_KDD_checkedElementIds();
    var ids = [];
    if (column) {
        for (var i = 0; i < IMS_KDD_checkedElementIds.length; i++) {
            var parts = IMS_KDD_checkedElementIds[i].split("-");
            if (parts.length >= 4 && parts[1] == column && ids.indexOf(parts[2]) === -1) {
                ids.push(parts[2]);
            }
        }
    }
    return ids;
}

function IMS_KDD_getCheckedElementObjectIdsOfColumn(column) {
    IMS_KDD_ensure_IMS_KDD_checkedElementIds();
    var ids = [];
    if (column) {
        for (var i = 0; i < IMS_KDD_checkedElementIds.length; i++) {
            var parts = IMS_KDD_checkedElementIds[i].split("-");
            if (parts.length >= 4 && parts[1] == column && ids.indexOf(parts[3]) === -1) {
                ids.push(parts[3]);
            }
        }
    }
    return ids;
}

function IMS_KDD_removeCheckedElementIds(column, rowId) {
    IMS_KDD_ensure_IMS_KDD_checkedElementIds();
    if (column) {
        var i = 0;
        while (i < IMS_KDD_checkedElementIds.length) {
            var parts = IMS_KDD_checkedElementIds[i].split("-");
            if (parts.length >= 4 && parts[1] == column && parts[2] == rowId) {
                IMS_KDD_checkedElementIds.splice(i, 1);
            } else {
                i++;
            }
        }
    }
}

function IMS_KDD_initCheckBox(id) {
    IMS_KDD_ensure_IMS_KDD_checkedElementIds();
    document.getElementById(id).checked = IMS_KDD_checkedElementIds.indexOf(id) > -1;
}

function IMS_KDD_handleMutationList(mutationsList, observer) {
    mutationsList.forEach(function (mutation) {
        if (mutation.type == 'childList') {
            for (var i = 0; i < mutation.addedNodes.length; i++) {
                var node = mutation.addedNodes[i];
                if (typeof (node.querySelectorAll) !== "undefined") {
                    var inputs = node.querySelectorAll(".IMS_KDD_CB");
                    for (var j = 0; j < inputs.length; j++) {
                        if (inputs[j].id.startsWith("checkbox-")) {
                            IMS_KDD_initCheckBox(inputs[j].id);
                        }
                    }
                }
            }
        }
    });
}

function IMS_KDD_post(document, action, target, parameterMap) {
    var form = document.createElement("form");
    form.setAttribute("method", "post");
    form.setAttribute("action", action);
    form.setAttribute("target", target);

    parameterMap.forEach(function (value, name) {
        var input = document.createElement("input");
        input.setAttribute("type", "hidden");
        input.setAttribute("name", name);
        input.setAttribute("value", value);
        form.appendChild(input);
    });

    document.body.appendChild(form);
    form.submit();
    form.remove();
}
