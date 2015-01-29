package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Rfc6020AbnfRule("module-header-stmts")
public interface ModuleHeaderGroup {

    @Nullable YangVersionStatement getYangVersion();
    @Nonnull NamespaceStatement getNamespace();
    @Nonnull String getPrefix();

}
