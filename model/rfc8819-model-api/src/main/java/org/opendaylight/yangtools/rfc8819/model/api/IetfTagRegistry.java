/*
 * Copyright (c) 2022 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.rfc8819.model.api;

import org.eclipse.jdt.annotation.NonNull;

/**
 * IETF YANG Module Tags Registry, as defined in
 * <a href="https://datatracker.ietf.org/doc/html/rfc8819#section-7.2">RFC8819</a> section 7.2, table 2.
 *
 * <p>
 * This registry allocates tags that have the registered prefix "ietf:".
 * New values should be well considered and not achievable through a
 * combination of already existing IETF tags.  IANA assigned tags must
 * conform to Net-Unicode as defined in
 * <a href="https://datatracker.ietf.org/doc/html/rfc5198">RFC5198</a>, and they shall not
 * need normalization.
 * </p>
 */
public final class IetfTagRegistry {
    /**
     * {@code ietf:network-element-class} {@link IetfTagRegistry}.
     * <pre>
     *    Network element as defined in <a href="https://datatracker.ietf.org/doc/html/rfc8199">RFC8199</a>.
     * </pre>
     */
    public static final @NonNull Tag NETWORK_ELEMENT_CLASS = new Tag(TagPrefix.IETF, "network-element-class");
    /**
     * {@code ietf:network-service-class} {@link IetfTagRegistry}
     * <pre>
     *    Network element as defined in <a href="https://datatracker.ietf.org/doc/html/rfc8199">RFC8199</a>.
     * </pre>
     */
    public static final @NonNull Tag NETWORK_SERVICE_CLASS = new Tag(TagPrefix.IETF, "network-service-class");
    /**
     * {@code ietf:sdo-defined-class} {@link IetfTagRegistry}
     * <pre>
     *    Module is defined by a standards organization.
     * </pre>
     */
    public static final @NonNull Tag SDO_DEFINED_CLASS = new Tag(TagPrefix.IETF, "sdo-defined-class");
    /**
     * {@code ietf:vendor-defined-class} {@link IetfTagRegistry}
     * <pre>
     *    Module is defined by a vendor.
     * </pre>
     */
    public static final @NonNull Tag VENDOR_DEFINED_CLASS = new Tag(TagPrefix.IETF, "vendor-defined-class");
    /**
     * {@code ietf:user-defined-class} {@link IetfTagRegistry}
     * <pre>
     *    Module is defined by the user.
     * </pre>
     */
    public static final @NonNull Tag USER_DEFINED_CLASS = new Tag(TagPrefix.IETF, "user-defined-class");
    /**
     * {@code ietf:hardware} {@link IetfTagRegistry}
     * <pre>
     *    Relates to hardware (e.g., inventory).
     * </pre>
     */
    public static final @NonNull Tag HARDWARE = new Tag(TagPrefix.IETF, "hardware");
    /**
     * {@code ietf:software} {@link IetfTagRegistry}
     * <pre>
     *    Relates to software (e.g., installed OS).
     * </pre>
     */
    public static final @NonNull Tag SOFTWARE = new Tag(TagPrefix.IETF, "software");
    /**
     * {@code ietf:protocol} {@link IetfTagRegistry}
     * <pre>
     *    Represents a protocol (often combined with another tag to refine).
     * </pre>
     */
    public static final @NonNull Tag PROTOCOL = new Tag(TagPrefix.IETF, "protocol");
    /**
     * {@code ietf:qos} {@link IetfTagRegistry}
     * <pre>
     *    Relates to quality of service.
     * </pre>
     */
    public static final @NonNull Tag QOS = new Tag(TagPrefix.IETF, "qos");
    /**
     * {@code ietf:network-service-app} {@link IetfTagRegistry}
     * <pre>
     *    Relates to a network service application (e.g., an NTP server, DNS server, DHCP server, etc.).
     * </pre>
     */
    public static final @NonNull Tag NETWORK_SERVICE_APP = new Tag(TagPrefix.IETF, "network-service-app");
    /**
     * {@code ietf:system-management} {@link IetfTagRegistry}
     * <pre>
     *    Relates to system management (e.g., a system management protocol such as syslog, TACAC+, SNMP, NETCONF, etc.).
     * </pre>
     */
    public static final @NonNull Tag SYSTEM_MANAGEMENT = new Tag(TagPrefix.IETF, "system-management");
    /**
     * {@code ietf:oam} {@link IetfTagRegistry}
     * <pre>
     *    Relates to Operations, Administration, and Maintenance (e.g., BFD).
     * </pre>
     */
    public static final @NonNull Tag OAM = new Tag(TagPrefix.IETF, "oam");
    /**
     * {@code ietf:routing} {@link IetfTagRegistry}
     * <pre>
     *    Relates to routing.
     * </pre>
     */
    public static final @NonNull Tag ROUTING = new Tag(TagPrefix.IETF, "routing");
    /**
     * {@code ietf:security} {@link IetfTagRegistry}
     * <pre>
     *    Relates to security.
     * </pre>
     */
    public static final @NonNull Tag SECURITY = new Tag(TagPrefix.IETF, "security");
    /**
     * {@code ietf:signaling} {@link IetfTagRegistry}
     * <pre>
     *    Relates to control-plane signaling.
     * </pre>
     */
    public static final @NonNull Tag SIGNALING = new Tag(TagPrefix.IETF, "signaling");
    /**
     * {@code ietf:link-management} {@link IetfTagRegistry}
     * <pre>
     *    Relates to link management.
     * </pre>
     */
    public static final @NonNull Tag LINK_MANAGEMENT = new Tag(TagPrefix.IETF, "link-management");

    private IetfTagRegistry() {
        // Hidden on purpose
    }
}
