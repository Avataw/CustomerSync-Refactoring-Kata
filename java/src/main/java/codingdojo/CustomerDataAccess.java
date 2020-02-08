package codingdojo;

import codingdojo.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomerDataAccess {

    private final CustomerDataLayer customerDataLayer;

    public CustomerDataAccess(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public Optional<Customer> loadCompanyById(String externalId) {
        return Optional.ofNullable(this.customerDataLayer.findByExternalId(externalId));
    }

    public Optional<Customer> loadCompanyByNumber(String companyNumber) {
        return Optional.ofNullable(this.customerDataLayer.findByCompanyNumber(companyNumber));
    }

    public List<Customer> loadDuplicates(String externalId, String companyNumber) {
        return null;
    }

    public CustomerMatches loadCompanyCustomer(String externalId, String companyNumber) {
        CustomerMatches matches = new CustomerMatches();

        Customer customer = this.customerDataLayer.findByExternalId(externalId);
        if (customer != null) {
            matches.setCustomer(byExternalId(externalId, companyNumber, customer));

        } else {
            customer = this.customerDataLayer.findByCompanyNumber(companyNumber);
            matches.setCustomer(byCompanyNumber(externalId, companyNumber, customer));
        }

        return matches;
    }

    public List<Customer> checkForDuplicates(String externalId, String companyNumber) {
        List<Customer> duplicates = new ArrayList<>();

        Customer matchByMasterId = this.customerDataLayer.findByMasterExternalId(externalId);
        Customer matchById = this.customerDataLayer.findByExternalId(externalId);
        if (matchByMasterId != null) duplicates.add(matchByMasterId);

        if (matchById != null) {
            if (matchById.getCompanyNumber() != null) {
                if (!matchById.getCompanyNumber().equals(companyNumber)) {
                    duplicates.add(matchById);
                }
            }
        }
        return duplicates;
    }

    private Customer byExternalId(String externalId, String companyNumber, Customer customer) {
        if (customer.getCustomerType() == CustomerType.PERSON)
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        if(!companyNumber.equals(customer.getCompanyNumber())) return null;

        return customer;
    }

    private Customer byCompanyNumber(String externalId, String companyNumber, Customer customer) {
        if (customer == null) return null;

        String customerExternalId = customer.getExternalId();
        if (customerExternalId != null && !externalId.equals(customerExternalId)) {
            throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId);
        }
        customer.setExternalId(externalId);
        customer.setMasterExternalId(externalId);
        return customer;
    }


    public Optional<Customer> loadPersonCustomer(String externalId) {
        Customer personCustomer = this.customerDataLayer.findByExternalId(externalId);
        if (personCustomer != null && personCustomer.getCustomerType() == CustomerType.COMPANY)
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a person");

        return Optional.ofNullable(personCustomer);
    }

    public void updateCustomerRecord(Customer customer) {
        customerDataLayer.updateCustomerRecord(customer);
    }

    public void createCustomerRecord(Customer customer) {
        customerDataLayer.createCustomerRecord(customer);
    }

    private void updateShoppingList(Customer customer, ShoppingList consumerShoppingList) {
        customerDataLayer.updateShoppingList(consumerShoppingList);
        customerDataLayer.updateCustomerRecord(customer);
    }

    public void updateShoppingLists(Customer customer) {
        for (ShoppingList consumerShoppingList : customer.getShoppingLists()) {
            updateShoppingList(customer, consumerShoppingList);
        }
    }
}
