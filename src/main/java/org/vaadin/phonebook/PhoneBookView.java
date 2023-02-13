package org.vaadin.phonebook;

import com.vaadin.componentfactory.enhancedcrud.BinderCrudEditor;
import com.vaadin.componentfactory.enhancedcrud.Crud;
import com.vaadin.componentfactory.enhancedcrud.CrudEditor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.confirmdialog.ConfirmDialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.Binder;
import com.vaadin.flow.data.validator.EmailValidator;
import com.vaadin.flow.function.SerializablePredicate;
import com.vaadin.flow.router.Route;

import java.time.LocalDateTime;
import java.util.*;


import org.vaadin.phonebook.entity.Contact;
import org.vaadin.phonebook.dataprovider.ContactDataProvider;

/**
 * The main view contains a text field for getting the user name and a button
 * that shows a greeting message in a notification.
 */
@SuppressWarnings("serial")
@Route("")
public class PhoneBookView extends VerticalLayout {

    Crud<Contact> crud;
    ContactDataProvider contactDataProvider;
    Binder<Contact> binder;
    TextField name = new TextField("Name");
    TextField phoneNumber = new TextField("Phone number");
    TextField email = new TextField("email");
    TextField street = new TextField("Street");
    TextField city = new TextField("City");
    TextField country = new TextField("Country");
    private String editPhoneNumber;
    private Boolean isAddContactClicked = false;

    private LocalDateTime lastUpdatedTimeFlag;
    private ConfirmDialog warnAlreadyUpdateddialog;
    private Boolean warnOnAlreadyUpdatedContact = true;

    public PhoneBookView() {
        initView();
    }

    private void initView() {
        crud = new Crud<>(Contact.class, createEditor());
        crud.setEditOnClick(true);
        crud.setSizeFull();
        setGridColumns();
        contactDataProvider = new ContactDataProvider(contactsMap);
        crud.setDataProvider(contactDataProvider);
        crud.addPreSaveListener(e -> {
            if (!isAddContactClicked && isContactAlreadyUpdated(e.getItem()) && warnOnAlreadyUpdatedContact) {
                createWarningDialogue();
                e.getSource().cancelSave();
            }
        });
        crud.addSaveListener(e -> {
            saveContact(e.getItem());
        });
        crud.addEditListener(e -> {
            editPhoneNumber = e.getItem().getPhoneNumber();
            isAddContactClicked = false;
            lastUpdatedTimeFlag = e.getItem().getLastUpdatedTime();
            validateFields();
        });
        crud.addDeleteListener(e -> {
            contactsMap.remove(e.getItem().getPhoneNumber());
        });
        crud.setToolbar(customAddContactButton());

        add(crud);
    }
    private Button customAddContactButton() {
        Button addContactButton = new Button("Add Contact", VaadinIcon.PLUS.create());
        addContactButton.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        addContactButton.addClickListener(e -> {
            isAddContactClicked = true;
            editPhoneNumber = null;
            crud.edit(new Contact(), Crud.EditMode.NEW_ITEM);
            validateFields();
        });
        return addContactButton;
    }
    private CrudEditor<Contact> createEditor() {

        FormLayout formLayout = new FormLayout(name, phoneNumber, email, street, city, country);
        binder = new Binder<>(Contact.class);
        binder.forField(name).asRequired().bind(Contact::getName, Contact::setName);
        binder.bind(phoneNumber, Contact::getPhoneNumber, Contact::setPhoneNumber);
        binder.bind(email, Contact::getEmail, Contact::setEmail);
        binder.bind(street, Contact::getStreet, Contact::setStreet);
        binder.bind(city, Contact::getCity, Contact::setCity);
        binder.bind(country, Contact::getCountry, Contact::setCountry);

        return new BinderCrudEditor<>(binder, formLayout);
    }
    private void saveContact(Contact contact) {
        contact.setLastUpdatedTime(LocalDateTime.now());
        if (contactsMap.containsValue(contact)) {
            contactsMap.remove(editPhoneNumber);
            contactsMap.put(contact.getPhoneNumber(), contact);
        } else {
            contactsMap.put(contact.getPhoneNumber(), contact);
        }
    }

    private void setGridColumns() {
        crud.getGrid().removeColumn(crud.getGrid().getColumnByKey("lastUpdatedTime"));
        crud.getGrid().setColumnOrder(crud.getGrid().getColumnByKey("name"), crud.getGrid().getColumnByKey("phoneNumber"), crud.getGrid().getColumnByKey("email"), crud.getGrid().getColumnByKey("street"), crud.getGrid().getColumnByKey("city"), crud.getGrid().getColumnByKey("country"));
    }

    private Boolean isContactAlreadyUpdated(Contact contact) {
        return contact.getLastUpdatedTime().equals(lastUpdatedTimeFlag) ? false : true;
    }

    private void validateFields() {
        SerializablePredicate<String> alreadyExist = value -> !(contactsMap.containsKey(phoneNumber.getValue())&&(isAddContactClicked || !phoneNumber.getValue().equals(editPhoneNumber)));
        Binder.Binding<Contact, String> phoneBinding = binder.forField(phoneNumber).asRequired().withValidator(alreadyExist, "Phone Number Already Exist")
                .bind(Contact::getPhoneNumber, Contact::setPhoneNumber);
        binder.forField(email).asRequired().withValidator(new EmailValidator("Invalid Email Address")).bind(Contact::getEmail, Contact::setEmail);
        phoneNumber.addValueChangeListener(e -> phoneBinding.validate());

    }
    private void createWarningDialogue() {
        warnAlreadyUpdateddialog = new ConfirmDialog();
        warnAlreadyUpdateddialog.setHeader("Already Updated");
        warnAlreadyUpdateddialog.setText("This Contact is already updated by another user.");
        warnAlreadyUpdateddialog.setConfirmText("OK");
        warnAlreadyUpdateddialog.open();
        warnOnAlreadyUpdatedContact = false;
    }
    static Map<String, Contact> contactsMap = new HashMap<String, Contact>() {{
        put("03451550528", new Contact("Salman", "03451550528", "salman@gmail.com", "abc street", "xyz city", "Pakistan"));
        put("09251550528", new Contact("Hafiz", "09251550528", "hafiz@gmail.com", "abc street", "xyz city", "Pakistan"));
    }};
}
