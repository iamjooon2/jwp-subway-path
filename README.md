# jwp-subway-path

# 도메인

- [x] 역
  - [x] 3~10글자 사이의 이름을 가진다.

- [ ] 노선
  - [x] 3~10글자 사이의 이름을 가진다.
  - [x] 구간을 추가할 수 있다.
    - [x] 첫 노선은 구간이 비어있을 때 등록 가능하다. 
    - [x] 기준 역이 노선에 존재하지 않으면 예외가 발생한다.
    - [x] 추가될 역이 노선에 존재하면 예외가 발생한다.
    - [x] 기존 역을 포함한 구간 추가시 거리정보를 고려한다.
      - [x] A-B-C 노선에서 B 다음에 D 역을 추가하려 할 때 B-C 구간이 3, B-D 구간이 2라면 B-D 거리는 2로 바뀌고 D-C 거리는 1로 추가되어야 한다.
    - [x] 추가할 역이 종점이 되는 경우 거리정보를 고려하지 않는다.
  - [x] 구간을 제거할 수 있다.
    - [x] 제거할 역이 중간에 존재하는 역이라면, 해당 역을 포함한 구간을 제거하고 남은 역과 거리정보를 합쳐 새로운 구간을 만든다.
    - [x] 제거할 역이 종점이면 그냥 제거한다.
  - [ ] 노선에 포함된 역들을 순서대로 조회할 수 있다.
  - [ ] 모든 노선을 조회할 수 있다.

- [x] 구간
  - [x] 시작역, 도착역, 거리를 가진다.
  - [x] 시작역, 도착역은 동일할 수 없다.
  - [x] 거리는 1보다 작을 수 없다.
 