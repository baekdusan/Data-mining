###### 24 - 1 UNIST CSE
# Intro to Data mining Assignment2
##### Group 8 (문정우, 이형준, 백두산)

***

## HOW TO IMPLEMENT

### K-means++
#### 코드 실행
- 같은 디렉토리 내에 있는 artd-31.csv, blobs_dataset.csv, cluster_data.csv, moons_dataset_only.csv 를 모두 사용할 수 있음.
```bash
java A2_G8_t1.java ./artd-31.csv 15

// or

java A2_G8_t1.java ./artd-31.csv
```
#### 실험
##### 1. k-Means와 k-Means++의 차이
- 주어진 데이터의 클러스터링 결과와 Elbow point, Silhouette 점수를 시각화하기 위해 파이썬의 내장 함수를 사용함.
- 방법은 java와 동일함.
```bash
python3 kMeans.py ./artd-31.py 7
```
```bash
python3 kMeansPlusPlus.py ./artd-31.py 7
```
- 같은 데이터와 클러스터 수를 입력으로 넣어 k-Means와 k-Means++에서 실행시켰을 때 차이를 시각화하여 비교할 수 있음

##### 2. 추정치 찾기
```bash
python3 estimated.py ./artd-31.csv
```
- 모든 데이터를 추정해보았을 때, Elbow point는 항상 2에서 3으로 갈 때 가장 큰 폭으로 감소하였고, Silhouette 점수는 모두 하나의 최댓값을 얻을 수 있었음.


- ./moons_dataset_only.csv는 kmeans++의 단점을 부각할 수 있는 데이터로써, 해당 데이터에서의 클러스터링 결과는 실루엣 점수가 들쑥날쑥하다는 것을 확인 가능함.

---

### DBSCAN
#### 코드 실행
- k-means++ 에서 사용한 데이터 역시 모두 DBSCAN에서 사용 가능함.
```bash
//eps, mu 모두 주어짐
java A2_G2_t2 ./artd-31.csv 5 0.5

// mu 만 주어짐
java A2_G2_t2 ./artd-31.csv 5

// eps 만 주어짐
java A2_G2_t2 ./artd-31.csv 0.5
```
#### 실험
##### 1. k-Means와 DBSCAN의 차이
- 주어진 데이터의 클러스터링 결과를 시각적으로 비교하기 위해 파이썬의 내장 함수를 사용함.
- 방법은 java와 동일함.
```bash
python3 kMeans.py ./moons_dataset_only.csv 7
```
```bash
python3 dbscan_moon.py
```
- 같은 데이터셋에서 k-Means와 DBSCAN의 차이로 인한 결과를 시각적으로 비교할 수 있음
