import pandas as pd
from sklearn.cluster import KMeans
import matplotlib.pyplot as plt
import sys
import numpy as np
from sklearn.metrics import silhouette_score

def find_optimal_clusters(data, max_k):
    wcss = []
    silhouette_scores = []
    for i in range(2, max_k + 1):
        kmeans = KMeans(n_clusters=i, init='k-means++', random_state=42)
        kmeans.fit(data)
        wcss.append(kmeans.inertia_)
        silhouette_scores.append(silhouette_score(data, kmeans.labels_))
    
    # 엘보우 포인트 그래프 그리기
    plt.figure(figsize=(14, 7))
    plt.subplot(1, 2, 1)
    plt.plot(range(2, max_k + 1), wcss, marker='o')
    plt.title('Elbow Method')
    plt.xlabel('Number of clusters')
    plt.ylabel('WCSS')
    
    plt.subplot(1, 2, 2)
    plt.plot(range(2, max_k + 1), silhouette_scores, marker='o')
    plt.title('Silhouette Scores')
    plt.xlabel('Number of clusters')
    plt.ylabel('Silhouette Score')
    
    plt.show()
    
    # 실루엣 점수가 최대가 되는 지점 찾기
    optimal_k = np.argmax(silhouette_scores) + 2  # silhouette_scores의 인덱스가 2부터 시작하므로 +2
    
    return optimal_k

def main(csv_file_path, num_clusters=None):
    # CSV 파일 읽기
    data = pd.read_csv(csv_file_path, header=None)
    
    # 데이터 샘플 출력
    print("Data Sample:\n", data.head())
    
    # 마지막 열을 제외한 나머지 열을 사용
    X = data.iloc[:, 1:-1].values
    
    # 클러스터 개수가 지정되지 않은 경우 엘보우 방법으로 결정
    if num_clusters is None:
        print("Finding the optimal number of clusters using the Elbow Method and Silhouette Scores...")
        num_clusters = find_optimal_clusters(X, max_k=10)
        print(f"Optimal number of clusters: {num_clusters}")
    
    # K-means++ 클러스터링
    km = KMeans(init='k-means++', n_clusters=num_clusters, random_state=42)
    km.fit(X)
    
    # 클러스터 할당 결과 출력
    clusters = km.labels_
    for i in range(num_clusters):
        print(f"Cluster #{i + 1} =>", end=" ")
        for index, cluster in enumerate(clusters):
            if cluster == i:
                print(data.iloc[index, 0], end=" ")
        print("\n")
    
    # 데이터 시각화
    plt.scatter(X[:, 0], X[:, 1], c=clusters, cmap='viridis', marker='o')
    plt.scatter(km.cluster_centers_[:, 0], km.cluster_centers_[:, 1], s=300, c='red', marker='x')
    plt.title('K-means++ Clustering')
    plt.xlabel('X coordinate')
    plt.ylabel('Y coordinate')
    plt.show()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 script.py csvFilePath numClusters (optional)")
        sys.exit(1)
    
    csv_file_path = sys.argv[1]
    num_clusters = int(sys.argv[2]) if len(sys.argv) > 2 else None
    
    main(csv_file_path, num_clusters)
