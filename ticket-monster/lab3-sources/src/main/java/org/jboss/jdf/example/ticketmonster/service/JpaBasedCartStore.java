package org.jboss.jdf.example.ticketmonster.service;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.jdf.example.ticketmonster.model.Cart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JPA based implementation of the {@link CartStore}.
 * 
 * @author @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 */
@JpaBased
public class JpaBasedCartStore implements CartStore {

	private static final Logger LOGGER = LoggerFactory.getLogger(JpaBasedCartStore.class);
	
	@Inject
	EntityManager entityManager;
	
	@Override
	public Cart getCart(String cartId) {
		Cart persistedCart = entityManager.find(Cart.class, cartId);
		return persistedCart;
	}

	@Override
	public void saveCart(Cart cart) {
		//using merge, as the detached entity might already exist.
		entityManager.merge(cart);
		
	}

	@Override
	public void delete(Cart cart) {
		/*
		SectionAllocation sectionAllocationStatus = (SectionAllocation) entityManager
				.createQuery(
						"select s from SectionAllocation s where " + "s.performance.id = :performanceId and " + "s.section.id = :sectionId")
				.setParameter("performanceId", performance.getId()).setParameter("sectionId", section.getId()).getSingleResult();
		entityManager.lock(sectionAllocationStatus, LockModeType.PESSIMISTIC_WRITE);
		return sectionAllocationStatus;
		*/
	
	
		int rowsUpdated = entityManager.createQuery("delete from Cart c where c.id = :cartId").setParameter("cartId", cart.getId()).executeUpdate();
		LOGGER.debug("Number of Cart rows delete is: " + rowsUpdated);
		
	}

}
