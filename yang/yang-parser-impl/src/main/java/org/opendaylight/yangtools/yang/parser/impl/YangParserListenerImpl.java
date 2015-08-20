/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.impl;

import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.checkMissingBody;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.createListKey;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.getConfig;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.getIdentityrefBase;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseConstraints;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseDefault;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseRefine;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseSchemaNodeArgs;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseStatus;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseTypeWithBody;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseUnits;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseUserOrdered;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.parseYinValue;
import static org.opendaylight.yangtools.yang.parser.impl.ParserListenerUtils.stringFromNode;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import java.net.URI;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Argument_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Base_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Contact_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Container_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Default_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Description_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_add_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_delete_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_not_supported_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Deviate_replace_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Import_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Key_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Leaf_list_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Leaf_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.List_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Module_header_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Namespace_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Ordered_by_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Organization_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Prefix_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Presence_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Reference_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_date_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Revision_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Status_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Type_body_stmtsContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Units_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.When_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParser.Yang_version_stmtContext;
import org.opendaylight.yangtools.antlrv4.code.gen.YangParserBaseListener;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.model.api.ModuleImport;
import org.opendaylight.yangtools.yang.model.api.SchemaPath;
import org.opendaylight.yangtools.yang.model.api.TypeDefinition;
import org.opendaylight.yangtools.yang.model.util.BaseTypes;
import org.opendaylight.yangtools.yang.parser.builder.api.AugmentationSchemaBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.Builder;
import org.opendaylight.yangtools.yang.parser.builder.api.ExtensionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.GroupingBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeAwareBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.TypeDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.api.UsesNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.AnyXmlBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ChoiceCaseBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ContainerSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.DeviationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.FeatureBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.IdentitySchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.LeafSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ListSchemaNodeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.ModuleBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.NotificationBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.RefineHolderImpl;
import org.opendaylight.yangtools.yang.parser.builder.impl.RpcDefinitionBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnionTypeBuilder;
import org.opendaylight.yangtools.yang.parser.builder.impl.UnknownSchemaNodeBuilderImpl;
import org.opendaylight.yangtools.yang.parser.util.YangParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class YangParserListenerImpl extends YangParserBaseListener {
    private static final Logger LOG = LoggerFactory.getLogger(YangParserListenerImpl.class);
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings();
    private static final Splitter COLON_SPLITTER = Splitter.on(':');
    private static final String AUGMENT_STR = "augment";

    private static final String IMPORT_STR = "import";
    private static final String UNION_STR = "union";
    private static final String UNKNOWN_NODE_STR = "unknown-node";

    /**
     * Date Format is not thread-safe so we cannot make constant from it.
     */
    private final DateFormat revisionFormat = new SimpleDateFormat("yyyy-MM-dd");
    private final SchemaPathStack stack = new SchemaPathStack();
    private final Map<String, NavigableMap<Date, URI>> namespaceContext;
    private final String sourcePath;
    private QName moduleQName = QName.create(null, new Date(0L), "dummy");
    private ModuleBuilder moduleBuilder;
    private String moduleName;
    private int augmentOrder;
    private String yangModelPrefix;

    public YangParserListenerImpl(final Map<String, NavigableMap<Date, URI>> namespaceContext, final String sourcePath) {
        this.namespaceContext = namespaceContext;
        this.sourcePath = sourcePath;
    }

    /**
     * Create a new instance.
     *
     * FIXME: the resulting type needs to be extracted, such that we can reuse
     * the "BaseListener" aspect, which need not be exposed to the user. Maybe
     * factor out a base class into repo.spi?
     *
     * @param namespaceContext namespaceContext
     * @param sourcePath sourcePath
     * @param walker walker
     * @param tree tree
     * @return new instance of YangParserListenerImpl
     */
    public static YangParserListenerImpl create(final Map<String, NavigableMap<Date, URI>> namespaceContext,
            final String sourcePath, final ParseTreeWalker walker, final ParseTree tree) {
        final YangParserListenerImpl ret = new YangParserListenerImpl(namespaceContext, sourcePath);
        walker.walk(ret, tree);
        return ret;
    }

    @Override
    public void enterModule_stmt(final YangParser.Module_stmtContext ctx) {
        moduleName = stringFromNode(ctx);
        LOG.trace("entering module {}", moduleName);
        enterLog("module", moduleName, 0);
        stack.push();

        moduleBuilder = new ModuleBuilder(moduleName, sourcePath);

        String description = null;
        String reference = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else {
                if (description != null && reference != null) {
                    break;
                }
            }
        }
        moduleBuilder.setDescription(description);
        moduleBuilder.setReference(reference);
    }

    @Override
    public void exitModule_stmt(final YangParser.Module_stmtContext ctx) {
        exitLog("module");
        stack.pop();
    }

    @Override
    public void enterSubmodule_stmt(final YangParser.Submodule_stmtContext ctx) {
        moduleName = stringFromNode(ctx);
        LOG.trace("entering submodule {}", moduleName);
        enterLog("submodule", moduleName, 0);
        stack.push();

        moduleBuilder = new ModuleBuilder(moduleName, true, sourcePath);

        String description = null;
        String reference = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                description = stringFromNode(child);
            } else if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else {
                if (description != null && reference != null) {
                    break;
                }
            }
        }
        moduleBuilder.setDescription(description);
        moduleBuilder.setReference(reference);
    }

    @Override
    public void exitSubmodule_stmt(final YangParser.Submodule_stmtContext ctx) {
        exitLog("submodule");
        stack.pop();
    }

    @Override
    public void enterBelongs_to_stmt(final YangParser.Belongs_to_stmtContext ctx) {
        final String belongsTo = stringFromNode(ctx);
        final NavigableMap<Date, URI> context = namespaceContext.get(belongsTo);
        final Map.Entry<Date, URI> entry = context.firstEntry();
        // TODO
        // Submodule will contain namespace and revision from module to which it
        // belongs. If there are multiple modules with same name and different
        // revisions, it will has revision from the newest one.
        this.moduleQName = QName.create(entry.getValue(), entry.getKey(), moduleQName.getLocalName());
        moduleBuilder.setQNameModule(moduleQName.getModule());
        moduleBuilder.setBelongsTo(belongsTo);
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Prefix_stmtContext) {
                yangModelPrefix = stringFromNode(treeNode);
                moduleBuilder.setPrefix(yangModelPrefix);
                setLog("prefix", yangModelPrefix);
            }
        }
    }

    @Override
    public void enterModule_header_stmts(final Module_header_stmtsContext ctx) {
        enterLog("module_header", "", ctx.getStart().getLine());
        String yangVersion = null;
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Namespace_stmtContext) {
                final String namespaceStr = stringFromNode(treeNode);
                final URI namespace = URI.create(namespaceStr);
                this.moduleQName = QName.create(namespace, moduleQName.getRevision(), moduleQName.getLocalName());
                moduleBuilder.setQNameModule(moduleQName.getModule());
                setLog("namespace", namespaceStr);
            } else if (treeNode instanceof Prefix_stmtContext) {
                yangModelPrefix = stringFromNode(treeNode);
                this.moduleQName = QName.create(moduleQName.getModule(), moduleQName.getLocalName());
                moduleBuilder.setPrefix(yangModelPrefix);
                setLog("prefix", yangModelPrefix);
            } else if (treeNode instanceof Yang_version_stmtContext) {
                yangVersion = stringFromNode(treeNode);
                setLog("yang-version", yangVersion);
            }
        }

        if (yangVersion == null) {
            yangVersion = "1";
        }
        moduleBuilder.setYangVersion(yangVersion);
    }

    @Override
    public void exitModule_header_stmts(final Module_header_stmtsContext ctx) {
        exitLog("module_header");
    }

    @Override
    public void enterMeta_stmts(final YangParser.Meta_stmtsContext ctx) {
        enterLog("meta_stmt", "", ctx.getStart().getLine());
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Organization_stmtContext) {
                final String organization = stringFromNode(child);
                moduleBuilder.setOrganization(organization);
                setLog("organization", organization);
            } else if (child instanceof Contact_stmtContext) {
                final String contact = stringFromNode(child);
                moduleBuilder.setContact(contact);
                setLog("contact", contact);
            } else if (child instanceof Description_stmtContext) {
                final String description = stringFromNode(child);
                moduleBuilder.setDescription(description);
                setLog("description", description);
            } else if (child instanceof Reference_stmtContext) {
                final String reference = stringFromNode(child);
                moduleBuilder.setReference(reference);
                setLog("reference", reference);
            }
        }
    }

    @Override
    public void exitMeta_stmts(final YangParser.Meta_stmtsContext ctx) {
        exitLog("meta_stmt");
    }

    @Override
    public void enterRevision_stmts(final Revision_stmtsContext ctx) {
        enterLog("revisions", "", ctx.getStart().getLine());
        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Revision_stmtContext) {
                updateRevisionForRevisionStatement(treeNode);
            }
        }
    }

    @Override
    public void exitRevision_stmts(final Revision_stmtsContext ctx) {
        exitLog("revisions");
    }

    private void updateRevisionForRevisionStatement(final ParseTree treeNode) {
        final String revisionDateStr = stringFromNode(treeNode);
        try {
            final Date revisionDate = revisionFormat.parse(revisionDateStr);
            if ((revisionDate != null) && (this.moduleQName.getRevision().compareTo(revisionDate) < 0)) {
                this.moduleQName = QName.create(moduleQName.getNamespace(), revisionDate, moduleQName.getLocalName());
                moduleBuilder.setQNameModule(moduleQName.getModule());
                setLog("revision", revisionDate.toString());
                for (int i = 0; i < treeNode.getChildCount(); ++i) {
                    final ParseTree child = treeNode.getChild(i);
                    if (child instanceof Reference_stmtContext) {
                        moduleBuilder.setReference(stringFromNode(child));
                    }
                }
            }
        } catch (final ParseException e) {
            LOG.warn("Failed to parse revision string: {}", revisionDateStr, e);
        }
    }

    @Override
    public void enterImport_stmt(final Import_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String importName = stringFromNode(ctx);
        enterLog(IMPORT_STR, importName, line);

        String importPrefix = null;
        Date importRevision = null;

        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Prefix_stmtContext) {
                importPrefix = stringFromNode(treeNode);
            } else if (treeNode instanceof Revision_date_stmtContext) {
                final String importRevisionStr = stringFromNode(treeNode);
                try {
                    importRevision = revisionFormat.parse(importRevisionStr);
                } catch (final ParseException e) {
                    LOG.warn("Failed to parse import revision-date at line {}: {}", line, importRevisionStr, e);
                }
            }
        }
        moduleBuilder.addModuleImport(importName, importRevision, importPrefix);
        LOG.trace("setting import ({}; {}; {})", importName, importRevision, importPrefix);
    }

    @Override
    public void exitImport_stmt(final Import_stmtContext ctx) {
        exitLog(IMPORT_STR);
    }

    @Override
    public void enterInclude_stmt(final YangParser.Include_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String includeName = stringFromNode(ctx);
        enterLog(IMPORT_STR, includeName, line);

        Date includeRevision = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree treeNode = ctx.getChild(i);
            if (treeNode instanceof Revision_date_stmtContext) {
                final String importRevisionStr = stringFromNode(treeNode);
                try {
                    includeRevision = revisionFormat.parse(importRevisionStr);
                } catch (final ParseException e) {
                    LOG.warn("Failed to parse import revision-date at line {}: {}", line, importRevisionStr, e);
                }
            }
        }
        moduleBuilder.addInclude(includeName, includeRevision);
    }

    @Override
    public void exitInclude_stmt(final YangParser.Include_stmtContext ctx) {
        exitLog("include");
    }

    @Override
    public void enterAugment_stmt(final YangParser.Augment_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String augmentPath = stringFromNode(ctx);
        enterLog(AUGMENT_STR, augmentPath, line);
        stack.push();

        final SchemaPath targetPath = parseXPathString(augmentPath, line);
        final AugmentationSchemaBuilder builder = moduleBuilder.addAugment(line, augmentPath, targetPath,
                augmentOrder++);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                builder.setDescription(stringFromNode(child));
            } else if (child instanceof Reference_stmtContext) {
                builder.setReference(stringFromNode(child));
            } else if (child instanceof Status_stmtContext) {
                builder.setStatus(parseStatus((Status_stmtContext) child));
            } else if (child instanceof When_stmtContext) {
                builder.addWhenCondition(stringFromNode(child));
            }
        }

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitAugment_stmt(final YangParser.Augment_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog(AUGMENT_STR);
        stack.pop();
    }

    @Override
    public void enterExtension_stmt(final YangParser.Extension_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String extName = stringFromNode(ctx);
        enterLog("extension", extName, line);
        final QName qname = QName.create(moduleQName, extName);
        final SchemaPath path = stack.addNodeToPath(qname);

        final ExtensionBuilder builder = moduleBuilder.addExtension(qname, line, path);
        parseSchemaNodeArgs(ctx, builder);

        String argument = null;
        boolean yin = false;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Argument_stmtContext) {
                argument = stringFromNode(child);
                yin = parseYinValue((Argument_stmtContext) child);
                break;
            }
        }
        builder.setArgument(argument);
        builder.setYinElement(yin);

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitExtension_stmt(final YangParser.Extension_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("extension", stack.removeNodeFromPath());
    }

    @Override
    public void enterTypedef_stmt(final YangParser.Typedef_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String typedefName = stringFromNode(ctx);
        enterLog("typedef", typedefName, line);
        final QName typedefQName = QName.create(moduleQName, typedefName);
        final SchemaPath path = stack.addNodeToPath(typedefQName);

        final TypeDefinitionBuilder builder = moduleBuilder.addTypedef(line, typedefQName, path);
        parseSchemaNodeArgs(ctx, builder);
        builder.setUnits(parseUnits(ctx));
        builder.setDefaultValue(parseDefault(ctx));

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitTypedef_stmt(final YangParser.Typedef_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("typedef", stack.removeNodeFromPath());
    }

    @Override
    public void enterType_stmt(final YangParser.Type_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String typeName = stringFromNode(ctx);
        enterLog("type", typeName, line);

        final QName typeQName = parseQName(typeName, line);

        TypeDefinition<?> type;
        Type_body_stmtsContext typeBody = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            if (ctx.getChild(i) instanceof Type_body_stmtsContext) {
                typeBody = (Type_body_stmtsContext) ctx.getChild(i);
                break;
            }
        }

        // if this is base yang type...
        if (BaseTypes.isYangBuildInType(typeName)) {
            if (typeBody == null) {
                // check for types which must have body
                checkMissingBody(typeName, moduleName, line);
                // if there are no constraints, just grab default base yang type
                type = BaseTypes.defaultBaseTypeFor(typeName).orNull();
                stack.addNodeToPath(type.getQName());
                moduleBuilder.setType(type);
            } else {
                QName qname;
                switch (typeName) {
                case UNION_STR:
                    stack.addNodeToPath(BaseTypes.UNION_QNAME);
                    final UnionTypeBuilder unionBuilder = moduleBuilder.addUnionType(line, moduleQName.getModule());
                    final Builder parentBuilder = moduleBuilder.getActualNode();
                    unionBuilder.setParent(parentBuilder);
                    moduleBuilder.enterNode(unionBuilder);
                    break;
                case "identityref":
                    qname = BaseTypes.IDENTITYREF_QNAME;
                    final SchemaPath path = stack.addNodeToPath(qname);
                    moduleBuilder.addIdentityrefType(line, path, getIdentityrefBase(typeBody));
                    break;
                default:
                    type = parseTypeWithBody(typeName, typeBody, stack.currentSchemaPath(), moduleQName,
                            moduleBuilder.getActualNode());
                    moduleBuilder.setType(type);
                    stack.addNodeToPath(type.getQName());
                }
            }
        } else {
            final TypeAwareBuilder parent = (TypeAwareBuilder) moduleBuilder.getActualNode();
            if (typeBody == null) {
                parent.setTypeQName(typeQName);
                moduleBuilder.markActualNodeDirty();
            } else {
                ParserListenerUtils.parseUnknownTypeWithBody(typeBody, parent, typeQName, moduleBuilder, moduleQName,
                        stack.currentSchemaPath());
            }
            stack.addNodeToPath(QName.create(moduleQName.getModule(), typeQName.getLocalName()));
        }
    }

    /**
     * Method transforms string representation of yang element (i.e. leaf name,
     * container name etc.) into QName. The namespace of QName is assigned from
     * parent module same as revision date of module. If String qname parameter
     * contains ":" the string is evaluated as prefix:name of element. In this
     * case method will look into import map and extract correct ModuleImport.
     * If such import is not present in import map the method will throw
     * {@link YangParseException} <br>
     * If ModuleImport is present but the value of namespace in ModuleImport is
     * <code>null</code> the method will throw {@link YangParseException}
     *
     * @param qnameString
     *            QName value as String
     * @param line
     *            line in Yang model document where QName occur.
     * @return transformed string qname parameter as QName structure.
     *
     * @throws YangParseException
     */
    private QName parseQName(final String qnameString, final int line) {
        final QName qname;
        if (qnameString.indexOf(':') == -1) {
            qname = QName.create(moduleQName, qnameString);
        } else {
            final Iterator<String> split = COLON_SPLITTER.split(qnameString).iterator();
            final String prefix = split.next();
            final String name = split.next();
            if (prefix.equals(moduleBuilder.getPrefix())) {
                qname = QName.create(moduleQName, name);
            } else {
                final ModuleImport imp = moduleBuilder.getImport(prefix);
                if (imp == null) {
                    LOG.debug("Error in module {} at line {}: No import found with prefix {}", moduleName, line, prefix);
                    throw new YangParseException(moduleName, line, "Error in module " + moduleName
                            + " No import found with prefix " + prefix + " not found.");
                }
                Date revision = imp.getRevision();
                final NavigableMap<Date, URI> namespaces = namespaceContext.get(imp.getModuleName());
                if (namespaces == null) {
                    throw new YangParseException(moduleName, line, String.format("Imported module %s not found",
                            imp.getModuleName()));
                }
                URI namespace;
                if (revision == null) {
                    revision = namespaces.lastEntry().getKey();
                    namespace = namespaces.lastEntry().getValue();
                } else {
                    // FIXME: this lookup does not look right, as we will end up
                    // with
                    // a qname which does not have a namespace. At any rate we
                    // should arrive at a QNameModule!
                    namespace = namespaces.get(revision);
                }

                final QNameModule mod = QNameModule.cachedReference(QNameModule.create(namespace, revision));
                qname = QName.create(mod, name);
            }
        }
        return qname;
    }

    @Override
    public void exitType_stmt(final YangParser.Type_stmtContext ctx) {
        final String typeName = stringFromNode(ctx);
        if (UNION_STR.equals(typeName)) {
            moduleBuilder.exitNode();
        }
        exitLog("type", stack.removeNodeFromPath());
    }

    @Override
    public void enterGrouping_stmt(final YangParser.Grouping_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String groupName = stringFromNode(ctx);
        enterLog("grouping", groupName, line);
        final QName groupQName = QName.create(moduleQName, groupName);
        final SchemaPath path = stack.addNodeToPath(groupQName);

        final GroupingBuilder builder = moduleBuilder.addGrouping(ctx.getStart().getLine(), groupQName, path);
        parseSchemaNodeArgs(ctx, builder);

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitGrouping_stmt(final YangParser.Grouping_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("grouping", stack.removeNodeFromPath());
    }

    @Override
    public void enterContainer_stmt(final Container_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String containerName = stringFromNode(ctx);
        enterLog("container", containerName, line);

        final QName containerQName = QName.create(moduleQName, containerName);
        final SchemaPath path = stack.addNodeToPath(containerQName);

        final ContainerSchemaNodeBuilder builder = moduleBuilder.addContainerNode(line, containerQName, path);
        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));

        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree childNode = ctx.getChild(i);
            if (childNode instanceof Presence_stmtContext) {
                builder.setPresence(true);
                break;
            }
        }

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitContainer_stmt(final Container_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("container", stack.removeNodeFromPath());
    }

    @Override
    public void enterLeaf_stmt(final Leaf_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String leafName = stringFromNode(ctx);
        enterLog("leaf", leafName, line);

        final QName leafQName = QName.create(moduleQName, leafName);
        final SchemaPath path = stack.addNodeToPath(leafQName);

        final LeafSchemaNodeBuilder builder = moduleBuilder.addLeafNode(line, leafQName, path);
        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));

        String defaultStr = null;
        String unitsStr = null;
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Default_stmtContext) {
                defaultStr = stringFromNode(child);
            } else if (child instanceof Units_stmtContext) {
                unitsStr = stringFromNode(child);
            }
        }
        builder.setDefaultStr(defaultStr);
        builder.setUnits(unitsStr);

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitLeaf_stmt(final YangParser.Leaf_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("leaf", stack.removeNodeFromPath());
    }

    @Override
    public void enterUses_stmt(final YangParser.Uses_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String groupingPathStr = stringFromNode(ctx);
        final SchemaPath groupingPath = parseXPathString(groupingPathStr, line);
        enterLog("uses", groupingPathStr, line);

        final UsesNodeBuilder builder = moduleBuilder.addUsesNode(line, groupingPath);

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitUses_stmt(final YangParser.Uses_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("uses");
    }

    @Override
    public void enterUses_augment_stmt(final YangParser.Uses_augment_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String augmentPath = stringFromNode(ctx);
        enterLog(AUGMENT_STR, augmentPath, line);
        stack.push();

        final SchemaPath targetPath = parseXPathString(augmentPath, line);
        final AugmentationSchemaBuilder builder = moduleBuilder.addAugment(line, augmentPath, targetPath,
                augmentOrder++);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Description_stmtContext) {
                builder.setDescription(stringFromNode(child));
            } else if (child instanceof Reference_stmtContext) {
                builder.setReference(stringFromNode(child));
            } else if (child instanceof Status_stmtContext) {
                builder.setStatus(parseStatus((Status_stmtContext) child));
            } else if (child instanceof When_stmtContext) {
                builder.addWhenCondition(stringFromNode(child));
            }
        }

        moduleBuilder.enterNode(builder);
    }

    @Override
    public void exitUses_augment_stmt(final YangParser.Uses_augment_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog(AUGMENT_STR);
        stack.pop();
    }

    @Override
    public void enterRefine_stmt(final YangParser.Refine_stmtContext ctx) {
        final String refineString = stringFromNode(ctx);
        enterLog("refine", refineString, ctx.getStart().getLine());

        final RefineHolderImpl refine = parseRefine(ctx, moduleName);
        moduleBuilder.addRefine(refine);
        moduleBuilder.enterNode(refine);
    }

    @Override
    public void exitRefine_stmt(final YangParser.Refine_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("refine");
    }

    @Override
    public void enterLeaf_list_stmt(final Leaf_list_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String leafListName = stringFromNode(ctx);
        enterLog("leaf-list", leafListName, line);
        final QName leafListQName = QName.create(moduleQName, leafListName);
        final SchemaPath path = stack.addNodeToPath(leafListQName);

        final LeafListSchemaNodeBuilder builder = moduleBuilder.addLeafListNode(line, leafListQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, ctx.getStart().getLine()));

        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree childNode = ctx.getChild(i);
            if (childNode instanceof Ordered_by_stmtContext) {
                final Ordered_by_stmtContext orderedBy = (Ordered_by_stmtContext) childNode;
                final boolean userOrdered = parseUserOrdered(orderedBy);
                builder.setUserOrdered(userOrdered);
                break;
            }
        }
    }

    @Override
    public void exitLeaf_list_stmt(final YangParser.Leaf_list_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("leaf-list", stack.removeNodeFromPath());
    }

    @Override
    public void enterList_stmt(final List_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String listName = stringFromNode(ctx);
        enterLog("list", listName, line);

        final QName listQName = QName.create(moduleQName, listName);
        final SchemaPath path = stack.addNodeToPath(listQName);

        final ListSchemaNodeBuilder builder = moduleBuilder.addListNode(line, listQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));

        for (int i = 0; i < ctx.getChildCount(); ++i) {
            final ParseTree childNode = ctx.getChild(i);
            if (childNode instanceof Ordered_by_stmtContext) {
                final Ordered_by_stmtContext orderedBy = (Ordered_by_stmtContext) childNode;
                final boolean userOrdered = parseUserOrdered(orderedBy);
                builder.setUserOrdered(userOrdered);
            } else if (childNode instanceof Key_stmtContext) {
                final Set<String> key = createListKey((Key_stmtContext) childNode);
                builder.setKeys(key);
            } else if (childNode instanceof YangParser.Identifier_stmtContext
                    && UNION_STR.equals(childNode.getChild(0).toString())) {
                throw new YangParseException(moduleName, line, "Union statement is not allowed inside a list statement");
            }
        }
    }

    @Override
    public void exitList_stmt(final List_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("list", stack.removeNodeFromPath());
    }

    @Override
    public void enterAnyxml_stmt(final YangParser.Anyxml_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String anyXmlName = stringFromNode(ctx);
        enterLog("anyxml", anyXmlName, line);

        final QName anyXmlQName = QName.create(moduleQName, anyXmlName);
        final SchemaPath path = stack.addNodeToPath(anyXmlQName);

        final AnyXmlBuilder builder = moduleBuilder.addAnyXml(line, anyXmlQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));
    }

    @Override
    public void exitAnyxml_stmt(final YangParser.Anyxml_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("anyxml", stack.removeNodeFromPath());
    }

    @Override
    public void enterChoice_stmt(final YangParser.Choice_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String choiceName = stringFromNode(ctx);
        enterLog("choice", choiceName, line);

        final QName choiceQName = QName.create(moduleQName, choiceName);
        final SchemaPath path = stack.addNodeToPath(choiceQName);

        final ChoiceBuilder builder = moduleBuilder.addChoice(line, choiceQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
        builder.setConfiguration(getConfig(ctx, builder, moduleName, line));

        // set 'default' case
        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Default_stmtContext) {
                final String defaultCase = stringFromNode(child);
                builder.setDefaultCase(defaultCase);
                break;
            }
        }
    }

    @Override
    public void exitChoice_stmt(final YangParser.Choice_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("choice", stack.removeNodeFromPath());
    }

    @Override
    public void enterCase_stmt(final YangParser.Case_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String caseName = stringFromNode(ctx);
        enterLog("case", caseName, line);

        final QName caseQName = QName.create(moduleQName, caseName);
        final SchemaPath path = stack.addNodeToPath(caseQName);

        final ChoiceCaseBuilder builder = moduleBuilder.addCase(line, caseQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
    }

    @Override
    public void exitCase_stmt(final YangParser.Case_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("case", stack.removeNodeFromPath());
    }

    @Override
    public void enterNotification_stmt(final YangParser.Notification_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String notificationName = stringFromNode(ctx);
        enterLog("notification", notificationName, line);

        final QName notificationQName = QName.create(moduleQName, notificationName);
        final SchemaPath path = stack.addNodeToPath(notificationQName);

        final NotificationBuilder builder = moduleBuilder.addNotification(line, notificationQName, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);
    }

    @Override
    public void exitNotification_stmt(final YangParser.Notification_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("notification", stack.removeNodeFromPath());
    }

    // Unknown nodes
    @Override
    public void enterIdentifier_stmt(final YangParser.Identifier_stmtContext ctx) {
        handleUnknownNode(ctx.getStart().getLine(), ctx);
    }

    @Override
    public void exitIdentifier_stmt(final YangParser.Identifier_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog(UNKNOWN_NODE_STR, stack.removeNodeFromPath());
    }

    @Override
    public void enterUnknown_statement(final YangParser.Unknown_statementContext ctx) {
        handleUnknownNode(ctx.getStart().getLine(), ctx);
    }

    @Override
    public void exitUnknown_statement(final YangParser.Unknown_statementContext ctx) {
        moduleBuilder.exitNode();
        exitLog(UNKNOWN_NODE_STR, stack.removeNodeFromPath());
    }

    @Override
    public void enterRpc_stmt(final YangParser.Rpc_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String rpcName = stringFromNode(ctx);
        enterLog("rpc", rpcName, line);

        final QName rpcQName = QName.create(moduleQName, rpcName);
        final SchemaPath path = stack.addNodeToPath(rpcQName);

        final RpcDefinitionBuilder rpcBuilder = moduleBuilder.addRpc(line, rpcQName, path);
        moduleBuilder.enterNode(rpcBuilder);

        parseSchemaNodeArgs(ctx, rpcBuilder);
    }

    @Override
    public void exitRpc_stmt(final YangParser.Rpc_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("rpc", stack.removeNodeFromPath());
    }

    @Override
    public void enterInput_stmt(final YangParser.Input_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String input = "input";
        enterLog(input, input, line);

        final QName rpcQName = QName.create(moduleQName, input);
        final SchemaPath path = stack.addNodeToPath(rpcQName);

        final ContainerSchemaNodeBuilder builder = moduleBuilder.addRpcInput(line, rpcQName, path);
        moduleBuilder.enterNode(builder);
        builder.setConfiguration(true);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
    }

    @Override
    public void exitInput_stmt(final YangParser.Input_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("input", stack.removeNodeFromPath());
    }

    @Override
    public void enterOutput_stmt(final YangParser.Output_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String output = "output";
        enterLog(output, output, line);

        final QName rpcQName = QName.create(moduleQName, output);
        final SchemaPath path = stack.addNodeToPath(rpcQName);

        final ContainerSchemaNodeBuilder builder = moduleBuilder.addRpcOutput(path, rpcQName, line);
        moduleBuilder.enterNode(builder);
        builder.setConfiguration(true);

        parseSchemaNodeArgs(ctx, builder);
        parseConstraints(ctx, builder.getConstraints());
    }

    @Override
    public void exitOutput_stmt(final YangParser.Output_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("output", stack.removeNodeFromPath());
    }

    @Override
    public void enterFeature_stmt(final YangParser.Feature_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String featureName = stringFromNode(ctx);
        enterLog("feature", featureName, line);

        final QName featureQName = QName.create(moduleQName, featureName);
        final SchemaPath path = stack.addNodeToPath(featureQName);

        final FeatureBuilder featureBuilder = moduleBuilder.addFeature(line, featureQName, path);
        moduleBuilder.enterNode(featureBuilder);

        parseSchemaNodeArgs(ctx, featureBuilder);
    }

    @Override
    public void exitFeature_stmt(final YangParser.Feature_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("feature", stack.removeNodeFromPath());
    }

    @Override
    public void enterDeviation_stmt(final YangParser.Deviation_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String targetPathStr = stringFromNode(ctx);
        if (!targetPathStr.startsWith("/")) {
            throw new YangParseException(moduleName, line,
                    "Deviation argument string must be an absolute schema node identifier.");
        }
        enterLog("deviation", targetPathStr, line);

        String reference = null;
        String deviate = null;

        final SchemaPath targetPath = parseXPathString(targetPathStr, line);
        final DeviationBuilder builder = moduleBuilder.addDeviation(line, targetPath);
        moduleBuilder.enterNode(builder);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Reference_stmtContext) {
                reference = stringFromNode(child);
            } else if (child instanceof Deviate_not_supported_stmtContext) {
                deviate = stringFromNode(child);
            } else if (child instanceof Deviate_add_stmtContext) {
                deviate = stringFromNode(child);
            } else if (child instanceof Deviate_replace_stmtContext) {
                deviate = stringFromNode(child);
            } else if (child instanceof Deviate_delete_stmtContext) {
                deviate = stringFromNode(child);
            }
        }
        builder.setReference(reference);
        builder.setDeviate(deviate);
    }

    public SchemaPath parseXPathString(final String xpathString, final int line) {
        final boolean absolute = !xpathString.isEmpty() && xpathString.charAt(0) == '/';

        final List<QName> path = new ArrayList<>();
        for (final String pathElement : SLASH_SPLITTER.split(xpathString)) {
            final Iterator<String> it = COLON_SPLITTER.split(pathElement).iterator();
            final String s = it.next();
            if (it.hasNext()) {
                path.add(parseQName(pathElement, line));
            } else {
                path.add(QName.create(moduleQName, s));
            }
        }
        return SchemaPath.create(path, absolute);
    }

    @Override
    public void exitDeviation_stmt(final YangParser.Deviation_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("deviation");
    }

    @Override
    public void enterIdentity_stmt(final YangParser.Identity_stmtContext ctx) {
        final int line = ctx.getStart().getLine();
        final String identityName = stringFromNode(ctx);
        enterLog("identity", identityName, line);

        final QName identityQName = QName.create(moduleQName, identityName);
        final SchemaPath path = stack.addNodeToPath(identityQName);

        final IdentitySchemaNodeBuilder builder = moduleBuilder.addIdentity(identityQName, line, path);
        moduleBuilder.enterNode(builder);

        parseSchemaNodeArgs(ctx, builder);

        for (int i = 0; i < ctx.getChildCount(); i++) {
            final ParseTree child = ctx.getChild(i);
            if (child instanceof Base_stmtContext) {
                final String baseIdentityName = stringFromNode(child);
                builder.setBaseIdentityName(baseIdentityName);
            }
        }
    }

    @Override
    public void exitIdentity_stmt(final YangParser.Identity_stmtContext ctx) {
        moduleBuilder.exitNode();
        exitLog("identity", stack.removeNodeFromPath());
    }

    public ModuleBuilder getModuleBuilder() {
        return moduleBuilder;
    }

    private static void enterLog(final String p1, final String p2, final int line) {
        LOG.trace("entering {} {} ({})", p1, p2, line);
    }

    private static void exitLog(final String p1) {
        LOG.trace("exiting {}", p1);
    }

    private static void exitLog(final String p1, final QName p2) {
        LOG.trace("exiting {} {}", p1, p2.getLocalName());
    }

    private static void setLog(final String p1, final String p2) {
        LOG.trace("setting {} {}", p1, p2);
    }

    private void handleUnknownNode(final int line, final ParseTree ctx) {
        final String nodeParameter = stringFromNode(ctx);
        enterLog(UNKNOWN_NODE_STR, nodeParameter, line);

        final String nodeTypeStr = ctx.getChild(0).getText();
        final QName nodeType = parseQName(nodeTypeStr, line);

        QName qname = null;
        try {
            // FIXME: rewrite whole method to handle unknown nodes properly.
            // This should be bugfix for bug
            // https://bugs.opendaylight.org/show_bug.cgi?id=1539
            // After this fix bug
            // https://bugs.opendaylight.org/show_bug.cgi?id=1538 MUST be fixed
            // since
            // they are dependent!!!
            if (Strings.isNullOrEmpty(nodeParameter)) {
                qname = nodeType;
            } else {
                final Iterable<String> splittedName = COLON_SPLITTER.split(nodeParameter);
                final Iterator<String> it = splittedName.iterator();
                if (Iterables.size(splittedName) == 2) {
                    qname = parseQName(nodeParameter, line);
                } else {
                    qname = QName.create(moduleQName, it.next());
                }
            }
        } catch (IllegalArgumentException | YangParseException ex) {
            qname = nodeType;
        }

        final SchemaPath path = stack.addNodeToPath(qname);

        final UnknownSchemaNodeBuilderImpl builder = moduleBuilder.addUnknownSchemaNode(line, qname, path);
        builder.setNodeType(nodeType);
        builder.setNodeParameter(nodeParameter);

        parseSchemaNodeArgs(ctx, builder);
        moduleBuilder.enterNode(builder);
    }

}
