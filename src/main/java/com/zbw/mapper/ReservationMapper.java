package com.zbw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zbw.domain.Reservation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ReservationMapper extends BaseMapper<Reservation> {

    /** 三表 JOIN 查用户预约列表 */
    @Select("SELECT r.*, u.user_name, b.book_name " +
            "FROM reservation r " +
            "LEFT JOIN user u ON r.user_id = u.user_id " +
            "LEFT JOIN book b ON r.book_id = b.book_id " +
            "WHERE r.user_id = #{userId} " +
            "ORDER BY r.status ASC, r.reserve_time DESC")
    List<Reservation> selectReservationsWithBook(@Param("userId") int userId);

    /** 统计某书排队人数 */
    @Select("SELECT COUNT(*) FROM reservation WHERE book_id = #{bookId} AND status = 0")
    int countQueueByBook(@Param("bookId") int bookId);

    /** 某用户是否已预约某书（排队中或待借阅） */
    @Select("SELECT COUNT(*) FROM reservation WHERE user_id = #{userId} AND book_id = #{bookId} AND status IN (0,1)")
    int countUserReserved(@Param("userId") int userId, @Param("bookId") int bookId);
}
