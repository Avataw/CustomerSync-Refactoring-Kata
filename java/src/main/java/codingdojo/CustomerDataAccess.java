package codingdojo;

import codingdojo.model.Customer;
import codingdojo.model.CustomerMatches;
import codingdojo.model.CustomerType;
import codingdojo.model.ShoppingList;

import java.util.Optional;

public class CustomerDataAccess {

    private final CustomerDataLayer customerDataLayer;

    public CustomerDataAccess(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public CustomerMatches loadCompanyCustomer(String externalId, String companyNumber) {
        CustomerMatches matches = new CustomerMatches();

        Customer customer = this.customerDataLayer.findByExternalId(externalId);

        if(customer != null) {
            byExternalId(externalId, companyNumber, matches, customer);
        } else {
            customer = this.customerDataLayer.findByCompanyNumber(companyNumber);
            byCompanyNumber(externalId, companyNumber, matches, customer);
        }

        Customer matchByMasterId = this.customerDataLayer.findByMasterExternalId(externalId);
        if (matchByMasterId != null) matches.addDuplicate(matchByMasterId);

        return matches;

    }

    private void checkForDuplicate(String companyNumber, Customer customer, CustomerMatches matches) {
        if (customer.getCompanyNumber().equals(companyNumber)) {
            matches.setCustomer(customer);
        } else {
            matches.addDuplicate(customer);
        }
    }

    private void byExternalId(String externalId, String companyNumber, CustomerMatches matches, Customer customer) {
        if (customer.getCustomerType() == CustomerType.PERSON)
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        checkForDuplicate(companyNumber, customer, matches);
    }

    private void byCompanyNumber(String externalId, String companyNumber, CustomerMatches matches, Customer customer) {
        if (customer == null) return;
        matches.setCustomer(customer);

        String customerExternalId = customer.getExternalId();
        if (customerExternalId != null && !externalId.equals(customerExternalId)) {
            throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId);
        }
        customer.setExternalId(externalId);
        customer.setMasterExternalId(externalId);
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
