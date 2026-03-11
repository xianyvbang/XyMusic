# Codex全局工作指南

## 回答风格:
- 回答必须使用中文
- 对总结、Plan、Task、以及长内容的输出，优先进行逻辑整理后使用美观的Table格式整齐输出;普通内容正常输出

## 工具使用:
1. 文件与代码检索:使用serena mcp来进行文件与代码的检索
2. 文件相关操作:对文件的创建、读取、编辑、删除等操作
    - 优先使用apply_patch工具进行
    - 读文件，apply_patch工具报错或出现问题的情况下使用desktop-commander mcp
    - 任何情况下，禁止使用cmd、powershell或者python来进行文件相关操作