package codingdojo.model;

import java.util.List;

public interface Company {
    String getExternalId();

    String getCompanyNumber();

    String getName();

    String getPreferredStore();

    Address getAddress();

    void setName(String name);

    void setPreferredStore(String preferredStore);

    List<ShoppingList> getShoppingLists();

    void addShoppingList(ShoppingList consumerShoppingList);

    void setAddress(Address address);

    void setCompanyNumber(String companyNumber);
}
