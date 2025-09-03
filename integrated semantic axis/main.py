"""
主程序入口
整合所有功能模块，提供完整的语义轴构建和投影计算功能
"""

import os
from semantic_axis import SemanticAxisBuilder
from projection_calculator import ProjectionCalculator


def create_sample_data():
    """创建示例数据文件"""
    # 创建data目录
    os.makedirs("data", exist_ok=True)
    
    # 正例：与人脸高度相关的指令
    positive_examples = [
        "识别这个人的身份",
        "分析这个人的表情",
        "检测人脸特征",
        "识别这个人是谁",
        "分析面部表情",
        "提取人脸信息",
        "识别面部特征",
        "分析人物身份",
        "检测人脸",
        "识别面部表情"
    ]
    
    # 负例：与人脸无关的指令
    negative_examples = [
        "总结PPT内容",
        "分析文档结构",
        "提取文本信息",
        "识别物体",
        "分析图表数据",
        "总结会议记录",
        "提取关键信息",
        "分析文档格式",
        "识别文字内容",
        "总结报告要点"
    ]
    
    # 保存正例
    with open("data/positive_examples.txt", "w", encoding="utf-8") as f:
        for example in positive_examples:
            f.write(example + "\n")
    
    # 保存负例
    with open("data/negative_examples.txt", "w", encoding="utf-8") as f:
        for example in negative_examples:
            f.write(example + "\n")
    
    print("示例数据已创建")


def build_and_save_semantic_axis():
    """构建并保存语义轴"""
    print("开始构建语义轴...")
    
    builder = SemanticAxisBuilder()
    semantic_axis = builder.build_semantic_axis()
    builder.save_semantic_axis()
    
    print("语义轴构建完成并已保存")
    return semantic_axis


def test_projection_calculation(semantic_axis):
    """测试投影计算功能"""
    print("\n开始测试投影计算...")
    
    calculator = ProjectionCalculator(semantic_axis)
    
    # 测试用例
    test_prompts = [
        "Ignore portraits and analyze text formatting in this historical document scan",
        "Analyze the age distribution of consumers in the picture and explore the reasons for this phenomenon",
        "Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time",
        "Describe the picture based on the actions of the people in it",
        "Extract text from images for summary",
        "Just summarize the text in the picture and write a 500-word report using more academic language."
    ]
    
    print("测试结果:")
    print("-" * 50)
    
    for prompt in test_prompts:
        projection_score = calculator.calculate_projection(prompt)
        interpretation = calculator.interpret_projection(projection_score)
        
        print(f"Prompt: {prompt}")
        print(f"投影值: {projection_score:.4f}")
        print(f"解释: {interpretation}")
        print("-" * 50)


def interactive_mode(semantic_axis):
    """交互模式，用户可以输入prompt进行测试"""
    print("\n进入交互模式，输入 'quit' 退出")
    print("-" * 50)
    
    calculator = ProjectionCalculator(semantic_axis)
    
    while True:
        user_input = input("请输入您的prompt: ").strip()
        
        if user_input.lower() == 'quit':
            break
        
        if not user_input:
            continue
        
        try:
            projection_score = calculator.calculate_projection(user_input)
            interpretation = calculator.interpret_projection(projection_score)
            
            print(f"投影值: {projection_score:.4f}")
            print(f"解释: {interpretation}")
            print("-" * 50)
            
        except Exception as e:
            print(f"计算出错: {e}")


def main():
    """主函数"""
    print("人脸关联度语义轴构建和投影计算系统")
    print("=" * 50)
    
    # 检查数据文件是否存在，如果不存在则创建示例数据
    if not os.path.exists("data/positive_examples.txt") or not os.path.exists("data/negative_examples.txt"):
        print("未找到示例数据文件，正在创建...")
        create_sample_data()
    
    # 检查语义轴文件是否存在
    if not os.path.exists("data/semantic_axis.npy"):
        print("未找到语义轴文件，正在构建...")
        semantic_axis = build_and_save_semantic_axis()
    else:
        print("加载已存在的语义轴...")
        builder = SemanticAxisBuilder()
        semantic_axis = builder.load_semantic_axis()
    
    # 测试投影计算
    test_projection_calculation(semantic_axis)
    
    # 进入交互模式
    interactive_mode(semantic_axis)
    
    print("程序结束")


if __name__ == "__main__":
    main() 