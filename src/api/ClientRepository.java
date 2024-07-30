package org.example;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ClientRepository {
    private final EntityManager em;
    public ClientRepository(){
        Manager manager = new Manager();
        EntityManagerFactory emf = manager.getEntityManagerFactory();
        em = manager.getEntityManager();
    }

    public String findEmailById(Integer value) {
        try{
            User user = (User) em.find(User.class,value);
            return user.getEmail();
        }catch (NoResultException e){
            System.out.println("id received "+value);
            return null;
        }
    }

    public void create(Client client){
        em.clear();
        em.getTransaction().begin();
        em.persist(client);
        em.getTransaction().commit();
    }



    public User findByEmail(String email){
            try {
                User user=(User) em.createNamedQuery("User.findByEmail").setParameter("email", email).getSingleResult();
                if(user==null){
                    return null;
                }
                if(user.isAdmin){
                    Admin admin = (Admin) user;
                    return admin;
                } else {
                    Client client = (Client) user;
                    return client;
                }
            } catch (NoResultException e){
                System.out.println("Email received "+email);
                return null;
            }
    }

    public String getNameByEmail(String email){
        try{
            User user=(User) em.createNamedQuery("User.findByEmail").setParameter("email", email).getSingleResult();
            if(user==null){
                return null;
            }
            if(user.isAdmin){
                Admin admin = (Admin) user;
                return admin.getFirstName();
            } else {
                Client client = (Client) user;
                return client.getFirstName();
            }
        }catch (NoResultException e){
            System.out.println("Email received "+email);
            return null;
        }
    }

    public boolean getAdminStatusById(Integer id){
        try {
            return em.find(User.class, id).getAdminStatus();
        } catch (NoResultException e){
            System.out.println("id received "+id);
            return false;
        }
    }


    public int checkCredentials(String email, String password) {
        em.clear();
        em.getTransaction().begin();
        User user=findByEmail(email);
        em.getTransaction().commit();
        if(user!=null){
            if(user.getPassword().equals(password)){
                //conectare reusita
                System.out.println("Good to go");
                return 1;
            } else {
                System.out.println("Password wrong");
                //parola incorecta
                return 2;
            }
        } else {
            //nu exista utilizatorul
            System.out.println("No user");
            return 0;
        }
    }

    public int checkRegister(String firstName, String lastName, String email, String password) {
        em.clear();
        em.getTransaction().begin();
       User user=findByEmail(email);
        em.getTransaction().commit();
        if(user!=null){
            //Email deja folosit
            System.out.println("Email already used");
            return -1;
        } else if(password.length()<6){
            //Parola nu are 6 caractere
            System.out.println("Password not long enough");
            return -2;
        } else {
            System.out.println("Characteristics are correct");
            Client thisClient=new Client(firstName,lastName,email,password);
            em.clear();
            em.getTransaction().begin();
            em.persist(thisClient);
            em.getTransaction().commit();
            return 1;
        }
    }

    public Integer findIdByEmail(String email) {
        User user=findByEmail(email);
        return user.getId();
    }

    public String getLastNameByEmail(String email) {
        User user=findByEmail(email);
        return user.getLastName();
    }

    public String getPasswordByEmail(String email) {
        User user=findByEmail(email);
        return user.getPassword();
    }

    public String getProfilePictureByEmail(String email) {
        User user=findByEmail(email);
        return user.getProfilePicture();
    }

    public int updateUser(Integer id, String firstName, String lastName, String email, String password, String picture) {
        try {
            User user = em.find(User.class, id);

            if (!email.equals(user.getEmail()) && !email.equals("")) {
                user.setEmail(email);
            }
            if (!firstName.equals(user.getFirstName()) && !firstName.equals("")) {
                user.setFirstName(firstName);
            }
            if (!lastName.equals(user.getLastName()) && !lastName.equals("")) {
                user.setLastName(lastName);
            }
            if (!password.equals(user.getPassword()) && !password.equals("")) {
                user.setPassword(password);
            }
            if(!picture.equals("none")) {
                if (!picture.equals(user.getProfilePicture()) && !picture.equals("")) {
                    user.setProfilePicture(picture);
                }
            } else {
                user.setProfilePicture("");
            }

            em.clear();
            em.getTransaction().begin();
            em.merge(user);
            em.getTransaction().commit();

            if(!picture.equals("none")){
                return 2;
            }
            return 1;

        }catch (NoResultException e){
            System.out.println("id received "+id);
            return 0;
         }
    }

    public List<String> getAllEmailAddresses() {
        List<String> emailAddresses = new ArrayList<>();

        try {
            List<User> users = em.createNamedQuery("User.getAllUsers").getResultList();
            for (User user : users) {
                emailAddresses.add(user.getEmail());
            }
        } catch (NoResultException e) {
            System.out.println("No users found in the database: " + e);
        }

        return emailAddresses;
    }

    public User getUserById(Integer userId) {
        try {
            em.clear();
            return em.find(User.class, userId);
        } catch(NoResultException e){
            return null;
        }
    }

    public void reSaveUser(User user){
        em.getTransaction().begin();
        em.merge(user);
        em.getTransaction().commit();
    }

    public Integer getUserRatingByUserId(Integer userId,Shoe shoe){
        em.clear();
        User user=em.find(User.class,userId);
        if(user.checkAlreadyRated(shoe)){
            return user.getRatingForShoe(shoe);
        } else {
            return 0;
        }
    }

    public Integer getUserLikeByUserId(Integer userId, Shoe shoe) {
        em.clear();
        User user=em.find(User.class,userId);
        if(user.getLikedShoes().contains(shoe)){
            return 1;
        } else {
            return 0;
        }
    }

    public List<String> getLikedShoeNames(Integer value) {
        List<String> nameList=new ArrayList<>();
        try{
            User user=em.find(User.class,value);
            nameList = user.getLikedShoes().stream().map(Shoe::getName).collect(Collectors.toList());
            return nameList;

        } catch(NoResultException e){
            return null;
        }
    }

    public List<String> getLikedShoePhotos(Integer value) {
        List<String> photoList=new ArrayList<>();
        try{
            User user=em.find(User.class,value);
            photoList = user.getLikedShoes().stream().map(shoe -> Optional.ofNullable(shoe.getPhoto()).orElse("noPhoto")).collect(Collectors.toList());
            return photoList;
        } catch(NoResultException e){
            return null;
        }
    }

    public List<Integer> getLikedShoeIds(Integer value) {
        List<Integer> idList=new ArrayList<>();
        try{
            User user=em.find(User.class,value);
            idList = user.getLikedShoes().stream().map(Shoe::getId).collect(Collectors.toList());
            return idList;
        } catch(NoResultException e){
            return null;
        }
    }
}
