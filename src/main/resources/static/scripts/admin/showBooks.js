/**
 * 管理员端 - 查询书籍页面 JS
 * 修复：layui.form 竞态 + 空数据兜底 + 分类/书籍无数据提示
 */
layui.use(['form', 'element', 'layer'], function () {
    var form = layui.form;
    var layer = layui.layer;

    // ========== 加载图书分类下拉框 ==========
    findAllBookCategory(form);

    // ========== 分页按钮控制 ==========
    var $lab1 = $("#lab1");
    var $lab2 = $("#lab2");
    var lab1 = ($lab1.length && $lab1.html()) ? $lab1.html().trim() : '';
    var lab2 = ($lab2.length && $lab2.html()) ? $lab2.html().trim() : '';

    $("#prePage").click(function () {
        if (lab1 === '1' || lab1 === '') {
            layer.msg("已经是第一页了!", {icon: 7});
            return false;
        }
        return true;
    });
    $("#nextPage").click(function () {
        if (lab1 === lab2 || lab2 === '' || lab1 === '') {
            layer.msg("已经是最后一页了!", {icon: 7});
            return false;
        }
        return true;
    });

    // ========== 刷新推荐缓存按钮 ==========
    $(document).on('click', '#btnRefreshCache', function () {
        var $btn = $(this);
        $btn.prop('disabled', true).text('刷新中...');
        layer.load(2);

        $.ajax({
            type: 'POST',
            url: '/admin/refreshRecommendCache',
            dataType: 'json',
            timeout: 30000,
            success: function (res) {
                layer.closeAll('loading');
                if (res && res.success) {
                    layer.msg('推荐缓存刷新成功！' +
                        '总图书: ' + (res.totalBooks || 0) +
                        ', 可借: ' + (res.availableBooks || 0) +
                        ', 分类: ' + (res.categoryCount || 0) +
                        ', 耗时: ' + (res.elapsedMs || 0) + 'ms',
                        {icon: 1, time: 3000});
                } else {
                    var errMsg = (res && res.msg) ? res.msg : '刷新失败';
                    layer.msg(errMsg, {icon: 2, time: 3000});
                }
            },
            error: function (xhr, status, err) {
                layer.closeAll('loading');
                layer.msg('刷新失败，服务器异常（状态码: ' + xhr.status + '）', {icon: 2});
                console.error('[refreshCache] 请求失败:', status, err);
            },
            complete: function () {
                $btn.prop('disabled', false).html('<i class="layui-icon layui-icon-refresh"></i> 刷新缓存');
            }
        });
    });

    // ========== 删除图书按钮 —— 二次确认流程 ==========
    // 使用事件委托绑定，兼容 AJAX 后动态加载的按钮
    $(document).on('click', '.btn_deleteBook', function () {
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
                            layer.load(2);
                            $.ajax({
                                type: "post",
                                url: "/deleteBook",
                                dataType: "json",
                                data: { bookId: bookId },
                                success: function (deleteRes) {
                                    layer.closeAll('loading');
                                    if (deleteRes.toString() === "true") {
                                        layer.msg("删除成功", {icon: 1, time: 1500});
                                        setTimeout(function () {
                                            location.reload();
                                        }, 1500);
                                    } else if (deleteRes.toString() === "borrowed") {
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

// ========== 加载图书分类（供 layui.use 回调调用） ==========
function findAllBookCategory(form) {
    $.ajax({
        type: "post",
        url: "/findAllBookCategory",
        dataType: "json",
        success: function (data) {
            var $select = $("select[name='bookCategory']");
            if (!$select.length) return;

            $select.empty();

            // 空数据 / 数据异常兜底
            if (!data || !Array.isArray(data) || data.length === 0) {
                $select.append('<option value="">——暂无分类数据——</option>');
                console.warn('[showBooks] 图书分类数据为空，请先到【新建类别】页面添加分类');
            } else {
                $select.append('<option value="">——请选择——</option>');
                for (var i = 0; i < data.length; i++) {
                    var item = data[i];
                    if (!item || item.categoryId == null) continue;

                    var selected = '';
                    if (typeof selectedCategory !== 'undefined' && selectedCategory > 0
                        && item.categoryId == selectedCategory) {
                        selected = ' selected';
                    }
                    var html = '<option value="' + item.categoryId + '"' + selected + '>'
                        + (item.categoryName || '未命名') + '</option>';
                    $select.append(html);
                }
            }

            // 安全渲染select：只有 form 有效时才调用
            safeFormRender(form, 'select');
        },
        error: function (xhr, status, err) {
            console.error('[showBooks] 加载图书分类失败:', status, err);
            var $select = $("select[name='bookCategory']");
            if ($select.length) {
                $select.empty().append('<option value="">——加载失败——</option>');
            }
            safeFormRender(form, 'select');
        }
    });
}

/**
 * 安全的 form.render 调用，防止 layui.form 未就绪时报错
 */
function safeFormRender(form, type) {
    try {
        if (form && typeof form.render === 'function') {
            form.render(type);
        } else if (layui && layui.form && typeof layui.form.render === 'function') {
            layui.form.render(type);
        }
    } catch (e) {
        console.warn('[showBooks] form.render 调用失败（模块未就绪）:', e.message);
    }
}

// ========== 书籍详情（更多内容） ==========

/**
 * 点击【更多内容】按钮 → AJAX 异步获取书籍完整详情 → 渲染到下方详情面板
 * @param {HTMLElement|jQuery} btn  触发按钮
 */
function showBookDetail(btn) {
    var $btn = $(btn);
    var bookId = $btn.attr('data-bookid') || (btn.getAttribute && btn.getAttribute('data-bookid'));
    if (!bookId) {
        console.warn('[showBookDetail] 未获取到 bookId');
        return;
    }

    // 按钮防抖
    if ($btn.data('loading') === true) return;
    $btn.data('loading', true);
    $btn.text('加载中...').prop('disabled', true);

    var $panel = $('#bookDetailPanel');
    if (!$panel.length) {
        console.error('[showBookDetail] 页面上不存在 #bookDetailPanel 元素');
        $btn.data('loading', false).text('更多内容').prop('disabled', false);
        return;
    }

    // 显示面板 + 重置字段 + 滚动
    $panel.show();
    $('#detail_empty').hide();
    resetDetailFields();
    $('#detail_bookName').text('加载中...');
    $panel[0].scrollIntoView({ behavior: 'smooth', block: 'center' });

    $.ajax({
        type: 'GET',
        url: '/getBookDetail',
        data: { bookId: bookId },
        dataType: 'json',
        timeout: 10000,
        success: function (data) {
            if (data && data.success === true) {
                fillDetailFields(data);
                // 同时加载推荐图书
                var categoryId = data.categoryId || 0;
                loadRecommendBooks(categoryId, bookId);
            } else {
                var msg = (data && data.msg) ? data.msg : '获取书籍详情失败';
                // 显示空数据兜底
                showEmptyDetail();
                if (typeof layer !== 'undefined' && layer.msg) {
                    layer.msg(msg, {icon: 2});
                } else {
                    alert(msg);
                }
            }
        },
        error: function (xhr, status, err) {
            console.error('[showBookDetail] AJAX 请求失败:', {
                status: status,
                error: err,
                statusCode: xhr.status,
                responseText: xhr.responseText
            });
            showEmptyDetail();
            if (typeof layer !== 'undefined' && layer.msg) {
                layer.msg('网络请求失败（状态码: ' + xhr.status + '），请稍后重试', {icon: 2});
            } else {
                alert('网络请求失败，请稍后重试');
            }
        },
        complete: function () {
            $btn.data('loading', false);
            $btn.text('更多内容').prop('disabled', false);
        }
    });
}

/**
 * 关闭详情面板
 */
function closeBookDetail() {
    $('#bookDetailPanel').hide();
}

/**
 * 显示空数据兜底提示
 */
function showEmptyDetail() {
    $('#bookDetailPanel').show();
    // 隐藏所有 fieldset，显示空提示
    $('#bookDetailPanel fieldset').hide();
    $('#detail_empty').show();
    // 同时重置推荐区域
    resetRecommendPanel();
}

/**
 * 将 AJAX 返回的数据填充到详情面板（带完整的空值兜底）
 * @param {Object} data  后端返回的 JSON 对象
 */
function fillDetailFields(data) {
    // 显示 fieldset，隐藏空提示
    $('#bookDetailPanel fieldset').show();
    $('#detail_empty').hide();

    var safe = function (val, fallback) {
        if (fallback === undefined) fallback = '-';
        return (val !== null && val !== undefined && val !== '') ? val : fallback;
    };

    // 基础信息
    setText('detail_bookId',      safe(data.bookId));
    setText('detail_bookName',    safe(data.bookName));
    setText('detail_bookAuthor',  safe(data.bookAuthor));
    setText('detail_bookPublish', safe(data.bookPublish));

    var isExist = safe(data.isExist, '-');
    var $isExist = $('#detail_isExist');
    $isExist.text(isExist);
    if (isExist === '可借') {
        $isExist.css('color', '#5FB878');
    } else if (isExist === '不可借') {
        $isExist.css('color', '#FF5722');
    } else {
        $isExist.css('color', '');
    }

    // 扩展信息
    setText('detail_isbn',             safe(data.isbn));
    setText('detail_categoryName',     safe(data.categoryName));
    setText('detail_publishDate',      safe(data.publishDate));
    setText('detail_totalStock',       safe(data.totalStock));
    setText('detail_availableCount',   safe(data.availableCount));
    setText('detail_bookIntroduction', safe(data.bookIntroduction, '暂无简介'));

    // 检查是否所有扩展字段都为空 → 显示兜底
    var extFields = ['detail_isbn', 'detail_categoryName', 'detail_publishDate',
                     'detail_totalStock', 'detail_availableCount', 'detail_bookIntroduction'];
    var allEmpty = true;
    for (var i = 0; i < extFields.length; i++) {
        var t = $('#' + extFields[i]).text();
        if (t && t !== '-' && t !== '' && t !== '暂无简介') {
            allEmpty = false;
            break;
        }
    }
    if (allEmpty) {
        $('#bookDetailPanel fieldset').hide();
        $('#detail_empty').show();
    }
}

/**
 * 安全设置元素文本（带空值检查）
 */
function setText(id, text) {
    var $el = $('#' + id);
    if ($el.length) {
        $el.text(text);
    }
}

/**
 * 重置详情面板所有字段为初始占位符
 */
function resetDetailFields() {
    // 先恢复 fieldset 显示
    $('#bookDetailPanel fieldset').show();
    $('#detail_empty').hide();

    var ids = [
        'detail_bookId', 'detail_bookName', 'detail_bookAuthor', 'detail_bookPublish',
        'detail_isExist', 'detail_isbn', 'detail_categoryName', 'detail_publishDate',
        'detail_totalStock', 'detail_availableCount', 'detail_bookIntroduction'
    ];
    for (var i = 0; i < ids.length; i++) {
        var $el = $('#' + ids[i]);
        if ($el.length) {
            $el.text('-').css('color', '');
        }
    }
    // 同时重置推荐区域
    resetRecommendPanel();
}

// ========== 推荐图书 ==========

/**
 * 加载推荐图书（同类别下排除当前书籍）
 * @param {number} categoryId  当前书籍的类别ID
 * @param {number} bookId      当前书籍ID
 */
function loadRecommendBooks(categoryId, bookId) {
    if (!categoryId || categoryId === 0) {
        showRecommendEmpty();
        return;
    }

    $.ajax({
        type: 'GET',
        url: '/getRecommendBooks',
        data: { categoryId: categoryId, bookId: bookId },
        dataType: 'json',
        timeout: 10000,
        success: function (res) {
            if (res && res.success && res.data && res.data.length > 0) {
                renderRecommendList(res.data);
            } else {
                showRecommendEmpty();
            }
        },
        error: function (xhr, status, err) {
            console.error('[loadRecommendBooks] 请求失败:', status, err);
            showRecommendEmpty();
        }
    });
}

/**
 * 渲染推荐图书列表
 * @param {Array} books  [{bookId, bookName, bookAuthor, bookPublish}, ...]
 */
function renderRecommendList(books) {
    var $list = $('#recommendList');
    var $empty = $('#recommendEmpty');
    var $panel = $('#recommendPanel');
    if (!$list.length) return;

    $list.empty();
    $empty.hide();
    $panel.show();

    var cardStyle = 'border:1px solid #e2e2e2;border-radius:6px;padding:15px;' +
        'flex:1;min-width:200px;max-width:32%;background:#fff;' +
        'box-shadow:0 1px 4px rgba(0,0,0,.06);transition:box-shadow .2s;';
    var hoverStyle = 'box-shadow:0 3px 10px rgba(0,0,0,.12)';
    var titleStyle = 'font-size:15px;font-weight:bold;color:#1E9FFF;margin-bottom:8px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;';
    var textStyle = 'font-size:13px;color:#666;margin-bottom:4px;';
    var btnStyle = 'margin-top:10px;font-size:12px;cursor:pointer;border:none;padding:5px 12px;border-radius:3px;background:#1E9FFF;color:#fff;';

    for (var i = 0; i < books.length; i++) {
        var b = books[i];
        if (!b) continue;
        var bookName   = b.bookName   || '未知书名';
        var bookAuthor = b.bookAuthor || '未知作者';
        var bookPublish= b.bookPublish|| '未知出版社';
        var bid        = b.bookId     || '';

        var cardHtml =
            '<div class="recommend-card" style="' + cardStyle + '"' +
            ' onmouseover="this.style.boxShadow=\'' + hoverStyle.replace(/'/g, "\\'") + '\'"' +
            ' onmouseleave="this.style.boxShadow=\'0 1px 4px rgba(0,0,0,.06)\'">' +
            '<div style="' + titleStyle + '" title="' + bookName + '">' + bookName + '</div>' +
            '<div style="' + textStyle + '">作者：' + bookAuthor + '</div>' +
            '<div style="' + textStyle + '">出版社：' + bookPublish + '</div>' +
            '<button style="' + btnStyle + '" ' +
            'data-bookid="' + bid + '" onclick="showBookDetail(this)">更多内容</button>' +
            '</div>';
        $list.append(cardHtml);
    }
}

/**
 * 显示推荐为空提示
 */
function showRecommendEmpty() {
    var $list = $('#recommendList');
    var $empty = $('#recommendEmpty');
    var $panel = $('#recommendPanel');
    if ($list.length) $list.empty();
    if ($empty.length) $empty.show();
    if ($panel.length) $panel.show();
}

/**
 * 重置推荐区域（隐藏推荐模块、清空内容）
 */
function resetRecommendPanel() {
    var $list = $('#recommendList');
    var $empty = $('#recommendEmpty');
    var $panel = $('#recommendPanel');
    if ($list.length) $list.empty();
    if ($empty.length) $empty.hide();
    if ($panel.length) $panel.hide();
}