package uk.gov.dwp.uc.pairtest.util;

import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

/**
 * Interface for business validations
 *
 */
public interface Validator {

	String NULL_REQUEST_ERROR = "No request provided for booking.";

	String ZERO_TICKET_ERROR = "At least one TicketRequest should be provided.";

	String NO_ADULT_ERROR = "Minimum one adult is needed for valid ticket booking.";

	String MAX_TICKET_ERROR = "Maximum 20 tickets can be purchased in a single request.";

	String ADULTS_LESS_THAN_INFANTS_ERROR = "Number of infants should not be greater than number of adults.";

	void validate(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException;
}
