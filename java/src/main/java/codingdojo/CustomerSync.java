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
        Person person = loadCustomer.isEmpty()
                ? create((Customer) externalPerson)
                : loadCustomer.get();

        person.setName(externalPerson.getName());
        person.setPreferredStore(externalPerson.getPreferredStore());
        person.setAddress(externalPerson.getAddress());

        for (ShoppingList consumerShoppingList : externalPerson.getShoppingLists()) {
            person.addShoppingList(consumerShoppingList);
        }

        updateCustomer((Customer) person);
        return loadCustomer.isEmpty();
    }

    public boolean syncCompany(Company externalCompany) {
        List<Duplicate> duplicates = customerDataAccess.checkForDuplicates(externalCompany.getExternalId(), externalCompany.getCompanyNumber());
        updateDuplicates(duplicates, externalCompany.getName());

        Optional<Company> loadCustomer = loadCompany(externalCompany);
        Company company = loadCustomer.isEmpty()
                ? create((Customer) externalCompany)
                : loadCustomer.get();

        company.setName(externalCompany.getName());
        company.setPreferredStore(externalCompany.getPreferredStore());
        company.setAddress(externalCompany.getAddress());

        for (ShoppingList consumerShoppingList : externalCompany.getShoppingLists()) {
            company.addShoppingList(consumerShoppingList);
        }
        company.setCompanyNumber(externalCompany.getCompanyNumber());

        updateCustomer((Customer) company);
        return loadCustomer.isEmpty();
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

    public Optional<Company> loadCompany(Company externalCustomer) {
        return customerDataAccess.loadCompanyCustomer(externalCustomer.getExternalId(), externalCustomer.getCompanyNumber());
    }

    public Optional<Person> loadPerson(Person externalPerson) {
        return customerDataAccess.loadPersonCustomer(externalPerson.getExternalId());
    }
}
