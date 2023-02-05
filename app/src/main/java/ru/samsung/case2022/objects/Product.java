package ru.samsung.case2022.objects;

import java.io.Serializable;

public class Product implements Serializable {

    private String name;
    private float cost;

    public Product(String name, float cost) {
        this.name = name;
        this.cost = cost;
    }

    public Product(String name) {
        this.name = name;
    }

    public Product() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
