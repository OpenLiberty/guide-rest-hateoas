// tag::comment[]
/*******************************************************************************
 * Copyright (c) 2017, 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial implementation
 *******************************************************************************/
 // end::comment[]
package io.openliberty.guides.microprofile.util;

import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.commons.lang3.StringUtils;

public class InventoryUtil {

    private static final int PORT = 9080;
    private static final String PROTOCOL = "http";
    private static final String SYSTEM_PROPERTIES = "/system/properties";

    public static JsonObject getProperties(String hostname) {
        Client client = ClientBuilder.newClient();
        URI propURI = InventoryUtil.buildUri(hostname);
        return client.target(propURI)
                     .request(MediaType.APPLICATION_JSON)
                     .get(JsonObject.class);
    }
    
    // tag::buildHostJson[]
    public static JsonObject buildHostJson(String hostname, String url) {
        return Json.createObjectBuilder()
                   // tag::hostname[]
                   .add("hostname", hostname)
                   // end::hostname[]
                   // tag::links[]
                   .add("_links", InventoryUtil.buildLinksForHost(hostname, url))
                   // end::links[]
                   .build();
    }
    // end::buildHostJson[]
    
    // tag::buildLinksForHost[]
    public static JsonArray buildLinksForHost(String hostname, String invUri) {
        
        JsonArrayBuilder links = Json.createArrayBuilder(); 
        
        links.add(Json.createObjectBuilder()
                      .add("href", StringUtils.appendIfMissing(invUri, "/") + hostname)
                      // tag::self[]
                      .add("rel", "self"));
                      // end::self[]
        
        links.add(Json.createObjectBuilder()
                .add("href", InventoryUtil.buildUri(hostname).toString())
                // tag::properties[]
                .add("rel", "properties"));
                // end::properties[]
        
        return links.build();
    }
    // end::buildLinksForHost[]
    
    public static boolean responseOk(String hostname) {
        try {
            URL target = new URL(buildUri(hostname).toString());
            HttpURLConnection http = (HttpURLConnection) target.openConnection();
            http.setConnectTimeout(50);
            int response = http.getResponseCode();
            return (response != 200) ? false : true;
        } catch (Exception e) {
            return false;
        }
    }

    private static URI buildUri(String hostname) {
        return UriBuilder.fromUri(SYSTEM_PROPERTIES)
                .host(hostname)
                .port(PORT)
                .scheme(PROTOCOL)
                .build();
    }

}
