package org.vaadin.phonebook.datamanager;

import org.vaadin.phonebook.entity.Contact;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContactMgr {

    private static Map<String, Contact> contactsMap = new LinkedHashMap<>();

    static {
        contactsMap.put("03451550528", new Contact("Salman", "03451550528", "salman@gmail.com", "abc street", "xyz city", "Pakistan"));
        contactsMap.put("09251550528", new Contact("Hafiz", "09251550528", "hafiz@gmail.com", "abc street", "xyz city", "Pakistan"));
    }

    public static Map<String, Contact> getContacts() {
        return new LinkedHashMap<>(contactsMap);
    }

    public static Contact getContact(String phoneNumber) {
        return contactsMap.get(phoneNumber);
    }

    public static void deleteContact(String phoneNumber) {
        contactsMap.remove(phoneNumber);
    }

    public static void saveContact(Contact contact) {
        contactsMap.put(contact.getPhoneNumber(), contact);
    }

    public static boolean contains(String phoneNumber) {
        return contactsMap.containsValue(phoneNumber);
    }

    public static boolean contains(Contact contact) {
        return contactsMap.containsValue(contact);
    }


}
