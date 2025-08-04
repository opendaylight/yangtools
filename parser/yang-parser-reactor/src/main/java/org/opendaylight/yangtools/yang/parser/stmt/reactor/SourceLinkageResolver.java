package org.opendaylight.yangtools.yang.parser.stmt.reactor;

import static java.util.Objects.requireNonNull;

import com.google.common.collect.ImmutableSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.YangConstants;
import org.opendaylight.yangtools.yang.ir.IRArgument;
import org.opendaylight.yangtools.yang.ir.IRStatement;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDeclaration;
import org.opendaylight.yangtools.yang.model.spi.meta.StatementDeclarations;
import org.opendaylight.yangtools.yang.model.spi.source.SourceInfo;

public class SourceLinkageResolver {


    private final BuildGlobalContext globalContext;

    public SourceLinkageResolver(final BuildGlobalContext globalContext) {
        this.globalContext = globalContext;
    }

//    private void linkInvolvedSources(final Map<Unqualified, InvolvedSource> involvedSources) {
//        QNameToStatementDefinitionMap stmtDefs = new QNameToStatementDefinitionMap();
////        stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_LINKAGE).getCommonDefinitions());
//        stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE).getCommonDefinitions());
//
//        for (Map.Entry<Unqualified, InvolvedSource> involvedSource : involvedSources.entrySet()) {
////            involvedSource.getValue().sourceContext.startPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE);
////            involvedSource.getValue().sourceContext.end(ModelProcessingPhase.SOURCE_PRE_LINKAGE);
//
//            // add linkage as well
////            stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_LINKAGE)
////                .getDefinitionsSpecificForVersion(involvedSource.getValue().sourceInfo.getSourceInfo().yangVersion()));
//            stmtDefs.putAll(getSupportsForPhase(ModelProcessingPhase.SOURCE_PRE_LINKAGE)
//                .getDefinitionsSpecificForVersion(involvedSource.getValue().sourceInfo.getSourceInfo().yangVersion()));
//            involvedSource.getValue().sourceContext.startPhase(ModelProcessingPhase.SOURCE_LINKAGE);
//            StmtContext.Mutable<Unqualified, ?, ?> rootStatement =
//                createRootStatement(involvedSource.getValue(), stmtDefs);
//
//            //TODO: fill the pre-linkage and linkage namespaces of each root statement
//
//            if (involvedSource.getValue().sourceInfo.getSourceInfo() instanceof SourceInfo.Module moduleInfo) {
//                rootStatement.addToNs(ParserNamespaces.MODULE_NAME_TO_NAMESPACE, involvedSource.getKey(), moduleInfo.namespace());
//                rootStatement.addToNs(ParserNamespaces.IMP_PREFIX_TO_NAMESPACE, moduleInfo.prefix().getPrefix(), moduleInfo.namespace());
//                rootStatement.addToNs(ParserNamespaces.PRELINKAGE_MODULE, rootStatement.argument(),
//                    (StmtContext.Mutable<Unqualified, ModuleStatement, ModuleEffectiveStatement>)rootStatement);
//
//                final var revisionDate = moduleInfo.revisions().iterator().next();
//                final var qNameModule = QNameModule.ofRevision(moduleInfo.namespace(), revisionDate).intern();
//                rootStatement.addToNs(ParserNamespaces.MODULECTX_TO_QNAME, rootStatement, qNameModule);
//            }
//        }
//    }

    record InvolvedSource(
        SourceSpecificContext sourceContext,
        SourceInfo sourceInfo) {
        public InvolvedSource {
            requireNonNull(sourceContext);
            requireNonNull(sourceInfo);
        }
    }

//    Map<Unqualified, InvolvedSource> resolveInvolvedSources(final Set<SourceSpecificContext> sources,
//        final Set<SourceSpecificContext> libSources) {
//
//        //TODO: should probably map by SourceIdentifier, not just Qname..
//        final Map<Unqualified, InvolvedSource> involvedSources = new HashMap<>();
//        final Map<SourceIdentifier, SourceSpecificContext> yinSources = new HashMap<>();
//        final Map<SourceIdentifier, SourceSpecificContext> namedLibSources = new HashMap<>();
//        final Deque<Map.Entry<Unqualified, SourceSpecificContext>> importedToResolve = new ArrayDeque<>();
//
//        for (SourceSpecificContext libSource : libSources) {
//            if (libSource.getSourceInfo() != null) {
//                namedLibSources.put(libSource.getInternalSourceId(), libSource);
//            } else {
//                yinSources.put(libSource.getInternalSourceId(), libSource);
//            }
//        }
//
//        for (SourceSpecificContext source : sources) {
//            final SourceInfo sourceInfo = source.getSourceInfo();
//            if (sourceInfo != null) {
//                involvedSources.put(sourceInfo.sourceId().name(), new InvolvedSource(source, sourceInfo));
//                for (SourceDependency.Import anImport : sourceInfo.imports()) {
//                    final Unqualified importName = anImport.name();
//                    if (!involvedSources.containsKey(importName)) {
//                        final SourceSpecificContext importedLibSource = namedLibSources.get(importName);
//                        Verify.verifyNotNull(importedLibSource, "Imported source [%s] not found", importName.toString());
//                        importedToResolve.add(Map.entry(importName, importedLibSource));
//                    }
//                }
//            }
//
//        }
//
//        while (!importedToResolve.isEmpty()) {
//            final Map.Entry<Unqualified, SourceSpecificContext> toResolve = importedToResolve.removeFirst();
//            final ExtendedSourceInfo sourceInfo = toResolve.getValue().getSourceInfo();
//            involvedSources.put(toResolve.getKey(), new InvolvedSource(toResolve.getValue(), sourceInfo));
//            for (SourceDependency.Import anImport : sourceInfo.getSourceInfo().imports()) {
//                final Unqualified importName = anImport.name();
//                if (!involvedSources.containsKey(importName)) {
//                    final SourceSpecificContext importedLibSource = namedLibSources.get(importName);
//                    Verify.verifyNotNull(importedLibSource, "Imported source [%s] not found", importName.toString());
//                    importedToResolve.add(Map.entry(importName, importedLibSource));
//                }
//            }
//        }
//        return involvedSources;
//    }

    void fillSourceFromSourceInfo(SourceSpecificContext source) throws RuntimeException {
        final SourceInfo sourceInfo = source.getSourceInfo();
        if (sourceInfo == null) {
            source.loadStatements();
            return;
        }
        final AtomicInteger childOffset = new AtomicInteger();
        final RootStatementContext<?,?,?> root = createRootStatement(source, sourceInfo);

        addSimpleStatement(source, root, sourceInfo, "yang-version", childOffset);
        addSimpleStatement(source, root, sourceInfo, "namespace", childOffset);
        addSimpleStatement(source, root, sourceInfo, "prefix", childOffset);
        addSimpleStatement(source, root, sourceInfo, "contact", childOffset);
        addSimpleStatement(source, root, sourceInfo, "description", childOffset);
        addSimpleStatement(source, root, sourceInfo, "organization", childOffset);
        addSimpleStatement(source, root, sourceInfo, "reference", childOffset);

        addRevisions(source, root, sourceInfo, childOffset);
        addImports(source, root, sourceInfo, childOffset);
        addIncludes(source, root, sourceInfo, childOffset);
        addBelongsTo(source, root, sourceInfo, childOffset);
    }

    private AbstractResumedStatement<?, ?, ?> addSimpleStatement(SourceSpecificContext source, RootStatementContext<?, ?, ?> parent, SourceInfo sourceInfo, String statementName,
        AtomicInteger childOffset) {
        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, statementName);
        Optional<? extends IRStatement> statement = sourceInfo.rootStatement().statements().stream()
            .filter(s -> s.keyword().asStringDeclaration()
                .equals(statementName)).findFirst();
        if (!statement.isPresent()) {
            return null;
        }
        globalContext.getStatementDefContext(sourceInfo.yangVersion(), qName);
        return source.createDeclaredChild(parent, childOffset.getAndIncrement(), qName,
            stmtToArgumentString(statement.get()), toStmtRef(statement.get(), sourceInfo));
    }

    private void addRevisions(SourceSpecificContext source, RootStatementContext<?,?,?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        final ImmutableSet<Revision> revisions = sourceInfo.revisions();
        if (revisions.isEmpty()) {
            return;
        }

        for (IRStatement statement : sourceInfo.rootStatement().statements()) {
            if (statement.keyword().asStringDeclaration().equals("revision")) {
                final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "revision");
                final String argument = ((IRArgument.Single) statement.argument()).string();

                AbstractResumedStatement<?, ?, ?> revisionRoot = source.createDeclaredChild(root,
                    childOffset.getAndIncrement(), qName, argument, toStmtRef(statement, sourceInfo));
                if (!statement.statements().isEmpty()) {
                    Map<String, ? extends IRStatement> collect = statement.statements().stream()
                        .collect(Collectors.toMap(child -> child.keyword().asStringDeclaration(),
                            child -> child));
                    int revisionChildOffset = 0;
                    if (collect.containsKey("description")) {
                        addSubstatement(revisionRoot, collect.get("description"), source, sourceInfo, revisionChildOffset++);
                    }
                    if (collect.containsKey("reference")) {
                        addSubstatement(revisionRoot, collect.get("reference"), source, sourceInfo, revisionChildOffset);
                    }
                }
            }
        }
    }


    private void addSubstatement(AbstractResumedStatement<?, ?, ?> parent, IRStatement substatement,
        SourceSpecificContext source, SourceInfo sourceInfo, int childOffset) {
        final QName qName = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, substatement.keyword().asStringDeclaration());
        StatementDefinitionContext<?, ?, ?> statementDefContext = globalContext.getStatementDefContext(sourceInfo.yangVersion(), qName);
        parent.createSubstatement(childOffset, statementDefContext, toStmtRef(substatement, sourceInfo),
            ((IRArgument.Single) substatement.argument()).string());
    }

    private RootStatementContext<?, ?, ?> createRootStatement(SourceSpecificContext source, SourceInfo sourceInfo) {
        final QName qName = sourceInfo instanceof SourceInfo.Module ?
            QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "module") :
            QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "submodule");

        final AbstractResumedStatement<?, ?, ?> newRoot = source.createDeclaredChild(null, 0, qName,
            sourceInfo.sourceId().name().getLocalName(), toStmtRef(sourceInfo.rootStatement(), sourceInfo));
        newRoot.setRootIdentifier(sourceInfo.sourceId());
        return (RootStatementContext<?, ?, ?>) newRoot;
    }

    private String stmtToArgumentString(IRStatement stmt) {
        return ((IRArgument.Single) stmt.argument()).string();
    }

    private StatementDeclaration.InText toStmtRef(IRStatement statement, SourceInfo info) {
        return StatementDeclarations.inText(info.sourceId().toYangFilename(),
            statement.startLine(), statement.startColumn() + 1);
    }

    private void addBelongsTo(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        if (sourceInfo instanceof SourceInfo.Submodule submoduleInfo) {
            IRStatement belongsToIR = sourceInfo.rootStatement().statements().stream()
                .filter(stmt -> stmt.keyword().asStringDeclaration().equals("belongs-to"))
                .findFirst().get();
            AbstractResumedStatement<?, ?, ?> belongsToStmt = addSimpleStatement(source, root, sourceInfo, "belongs-to", childOffset);
            addSubstatement(belongsToStmt, belongsToIR.statements().get(0), source, sourceInfo, 0);
        }
    }

    private void addIncludes(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        if (sourceInfo.includes().isEmpty()) {
            return;
        }

        final QName qNameInclude = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "include");

        List<? extends IRStatement> includeIRStatements = sourceInfo.rootStatement().statements().stream()
            .filter(stmt -> stmt.keyword().asStringDeclaration().equals("include"))
            .collect(Collectors.toList());

        for (IRStatement includeIR : includeIRStatements) {
            AbstractResumedStatement<?, ?, ?> includeStatement = source.createDeclaredChild(root,
                childOffset.getAndIncrement(), qNameInclude, stmtToArgumentString(includeIR), toStmtRef(includeIR, sourceInfo));
            if (!includeIR.statements().isEmpty()) {
                Map<String, ? extends IRStatement> collect = includeIR.statements().stream()
                    .collect(Collectors.toMap(child -> child.keyword().asStringDeclaration(),
                        child -> child));
                if (collect.containsKey("revision-date")) {
                    addSubstatement(includeStatement, collect.get("revision-date"), source, sourceInfo, 0);
                }
            }
        }
    }

    private void addImports(SourceSpecificContext source, RootStatementContext<?, ?, ?> root, SourceInfo sourceInfo,
        AtomicInteger childOffset) {
        if (sourceInfo.imports().isEmpty()) {
            return;
        }

        final QName qNameImport = QName.unsafeOf(YangConstants.RFC6020_YIN_MODULE, "import");

        List<? extends IRStatement> importIRStatements = sourceInfo.rootStatement().statements().stream()
            .filter(stmt -> stmt.keyword().asStringDeclaration().equals("import"))
            .collect(Collectors.toList());

        for (IRStatement importIR : importIRStatements) {
            AbstractResumedStatement<?, ?, ?> importStatement = source.createDeclaredChild(root,
                childOffset.getAndIncrement(), qNameImport, stmtToArgumentString(importIR), toStmtRef(importIR, sourceInfo));
            if (!importIR.statements().isEmpty()) {
                Map<String, ? extends IRStatement> collect = importIR.statements().stream()
                    .collect(Collectors.toMap(child -> child.keyword().asStringDeclaration(),
                        child -> child));
                int importChildOffset = 0;
                if (collect.containsKey("prefix")) {
                    addSubstatement(importStatement, collect.get("prefix"), source, sourceInfo, importChildOffset++);
                }
                if (collect.containsKey("revision-date")) {
                    addSubstatement(importStatement, collect.get("revision-date"), source, sourceInfo, importChildOffset++);
                }
                if (collect.containsKey("description")) {
                    addSubstatement(importStatement, collect.get("description"), source, sourceInfo, importChildOffset++);
                }
                if (collect.containsKey("reference")) {
                    addSubstatement(importStatement, collect.get("reference"), source, sourceInfo, importChildOffset++);
                }

            }
        }
    }
}
