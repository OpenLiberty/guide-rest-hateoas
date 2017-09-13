// tag::comment[]
/*******************************************************************************
 * Copyright (c) 2017 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
 // end::comment[]
package it.io.openliberty.guides.hypermediarest;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonArray;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;

// tag::class[]
public class EndpointTest {
    // tag::class-contents[]
    // tag::setup[]
    private String sysPort;
    private String invPort;
    private String sysBaseUrl;
    private String invBaseUrl;
    
    private Client client;
    
    private final String SYSTEM_PROPERTIES = "System/properties";
    private final String INVENTORY_SYSTEMS = "Inventory/systems";
    
    @Before
    public void setup() {
        // tag::urlCreation[]
        sysPort = System.getProperty("sys.test.port");
        invPort = System.getProperty("inv.test.port");
        String war = System.getProperty("war.name");
        sysBaseUrl = "http://localhost:" + sysPort + "/" + war + "/";
        invBaseUrl = "http://localhost:" + invPort + "/" + war + "/";
        // end::urlCreation[]
        
        // tag::clientInit[]
        client = ClientBuilder.newClient();
        client.register(JsrJsonpProvider.class);
        // end::clientInit[]
    }
    
    @After
    public void teardown() {
        client.close();
    }
    // end::setup[]
    
    // tag::testSuite[]
    @Test
    public void testSuite() {
        this.testLinkForInventoryContents();
        this.testLinksForSystem();
    }
    // end::testSuite[]
    
    /**
     * Checks if the HATEOAS link for the inventory contents (hostname=*) is as expected.
     */
    // tag::testLinkForInventoryContents[]
    public void testLinkForInventoryContents() {
        Response response = this.getResponse(invBaseUrl + INVENTORY_SYSTEMS);
        this.assertResponse(invBaseUrl, response);
        
        // tag::jsonobj[]
        JsonArray sysArray = response.readEntity(JsonArray.class);
        // end::jsonobj[]
        
        // tag::assertAndClose[]
        String expected, actual;

        expected = invBaseUrl + INVENTORY_SYSTEMS + "/*";
        actual = sysArray.getJsonObject(0).getJsonArray("_links").getJsonObject(0).getString("href");
        assertEquals("Incorrect href", expected, actual);
        
        // asserting that rel was correct
        expected = "self";
        actual = sysArray.getJsonObject(0).getJsonArray("_links").getJsonObject(0).getString("rel");
        assertEquals("Incorrect rel", expected, actual);
        
        response.close();
        // end::assertAndClose[]
    }
    // end::testLinkForInventoryContents[]
    
    /**
     * Checks that the HATEOAS links, with relationships 'self' and 'properties' for a simple 
     * localhost system is as expected.
     */
    // tag::testLinksForSystem[]
    public void testLinksForSystem() {
        this.visitLocalhost();
        
        Response response = this.getResponse(invBaseUrl + INVENTORY_SYSTEMS);
        this.assertResponse(invBaseUrl, response);
        
        JsonArray sysArray = response.readEntity(JsonArray.class);
        
        String expected, actual;
        
        // testing the 'self' link

        expected = invBaseUrl + INVENTORY_SYSTEMS + "/localhost";
        actual = sysArray.getJsonObject(0).getJsonArray("_links").getJsonObject(0).getString("href");
        assertEquals("Incorrect href", expected, actual);
        
        expected = "self";
        actual = sysArray.getJsonObject(0).getJsonArray("_links").getJsonObject(0).getString("rel");
        assertEquals("Incorrect rel", expected, actual);
        
        // testing the 'properties' link
        
        expected = sysBaseUrl + SYSTEM_PROPERTIES;
        actual = sysArray.getJsonObject(0).getJsonArray("_links").getJsonObject(1).getString("href");
        assertEquals("Incorrect href", expected, actual);
        
        expected = "properties";
        actual = sysArray.getJsonObject(0).getJsonArray("_links").getJsonObject(1).getString("rel");
        assertEquals("Incorrect rel", expected, actual);
    }
    // end::testLinksForSystem[]
    
    /**
     * Returns a Response object for the specified URL.
     */
    // tag::getResponse[]
    private Response getResponse(String url) {
        return client.target(url).request().get();
    }
    // end::getResponse[]
    
    /**
     * Asserts that the given URL has the correct (200) response code.
     */
    // tag::assertResponse[]
    private void assertResponse(String url, Response response) {
        assertEquals("Incorrect response code from " + url, 200, response.getStatus());;
    }
    // end::assertResponse[]
    
    /**
     * Makes a GET request to localhost at the Inventory service.
     */
    // tag::visitLocalhost[]
    private void visitLocalhost() {
        Response response = this.getResponse(sysBaseUrl + SYSTEM_PROPERTIES);
        this.assertResponse(sysBaseUrl, response);
        response.close();
            
        Response targetResponse = client.target(invBaseUrl + INVENTORY_SYSTEMS + "/localhost")
                                        .request()
                                        .get();
        targetResponse.close();
    }
    // end::visitLocalhost[]
    // end::class-contents[]
}
// end::class[]
