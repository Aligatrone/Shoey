package org.example;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

public class Manager {
    javax.persistence.EntityManagerFactory emf;
    EntityManager em;

    public EntityManagerFactory getEntityManagerFactory(){
        if(emf==null){
            emf = Persistence.createEntityManagerFactory("WebsitePU");
        }
        return emf;
    }

    public EntityManager getEntityManager() {
        if (em == null) {
            em = emf.createEntityManager();
        }
        return em;
    }

    public void endManager(){
        if(emf!=null) {
            emf.close();
        }

        if(em!=null) {
            em.close();
        }
    }
}
