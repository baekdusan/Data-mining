###### 24 - 1 UNIST CSE
# Intro to Data mining Assignment1
##### Group 8 (문정우, 이형준, 백두산)

***

## HOW TO IMPLEMENT

### Apriori
- java 구현 완료

---

- 과제 제공 Data
```bash
java A1_G8_t1.java ./groceries.csv 0.15
```
- ppt 자료 예시 데이터
```bash
java A1_G8_t1.java ./ex2.csv 0.6 
```
- 랜덤 샘플링 정수 데이터
```bash
java A1_G8_t1.java ./numbers.csv 0.3
```
---
- 데이터 셋 고정, support 값을 변경하는 실험
```bash
java A1_G8_t1_test.java ./groceries.csv 0.01 9835
java A1_G8_t1_test.java ./groceries.csv 0.02 9835
java A1_G8_t1_test.java ./groceries.csv 0.03 9835
java A1_G8_t1_test.java ./groceries.csv 0.04 9835
java A1_G8_t1_test.java ./groceries.csv 0.05 9835
java A1_G8_t1_test.java ./groceries.csv 0.06 9835
java A1_G8_t1_test.java ./groceries.csv 0.07 9835
```
- support 값 고정, 데이터 셋의 크기를 1000, 3000, 5000, ... 9000 변경하는 실험
```bash
java A1_G8_t1_test.java ./groceries.csv 0.05 1000
java A1_G8_t1_test.java ./groceries.csv 0.05 3000
java A1_G8_t1_test.java ./groceries.csv 0.05 5000
java A1_G8_t1_test.java ./groceries.csv 0.05 7000
java A1_G8_t1_test.java ./groceries.csv 0.05 9000
```
두 실험의 결괏값은 해당 알고리즘의 실제적인 러닝타임이다.

---

### FP growth
- python 구현 -> java로 변경 계획하였으나, <python 완료, java 실패>인 관계로 둘 다 제출합니다.
- 다행히 해당 Part는 java -> python 외에 나머지 기준은 모두 만족합니다.

---
- 과제 제공 Data
```bash
python3 A1_G8_t2.py ./groceries.csv 0.15
//  or 'python'
```
- ppt 자료 예시 데이터
```bash
python3 A1_G8_t2.py ./ex2.csv 0.6 
```
- 랜덤 샘플링 정수 데이터
```bash
python3 A1_G8_t2.py ./numbers.csv 0.3
```
---
- 데이터 셋 고정, support 값을 변경하는 실험
```bash
python3 A1_G8_t2_test.py ./groceries.csv 0.01 0
python3 A1_G8_t2_test.py ./groceries.csv 0.02 0
python3 A1_G8_t2_test.py ./groceries.csv 0.03 0
python3 A1_G8_t2_test.py ./groceries.csv 0.04 0
python3 A1_G8_t2_test.py ./groceries.csv 0.05 0
python3 A1_G8_t2_test.py ./groceries.csv 0.06 0
python3 A1_G8_t2_test.py ./groceries.csv 0.07 0
```
- support 값 고정, 데이터 셋의 크기를 1000, 3000, 5000, ... 9000 변경하는 실험
```bash
python3 A1_G8_t2_test.py ./groceries.csv 0.05 1000
python3 A1_G8_t2_test.py ./groceries.csv 0.05 3000
python3 A1_G8_t2_test.py ./groceries.csv 0.05 5000
python3 A1_G8_t2_test.py ./groceries.csv 0.05 7000
python3 A1_G8_t2_test.py ./groceries.csv 0.05 9000
```
두 실험의 결괏값은 해당 알고리즘의 실제적인 러닝타임이다.

---

