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
                Phase(1, "阶段一 Python 爬虫实战", 1, 12, "掌握 Python 基础和网络爬虫核心技术"),
                Phase(2, "阶段二 迷你C底层追问", 2, 4, "通过费曼学习法深入理解 C 语言底层原理"),
                Phase(3, "阶段三 JavaScript 逆向核心", 3, 12, "学习 JavaScript 逆向工程和爬虫对抗技术"),
                Phase(4, "阶段四 Go 高并发爬虫", 4, 8, "Go 语言并发编程与分布式爬虫开发"),
                Phase(5, "阶段五 C语言 + CSAPP 核心 Lab", 5, 11, "深入理解计算机系统与 C 语言系统编程"),
                Phase(6, "阶段六 Rust 底层协议定制", 6, 9, "Rust 安全编程与底层协议定制"),
                Phase(7, "阶段七 汇编阅读 + 多语言协作", 7, 6, "汇编分析与多语言 FFI 协作开发")
            )
            database.phaseDao().insertAll(phases)

            // ============ 阶段一：Python 爬虫实战 ============
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

            // ============ 阶段二：迷你C底层追问（费曼测验） ============
            val phase2Chapters = listOf(
                Chapter(13, 2, "C 语言指针基础", 1, "指针概念、指针变量、&和*运算符、指针与数组、指针运算", false, "feynman"),
                Chapter(14, 2, "内存管理 malloc/free", 2, "动态内存分配、堆与栈、内存泄漏、double free、内存对齐", false, "feynman"),
                Chapter(15, 2, "结构体与联合体", 3, "结构体定义、内存布局、结构体指针、位域、联合体节省空间", false, "feynman"),
                Chapter(16, 2, "文件操作与系统调用", 4, "文件描述符、open/read/write/close、系统调用与库函数区别、缓冲区", false, "feynman")
            )
            database.chapterDao().insertAll(phase2Chapters)

            // ============ 阶段三：JavaScript 逆向核心 ============
            val phase3Chapters = listOf(
                Chapter(17, 3, "第1周 JS 基础与 DOM", 1, "JavaScript 基础语法、变量、函数、DOM 操作、事件绑定", false, "choice"),
                Chapter(18, 3, "第2周 JS 调试与断点", 2, "浏览器调试工具、断点、Call Stack、Scope 查看、网络抓包", false, "choice"),
                Chapter(19, 3, "第3周 算法与逆向分析", 3, "基础算法、加密解密、哈希函数、逆向分析方法", false, "choice"),
                Chapter(20, 3, "第4周 反调试与混淆", 4, "反调试技术、代码混淆、AST 抽象语法树、反混淆", true, "code"),
                Chapter(21, 3, "第5周 Cookie与Session", 5, "Cookie 机制、Session 管理、Token、JWT、签名验证", false, "choice"),
                Chapter(22, 3, "第6周 AES/RSA 加密", 6, "对称加密 AES、非对称加密 RSA、签名与验签、证书机制", false, "choice"),
                Chapter(23, 3, "第7周 爬虫自动化", 7, "Selenium、Playwright、Pyppeteer、无头浏览器", true, "code"),
                Chapter(24, 3, "第8周 验证码识别", 8, "OCR、Tesseract、打码平台、机器学习识别", true, "code"),
                Chapter(25, 3, "第9周 WebAssembly", 9, "WASM 逆向、Wasm 分析、内存访问、函数导出", false, "choice"),
                Chapter(26, 3, "第10周 App 抓包与 Hook", 10, "Charles、Fiddler、Frida、Xposed、抓包分析", true, "code"),
                Chapter(27, 3, "第11周 协议逆向", 11, "TCP/IP、HTTP/HTTPS、WebSocket、自定义协议分析", false, "choice"),
                Chapter(28, 3, "第12周 JS 逆向综合实战", 12, "逆向案例分析、完整项目实战、防爬对抗", true, "project")
            )
            database.chapterDao().insertAll(phase3Chapters)

            // ============ 阶段四：Go 高并发爬虫 ============
            val phase4Chapters = listOf(
                Chapter(29, 4, "Go 语法基础：变量、控制流、函数", 1, "变量声明、if/for、函数定义与多返回值", true, "choice"),
                Chapter(30, 4, "Go 数据结构：结构体、接口", 2, "struct、interface、方法接收者", true, "choice"),
                Chapter(31, 4, "并发基石：goroutine 与 channel", 3, "go 关键字、chan、无缓冲/有缓冲 channel", true, "choice"),
                Chapter(32, 4, "并发同步：select 与 sync", 4, "select 多路复用、Mutex、WaitGroup", true, "choice"),
                Chapter(33, 4, "Go 网络编程：net/http 与 colly", 5, "发起 HTTP 请求、colly 爬虫框架、限流", true, "code"),
                Chapter(34, 4, "代理与分布式：代理池、工作队列", 6, "代理切换、任务队列、分布式思想", true, "code"),
                Chapter(35, 4, "CGO 基础：Go 调用 C", 7, "#cgo、动态库编译", true, "code"),
                Chapter(36, 4, "实战项目：分布式爬虫系统", 8, "综合使用 goroutine、colly、CGO", true, "project")
            )
            database.chapterDao().insertAll(phase4Chapters)

            // ============ 阶段五：C语言 + CSAPP 核心 Lab ============
            val phase5Chapters = listOf(
                Chapter(37, 5, "C 基础：变量、控制流、数组", 1, "类型、运算符、循环、一维/二维数组", true, "choice"),
                Chapter(38, 5, "函数与指针基础", 2, "函数调用、指针基本操作", true, "choice"),
                Chapter(39, 5, "动态内存管理", 3, "malloc/free、堆与栈区别", true, "choice"),
                Chapter(40, 5, "数据表示（Data Lab）", 4, "位运算、补码、浮点数", true, "code"),
                Chapter(41, 5, "x86-64 汇编与栈帧", 5, "寄存器、mov、call/ret、栈帧布局", true, "choice"),
                Chapter(42, 5, "逆向与缓冲区溢出（Bomb Lab）", 6, "gdb、反汇编、溢出原理", true, "code"),
                Chapter(43, 5, "攻击技术（Attack Lab）", 7, "ROP、代码注入", true, "code"),
                Chapter(44, 5, "系统级 I/O 与虚拟内存", 8, "open/read/write、mmap、页表", true, "choice"),
                Chapter(45, 5, "动态内存分配器（Malloc Lab）", 9, "隐式/显式空闲链表、合并", true, "code"),
                Chapter(46, 5, "并发编程与网络编程", 10, "pthread、socket、信号", true, "code"),
                Chapter(47, 5, "综合项目：简易 HTTP 服务器", 11, "多线程 Web 服务器", true, "project")
            )
            database.chapterDao().insertAll(phase5Chapters)

            // ============ 阶段六：Rust 底层协议定制 ============
            val phase6Chapters = listOf(
                Chapter(48, 6, "Rust 所有权与移动", 1, "所有权规则、引用、借用", true, "choice"),
                Chapter(49, 6, "结构体、枚举与模式匹配", 2, "struct、enum、match", true, "choice"),
                Chapter(50, 6, "泛型、trait 与生命周期", 3, "trait、生命周期标注", true, "choice"),
                Chapter(51, 6, "错误处理与包管理", 4, "Result/Option、cargo", true, "code"),
                Chapter(52, 6, "异步编程：Tokio 与 reqwest", 5, "async/await、tokio 运行时", true, "code"),
                Chapter(53, 6, "自定义 TLS 客户端：rustls", 6, "配置 TLS、指纹模拟", true, "code"),
                Chapter(54, 6, "PyO3：Rust 写 Python 扩展", 7, "编写、编译、调用", true, "code"),
                Chapter(55, 6, "安全实践：模糊测试与审计", 8, "cargo-fuzz、依赖审计", true, "code"),
                Chapter(56, 6, "实战：定制 TLS 指纹爬虫", 9, "综合所有权、异步、PyO3", true, "project")
            )
            database.chapterDao().insertAll(phase6Chapters)

            // ============ 阶段七：汇编阅读 + 多语言协作 ============
            val phase7Chapters = listOf(
                Chapter(57, 7, "x86 指令集与 GDB 反汇编", 1, "常见指令、断点、查看寄存器", true, "choice"),
                Chapter(58, 7, "栈帧详细分析", 2, "函数参数传递、返回值、局部变量", true, "choice"),
                Chapter(59, 7, "Python 调用 Go 动态库", 3, "ctypes、.so 文件、类型转换", true, "code"),
                Chapter(60, 7, "Go 通过 CGO 调用 Rust", 4, "生成 C 头文件、链接", true, "code"),
                Chapter(61, 7, "三者协作：Rust 加密 + Go 调度 + Python 逻辑", 5, "综合 FFI 协作", true, "code"),
                Chapter(62, 7, "完整混合爬虫系统", 6, "从需求到实现，打包发布", true, "project")
            )
            database.chapterDao().insertAll(phase7Chapters)

            // ============ 插入所有题目 ============
            val questions = mutableListOf<QuestionBank>()
            var questionId = 1

            // ============ 阶段一题目 ============
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
                QuestionBank(questionId++, 2, "请写出计算 1+2+3+...+100 的 while 循环核心逻辑", "fill", null, "while i <= 100:\n    sum += i\n    i += 1", 15)
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

            // ============ 阶段三题目 ============
            // 章节17: JS 基础与 DOM
            questions.addAll(listOf(
                QuestionBank(questionId++, 17, "JavaScript 中声明变量的关键字是？", "choice", """["A: var, let, const", "B: int, float, bool", "C: var, let, static", "D: let, const, final"]""", "A", 10),
                QuestionBank(questionId++, 17, "DOM 代表什么？", "choice", """["A: Document Object Model", "B: Data Object Model", "C: Document Oriented Model", "D: Data Oriented Model"]""", "A", 10),
                QuestionBank(questionId++, 17, "获取 DOM 元素的方法是？", "choice", """["A: getElementById()", "B: selectElement()", "C: findElement()", "D: searchElement()"]""", "A", 10)
            ))

            // 章节18: JS 调试与断点
            questions.addAll(listOf(
                QuestionBank(questionId++, 18, "浏览器调试工具 F12 打开的叫什么？", "choice", """["A: Developer Tools", "B: Browser Tools", "C: Debug Tools", "D: Code Tools"]""", "A", 10),
                QuestionBank(questionId++, 18, "设置断点的快捷键通常是？", "choice", """["A: F9", "B: F5", "C: F8", "D: F10"]""", "A", 10),
                QuestionBank(questionId++, 18, "查看调用栈的面板是？", "choice", """["A: Call Stack", "B: Stack Trace", "C: Function List", "D: Memory Stack"]""", "A", 10)
            ))

            // 章节19: 算法与逆向分析
            questions.addAll(listOf(
                QuestionBank(questionId++, 19, "常见的哈希算法是？", "choice", """["A: MD5, SHA-1, SHA-256", "B: AES, DES, RSA", "C: Base64, Hex, URL", "D: UTF-8, ASCII, GBK"]""", "A", 10),
                QuestionBank(questionId++, 19, "加密和解密使用相同密钥的是？", "choice", """["A: 对称加密", "B: 非对称加密", "C: 哈希加密", "D: 单向加密"]""", "A", 10),
                QuestionBank(questionId++, 19, "RSA 使用什么密钥对？", "choice", """["A: 公钥和私钥", "B: 对称密钥", "C: 会话密钥", "D: 临时密钥"]""", "A", 10)
            ))

            // 章节21: Cookie与Session
            questions.addAll(listOf(
                QuestionBank(questionId++, 21, "Cookie 存储在什么位置？", "choice", """["A: 客户端浏览器", "B: 服务器", "C: 数据库", "D: 中间件"]""", "A", 10),
                QuestionBank(questionId++, 21, "JWT 的全称是什么？", "choice", """["A: JSON Web Token", "B: JavaScript Web Token", "C: Java Web Token", "D: JSON Web Transport"]""", "A", 10),
                QuestionBank(questionId++, 21, "Session ID 通常存储在？", "choice", """["A: Cookie", "B: URL", "C: Header", "D: Body"]""", "A", 10)
            ))

            // 章节22: AES/RSA 加密
            questions.addAll(listOf(
                QuestionBank(questionId++, 22, "AES 是哪种加密方式？", "choice", """["A: 对称加密", "B: 非对称加密", "C: 哈希加密", "D: 单向加密"]""", "A", 10),
                QuestionBank(questionId++, 22, "RSA 公钥用于？", "choice", """["A: 加密", "B: 解密", "C: 签名", "D: 验签"]""", "A", 10),
                QuestionBank(questionId++, 22, "RSA 私钥用于？", "choice", """["A: 解密", "B: 加密", "C: 加密和解密", "D: 哈希"]""", "A", 10)
            ))

            // ============ 阶段四题目 ============
            // 章节29: Go 语法基础
            questions.addAll(listOf(
                QuestionBank(questionId++, 29, "Go 中声明变量的关键字是？", "choice", """["A: var", "B: let", "C: const", "D: val"]""", "A", 10),
                QuestionBank(questionId++, 29, "Go 中函数可以返回几个值？", "choice", """["A: 一个", "B: 多个", "C: 必须两个", "D: 不能返回"]""", "B", 10),
                QuestionBank(questionId++, 29, "Go 中 if 语句的条件？", "choice", """["A: 必须是布尔类型", "B: 可以是任何类型", "C: 必须是整数", "D: 必须是字符串"]""", "A", 10)
            ))

            // 章节30: Go 数据结构
            questions.addAll(listOf(
                QuestionBank(questionId++, 30, "Go 中定义结构体使用的关键字是？", "choice", """["A: struct", "B: class", "C: type", "D: object"]""", "A", 10),
                QuestionBank(questionId++, 30, "Go 中接口的特点是？", "choice", """["A: 隐式实现", "B: 显式声明", "C: 必须实现所有方法", "D: 不能有方法"]""", "A", 10),
                QuestionBank(questionId++, 30, "Go 中方法接收者在什么位置？", "choice", """["A: 函数名前面", "B: 参数列表", "C: 返回值", "D: 函数体内部"]""", "A", 10)
            ))

            // 章节31: goroutine 与 channel
            questions.addAll(listOf(
                QuestionBank(questionId++, 31, "启动 goroutine 使用什么关键字？", "choice", """["A: go", "B: goroutine", "C: thread", "D: async"]""", "A", 10),
                QuestionBank(questionId++, 31, "channel 的声明关键字是？", "choice", """["A: chan", "B: channel", "C: pipe", "D: queue"]""", "A", 10),
                QuestionBank(questionId++, 31, "无缓冲 channel 发送后会？", "choice", """["A: 阻塞直到接收", "B: 立即返回", "C: 忽略", "D: 报错"]""", "A", 10)
            ))

            // 章节32: select 与 sync
            questions.addAll(listOf(
                QuestionBank(questionId++, 32, "select 用于什么场景？", "choice", """["A: 多路复用 channel", "B: 选择分支", "C: 循环控制", "D: 异常处理"]""", "A", 10),
                QuestionBank(questionId++, 32, "sync.Mutex 的作用是？", "choice", """["A: 互斥锁", "B: 读写锁", "C: 等待组", "D: 条件变量"]""", "A", 10),
                QuestionBank(questionId++, 32, "sync.WaitGroup 的 Add() 方法用于？", "choice", """["A: 添加等待计数", "B: 增加 goroutine", "C: 启动线程", "D: 等待结束"]""", "A", 10)
            ))

            // ============ 阶段五题目 ============
            // 章节37: C 基础
            questions.addAll(listOf(
                QuestionBank(questionId++, 37, "C 语言中 int 类型占用多少字节？", "choice", """["A: 2", "B: 4", "C: 8", "D: 1"]""", "B", 10),
                QuestionBank(questionId++, 37, "C 语言数组下标从什么开始？", "choice", """["A: 0", "B: 1", "C: 任意值", "D: 不固定"]""", "A", 10),
                QuestionBank(questionId++, 37, "for 循环的三个部分分别是？", "choice", """["A: 初始化、条件、更新", "B: 条件、初始化、更新", "C: 更新、条件、初始化", "D: 任意顺序"]""", "A", 10)
            ))

            // 章节38: 函数与指针
            questions.addAll(listOf(
                QuestionBank(questionId++, 38, "指针变量存储的是什么？", "choice", """["A: 内存地址", "B: 数值", "C: 字符", "D: 字符串"]""", "A", 10),
                QuestionBank(questionId++, 38, "& 运算符的作用是？", "choice", """["A: 取地址", "B: 取内容", "C: 按位与", "D: 引用"]""", "A", 10),
                QuestionBank(questionId++, 38, "* 运算符用于指针时表示？", "choice", """["A: 取内容", "B: 取地址", "C: 乘法", "D: 指针标记"]""", "A", 10)
            ))

            // 章节39: 动态内存
            questions.addAll(listOf(
                QuestionBank(questionId++, 39, "分配动态内存使用什么函数？", "choice", """["A: malloc()", "B: alloc()", "C: new()", "D: create()"]""", "A", 10),
                QuestionBank(questionId++, 39, "释放动态内存使用什么函数？", "choice", """["A: free()", "B: delete()", "C: release()", "D: destroy()"]""", "A", 10),
                QuestionBank(questionId++, 39, "堆和栈的区别是？", "choice", """["A: 堆需要手动管理，栈自动管理", "B: 栈需要手动管理，堆自动管理", "C: 一样", "D: 堆在CPU，栈在内存"]""", "A", 10)
            ))

            // 章节41: 汇编与栈帧
            questions.addAll(listOf(
                QuestionBank(questionId++, 41, "x86-64 中返回值通常存放在哪个寄存器？", "choice", """["A: rax", "B: rbx", "C: rcx", "D: rdx"]""", "A", 10),
                QuestionBank(questionId++, 41, "call 指令会做什么？", "choice", """["A: 保存返回地址并跳转", "B: 直接跳转", "C: 保存所有寄存器", "D: 清栈"]""", "A", 10),
                QuestionBank(questionId++, 41, "栈帧的基址指针通常是？", "choice", """["A: rbp", "B: rsp", "C: rax", "D: rbx"]""", "A", 10)
            ))

            // 章节44: 系统级 I/O
            questions.addAll(listOf(
                QuestionBank(questionId++, 44, "打开文件的系统调用是？", "choice", """["A: open()", "B: fopen()", "C: create()", "D: init()"]""", "A", 10),
                QuestionBank(questionId++, 44, "mmap 的作用是？", "choice", """["A: 内存映射文件", "B: 分配内存", "C: 复制内存", "D: 释放内存"]""", "A", 10),
                QuestionBank(questionId++, 44, "虚拟内存通过什么映射到物理内存？", "choice", """["A: 页表", "B: 段表", "C: MMU", "D: TLB"]""", "A", 10)
            ))

            // ============ 阶段六题目 ============
            // 章节48: 所有权与移动
            questions.addAll(listOf(
                QuestionBank(questionId++, 48, "Rust 中值在同一时刻有几个所有者？", "choice", """["A: 一个", "B: 多个", "C: 零个", "D: 不限"]""", "A", 10),
                QuestionBank(questionId++, 48, "赋值时默认发生什么？", "choice", """["A: 移动", "B: 复制", "C: 引用", "D: 借用"]""", "A", 10),
                QuestionBank(questionId++, 48, "引用使用什么符号？", "choice", """["A: &", "B: *", "C: @", "D: #"]""", "A", 10)
            ))

            // 章节49: 结构体、枚举、模式匹配
            questions.addAll(listOf(
                QuestionBank(questionId++, 49, "Rust 中结构体使用什么关键字？", "choice", """["A: struct", "B: class", "C: type", "D: object"]""", "A", 10),
                QuestionBank(questionId++, 49, "模式匹配使用什么关键字？", "choice", """["A: match", "B: switch", "C: case", "D: select"]""", "A", 10),
                QuestionBank(questionId++, 49, "Option 枚举有几个变体？", "choice", """["A: 2 (Some, None)", "B: 1 (Some)", "C: 3", "D: 4"]""", "A", 10)
            ))

            // 章节50: 泛型、trait、生命周期
            questions.addAll(listOf(
                QuestionBank(questionId++, 50, "定义泛型使用什么符号？", "choice", """["A: <T>", "B: (T)", "C: [T]", "D: {T}"]""", "A", 10),
                QuestionBank(questionId++, 50, "trait 类似于其他语言的什么？", "choice", """["A: 接口", "B: 类", "C: 结构体", "D: 模板"]""", "A", 10),
                QuestionBank(questionId++, 50, "生命周期标注使用什么符号？", "choice", """["A: 'a", "B: `a", "C: &a", "D: *a"]""", "A", 10)
            ))

            // 章节51: 错误处理与包管理
            questions.addAll(listOf(
                QuestionBank(questionId++, 51, "Result 枚举有几个变体？", "choice", """["A: 2 (Ok, Err)", "B: 1 (Ok)", "C: 3", "D: 4"]""", "A", 10),
                QuestionBank(questionId++, 51, "Rust 的包管理器是？", "choice", """["A: cargo", "B: npm", "C: pip", "D: go"]""", "A", 10),
                QuestionBank(questionId++, 51, "cargo build 的作用是？", "choice", """["A: 编译项目", "B: 运行项目", "C: 测试项目", "D: 发布项目"]""", "A", 10)
            ))

            // ============ 阶段七题目 ============
            // 章节57: x86 指令集与 GDB
            questions.addAll(listOf(
                QuestionBank(questionId++, 57, "GDB 中设置断点的命令是？", "choice", """["A: break", "B: stop", "C: pause", "D: halt"]""", "A", 10),
                QuestionBank(questionId++, 57, "GDB 中查看寄存器的命令是？", "choice", """["A: info registers", "B: show reg", "C: print reg", "D: regs"]""", "A", 10),
                QuestionBank(questionId++, 57, "GDB 中单步执行的命令是？", "choice", """["A: step / next", "B: run", "C: continue", "D: go"]""", "A", 10)
            ))

            // 章节58: 栈帧分析
            questions.addAll(listOf(
                QuestionBank(questionId++, 58, "函数参数通常通过什么传递？", "choice", """["A: 寄存器或栈", "B: 全局变量", "C: 文件", "D: 网络"]""", "A", 10),
                QuestionBank(questionId++, 58, "返回地址保存在哪里？", "choice", """["A: 栈上", "B: 寄存器", "C: 静态区", "D: 堆"]""", "A", 10),
                QuestionBank(questionId++, 58, "局部变量通常存储在？", "choice", """["A: 栈帧内", "B: 堆", "C: 静态区", "D: 寄存器"]""", "A", 10)
            ))

            database.questionBankDao().insertAll(questions)

            // ============ 为所有章节初始化进度记录 ============
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
