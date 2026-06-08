layui.use(['form', 'element', 'layer'], function () {
    var form = layui.form;
    var element = layui.element;
    var layer = layui.layer;

    // 提交事件
    form.on('submit(btn_addBookCategory)', function (data) {
        addBookCategory();
        return false;
    });

    // 页面加载完成后的逻辑
    $(function () {
        // 检查能否点击上一页、下一页
        var lab1 = $("#lab1").html().trim(); // 当前页码
        var lab2 = $("#lab2").html().trim(); // 总页码

        $("#prePage").click(function () {
            if (lab1 == 1) {
                layer.msg("已经是第一页了！", {icon: 7});
                return false;
            }
            return true;
        });

        $("#nextPage").click(function () {
            if (lab1 == lab2) {
                layer.msg("已经是最后一页了！", {icon: 7});
                return false;
            }
            return true;
        });

        // 点击删除按钮 —— 二次确认流程
        $(".btn_deleteCategory").click(function () {
            var that = $(this);
            var bookCategoryId = that.val();
            var categoryName = that.data("category-name") || ("ID:" + bookCategoryId);

            // 第一步：查询该类别下的图书
            $.ajax({
                type: "post",
                url: "/findBooksByCategoryId",
                dataType: "json",
                data: { bookCategoryId: bookCategoryId },
                success: function (res) {
                    var bookCount = res.count;
                    var books = res.books;

                    // 构建确认弹窗内容
                    var contentHtml = '';
                    if (bookCount > 0) {
                        // 有图书，展示列表
                        contentHtml += '<div style="margin-bottom:15px;">';
                        contentHtml += '<p style="color:#ff5722;font-size:14px;margin-bottom:10px;">';
                        contentHtml += '⚠ 类别 <strong>"' + categoryName + '"</strong> 下共有 <strong>' + bookCount + '</strong> 本书：';
                        contentHtml += '</p>';
                        contentHtml += '<table class="layui-table" lay-size="sm" style="margin:0;">';
                        contentHtml += '<thead><tr><th>图书ID</th><th>书名</th><th>作者</th><th>出版社</th></tr></thead><tbody>';
                        for (var i = 0; i < books.length; i++) {
                            contentHtml += '<tr>';
                            contentHtml += '<td>' + books[i].bookId + '</td>';
                            contentHtml += '<td>' + books[i].bookName + '</td>';
                            contentHtml += '<td>' + (books[i].bookAuthor || '') + '</td>';
                            contentHtml += '<td>' + (books[i].bookPublish || '') + '</td>';
                            contentHtml += '</tr>';
                        }
                        contentHtml += '</tbody></table>';
                        contentHtml += '<p style="color:#ff5722;font-size:13px;margin-top:10px;">';
                        contentHtml += '删除类别后，这些书的类别信息将丢失！</p>';
                        contentHtml += '</div>';
                    } else {
                        // 没有图书
                        contentHtml += '<p style="color:#666;font-size:14px;">';
                        contentHtml += '类别 <strong>"' + categoryName + '"</strong> 下没有图书，可以安全删除。</p>';
                    }

                    contentHtml += '<p style="text-align:center;color:#333;font-weight:bold;">确定要删除类别 <span style="color:#ff5722;">"' + categoryName + '"</span> 吗？</p>';

                    // 第二步：弹出确认框
                    layer.open({
                        type: 1,
                        title: '删除确认',
                        area: bookCount > 0 ? ['600px', '420px'] : ['450px', '200px'],
                        shadeClose: false,
                        content: '<div style="padding:20px;">' + contentHtml + '</div>',
                        btn: ['确认删除', '取消'],
                        yes: function (index) {
                            layer.close(index);
                            // 第三步：真正执行删除
                            layer.load(2);
                            $.ajax({
                                type: "post",
                                url: "/deleteCategory",
                                dataType: "json",
                                data: { bookCategoryId: bookCategoryId },
                                success: function (deleteRes) {
                                    layer.closeAll('loading');
                                    if (deleteRes.toString() == "true") {
                                        layer.msg("删除成功", {icon: 1, time: 1500});
                                        setTimeout(function () {
                                            location.reload();
                                        }, 1500);
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
                },
                error: function () {
                    layer.msg("查询失败，请重试", {icon: 2});
                }
            });
        });
    });
});

// ajax 添加种类
function addBookCategory() {
    $.ajax({
        async: false,
        type: "post",
        url: "/addBookCategory",
        dataType: "json",
        data: $("#addBookCategoryForm").serialize(),
        success: function (data) {
            if (data.toString() == "true") {
                layer.msg("添加成功!", {icon: 1, time: 1500});
                setTimeout(function () {
                    location.reload();
                }, 1500);
            } else {
                layer.msg("添加失败!", {icon: 2, time: 1500});
            }
        },
        error: function () {
            layer.msg("添加失败!", {icon: 2, time: 1500});
        }
    });
}
