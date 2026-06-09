package com.zbw.config;

import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.Map;

/**
 * 全局异常处理器 — 统一处理后端校验失败等异常
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 处理 @Valid 校验失败异常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public Map<String, Object> handleValidationException(MethodArgumentNotValidException ex) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);

        StringBuilder sb = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            if (sb.length() > 0) {
                sb.append("；");
            }
            sb.append(error.getDefaultMessage());
        });

        result.put("msg", sb.toString());
        return result;
    }
}
