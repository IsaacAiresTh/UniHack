package com.unihack.unihack.exceptions;

// Add this import if the class exists in the same package

import java.net.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptions {

    @ExceptionHandler(UsuarioJaCadastrado.class)
    public ResponseEntity<String> handleUsuarioJaCadastado(UsuarioJaCadastrado ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    @ExceptionHandler(CampoInvalido.class)
    public ResponseEntity<String> handleCampoInvalido(CampoInvalido ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
            HttpHeaders headers,
            HttpStatusCode status, 
            WebRequest request) {
        String errorMessage = "Erro de validação: ";
        if (ex.getBindingResult().getFieldError() != null) {
            errorMessage += ex.getBindingResult().getFieldError().getDefaultMessage();
        } else {
            errorMessage += "Erro desconhecido.";
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }
}
