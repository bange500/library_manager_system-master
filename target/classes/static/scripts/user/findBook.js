/**
 * 查询书籍页面 - 图书类别下拉框加载 + 更多内容 + 分页控制
 */

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

    // 在 layui 模块就绪后再加载类别下拉框，避免 layui.form 未定义的竞态问题
    findAllBookCategorySafe(form, layer);
});

$(document).ready(function () {

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

// ========== 安全的类别加载（由 layui.use 回调调用） ==========
function findAllBookCategorySafe(form, layer) {
    $.ajax({
        type: "post",
        url: "/findAllBookCategory",
        dataType: "json",
        success: function (data) {
            var $select = $("select[name='bookCategory']");
            if (!$select.length) return;

            $select.empty();
            $select.append('<option value="">——请选择——</option>');

            if (!data || !Array.isArray(data)) {
                console.warn('[findBook] 类别数据格式异常:', data);
                if (form && typeof form.render === 'function') {
                    form.render('select');
                }
                return;
            }

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

            // 安全渲染select：只有 form 对象存在时才调用 render
            if (form && typeof form.render === 'function') {
                form.render('select');
            }
        },
        error: function (xhr, status, err) {
            console.error('[findBook] 加载类别失败:', status, err);
        }
    });
}

// ========== 旧版兼容：如果 layui 未就绪时被调用，跳过 render ==========
function findAllBookCategory() {
    $.ajax({
        async: false,
        type: "post",
        url: "/findAllBookCategory",
        dataType: "json",
        success: function (data) {
            var $select = $("select[name='bookCategory']");
            if (!$select.length) return;

            $select.empty();
            $select.append('<option value="">——请选择——</option>');

            if (!data || !Array.isArray(data)) return;

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

            // 安全渲染：防止 layui.form 未就绪时报错
            try {
                if (layui && layui.form && typeof layui.form.render === 'function') {
                    layui.form.render('select');
                }
            } catch (e) {
                console.warn('[findBook] layui.form.render 调用失败（模块未就绪）:', e.message);
            }
        },
        error: function (xhr, status, err) {
            console.error('[findBook] 加载类别失败:', status, err);
        }
    });
}

// ========== 查看书籍简介 ==========

/**
 * 点击【更多内容】按钮 → AJAX 异步获取书籍完整详情 → 渲染到下方详情面板
 * @param {HTMLElement|jQuery} btn  触发按钮
 */
function showBookDetail(btn) {
    // 兼容传入原生 DOM 或 jQuery 对象
    var $btn = $(btn);
    var bookId = $btn.attr('data-bookid') || (btn.getAttribute && btn.getAttribute('data-bookid'));
    if (!bookId) {
        console.warn('[showBookDetail] 未获取到 bookId');
        return;
    }

    // 按钮防抖：请求中不允许重复点击
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
    resetDetailFields();
    $('#detail_bookName').text('加载中...');
    $panel[0].scrollIntoView({ behavior: 'smooth', block: 'center' });

    // 发起 AJAX 请求
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
                showEmptyDetail();
                layer.msg(msg, {icon: 2});
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
            layer.msg('网络请求失败（状态码: ' + xhr.status + '），请稍后重试', {icon: 2});
        },
        complete: function () {
            // 恢复按钮状态
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

    // 安全获取字段值，统一兜底
    var safe = function (val, fallback) {
        if (fallback === undefined) fallback = '-';
        return (val !== null && val !== undefined && val !== '') ? val : fallback;
    };

    // 基础信息
    setText('detail_bookId',        safe(data.bookId));
    setText('detail_bookName',      safe(data.bookName));
    setText('detail_bookAuthor',    safe(data.bookAuthor));
    setText('detail_bookPublish',   safe(data.bookPublish));

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
    setText('detail_bookIntroduction', safe(data.bookIntroduction, '暂无简介'));
    setText('detail_totalStock',       safe(data.totalStock));
    setText('detail_availableCount',   safe(data.availableCount));
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
    var ids = [
        'detail_bookId', 'detail_bookName', 'detail_bookAuthor', 'detail_bookPublish',
        'detail_isExist', 'detail_isbn', 'detail_categoryName', 'detail_publishDate',
        'detail_bookIntroduction', 'detail_totalStock', 'detail_availableCount'
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