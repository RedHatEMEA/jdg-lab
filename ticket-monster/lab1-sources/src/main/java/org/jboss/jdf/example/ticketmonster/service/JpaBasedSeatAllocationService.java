package org.jboss.jdf.example.ticketmonster.service;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;

import org.jboss.jdf.example.ticketmonster.model.Performance;
import org.jboss.jdf.example.ticketmonster.model.Seat;
import org.jboss.jdf.example.ticketmonster.model.SeatAllocationException;
import org.jboss.jdf.example.ticketmonster.model.Section;
import org.jboss.jdf.example.ticketmonster.model.SectionAllocation;

/**
 * JPA Based implementation of the {@link SeatAllocationService}.
 * 
 * 
 * @author Marius Bogoevici
 * @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@SuppressWarnings("serial")
@JpaBased
public class JpaBasedSeatAllocationService implements SeatAllocationService {

	@Inject
	EntityManager entityManager;

	public AllocatedSeats allocateSeats(Section section, Performance performance, int seatCount, boolean contiguous) {
		SectionAllocation sectionAllocation = retrieveSectionAllocationExclusively(section, performance);
		ArrayList<Seat> seats = sectionAllocation.allocateSeats(seatCount, contiguous);
		return new AllocatedSeats(sectionAllocation, seats);
	}

	public void deallocateSeats(Section section, Performance performance, List<Seat> seats) {
		SectionAllocation sectionAllocation = retrieveSectionAllocationExclusively(section, performance);
		for (Seat seat : seats) {
			if (!seat.getSection().equals(section)) {
				throw new SeatAllocationException("All seats must be in the same section!");
			}
			sectionAllocation.deallocate(seat);
		}
	}

	private SectionAllocation retrieveSectionAllocationExclusively(Section section, Performance performance) {
		SectionAllocation sectionAllocationStatus = (SectionAllocation) entityManager
				.createQuery(
						"select s from SectionAllocation s where " + "s.performance.id = :performanceId and " + "s.section.id = :sectionId")
				.setParameter("performanceId", performance.getId()).setParameter("sectionId", section.getId()).getSingleResult();
		entityManager.lock(sectionAllocationStatus, LockModeType.PESSIMISTIC_WRITE);
		return sectionAllocationStatus;
	}
	
	private SectionAllocation retrieveSectionAllocation(Section section, Performance performance) {
		SectionAllocation sectionAllocationStatus = (SectionAllocation) entityManager
				.createQuery(
						"select s from SectionAllocation s where " + "s.performance.id = :performanceId and " + "s.section.id = :sectionId")
				.setParameter("performanceId", performance.getId()).setParameter("sectionId", section.getId()).getSingleResult();
		return sectionAllocationStatus;
	}

	@Override
	public void finalizeAllocation(AllocatedSeats allocatedSeats) {
		allocatedSeats.markOccupied();
		
	}

	@Override
	public void finalizeAllocation(Performance performance, List<Seat> allocatedSeats) {
		SectionAllocation sectionAllocation = retrieveSectionAllocation(allocatedSeats.get(0).getSection(), performance);
        sectionAllocation.markOccupied(allocatedSeats);
		
	}
}
