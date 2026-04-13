from __future__ import annotations

import copy
import zipfile
from pathlib import Path
import xml.etree.ElementTree as ET


ROOT = Path(__file__).resolve().parents[1]
DOCS_DIR = ROOT / "docs"
SOURCE_DOC = DOCS_DIR / "系统说明文档-文学创作社区及智能浏览助手-修订版.docx"
OUTPUT_DOC = DOCS_DIR / "系统说明文档-文学创作社区及智能浏览助手-插图版.docx"
DIAGRAM_DIR = ROOT / "tmp_2_2_diagrams"
TMP_TEMPLATE_DIR = ROOT / "tmp_system_doc"

W_NS = "http://schemas.openxmlformats.org/wordprocessingml/2006/main"
NS = {"w": W_NS}



def w(tag: str) -> str:
    return f"{{{W_NS}}}{tag}"


def load_template_image_paragraphs() -> list[ET.Element]:
    root = ET.parse(TMP_TEMPLATE_DIR / "word" / "document.xml").getroot()
    body = root.find("w:body", NS)
    if body is None:
        raise RuntimeError("template body not found")
    indices = [58, 64, 68, 70, 76, 80]
    children = list(body)
    return [copy.deepcopy(children[idx]) for idx in indices]


def update_drawing_paragraph(paragraph: ET.Element, rid: str, docpr_id: int, name: str) -> None:
    doc_pr = paragraph.find(".//{http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing}docPr")
    if doc_pr is not None:
        doc_pr.set("id", str(docpr_id))
        doc_pr.set("name", name)
    cnv_pr = paragraph.find(".//{http://schemas.openxmlformats.org/drawingml/2006/picture}cNvPr")
    if cnv_pr is not None:
        cnv_pr.set("id", str(docpr_id))
        cnv_pr.set("name", name)
    blip = paragraph.find(".//{http://schemas.openxmlformats.org/drawingml/2006/main}blip")
    if blip is not None:
        blip.set("{http://schemas.openxmlformats.org/officeDocument/2006/relationships}embed", rid)


def replace_caption_text(caption_paragraph: ET.Element, text: str) -> None:
    for node in caption_paragraph.findall(".//w:t", NS):
        node.text = ""
    text_nodes = caption_paragraph.findall(".//w:t", NS)
    if text_nodes:
        text_nodes[-1].text = text


def build_document(document_xml: bytes) -> bytes:
    root = ET.fromstring(document_xml)
    body = root.find("w:body", NS)
    if body is None:
        raise RuntimeError("document body not found")

    template_paragraphs = load_template_image_paragraphs()
    caption_map = {
        "图 2-1 系统功能结构图（待插入）": ("图 2-1 系统功能结构图", "rId18", 201, "系统功能结构图"),
        "图 2-2 用户管理模块功能结构图（待插入）": ("图 2-2 用户管理模块功能结构图", "rId19", 202, "用户管理模块功能结构图"),
        "图 2-3 创作管理模块功能结构图（待插入）": ("图 2-3 创作管理模块功能结构图", "rId20", 203, "创作管理模块功能结构图"),
        "图 2-4 社区互动模块功能结构图（待插入）": ("图 2-4 社区互动模块功能结构图", "rId21", 204, "社区互动模块功能结构图"),
        "图 2-5 智能辅助模块功能结构图（待插入）": ("图 2-5 智能辅助模块功能结构图", "rId22", 205, "智能辅助模块功能结构图"),
        "图 2-6 系统管理模块功能结构图（待插入）": ("图 2-6 系统管理模块功能结构图", "rId23", 206, "系统管理模块功能结构图"),
    }

    children = list(body)
    i = 0
    image_index = 0
    while i < len(children):
        child = children[i]
        if child.tag == w("p"):
            text = "".join(t.text or "" for t in child.findall(".//w:t", NS)).strip()
            if text.startswith("【截图占位：") and i + 1 < len(children):
                next_p = children[i + 1]
                next_text = "".join(t.text or "" for t in next_p.findall(".//w:t", NS)).strip()
                if next_text in caption_map:
                    new_caption, rid, docpr_id, name = caption_map[next_text]
                    image_para = copy.deepcopy(template_paragraphs[image_index])
                    update_drawing_paragraph(image_para, rid, docpr_id, name)
                    body.insert(i, image_para)
                    body.remove(child)
                    replace_caption_text(next_p, new_caption)
                    children = list(body)
                    image_index += 1
                    i += 2
                    continue
        i += 1

    return ET.tostring(root, encoding="utf-8", xml_declaration=True)


def build_doc() -> None:
    diagram_files = {
        "word/media/image7.png": DIAGRAM_DIR / "system_overview.png",
        "word/media/image8.png": DIAGRAM_DIR / "user_module.png",
        "word/media/image9.png": DIAGRAM_DIR / "creation_module.png",
        "word/media/image10.png": DIAGRAM_DIR / "interaction_module.png",
        "word/media/image11.png": DIAGRAM_DIR / "ai_module.png",
        "word/media/image12.png": DIAGRAM_DIR / "admin_module.png",
    }
    missing = [str(p) for p in diagram_files.values() if not p.exists()]
    if missing:
        raise FileNotFoundError("Missing diagram files: " + ", ".join(missing))

    with zipfile.ZipFile(SOURCE_DOC, "r") as src_zip:
        document_xml = src_zip.read("word/document.xml")
        new_document_xml = build_document(document_xml)
        with zipfile.ZipFile(OUTPUT_DOC, "w", compression=zipfile.ZIP_DEFLATED) as dst_zip:
            for info in src_zip.infolist():
                data = src_zip.read(info.filename)
                if info.filename == "word/document.xml":
                    data = new_document_xml
                elif info.filename in diagram_files:
                    data = diagram_files[info.filename].read_bytes()
                dst_zip.writestr(info, data)

    print(OUTPUT_DOC)


if __name__ == "__main__":
    build_doc()
