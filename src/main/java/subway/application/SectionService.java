package subway.application;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.dao.LineDao;
import subway.dao.SectionDao;
import subway.dao.StationDao;
import subway.dao.entity.SectionEntity;
import subway.domain.line.Line;
import subway.domain.section.Section;
import subway.domain.section.SectionType;
import subway.domain.section.Sections;
import subway.domain.station.Station;
import subway.dto.LineResponse;
import subway.dto.SectionRequest;
import subway.dto.StationResponse;

@Service
public class SectionService {

    private final LineDao lineDao;
    private final SectionDao sectionDao;
    private final StationDao stationDao;

    public SectionService(final LineDao lineDao, final SectionDao sectionDao, final StationDao stationDao) {
        this.lineDao = lineDao;
        this.sectionDao = sectionDao;
        this.stationDao = stationDao;
    }

    @Transactional
    public void saveSection(final SectionRequest sectionRequest) {
        final Line line = getLineById(sectionRequest.getLineId());
        final List<SectionEntity> allSections = sectionDao.findByLineId(line.getId());
        final Sections sections = convertSections(allSections);

        if (sections.isEmpty()) {
            saveFirstSection(sectionRequest, line.getId());
            return;
        }

        final Section section = convertSection(sectionRequest, SectionType.NORMAL);
        sections.validateSections(section);

        final Station targetStation = getStationById(sectionRequest.getTargetStationId());
        final Station sourceStation = getStationById(sectionRequest.getSourceStationId());

        if (sections.isTargetUpward(targetStation)) {
            updateFirstSection(line.getId(), targetStation.getId());
            saveNewSection(line.getId(), sourceStation.getId(), targetStation.getId(),
                section.getDistance(), SectionType.UPWARD);
        }

        if (sections.isSourceDownward(sourceStation)) {
            saveNewSection(line.getId(), sourceStation.getId(), targetStation.getId(),
                section.getDistance(), SectionType.NORMAL);
        }

        updateExistedSourceSection(line.getId(), sections, section, targetStation.getId(), sourceStation.getId());
        updateExistedTargetSection(line.getId(), sections, section, targetStation.getId(), sourceStation.getId());
    }

    public LineResponse getStationsByLineId(final Long lineId) {
        final Line line = getLineById(lineId);
        final List<SectionEntity> lineEntities = sectionDao.findByLineId(lineId);
        final Sections sections = convertSections(lineEntities);
        final List<Station> stations = sections.getStations();
        final List<StationResponse> stationResponses = convertStationResponses(stations);
        return LineResponse.of(line.getId(), line.getName(), line.getColor(), stationResponses);
    }

    private void updateFirstSection(final Long lineId, final Long targetStationId) {
        final int updatedCount = sectionDao.updateSectionTypeByLineIdAndSourceStationId(lineId,
            targetStationId, SectionType.NORMAL.name());
        if (updatedCount != 1) {
            throw new IllegalArgumentException("DB 업데이트 중 예외가 발생했습니다.");
        }
    }

    private Line getLineById(final Long lineId) {
        return lineDao.findById(lineId)
            .orElseThrow(() -> new IllegalArgumentException("해당하는 노선이 없습니다."));
    }

    private Station getStationById(final Long stationId) {
        return stationDao.findById(stationId)
            .orElseThrow(() -> new IllegalArgumentException("해당하는 역이 없습니다."));
    }

    private List<StationResponse> convertStationResponses(final List<Station> stations) {
        return stations.stream()
            .map(StationResponse::of)
            .collect(Collectors.toUnmodifiableList());
    }

    private Section convertSection(final SectionEntity sectionEntity) {
        return new Section(
            getStationById(sectionEntity.getSourceStationId()),
            getStationById(sectionEntity.getTargetStationId()),
            sectionEntity.getDistance(), SectionType.from(sectionEntity.getSectionType()));
    }

    private Section convertSection(final SectionRequest sectionRequest, final SectionType sectionType) {
        return new Section(
            getStationById(sectionRequest.getSourceStationId()),
            getStationById(sectionRequest.getTargetStationId()),
            sectionRequest.getDistance(), sectionType);
    }

    private Sections convertSections(final List<SectionEntity> sectionEntities) {
        final List<Section> sections = sectionEntities.stream()
            .map(this::convertSection)
            .collect(Collectors.toList());
        return new Sections(sections);
    }

    private void saveFirstSection(final SectionRequest sectionRequest, final Long lineId) {
        final Section section = convertSection(sectionRequest, SectionType.UPWARD);
        saveNewSection(lineId, section.getSource().getId(), section.getTarget().getId(),
            section.getDistance(), section.getSectionType());
    }

    private void updateExistedSourceSection(final Long lineId, final Sections sections,
                                            final Section section, final Long targetSectionId, final Long sourceStationId) {
        sections.getExistsSectionOfSource(section).ifPresent(oldSection -> {
            deleteOldSection(lineId, sourceStationId);
            final int newDistance = oldSection.getDistance() - section.getDistance();
            saveNewSection(lineId, sourceStationId, targetSectionId, section.getDistance(), oldSection.getSectionType());
            saveNewSection(lineId, targetSectionId, oldSection.getTarget().getId(), newDistance, SectionType.NORMAL);
        });
    }


    private void updateExistedTargetSection(final Long lineId, final Sections sections,
                                            final Section section, final Long targetStationId, final Long sourceStationId) {
        sections.getExistsSectionOfTarget(section).ifPresent(oldSection -> {
            deleteOldSection(lineId, targetStationId);
            final int newDistance = oldSection.getDistance() - section.getDistance();
            saveNewSection(lineId, oldSection.getSource().getId(), sourceStationId, newDistance, SectionType.NORMAL);
            saveNewSection(lineId, oldSection.getSource().getId(), targetStationId, section.getDistance(), SectionType.NORMAL);
        });
    }
    private void saveNewSection(final Long lineId, final Long sourceStationId, final Long targetStationId,
                                final int distance, final SectionType sectionType) {
        final SectionEntity newSectionEntity = new SectionEntity(lineId, sourceStationId,
            targetStationId, distance, sectionType.name());
        sectionDao.insert(newSectionEntity);
    }

    private void deleteOldSection(final Long lineId, final Long sourceStationId) {
        final int deleteCount = sectionDao.deleteByLineIdAndSourceStationId(lineId, sourceStationId);
        if (deleteCount != 1) {
            throw new IllegalArgumentException("DB 삭제가 정상적으로 진행되지 않았습니다.");
        }
    }

    public List<LineResponse> getAllStations() {
        final List<Line> lines = lineDao.findAll();
        return lines.stream()
            .map(line -> getStationsByLineId(line.getId()))
            .collect(Collectors.toUnmodifiableList());
    }
}
