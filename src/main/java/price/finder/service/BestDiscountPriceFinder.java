package price.finder.service;

import price.finder.model.Discount;
import price.finder.model.Quote;
import price.finder.model.ShopWithDiscount;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class BestDiscountPriceFinder {

    private final List<ShopWithDiscount> shopWithDiscounts = Arrays.asList(new ShopWithDiscount("BestPrice"),
            new ShopWithDiscount("LetsSaveBigWithDiscount"),
            new ShopWithDiscount("MyFavoriteShopWithDiscount"),
            new ShopWithDiscount("BuyItAllWithDiscount"),
            new ShopWithDiscount("ShopWithDiscountEasy"));

    private final Executor executor = Executors.newFixedThreadPool(shopWithDiscounts.size());
    
    public Stream<CompletableFuture<String>> findPricesStream(String product) {
        return shopWithDiscounts.stream()
                .map(ShopWithDiscount -> CompletableFuture.supplyAsync(() -> ShopWithDiscount.getPrice(product), executor))
                .map(future -> future.thenApply(Quote::parse))
                .map(future -> future.thenCompose(quote -> CompletableFuture.supplyAsync(() -> Discount.applyDiscount(quote), executor)));
    }

    public void printPricesStream(String product) {
        Instant start = Instant.now();
        CompletableFuture[] futures = (CompletableFuture[]) findPricesStream(product)
                .map(f -> f.thenAccept(s -> System.out.println(s)))
                .toArray(size -> new CompletableFuture[size]);
        CompletableFuture.allOf(futures).join();
        System.out.println("All ShopWithDiscounts have now responded in " + Duration.between(start, Instant.now()).toMillis() + " Millis Seconds");
    }
}
