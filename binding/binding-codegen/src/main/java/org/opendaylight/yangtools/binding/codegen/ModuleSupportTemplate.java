/*
 * Copyright (c) 2013 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.binding.codegen;

import static java.util.Objects.requireNonNull;
import static org.opendaylight.yangtools.binding.contract.Naming.getModelRootPackageName;
import static org.opendaylight.yangtools.binding.contract.Naming.getRootPackageName;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.TreeMap;
import java.util.function.Function;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.opendaylight.yangtools.binding.lib.ScalarTypeObjectRegistrar;
import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
import org.opendaylight.yangtools.binding.model.api.GeneratedType;
import org.opendaylight.yangtools.binding.model.api.JavaTypeName;
import org.opendaylight.yangtools.rfc8040.model.api.YangDataEffectiveStatement;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.Revision;
import org.opendaylight.yangtools.yang.common.RevisionUnion;
import org.opendaylight.yangtools.yang.common.YangDataName;
import org.opendaylight.yangtools.yang.model.api.EffectiveModelContext;
import org.opendaylight.yangtools.yang.model.api.ModuleLike;
import org.opendaylight.yangtools.yang.model.api.Submodule;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleEffectiveStatement;

/**
 * Template for creating module-specific static utility class, which exposes interface described via
 * {@link ModuleSupport}.
 */
final class ModuleSupportTemplate extends Template {
    /**
     * The root package hierarchy of all classes generated for the purposes of discovering and loading YANG modules
     * along with their semantics. Each module is assigned a its own sub-hierarchy based on its {@code namespace} and
     * {@code revision}.
     */
    private static final @NonNull String PACKAGE_PREFIX = "org.opendaylight.yang.svc.v1";
    /**
     * The simple name of the generated class.
     */
    private static final @NonNull String SIMPLE_NAME = "MS";
    /**
     * The name of the field holding the {@link YangModuleInfo} instance.
     */
    private static final @NonNull String CONST_MODULE_INFO = "MODULE_INFO";
    /**
     * The name of the static field holding the {@link ScalarTypeObjectRegistrar} instance.
     */
    private static final @NonNull String CONST_STO_REGISTRAR = "STO_REGISTRAR";
    /**
     * The name of the static field holding the {@link UnsafeAccess} instance.
     */
    private static final @NonNull String CONST_UNSAFE_ACCESS = "UNSAFE_ACCESS";
    /**
     * The name of the {@link QName} factory method.
     */
    private static final @NonNull String METHOD_QNAME_OF = "qnameOf";
    /**
     * The name of the {@link YangDataName} factory method.
     */
    private static final @NonNull String METHOD_YANG_DATA_NAME_OF = "yangDataNameOf";

    // These are always imported. Note we need to import even java.lang members, as there can be conflicting definitions
    // in our package
    private static final String CORE_IMPORT_STR = """
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.binding.lib.ScalarTypeObjectRegistrar;
        import org.opendaylight.yangtools.binding.lib.UnsafeAccessSupport;
        import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
        """;

    private static final String EXT_IMPORT_STR = """
        import com.google.common.collect.ImmutableSet;
        import java.lang.Override;
        import java.lang.String;
        import java.util.HashSet;
        import java.util.Set;
        import org.eclipse.jdt.annotation.NonNull;
        import org.opendaylight.yangtools.binding.lib.ResourceYangModuleInfo;
        import org.opendaylight.yangtools.binding.lib.ScalarTypeObjectRegistrar;
        import org.opendaylight.yangtools.binding.lib.UnsafeAccessSupport;
        import org.opendaylight.yangtools.binding.meta.UnsafeAccess;
        import org.opendaylight.yangtools.binding.meta.YangModuleInfo;
        import org.opendaylight.yangtools.yang.common.QName;
        """;

    private final @NonNull Function<ModuleLike, List<String>> moduleFilePathResolver;
    private final @NonNull EffectiveModelContext modelContext;
    private final @NonNull ModuleEffectiveStatement module;
    private final @NonNull JavaTypeName typeName;
    private final boolean hasYangData;

    private final String importedTypes = CORE_IMPORT_STR;

    ModuleSupportTemplate(final ModuleEffectiveStatement module, final EffectiveModelContext modelContext,
            final Function<ModuleLike, List<String>> moduleFilePathResolver) {
        this.module = requireNonNull(module);
        this.modelContext = requireNonNull(modelContext);
        this.moduleFilePathResolver = requireNonNull(moduleFilePathResolver);
        typeName = JavaTypeName.create(servicePackageName(module.localQNameModule()), SIMPLE_NAME);
        hasYangData = module.findFirstEffectiveSubstatement(YangDataEffectiveStatement.class).isPresent();
    }

    @Deprecated(since = "16.0.0", forRemoval = true)
    @NonNullByDefault
    static JavaTypeName nameInModuleOf(final GeneratedType genType) {
        // Yeah: not pretty but works
        return JavaTypeName.create(rootToService(genType.packageName()), SIMPLE_NAME);
    }

    @NonNullByDefault
    static JavaTypeName yangModuleInfoOf(final QNameModule module) {
        return JavaTypeName.create(servicePackageName(module), SIMPLE_NAME);
    }

    /**
     * Return the package name for placing generated ServiceLoader entities.
     *
     * @param module module namespace
     * @return the package name for placing generated ServiceLoader entities
     */
    @NonNullByDefault
    static String servicePackageName(final QNameModule module) {
        return rootToService(getRootPackageName(module));
    }

    @NonNullByDefault
    private static String rootToService(final String packageName) {
        return getModelRootPackageName(packageName).replace(PACKAGE_PREFIX, PACKAGE_PREFIX);
    }

    @Override
    JavaTypeName typeName() {
        return typeName;
    }

    @Override
    void generateTo(final Appendable out) throws IOException {
        final var blk = classBlock();

        out
            .append("package ").append(typeName.packageName()).append(";\n")
            .append('\n')
            .append(importedTypes);
        if (hasYangData) {
            out.append("import org.opendaylight.yangtools.yang.common.YangDataName;\n");
        }
        blk.appendTo(out.append('\n'));
    }

    private @NonNull String moduleName() {
        return module.argument().getLocalName();
    }

    private Block classBlock() {
        return Block.builder()
            .eol("/**")
            .str(" * Internal state for code generated in for {@code ").str(moduleName()).eol("} module.")
            .eol(" */")
            .eol("@javax.annotation.processing.Generated(\"mdsal-binding-generator\")")
            .str("public final class " + SIMPLE_NAME).oB()
                .eol("/**")
                .eol(" * The {@link URLYangModuleInfo} corresponding to this module.")
                .eol(" */")
                .eol("public static final @NonNull URLYangModuleInfo " + CONST_MODULE_INFO).eS()
                .eol("/**")
                .eol(" * The {@link ScalarTypeObjectRegistrar} instance. Exposed for technical reasons: this field")
                .eol(" * should only be referenced from a static initialization block of a generated ScalarTypeObject")
                .eol(" * class.")
                .eol(" */")
                .str("public static final @NonNull ScalarTypeObjectRegistrar " + CONST_STO_REGISTRAR).eS()
                .eol("/**")
                .eol(" * The {@link UnsafeAccess} instance. Exposed for technical reasons: use this module's")
                .eol(" * DataRoot's {@code META} field and acquire an instance through its {@code unsafeAccess()}")
                .eol(" * method instead.")
                .eol(" */")
                .str("public static final @NonNull UnsafeAccess " + CONST_UNSAFE_ACCESS).eS()
                .nl()
                .eol("private static final @NonNull QName NAME;")
                .nl()
                .str("static").jBlock(bb -> {
                    final var qnameModule = module.localQNameModule();
                    bb
                        .str("NAME = QName.create(").jStr(qnameModule.namespace().toString()).str(", ");
                    if (qnameModule.revisionUnion() instanceof Revision revision) {
                        bb.jStr(revision.toString()).str(", ");
                    }
                    bb
                        .jStr(moduleName()).eol(").intern();")
                        .str(CONST_MODULE_INFO + " = URLYangModuleInfo.of(NAME, " + SIMPLE_NAME + ".class.getResource(")
                            .str("\"");
                    final var pathItems = moduleFilePathResolver.apply(module.toDataNodeContainer());
                    if (pathItems.isEmpty()) {
                        throw new IllegalStateException(module + " does not have a file path");
                    }
                    for (var pathItem : pathItems) {
                        bb.str("/").str(pathItem);
                    }
                    bb
                        .str("\"").frg(this::submoduleInfos).eol(");")
                        .eol("final var support = UnsafeAccessSupport.of(")
                            .ind().jStr(getRootPackageName(qnameModule)).eol(",")
                            .ind().eol(SIMPLE_NAME + ".class.getModule());")
                        .eol(CONST_UNSAFE_ACCESS + " = support.access();")
                        .eol(CONST_STO_REGISTRAR + " = support.stoRegistrar();");
                })
                .nl()
                .eol("/**")
                .eol(" * Create an interned {@link QName} with specified {@code localName} and namespace/revision")
                .eol(" * of this  module.")
                .eol(" *")
                .eol(" * @param localName local name")
                .eol(" * @return A QName")
                .eol(" * @throws NullPointerException if {@code localName} is {@code null}")
                .eol(" * @throws IllegalArgumentException if {@code localName} is not a valid YANG identifier")
                .eol(" */")
                .str("public static @NonNull QName " + METHOD_QNAME_OF + "(String localName)").oB()
                    .eol("return QName.create(NAME, localName).intern();")
                .cB()
                .frg(this::yangDataNameOf)
            .cB()
            .build();
    }

    private void yangDataNameOf(final BlockBuilder bb) {
        if (!hasYangData) {
            return;
        }
        bb
            .nl()
            .eol("/**")
            .eol(" * Create an interned {@link YangDataName} with specified {@code templateName}")
            .eol(" * and namespace/revision of this module.")
            .eol(" *")
            .eol(" * @param templateName template name")
            .eol(" * @return A YangDataName")
            .eol(" * @throws NullPointerException if {@code templateName} is {@code null}")
            .eol(" * @throws IllegalArgumentException if {@code templateName} is empty")
            .eol(" */")
            .str("public static @NonNull YangDataName " + METHOD_YANG_DATA_NAME_OF + "(final String templateName)").oB()
                .eol("return new YangDataName(NAME.getModule(), templateName).intern();")
            .cB();
    }

    private static void collectSubmodules(final LinkedHashSet<Submodule> dest, final ModuleLike module) {
        for (var submodule : module.getSubmodules()) {
            if (dest.add(submodule)) {
                collectSubmodules(dest, submodule);
            }
        }
    }

    private void submoduleInfos(final BlockBuilder bb) {
        final var mod = module.toDataNodeContainer();
        final var imports = mod.getImports();
        final var submodules = new LinkedHashSet<Submodule>();
        collectSubmodules(submodules, mod);
        if (imports.isEmpty() && submodules.isEmpty()) {
            return;
        }

        for (var imp : imports) {
            final var stmt = imp.asEffectiveStatement();
            final var name = stmt.argument();

//          final var name = imp.getModuleName().getLocalName();
          final var optRrev = imp.getRevision();

          final QNameModule qnameModule;
          if (optRrev.isEmpty()) {
              final var sorted = new TreeMap<RevisionUnion, ModuleEffectiveStatement>();
              for (var m : modelContext.getModules()) {
                  if (name.equals(m.getName())) {
                      sorted.put(m.getQNameModule().revisionUnion(), m);
                  }
              }
              qnameModule = sorted.lastEntry().getValue().getQNameModule();
          } else {
              qnameModule = modelContext.findModuleStatement(name, optRrev).orElseThrow().getQNameModule();
          }

          bb.str("set.add(").str(servicePackageName(qnameModule))
              .eol('.' + CLASS_NAME + '.' + INSTANCE_FIELD_NAME + ");");
        }



////      .nl()
////      .eol("private final @NonNull ImmutableSet<YangModuleInfo> importedModules;")
////      .nl()
////      .blk(classBody(module, CLASS_NAME, submodules))
////      .nl()
//
//
//    }
//
//
//    @NonNullByDefault
//    private BlockBuilder classBody(final ModuleLike mod, final String className, final Set<Submodule> submodules) {
//        final var bb = Block.builder()
//            .str("private ").str(className).str("()").oB();
//
//        if (!mod.getImports().isEmpty() || !submodules.isEmpty()) {
//            importedTypes = ModuleSupportTemplate.EXT_IMPORT_STR;
//            bb.eol("Set<YangModuleInfo> set = new HashSet<>();");
//        }
//
//        for (var imp : mod.getImports()) {
//        }
//
//        for (var submodule : submodules) {
//            bb.str("set.add(").str(getClassName(submodule.getName())).eol("Info." + INSTANCE_FIELD_NAME + ");");
//        }
//
//        if (mod.getImports().isEmpty() && submodules.isEmpty()) {
//            bb.eol("importedModules = ImmutableSet.of();");
//        } else {
//            bb.eol("importedModules = ImmutableSet.copyOf(set);");
//        }
//
//        final var pathItems = moduleFilePathResolver.apply(mod);
//        if (pathItems.isEmpty()) {
//            throw new IllegalStateException("Module " + mod + " does not have a file path");
//        }
//
//        bb
//            .cB()
//            .txt("""
//
//                  @Override
//                  public QName getName() {
//                      return NAME;
//                  }
//
//                  @Override
//                  protected String resourceName() {
//                  """)
//                .str("    return \"");
//        for (var pathItem : pathItems) {
//            bb.str("/").str(pathItem);
//        }
//        bb
//            .eol("\";")
//            .txt("""
//                  }
//
//                  @Override
//                  public ImmutableSet<YangModuleInfo> getImportedModules() {
//                      return importedModules;
//                  }
//                  """);
//
//        for (var sub : submodules) {
//            final var subName = getClassName(sub.getName());
//
//            bb
//                .nl()
//                .str("private static final class ").str(subName).str("Info extends ResourceYangModuleInfo").oB()
//                    .str("private final @NonNull QName NAME = QName.create(")
//                        .jStr(sub.getQNameModule().namespace().toString()).str(", ");
//            sub.getRevision().ifPresent(rev -> bb.jStr(rev.toString()).str(", "));
//            bb
//                .jStr(sub.getName()).eol(").intern();")
//                .nl()
//                .str("static final @NonNull YangModuleInfo INSTANCE = new ").str(subName).eol("Info();")
//                .nl()
//                .eol("private final @NonNull ImmutableSet<YangModuleInfo> importedModules;")
//                .nl()
//                .blk(classBody(sub, subName + "Info", Set.of()))
//                .cB();
//        }
//
//        return bb;
    }
}
