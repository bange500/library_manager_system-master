layui.use(['form', 'element', 'layer'], function () {
    let form = layui.form;
    let element = layui.element;
    let layer = layui.layer;

    form.verify({
        pwd: [/^[\S]{4,24}$/, '密码长度4-24位，不能出现空格'],
        confirmPwd: function (value) {
            if ($('#newPwd').val() !== value) {
                return '两次输入的密码不一致';
            }
        }
    });

    form.on('submit(updatePwd)', function (data) {
        updatePassword();
        return false;
    });
});

function updatePassword() {
    let oldPwd = $('#oldPwd').val();
    let newPwd = $('#newPwd').val();

    $.ajax({
        async: false,
        type: 'post',
        url: '/updateAdminPwd',
        data: {
            oldPwd: oldPwd,
            newPwd: newPwd
        },
        success: function (data) {
            if (data.code === 0) {
                layer.alert('密码修改成功', {icon: 1}, function () {
                    location.reload();
                });
            } else {
                layer.alert(data.msg, {icon: 2});
            }
        },
        error: function () {
            layer.alert("密码修改失败");
        }
    });
}