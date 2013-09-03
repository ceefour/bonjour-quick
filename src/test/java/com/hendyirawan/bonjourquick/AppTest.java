package com.hendyirawan.bonjourquick;

import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.jmdns.JmDNS;
import javax.jmdns.ServiceEvent;
import javax.jmdns.ServiceInfo;
import javax.jmdns.ServiceListener;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AppTest 
{
	private static final Logger log = LoggerFactory.getLogger(AppTest.class);

	/**
	 * Before Bonjour link-local/serverless XMPP (XEP-0174) can work, 
	 * at least Bonjour/JmDNS service discovery must work first.
	 * @throws UnknownHostException
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Test
	public void discover() throws UnknownHostException, IOException, InterruptedException {
		try (final JmDNS jmdns = JmDNS.create()) {
			final AtomicBoolean wasAdded = new AtomicBoolean();
			jmdns.addServiceListener("_presence._tcp.local.", new ServiceListener() {
				@Override
				public void serviceResolved(ServiceEvent event) {
					log.info("Resolved {}", event);
				}
				
				@Override
				public void serviceRemoved(ServiceEvent event) {
					log.info("Removed {}", event);
				}
				
				@Override
				public void serviceAdded(ServiceEvent event) {
					log.info("Added {}", event);
					wasAdded.set(true);
				}
			});
			final ServiceInfo[] presenceSvcs = jmdns.list("_presence._tcp.local.", 400);
			log.info("{} Presence: {}", presenceSvcs.length, presenceSvcs);
			assertThat(presenceSvcs.length, Matchers.greaterThan(0));
			assertTrue(wasAdded.get());
		}
	}
	
	@Test
	public void register() throws InterruptedException, IOException {
		try (final JmDNS jmdns = JmDNS.create()) {
			final ServiceInfo serviceInfo = ServiceInfo.create("_presence._tcp.local.",
	                "something@amanah", 2300, 0, 0, new HashMap<String, Object>());
			jmdns.registerService(serviceInfo);
			Thread.sleep(5000);
		}
	}
}
