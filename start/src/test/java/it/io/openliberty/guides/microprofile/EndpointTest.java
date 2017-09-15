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
package it.io.openliberty.guides.microprofile;

import static org.junit.Assert.*;

import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.cxf.jaxrs.provider.jsrjsonp.JsrJsonpProvider;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

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
        this.testEmptyInventory();
        this.testHostRegistration();
        this.testSystemPropertiesMatch();
        this.testUnknownHost();
    }
    // end::testSuite[]

    // tag::testEmptyInventory[]
    public void testEmptyInventory() {
        Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
        this.assertResponse(baseUrl, response);

        // tag::jsonobj[]
        JsonObject obj = response.readEntity(JsonObject.class);
        // end::jsonobj[]

        // tag::assertAndClose[]
        int expected = 0;
        int actual = obj.getInt("total");
        assertEquals("The systems should be empty", expected, actual);

        response.close();
        // end::assertAndClose[]
    }
    // end::testEmptyInventory[]

    // tag::testHostRegistration[]
    public void testHostRegistration() {
        this.visitLocalhost();

        Response invResponse = this.getResponse(baseUrl + INVENTORY_HOSTS);
        this.assertResponse(baseUrl, invResponse);

        JsonObject obj = invResponse.readEntity(JsonObject.class);

        int expected = 1;
        int actual = obj.getInt("total");
        assertEquals("The inventory must have one entry for localhost", expected, actual);

        boolean expectedLocalhost = true;
        boolean actualLocalhost = obj.getJsonObject("hosts").containsKey("localhost");
        assertEquals("A host was registered, but it was not localhost", expectedLocalhost, actualLocalhost);

        invResponse.close();
    }
    // end::testHostRegistration[]

    // tag::testSystemPropertiesMatch[]
    public void testSystemPropertiesMatch() {
        Response invResponse = this.getResponse(baseUrl + INVENTORY_HOSTS);
        Response sysResponse = this.getResponse(baseUrl + SYSTEM_PROPERTIES);
        this.assertResponse(baseUrl, invResponse);
        this.assertResponse(baseUrl, sysResponse);

        JsonObject jsonFromInventory = invResponse.readEntity(JsonObject.class)
                                                  .getJsonObject("hosts")
                                                  .getJsonObject("localhost");
        JsonObject jsonFromSystem = sysResponse.readEntity(JsonObject.class);

        String osNameFromInventory = jsonFromInventory.getString("os.name");
        String osNameFromSystem = jsonFromSystem.getString("os.name");

        String userNameFromInventory = jsonFromInventory.getString("user.name");
        String userNameFromSystem = jsonFromSystem.getString("user.name");

        this.assertProperty("os.name", osNameFromSystem, osNameFromInventory);
        this.assertProperty("user.name", userNameFromSystem, userNameFromInventory);

        invResponse.close();
        sysResponse.close();
    }
    // end::testSystemPropertiesMatch[]

    // tag::testUnknownHost[]
    public void testUnknownHost() {
        Response response = this.getResponse(baseUrl + INVENTORY_HOSTS);
        this.assertResponse(baseUrl, response);

        Response badResponse = client.target(baseUrl + INVENTORY_HOSTS + "/" + "badhostname")
                                      .request(MediaType.APPLICATION_JSON)
                                      .get();

        JsonObject obj = badResponse.readEntity(JsonObject.class);

        boolean expected = true;
        boolean actual = obj.containsKey("ERROR");
        assertEquals("[badhostname] is invalid but didn't raise an error", expected, actual);

        response.close();
        badResponse.close();
    }
    // end::testUnknownHost[]

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
     * Asserts that the specified property is equivalent in both the System service
     * and the Inventory service.
     * @param expected - System service value
     * @param actual   - Inventory service value
     */
    // tag::assertProperty[]
    private void assertProperty(String propertyName, String expected, String actual) {
        assertEquals("System property [" + propertyName + "] "
                + "from the System service does not match the one in "
                + "the Inventory service for localhost",
                expected, actual);
    }
    // end::assertProperty[]

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
