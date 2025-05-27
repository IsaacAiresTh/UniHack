package com.unihack.unihack.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptions extends ResponseEntityExceptionHandler {

    // Handler para quando um usuário já está cadastrado (poderia ser CONFLICT)
    @ExceptionHandler(UsuarioJaCadastrado.class)
    public ResponseEntity<Object> handleUsuarioJaCadastado(UsuarioJaCadastrado ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.CONFLICT.value()); // 409 Conflict é mais semântico
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // Handler para campos inválidos genéricos (se lançar manualmente)
    @ExceptionHandler(CampoInvalido.class)
    public ResponseEntity<Object> handleCampoInvalido(CampoInvalido ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        body.put("error", "Bad Request");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    // Handler para UserNotFoundException
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Object> handleUserNotFoundException(UserNotFoundException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.NOT_FOUND.value()); // 404 Not Found
        body.put("error", "Not Found");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.NOT_FOUND);
    }

    // Handler para UsernameAlreadyExistsException
    @ExceptionHandler(UsernameAlreadyExistsException.class)
    public ResponseEntity<Object> handleUsernameAlreadyExistsException(UsernameAlreadyExistsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.CONFLICT.value()); // 409 Conflict
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // Handler para MatriculaAlreadyExistsException
    @ExceptionHandler(MatriculaAlreadyExistsException.class)
    public ResponseEntity<Object> handleMatriculaAlreadyExistsException(MatriculaAlreadyExistsException ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.CONFLICT.value()); // 409 Conflict
        body.put("error", "Conflict");
        body.put("message", ex.getMessage());
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.CONFLICT);
    }

    // Sobrescrevendo o método para tratar MethodArgumentNotValidException
    // Este método é da classe ResponseEntityExceptionHandler que estendemos
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex,
            org.springframework.http.HttpHeaders headers,
            HttpStatusCode status,
            WebRequest request) {

        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status.value());
        body.put("error", "Validation Error");

        // Coleta todas as mensagens de erro de validação
        String errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));

        body.put("message", "Erro de validação: " + errors);
        body.put("path", request.getDescription(false).replace("uri=", ""));

        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    //pode adicionar um handler genérico para qualquer outra exceção não tratada
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGenericException(Exception ex, WebRequest request) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        body.put("error", "Internal Server Error");
        body.put("message", "Ocorreu um erro inesperado no servidor.");
        // Em ambiente de desenvolvimento, você pode querer logar ex.getMessage() ou a stacktrace
        // Em produção, evite expor detalhes internos da exceção.
        // log.error("Erro inesperado:", ex); // Se tiver um logger configurado
        body.put("path", request.getDescription(false).replace("uri=", ""));
        return new ResponseEntity<>(body, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}