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
 * The ipv6-prefix type represents an IPv6 address prefix.
 *        The prefix length is given by the number following the
 *        slash character and must be less than or equal 128.
 *
 *        A prefix length value of n corresponds to an IP address
 *        mask that has n contiguous 1-bits from the most
 *        significant bit (MSB) and all other bits set to 0.
 *
 *        The IPv6 address should have all bits that do not belong
 *        to the prefix set to zero.
 *
 *        The canonical format of an IPv6 prefix has all bits of
 *        the IPv6 address set to zero that are not part of the
 *        IPv6 prefix.  Furthermore, IPv6 address is represented
 *        in the compressed format described in RFC 4291, Section
 *        2.2, item 2 with the following additional rules: the ::
 *        substitution must be applied to the longest sequence of
 *        all-zero 16-bit chunks in an IPv6 address.  If there is
 *        a tie, the first sequence of all-zero 16-bit chunks is
 *        replaced by ::.  Single all-zero 16-bit chunks are not
 *        compressed.  The canonical format uses lowercase
 *        characters and leading zeros are not allowed.
 *
 */
public class Ipv6Prefix
 implements Serializable {
    private static final long serialVersionUID = 1581919727385448593L;
    private static final List<Pattern> patterns;
    public static final List<String> PATTERN_CONSTANTS = ImmutableList.of("^((:|[0-9a-fA-F]{0,4}):)([0-9a-fA-F]{0,4}:){0,5}((([0-9a-fA-F]{0,4}:)?(:|[0-9a-fA-F]{0,4}))|(((25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9]?[0-9])))(/(([0-9])|([0-9]{2})|(1[0-1][0-9])|(12[0-8])))$", "^(([^:]+:){6}(([^:]+:[^:]+)|(.*\\..*)))|((([^:]+:)*[^:]+)?::(([^:]+:)*[^:]+)?)(/.+)$");


    final private byte [] _binary_form;
    final private int _netmask;
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
    public Ipv6Prefix(java.lang.String _value) {


        Preconditions.checkNotNull(_value, "Supplied value may not be null");

        boolean valid = false;
        for (Pattern p : patterns) {
            if (p.matcher(_value).matches()) {
                valid = true;
                break;
            }
        }

        int mask = 128;

        String [] address =  _value.split("/");

        try {
            mask = Integer.parseInt(address[1]);
            if (mask > 32) {
                valid = false;
            }
        } catch (NumberFormatException e) {
            valid = false;
        }

        Preconditions.checkArgument(valid, "Supplied value \"%s\" does not match any of the permitted patterns %s", _value, PATTERN_CONSTANTS);


        /*
        * our real IPv6 prefix representation - this is borrowed (and documented)
        * from Ipv6Address.
        * We add here canonicalization on the fly. We cannot use the normal SHR with
        * 1 fill approach from Ipv4Prefix because v6 addresses do not fit into a
        * primitive integer type so we cannot efficiently shr any more.
        *
        * This also deals with all the breakage described in Ipv6Address()
        */

        int colonp;
        char ch;
        boolean saw_xdigit;

        /* Isn't it fun - the above variable names are the same in BSD and Sun sources */

        int val;

        char[] src = address[0].toCharArray();

        byte[] dst = new byte[INADDR6SZ];

        int m = mask;

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

                saw_xdigit = false;

                if (m < 0) {
                    /* stop parsing if we are past the mask */
                    break;
                }

                dst[j] = (byte) ((val >> 8) & nextNibble(m)); j++; m = m - 8;

                if (m < 0) {
                    /* stop parsing if we are past the mask */
                    break;
                }

                dst[j] = (byte) (val & nextNibble(m)); j++; m = m - 8;

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
            dst[j] = (byte) ((val >> 8) & nextNibble(m)) ; j++; m = m - 8;
            dst[j] = (byte) (val & nextNibble(m)); j++; m = m - 8;
        }

        if ((j < INADDR6SZ) && (m < 0)) {
            /* past the mask */
            for (i = j; i < INADDR6SZ; i++) {
                dst[i] = 0;
            }
        } else {
            /* normal parsing */
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
        }

        this._binary_form = dst;
        this._netmask = mask;

    }

    static private int nextNibble(int mask) {
        if (mask <= 0) {
            return 0;
        }
        if (mask > 8) {
            return 0xff;
        }

        int topBit = 0x80; /* 1 bit in leftmost address byte */
        int address = 0;
        for (int i = 0; i <= mask; i++) {
            address = topBit | (address >> 1);
        }
        return address;
    }

     /**
     * Canonicalize a v6 prefix
     *
     * @param _prefix - prefix, in byte [] form
     * @param mask - mask - number of bits
     */

    static public void canonicalizeIpv6Prefix(byte [] _prefix, int mask) {

        /* We may convert this to a table lookup later, it is daft how two
         * rep-prefixed instruction available in assembler on nearly any architecture
         * (shr with 1 fill and logical and) needs 10 lines of code in a high level
         * language
         */

        for (int i=0; i < INADDR6SZ; i++) {
            _prefix[i] = (byte) (_prefix[i] & nextNibble(mask));
            mask = mask - 8;
        }
    }


    /**
     * Creates a copy from Source Object.
     *
     * @param source Source object
     */
    public Ipv6Prefix(Ipv6Prefix source) {
        this._binary_form = source._binary_form;
        this._netmask = source._netmask;
    }

    public Ipv6Prefix(byte [] _binary_form, int _netmask) {
        /* check onjava.lang.ly length */
        Preconditions.checkArgument(
            (_binary_form.length == INADDR6SZ),
            "Supplied binary value \"%d\" is not the correct length",
            _binary_form.length
        );
        Preconditions.checkArgument(
            ((_netmask > 128) || (_netmask < 0)),
            "Supplied binary value \"%d\" is not the correct length",
            _binary_form.length
        );
        canonicalizeIpv6Prefix(_binary_form, _netmask);
        this._binary_form = _binary_form;
        this._netmask = _netmask;
    }

    /**
     * @return binary form as used for comparisons
     */

    public byte [] getBinaryForm() {
        return this._binary_form;
    }

    /**
     * @return netmask
     */

    public int getNetMask() {
        return this._netmask;
    }


    public static Ipv6Prefix getDefaultInstance(String defaultValue) {
        return new Ipv6Prefix(defaultValue);
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
        sb.append("/");
        sb.append(_netmask);
        return sb.toString();
    }

    public InetAddress getInetForm() {
        try {
            return InetAddress.getByAddress(this._binary_form);
        } catch (final Exception e) {}
            /* never occurs - exception is thrown only if
             * 16 bytes are not 16 bytes
             */
        return null;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = _netmask;
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
        Ipv6Prefix other = (Ipv6Prefix) obj;
        if(! Arrays.equals(this._binary_form , other._binary_form)) {
            return false;
        }

        /* address portions are equal, return equality of masks */

        return (this._netmask == other._netmask);
    }

    @Override
    public java.lang.String toString() {
        java.lang.StringBuilder builder = new java.lang.StringBuilder(org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.inet.types.rev100924.Ipv6Prefix.class.getSimpleName()).append(" [");

        builder.append(this.getValue());

        return builder.append(']').toString();
    }

}

