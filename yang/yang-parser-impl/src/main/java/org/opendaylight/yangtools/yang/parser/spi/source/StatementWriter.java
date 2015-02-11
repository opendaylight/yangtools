package org.opendaylight.yangtools.yang.parser.spi.source;

import javax.annotation.Nonnull;
import org.opendaylight.yangtools.yang.common.QName;

public interface StatementWriter {

    void startStatement(@Nonnull QName name, @Nonnull StatementSourceReference ref);

    void argumentValue(@Nonnull String value,@Nonnull StatementSourceReference ref);

    void endStatement(@Nonnull StatementSourceReference ref);

}
