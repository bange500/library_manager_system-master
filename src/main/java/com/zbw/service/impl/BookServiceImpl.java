package com.zbw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zbw.domain.Book;
import com.zbw.domain.BorrowingBooks;
import com.zbw.domain.Vo.BookVo;
import com.zbw.mapper.BookMapper;
import com.zbw.mapper.BorrowingBooksMapper;
import com.zbw.service.IBookService;
import com.zbw.utils.page.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class BookServiceImpl implements IBookService {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);

    private static final String RECOMMEND_KEY_PREFIX = "recommend:category:";

    @Resource
    private BookMapper bookMapper;
    @Resource
    private BorrowingBooksMapper borrowingBooksMapper;
    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public List<BookVo> selectBooksByBookPartInfo(String partInfo) {
        List<BookVo> bookVos = new LinkedList<>();
        List<Book> books = bookMapper.selectList(
            new LambdaQueryWrapper<Book>().like(Book::getBookName, partInfo));

        if (null == books) {
            return bookVos;
        }

        for (Book b : books) {
            BookVo bookVo = new BookVo();
            bookVo.setBookId(b.getBookId());
            bookVo.setBookName(b.getBookName());
            bookVo.setBookAuthor(b.getBookAuthor());
            bookVo.setBookPublish(b.getBookPublish());

            List<BorrowingBooks> borrowingBooks = borrowingBooksMapper.selectList(
                new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, b.getBookId()));

            if (borrowingBooks == null || borrowingBooks.isEmpty()) {
                bookVo.setIsExist("可借");
            } else {
                bookVo.setIsExist("不可借");
            }
            bookVos.add(bookVo);
        }
        return bookVos;
    }

    @Override
    public Page<BookVo> findBooksByCategoryId(int categoryId, int pageNum) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Book> mpPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, 10);
        mpPage = bookMapper.selectPage(mpPage,
            new LambdaQueryWrapper<Book>().eq(Book::getBookCategory, categoryId));

        List<BookVo> bookVos = new LinkedList<>();
        for (Book b : mpPage.getRecords()) {
            BookVo bookVo = new BookVo();
            bookVo.setBookId(b.getBookId());
            bookVo.setBookName(b.getBookName());
            bookVo.setBookAuthor(b.getBookAuthor());
            bookVo.setBookPublish(b.getBookPublish());

            List<BorrowingBooks> borrowingBooks = borrowingBooksMapper.selectList(
                new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, b.getBookId()));

            if (borrowingBooks == null || borrowingBooks.isEmpty()) {
                bookVo.setIsExist("可借");
            } else {
                bookVo.setIsExist("不可借");
            }
            bookVos.add(bookVo);
        }

        Page<BookVo> page = new Page<>();
        page.setList(bookVos);
        page.setPageNum((int) mpPage.getCurrent());
        page.setPageSize((int) mpPage.getSize());
        page.setPageCount((int) mpPage.getPages());
        if (mpPage.getTotal() == 0) {
            page.setPageCount(1);
        }
        return page;
    }

    @Override
    public List<Book> findBooksByCategoryId(int categoryId) {
        return bookMapper.selectList(
            new LambdaQueryWrapper<Book>().eq(Book::getBookCategory, categoryId));
    }

    @Override
    public Page<BookVo> findBooksByKeyword(String keyword, int pageNum) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Book> mpPage =
            new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(pageNum, 10);
        mpPage = bookMapper.selectPage(mpPage,
            new LambdaQueryWrapper<Book>().like(Book::getBookName, keyword));

        List<BookVo> bookVos = new LinkedList<>();
        for (Book b : mpPage.getRecords()) {
            BookVo bookVo = new BookVo();
            bookVo.setBookId(b.getBookId());
            bookVo.setBookName(b.getBookName());
            bookVo.setBookAuthor(b.getBookAuthor());
            bookVo.setBookPublish(b.getBookPublish());

            List<BorrowingBooks> borrowingBooks = borrowingBooksMapper.selectList(
                new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, b.getBookId()));

            if (borrowingBooks == null || borrowingBooks.isEmpty()) {
                bookVo.setIsExist("可借");
            } else {
                bookVo.setIsExist("不可借");
            }
            bookVos.add(bookVo);
        }

        Page<BookVo> page = new Page<>();
        page.setList(bookVos);
        page.setPageNum((int) mpPage.getCurrent());
        page.setPageSize((int) mpPage.getSize());
        page.setPageCount((int) mpPage.getPages());
        if (mpPage.getTotal() == 0) {
            page.setPageCount(1);
        }
        return page;
    }

    @Override
    public boolean isBookBorrowed(int bookId) {
        List<BorrowingBooks> list = borrowingBooksMapper.selectList(
            new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, bookId));
        return list != null && !list.isEmpty();
    }

    @Override
    public Book getBookDetailById(Integer bookId) {
        return bookMapper.selectById(bookId);
    }

    @Override
    public int getBorrowedCountByBookId(Integer bookId) {
        List<BorrowingBooks> list = borrowingBooksMapper.selectList(
            new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getBookId, bookId));
        return (list != null) ? list.size() : 0;
    }

    @Override
    public List<Book> findByIsbn(String isbn) {
        return bookMapper.selectList(
            new LambdaQueryWrapper<Book>().eq(Book::getIsbn, isbn));
    }

    @Override
    public Map<String, Object> refreshRecommendCache() {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();

        try {
            // 1. 全量查询所有图书（不过滤借出，缓存所有书）
            List<Book> allBooks = bookMapper.selectList(null);
            log.info("全量查询图书: {} 本", allBooks.size());

            // 2. 全量查询当前被借出的 bookId 集合（仅用于统计展示）
            List<BorrowingBooks> borrowingList = borrowingBooksMapper.selectList(null);
            Set<Integer> borrowedBookIds = new HashSet<>();
            for (BorrowingBooks bb : borrowingList) {
                borrowedBookIds.add(bb.getBookId());
            }
            log.info("当前借出中的图书: {} 本", borrowedBookIds.size());

            // 3. 按 bookCategory 分组（所有书都入缓存，借出过滤在查询时按用户处理）
            Map<Integer, Set<String>> categoryMap = new HashMap<>();
            for (Book book : allBooks) {
                Integer categoryId = book.getBookCategory();
                if (categoryId == null) {
                    continue;
                }
                categoryMap.computeIfAbsent(categoryId, k -> new HashSet<>())
                        .add(String.valueOf(book.getBookId()));
            }
            log.info("图书分布在 {} 个分类", categoryMap.size());

            // 4. 清理旧的推荐缓存 key
            Set<String> oldKeys = stringRedisTemplate.keys(RECOMMEND_KEY_PREFIX + "*");
            if (oldKeys != null && !oldKeys.isEmpty()) {
                stringRedisTemplate.delete(oldKeys);
                log.info("清理旧缓存 key: {} 个", oldKeys.size());
            }

            // 5. 批量写入新的 SET（每个分类一个 key）
            int writtenKeys = 0;
            for (Map.Entry<Integer, Set<String>> entry : categoryMap.entrySet()) {
                String key = RECOMMEND_KEY_PREFIX + entry.getKey();
                Set<String> bookIds = entry.getValue();
                if (!bookIds.isEmpty()) {
                    stringRedisTemplate.opsForSet().add(key, bookIds.toArray(new String[0]));
                    writtenKeys++;
                }
            }

            long elapsed = System.currentTimeMillis() - startTime;
            log.info("推荐缓存刷新完成: {} 个分类, {} 本图书, 耗时 {} ms",
                    writtenKeys, allBooks.size(), elapsed);

            result.put("success", true);
            result.put("totalBooks", allBooks.size());
            result.put("categoryCount", writtenKeys);
            result.put("elapsedMs", elapsed);

        } catch (Exception e) {
            log.error("刷新推荐缓存失败", e);
            result.put("success", false);
            result.put("msg", "刷新失败: " + e.getMessage());
        }

        return result;
    }

    @Override
    public List<Book> getRecommendBooks(int categoryId, int excludeBookId, int limit, Integer userId) {
        // 1. 查出当前书的书名，用于按书名排除（同一本书可能有多条记录不同ID）
        String excludeBookName = null;
        Book excludeBook = bookMapper.selectById(excludeBookId);
        if (excludeBook != null) {
            excludeBookName = excludeBook.getBookName();
        }

        // 2. 如果传了 userId，先查出该用户正在借阅的 bookId 集合
        Set<Integer> userBorrowedBookIds = new HashSet<>();
        if (userId != null) {
            List<BorrowingBooks> userBorrowing = borrowingBooksMapper.selectList(
                    new LambdaQueryWrapper<BorrowingBooks>().eq(BorrowingBooks::getUserId, userId));
            for (BorrowingBooks bb : userBorrowing) {
                userBorrowedBookIds.add(bb.getBookId());
            }
        }

        // 3. 优先从 Redis 读取
        try {
            String key = RECOMMEND_KEY_PREFIX + categoryId;

            Boolean hasKey = stringRedisTemplate.hasKey(key);
            if (Boolean.FALSE.equals(hasKey)) {
                log.debug("Redis key {} 不存在，fallback 到 MySQL", key);
                return getRecommendBooksFromMysql(categoryId, excludeBookName, limit, userBorrowedBookIds);
            }

            // 随机取较多候选
            List<String> candidates = stringRedisTemplate.opsForSet()
                    .randomMembers(key, Math.min(limit * 10, 100));

            if (candidates == null || candidates.isEmpty()) {
                return Collections.emptyList();
            }

            // 排除同名书 + 排除该用户正在借的书
            List<Book> result = new ArrayList<>();
            Set<String> seenNames = new HashSet<>();  // 按书名去重
            for (String idStr : candidates) {
                if (result.size() >= limit) {
                    break;
                }
                int candidateId;
                try {
                    candidateId = Integer.parseInt(idStr);
                } catch (NumberFormatException e) {
                    continue;
                }
                // 排除该用户正在借阅的书
                if (userBorrowedBookIds.contains(candidateId)) {
                    continue;
                }
                Book book = bookMapper.selectById(candidateId);
                if (book == null) {
                    continue;
                }
                // 排除与当前书同名的书（同一本书的不同记录）
                if (excludeBookName != null && excludeBookName.equals(book.getBookName())) {
                    continue;
                }
                // 按书名去重，不推荐同名书
                if (!seenNames.add(book.getBookName())) {
                    continue;
                }
                result.add(book);
            }

            return result;

        } catch (Exception e) {
            log.warn("Redis 读取推荐失败，fallback 到 MySQL: {}", e.getMessage());
            return getRecommendBooksFromMysql(categoryId, excludeBookName, limit, userBorrowedBookIds);
        }
    }

    /**
     * MySQL 直接查询推荐（Redis 不可用时的 fallback）
     * 按书名过滤，同时过滤用户正在借阅的书
     */
    private List<Book> getRecommendBooksFromMysql(int categoryId, String excludeBookName, int limit,
                                                   Set<Integer> userBorrowedBookIds) {
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Book> mpPage =
                new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(1, limit * 10);
        mpPage = bookMapper.selectPage(mpPage,
                new LambdaQueryWrapper<Book>()
                        .eq(Book::getBookCategory, categoryId));

        List<Book> result = new ArrayList<>();
        Set<String> seenNames = new HashSet<>();
        for (Book book : mpPage.getRecords()) {
            if (result.size() >= limit) {
                break;
            }
            if (userBorrowedBookIds.contains(book.getBookId())) {
                continue;
            }
            // 排除与当前书同名的书
            if (excludeBookName != null && excludeBookName.equals(book.getBookName())) {
                continue;
            }
            // 按书名去重
            if (!seenNames.add(book.getBookName())) {
                continue;
            }
            result.add(book);
        }
        return result;
    }
}
