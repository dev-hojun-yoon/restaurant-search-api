package io.hhplus.tdd.point;

import io.hhplus.tdd.database.UserPointTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/point")
public class PointController {

    private static final Logger log = LoggerFactory.getLogger(PointController.class);
    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }


    /**
     * TODO - 특정 유저의 포인트를 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}")
    public UserPoint point(
            @PathVariable long id
    ) {
        return pointService.selectById(id);
    }

    /**
     * TODO - 특정 유저의 포인트 충전/이용 내역을 조회하는 기능을 작성해주세요.
     */
    @GetMapping("{id}/histories")
    public List<PointHistory> history(
            @PathVariable long id
    ) {
        return pointService.selectAllByUserId(id);
    }

    /**
     * TODO - 특정 유저의 포인트를 충전하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/charge")
    public UserPoint charge(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        UserPoint userPoint = pointService.selectById(id);
        long point = userPoint.point();
        long tmp_amount = point + amount;

        if (tmp_amount > 5000) {
            tmp_amount = 5000;
            System.out.println("최대 잔고 (5,000) 를 초과했습니다.");
        }
        userPoint = pointService.insertOrUpdate(id, tmp_amount);
        pointService.insert(id, amount, TransactionType.CHARGE, System.currentTimeMillis());
        return userPoint;
    }

    /**
     * TODO - 특정 유저의 포인트를 사용하는 기능을 작성해주세요.
     */
    @PatchMapping("{id}/use")
    public UserPoint use(
            @PathVariable long id,
            @RequestBody long amount
    ) {
        UserPoint userPoint = pointService.selectById(id);
        long point = userPoint.point();
        long tmp_amount = point - amount;

        if (tmp_amount < 0) {
            tmp_amount = point;
            System.out.println("잔고가 부족해 더이상 포인트를 사용할 수 없습니다.");
        }
        userPoint = pointService.insertOrUpdate(id, tmp_amount);
        pointService.insert(id, amount, TransactionType.USE, System.currentTimeMillis());
        return userPoint;
    }
}
