package org.jboss.jdf.example.ticketmonster.rest;

import java.io.Serializable;

import org.jboss.jdf.example.ticketmonster.model.TicketPrice;

/**
 * <p>
 * A {@link BookingRequest} will contain multiple {@link TicketRequest}s.
 * </p>
 * 
 * @author Marius Bogoevici
 * @author Pete Muir
 * 
 */
public class TicketRequest implements Serializable {

    private TicketPrice ticketPrice;

    private int quantity;

    public TicketRequest() {
        // Empty constructor
    }

    public TicketRequest(TicketPrice ticketPrice, int quantity) {
        this.ticketPrice = ticketPrice;
        this.quantity = quantity;
    }

    public TicketPrice getTicketPrice() {
        return ticketPrice;
    }

    public void setTicketPrice(TicketPrice ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
