package codingdojo;

import codingdojo.model.Customer;
import codingdojo.model.CustomerMatches;
import codingdojo.model.CustomerType;
import codingdojo.model.ShoppingList;

public class CustomerDataAccess {

    private final CustomerDataLayer customerDataLayer;

    public CustomerDataAccess(CustomerDataLayer customerDataLayer) {
        this.customerDataLayer = customerDataLayer;
    }

    public CustomerMatches loadCompanyCustomer(String externalId, String companyNumber) {
        Customer matchByExternalId = this.customerDataLayer.findByExternalId(externalId);

        return matchByExternalId != null
                ? matchByExternalId(externalId, companyNumber, matchByExternalId)
                : matchByCompanyNumber(externalId, companyNumber);
    }

    private CustomerMatches matchByCompanyNumber(String externalId, String companyNumber) {
        CustomerMatches matches = new CustomerMatches();

        Customer matchByCompanyNumber = this.customerDataLayer.findByCompanyNumber(companyNumber);
        if (matchByCompanyNumber != null) {

            if (matchByCompanyNumber.getCustomerType() == CustomerType.PERSON) {
                throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
            }
            matches.setCustomer(matchByCompanyNumber);
            String customerExternalId = matchByCompanyNumber.getExternalId();
            if (customerExternalId != null && !externalId.equals(customerExternalId)) {
                throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId);
            }
            matchByCompanyNumber.setExternalId(externalId);
            matchByCompanyNumber.setMasterExternalId(externalId);
        }

        return matches;
    }

    private CustomerMatches matchByExternalId(String externalId, String companyNumber, Customer matchByExternalId) {
        CustomerMatches matches = new CustomerMatches();

        if (matchByExternalId.getCustomerType() == CustomerType.PERSON) {
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
        }

        if (!matchByExternalId.getCompanyNumber().equals(companyNumber)) {
            matches.addDuplicate(matchByExternalId);
        } else {
            matches.setCustomer(matchByExternalId);
        }

        Customer matchByMasterId = this.customerDataLayer.findByMasterExternalId(externalId);
        if (matchByMasterId != null) matches.addDuplicate(matchByMasterId);
        return matches;

    }


    public Customer loadPersonCustomer(String externalId) {
        Customer personCustomer = this.customerDataLayer.findByExternalId(externalId);

        if (personCustomer != null && personCustomer.getCustomerType() == CustomerType.COMPANY ){
            throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a person");
        }
        return personCustomer;
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
