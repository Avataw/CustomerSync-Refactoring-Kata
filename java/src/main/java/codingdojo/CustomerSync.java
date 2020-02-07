package codingdojo;

import codingdojo.model.*;

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
        Customer customer = loadPerson(externalCustomer);

        boolean created = false;
        if (customer == null) {
            customer = create(externalCustomer);
            created = true;
        }
        prepare(externalCustomer, customer);
        update(customer);
        return created;
    }

    public boolean syncCompany(ExternalCustomer externalCustomer) {
        CustomerMatches customerMatches = loadCompany(externalCustomer);
        Customer customer = customerMatches.getCustomer();

        boolean created = false;

        if (customer == null) {
            customer = create(externalCustomer);
            customer.setCompanyNumber(externalCustomer.getCompanyNumber());
            created = true;
        }

        for (Customer duplicate : customerMatches.getDuplicates()) {
            duplicate.setName(externalCustomer.getName()); // in prepare
            if (duplicate.getInternalId() == null) {
                createCustomer(duplicate); //in create
            } else {
                updateCustomer(duplicate); // in update
            }
        }

        prepare(externalCustomer, customer);
        update(customer);

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

        for (ShoppingList consumerShoppingList : externalCustomer.getShoppingLists()) {
            customer.addShoppingList(consumerShoppingList);
        }
    }

    private void update(Customer customer) {
        updateRelations(customer);
        updateCustomer(customer);
    }

    private void updateRelations(Customer customer) {
        this.customerDataAccess.updateShoppingLists(customer);
    }

    private void updateCustomer(Customer customer) {
        this.customerDataAccess.updateCustomerRecord(customer);
    }

    private Customer createCustomer(Customer customer) {
        return this.customerDataAccess.createCustomerRecord(customer);
    }

    public CustomerMatches loadCompany(ExternalCustomer externalCustomer) {
        return customerDataAccess.loadCompanyCustomer(externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());
    }

    public Customer loadPerson(ExternalCustomer externalCustomer) {
        return customerDataAccess.loadPersonCustomer(externalCustomer.getExternalId());
    }
}
