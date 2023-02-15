package ru.samsung.case2022.objects;

import java.io.Serializable;
import java.util.Objects;

public class Product implements Serializable {

    private String name;
    private float cost;
    private int count;

    public Product(String name, float cost) {
        this.name = name;
        this.cost = cost;
    }

    public Product(String name) {
        this.name = name;
    }

    public Product() {

    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Product) {
            Product pr = (Product) o;
            if (pr.name.equals(this.name)) {
                return true;
            }
        }
        return false;
    }
}
