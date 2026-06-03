package com.rustcrawlercoach.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rustcrawlercoach.data.dao.ChapterDao
import com.rustcrawlercoach.data.dao.ChapterProgressDao
import com.rustcrawlercoach.data.dao.CodeFileDao
import com.rustcrawlercoach.data.dao.FeynmanAnswerDao
import com.rustcrawlercoach.data.dao.PhaseDao
import com.rustcrawlercoach.data.dao.QuestionBankDao
import com.rustcrawlercoach.data.dao.UserProgressDao
import com.rustcrawlercoach.data.entity.Chapter
import com.rustcrawlercoach.data.entity.ChapterProgress
import com.rustcrawlercoach.data.entity.CodeFile
import com.rustcrawlercoach.data.entity.FeynmanAnswer
import com.rustcrawlercoach.data.entity.Phase
import com.rustcrawlercoach.data.entity.QuestionBank
import com.rustcrawlercoach.data.entity.UserProgress
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Phase::class,
        Chapter::class,
        QuestionBank::class,
        UserProgress::class,
        CodeFile::class,
        FeynmanAnswer::class,
        ChapterProgress::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun phaseDao(): PhaseDao
    abstract fun chapterDao(): ChapterDao
    abstract fun chapterProgressDao(): ChapterProgressDao
    abstract fun questionBankDao(): QuestionBankDao
    abstract fun userProgressDao(): UserProgressDao
    abstract fun codeFileDao(): CodeFileDao
    abstract fun feynmanAnswerDao(): FeynmanAnswerDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rust_crawler_coach_db"
                )
                    .addCallback(DatabaseCallback())
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    populateDatabase(database)
                }
            }
        }

        private suspend fun populateDatabase(database: AppDatabase) {
            // 初始化用户进度
            database.userProgressDao().insert(
                UserProgress(
                    userId = 1,
                    currentPhaseId = 1,
                    currentChapterId = 1,
                    completedChapterIds = "[]",
                    streakDays = 0,
                    totalXp = 0,
                    lastActiveDate = "",
                    streakFreeze = 0
                )
            )

            // 插入阶段数据
            val phases = listOf(
                Phase(1, "阶段一 Python 爬虫实战", 1, 4, "掌握 Python 基础和网络爬虫核心技术"),
                Phase(2, "阶段二 迷你C底层追问", 2, 2, "通过费曼学习法深入理解 C 语言底层原理"),
                Phase(3, "阶段三 JavaScript 逆向核心", 3, 12, "学习 JavaScript 逆向工程和爬虫对抗技术"),
                Phase(4, "阶段四 Go 语言基础", 4, 4, "掌握 Go 语言并发编程和网络开发"),
                Phase(5, "阶段五 Rust 系统编程", 5, 6, "学习 Rust 所有权、生命周期和性能优化"),
                Phase(6, "阶段六 爬虫框架开发", 6, 4, "使用 Rust 开发高性能爬虫框架"),
                Phase(7, "阶段七 分布式爬虫", 7, 4, "构建分布式爬虫系统和大数据处理")
            )
            database.phaseDao().insertAll(phases)

            // 插入阶段一的章节
            val phase1Chapters = listOf(
                Chapter(1, 1, "A 变量、类型、输入输出、条件判断", 1, "Python 基本语法：变量定义、数据类型（int, float, str, bool）、input()输入、print()输出、if/elif/else条件判断", false, "choice"),
                Chapter(2, 1, "B while 循环", 2, "while 循环语法、break/continue 语句、循环嵌套、无限循环处理", false, "choice"),
                Chapter(3, 1, "C 列表增删改查", 3, "列表定义、append/insert/remove/del 操作、索引访问、切片操作、列表推导式", false, "choice"),
                Chapter(4, 1, "D for 循环", 4, "for 循环语法、range()函数、enumerate()、zip()、for-else 结构", false, "choice"),
                Chapter(5, 1, "E 异常处理 try-except", 5, "异常概念、try-except-finally、常见异常类型、自定义异常、异常捕获顺序", false, "choice"),
                Chapter(6, 1, "F 字典", 6, "字典创建、访问、添加、删除、get()方法、keys()/values()/items()、字典推导式", false, "choice"),
                Chapter(7, 1, "G 函数定义、参数、返回值", 7, "函数定义、参数类型（位置参数、默认参数、关键字参数、*args、**kwargs）、返回值、lambda 表达式", false, "choice"),
                Chapter(8, 1, "H 文件读写（txt/json）", 8, "open()函数、文件模式、read()/readline()/readlines()、write()、JSON序列化(json.dump/load)、with语句", false, "choice"),
                Chapter(9, 1, "I 面向对象入门", 9, "类与对象、__init__方法、实例属性、类属性、self 参数、方法类型、继承基础", false, "choice"),
                Chapter(10, 1, "J 模块、包、虚拟环境、pip", 10, "import 语句、模块搜索路径、pip 安装包、virtualenv/venv 虚拟环境、requirements.txt、pip镜像配置", false, "choice"),
                Chapter(11, 1, "K 爬虫基础（requests/BS4/分页/存储）", 11, "requests 库使用、HTTP方法、请求头、BS4 解析HTML、BeautifulSoup选择器、分页爬取、数据存储（CSV、JSON、MySQL）", true, "code"),
                Chapter(12, 1, "L 爬虫进阶（多线程/协程/反爬）", 12, "并发编程：threading、asyncio、aiohttp、反爬应对：IP代理、User-Agent轮换、验证码处理、登录态维护", true, "code")
            )
            database.chapterDao().insertAll(phase1Chapters)

            // 插入阶段二的章节（费曼测验）
            val phase2Chapters = listOf(
                Chapter(13, 2, "C 语言指针基础", 1, "指针概念、指针变量、&和*运算符、指针与数组、指针运算", false, "feynman"),
                Chapter(14, 2, "内存管理 malloc/free", 2, "动态内存分配、堆与栈、内存泄漏、double free、内存对齐", false, "feynman"),
                Chapter(15, 2, "结构体与联合体", 3, "结构体定义、内存布局、结构体指针、位域、联合体节省空间", false, "feynman"),
                Chapter(16, 2, "文件操作与系统调用", 4, "文件描述符、open/read/write/close、系统调用与库函数区别、缓冲区", false, "feynman")
            )
            database.chapterDao().insertAll(phase2Chapters)

            // 插入阶段三的章节（JavaScript 逆向）
            val phase3Chapters = (17..28).map { index ->
                val week = index - 16
                Chapter(
                    id = index,
                    phaseId = 3,
                    title = "第 ${week} 周 JavaScript 核心",
                    orderIndex = week,
                    knowledgePoints = "JavaScript 逆向第 ${week} 周知识点",
                    requiredProject = week % 3 == 0,
                    examType = if (week % 2 == 0) "code" else "choice"
                )
            }
            database.chapterDao().insertAll(phase3Chapters)

            // 插入阶段四到七的占位章节
            val phase4Chapters = (29..36).map { index ->
                val chapter = index - 28
                Chapter(index, 4, "Go 语言 Chapter ${chapter}", chapter, "Go 语言知识点 ${chapter}", false, "choice")
            }
            database.chapterDao().insertAll(phase4Chapters)

            val phase5Chapters = (37..48).map { index ->
                val chapter = index - 36
                Chapter(index, 5, "Rust 语言 Chapter ${chapter}", chapter, "Rust 语言知识点 ${chapter}", false, "choice")
            }
            database.chapterDao().insertAll(phase5Chapters)

            val phase6Chapters = (49..56).map { index ->
                val chapter = index - 48
                Chapter(index, 6, "爬虫框架 Chapter ${chapter}", chapter, "爬虫框架知识点 ${chapter}", false, "code")
            }
            database.chapterDao().insertAll(phase6Chapters)

            val phase7Chapters = (57..64).map { index ->
                val chapter = index - 56
                Chapter(index, 7, "分布式爬虫 Chapter ${chapter}", chapter, "分布式爬虫知识点 ${chapter}", false, "code")
            }
            database.chapterDao().insertAll(phase7Chapters)

            // 插入阶段一的选择题题库
            val questions = mutableListOf<QuestionBank>()
            var questionId = 1

            // 章节1: 变量、类型、输入输出、条件判断
            questions.addAll(listOf(
                QuestionBank(questionId++, 1, "Python 中以下哪个是合法的变量名？", "choice", """["A: 2name", "B: name-2", "C: name_2", "D: name.2"]""", "C", 10),
                QuestionBank(questionId++, 1, "type(3.14) 的返回值是什么？", "choice", """["A: <class 'int'>", "B: <class 'float'>", "C: <class 'str'>", "D: <class 'double'>"]""", "B", 10),
                QuestionBank(questionId++, 1, "以下哪个函数可以获取用户输入？", "choice", """["A: print()", "B: input()", "C: read()", "D: scan()"]""", "B", 10),
                QuestionBank(questionId++, 1, "Python 中 elif 的作用是什么？", "choice", """["A: 结束程序", "B: 条件分支", "C: 循环控制", "D: 异常处理"]""", "B", 10),
                QuestionBank(questionId++, 1, "请写出计算两个数之和的表达式（使用变量 a 和 b）", "fill", null, "a + b", 15)
            ))

            // 章节2: while 循环
            questions.addAll(listOf(
                QuestionBank(questionId++, 2, "while 循环中，break 语句的作用是？", "choice", """["A: 跳过本次迭代", "B: 退出整个循环", "C: 继续执行", "D: 重新开始"]""", "B", 10),
                QuestionBank(questionId++, 2, "while 循环中，continue 语句的作用是？", "choice", """["A: 退出整个循环", "B: 跳过本次迭代", "C: 暂停执行", "D: 报错"]""", "B", 10),
                QuestionBank(questionId++, 2, "死循环的特征是什么？", "choice", """["A: 循环次数为0", "B: 条件永远为True", "C: 条件永远为False", "D: 循环体为空"]""", "B", 10),
                QuestionBank(questionId++, 2, "如何在 while 循环中累加 1 到 10？", "choice", """["A: i = i + 1", "B: i++", "C: ++i", "D: i =+ 1"]""", "A", 10),
                QuestionBank(questionId++, 2, "请写出计算 1+2+3+...+100 的 while 循环核心逻辑（变量 i=1, sum=0）", "fill", null, "while i <= 100:\n    sum += i\n    i += 1", 15)
            ))

            // 章节3: 列表
            questions.addAll(listOf(
                QuestionBank(questionId++, 3, "如何向列表末尾添加元素？", "choice", """["A: list.add()", "B: list.append()", "C: list.insert()", "D: list.push()"]""", "B", 10),
                QuestionBank(questionId++, 3, "list.pop() 默认删除哪个位置的元素？", "choice", """["A: 开头", "B: 末尾", "C: 随机", "D: 不删除"]""", "B", 10),
                QuestionBank(questionId++, 3, "列表切片 list[1:4] 获取哪些元素？", "choice", """["A: 索引1,2,3", "B: 索引1,2,3,4", "C: 索引2,3,4", "D: 索引1,2"]""", "A", 10),
                QuestionBank(questionId++, 3, "列表推导式 [x*2 for x in range(5)] 的结果是？", "choice", """["A: [0,2,4,6,8]", "B: [2,4,6,8,10]", "C: [1,2,4,8,16]", "D: [0,1,4,9,16]"]""", "A", 10),
                QuestionBank(questionId++, 3, "如何删除列表中值为 3 的第一个匹配项？", "choice", """["A: list.remove(3)", "B: list.delete(3)", "C: list.pop(3)", "D: del list[3]"]""", "A", 10)
            ))

            // 章节4: for 循环
            questions.addAll(listOf(
                QuestionBank(questionId++, 4, "range(5) 生成的范围是？", "choice", """["A: 0,1,2,3,4,5", "B: 1,2,3,4,5", "C: 0,1,2,3,4", "D: 0,1,2,3,4,5,6"]""", "C", 10),
                QuestionBank(questionId++, 4, "enumerate() 函数返回什么？", "choice", """["A: 只有索引", "B: 只有值", "C: 索引和值的元组", "D: 键值对"]""", "C", 10),
                QuestionBank(questionId++, 4, "zip() 函数的作用是？", "choice", """["A: 压缩文件", "B: 合并多个可迭代对象", "C: 解压文件", "D: 排序"]""", "B", 10),
                QuestionBank(questionId++, 4, "for-else 结构中，else 何时执行？", "choice", """["A: 循环被break时", "B: 循环正常结束", "C: 循环被continue时", "D: 永不执行"]""", "B", 10),
                QuestionBank(questionId++, 4, "请写出遍历字典 d 的所有键值对的代码", "fill", null, "for k, v in d.items():\n    print(k, v)", 15)
            ))

            // 章节5: 异常处理
            questions.addAll(listOf(
                QuestionBank(questionId++, 5, "try-except-finally 中，finally 块的特点是？", "choice", """["A: 仅在有异常时执行", "B: 仅在无异常时执行", "C: 无论是否有异常都执行", "D: 永不执行"]""", "C", 10),
                QuestionBank(questionId++, 5, "捕获所有异常的写法是？", "choice", """["A: except Exception:", "B: except:", "C: except All:", "D: except Error:"]""", "B", 10),
                QuestionBank(questionId++, 5, "IndexError 表示什么异常？", "choice", """["A: 类型错误", "B: 索引错误", "C: 值错误", "D: 名称错误"]""", "B", 10),
                QuestionBank(questionId++, 5, "如何获取异常对象？", "choice", """["A: except Exception now:", "B: except Exception as e:", "C: except Exception e:", "D: except Exception @e:"]""", "B", 10),
                QuestionBank(questionId++, 5, "请写出捕获并打印异常的代码", "fill", null, "try:\n    pass\nexcept Exception as e:\n    print(e)", 15)
            ))

            // 章节6: 字典
            questions.addAll(listOf(
                QuestionBank(questionId++, 6, "如何访问字典中不存在的键而不报错？", "choice", """["A: dict.key", "B: dict[key]", "C: dict.get(key)", "D: dict.exists(key)"]""", "C", 10),
                QuestionBank(questionId++, 6, "dict.keys() 返回的是什么类型？", "choice", """["A: list", "B: tuple", "C: dict_keys", "D: set"]""", "C", 10),
                QuestionBank(questionId++, 6, "如何获取字典的所有值？", "choice", """["A: dict.keys()", "B: dict.values()", "C: dict.items()", "D: dict.get()"]""", "B", 10),
                QuestionBank(questionId++, 6, "字典推导式 {k:v for k,v in items} 的作用是？", "choice", """["A: 过滤字典", "B: 从键值对创建字典", "C: 合并字典", "D: 删除字典"]""", "B", 10),
                QuestionBank(questionId++, 6, "请写出安全获取字典值的方法，如果不存在返回默认值 'N/A'", "fill", null, "dict.get(key, 'N/A')", 15)
            ))

            // 章节7: 函数
            questions.addAll(listOf(
                QuestionBank(questionId++, 7, "*args 的作用是？", "choice", """["A: 接收关键字参数", "B: 接收任意数量位置参数", "C: 接收字典", "D: 默认参数"]""", "B", 10),
                QuestionBank(questionId++, 7, "**kwargs 的作用是？", "choice", """["A: 接收位置参数", "B: 接收关键字参数", "C: 默认参数", "D: 可变参数"]""", "B", 10),
                QuestionBank(questionId++, 7, "lambda 表达式的作用是？", "choice", """["A: 定义类", "B: 创建匿名函数", "C: 列表推导", "D: 字典操作"]""", "B", 10),
                QuestionBank(questionId++, 7, "函数的默认参数应该放在什么位置？", "choice", """["A: 任意位置", "B: 必须放在最前面", "C: 必须放在最后面", "D: 中间位置"]""", "C", 10),
                QuestionBank(questionId++, 7, "请写出一个接收两个参数并返回它们和的函数", "fill", null, "def add(a, b):\n    return a + b", 15)
            ))

            // 章节8: 文件读写
            questions.addAll(listOf(
                QuestionBank(questionId++, 8, "with open() as f 的作用是？", "choice", """["A: 加快读写速度", "B: 自动关闭文件", "C: 加密文件", "D: 压缩文件"]""", "B", 10),
                QuestionBank(questionId++, 8, "'r' 模式表示什么？", "choice", """["A: 写入", "B: 读取", "C: 追加", "D: 二进制"]""", "B", 10),
                QuestionBank(questionId++, 8, "json.dump() 和 json.dumps() 的区别是？", "choice", """["A: 无区别", "B: dump写入文件，dumps返回字符串", "C: dumps写入文件", "D: dump返回字符串"]""", "B", 10),
                QuestionBank(questionId++, 8, "readlines() 返回什么类型？", "choice", """["A: str", "B: list", "C: dict", "D: set"]""", "B", 10),
                QuestionBank(questionId++, 8, "请写出读取 JSON 文件的代码（文件对象为 f）", "fill", null, "import json\ndata = json.load(f)", 15)
            ))

            // 章节9: 面向对象
            questions.addAll(listOf(
                QuestionBank(questionId++, 9, "__init__ 方法的作用是？", "choice", """["A: 析构函数", "B: 构造函数/初始化", "C: 普通方法", "D: 静态方法"]""", "B", 10),
                QuestionBank(questionId++, 9, "self 参数代表什么？", "choice", """["A: 类本身", "B: 实例对象", "C: 父类", "D: 模块"]""", "B", 10),
                QuestionBank(questionId++, 9, "类属性和实例属性的区别是？", "choice", """["A: 无区别", "B: 类属性所有实例共享，实例属性各自独有", "C: 实例属性共享，类属性独有", "D: 无法区分"]""", "B", 10),
                QuestionBank(questionId++, 9, "继承的语法是 class Child(Parent):，这里 Parent 叫什么？", "choice", """["A: 子类", "B: 父类/基类", "C: 派生类", "D: 接口"]""", "B", 10),
                QuestionBank(questionId++, 9, "请写出定义一个名为 Person 的类，包含 name 属性", "fill", null, "class Person:\n    def __init__(self, name):\n        self.name = name", 15)
            ))

            // 章节10: 模块、包、pip
            questions.addAll(listOf(
                QuestionBank(questionId++, 10, "import os 中的 os 叫什么？", "choice", """["A: 函数", "B: 类", "C: 模块", "D: 包"]""", "C", 10),
                QuestionBank(questionId++, 10, "pip install requests 中的 requests 是什么？", "choice", """["A: 内置模块", "B: 第三方库", "C: 自定义模块", "D: 系统包"]""", "B", 10),
                QuestionBank(questionId++, 10, "virtualenv 的作用是？", "choice", """["A: 版本控制", "B: 创建隔离的Python环境", "C: 调试代码", "D: 打包项目"]""", "B", 10),
                QuestionBank(questionId++, 10, "requirements.txt 的作用是？", "choice", """["A: 项目配置", "B: 记录依赖包版本", "C: 源代码", "D: 测试用例"]""", "B", 10),
                QuestionBank(questionId++, 10, "from os import path 是什么导入方式？", "choice", """["A: 模块导入", "B: 部分导入", "C: 别名导入", "D: 相对导入"]""", "B", 10)
            ))

            // 章节11: 爬虫基础
            questions.addAll(listOf(
                QuestionBank(questionId++, 11, "requests.get() 返回的是什么对象？", "choice", """["A: String", "B: Response", "C: JSON", "D: HTML"]""", "B", 10),
                QuestionBank(questionId++, 11, "BeautifulSoup 常用的解析器是？", "choice", """["A: json", "B: html.parser", "C: xml", "D: csv"]""", "B", 10),
                QuestionBank(questionId++, 11, "select() 和 find_all() 哪个是 BS4 的方法？", "choice", """["A: 都不是", "B: select()", "C: find_all()", "D: 两者都是"]""", "D", 10),
                QuestionBank(questionId++, 11, "response.text 和 response.content 的区别是？", "choice", """["A: 无区别", "B: text是字符串，content是字节", "C: content是字符串", "D: text是字节"]""", "B", 10),
                QuestionBank(questionId++, 11, "请写出使用 requests 获取网页的基本代码", "code", null, "import requests\nurl = 'https://example.com'\nresponse = requests.get(url)\nprint(response.text)", 20)
            ))

            // 章节12: 爬虫进阶
            questions.addAll(listOf(
                QuestionBank(questionId++, 12, "asyncio 使用什么关键字定义异步函数？", "choice", """["A: async", "B: await", "C: await async", "D: sync"]""", "A", 10),
                QuestionBank(questionId++, 12, "aiohttp 和 requests 的主要区别是？", "choice", """["A: 无区别", "B: aiohttp是异步的", "C: requests是异步的", "D: aiohttp不能发送请求"]""", "B", 10),
                QuestionBank(questionId++, 12, "反爬策略中，UA 轮换是为了什么？", "choice", """["A: 加快速度", "B: 模拟不同用户", "C: 省钱", "D: 加密数据"]""", "B", 10),
                QuestionBank(questionId++, 12, "threading 模块实现的是什么并发？", "choice", """["A: 进程并发", "B: 线程并发", "C: 协程并发", "D: 异步并发"]""", "B", 10),
                QuestionBank(questionId++, 12, "请写出使用 aiohttp 发送异步请求的基本代码", "code", null, "import aiohttp\nimport asyncio\n\nasync def fetch(session, url):\n    async with session.get(url) as response:\n        return await response.text()", 20)
            ))

            database.questionBankDao().insertAll(questions)

            // 为所有章节初始化进度记录
            val allChapters = database.chapterDao().getAllChaptersSync()
            val chapterProgressList = allChapters.map { chapter ->
                ChapterProgress(
                    chapterId = chapter.id,
                    isQuizCompleted = false,
                    quizScore = 0,
                    isProjectCompleted = false,
                    isCompleted = false,
                    completedAt = null
                )
            }
            database.chapterProgressDao().insertAll(chapterProgressList)
        }
    }
}
