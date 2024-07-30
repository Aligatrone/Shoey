package org.example;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name="admins")
public class Admin extends User {

    public Admin(){
        this.isAdmin = true;
    }

    public Admin(String firstName, String lastName, String email, String password){
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isAdmin = true;
        this.profilePicture = "";
    }

    public Admin(Integer id,String firstName, String lastName, String email, String password){
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.password = password;
        this.isAdmin = true;
        this.profilePicture = "";
    }
}
