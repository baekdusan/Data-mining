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

        List<DataPoint> dataPoints = loadCSVData(csvFilePath);

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
                // 마지막 열에 해당하는 값은 뭘까? 일단 이거는 무시하고 해보자
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

    private static int estimateNumClusters(List<DataPoint> dataPoints) {
        int maxK = 20;  // Maximum number of clusters to consider
        double[] sse = new double[maxK];

        for (int k = 1; k <= maxK; k++) {
            List<Cluster> clusters = kMeansPlusPlus(dataPoints, k);
            sse[k - 1] = calculateSSE(clusters);
        }

        // Find the elbow point
        return findElbowPoint(sse);
    }

    // 각 클러스터의 중심점과 모든 데이터 포인트 사이의 거리의 합
    private static double calculateSSE(List<Cluster> clusters) {
        double sse = 0.0;
        for (Cluster cluster : clusters) {
            DataPoint center = cluster.getCenter();
            for (DataPoint point : cluster.getPoints()) {
                sse += point.distanceTo(center);
            }
        }
        return sse;
    }

    // 몇 개의 클러스터로 나눌지 2번째 인자가 주어지지 않는다면
    // 갑자기 에러 값이 가장 커지는 순간을 찾는다
    private static int findElbowPoint(double[] sse) {
        int elbowPoint = 1;
        double maxChange = 0.0;

        for (int i = 1; i < sse.length - 1; i++) {
            double change = sse[i - 1] - sse[i];
            if (change > maxChange) {
                maxChange = change;
                elbowPoint = i + 1;
            }
        }

        return elbowPoint;
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
            for (Cluster cluster : clusters) {
                cluster.clearPoints();
            }

            // Assign points to the nearest cluster
            for (DataPoint point : dataPoints) {
                Cluster nearestCluster = findNearestCluster(point, clusters);
                nearestCluster.addPoint(point);
            }

            converged = true;
            // Update cluster centers
            for (Cluster cluster : clusters) {
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
