package com.github.oowekyala.ijcc.lang.psi

import com.github.oowekyala.ijcc.lang.JavaccTypes
import com.github.oowekyala.ijcc.model.RegexKind
import com.intellij.openapi.diagnostic.Logger
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.util.parents
import com.intellij.psi.util.strictParents
import java.util.regex.Pattern
import java.util.regex.PatternSyntaxException

/*

    Extensions and utilities for the class hierarchy of JccRegularExpression.

 */

private val LOG: Logger = Logger.getInstance("#com.github.oowekyala.ijcc.lang.psi.RegexExtensionsKt")


/** The text matched by this literal regex. */
val JccLiteralRegularExpression.match: String
    get() = stringLiteral.text.removeSurrounding("\"")

fun JccRegularExpression.toPattern(prefixMatch: Boolean = false): Regex? {
    val root = this
    val visitor = RegexResolutionVisitor(prefixMatch)
    root.accept(visitor)
    return if (visitor.unresolved) null
    else try {
        Regex(visitor.builder.toString())
    } catch (e: PatternSyntaxException) {
        LOG.warn(e)
        null
    }
}

/** Returns the regex spec this regex is declared in, or null if this is a regex inside an expansion. */
val JccRegexpLike.specContext: JccRegexprSpec?
    get() = parents().firstOrNull { it is JccRegexprSpec } as? JccRegexprSpec


private class RegexResolutionVisitor(prefixMatch: Boolean) : RegexLikeDFVisitor() {

    val builder = StringBuilder()

    init {
        if (prefixMatch) builder.append('^')
    }

    var unresolved = false

    override fun visitLiteralRegularExpression(o: JccLiteralRegularExpression) {
        builder.append(Pattern.quote(o.match))
    }

    override fun visitInlineRegularExpression(o: JccInlineRegularExpression) {
        val regex = o.regexpElement
        if (regex == null) unresolved = true
        else regex.accept(this)
    }

    override fun visitRegularExpressionReference(o: JccRegularExpressionReference) {
        val ref = o.reference.resolveToken()
        if (ref == null)
            unresolved = true
        else
            ref.getRootRegexElement()?.accept(this@RegexResolutionVisitor)
    }

    override fun visitNamedRegularExpression(o: JccNamedRegularExpression) {
        o.regexpElement?.accept(this)
    }

    override fun visitRegexpSequence(o: JccRegexpSequence) {
        o.regexpUnitList.forEach { it.accept(this) }
    }

    override fun visitRegexpAlternative(o: JccRegexpAlternative) {
        val iterator = o.regexpElementList.iterator()
        // we know there's at least two

        iterator.next().accept(this)
        while (iterator.hasNext()) {
            builder.append('|')
            iterator.next().accept(this)
        }
    }

    override fun visitEofRegularExpression(o: JccEofRegularExpression) {
        builder.append("$")
    }

    override fun visitCharacterList(o: JccCharacterList) {
        if (o.isAnyMatch) {
            builder.append(".")
            return
        }
        builder.append('[')
        if (o.isNegated) builder.append('^')
        o.characterDescriptorList.forEach { it.accept(this) }
        builder.append(']')
    }

    override fun visitParenthesizedRegexpUnit(o: JccParenthesizedRegexpUnit) {
        builder.append('(')
        o.regexpElement.accept(this)
        builder.append(')')
        val occurrenceIndicator = o.lastChildNoWhitespace
        if (occurrenceIndicator != null) {
            builder.append(occurrenceIndicator.text)
        }
    }

    // [[\Q$\E][\QA\E-\QZ\E][\Q_\E][\Qa\E-\Qz\E][\x{00a2}-\x{00a5}][\x{00aa}][\x{00b5}][\x{00ba}][\x{00c0}-\x{00d6}][\x{00d8}-\x{00f6}][\x{00f8}-\x{02c1}][\x{02c6}-\x{02d1}][\x{02e0}-\x{02e4}][\x{02ec}][\x{02ee}][\x{0370}-\x{0374}][\x{0376}-\x{0377}][\x{037a}-\x{037d}][\x{037f}][\x{0386}][\x{0388}-\x{038a}][\x{038c}][\x{038e}-\x{03a1}][\x{03a3}-\x{03f5}][\x{03f7}-\x{0481}][\x{048a}-\x{052f}][\x{0531}-\x{0556}][\x{0559}][\x{0561}-\x{0587}][\x{058f}][\x{05d0}-\x{05ea}][\x{05f0}-\x{05f2}][\x{060b}][\x{0620}-\x{064a}][\x{066e}-\x{066f}][\x{0671}-\x{06d3}][\x{06d5}][\x{06e5}-\x{06e6}][\x{06ee}-\x{06ef}][\x{06fa}-\x{06fc}][\x{06ff}][\x{0710}][\x{0712}-\x{072f}][\x{074d}-\x{07a5}][\x{07b1}][\x{07ca}-\x{07ea}][\x{07f4}-\x{07f5}][\x{07fa}][\x{0800}-\x{0815}][\x{081a}][\x{0824}][\x{0828}][\x{0840}-\x{0858}][\x{08a0}-\x{08b4}][\x{0904}-\x{0939}][\x{093d}][\x{0950}][\x{0958}-\x{0961}][\x{0971}-\x{0980}][\x{0985}-\x{098c}][\x{098f}-\x{0990}][\x{0993}-\x{09a8}][\x{09aa}-\x{09b0}][\x{09b2}][\x{09b6}-\x{09b9}][\x{09bd}][\x{09ce}][\x{09dc}-\x{09dd}][\x{09df}-\x{09e1}][\x{09f0}-\x{09f3}][\x{09fb}][\x{0a05}-\x{0a0a}][\x{0a0f}-\x{0a10}][\x{0a13}-\x{0a28}][\x{0a2a}-\x{0a30}][\x{0a32}-\x{0a33}][\x{0a35}-\x{0a36}][\x{0a38}-\x{0a39}][\x{0a59}-\x{0a5c}][\x{0a5e}][\x{0a72}-\x{0a74}][\x{0a85}-\x{0a8d}][\x{0a8f}-\x{0a91}][\x{0a93}-\x{0aa8}][\x{0aaa}-\x{0ab0}][\x{0ab2}-\x{0ab3}][\x{0ab5}-\x{0ab9}][\x{0abd}][\x{0ad0}][\x{0ae0}-\x{0ae1}][\x{0af1}][\x{0af9}][\x{0b05}-\x{0b0c}][\x{0b0f}-\x{0b10}][\x{0b13}-\x{0b28}][\x{0b2a}-\x{0b30}][\x{0b32}-\x{0b33}][\x{0b35}-\x{0b39}][\x{0b3d}][\x{0b5c}-\x{0b5d}][\x{0b5f}-\x{0b61}][\x{0b71}][\x{0b83}][\x{0b85}-\x{0b8a}][\x{0b8e}-\x{0b90}][\x{0b92}-\x{0b95}][\x{0b99}-\x{0b9a}][\x{0b9c}][\x{0b9e}-\x{0b9f}][\x{0ba3}-\x{0ba4}][\x{0ba8}-\x{0baa}][\x{0bae}-\x{0bb9}][\x{0bd0}][\x{0bf9}][\x{0c05}-\x{0c0c}][\x{0c0e}-\x{0c10}][\x{0c12}-\x{0c28}][\x{0c2a}-\x{0c39}][\x{0c3d}][\x{0c58}-\x{0c5a}][\x{0c60}-\x{0c61}][\x{0c85}-\x{0c8c}][\x{0c8e}-\x{0c90}][\x{0c92}-\x{0ca8}][\x{0caa}-\x{0cb3}][\x{0cb5}-\x{0cb9}][\x{0cbd}][\x{0cde}][\x{0ce0}-\x{0ce1}][\x{0cf1}-\x{0cf2}][\x{0d05}-\x{0d0c}][\x{0d0e}-\x{0d10}][\x{0d12}-\x{0d3a}][\x{0d3d}][\x{0d4e}][\x{0d5f}-\x{0d61}][\x{0d7a}-\x{0d7f}][\x{0d85}-\x{0d96}][\x{0d9a}-\x{0db1}][\x{0db3}-\x{0dbb}][\x{0dbd}][\x{0dc0}-\x{0dc6}][\x{0e01}-\x{0e30}][\x{0e32}-\x{0e33}][\x{0e3f}-\x{0e46}][\x{0e81}-\x{0e82}][\x{0e84}][\x{0e87}-\x{0e88}][\x{0e8a}][\x{0e8d}][\x{0e94}-\x{0e97}][\x{0e99}-\x{0e9f}][\x{0ea1}-\x{0ea3}][\x{0ea5}][\x{0ea7}][\x{0eaa}-\x{0eab}][\x{0ead}-\x{0eb0}][\x{0eb2}-\x{0eb3}][\x{0ebd}][\x{0ec0}-\x{0ec4}][\x{0ec6}][\x{0edc}-\x{0edf}][\x{0f00}][\x{0f40}-\x{0f47}][\x{0f49}-\x{0f6c}][\x{0f88}-\x{0f8c}][\x{1000}-\x{102a}][\x{103f}][\x{1050}-\x{1055}][\x{105a}-\x{105d}][\x{1061}][\x{1065}-\x{1066}][\x{106e}-\x{1070}][\x{1075}-\x{1081}][\x{108e}][\x{10a0}-\x{10c5}][\x{10c7}][\x{10cd}][\x{10d0}-\x{10fa}][\x{10fc}-\x{1248}][\x{124a}-\x{124d}][\x{1250}-\x{1256}][\x{1258}][\x{125a}-\x{125d}][\x{1260}-\x{1288}][\x{128a}-\x{128d}][\x{1290}-\x{12b0}][\x{12b2}-\x{12b5}][\x{12b8}-\x{12be}][\x{12c0}][\x{12c2}-\x{12c5}][\x{12c8}-\x{12d6}][\x{12d8}-\x{1310}][\x{1312}-\x{1315}][\x{1318}-\x{135a}][\x{1380}-\x{138f}][\x{13a0}-\x{13f5}][\x{13f8}-\x{13fd}][\x{1401}-\x{166c}][\x{166f}-\x{167f}][\x{1681}-\x{169a}][\x{16a0}-\x{16ea}][\x{16ee}-\x{16f8}][\x{1700}-\x{170c}][\x{170e}-\x{1711}][\x{1720}-\x{1731}][\x{1740}-\x{1751}][\x{1760}-\x{176c}][\x{176e}-\x{1770}][\x{1780}-\x{17b3}][\x{17d7}][\x{17db}-\x{17dc}][\x{1820}-\x{1877}][\x{1880}-\x{18a8}][\x{18aa}][\x{18b0}-\x{18f5}][\x{1900}-\x{191e}][\x{1950}-\x{196d}][\x{1970}-\x{1974}][\x{1980}-\x{19ab}][\x{19b0}-\x{19c9}][\x{1a00}-\x{1a16}][\x{1a20}-\x{1a54}][\x{1aa7}][\x{1b05}-\x{1b33}][\x{1b45}-\x{1b4b}][\x{1b83}-\x{1ba0}][\x{1bae}-\x{1baf}][\x{1bba}-\x{1be5}][\x{1c00}-\x{1c23}][\x{1c4d}-\x{1c4f}][\x{1c5a}-\x{1c7d}][\x{1ce9}-\x{1cec}][\x{1cee}-\x{1cf1}][\x{1cf5}-\x{1cf6}][\x{1d00}-\x{1dbf}][\x{1e00}-\x{1f15}][\x{1f18}-\x{1f1d}][\x{1f20}-\x{1f45}][\x{1f48}-\x{1f4d}][\x{1f50}-\x{1f57}][\x{1f59}][\x{1f5b}][\x{1f5d}][\x{1f5f}-\x{1f7d}][\x{1f80}-\x{1fb4}][\x{1fb6}-\x{1fbc}][\x{1fbe}][\x{1fc2}-\x{1fc4}][\x{1fc6}-\x{1fcc}][\x{1fd0}-\x{1fd3}][\x{1fd6}-\x{1fdb}][\x{1fe0}-\x{1fec}][\x{1ff2}-\x{1ff4}][\x{1ff6}-\x{1ffc}][\x{203f}-\x{2040}][\x{2054}][\x{2071}][\x{207f}][\x{2090}-\x{209c}][\x{20a0}-\x{20be}][\x{2102}][\x{2107}][\x{210a}-\x{2113}][\x{2115}][\x{2119}-\x{211d}][\x{2124}][\x{2126}][\x{2128}][\x{212a}-\x{212d}][\x{212f}-\x{2139}][\x{213c}-\x{213f}][\x{2145}-\x{2149}][\x{214e}][\x{2160}-\x{2188}][\x{2c00}-\x{2c2e}][\x{2c30}-\x{2c5e}][\x{2c60}-\x{2ce4}][\x{2ceb}-\x{2cee}][\x{2cf2}-\x{2cf3}][\x{2d00}-\x{2d25}][\x{2d27}][\x{2d2d}][\x{2d30}-\x{2d67}][\x{2d6f}][\x{2d80}-\x{2d96}][\x{2da0}-\x{2da6}][\x{2da8}-\x{2dae}][\x{2db0}-\x{2db6}][\x{2db8}-\x{2dbe}][\x{2dc0}-\x{2dc6}][\x{2dc8}-\x{2dce}][\x{2dd0}-\x{2dd6}][\x{2dd8}-\x{2dde}][\x{2e2f}][\x{3005}-\x{3007}][\x{3021}-\x{3029}][\x{3031}-\x{3035}][\x{3038}-\x{303c}][\x{3041}-\x{3096}][\x{309d}-\x{309f}][\x{30a1}-\x{30fa}][\x{30fc}-\x{30ff}][\x{3105}-\x{312d}][\x{3131}-\x{318e}][\x{31a0}-\x{31ba}][\x{31f0}-\x{31ff}][\x{3400}-\x{4db5}][\x{4e00}-\x{9fd5}][\x{a000}-\x{a48c}][\x{a4d0}-\x{a4fd}][\x{a500}-\x{a60c}][\x{a610}-\x{a61f}][\x{a62a}-\x{a62b}][\x{a640}-\x{a66e}][\x{a67f}-\x{a69d}][\x{a6a0}-\x{a6ef}][\x{a717}-\x{a71f}][\x{a722}-\x{a788}][\x{a78b}-\x{a7ad}][\x{a7b0}-\x{a7b7}][\x{a7f7}-\x{a801}][\x{a803}-\x{a805}][\x{a807}-\x{a80a}][\x{a80c}-\x{a822}][\x{a838}][\x{a840}-\x{a873}][\x{a882}-\x{a8b3}][\x{a8f2}-\x{a8f7}][\x{a8fb}][\x{a8fd}][\x{a90a}-\x{a925}][\x{a930}-\x{a946}][\x{a960}-\x{a97c}][\x{a984}-\x{a9b2}][\x{a9cf}][\x{a9e0}-\x{a9e4}][\x{a9e6}-\x{a9ef}][\x{a9fa}-\x{a9fe}][\x{aa00}-\x{aa28}][\x{aa40}-\x{aa42}][\x{aa44}-\x{aa4b}][\x{aa60}-\x{aa76}][\x{aa7a}][\x{aa7e}-\x{aaaf}][\x{aab1}][\x{aab5}-\x{aab6}][\x{aab9}-\x{aabd}][\x{aac0}][\x{aac2}][\x{aadb}-\x{aadd}][\x{aae0}-\x{aaea}][\x{aaf2}-\x{aaf4}][\x{ab01}-\x{ab06}][\x{ab09}-\x{ab0e}][\x{ab11}-\x{ab16}][\x{ab20}-\x{ab26}][\x{ab28}-\x{ab2e}][\x{ab30}-\x{ab5a}][\x{ab5c}-\x{ab65}][\x{ab70}-\x{abe2}][\x{ac00}-\x{d7a3}][\x{d7b0}-\x{d7c6}][\x{d7cb}-\x{d7fb}][\x{f900}-\x{fa6d}][\x{fa70}-\x{fad9}][\x{fb00}-\x{fb06}][\x{fb13}-\x{fb17}][\x{fb1d}][\x{fb1f}-\x{fb28}][\x{fb2a}-\x{fb36}][\x{fb38}-\x{fb3c}][\x{fb3e}][\x{fb40}-\x{fb41}][\x{fb43}-\x{fb44}][\x{fb46}-\x{fbb1}][\x{fbd3}-\x{fd3d}][\x{fd50}-\x{fd8f}][\x{fd92}-\x{fdc7}][\x{fdf0}-\x{fdfc}][\x{fe33}-\x{fe34}][\x{fe4d}-\x{fe4f}][\x{fe69}][\x{fe70}-\x{fe74}][\x{fe76}-\x{fefc}][\x{ff04}][\x{ff21}-\x{ff3a}][\x{ff3f}][\x{ff41}-\x{ff5a}][\x{ff66}-\x{ffbe}][\x{ffc2}-\x{ffc7}][\x{ffca}-\x{ffcf}][\x{ffd2}-\x{ffd7}][\x{ffda}-\x{ffdc}][\x{ffe0}-\x{ffe1}][\x{ffe5}-\x{ffe6}]]([[\x{0000}-\x{0008}][\x{000e}-\x{001b}][\Q$\E][\Q0\E-\Q9\E][\QA\E-\QZ\E][\Q_\E][\Qa\E-\Qz\E][\x{007f}-\x{009f}][\x{00a2}-\x{00a5}][\x{00aa}][\x{00ad}][\x{00b5}][\x{00ba}][\x{00c0}-\x{00d6}][\x{00d8}-\x{00f6}][\x{00f8}-\x{02c1}][\x{02c6}-\x{02d1}][\x{02e0}-\x{02e4}][\x{02ec}][\x{02ee}][\x{0300}-\x{0374}][\x{0376}-\x{0377}][\x{037a}-\x{037d}][\x{037f}][\x{0386}][\x{0388}-\x{038a}][\x{038c}][\x{038e}-\x{03a1}][\x{03a3}-\x{03f5}][\x{03f7}-\x{0481}][\x{0483}-\x{0487}][\x{048a}-\x{052f}][\x{0531}-\x{0556}][\x{0559}][\x{0561}-\x{0587}][\x{058f}][\x{0591}-\x{05bd}][\x{05bf}][\x{05c1}-\x{05c2}][\x{05c4}-\x{05c5}][\x{05c7}][\x{05d0}-\x{05ea}][\x{05f0}-\x{05f2}][\x{0600}-\x{0605}][\x{060b}][\x{0610}-\x{061a}][\x{061c}][\x{0620}-\x{0669}][\x{066e}-\x{06d3}][\x{06d5}-\x{06dd}][\x{06df}-\x{06e8}][\x{06ea}-\x{06fc}][\x{06ff}][\x{070f}-\x{074a}][\x{074d}-\x{07b1}][\x{07c0}-\x{07f5}][\x{07fa}][\x{0800}-\x{082d}][\x{0840}-\x{085b}][\x{08a0}-\x{08b4}][\x{08e3}-\x{0963}][\x{0966}-\x{096f}][\x{0971}-\x{0983}][\x{0985}-\x{098c}][\x{098f}-\x{0990}][\x{0993}-\x{09a8}][\x{09aa}-\x{09b0}][\x{09b2}][\x{09b6}-\x{09b9}][\x{09bc}-\x{09c4}][\x{09c7}-\x{09c8}][\x{09cb}-\x{09ce}][\x{09d7}][\x{09dc}-\x{09dd}][\x{09df}-\x{09e3}][\x{09e6}-\x{09f3}][\x{09fb}][\x{0a01}-\x{0a03}][\x{0a05}-\x{0a0a}][\x{0a0f}-\x{0a10}][\x{0a13}-\x{0a28}][\x{0a2a}-\x{0a30}][\x{0a32}-\x{0a33}][\x{0a35}-\x{0a36}][\x{0a38}-\x{0a39}][\x{0a3c}][\x{0a3e}-\x{0a42}][\x{0a47}-\x{0a48}][\x{0a4b}-\x{0a4d}][\x{0a51}][\x{0a59}-\x{0a5c}][\x{0a5e}][\x{0a66}-\x{0a75}][\x{0a81}-\x{0a83}][\x{0a85}-\x{0a8d}][\x{0a8f}-\x{0a91}][\x{0a93}-\x{0aa8}][\x{0aaa}-\x{0ab0}][\x{0ab2}-\x{0ab3}][\x{0ab5}-\x{0ab9}][\x{0abc}-\x{0ac5}][\x{0ac7}-\x{0ac9}][\x{0acb}-\x{0acd}][\x{0ad0}][\x{0ae0}-\x{0ae3}][\x{0ae6}-\x{0aef}][\x{0af1}][\x{0af9}][\x{0b01}-\x{0b03}][\x{0b05}-\x{0b0c}][\x{0b0f}-\x{0b10}][\x{0b13}-\x{0b28}][\x{0b2a}-\x{0b30}][\x{0b32}-\x{0b33}][\x{0b35}-\x{0b39}][\x{0b3c}-\x{0b44}][\x{0b47}-\x{0b48}][\x{0b4b}-\x{0b4d}][\x{0b56}-\x{0b57}][\x{0b5c}-\x{0b5d}][\x{0b5f}-\x{0b63}][\x{0b66}-\x{0b6f}][\x{0b71}][\x{0b82}-\x{0b83}][\x{0b85}-\x{0b8a}][\x{0b8e}-\x{0b90}][\x{0b92}-\x{0b95}][\x{0b99}-\x{0b9a}][\x{0b9c}][\x{0b9e}-\x{0b9f}][\x{0ba3}-\x{0ba4}][\x{0ba8}-\x{0baa}][\x{0bae}-\x{0bb9}][\x{0bbe}-\x{0bc2}][\x{0bc6}-\x{0bc8}][\x{0bca}-\x{0bcd}][\x{0bd0}][\x{0bd7}][\x{0be6}-\x{0bef}][\x{0bf9}][\x{0c00}-\x{0c03}][\x{0c05}-\x{0c0c}][\x{0c0e}-\x{0c10}][\x{0c12}-\x{0c28}][\x{0c2a}-\x{0c39}][\x{0c3d}-\x{0c44}][\x{0c46}-\x{0c48}][\x{0c4a}-\x{0c4d}][\x{0c55}-\x{0c56}][\x{0c58}-\x{0c5a}][\x{0c60}-\x{0c63}][\x{0c66}-\x{0c6f}][\x{0c81}-\x{0c83}][\x{0c85}-\x{0c8c}][\x{0c8e}-\x{0c90}][\x{0c92}-\x{0ca8}][\x{0caa}-\x{0cb3}][\x{0cb5}-\x{0cb9}][\x{0cbc}-\x{0cc4}][\x{0cc6}-\x{0cc8}][\x{0cca}-\x{0ccd}][\x{0cd5}-\x{0cd6}][\x{0cde}][\x{0ce0}-\x{0ce3}][\x{0ce6}-\x{0cef}][\x{0cf1}-\x{0cf2}][\x{0d01}-\x{0d03}][\x{0d05}-\x{0d0c}][\x{0d0e}-\x{0d10}][\x{0d12}-\x{0d3a}][\x{0d3d}-\x{0d44}][\x{0d46}-\x{0d48}][\x{0d4a}-\x{0d4e}][\x{0d57}][\x{0d5f}-\x{0d63}][\x{0d66}-\x{0d6f}][\x{0d7a}-\x{0d7f}][\x{0d82}-\x{0d83}][\x{0d85}-\x{0d96}][\x{0d9a}-\x{0db1}][\x{0db3}-\x{0dbb}][\x{0dbd}][\x{0dc0}-\x{0dc6}][\x{0dca}][\x{0dcf}-\x{0dd4}][\x{0dd6}][\x{0dd8}-\x{0ddf}][\x{0de6}-\x{0def}][\x{0df2}-\x{0df3}][\x{0e01}-\x{0e3a}][\x{0e3f}-\x{0e4e}][\x{0e50}-\x{0e59}][\x{0e81}-\x{0e82}][\x{0e84}][\x{0e87}-\x{0e88}][\x{0e8a}][\x{0e8d}][\x{0e94}-\x{0e97}][\x{0e99}-\x{0e9f}][\x{0ea1}-\x{0ea3}][\x{0ea5}][\x{0ea7}][\x{0eaa}-\x{0eab}][\x{0ead}-\x{0eb9}][\x{0ebb}-\x{0ebd}][\x{0ec0}-\x{0ec4}][\x{0ec6}][\x{0ec8}-\x{0ecd}][\x{0ed0}-\x{0ed9}][\x{0edc}-\x{0edf}][\x{0f00}][\x{0f18}-\x{0f19}][\x{0f20}-\x{0f29}][\x{0f35}][\x{0f37}][\x{0f39}][\x{0f3e}-\x{0f47}][\x{0f49}-\x{0f6c}][\x{0f71}-\x{0f84}][\x{0f86}-\x{0f97}][\x{0f99}-\x{0fbc}][\x{0fc6}][\x{1000}-\x{1049}][\x{1050}-\x{109d}][\x{10a0}-\x{10c5}][\x{10c7}][\x{10cd}][\x{10d0}-\x{10fa}][\x{10fc}-\x{1248}][\x{124a}-\x{124d}][\x{1250}-\x{1256}][\x{1258}][\x{125a}-\x{125d}][\x{1260}-\x{1288}][\x{128a}-\x{128d}][\x{1290}-\x{12b0}][\x{12b2}-\x{12b5}][\x{12b8}-\x{12be}][\x{12c0}][\x{12c2}-\x{12c5}][\x{12c8}-\x{12d6}][\x{12d8}-\x{1310}][\x{1312}-\x{1315}][\x{1318}-\x{135a}][\x{135d}-\x{135f}][\x{1380}-\x{138f}][\x{13a0}-\x{13f5}][\x{13f8}-\x{13fd}][\x{1401}-\x{166c}][\x{166f}-\x{167f}][\x{1681}-\x{169a}][\x{16a0}-\x{16ea}][\x{16ee}-\x{16f8}][\x{1700}-\x{170c}][\x{170e}-\x{1714}][\x{1720}-\x{1734}][\x{1740}-\x{1753}][\x{1760}-\x{176c}][\x{176e}-\x{1770}][\x{1772}-\x{1773}][\x{1780}-\x{17d3}][\x{17d7}][\x{17db}-\x{17dd}][\x{17e0}-\x{17e9}][\x{180b}-\x{180e}][\x{1810}-\x{1819}][\x{1820}-\x{1877}][\x{1880}-\x{18aa}][\x{18b0}-\x{18f5}][\x{1900}-\x{191e}][\x{1920}-\x{192b}][\x{1930}-\x{193b}][\x{1946}-\x{196d}][\x{1970}-\x{1974}][\x{1980}-\x{19ab}][\x{19b0}-\x{19c9}][\x{19d0}-\x{19d9}][\x{1a00}-\x{1a1b}][\x{1a20}-\x{1a5e}][\x{1a60}-\x{1a7c}][\x{1a7f}-\x{1a89}][\x{1a90}-\x{1a99}][\x{1aa7}][\x{1ab0}-\x{1abd}][\x{1b00}-\x{1b4b}][\x{1b50}-\x{1b59}][\x{1b6b}-\x{1b73}][\x{1b80}-\x{1bf3}][\x{1c00}-\x{1c37}][\x{1c40}-\x{1c49}][\x{1c4d}-\x{1c7d}][\x{1cd0}-\x{1cd2}][\x{1cd4}-\x{1cf6}][\x{1cf8}-\x{1cf9}][\x{1d00}-\x{1df5}][\x{1dfc}-\x{1f15}][\x{1f18}-\x{1f1d}][\x{1f20}-\x{1f45}][\x{1f48}-\x{1f4d}][\x{1f50}-\x{1f57}][\x{1f59}][\x{1f5b}][\x{1f5d}][\x{1f5f}-\x{1f7d}][\x{1f80}-\x{1fb4}][\x{1fb6}-\x{1fbc}][\x{1fbe}][\x{1fc2}-\x{1fc4}][\x{1fc6}-\x{1fcc}][\x{1fd0}-\x{1fd3}][\x{1fd6}-\x{1fdb}][\x{1fe0}-\x{1fec}][\x{1ff2}-\x{1ff4}][\x{1ff6}-\x{1ffc}][\x{200b}-\x{200f}][\x{202a}-\x{202e}][\x{203f}-\x{2040}][\x{2054}][\x{2060}-\x{2064}][\x{2066}-\x{206f}][\x{2071}][\x{207f}][\x{2090}-\x{209c}][\x{20a0}-\x{20be}][\x{20d0}-\x{20dc}][\x{20e1}][\x{20e5}-\x{20f0}][\x{2102}][\x{2107}][\x{210a}-\x{2113}][\x{2115}][\x{2119}-\x{211d}][\x{2124}][\x{2126}][\x{2128}][\x{212a}-\x{212d}][\x{212f}-\x{2139}][\x{213c}-\x{213f}][\x{2145}-\u21 49][\x{214e}][\x{2160}-\x{2188}][\x{2c00}-\x{2c2e}][\x{2c30}-\x{2c5e}][\x{2c60}-\x{2ce4}][\x{2ceb}-\x{2cf3}][\x{2d00}-\x{2d25}][\x{2d27}][\x{2d2d}][\x{2d30}-\x{2d67}][\x{2d6f}][\x{2d7f}-\x{2d96}][\x{2da0}-\x{2da6}][\x{2da8}-\x{2dae}][\x{2db0}-\x{2db6}][\x{2db8}-\x{2dbe}][\x{2dc0}-\x{2dc6}][\x{2dc8}-\x{2dce}][\x{2dd0}-\x{2dd6}][\x{2dd8}-\x{2dde}][\x{2de0}-\x{2dff}][\x{2e2f}][\x{3005}-\x{3007}][\x{3021}-\x{302f}][\x{3031}-\x{3035}][\x{3038}-\x{303c}][\x{3041}-\x{3096}][\x{3099}-\x{309a}][\x{309d}-\x{309f}][\x{30a1}-\x{30fa}][\x{30fc}-\x{30ff}][\x{3105}-\x{312d}][\x{3131}-\x{318e}][\x{31a0}-\x{31ba}][\x{31f0}-\x{31ff}][\x{3400}-\x{4db5}][\x{4e00}-\x{9fd5}][\x{a000}-\x{a48c}][\x{a4d0}-\x{a4fd}][\x{a500}-\x{a60c}][\x{a610}-\x{a62b}][\x{a640}-\x{a66f}][\x{a674}-\x{a67d}][\x{a67f}-\x{a6f1}][\x{a717}-\x{a71f}][\x{a722}-\x{a788}][\x{a78b}-\x{a7ad}][\x{a7b0}-\x{a7b7}][\x{a7f7}-\x{a827}][\x{a838}][\x{a840}-\x{a873}][\x{a880}-\x{a8c4}][\x{a8d0}-\x{a8d9}][\x{a8e0}-\x{a8f7}][\x{a8fb}][\x{a8fd}][\x{a900}-\x{a92d}][\x{a930}-\x{a953}][\x{a960}-\x{a97c}][\x{a980}-\x{a9c0}][\x{a9cf}-\x{a9d9}][\x{a9e0}-\x{a9fe}][\x{aa00}-\x{aa36}][\x{aa40}-\x{aa4d}][\x{aa50}-\x{aa59}][\x{aa60}-\x{aa76}][\x{aa7a}-\x{aac2}][\x{aadb}-\x{aadd}][\x{aae0}-\x{aaef}][\x{aaf2}-\x{aaf6}][\x{ab01}-\x{ab06}][\x{ab09}-\x{ab0e}][\x{ab11}-\x{ab16}][\x{ab20}-\x{ab26}][\x{ab28}-\x{ab2e}][\x{ab30}-\x{ab5a}][\x{ab5c}-\x{ab65}][\x{ab70}-\x{abea}][\x{abec}-\x{abed}][\x{abf0}-\x{abf9}][\x{ac00}-\x{d7a3}][\x{d7b0}-\x{d7c6}][\x{d7cb}-\x{d7fb}][\x{f900}-\x{fa6d}][\x{fa70}-\x{fad9}][\x{fb00}-\x{fb06}][\x{fb13}-\x{fb17}][\x{fb1d}-\x{fb28}][\x{fb2a}-\x{fb36}][\x{fb38}-\x{fb3c}][\x{fb3e}][\x{fb40}-\x{fb41}][\x{fb43}-\x{fb44}][\x{fb46}-\x{fbb1}][\x{fbd3}-\x{fd3d}][\x{fd50}-\x{fd8f}][\x{fd92}-\x{fdc7}][\x{fdf0}-\x{fdfc}][\x{fe00}-\x{fe0f}][\x{fe20}-\x{fe2f}][\x{fe33}-\x{fe34}][\x{fe4d}-\x{fe4f}][\x{fe69}][\x{fe70}-\x{fe74}][\x{fe76}-\x{fefc}][\x{feff}][\x{ff04}][\x{ff10}-\x{ff19}][\x{ff21}-\x{ff3a}][\x{ff3f}][\x{ff41}-\x{ff5a}][\x{ff66}-\x{ffbe}][\x{ffc2}-\x{ffc7}][\x{ffca}-\x{ffcf}][\x{ffd2}-\x{ffd7}][\x{ffda}-\x{ffdc}][\x{ffe0}-\x{ffe1}][\x{ffe5}-\x{ffe6}][\x{fff9}-\x{fffb}]])*
    override fun visitCharacterDescriptor(o: JccCharacterDescriptor) {
        fun String.quoteRegexChar(): String = when {
            length == 1                      -> Pattern.quote(this)
            this == "\\n"
                    || this == "\\r"
                    || this == "\\f"
                    || this == "\\b"
                    || this == "\\a"
                    || this == "\\e"
                    || this == "\\\\"        -> this
            this.matches(UnicodeStringRegex) -> "\\x{" + this.removePrefix("\\u") + "}"
            else                             -> this // ??
        }

        if (o.baseCharAsString == "\\u212f") return

        builder.append('[')
        builder.append(o.baseCharAsString.quoteRegexChar())
        val toChar = o.toCharAsString
        if (toChar != null) {
            builder.append("-").append(toChar.quoteRegexChar())
        }
        builder.append(']')
    }

    // [[\Q$\E][\QA\E-\QZ\E][\Q_\E][\Qa\E-\Qz\E][\x{00a2}-\x{00a5}][\x{00aa}][\x{00b5}][\x{00ba}][\x{00c0}-\x{00d6}][\x{00d8}-\x{00f6}][\x{00f8}-\x{02c1}][\x{02c6}-\x{02d1}][\x{02e0}-\x{02e4}][\x{02ec}][\x{02ee}][\x{0370}-\x{0374}][\x{0376}-\x{0377}][\x{037a}-\x{037d}][\x{037f}][\x{0386}][\x{0388}-\x{038a}][\x{038c}][\x{038e}-\x{03a1}][\x{03a3}-\x{03f5}][\x{03f7}-\x{0481}][\x{048a}-\x{052f}][\x{0531}-\x{0556}][\x{0559}][\x{0561}-\x{0587}][\x{058f}][\x{05d0}-\x{05ea}][\x{05f0}-\x{05f2}][\x{060b}][\x{0620}-\x{064a}][\x{066e}-\x{066f}][\x{0671}-\x{06d3}][\x{06d5}][\x{06e5}-\x{06e6}][\x{06ee}-\x{06ef}][\x{06fa}-\x{06fc}][\x{06ff}][\x{0710}][\x{0712}-\x{072f}][\x{074d}-\x{07a5}][\x{07b1}][\x{07ca}-\x{07ea}][\x{07f4}-\x{07f5}][\x{07fa}][\x{0800}-\x{0815}][\x{081a}][\x{0824}][\x{0828}][\x{0840}-\x{0858}][\x{08a0}-\x{08b4}][\x{0904}-\x{0939}][\x{093d}][\x{0950}][\x{0958}-\x{0961}][\x{0971}-\x{0980}][\x{0985}-\x{098c}][\x{098f}-\x{0990}][\x{0993}-\x{09a8}][\x{09aa}-\x{09b0}][\x{09b2}][\x{09b6}-\x{09b9}][\x{09bd}][\x{09ce}][\x{09dc}-\x{09dd}][\x{09df}-\x{09e1}][\x{09f0}-\x{09f3}][\x{09fb}][\x{0a05}-\x{0a0a}][\x{0a0f}-\x{0a10}][\x{0a13}-\x{0a28}][\x{0a2a}-\x{0a30}][\x{0a32}-\x{0a33}][\x{0a35}-\x{0a36}][\x{0a38}-\x{0a39}][\x{0a59}-\x{0a5c}][\x{0a5e}][\x{0a72}-\x{0a74}][\x{0a85}-\x{0a8d}][\x{0a8f}-\x{0a91}][\x{0a93}-\x{0aa8}][\x{0aaa}-\x{0ab0}][\x{0ab2}-\x{0ab3}][\x{0ab5}-\x{0ab9}][\x{0abd}][\x{0ad0}][\x{0ae0}-\x{0ae1}][\x{0af1}][\x{0af9}][\x{0b05}-\x{0b0c}][\x{0b0f}-\x{0b10}][\x{0b13}-\x{0b28}][\x{0b2a}-\x{0b30}][\x{0b32}-\x{0b33}][\x{0b35}-\x{0b39}][\x{0b3d}][\x{0b5c}-\x{0b5d}][\x{0b5f}-\x{0b61}][\x{0b71}][\x{0b83}][\x{0b85}-\x{0b8a}][\x{0b8e}-\x{0b90}][\x{0b92}-\x{0b95}][\x{0b99}-\x{0b9a}][\x{0b9c}][\x{0b9e}-\x{0b9f}][\x{0ba3}-\x{0ba4}][\x{0ba8}-\x{0baa}][\x{0bae}-\x{0bb9}][\x{0bd0}][\x{0bf9}][\x{0c05}-\x{0c0c}][\x{0c0e}-\x{0c10}][\x{0c12}-\x{0c28}][\x{0c2a}-\x{0c39}][\x{0c3d}][\x{0c58}-\x{0c5a}][\x{0c60}-\x{0c61}][\x{0c85}-\x{0c8c}][\x{0c8e}-\x{0c90}][\x{0c92}-\x{0ca8}][\x{0caa}-\x{0cb3}][\x{0cb5}-\x{0cb9}][\x{0cbd}][\x{0cde}][\x{0ce0}-\x{0ce1}][\x{0cf1}-\x{0cf2}][\x{0d05}-\x{0d0c}][\x{0d0e}-\x{0d10}][\x{0d12}-\x{0d3a}][\x{0d3d}][\x{0d4e}][\x{0d5f}-\x{0d61}][\x{0d7a}-\x{0d7f}][\x{0d85}-\x{0d96}][\x{0d9a}-\x{0db1}][\x{0db3}-\x{0dbb}][\x{0dbd}][\x{0dc0}-\x{0dc6}][\x{0e01}-\x{0e30}][\x{0e32}-\x{0e33}][\x{0e3f}-\x{0e46}][\x{0e81}-\x{0e82}][\x{0e84}][\x{0e87}-\x{0e88}][\x{0e8a}][\x{0e8d}][\x{0e94}-\x{0e97}][\x{0e99}-\x{0e9f}][\x{0ea1}-\x{0ea3}][\x{0ea5}][\x{0ea7}][\x{0eaa}-\x{0eab}][\x{0ead}-\x{0eb0}][\x{0eb2}-\x{0eb3}][\x{0ebd}][\x{0ec0}-\x{0ec4}][\x{0ec6}][\x{0edc}-\x{0edf}][\x{0f00}][\x{0f40}-\x{0f47}][\x{0f49}-\x{0f6c}][\x{0f88}-\x{0f8c}][\x{1000}-\x{102a}][\x{103f}][\x{1050}-\x{1055}][\x{105a}-\x{105d}][\x{1061}][\x{1065}-\x{1066}][\x{106e}-\x{1070}][\x{1075}-\x{1081}][\x{108e}][\x{10a0}-\x{10c5}][\x{10c7}][\x{10cd}][\x{10d0}-\x{10fa}][\x{10fc}-\x{1248}][\x{124a}-\x{124d}][\x{1250}-\x{1256}][\x{1258}][\x{125a}-\x{125d}][\x{1260}-\x{1288}][\x{128a}-\x{128d}][\x{1290}-\x{12b0}][\x{12b2}-\x{12b5}][\x{12b8}-\x{12be}][\x{12c0}][\x{12c2}-\x{12c5}][\x{12c8}-\x{12d6}][\x{12d8}-\x{1310}][\x{1312}-\x{1315}][\x{1318}-\x{135a}][\x{1380}-\x{138f}][\x{13a0}-\x{13f5}][\x{13f8}-\x{13fd}][\x{1401}-\x{166c}][\x{166f}-\x{167f}][\x{1681}-\x{169a}][\x{16a0}-\x{16ea}][\x{16ee}-\x{16f8}][\x{1700}-\x{170c}][\x{170e}-\x{1711}][\x{1720}-\x{1731}][\x{1740}-\x{1751}][\x{1760}-\x{176c}][\x{176e}-\x{1770}][\x{1780}-\x{17b3}][\x{17d7}][\x{17db}-\x{17dc}][\x{1820}-\x{1877}][\x{1880}-\x{18a8}][\x{18aa}][\x{18b0}-\x{18f5}][\x{1900}-\x{191e}][\x{1950}-\x{196d}][\x{1970}-\x{1974}][\x{1980}-\x{19ab}][\x{19b0}-\x{19c9}][\x{1a00}-\x{1a16}][\x{1a20}-\x{1a54}][\x{1aa7}][\x{1b05}-\x{1b33}][\x{1b45}-\x{1b4b}][\x{1b83}-\x{1ba0}][\x{1bae}-\x{1baf}][\x{1bba}-\x{1be5}][\x{1c00}-\x{1c23}][\x{1c4d}-\x{1c4f}][\x{1c5a}-\x{1c7d}][\x{1ce9}-\x{1cec}][\x{1cee}-\x{1cf1}][\x{1cf5}-\x{1cf6}][\x{1d00}-\x{1dbf}][\x{1e00}-\x{1f15}][\x{1f18}-\x{1f1d}][\x{1f20}-\x{1f45}][\x{1f48}-\x{1f4d}][\x{1f50}-\x{1f57}][\x{1f59}][\x{1f5b}][\x{1f5d}][\x{1f5f}-\x{1f7d}][\x{1f80}-\x{1fb4}][\x{1fb6}-\x{1fbc}][\x{1fbe}][\x{1fc2}-\x{1fc4}][\x{1fc6}-\x{1fcc}][\x{1fd0}-\x{1fd3}][\x{1fd6}-\x{1fdb}][\x{1fe0}-\x{1fec}][\x{1ff2}-\x{1ff4}][\x{1ff6}-\x{1ffc}][\x{203f}-\x{2040}][\x{2054}][\x{2071}][\x{207f}][\x{2090}-\x{209c}][\x{20a0}-\x{20be}][\x{2102}][\x{2107}][\x{210a}-\x{2113}][\x{2115}][\x{2119}-\x{211d}][\x{2124}][\x{2126}][\x{2128}][\x{212a}-\x{212d}][\x{212f}-\x{2139}][\x{213c}-\x{213f}][\x{2145}-\x{2149}][\x{214e}][\x{2160}-\x{2188}][\x{2c00}-\x{2c2e}][\x{2c30}-\x{2c5e}][\x{2c60}-\x{2ce4}][\x{2ceb}-\x{2cee}][\x{2cf2}-\x{2cf3}][\x{2d00}-\x{2d25}][\x{2d27}][\x{2d2d}][\x{2d30}-\x{2d67}][\x{2d6f}][\x{2d80}-\x{2d96}][\x{2da0}-\x{2da6}][\x{2da8}-\x{2dae}][\x{2db0}-\x{2db6}][\x{2db8}-\x{2dbe}][\x{2dc0}-\x{2dc6}][\x{2dc8}-\x{2dce}][\x{2dd0}-\x{2dd6}][\x{2dd8}-\x{2dde}][\x{2e2f}][\x{3005}-\x{3007}][\x{3021}-\x{3029}][\x{3031}-\x{3035}][\x{3038}-\x{303c}][\x{3041}-\x{3096}][\x{309d}-\x{309f}][\x{30a1}-\x{30fa}][\x{30fc}-\x{30ff}][\x{3105}-\x{312d}][\x{3131}-\x{318e}][\x{31a0}-\x{31ba}][\x{31f0}-\x{31ff}][\x{3400}-\x{4db5}][\x{4e00}-\x{9fd5}][\x{a000}-\x{a48c}][\x{a4d0}-\x{a4fd}][\x{a500}-\x{a60c}][\x{a610}-\x{a61f}][\x{a62a}-\x{a62b}][\x{a640}-\x{a66e}][\x{a67f}-\x{a69d}][\x{a6a0}-\x{a6ef}][\x{a717}-\x{a71f}][\x{a722}-\x{a788}][\x{a78b}-\x{a7ad}][\x{a7b0}-\x{a7b7}][\x{a7f7}-\x{a801}][\x{a803}-\x{a805}][\x{a807}-\x{a80a}][\x{a80c}-\x{a822}][\x{a838}][\x{a840}-\x{a873}][\x{a882}-\x{a8b3}][\x{a8f2}-\x{a8f7}][\x{a8fb}][\x{a8fd}][\x{a90a}-\x{a925}][\x{a930}-\x{a946}][\x{a960}-\x{a97c}][\x{a984}-\x{a9b2}][\x{a9cf}][\x{a9e0}-\x{a9e4}][\x{a9e6}-\x{a9ef}][\x{a9fa}-\x{a9fe}][\x{aa00}-\x{aa28}][\x{aa40}-\x{aa42}][\x{aa44}-\x{aa4b}][\x{aa60}-\x{aa76}][\x{aa7a}][\x{aa7e}-\x{aaaf}][\x{aab1}][\x{aab5}-\x{aab6}][\x{aab9}-\x{aabd}][\x{aac0}][\x{aac2}][\x{aadb}-\x{aadd}][\x{aae0}-\x{aaea}][\x{aaf2}-\x{aaf4}][\x{ab01}-\x{ab06}][\x{ab09}-\x{ab0e}][\x{ab11}-\x{ab16}][\x{ab20}-\x{ab26}][\x{ab28}-\x{ab2e}][\x{ab30}-\x{ab5a}][\x{ab5c}-\x{ab65}][\x{ab70}-\x{abe2}][\x{ac00}-\x{d7a3}][\x{d7b0}-\x{d7c6}][\x{d7cb}-\x{d7fb}][\x{f900}-\x{fa6d}][\x{fa70}-\x{fad9}][\x{fb00}-\x{fb06}][\x{fb13}-\x{fb17}][\x{fb1d}][\x{fb1f}-\x{fb28}][\x{fb2a}-\x{fb36}][\x{fb38}-\x{fb3c}][\x{fb3e}][\x{fb40}-\x{fb41}][\x{fb43}-\x{fb44}][\x{fb46}-\x{fbb1}][\x{fbd3}-\x{fd3d}][\x{fd50}-\x{fd8f}][\x{fd92}-\x{fdc7}][\x{fdf0}-\x{fdfc}][\x{fe33}-\x{fe34}][\x{fe4d}-\x{fe4f}][\x{fe69}][\x{fe70}-\x{fe74}][\x{fe76}-\x{fefc}][\x{ff04}][\x{ff21}-\x{ff3a}][\x{ff3f}][\x{ff41}-\x{ff5a}][\x{ff66}-\x{ffbe}][\x{ffc2}-\x{ffc7}][\x{ffca}-\x{ffcf}][\x{ffd2}-\x{ffd7}][\x{ffda}-\x{ffdc}][\x{ffe0}-\x{ffe1}][\x{ffe5}-\x{ffe6}]]([[\x{0000}-\x{0008}][\x{000e}-\x{001b}][\Q$\E][\Q0\E-\Q9\E][\QA\E-\QZ\E][\Q_\E][\Qa\E-\Qz\E][\x{007f}-\x{009f}][\x{00a2}-\x{00a5}][\x{00aa}][\x{00ad}][\x{00b5}][\x{00ba}][\x{00c0}-\x{00d6}][\x{00d8}-\x{00f6}][\x{00f8}-\x{02c1}][\x{02c6}-\x{02d1}][\x{02e0}-\x{02e4}][\x{02ec}][\x{02ee}][\x{0300}-\x{0374}][\x{0376}-\x{0377}][\x{037a}-\x{037d}][\x{037f}][\x{0386}][\x{0388}-\x{038a}][\x{038c}][\x{038e}-\x{03a1}][\x{03a3}-\x{03f5}][\x{03f7}-\x{0481}][\x{0483}-\x{0487}][\x{048a}-\x{052f}][\x{0531}-\x{0556}][\x{0559}][\x{0561}-\x{0587}][\x{058f}][\x{0591}-\x{05bd}][\x{05bf}][\x{05c1}-\x{05c2}][\x{05c4}-\x{05c5}][\x{05c7}][\x{05d0}-\x{05ea}][\x{05f0}-\x{05f2}][\x{0600}-\x{0605}][\x{060b}][\x{0610}-\x{061a}][\x{061c}][\x{0620}-\x{0669}][\x{066e}-\x{06d3}][\x{06d5}-\x{06dd}][\x{06df}-\x{06e8}][\x{06ea}-\x{06fc}][\x{06ff}][\x{070f}-\x{074a}][\x{074d}-\x{07b1}][\x{07c0}-\x{07f5}][\x{07fa}][\x{0800}-\x{082d}][\x{0840}-\x{085b}][\x{08a0}-\x{08b4}][\x{08e3}-\x{0963}][\x{0966}-\x{096f}][\x{0971}-\x{0983}][\x{0985}-\x{098c}][\x{098f}-\x{0990}][\x{0993}-\x{09a8}][\x{09aa}-\x{09b0}][\x{09b2}][\x{09b6}-\x{09b9}][\x{09bc}-\x{09c4}][\x{09c7}-\x{09c8}][\x{09cb}-\x{09ce}][\x{09d7}][\x{09dc}-\x{09dd}][\x{09df}-\x{09e3}][\x{09e6}-\x{09f3}][\x{09fb}][\x{0a01}-\x{0a03}][\x{0a05}-\x{0a0a}][\x{0a0f}-\x{0a10}][\x{0a13}-\x{0a28}][\x{0a2a}-\x{0a30}][\x{0a32}-\x{0a33}][\x{0a35}-\x{0a36}][\x{0a38}-\x{0a39}][\x{0a3c}][\x{0a3e}-\x{0a42}][\x{0a47}-\x{0a48}][\x{0a4b}-\x{0a4d}][\x{0a51}][\x{0a59}-\x{0a5c}][\x{0a5e}][\x{0a66}-\x{0a75}][\x{0a81}-\x{0a83}][\x{0a85}-\x{0a8d}][\x{0a8f}-\x{0a91}][\x{0a93}-\x{0aa8}][\x{0aaa}-\x{0ab0}][\x{0ab2}-\x{0ab3}][\x{0ab5}-\x{0ab9}][\x{0abc}-\x{0ac5}][\x{0ac7}-\x{0ac9}][\x{0acb}-\x{0acd}][\x{0ad0}][\x{0ae0}-\x{0ae3}][\x{0ae6}-\x{0aef}][\x{0af1}][\x{0af9}][\x{0b01}-\x{0b03}][\x{0b05}-\x{0b0c}][\x{0b0f}-\x{0b10}][\x{0b13}-\x{0b28}][\x{0b2a}-\x{0b30}][\x{0b32}-\x{0b33}][\x{0b35}-\x{0b39}][\x{0b3c}-\x{0b44}][\x{0b47}-\x{0b48}][\x{0b4b}-\x{0b4d}][\x{0b56}-\x{0b57}][\x{0b5c}-\x{0b5d}][\x{0b5f}-\x{0b63}][\x{0b66}-\x{0b6f}][\x{0b71}][\x{0b82}-\x{0b83}][\x{0b85}-\x{0b8a}][\x{0b8e}-\x{0b90}][\x{0b92}-\x{0b95}][\x{0b99}-\x{0b9a}][\x{0b9c}][\x{0b9e}-\x{0b9f}][\x{0ba3}-\x{0ba4}][\x{0ba8}-\x{0baa}][\x{0bae}-\x{0bb9}][\x{0bbe}-\x{0bc2}][\x{0bc6}-\x{0bc8}][\x{0bca}-\x{0bcd}][\x{0bd0}][\x{0bd7}][\x{0be6}-\x{0bef}][\x{0bf9}][\x{0c00}-\x{0c03}][\x{0c05}-\x{0c0c}][\x{0c0e}-\x{0c10}][\x{0c12}-\x{0c28}][\x{0c2a}-\x{0c39}][\x{0c3d}-\x{0c44}][\x{0c46}-\x{0c48}][\x{0c4a}-\x{0c4d}][\x{0c55}-\x{0c56}][\x{0c58}-\x{0c5a}][\x{0c60}-\x{0c63}][\x{0c66}-\x{0c6f}][\x{0c81}-\x{0c83}][\x{0c85}-\x{0c8c}][\x{0c8e}-\x{0c90}][\x{0c92}-\x{0ca8}][\x{0caa}-\x{0cb3}][\x{0cb5}-\x{0cb9}][\x{0cbc}-\x{0cc4}][\x{0cc6}-\x{0cc8}][\x{0cca}-\x{0ccd}][\x{0cd5}-\x{0cd6}][\x{0cde}][\x{0ce0}-\x{0ce3}][\x{0ce6}-\x{0cef}][\x{0cf1}-\x{0cf2}][\x{0d01}-\x{0d03}][\x{0d05}-\x{0d0c}][\x{0d0e}-\x{0d10}][\x{0d12}-\x{0d3a}][\x{0d3d}-\x{0d44}][\x{0d46}-\x{0d48}][\x{0d4a}-\x{0d4e}][\x{0d57}][\x{0d5f}-\x{0d63}][\x{0d66}-\x{0d6f}][\x{0d7a}-\x{0d7f}][\x{0d82}-\x{0d83}][\x{0d85}-\x{0d96}][\x{0d9a}-\x{0db1}][\x{0db3}-\x{0dbb}][\x{0dbd}][\x{0dc0}-\x{0dc6}][\x{0dca}][\x{0dcf}-\x{0dd4}][\x{0dd6}][\x{0dd8}-\x{0ddf}][\x{0de6}-\x{0def}][\x{0df2}-\x{0df3}][\x{0e01}-\x{0e3a}][\x{0e3f}-\x{0e4e}][\x{0e50}-\x{0e59}][\x{0e81}-\x{0e82}][\x{0e84}][\x{0e87}-\x{0e88}][\x{0e8a}][\x{0e8d}][\x{0e94}-\x{0e97}][\x{0e99}-\x{0e9f}][\x{0ea1}-\x{0ea3}][\x{0ea5}][\x{0ea7}][\x{0eaa}-\x{0eab}][\x{0ead}-\x{0eb9}][\x{0ebb}-\x{0ebd}][\x{0ec0}-\x{0ec4}][\x{0ec6}][\x{0ec8}-\x{0ecd}][\x{0ed0}-\x{0ed9}][\x{0edc}-\x{0edf}][\x{0f00}][\x{0f18}-\x{0f19}][\x{0f20}-\x{0f29}][\x{0f35}][\x{0f37}][\x{0f39}][\x{0f3e}-\x{0f47}][\x{0f49}-\x{0f6c}][\x{0f71}-\x{0f84}][\x{0f86}-\x{0f97}][\x{0f99}-\x{0fbc}][\x{0fc6}][\x{1000}-\x{1049}][\x{1050}-\x{109d}][\x{10a0}-\x{10c5}][\x{10c7}][\x{10cd}][\x{10d0}-\x{10fa}][\x{10fc}-\x{1248}][\x{124a}-\x{124d}][\x{1250}-\x{1256}][\x{1258}][\x{125a}-\x{125d}][\x{1260}-\x{1288}][\x{128a}-\x{128d}][\x{1290}-\x{12b0}][\x{12b2}-\x{12b5}][\x{12b8}-\x{12be}][\x{12c0}][\x{12c2}-\x{12c5}][\x{12c8}-\x{12d6}][\x{12d8}-\x{1310}][\x{1312}-\x{1315}][\x{1318}-\x{135a}][\x{135d}-\x{135f}][\x{1380}-\x{138f}][\x{13a0}-\x{13f5}][\x{13f8}-\x{13fd}][\x{1401}-\x{166c}][\x{166f}-\x{167f}][\x{1681}-\x{169a}][\x{16a0}-\x{16ea}][\x{16ee}-\x{16f8}][\x{1700}-\x{170c}][\x{170e}-\x{1714}][\x{1720}-\x{1734}][\x{1740}-\x{1753}][\x{1760}-\x{176c}][\x{176e}-\x{1770}][\x{1772}-\x{1773}][\x{1780}-\x{17d3}][\x{17d7}][\x{17db}-\x{17dd}][\x{17e0}-\x{17e9}][\x{180b}-\x{180e}][\x{1810}-\x{1819}][\x{1820}-\x{1877}][\x{1880}-\x{18aa}][\x{18b0}-\x{18f5}][\x{1900}-\x{191e}][\x{1920}-\x{192b}][\x{1930}-\x{193b}][\x{1946}-\x{196d}][\x{1970}-\x{1974}][\x{1980}-\x{19ab}][\x{19b0}-\x{19c9}][\x{19d0}-\x{19d9}][\x{1a00}-\x{1a1b}][\x{1a20}-\x{1a5e}][\x{1a60}-\x{1a7c}][\x{1a7f}-\x{1a89}][\x{1a90}-\x{1a99}][\x{1aa7}][\x{1ab0}-\x{1abd}][\x{1b00}-\x{1b4b}][\x{1b50}-\x{1b59}][\x{1b6b}-\x{1b73}][\x{1b80}-\x{1bf3}][\x{1c00}-\x{1c37}][\x{1c40}-\x{1c49}][\x{1c4d}-\x{1c7d}][\x{1cd0}-\x{1cd2}][\x{1cd4}-\x{1cf6}][\x{1cf8}-\x{1cf9}][\x{1d00}-\x{1df5}][\x{1dfc}-\x{1f15}][\x{1f18}-\x{1f1d}][\x{1f20}-\x{1f45}][\x{1f48}-\x{1f4d}][\x{1f50}-\x{1f57}][\x{1f59}][\x{1f5b}][\x{1f5d}][\x{1f5f}-\x{1f7d}][\x{1f80}-\x{1fb4}][\x{1fb6}-\x{1fbc}][\x{1fbe}][\x{1fc2}-\x{1fc4}][\x{1fc6}-\x{1fcc}][\x{1fd0}-\x{1fd3}][\x{1fd6}-\x{1fdb}][\x{1fe0}-\x{1fec}][\x{1ff2}-\x{1ff4}][\x{1ff6}-\x{1ffc}][\x{200b}-\x{200f}][\x{202a}-\x{202e}][\x{203f}-\x{2040}][\x{2054}][\x{2060}-\x{2064}][\x{2066}-\x{206f}][\x{2071}][\x{207f}][\x{2090}-\x{209c}][\x{20a0}-\x{20be}][\x{20d0}-\x{20dc}][\x{20e1}][\x{20e5}-\x{20f0}][\x{2102}][\x{2107}][\x{210a}-\x{2113}][\x{2115}][\x{2119}-\x{211d}][\x{2124}][\x{2126}][\x{2128}][\x{212a}-\x{212d}][\x{212f}-\x{2139}][\x{213c}-\x{213f}][\x{2145}-\u0000][\x{214e}][\x{2160}-\x{2188}][\x{2c00}-\x{2c2e}][\x{2c30}-\x{2c5e}][\x{2c60}-\x{2ce4}][\x{2ceb}-\x{2cf3}][\x{2d00}-\x{2d25}][\x{2d27}][\x{2d2d}][\x{2d30}-\x{2d67}][\x{2d6f}][\x{2d7f}-\x{2d96}][\x{2da0}-\x{2da6}][\x{2da8}-\x{2dae}][\x{2db0}-\x{2db6}][\x{2db8}-\x{2dbe}][\x{2dc0}-\x{2dc6}][\x{2dc8}-\x{2dce}][\x{2dd0}-\x{2dd6}][\x{2dd8}-\x{2dde}][\x{2de0}-\x{2dff}][\x{2e2f}][\x{3005}-\x{3007}][\x{3021}-\x{302f}][\x{3031}-\x{3035}][\x{3038}-\x{303c}][\x{3041}-\x{3096}][\x{3099}-\x{309a}][\x{309d}-\x{309f}][\x{30a1}-\x{30fa}][\x{30fc}-\x{30ff}][\x{3105}-\x{312d}][\x{3131}-\x{318e}][\x{31a0}-\x{31ba}][\x{31f0}-\x{31ff}][\x{3400}-\x{4db5}][\x{4e00}-\x{9fd5}][\x{a000}-\x{a48c}][\x{a4d0}-\x{a4fd}][\x{a500}-\x{a60c}][\x{a610}-\x{a62b}][\x{a640}-\x{a66f}][\x{a674}-\x{a67d}][\x{a67f}-\x{a6f1}][\x{a717}-\x{a71f}][\x{a722}-\x{a788}][\x{a78b}-\x{a7ad}][\x{a7b0}-\x{a7b7}][\x{a7f7}-\x{a827}][\x{a838}][\x{a840}-\x{a873}][\x{a880}-\x{a8c4}][\x{a8d0}-\x{a8d9}][\x{a8e0}-\x{a8f7}][\x{a8fb}][\x{a8fd}][\x{a900}-\x{a92d}][\x{a930}-\x{a953}][\x{a960}-\x{a97c}][\x{a980}-\x{a9c0}][\x{a9cf}-\x{a9d9}][\x{a9e0}-\x{a9fe}][\x{aa00}-\x{aa36}][\x{aa40}-\x{aa4d}][\x{aa50}-\x{aa59}][\x{aa60}-\x{aa76}][\x{aa7a}-\x{aac2}][\x{aadb}-\x{aadd}][\x{aae0}-\x{aaef}][\x{aaf2}-\x{aaf6}][\x{ab01}-\x{ab06}][\x{ab09}-\x{ab0e}][\x{ab11}-\x{ab16}][\x{ab20}-\x{ab26}][\x{ab28}-\x{ab2e}][\x{ab30}-\x{ab5a}][\x{ab5c}-\x{ab65}][\x{ab70}-\x{abea}][\x{abec}-\x{abed}][\x{abf0}-\x{abf9}][\x{ac00}-\x{d7a3}][\x{d7b0}-\x{d7c6}][\x{d7cb}-\x{d7fb}][\x{f900}-\x{fa6d}][\x{fa70}-\x{fad9}][\x{fb00}-\x{fb06}][\x{fb13}-\x{fb17}][\x{fb1d}-\x{fb28}][\x{fb2a}-\x{fb36}][\x{fb38}-\x{fb3c}][\x{fb3e}][\x{fb40}-\x{fb41}][\x{fb43}-\x{fb44}][\x{fb46}-\x{fbb1}][\x{fbd3}-\x{fd3d}][\x{fd50}-\x{fd8f}][\x{fd92}-\x{fdc7}][\x{fdf0}-\x{fdfc}][\x{fe00}-\x{fe0f}][\x{fe20}-\x{fe2f}][\x{fe33}-\x{fe34}][\x{fe4d}-\x{fe4f}][\x{fe69}][\x{fe70}-\x{fe74}][\x{fe76}-\x{fefc}][\x{feff}][\x{ff04}][\x{ff10}-\x{ff19}][\x{ff21}-\x{ff3a}][\x{ff3f}][\x{ff41}-\x{ff5a}][\x{ff66}-\x{ffbe}][\x{ffc2}-\x{ffc7}][\x{ffca}-\x{ffcf}][\x{ffd2}-\x{ffd7}][\x{ffda}-\x{ffdc}][\x{ffe0}-\x{ffe1}][\x{ffe5}-\x{ffe6}][\x{fff9}-\x{fffb}]])*
    private companion object {
        val UnicodeStringRegex = Regex("""\\u[0-9a-fA-F]{4}""")

    }
}


//val JccCharacterDescriptor.baseCharAsString: String
//    get() = firstChild.text.removeSurrounding("\"")
//
//val JccCharacterDescriptor.toCharAsString: String?
//

/**
 * Returns true if this regular expression occurs somewhere inside a RegexpSpec.
 * In that case it may reference private regexes.
 */
fun JccRegularExpression.isInRegexContext(): Boolean =
        strictParents().none { it is JccExpansion }

val JccCharacterDescriptor.baseCharElement: PsiElement
    get() = firstChild

val JccCharacterDescriptor.toCharElement: PsiElement?
    get() {
        val strings = node.getChildren(TokenSet.create(JavaccTypes.JCC_STRING_LITERAL))
        return if (strings.size < 2) null
        else strings[1].psi
    }


val JccCharacterDescriptor.baseCharAsString: String
    get() = baseCharElement.text.removeSurrounding("\"")

val JccCharacterDescriptor.toCharAsString: String?
    get() = toCharElement?.text?.removeSurrounding("\"")

val JccCharacterList.isNegated
    get() = firstChild.node.elementType == JavaccTypes.JCC_TILDE


/** Converts this node to the enum constant from [RegexKind]. */
val JccRegexprKind.modelConstant: RegexKind
    get() = when (text.trim()) {
        "TOKEN"         -> RegexKind.TOKEN
        "SPECIAL_TOKEN" -> RegexKind.SPECIAL_TOKEN
        "MORE"          -> RegexKind.MORE
        "SKIP"          -> RegexKind.SKIP
        else            -> throw IllegalArgumentException("Unknown regex kind ${text.trim()}")
    }

/** True if this [JccCharacterList] is of the form `~[]`, which matches any character. */
val JccCharacterList.isAnyMatch: Boolean
    get() = this.isNegated && this.characterDescriptorList.isEmpty()

// TODO remove?
fun JccRegexprSpec.getLiteralsExactMach() {
    // Gathers literals that match this expression recursively. Returns true whether a construct all
    // expansions of this regex match a literal, false if not.
    fun JccRegexpLike.gatherMatchingLiterals(result: MutableList<JccLiteralRegularExpression>): Boolean =
            when (this) {
                is JccLiteralRegularExpression -> {
                    result += this
                    true
                }
                is JccNamedRegularExpression   -> regexpElement?.gatherMatchingLiterals(result) == true
                is JccInlineRegularExpression  -> regexpElement?.gatherMatchingLiterals(result) == true
                is JccRegexpSequence           ->
                    regexpUnitList.size == 1 && regexpUnitList[0].gatherMatchingLiterals(result)
                is JccRegexpAlternative        -> regexpElementList.all {
                    it.gatherMatchingLiterals(result)
                }
                else                           -> false
            }

    val result = mutableListOf<JccLiteralRegularExpression>()

    this.regularExpression.gatherMatchingLiterals(result)
}
