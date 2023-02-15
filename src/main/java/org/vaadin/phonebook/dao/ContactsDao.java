package org.vaadin.phonebook.dao;

import org.vaadin.phonebook.entity.Contact;

import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ContactsDao {

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public Map<String, Contact> getContacts() {
        Map<String, Contact> contactMap = new LinkedHashMap<>();
        String query = "select * from contacts";
        try (PreparedStatement prepareStatement = DBConnection.getConnection().prepareStatement(query)) {
            ResultSet resultSet = prepareStatement.executeQuery();
            while (resultSet.next()) {
                Contact contact = new Contact();
                contact.setPhoneNumber(resultSet.getString("phone_number"));
                contact.setName(resultSet.getString("name"));
                contact.setEmail(resultSet.getString("email"));
                contact.setStreet(resultSet.getString("street"));
                contact.setCity(resultSet.getString("city"));
                contact.setCountry(resultSet.getString("country"));
                contact.setLastUpdatedTime(LocalDateTime.parse(resultSet.getString("last_update_time"), formatter));
                contactMap.put(contact.getPhoneNumber(), contact);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return contactMap;
    }

    public Optional<Contact> getContact(String phoneNumber) {
        String query = "select * from contacts where phone_number=?";
        Contact contact = null;
        try (PreparedStatement prepareStatement = DBConnection.getConnection().prepareStatement(query)) {
            prepareStatement.setString(1, phoneNumber);
            ResultSet resultSet = prepareStatement.executeQuery();
            if (resultSet.next()) {
                contact = new Contact();
                contact.setPhoneNumber(resultSet.getString("phone_number"));
                contact.setName(resultSet.getString("name"));
                contact.setEmail(resultSet.getString("email"));
                contact.setStreet(resultSet.getString("street"));
                contact.setCity(resultSet.getString("city"));
                contact.setCountry(resultSet.getString("country"));
                contact.setLastUpdatedTime(LocalDateTime.parse(resultSet.getString("last_update_time"), formatter));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return Optional.ofNullable(contact);
    }

    public boolean addContact(Contact contact) {
        String query = "INSERT INTO contacts (phone_number, name, email, street, city, country, last_update_time)  VALUES  (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement preparedStatement = DBConnection.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, contact.getPhoneNumber());
            preparedStatement.setString(2, contact.getName());
            preparedStatement.setString(3, contact.getEmail());
            preparedStatement.setString(4, contact.getStreet());
            preparedStatement.setString(5, contact.getCity());
            preparedStatement.setString(6, contact.getCountry());
            preparedStatement.setString(7, String.valueOf(contact.getLastUpdatedTime()));
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean updateContact(Contact contact) {
        String query = "UPDATE contacts SET  name=?, email=?, street=?, city=?, country=?, last_update_time=?  WHERE phone_number=?";
        try (PreparedStatement preparedStatement = DBConnection.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, contact.getName());
            preparedStatement.setString(2, contact.getEmail());
            preparedStatement.setString(3, contact.getStreet());
            preparedStatement.setString(4, contact.getCity());
            preparedStatement.setString(5, contact.getCountry());
            preparedStatement.setString(6, String.valueOf(contact.getLastUpdatedTime()));
            preparedStatement.setString(7, contact.getPhoneNumber());
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean deleteContact(String phoneNumber) {
        String query = "delete from contacts where phone_number= ?";
        try (PreparedStatement preparedStatement = DBConnection.getConnection().prepareStatement(query)) {
            preparedStatement.setString(1, phoneNumber);
            return preparedStatement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
