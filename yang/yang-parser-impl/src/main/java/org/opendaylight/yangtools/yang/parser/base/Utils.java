package org.opendaylight.yangtools.yang.parser.base;

import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;

public class Utils {


    public static QName qNameFromArgument(StmtContext<?,?,?> ctx, String value) {

        String prefix = "";
        ctx.getFromNamespace(PrefixToModule.class, prefix);

        return QName.create(value);
    }


}
