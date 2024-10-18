/*
 * Copyright (c) 2024 PANTHEON.tech, s.r.o. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.ir;

import org.eclipse.jdt.annotation.NonNullByDefault;

/**
 * Constants related to {@code YANG IR} and its {@code YODL file format}  as defined in this package.
 */
@NonNullByDefault
public final class YodlConstants {
    /**
     * YODL File Extension.
     */
    public static final String YODL_FILE_EXTENSION = ".yodl";
    /**
     * Magic starting each {@code .yodl} file, in little-endian byte order. It is followed by an uint8 version field,
     */
    public static final int YODL_MAGICK = 0xAF57BA07;
    /**
     * YODL media type. This type has one parameter, {@code v}, which specifies the encoding version. If not present,
     * {@code v=1} is assumed.
     */
    // TODO: register a vendor media type, perhaps application/vnd.opendaylight.yodl? see:
    //       - Vendor Tree: https://www.rfc-editor.org/rfc/rfc6838.html#section-3.2
    //       - Application Media Types: https://www.rfc-editor.org/rfc/rfc6838.html#section-4.2.5
    //       - Registration form: https://www.iana.org/form/media-types
    public static final String YODL_MEDIA_TYPE = "application/x.yodl";
    /**
     * Initial YODL encoding. Sorry for Pascalesque counting, but a {@code 0x01} byte is (arguably) (a little) less
     * likely to be encountered than a {@code 0x00} byte. See {@link IOConstantsV1} for details on file encoding.
     */
    public static final String YODL_MEDIA_TYPE_V1 = YODL_MEDIA_TYPE + ";v=1";

    private YodlConstants() {
        // Hidden on purpose
    }
}
