/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.restconf.common;

import java.util.HashMap;
import java.util.Map;
/**
 * 
 * 
 * Reference:
 * http://tools.ietf.org/html/draft-bierman-netconf-restconf-01#page-56
 * 
 * @author ttkacik
 *
 */
public enum ErrorTag {
    InUse ("in-use",409),
    InvalidValue ("invalid-value",400),
    TooBig ("too-big",413),
    MissingAttribute ("missing-attribute",400),
    BadAttribute ("bad-attribute",400),
    UnknownAttribute ("unknown-attribute",400),
    BadElement ("bad-element",400),
    UnknownElement ("unknown-element",400),
    UnknownNamespace ("unknown-namespace",400),
    AccessDenied ("access-denied",403),
    LockDenied ("lock-denied",409),
    ResourceDenied ("resource-denied",409),
    RollbackFailed ("rollback-failed",500),
    DataExists ("data-exists",409),
    DataMissing ("data-missing",409),
    OperationNotSupported ("operation-not-supported",501),
    OperationFailed ("operation-failed",500),
    PartialOperation ("partial-operation",500),
    MalformedMessage ("malformed-message",400);

    private ErrorTag(String tag,int httpStatus) {
        this.tag = tag;
        this.httpStatusCode = httpStatus;
    }
    
    private final String tag;
    private final int httpStatusCode;
    private static final Map<String,ErrorTag> tagToEnum = new HashMap<>();
    
    public String getTag() {
        return tag;
    }
    public int getHttpStatusCode() {
        return httpStatusCode;
    }
    
    public static ErrorTag fromString(String tag) {
        if(tagToEnum.isEmpty()) {
            initTagToEnum();
        }
        return tagToEnum.get(tag);
    }
    private static void initTagToEnum() {
        ErrorTag[] values = ErrorTag.values();
        for (ErrorTag errorTag : values) {
            tagToEnum.put(errorTag.getTag(), errorTag);
        }
    }
    
    
}
