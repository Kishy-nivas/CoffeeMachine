package services;

import com.fasterxml.jackson.databind.ObjectMapper;

import dao.MachineIngredientDao;

import model.Beverage;
import model.CoffeeMachineConfig;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A service which handles the CoffeeMachine's ingredients, configs and processes the incoming beverage
 * requests via json file
 */
public class CoffeeMachineService {
    private final MachineIngredientDao machineIngredientDao;
    private final ThreadPoolExecutor executor;
    // Every 1 ms we check for low ingredients, setting this low to see this in working.
    private static final int LOW_INGREDIENT_TIMER_INTERVAL = 2;
    // Lets assume any ingredient running below 100 qty is low and needs to be refilled.
    private static  final int LOW_INGREDIENT_THRESHOLD = 100;
    private final CoffeeMachineConfig coffeeMachineConfig;
    private Timer lowIngredientIndicator;
    private TimerTask timerTask;

    public CoffeeMachineService(String configFilePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String content = getFileContentAsString(configFilePath);
        this.coffeeMachineConfig = objectMapper.readValue(content,
                CoffeeMachineConfig.class);
        int outlets = coffeeMachineConfig.getOutlets().getCount();
        // We assume maximum valid requests to be twice the size of outlets, we discard the other requests
        int MAX_REQUESTS = outlets * 2;
        this.machineIngredientDao = new MachineIngredientDao(LOW_INGREDIENT_THRESHOLD);
        this.executor = new ThreadPoolExecutor(outlets, outlets,1000L, TimeUnit.MILLISECONDS,
                new LinkedBlockingDeque<>(MAX_REQUESTS));
        // If we receive more requests than MAX_REQUESTS, we reject the request
        this.executor.setRejectedExecutionHandler(new RejectedExecutionHandler() {
            @Override
            public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                System.out.println("Coffee Machine is currently full, unable to process this request, " +
                        "Please try later");
            }
        });
        this.initMachine();
    }

    public void initMachine() {
        // Setup low ingredient monitor, which runs at a fixed rate.
        this.lowIngredientIndicator = new Timer();
        this.timerTask = new TimerTask() {
            @Override
            public void run() {
                machineIngredientDao.printIngredientsRunningLow();
            };
        };
        this.lowIngredientIndicator.scheduleAtFixedRate(this.timerTask,
                LOW_INGREDIENT_TIMER_INTERVAL, LOW_INGREDIENT_TIMER_INTERVAL);
        // Add the initial ingredients to the machine
        Map<String, Integer> ingredients = this.coffeeMachineConfig.getItems();
        ingredients.keySet().forEach(ingredient -> this.machineIngredientDao.addIngredient(ingredient,
                ingredients.get(ingredient)));

    }

    public void processBeverageRequests() {
        this.coffeeMachineConfig.getBeverages().keySet().forEach(beverageName -> {
            Map<String, Integer> ingredientsMap = this.coffeeMachineConfig.getBeverages().
                    get(beverageName);
            Beverage beverage = new Beverage(beverageName, ingredientsMap);
            System.out.println("processing : " + beverageName);
            serveBeverage(beverage);
        });
        // wait until all requests are processed.
        executor.shutdown();
        try {
            executor.awaitTermination(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Shutdown executor");
        this.cancelTimers();
    }

    private String getFileContentAsString(String path ) {
        try {
            return new String(Files.readAllBytes(
                    Paths.get(path)), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        return null;
    }

    private void serveBeverage(Beverage beverage) {
        Runnable runnable = () -> machineIngredientDao.serveBeverageIfPossible(beverage);
        executor.execute(runnable);
    }

    public void cancelTimers() {
        this.lowIngredientIndicator.cancel();
        this.timerTask.cancel();
    }

    public void refillAllIngredients() {
        this.machineIngredientDao.refillAllIngredients();
    }
}
