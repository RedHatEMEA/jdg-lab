/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.jdf.example.ticketmonster.monitor.client.shared;

import java.util.List;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * A service used by the bot.
 * 
 * Errai's @Remote annotation indicates that the Service implementation can
 * be used as an RPC endpoint and that this interface can be used on the
 * client for type safe method invocation on this endpoint.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Pete Muir
 */
@Remote
public interface BotService {
  
    /**
     * Start the bot. 
     * 
     */
    public void start();
    
    /**
     * Stop the bot.
     */
    public void stop();
    
    /**
     * Delete all bookings
     */
    public void deleteAll();

    /**
     * Get the log for the bot
     */
    public List<String> fetchLog(); 
}
