package com.file.commentrate.commentrateplugin;

public class CommentRateResult {
    public final int totalLines;
    public final int effectiveCodeLines; // 有效代码行数（总行数-空行数）
    public final int commentLines;
    public final double commentRate;
    public final int methodComments;
    public final int classComments;
    
    public CommentRateResult(int totalLines, int effectiveCodeLines, int commentLines, 
                           double commentRate, int methodComments, int classComments) {
        this.totalLines = totalLines;
        this.effectiveCodeLines = effectiveCodeLines;
        this.commentLines = commentLines;
        this.commentRate = commentRate;
        this.methodComments = methodComments;
        this.classComments = classComments;
    }
    
    public String getGrade() {
        if (commentRate >= 40) return "优秀";
        if (commentRate >= 25) return "良好";
        if (commentRate >= 15) return "一般";
        return "偏低";
    }
    
    public String getGradeEmoji() {
        if (commentRate >= 40) return "✅";
        if (commentRate >= 25) return "✅";
        if (commentRate >= 15) return "⚠️";
        return "❌";
    }
}