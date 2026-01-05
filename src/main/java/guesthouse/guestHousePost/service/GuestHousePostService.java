package guesthouse.guestHousePost.service;

import guesthouse.guestHousePost.domain.model.*;
import guesthouse.guestHousePost.dto.request.GuestHouseCreateRequest;
import guesthouse.guestHousePost.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
