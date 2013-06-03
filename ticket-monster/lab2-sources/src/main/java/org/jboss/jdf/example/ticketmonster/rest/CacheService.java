package org.jboss.jdf.example.ticketmonster.rest;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.infinispan.AdvancedCache;
import org.infinispan.Cache;
import org.infinispan.distexec.mapreduce.Collector;
import org.infinispan.distexec.mapreduce.MapReduceTask;
import org.infinispan.distexec.mapreduce.Mapper;
import org.infinispan.distexec.mapreduce.Reducer;
import org.infinispan.distribution.DistributionManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.remoting.transport.Address;
import org.jboss.jdf.example.ticketmonster.model.Cart;
import org.jboss.jdf.example.ticketmonster.model.Seat;
import org.jboss.jdf.example.ticketmonster.model.SeatAllocation;
import org.jboss.jdf.example.ticketmonster.service.CacheBasedCartStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple RESTful Cache Service.
 * 
 * @author @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
 *
 */
@Path("/caches")
public class CacheService implements Serializable {

	/**
	 * SerialVersionUID.
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger LOGGER = LoggerFactory.getLogger(CacheService.class);

	@Inject
	private EmbeddedCacheManager cacheManager;

	@GET
	@Path("/{cacheName}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCacheEntries(@PathParam("cacheName") String cacheName) {
		Response response;
		// We only support the Carts Cache.
		if (CacheBasedCartStore.CARTS_CACHE.equals(cacheName)) {
			Cache<String, Object> cache = cacheManager.getCache(cacheName);
			Set<Map.Entry<String, Object>> entrySet;
			//test if we have a distributed cache.
			AdvancedCache<String, Object> advancedCache = cache.getAdvancedCache();
			if (advancedCache.getCacheConfiguration().clustering().cacheMode().isDistributed()) {
				entrySet = getDistCacheEntrySet(cache);
			} else { 
				//TODO: If this is an invalidation cache, this might not return all the keys either ...
				entrySet = cache.entrySet();
			}
			//TODO: We need to find a proper way to map Carts and SeatAllocation data to JSON or XML.
			List<Object> entryKeys = new ArrayList<Object>();
			
			for (Map.Entry<String, Object> nextEntry: entrySet) {
				Object value = nextEntry.getValue();
				if (value instanceof Cart) {
					StringBuilder addressesBuilder = new StringBuilder();
					
					//Get the address of the key (if this is a dist cache)
					DistributionManager distManager = advancedCache.getDistributionManager();
					if (distManager != null) {
						List<Address> addresses = distManager.locate(nextEntry.getKey());
						Iterator<Address> addressesIterator = addresses.iterator();
						while (addressesIterator.hasNext()) {
							Address nextAddress = addressesIterator.next();
							addressesBuilder.append(nextAddress.toString());
							if (addressesIterator.hasNext()) {
								addressesBuilder.append(";");
							}
						}
					}
					entryKeys.add(new CartCacheEntry(nextEntry.getKey(),(Cart) value, addressesBuilder.toString()));
				} else {
					LOGGER.warn("Retrieved a non-Cart object");
				}
			}
			response = Response.ok(entryKeys).build();
			
		} else {
			ResponseBuilder responseBuilder = Response.status(Status.NOT_FOUND);
			response = responseBuilder.build();
			
		}

		return response;
	}
	
	@GET
	@Path("/{cacheName}/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getCacheEntry(@PathParam("cacheName") String cacheName, @PathParam("key") String key) {
		Response response;
		// We only support the Carts Cache.
		if (CacheBasedCartStore.CARTS_CACHE.equals(cacheName)) {
			Cache<String, Object> cache = cacheManager.getCache(cacheName);
			Object value = cache.get(key);
			response = Response.ok(value).build();
		} else {
			ResponseBuilder responseBuilder = Response.status(Status.NOT_FOUND);
			response = responseBuilder.build();
		}
		return response;
	}
	
	
	
	@GET
	@Path("/{cacheName}/count")
	@Produces(MediaType.APPLICATION_JSON)
	public Response count(@PathParam("cacheName") String cacheName) {
		Response response;
		// We only support the Carts Cache eand the Allocations Cache
		if (CacheBasedCartStore.CARTS_CACHE.equals(cacheName)) {
			Cache<String, Object> cache = cacheManager.getCache(cacheName);
			Set<Map.Entry<String, Object>> entrySet;
			//test if we have a distributed cache.
			AdvancedCache<String, Object> advancedCache = cache.getAdvancedCache();
			if (advancedCache.getCacheConfiguration().clustering().cacheMode().isDistributed()) {
				entrySet = getDistCacheEntrySet(cache);
			} else { 
				//TODO: If this is an invalidation cache, this might not return all the keys either ...
				entrySet = cache.entrySet();
			}
			Map<String, Long> result = new HashMap<String, Long>();
			result.put("count", new Integer(entrySet.size()).longValue());
			response = Response.ok(result).build();
			
		} else {
			ResponseBuilder responseBuilder = Response.status(Status.NOT_FOUND);
			response = responseBuilder.build();
		}

		return response;
	}
	
	private class CartCacheEntry implements Serializable {
		/**
		 * SerialVersionUID. 
		 */
		private static final long serialVersionUID = 1L;

		private String cacheKey;
		
		private Long performanceId;
		
		private Date performanceDate;
		
		private int numberOfReservedSeats;
		
		private String addresses;
		
		public CartCacheEntry(String cacheKey, Cart cart, String addresses) {
			this.cacheKey = cacheKey;
			
			this.performanceId = cart.getPerformance().getId();
			this.performanceDate = cart.getPerformance().getDate();
			this.numberOfReservedSeats = getNumberOfReservedSeats(cart);
			this.addresses = addresses;
		}
		
		private int getNumberOfReservedSeats(Cart cart) {
			int reservedSeats = 0;
			ArrayList<SeatAllocation> seatAllocations = cart.getSeatAllocations();
			for (SeatAllocation nextSeatAllocation: seatAllocations) {
				ArrayList<Seat> seats = nextSeatAllocation.getAllocatedSeats();
				for (Seat nextSeat: seats) {
					reservedSeats++;
				}
			}
			return reservedSeats;
		}
				
		public String getAddresses() {
			return addresses;
		}

		public void setAddresses(String addresses) {
			this.addresses = addresses;
		}

		public void setCacheKey(String cacheKey) {
			this.cacheKey = cacheKey;
		}
		
		public String getCacheKey() {
			return cacheKey;
		}

		public Long getPerformanceId() {
			return performanceId;
		}

		public void setPerformanceId(Long performanceId) {
			this.performanceId = performanceId;
		}

		public Date getPerformanceDate() {
			return performanceDate;
		}

		public void setPerformanceDate(Date performanceDate) {
			this.performanceDate = performanceDate;
		}

		public int getNumberOfReservedSeats() {
			return numberOfReservedSeats;
		}

		public void setNumberOfReservedSeats(int numberOfReservedSeats) {
			this.numberOfReservedSeats = numberOfReservedSeats;
		}
	}
	
	
	private Set<Map.Entry<String, Object>> getDistCacheEntrySet(Cache cache) {
		// Use MapReduce to collect all key/values from the distributed cache.
		MapReduceTask<String, Object, String, Object> entrySetCollector = new MapReduceTask<String, Object, String, Object>(cache);

		entrySetCollector.mappedWith(new RetrieveFullMapMapper()).reducedWith(new RetrieveFullMapReducer());
		// This Map/Reduce task should give us a Map containing all the key/value pairs in thed distributed cache.
		Map<String, Object> distributedCacheMap = entrySetCollector.execute();
		Set<Map.Entry<String, Object>> entrySet = distributedCacheMap.entrySet();
		return entrySet;
	}

	/**
	 * Very simple {@link Mapper} implementation which copies the key/value pairs one-on-one to the Collector.
	 * 
	 * @author @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
	 */
	private class RetrieveFullMapMapper implements Mapper<String, Object, String, Object> {

		@Override
		public void map(String key, Object value, Collector<String, Object> collector) {
			// We will just pass the keys and values one-on-one to the Collector. We just want to collect the full EntrySet.
			collector.emit(key, value);
		}
	}

	/**
	 * Very simple {@link Reducer} which directly output the value.
	 * <p/>
	 * Note that we do a check whether we have one-and-only-one value (which should be the case) and log an error when we don't.
	 * 
	 * @author @author <a href="mailto:duncan.doyle@redhat.com">Duncan Doyle</a>
	 */
	private class RetrieveFullMapReducer implements Reducer<String, Object> {

		@Override
		public Object reduce(String reducedKey, Iterator<Object> iter) {
			/*
			 * Reduced key is still our original key, so if we should have only one entry in the iterartor for each key. We do a check
			 * however to see whether this is correct and log a warning otherwise.
			 */
			Object value = null;
			if (iter.hasNext()) {
				value = iter.next();
				if (iter.hasNext()) {
					LOGGER.warn("Huhhhh? We've got multiple values for the same key? Key: " + reducedKey);
				}
			} else {
				LOGGER.warn("Huhhh? We've got a key, but no value? Key: " + reducedKey);
			}

			return value;
		}

	}

}
