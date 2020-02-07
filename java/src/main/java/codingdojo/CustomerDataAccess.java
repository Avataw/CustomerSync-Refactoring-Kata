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
        CustomerMatches matches = new CustomerMatches();
        Customer matchByExternalId = this.customerDataLayer.findByExternalId(externalId);

        if (matchByExternalId != null) {
            if (matchByExternalId.getCompanyNumber() == null) {
                throw new ConflictException("Existing customer for externalCustomer " + externalId + " already exists and is not a company");
            }

            if (!companyNumber.equals(matchByExternalId.getCompanyNumber())) {
                matchByExternalId.setMasterExternalId(null);
                matches.addDuplicate(matchByExternalId);
                matches.setCustomer(null);
            } else {
                matches.setCustomer(matchByExternalId);
            }

            Customer matchByMasterId = this.customerDataLayer.findByMasterExternalId(externalId);
            if (matchByMasterId != null) matches.addDuplicate(matchByMasterId);
        } else {
            Customer matchByCompanyNumber = this.customerDataLayer.findByCompanyNumber(companyNumber);
            if (matchByCompanyNumber != null) {

                matches.setCustomer(matchByCompanyNumber);
                String customerExternalId = matchByCompanyNumber.getExternalId();
                if (customerExternalId != null && !externalId.equals(customerExternalId)) {
                    throw new ConflictException("Existing customer for externalCustomer " + companyNumber + " doesn't match external id " + externalId + " instead found " + customerExternalId);
                }
                matchByCompanyNumber.setExternalId(externalId);
                matchByCompanyNumber.setMasterExternalId(externalId);
            }
        }

        return matches;
    }

    public Customer loadPersonCustomer(String externalId) {
        Customer personCustomer = this.customerDataLayer.findByExternalId(externalId);

        if (personCustomer != null && personCustomer.getCompanyNumber() != null){
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
