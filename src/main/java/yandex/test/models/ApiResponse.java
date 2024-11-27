package yandex.test.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ApiResponse {
    private int code;
    private String type;
    private String message;
}