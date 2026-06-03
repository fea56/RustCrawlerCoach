package com.rustcrawlercoach.util

/**
 * 代码语法高亮辅助类
 */
object CodeHighlighter {

    /**
     * 根据文件扩展名获取语言标识
     */
    fun getLanguage(fileName: String): String {
        return when {
            fileName.endsWith(".py") -> "python"
            fileName.endsWith(".js") -> "javascript"
            fileName.endsWith(".ts") -> "typescript"
            fileName.endsWith(".go") -> "go"
            fileName.endsWith(".rs") -> "rust"
            fileName.endsWith(".c") -> "c"
            fileName.endsWith(".cpp") || fileName.endsWith(".cc") -> "cpp"
            fileName.endsWith(".java") -> "java"
            fileName.endsWith(".kt") -> "kotlin"
            fileName.endsWith(".sh") -> "bash"
            fileName.endsWith(".json") -> "json"
            fileName.endsWith(".xml") -> "xml"
            fileName.endsWith(".html") || fileName.endsWith(".htm") -> "html"
            fileName.endsWith(".css") -> "css"
            fileName.endsWith(".sql") -> "sql"
            fileName.endsWith(".md") -> "markdown"
            else -> "plaintext"
        }
    }

    /**
     * 获取文件图标类型
     */
    fun getFileIconType(fileName: String): FileType {
        return when {
            fileName.endsWith(".py") -> FileType.PYTHON
            fileName.endsWith(".js") || fileName.endsWith(".ts") -> FileType.JAVASCRIPT
            fileName.endsWith(".go") -> FileType.GO
            fileName.endsWith(".rs") -> FileType.RUST
            fileName.endsWith(".c") || fileName.endsWith(".cpp") -> FileType.C
            fileName.endsWith(".java") -> FileType.JAVA
            fileName.endsWith(".kt") -> FileType.KOTLIN
            fileName.endsWith(".json") -> FileType.JSON
            fileName.endsWith(".xml") -> FileType.XML
            fileName.endsWith(".html") || fileName.endsWith(".htm") -> FileType.HTML
            fileName.endsWith(".css") -> FileType.CSS
            fileName.endsWith(".sql") -> FileType.SQL
            fileName.endsWith(".md") -> FileType.MARKDOWN
            else -> FileType.OTHER
        }
    }

    enum class FileType {
        PYTHON, JAVASCRIPT, GO, RUST, C, JAVA, KOTLIN,
        JSON, XML, HTML, CSS, SQL, MARKDOWN, OTHER
    }

    /**
     * 默认代码模板
     */
    fun getDefaultTemplate(fileName: String): String {
        return when {
            fileName.endsWith(".py") -> """# -*- coding: utf-8 -*-
# Python 代码

def main():
    print("Hello, World!")

if __name__ == "__main__":
    main()
"""
            fileName.endsWith(".js") -> """// JavaScript 代码

function main() {
    console.log("Hello, World!");
}

main();
"""
            fileName.endsWith(".go") -> """package main

import "fmt"

func main() {
    fmt.Println("Hello, World!")
}
"""
            fileName.endsWith(".rs") -> """fn main() {
    println!("Hello, World!");
}
"""
            fileName.endsWith(".c") -> """#include <stdio.h>

int main() {
    printf("Hello, World!\\n");
    return 0;
}
"""
            fileName.endsWith(".cpp") -> """#include <iostream>

int main() {
    std::cout << "Hello, World!" << std::endl;
    return 0;
}
"""
            fileName.endsWith(".java") -> """public class Main {
    public static void main(String[] args) {
        System.out.println("Hello, World!");
    }
}
"""
            fileName.endsWith(".kt") -> """fun main() {
    println("Hello, World!")
}
"""
            fileName.endsWith(".json") -> """{
    "name": "project",
    "version": "1.0.0"
}
"""
            else -> "// 代码文件\n"
        }
    }
}
