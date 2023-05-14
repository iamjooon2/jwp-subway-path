package subway.domain;

import java.util.ArrayList;
import java.util.List;

public class Line {
    private static final int MIN_NAME_LENGTH = 3;
    private static final int MAX_NAME_LENGTH = 10;

    private final String name;
    private final String color;
    private final List<Section> sections;

    public Line(final String name, final String color, final List<Section> sections) {
        validate(name);
        this.name = name;
        this.color = color;
        this.sections = new ArrayList<>(sections);
    }

    private void validate(final String name) {
        if (name.length() < MIN_NAME_LENGTH || name.length() > MAX_NAME_LENGTH) {
            throw new IllegalArgumentException(String.format("노선 이름은 %d~%d자 사이여야 합니다.", MIN_NAME_LENGTH, MAX_NAME_LENGTH));
        }
    }

    public void register(final String source, final String target, final int distance) {
        if (haveSection(source, target)) {
            throw new IllegalArgumentException("이미 존재하는 구간입니다.");
        }
    }

    private boolean haveSection(final String source, final String target) {
        return sections.stream()
                .map(section -> section.have(source, target) || section.have(target, source))
                .findAny()
                .orElse(false);
    }
}