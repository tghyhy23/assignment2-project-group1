package com.group01.asm2.services;

import com.group01.asm2.core.SessionManager;
import com.group01.asm2.dtos.UserProfileStatisticsDto;
import com.group01.asm2.dtos.UserProfileViewDto;
import com.group01.asm2.dtos.reports.BuyerBiddingHistoryReportDto;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.User;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.constants.ActivityTarget;
import com.group01.asm2.repositories.UserRepository;
import com.group01.asm2.security.Permission;
import com.group01.asm2.utils.CsvWriterUtil;

import com.group01.asm2.dtos.reports.BuyerBiddingHistoryReportDto;
import com.group01.asm2.dtos.reports.BuyerPurchaseSummaryReportDto;
import com.group01.asm2.dtos.reports.SellerActivitySummaryReportDto;
import com.group01.asm2.repositories.AuctionRepository;
import com.group01.asm2.repositories.BidRepository;
import com.group01.asm2.utils.CsvWriterUtil;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

/**
 * @author Group 01
 */

public class UserService extends BaseService {
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$",
        Pattern.CASE_INSENSITIVE
    );

    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[A-Za-z0-9_]{3,80}$"
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^[0-9+()\\-\\s]{8,30}$"
    );

    private static final BigDecimal DEFAULT_COMMISSION_RATE = new BigDecimal("0.05");
    private static final DateTimeFormatter REPORT_DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRepository userRepository;
    private final ActivityLogService activityLogService;
    private final BidRepository bidRepository;
    private final AuctionRepository auctionRepository;

    public UserService() {
        this(
            new UserRepository(),
            new ActivityLogService(),
            new BidRepository(),
            new AuctionRepository()
        );
    }

    public UserService(UserRepository userRepository) {
        this(
            userRepository,
            new ActivityLogService(),
            new BidRepository(),
            new AuctionRepository()
        );
    }

    public UserService(UserRepository userRepository, ActivityLogService activityLogService) {
        this(
            userRepository,
            activityLogService,
            new BidRepository(),
            new AuctionRepository()
        );
    }

    public UserService(
        UserRepository userRepository,
        ActivityLogService activityLogService,
        BidRepository bidRepository,
        AuctionRepository auctionRepository
    ) {
        this.userRepository = userRepository;
        this.activityLogService = activityLogService;
        this.bidRepository = bidRepository;
        this.auctionRepository = auctionRepository;
    }

    public User readUserProfile() {
        return readUserProfile(null);
    }

    public User readUserProfile(Integer userId) {
        Person currentUser = SessionManager.getCurrentUser();
        Integer targetUserId = userId;

        if (targetUserId == null) {
            currentUser = getCurrentUserOrThrow();
            targetUserId = currentUser.getId();
        }

        validateUserId(targetUserId);

        User user = userRepository.readUserProfile(targetUserId);

        if (user == null) {
            throw AppException.notFound("User profile not found.");
        }

        if (isProfileOwner(currentUser, targetUserId) || isSystemAdministrator(currentUser)) {
            return toPrivateUserProfile(user, true);
        }

        return toPublicUserProfile(user);
    }

    public UserProfileViewDto readProfilePage(Integer userId) {
        Person currentUser = SessionManager.getCurrentUser();
        Integer targetUserId = userId;

        if (targetUserId == null) {
            currentUser = getCurrentUserOrThrow();
            targetUserId = currentUser.getId();
        }

        validateUserId(targetUserId);

        User targetUser = userRepository.readUserProfile(targetUserId);

        if (targetUser == null) {
            throw AppException.notFound("User profile not found.");
        }

        boolean isOwner = isProfileOwner(currentUser, targetUserId);
        boolean isSystemAdmin = isSystemAdministrator(currentUser);
        boolean isSellerProfile = targetUser.isSeller();

        boolean canViewPrivateDetails = isOwner || isSystemAdmin;
        boolean canEditProfile = isOwner || isSystemAdmin;

        boolean canViewWallet = isOwner;
        boolean canRequestTopUp = isOwner && targetUser.isRegisteredUser();

        boolean canViewActivityLog = isOwner || isSystemAdmin;

        boolean canViewListings = isSellerProfile;

        boolean canViewSellerStatistics = isSellerProfile && (isOwner || isSystemAdmin);

        User safeUser = canViewPrivateDetails
            ? toPrivateUserProfile(targetUser, canViewWallet)
            : toPublicUserProfile(targetUser);

        return new UserProfileViewDto(
            safeUser,
            isOwner,
            isSellerProfile,
            canEditProfile,
            canViewPrivateDetails,
            canViewWallet,
            canRequestTopUp,
            canViewActivityLog,
            canViewSellerStatistics,
            canViewListings
        );
    }

    public UserProfileStatisticsDto readProfileStatistics(Integer userId) {
        UserProfileViewDto profileView = readProfilePage(userId);
        User profileUser = profileView.getUser();

        UserProfileStatisticsDto statistics =
            userRepository.readProfileStatistics(profileUser.getId());

        if (!profileView.canViewWallet()) {
            statistics.hideBalance();
        }

        if (!profileView.canViewListings()) {
            statistics.hideListingStats();
        }

        if (profileView.canViewSellerStatistics()) {
            UserProfileStatisticsDto sellerStatistics =
                userRepository.readSellerStatistics(profileUser.getId());

            sellerStatistics.setCommissionFees(
                calculateCommissionFees(sellerStatistics.getTotalRevenue())
            );

            statistics.mergeSellerStatisticsFrom(sellerStatistics);
        } else {
            statistics.clearSellerStatistics();
        }

        return statistics;
    }

    public List<User> readUsers() {
        requireCurrentUser(Permission.MANAGE_USERS);

        List<User> users = userRepository.readUsers();

        activityLogService.createActivityLog(
            ActivityActionType.READ_USER,
            ActivityTarget.USER,
            null,
            "Viewed user account list."
        );

        return users.stream()
            .map(user -> toPrivateUserProfile(user, true))
            .toList();
    }

    public User updateUserProfile(
        Integer userId,
        String fullName,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String address,
        String username
    ) {
        Person currentUser = getCurrentUserOrThrow();
        Integer targetUserId = userId != null ? userId : currentUser.getId();

        validateUserId(targetUserId);

        requireOwnershipOrPermission(
            targetUserId,
            Permission.MANAGE_USERS,
            "You can only update your own profile."
        );

        User existingUser = userRepository.readUserProfile(targetUserId);

        if (existingUser == null) {
            throw AppException.notFound("User profile not found.");
        }

        String cleanFullName = validateFullName(fullName);
        validateDateOfBirth(dateOfBirth);
        String cleanEmail = validateEmail(email);
        String cleanPhone = validatePhone(phone);
        String cleanAddress = validateAddress(address);

        String requestedUsername = isBlank(username)
            ? existingUser.getUsername()
            : username;

        String cleanUsername = validateUsername(requestedUsername);

        if (userRepository.existsByEmailExceptId(cleanEmail, targetUserId)) {
            throw AppException.conflict("Email is already used by another account.");
        }

        if (userRepository.existsByUsernameExceptId(cleanUsername, targetUserId)) {
            throw AppException.conflict("Username is already used by another account.");
        }

        existingUser.setFullName(cleanFullName);
        existingUser.setDateOfBirth(dateOfBirth);
        existingUser.setEmail(cleanEmail);
        existingUser.setPhone(cleanPhone);
        existingUser.setAddress(cleanAddress);
        existingUser.setUsername(cleanUsername);

        User updatedUser = userRepository.updateUserProfile(existingUser);

        boolean updatingOwnProfile = targetUserId.equals(currentUser.getId());

        activityLogService.createActivityLog(
            ActivityActionType.UPDATE_PROFILE,
            ActivityTarget.USER,
            updatedUser.getId(),
            updatingOwnProfile
                ? "Updated own profile information."
                : "Updated user profile: " + updatedUser.getUsername() + "."
        );

        return toPrivateUserProfile(updatedUser, true);
    }

    public User updateUserProfile(
        String fullName,
        LocalDate dateOfBirth,
        String email,
        String phone,
        String address,
        String username
    ) {
        return updateUserProfile(null, fullName, dateOfBirth, email, phone, address, username);
    }

    public int deleteUser(Integer userId) {
        requireCurrentUser(Permission.MANAGE_USERS);

        validateUserId(userId);

        User existingUser = userRepository.readUserProfile(userId);

        if (existingUser == null) {
            throw AppException.notFound("User profile not found.");
        }

        int deletedRows = userRepository.deleteUser(userId);

        if (deletedRows > 0) {
            activityLogService.createActivityLog(
                ActivityActionType.DELETE_USER,
                ActivityTarget.USER,
                userId,
                "Deleted user account: " + existingUser.getUsername() + "."
            );
        }

        return deletedRows;
    }

    public Path exportMyBiddingHistory(Path outputPath) {
        User currentUser = requireCurrentRegisteredUserProfileForExport();

        List<BuyerBiddingHistoryReportDto> rows =
            bidRepository.readBuyerBiddingHistoryReport(currentUser.getId());

        return CsvWriterUtil.writeCsv(
            outputPath,
            List.of(
                "Bid ID",
                "Auction ID",
                "Item Title",
                "Category",
                "Bid Amount",
                "Bid Date Time",
                "Auction Status",
                "Current Highest Bid",
                "Bid Result"
            ),
            rows,
            row -> List.of(
                text(row.getBidId()),
                text(row.getAuctionId()),
                text(row.getItemTitle()),
                text(row.getCategoryName()),
                money(row.getBidAmount()),
                dateTime(row.getBidDateTime()),
                text(row.getAuctionStatus()),
                money(row.getCurrentHighestBid()),
                text(row.getBidResult())
            )
        );
    }

    public Path exportMyPurchaseSummary(Path outputPath) {
//        User currentUser = requireCurrentRegisteredUserProfileForExport();
//
//        List<BuyerPurchaseSummaryReportDto> rows =
//            paymentRepository.readBuyerPurchaseSummaryReport(currentUser.getId());
//
//        return CsvWriterUtil.writeCsv(
//            outputPath,
//            List.of(
//                "Payment ID",
//                "Auction ID",
//                "Item Title",
//                "Seller Username",
//                "Final Sale Price",
//                "Commission Amount",
//                "Seller Payout",
//                "Payment Status",
//                "Payment Date Time"
//            ),
//            rows,
//            row -> List.of(
//                text(row.getPaymentId()),
//                text(row.getAuctionId()),
//                text(row.getItemTitle()),
//                text(row.getSellerUsername()),
//                money(row.getFinalSalePrice()),
//                money(row.getCommissionAmount()),
//                money(row.getSellerPayout()),
//                text(row.getPaymentStatus()),
//                dateTime(row.getPaymentDateTime())
//            )
//        );
        return null;
    }

    public Path exportMySellerActivitySummary(Path outputPath) {
        User currentUser = requireCurrentRegisteredUserProfileForExport();

        if (!currentUser.isSeller()) {
            throw AppException.validation("Only sellers can export seller activity summary.");
        }

        List<SellerActivitySummaryReportDto> rows = auctionRepository.readSellerActivitySummaryReport(currentUser.getId());

        return CsvWriterUtil.writeCsv(
            outputPath,
            List.of(
                "Item ID",
                "Auction ID",
                "Item Title",
                "Category",
                "Condition",
                "Starting Price",
                "Reserve Price",
                "Auction Status",
                "Final Sale Price",
                "Winner Username",
                "Commission Amount",
                "Seller Payout",
                "Start Date Time",
                "End Date Time"
            ),
            rows,
            row -> List.of(
                text(row.getItemId()),
                text(row.getAuctionId()),
                text(row.getItemTitle()),
                text(row.getCategoryName()),
                text(row.getCondition()),
                money(row.getStartingPrice()),
                money(row.getReservePrice()),
                text(row.getAuctionStatus()),
                money(row.getFinalSalePrice()),
                text(row.getWinnerUsername()),
                money(row.getCommissionAmount()),
                money(row.getSellerPayout()),
                dateTime(row.getStartDateTime()),
                dateTime(row.getEndDateTime())
            )
        );
    }

    private User requireCurrentRegisteredUserProfileForExport() {
        Person currentPerson = getCurrentUserOrThrow();

        if (!currentPerson.isRegisteredUser()) {
            throw AppException.validation("Only registered users can export personal reports.");
        }

        User currentUser = userRepository.readUserProfile(currentPerson.getId());

        if (currentUser == null) {
            throw AppException.notFound("User profile not found.");
        }

        return currentUser;
    }

    private String text(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String money(BigDecimal value) {
        if (value == null) {
            return "0.00";
        }

        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private String dateTime(LocalDateTime value) {
        if (value == null) {
            return "";
        }

        return value.format(REPORT_DATE_TIME_FORMATTER);
    }

    private BigDecimal calculateCommissionFees(BigDecimal totalRevenue) {
        if (totalRevenue == null || totalRevenue.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        return totalRevenue
            .multiply(DEFAULT_COMMISSION_RATE)
            .setScale(2, RoundingMode.HALF_UP);
    }

    private void validateUserId(Integer userId) {
        if (userId == null || userId <= 0) {
            throw AppException.validation("User ID is invalid.");
        }
    }

    private boolean isProfileOwner(Person currentUser, Integer targetUserId) {
        return currentUser != null
            && currentUser.getId() != null
            && targetUserId != null
            && targetUserId.equals(currentUser.getId());
    }

    private boolean isSystemAdministrator(Person currentUser) {
        return currentUser != null && currentUser.isSystemAdministrator();
    }

    private User toPublicUserProfile(User user) {
        return new User(
            user.getId(),
            user.getFullName(),
            null,
            null,
            null,
            null,
            user.getUsername(),
            null,
            user.getRole(),
            user.getCreatedAt(),
            null,
            BigDecimal.ZERO,
            user.getRating(),
            user.getCompletedSalesCount()
        );
    }

    private User toPrivateUserProfile(User user, boolean includeBalance) {
        return new User(
            user.getId(),
            user.getFullName(),
            user.getDateOfBirth(),
            user.getEmail(),
            user.getPhone(),
            user.getAddress(),
            user.getUsername(),
            null,
            user.getRole(),
            user.getCreatedAt(),
            user.getUpdatedAt(),
            includeBalance ? user.getBalance() : BigDecimal.ZERO,
            user.getRating(),
            user.getCompletedSalesCount()
        );
    }

    private String validateFullName(String fullName) {
        String cleanFullName = normalizeRequiredText(fullName, "Full name is required.");

        if (cleanFullName.length() > 120) {
            throw AppException.validation("Full name must not exceed 120 characters.");
        }

        return cleanFullName;
    }

    private void validateDateOfBirth(LocalDate dateOfBirth) {
        if (dateOfBirth != null && dateOfBirth.isAfter(LocalDate.now())) {
            throw AppException.validation("Date of birth cannot be in the future.");
        }
    }

    private String validateEmail(String email) {
        String cleanEmail = normalizeRequiredText(email, "Email is required.").toLowerCase();

        if (!EMAIL_PATTERN.matcher(cleanEmail).matches()) {
            throw AppException.validation("Email format is invalid.");
        }

        return cleanEmail;
    }

    private String validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return null;
        }

        String cleanPhone = phone.trim();

        if (!PHONE_PATTERN.matcher(cleanPhone).matches()) {
            throw AppException.validation("Phone number format is invalid.");
        }

        return cleanPhone;
    }

    private String validateAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return null;
        }

        String cleanAddress = address.trim();

        if (cleanAddress.length() > 255) {
            throw AppException.validation("Address must not exceed 255 characters.");
        }

        return cleanAddress;
    }

    private String validateUsername(String username) {
        String cleanUsername = normalizeRequiredText(username, "Username is required.");

        if (!USERNAME_PATTERN.matcher(cleanUsername).matches()) {
            throw AppException.validation(
                "Username must be 3-80 characters and contain only letters, numbers, or underscores."
            );
        }

        return cleanUsername;
    }

    private String normalizeRequiredText(String value, String errorMessage) {
        if (value == null || value.trim().isEmpty()) {
            throw AppException.validation(errorMessage);
        }

        return value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}