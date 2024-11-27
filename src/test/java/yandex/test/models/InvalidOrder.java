package yandex.test.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvalidOrder {
    private long id;
    private long petId;
    private String quantity;
    private String shipDate;
    private String status;
    private boolean complete;

    public InvalidOrder(Order order) {
        this.id = order.getId();
        this.petId = order.getPetId();
        this.quantity = "gfdg";
        this.shipDate = order.getShipDate();
        this.status = order.getStatus();
        this.complete = order.isComplete();
    }
}
