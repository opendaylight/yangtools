/**
 * (C)2015 Brocade Communications Systems, Inc and others
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 */


package org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924;
import com.google.common.base.Preconditions;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.io.Serializable;
import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Arrays;
import java.beans.ConstructorProperties;
import java.net.Inet6Address;
import java.net.Inet4Address;
import java.net.InetAddress;
import com.google.common.net.InetAddresses;

/**
 * The ipv6-address type represents an IPv6 address in full,
 *        mixed, shortened, and shortened-mixed notation.  The IPv6
 *        address may include a zone index, separated by a % sign.
 *        The zone index is used to disambiguate identical address
 *        values.  For link-local addresses, the zone index will
 *        typically be the interface index number or the name of an
 *        interface.  If the zone index is not present, the default
 *        zone of the device will be used.
 *        The canonical format of IPv6 addresses uses the compressed
 *        format described in RFC 4291, Section 2.2, item 2 with the
 *        following additional rules: the :: substitution must be
 *        applied to the longest sequence of all-zero 16-bit chunks
 *        in an IPv6 address.  If there is a tie, the first sequence
 *        of all-zero 16-bit chunks is replaced by ::.  Single
 *        all-zero 16-bit chunks are not compressed.  The canonical
 *        format uses lowercase characters and leading zeros are
 *        not allowed.  The canonical format for the zone index is
 *        the numerical format as described in RFC 4007, Section
 *        11.2.
 */
public class Ipv6Address
 implements Serializable {
    private static final long serialVersionUID = 899442110750920218L;
    private static final List<Pattern> patterns;
    public static final List<String> PATTERN_CONSTANTS = ImmutableList.of("^((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(%[\\p{N}\\p{L}]+)?$", "^(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(%.+)?$");

    final private byte [] _binary_form;
    final private String _zone;
    final private static int INADDR4SZ = 4;
    final private static int INADDR6SZ = 16;
    final private static int INT16SZ = 2;

    static {
        final List<Pattern> l = new ArrayList<Pattern>();
        for (String regEx : PATTERN_CONSTANTS) {
            l.add(Pattern.compile(regEx));
        }

        patterns = ImmutableList.copyOf(l);
    }

    @ConstructorProperties("value")
    public Ipv6Address(java.lang.String _value) {


        Preconditions.checkNotNull(_value, "Supplied value may not be null");

        boolean valid = false;
        for (Pattern p : patterns) {
            if (p.matcher(_value).matches()) {
                valid = true;
                break;
            }
        }

        Preconditions.checkArgument(valid, "Supplied value \"%s\" does not match any of the permitted patterns %s", _value, PATTERN_CONSTANTS);

        String [] address =  _value.split("%");

        /*
        * our real IPv6 presentation - we may consider replacing this with int at
        * a later date. Depends on how it is being (ab)used downstream.
        * getByAddress when given a numeric IP does not do any checking besides length
        * and that is verified by the regexp so it should always succeed
        */


        /*
         * Java v6 is fundamentally broken and Google libraries do not fix it.
         * 1. Java will allways implicitly rewrite v4 mapped into v6 as a v4 address
         *      and there is absolutely no way to override this behaviour
         * 2. Guava libraries cannot parse non-canonical IPv6. They will throw an
         *      exception. Even if they did, they re-use the same broken java code
         *      underneath.
         *
         * This is why we have to parse v6 by ourselves.
         *
         * The following conversion code is based on inet_cidr_pton_ipv6 in NetBSD
         *
         * The original BSD code is licensed under standard BSD license. While we
         * are not obliged to provide an attribution, credit where credit is due.
         * As far as why it is similar to Sun's sun.net.util please ask Sun why
         * their code has the same variable names, comments and code flow.
         *
         */

        int colonp;
        char ch;
        boolean saw_xdigit;

        /* Isn't it fun - the above variable names are the same in BSD and Sun sources */

        int val;

        char[] src = address[0].toCharArray();

        byte[] dst = new byte[INADDR6SZ];

        int src_length = src.length;

        colonp = -1;
        int i = 0, j = 0;

        /* Leading :: requires some special handling. */

        /* Isn't it fun - the above comment is again the same in BSD and Sun sources,
         * We will derive our code from BSD. Shakespear always sounds better
         * in original Clingon. So does Dilbert.
         */

        if (src[i] == ':') {
            Preconditions.checkArgument(src[++i] == ':', "Invalid v6 address");
        }

        int curtok = i;
        saw_xdigit = false;


        val = 0;
        while (i < src_length) {
            ch = src[i++];
            int chval = Character.digit(ch, 16);

            /* Business as usual - ipv6 address digit.
             * We can remove all checks from the original BSD code because
             * the regexp has already verified that we are not being fed
             * anything bigger than 0xffff between the separators.
             */

            if (chval != -1) {
                val <<= 4;
                val |= chval;
                saw_xdigit = true;
                continue;
            }

            /* v6 separator */

            if (ch == ':') {
                curtok = i;
                if (!saw_xdigit) {
                    /* no need to check separator position validity - regexp does that */
                    colonp = j;
                    continue;
                }

                /* removed overrun check - the regexp checks for valid data */

                dst[j++] = (byte) ((val >> 8) & 0xff);
                dst[j++] = (byte) (val & 0xff);
                saw_xdigit = false;
                val = 0;
                continue;
            }

            /* frankenstein - v4 attached to v6, mixed notation */

            if (ch == '.' && ((j + INADDR4SZ) <= INADDR6SZ)) {

                /* this has passed the regexp so it is fairly safe to parse it
                 * straight away. As v4 addresses do not suffer from the same
                 * defficiencies as the java v6 implementation we can invoke it
                 * straight away and be done with it
                 */

                Preconditions.checkArgument(j != (INADDR6SZ - INADDR4SZ - 1), "Invalid v4 in v6 mapping");

                InetAddress _inet_form = InetAddresses.forString(address[0].substring(curtok, src_length));

                Preconditions.checkArgument(_inet_form instanceof Inet4Address);

                byte[] v4addr =  _inet_form.getAddress();

                for (int k = 0; k < INADDR4SZ; k++) {
                    dst[j++] = v4addr[k];
                }
                saw_xdigit = false;
                break;
            }
            /* removed parser exit on ivalid char - no need to do it, regexp checks it */
        }
        if (saw_xdigit) {
            Preconditions.checkArgument(j + INT16SZ <= INADDR6SZ, "Overrun in v6 parsing, should not occur");
            dst[j++] = (byte) ((val >> 8) & 0xff);
            dst[j++] = (byte) (val & 0xff);
        }

        if (colonp != -1) {
            int n = j - colonp;

            Preconditions.checkArgument(j != INADDR6SZ, "Overrun in v6 parsing, should not occur");
            for (i = 1; i <= n; i++) {
                dst[INADDR6SZ - i] = dst[colonp + n - i];
                dst[colonp + n - i] = 0;
            }
            j = INADDR6SZ;
        }

        Preconditions.checkArgument(j == INADDR6SZ, "Overrun in v6 parsing, should not occur");

        this._binary_form = dst;

        if (address.length == 2) {
            this._zone = address[1];
        } else {
            this._zone = null;
        }
    }

    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public Ipv6Address(Ipv6Address source) {
        this._binary_form = source._binary_form;
        this._zone = source._zone;
    }

    /**
     * Creates an Ipv6Address from java native representation
     *
     * @param _inet_form - ip address as InetAddress
     */


    public Ipv6Address(InetAddress _inet_form) {

        Preconditions.checkArgument(_inet_form instanceof Inet6Address);

        this._binary_form = _inet_form.getAddress();
        this._zone = null;
    }


    /**
     * @return binary form as used in network
     */

    public byte [] getBinaryForm() {
        return this._binary_form;
    }

    /**
     *
     * @return java native representation
     *
     */


    public InetAddress getInetForm() {
        try {
            return InetAddress.getByAddress(this._binary_form);
        } catch (final Exception e) {}
            /* never occurs - exception is thrown only if
             * 16 bytes are not 16 bytes
             */
        return null;
    }


    public static Ipv6Address getDefaultInstance(String defaultValue) {
        return new Ipv6Address(defaultValue);
    }

    public java.lang.String getValue() {
        /* fake the "expected" value by rebuilding string out of binary */
        StringBuilder sb = new java.lang.StringBuilder();
        /* Yang RFC specifies that the normalized form is RFC 5952, note - java
         * core type is not RFC compliant, guava is.
         */
        sb.append(
            InetAddresses.toAddrString(this.getInetForm())
        );
        if (_zone != null) {
            sb.append("%");
            sb.append(_zone);
        }
        return sb.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((_binary_form == null) ? 0 : Arrays.hashCode(_binary_form));
        return result;
    }

    @Override
    public boolean equals(java.lang.Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Ipv6Address other = (Ipv6Address) obj;
        if(! Arrays.equals(this._binary_form , other._binary_form)) {
            return false;
        }

        /* compare zones if present */

        if (_zone == null) {
            if (other._zone != null) {
                return false;
            }
        } else if (! _zone.equals(other._zone)) {
            return false;
        }

        return true;
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Address.class.getSimpleName()).append(" [");

        builder.append(this.getValue());

        return builder.append(']').toString();
    }

}
