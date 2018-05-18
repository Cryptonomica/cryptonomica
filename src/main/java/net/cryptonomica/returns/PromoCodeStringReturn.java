package net.cryptonomica.returns;

import net.cryptonomica.entities.PromoCode;

import java.io.Serializable;

public class PromoCodeStringReturn implements Serializable {

    private String promoCode;
    private Integer discountInPercent; // price = price - ((price/100)*discountInPercent)

    /* --- Constructors */

    public PromoCodeStringReturn() {
    }

    public PromoCodeStringReturn(PromoCode promoCode) {
        this.promoCode = promoCode.getPromoCode();
        this.discountInPercent = promoCode.getDiscountInPercent();
    }

    public PromoCodeStringReturn(String promoCode, Integer discountInPercent) {
        this.promoCode = promoCode;
        this.discountInPercent = discountInPercent;
    }

    /* --- Getters and Setters */

    public String getPromoCode() {
        return promoCode;
    }

    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    public Integer getDiscountInPercent() {
        return discountInPercent;
    }

    public void setDiscountInPercent(Integer discountInPercent) {
        this.discountInPercent = discountInPercent;
    }
}
