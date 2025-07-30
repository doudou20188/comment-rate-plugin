package com.file.commentrate.commentrateplugin;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class CommentRateDialog extends DialogWrapper {
    private final CommentRateResult result;
    private final String fileName;

    public CommentRateDialog(Project project, CommentRateResult result, String fileName) {
        super(project);
        this.result = result;
        this.fileName = fileName;
        setTitle("注释率统计 - " + fileName);
        init();
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        
        // 标题
        JLabel titleLabel = new JLabel(fileName, JLabel.CENTER);
        titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 16));
        gbc.gridx = 0; gbc.gridy = 0; gbc.gridwidth = 2;
        gbc.insets = new Insets(10, 10, 20, 10);
        panel.add(titleLabel, gbc);
        
        // 统计信息
        gbc.gridwidth = 1;
        gbc.insets = new Insets(5, 10, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        
        addRow(panel, gbc, 1, "总行数:", String.valueOf(result.totalLines));
        addRow(panel, gbc, 2, "有效代码行数:", String.valueOf(result.effectiveCodeLines));
        addRow(panel, gbc, 3, "注释行数:", String.valueOf(result.commentLines));
        addRow(panel, gbc, 4, "方法注释:", String.valueOf(result.methodComments));
        addRow(panel, gbc, 5, "类注释:", String.valueOf(result.classComments));
        
        // 注释率（高亮显示）
        gbc.gridy = 6;
        gbc.gridx = 0;
        JLabel rateLabel = new JLabel("注释率:");
        rateLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        panel.add(rateLabel, gbc);
        
        gbc.gridx = 1;
        String rateText = String.format("%.2f%% %s %s", 
            result.commentRate, result.getGradeEmoji(), result.getGrade());
        JLabel rateValue = new JLabel(rateText);
        rateValue.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 14));
        rateValue.setForeground(getGradeColor());
        panel.add(rateValue, gbc);
        
        // 建议
        gbc.gridy = 7;
        gbc.gridx = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 10, 10, 10);
        JLabel suggestionLabel = new JLabel(getSuggestion(), JLabel.CENTER);
        suggestionLabel.setFont(new Font(Font.SANS_SERIF, Font.ITALIC, 12));
        panel.add(suggestionLabel, gbc);
        
        panel.setPreferredSize(new Dimension(350, 280));
        return panel;
    }
    
    private void addRow(JPanel panel, GridBagConstraints gbc, int row, String label, String value) {
        gbc.gridy = row;
        gbc.gridx = 0;
        panel.add(new JLabel(label), gbc);
        gbc.gridx = 1;
        panel.add(new JLabel(value), gbc);
    }
    
    private Color getGradeColor() {
        if (result.commentRate >= 25) return new Color(0, 128, 0); // Green
        if (result.commentRate >= 15) return new Color(255, 140, 0); // Orange
        return new Color(220, 20, 60); // Crimson
    }
    
    private String getSuggestion() {
        if (result.commentRate >= 40) {
            return "注释率很棒！继续保持良好的编码习惯。";
        } else if (result.commentRate >= 25) {
            return "注释率良好，可以考虑为复杂逻辑添加更多注释。";
        } else if (result.commentRate >= 15) {
            return "建议增加方法和类的注释，提高代码可读性。";
        } else {
            return "注释率偏低，强烈建议为代码添加必要的注释。";
        }
    }
}