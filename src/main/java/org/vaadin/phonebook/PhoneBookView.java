package org.vaadin.phonebook;

import com.vaadin.componentfactory.enhancedcrud.BinderCrudEditor;
import com.vaadin.componentfactory.enhancedcrud.Crud;
import com.vaadin.componentfactory.enhancedcrud.CrudEditor;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
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
import java.util.stream.Collectors;


import org.vaadin.phonebook.dao.ContactsDao;
import org.vaadin.phonebook.entity.Contact;
import org.vaadin.phonebook.dataprovider.ContactDataProvider;


@SuppressWarnings("serial")
@Route("")
public class PhoneBookView extends VerticalLayout {

    private Crud<Contact> crud;
    private ContactDataProvider contactDataProvider = new ContactDataProvider();
    private Binder<Contact> binder;
    private TextField name = new TextField("Name");
    private TextField phoneNumber = new TextField("Phone number");
    private TextField email = new TextField("email");
    private TextField street = new TextField("Street");
    private TextField city = new TextField("City");
    private ComboBox<String> countryComboBox = new ComboBox<>("Country");
    private String editPhoneNumber;
    private Boolean isAddContactClicked = false;
    private LocalDateTime lastUpdatedTimeFlag;
    private boolean warnOnAlreadyUpdatedContact = true;
    private transient ContactsDao contactsDao = new ContactsDao();

    public PhoneBookView() {
        initView();
    }

    private void initView() {
        crud = new Crud<>(Contact.class, createEditor());
        crud.setEditOnClick(true);
        crud.setSizeFull();
        setGridColumns();
        ContactDataProvider.setContactMap(contactsDao.getContacts());
        crud.setDataProvider(contactDataProvider);
        crud.addPreSaveListener(e -> {
            if (!isAddContactClicked && warnOnAlreadyUpdatedContact && isContactAlreadyUpdated()) {
                createWarningDialogue();
                e.getSource().cancelSave();
            }
        });
        crud.addSaveListener(e -> saveContact(e.getItem()));
        crud.addEditListener(e -> {
            editPhoneNumber = e.getItem().getPhoneNumber();
            isAddContactClicked = false;
            lastUpdatedTimeFlag = e.getItem().getLastUpdatedTime();
            validateFields();
        });
        crud.addDeleteListener(e -> deleteContact(e.getItem().getPhoneNumber()));
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

        FormLayout formLayout = new FormLayout(name, phoneNumber, email, street, city, countryComboBox);
        binder = new Binder<>(Contact.class);
        binder.forField(name).asRequired().bind(Contact::getName, Contact::setName);
        binder.forField(phoneNumber).asRequired().bind(Contact::getPhoneNumber, Contact::setPhoneNumber);
        binder.forField(email).asRequired().bind(Contact::getEmail, Contact::setEmail);
        binder.bind(street, Contact::getStreet, Contact::setStreet);
        binder.bind(city, Contact::getCity, Contact::setCity);
        binder.bind(countryComboBox, Contact::getCountry, Contact::setCountry);
        countryComboBox.setItems(getCountriesNameList());
        return new BinderCrudEditor<>(binder, formLayout);
    }

    private void saveContact(Contact contact) {
        contact.setLastUpdatedTime(LocalDateTime.now());
        boolean isSuccess = true;
        if (Objects.isNull(editPhoneNumber)) {
            isSuccess = contactsDao.addContact(contact);
        } else if (!contact.getPhoneNumber().equals(editPhoneNumber)) {
            deleteContact(editPhoneNumber);
            isSuccess = contactsDao.addContact(contact);
        } else {
            isSuccess = contactsDao.updateContact(contact);
        }
        if (isSuccess) {
            contactDataProvider.persist(contact);
        }
    }

    private void deleteContact(String phoneNumber) {
        if (contactsDao.deleteContact(phoneNumber)) {
            contactDataProvider.delete(phoneNumber);
        }
    }

    private void setGridColumns() {
        crud.getGrid().removeColumn(crud.getGrid().getColumnByKey("lastUpdatedTime"));
        crud.getGrid().setColumnOrder(crud.getGrid().getColumnByKey("name"), crud.getGrid().getColumnByKey("phoneNumber"), crud.getGrid().getColumnByKey("email"), crud.getGrid().getColumnByKey("street"), crud.getGrid().getColumnByKey("city"), crud.getGrid().getColumnByKey("country"));
    }

    private Boolean isContactAlreadyUpdated() {
        Optional<Contact> contactOptional = contactDataProvider.find(editPhoneNumber);
        if (contactOptional.isPresent() && contactOptional.get().getLastUpdatedTime().equals(lastUpdatedTimeFlag))
            return false;
        return true;
    }

    private void validateFields() {
        SerializablePredicate<String> alreadyExist = value -> !(contactDataProvider.contains(phoneNumber.getValue()) && (isAddContactClicked || !phoneNumber.getValue().equals(editPhoneNumber)));
        Binder.Binding<Contact, String> phoneBinding = binder.forField(phoneNumber).asRequired().withValidator(alreadyExist, "Phone Number Already Exist")
                .bind(Contact::getPhoneNumber, Contact::setPhoneNumber);
        binder.forField(email).asRequired().withValidator(new EmailValidator("Invalid Email Address")).bind(Contact::getEmail, Contact::setEmail);
        phoneNumber.addValueChangeListener(e -> phoneBinding.validate());

    }

    private void createWarningDialogue() {
        ConfirmDialog warningOnAlreadyUpdateContact = new ConfirmDialog();
        warningOnAlreadyUpdateContact.setHeader("Already Updated");
        warningOnAlreadyUpdateContact.setText("This Contact is already updated by another user.");
        warningOnAlreadyUpdateContact.setConfirmText("OK");
        warningOnAlreadyUpdateContact.open();
        warnOnAlreadyUpdatedContact = false;
    }

    private List<String> getCountriesNameList() {
        return Arrays.stream(Locale.getISOCountries()).map(e -> (new Locale("", e)).getDisplayCountry()).sorted().collect(Collectors.toList());
    }

}
