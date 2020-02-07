package codingdojo;

import codingdojo.model.*;

import java.util.List;

public class CustomerSync {

    private final CustomerDataAccess customerDataAccess;

    public CustomerSync(CustomerDataLayer customerDataLayer) {
        this(new CustomerDataAccess(customerDataLayer));
    }

    public CustomerSync(CustomerDataAccess db) {
        this.customerDataAccess = db;
    }

    public boolean syncWithDataLayer(ExternalCustomer externalCustomer) {
        return externalCustomer.isCompany() ? syncCompany(externalCustomer) : syncPerson(externalCustomer);
    }

    public boolean syncPerson(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = loadPerson(externalCustomer);
        Customer customer = customerMatches.getCustomer();

        boolean created = false;
        if (customer == null) {
            customer = create(externalCustomer);
            created = true;
        }

        prepare(externalCustomer, customer);

        update(externalCustomer, customer);
        return created;
    }

    public boolean syncCompany(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = loadCompany(externalCustomer);
        Customer customer = customerMatches.getCustomer();

        boolean created = false;

        if (customer == null) {
            customer = create(externalCustomer);
            created = true;
        }

        if (customerMatches.hasDuplicates()) {
            for (Customer duplicate : customerMatches.getDuplicates()) {
                updateDuplicate(externalCustomer, duplicate);
            }
        }
        prepare(externalCustomer, customer);
        update(externalCustomer, customer);

        return created;
    }

    private Customer create(ExternalCustomer externalCustomer) {
        Customer customer = new Customer();
        customer.setExternalId(externalCustomer.getExternalId());
        customer.setMasterExternalId(externalCustomer.getExternalId());
        customer = createCustomer(customer);
        return customer;
    }

    private void prepare(ExternalCustomer externalCustomer, Customer customer) {
        customer.setName(externalCustomer.getName());
        customer.setPreferredStore(externalCustomer.getPreferredStore());
        customer.setAddress(externalCustomer.getPostalAddress());
        if (externalCustomer.isCompany()) {
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
            customer.setCustomerType(CustomerType.COMPANY);
        } else {
            customer.setCustomerType(CustomerType.PERSON);
        }
    }

    private void update(ExternalCustomer externalCustomer, Customer customer) {
        updateRelations(externalCustomer, customer);
        updateCustomer(customer);
    }

    private void updateRelations(ExternalCustomer externalCustomer, Customer customer) {
        List<ShoppingList> consumerShoppingLists = externalCustomer.getShoppingLists();
        for (ShoppingList consumerShoppingList : consumerShoppingLists) {
            this.customerDataAccess.updateShoppingList(customer, consumerShoppingList);
        }
    }

    private void updateCustomer(Customer customer) {
        this.customerDataAccess.updateCustomerRecord(customer);
    }

    private void updateDuplicate(ExternalCustomer externalCustomer, Customer duplicate) {
        duplicate.setName(externalCustomer.getName());
        if (duplicate.getInternalId() == null) {
            createCustomer(duplicate);
        } else {
            updateCustomer(duplicate);
        }
    }

    private Customer createCustomer(Customer customer) {
        return this.customerDataAccess.createCustomerRecord(customer);
    }

    public CustomerMatches loadCompany(ExternalCustomer externalCustomer) {
        return customerDataAccess.loadCompanyCustomer(externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());
    }

    public CustomerMatches loadPerson(ExternalCustomer externalCustomer) {
        return customerDataAccess.loadPersonCustomer(externalCustomer.getExternalId());
    }
}
