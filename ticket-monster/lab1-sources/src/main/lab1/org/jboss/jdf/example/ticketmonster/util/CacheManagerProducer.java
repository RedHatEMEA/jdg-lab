package org.jboss.jdf.example.ticketmonster.util;

import java.io.IOException;
import java.io.InputStream;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Disposes;
import javax.enterprise.inject.Produces;

import org.infinispan.manager.DefaultCacheManager;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.util.Util;

import com.google.inject.Inject;

public class CacheManagerProducer {

	@Inject
	@DataDir
	private String dataDir;

	private static final String INFINISPAN_CONFIG_FILE_NAME = "infinispan/infinispan.xml";
	
	/**
	 * Loads the CacheManager using Infinispan Config File.
	 * 
	 * @return
	 */
	@Produces
	@ApplicationScoped
	public EmbeddedCacheManager getCacheContainer() {
		EmbeddedCacheManager cacheManager = null;
		// Retrieve Infinispan config file.
		InputStream infinispanConfigStream = this.getClass().getClassLoader().getResourceAsStream(INFINISPAN_CONFIG_FILE_NAME);
		try {
			try {
				cacheManager = new DefaultCacheManager(infinispanConfigStream);
			} catch (IOException ioe) {
				throw new RuntimeException("Error loading Infinispan CacheManager.", ioe);
			}
		} finally {
			// Use Infinispan Util class to flush and close stream.
			Util.close(infinispanConfigStream);
		}

		return cacheManager;
	}
	
	public void cleanUp(@Disposes EmbeddedCacheManager manager) {
		manager.stop();
	}
	
}
