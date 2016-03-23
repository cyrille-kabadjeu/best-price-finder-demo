package price.finder.service;

import org.junit.Test;
import org.mockito.Mockito;
import price.finder.model.ShopWithDiscount;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import static org.mockito.Mockito.times;

public class BestDiscountPriceFinderTest {

    @Test
    public void shouldInvokeDiscountServiceAsynchronouslyFailed(){
        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        DiscountService discountService = Mockito.mock(DiscountService.class);
        BestDiscountPriceFinder bestDiscountPriceFinder = new BestDiscountPriceFinder();
        bestDiscountPriceFinder.setExecutor(executor);
        bestDiscountPriceFinder.setDiscountService(discountService);

        ShopWithDiscount shopWithDiscount = Mockito.mock(ShopWithDiscount.class);
        String shopResponse = "BuyItAllWithDiscount:150:GOLD";
        Mockito.doReturn(shopResponse).when(shopWithDiscount).getPrice("Lenovo");

        bestDiscountPriceFinder.computeDiscountPriceAsync("Lenovo", shopWithDiscount).join();
        Mockito.verify(discountService, times(1)).apply(150, DiscountService.Code.GOLD);

    }

    @Test
    public void shouldInvokeDiscountServiceAsynchronouslyPass(){
        Executor executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        DiscountService discountService = Mockito.mock(DiscountService.class);
        BestDiscountPriceFinder bestDiscountPriceFinder = new BestDiscountPriceFinder();
        bestDiscountPriceFinder.setExecutor(executor);
        bestDiscountPriceFinder.setDiscountService(discountService);

        ShopWithDiscount shopWithDiscount = Mockito.mock(ShopWithDiscount.class);
        String shopResponse = "BuyItAllWithDiscount:150:GOLD";

        Runnable train = ()-> Mockito.doReturn(shopResponse).when(shopWithDiscount).getPrice("Lenovo");
        Runnable verify = ()-> Mockito.verify(discountService, times(1)).apply(150.0, DiscountService.Code.GOLD);
        Runnable callAndVerify = ()-> bestDiscountPriceFinder.computeDiscountPriceAsync("Lenovo", shopWithDiscount).thenRun(verify);

        CompletableFuture.runAsync(train, executor)
                .thenRun(callAndVerify)
                .join();

    }

}