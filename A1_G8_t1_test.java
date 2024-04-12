import java.io.*;
import java.util.*;

public class A1_G8_t1_test {
    public static int t_num = 0;
    public static double minSupport;
    public static List<List<String>> dataList = new ArrayList<>();
    public static Map<Set<String>, Integer> answer = new HashMap<>();
    
    public static Map<Set<String>, Integer> fill_candidateSet(Map<Set<String>, Integer> data) {
        for(Set<String> key : data.keySet()) {
            for(List<String> list : dataList) {
                if(list.containsAll(key)) {
                    data.replace(key, data.get(key)+1);
                }
            }
        }
        return data;
    }

    public static Map<Set<String>, Integer> make_candidateSet(Map<Set<String>, Integer> data) {
        Iterator<Set<String>> iterator = data.keySet().iterator();
        int elementNum = iterator.next().size() + 1;

        Map<Set<String>, Integer> candidateSet = new HashMap<>();
        Set<String> tmp = new HashSet<>();
        for(Set<String> i : data.keySet()) {
            for(Set<String> j : data.keySet()) {
                tmp.clear();
                tmp.addAll(i);
                tmp.addAll(j);
                if(tmp.size() == elementNum) {
                    candidateSet.put(new HashSet<>(tmp), 0);
                }
            }
        }
        return candidateSet;
    }

    public static Map<Set<String>, Integer> check_minSupport(Map<Set<String>, Integer> data) {
        Set<Set<String>> keysToRemove = new HashSet<>();
        for (Set<String> key : data.keySet()) {
            if ((double)data.get(key) / t_num < minSupport) {
                keysToRemove.add(key);
            }
        }
        for (Set<String> key : keysToRemove) {
            data.remove(key);
        }
        return data;
    }
    
    public static void main(String[] args) {
        String fileName = args[0];
        minSupport = Double.parseDouble(args[1]);
        t_num = Integer.parseInt(args[2]);
        long beforeTime = System.currentTimeMillis();

        int line_check = 0;
        String line = "";
        Map<Set<String>, Integer> nowSet = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            while ((line = br.readLine()) != null) {
                if(line_check++ >= t_num) break;
                //t_num++;
                List<String> items = Arrays.asList(line.split(","));
                for(String item : items) {
                    nowSet.put(new HashSet<>(Arrays.asList(item)), 0);
                }
                dataList.add(items);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        nowSet = fill_candidateSet(nowSet);
        nowSet = check_minSupport(nowSet);
        answer.putAll(nowSet);
        while(!nowSet.isEmpty()) {
            nowSet = make_candidateSet(nowSet);
            nowSet = fill_candidateSet(nowSet);
            nowSet = check_minSupport(nowSet);
            answer.putAll(nowSet);
        }

        List<Map.Entry<Set<String>, Integer>> list = new ArrayList<>(answer.entrySet());

        Collections.sort(list, new Comparator<Map.Entry<Set<String>, Integer>>() {
            public int compare(Map.Entry<Set<String>, Integer> o1, Map.Entry<Set<String>, Integer> o2) {
                return o1.getValue().compareTo(o2.getValue());
            }
        });

        for (Map.Entry<Set<String>, Integer> entry : list) {
            System.out.println(entry.getKey() + ": " + (double)entry.getValue()/t_num);
        }

        long afterTime = System.currentTimeMillis();    
        long secDiffTime = (afterTime - beforeTime);
        System.out.println("runTime(ms) : "+secDiffTime);  
    }
}