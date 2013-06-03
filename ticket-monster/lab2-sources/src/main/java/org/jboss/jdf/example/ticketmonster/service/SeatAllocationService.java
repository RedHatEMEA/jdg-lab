package org.jboss.jdf.example.ticketmonster.service;

import java.util.List;

import org.jboss.jdf.example.ticketmonster.model.Performance;
import org.jboss.jdf.example.ticketmonster.model.Seat;
import org.jboss.jdf.example.ticketmonster.model.Section;

/**
 *
 * Helper service for allocation seats.
 *
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
public interface SeatAllocationService {


    public static final String ALLOCATIONS = "TICKETMONSTER_ALLOCATIONS";

    
    public AllocatedSeats allocateSeats(Section section, Performance performance,
                                        int seatCount, boolean contiguous);

    public void deallocateSeats(Section section, Performance performance, List<Seat> seats);

    public void finalizeAllocation(AllocatedSeats allocatedSeats);

    /**
     * Mark the allocat
     * @param performance
     * @param allocatedSeats
     */
    public void finalizeAllocation(Performance performance, List<Seat> allocatedSeats);

    
}
