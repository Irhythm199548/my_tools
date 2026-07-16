package com.workbuddy.service;

import com.workbuddy.utils.AuxUtils;
import lombok.RequiredArgsConstructor;
import org.quartz.CronExpression;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Cron 表达式 Service
 * 使用 Quartz 的 CronExpression 做合法性校验与解释（含后续执行时间）。
 */
@Service
@RequiredArgsConstructor
public class CronService {

    private final OperationLogService logService;

    public Map<String, Object> validate(String expression) {
        logService.record("cron.validate", "辅助工具");
        Map<String, Object> result = new HashMap<>();
        if (expression == null || expression.trim().isEmpty()) {
            result.put("valid", false);
            result.put("message", "表达式不能为空");
            return result;
        }
        boolean valid = CronExpression.isValidExpression(expression);
        result.put("valid", valid);
        if (!valid) {
            result.put("message", "表达式不合法");
            return result;
        }
        try {
            CronExpression ce = new CronExpression(expression);
            result.put("summary", ce.getExpressionSummary());
            List<String> nextFires = new ArrayList<>();
            Date d = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < 5; i++) {
                Date next = ce.getNextValidTimeAfter(d);
                if (next == null) break;
                nextFires.add(sdf.format(next));
                d = next;
            }
            result.put("nextFireTimes", nextFires);
            result.put("message", "表达式合法");
        } catch (Exception e) {
            result.put("valid", false);
            result.put("message", "解析失败: " + e.getMessage());
        }
        return result;
    }
}
