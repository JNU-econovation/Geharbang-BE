package guesthouse.guestHousePost.service;

import guesthouse.guestHousePost.domain.model.*;
import guesthouse.guestHousePost.dto.GuestHousePostDetailsDTO;
import guesthouse.guestHousePost.dto.PartyWithImageUrlDTO;
import guesthouse.guestHousePost.dto.RoomWithImageUrlDTO;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import guesthouse.guestHousePost.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GuestHousePostService {

    private final AmenityRepository amenityRepository;
    private final GuestHousePostRepository guestHousePostRepository;
    private final GuestHousePostImageRepository guestHousePostImageRepository;
    private final PartyRepository partyRepository;
    private final PartyImageRepository partyImageRepository;
    private final RoomRepository roomRepository;
    private final RoomImageRepository roomImageRepository;

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
        List<String> images = getGuestHousePostImageUrlsByPostId(guestHousePostId);
        List<Amenity> amenities = getAmenitiesByPostId(guestHousePostId);
        List<PartyWithImageUrlDTO> parties = getPartiesWithImageUrlByPostId(guestHousePostId);
        List<RoomWithImageUrlDTO> rooms = getRoomsWithImageUrlByPostId(guestHousePostId);

        return GuestHousePostDetailsDTO.from(guestHousePost, images, amenities, parties, rooms);
    }

    @Transactional(readOnly = true)
    public GuestHousePost getGuestHousePostById(Long guestHousePostId) {
        return guestHousePostRepository.findById(guestHousePostId)
                .orElseThrow(()-> new IllegalArgumentException("게스트하우스 게시글이 존재하지 않습니다"));
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

}
