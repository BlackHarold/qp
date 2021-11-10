<%@ page import="matrix.db.Context" %>
<%@ page import="com.matrixone.servlet.Framework" %>
<%@ page contentType="text/html; charset=UTF-8" %>

<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <title>AJAX запрос в JSP</title>
    <style>
        .text-field__group {
            display: flex;
        }
        /* кнопка */
        .text-field__btn {
            display: inline-block;
            font-weight: 400;
            line-height: 1.5;
            color: #032342;
            text-align: center;
            vertical-align: middle;
            cursor: pointer;
            -webkit-user-select: none;
            -moz-user-select: none;
            user-select: none;
            background-color: #eee;
            border: 1px solid #bdbdbd;
            padding: .375rem .75rem;
            font-size: 1rem;
            border-radius: .25rem;
            transition: background-color .15s ease-in-out;
        }
        .text-field__btn:hover {
            background-color: #bdbdbd;
        }
        .text-field__group .text-field__input {
            border-top-right-radius: 0;
            border-bottom-right-radius: 0;
            position: relative;
            z-index: 2;
        }
        .text-field__group .text-field__btn {
            position: relative;
            border-top-left-radius: 0;
            border-bottom-left-radius: 0;
            border-left-width: 0;
        }
    </style>
    <script type="text/javascript">
        let xhr = new XMLHttpRequest();

        function loadAjax() {
            var objectId = document.getElementById("objectId")

            // 64032.5386.62200.61271
            let url = "IMS_Ajax.jsp?objectId=" + objectId.value;
            <%--alert(url + ': ' + <%=ctx.getUser()%>);--%>

            try {
                xhr.timeout = 10000;
                xhr.onreadystatechange = sendInfo;
                xhr.open(/*method*/ "GET",/*URL*/ url, true);
                // xhr.open(/*method*/ "POST",/*URL*/ url,/*async*/ true);
                xhr.send();

            } catch (e) {
                alert("Unable to connect server");
            }
        }

        function sendInfo() {
            let p = document.getElementById("print");
            let text;
            if (xhr.readyState === 1) {
                text = xhr.responseText;
                p.innerHTML = "Please Wait.....";
                console.log("1");
            }

            if (xhr.readyState == 2) {
                text = xhr.responseText;
                console.log("2");

            }
            if (xhr.readyState == 3) {
                text = xhr.responseText;
                console.log("3");

            }
            if (xhr.readyState == 4) {
                text = xhr.responseText;
                p.innerHTML = " " + text;
            }
        }
    </script>
</head>
<body>
<h1 class="title">Поиск по ID объекта или Пользователя по ИМЕНИ</h1>
<form>
    <div class="text-field">
        <div class="text-field__group">
            <input class="text-field__input" type="search" id="objectId" name="search" required="true">
            <button class="text-field__btn" type="button" onclick="loadAjax()">Найти</button>
        </div>
    </div>

    <%--        <label>Object ID</label>--%>
    <%--        <input type="text" name="object-info" id="objectId" required="required" placeholder="64032.5386.62200.61271"/>--%>
    <%--        <sub>64032.5386.62200.61271</sub>--%>
    <%--        <button type="button" onclick="loadAjax()">Do It!</button>--%>
    <%--    </div>--%>
</form>
<p id="print"></p>
</body>
</html>
