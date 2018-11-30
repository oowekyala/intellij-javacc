//package com.github.oowekyala.ijcc.insight.jjtree
//
//import com.github.oowekyala.ijcc.lang.psi.JccFile
//import com.github.oowekyala.ijcc.lang.psi.JccJjtreeNodeDescriptor
//import com.github.oowekyala.ijcc.lang.psi.JccNonTerminalProduction
//import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
//import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
//import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
//import com.intellij.openapi.actionSystem.ActionManager
//import com.intellij.openapi.keymap.KeymapUtil
//import com.intellij.openapi.util.text.StringUtil
//import com.intellij.psi.NavigatablePsiElement
//import com.intellij.psi.PsiElement
//import com.intellij.psi.util.PsiTreeUtil
//import com.intellij.util.containers.ContainerUtil
//import gnu.trove.THashSet
//import org.intellij.grammar.BnfIcons
//import org.intellij.grammar.KnownAttribute
//import org.intellij.grammar.generator.ParserGeneratorUtil
//import org.intellij.grammar.generator.RuleGraphHelper
//import org.intellij.grammar.java.JavaHelper
//import org.intellij.grammar.psi.BnfRule
//import org.intellij.grammar.psi.impl.GrammarUtil
//import java.util.*
//
///**
// * @author Clément Fournier
// * @since 1.0
// */
//class JjtreeLineMarkerProvider : RelatedItemLineMarkerProvider() {
//
//    override fun collectNavigationMarkers(elements: List<PsiElement>,
//                                          result: MutableCollection<in RelatedItemLineMarkerInfo<*>>,
//                                          forNavigation: Boolean) {
//        val visited = if (forNavigation) THashSet<PsiElement>() else null
//        for (element in elements) {
//            val parent = element.parent
//            val isRuleId = parent is JccNonTerminalProduction && (forNavigation || element === parent.nameIdentifier)
//            if (!(isRuleId)) continue
//            val items = ArrayList<PsiElement>()
//
//            val method = getMethod(element)
//            if (method != null && (!forNavigation || visited!!.add(method))) {
//                items.add(method)
//            }
//            var hasPSI = false
//            if (isRuleId) {
//                val rule = RuleGraphHelper.getSynonymTargetOrSelf(parent as BnfRule)
//                if (RuleGraphHelper.hasPsiClass(rule)) {
//                    hasPSI = true
//                    val javaHelper = JavaHelper.getJavaHelper(rule)
//                    val names = ParserGeneratorUtil.getQualifiedRuleClassName(rule)
//                    for (className in arrayOf(names.first, names.second)) {
//                        val aClass = javaHelper.findClass(className)
//                        if (aClass != null && (!forNavigation || visited!!.add(aClass))) {
//                            items.add(aClass)
//                        }
//                    }
//                }
//            }
//            if (!items.isEmpty()) {
//                val action = ActionManager.getInstance().getAction("GotoRelated")
//                var tooltipAd = ""
//                var popupTitleAd = ""
//                if (action != null) {
//                    val shortcutText = KeymapUtil.getFirstKeyboardShortcutText(action)
//                    val actionText =
//                            if (StringUtil.isEmpty(shortcutText)) "'" + action.templatePresentation.text + "' action" else shortcutText
//                    tooltipAd = "\nGo to sub-expression code via $actionText"
//                    popupTitleAd = " (for sub-expressions use $actionText)"
//                }
//                val title = "parser " + (if (hasPSI) "and PSI " else "") + "code"
//                val builder = NavigationGutterIconBuilder.create(BnfIcons.RELATED_METHOD).setTargets(items)
//                    .setTooltipText("Click to navigate to $title$tooltipAd").setPopupTitle(
//                        StringUtil.capitalize(title) + popupTitleAd
//                    )
//                result.add(builder.createLineMarkerInfo(element))
//            }
//        }
//    }
//
//    private fun getNodePsiClass(element: PsiElement): NavigatablePsiElement? {
//        val nodeName = when (element) {
//            is JccNonTerminalProduction -> element.jjtreeNodeDescriptor?.name ?: element.name
//            is JccJjtreeNodeDescriptor  -> element.name
//            else                        -> return null
//        }
//
//        val file = element.containingFile as? JccFile ?: return null
//
//
//        if (StringUtil.isEmpty(parserClass)) return null
//        val helper = JavaHelper.getJavaHelper(element)
//        val methods = helper.findClassMethods(
//            parserClass, JavaHelper.MethodType.STATIC, GrammarUtil.getMethodName(rule, element), -1
//        )
//        return ContainerUtil.getFirstItem(methods)
//    }
//}