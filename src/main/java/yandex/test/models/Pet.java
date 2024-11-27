package yandex.test.models;

import java.util.List;
import lombok.Getter;
import lombok.Setter;


@Setter
@Getter
public class Pet {
    private long id;
    private Category category;
    private String name;
    private List<String> photoUrls;
    private List<Tag> tags;
    private String status;

}