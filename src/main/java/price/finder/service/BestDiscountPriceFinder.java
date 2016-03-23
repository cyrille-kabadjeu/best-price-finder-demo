package price.finder.service;

import price.finder.model.Quote;
import price.finder.model.ShopWithDiscount;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Stream;

public class BestDiscountPriceFinder {


    private Executor executor;
    private DiscountService discountService;

    public void setExecutor(Executor executor) {
        this.executor = executor;
    }

    public void setDiscountService(DiscountService discountService) {
        this.discountService = discountService;
    }

    public CompletableFuture<String> computeDiscountPriceAsync(String productId, ShopWithDiscount shopWithDiscount) {
        return CompletableFuture.supplyAsync(() -> shopWithDiscount.getPrice(productId), executor)
                .thenApply(Quote::parse)
                .thenCompose(quote -> CompletableFuture.supplyAsync(() -> discountService.applyDiscount(quote), executor));
    }


    public Stream<CompletableFuture<String>> findPricesStream(String product, List<ShopWithDiscount> shopWithDiscounts) {
        return shopWithDiscounts.stream()
                .map(shopWithDiscount -> CompletableFuture.supplyAsync(() -> shopWithDiscount.getPrice(product), executor))
                .map(future -> future.thenApply(Quote::parse))
                .map(future -> future
                        .thenCompose(quote -> CompletableFuture.supplyAsync(() -> discountService.applyDiscount(quote), executor)));
    }


    public void printPricesStream(String product) {

        List<ShopWithDiscount> shopWithDiscounts = Arrays.asList(new ShopWithDiscount("BestPrice"),
                new ShopWithDiscount("LetsSaveBigWithDiscount"),
                new ShopWithDiscount("MyFavoriteShopWithDiscount"),
                new ShopWithDiscount("BuyItAllWithDiscount"),
                new ShopWithDiscount("ShopWithDiscountEasy"));

        Instant start = Instant.now();
        CompletableFuture[] futures = (CompletableFuture[]) findPricesStream(product, shopWithDiscounts)
                .map(f -> f.thenAccept(s -> System.out.println(s)))
                .toArray(size -> new CompletableFuture[size]);
        CompletableFuture.allOf(futures).join();
        System.out.println("All ShopWithDiscounts have now responded in " + Duration.between(start, Instant.now()).toMillis() + " Millis Seconds");
    }
}
