"""
Integrated Semantic Axis System
Integrates all functional modules, providing complete semantic axis construction, projection calculation, and evaluation functions.
"""

import numpy as np
import os
from sentence_transformers import SentenceTransformer

# ==================== Configuration ====================

# SBERT model configuration
SBERT_MODEL_NAME = "sentence-transformers/all-MiniLM-L6-v2"

# Data file paths
POSITIVE_EXAMPLES_FILE = "data/positive_examples.txt"
NEGATIVE_EXAMPLES_FILE = "data/negative_examples.txt"

# Semantic axis file path
SEMANTIC_AXIS_FILE = "data/semantic_axis.npy"

# ==================== Embedding Model ====================

class EmbeddingModel:
    """SBERT model wrapper class"""
    
    def __init__(self, model_name=SBERT_MODEL_NAME):
        """
        Initialize SBERT model
        
        Args:
            model_name (str): SBERT model name
        """
        self.model = SentenceTransformer(model_name)
    
    def get_embedding(self, text):
        """
        Get the embedding vector of a single text
        
        Args:
            text (str): Input text
            
        Returns:
            np.ndarray: Embedding vector of the text
        """
        embedding = self.model.encode(text)
        return embedding
    
    def get_embeddings(self, texts):
        """
        Get the embedding vectors of multiple texts
        
        Args:
            texts (list): List of input texts
            
        Returns:
            np.ndarray: Array of text embedding vectors
        """
        embeddings = self.model.encode(texts)
        return embeddings
    
    def get_average_embedding(self, texts):
        """
        Get the average embedding vector of multiple texts
        
        Args:
            texts (list): List of input texts
            
        Returns:
            np.ndarray: Average embedding vector
        """
        embeddings = self.get_embeddings(texts)
        return np.mean(embeddings, axis=0)

# ==================== Semantic Axis Construction ====================

class SemanticAxisBuilder:
    """Semantic axis builder"""
    
    def __init__(self):
        """Initialize semantic axis builder"""
        self.embedding_model = EmbeddingModel()
        self.semantic_axis = None
    
    def load_examples(self, file_path):
        """
        Load example texts from file
        
        Args:
            file_path (str): File path
            
        Returns:
            list: List of texts
        """
        if not os.path.exists(file_path):
            raise FileNotFoundError(f"File not found: {file_path}")
        
        with open(file_path, 'r', encoding='utf-8') as f:
            texts = [line.strip() for line in f if line.strip()]
        
        return texts
    
    def build_semantic_axis(self):
        """
        Build the semantic axis for face relevance
        
        Returns:
            np.ndarray: Semantic axis vector
        """
        # Load positive and negative examples
        positive_texts = self.load_examples(POSITIVE_EXAMPLES_FILE)
        negative_texts = self.load_examples(NEGATIVE_EXAMPLES_FILE)
        
        print(f"Loaded {len(positive_texts)} positive examples")
        print(f"Loaded {len(negative_texts)} negative examples")
        
        # Calculate average embeddings
        positive_embedding = self.embedding_model.get_average_embedding(positive_texts)
        negative_embedding = self.embedding_model.get_average_embedding(negative_texts)
        
        # Build semantic axis: positive - negative
        self.semantic_axis = positive_embedding - negative_embedding
        
        print(f"Semantic axis built, dimension: {self.semantic_axis.shape}")
        
        return self.semantic_axis
    
    def save_semantic_axis(self):
        """
        Save semantic axis to file
        """
        if self.semantic_axis is None:
            raise ValueError("Semantic axis not built yet, please call build_semantic_axis() first")
        
        # Ensure directory exists
        os.makedirs(os.path.dirname(SEMANTIC_AXIS_FILE), exist_ok=True)
        
        np.save(SEMANTIC_AXIS_FILE, self.semantic_axis)
        print(f"Semantic axis saved to: {SEMANTIC_AXIS_FILE}")
    
    def load_semantic_axis(self):
        """
        Load semantic axis from file
        
        Returns:
            np.ndarray: Semantic axis vector
        """
        if not os.path.exists(SEMANTIC_AXIS_FILE):
            raise FileNotFoundError(f"Semantic axis file not found: {SEMANTIC_AXIS_FILE}")
        
        self.semantic_axis = np.load(SEMANTIC_AXIS_FILE)
        print(f"Semantic axis loaded, dimension: {self.semantic_axis.shape}")
        
        return self.semantic_axis

# ==================== Projection Calculation ====================

class ProjectionCalculator:
    """Projection calculator"""
    
    def __init__(self, semantic_axis):
        """
        Initialize projection calculator
        
        Args:
            semantic_axis (np.ndarray): Semantic axis vector
        """
        self.embedding_model = EmbeddingModel()
        self.semantic_axis = semantic_axis
        self.axis_norm = np.linalg.norm(semantic_axis)
    
    def calculate_projection(self, text):
        """
        Calculate the projection value of text on the semantic axis
        
        Args:
            text (str): Input text
            
        Returns:
            float: Projection value
        """
        query_embedding = self.embedding_model.get_embedding(text)
        projection_score = np.dot(query_embedding, self.semantic_axis) / self.axis_norm
        return projection_score
    
    def calculate_projections(self, texts):
        """
        Calculate the projection values of multiple texts on the semantic axis
        
        Args:
            texts (list): List of input texts
            
        Returns:
            list: List of projection values
        """
        query_embeddings = self.embedding_model.get_embeddings(texts)
        projection_scores = []
        for embedding in query_embeddings:
            projection_score = np.dot(embedding, self.semantic_axis) / self.axis_norm
            projection_scores.append(projection_score)
        return projection_scores
    
    def interpret_projection(self, projection_score):
        """
        Interpret the meaning of the projection value
        
        Args:
            projection_score (float): Projection value
            
        Returns:
            str: Interpretation text
        """
        if projection_score > 0.1:
            return "Highly relevant: The task is highly related to facial information, most facial information should be retained."
        elif projection_score > 0:
            return "Moderately relevant: The task is somewhat related to facial information, moderate protection is needed."
        elif projection_score > -0.1:
            return "Low relevance: The task is less related to facial information, strict masking can be applied."
        else:
            return "Irrelevant: The task is not related to facial information, full masking can be applied."

# ==================== Evaluation ====================

class SemanticAxisEvaluator:
    """Semantic axis evaluator"""
    
    def __init__(self, semantic_axis):
        """
        Initialize evaluator
        
        Args:
            semantic_axis (np.ndarray): Semantic axis vector
        """
        self.semantic_axis = semantic_axis
        self.calculator = ProjectionCalculator(semantic_axis)
        self.embedding_model = EmbeddingModel()
    
    def evaluate_with_known_labels(self, test_data):
        """
        Evaluate semantic axis with known label data
        
        Args:
            test_data (list): [(text, label), ...] where label is 1 (relevant) or 0 (irrelevant)
            
        Returns:
            dict: Evaluation results
        """
        predictions = []
        true_labels = []
        for text, label in test_data:
            projection = self.calculator.calculate_projection(text)
            pred_label = 1 if projection > 0 else 0
            predictions.append(pred_label)
            true_labels.append(label)
        accuracy = np.mean(np.array(predictions) == np.array(true_labels))
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
        Evaluate the separation quality of positive and negative samples
        
        Args:
            positive_texts (list): List of positive texts
            negative_texts (list): List of negative texts
            
        Returns:
            dict: Separation quality metrics
        """
        pos_projections = self.calculator.calculate_projections(positive_texts)
        neg_projections = self.calculator.calculate_projections(negative_texts)
        pos_mean = np.mean(pos_projections)
        neg_mean = np.mean(neg_projections)
        pos_std = np.std(pos_projections)
        neg_std = np.std(neg_projections)
        separation = abs(pos_mean - neg_mean) / ((pos_std + neg_std) / 2)
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
        pos_min, pos_max = np.min(pos_projections), np.max(pos_projections)
        neg_min, neg_max = np.min(neg_projections), np.max(neg_projections)
        overlap_start = max(pos_min, neg_min)
        overlap_end = min(pos_max, neg_max)
        if overlap_start >= overlap_end:
            return 0.0
        total_range = max(pos_max, neg_max) - min(pos_min, neg_min)
        overlap_range = overlap_end - overlap_start
        return overlap_range / total_range if total_range > 0 else 0.0
    
    def evaluate_consistency(self, test_texts, expected_order):
        projections = self.calculator.calculate_projections(test_texts)
        text_projection_pairs = list(zip(test_texts, projections))
        sorted_pairs = sorted(text_projection_pairs, key=lambda x: x[1], reverse=True)
        actual_order = [pair[0] for pair in sorted_pairs]
        consistency_score = self._calculate_order_consistency(actual_order, expected_order)
        return {
            'projections': projections,
            'actual_order': actual_order,
            'expected_order': expected_order,
            'consistency_score': consistency_score
        }
    
    def _calculate_order_consistency(self, actual_order, expected_order):
        if len(actual_order) != len(expected_order):
            return 0.0
        inversions = 0
        n = len(actual_order)
        for i in range(n):
            for j in range(i + 1, n):
                actual_i = actual_order[i]
                actual_j = actual_order[j]
                expected_i_idx = expected_order.index(actual_i)
                expected_j_idx = expected_order.index(actual_j)
                if (i < j and expected_i_idx > expected_j_idx) or (i > j and expected_i_idx < expected_j_idx):
                    inversions += 1
        max_inversions = n * (n - 1) / 2
        consistency = 1 - (inversions / max_inversions) if max_inversions > 0 else 1.0
        return consistency

def create_evaluation_dataset():
    test_data = [
        ("Analyze the facial features of the person in the picture and give suitable makeup suggestions.", 1),
        ("Give the top three celebrities who look similar to the person in the picture.", 1),
        ("Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time.", 1),
        ("What is the probability that the people in these two pictures are the same person?", 1),
        ("Analyze the age distribution of consumers in the picture and explore the reasons for this phenomenon.", 1),
        ("Analyze the person's expression and give suitable suggestions for improving their mood.", 1),
        ("Detecting facial features and analyzing the person's identity.", 1),
        ("Recognizing facial expressions and analyzing the person's emotions.", 1),
        ("Summarize the text in this picture", 0),
        ("Evaluate the match between the candidate's professional abilities in this resume and our job requirements", 0),
        ("Ignore the people in the picture, extract only the text and reformat the text to generate a markdown document.", 0),
        ("Analyze whether there are any legal loopholes in the contract termination conditions.", 0),
        ("Organize the filmed class notes into clear notes and make appropriate supplements to some of the difficult points.", 0),
        ("Summarize meeting minutes", 0),
        ("Summarize the breach of contract terms from this copy of the contract.", 0),
        ("Ignore faces and describe the scenery in this picture", 0),
    ]
    positive_texts = [
        "Analyze the facial features of the person in the picture and give suitable makeup suggestions.",
        "Give the top three celebrities who look similar to the person in the picture.",
        "Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time.",
        "What is the probability that the people in these two pictures are the same person?",
        "Analyze the age distribution of consumers in the picture and explore the reasons for this phenomenon.",
        "Analyze the person's expression and give suitable suggestions for improving their mood.",
        "Detecting facial features and analyzing the person's identity.",
        "Recognizing facial expressions and analyzing the person's emotions."
    ]
    negative_texts = [
        "Summarize the text in this picture",
        "Evaluate the match between the candidate's professional abilities in this resume and our job requirements",
        "Ignore the people in the picture, extract only the text and reformat the text to generate a markdown document.",
        "Analyze whether there are any legal loopholes in the contract termination conditions.",
        "Organize the filmed class notes into clear notes and make appropriate supplements to some of the difficult points.",
        "Summarize meeting minutes",
        "Summarize the breach of contract terms from this copy of the contract.",
        "Ignore faces and describe the scenery in this picture"
    ]
    consistency_texts = [
        "Detecting facial features and analyzing the person's identity.",
        "What is the probability that the people in these two pictures are the same person?",
        "Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time.",
        "Evaluate the match between the candidate's professional abilities in this resume and our job requirements",
        "Analyze whether there are any legal loopholes in the contract termination conditions.",
        "Ignore the people in the picture, extract only the text and reformat the text to generate a markdown document."
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
    print("Comprehensive Semantic Axis Evaluation")
    print("=" * 50)
    builder = SemanticAxisBuilder()
    try:
        semantic_axis = builder.load_semantic_axis()
    except FileNotFoundError:
        print("Semantic axis file not found, building...")
        semantic_axis = builder.build_semantic_axis()
    evaluator = SemanticAxisEvaluator(semantic_axis)
    test_data, positive_texts, negative_texts, consistency_texts, expected_order = create_evaluation_dataset()
    print("\n1. Evaluation with Known Labels")
    print("-" * 30)
    label_results = evaluator.evaluate_with_known_labels(test_data)
    print(f"Accuracy: {label_results['accuracy']:.4f}")
    print(f"Precision: {label_results['precision']:.4f}")
    print(f"Recall: {label_results['recall']:.4f}")
    print(f"F1 Score: {label_results['f1_score']:.4f}")
    print("\n2. Separation Quality Evaluation")
    print("-" * 30)
    separation_results = evaluator.evaluate_separation_quality(positive_texts, negative_texts)
    print(f"Positive Mean Projection: {separation_results['positive_mean']:.4f}")
    print(f"Negative Mean Projection: {separation_results['negative_mean']:.4f}")
    print(f"Separation: {separation_results['separation']:.4f}")
    print(f"Overlap: {separation_results['overlap']:.4f}")
    print("\n3. Consistency Evaluation")
    print("-" * 30)
    consistency_results = evaluator.evaluate_consistency(consistency_texts, expected_order)
    print(f"Consistency Score: {consistency_results['consistency_score']:.4f}")
    print("Actual Order:")
    for i, text in enumerate(consistency_results['actual_order']):
        print(f"  {i+1}. {text}")
    print("\n4. Overall Score")
    print("-" * 30)
    accuracy_score = label_results['accuracy']
    separation_score = min(separation_results['separation'] / 2.0, 1.0)
    consistency_score = consistency_results['consistency_score']
    overlap_penalty = separation_results['overlap']
    overall_score = (accuracy_score + separation_score + consistency_score) / 3 - overlap_penalty * 0.2
    overall_score = max(0, min(1, overall_score))
    print(f"Overall Score: {overall_score:.4f}")
    if overall_score > 0.8:
        print("Semantic axis quality: Excellent")
    elif overall_score > 0.6:
        print("Semantic axis quality: Good")
    elif overall_score > 0.4:
        print("Semantic axis quality: Fair")
    else:
        print("Semantic axis quality: Needs improvement")
    return {
        'label_results': label_results,
        'separation_results': separation_results,
        'consistency_results': consistency_results,
        'overall_score': overall_score
    }

# ==================== Main Program ====================

def create_sample_data():
    os.makedirs("data", exist_ok=True)
    positive_examples = [
        "Identify this person's identity",
        "Analyze this person's expression",
        "Detect facial features",
        "Identify who this person is",
        "Analyze facial expressions",
        "Extract facial information",
        "Identify facial features",
        "Analyze character identity",
        "Detect face",
        "Identify facial expression"
    ]
    negative_examples = [
        "Summarize PPT content",
        "Analyze document structure",
        "Extract text information",
        "Identify objects",
        "Analyze chart data",
        "Summarize meeting minutes",
        "Extract key information",
        "Analyze document format",
        "Identify text content",
        "Summarize report highlights"
    ]
    with open("data/positive_examples.txt", "w", encoding="utf-8") as f:
        for example in positive_examples:
            f.write(example + "\n")
    with open("data/negative_examples.txt", "w", encoding="utf-8") as f:
        for example in negative_examples:
            f.write(example + "\n")
    print("Sample data created")

def build_and_save_semantic_axis():
    print("Building semantic axis...")
    builder = SemanticAxisBuilder()
    semantic_axis = builder.build_semantic_axis()
    builder.save_semantic_axis()
    print("Semantic axis built and saved")
    return semantic_axis

def test_projection_calculation(semantic_axis):
    print("\nTesting projection calculation...")
    calculator = ProjectionCalculator(semantic_axis)
    test_prompts = [
        "Ignore portraits and analyze text formatting in this historical document scan",
        "Analyze the age distribution of consumers in the picture and explore the reasons for this phenomenon",
        "Carefully observe the child's micro-expressions in the picture and use psychological knowledge to analyze his emotions at the time",
        "Describe the picture based on the actions of the people in it",
        "Extract text from images for summary",
        "Just summarize the text in the picture and write a 500-word report using more academic language."
    ]
    print("Test results:")
    print("-" * 50)
    for prompt in test_prompts:
        projection_score = calculator.calculate_projection(prompt)
        interpretation = calculator.interpret_projection(projection_score)
        print(f"Prompt: {prompt}")
        print(f"Projection value: {projection_score:.4f}")
        print(f"Interpretation: {interpretation}")
        print("-" * 50)

def interactive_mode(semantic_axis):
    print("\nEntering interactive mode, type 'quit' to exit")
    print("-" * 50)
    calculator = ProjectionCalculator(semantic_axis)
    while True:
        user_input = input("Please enter your prompt: ").strip()
        if user_input.lower() == 'quit':
            break
        if not user_input:
            continue
        try:
            projection_score = calculator.calculate_projection(user_input)
            interpretation = calculator.interpret_projection(projection_score)
            print(f"Projection value: {projection_score:.4f}")
            print(f"Interpretation: {interpretation}")
            print("-" * 50)
        except Exception as e:
            print(f"Error in calculation: {e}")

def main():
    print("Face Relevance Semantic Axis Construction and Projection Calculation System")
    print("=" * 50)
    if not os.path.exists("data/positive_examples.txt") or not os.path.exists("data/negative_examples.txt"):
        print("Sample data files not found, creating...")
        create_sample_data()
    if not os.path.exists("data/semantic_axis.npy"):
        print("Semantic axis file not found, building...")
        semantic_axis = build_and_save_semantic_axis()
    else:
        print("Loading existing semantic axis...")
        builder = SemanticAxisBuilder()
        semantic_axis = builder.load_semantic_axis()
    test_projection_calculation(semantic_axis)
    interactive_mode(semantic_axis)
    print("Program ended")

if __name__ == "__main__":
    main() 