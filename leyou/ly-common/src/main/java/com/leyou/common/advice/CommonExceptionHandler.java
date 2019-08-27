package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.LyException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class CommonExceptionHandler {
    @ExceptionHandler(LyException.class)
    public ResponseEntity<String> handlerException(LyException e){
        ExceptionEnum em = e.getExceptionEnum();
        return ResponseEntity.status(em.getCode()).body(em.getMsg());
    }
}
