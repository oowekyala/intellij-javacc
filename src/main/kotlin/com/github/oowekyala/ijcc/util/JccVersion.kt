package com.github.oowekyala.ijcc.util

/**
 * Version of Javacc, based on the versions published to maven.
 *
 * @author Clément Fournier
 * @since 1.0
 */
enum class JccVersion {
    V_2_1,   //
    V_3_2,   //	Mar, 2006
    V_4_0,   //	Mar, 2006
    V_4_1,   //	Oct, 2008
    V_4_2,   //	Feb, 2009
    V_5_0,   //	Sep, 2009
    V_6_1_0, // Apr, 2014
    V_6_1_1, // May, 2014
    V_6_1_2, //	May, 2014
    V_7_0_0, //	Dec, 2016
    V_7_0_1, // Jan, 2017
    V_7_0_2, //	Jan, 2017
    V_7_0_3, //	Nov, 2017
    V_7_0_4; // Sep, 2018

    companion object {
        val Latest = values().last()
    }
}