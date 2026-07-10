# -*- coding: utf-8 -*-
"""
生成《毕业实习报告》Word文档
"""

from docx import Document
from docx.shared import Pt, Cm, RGBColor
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml.ns import qn
import os

doc = Document()

# 全局样式
style = doc.styles['Normal']
font = style.font
font.name = '宋体'
font.size = Pt(12)
style.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
style.paragraph_format.line_spacing = 1.5

for section in doc.sections:
    section.top_margin = Cm(2.54)
    section.bottom_margin = Cm(2.54)
    section.left_margin = Cm(3.17)
    section.right_margin = Cm(3.17)


def add_p(text, bold=False, size=12, align=None, font_name='宋体', color=None, spacing_after=0):
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.bold = bold
    run.font.size = Pt(size)
    run.font.name = font_name
    run.element.rPr.rFonts.set(qn('w:eastAsia'), font_name)
    if color:
        run.font.color.rgb = color
    if align:
        p.alignment = align
    if spacing_after:
        p.paragraph_format.space_after = Pt(spacing_after)
    return p


def add_blue(text):
    """添加蓝色占位文本"""
    return add_p(text, color=RGBColor(0, 0, 255))


def add_body(text):
    p = doc.add_paragraph()
    run = p.add_run(text)
    run.font.size = Pt(12)
    run.font.name = '宋体'
    run.element.rPr.rFonts.set(qn('w:eastAsia'), '宋体')
    p.paragraph_format.first_line_indent = Cm(0.74)
    return p


# ============================================================
# 封面
# ============================================================
for _ in range(4):
    doc.add_paragraph()

add_p('毕 业 实 习 报 告', bold=True, size=26, align=WD_ALIGN_PARAGRAPH.CENTER, font_name='黑体')
doc.add_paragraph()
add_p('实习单位：校园综合服务平台项目组', size=14, align=WD_ALIGN_PARAGRAPH.CENTER)
add_p('实习岗位：Java后端开发工程师', size=14, align=WD_ALIGN_PARAGRAPH.CENTER)
doc.add_paragraph()
add_p('学    院：____________________', size=14, align=WD_ALIGN_PARAGRAPH.CENTER)
add_p('专    业：____________________', size=14, align=WD_ALIGN_PARAGRAPH.CENTER)
add_p('班    级：____________________', size=14, align=WD_ALIGN_PARAGRAPH.CENTER)
add_p('学    号：____________________', size=14, align=WD_ALIGN_PARAGRAPH.CENTER)
add_p('姓    名：____________________', size=14, align=WD_ALIGN_PARAGRAPH.CENTER)
doc.add_paragraph()
add_p('2026年  月  日', size=14, align=WD_ALIGN_PARAGRAPH.CENTER)

doc.add_page_break()

# ============================================================
# 正文
# ============================================================
add_p('一、实习目的', bold=True, size=14, font_name='黑体', spacing_after=6)
add_body('毕业实习是高校人才培养方案中的重要实践环节，是学生将所学理论知识应用于实际项目开发的重要过程。通过本次实习，旨在：')
add_body('1. 巩固和深化在校期间所学的计算机科学与技术专业知识，特别是Java Web开发、数据库设计、前端开发等核心课程内容。')
add_body('2. 掌握企业级项目开发的完整流程，包括需求分析、系统设计、编码实现、测试部署等各阶段的工作方法。')
add_body('3. 熟悉Spring Boot、MyBatis-Plus、Vue 3等主流开发框架的实际应用，提升工程实践能力。')
add_body('4. 培养团队协作能力、问题解决能力和职业素养，为今后从事软件开发工作打下坚实基础。')
add_body('5. 体验AI辅助编程在实际项目开发中的应用，了解人工智能技术如何提升开发效率。')

doc.add_paragraph()

add_p('二、实习单位及岗位介绍', bold=True, size=14, font_name='黑体', spacing_after=6)
add_p('实习单位：', bold=True)
add_blue('请填写实习单位名称（如：XX科技有限公司）')
add_body('实习岗位：Java后端开发工程师')
add_body('实习内容：参与"校园综合服务平台"项目的全栈开发工作，主要负责后端API设计与开发、数据库设计、系统架构搭建、前后端联调测试等工作。具体包括：')
add_body('（1）基于Spring Boot 3.2搭建RESTful API后端服务，实现用户认证、公告管理、失物招领、报修工单等核心业务模块。')
add_body('（2）使用MyBatis-Plus进行数据库访问层开发，设计7张核心业务表及22个索引的数据库方案。')
add_body('（3）实现JWT Token认证机制，设计基于注解（@NoAuth/@RoleRequired）的权限控制体系。')
add_body('（4）开发滑块验证码、文件上传、天气查询等通用功能组件。')
add_body('（5）配合前端开发人员完成微信小程序和Vue管理后台的接口联调。')
add_body('（6）利用Trae IDE的AI辅助编程功能，提升代码编写效率和质量。')

doc.add_paragraph()

add_p('三、实习内容', bold=True, size=14, font_name='黑体', spacing_after=6)

add_p('3.1 项目概述', bold=True, size=12)
add_body('校园综合服务平台是一个面向高校师生的多功能服务平台，包含微信小程序学生端和Vue 3 Web管理后台。系统整合了校园公告、失物招领、设施报修、天气查询等核心服务，为师生提供一站式校园服务体验。项目采用前后端分离架构，后端基于Spring Boot 3.2 + MyBatis-Plus + Redis + MySQL构建，前端使用微信原生框架和Vue 3 + Element Plus开发。')

add_p('3.2 后端开发工作', bold=True, size=12)
add_body('（1）系统架构搭建：搭建Spring Boot 3.2项目骨架，配置MyBatis-Plus、Redis、CORS、JWT等核心组件，设计分层架构（Controller-Service-Mapper-Entity）。')
add_body('（2）数据库设计：设计7张业务表（user、announcement、lost_found、repair_order、repair_log、operation_log、system_config），共22个索引，覆盖所有高频查询场景。')
add_body('（3）认证模块开发：实现JWT Token生成与校验，设计@NoAuth和@RoleRequired注解驱动的权限控制体系，开发滑块验证码组件和邮箱验证码功能。')
add_body('（4）业务模块开发：开发公告管理、失物招领（含审核流程）、报修工单（含状态机流转）、文件上传、天气查询、系统配置、操作日志等11个Controller的完整RESTful API。')
add_body('（5）全局异常处理：实现全局异常拦截器，统一处理参数校验、业务异常、JWT异常、空指针等各类异常，确保接口返回格式统一。')

add_p('3.3 前端开发工作', bold=True, size=12)
add_body('（1）微信小程序开发：开发首页（公告轮播+天气卡片+快捷入口）、失物招领（列表浏览+发布+搜索）、报修中心（表单提交+记录查询）、个人中心（登录+信息完善）等4个核心页面。')
add_body('（2）Vue管理后台开发：开发登录页（含滑块验证码）、仪表盘（数据概览）、公告管理、失物招领管理、报修工单管理、用户管理、数据统计、系统配置、操作日志等9个功能页面。')
add_body('（3）API封装：统一封装axios请求，配置响应拦截器统一处理Token过期、业务错误提示等场景。')

add_p('3.4 AI辅助编程实践', bold=True, size=12)
add_body('在实习过程中，使用Trae IDE内置的AI编程助手（基于DeepSeek-V4-Flash模型）辅助项目开发，主要包括：')
add_body('（1）代码生成：通过自然语言描述功能需求，由AI生成对应的控制器、服务层、实体类代码。')
add_body('（2）代码审查：将已编写的代码提交给AI审查，发现潜在的安全漏洞和性能问题。')
add_body('（3）Bug修复：向AI描述错误现象和日志，快速定位问题根因并获得修复方案，如管理后台白屏修复、Redis连接失败修复等。')
add_body('（4）架构咨询：在技术选型和方案设计阶段向AI咨询最佳实践，如JWT密钥长度要求、Redis序列化安全配置等。')

doc.add_paragraph()

add_p('四、实习收获与体会', bold=True, size=14, font_name='黑体', spacing_after=6)
add_body('通过本次毕业实习，我获得了以下收获和体会：')
add_body('1. 技术能力提升：深入掌握了Spring Boot框架的核心特性和最佳实践，包括依赖注入、面向切面编程、事务管理、缓存机制等。熟练掌握了MyBatis-Plus的ORM映射、分页查询、条件构造器等高级功能。对RESTful API设计原则和JWT认证机制有了深入理解。')
add_body('2. 工程实践能力：经历了从需求分析、系统设计、编码实现到测试部署的完整项目开发流程，理解了软件工程方法论在实际项目中的应用。学会了Git版本控制和团队协作开发模式。')
add_body('3. 问题解决能力：在开发过程中遇到并解决了多个技术难题，如管理后台白屏问题、小程序重复创建用户、Redis连接失败、枚举序列化问题等，提升了分析问题和解决问题的能力。')
add_body('4. AI辅助开发：体验了AI编程助手在实际项目开发中的应用价值。AI能够快速生成高质量代码、辅助定位Bug、提供技术建议，显著提升了开发效率。但也认识到AI生成代码需要人工审查，特别是在安全性和事务一致性方面需要开发者具备判断能力。')
add_body('5. 职业素养：培养了良好的编码习惯和文档规范意识，理解了团队协作中沟通协调的重要性，为今后从事软件开发工作积累了宝贵经验。')

doc.add_paragraph()

add_p('五、实习总结', bold=True, size=14, font_name='黑体', spacing_after=6)
add_body('本次毕业实习圆满完成了预期目标。通过参与"校园综合服务平台"项目的全栈开发，我将大学期间所学的理论知识与实际项目开发相结合，全面提升了Java Web开发能力和工程实践素养。')
add_body('项目最终交付了完整的微信小程序、Vue管理后台和Spring Boot后端服务，实现了校园公告、失物招领、设施报修、天气查询等核心功能，系统功能完整、运行稳定。在开发过程中，我还积极探索了AI辅助编程这一新兴技术，积累了AI时代软件开发的新经验。')
add_body('本次实习让我深刻认识到，软件开发不仅需要扎实的技术功底，还需要良好的学习能力、沟通协作能力和问题解决能力。在今后的学习和工作中，我将继续努力，不断提升自己的专业水平和综合素质。')

doc.add_paragraph()

add_p('六、指导教师评语', bold=True, size=14, font_name='黑体', spacing_after=6)
doc.add_paragraph()
doc.add_paragraph()
add_p('指导教师签名：', size=12)
add_p('日期：    年  月  日', size=12)
doc.add_paragraph()

add_p('七、实习单位鉴定意见', bold=True, size=14, font_name='黑体', spacing_after=6)
doc.add_paragraph()
doc.add_paragraph()
add_p('实习单位盖章：', size=12)
add_p('日期：    年  月  日', size=12)

# 保存
output_path = os.path.join(os.path.dirname(os.path.abspath(__file__)), '毕业实习报告-学号-姓名.docx')
doc.save(output_path)
print(f'文档已保存至: {output_path}')