package org.example;

import org.eclipse.persistence.annotations.TypeConverter;

import javax.persistence.*;
import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "users")
@Inheritance(strategy = InheritanceType.JOINED)
@DiscriminatorColumn(name = "user_type", discriminatorType = DiscriminatorType.STRING)

@NamedQueries({
        @NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email"),
        @NamedQuery(name = "User.getAllUsers", query = "SELECT u FROM User u")
})
public abstract class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id")
    protected Integer id;

    @Column(name="email")
    protected String email;

    @Column(name="firstName")
    protected String firstName;

    @Column(name="lastName")
    protected String lastName;

    @Column(name="password")
    protected String password;

    @Column(name="isAdmin")
    protected boolean isAdmin;

    @Column(name = "user_type", insertable = false, updatable = false)
    protected String userType;

    @Column(name="profilepicture")
    protected String profilePicture;

    @ElementCollection
    @CollectionTable(name = "user_rated_shoes", joinColumns = @JoinColumn(name = "user_id"))
    @MapKeyJoinColumn(name = "shoe_id")
    @Column(name = "rating")
    protected Map<Shoe,Integer> ratedShoes;

    @ManyToMany
    @JoinTable(name = "user_liked_shoes",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "shoe_id"))
    protected List<Shoe> likedShoes;


    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id=id;
    }
    public String getFirstName() {
        return firstName;
    }
    public void setFirstName(String firstName) {
        this.firstName=firstName;
    }
    public String getLastName() {
        return lastName;
    }
    public void setLastName(String lastName) {
        this.lastName=lastName;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email=email;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password=password;
    }

    public String getProfilePicture() {
        return profilePicture;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Map<Shoe, Integer> getRatedShoes() {
        return ratedShoes;
    }

    public void setRatedShoes(Map<Shoe, Integer> ratedShoes) {
        this.ratedShoes = ratedShoes;
    }

    public void addRatedShoe(Shoe shoe, Integer rating){
            ratedShoes.put(shoe,rating);
    }

    public boolean checkAlreadyRated(Shoe shoe){
        return ratedShoes.containsKey(shoe);
    }

    public void removeRatedShoe(Shoe shoe){
        if(ratedShoes.containsKey(shoe)){
            ratedShoes.remove(shoe);
        }
    }

    public void changeShoeRating(Shoe shoe, Integer newRating){
        if(ratedShoes.containsKey(shoe) && !ratedShoes.get(shoe).equals(newRating)){
            ratedShoes.remove(shoe);
            ratedShoes.put(shoe,newRating);
        }
    }

    public Integer getRatingForShoe(Shoe shoe){
        return ratedShoes.get(shoe);
    }

    public List<Shoe> getLikedShoes() {
        return likedShoes;
    }

    public void setLikedShoes(List<Shoe> likedShoes) {
        this.likedShoes = likedShoes;
    }

    public void addLikedShoe(Shoe shoe){
        likedShoes.add(shoe);
    }

    public void removeLikedShoe(Shoe shoe){
        likedShoes.remove(shoe);
    }

    public boolean getAdminStatus(){
        return isAdmin;
    }
}
