package codingdojo;

import codingdojo.model.*;
import org.approvaltests.Approvals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class CustomerSyncTest {

    @Test
    public void syncCompanyByExternalId(){
        String externalId = "12345";

        Customer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void syncPrivatePersonByExternalId(){
        String externalId = "12345";

        Customer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = new Customer();
        customer.setInternalId("67576");
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void syncShoppingLists(){
        String externalId = "12345";

        Customer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        ArrayList<ShoppingList> shoppingLists = new ArrayList<>();
        shoppingLists.add(new ShoppingList("eyeliner", "blusher"));
        customer.setShoppingLists(shoppingLists);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void syncNewCompanyCustomer(){

        Customer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId("12345");

        FakeDatabase db = new FakeDatabase();
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void syncNewPrivateCustomer(){

        Customer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId("12345");

        FakeDatabase db = new FakeDatabase();
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void conflictExceptionWhenExistingCustomerIsPerson() {
        String externalId = "12345";

        Customer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = new Customer();
        customer.setInternalId("45435");
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    public void syncByExternalIdButCompanyNumbersConflict(){
        String externalId = "12345";

        Customer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setCompanyNumber("000-3234");

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertTrue(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }


    @Test
    public void syncByCompanyNumber(){
        String companyNumber = "12345";

        Customer externalCustomer = createExternalCompany();
        externalCustomer.setCompanyNumber(companyNumber);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setCompanyNumber(companyNumber);
        customer.addShoppingList(new ShoppingList("eyeliner", "mascara", "blue bombe eyeshadow"));

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }

    @Test
    public void syncByCompanyNumberWithConflictingExternalId(){
        String companyNumber = "12345";

        Customer externalCustomer = createExternalCompany();
        externalCustomer.setCompanyNumber(companyNumber);
        externalCustomer.setExternalId("45646");

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setCompanyNumber(companyNumber);
        customer.setExternalId("conflicting id");

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    public void conflictExceptionWhenExistingCustomerIsCompany() {
        String externalId = "12345";

        Customer externalCustomer = createExternalPrivatePerson();
        externalCustomer.setExternalId(externalId);

        Customer customer = new Customer();
        customer.setCompanyNumber("32423-342");
        customer.setInternalId("45435");
        customer.setExternalId(externalId);

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        Assertions.assertThrows(ConflictException.class, () -> {
            sut.syncWithDataLayer(externalCustomer);
        }, printAfterState(db, toAssert).toString());

        Approvals.verify(toAssert);
    }

    @Test
    public void syncCompanyByExternalIdWithNonMatchingMasterId(){
        String externalId = "12345";

        Customer externalCustomer = createExternalCompany();
        externalCustomer.setExternalId(externalId);

        Customer customer = createCustomerWithSameCompanyAs(externalCustomer);
        customer.setExternalId(externalId);
        customer.setName("company 1");

        Customer customer2 = new Customer();
        customer2.setCompanyNumber(externalCustomer.getCompanyNumber());
        customer2.setInternalId("45435234");
        customer2.setMasterExternalId(externalId);
        customer2.setName("company 2");

        FakeDatabase db = new FakeDatabase();
        db.addCustomer(customer);
        db.addCustomer(customer2);
        CustomerSync sut = new CustomerSync(db);

        StringBuilder toAssert = printBeforeState(externalCustomer, db);

        // ACT
        boolean created = sut.syncWithDataLayer(externalCustomer);

        assertFalse(created);
        printAfterState(db, toAssert);
        Approvals.verify(toAssert);
    }


    private Customer createExternalPrivatePerson() {
        Customer externalCustomer = new Customer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Joe Bloggs");
        externalCustomer.setAddress(new Address("123 main st", "Stockholm", "SE-123 45"));
        externalCustomer.setPreferredStore("Nordstan");
        externalCustomer.setShoppingLists(Arrays.asList(new ShoppingList("lipstick", "foundation")));
        return externalCustomer;
    }


    private Customer createExternalCompany() {
        Customer externalCustomer = new Customer();
        externalCustomer.setExternalId("12345");
        externalCustomer.setName("Acme Inc.");
        externalCustomer.setAddress(new Address("123 main st", "Helsingborg", "SE-123 45"));
        externalCustomer.setCompanyNumber("470813-8895");
        externalCustomer.setShoppingLists(Arrays.asList(new ShoppingList("lipstick", "blusher")));
        return externalCustomer;
    }

    private Customer createCustomerWithSameCompanyAs(Customer externalCustomer) {
        Customer customer = new Customer();
        customer.setCompanyNumber(externalCustomer.getCompanyNumber());
        customer.setInternalId("45435");
        return customer;
    }

    private StringBuilder printBeforeState(Customer externalCustomer, FakeDatabase db) {
        StringBuilder toAssert = new StringBuilder();
        toAssert.append("BEFORE:\n");
        toAssert.append(db.printContents());

        toAssert.append("\nSYNCING THIS:\n");
        toAssert.append(ExternalCustomerPrinter.print(externalCustomer, ""));
        return toAssert;
    }

    private StringBuilder printAfterState(FakeDatabase db, StringBuilder toAssert) {
        toAssert.append("\nAFTER:\n");
        toAssert.append(db.printContents());
        return toAssert;
    }
}
