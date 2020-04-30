function getDropAreaId(id) {
    return "dropArea" + "-" + id;
}

function getSpinnerId(id) {
    return "spinner" + "-" + id;
}

function getDropArea(id) {
    return document.getElementById(getDropAreaId(id));
}

function getSpinner(id) {
    return document.getElementById(getSpinnerId(id));
}

function IMS_DragNDrop_setProgressMode(id) {
    var dropArea = getDropArea(id);
    if (dropArea) {
        dropArea.style.display = "none";
    }
    var spinner = getSpinner(id);
    if (spinner) {
        spinner.style.display = "table";
    }
}

function IMS_DragNDrop_setIdleMode(id) {
    var spinner = getSpinner(id);
    if (spinner) {
        spinner.style.display = "none";    
    }
    var dropArea = getDropArea(id);
    if (dropArea) {
        dropArea.style.display = "table";
        dropArea.className = "dropArea";
    }
}

function IMS_DragNDrop_setActiveMode(id) {
    var dropArea = getDropArea(id);
    if (dropArea) {
        dropArea.className = "dropTarget";
    }
}

function getCheckedRowAttributeNodes(attributeName) {
    return emxUICore.selectNodes(oXML, "/mxRoot/rows//r[((@level = '0' or count(ancestor::r[not(@display) or @display = 'none']) = '0') and @checked = 'checked' and not(@rg) and not(@calc))]/@" + attributeName);
}

function getCheckedRowIds() {
    return getCheckedRowAttributeNodes("id").map(function(attr) { return attr.value; });
}

function getCheckedRowObjectIds() {
    return getCheckedRowAttributeNodes("o").map(function(attr) { return attr.value; });
}

function getCheckedRowIdObjectIdPairs() {
    var checkedRowIds = getCheckedRowIds();
    var checkedRowObjectIds = getCheckedRowObjectIds();
    var pairs = [];
    for (var i = 0; i < checkedRowIds.length; i++) {
        pairs.push(checkedRowIds[i] + "-" + checkedRowObjectIds[i]);
    }
    return pairs;
}

function IMS_DragNDrop_connect(connectProgram, connectFunction, targetId, dropEvent, relationship, isFromTarget, rowId, onConnected, allowedSource, checkboxesColumn) {
    dropEvent.preventDefault();

    var sourceId = dropEvent.dataTransfer.getData("objectId");
    if (!sourceId) {
        var url = dropEvent.dataTransfer.getData("text/html");
        if (url) {
            var match = url.match("objectId=(\\d*\.\\d*\.\\d*\.\\d*)");
            if (match && match.length === 2) {
                sourceId = match[1];
            }
            else {
                var match = url.match("%22(\\d+\.\\d+\.\\d+\.\\d+)");
                if (match && match.length === 2) {
                    sourceId = match[1];
                }
            }
        }
        if (!sourceId) {
            sourceId = dropEvent.dataTransfer.getData("text/plain")
        }
    }

    var affectedRowIds;
    var checkedRowIdObjectIdPairs;
    
    if (dropEvent.shiftKey) {
        affectedRowIds = IMS_KDD_getCheckedElementsRowIdsOfColumn(checkboxesColumn);
        if (affectedRowIds.length > 0) { // checked elements mode
            checkedRowIdObjectIdPairs = IMS_KDD_getCheckedElementsRowIdObjectIdPairsOfColumn(checkboxesColumn);
            targetId = IMS_KDD_getCheckedElementObjectIdsOfColumn(checkboxesColumn).join(",");
        }
        else { // checked rows mode
            affectedRowIds = getCheckedRowIds();
            checkedRowIdObjectIdPairs = getCheckedRowIdObjectIdPairs();
            targetId = getCheckedRowObjectIds().join(",");
        }
    }
    
    var fromId = isFromTarget ? targetId : sourceId;
    var toId = isFromTarget ? sourceId : targetId;

    var source = dropEvent.dataTransfer.getData("source");
    var dragSource = dropEvent.dataTransfer.getData("dragSource");

    if (fromId && toId && (!allowedSource || allowedSource === source || source && allowedSource.indexOf(source) !== -1)) {
        if (dropEvent.shiftKey) {
            checkedRowIdObjectIdPairs.forEach(function (value) { IMS_DragNDrop_setProgressMode(relationship + "-" + value); });
        }
        else {
            IMS_DragNDrop_setProgressMode(relationship + "-" + rowId + "-" + targetId);
        }

        $.post(
            "IMS_KDD_Connect.jsp",
            {
                program: connectProgram,
                function: connectFunction,
                fromId: fromId,
                toId: toId,
                relationship: relationship,
                dragSource: dragSource
            },
            function (data, status) {
                if (dropEvent.shiftKey) {
                    checkedRowIdObjectIdPairs.forEach(function (value) { IMS_DragNDrop_setIdleMode(relationship + "-" + value); });
                }
                else {
                    IMS_DragNDrop_setIdleMode(relationship + "-" + rowId + "-" + targetId);
                }

                var message = data.trim();
                if (message !== "") {
                    alert(data);
                }
                else if (onConnected) {
                    IMS_KDD_reset_IMS_KDD_checkedElementIds();
                    if (dropEvent.shiftKey) {
                        emxEditableTable.refreshRowByRowId(affectedRowIds);                        
                    }
                    else {
                        onConnected();
                    }
                }
            });
    }
    else {
        if (dropEvent.shiftKey) {
            checkedRowIdObjectIdPairs.forEach(function (value) { IMS_DragNDrop_setIdleMode(relationship + "-" + value); });
        }
        else {
            IMS_DragNDrop_setIdleMode(relationship + "-" + rowId + "-" + targetId);
        }
    }
}

function IMS_DragNDrop_onDrag(event, column, rowId, targetId, checkboxesColumn) {
    event.stopPropagation();
    event.preventDefault();    
    var checkedRowIdObjectIdPairs;
    
    if (event.shiftKey) {
        checkedRowIdObjectIdPairs = IMS_KDD_getCheckedElementsRowIdObjectIdPairsOfColumn(checkboxesColumn);
        if (checkedRowIdObjectIdPairs.length == 0) { // use checked rows mode
            checkedRowIdObjectIdPairs = getCheckedRowIdObjectIdPairs();
        }
    }
    
    if (event.type === "dragover") {
        if (event.shiftKey) {
            if (checkedRowIdObjectIdPairs) {
                checkedRowIdObjectIdPairs.forEach(function (value) { IMS_DragNDrop_setActiveMode(column + "-" + value); });
            }
        }
        else {
            IMS_DragNDrop_setActiveMode(column + "-" + rowId + "-" + targetId);
        }
    }
    else if (event.type === "dragleave") {
        if (event.shiftKey) {
            if (checkedRowIdObjectIdPairs) {
                checkedRowIdObjectIdPairs.forEach(function (value) { IMS_DragNDrop_setIdleMode(column + "-" + value); });
            }
        }
        else {
            IMS_DragNDrop_setIdleMode(column + "-" + rowId + "-" + targetId);
        }
    }
}

function IMS_DragNDrop_onDragStart(event, objectId, parentId, isIndentedTable) {
    event.dataTransfer.setData("objectId", event.ctrlKey ?
        $("#" + parentId)
            .find("input")
            .filter(function() {
                return ($(this).context.name === (isIndentedTable ? "emxTableRowIdActual" : "emxTableRowId")) && $(this).context.checked;
            })
            .map(function() { return isIndentedTable ? $(this).context.value.match("\\|(\\d*\.\\d*\.\\d*\.\\d*)?\\|")[1] : $(this).context.value; })
            .get()
            .join(",") :
        objectId);
}

function IMS_DragNDrop_getCheckedValueList(parentId, inputName) {
    return $("#" + parentId)
        .find("input")
        .filter(function() { return ($(this).context.name === inputName) && $(this).context.checked; })
        .map(function() { return $(this).context.value; })
        .get()
        .join(",");
}