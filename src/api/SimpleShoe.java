package org.example;

public class SimpleShoe {
    int id;
    String name;
    String photo;
    Double price;

    public SimpleShoe(Shoe shoe) {
        this.id = shoe.getId();
        this.name = shoe.getName();
        this.photo = shoe.getPhoto();
        this.price = shoe.getPrice();
    }

    @Override
    public String toString() {
        return "SimpleShoe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", photo='" + photo + '\'' +
                ", price=" + price +
                '}';
    }
}
