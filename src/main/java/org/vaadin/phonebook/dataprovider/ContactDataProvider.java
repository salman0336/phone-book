package org.vaadin.phonebook.dataprovider;

import com.vaadin.componentfactory.enhancedcrud.CrudFilter;
import com.vaadin.flow.data.provider.AbstractBackEndDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.data.provider.SortDirection;
import org.vaadin.phonebook.entity.Contact;

import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class ContactDataProvider extends AbstractBackEndDataProvider<Contact, CrudFilter> {

    final Map<String, Contact> contactsMap;
    private Consumer<Long> sizeChangeListener;

    public ContactDataProvider(Map<String, Contact> contacts) {
        this.contactsMap = contacts;
    }

    @Override
    protected Stream<Contact> fetchFromBackEnd(Query<Contact, CrudFilter> query) {
        int offset = query.getOffset();
        int limit = query.getLimit();
        Stream<Contact> stream = contactsMap.values().stream();
        if (query.getFilter().isPresent()) {
            stream = stream.filter(predicate(query.getFilter().get())).sorted(comparator(query.getFilter().get()));
        }
        return stream.skip(offset).limit(limit);
    }

    private static Predicate<Contact> predicate(CrudFilter filter) {
        Stream<Map.Entry<String, String>> a = filter.getConstraints().entrySet().stream();
        return filter.getConstraints().entrySet().stream().map(constraint -> (Predicate<Contact>) contact -> {
            try {
                Object value = valueOf(constraint.getKey(), contact);
                return value != null && value.toString().toLowerCase().contains(constraint.getValue().toLowerCase());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }).reduce(Predicate::and).orElse(e -> true);
    }

    private static Object valueOf(String fieldName, Contact contact) {
        try {
            Field field = Contact.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(contact);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    private static Comparator<Contact> comparator(CrudFilter filter) {
        return filter.getSortOrders().entrySet().stream().map(sortClause -> {
            try {
                Comparator<Contact> comparator = Comparator.comparing(contact -> (Comparable) valueOf(sortClause.getKey(), contact));

                if (sortClause.getValue() == SortDirection.DESCENDING) {
                    comparator = comparator.reversed();
                }
                return comparator;

            } catch (Exception ex) {
                return (Comparator<Contact>) (o1, o2) -> 0;
            }
        }).reduce(Comparator::thenComparing).orElse((o1, o2) -> 0);
    }

    public Optional<Contact> find(String phoneNumber) {
        return Optional.ofNullable(contactsMap.get(phoneNumber));

    }

    public void persist(Contact contact) {
        contactsMap.put(contact.getPhoneNumber(), contact);

    }

    public void delete(Contact contact) {
        contactsMap.remove(contact.getPhoneNumber());
    }

    @Override
    protected int sizeInBackEnd(Query<Contact, CrudFilter> query) {
        // For RDBMS just execute a SELECT COUNT(*) ... WHERE query
        long count = fetchFromBackEnd(query).count();

        if (sizeChangeListener != null) {
            sizeChangeListener.accept(count);
        }

        return (int) count;
    }
}
