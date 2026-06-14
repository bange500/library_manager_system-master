layui.use(['form', 'layer', 'element'], function () {
    let form = layui.form;
    let layer = layui.layer;

    // 新建按钮
    $('#btnAdd').on('click', function () {
        openFormLayer('新建公告/活动', null);
    });

    // 编辑按钮 — 先通过JSON接口获取完整数据再打开弹窗
    $(document).on('click', '.btn-edit', function () {
        let id = $(this).data('id');
        // 获取完整数据
        $.get('/getAnnouncementJson', { id: id }, function (data) {
            if (data) {
                openFormLayer('编辑公告/活动', data);
            } else {
                layer.msg('获取公告信息失败', { icon: 2 });
            }
        }, 'json').fail(function () {
            layer.msg('请求失败，请重试', { icon: 2 });
        });
    });

    // 删除按钮
    $(document).on('click', '.btn-delete', function () {
        let id = $(this).data('id');
        layer.confirm('确定要删除该公告/活动吗？此操作不可恢复。', {
            icon: 3,
            title: '确认删除',
            btn: ['确定', '取消']
        }, function (index) {
            $.ajax({
                url: '/deleteAnnouncement',
                type: 'POST',
                data: { id: id },
                dataType: 'json',
                success: function (res) {
                    if (res.success) {
                        layer.msg('删除成功', { icon: 1 }, function () {
                            location.reload();
                        });
                    } else {
                        layer.msg(res.msg || '删除失败', { icon: 2 });
                    }
                },
                error: function () {
                    layer.msg('请求失败，请重试', { icon: 2 });
                }
            });
            layer.close(index);
        });
    });

    // 打开新增/编辑弹窗
    function openFormLayer(title, data) {
        layer.open({
            type: 1,
            title: title,
            area: ['720px', '560px'],
            content: $('#formTpl').html(),
            success: function (layero, index) {
                // 填充编辑数据
                if (data) {
                    layero.find('input[name="id"]').val(data.id || '');
                    layero.find('input[name="title"]').val(data.title || '');
                    layero.find('textarea[name="summary"]').val(data.summary || '');
                    layero.find('input[name="coverImage"]').val(data.coverImage || '');
                    layero.find('textarea[name="content"]').val(data.content || '');
                    if (data.type) {
                        layero.find('input[name="type"][value="' + data.type + '"]').prop('checked', true);
                    }
                    var carouselVal = (data.isCarousel !== undefined && data.isCarousel !== null) ? data.isCarousel : 0;
                    layero.find('input[name="isCarousel"][value="' + carouselVal + '"]').prop('checked', true);
                }
                form.render(null, 'announcementForm');
            },
            btn: ['保存', '取消'],
            yes: function (index) {
                var layerBody = $('#layui-layer' + index);
                var titleVal = layerBody.find('input[name="title"]').val();
                var contentVal = layerBody.find('textarea[name="content"]').val();

                if (!titleVal || titleVal.trim() === '') {
                    layer.msg('请输入标题', { icon: 5 });
                    return;
                }
                if (!contentVal || contentVal.trim() === '') {
                    layer.msg('请输入详细内容', { icon: 5 });
                    return;
                }

                var formData = {
                    id: layerBody.find('input[name="id"]').val() || undefined,
                    title: titleVal.trim(),
                    type: layerBody.find('input[name="type"]:checked').val(),
                    isCarousel: parseInt(layerBody.find('input[name="isCarousel"]:checked').val()),
                    summary: layerBody.find('textarea[name="summary"]').val().trim(),
                    coverImage: layerBody.find('input[name="coverImage"]').val().trim(),
                    content: contentVal.trim()
                };

                // 新建时不传空id
                if (!formData.id) {
                    delete formData.id;
                }

                var url = formData.id ? '/updateAnnouncement' : '/addAnnouncement';

                $.ajax({
                    url: url,
                    type: 'POST',
                    data: formData,
                    dataType: 'json',
                    success: function (res) {
                        if (res.success) {
                            layer.msg(res.msg || '保存成功', { icon: 1 }, function () {
                                location.reload();
                            });
                        } else {
                            layer.msg(res.msg || '保存失败', { icon: 2 });
                        }
                    },
                    error: function () {
                        layer.msg('请求失败，请重试', { icon: 2 });
                    }
                });
            },
            btn2: function (index) {
                layer.close(index);
            }
        });
    }
});
