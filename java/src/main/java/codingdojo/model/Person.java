package codingdojo.model;

import java.util.List;

public interface Person {
    String getExternalId();

    String getName();

    String getPreferredStore();

    Address getAddress();

    void setName(String name);

    void setPreferredStore(String preferredStore);

    void setAddress(Address address);

    List<ShoppingList> getShoppingLists();

    void addShoppingList(ShoppingList consumerShoppingList);
}
