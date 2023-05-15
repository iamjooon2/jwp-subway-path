package subway.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class Sections {
    private static final int CLEAR_SECTIONS_SIZE = 1;

    private final List<Section> sections;

    public Sections(final List<Section> sections) {
        this.sections = new ArrayList<>(sections);
    }

    public void register(final Station source, final Station target, final int distance) {
        if (sections.isEmpty()) {
            sections.add(new Section(source, target, distance));
            return;
        }
        validateRegister(source, target);
        final Station existence = getExistingStation(source, target);
        if (existence.equals(source)) {
            registerTargetStation(existence, target, distance);
        }
        if (existence.equals(target)) {
            registerSourceStation(existence, source, distance);
        }
    }

    private void validateRegister(final Station source, final Station target) {
        if (doesNotHave(source) && doesNotHave(target)) {
            throw new IllegalArgumentException("기준역이 존재하지 않아 추가할 수 없습니다.");
        }
        if (have(source) && have(target)) {
            throw new IllegalArgumentException("두 역 모두 노선에 존재하는 역입니다.");
        }
    }

    private boolean doesNotHave(final Station station) {
        return !have(station);
    }

    private boolean have(final Station station) {
        return sections.stream()
                .anyMatch(section -> section.have(station));
    }

    private Station getExistingStation(final Station source, final Station target) {
        if (have(source)) {
            return source;
        }
        return target;
    }

    private void registerTargetStation(final Station existence, final Station additional, final int distance) {
        if (isTargetDistanceUnRegistrable(existence, distance)) {
            throw new IllegalArgumentException("등록하려는 구간의 거리는 기존 구간의 거리보다 짧아야 합니다.");
        }
        final Optional<Section> foundSection = findSourceSection(existence);
        if (foundSection.isPresent()) {
            changeDistance(additional, foundSection.get().getTarget(), foundSection.get(), distance);
        }
        sections.add(new Section(existence, additional, distance));
    }

    private boolean isTargetDistanceUnRegistrable(final Station existence, final int distance) {
        final Optional<Section> foundSourceSection = findSourceSection(existence);
        return foundSourceSection.map(section -> section.isLongOrEqualThan(distance))
                .orElse(false);
    }

    private Optional<Section> findSourceSection(final Station existence) {
        return sections.stream()
                .filter(section -> section.isSource(existence))
                .findAny();
    }

    private void changeDistance(final Station source, final Station target, final Section oldSection, final int distance) {
        sections.add(new Section(source, target, oldSection.getDistance() - distance));
        sections.remove(oldSection);
    }

    private void registerSourceStation(final Station existence, final Station additional, final int distance) {
        if (isSourceDistanceUnRegistrable(existence, distance)) {
            throw new IllegalArgumentException("등록하려는 구간의 거리는 기존 구간의 거리보다 짧아야 합니다.");
        }
        final Optional<Section> foundSection = findTargetSection(existence);
        if (foundSection.isPresent()) {
            changeDistance(foundSection.get().getSource(), additional, foundSection.get(), distance);
        }
        sections.add(new Section(additional, existence, distance));
    }

    private boolean isSourceDistanceUnRegistrable(final Station existence, final int distance) {
        final Optional<Section> foundSection = findTargetSection(existence);
        return foundSection.map(section -> section.isLongOrEqualThan(distance))
                .orElse(false);
    }

    private Optional<Section> findTargetSection(final Station existence) {
        return sections.stream()
                .filter(section -> section.isTarget(existence))
                .findAny();
    }

    public void delete(final Station station) {
        if (doesNotHave(station)) {
            throw new IllegalArgumentException("존재하지 않는 역을 삭제할 수 없습니다.");
        }
        if (sections.size() == CLEAR_SECTIONS_SIZE) {
            sections.clear();
            return;
        }
        final Optional<Section> foundTargetSection = findTargetSection(station);
        final Optional<Section> foundSourceSection = findSourceSection(station);
        if (foundSourceSection.isPresent() && foundTargetSection.isPresent()) {
            mergeSections(foundTargetSection, foundSourceSection);
            return;
        }
        deleteLastStation(foundTargetSection, foundSourceSection);
    }

    private void mergeSections(final Optional<Section> foundTargetSection, final Optional<Section> foundSourceSection) {
        final int newDistance = foundTargetSection.get().getDistance() + foundSourceSection.get().getDistance();
        sections.add(new Section(foundTargetSection.get().getSource(), foundSourceSection.get().getTarget(), newDistance));
        sections.remove(foundTargetSection.get());
        sections.remove(foundSourceSection.get());
    }

    private void deleteLastStation(final Optional<Section> upSection, final Optional<Section> downSection) {
        if (downSection.isPresent()) {
            sections.remove(downSection.get());
            return;
        }
        if (upSection.isPresent()) {
            sections.remove(upSection.get());
        }
    }

    public List<Station> getOrderedStations() {
        final Map<Station, Station> stationsChain = sectionsToChain(sections);
        final Optional<Station> firstStation = getFirstStation(stationsChain);
        return firstStation.map(station -> getSortedStations(stationsChain, station))
                .orElse(Collections.emptyList());
    }

    private Map<Station, Station> sectionsToChain(final List<Section> sections) {
        return sections.stream()
                .collect(Collectors.toMap(Section::getSource, Section::getTarget));
    }

    private Optional<Station> getFirstStation(final Map<Station, Station> stationsChain) {
        return stationsChain.keySet().stream()
                .filter(station -> !stationsChain.containsValue(station))
                .findFirst();
    }

    private List<Station> getSortedStations(final Map<Station, Station> stationsChain, final Station startStation) {
        final List<Station> sortedStations = new ArrayList<>();
        Optional<Station> nextStation = Optional.ofNullable(startStation);
        while (nextStation.isPresent()) {
            sortedStations.add(nextStation.get());
            nextStation = Optional.ofNullable(stationsChain.get(nextStation.get()));
        }
        return sortedStations;
    }

    public List<Section> get() {
        return sections;
    }
}
