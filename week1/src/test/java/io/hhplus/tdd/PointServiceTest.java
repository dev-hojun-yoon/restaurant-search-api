package io.hhplus.tdd;

import io.hhplus.tdd.database.PointHistoryTable;
import io.hhplus.tdd.database.UserPointTable;
import io.hhplus.tdd.point.PointService;
import io.hhplus.tdd.point.UserPoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


//@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    private UserPointTable userPointTable;
    private PointHistoryTable pointHistoryTable;
    private PointService pointService;

    @BeforeEach
    void setUp() {
        userPointTable = new UserPointTable();
        pointHistoryTable = new PointHistoryTable();
        pointService = new PointService(userPointTable, pointHistoryTable);
    }


    @Test
    @DisplayName("포인트 충전 기능 테스트")
    void chargePoint() {
        long userId = 1234;
        UserPoint result = userPointTable.insertOrUpdate(userId, 1000);
        System.out.println("초기 포인트: " + userPointTable.selectById(userId).point());
        result = pointService.insertOrUpdate(userId, 2000);

        assertEquals(3000, result.point());
        assertNotNull(pointHistoryTable.selectAllByUserId(userId));
    }
}
