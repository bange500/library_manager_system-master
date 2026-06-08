layui.use(['upload', 'element', 'layer'], function () {
    let layer = layui.layer;
    let element = layui.element;
    let upload = layui.upload;

    let selectedFile = null;

    // 拖拽上传控件
    upload.render({
        elem: '#uploadBookExcel',
        url: '/importBooksByExcel',
        auto: false,          // 不自动上传
        accept: 'file',
        exts: 'xls|xlsx',
        size: 10240,          // 10MB
        choose: function (obj) {
            // 预读文件，只选中不自动提交
            obj.preview(function (index, file, result) {
                selectedFile = file;
                $('#fileNameDisplay').show().html(
                    '<span style="color:#1e9fff;">📄 已选择文件：</span><strong>' + file.name + '</strong>' +
                    '&nbsp;<span style="color:#999;">(' + (file.size / 1024).toFixed(1) + ' KB)</span>'
                );
                $('#btn_import').show();
            });
        },
        done: function (res) {
            // 显示导入结果
            if (res.success) {
                $('#importResult').show().html(
                    '<div class="layui-bg-green" style="padding:15px; border-radius:5px; text-align:center;">' +
                    '<i class="layui-icon layui-icon-ok-circle" style="font-size:30px;"></i><br>' +
                    '<strong>' + res.msg + '</strong></div>'
                );
            } else {
                $('#importResult').show().html(
                    '<div class="layui-bg-red" style="padding:15px; border-radius:5px; text-align:center;">' +
                    '<i class="layui-icon layui-icon-close-fill" style="font-size:30px;"></i><br>' +
                    '<strong>' + res.msg + '</strong></div>'
                );
            }
            // 重置选择
            selectedFile = null;
            $('#fileNameDisplay').hide();
            $('#btn_import').hide();
        },
        error: function () {
            layer.msg('上传失败，请重试', {icon: 2});
        }
    });

    // 点击"开始导入"按钮
    $('#btn_import').on('click', function () {
        if (!selectedFile) {
            layer.msg('请先选择Excel文件', {icon: 0});
            return;
        }

        layer.load(2); // 显示加载动画

        let formData = new FormData();
        formData.append('file', selectedFile);

        $.ajax({
            url: '/importBooksByExcel',
            type: 'POST',
            data: formData,
            processData: false,
            contentType: false,
            success: function (res) {
                layer.closeAll('loading');
                if (res.success) {
                    layer.msg(res.msg, {icon: 1, time: 3000});
                    $('#importResult').show().html(
                        '<div class="layui-bg-green" style="padding:15px; border-radius:5px; text-align:center;">' +
                        '<i class="layui-icon layui-icon-ok-circle" style="font-size:30px;"></i><br>' +
                        '<strong>' + res.msg + '</strong></div>'
                    );
                } else {
                    layer.msg(res.msg, {icon: 2, time: 3000});
                    $('#importResult').show().html(
                        '<div class="layui-bg-red" style="padding:15px; border-radius:5px; text-align:center;">' +
                        '<i class="layui-icon layui-icon-close-fill" style="font-size:30px;"></i><br>' +
                        '<strong>' + res.msg + '</strong></div>'
                    );
                }
                selectedFile = null;
                $('#fileNameDisplay').hide();
                $('#btn_import').hide();
            },
            error: function () {
                layer.closeAll('loading');
                layer.msg('导入失败，服务器异常', {icon: 2});
            }
        });
    });

    // 下载模板
    $('#downloadTemplate').on('click', function () {
        // 使用 Blob 在浏览器端生成 Excel 模板（CSV 格式，Excel 可打开）
        var csvContent = '﻿书名,作者,出版社,类别ID,价格,简介\n';
        csvContent += 'Java编程思想,Bruce Eckel,机械工业出版社,1,79.00,Java经典入门书籍\n';
        csvContent += '深入理解Java虚拟机,周志明,机械工业出版社,1,89.00,深入JVM原理';

        var blob = new Blob([csvContent], {type: 'text/csv;charset=utf-8;'});
        var link = document.createElement('a');
        var url = URL.createObjectURL(blob);
        link.href = url;
        link.download = '图书导入模板.csv';
        link.click();
        URL.revokeObjectURL(url);
        layer.msg('模板下载成功！请用Excel打开并编辑', {icon: 1});
    });
});
