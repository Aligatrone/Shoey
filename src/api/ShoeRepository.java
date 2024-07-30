package org.example;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ShoeRepository {
    private final EntityManager em;

    public ShoeRepository(){
        Manager manager = new Manager();
        EntityManagerFactory emf = manager.getEntityManagerFactory();
        em = manager.getEntityManager();
    }

    public void create(Shoe shoe){
        em.clear();
        em.getTransaction().begin();
        em.persist(shoe);
        em.getTransaction().commit();
    }


    public List<Shoe> getShoes() {
        List<Shoe> shoes = getAllShoes();
        for(Shoe iterator : shoes) {
            System.out.println("I got this Shoe : "+iterator.toString());
        }

        return shoes;
    }

    public void saveShoes(List<Shoe> shoes) {
        for(Shoe shoe: shoes) {
            create(shoe);
        }
    }

    public List<Shoe> getShoesByParam(String value) {
        String[] words = value.split("=");

        List<Shoe> shoes = getShoes();

        List<Shoe> shoesToReturn = new ArrayList<>();

        if(words[0].equals("search")) {
            for(Shoe shoe: shoes) {

                if(shoe.getName().toLowerCase().contains(words[1]))
                    shoesToReturn.add(shoe);
            }
        } else if(words[0].equals("gendre")) {
            for(Shoe shoe: shoes) {
                if(shoe.getGender().toString().equals(words[1]))
                    shoesToReturn.add(shoe);
            }
        } else if(words[0].equals("season")) {
            for(Shoe shoe: shoes) {
                if(shoe.getSeason().equalsIgnoreCase(words[1]) || shoe.getSeason().equals("Tot anul"))
                    shoesToReturn.add(shoe);
            }
        } else if(words[0].equals("category")) {
            for(Shoe shoe: shoes) {
                if(shoe.getStyle().toLowerCase().contains(words[1]))
                    shoesToReturn.add(shoe);
            }
        }

        System.out.println(shoesToReturn.toString());
        return shoesToReturn;
    }

    public List<Shoe> getAllShoes() {
        List<Shoe> shoes = new ArrayList<>();

        try {
            shoes = em.createNamedQuery("Shoe.getAllShoes").getResultList();
        } catch (NoResultException e) {
            System.out.println("NO content: " + e);
            return null;
        }

        return shoes;
    }

    public List<Shoe> findShoes(ShoeFilter filter) {
        List<Shoe> shoes = new ArrayList<>();

        try {
            StringBuilder jpql = new StringBuilder("SELECT s FROM Shoe s WHERE 1=1");

            if (filter.getGender() != null) {
                jpql.append(" AND s.gender = :gender");
            }

            // Add more filters here

            Query query = em.createQuery(jpql.toString());

            if (filter.getGender() != null) {
                if(filter.getGender().equals("male"))
                    query.setParameter("gender", Genders.male);
                else query.setParameter("gender", Genders.female);
            }

            // Set values for more filters here

            shoes = query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return shoes;
    }

    public List<String> getAllPhotos(int id){
        try {
            em.clear();
            List<String> photos = em.find(Shoe.class, id).getImage();
            String thumbnail = em.find(Shoe.class, id).getPhoto();
            photos.add(0, thumbnail);
            return photos;
        } catch(NoResultException e){
            return null;
        }
    }

    public Shoe getRandomShoe() {
        List<Shoe> shoes = getAllShoes();
        if (!shoes.isEmpty()) {
            int randomIndex = new Random().nextInt(shoes.size());
            return shoes.get(randomIndex);
        }
        return null;
    }

    public Shoe getShoeById(Integer id){
        try {
            em.clear();
            return em.find(Shoe.class, id);
        }catch(NoResultException e){
            return null;
        }

    }

    public void reSaveShoe(Shoe shoe){
        em.getTransaction().begin();
        em.merge(shoe);
        em.getTransaction().commit();
    }

    public boolean deleteShoeById(Integer shoeId){
        try{
            Shoe shoe=em.find(Shoe.class,shoeId);
            if(shoe != null) {
                em.getTransaction().begin();

                shoe.setRating(0.0);
                shoe.setNumberOfRatings(0);

                String sqlQuery = "DELETE FROM user_rated_shoes WHERE shoe_id = ?1";
                String secondSqlQuery = "DELETE FROM user_liked_shoes WHERE shoe_id = ?1";

                Query query = em.createNativeQuery(sqlQuery);
                query.setParameter(1, shoeId);
                query.executeUpdate();

                Query secondQuery = em.createNativeQuery(secondSqlQuery);
                secondQuery.setParameter(1, shoeId);
                secondQuery.executeUpdate();

                em.remove(shoe);
                em.getTransaction().commit();

                return true;
            } else {
                return false;
            }
        }catch(NoResultException e){
            return false;
        }
    }
}
