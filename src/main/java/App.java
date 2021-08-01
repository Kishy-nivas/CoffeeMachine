import services.CoffeeMachineService;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws IOException {

        // adding functional test cases here, as junit doesn't print system.out lines
        // Test case -1
        String content;
        CoffeeMachineService coffeeMachineService;
        content = "src/main/resources/testcase1.txt";
        System.out.println("TEST CASE -1 given test case");
        coffeeMachineService = new CoffeeMachineService(content);
        coffeeMachineService.processBeverageRequests();

        // Test case -2
        content = "src/main/resources/testcase2.txt";
        System.out.println("TEST CASE - 2, check machine works after refilling");
        coffeeMachineService = new CoffeeMachineService(content);
        System.out.println("Refill ingredients to max");
        coffeeMachineService.refillAllIngredients();
        coffeeMachineService.processBeverageRequests();

        // Test case -3
        content = "src/main/resources/testcase3.txt";
        System.out.println("TEST CASE -3, check invalid ingredients and single outlet");
        coffeeMachineService = new CoffeeMachineService(content);
        coffeeMachineService.processBeverageRequests();
    }
}
