/*
 * Copyright (c) 2015 Cisco Systems, Inc. and others.  All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */
package org.opendaylight.yangtools.yang.parser.stmt.rfc6020;

import static org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils.firstAttributeOf;

import com.google.common.base.CharMatcher;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.ImmutableSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import javax.annotation.Nullable;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.opendaylight.yangtools.antlrv4.code.gen.YangStatementParser;
import org.opendaylight.yangtools.yang.common.QName;
import org.opendaylight.yangtools.yang.common.QNameModule;
import org.opendaylight.yangtools.yang.common.SimpleDateFormatUtil;
import org.opendaylight.yangtools.yang.model.api.DeviateKind;
import org.opendaylight.yangtools.yang.model.api.ModuleIdentifier;
import org.opendaylight.yangtools.yang.model.api.RevisionAwareXPath;
import org.opendaylight.yangtools.yang.model.api.Status;
import org.opendaylight.yangtools.yang.model.api.meta.StatementDefinition;
import org.opendaylight.yangtools.yang.model.api.stmt.BelongsToStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.ModuleStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.RevisionStatement;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier;
import org.opendaylight.yangtools.yang.model.api.stmt.SchemaNodeIdentifier.Relative;
import org.opendaylight.yangtools.yang.model.api.stmt.SubmoduleStatement;
import org.opendaylight.yangtools.yang.model.repo.api.RevisionSourceIdentifier;
import org.opendaylight.yangtools.yang.model.repo.api.SourceIdentifier;
import org.opendaylight.yangtools.yang.model.util.RevisionAwareXPathImpl;
import org.opendaylight.yangtools.yang.parser.spi.meta.QNameCacheNamespace;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContext;
import org.opendaylight.yangtools.yang.parser.spi.meta.StmtContextUtils;
import org.opendaylight.yangtools.yang.parser.spi.meta.CopyType;
import org.opendaylight.yangtools.yang.parser.spi.source.BelongsToPrefixToModuleName;
import org.opendaylight.yangtools.yang.parser.spi.source.ImpPrefixToModuleIdentifier;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleCtxToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleIdentifierToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.ModuleNameToModuleQName;
import org.opendaylight.yangtools.yang.parser.spi.source.PrefixToModule;
import org.opendaylight.yangtools.yang.parser.spi.source.QNameToStatementDefinition;
import org.opendaylight.yangtools.yang.parser.spi.source.SourceException;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.RootStatementContext;
import org.opendaylight.yangtools.yang.parser.stmt.reactor.StatementContextBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class Utils {
    private static final int UNICODE_SCRIPT_FIX_COUNTER = 30;
    private static final Logger LOG = LoggerFactory.getLogger(Utils.class);
    private static final CharMatcher LEFT_PARENTHESIS_MATCHER = CharMatcher.is('(');
    private static final CharMatcher RIGHT_PARENTHESIS_MATCHER = CharMatcher.is(')');
    private static final CharMatcher AMPERSAND_MATCHER = CharMatcher.is('&');
    private static final CharMatcher QUESTION_MARK_MATCHER = CharMatcher.is('?');
    private static final Splitter SLASH_SPLITTER = Splitter.on('/').omitEmptyStrings().trimResults();
    private static final Splitter SPACE_SPLITTER = Splitter.on(' ').omitEmptyStrings().trimResults();
    private static final Splitter COLON_SPLITTER = Splitter.on(":").omitEmptyStrings().trimResults();
    private static final Pattern PATH_ABS = Pattern.compile("/[^/].*");
    private static final Pattern BETWEEN_CURLY_BRACES_PATTERN = Pattern.compile("\\{(.+?)\\}");
    private static final Set<String> JAVA_UNICODE_BLOCKS = ImmutableSet.<String>builder()
            .add("AegeanNumbers")
            .add("AlchemicalSymbols")
            .add("AlphabeticPresentationForms")
            .add("AncientGreekMusicalNotation")
            .add("AncientGreekNumbers")
            .add("AncientSymbols")
            .add("Arabic")
            .add("ArabicPresentationForms-A")
            .add("ArabicPresentationForms-B")
            .add("ArabicSupplement")
            .add("Armenian")
            .add("Arrows")
            .add("Avestan")
            .add("Balinese")
            .add("Bamum")
            .add("BamumSupplement")
            .add("BasicLatin")
            .add("Batak")
            .add("Bengali")
            .add("BlockElements")
            .add("Bopomofo")
            .add("BopomofoExtended")
            .add("BoxDrawing")
            .add("Brahmi")
            .add("BraillePatterns")
            .add("Buginese")
            .add("Buhid")
            .add("ByzantineMusicalSymbols")
            .add("Carian")
            .add("Cham")
            .add("Cherokee")
            .add("CJKCompatibility")
            .add("CJKCompatibilityForms")
            .add("CJKCompatibilityIdeographs")
            .add("CJKCompatibilityIdeographsSupplement")
            .add("CJKRadicalsSupplement")
            .add("CJKStrokes")
            .add("CJKSymbolsandPunctuation")
            .add("CJKUnifiedIdeographs")
            .add("CJKUnifiedIdeographsExtensionA")
            .add("CJKUnifiedIdeographsExtensionB")
            .add("CJKUnifiedIdeographsExtensionC")
            .add("CJKUnifiedIdeographsExtensionD")
            .add("CombiningDiacriticalMarks")
            .add("CombiningDiacriticalMarksSupplement")
            .add("CombiningHalfMarks")
            .add("CombiningDiacriticalMarksforSymbols")
            .add("CommonIndicNumberForms")
            .add("ControlPictures")
            .add("Coptic")
            .add("CountingRodNumerals")
            .add("Cuneiform")
            .add("CuneiformNumbersandPunctuation")
            .add("CurrencySymbols")
            .add("CypriotSyllabary")
            .add("Cyrillic")
            .add("CyrillicExtended-A")
            .add("CyrillicExtended-B")
            .add("CyrillicSupplementary")
            .add("Deseret")
            .add("Devanagari")
            .add("DevanagariExtended")
            .add("Dingbats")
            .add("DominoTiles")
            .add("EgyptianHieroglyphs")
            .add("Emoticons")
            .add("EnclosedAlphanumericSupplement")
            .add("EnclosedAlphanumerics")
            .add("EnclosedCJKLettersandMonths")
            .add("EnclosedIdeographicSupplement")
            .add("Ethiopic")
            .add("EthiopicExtended")
            .add("EthiopicExtended-A")
            .add("EthiopicSupplement")
            .add("GeneralPunctuation")
            .add("GeometricShapes")
            .add("Georgian")
            .add("GeorgianSupplement")
            .add("Glagolitic")
            .add("Gothic")
            .add("GreekandCoptic")
            .add("GreekExtended")
            .add("Gujarati")
            .add("Gurmukhi")
            .add("HalfwidthandFullwidthForms")
            .add("HangulCompatibilityJamo")
            .add("HangulJamo")
            .add("HangulJamoExtended-A")
            .add("HangulJamoExtended-B")
            .add("HangulSyllables")
            .add("Hanunoo")
            .add("Hebrew")
            .add("HighPrivateUseSurrogates")
            .add("HighSurrogates")
            .add("Hiragana")
            .add("IdeographicDescriptionCharacters")
            .add("ImperialAramaic")
            .add("InscriptionalPahlavi")
            .add("InscriptionalParthian")
            .add("IPAExtensions")
            .add("Javanese")
            .add("Kaithi")
            .add("KanaSupplement")
            .add("Kanbun")
            .add("Kangxi Radicals")
            .add("Kannada")
            .add("Katakana")
            .add("KatakanaPhoneticExtensions")
            .add("KayahLi")
            .add("Kharoshthi")
            .add("Khmer")
            .add("KhmerSymbols")
            .add("Lao")
            .add("Latin-1Supplement")
            .add("LatinExtended-A")
            .add("LatinExtendedAdditional")
            .add("LatinExtended-B")
            .add("LatinExtended-C")
            .add("LatinExtended-D")
            .add("Lepcha")
            .add("LetterlikeSymbols")
            .add("Limbu")
            .add("LinearBIdeograms")
            .add("LinearBSyllabary")
            .add("Lisu")
            .add("LowSurrogates")
            .add("Lycian")
            .add("Lydian")
            .add("MahjongTiles")
            .add("Malayalam")
            .add("Mandaic")
            .add("MathematicalAlphanumericSymbols")
            .add("MathematicalOperators")
            .add("MeeteiMayek")
            .add("MiscellaneousMathematicalSymbols-A")
            .add("MiscellaneousMathematicalSymbols-B")
            .add("MiscellaneousSymbols")
            .add("MiscellaneousSymbolsandArrows")
            .add("MiscellaneousSymbolsAndPictographs")
            .add("MiscellaneousTechnical")
            .add("ModifierToneLetters")
            .add("Mongolian")
            .add("MusicalSymbols")
            .add("Myanmar")
            .add("MyanmarExtended-A")
            .add("NewTaiLue")
            .add("NKo")
            .add("NumberForms")
            .add("Ogham")
            .add("OlChiki")
            .add("OldItalic")
            .add("OldPersian")
            .add("OldSouthArabian")
            .add("OldTurkic")
            .add("OpticalCharacterRecognition")
            .add("Oriya")
            .add("Osmanya")
            .add("Phags-pa")
            .add("PhaistosDisc")
            .add("Phoenician")
            .add("PhoneticExtensions")
            .add("PhoneticExtensionsSupplement")
            .add("PlayingCards")
            .add("PrivateUseArea")
            .add("Rejang")
            .add("RumiNumeralSymbols")
            .add("Runic")
            .add("Samaritan")
            .add("Saurashtra")
            .add("Shavian")
            .add("Sinhala")
            .add("SmallFormVariants")
            .add("SpacingModifierLetters")
            .add("Specials")
            .add("Sundanese")
            .add("SuperscriptsandSubscripts")
            .add("SupplementalArrows-A")
            .add("SupplementalArrows-B")
            .add("SupplementalMathematicalOperators")
            .add("SupplementalPunctuation")
            .add("SupplementaryPrivateUseArea-A")
            .add("SupplementaryPrivateUseArea-B")
            .add("SylotiNagri")
            .add("Syriac")
            .add("Tagalog")
            .add("Tagbanwa")
            .add("Tags")
            .add("TaiLe")
            .add("TaiTham")
            .add("TaiViet")
            .add("TaiXuanJingSymbols")
            .add("Tamil")
            .add("Telugu")
            .add("Thaana")
            .add("Thai")
            .add("Tibetan")
            .add("Tifinagh")
            .add("TransportAndMapSymbols")
            .add("Ugaritic")
            .add("UnifiedCanadianAboriginalSyllabics")
            .add("UnifiedCanadianAboriginalSyllabicsExtended")
            .add("Vai")
            .add("VariationSelectors")
            .add("VariationSelectorsSupplement")
            .add("VedicExtensions")
            .add("VerticalForms")
            .add("YiRadicals")
            .add("YiSyllables")
            .add("YijingHexagramSymbols").build();

    private static final Map<String, DeviateKind> KEYWORD_TO_DEVIATE_MAP;
    static {
        final Builder<String, DeviateKind> keywordToDeviateMapBuilder = ImmutableMap.builder();
        for (final DeviateKind deviate : DeviateKind.values()) {
            keywordToDeviateMapBuilder.put(deviate.getKeyword(), deviate);
        }
        KEYWORD_TO_DEVIATE_MAP = keywordToDeviateMapBuilder.build();
    }

    private static final ThreadLocal<XPathFactory> XPATH_FACTORY = new ThreadLocal<XPathFactory>() {
        @Override
        protected XPathFactory initialValue() {
            return XPathFactory.newInstance();
        }
    };

    private Utils() {
        throw new UnsupportedOperationException();
    }

    /**
     * Cleanup any resources attached to the current thread. Threads interacting with this class can cause thread-local
     * caches to them. Invoke this method if you want to detach those resources.
     */
    public static void detachFromCurrentThread() {
        XPATH_FACTORY.remove();
    }

    public static Collection<SchemaNodeIdentifier.Relative> transformKeysStringToKeyNodes(final StmtContext<?, ?, ?> ctx,
            final String value) {
        final List<String> keyTokens = SPACE_SPLITTER.splitToList(value);

        // to detect if key contains duplicates
        if ((new HashSet<>(keyTokens)).size() < keyTokens.size()) {
            // FIXME: report all duplicate keys
            throw new SourceException(ctx.getStatementSourceReference(), "Duplicate value in list key: %s", value);
        }

        final Set<SchemaNodeIdentifier.Relative> keyNodes = new HashSet<>();

        for (final String keyToken : keyTokens) {

            final SchemaNodeIdentifier.Relative keyNode = (Relative) SchemaNodeIdentifier.Relative.create(false,
                    Utils.qNameFromArgument(ctx, keyToken));
            keyNodes.add(keyNode);
        }

        return keyNodes;
    }

    static Collection<SchemaNodeIdentifier.Relative> parseUniqueConstraintArgument(final StmtContext<?, ?, ?> ctx,
            final String argumentValue) {
        final Set<SchemaNodeIdentifier.Relative> uniqueConstraintNodes = new HashSet<>();
        for (final String uniqueArgToken : SPACE_SPLITTER.split(argumentValue)) {
            final SchemaNodeIdentifier nodeIdentifier = Utils.nodeIdentifierFromPath(ctx, uniqueArgToken);
            SourceException.throwIf(nodeIdentifier.isAbsolute(), ctx.getStatementSourceReference(),
                    "Unique statement argument '%s' contains schema node identifier '%s' "
                            + "which is not in the descendant node identifier form.", argumentValue, uniqueArgToken);
            uniqueConstraintNodes.add((SchemaNodeIdentifier.Relative) nodeIdentifier);
        }
        return ImmutableSet.copyOf(uniqueConstraintNodes);
    }

    private static String trimSingleLastSlashFromXPath(final String path) {
        return path.endsWith("/") ? path.substring(0, path.length() - 1) : path;
    }

    static RevisionAwareXPath parseXPath(final StmtContext<?, ?, ?> ctx, final String path) {
        final XPath xPath = XPATH_FACTORY.get().newXPath();
        xPath.setNamespaceContext(StmtNamespaceContext.create(ctx));

        final String trimmed = trimSingleLastSlashFromXPath(path);
        try {
            // TODO: we could capture the result and expose its 'evaluate' method
            xPath.compile(trimmed);
        } catch (final XPathExpressionException e) {
            LOG.warn("Argument \"{}\" is not valid XPath string at \"{}\"", path, ctx.getStatementSourceReference(), e);
        }

        return new RevisionAwareXPathImpl(path, PATH_ABS.matcher(path).matches());
    }

    public static QName trimPrefix(final QName identifier) {
        final String prefixedLocalName = identifier.getLocalName();
        final String[] namesParts = prefixedLocalName.split(":");

        if (namesParts.length == 2) {
            final String localName = namesParts[1];
            return QName.create(identifier.getModule(), localName);
        }

        return identifier;
    }

    public static String trimPrefix(final String identifier) {
        final List<String> namesParts = COLON_SPLITTER.splitToList(identifier);
        if (namesParts.size() == 2) {
            return namesParts.get(1);
        }
        return identifier;
    }

    /**
     *
     * Based on identifier read from source and collections of relevant prefixes and statement definitions mappings
     * provided for actual phase, method resolves and returns valid QName for declared statement to be written.
     * This applies to any declared statement, including unknown statements.
     *
     * @param prefixes - collection of all relevant prefix mappings supplied for actual parsing phase
     * @param stmtDef - collection of all relevant statement definition mappings provided for actual parsing phase
     * @param identifier - statement to parse from source
     * @return valid QName for declared statement to be written
     *
     */
    public static QName getValidStatementDefinition(final PrefixToModule prefixes,
            final QNameToStatementDefinition stmtDef, final QName identifier) {
        if (stmtDef.get(identifier) != null) {
            return stmtDef.get(identifier).getStatementName();
        } else {
            final String prefixedLocalName = identifier.getLocalName();
            final String[] namesParts = prefixedLocalName.split(":");

            if (namesParts.length == 2) {
                final String prefix = namesParts[0];
                final String localName = namesParts[1];

                if (prefixes == null) {
                    return null;
                }

                final QNameModule qNameModule = prefixes.get(prefix);
                if (qNameModule == null) {
                    return null;
                }

                if (prefixes.isPreLinkageMap()) {
                    final StatementDefinition foundStmtDef = stmtDef.getByNamespaceAndLocalName(qNameModule.getNamespace(),
                            localName);
                    return foundStmtDef != null ? foundStmtDef.getStatementName() : null;
                } else {
                    final QName qName = QName.create(qNameModule, localName);
                    return stmtDef.get(qName) != null ? qName : null;
                }
            }
        }
        return null;
    }

    static SchemaNodeIdentifier nodeIdentifierFromPath(final StmtContext<?, ?, ?> ctx, final String path) {
        // FIXME: is the path trimming really necessary??
        final List<QName> qNames = new ArrayList<>();
        for (final String nodeName : SLASH_SPLITTER.split(trimSingleLastSlashFromXPath(path))) {
            try {
                final QName qName = Utils.qNameFromArgument(ctx, nodeName);
                qNames.add(qName);
            } catch (final Exception e) {
                throw new IllegalArgumentException(
                    String.format("Failed to parse node '%s' in path '%s'", nodeName, path), e);
            }
        }

        return SchemaNodeIdentifier.create(qNames, PATH_ABS.matcher(path).matches());
    }

    public static String stringFromStringContext(final YangStatementParser.ArgumentContext context) {
        final StringBuilder sb = new StringBuilder();
        List<TerminalNode> strings = context.STRING();
        if (strings.isEmpty()) {
            strings = Collections.singletonList(context.IDENTIFIER());
        }
        for (final TerminalNode stringNode : strings) {
            final String str = stringNode.getText();
            final char firstChar = str.charAt(0);
            final char lastChar = str.charAt(str.length() - 1);
            if (firstChar == '"' && lastChar == '"') {
                final String innerStr = str.substring(1, str.length() - 1);
                /*
                 * Unescape escaped double quotes, tabs, new line and backslash
                 * in the inner string and trim the result.
                 */
                sb.append(innerStr.replace("\\\"", "\"").replace("\\\\", "\\").replace("\\n", "\n")
                        .replace("\\t", "\t"));
            } else if (firstChar == '\'' && lastChar == '\'') {
                /*
                 * According to RFC6020 a single quote character cannot occur in
                 * a single-quoted string, even when preceded by a backslash.
                 */
                sb.append(str.substring(1, str.length() - 1));
            } else {
                sb.append(str);
            }
        }
        return sb.toString();
    }

    public static QName qNameFromArgument(StmtContext<?, ?, ?> ctx, final String value) {
        if (Strings.isNullOrEmpty(value)) {
            return ctx.getPublicDefinition().getStatementName();
        }

        String prefix;
        QNameModule qNameModule = null;
        String localName = null;

        final String[] namesParts = value.split(":");
        switch (namesParts.length) {
        case 1:
            localName = namesParts[0];
            qNameModule = getRootModuleQName(ctx);
            break;
        default:
            prefix = namesParts[0];
            localName = namesParts[1];
            qNameModule = getModuleQNameByPrefix(ctx, prefix);
            // in case of unknown statement argument, we're not going to parse it
            if (qNameModule == null
                    && ctx.getPublicDefinition().getDeclaredRepresentationClass()
                    .isAssignableFrom(UnknownStatementImpl.class)) {
                localName = value;
                qNameModule = getRootModuleQName(ctx);
            }
            if (qNameModule == null
                    && ctx.getCopyHistory().getLastOperation() == CopyType.ADDED_BY_AUGMENTATION) {
                ctx = ctx.getOriginalCtx();
                qNameModule = getModuleQNameByPrefix(ctx, prefix);
            }
            break;
        }

        Preconditions.checkArgument(qNameModule != null,
                "Error in module '%s': can not resolve QNameModule for '%s'. Statement source at %s",
                ctx.getRoot().rawStatementArgument(), value, ctx.getStatementSourceReference());
        final QNameModule resultQNameModule;
        if (qNameModule.getRevision() == null) {
            resultQNameModule = QNameModule.create(qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV)
                .intern();
        } else {
            resultQNameModule = qNameModule;
        }

        return ctx.getFromNamespace(QNameCacheNamespace.class, QName.create(resultQNameModule, localName));
    }

    public static QNameModule getModuleQNameByPrefix(final StmtContext<?, ?, ?> ctx, final String prefix) {
        final ModuleIdentifier modId = ctx.getRoot().getFromNamespace(ImpPrefixToModuleIdentifier.class, prefix);
        final QNameModule qNameModule = ctx.getFromNamespace(ModuleIdentifierToModuleQName.class, modId);

        if (qNameModule == null && StmtContextUtils.producesDeclared(ctx.getRoot(), SubmoduleStatement.class)) {
            final String moduleName = ctx.getRoot().getFromNamespace(BelongsToPrefixToModuleName.class, prefix);
            return ctx.getFromNamespace(ModuleNameToModuleQName.class, moduleName);
        }
        return qNameModule;
    }

    public static QNameModule getRootModuleQName(final StmtContext<?, ?, ?> ctx) {
        if (ctx == null) {
            return null;
        }

        final StmtContext<?, ?, ?> rootCtx = ctx.getRoot();
        final QNameModule qNameModule;

        if (StmtContextUtils.producesDeclared(rootCtx, ModuleStatement.class)) {
            qNameModule = rootCtx.getFromNamespace(ModuleCtxToModuleQName.class, rootCtx);
        } else if (StmtContextUtils.producesDeclared(rootCtx, SubmoduleStatement.class)) {
            final String belongsToModuleName = firstAttributeOf(rootCtx.substatements(), BelongsToStatement.class);
            qNameModule = rootCtx.getFromNamespace(ModuleNameToModuleQName.class, belongsToModuleName);
        } else {
            qNameModule = null;
        }

        Preconditions.checkArgument(qNameModule != null, "Failed to look up root QNameModule for %s", ctx);
        if (qNameModule.getRevision() != null) {
            return qNameModule;
        }

        return QNameModule.create(qNameModule.getNamespace(), SimpleDateFormatUtil.DEFAULT_DATE_REV).intern();
    }

    @Nullable
    public static StatementContextBase<?, ?, ?> findNode(final StmtContext<?, ?, ?> rootStmtCtx,
            final SchemaNodeIdentifier node) {
        return (StatementContextBase<?, ?, ?>) rootStmtCtx.getFromNamespace(SchemaNodeIdentifierBuildNamespace.class, node);
    }

    public static boolean isUnknownNode(final StmtContext<?, ?, ?> stmtCtx) {
        return stmtCtx != null && stmtCtx.getPublicDefinition().getDeclaredRepresentationClass()
                .isAssignableFrom(UnknownStatementImpl.class);
    }

    public static DeviateKind parseDeviateFromString(final StmtContext<?, ?, ?> ctx, final String deviateKeyword) {
        return Preconditions.checkNotNull(KEYWORD_TO_DEVIATE_MAP.get(deviateKeyword),
                "String '%s' is not valid deviate argument. Statement source at %s", deviateKeyword,
                ctx.getStatementSourceReference());
    }

    public static Status parseStatus(final String value) {
        switch (value) {
        case "current":
            return Status.CURRENT;
        case "deprecated":
            return Status.DEPRECATED;
        case "obsolete":
            return Status.OBSOLETE;
        default:
            LOG.warn("Invalid 'status' statement: {}", value);
            return null;
        }
    }

    public static Date getLatestRevision(final Iterable<? extends StmtContext<?, ?, ?>> subStmts) {
        Date revision = null;
        for (final StmtContext<?, ?, ?> subStmt : subStmts) {
            if (subStmt.getPublicDefinition().getDeclaredRepresentationClass().isAssignableFrom(RevisionStatement
                    .class)) {
                if (revision == null && subStmt.getStatementArgument() != null) {
                    revision = (Date) subStmt.getStatementArgument();
                } else if (subStmt.getStatementArgument() != null && ((Date) subStmt.getStatementArgument()).compareTo
                        (revision) > 0) {
                    revision = (Date) subStmt.getStatementArgument();
                }
            }
        }
        return revision;
    }

    public static boolean isModuleIdentifierWithoutSpecifiedRevision(final Object o) {
        return (o instanceof ModuleIdentifier)
                && (((ModuleIdentifier) o).getRevision() == SimpleDateFormatUtil.DEFAULT_DATE_IMP || ((ModuleIdentifier) o)
                        .getRevision() == SimpleDateFormatUtil.DEFAULT_BELONGS_TO_DATE);
    }

    /**
     * Replaces illegal characters of QName by the name of the character (e.g.
     * '?' is replaced by "QuestionMark" etc.).
     *
     * @param string
     *            input String
     * @return result String
     */
    public static String replaceIllegalCharsForQName(String string) {
        string = LEFT_PARENTHESIS_MATCHER.replaceFrom(string, "LeftParenthesis");
        string = RIGHT_PARENTHESIS_MATCHER.replaceFrom(string, "RightParenthesis");
        string = AMPERSAND_MATCHER.replaceFrom(string, "Ampersand");
        string = QUESTION_MARK_MATCHER.replaceFrom(string, "QuestionMark");

        return string;
    }

    public static String fixUnicodeScriptPattern(String rawPattern) {
        for (int i = 0; i < UNICODE_SCRIPT_FIX_COUNTER; i++) {
            try {
                Pattern.compile(rawPattern);
                return rawPattern;
            } catch(final PatternSyntaxException ex) {
                LOG.debug("Invalid regex pattern syntax in: {}", rawPattern, ex);
                if (ex.getMessage().contains("Unknown character script name")) {
                    rawPattern = fixUnknownScripts(ex.getMessage(), rawPattern);
                } else {
                    return rawPattern;
                }
            }
        }

        LOG.warn("Regex pattern could not be fixed: {}", rawPattern);
        return rawPattern;
    }

    private static String fixUnknownScripts(final String exMessage, final String rawPattern) {
        StringBuilder result = new StringBuilder(rawPattern);
        final Matcher matcher = BETWEEN_CURLY_BRACES_PATTERN.matcher(exMessage);
        if (matcher.find()) {
            final String capturedGroup = matcher.group(1);
            if (JAVA_UNICODE_BLOCKS.contains(capturedGroup)) {
                final int idx = rawPattern.indexOf("Is" + capturedGroup);
                result = result.replace(idx, idx + 2, "In");
            }
        }
        return result.toString();
    }

    public static boolean belongsToTheSameModule(final QName targetStmtQName, final QName sourceStmtQName) {
        if (targetStmtQName.getModule().equals(sourceStmtQName.getModule())) {
            return true;
        }
        return false;
    }

    public static SourceIdentifier createSourceIdentifier(final RootStatementContext<?, ?, ?> root) {
        final QNameModule qNameModule = root.getFromNamespace(ModuleCtxToModuleQName.class, root);
        if (qNameModule != null) {
            // creates SourceIdentifier for a module
            return RevisionSourceIdentifier.create((String) root.getStatementArgument(),
                qNameModule.getFormattedRevision());
        } else {
            // creates SourceIdentifier for a submodule
            final Date revision = Optional.fromNullable(Utils.getLatestRevision(root.declaredSubstatements()))
                    .or(SimpleDateFormatUtil.DEFAULT_DATE_REV);
            final String formattedRevision = SimpleDateFormatUtil.getRevisionFormat().format(revision);
            return RevisionSourceIdentifier.create((String) root.getStatementArgument(),
                    formattedRevision);
        }
    }
}
