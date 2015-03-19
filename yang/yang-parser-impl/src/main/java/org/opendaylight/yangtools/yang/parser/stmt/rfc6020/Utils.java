/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import org.opendaylight.yangtools.yang.model.api.stmt.PrefixStatement;

import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import java.net.URISyntaxException;
import java.net.URI;
import java.util.Date;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;

public class Utils {


    public static QName qNameFromArgument(StmtContext<?,?,?> ctx, String value) {

        String prefix = null;
        QNameModule qNameModule = null;
        try {
            qNameModule = QNameModule.create(new URI(""), new Date(0));
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        String localName = null;

        String[] namesParts = value.split(":");
        switch (namesParts.length) {
        case 1:
            localName = namesParts[0];
            prefix = firstAttributeOf(ctx.getRoot().declaredSubstatements(),
                    PrefixStatement.class);
            qNameModule = ctx.getFromNamespace(PrefixToModule.class, prefix);
            break;
        case 2:
            prefix = namesParts[0];
            localName = namesParts[1];
            qNameModule = ctx.getFromNamespace(PrefixToModule.class, prefix);
            break;
        default:
            break;
        }

        return QName.create(qNameModule, localName);
    }


}
