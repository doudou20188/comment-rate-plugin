# IntelliJ IDEA 插件开发套路详解

## 🎯 项目概述

本项目是一个 **IntelliJ IDEA 注释率计算插件**，通过右键菜单快速统计 Java 文件的注释率，帮助开发者评估代码注释质量。

## 📋 插件开发核心流程

### 🔄 标准开发套路：**注册 → 触发 → 处理 → 展示**

```
plugin.xml → AnAction类 → 业务逻辑类 → DialogWrapper类
```

## 🏗️ 技术架构分解

### 1️⃣ **动作注册阶段** - plugin.xml
```xml
<!--动作注册-->
<actions>
    <action id="CommentRateAction"
            class="com.file.commentrate.commentrateplugin.CommentRateAction"
            text="Calculate Comment Rate"
            description="Calculate comment rate for this file">
        
        <!--右键菜单-->
        <add-to-group group-id="ProjectViewPopupMenu" anchor="after" relative-to-action="$Cut"/>
        
        <!--编辑右键-->
        <add-to-group group-id="EditorPopupMenu" anchor="after" relative-to-action="$Cut"/>
    </action>
</actions>
```

**关键要素：**
- `id`: 动作唯一标识符
- `class`: 处理动作的Java类
- `text`: 菜单项显示文本
- `group-id`: 菜单显示位置
- `anchor`: 相对位置锚点

### 2️⃣ **事件触发阶段** - CommentRateAction.java
```java
public class CommentRateAction extends AnAction {
    
    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        // 1. 获取上下文环境
        Project project = e.getProject();
        VirtualFile virtualFile = e.getData(CommonDataKeys.VIRTUAL_FILE);
        
        // 2. 文件有效性检查
        if (project == null || virtualFile == null || virtualFile.isDirectory()) return;
        
        // 3. 转换为PSI文件（程序结构接口）
        PsiFile psiFile = PsiManager.getInstance(project).findFile(virtualFile);
        if (psiFile == null) return;
        
        // 4. 执行业务逻辑
        CommentRateCalculator calculator = new CommentRateCalculator();
        CommentRateResult result = calculator.calculate(psiFile);
        
        // 5. 渲染结果
        CommentRateDialog dialog = new CommentRateDialog(project, result, virtualFile.getName());
        dialog.show();
    }
    
    /**
     * 可见性过滤 - 控制菜单项何时显示
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
```

**关键概念：**
- `actionPerformed()`: 用户点击时的处理逻辑
- `update()`: 动态控制菜单项可见性
- `AnActionEvent`: 包含用户操作的上下文信息
- `CommonDataKeys`: 标准数据获取键值

### 3️⃣ **业务处理阶段** - CommentRateCalculator.java
```java
public class CommentRateCalculator {
    
    public CommentRateResult calculate(PsiFile psiFile) {
        // 1. 文件内容解析
        String fileContent = psiFile.getText();
        String[] lines = fileContent.split("\n");
        
        // 2. 统计分析
        int totalLines = lines.length;
        int commentLines = 0;
        int emptyLines = 0;
        
        // 3. 逐行分析注释类型
        for (String line : lines) {
            String trimmedLine = line.trim();
            if (isEmptyLine(trimmedLine)) {
                emptyLines++;
            } else if (isValidCommentLine(trimmedLine)) {
                commentLines++;
            }
        }
        
        // 4. 计算注释率
        int effectiveCodeLines = totalLines - emptyLines;
        double commentRate = effectiveCodeLines > 0 ? 
            (double) commentLines * 100 / effectiveCodeLines : 0;
        
        // 5. 构建结果对象
        return new CommentRateResult(totalLines, effectiveCodeLines, 
                                   commentLines, commentRate, methodComments, classComments);
    }
}
```

**核心技术：**
- `PsiFile`: 程序结构接口，提供代码解析能力
- 字符串解析：识别不同类型注释（//、/* */、/** */）
- 统计算法：过滤空行、计算注释覆盖率

### 4️⃣ **结果展示阶段** - CommentRateDialog.java
```java
public class CommentRateDialog extends DialogWrapper {
    
    public CommentRateDialog(Project project, CommentRateResult result, String fileName) {
        super(project);
        this.result = result;
        this.fileName = fileName;
        setTitle("注释率统计 - " + fileName);
        init(); // 初始化对话框
    }
    
    @Override
    protected JComponent createCenterPanel() {
        // 1. 创建主面板
        JPanel panel = new JPanel(new GridBagLayout());
        
        // 2. 添加统计信息
        addRow(panel, gbc, 1, "总行数:", String.valueOf(result.totalLines));
        addRow(panel, gbc, 2, "有效代码行数:", String.valueOf(result.effectiveCodeLines));
        addRow(panel, gbc, 3, "注释行数:", String.valueOf(result.commentLines));
        
        // 3. 高亮显示注释率
        String rateText = String.format("%.2f%% %s %s", 
            result.commentRate, result.getGradeEmoji(), result.getGrade());
        JLabel rateValue = new JLabel(rateText);
        rateValue.setForeground(getGradeColor());
        
        return panel;
    }
}
```

**UI设计原则：**
- `DialogWrapper`: IDEA标准对话框基类
- `GridBagLayout`: 灵活的网格布局管理器
- 颜色编码：不同等级使用不同颜色提示
- 用户体验：添加表情符号和建议文案

## 💡 核心API详解

### 🔑 必备API类
| API类 | 作用 | 关键方法 |
|-------|------|----------|
| `AnAction` | 动作基类 | `actionPerformed()`, `update()` |
| `AnActionEvent` | 事件上下文 | `getProject()`, `getData()` |
| `CommonDataKeys` | 数据获取 | `VIRTUAL_FILE`, `PROJECT` |
| `VirtualFile` | 文件抽象 | `getName()`, `getExtension()` |
| `PsiFile` | 程序结构 | `getText()`, `accept()` |
| `DialogWrapper` | 对话框基类 | `createCenterPanel()`, `show()` |

### 🎨 UI组件库
- **Swing组件**: `JPanel`, `JLabel`, `GridBagLayout`
- **IDEA主题**: 自动适配IDE的主题色彩
- **国际化支持**: 可扩展多语言显示

## 🚀 开发扩展指南

### 📝 添加新功能的步骤：
1. **扩展plugin.xml** - 注册新的动作或配置
2. **创建Action类** - 处理新的用户交互
3. **实现业务逻辑** - 独立的功能处理类
4. **设计UI界面** - 继承DialogWrapper或其他UI基类
5. **测试验证** - 在开发环境中测试插件功能

### 🔧 常用开发模式：
- **单一职责**: 每个类专注一个功能
- **事件驱动**: 基于用户操作触发处理
- **MVC分离**: 界面、逻辑、数据分离
- **上下文获取**: 从AnActionEvent获取所需信息

## 📚 学习资源

- [IntelliJ Platform SDK](https://plugins.jetbrains.com/docs/intellij/welcome.html)
- [Plugin DevKit](https://plugins.jetbrains.com/docs/intellij/plugin-development.html)
- [PSI Cookbook](https://plugins.jetbrains.com/docs/intellij/psi-cookbook.html)

---
**总结**: IDEA插件开发遵循 **"配置驱动、事件响应、组件化开发"** 的设计理念，通过标准化的API和框架，让开发者专注于业务逻辑实现。