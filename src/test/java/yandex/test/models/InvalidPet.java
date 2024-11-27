package yandex.test.models;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class InvalidPet {
    private String id;
    private Category category;
    private String name;
    private List<String> photoUrls;
    private List<Tag> tags;
    private String status;

    public InvalidPet(Pet pet) {
        this.id = "";
        this.category = pet.getCategory();
        this.name = pet.getName();
        this.photoUrls = pet.getPhotoUrls();
        this.tags = pet.getTags();
        this.status = "invalid";
    }
}