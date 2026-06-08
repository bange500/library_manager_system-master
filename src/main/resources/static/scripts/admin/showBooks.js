layui.use(['form', 'element', 'layer'], function () {
    let form = layui.form;
    let element = layui.element;
    let layer = layui.layer;
});

$(document).ready(function () {

    //给选择框赋值
    findAllBookCategory();

    //检查能否再点击上一页，下一页
    let lab1 = $("#lab1").html().trim();//获取当前页码
    let lab2 = $("#lab2").html().trim();//获取总页码

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

    // 点击删除图书按钮 —— 二次确认流程
    $(".btn_deleteBook").click(function () {
        var that = $(this);
        var bookId = that.val();
        var bookName = that.data("book-name") || ("ID:" + bookId);

        // 第一步：检查图书是否在借阅中
        $.ajax({
            type: "post",
            url: "/checkBookStatus",
            dataType: "json",
            data: { bookId: bookId },
            success: function (res) {
                if (res.isBorrowed) {
                    // 图书正在被借阅，无法删除
                    layer.open({
                        type: 1,
                        title: '无法删除',
                        area: ['450px', '200px'],
                        shadeClose: false,
                        content: '<div style="padding:20px;text-align:center;">' +
                            '<p style="color:#ff5722;font-size:16px;margin-bottom:10px;">⚠ ' + res.msg + '</p>' +
                            '<p style="color:#666;">图书 <strong>"' + bookName + '"</strong> 当前有借阅记录，请等待归还后再删除。</p>' +
                            '</div>',
                        btn: ['知道了'],
                        yes: function (index) {
                            layer.close(index);
                        }
                    });
                } else {
                    // 未被借阅，二次确认删除
                    layer.open({
                        type: 1,
                        title: '删除确认',
                        area: ['450px', '220px'],
                        shadeClose: false,
                        content: '<div style="padding:20px;text-align:center;">' +
                            '<p style="color:#666;font-size:14px;margin-bottom:15px;">' +
                            '确定要删除图书 <strong>"' + bookName + '"</strong> 吗？</p>' +
                            '<p style="color:#ff5722;font-size:13px;">此操作不可恢复，请谨慎操作！</p>' +
                            '</div>',
                        btn: ['确认删除', '取消'],
                        yes: function (index) {
                            layer.close(index);
                            // 第二步：真正执行删除
                            layer.load(2);
                            $.ajax({
                                type: "post",
                                url: "/deleteBook",
                                dataType: "json",
                                data: { bookId: bookId },
                                success: function (deleteRes) {
                                    layer.closeAll('loading');
                                    if (deleteRes.toString() == "true") {
                                        layer.msg("删除成功", {icon: 1, time: 1500});
                                        setTimeout(function () {
                                            location.reload();
                                        }, 1500);
                                    } else if (deleteRes.toString() == "borrowed") {
                                        layer.msg("删除失败：该图书正在被借阅中", {icon: 2, time: 2000});
                                    } else {
                                        layer.msg("删除失败", {icon: 2, time: 2000});
                                    }
                                },
                                error: function () {
                                    layer.closeAll('loading');
                                    layer.msg("删除失败，服务器异常", {icon: 2});
                                }
                            });
                        },
                        btn2: function (index) {
                            layer.close(index);
                        }
                    });
                }
            },
            error: function () {
                layer.msg("检查失败，请重试", {icon: 2});
            }
        });
    });
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
};
