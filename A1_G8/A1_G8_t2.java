
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class A1_G8_t2 {

    public static void main(String[] args) {
        if (args.length != 2) {
            System.out.println("Usage: java A1_G8_t2.java <filename> <min_support>");
            return;
        }

        String filename = args[0];
        double minSupport = Double.parseDouble(args[1]);

        Map<Set<String>, Integer> dataset = readDatasetFromCSV(filename);
        int totalTransactions = dataset.values().stream().mapToInt(Integer::intValue).sum();
        int minSupportCount = (int) (minSupport * totalTransactions);

        List<Pair<Set<String>, Integer>> frequentItemsets = fpGrowth(dataset, minSupportCount);
        printFrequentItemsets(frequentItemsets, totalTransactions);
    }
    
    private static TreeNode createTree(Map<Set<String>, Integer> dataset, int minSupport) {
        Map<String, Integer> headerTable = new HashMap<>();
        for (Set<String> transaction : dataset.keySet()) {
            for (String item : transaction) {
                headerTable.put(item, headerTable.getOrDefault(item, 0) + dataset.get(transaction));
            }
        }

        headerTable.entrySet().removeIf(entry -> entry.getValue() < minSupport);

        Set<String> frequentItems = new HashSet<>(headerTable.keySet());
        if (frequentItems.isEmpty()) {
            return null;
        }

        Map<String, Node> headerTableWithLinks = new HashMap<>();
        for (String item : headerTable.keySet()) {
            headerTableWithLinks.put(item, new Node(headerTable.get(item), null));
        }

        TreeNode fpTree = new TreeNode("Null Set", 1, null);
        for (Map.Entry<Set<String>, Integer> entry : dataset.entrySet()) {
            Set<String> transaction = entry.getKey();
            int count = entry.getValue();
            Map<String, Integer> localD = new HashMap<>();
            for (String item : transaction) {
                if (frequentItems.contains(item)) {
                    localD.put(item, headerTableWithLinks.get(item).count);
                }
            }
            if (!localD.isEmpty()) {
                List<String> orderedItems = new ArrayList<>(localD.keySet());
                orderedItems.sort((a, b) -> localD.get(b) - localD.get(a));
                updateTree(orderedItems, fpTree, headerTableWithLinks, count);
            }
        }

        return fpTree;
    }

    private static void updateTree(List<String> items, TreeNode inTree, Map<String, Node> headerTable, int count) {
        if (inTree.children.containsKey(items.get(0))) {
            inTree.children.get(items.get(0)).increaseCount(count);
        } else {
            TreeNode newNode = new TreeNode(items.get(0), count, inTree);
            inTree.children.put(items.get(0), newNode);
            Node node = headerTable.get(items.get(0));
            if (node.nodeLink == null) {
                node.nodeLink = newNode;
            } else {
                updateHeader(node.nodeLink, newNode);
            }
        }
        if (items.size() > 1) {
            updateTree(items.subList(1, items.size()), inTree.children.get(items.get(0)), headerTable, count);
        }
    }

    private static void updateHeader(TreeNode nodeToTest, TreeNode targetNode) {
        while (nodeToTest.nodeLink != null) {
            nodeToTest = nodeToTest.nodeLink;
        }
        nodeToTest.nodeLink = targetNode;
    }

    private static void ascendTree(TreeNode leafNode, List<String> prefixPath) {
        if (leafNode.parent != null) {
            prefixPath.add(0, leafNode.name);
            ascendTree(leafNode.parent, prefixPath);
        }
    }

    private static Map<Set<String>, Integer> findPrefixPath(String basePat, TreeNode treeNode) {
        Map<Set<String>, Integer> conditionalPatterns = new HashMap<>();
        while (treeNode != null) {
            List<String> prefixPath = new ArrayList<>();
            ascendTree(treeNode, prefixPath);
            if (prefixPath.size() > 1) {
                Set<String> prefixSet = new HashSet<>(prefixPath.subList(1, prefixPath.size()));
                conditionalPatterns.put(prefixSet, treeNode.count);
            }
            treeNode = treeNode.nodeLink;
        }
        return conditionalPatterns;
    }

    private static void mineTree(TreeNode inTree, Map<String, Node> headerTable, int minSupport, Set<String> prefix, List<Pair<Set<String>, Integer>> frequentItemList) {
        List<String> bigL = new ArrayList<>(headerTable.keySet());
        bigL.sort((a, b) -> headerTable.get(b).count - headerTable.get(a).count);
        for (String basePat : bigL) {
            Set<String> newFreqSet = new HashSet<>(prefix);
            newFreqSet.add(basePat);
            int support = headerTable.get(basePat).count;
            frequentItemList.add(new Pair<>(newFreqSet, support));
            Map<Set<String>, Integer> conditionalPatterns = findPrefixPath(basePat, headerTable.get(basePat).nodeLink);
            TreeNode myCondTree = createTree(conditionalPatterns, minSupport);
            if (myCondTree != null) {
                Map<String, Node> myHead = new HashMap<>();
                for (Set<String> itemSet : conditionalPatterns.keySet()) {
                    for (String item : itemSet) {
                        myHead.put(item, new Node(conditionalPatterns.get(itemSet), null));
                        break; // Only add the first item from the itemSet
                    }
                }
                mineTree(myCondTree, myHead, minSupport, newFreqSet, frequentItemList);
            }
        }
    }

    private static List<Pair<Set<String>, Integer>> fpGrowth(Map<Set<String>, Integer> dataset, int minSupport) {
        TreeNode fpTree = createTree(dataset, minSupport);
        List<Pair<Set<String>, Integer>> frequentItemList = new ArrayList<>();
        if (fpTree != null) {
            Map<String, Node> headerTable = new HashMap<>();
            for (String item : fpTree.children.keySet()) {
                headerTable.put(item, new Node(fpTree.children.get(item).count, fpTree.children.get(item)));
            }
            mineTree(fpTree, headerTable, minSupport, new HashSet<>(), frequentItemList);
        }
        return frequentItemList;
    }

    private static Map<Set<String>, Integer> readDatasetFromCSV(String filename) {
        Map<Set<String>, Integer> dataset = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] items = line.split(",");
                Set<String> transaction = new HashSet<>(Arrays.asList(items));
                dataset.put(transaction, dataset.getOrDefault(transaction, 0) + 1);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return dataset;
    }

    private static void printFrequentItemsets(List<Pair<Set<String>, Integer>> frequentItemsets, int totalTransactions) {
        frequentItemsets.sort((a, b) -> a.second - b.second);
        for (Pair<Set<String>, Integer> itemset : frequentItemsets) {
            StringBuilder sb = new StringBuilder();
            for (String item : itemset.first) {
                sb.append(item).append(", ");
            }
            sb.setLength(sb.length() - 2);
            double support = (double) itemset.second / totalTransactions;
            System.out.println(sb.toString() + " " + support);
        }
    }

    private static class Node {
        int count;
        TreeNode nodeLink;

        Node(int count, TreeNode nodeLink) {
            this.count = count;
            this.nodeLink = nodeLink;
        }
    }

    private static class Pair<T, U> {
        T first;
        U second;

        Pair(T first, U second) {
            this.first = first;
            this.second = second;
        }
    }
}

class TreeNode {
    String name;
    int count;
    TreeNode nodeLink;
    TreeNode parent; 
    Map<String, TreeNode> children;

    TreeNode(String name, int count, TreeNode parent) {
        this.name = name;
        this.count = count;
        this.parent = parent;
        this.children = new HashMap<>();
    }

    void increaseCount(int count) {
        this.count += count;
    }
}