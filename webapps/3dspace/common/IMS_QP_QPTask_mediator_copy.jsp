<script>

    // main function
    getChecked();

    function getChecked() {
        console.log(window.opener.document.location.href);
        console.log(window.document.location.href);


        let checkBoxes = window.opener.document.getElementsByTagName('input');

        let selectedCheckedBoxes = [];

        for (let i = 0; i < checkBoxes.length; i++) {
            if (checkBoxes[i].checked && checkBoxes[i].name === 'emxTableRowIdActual') {
                console.log(checkBoxes[i].name + " " + checkBoxes[i].type);
                console.log("checked: " + checkBoxes[i].value);
                selectedCheckedBoxes.push('\"' + checkBoxes[i].value + '\"');
            }
        }

        let oldRequest = window.location.href;
        oldRequest = oldRequest.replace('IMS_QP_QPTask_mediator_copy.jsp', 'IMS_QP_QPTask_copy.jsp');

        let xmlHttpRequest = function () {
            return new Promise(function (resolve, reject) {
                    var xhr = new XMLHttpRequest();
                    xhr.open('GET', oldRequest + '&emxTableRowId=' + selectedCheckedBoxes, true);
                    xhr.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded; charset=UTF-8');

                    xhr.onreadystatechange = function () {
                        if (this.readyState === 4) {
                            var text_out = document.getElementById('text_out');
                            var ok_btn = document.getElementById('ok_btn');

                            try {
                                if (xhr.status === 200) {
                                    window.opener.document.location.reload();
                                    // alert('Object copied successfully');
                                    text_out.innerHTML = 'Ready!';
                                    window.close();
                                } else {
                                    text_out.innerHTML = 'response_server_status: ' + xhr.status;
                                    // alert('Error when copying. Status: ' + xhr.status);
                                    reject(new Error('Error'));
                                }
                                ok_btn.hidden = false;
                            } catch (e) {
                                console.log('error');

                            }
                        }
                    }
                    xhr.send();
                }
            );
        };

        xmlHttpRequest().then(function () {
            console.log('text');
        }).catch(function (err) {
            console.error(err);
        });
    }

</script>
<body>
<style>

    a.button {
        font-weight: 700;
        color: white;
        text-decoration: none;
        padding: .8em 5em calc(.8em + 3px);
        border-radius: 30px;
        background: rgb(73, 152, 199);
        box-shadow: 0 -3px rgb(81, 134, 199) inset;
        transition: 0.2s;
    }

    a.button:hover {
        background: rgb(75, 123, 167);
    }

    a.button:active {
        background: rgb(75, 123, 167);
        box-shadow: 0 3px rgb(49, 73, 129) inset;
    }

    .header {
        min-width: 640px;
        height: 44px;
        color: #d54c98;
        font-weight: bold;
        font-size: 24px;
        border: none;
    }

    body {
        margin-top: 60px;
        font-family: Arial;
        font-size: 16px;
        color: #243b77;
    }

    p {
        margin-top: 60px;
        font-size: 16px;
    }
</style>
<div style="text-align: center">
    <div id="text_out" align="center" class="header">
        Loading ...
    </div>
    <div>
        <p>
            <a id="ok_btn" class="button" onclick="window.close();" hidden>Ok</a>
        <p>
    </div>
</div>
</body>
