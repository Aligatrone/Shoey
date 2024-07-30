package org.example;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "shoes")
@NamedQuery(name = "Shoe.getAllShoes", query = "SELECT s FROM Shoe s")
public class Shoe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "name")
    private String name;

    @ElementCollection
    @CollectionTable(name = "shoe_sizes", joinColumns = @JoinColumn(name = "shoe_id"))
    @Column(name = "size")
    private List<Integer> size;

    @Column(name = "color")
    private String color;

    @Column(name = "photo")
    private String photo;

    @ElementCollection
    @CollectionTable(name = "shoe_images", joinColumns = @JoinColumn(name = "shoe_id"))
    @Column(name = "image")
    private List<String> image;

    @Column(name = "link")
    private String link;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender")
    private Genders gender;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "number_of_ratings")
    private Integer numberOfRatings;

    @Column(name = "price")
    private Double price;

    @Column(name = "season")
    private String season;

    @Column(name = "style")
    private String style;

    public Shoe() {
        this.rating = 0.0;
        this.numberOfRatings = 0;
    }

    public String getSeason() {
        return season;
    }

    public void setSeason(String season) {
        this.season = season;
    }

    public String getStyle() {
        return style;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getSize() {
        return size;
    }

    public void setSize(List<Integer> size) {
        this.size = size;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPhoto() {
        return photo;
    }

    public void setPhoto(String photo) {
        this.photo = photo;
    }

    public List<String> getImage() {
        return image;
    }

    public void setImage(List<String> image) {
        this.image = image;
    }

    public void addImage(String image) {
        this.image.add(image);
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Genders getGender() {
        return gender;
    }

    public void setGender(Genders gender) {
        this.gender = gender;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public void addRating(Integer userNewRating,boolean newRating,Integer previousRating){
        if(newRating){
            Double remember = rating*numberOfRatings;
            remember = remember + userNewRating;

            numberOfRatings = numberOfRatings+1;
            rating = remember/numberOfRatings;
        } else {
            Double remember = rating*numberOfRatings;
            remember = remember - previousRating;
            remember = remember + userNewRating;

            rating = remember/numberOfRatings;
        }
    }

    public Integer getNumberOfRatings() {
        return numberOfRatings;
    }

    public void setNumberOfRatings(Integer numberOfRatings) {
        this.numberOfRatings = numberOfRatings;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Shoe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", color='" + color + '\'' +
                ", photo='" + photo + '\'' +
                ", image=" + image +
                ", link='" + link + '\'' +
                ", gender=" + gender +
                ", rating=" + rating +
                ", numberOfRatings=" + numberOfRatings +
                ", price=" + price +
                ", season='" + season + '\'' +
                ", style='" + style + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Shoe otherShoe = (Shoe) obj;
        return Objects.equals(id, otherShoe.id);
    }
}
