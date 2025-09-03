"""
SBERT模型封装
提供文本嵌入功能
"""

import numpy as np
from sentence_transformers import SentenceTransformer
from config import SBERT_MODEL_NAME


class EmbeddingModel:
    """SBERT模型封装类"""
    
    def __init__(self, model_name=SBERT_MODEL_NAME):
        """
        初始化SBERT模型
        
        Args:
            model_name (str): SBERT模型名称
        """
        self.model = SentenceTransformer(model_name)
    
    def get_embedding(self, text):
        """
        获取单个文本的嵌入向量
        
        Args:
            text (str): 输入文本
            
        Returns:
            np.ndarray: 文本的嵌入向量
        """
        embedding = self.model.encode(text)
        return embedding
    
    def get_embeddings(self, texts):
        """
        获取多个文本的嵌入向量
        
        Args:
            texts (list): 输入文本列表
            
        Returns:
            np.ndarray: 文本嵌入向量数组
        """
        embeddings = self.model.encode(texts)
        return embeddings
    
    def get_average_embedding(self, texts):
        """
        获取多个文本的平均嵌入向量
        
        Args:
            texts (list): 输入文本列表
            
        Returns:
            np.ndarray: 平均嵌入向量
        """
        embeddings = self.get_embeddings(texts)
        return np.mean(embeddings, axis=0) 