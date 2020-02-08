package codingdojo;

import codingdojo.model.*;

import java.util.Optional;

public class CustomerSync {

    private final CustomerDataAccess customerDataAccess;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.customerDataAccess = db;
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        return externalCustomer.getCustomerType() == CustomerType.COMPANY ? syncCompany(externalCustomer) : syncPerson(externalCustomer);
    }

    public boolean syncPerson(ExternalCustomer externalCustomer) {
        //load one customer
        Optional<Customer> loadCustomer = loadPerson(externalCustomer);

        if (!loadCustomer.isPresent()) {
            Customer customer = create(externalCustomer);
            prepare(externalCustomer, customer);
            updateRelations(customer);
            updateCustomer(customer);
        } else {
            prepare(externalCustomer, loadCustomer.get());
            updateRelations(loadCustomer.get());
            updateCustomer(loadCustomer.get());
        }

        return !loadCustomer.isPresent();
    }

    public boolean syncCompany(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = loadCompany(externalCustomer);
        Customer customer = customerMatches.getCustomer();

        boolean created = false;

        if (customer == null) {
            customer = create(externalCustomer);
            created = true;
        }
        customer.setCompanyNumber(externalCustomer.getCompanyNumber());

        prepare(externalCustomer, customer);
        updateRelations(customer);
        updateCustomer(customer);

        for (Customer duplicate : customerMatches.getDuplicates()) {
            duplicate.setName(externalCustomer.getName()); // in prepare
            updateCustomer(duplicate); // in update
        }

        return created;
    }

    private Customer create(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        createCustomer(customer);
        return customer;
    }

    private void prepare(ExternalCustomer externalCustomer, Customer customer) {
        customer.setName(externalCustomer.getName());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        customer.setAddress(externalCustomer.getAddress());

        for (ShoppingList consumerShoppingList : externalCustomer.getShoppingLists()) {
            customer.addShoppingList(consumerShoppingList);
        }
    }

    private void updateRelations(Customer customer) {
        this.customerDataAccess.updateShoppingLists(customer);
    }

    private void updateCustomer(Customer customer) {
        this.customerDataAccess.updateCustomerRecord(customer);
    }

    private void createCustomer(Customer customer) {
        this.customerDataAccess.createCustomerRecord(customer);
    }

    public CustomerMatches loadCompany(ExternalCustomer externalCustomer) {
        return customerDataAccess.loadCompanyCustomer(externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());
    }

    public Optional<Customer> loadPerson(ExternalCustomer externalCustomer) {
        return customerDataAccess.loadPersonCustomer(externalCustomer.getExternalId());
    }
}
