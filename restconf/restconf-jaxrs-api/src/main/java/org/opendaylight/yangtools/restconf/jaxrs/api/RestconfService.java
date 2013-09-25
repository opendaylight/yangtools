/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.jaxrs.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.opendaylight.yangtools.restconf.common.MediaTypes;

/**
 *   The URI hierarchy for the RESTCONF resources consists of an entry
 *   point container, 3 top-level resources, and 1 field.  Refer to
 *  Section 5 for details on each URI.
 *    <ul>
 *    <li><b>/restconf</b> - {@link #getRoot()}
 *     <ul><li><b>/datastore</b> - {@link #getDatastores()}
 *         <ul>
 *            <li>/(top-level-data-nodes) (config=true or false)
 *         </ul>
 *         <li>/modules
 *          <ul><li>/module
 *              <li>/name
 *              <li>/revision
 *              <li>/namespace
 *              <li>/feature
 *             <li>/deviation
 *          </ul>
 *          <li>/operations
 *          <ul>
 *             <li>/(custom protocol operations)
 *          </ul>
 *         <li>/version (field)
 *     </ul>
 */
@Path("restconf")
public interface RestconfService {

    @GET
    public Object getRoot();
    
    @GET
    @Path("/datastore")
    @Produces(MediaTypes.API)
    public Object getDatastores();
    
    @GET
    @Path("/modules")
    public Object getModules();
}
