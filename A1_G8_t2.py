class TreeNode:
    def __init__(self, name, count, parent):
        self.name = name  # 항목 이름
        self.count = count  # 항목의 빈도
        self.node_link = None  # 동일한 항목을 가진 다음 노드에 대한 링크
        self.parent = parent  # 부모 노드
        self.children = {}  # 자식 노드들

    def increase_count(self, count):
        self.count += count

def create_tree(dataset, min_support):
    header_table = {}
    for transaction in dataset:
        for item in transaction:
            header_table[item] = header_table.get(item, 0) + dataset[transaction]

    for item in list(header_table.keys()):
        if header_table[item] < min_support:
            del(header_table[item])

    frequent_items = set(header_table.keys())
    if len(frequent_items) == 0:
        return None, None

    for item in header_table:
        header_table[item] = [header_table[item], None]

    fp_tree = TreeNode('Null Set', 1, None)
    for transaction, count in dataset.items():
        local_d = {}
        for item in transaction:
            if item in frequent_items:
                local_d[item] = header_table[item][0]
        if len(local_d) > 0:
            ordered_items = [v[0] for v in sorted(local_d.items(), key=lambda p: p[1], reverse=True)]
            update_tree(ordered_items, fp_tree, header_table, count)

    return fp_tree, header_table

def update_tree(items, in_tree, header_table, count):
    if items[0] in in_tree.children:
        in_tree.children[items[0]].increase_count(count)
    else:
        in_tree.children[items[0]] = TreeNode(items[0], count, in_tree)
        if header_table[items[0]][1] is None:
            header_table[items[0]][1] = in_tree.children[items[0]]
        else:
            update_header(header_table[items[0]][1], in_tree.children[items[0]])
    if len(items) > 1:
        update_tree(items[1:], in_tree.children[items[0]], header_table, count)

def update_header(node_to_test, target_node):
    while node_to_test.node_link is not None:
        node_to_test = node_to_test.node_link
    node_to_test.node_link = target_node

def ascend_tree(leaf_node, prefix_path):
    if leaf_node.parent is not None:
        prefix_path.append(leaf_node.name)
        ascend_tree(leaf_node.parent, prefix_path)

def find_prefix_path(base_pat, tree_node):
    conditional_patterns = {}
    while tree_node is not None:
        prefix_path = []
        ascend_tree(tree_node, prefix_path)
        if len(prefix_path) > 1:
            conditional_patterns[frozenset(prefix_path[1:])] = tree_node.count
        tree_node = tree_node.node_link
    return conditional_patterns

def mine_tree(in_tree, header_table, min_support, pre_fix, frequent_item_list):
    big_l = [v[0] for v in sorted(header_table.items(), key=lambda p: p[1][0])]
    for base_pat in big_l:
        new_freq_set = pre_fix.copy()
        new_freq_set.add(base_pat)
        support = header_table[base_pat][0]
        frequent_item_list.append((new_freq_set, support))  # frequent_item_list에 (항목 집합, 지지도) 튜플을 추가
        conditional_patterns = find_prefix_path(base_pat, header_table[base_pat][1])
        my_cond_tree, my_head = create_tree(conditional_patterns, min_support)
        if my_head is not None:
            mine_tree(my_cond_tree, my_head, min_support, new_freq_set, frequent_item_list)


def fp_growth(dataset, min_support):
    fp_tree, header_table = create_tree(dataset, min_support)
    frequent_item_list = []
    mine_tree(fp_tree, header_table, min_support, set([]), frequent_item_list)
    return frequent_item_list

import argparse
import csv

def read_dataset_from_csv(filename):
    lines = 0
    dataset = {}
    with open(filename, 'r', newline='') as csvfile:
        csvreader = csv.reader(csvfile)
        for row in csvreader:
            lines += 1
            items = frozenset(row)
            if items in dataset:
                dataset[items] += 1
            else:
                dataset[items] = 1
    return dataset, lines

def print_frequent_itemsets(frequent_itemsets, length):
    sorted_itemsets = sorted(frequent_itemsets, key=lambda x: x[1])  # 지지도를 기준으로 오름차순 정렬
    for itemset, support in sorted_itemsets:
        itemset_str = ', '.join(itemset)  # 항목을 쉼표로 구분하여 문자열로 변환
        print(itemset_str, support / length)


if __name__ == "__main__":
    # 커맨드 라인 인자 파싱
    parser = argparse.ArgumentParser(description='Run FP-Growth algorithm on a dataset.')
    parser.add_argument('filename', type=str, help='Path to the CSV file containing the dataset')
    parser.add_argument('min_support', type=float, help='Minimum support threshold')
    args = parser.parse_args()

    # 데이터셋 읽기
    dataset, line = read_dataset_from_csv(args.filename)
    print(args.filename, args.min_support, line)
    # FP-Growth 알고리즘 실행
    frequent_itemsets = fp_growth(dataset, args.min_support * line)
    print_frequent_itemsets(frequent_itemsets, line)

