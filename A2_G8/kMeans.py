import pandas as pd
from sklearn.cluster import KMeans
import matplotlib.pyplot as plt
import sys

def main(csv_file_path, num_clusters=None):
    # CSV 파일 읽기
    data = pd.read_csv(csv_file_path, header=None)
    
    # 데이터 샘플 출력
    print("Data Sample:\n", data.head())
    
    # 마지막 열을 제외한 나머지 열을 사용
    X = data.iloc[:, 1:-1].values
    
    # 클러스터 개수가 지정되지 않은 경우, 기본값으로 3 설정
    if num_clusters == None:
        num_clusters = 3

    # K-means 클러스터링
    km = KMeans(init='random', n_clusters=num_clusters, random_state=42)
    km.fit(X)
    
    # 클러스터 할당 결과 출력
    clusters = km.labels_
    print(f"Number of clusters: {num_clusters}")
    print(f"Cluster centers found: {len(km.cluster_centers_)}")

    for i in range(num_clusters):
        print(f"Cluster #{i + 1} =>", end=" ")
        cluster_points = []
        for index, cluster in enumerate(clusters):
            if cluster == i:
                cluster_points.append(data.iloc[index, 0])
                print(data.iloc[index, 0], end=" ")
        if not cluster_points:
            print("Empty cluster!", end=" ")
        print("\n")
    
    # 데이터 시각화
    plt.scatter(X[:, 0], X[:, 1], c=clusters, cmap='viridis', marker='o')
    plt.scatter(km.cluster_centers_[:, 0], km.cluster_centers_[:, 1], s=300, c='red', marker='x')
    plt.title('K-means Clustering')
    plt.xlabel('X coordinate')
    plt.ylabel('Y coordinate')
    plt.show()

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python3 A2_G8_t1.py csvFilePath numClusters (optional)")
        sys.exit(1)
    
    csv_file_path = sys.argv[1]
    num_clusters = int(sys.argv[2]) if len(sys.argv) > 2 else None
    
    main(csv_file_path, num_clusters)
