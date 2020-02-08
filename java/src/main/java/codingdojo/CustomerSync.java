package codingdojo;

import codingdojo.model.*;

import java.util.List;
import java.util.Optional;

public class CustomerSync {

    private final CustomerDataAccess customerDataAccess;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.customerDataAccess = db;
    }

    public boolean syncWithDataLayer(Customer externalCustomer) {
        return externalCustomer.getCustomerType() == CustomerType.COMPANY
                ? syncCompany(externalCustomer)
                : syncPerson(externalCustomer);
    }

    public boolean syncPerson(Person externalPerson) {
        Optional<Person> loadCustomer = loadPerson(externalPerson);
        Person customer = loadCustomer.isEmpty()
                ? create((Customer) externalPerson)
                : loadCustomer.get();

        customer.setName(externalPerson.getName());
        customer.setPreferredStore(externalPerson.getPreferredStore());
        customer.setAddress(externalPerson.getAddress());

        for (ShoppingList consumerShoppingList : externalPerson.getShoppingLists()) {
            customer.addShoppingList(consumerShoppingList);
        }

        updateCustomer((Customer) customer);
        return loadCustomer.isEmpty();
    }

    public boolean syncCompany(Customer externalCustomer) {
        List<Duplicate> duplicates = customerDataAccess.checkForDuplicates(externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());
        updateDuplicates(duplicates, externalCustomer.getName());

        Optional<Customer> loadCustomer = loadCompany(externalCustomer);
        Customer customer = getOrCreate(externalCustomer, loadCustomer);

        customer.setCompanyNumber(externalCustomer.getCompanyNumber());

        updateCustomer(customer);
        return loadCustomer.isEmpty();
    }

    private Customer getOrCreate(Customer externalCustomer, Optional<Customer> loadCustomer) {
        Customer customer;

        customer = loadCustomer.isEmpty()
                ? create(externalCustomer)
                : loadCustomer.get();

        prepare(externalCustomer, customer);
        return customer;
    }

    private void updateDuplicates(List<Duplicate> duplicates, String name) {
        for (Duplicate duplicate : duplicates) {
            duplicate.setName(name); // in prepare
        }
    }


    private Customer create(Customer externalCustomer) {
        Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        createCustomer(customer);
        return customer;
    }

    private void prepare(Customer externalCustomer, Customer customer) {
        customer.setName(externalCustomer.getName());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        customer.setAddress(externalCustomer.getAddress());

        for (ShoppingList consumerShoppingList : externalCustomer.getShoppingLists()) {
            customer.addShoppingList(consumerShoppingList);
        }
    }

    private void updateCustomer(Customer customer) {
        this.customerDataAccess.updateShoppingLists(customer);
        this.customerDataAccess.updateCustomerRecord(customer);
    }

    private void createCustomer(Customer customer) {
        this.customerDataAccess.createCustomerRecord(customer);
    }

    public Optional<Customer> loadCompany(Customer externalCustomer) {
        return customerDataAccess.loadCompanyCustomer(externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());
    }

    public Optional<Person> loadPerson(Person externalCustomer) {
        return customerDataAccess.loadPersonCustomer(externalCustomer.getExternalId());
    }
}
