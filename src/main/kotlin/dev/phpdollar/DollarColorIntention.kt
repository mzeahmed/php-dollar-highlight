package dev.phpdollar

import com.intellij.codeInsight.intention.PsiElementBaseIntentionAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.ui.ColorChooserService
import com.jetbrains.php.lang.lexer.PhpTokenTypes
import com.jetbrains.php.lang.psi.elements.Variable

/** Alt+Enter on a `$`: pick the color of the dollar sign right from the editor. */
class DollarColorIntention : PsiElementBaseIntentionAction() {
    override fun getFamilyName() = "PHP Dollar Highlight"
    override fun getText() = "Change the color of the \$ sign…"
    override fun startInWriteAction() = false

    override fun isAvailable(project: Project, editor: Editor?, element: PsiElement): Boolean {
        if (editor == null || !DollarSettings.getInstance().enabled) return false
        val type = element.node?.elementType
        val isDollar = type == PhpTokenTypes.VARIABLE ||
            ((type == PhpTokenTypes.DOLLAR || type == PhpTokenTypes.DOLLAR_LBRACE) && element.parent is Variable)
        return isDollar && editor.caretModel.offset == element.textRange.startOffset
    }

    override fun invoke(project: Project, editor: Editor?, element: PsiElement) {
        editor ?: return
        val settings = DollarSettings.getInstance()
        val current = settings.customColor ?: DollarColors.DEFAULT_COLOR
        val chosen = ColorChooserService.getInstance().showDialog(project, editor.component, "Color of the \$ sign", current)
            ?: return
        settings.customColor = chosen
        DollarSettings.refreshEditors()
    }
}
