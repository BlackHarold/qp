var addFileHTML = $("<tr id=\"calc_AddFiles\">" +
    "    <td valign=\"middle\" width=\"150\" class=\"createLabel\">" +
    "<label for=\"AddFiles\">Add Files</label>" +
    "</td>" +
    "<td valign=\"middle\" class=\"createInputField\" colspan=\"0\">" +
    "    <input value=\"\" id=\"fileURL\" name=\"AddFilesDisplay\" multiple type=\"file\" fieldLabel=\"AddFiles\" title=\"AddFiles\" size=\"20\" >" +
    "    <input id=\"fileOutput\" type=\"hidden\"></td>" +
    "</tr>"
);

addFileHTML.insertAfter("#calc_Comment");
(function () {
    var files,
        file,
        extension,
        input = document.getElementById("fileURL"),
        output = document.getElementById("fileOutput");
    input.addEventListener("change", function (e) {
        files = e.target.files;
        output.innerHTML = "";
        for (var i = 0, len = files.length; i < len; i++) {
            file = files[i];
            extension = file.name.split(".").pop();
            output.value += "|" + file.name;
        }
    }, false);
})();
