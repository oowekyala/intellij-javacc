package com.github.oowekyala.ijcc.lang.model

import java.util.*

enum class RegexKind {
    TOKEN, SKIP, SPECIAL_TOKEN, MORE;

    companion object {
        val All: Set<RegexKind> = EnumSet.allOf(RegexKind::class.java)
        val JustToken: Set<RegexKind> = EnumSet.of(TOKEN)
    }
}