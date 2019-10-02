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
package it.guide-rest-hateoas;

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
    private String port;
    private String baseUrl;
    
    private Client client;
    
    private final String SYSTEM_PROPERTIES = "system/properties";
    private final String INVENTORY_HOSTS = "inventory/hosts";
    
    // tag::Before[]
    @Before
    // end::Before[]
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
    // tag::After[]
    @After
    // end::After[]
    public void teardown() {
        client.close();
    }
    // end::setup[]
    
    // tag::testSuite[]
    // tag::Test[]
    @Test
    // end::Test[]
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
        String expected, actual;

        JsonArray links = sysArray.getJsonObject(0).getJsonArray("_links");

        expected = baseUrl + INVENTORY_HOSTS + "/*";
        actual = links.getJsonObject(0).getString("href");
        assertEquals("Incorrect href", expected, actual);
        
        // asserting that rel was correct
        expected = "self";
        actual = links.getJsonObject(0).getString("rel");
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
        
        Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
        this.assertResponse(baseUrl, response);
        
        JsonArray sysArray = response.readEntity(JsonArray.class);
        
        String expected, actual;

        JsonArray links = sysArray.getJsonObject(0).getJsonArray("_links");
        
        // testing the 'self' link

        expected = baseUrl + INVENTORY_HOSTS + "/localhost";
        actual = links.getJsonObject(0).getString("href");
        assertEquals("Incorrect href", expected, actual);
        
        expected = "self";
        actual = links.getJsonObject(0).getString("rel");
        assertEquals("Incorrect rel", expected, actual);
        
        // testing the 'properties' link
        
        expected = baseUrl + SYSTEM_PROPERTIES;
        actual = links.getJsonObject(1).getString("href");
        assertEquals("Incorrect href", expected, actual);
        
        expected = "properties";
        actual = links.getJsonObject(1).getString("rel");
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
        Response response = this.getResponse(baseUrl + SYSTEM_PROPERTIES);
        this.assertResponse(baseUrl, response);
        response.close();
        // tag::targetResponse[]
        Response targetResponse = client.target(baseUrl + INVENTORY_HOSTS + "/localhost")
                                        .request()
                                        .get();
        // end::targetResponse[]
        targetResponse.close();
    }
    // end::visitLocalhost[]
    // end::class-contents[]
}
// end::class[]