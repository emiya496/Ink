from __future__ import annotations

import copy
import zipfile
from pathlib import Path
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
DOCS_DIR = ROOT / "docs"
TEMPLATE_DOC = DOCS_DIR / "系统说明文档.docx"
OUTPUT_DOC = DOCS_DIR / "系统说明文档-文学创作社区及智能浏览助手-修订版.docx"

W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
R_NS = "http://schemas.openxmlformats.org/officeDocument/2006/relationships"
XML_NS = "http://www.w3.org/XML/1998/namespace"

NAMESPACES = {
    "wpc": "http://schemas.microsoft.com/office/word/2010/wordprocessingCanvas",
    "mc": "http://schemas.openxmlformats.org/markup-compatibility/2006",
    "o": "urn:schemas-microsoft-com:office:office",
    "r": R_NS,
    "m": "http://schemas.openxmlformats.org/officeDocument/2006/math",
    "v": "urn:schemas-microsoft-com:vml",
    "wp14": "http://schemas.microsoft.com/office/word/2010/wordprocessingDrawing",
    "wp": "http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing",
    "w": W_NS,
    "w14": "http://schemas.microsoft.com/office/word/2010/wordml",
    "w10": "urn:schemas-microsoft-com:office:word",
    "w15": "http://schemas.microsoft.com/office/word/2012/wordml",
    "wpg": "http://schemas.microsoft.com/office/word/2010/wordprocessingGroup",
    "wpi": "http://schemas.microsoft.com/office/word/2010/wordprocessingInk",
    "wne": "http://schemas.microsoft.com/office/word/2006/wordml",
    "wps": "http://schemas.microsoft.com/office/word/2010/wordprocessingShape",
    "wpsCustomData": "http://www.wps.cn/officeDocument/2013/wpsCustomData",
}

for prefix, uri in NAMESPACES.items():
    ET.register_namespace(prefix, uri)


def w(tag: str) -> str:
    return f"{{{W_NS}}}{tag}"


def make_run(text: str) -> ET.Element:
    run = ET.Element(w("r"))
    text_node = ET.SubElement(run, w("t"))
    if text.startswith(" ") or text.endswith(" "):
        text_node.set(f"{{{XML_NS}}}space", "preserve")
    text_node.text = text
    return run


def make_paragraph(
    text: str = "",
    *,
    style: str | None = None,
    align: str | None = None,
    first_line: bool = False,
    spacing_after: str | None = None,
    line: str | None = None,
    line_rule: str | None = None,
) -> ET.Element:
    paragraph = ET.Element(w("p"))
    ppr = ET.SubElement(paragraph, w("pPr"))
    if style:
        pstyle = ET.SubElement(ppr, w("pStyle"))
        pstyle.set(w("val"), style)
    if spacing_after is not None or line is not None or line_rule is not None:
        spacing = ET.SubElement(ppr, w("spacing"))
        if spacing_after is not None:
            spacing.set(w("after"), spacing_after)
        if line is not None:
            spacing.set(w("line"), line)
        if line_rule is not None:
            spacing.set(w("lineRule"), line_rule)
    if first_line:
        ind = ET.SubElement(ppr, w("ind"))
        ind.set(w("firstLine"), "480")
    if align:
        jc = ET.SubElement(ppr, w("jc"))
        jc.set(w("val"), align)
    if text:
        paragraph.append(make_run(text))
    return paragraph


def h1(text: str) -> ET.Element:
    return make_paragraph(text, style="2")


def h2(text: str) -> ET.Element:
    return make_paragraph(text, style="3", spacing_after="120", line="400", line_rule="exact")


def h3(text: str) -> ET.Element:
    return make_paragraph(text, style="4")


def body(text: str) -> ET.Element:
    return make_paragraph(text, style="7", first_line=True)


def caption(text: str) -> ET.Element:
    return make_paragraph(text, style="12", align="center")


def placeholder(title: str, no: str) -> list[ET.Element]:
    return [
        make_paragraph(f"【截图占位：{title}】", style="7", align="center"),
        caption(f"图 {no} {title}"),
    ]


def replace_runs(paragraph: ET.Element, texts: list[str]) -> None:
    ppr = paragraph.find(w("pPr"))
    run_templates = paragraph.findall(w("r")) or [ET.Element(w("r"))]
    for child in list(paragraph):
        if child.tag != w("pPr"):
            paragraph.remove(child)
    for idx, text in enumerate(texts):
        run = copy.deepcopy(run_templates[min(idx, len(run_templates) - 1)])
        found_text = False
        for child in list(run):
            if child.tag == w("t"):
                child.text = text
                found_text = True
        if not found_text:
            t = ET.SubElement(run, w("t"))
            t.text = text
        paragraph.append(run)
    if ppr is not None and paragraph[0] is not ppr:
        paragraph.remove(ppr)
        paragraph.insert(0, ppr)


def update_fields(settings_root: ET.Element) -> None:
    node = settings_root.find(w("updateFields"))
    if node is None:
        node = ET.SubElement(settings_root, w("updateFields"))
    node.set(w("val"), "true")


def build_content() -> list[ET.Element]:
    items: list[ET.Element] = []

    def add(element: ET.Element) -> None:
        items.append(element)

    def add_many(elements: list[ET.Element]) -> None:
        items.extend(elements)

    add(h1("系统总体设计"))
    add(h2("系统说明或需求"))
    add(body("本系统名称为“基于大模型的文学创作社区及智能浏览助手”，主要面向文学创作者、文学爱好者和系统管理员，提供集作品创作、内容发布、社区互动、智能写作辅助、智能阅读辅助和后台治理于一体的综合服务。"))
    add(body("系统建设目标是通过文学社区与大语言模型能力的融合，提升文学内容创作效率和阅读体验。系统既支持普通用户在 Web 端完成注册登录、作品创作、阅读互动等核心流程，也支持管理员通过后台完成用户、内容、标签、评论及 AI 调用统计等治理工作。"))
    add(body("根据需求规格说明书，本系统覆盖用户管理、创作管理、社区互动、智能辅助和系统管理五个功能模块。系统不涉及商业支付、版权交易等商业化功能，重点围绕毕业设计范围内的文学创作社区与智能浏览助手能力展开实现。"))

    add(h2("运行环境"))
    add(body("硬件环境方面，系统建议运行在云服务器上，服务器处理器不低于 2 核，内存不低于 4GB，磁盘建议保留 40GB 以上可用空间；客户端设备可为笔记本或台式机，通过有线或无线网络访问系统。"))
    add(body("软件环境方面，后端运行环境为 Java 与 JDK17，数据库采用 MySQL 8.0；前端通过浏览器访问，浏览器插件运行于 Chromium 内核浏览器。开发工具建议采用 IntelliJ IDEA 与 Visual Studio Code。"))
    add(body("系统支持前后端分离部署，前端负责页面展示与交互，后端负责认证鉴权、业务处理和数据持久化，同时后端连接 AI 服务接口和邮件服务接口，为智能辅助和验证码功能提供支撑。"))

    add(h1("功能设计"))
    add(h2("功能描述"))
    add(body("本系统总共分为 5 个模块：用户管理模块、创作管理模块、社区互动模块、智能辅助模块、系统管理模块。"))
    add(body("（1）用户管理：涉及用户注册、登录、退出、资料维护、头像上传、邮箱绑定、更换邮箱、密码修改、找回密码以及用户主页等功能。"))
    add(body("（2）创作管理：涉及文章新建、编辑、保存草稿、发布作品、管理作品以及小说章节管理等功能。"))
    add(body("（3）社区互动：涉及首页推荐、分类浏览、内容详情、点赞、评论、收藏、书架管理、关注与粉丝等功能。"))
    add(body("（4）智能辅助：涉及文章摘要、AI 续写、AI 润色、关键词提取、情感分析、文风分析、智能问答以及浏览器插件辅助等功能。"))
    add(body("（5）系统管理：涉及后台用户管理、内容管理、标签管理、评论管理和 AI 调用统计等治理功能。"))

    add(h2("功能概要结构图"))
    add_many(placeholder("系统功能结构图（待插入）", "2-1"))
    add_many(placeholder("用户管理模块功能结构图（待插入）", "2-2"))
    add_many(placeholder("创作管理模块功能结构图（待插入）", "2-3"))
    add_many(placeholder("社区互动模块功能结构图（待插入）", "2-4"))
    add_many(placeholder("智能辅助模块功能结构图（待插入）", "2-5"))
    add_many(placeholder("系统管理模块功能结构图（待插入）", "2-6"))

    add(h1("操作说明"))

    add(h2("用户模块"))
    add(h3("登录"))
    add(body("未登录用户点击登录入口后进入登录页面，输入用户名或邮箱以及密码，系统校验账号状态和密码正确性，通过后以登录身份进入系统首页或原计划访问页面。登录成功后，系统会在前端保存 token 与用户基础信息。"))
    add_many(placeholder("登录界面（待插入）", "3-1"))
    add(h3("注销"))
    add(body("登录状态下，用户点击退出登录按钮后，系统清除当前登录状态信息，包括本地 token 和用户缓存数据，并跳转到未登录可访问页面。"))
    add(h3("注册"))
    add(body("用户点击注册入口进入注册页面，填写用户名、密码、确认密码和可选邮箱后提交。系统校验用户名唯一性和密码规则，注册成功后提示用户前往登录。"))
    add_many(placeholder("注册界面（待插入）", "3-2"))
    add(h3("用户信息维护"))
    add(body("用户登录系统后，可进入个人中心维护头像、个人简介、用户名、邮箱和密码等资料信息，并查看个人作品、草稿、点赞、关注和粉丝等内容。"))
    add_many(placeholder("用户中心资料维护界面（待插入）", "3-3"))
    add(h3("修改密码"))
    add(body("用户在个人中心点击修改密码按钮后，输入旧密码和新密码即可完成密码修改。系统会校验旧密码是否正确，并对新密码执行规则校验。"))
    add_many(placeholder("修改密码界面（待插入）", "3-4"))
    add(h3("找回密码"))
    add(body("用户忘记密码时，可在登录页面点击找回密码入口，通过邮箱验证码对密码进行重置。验证码为 6 位且在有效期内使用，一次使用后即失效。"))
    add_many(placeholder("找回密码界面（待插入）", "3-5"))
    add(h3("用户主页"))
    add(body("用户可通过作品详情页、评论区或关注列表进入其他用户主页，查看该用户的公开资料、关注关系以及有权限查看的公开作品列表。"))
    add_many(placeholder("用户主页界面（待插入）", "3-6"))

    add(h2("创作管理模块"))
    add(h3("新建文章"))
    add(body("已登录用户进入发布页后，可新建文学作品，填写内容类型、标题、正文、封面图、标签和可见性等信息。小说类型支持章节化创作，普通文章类型支持直接录入正文。"))
    add_many(placeholder("新建文章界面（待插入）", "3-7"))
    add(h3("编辑文章"))
    add(body("作者可进入作品编辑页面，对标题、正文、标签、封面、可见性和章节内容进行修改。系统仅允许作品作者本人执行编辑操作。"))
    add_many(placeholder("编辑文章界面（待插入）", "3-8"))
    add(h3("保存草稿"))
    add(body("用户在创作过程中可点击保存草稿按钮，将未完成内容保存为草稿。草稿允许内容暂不完整，后续可在个人中心草稿列表继续编辑。"))
    add_many(placeholder("保存草稿界面（待插入）", "3-9"))
    add(h3("发布文章"))
    add(body("用户确认文章标题、类型和正文内容后点击发布按钮，系统校验必填字段并将内容状态置为正式发布。公开作品发布后可被首页、分类页和排行榜检索。"))
    add_many(placeholder("发布文章界面（待插入）", "3-10"))
    add(h3("管理文章"))
    add(body("用户在个人中心可查看我的作品和我的草稿列表，对已有内容进行查看、编辑或删除操作。列表中通常展示标题、类型、状态、阅读量、点赞量和发布时间等字段。"))
    add_many(placeholder("作品管理界面（待插入）", "3-11"))
    add(h3("小说章节管理"))
    add(body("对于小说类型内容，系统支持章节新增、补写、编辑和删除。章节按顺序管理并在详情页按既定顺序展示。"))
    add_many(placeholder("小说章节管理界面（待插入）", "3-12"))

    add(h2("社区互动模块"))
    add(h3("首页浏览"))
    add(body("用户进入首页后，可浏览推荐内容、热门标签、轮播推荐和排行榜等信息。首页主要承担内容发现与作品分发功能。"))
    add_many(placeholder("首页界面（待插入）", "3-13"))
    add(h3("分类浏览"))
    add(body("用户进入分类页后，可按内容类型和分页条件浏览作品，并结合搜索或筛选条件查看对应内容。"))
    add_many(placeholder("分类浏览界面（待插入）", "3-14"))
    add(h3("内容详情"))
    add(body("用户点击作品卡片后进入详情页，可查看正文、章节、标签、作者信息、评论列表以及互动与 AI 阅读助手入口。"))
    add_many(placeholder("内容详情界面（待插入）", "3-15"))
    add(h3("点赞文章"))
    add(body("已登录用户在详情页点击点赞按钮后，系统执行点赞或取消点赞切换，并即时刷新点赞状态和点赞数量。"))
    add(h3("评论文章"))
    add(body("用户在详情页评论区输入评论内容后提交，系统将评论保存并按时间倒序展示在评论列表中；用户可删除本人评论，管理员可删除任意违规评论。"))
    add_many(placeholder("评论交互界面（待插入）", "3-16"))
    add(h3("收藏文章"))
    add(body("用户可在详情页将作品加入收藏或取消收藏。收藏结果会同步到书架页面，用于后续集中管理。"))
    add(h3("书架管理"))
    add(body("书架页面集中展示用户收藏的作品，支持分页浏览、查看详情和取消收藏。对于当前不可见的收藏内容，系统应进行容错处理并提示原因，而不影响其余内容展示。"))
    add_many(placeholder("书架管理界面（待插入）", "3-17"))
    add(h3("关注与粉丝"))
    add(body("用户可在其他用户主页执行关注操作，并在个人中心查看关注列表与粉丝列表。关注关系同时影响“仅粉丝可见”内容的可访问范围。"))
    add_many(placeholder("关注与粉丝界面（待插入）", "3-18"))

    add(h2("智能辅助模块"))
    add(h3("文章摘要"))
    add(body("用户在详情页点击文章摘要功能后，系统将当前文章文本提交给 AI 服务，返回摘要结果，用于帮助读者快速理解作品内容。"))
    add_many(placeholder("文章摘要界面（待插入）", "3-19"))
    add(h3("文章续写"))
    add(body("用户在发布页点击 AI 续写功能后，系统基于当前文本生成续写内容，用户可选择插入正文继续编辑。"))
    add_many(placeholder("AI续写界面（待插入）", "3-20"))
    add(h3("文章润色"))
    add(body("用户在发布页点击 AI 润色功能后，系统对选中文本或全文进行表达优化，并将结果返回到 AI 面板供用户决定是否采纳。"))
    add_many(placeholder("AI润色界面（待插入）", "3-21"))
    add(h3("关键词、情感、文风和问答"))
    add(body("系统在阅读辅助场景中提供关键词提取、情感分析、文风分析与智能问答等功能，帮助读者从多个角度理解作品内容。"))
    add_many(placeholder("阅读辅助分析界面（待插入）", "3-22"))
    add(h3("浏览器插件辅助"))
    add(body("系统以浏览器插件形式在第三方阅读页面提供悬浮入口，用户可通过插件调用摘要、问答等智能辅助能力，实现站外阅读时的快捷操作。"))
    add_many(placeholder("浏览器插件辅助界面（待插入）", "3-23"))

    add(h2("系统管理模块"))
    add(h3("用户管理"))
    add(body("管理员进入后台用户管理页面后，可分页查看用户信息，按用户名筛选用户，并执行启用、禁用和删除等治理操作。"))
    add_many(placeholder("后台用户管理界面（待插入）", "3-24"))
    add(h3("内容管理"))
    add(body("管理员进入后台内容管理页面后，可按内容类型、状态和标题等条件筛选作品，并执行下架、恢复或删除操作。"))
    add_many(placeholder("后台内容管理界面（待插入）", "3-25"))
    add(h3("标签管理"))
    add(body("管理员进入后台标签管理页面后，可新增系统标签，或对已有标签执行启用、禁用和删除操作。"))
    add_many(placeholder("后台标签管理界面（待插入）", "3-26"))
    add(h3("评论管理"))
    add(body("管理员进入后台评论管理页面后，可按关键词分页检索评论并删除违规评论，以维护社区内容秩序。"))
    add_many(placeholder("后台评论管理界面（待插入）", "3-27"))
    add(h3("AI详情"))
    add(body("管理员进入后台 AI 统计页面后，可查看全站 AI 调用总量、按功能分类统计和按用户调用排行，便于掌握系统智能能力使用情况。"))
    add_many(placeholder("后台AI统计界面（待插入）", "3-28"))

    return items


def rebuild_document(document_xml: bytes) -> bytes:
    root = ET.fromstring(document_xml)
    body_node = root.find(w("body"))
    if body_node is None:
        raise RuntimeError("word/document.xml missing body node")

    children = list(body_node)
    preserved_prefix = [copy.deepcopy(node) for node in children[:22]]
    final_sect = copy.deepcopy(children[-1])

    replace_runs(preserved_prefix[3], ["基于大模型的文学创作社区及智能浏览助手", "V1.0"])
    replace_runs(preserved_prefix[6], ["-", "---", "系统说明文档"])
    replace_runs(
        preserved_prefix[14],
        [
            "本文档用于说明系统总体设计、功能设计和主要使用流程；需要插入系统截图的位置均已预留占位，请根据最终界面手动补充。",
        ],
    )
    replace_runs(
        preserved_prefix[18],
        ["注：本文档中的截图占位、编号和界面名称均围绕当前系统实现编写，可在最终提交前按实际界面进一步校正。"],
    )

    for child in list(body_node):
        body_node.remove(child)

    for child in preserved_prefix:
        body_node.append(child)
    for child in build_content():
        body_node.append(child)
    body_node.append(final_sect)

    return ET.tostring(root, encoding="utf-8", xml_declaration=True)


def rebuild_settings(settings_xml: bytes) -> bytes:
    root = ET.fromstring(settings_xml)
    update_fields(root)
    return ET.tostring(root, encoding="utf-8", xml_declaration=True)


def main() -> None:
    if not TEMPLATE_DOC.exists():
        raise FileNotFoundError(TEMPLATE_DOC)

    with zipfile.ZipFile(TEMPLATE_DOC, "r") as src_zip:
        document_xml = src_zip.read("word/document.xml")
        settings_xml = src_zip.read("word/settings.xml")
        new_document = rebuild_document(document_xml)
        new_settings = rebuild_settings(settings_xml)

        with zipfile.ZipFile(OUTPUT_DOC, "w", compression=zipfile.ZIP_DEFLATED) as dst_zip:
            for info in src_zip.infolist():
                data = src_zip.read(info.filename)
                if info.filename == "word/document.xml":
                    data = new_document
                elif info.filename == "word/settings.xml":
                    data = new_settings
                dst_zip.writestr(info, data)

    print(OUTPUT_DOC)


if __name__ == "__main__":
    main()
