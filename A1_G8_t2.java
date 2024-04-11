import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.stream.Collectors;
import java.util.*;

public class A1_G8_t2 {
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: java A1_G8_t2 <file_path> <min_support>");
            System.exit(1);
        }

        String filePath = args[0];
        double minSupport = Double.parseDouble(args[1]);

        try {
            List<List<String>> transactions = readCSVFile(filePath);

            // FP-Tree 구축
            
            FPNode rootNode = new FPNode(null, null);
            FPBuilder fpBuilder = new FPBuilder(transactions, rootNode, minSupport);
            fpBuilder.buildFPTree();
            
            // 여기까지 FP-Tree 완성

            
            FPGrowth fpGrowth = new FPGrowth(minSupport, transactions.size());

            fpGrowth.findFrequentItemsets(rootNode);

            // 빈발 항목 집합과 그 지지도 값 출력
            Map<List<String>, Double> frequentItemsets = fpGrowth.getFrequentItemsets();
            for (Map.Entry<List<String>, Double> entry : frequentItemsets.entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // CSV 불러오기
    private static List<List<String>> readCSVFile(String filePath) throws IOException {
        List<List<String>> transactions = new ArrayList<>(); // 각각의 행을 item 별로 파싱하여 넣어줌; 2차원 배열
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] items = line.split(",");
                List<String> transaction = new ArrayList<>();
                for (String item : items) {
                    transaction.add(item.trim());
                }
                transactions.add(transaction);
            }
        }
        return transactions;
    }
    
    static class FPNode {
        private String itemName;
        private int count;
        private FPNode parent;
        private Map<String, FPNode> children;
    
        public FPNode(String itemName, FPNode parent) {
            this.itemName = itemName;
            this.count = 0;
            this.parent = parent;
            this.children = new HashMap<>();
        }
    
        // Getters and setters
    
        public String getItemName() {
            return itemName;
        }
    
        public void setItemName(String itemName) {
            this.itemName = itemName;
        }
    
        public int getCount() {
            return count;
        }
    
        public void setCount(int count) {
            this.count = count;
        }
    
        public FPNode getParent() {
            return parent;
        }
    
        public void setParent(FPNode parent) {
            this.parent = parent;
        }
    
        public Map<String, FPNode> getChildren() {
            return children;
        }
    
        public void setChildren(Map<String, FPNode> children) {
            this.children = children;
        }
    
        // FP 트리 시각화를 위한 메서드
        // 각 층별로 출력하기 위한 BFS 활용 메서드
        public static void printTreeByLevel(FPNode root) {
            Queue<FPNode> queue = new LinkedList<>();
            queue.add(root);
            queue.add(null); // 층별 구분을 위한 마커로 null 사용

            while (!queue.isEmpty()) {
                FPNode currentNode = queue.poll();

                if (currentNode == null) {
                    System.out.println(); // 층이 끝났으므로 줄바꿈
                    if (!queue.isEmpty()) {
                        queue.add(null); // 다음 층 구분을 위해 null 추가
                    }
                } else {
                    // 루트 노드는 itemName이 null입니다.
                    if (currentNode.getItemName() == null) {
                        System.out.print("Root ");
                    } else {
                        System.out.print(currentNode.getItemName() + ": " + currentNode.getCount() + " ");
                    }

                    // 현재 노드의 모든 자식을 큐에 추가
                    for (FPNode child : currentNode.getChildren().values()) {
                        queue.add(child);
                    }
                }
            }
        }
    }
    

    // FP Builder class
    static class FPBuilder {
        private List<List<String>> transactions;
        private FPNode rootNode;
        private Map<String, Integer> frequencyMap;
        private double minSupport;

        public FPBuilder(List<List<String>> transactions, FPNode rootNode, double minSupport) {
            this.transactions = transactions;
            this.rootNode = rootNode;
            this.frequencyMap = new HashMap<>();
            this.minSupport = minSupport;
            
        }

        public void buildFPTree() {
            // 아이템 하나씩 빈도 계산하기
            for (List<String> transaction : transactions) {
                for (String item : transaction) {
                    // 없으면 만들고 있으면 += 1
                    frequencyMap.put(item, frequencyMap.getOrDefault(item, 0) + 1);
                }
            }
            // frequencyMap 결과 예시: {a: 1, b: 4, c: 5 ...}

            // minsup을 애초에 넘지 못하는 요소들은 바로 제거, ppt에서 Ordered items만 남겨 놓기.
            frequencyMap.entrySet().removeIf(entry -> entry.getValue() / (double) transactions.size() < minSupport);
            // frequencyMap 결과 예시: {b: 4, c: 5 ...}

            // 행별 아이템 탐색
            for (List<String> transaction : transactions) {
                List<String> filteredTransaction = new ArrayList<>();
                for (String item : transaction) {
                    // transaction에 있는 아이템이 frequencyMap에 존재하는지 확인하고, 있으면 filteredTransaction
                    if (frequencyMap.containsKey(item)) {
                        filteredTransaction.add(item);
                    }
                }
            
                // 각 행별로 아이템들의 빈도를 체크하여 내림차순으로 정렬
                filteredTransaction.sort((item1, item2) -> {
                    int freqCompare = frequencyMap.get(item2).compareTo(frequencyMap.get(item1));
                    if (freqCompare != 0) {
                        return freqCompare;
                    }
                    return item1.compareTo(item2);
                });

                // 여기까지 하면 filteredTransaction 완성 -> ex> {f, c, a, m, p}
            
                // FP Tree 만들기
                FPNode currentNode = rootNode;
                for (String item : filteredTransaction) {
                    if (currentNode.children.containsKey(item)) {
                        // 현재 아이템이 이미 자식으로 존재한다면, 해당 노드를 현재 노드로 설정하고 카운트 증가
                        currentNode = currentNode.children.get(item);
                        currentNode.count++;
                    } else {
                        // 현재 아이템이 자식으로 존재하지 않는다면, 새 노드를 생성하고 현재 노드의 자식으로 추가
                        FPNode newNode = new FPNode(item, currentNode);
                        currentNode.children.put(item, newNode);
                        currentNode = newNode; // 새롭게 생성된 노드를 현재 노드로 설정
                        newNode.count = 1;
                    }
                }                
            }
            
            // 빈도 배열 잘 나오는지 확인용
            /*
            System.out.println("Frequency Map:");
            for (Map.Entry<String, Integer> entry : frequencyMap.entrySet()) {
                System.out.println(entry.getKey() + ": " + entry.getValue());
            }
            */

            // FP 트리 잘 나오는지 확인용
            FPNode.printTreeByLevel(rootNode);
        }
        
    }

 // FP Growth algorithm
    static class FPGrowth {
        private double minSupport;
        private int totalTransactions;
        private Map<List<String>, Integer> frequentItemsets;

        public FPGrowth(double minSupport, int totalTransactions) {
            this.minSupport = minSupport;
            this.totalTransactions = totalTransactions;
            this.frequentItemsets = new HashMap<>();
        }
        
        // 메인 클래스에서 fpGrowth.findFrequentItemsets(rootNode); 이렇게 실행
        private void findFrequentItemsets(FPNode node, List<String> suffix, int suffixSupport) {
            if (node.parent != null && !node.itemName.equals("")) { // 루트 노드와 아이템 이름이 없는 노드는 제외
                suffix.add(0, node.itemName);
                int support = Math.min(node.count, suffixSupport);

                // 현재 값이 threshold 이상이면 그걸 넣는다
                if (((double) support / totalTransactions) >= minSupport) {
                    frequentItemsets.put(new ArrayList<>(suffix), support);
                }
                
                // 조건부 FP 트리를 위한 접두사 경로 찾기
                Map<String, Integer> conditionalItems = new HashMap<>();
                FPNode currentNode = node;
                while (currentNode.parent != null) {
                    String itemName = currentNode.itemName;
                    conditionalItems.put(itemName, conditionalItems.getOrDefault(itemName, 0) + currentNode.count);
                    currentNode = currentNode.parent;
                }

                for (Map.Entry<String, Integer> entry : conditionalItems.entrySet()) {
                    double itemSupport = (double) entry.getValue() / totalTransactions;
                    if (itemSupport >= minSupport) {
                        List<String> newSuffix = new ArrayList<>(suffix);
                        newSuffix.add(0, entry.getKey());
                        frequentItemsets.put(newSuffix, entry.getValue());
                    }
                }
            }

            for (FPNode child : node.children.values()) {
                findFrequentItemsets(child, new ArrayList<>(suffix), node.count); // 재귀적으로 자식 노드 탐색
            }
        }

        public void findFrequentItemsets(FPNode root) {
            findFrequentItemsets(root, new ArrayList<>(), root.count);
        }

        // Getters and setters
        public Map<List<String>, Double> getFrequentItemsets() {
            return frequentItemsets.entrySet().stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            entry -> entry.getValue() / (double) totalTransactions
                    ));
        }
        
    }
}