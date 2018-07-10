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
package io.openliberty.guides.microprofile;

import java.util.Properties;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import io.openliberty.guides.microprofile.model.*;
import io.openliberty.guides.microprofile.util.InventoryUtil;

// tag::RequestScoped[]
@RequestScoped
// end::RequestScoped[]
@Path("/systems")
public class InventoryResource {

  // tag::Inject[]
  @Inject
  InventoryManager manager;
  // end::Inject[]

  @Context
  UriInfo uriInfo;

  @GET
  @Path("/{hostname}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getPropertiesForHost(@PathParam("hostname") String hostname) {
    if (hostname.equals("*")) {
        return Response.ok(manager.list()).build();
    } else {
        // Get properties for host
        Properties props = manager.get(hostname);
        if (props == null) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("ERROR: Unknown hostname or the system service may not be running on " + hostname)
                    .build();
        }

        // Add to inventory
        manager.add(hostname, props);
        return Response.ok(props).build();
    }
  }

  @GET
  @Produces(MediaType.APPLICATION_JSON)
  public JsonArray listContents() {
    String url = uriInfo.getAbsolutePath().toString();
    JsonArrayBuilder jsonArray = manager
                                .list()
                                .getSystems()
                                .stream()
                                .map(s -> InventoryUtil.buildHostJson(s.getHostname(), url))
                                .collect(Json::createArrayBuilder, JsonArrayBuilder::add, JsonArrayBuilder::add);

    JsonObject content = InventoryUtil.buildHostJson("*", url);
    jsonArray.add(content);

    return jsonArray.build();
  }
}
