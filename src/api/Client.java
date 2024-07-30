package org.example;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Table(name="clients")
public class Client extends User {
    public Client(){
        this.isAdmin = false;
    }

    public Client(String firstName, String lastName, String email, String password){
        this.id = 1;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isAdmin = false;
        this.profilePicture = "";
    }

    public Client(Integer id,String firstName, String lastName, String email, String password){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isAdmin = false;
        this.profilePicture = "";
    }

}
