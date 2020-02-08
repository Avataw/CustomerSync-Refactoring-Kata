package codingdojo.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Customer implements Duplicate, Person, Company {
    private String externalId;
    private String masterExternalId;
    private Address address;
    private String preferredStore;
    private List<ShoppingList> shoppingLists = new ArrayList<>();
    private String internalId;
    private String name;
    private String companyNumber;

    public Customer(){

    }

    public Customer(String externalId){
        this.externalId = this.masterExternalId = externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public void setMasterExternalId(String masterExternalId) {
        this.masterExternalId = masterExternalId;
    }

    public String getMasterExternalId() {
        return masterExternalId;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Address getAddress() {
        return address;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPreferredStore(String preferredStore) {
        this.preferredStore = preferredStore;
    }

    public String getPreferredStore() {
        return preferredStore;
    }

    public CustomerType getCustomerType() {
        return companyNumber == null ? CustomerType.PERSON : CustomerType.COMPANY;
    }

    public List<ShoppingList> getShoppingLists() {
        return shoppingLists;
    }

    public void setShoppingLists(List<ShoppingList> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }

    public String getName() {
        return name;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public void addShoppingList(ShoppingList consumerShoppingList) {
        shoppingLists.add(consumerShoppingList);
    }
}
