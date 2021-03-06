package price.finder.service;

import price.finder.model.Quote;

import static price.finder.Util.delay;
import static price.finder.Util.format;

public class DiscountService {

    public enum Code {
        NONE(0), SILVER(5), GOLD(10), PLATINUM(15), DIAMOND(20);

        private final int percentage;

        Code(int percentage) {
            this.percentage = percentage;
        }
    }

    public String applyDiscount(Quote quote) {
        return quote.getShopName() + " price is " + apply(quote.getPrice(), quote.getDiscountCode());
    }

    public double apply(double price, Code code) {
        delay();
        return format(price * (100 - code.percentage) / 100);
    }
}