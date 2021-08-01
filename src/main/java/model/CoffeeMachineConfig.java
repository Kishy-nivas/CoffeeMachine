package model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

import java.util.Map;

/**
 * Simple Pojo class to read the given Json config
 */
@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@JsonTypeName(value = "machine")
public class CoffeeMachineConfig {
    @JsonProperty("outlets")
    private final Outlets outlets;
    @JsonProperty("total_items_quantity")
    private final Map<String, Integer> items;
    @JsonProperty("beverages")
    private final Map<String, Map<String, Integer>> beverages;

    public CoffeeMachineConfig(@JsonProperty("outlets")  Outlets outlets,
                   @JsonProperty("total_items_quantity") Map<String, Integer> items,
                   @JsonProperty("beverages") Map<String, Map<String, Integer>> beverages) {
        this.outlets = outlets;
        this.items = items;
        this.beverages = beverages;
    }

    public Outlets getOutlets() {
        return outlets;
    }

    public Map<String, Integer> getItems() {
        return items;
    }

    public Map<String, Map<String, Integer>> getBeverages() {
        return beverages;
    }
    @Override
    public String toString() {
        return "CoffeeMachineConfig{" +
                "outlet=" + outlets +
                ", items=" + items +
                ", beverages=" + beverages +
                '}';
    }

    public static class Outlets {
        @JsonProperty("count_n")
        int count;

        public int getCount() {
            return count;
        }

        @Override
        public String toString() {
            return "Outlets{" +
                    "count=" + count +
                    '}';
        }
    }
}