layui.use(['form', 'element', 'layer'], function () {
    let form = layui.form;
    let element = layui.element;
    let layer = layui.layer;

    // 监听类别下拉框变化，同步到关键字搜索表单的隐藏域
    form.on('select', function (data) {
        if (data.elem.name === 'bookCategory') {
            $('#keywordBookCategory').val(data.value);
        }
    });
});

$(document).ready(function () {

    // 给选择框加载所有图书类别
    findAllBookCategory();

    // 将当前选中的类别同步到关键字搜索表单
    if (typeof selectedCategory !== 'undefined' && selectedCategory > 0) {
        $('#keywordBookCategory').val(selectedCategory);
    }

    // 检查能否再点击上一页，下一页
    let lab1 = $("#lab1").html();
    let lab2 = $("#lab2").html();

    if (lab1 && lab2) {
        lab1 = lab1.trim();
        lab2 = lab2.trim();

        $("#prePage").click(function () {
            if (lab1 == 1) {
                layer.msg("已经是第一页了!", {icon: 7});
                return false;
            }
            return true;
        });
        $("#nextPage").click(function () {
            if (lab1 == lab2) {
                layer.msg("已经是最后一页了!", {icon: 7});
                return false;
            }
            return true;
        });
    }
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
                if (typeof selectedCategory !== 'undefined' && selectedCategory > 0
                    && data[i].categoryId == selectedCategory) {
                    selected = ' selected';
                }
                let html = '<option value="' + data[i].categoryId + '"' + selected + '>';
                html += data[i].categoryName + '</option>';
                $("select[name='bookCategory']").append(html);
            }
            // 重新渲染select以显示选中项
            layui.form.render('select');
        },
        error: function (data) {
            alert(data.result);
        }
    });
}
