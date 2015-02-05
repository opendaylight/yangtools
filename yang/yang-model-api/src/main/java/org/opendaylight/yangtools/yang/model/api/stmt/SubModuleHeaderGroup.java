package org.opendaylight.yangtools.yang.model.api.stmt;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@Rfc6020AbnfRule("submodule-header-stmts")
public interface SubModuleHeaderGroup {

    @Nullable YangVersionStatement getYangVersion();

    @Nonnull BelongsToStatement getBelongsTo();

    @Nullable Iterable<? extends UnknownStatement> getStmtSeps();
}
