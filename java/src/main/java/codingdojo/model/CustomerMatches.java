package codingdojo.model;

import java.util.ArrayList;
import java.util.Collection;

public class CustomerMatches {
    private Collection<Customer> duplicates = new ArrayList<>();
    private Customer customer;

    public Customer getCustomer() {
        return customer;
    }

    public void addDuplicate(Customer duplicate) {
        duplicates.add(duplicate);
    }

    public Collection<Customer> getDuplicates() {
        return duplicates;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }
}
