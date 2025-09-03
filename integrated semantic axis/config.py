"""
配置文件
定义项目中的各种配置参数
"""

# SBERT模型配置
SBERT_MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"

# 数据文件路径
POSITIVE_EXAMPLES_FILE = "data/positive_examples.txt"
NEGATIVE_EXAMPLES_FILE = "data/negative_examples.txt"

# 语义轴文件路径
SEMANTIC_AXIS_FILE = "data/semantic_axis.npy" 