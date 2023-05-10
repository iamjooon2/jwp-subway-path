package subway.domain;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sections {

    private final List<Section> sections;

    public Sections(final List<Section> sections) {
        this.sections = sections;
    }

    public void add(final Section requestSection) {
        if (!sections.isEmpty()) {
            validateNonExists(requestSection);
        }
        sections.add(requestSection);
    }

    private void validateNonExists(final Section requestSection) {
        final List<Station> stations = sections.stream()
                .flatMap(section -> Stream.of(section.getSource(), section.getTarget()))
                .collect(Collectors.toUnmodifiableList());

        final Station sourceRequest = requestSection.getSource();
        final Station targetRequest = requestSection.getTarget();

        if (!stations.contains(sourceRequest) && !stations.contains(targetRequest)) {
            throw new IllegalArgumentException("존재하지 않는 역을 추가할 수 없습니다");
        }

        search(requestSection);
    }

    private void search(final Section requestSection) {
        final Station sourceRequest = requestSection.getSource();
        final Station targetRequest = requestSection.getTarget();

        // 요청한 끝 역이 상행 종점이라면 시작 역이 상행 종점
        if (!sections.isEmpty() && sections.get(0).getSource().equals(targetRequest)) {
            sections.add(0, requestSection);
            return;
        }

        // 요청한 시작 역이 하행 종점이라면 끝 역이 하행 종점
        if (!sections.isEmpty() && sections.get(sections.size()-1).getTarget().equals(sourceRequest)) {
            sections.add(sections.size(), requestSection);
            return;
        }

        searchSources(requestSection, sourceRequest);
        searchTargets(requestSection, targetRequest);
    }

    private void searchSources(final Section requestSection, final Station sourceRequest) {
        // 요청한 시작 역이 이미 노선에 존재함
        // A B -> (10) -> C, 요청 : B -> (3) D
        // 원하는 결과: A -> B -> (3) D -> (7) C
        for (Section section : sections) {
            if (section.getSource().equals(sourceRequest)) {
                // 존재하는 역의 하행방향 가중치
                final int distance = section.getDistance();
                // 입력받은 거리
                final int requestDistance = requestSection.getDistance();

                // 입력받은 거리가 더 크거나 같으면 예외 발생
                if (requestDistance >= distance) {
                    throw new IllegalArgumentException("거리가 너무 커서 역을 추가할 수 없습니다.");
                }

                // 더 작다면 노선에 추가한다.
                // 가중치 정보는 기존의 가중치에서 입력받은 거리를 뺀 값으로 갱신한다.
                final int newDistance = distance - requestDistance;

                // 기존에 존재하던 구간 제거 (B->C)
                sections.remove(section);

                // 새로운 구간 추가 (B->D, D->C)
                final Section newSection1 = new Section(requestSection.getSource(), requestSection.getTarget(), requestDistance);
                final Section newSection2 = new Section(requestSection.getTarget(), section.getTarget(), newDistance);
                sections.add(newSection1);
                sections.add(newSection2);
            }
        }
    }

    private void searchTargets(final Section requestSection, final Station targetRequest) {
        // 요청한 끝 역이 이미 노선에 존재함
        // A B -> (10) -> C, 요청 : D -> (3) C
        // 원하는 결과: A -> B -> (7) D -> (3) C
        for (Section section : sections) {
            if (section.getTarget().equals(targetRequest)) {
                // 존재하는 역의 하행방향 가중치
                final int distance = section.getDistance();
                // 입력받은 거리
                final int requestDistance = requestSection.getDistance();

                // 입력받은 거리가 더 크거나 같으면 예외 발생
                if (requestDistance >= distance) {
                    throw new IllegalArgumentException("거리가 너무 커서 역을 추가할 수 없습니다.");
                }

                // 더 작다면 노선에 추가한다.
                // 가중치 정보는 기존의 가중치에서 입력받은 거리를 뺀 값으로 갱신한다.
                final int newDistance = distance - requestDistance;

                // 기존에 존재하던 구간 제거 (B->C)
                sections.remove(section);

                // 새로운 구간 추가 (B->D, D->C)
                final Section newSection1 = new Section(section.getSource(), requestSection.getSource(), newDistance);
                final Section newSection2 = new Section(requestSection.getSource(), requestSection.getTarget(), distance);
                sections.add(newSection1);
                sections.add(newSection2);
            }
        }
    }

    public List<Section> getSections() {
        return sections;
    }
}
