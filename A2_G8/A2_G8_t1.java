import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class A2_G8_t1 {

    public static void main(String[] args) {

        // 입력 에러 처리
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: java A2_G8_t1.java csvFilePath numClusters(optional)");
            System.exit(1);
        }

        String csvFilePath = args[0];
        Integer numClusters = null;

        if (args.length == 2) {
            numClusters = Integer.parseInt(args[1]);
        }

        List<DataPoint> dataPoints = loadCSVData(csvFilePath); // 모든 데이터 포인트가 담겨 있음

        // 빈 파일 에러 처리
        if (dataPoints == null) {
            System.err.println("Failed to load data from CSV file.");
            System.exit(1);
        }
        
        // 클러스터 개수가 입력이 안되면 추정된 값을 사용
        if (numClusters == null) {
            numClusters = estimateNumClusters(dataPoints);
            System.out.println("Estimated k: " + numClusters);
        }

        List<Cluster> clusters = kMeansPlusPlus(dataPoints, numClusters);

        // 출력
        for (int i = 0; i < clusters.size(); i++) {
            System.out.print("Cluster #" + (i + 1) + " => ");
            for (DataPoint point : clusters.get(i).getPoints()) {
                System.out.print(point.getId() + " ");
            }
            System.out.println("\n");
        }
    }

    // CSV 파일로부터 data point를 파싱하는 함수
    private static List<DataPoint> loadCSVData(String csvFilePath) {
        List<DataPoint> dataPoints = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(","); // p1,25.0514,5.7475,1 -> ["p1", "25.0514", "5.7475", "1"]
                String id = tokens[0];
                double[] values = new double[tokens.length - 2];
                // 마지막 열에 해당하는 값은 무시
                for (int i = 1; i < tokens.length - 1; i++) {
                    values[i - 1] = Double.parseDouble(tokens[i]);
                }
                dataPoints.add(new DataPoint(id, values));
            }
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return dataPoints;
    }

    // 클러스터의 개수를 예측
    private static int estimateNumClusters(List<DataPoint> dataPoints) {
        int maxK = 31;  // 예시 데이터 기준 예상 최대 클러스터는 31; 이걸 어떻게 정해야 할지는 모르겠음
        double bestSilhouette = Double.NEGATIVE_INFINITY; // 최댓값; 초기는 -inf
        int bestK = 2; // 클러스터는 최소 2개 이상
        
        // 클러스터의 개수에 따라 분산도를 측정한다 -> 모든 경우의 수를 생각
        for (int k = 2; k <= maxK; k++) {
            List<Cluster> clusters = kMeansPlusPlus(dataPoints, k);
            double silhouette = calculateSilhouetteScore(dataPoints, clusters);
            // System.out.println("Silhouette score for k=" + k + ": " + silhouette);
            if (silhouette > bestSilhouette) {
                bestSilhouette = silhouette;
                bestK = k;
            }
        }

        return bestK;
    }

    // 실루엣 점수를 계산하는 함수
    private static double calculateSilhouetteScore(List<DataPoint> dataPoints, List<Cluster> clusters) {
        double totalScore = 0.0;
        for (DataPoint point : dataPoints) {
            // 각 데이터 포인트 하나하나마다 그 포인트가 속해있는 클러스터 내에서 평균 거리를 측정하고 (내부와의 평균 거리)
            double a = calculateAverageDistance(point, clusters.get(findClusterIndex(point, clusters)).getPoints());
            double b = Double.MAX_VALUE;
            for (Cluster cluster : clusters) {
                if (!cluster.getPoints().contains(point)) {
                    // 그 데이터 포인트와 외부 클러스터간 평균 거리를 구한 후
                    double distance = calculateAverageDistance(point, cluster.getPoints());
                    if (distance < b) {
                        // 가장 가까운 외부 클러스터와의 거리를 찾아준다
                        b = distance;
                    }
                }
            }
            // Kaufman, L., & Rousseeuw, P. J. (1987). Finding Groups in Data: An Introduction to Cluster Analysis. John Wiley & Sons.
            // 위 논문에 기반한 실루엣 점수 계산; 얼마나 올바른 클러스터 내에 속해있는지를 판단해 줌.
            totalScore += (b - a) / Math.max(a, b);
        }
        return totalScore / dataPoints.size();
    }

    // 데이터 포인트(point)에서 points에 속해 있는 모든 other 까지의 평균 거리를 계산하는 함수
    private static double calculateAverageDistance(DataPoint point, List<DataPoint> points) {
        double totalDistance = 0.0;
        for (DataPoint other : points) {
            totalDistance += point.distanceTo(other);
        }
        return totalDistance / points.size();
    }

    // 데이터 포인트가 속한 클러스터의 인덱스를 찾는 함수; 몇번째 클러스터에 있는지 확인할 수 있음
    private static int findClusterIndex(DataPoint point, List<Cluster> clusters) {
        for (int i = 0; i < clusters.size(); i++) {
            if (clusters.get(i).getPoints().contains(point)) {
                return i;
            }
        }
        return -1;
    }

    // 주요 함수: data point들의 리스트와 클러스터의 개수를 입력으로 받음.
    private static List<Cluster> kMeansPlusPlus(List<DataPoint> dataPoints, int k) {
        List<Cluster> clusters = new ArrayList<>();
        Random random = new Random();

        // 1. 첫번째 중심점이 될 녀석을 무작위로 선정
        DataPoint firstCenter = dataPoints.get(random.nextInt(dataPoints.size()));
        Cluster firstCluster = new Cluster(firstCenter);
        clusters.add(firstCluster);

        // 2. 첫번째 중심점을 기준으로 계산된 다른 중심점들을 순차적으로 넣음
        for (int i = 1; i < k; i++) {
            DataPoint nextCenter = selectNextCenter(dataPoints, clusters);
            clusters.add(new Cluster(nextCenter));
        }

        /////////////////////////////////////////////////////
        // 여기까지가                                         //
        // k means -> k means++로 업그레이드 하기 위해 필요한 내용  //
        // 중심점들의 최적화                                    //
        /////////////////////////////////////////////////////

        // 제일 가까운 데이터 포인트들을 모아 그 중심에 위치하기
        boolean converged;
        do {
            // 클러스터 비워준 뒤
            for (Cluster cluster : clusters) {
                cluster.clearPoints();
            }

            // 각각의 데이터 포인트들 새 클러스터로 재삽입
            for (DataPoint point : dataPoints) {
                Cluster nearestCluster = findNearestCluster(point, clusters);
                nearestCluster.addPoint(point);
            }

            converged = true;
            // 클러스터 데이터 리스트에는 새로운 데이터 포인트들이 모여 있음.
            // 이를 바탕으로 새로운 중심점을 계산하여 업데이트 해주는데
            for (Cluster cluster : clusters) {
                // 만약 업데이트 된 클러스터의 중심점이 변화가 없으면
                // 이제 더이상 iteration을 할 필요가 없다는 소리임. -> 그럼 do while문 탈출
                converged &= cluster.updateCenter();
            }
        } while (!converged);

        return clusters;
    }

    // 중심점이 주어졌을 때 그 중심점과 가장 멀리 떨어져 있는 데이터 포인트를 중심점으로 설정
    private static DataPoint selectNextCenter(List<DataPoint> dataPoints, List<Cluster> clusters) {

        // 각 데이터 포인트 하나하나를 클러스터의 중심과의 거리를 비교함으로써 가장 가까운 클러스터를 찾는다
        double[] distances = new double[dataPoints.size()];
        double totalDistance = 0.0;
        
        // 하나의 포인트에 대해
        for (int i = 0; i < dataPoints.size(); i++) {
            DataPoint point = dataPoints.get(i);
            double minDistance = Double.MAX_VALUE;

            // 모든 클러스터의 중심과의 거리를
            for (Cluster cluster : clusters) {
                // 찾아서
                double distance = point.distanceTo(cluster.getCenter());
                // 최솟값으로 갱신해주면
                if (distance < minDistance) {
                    minDistance = distance;
                }
            }
            distances[i] = Math.pow(minDistance, 2); // i번째 데이터포인트와 가장 가까운 중심점과의 거리가 담기고
            totalDistance += distances[i]; // 이거에 해당하는 거리를 total distance에 추가해 줌.
        }

        // 여기까지 하면 각각의 데이터 포인트들에서 가장 가까운 중심점까지의 거리가 담김
        for (int i = 0; i < distances.length; i++) {
            distances[i] /= totalDistance; // 각 데이터포인트까지의 거리 비례 확률로 변경
        }

        ///////////////////
        
        // 누적 확률 배열을 생성
        double[] cumulativeProbabilities = new double[distances.length];
        cumulativeProbabilities[0] = distances[0]; // 첫 번째 누적 확률 계산
        for (int i = 1; i < distances.length; i++) {
            cumulativeProbabilities[i] = cumulativeProbabilities[i - 1] + distances[i]; // 누적 확률 계산
        }
        
        // 무작위 값을 생성하여 해당 구간에 있는 값을 선택
        Random random = new Random(); 
        double rand = random.nextDouble(); // 0 ~ 1 사이의 값 생성
        
        for (int i = 0; i < cumulativeProbabilities.length; i++) {
            // 확률적으로 어느 인덱스를 골랐는지 선택함
            if (rand <= cumulativeProbabilities[i]) {
                return dataPoints.get(i);
            }
        }

        return dataPoints.get(dataPoints.size() - 1);
    }

    // 해당 데이터 포인트에 가장 가까운 중심점을 가진 클러스터 반환
    private static Cluster findNearestCluster(DataPoint point, List<Cluster> clusters) {
        Cluster nearestCluster = null;
        double minDistance = Double.MAX_VALUE;
        for (Cluster cluster : clusters) {
            double distance = point.distanceTo(cluster.getCenter());
            if (distance < minDistance) {
                minDistance = distance;
                nearestCluster = cluster;
            }
        }
        return nearestCluster;
    }

    // 데이터 포인트 자료형
    static class DataPoint {
        private final String id;
        private final double[] coordinates;

        public DataPoint(String id, double[] coordinates) {
            this.id = id;
            this.coordinates = coordinates;
        }

        public String getId() {
            return id;
        }

        public double[] getCoordinates() {
            return coordinates;
        }
        
        // 다른 데이터 포인트와 의 거리를 구하는 함수;
        // 이차원인지 알 수 없으니 n차원 거리라고 생각
        public double distanceTo(DataPoint other) {
            double sum = 0.0;
            for (int i = 0; i < coordinates.length; i++) {
                sum += Math.pow(coordinates[i] - other.coordinates[i], 2);
            }
            return Math.sqrt(sum);
        }
    }

    // 클러스터 자료형; 중심 데이터포인트와 나머지 데이터 포인트들의 배열로 이루어짐.
    static class Cluster {
        // 중심점과 그 주위로 형성된 데이터 포인트들
        private DataPoint center;
        private final List<DataPoint> points = new ArrayList<>();

        public Cluster(DataPoint center) {
            this.center = center;
        }

        public DataPoint getCenter() {
            return center;
        }

        public void addPoint(DataPoint point) {
            points.add(point);
        }

        public List<DataPoint> getPoints() {
            return points;
        }

        // 포인트 배열 비우기
        public void clearPoints() {
            points.clear();
        }
        
        // 새로운 중심점 찾기
        public boolean updateCenter() {

            // 클러스터에 중심점만 있는 경우
            if (points.isEmpty()) {
                return true;
            }
            
            // 모든 데이터 포인트의 축 별로 합을 구한 다음에
            double[] newCoordinates = new double[center.getCoordinates().length];
            for (DataPoint point : points) {
                double[] coordinates = point.getCoordinates();
                for (int i = 0; i < coordinates.length; i++) {
                    newCoordinates[i] += coordinates[i];
                }
            }

            // 차원의 개수로 나눠줌을 통해 (평균을 구함) 새로운 중심점을 설정
            for (int i = 0; i < newCoordinates.length; i++) {
                newCoordinates[i] /= points.size();
            }

            DataPoint newCenter = new DataPoint(center.getId(), newCoordinates);
            boolean converged = newCenter.distanceTo(center) == 0.0;
            center = newCenter;
            return converged;
        }
    }
}
