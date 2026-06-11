/**
 * 录入新书页面 JS
 * 新增：ISBN唯一性校验、出版日期选择器、总库存校验
 */
layui.use(['element', 'form', 'layer', 'laydate'], function () {
    let layer = layui.layer;
    let element = layui.element;
    let form = layui.form;
    let laydate = layui.laydate;

    // 出版日期选择器
    laydate.render({
        elem: '#publishDate',
        format: 'yyyy-MM-dd',
        value: ''
    });

    // 自定义校验规则
    form.verify({
        // ISBN 格式校验
        isbn: function (value) {
            if (!value) return 'ISBN不能为空';
            // ISBN支持10位或13位数字/连字符格式
            var isbnPattern = /^[0-9\-]{10,17}$/;
            if (!isbnPattern.test(value.trim())) {
                return 'ISBN格式不正确（10-17位数字或连字符）';
            }
        },
        // 库存校验
        stock: function (value) {
            if (!value) return '库存不能为空';
            var num = parseInt(value, 10);
            if (isNaN(num) || num < 0) {
                return '库存必须为非负整数';
            }
            if (value.indexOf('.') >= 0) {
                return '库存必须为整数';
            }
        }
    });

    // 表单提交
    form.on('submit(btn1)', function (data) {
        // 先做前端 ISBN 唯一性校验
        var isbn = $('#isbn').val().trim();
        if (!isbn) {
            layer.msg('请输入ISBN', {icon: 2});
            return false;
        }

        // 异步校验ISBN是否已存在
        $.ajax({
            type: 'GET',
            url: '/checkIsbnUnique',
            data: {isbn: isbn},
            async: false,
            success: function (resp) {
                if (resp === false || resp === 'false') {
                    // 注意：resp可能是字符串"true"/"false"或布尔值
                    layer.msg('ISBN已存在，请更换', {icon: 2});
                    isbnCheckPassed = false;
                } else {
                    isbnCheckPassed = true;
                }
            },
            error: function () {
                isbnCheckPassed = true; // 网络错误时放行，由后端兜底校验
            }
        });

        if (!isbnCheckPassed) return false;

        // 库存校验
        var totalStock = parseInt($('#totalStock').val(), 10);
        if (isNaN(totalStock) || totalStock < 0) {
            layer.msg('库存必须为非负整数', {icon: 2});
            return false;
        }

        addBook();
        return false;
    });

    // 给ISBN输入框添加失焦校验
    $('#isbn').on('blur', function () {
        var val = $(this).val().trim();
        if (!val) return;
        $.ajax({
            type: 'GET',
            url: '/checkIsbnUnique',
            data: {isbn: val},
            success: function (resp) {
                if (resp === false || resp === 'false') {
                    $(this).addClass('layui-form-danger');
                    layer.tips('此ISBN已存在', '#isbn', {tips: [1, '#FF5722'], time: 2000});
                }
            }.bind(this)
        });
    });
});

var isbnCheckPassed = true;

function addBook() {
    $.ajax({
        async: false,
        type: 'post',
        url: '/addBook',
        data: $('#addBookForm').serialize(),
        success: function (data) {
            if (data === 'true') {
                layer.msg("添加成功", {time: 1500, icon: 1});
                setTimeout(function () {
                    location.reload();
                }, 1500);
            } else {
                // 后端返回了错误信息（如ISBN重复）
                layer.msg(data, {time: 3000, icon: 2});
            }
        },
        error: function (data) {
            layer.msg("添加失败，服务器异常", {icon: 2});
        }
    });
}

$(document).ready(function () {
    findAllBookCategory();
});

function findAllBookCategory() {
    $.ajax({
        async: false,
        type: "post",
        url: "/findAllBookCategory",
        dataType: "json",
        success: function (data) {
            console.log(data);
            $("select[name='bookCategory']").empty();
            $("select[name='bookCategory']").append('<option value="">——请选择——</option>');
            for (let i = 0; i < data.length; i++) {
                let html = '<option value="' + data[i].categoryId + '">';
                html += data[i].categoryName + '</option>';
                $("select[name='bookCategory']").append(html);
            }
            // layui form 需要重新渲染
            if (layui && layui.form && typeof layui.form.render === 'function') {
                layui.form.render('select');
            }
        },
        error: function (data) {
            alert(data.result);
        }
    });
}