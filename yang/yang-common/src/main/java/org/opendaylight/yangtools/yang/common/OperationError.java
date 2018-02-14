package org.opendaylight.yangtools.yang.common;

import static java.util.Objects.requireNonNull;

import com.google.common.annotations.Beta;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import javax.xml.stream.XMLStreamReader;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A notable event in operation execution. Defined as rpc-error in RFC6241.
 *
 * @author Robert Varga
 */
@Beta
@NonNullByDefault
public interface OperationError {
    /**
     * Operation error severity, as defined in RFC6241 section 4.3.
     */
    final class Severity implements Serializable {
        /**
         * RFC 6241 error
         */
        public static final Severity ERROR = new Severity("error");
        /**
         * RFC 6241 warning
         */
        public static final Severity WARNING = new Severity("warning");

        private static final Map<String, Severity> KNOWN_SEVERITIES = ImmutableMap.of(
            ERROR.severity, ERROR, WARNING.severity, WARNING);

        private static final long serialVersionUID = 1L;

        private final String severity;

        private Severity(final String severity) {
            this.severity = requireNonNull(severity);
        }

        public static Severity of(final String severity) {
            final Severity common = KNOWN_SEVERITIES.get(severity);
            return common != null ? common : new Severity(severity);
        }

        @Override
        public int hashCode() {
            return severity.hashCode();
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            return this == obj || obj instanceof Severity && severity.equals(((Severity)obj).severity);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("severity", severity).toString();
        }

        Object readReplace() {
            return of(severity);
        }
    }

    /**
     * Error type, as defined in RFC6241 section 4.3.
     */
    enum Type {
        /**
         * Secure Transport layer error.
         */
        TRANSPORT,
        /**
         * Messages layer error.
         */
        RPC,
        /**
         * Operations layer error.
         */
        PROTOCOL,
        /**
         * Content layer error.
         */
        APPLICATION;
    }

    /**
     * Error tag, as defined in RFC6241 Appendix A.
     */
    enum Tag {
        IN_USE("in-use"),
        INVALID_VALUE("invalid-value"),
        TOO_BIG("too-big"),
        MISSING_ATTRIBUTE("missing-attribute"),
        BAD_ATTRIBUTE("bad-attribute"),
        UNKNOWN_ATTRIBUTE("unknown-attribute"),
        MISSING_ELEMENT("missing-element"),
        BAD_ELEMENT("bad-element"),
        UNKNOWN_ELEMENT("unknown-element"),
        UNKNOWN_NAMESPACE("unknown-namespace"),
        ACCESS_DENIED("access-denied"),
        LOCK_DENIED("lock-denied"),
        RESOURCE_DENIED("resource-denied"),
        ROLLBACK_DENIED("rollback-denied"),
        ROLLBACK_FAILED("rollback-failed"),
        DATA_EXISTS("data-exists"),
        DATA_MISSING("data-missing"),
        OPERATION_NON_SUPPORTED("operation-not-supported"),
        OPERATION_FAILED("operation-failed"),
        @Deprecated
        PARTIAL_OPERATION("partial-operation"),
        MALFORMED_MESSAGE("malformed-message");

        private static final Map<String, Tag> TAGS = Maps.uniqueIndex(Arrays.asList(values()), Tag::asString);

        private final String rfc6241;

        Tag(final String rfc6241) {
            this.rfc6241 = requireNonNull(rfc6241);
        }

        public final String asString() {
            return rfc6241;
        }

        public static Optional<Tag> ofString(final String rfc6241) {
            return Optional.ofNullable(TAGS.get(requireNonNull(rfc6241)));
        }
    }

    /**
     * Error message, as defined in RFC6241 section 4.3.
     */
    public final class Message {
        private final String message;
        private final Locale locale;

        private Message(final String message, final Locale locale) {
            this.message = requireNonNull(message);
            this.locale = requireNonNull(locale);
        }

        public static final Message of(final String message) {
            return of(message, Locale.ENGLISH);
        }

        public static final Message of(final String message, final Locale locale) {
            return new Message(message, locale);
        }

        @Override
        public int hashCode() {
            return Objects.hash(message, locale);
        }

        @Override
        public boolean equals(final @Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (!(obj instanceof Message)) {
                return false;
            }
            final Message other = (Message) obj;
            return message.equals(other.message) && locale.equals(other.locale);
        }

        @Override
        public String toString() {
            return MoreObjects.toStringHelper(this).add("message", message).add("locale", locale).toString();
        }
    }

    /**
     * Error information, as defined by RFC6241 section 4.3.
     */
    interface Info {
        XMLStreamReader toStreamReader();

    }
}
