package com.example.cachingorm.crm.model;

import javax.persistence.Id;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "clients")
public class Client implements Cloneable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address address;

    @OneToMany(mappedBy = "client", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Phone> phones;

    public Client() {
    }

    public Client(String name) {
       this.name = name;
       this.address = new Address();
       this.phones = new ArrayList<>();
    }

    public Client(Long id, String name) {
        this.id = id;
        this.name = name;
        this.address = new Address();
        this.phones = new ArrayList<>();
    }

    public Client(Long id, String name, Address address) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phones = new ArrayList<>();
    }



    public Client(Long id, String name, Address address, List<Phone> phones) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.phones = phones;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public List<Phone> getPhones() {
        return phones;
    }

    public void setPhones(List<Phone> phones) {
        this.phones = phones;
    }

    @Override
    public Client clone() {
        try {
            Client cloned = (Client) super.clone();
            if (this.address != null) {
                cloned.address = new Address();
                cloned.address.setId(this.address.getId());
                cloned.address.setStreet(this.address.getStreet());
            }
            if (this.phones != null) {
                cloned.phones = new ArrayList<>();
                for (Phone phone : this.phones) {
                    Phone clonedPhone = new Phone();
                    clonedPhone.setId(phone.getId());
                    clonedPhone.setNumber(phone.getNumber());
                    clonedPhone.setClient(cloned);
                    cloned.phones.add(clonedPhone);
                }
            }

            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

}


