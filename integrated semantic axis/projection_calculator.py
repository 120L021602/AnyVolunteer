"""
投影计算模块
计算用户prompt在语义轴上的投影值
"""

import numpy as np
from embedding_model import EmbeddingModel


class ProjectionCalculator:
    """投影计算器"""
    
    def __init__(self, semantic_axis):
        """
        初始化投影计算器
        
        Args:
            semantic_axis (np.ndarray): 语义轴向量
        """
        self.embedding_model = EmbeddingModel()
        self.semantic_axis = semantic_axis
        self.axis_norm = np.linalg.norm(semantic_axis)
    
    def calculate_projection(self, text):
        """
        计算文本在语义轴上的投影值
        
        Args:
            text (str): 输入文本
            
        Returns:
            float: 投影值
        """
        # 获取文本的嵌入向量
        query_embedding = self.embedding_model.get_embedding(text)
        
        # 计算投影值：点积除以轴向量的模
        projection_score = np.dot(query_embedding, self.semantic_axis) / self.axis_norm
        
        return projection_score
    
    def calculate_projections(self, texts):
        """
        计算多个文本在语义轴上的投影值
        
        Args:
            texts (list): 输入文本列表
            
        Returns:
            list: 投影值列表
        """
        # 获取所有文本的嵌入向量
        query_embeddings = self.embedding_model.get_embeddings(texts)
        
        # 计算所有投影值
        projection_scores = []
        for embedding in query_embeddings:
            projection_score = np.dot(embedding, self.semantic_axis) / self.axis_norm
            projection_scores.append(projection_score)
        
        return projection_scores
    
    def interpret_projection(self, projection_score):
        """
        解释投影值的含义
        
        Args:
            projection_score (float): 投影值
            
        Returns:
            str: 解释文本
        """
        if projection_score > 0.1:
            return "高度相关：任务与人脸信息高度相关，需要保留较多面部信息"
        elif projection_score > 0:
            return "中度相关：任务与人脸信息有一定相关性，需要适度保护"
        elif projection_score > -0.1:
            return "低度相关：任务与人脸信息相关性较低，可以严格打码"
        else:
            return "无关：任务与人脸信息无关，可以完全打码" 