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

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonValue;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

// tag::class[]
public class EndpointIT {
    // tag::class-contents[]
    // tag::setup[]
    private String port;
    private String baseUrl;
    
    private Client client;
    
    private final String SYSTEM_PROPERTIES = "system/properties";
    private final String INVENTORY_HOSTS = "inventory/hosts";
    
    // tag::Before[]
    @BeforeEach
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
    @AfterEach
    // end::After[]
    public void teardown() {
        client.close();
    }
    // end::setup[]
    
    /**
     * Checks if the HATEOAS link for the inventory contents (hostname=*) is as expected.
     */
    // tag::testLinkForInventoryContents[]
    // tag::Test1[]
    @Test
    // end::Test1[]
    // tag::Order1[]
    @Order(1)
    // end::Order1[]
    public void testLinkForInventoryContents() {
        Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
        this.assertResponse(baseUrl, response);
        
        // tag::jsonobj[]
        JsonArray sysArray = response.readEntity(JsonArray.class);
        // end::jsonobj[]
        
        // tag::assertAndClose[]
        String expected, actual;
        boolean isFound = false;

        for (JsonValue hostValue : sysArray) {
            // Try to find the JSON object for hostname *
            JsonObject host = hostValue.asJsonObject();
            String hostname = host.getJsonString("hostname").getString();

            if (hostname.equals("*")) {
                JsonArray links = host.getJsonArray("_links");

                expected = baseUrl + INVENTORY_HOSTS + "/*";
                actual = links.getJsonObject(0).getString("href");
                assertEquals(expected, actual, "Incorrect href");

                // asserting that rel was correct
                expected = "self";
                actual = links.getJsonObject(0).getString("rel");
                assertEquals(expected, actual, "Incorrect rel");

                // Assuming rel and href were correct, mark that the correct host info was found
                isFound = true;
                break;
            }
        }

        // If the hostname '*' was not even found, need to fail the testcase
        assertTrue(isFound, "Could not find system with hostname *");
        
        response.close();
        // end::assertAndClose[]
    }
    // end::testLinkForInventoryContents[]
    
    /**
     * Checks that the HATEOAS links, with relationships 'self' and 'properties' for a simple 
     * localhost system is as expected.
     */
    // tag::testLinksForSystem[]
    // tag::Test2[]
    @Test
    // end::Test2[]
    // tag::Order2[]
    @Order(2)
    // end::Order2[]
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
        assertEquals(expected, actual, "Incorrect href");
        
        expected = "self";
        actual = links.getJsonObject(0).getString("rel");
        assertEquals(expected, actual, "Incorrect rel");
        
        // testing the 'properties' link
        expected = baseUrl + SYSTEM_PROPERTIES;
        actual = links.getJsonObject(1).getString("href");
        assertEquals(expected, actual, "Incorrect href");
        
        expected = "properties";
        actual = links.getJsonObject(1).getString("rel");
        assertEquals(expected, actual, "Incorrect rel");
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
        assertEquals(200, response.getStatus(), "Incorrect response code from " + url);;
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
