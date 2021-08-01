package model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Beverage {
    private  String name;
    private Map<String, Integer> ingredients;
    public Beverage(String name, Map<String, Integer> ingredients) {
        this.ingredients = ingredients;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Map<String, Integer> getIngredients() {
        return ingredients;
    }
}
