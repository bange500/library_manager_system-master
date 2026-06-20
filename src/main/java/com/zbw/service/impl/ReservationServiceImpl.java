package com.zbw.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zbw.domain.Reservation;
import com.zbw.mapper.ReservationMapper;
import com.zbw.service.IReservationService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ReservationServiceImpl implements IReservationService {

    @Resource
    private ReservationMapper reservationMapper;

    @Override
    public String reserveBook(int userId, int bookId) {
        // 检查是否已预约
        int count = reservationMapper.countUserReserved(userId, bookId);
        if (count > 0) {
            return "您已预约过该书，请勿重复预约";
        }

        Reservation r = new Reservation();
        r.setUserId(userId);
        r.setBookId(bookId);
        r.setStatus(0);
        r.setReserveTime(new Date());

        int n = reservationMapper.insert(r);
        return n > 0 ? null : "预约失败，请重试";
    }

    @Override
    public List<Reservation> getUserReservations(int userId) {
        return reservationMapper.selectReservationsWithBook(userId);
    }

    @Override
    public int getNotificationCount(int userId) {
        List<Reservation> list = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getUserId, userId)
                        .eq(Reservation::getStatus, 1));
        return list == null ? 0 : list.size();
    }

    @Override
    public void processReturnNotification(int bookId) {
        // 找到该书第一个排队用户
        List<Reservation> list = reservationMapper.selectList(
                new LambdaQueryWrapper<Reservation>()
                        .eq(Reservation::getBookId, bookId)
                        .eq(Reservation::getStatus, 0)
                        .orderByAsc(Reservation::getReserveTime)
                        .last("LIMIT 1"));

        if (list != null && !list.isEmpty()) {
            Reservation r = list.get(0);
            r.setStatus(1);
            r.setNotifyTime(new Date());
            reservationMapper.updateById(r);
        }
    }

    @Override
    public int getQueueCount(int bookId) {
        return reservationMapper.countQueueByBook(bookId);
    }

    @Override
    public boolean hasUserReserved(int userId, int bookId) {
        return reservationMapper.countUserReserved(userId, bookId) > 0;
    }

    @Override
    public boolean cancelReservation(int id, int userId) {
        Reservation r = reservationMapper.selectById(id);
        if (r == null || !r.getUserId().equals(userId)) {
            return false;
        }
        r.setStatus(3);
        return reservationMapper.updateById(r) > 0;
    }
}
