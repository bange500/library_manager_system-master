/**
 * 编辑图书页面 JS
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
        value: $('#publishDate').val() || ''
    });

    // 自定义校验规则
    form.verify({
        isbn: function (value) {
            if (!value) return 'ISBN不能为空';
            var isbnPattern = /^[0-9\-]{10,17}$/;
            if (!isbnPattern.test(value.trim())) {
                return 'ISBN格式不正确（10-17位数字或连字符）';
            }
        },
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
        var isbn = $('#isbn').val().trim();
        if (!isbn) {
            layer.msg('请输入ISBN', {icon: 2});
            return false;
        }

        // 如果ISBN未修改，跳过唯一性校验
        if (originalIsbn === isbn) {
            updateBook();
            return false;
        }

        // 异步校验ISBN是否已存在
        $.ajax({
            type: 'GET',
            url: '/checkIsbnUnique',
            data: { isbn: isbn },
            async: false,
            success: function (resp) {
                if (resp === false || resp === 'false') {
                    layer.msg('ISBN已存在，请更换', {icon: 2});
                    isbnCheckPassed = false;
                } else {
                    isbnCheckPassed = true;
                }
            },
            error: function () {
                isbnCheckPassed = true;
            }
        });

        if (!isbnCheckPassed) return false;

        updateBook();
        return false;
    });

    // 给ISBN输入框添加失焦校验
    $('#isbn').on('blur', function () {
        var val = $(this).val().trim();
        if (!val || val === originalIsbn) return;
        $.ajax({
            type: 'GET',
            url: '/checkIsbnUnique',
            data: { isbn: val },
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

function updateBook() {
    $.ajax({
        async: false,
        type: 'post',
        url: '/updateBook',
        data: $('#editBookForm').serialize(),
        success: function (data) {
            if (data === 'true') {
                layer.msg("修改成功", {time: 1500, icon: 1});
                setTimeout(function () {
                    window.location.href = '/showBooksPage';
                }, 1500);
            } else {
                layer.msg(data, {time: 3000, icon: 2});
            }
        },
        error: function () {
            layer.msg("修改失败，服务器异常", {icon: 2});
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
            $("select[name='bookCategory']").empty();
            $("select[name='bookCategory']").append('<option value="">——请选择——</option>');
            for (let i = 0; i < data.length; i++) {
                let selected = '';
                if (originalCategoryId > 0 && data[i].categoryId == originalCategoryId) {
                    selected = ' selected';
                }
                let html = '<option value="' + data[i].categoryId + '"' + selected + '>';
                html += data[i].categoryName + '</option>';
                $("select[name='bookCategory']").append(html);
            }
            if (layui && layui.form && typeof layui.form.render === 'function') {
                layui.form.render('select');
            }
        },
        error: function () {
            console.error('加载图书分类失败');
        }
    });
}