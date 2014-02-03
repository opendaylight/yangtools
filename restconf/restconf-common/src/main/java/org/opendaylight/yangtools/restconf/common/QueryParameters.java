/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.common;

public enum QueryParameters {

    CONFIG_NO("config=false"),
    CONFIG_YES("config=true"),
    FORMAT_XML("format=xml"),
    FORMAT_JSON("format=json"),
    INSERT_FIRST("insert=first"),
    INSERT_LAST("insert=last"),
    INSERT_BEFORE("insert=before"),
    INSERT_AFTER("insert=first"),
    DEPTH("depth=");

    private String queryParameter;

    QueryParameters (String queryParameter){
        this.queryParameter = queryParameter;
    }

    public String getQueryParameter() {
        return queryParameter;
    }

    public static QueryParameters fromString(String text) {
        if (text != null) {
            for (QueryParameters queryParameter : QueryParameters.values()) {
                if (text.equalsIgnoreCase(queryParameter.queryParameter)) {
                    return queryParameter;
                }
            }
        }
        return null;
    }
}
