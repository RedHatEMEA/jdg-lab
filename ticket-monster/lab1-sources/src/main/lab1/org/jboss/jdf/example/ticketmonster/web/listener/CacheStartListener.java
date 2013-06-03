package org.jboss.jdf.example.ticketmonster.web.listener;

import javax.inject.Inject;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.infinispan.manager.EmbeddedCacheManager;
import org.jboss.jdf.example.ticketmonster.service.CacheBasedCartStore;

/**
 * Application Lifecycle Listener implementation class CacheStartListener
 * 
 */
public class CacheStartListener implements ServletContextListener {

	@Inject
	private EmbeddedCacheManager cacheManager;
	
	/**
	 * Default constructor.
	 */
	public CacheStartListener() {
	}

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent scEvent) {
		
		boolean startCachesOnStartup = Boolean.parseBoolean(scEvent.getServletContext().getInitParameter("startCachesOnStartup"));
		if (startCachesOnStartup == true) {
			cacheManager.startCaches(CacheBasedCartStore.CARTS_CACHE);
		}
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent arg0) {
		//Don't need to stop the cache as this is done by the Producer of the EmbeddedCacheManager on cleanup.
	}

}
