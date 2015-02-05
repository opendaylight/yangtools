package org.opendaylight.yangtools.yang.model.api.stmt;

@Rfc6020AbnfRule("revision-stmts")
public interface RevisionGroup {

    Iterable<? extends RevisionStatement> getRevisions();

}
