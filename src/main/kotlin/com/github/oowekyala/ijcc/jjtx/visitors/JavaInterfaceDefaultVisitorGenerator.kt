package com.github.oowekyala.ijcc.jjtx.visitors

import com.github.oowekyala.ijcc.jjtx.JjtxRunContext
import com.github.oowekyala.ijcc.jjtx.typeHierarchy.TypeHierarchyTree

interface TypeHierarchyRenderer {


    fun generate(typeHierarchyTree: TypeHierarchyTree,
                 jjtxRunContext: JjtxRunContext) {


        // Velocity template somewhere
        // User populates variables or uses defaults


    }
}

