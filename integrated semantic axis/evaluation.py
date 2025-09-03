"""
语义轴准确度评估模块
提供多种方法来评估语义轴的质量和准确度
"""

import numpy as np
from embedding_model import EmbeddingModel
from semantic_axis import SemanticAxisBuilder
from projection_calculator import ProjectionCalculator


class SemanticAxisEvaluator:
    """语义轴评估器"""
    
    def __init__(self, semantic_axis):
        """
        初始化评估器
        
        Args:
            semantic_axis (np.ndarray): 语义轴向量
        """
        self.semantic_axis = semantic_axis
        self.calculator = ProjectionCalculator(semantic_axis)
        self.embedding_model = EmbeddingModel()
    
    def evaluate_with_known_labels(self, test_data):
        """
        使用已知标签的数据评估语义轴
        
        Args:
            test_data (list): [(text, label), ...] 其中label为1(相关)或0(无关)
            
        Returns:
            dict: 评估结果
        """
        predictions = []
        true_labels = []
        
        for text, label in test_data:
            projection = self.calculator.calculate_projection(text)
            # 将投影值转换为预测标签
            pred_label = 1 if projection > 0 else 0
            predictions.append(pred_label)
            true_labels.append(label)
        
        # 计算准确率
        accuracy = np.mean(np.array(predictions) == np.array(true_labels))
        
        # 计算精确率、召回率、F1分数
        tp = sum((np.array(predictions) == 1) & (np.array(true_labels) == 1))
        fp = sum((np.array(predictions) == 1) & (np.array(true_labels) == 0))
        fn = sum((np.array(predictions) == 0) & (np.array(true_labels) == 1))
        tn = sum((np.array(predictions) == 0) & (np.array(true_labels) == 0))
        
        precision = tp / (tp + fp) if (tp + fp) > 0 else 0
        recall = tp / (tp + fn) if (tp + fn) > 0 else 0
        f1 = 2 * precision * recall / (precision + recall) if (precision + recall) > 0 else 0
        
        return {
            'accuracy': accuracy,
            'precision': precision,
            'recall': recall,
            'f1_score': f1,
            'tp': tp, 'fp': fp, 'fn': fn, 'tn': tn
        }
    
    def evaluate_separation_quality(self, positive_texts, negative_texts):
        """
        评估正负样本的分离质量
        
        Args:
            positive_texts (list): 正例文本列表
            negative_texts (list): 负例文本列表
            
        Returns:
            dict: 分离质量指标
        """
        # 计算正例和负例的投影值
        pos_projections = self.calculator.calculate_projections(positive_texts)
        neg_projections = self.calculator.calculate_projections(negative_texts)
        
        # 计算统计指标
        pos_mean = np.mean(pos_projections)
        neg_mean = np.mean(neg_projections)
        pos_std = np.std(pos_projections)
        neg_std = np.std(neg_projections)
        
        # 计算分离度（两个分布之间的距离）
        separation = abs(pos_mean - neg_mean) / ((pos_std + neg_std) / 2)
        
        # 计算重叠度（两个分布的重叠程度）
        overlap = self._calculate_overlap(pos_projections, neg_projections)
        
        return {
            'positive_mean': pos_mean,
            'negative_mean': neg_mean,
            'positive_std': pos_std,
            'negative_std': neg_std,
            'separation': separation,
            'overlap': overlap,
            'positive_projections': pos_projections,
            'negative_projections': neg_projections
        }
    
    def _calculate_overlap(self, pos_projections, neg_projections):
        """计算两个分布的重叠度"""
        # 简化的重叠度计算
        pos_min, pos_max = np.min(pos_projections), np.max(pos_projections)
        neg_min, neg_max = np.min(neg_projections), np.max(neg_projections)
        
        # 计算重叠区间
        overlap_start = max(pos_min, neg_min)
        overlap_end = min(pos_max, neg_max)
        
        if overlap_start >= overlap_end:
            return 0.0
        
        # 计算重叠比例
        total_range = max(pos_max, neg_max) - min(pos_min, neg_min)
        overlap_range = overlap_end - overlap_start
        
        return overlap_range / total_range if total_range > 0 else 0.0
    
    def evaluate_consistency(self, test_texts, expected_order):
        """
        评估语义轴的一致性
        
        Args:
            test_texts (list): 测试文本列表
            expected_order (list): 期望的相关性顺序（从高到低）
            
        Returns:
            dict: 一致性评估结果
        """
        # 计算投影值
        projections = self.calculator.calculate_projections(test_texts)
        
        # 创建(文本, 投影值)对
        text_projection_pairs = list(zip(test_texts, projections))
        
        # 按投影值排序
        sorted_pairs = sorted(text_projection_pairs, key=lambda x: x[1], reverse=True)
        actual_order = [pair[0] for pair in sorted_pairs]
        
        # 计算与期望顺序的一致性
        consistency_score = self._calculate_order_consistency(actual_order, expected_order)
        
        return {
            'projections': projections,
            'actual_order': actual_order,
            'expected_order': expected_order,
            'consistency_score': consistency_score
        }
    
    def _calculate_order_consistency(self, actual_order, expected_order):
        """计算顺序一致性分数"""
        # 简化的Kendall's tau计算
        if len(actual_order) != len(expected_order):
            return 0.0
        
        # 计算逆序对数量
        inversions = 0
        n = len(actual_order)
        
        for i in range(n):
            for j in range(i + 1, n):
                # 检查是否与期望顺序一致
                actual_i = actual_order[i]
                actual_j = actual_order[j]
                expected_i_idx = expected_order.index(actual_i)
                expected_j_idx = expected_order.index(actual_j)
                
                if (i < j and expected_i_idx > expected_j_idx) or \
                   (i > j and expected_i_idx < expected_j_idx):
                    inversions += 1
        
        # 计算一致性分数
        max_inversions = n * (n - 1) / 2
        consistency = 1 - (inversions / max_inversions) if max_inversions > 0 else 1.0
        
        return consistency


def create_evaluation_dataset():
    """创建评估数据集"""
    
    # 已知标签的测试数据
    test_data = [
        # 正例（人脸相关）
        ("Analyze the facial features of the person in the picture and give suitable makeup suggestions.", 1),
        ("Give the top three celebrities who look similar to the person in the picture.", 1),
        ("Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time.", 1),
        ("What is the probability that the people in these two pictures are the same person?", 1),
        ("Analyze the age distribution of consumers in the picture and explore the reasons for this phenomenon.", 1),
        ("Analyze the person's expression and give suitable suggestions for improving their mood.", 1),
        ("Detecting facial features and analyzing the person's identity.", 1),
        ("Recognizing facial expressions and analyzing the person's emotions.", 1),
        
        # 负例（人脸无关）
        ("Summarize the text in this picture", 0),
        ("Evaluate the match between the candidate's professional abilities in this resume and our job requirements", 0),
        ("Ignore the people in the picture, extract only the text and reformat the text to generate a markdown document.", 0),
        ("Analyze whether there are any legal loopholes in the contract termination conditions.", 0),
        ("Organize the filmed class notes into clear notes and make appropriate supplements to some of the difficult points.", 0),
        ("Summarize meeting minutes", 0),
        ("Summarize the breach of contract terms from this copy of the contract.", 0),
        ("Ignore faces and describe the scenery in this picture", 0),
    ]
    
    # 用于分离质量评估的数据
    positive_texts = [
        "Analyze the facial features of the person in the picture and give suitable makeup suggestions.","Give the top three celebrities who look similar to the person in the picture.", 
        "Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time.", "What is the probability that the people in these two pictures are the same person?", 
        "Analyze the age distribution of consumers in the picture and explore the reasons for this phenomenon.", "Analyze the person's expression and give suitable suggestions for improving their mood.", 
        "Detecting facial features and analyzing the person's identity.", "Recognizing facial expressions and analyzing the person's emotions."]
    
    negative_texts = [
        "Summarize the text in this picture", "Evaluate the match between the candidate's professional abilities in this resume and our job requirements", 
        "Ignore the people in the picture, extract only the text and reformat the text to generate a markdown document.", "Analyze whether there are any legal loopholes in the contract termination conditions.", 
        "Organize the filmed class notes into clear notes and make appropriate supplements to some of the difficult points.", "Summarize meeting minutes", 
        "Summarize the breach of contract terms from this copy of the contract.", "Ignore faces and describe the scenery in this picture"
    ]
    
    # 用于一致性评估的数据
    consistency_texts = [
        "Detecting facial features and analyzing the person's identity.",      # 应该最高相关
        "What is the probability that the people in these two pictures are the same person?",      # 应该高相关
        "Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time.",          # 应该高相关
        "Evaluate the match between the candidate's professional abilities in this resume and our job requirements",          # 应该低相关
        "Analyze whether there are any legal loopholes in the contract termination conditions.",         # 应该低相关
        "Ignore the people in the picture, extract only the text and reformat the text to generate a markdown document."          # 应该最低相关
    ]
    
    expected_order = [
        "Detecting facial features and analyzing the person's identity.",
        "What is the probability that the people in these two pictures are the same person?", 
        "Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time.",
        "Evaluate the match between the candidate's professional abilities in this resume and our job requirements",
        "Analyze whether there are any legal loopholes in the contract termination conditions.",
        "Ignore the people in the picture, extract only the text and reformat the text to generate a markdown document."
    ]
    
    return test_data, positive_texts, negative_texts, consistency_texts, expected_order


def run_comprehensive_evaluation():
    """运行综合评估"""
    
    print("语义轴综合评估")
    print("=" * 50)
    
    # 构建语义轴
    builder = SemanticAxisBuilder()
    try:
        semantic_axis = builder.load_semantic_axis()
    except FileNotFoundError:
        print("语义轴文件不存在，正在构建...")
        semantic_axis = builder.build_semantic_axis()
    
    # 创建评估器
    evaluator = SemanticAxisEvaluator(semantic_axis)
    
    # 创建评估数据
    test_data, positive_texts, negative_texts, consistency_texts, expected_order = create_evaluation_dataset()
    
    # 1. 已知标签评估
    print("\n1. 已知标签评估")
    print("-" * 30)
    label_results = evaluator.evaluate_with_known_labels(test_data)
    print(f"准确率: {label_results['accuracy']:.4f}")
    print(f"精确率: {label_results['precision']:.4f}")
    print(f"召回率: {label_results['recall']:.4f}")
    print(f"F1分数: {label_results['f1_score']:.4f}")
    
    # 2. 分离质量评估
    print("\n2. 分离质量评估")
    print("-" * 30)
    separation_results = evaluator.evaluate_separation_quality(positive_texts, negative_texts)
    print(f"正例平均投影值: {separation_results['positive_mean']:.4f}")
    print(f"负例平均投影值: {separation_results['negative_mean']:.4f}")
    print(f"分离度: {separation_results['separation']:.4f}")
    print(f"重叠度: {separation_results['overlap']:.4f}")
    
    # 3. 一致性评估
    print("\n3. 一致性评估")
    print("-" * 30)
    consistency_results = evaluator.evaluate_consistency(consistency_texts, expected_order)
    print(f"一致性分数: {consistency_results['consistency_score']:.4f}")
    print("实际排序:")
    for i, text in enumerate(consistency_results['actual_order']):
        print(f"  {i+1}. {text}")
    
    # 4. 综合评分
    print("\n4. 综合评分")
    print("-" * 30)
    
    # 计算综合分数
    accuracy_score = label_results['accuracy']
    separation_score = min(separation_results['separation'] / 2.0, 1.0)  # 归一化
    consistency_score = consistency_results['consistency_score']
    overlap_penalty = separation_results['overlap']
    
    overall_score = (accuracy_score + separation_score + consistency_score) / 3 - overlap_penalty * 0.2
    overall_score = max(0, min(1, overall_score))  # 限制在[0,1]范围内
    
    print(f"综合评分: {overall_score:.4f}")
    
    # 评分解释
    if overall_score > 0.8:
        print("语义轴质量: 优秀")
    elif overall_score > 0.6:
        print("语义轴质量: 良好")
    elif overall_score > 0.4:
        print("语义轴质量: 一般")
    else:
        print("语义轴质量: 需要改进")
    
    return {
        'label_results': label_results,
        'separation_results': separation_results,
        'consistency_results': consistency_results,
        'overall_score': overall_score
    }


if __name__ == "__main__":
    run_comprehensive_evaluation() 