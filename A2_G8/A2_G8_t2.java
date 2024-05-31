import java.io.*;
import java.util.*;

public class A2_G2_t2 {
    private static double epsilon;
    private static int minPts;
    private static int[] labels;

    public static double[][] loadData(String filePath) {
        List<double[]> data = new ArrayList<>();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new FileReader(filePath));
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                double x = Double.parseDouble(values[1]);
                double y = Double.parseDouble(values[2]);
                data.add(new double[] {x, y});
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
            System.exit(1);
        }
        double[][] dataArray = data.toArray(new double[data.size()][]);
        return dataArray;
    }

    // calculate distace
    public static double calcDist(double[] point1, double[] point2) {
        double sum = 0;
        for (int i = 0; i < point1.length; i++) {
            sum += Math.pow(point1[i] - point2[i], 2);
        }
        return Math.sqrt(sum);
    }

    // find neighbor
    public static List<Integer> getNeighbor(double[][] data, int pointIndex) {
        List<Integer> neighbors = new ArrayList<>();
        for (int i = 0; i < data.length; i++) {
            if (i != pointIndex && calcDist(data[pointIndex], data[i]) <= epsilon) {
                neighbors.add(i);
            }
        }
        return neighbors;
    }

    // estimate epsilon
    public static double[] calculateKthDistances(double[][] data, int k) {
        int numPoints = data.length;
        double[] kthDistances = new double[numPoints];

        for (int i = 0; i < numPoints; i++) {
            double[] distances = new double[numPoints];
            for (int j = 0; j < numPoints; j++) {
                if (i != j) {
                    distances[j] = calcDist(data[i], data[j]);
                } else {
                    distances[j] = Double.MAX_VALUE;
                }
            }
            Arrays.sort(distances);
            kthDistances[i] = distances[k];
        }
        Arrays.sort(kthDistances);
        return kthDistances;
    }

    public static int findElbowPoint(double[] distances) {
        int elbowPoint = 0;
        double maxNum = 0;

        for (int i = 1; i < distances.length - 1; i++) {
            double slopeChange = distances[i + 1] - 2 * distances[i] + distances[i - 1];
            if (slopeChange > maxNum) {
                maxNum = slopeChange;
                elbowPoint = i;
            }
        }

        return elbowPoint;
    }

    public static void estimateEps(double[][] data) {
        double[] distances = calculateKthDistances(data, minPts);
        int elbowPoint = findElbowPoint(distances);
        epsilon = distances[elbowPoint];
    }

    // estimate minPts
    public static double calculateAverageDistance(double[] distances) {
        double sum = 0;
        for (double distance : distances) {
            sum += distance;
        }
        return sum / distances.length;
    }

    public static void estimateMinPts(double[][] data) {
        int bestMinPts = 1;
        int maxMinPts = 10;
        double minDifference = Double.MAX_VALUE;

        for (int k = 1; k <= maxMinPts; k++) {
            double[] kthDistances = calculateKthDistances(data, k);
            double averageDistance = calculateAverageDistance(kthDistances);

            double difference = Math.abs(averageDistance - epsilon);
            if (difference < minDifference) {
                minDifference = difference;
                bestMinPts = k;
            }
        }

        minPts = bestMinPts;
    }

    // DBscan
    public static void checkCluster(double[][] data, int pointIndex, int cluster) {
        List<Integer> neighbors = getNeighbor(data, pointIndex);
        labels[pointIndex] = cluster;
        if(neighbors.size()+1 >= minPts) {
            for (int neighborIndex : neighbors) {
                if (labels[neighborIndex] != cluster) {
                    checkCluster(data, neighborIndex, cluster);
                }
            }
        }
    }

    public static int[] getDBSCAN(double[][] data) {
        int numPoints = data.length;
        labels = new int[numPoints];
        int cluster = 1;

        for (int i = 0; i < numPoints; i++) {
            if (labels[i] != 0) {
                continue;
            }

            List<Integer> neighbors = getNeighbor(data, i);
            if (neighbors.size()+1 < minPts) {
                labels[i] = -1;
            } else {
                checkCluster(data, i, cluster);
                cluster++;
            }
        }

        return labels;
    }

    // print cluster
    public static void printClusters() {
        int numClusters = 0;
        int numNoise = 0;
        for (int label : labels) {
            if (label > numClusters) {
                numClusters = label;
            }
            if (label==-1) {
                numNoise++;
            }
        }
        System.out.println("Number of clusters : " + numClusters);
        System.out.println("Number of noise : " + numNoise);

        for (int cluster = 1; cluster <= numClusters; cluster++) {
            System.out.print("Cluster #" + cluster + " => ");
            for (int i = 0; i < labels.length; i++) {
                if (labels[i] == cluster) {
                    System.out.print("p" + (i+1) + " ");
                }
            }
            System.out.println();
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: java DBscan <file_path> <epsilon or minPts> [<minPts or epsilon>]");
            System.exit(1);
        }

        String filePath = args[0];
        epsilon = -1;
        minPts = -1;

        double[][] dataArray = loadData(filePath);


        if (args.length == 2) {
            if (args[1].contains(".")) {
                epsilon = Double.parseDouble(args[1]);
                estimateMinPts(dataArray);
                System.out.println("Estimated MinPts : " + minPts);
            } else {
                minPts = Integer.parseInt(args[1]);
                estimateEps(dataArray);
                System.out.println("Estimated eps : " + epsilon);
            }
        } else {
            if (args[1].contains(".")) {
                epsilon = Double.parseDouble(args[1]);
                minPts = Integer.parseInt(args[2]);
            } else {
                epsilon = Double.parseDouble(args[2]);
                minPts = Integer.parseInt(args[1]);
            }
        }

        int[] labels = getDBSCAN(dataArray);

        printClusters();
    }
}
