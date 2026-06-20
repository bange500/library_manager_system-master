package com.zbw.service;

import com.zbw.domain.Reservation;

import java.util.List;

public interface IReservationService {

    /** 预约图书，返回 null=成功，否则返回失败原因 */
    String reserveBook(int userId, int bookId);

    /** 用户预约列表（含书名） */
    List<Reservation> getUserReservations(int userId);

    /** 待借阅通知数 */
    int getNotificationCount(int userId);

    /** 图书归还后，通知第一个排队用户 */
    void processReturnNotification(int bookId);

    /** 取消预约 */
    boolean cancelReservation(int id, int userId);

    /** 某书当前排队人数 */
    int getQueueCount(int bookId);

    /** 某用户是否已预约某书 */
    boolean hasUserReserved(int userId, int bookId);
}
