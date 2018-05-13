package net.cryptonomica.returns;

import net.cryptonomica.entities.PromoCode;

import java.io.Serializable;
import java.util.ArrayList;

public class CreatePromoCodesReturn implements Serializable {

    private ArrayList<PromoCode> promoCodes;

    /* --- Constructor */

    public CreatePromoCodesReturn() {
        this.promoCodes = new ArrayList<>();
    }

    /* --- Methods */

    public Boolean addPromoCode(PromoCode promoCode) {
        return this.promoCodes.add(promoCode);
    }

    /* --- Getters and Setters */

    public ArrayList<PromoCode> getPromoCodes() {
        return promoCodes;
    }

    public void setPromoCodes(ArrayList<PromoCode> promoCodes) {
        this.promoCodes = promoCodes;
    }

}
