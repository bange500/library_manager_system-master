layui.use(['form', 'element', 'layer'], function () {
    let form = layui.form;
    let element = layui.element;
    let layer = layui.layer;
});

$('#modify-info').on('click', function () {
    layer.open({
        type: 1,
        skin: 'layui-layer-molv',
        area: ['380px', '220px'],
        title: ['修改资料', 'font-size:18px'],
        btn: ['保存', '取消'],
        shadeClose: true,
        shade: 0,
        content: $("#window"),
        yes: function () {
            updateAdminInfo();
        }
    });
});

function updateAdminInfo() {
    $.ajax({
        async: false,
        type: 'post',
        url: '/updateAdmin',
        data: $('#updateAdminForm').serialize(),
        success: function (data) {
            layer.alert('修改成功', {icon: 1}, function () {
                location.reload();
            });
        },
        error: function (data) {
            layer.alert("修改失败");
        }
    });
}