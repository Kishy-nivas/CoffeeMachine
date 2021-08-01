package dao;

import model.Beverage;

import java.util.HashMap;
import java.util.Map;

import static java.lang.System.*;

/**
 * Thread-safe implementation of MachineIngredientDao, which maintains the machine ingredient count,
 * we only serve beverage if its possible to serve it, other implementation can be like having a lock
 * at the common resource level and then blocking it while serving a beverage, but we need to refill the
 * resource again if we are unable to full fill the request and there is a possibility some other drink might
 * go un-served in this kind of implementation, Hence decided to have it simple without missing out requests.
 */
public class MachineIngredientDao {
    private final Map<String, Integer> ingredientsMap;
    private static final int INGREDIENT_NOT_PRESENT = -1;
    private int lowIngredientThreshold;
    // lets assume threshold for refill is 1000
    private static final int REFILL_THRESHOLD = 1000;
    public  MachineIngredientDao(int lowIngredientThreshold) {
        this.lowIngredientThreshold =  lowIngredientThreshold;
        ingredientsMap = new HashMap<>();
    }

    public synchronized void addIngredient(String ingredientName, int qty) {
        ingredientsMap.put(ingredientName,
                ingredientsMap.getOrDefault(ingredientName,0) + qty);
    }

    public synchronized boolean serveBeverageIfPossible(Beverage beverage) {
        out.println("BeverageMaker: Trying to serve " + beverage.getName());
        Map<String, Integer>  requiredIngredients = beverage.getIngredients();
        for (String ingredient : requiredIngredients.keySet()) {
            int machineIngredientCount = ingredientsMap.getOrDefault(ingredient, INGREDIENT_NOT_PRESENT);
            if (machineIngredientCount == INGREDIENT_NOT_PRESENT) {
                out.println(beverage.getName() + " cannot be prepared because "
                        + ingredient + " is not available");
                return false;
            }
            if (requiredIngredients.get(ingredient) > machineIngredientCount) {
                out.println(beverage.getName() + " cannot be prepared because " + ingredient
                        + " is not sufficient");
                return false;
            }
        }
        // its possible to serve the request for this beverage
        requiredIngredients.keySet().forEach(ingredient -> {
            int machineIngredientCount = ingredientsMap.getOrDefault(ingredient, INGREDIENT_NOT_PRESENT);
            ingredientsMap.put(ingredient,
                    (machineIngredientCount - requiredIngredients.getOrDefault(ingredient, 0)));
        });
        out.println(beverage.getName() + " is prepared");
        return true;
    }

    public synchronized void printIngredientsRunningLow() {
        this.ingredientsMap.keySet().stream().filter(ingredient -> ingredientsMap.get(ingredient)
                <= this.lowIngredientThreshold).map(ingredient ->
                "lowlevelindicator: "  + ingredient + " is running low").forEach(out::println);
    }

    public synchronized void refillAllIngredients() {
        ingredientsMap.replaceAll((n, v) -> REFILL_THRESHOLD);
    }
}
