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
package it.io.openliberty.guides.hateoas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;

// tag::class[]
public class EndpointTest {
    // tag::class-contents[]
    // tag::setup[]
    private String port;
    private String baseUrl;
    
    private Client client;
    
    private final String SYSTEM_PROPERTIES = "system/properties";
    private final String INVENTORY_HOSTS = "inventory/hosts";
    
    @Before
    public void setup() {
        // tag::urlCreation[]
        port = System.getProperty("liberty.test.port");
        baseUrl = "http://localhost:" + port + "/";
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
        Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
        this.assertResponse(baseUrl, response);
        
        // tag::jsonobj[]
        JsonArray sysArray = response.readEntity(JsonArray.class);
        // end::jsonobj[]
        
        // tag::assertAndClose[]
        JsonObject links = sysArray.getJsonObject(0).getJsonObject("_links");

        // Asserting that the self relationship actually exists.
        assertTrue("No 'self' relationship exists", links.containsKey("self"));
        
        // Asserting that the self link was as expected.
        String expected = baseUrl + INVENTORY_HOSTS + "/*";
        String actual = links.getString("self");
        assertEquals("'self' relationship contains incorrect link", expected, actual);
        
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
        
        Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
        this.assertResponse(baseUrl, response);
        
        JsonArray sysArray = response.readEntity(JsonArray.class);
        
        String expected, actual;

        JsonObject links = sysArray.getJsonObject(0).getJsonObject("_links");
        
        // Testing 'self'.
        assertTrue("No 'self' relationship exists", links.containsKey("self"));

        expected = baseUrl + INVENTORY_HOSTS + "/localhost";
        actual = links.getString("self");
        assertEquals("'self' relationship contains incorrect link", expected, actual);

        // Testing 'properties'.
        assertTrue("No 'properties' relationship exists", links.containsKey("properties"));
        
        expected = baseUrl + SYSTEM_PROPERTIES;
        actual = links.getString("properties");
        assertEquals("'properties' relationship contains incorrect link", expected, actual);
        
        response.close();
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
        Response response = this.getResponse(baseUrl + SYSTEM_PROPERTIES);
        this.assertResponse(baseUrl, response);
        response.close();
            
        Response targetResponse = client.target(baseUrl + INVENTORY_HOSTS + "/localhost")
                                        .request()
                                        .get();
        targetResponse.close();
    }
    // end::visitLocalhost[]
    // end::class-contents[]
}
// end::class[]