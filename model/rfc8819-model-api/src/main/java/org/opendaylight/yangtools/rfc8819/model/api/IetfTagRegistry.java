/*
 * Copyright (c) 2022 Pantheon Technologies, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;


import org.eclipse.jdt.annotation.NonNull;

public final class IetfTagRegistry {
    public static final @NonNull Tag NETWORK_ELEMENT_CLASS = new Tag(TagPrefix.IETF, "network-element-class");
    public static final @NonNull Tag NETWORK_SERVICE_CLASS = new Tag(TagPrefix.IETF, "network-service-class");
    public static final @NonNull Tag SDO_DEFINED_CLASS = new Tag(TagPrefix.IETF, "sdo-defined-class");
    public static final @NonNull Tag VENDOR_DEFINED_CLASS = new Tag(TagPrefix.IETF, "vendor-defined-class");
    public static final @NonNull Tag USER_DEFINED_CLASS = new Tag(TagPrefix.IETF, "user-defined-class");
    public static final @NonNull Tag HARDWARE = new Tag(TagPrefix.IETF, "hardware");
    public static final @NonNull Tag SOFTWARE = new Tag(TagPrefix.IETF, "software");
    public static final @NonNull Tag PROTOCOL = new Tag(TagPrefix.IETF, "protocol");
    public static final @NonNull Tag QOS = new Tag(TagPrefix.IETF, "qos");
    public static final @NonNull Tag NETWORK_SERVICE_APP = new Tag(TagPrefix.IETF, "network-service-app");
    public static final @NonNull Tag SYSTEM_MANAGEMENT = new Tag(TagPrefix.IETF, "system-management");
    public static final @NonNull Tag OAM = new Tag(TagPrefix.IETF, "oam");
    public static final @NonNull Tag ROUTING = new Tag(TagPrefix.IETF, "routing");
    public static final @NonNull Tag SECURITY = new Tag(TagPrefix.IETF, "security");
    public static final @NonNull Tag SIGNALING = new Tag(TagPrefix.IETF, "signaling");
    public static final @NonNull Tag LINK_MANAGEMENT = new Tag(TagPrefix.IETF, "link-management");
}
