package org.jboss.jdf.example.ticketmonster.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

/**
 * <p>
 *     A Cart contains tickets that the user has reserved for purchase in this session.
 * </p>
 *
 * @author Marius Bogoevici
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"id"}))
public class Cart implements Serializable  {

	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	@Id
    private String id;
	
	 /**
     * <p>
     * The version used to optimistically lock this entity.
     * </p>
     * DGD: Note that in the current implementation, a cart can only contain tickets for a single performance.
     * 
     * <p>
     * Adding this field enables optimistic locking. As we don't access this field in the application, we need to suppress the
     * warnings the java compiler gives us about not using the field!
     * </p>
     */
    @SuppressWarnings("unused")
    @Version
    private long version;

    @ManyToOne
    private Performance performance;

    @Lob
    private ArrayList<SeatAllocation> seatAllocations = new ArrayList<SeatAllocation>();

    /**
     * Constructor for deserialization
     */
    public Cart() {
    }

    private Cart(String id) {
        this.id = id;
    }

    public static Cart initialize() {
        return new Cart(UUID.randomUUID().toString());
    }

    public String getId() {
        return id;
    }

    public Performance getPerformance() {
        return performance;
    }

    public void setPerformance(Performance performance) {
        this.performance = performance;
    }

    public ArrayList<SeatAllocation> getSeatAllocations() {
        return seatAllocations;
    }
}
