package service;

import datatransforobject.ReviewBasicDTO;
import datatransforobject.UserCompleteProfileDTO;
import datatransforobject.UserCoreDTO;
import datatransforobject.UserLoginDTO;
import datatransforobject.UserNameIdDTO;
import datatransforobject.UserProfileDTO;
import java.util.List;
import java.util.Optional;
import static java.util.stream.Collectors.toList;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import mapper.ReviewMapper;
import mapper.UserMapper;
import model.BookingDetail;
import model.Host;
import model.Renter;
import model.Review;
import model.User;
import repository.ActiveSessionRepository;
import repository.BookingDetailRepository;
import repository.HostRepository;
import repository.RenterRepository;
import repository.ReviewRepository;
import repository.UserRepository;
import utility.ManagerFactory;
import utility.Utility;

public class UserService {

  private final EntityManagerFactory entityManagerFactory = ManagerFactory.getEntityManagerFactory(
      "User");
  private final EntityManager entityManager = entityManagerFactory.createEntityManager();
  private final UserRepository userRepository = new UserRepository(entityManager);
  private final RenterRepository renterRepository = new RenterRepository(entityManager);
  private final BookingDetailRepository bookingDetailRepository = new BookingDetailRepository(
      entityManager);
  private final ReviewRepository reviewRepository = new ReviewRepository(entityManager);
  private final HostRepository hostRepository = new HostRepository(entityManager);

  private final ActiveSessionRepository activeSessionRepository = new ActiveSessionRepository(
      entityManager);

  private final ActiveSessionService activeSessionService;

  public UserService() {
    this.activeSessionService = new ActiveSessionService();
  }

  public Optional<User> getById(String id) {
    Optional<User> userDO = userRepository.findById(id);
    return userDO;
  }

  public List<User> getAllWithEverything() {
    List<User> users = userRepository.findAll();
    users.forEach(UserMapper :: hidePasswordFromUser);
    return users;
  }

  public List<UserNameIdDTO> getAllNames() {
    List<User> users = userRepository.findAll();
    List<UserNameIdDTO> list = users.stream().map(UserMapper :: convertToNameAndId)
                                    .collect(toList());
    return list;
  }

  public Optional<UserCoreDTO> registerUser(UserCoreDTO user) {
    Optional<User> exist = userRepository.findByEmail(user.getEmail());
    if (exist.isEmpty()) {
      String hashedPassword = Utility.hash(user.getPassword());
      user.setPassword(hashedPassword);
      Optional<User> createdUser = userRepository.save(user.convertToUser());
      if (createdUser.isPresent()) {
        //To send a json
        UserCoreDTO createdUserCoreDTO = UserMapper.convertToCoreDTOWithoutPassword(
            createdUser.get());
        System.out.println(createdUserCoreDTO);
        return Optional.of(createdUserCoreDTO);
      }
    }
    return Optional.empty();
  }

  public UserCoreDTO checkUserCredentials(UserLoginDTO userLoginDTO) {
    UserCoreDTO userCoreDTO = userRepository.login(userLoginDTO.getEmail());
    if (userCoreDTO == null) {
      return null;
    }
    if (!Utility.match(userLoginDTO.getPassword(), userCoreDTO.getPassword())) {
      return null;
    }
    userCoreDTO.setPassword("***");
    return userCoreDTO;
  }

  public UserCoreDTO findByIdAndReturnUserCoreDTO(String userId) throws Exception {
    Optional<User> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      throw new Exception();
    }
    UserCoreDTO userCoreDTO = user.get().convertToUserCoreDTO();
    userCoreDTO.setPassword("***");
    return userCoreDTO;
  }

  public UserProfileDTO getUserProfile(String userId) {
    Optional<User> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      return null;
    }
    return UserMapper.convertToProfile(user.get());
  }

  public Review createHostReview(String userId, ReviewBasicDTO dto, String hostId) {
    Optional<Host> host = hostRepository.findById(hostId);
    Optional<User> creator = userRepository.findById(userId);
    Optional<BookingDetail> bookingDetail = bookingDetailRepository.findById(
        dto.getBookingDetailId());
    Review review = ReviewMapper.convertToReview(dto, creator.get(), bookingDetail.get());
    List<Review> hostReviews = host.get().getReviews();
    hostReviews.add(review);
    host.get().setReviews(hostReviews);
    Optional<Host> savedHost = hostRepository.save(host.get());
    return review;
  }

  public UserCompleteProfileDTO getUserCompleteProfile(String userId) {
    Optional<User> user = userRepository.findById(userId);
    if (user.isEmpty()) {
      return null;
    }
    return UserMapper.convertToCompleteProfile(user.get());
  }

  public Review createRenterReview(String userID, ReviewBasicDTO dto, String renterID) {
    Optional<Renter> renter = renterRepository.findById(renterID);
    Optional<User> user = userRepository.findById(userID);
    Optional<BookingDetail> bookingDetail = bookingDetailRepository.findById(
        dto.getBookingDetailId());
    Review review = ReviewMapper.convertToReview(dto, user.get(), bookingDetail.get());
    renter.get().getReviews().add(review);
    Optional<Renter> savedRenter = renterRepository.save(renter.get());
    return review;
  }

  public Review deleteRenterReview(ReviewBasicDTO dto, String reviewID, String renterID) {
    Optional<Renter> renter = renterRepository.findById(renterID);
    Optional<Review> updatedReview = reviewRepository.update(reviewID);
    return updatedReview.get();
  }

}
