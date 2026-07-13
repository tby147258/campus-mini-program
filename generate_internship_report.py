# -*- coding: utf-8 -*-
"""生成毕业实习报告 - 按学生02模板格式"""
from docx import Document
from docx.shared import Pt, Cm, RGBColor, Inches
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.oxml.ns import qn, nsdecls
from docx.oxml import parse_xml
import os

doc = Document()

# ========== 全局样式设置 ==========
style = doc.styles['Normal']
font = style.font
font.name = 'Times New Roman'
font.size = Pt(12)  # 小四号
style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
style.paragraph_format.line_spacing = 1.0

# 页边距
for section in doc.sections:
    section.top_margin = Cm(2.54)
    section.bottom_margin = Cm(2.54)
    section.left_margin = Cm(3.00)
    section.right_margin = Cm(2.00)
    section.header_distance = Cm(1.50)
    section.footer_distance = Cm(1.75)

BLUE = RGBColor(0, 0, 255)
BLACK = RGBColor(0, 0, 0)


def set_run_font(run, font_name='宋体', size=Pt(12), bold=False, color=None, western='Times New Roman'):
    run.font.name = western
    run.font.size = size
    run.bold = bold
    run.element.rPr.rFonts.set(qn('w:eastAsia'), font_name)
    if color:
        run.font.color.rgb = color


def add_paragraph_with_text(text, font_name='宋体', size=Pt(12), bold=False, align=None, color=None, western='Times New Roman', first_line_indent=None, spacing=0):
    p = doc.add_paragraph()
    run = p.add_run(text)
    set_run_font(run, font_name, size, bold, color, western)
    if align:
        p.alignment = align
    if first_line_indent:
        p.paragraph_format.first_line_indent = first_line_indent
    if spacing:
        p.paragraph_format.space_after = Pt(spacing)
        p.paragraph_format.space_before = Pt(spacing)
    return p


def add_empty_line():
    p = doc.add_paragraph()
    run = p.add_run('')
    run.font.size = Pt(12)
    return p


def add_title(text, size=Pt(22), bold=True):  # 二号=22pt
    p = add_paragraph_with_text(text, size=size, bold=bold, align=WD_ALIGN_PARAGRAPH.CENTER)

def add_main_title(text, size=Pt(16), bold=True):  # 三号=16pt
    p = add_paragraph_with_text(text, size=size, bold=bold, align=WD_ALIGN_PARAGRAPH.CENTER)

def add_section_title(text, size=Pt(14), bold=True):  # 四号=14pt
    p = add_paragraph_with_text(text, size=size, bold=bold, align=WD_ALIGN_PARAGRAPH.LEFT)

def add_sub_section_title(text, size=Pt(12), bold=True):  # 小四号=12pt
    p = add_paragraph_with_text(text, size=size, bold=bold, align=WD_ALIGN_PARAGRAPH.LEFT)

def add_body(text, indent=True):
    p = add_paragraph_with_text(text, size=Pt(12), bold=False, first_line_indent=Cm(0.74) if indent else None)

def add_blue(text, size=Pt(12), bold=False, indent=True):
    p = add_paragraph_with_text(text, size=size, bold=bold, color=BLUE, first_line_indent=Cm(0.74) if indent else None)


# ========== 封面页 ==========
for _ in range(3):
    add_empty_line()

add_title('毕业实习报告', size=Pt(22), bold=True)

for _ in range(3):
    add_empty_line()

# 封面信息
cover_items = [
    ('实习单位：', ''),
    ('实习时间：', '              至              '),
    ('系  (部)：', '软件工程学院'),
    ('专    业：', '软件工程'),
    ('学生姓名：', '              学号：'),
]

for label, value in cover_items:
    p = doc.add_paragraph()
    p.alignment = WD_ALIGN_PARAGRAPH.LEFT
    p.paragraph_format.first_line_indent = Cm(4)
    run_label = p.add_run(label)
    set_run_font(run_label, size=Pt(14), bold=False)
    if value:
        run_val = p.add_run(value)
        set_run_font(run_val, size=Pt(14), bold=False, color=BLUE)
    else:
        # 空白下划线
        run_blank = p.add_run('_' * 30)
        set_run_font(run_blank, size=Pt(14), bold=False, color=BLUE)

add_empty_line()
add_blue('注意：打印前将所有蓝色字体删除。', size=Pt(12), bold=True, indent=False)
add_blue('封面落款时间尽量2026年8月以后', size=Pt(12), bold=True, indent=False)
add_empty_line()
add_paragraph_with_text('年    月    日', size=Pt(14), align=WD_ALIGN_PARAGRAPH.CENTER)

# ========== 分页：实习报告撰写要求 ==========
doc.add_page_break()

add_section_title('实习报告撰写要求', size=Pt(14), bold=True)

add_body('实习报告在实习的基础上完成，运用基础理论知识结合实习资料，进行比较深入的分析、总结。实习报告内容要求实事求是，简明扼要，能反映出实习单位的情况及本人实习的情况、体会和感受。报告的资料必须真实可靠，有独立的见解，重点突出、条理清晰，字数3000字左右。')

add_sub_section_title('一、实习报告正文内容必须与所学专业内容相关并包含以下四个方面：')
add_body('1、实习目的：要求言简意赅，点明主题。')
add_body('2、实习单位及岗位介绍：要求详略得当、重点突出，着重介绍实习岗位的介绍。')
add_body('3、实习内容及过程：要求内容详实、层次清楚；侧重实际动手能力和技能的培养、锻炼和提高，但切忌记帐式或日记式的简单罗列。')
add_body('4、实习总结及体会：要求条理清楚、逻辑性强；着重写出对实习内容的总结、体会和感受，特别是自己所学的专业理论与实践的差距和今后应努力的方向。')

add_sub_section_title('二、实习报告文字打印格式和装订要求')
add_body('1、实习报告一律要使用A4纸打印成文；')
add_body('2、字间距设置为"标准"；')
add_body('3、段落设置为"单倍行间距"；')
add_body('4、字号设置为：')
add_body('a) 标题：宋体二号加粗；')
add_body('b) 正文一级标题：宋体四号加粗；')
add_body('c) 正文二级标题：宋体小四号加粗；')
add_body('d) 其余汉字均为宋体小四号；')
add_body('e) 正文中所有非汉字均为Times New Roman体；')
add_body('5、页边距：上 2.54cm 下 2.54cm 左 3.00cm 右 2.00cm')
add_body('页眉：1.50cm 页脚：1.75cm 页码置于右下角')
add_body('6、实习报告最后统一用学院提供的毕业实习报告封面装订成册，报告纸样本可从教务处网页下载。')

# ========== 分页：实习考核表 ==========
doc.add_page_break()

add_section_title('实习考核表', size=Pt(14), bold=True)

# 基本信息表
table1 = doc.add_table(rows=4, cols=6)
table1.alignment = WD_TABLE_ALIGNMENT.CENTER

# Set table borders
tbl = table1._tbl
tblPr = tbl.tblPr if tbl.tblPr is not None else parse_xml(f'<w:tblPr {nsdecls("w")}/>')
borders = parse_xml(
    f'<w:tblBorders {nsdecls("w")}>'
    '  <w:top w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '  <w:left w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '  <w:bottom w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '  <w:right w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '  <w:insideH w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '  <w:insideV w:val="single" w:sz="4" w:space="0" w:color="000000"/>'
    '</w:tblBorders>'
)
tblPr.append(borders)

# Row 1: 学生姓名, 专业班级, 学生电话
cells_r1 = table1.rows[0].cells
for c in cells_r1:
    c.paragraphs[0].alignment = WD_ALIGN_PARAGRAPH.CENTER
labels_r1 = ['学生姓名', '专业班级', '学生电话', '实习单位', '电话', '通讯地址']
data_r1 = ['', '软件工程', '', '（实习单位名称）', '', '（实习单位地址）']
for i in range(6):
    if i % 2 == 0:  # label
        run = cells_r1[i].paragraphs[0].add_run(labels_r1[i])
    else:  # value
        run = cells_r1[i].paragraphs[0].add_run(data_r1[i])
    set_run_font(run, size=Pt(10))

# Merge cells for row 2 (通讯地址 and 邮编)
table1.rows[1].cells[0].merge(table1.rows[1].cells[3])
cell_addr = table1.rows[1].cells[0]
p_addr = cell_addr.paragraphs[0]
p_addr.alignment = WD_ALIGN_PARAGRAPH.LEFT
run_addr = p_addr.add_run('通讯地址：')
set_run_font(run_addr, size=Pt(10))
run_addr_val = p_addr.add_run('（实习单位通讯地址）')
set_run_font(run_addr_val, size=Pt(10), color=BLUE)

cell_zip = table1.rows[1].cells[4]
cell_zip.merge(table1.rows[1].cells[5])
p_zip = cell_zip.paragraphs[0]
run_zip = p_zip.add_run('邮编：')
set_run_font(run_zip, size=Pt(10))
run_zip_val = p_zip.add_run('（邮编）')
set_run_font(run_zip_val, size=Pt(10), color=BLUE)

# Row 2: 实习任务
cell_task = table1.rows[2].cells[0]
cell_task.merge(table1.rows[2].cells[5])
p_task = cell_task.paragraphs[0]
run_task = p_task.add_run('实习任务：由公司的工程师/项目经理引导学生一起填写。同一方向，可以一样。')
set_run_font(run_task, size=Pt(10), color=BLUE)

# Row 3: 考勤记录
cell_att = table1.rows[3].cells[0]
cell_att.merge(table1.rows[3].cells[1])
p_att = cell_att.paragraphs[0]
p_att.alignment = WD_ALIGN_PARAGRAPH.CENTER
run_att = p_att.add_run('考勤记录')
set_run_font(run_att, size=Pt(10), bold=True)

# Add attendance table
doc.add_paragraph()  # space
add_blue('考勤记录表（根据实际出勤情况填写，出勤打✔，缺勤打✕）', size=Pt(10), indent=False)

# ========== 分页：实习内容 ==========
doc.add_page_break()

# 正文内容
add_section_title('一、实习目的', size=Pt(14), bold=True)

add_sub_section_title('1、实习的时间', size=Pt(12), bold=True)
add_blue('（请填写实习起止时间，例如：2026年7月1日至2026年8月31日）', size=Pt(12), indent=True)

add_sub_section_title('2、实习的目的', size=Pt(12), bold=True)
add_blue('（请阐述本次实习的目的，包括：将所学理论知识与实际工作相结合、了解软件工程在实际项目中的应用、提高实践动手能力、培养团队协作和沟通能力、为今后就业打下基础等。字数不少于300字。）', size=Pt(12), indent=True)

add_empty_line()
add_section_title('二、实习单位及岗位介绍', size=Pt(14), bold=True)

add_sub_section_title('1、实习单位', size=Pt(12), bold=True)
add_blue('（请介绍实习单位的全称、所在行业、主营业务、规模、发展历程等基本信息。字数不少于300字。）', size=Pt(12), indent=True)

add_sub_section_title('2、实习岗位', size=Pt(12), bold=True)
add_blue('（请介绍实习岗位的名称、职责、所需技能、工作内容等。着重介绍岗位的具体要求和工作内容。字数不少于300字。）', size=Pt(12), indent=True)

add_empty_line()
add_section_title('三、实习内容及过程', size=Pt(14), bold=True)
add_blue('（请详细描述实习期间参与的具体项目、完成的工作任务、使用的技术工具、遇到的问题及解决方案、取得的成果等。内容详实、层次清楚，侧重实际动手能力和技能的培养、锻炼和提高，切忌记帐式或日记式的简单罗列。字数不少于800字。）', size=Pt(12), indent=True)

add_empty_line()
add_section_title('四、实习总结及体会', size=Pt(14), bold=True)

add_sub_section_title('1、实习总结', size=Pt(12), bold=True)
add_blue('（请对实习期间的工作和收获进行总结，分析专业理论与实践的差距，以及今后应努力的方向。字数不少于300字。）', size=Pt(12), indent=True)

add_sub_section_title('2、体会', size=Pt(12), bold=True)

add_sub_section_title('（1）社会责任感与工程伦理', size=Pt(12), bold=True)
add_blue('必须结合实习中的具体事例分析：你是否遇到过或可能遇到用户数据泄露、算法公平性、系统安全性等伦理问题？你是如何处理的？如果未遇到，谈谈你将在未来如何预防。')
add_blue('阐述你对"工程师对公众的安全、健康和福祉的社会责任"的理解，并引用实习单位的相关规章制度或行业规范（如ISO 27001、软件工程职业道德准则）。')
add_blue('（字数不少于400字。）', size=Pt(12), bold=True, indent=True)

add_sub_section_title('（2）终身学习与新技术跟踪', size=Pt(12), bold=True)
add_blue('列出你在实习期间主动学习的新框架/工具/语言（至少2项），说明学习方式（看文档、做小实验、向同事请教等）。')
add_blue('分析技术迭代对你负责模块的影响（例如：原本计划用技术A，但中途发现技术B更适合，你如何快速学习切换）。')
add_blue('制定未来6个月的学习计划，包括具体的学习资源（书籍、课程、社区）、时间安排和预期成果（如考取证书、贡献开源项目）。')
add_blue('（字数不少于400字。）', size=Pt(12), bold=True, indent=True)

# ========== 实习纪律 ==========
doc.add_page_break()
add_section_title('实习纪律', size=Pt(14), bold=True)

disciplines = [
    '1.严格遵守国家法令，遵守学校和实习单位的有关规章制度，尊重实习单位的领导和职工，虚心学习。',
    '2.服从领导，听从指挥，不迟到、不早退、不旷实习、不擅离职守。实习期间一般不得请假。',
    '3.团结友爱，文明礼貌，严禁酗酒闹事、打架斗殴以及其他不文明行为。',
    '4.严格遵守保密制度，不遗失和损坏保密文档。',
    '5.爱护公共财物，不得擅自动用实习单位的仪器设备和实习用品。',
    '6.严格遵守操作规程和安全制度。',
    '7.注意交通安全，遵守交通规则，防止交通事故。未经批准，学生一律不准离队单独活动，不准离队外宿，更不得到无安全防护措施的水域中游泳。',
    '8.要培养勤俭节约的优良习惯，不浪费水电，不准私自使用电炉、煤炉等。',
    '9.凡违反上述规定造成个人人身安全事故和损失的，由个人负责。造成集体和国家损失的视情节轻重，按照学院和单位规定或国家有关法纪、法规处理。',
]
for d in disciplines:
    add_body(d)

# ========== 实习报告评阅人 ==========
doc.add_page_break()
add_section_title('实习报告评阅人', size=Pt(14), bold=True)
add_blue('公司的工程师/项目经理根据每个学生具体情况填写。主要是学生的实习参与情况、具体做了哪个项目，采用了什么技术手段和过程等，不要写优秀、良好等主观评价内容。', size=Pt(12), indent=True)
add_empty_line()
add_blue('（此处填写评阅意见，由实习单位工程师/项目经理填写）', size=Pt(12), indent=True)
add_empty_line()
add_blue('公司填写成绩(具体分数）和公司评阅人签名', size=Pt(12), bold=True, indent=False)
add_empty_line()
add_paragraph_with_text('评阅成绩：                    评阅人（签名）：             ', size=Pt(12))
add_paragraph_with_text('年    月    日', size=Pt(12), align=WD_ALIGN_PARAGRAPH.RIGHT)

# ========== 成绩认定 ==========
add_empty_line()
add_section_title('成  绩  认  定', size=Pt(14), bold=True)
add_blue('此处成绩认定由学院实习管理老师填写！', size=Pt(12), bold=True, indent=False)
add_empty_line()
add_paragraph_with_text('评定成绩 ：________________', size=Pt(12))
add_paragraph_with_text('负责人(签名)：________________', size=Pt(12))
add_paragraph_with_text('年    月    日', size=Pt(12), align=WD_ALIGN_PARAGRAPH.RIGHT)

# 页眉
for section in doc.sections:
    header = section.header
    header.is_linked_to_previous = False
    hp = header.paragraphs[0]
    hp.alignment = WD_ALIGN_PARAGRAPH.CENTER
    run = hp.add_run('成都信息工程大学 ChengDu University Of Information Technology             学生实习报告用纸')
    set_run_font(run, size=Pt(9), color=RGBColor(128, 128, 128))

# 保存
output_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), '毕业实习报告-学号-姓名.docx')
doc.save(output_path)
print(f'文档已保存至: {output_path}')