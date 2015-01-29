package org.opendaylight.yangtools.yang.model.api.stmt;

import java.util.Collection;

@Rfc6020AbnfRule("revision-stmts")
public interface RevisionGroup {

    Collection<? extends RevisionStatement> getRevisions();

}
