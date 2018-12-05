package com.github.oowekyala.ijcc.lang.psi.impl;

import com.github.oowekyala.ijcc.insight.model.JavaccConfig;
import com.github.oowekyala.ijcc.lang.psi.JccJjtreeNodeDescriptor;
import com.github.oowekyala.ijcc.lang.psi.JccNodeClassOwner;
import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction;
import com.intellij.psi.NavigatablePsiElement;
import org.jetbrains.annotations.Nullable;

/**
 * FIXME not on the classpath
 * @author Clément Fournier
 * @since 1.0
 */
public class JccPsiImplUtil {

//    public static @Nullable
//    NavigatablePsiElement getNodeClass(JccNonTerminalProduction production, JavaccConfig javaccConfig) {
//        JccJjtreeNodeDescriptor nodeDescriptor = production.getJjtreeNodeDescriptor();
//        if (nodeDescriptor == null && javaccConfig.isDefaultVoid() || nodeDescriptor != null && nodeDescriptor.isVoid() == true) {
//            return null;
//        }
//
//        String nodePackage = javaccConfig.getNodePackage();
//        String nodeName = javaccConfig.getNodePrefix() + (nodeDescriptor != null ? nodeDescriptor.getName() : production.getName());
//
//
//        return JccNodeClassOwner.getJavaClassFromQname(production, "$nodePackage.$nodeName");
//    }

}
