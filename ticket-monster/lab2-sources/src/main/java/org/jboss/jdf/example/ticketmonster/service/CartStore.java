package org.jboss.jdf.example.ticketmonster.service;

import org.jboss.jdf.example.ticketmonster.model.Cart;

/**
 * A service for storing and retrieving carts.
 *
 * @author Marius Bogoevici
 */
public interface CartStore {

    public static final String CARTS_CACHE = "TICKETMONSTER_CARTS";

    public Cart getCart(String cartId);

    /**
     * Saves or updates a cart, setting an expiration time.
     *
     * @param cart - the cart to be saved
     */
    public void saveCart(Cart cart);

    /**
     * Removes a cart
     *
     * @param cart - the cart to be removed
     */
    public void delete(Cart cart);
    
}
