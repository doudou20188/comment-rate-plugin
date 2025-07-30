package com.file.commentrate.commentrateplugin;

import com.intellij.psi.*;
import com.intellij.psi.javadoc.PsiDocComment;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

public class CommentRateCalculator {

    public CommentRateResult calculate(PsiFile psiFile) {
        String fileContent = psiFile.getText();
        String[] lines = fileContent.split("\n");

        int totalLines = lines.length;
        int commentLines = 0;
        int emptyLines = 0; // 空行数量
        final AtomicInteger methodComments = new AtomicInteger(0);
        final AtomicInteger classComments = new AtomicInteger(0);

        // 统计注释行和空行
        for (String line : lines) {
            String trimmedLine = line.trim();

            // 统计空行（包括只有注释符号的行）
            if (isEmptyLine(trimmedLine)) {
                emptyLines++;
                continue;
            }

            // 判断是否为有效注释行
            if (isValidCommentLine(trimmedLine)) {
                commentLines++;
            }
        }

        // 计算有效代码行数 = 总行数 - 空行数
        int effectiveCodeLines = totalLines - emptyLines;

        // 统计方法和类级注释
        psiFile.accept(new JavaRecursiveElementVisitor() {
            @Override
            public void visitDocComment(PsiDocComment comment) {
                super.visitDocComment(comment);
                PsiElement parent = comment.getParent();
                if (parent instanceof PsiMethod) {
                    methodComments.incrementAndGet();
                } else if (parent instanceof PsiClass) {
                    classComments.incrementAndGet();
                }
            }
        });

        double commentRate = effectiveCodeLines > 0 ?
            (double) commentLines * 100 / effectiveCodeLines : 0;

        return new CommentRateResult(
            totalLines,
            effectiveCodeLines,
            commentLines,
            commentRate,
            methodComments.get(),
            classComments.get()
        );
    }

    /**
     * 判断是否为有效注释行
     * 规则：
     * 1. // 开头且后面有内容的行算有效
     * 2. /* 开头且后面有内容的行算有效（单纯的/*不算）
     * 3.  开头/结尾且只有*的行不算有效
     * 4.  * 开头的行，只有当 * 后面有内容时才算有效注释
     */
    private boolean isValidCommentLine(String trimmedLine) {
        // 单行注释 //
        if (trimmedLine.startsWith("//")) {
            String afterSlash = trimmedLine.substring(2).trim();
            return !afterSlash.isEmpty();
        }

        // 检查是否只是 /* 没有其他内容
        if (trimmedLine.equals("/*")) {
            return false;
        }

        // 检查是否只是 */ 没有其他内容
        if (trimmedLine.equals("*/")) {
            return false;
        }

        // 多行注释开始 /* 且后面有内容
        if (trimmedLine.startsWith("/*")) {
            String afterStart = trimmedLine.substring(2).trim();
            return !afterStart.isEmpty();
        }

        // 多行注释结束 */ 且前面有内容
        if (trimmedLine.endsWith("*/")) {
            String beforeEnd = trimmedLine.substring(0, trimmedLine.length() - 2).trim();
            return !beforeEnd.isEmpty();
        }

        // * 开头的注释行，检查 * 后是否有内容
        if (trimmedLine.startsWith("*")) {
            // 去掉开头的 * 和空格，检查是否还有内容
            String afterStar = trimmedLine.substring(1).trim();
            return !afterStar.isEmpty();
        }

        return false;
    }

    /**
     * 判断是否为空行（包括只有注释符号没有内容的行）
     */
    private boolean isEmptyLine(String trimmedLine) {
        // 完全空行
        if (trimmedLine.isEmpty()) {
            return true;
        }

        // 只有 /* 的行
        if (trimmedLine.equals("/*")) {
            return true;
        }

        // 只有 */ 的行
        if (trimmedLine.equals("*/")) {
            return true;
        }

        // 只有 * 的行
        if (trimmedLine.equals("*")) {
            return true;
        }

        return false;
    }
}
