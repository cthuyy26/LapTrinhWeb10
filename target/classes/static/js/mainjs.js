$(document).ready(function () {
    // Hiển thị thông tin người dùng đăng nhập thành công khi vào trang profile
    if (window.location.pathname.includes('/user/profile')) {
        if (!localStorage.token) {
            alert("Bạn chưa đăng nhập! Vui lòng đăng nhập trước.");
            window.location.href = "/login";
            return;
        }

        if (document.getElementById('tokenDisplay')) {
            $('#tokenDisplay').text(localStorage.token);
        }

        $.ajax({
            type: 'GET',
            url: '/users/me',
            dataType: 'json',
            contentType: 'application/json; charset=utf-8',
            beforeSend: function (xhr) {
                if (localStorage.token) {
                    xhr.setRequestHeader('Authorization', 'Bearer ' + localStorage.token);
                }
            },
            success: function (data) {
                var json = JSON.stringify(data, null, 4);
                $('#profile').html(data.fullName);
                if (data.images) {
                    var imgEl = document.getElementById("images");
                    if (imgEl) {
                        imgEl.src = data.images;
                    }
                }
                if (document.getElementById('emailInfo')) {
                    $('#emailInfo').html(data.email);
                }
                console.log("SUCCESS : ", data);
            },
            error: function (e) {
                var json = e.responseText;
                if ($('#feedback').length) {
                    $('#feedback').html(json);
                }
                console.log("ERROR : ", e);
                alert("Sorry, you are not logged in or token is invalid/expired.");
                localStorage.clear();
                window.location.href = "/login";
            }
        });
    }

    // Hàm đăng xuất
    $('#logout').click(function () {
        localStorage.clear();
        window.location.href = "/login";
    });

    // Hàm Login
    $('#Login').click(function () {
        var email = document.getElementById('email').value.trim();
        var password = document.getElementById('password').value.trim();

        if (!email || !password) {
            showAlert("Vui lòng nhập đầy đủ Email và Mật khẩu!", "danger");
            return;
        }

        var basicInfo = JSON.stringify({
            email: email,
            password: password
        });

        $.ajax({
            type: "POST",
            url: "/auth/login",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: basicInfo,
            success: function (data) {
                localStorage.token = data.token;
                console.log("Got token from Nimbus JWT:", data.token);
                window.location.href = "/user/profile";
            },
            error: function (xhr) {
                var msg = "Login Failed";
                try {
                    var res = JSON.parse(xhr.responseText);
                    if (res.detail) msg = res.detail;
                    if (res.description) msg += " (" + res.description + ")";
                } catch (e) {}
                showAlert(msg, "danger");
            }
        });
    });

    // Hàm Đăng ký tài khoản (hỗ trợ tiện lợi khi test)
    $('#RegisterBtn').click(function () {
        var fullName = $('#reg_fullName').val().trim();
        var email = $('#reg_email').val().trim();
        var password = $('#reg_password').val().trim();

        if (!fullName || !email || !password) {
            showAlert("Vui lòng điền đủ thông tin đăng ký!", "warning");
            return;
        }

        var regData = JSON.stringify({
            fullName: fullName,
            email: email,
            password: password
        });

        $.ajax({
            type: "POST",
            url: "/auth/signup",
            dataType: 'json',
            contentType: "application/json; charset=utf-8",
            data: regData,
            success: function (data) {
                showAlert("Đăng ký thành công! Hãy chuyển sang tab Đăng nhập.", "success");
                $('#email').val(email);
                $('#password').val(password);
                var loginTab = new bootstrap.Tab(document.querySelector('#login-tab'));
                loginTab.show();
            },
            error: function (xhr) {
                showAlert("Đăng ký thất bại! Email có thể đã tồn tại.", "danger");
            }
        });
    });

    function showAlert(message, type) {
        var alertEl = $('#authAlert');
        if (alertEl.length) {
            alertEl.removeClass('d-none alert-success alert-danger alert-warning')
                   .addClass('alert-' + type)
                   .text(message);
        } else {
            alert(message);
        }
    }
});
