/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * IETF YANG Module Tags Registry, as defined in
 * <a href="https://www.rfc-editor.org/rfc/rfc8819#section-7.2">RFC8819</a> section 7.2, table 2.
 *
 * <p>
 * This registry allocates tags that have the registered prefix "ietf:". New values should be well considered and not
 * achievable through a combination of already existing IETF tags. IANA assigned tags must conform to Net-Unicode as
 * defined in <a href="https://www.rfc-editor.org/rfc/rfc5198">RFC5198</a>, and they shall not need normalization.
 */
@NonNullByDefault
public final class IetfTags {
    /**
     * {@code ietf:network-element-class} {@link IetfTags}.
     * <pre>
     *    Network element as defined in <a href="https://www.rfc-editor.org/rfc/rfc8199">RFC8199</a>.
     * </pre>
     */
    public static final Tag NETWORK_ELEMENT_CLASS = new Tag("ietf:network-element-class").intern();
    /**
     * {@code ietf:network-service-class} {@link IetfTags}
     * <pre>
     *    Network element as defined in <a href="https://www.rfc-editor.org/rfc/rfc8199">RFC8199</a>.
     * </pre>
     */
    public static final Tag NETWORK_SERVICE_CLASS = new Tag("ietf:network-service-class").intern();
    /**
     * {@code ietf:sdo-defined-class} {@link IetfTags}
     * <pre>
     *    Module is defined by a standards organization.
     * </pre>
     */
    public static final Tag SDO_DEFINED_CLASS = new Tag("ietf:sdo-defined-class").intern();
    /**
     * {@code ietf:vendor-defined-class} {@link IetfTags}
     * <pre>
     *    Module is defined by a vendor.
     * </pre>
     */
    public static final Tag VENDOR_DEFINED_CLASS = new Tag("ietf:vendor-defined-class").intern();
    /**
     * {@code ietf:user-defined-class} {@link IetfTags}
     * <pre>
     *    Module is defined by the user.
     * </pre>
     */
    public static final Tag USER_DEFINED_CLASS = new Tag("ietf:user-defined-class").intern();
    /**
     * {@code ietf:hardware} {@link IetfTags}
     * <pre>
     *    Relates to hardware (e.g., inventory).
     * </pre>
     */
    public static final Tag HARDWARE = new Tag("ietf:hardware").intern();
    /**
     * {@code ietf:software} {@link IetfTags}
     * <pre>
     *    Relates to software (e.g., installed OS).
     * </pre>
     */
    public static final Tag SOFTWARE = new Tag("ietf:software").intern();
    /**
     * {@code ietf:protocol} {@link IetfTags}
     * <pre>
     *    Represents a protocol (often combined with another tag to refine).
     * </pre>
     */
    public static final Tag PROTOCOL = new Tag("ietf:protocol").intern();
    /**
     * {@code ietf:qos} {@link IetfTags}
     * <pre>
     *    Relates to quality of service.
     * </pre>
     */
    public static final Tag QOS = new Tag("ietf:qos").intern();
    /**
     * {@code ietf:network-service-app} {@link IetfTags}
     * <pre>
     *    Relates to a network service application (e.g., an NTP server, DNS server, DHCP server, etc.).
     * </pre>
     */
    public static final Tag NETWORK_SERVICE_APP = new Tag("ietf:network-service-app").intern();
    /**
     * {@code ietf:system-management} {@link IetfTags}
     * <pre>
     *    Relates to system management (e.g., a system management protocol such as syslog, TACAC+, SNMP, NETCONF, etc.).
     * </pre>
     */
    public static final Tag SYSTEM_MANAGEMENT = new Tag("ietf:system-management").intern();
    /**
     * {@code ietf:oam} {@link IetfTags}
     * <pre>
     *    Relates to Operations, Administration, and Maintenance (e.g., BFD).
     * </pre>
     */
    public static final Tag OAM = new Tag("ietf:oam").intern();
    /**
     * {@code ietf:routing} {@link IetfTags}
     * <pre>
     *    Relates to routing.
     * </pre>
     */
    public static final Tag ROUTING = new Tag("ietf:routing").intern();
    /**
     * {@code ietf:security} {@link IetfTags}
     * <pre>
     *    Relates to security.
     * </pre>
     */
    public static final Tag SECURITY = new Tag("ietf:security").intern();
    /**
     * {@code ietf:signaling} {@link IetfTags}
     * <pre>
     *    Relates to control-plane signaling.
     * </pre>
     */
    public static final Tag SIGNALING = new Tag("ietf:signaling").intern();
    /**
     * {@code ietf:link-management} {@link IetfTags}
     * <pre>
     *    Relates to link management.
     * </pre>
     */
    public static final Tag LINK_MANAGEMENT = new Tag("ietf:link-management").intern();

    private IetfTags() {
        // Hidden on purpose
    }
}
