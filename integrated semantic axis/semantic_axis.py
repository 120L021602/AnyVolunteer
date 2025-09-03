"""
语义轴构建模块
构建人脸关联度语义轴
"""

import numpy as np
import os
from embedding_model import EmbeddingModel
from config import POSITIVE_EXAMPLES_FILE, NEGATIVE_EXAMPLES_FILE, SEMANTIC_AXIS_FILE


class SemanticAxisBuilder:
    """语义轴构建器"""
    
    def __init__(self):
        """初始化语义轴构建器"""
        self.embedding_model = EmbeddingModel()
        self.semantic_axis = None
    
    def load_examples(self, file_path):
        """
        从文件加载示例文本
        
        Args:
            file_path (str): 文件路径
            
        Returns:
            list: 文本列表
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"文件不存在: {file_path}")
        
        with open(file_path, 'r', encoding='utf-8') as f:
            texts = [line.strip() for line in f if line.strip()]
        
        return texts
    
    def build_semantic_axis(self):
        """
        构建人脸关联度语义轴
        
        Returns:
            np.ndarray: 语义轴向量
        """
        # 加载正例和负例
        positive_texts = self.load_examples(POSITIVE_EXAMPLES_FILE)
        negative_texts = self.load_examples(NEGATIVE_EXAMPLES_FILE)
        
        print(f"加载了 {len(positive_texts)} 个正例")
        print(f"加载了 {len(negative_texts)} 个负例")
        
        # 计算正例和负例的平均嵌入
        positive_embedding = self.embedding_model.get_average_embedding(positive_texts)
        negative_embedding = self.embedding_model.get_average_embedding(negative_texts)
        
        # 构建语义轴：正例嵌入 - 负例嵌入
        self.semantic_axis = positive_embedding - negative_embedding
        
        print(f"语义轴构建完成，维度: {self.semantic_axis.shape}")
        
        return self.semantic_axis
    
    def save_semantic_axis(self):
        """
        保存语义轴到文件
        
        Args:
            axis_vector (np.ndarray): 语义轴向量
        """
        if self.semantic_axis is None:
            raise ValueError("语义轴尚未构建，请先调用 build_semantic_axis()")
        
        # 确保目录存在
        os.makedirs(os.path.dirname(SEMANTIC_AXIS_FILE), exist_ok=True)
        
        np.save(SEMANTIC_AXIS_FILE, self.semantic_axis)
        print(f"语义轴已保存到: {SEMANTIC_AXIS_FILE}")
    
    def load_semantic_axis(self):
        """
        从文件加载语义轴
        
        Returns:
            np.ndarray: 语义轴向量
        """
        if not os.path.exists(SEMANTIC_AXIS_FILE):
            raise FileNotFoundError(f"语义轴文件不存在: {SEMANTIC_AXIS_FILE}")
        
        self.semantic_axis = np.load(SEMANTIC_AXIS_FILE)
        print(f"语义轴已加载，维度: {self.semantic_axis.shape}")
        
        return self.semantic_axis 