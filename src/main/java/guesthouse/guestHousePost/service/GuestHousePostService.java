package guesthouse.guestHousePost.service;

import guesthouse.common.exception.GuestHouseException;
import guesthouse.guestHousePost.domain.model.*;
import guesthouse.guestHousePost.domain.vo.GuestHouseFilter;
import guesthouse.guestHousePost.domain.vo.Status;
import guesthouse.guestHousePost.dto.*;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import guesthouse.guestHousePost.dto.response.OwnerGuestHousePostDto;
import guesthouse.guestHousePost.dto.response.OwnerGuestHousePostsResponse;
import guesthouse.guestHousePost.exception.GuestHousePostErrorCode;
import guesthouse.guestHousePost.exception.GuestHousePostException;
import guesthouse.guestHousePost.repository.*;
import guesthouse.staffrecruitment.domain.model.StaffRecruitment;
import guesthouse.staffrecruitment.domain.vo.Region;
import guesthouse.staffrecruitment.exception.StaffRecruitmentErrorCode;
import guesthouse.staffrecruitment.exception.StaffRecruitmentException;
import guesthouse.user.service.UserService;
import guesthouse.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestHousePostService {


    private final GuestHousePostRepository guestHousePostRepository;
    private final GuestHousePostImageRepository guestHousePostImageRepository;
    private final AmenityRepository amenityRepository;
    private final PartyRepository partyRepository;
    private final PartyImageRepository partyImageRepository;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;
    private final WishService wishService;

    @Transactional(readOnly = true)
    public GuestHousePostsResponse getGuestHousePosts(Long userId, int pageNumber, GuestHouseFilter filter) {
        Pageable pageable = PageRequest.of(pageNumber, 10);

        List<GuestHousePost> guestHousePosts = guestHousePostRepository.searchByFilter(pageable, filter);

        List<GuestHousePostDto> dtos = guestHousePosts.stream()
                .map(guestHousePost -> createDto(guestHousePost, userId))
                .toList();

        return new GuestHousePostsResponse(dtos);
    }

    private GuestHousePostDto createDto(GuestHousePost guestHousePost, Long userId) {
        GuestHousePostImage image = guestHousePostImageRepository.getFirstImageByGuestHousePostId(guestHousePost.getId());
        return GuestHousePostDto.of(
                guestHousePost,
                isWished(guestHousePost.getId(), userId),
                image.getImageUrl()
        );
    }

    private Boolean isWished(Long staffRecruitmentId, Long userId) {
        if (isGuest(userId))
            return false;

        return wishService.isWished(userId, staffRecruitmentId);
    }

    private boolean isGuest(Long userId) {
        return userId == null;
    }

    public RandomGuestHousePostsResponse getRandomGuestHousePosts(Region region) {
        List<GuestHousePost> guestHousePosts = guestHousePostRepository.findRandom(10, region);
        List<RandomGuestHousePostDto> dtos = guestHousePosts.stream()
                .map(this::createRandomDto)
                .toList();
        return new RandomGuestHousePostsResponse(dtos);
    }

    private RandomGuestHousePostDto createRandomDto(GuestHousePost guestHousePost) {
        GuestHousePostImage image = guestHousePostImageRepository.getFirstImageByGuestHousePostId(guestHousePost.getId());
        return RandomGuestHousePostDto.of(guestHousePost, image.getImageUrl());
    }

    @Transactional
    public Long create(GuestHouseCreateRequest request, Long userId) {
        GuestHousePost post = GuestHouseMapper.toPost(request, userId);
        guestHousePostRepository.save(post);
        Long postId = post.getId();

        List<GuestHousePostImage> postImages = ImageMapper.toGuestHousePostImages(request.imageUrls(), postId);
        guestHousePostImageRepository.saveAll(postImages);

        List<Amenity> amenities = AmenityMapper.toAmenities(request.amenities(), postId);
        amenityRepository.saveAll(amenities);

        saveParties(request.parties(), postId);
        saveRooms(request.rooms(), postId);

        return postId;
    }

    private void saveParties(List<GuestHouseCreateRequest.Party> parties, Long postId) {
        parties
                .forEach(party -> {
                    Party partyEntity = PartyMapper.toParty(party, postId);
                    partyRepository.save(partyEntity);
                    List<PartyImage> partyImages = ImageMapper.toPartyImage(party.imageUrls(), partyEntity.getId());
                    partyImageRepository.saveAll(partyImages);
                });
    }

    private void saveRooms(List<GuestHouseCreateRequest.Room> rooms, Long postId) {
        rooms
                .forEach(room -> {
                    Room roomEntity = RoomMapper.toRoom(room, postId);
                    roomRepository.save(roomEntity);
                    List<RoomImage> roomImages = ImageMapper.toRoomImage(room.imageUrls(), roomEntity.getId());
                    roomImageRepository.saveAll(roomImages);
                });
    }

    @Transactional(readOnly = true)
    public GuestHousePostDetailsDTO getDetails(Long guestHousePostId) {
        GuestHousePost guestHousePost = getGuestHousePostById(guestHousePostId);
        List<String> imageUrls = getGuestHousePostImageUrlsByPostId(guestHousePostId);
        List<Amenity> amenities = getAmenitiesByPostId(guestHousePostId);
        List<PartyWithImageUrlDTO> parties = getPartiesWithImageUrlByPostId(guestHousePostId);
        List<RoomWithImageUrlDTO> rooms = getRoomsWithImageUrlByPostId(guestHousePostId);

        return GuestHousePostDetailsDTO.from(guestHousePost, imageUrls, amenities, parties, rooms);
    }

    @Transactional(readOnly = true)
    public GuestHousePost getGuestHousePostById(Long guestHousePostId) {
        return guestHousePostRepository.findById(guestHousePostId)
                .orElseThrow(() -> new IllegalArgumentException("게스트하우스 게시글이 존재하지 않습니다"));
    }

    @Transactional(readOnly = true)
    public List<String> getGuestHousePostImageUrlsByPostId(Long guestHousePostId) {
        return guestHousePostImageRepository.findByGuestHousePostId(guestHousePostId)
                .stream()
                .sorted(Comparator.comparing(GuestHousePostImage::getIndex))
                .map(GuestHousePostImage::getImageUrl)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Amenity> getAmenitiesByPostId(Long guestHousePostId) {
        return amenityRepository.findByGuestHousePostId(guestHousePostId);
    }

    @Transactional(readOnly = true)
    public List<PartyWithImageUrlDTO> getPartiesWithImageUrlByPostId(Long guestHousePostId) {
        List<Party> parties = partyRepository.findByGuestHousePostId(guestHousePostId);

        return parties
                .stream()
                .map(party -> {
                    List<String> imageUrls = getPartyImageUrlsByPartyId(party.getId());
                    return PartyWithImageUrlDTO.from(party, imageUrls);
                })
                .toList();
    }

    private List<String> getPartyImageUrlsByPartyId(Long partyId) {
        return partyImageRepository.findByPartyId(partyId)
                .stream()
                .sorted(Comparator.comparing(PartyImage::getIndex))
                .map(PartyImage::getImageUrl)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RoomWithImageUrlDTO> getRoomsWithImageUrlByPostId(Long guestHousePostId) {
        List<Room> rooms = roomRepository.findByGuestHousePostId(guestHousePostId);

        return rooms
                .stream()
                .map(room -> {
                    List<String> imageUrls = getRoomImageUrlsByPartyId(room.getId());
                    return RoomWithImageUrlDTO.from(room, imageUrls);
                })
                .toList();
    }

    private List<String> getRoomImageUrlsByPartyId(Long roomId) {
        return roomImageRepository.findByRoomId(roomId)
                .stream()
                .sorted(Comparator.comparing(RoomImage::getIndex))
                .map(RoomImage::getImageUrl)
                .toList();
    }

    public OwnerGuestHousePostsResponse getOwnGuestHousePosts(Long userId) {
        List<GuestHousePost> guestHousePosts = guestHousePostRepository.findByOwnerId(userId);
        List<String> imageUrls = guestHousePosts.stream()
                .map(p -> guestHousePostImageRepository.getFirstImageByGuestHousePostId(p.getId()))
                .map(i -> i.getImageUrl())
                .toList();
        List<OwnerGuestHousePostDto> dtos = OwnerGuestHousePostDto.of(guestHousePosts, imageUrls);
        return new OwnerGuestHousePostsResponse(dtos);
    }

    @Transactional
    public void changeStatus(Status status, Long userId, Long guestHousePostId) {
        GuestHousePost guestHousePost = getGuestHousePostById(guestHousePostId);

        if (!existsByOwnerIdAndId(userId, guestHousePostId))
            throw new GuestHousePostException(GuestHousePostErrorCode.NOT_FOUND);

        guestHousePost.changeStatus(status);
    }

    public boolean existsByOwnerIdAndId(Long userId, Long guestHousePostId) {
        return guestHousePostRepository.existsByOwnerIdAndId(userId, guestHousePostId);
    }

    @Transactional
    public void deleteGuestHousePost(Long userId, Long guestHousePostId) {
        if (!existsByOwnerIdAndId(userId, guestHousePostId))
            throw new GuestHousePostException(GuestHousePostErrorCode.NOT_FOUND);

        List<Party> parties = partyRepository.findByGuestHousePostId(guestHousePostId);
        for (Party party : parties) {
            partyImageRepository.deleteByPartyId(party.getId());
        }
        partyRepository.deleteByGuestHousePostId(guestHousePostId);

        List<Room> rooms = roomRepository.findByGuestHousePostId(guestHousePostId);
        for (Room room : rooms) {
            roomImageRepository.deleteByRoomId(room.getId());
        }
        roomRepository.deleteByGuestHousePostId(guestHousePostId);

        amenityRepository.deleteByGuestHousePostId(guestHousePostId);

        guestHousePostImageRepository.deleteByGuestHousePostId(guestHousePostId);
        guestHousePostRepository.deleteById(guestHousePostId);
    }

}
