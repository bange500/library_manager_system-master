package com.zbw.utils;

import com.zbw.domain.Book;
import com.zbw.domain.User;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Excel 导入工具类
 * 支持 .xlsx 和 .xls 格式
 */
public class ExcelImportUtil {

    /**
     * 从 Excel 文件解析图书列表
     * Excel 表头：书名 | 作者 | 出版社 | 类别ID | 价格 | ISBN | 出版日期 | 入库数量 | 简介
     * 返回带行号的 ImportBookResult 列表，支持逐行校验和错误报告
     */
    public static List<ImportBookResult> parseBooksFromExcel(MultipartFile file) throws Exception {
        List<ImportBookResult> results = new ArrayList<>();
        Workbook workbook = getWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        // 跳过表头（第0行），从第1行开始读取
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;
            if (isRowEmpty(row)) continue;

            ImportBookResult result = new ImportBookResult();
            result.setRowNum(i + 1); // Excel行号（1-based）

            try {
                Book book = new Book();

                // 列0: 书名
                book.setBookName(getCellStringValue(row.getCell(0)));

                // 列1: 作者
                book.setBookAuthor(getCellStringValue(row.getCell(1)));

                // 列2: 出版社
                book.setBookPublish(getCellStringValue(row.getCell(2)));

                // 列3: 类别ID
                String categoryStr = getCellStringValue(row.getCell(3));
                if (categoryStr != null && !categoryStr.isEmpty()) {
                    book.setBookCategory((int) Double.parseDouble(categoryStr));
                }

                // 列4: 价格
                String priceStr = getCellStringValue(row.getCell(4));
                if (priceStr != null && !priceStr.isEmpty()) {
                    book.setBookPrice(Double.parseDouble(priceStr));
                }

                // 列5: ISBN（新增）
                book.setIsbn(getCellStringValue(row.getCell(5)));

                // 列6: 出版日期（新增）
                String dateStr = getCellStringValue(row.getCell(6));
                if (dateStr != null && !dateStr.isEmpty()) {
                    try {
                        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd");
                        sdf.setLenient(false);
                        book.setPublishDate(sdf.parse(dateStr));
                    } catch (Exception e) {
                        result.setError("出版日期格式不正确，应为 yyyy-MM-dd");
                        result.setBook(book);
                        results.add(result);
                        continue;
                    }
                }

                // 列7: 入库数量（新增）
                String stockStr = getCellStringValue(row.getCell(7));
                if (stockStr != null && !stockStr.isEmpty()) {
                    try {
                        int stock = (int) Double.parseDouble(stockStr);
                        if (stock < 0) {
                            result.setError("入库数量不能为负数");
                            result.setBook(book);
                            results.add(result);
                            continue;
                        }
                        book.setTotalStock(stock);
                    } catch (Exception e) {
                        result.setError("入库数量格式不正确，应为整数");
                        result.setBook(book);
                        results.add(result);
                        continue;
                    }
                }

                // 列8: 简介
                book.setBookIntroduction(getCellStringValue(row.getCell(8)));

                // 基本校验：书名不能为空
                if (book.getBookName() == null || book.getBookName().trim().isEmpty()) {
                    result.setError("书名不能为空");
                    result.setBook(book);
                    results.add(result);
                    continue;
                }

                result.setBook(book);
                result.setSuccess(true);
                results.add(result);

            } catch (Exception e) {
                result.setError("解析失败: " + e.getMessage());
                results.add(result);
            }
        }
        workbook.close();
        return results;
    }

    /**
     * 导入结果包装类
     */
    public static class ImportBookResult {
        private int rowNum;
        private boolean success;
        private Book book;
        private String error;

        public int getRowNum() { return rowNum; }
        public void setRowNum(int rowNum) { this.rowNum = rowNum; }
        public boolean isSuccess() { return success; }
        public void setSuccess(boolean success) { this.success = success; }
        public Book getBook() { return book; }
        public void setBook(Book book) { this.book = book; }
        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    /**
     * 从 Excel 文件解析用户列表
     * Excel 表头：用户名 | 密码 | 邮箱
     */
    public static List<User> parseUsersFromExcel(MultipartFile file) throws Exception {
        List<User> users = new ArrayList<>();
        Workbook workbook = getWorkbook(file);
        Sheet sheet = workbook.getSheetAt(0);

        // 跳过表头（第0行），从第1行开始读取
        for (int i = 1; i <= sheet.getLastRowNum(); i++) {
            Row row = sheet.getRow(i);
            if (row == null) continue;

            // 跳过全空行
            if (isRowEmpty(row)) continue;

            try {
                User user = new User();
                user.setUserName(getCellStringValue(row.getCell(0)));
                user.setUserPwd(getCellStringValue(row.getCell(1)));
                user.setUserEmail(getCellStringValue(row.getCell(2)));

                // 基本校验：用户名和密码不能为空
                if (user.getUserName() == null || user.getUserName().trim().isEmpty()
                    || user.getUserPwd() == null || user.getUserPwd().trim().isEmpty()) {
                    continue;
                }

                users.add(user);
            } catch (Exception e) {
                System.err.println("解析第" + (i + 1) + "行失败: " + e.getMessage());
            }
        }
        workbook.close();
        return users;
    }

    /**
     * 根据文件后缀获取对应的 Workbook
     */
    private static Workbook getWorkbook(MultipartFile file) throws Exception {
        String fileName = file.getOriginalFilename();
        InputStream inputStream = file.getInputStream();

        if (fileName != null && fileName.endsWith(".xlsx")) {
            return new XSSFWorkbook(inputStream);
        } else if (fileName != null && fileName.endsWith(".xls")) {
            return new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("不支持的文件格式，请上传 .xlsx 或 .xls 文件");
        }
    }

    /**
     * 获取单元格的字符串值
     */
    private static String getCellStringValue(Cell cell) {
        if (cell == null) return null;

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                // 去掉小数点后多余的零
                double val = cell.getNumericCellValue();
                if (val == Math.floor(val) && !Double.isInfinite(val)) {
                    return String.valueOf((long) val);
                }
                return String.valueOf(val);
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                try {
                    return cell.getStringCellValue().trim();
                } catch (Exception e) {
                    return String.valueOf(cell.getNumericCellValue());
                }
            default:
                return null;
        }
    }

    /**
     * 判断一整行是否为空
     */
    private static boolean isRowEmpty(Row row) {
        if (row == null) return true;
        Iterator<Cell> cellIterator = row.cellIterator();
        while (cellIterator.hasNext()) {
            Cell cell = cellIterator.next();
            if (cell != null && cell.getCellType() != CellType.BLANK
                && getCellStringValue(cell) != null && !getCellStringValue(cell).isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
