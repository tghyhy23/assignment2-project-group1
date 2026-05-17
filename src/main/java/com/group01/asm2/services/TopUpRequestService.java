package com.group01.asm2.services;

import com.group01.asm2.configs.DatabaseConfig;
import com.group01.asm2.constants.ActivityTarget;
import com.group01.asm2.enums.ActivityActionType;
import com.group01.asm2.enums.UserRole;
import com.group01.asm2.exceptions.AppException;
import com.group01.asm2.models.Person;
import com.group01.asm2.models.TopUpRequest;
import com.group01.asm2.repositories.TopUpRequestRepository;

import java.sql.Connection;
import java.util.List;
import java.util.Optional;

/**
 * @author Group 01
 */
public class TopUpRequestService extends BaseService {
    private static final double MIN_TOP_UP_AMOUNT = 10.0;
    private static final double MAX_TOP_UP_AMOUNT = 5000.0;

    private final TopUpRequestRepository topUpRequestRepository;
    private final ActivityLogService activityLogService;

    public TopUpRequestService() {
        this(
            new TopUpRequestRepository(),
            new ActivityLogService()
        );
    }

    public TopUpRequestService(
        TopUpRequestRepository topUpRequestRepository,
        ActivityLogService activityLogService
    ) {
        this.topUpRequestRepository = topUpRequestRepository;
        this.activityLogService = activityLogService;
    }

    public TopUpRequest createTopUpRequest(double amount) {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Validate amount
        validateAmount(amount);

        // 3. Create request and activity log in one transaction
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                Optional<TopUpRequest> existingPendingRequest =
                    topUpRequestRepository.readLatestPendingRequestByUserId(conn, currentUser.getId());

                if (existingPendingRequest.isPresent()) {
                    throw AppException.validation("You already have a pending top-up request.");
                }

                TopUpRequest topUpRequest = new TopUpRequest();
                topUpRequest.setUserId(currentUser.getId());
                topUpRequest.setAmount(amount);
                topUpRequest.setStatus(TopUpRequest.STATUS_PENDING);

                TopUpRequest createdRequest =
                    topUpRequestRepository.createTopUpRequest(conn, topUpRequest);

                activityLogService.createActivityLog(
                    conn,
                    ActivityActionType.REQUEST_TOP_UP,
                    ActivityTarget.TOP_UP_REQUEST,
                    createdRequest.getId(),
                    String.format(
                        "Requested a top-up of $%,.2f. Waiting for administrator approval.",
                        createdRequest.getAmount()
                    )
                );

                conn.commit();

                return createdRequest;

            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not submit top-up request.");
        }
    }

    public Optional<TopUpRequest> readMyLatestPendingRequest() {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Read pending request
        try (Connection conn = DatabaseConfig.getConnection()) {
            return topUpRequestRepository.readLatestPendingRequestByUserId(conn, currentUser.getId());

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not read pending top-up request.");
        }
    }

    public List<TopUpRequest> readMyTopUpRequests() {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Read own requests
        try (Connection conn = DatabaseConfig.getConnection()) {
            return topUpRequestRepository.readTopUpRequestsByUserId(conn, currentUser.getId());

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not read top-up requests.");
        }
    }

    public List<TopUpRequest> readPendingTopUpRequests() {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Only system admin can read approval queue
        if (currentUser.getRole() != UserRole.SYSTEM_ADMINISTRATOR) {
            throw AppException.authorization("Only system administrators can view pending top-up requests.");
        }

        // 3. Read pending requests
        try (Connection conn = DatabaseConfig.getConnection()) {
            return topUpRequestRepository.readPendingTopUpRequests(conn);

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not read pending top-up requests.");
        }
    }

    public void approveTopUpRequest(Integer requestId) {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Only system admin can approve
        if (currentUser.getRole() != UserRole.SYSTEM_ADMINISTRATOR) {
            throw AppException.authorization("Only system administrators can approve top-up requests.");
        }

        // 3. Validate request ID
        Integer validRequestId = validateId(requestId, "Top-up request ID");

        // 4. Approve request and record activity log
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                TopUpRequest request = topUpRequestRepository.readTopUpRequestById(conn, validRequestId)
                    .orElseThrow(() -> AppException.notFound("Top-up request not found."));

                if (!request.isPending()) {
                    throw AppException.conflict("Only pending top-up requests can be approved.");
                }

                boolean updated = topUpRequestRepository.updateTopUpRequestStatus(
                    conn,
                    validRequestId,
                    TopUpRequest.STATUS_APPROVED
                );

                if (!updated) {
                    throw AppException.database("Failed to approve top-up request.");
                }

                activityLogService.createActivityLog(
                    conn,
                    ActivityActionType.APPROVE_TOP_UP,
                    ActivityTarget.TOP_UP_REQUEST,
                    request.getId(),
                    String.format(
                        "Approved top-up request of $%,.2f for user ID %d.",
                        request.getAmount(),
                        request.getUserId()
                    )
                );

                conn.commit();

            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not approve top-up request.");
        }
    }

    public void rejectTopUpRequest(Integer requestId) {
        // 1. Check current user
        Person currentUser = getCurrentUserOrThrow();

        // 2. Only system admin can reject
        if (currentUser.getRole() != UserRole.SYSTEM_ADMINISTRATOR) {
            throw AppException.authorization("Only system administrators can reject top-up requests.");
        }

        // 3. Validate request ID
        Integer validRequestId = validateId(requestId, "Top-up request ID");

        // 4. Reject request and record activity log
        try (Connection conn = DatabaseConfig.getConnection()) {
            conn.setAutoCommit(false);

            try {
                TopUpRequest request = topUpRequestRepository.readTopUpRequestById(conn, validRequestId)
                    .orElseThrow(() -> AppException.notFound("Top-up request not found."));

                if (!request.isPending()) {
                    throw AppException.conflict("Only pending top-up requests can be rejected.");
                }

                boolean updated = topUpRequestRepository.updateTopUpRequestStatus(
                    conn,
                    validRequestId,
                    TopUpRequest.STATUS_REJECTED
                );

                if (!updated) {
                    throw AppException.database("Failed to reject top-up request.");
                }

                activityLogService.createActivityLog(
                    conn,
                    ActivityActionType.REJECT_TOP_UP,
                    ActivityTarget.TOP_UP_REQUEST,
                    request.getId(),
                    String.format(
                        "Rejected top-up request of $%,.2f for user ID %d.",
                        request.getAmount(),
                        request.getUserId()
                    )
                );

                conn.commit();

            } catch (Exception exception) {
                conn.rollback();
                throw exception;
            }

        } catch (AppException exception) {
            throw exception;
        } catch (Exception exception) {
            exception.printStackTrace();
            throw AppException.database("Could not reject top-up request.");
        }
    }

    private void validateAmount(double amount) {
        if (amount < MIN_TOP_UP_AMOUNT) {
            throw AppException.validation("Top-up amount must be at least 10.");
        }

        if (amount > MAX_TOP_UP_AMOUNT) {
            throw AppException.validation("Top-up amount cannot be greater than 5000.");
        }
    }

    private Integer validateId(Integer id, String fieldName) {
        if (id == null || id <= 0) {
            throw AppException.validation(fieldName + " must be a positive number.");
        }

        return id;
    }
}