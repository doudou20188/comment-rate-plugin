package com.file.commentrate.commentrateplugin;

import com.file.commentrate.view.CommentRateDialog;
import com.file.commentrate.entity.CommentRateResult;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import org.jetbrains.annotations.NotNull;

public class CommentRateAction extends AnAction {

    // 插件入口点，处理注释率统计的动作
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (virtualFile == null || virtualFile.isDirectory()) return;

        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) return;

        CommentRateCalculator calculator = new CommentRateCalculator();
        // 选中文件，并解析文件的注释率
        CommentRateResult result = calculator.calculate(psiFile);
        // 结果渲染、展示
        CommentRateDialog dialog = new CommentRateDialog(project, result, virtualFile.getName());
        dialog.show();
    }


    /**
     * 可见区过滤
     * @param e
     */
    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        boolean visible = file != null &&
                         !file.isDirectory() &&
                         "java".equalsIgnoreCase(file.getExtension());
        e.getPresentation().setEnabledAndVisible(visible);
    }
}
