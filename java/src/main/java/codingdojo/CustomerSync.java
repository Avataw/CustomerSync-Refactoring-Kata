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

    public boolean syncPerson(Customer externalCustomer) {
        Optional<Customer> loadCustomer = loadPerson(externalCustomer);
        Customer customer = getOrCreate(externalCustomer, loadCustomer);

        updateCustomer(customer);
        return loadCustomer.isEmpty();
    }

    public boolean syncCompany(Customer externalCustomer) {
        handleDuplicates(externalCustomer);

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

    private void handleDuplicates(Customer externalCustomer) {
        List<Customer> duplicates = customerDataAccess.checkForDuplicates(externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());
        for (Customer duplicate : duplicates) {
            duplicate.setName(externalCustomer.getName()); // in prepare
            updateCustomer(duplicate); // in update
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

    public Optional<Customer> loadPerson(Customer externalCustomer) {
        return customerDataAccess.loadPersonCustomer(externalCustomer.getExternalId());
    }
}
