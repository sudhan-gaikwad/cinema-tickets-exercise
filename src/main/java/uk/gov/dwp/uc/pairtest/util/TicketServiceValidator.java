package uk.gov.dwp.uc.pairtest.util;

import java.util.Objects;
import java.util.stream.Stream;

import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;

/**
 * Class responsible of doing business validations for the ticket purchase
 * request
 *
 */

public class TicketServiceValidator implements Validator {

	// Max ticket allowed as per business
	private static final int MAX_TICKETS_ALLOWED = 20;

	/**
	 * Validates the ticketPurchaseRequest based on the business rules and throw the
	 * exception if not valid
	 */
	@Override
	public void validate(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {

		if (Objects.isNull(ticketPurchaseRequest)) {
			throw new InvalidPurchaseException(NULL_REQUEST_ERROR);
		} else if (ticketPurchaseRequest.getTicketTypeRequests().length < 1) {
			throw new InvalidPurchaseException(ZERO_TICKET_ERROR);
		} else if (checkForAdultPresence(ticketPurchaseRequest)) {
			throw new InvalidPurchaseException(NO_ADULT_ERROR);
		} else if (checkTotalTicketCountInvalid(ticketPurchaseRequest)) {
			throw new InvalidPurchaseException(MAX_TICKET_ERROR);
		} else if (checkInvalidInfantCount(ticketPurchaseRequest)) {
			throw new InvalidPurchaseException(ADULTS_LESS_THAN_INFANTS_ERROR);
		}
	}

	/**
	 * Check if any single adult present or not in the given ticketPurchaseRequest
	 * 
	 * @param ticketPurchaseRequest
	 * @return
	 */
	private boolean checkForAdultPresence(TicketPurchaseRequest ticketPurchaseRequest) {

		TicketRequest[] ticketRequests = ticketPurchaseRequest.getTicketTypeRequests();

		return Stream.of(ticketRequests).map(TicketRequest::getTicketType)
				.noneMatch(ticketType -> ticketType.equals(Type.ADULT));

	}

	/**
	 * Check if total number of tickets greater than allowed limit
	 * 
	 * @param ticketPurchaseRequest
	 * @return
	 */
	private boolean checkTotalTicketCountInvalid(TicketPurchaseRequest ticketPurchaseRequest) {

		TicketRequest[] ticketRequests = ticketPurchaseRequest.getTicketTypeRequests();

		int numberOfTickets = Stream.of(ticketRequests)
				// ignore the Infant as no seat allocated to them
				.filter(ticketRequest -> !ticketRequest.getTicketType().equals(Type.INFANT))
				.mapToInt(TicketRequest::getNoOfTickets).sum();
		return (numberOfTickets > MAX_TICKETS_ALLOWED);

	}

	/**
	 * Check if total number of Infant tickets greater then adults tickets
	 * 
	 * @param ticketPurchaseRequest
	 * @return
	 */
	private boolean checkInvalidInfantCount(TicketPurchaseRequest ticketPurchaseRequest) {

		TicketRequest[] ticketRequests = ticketPurchaseRequest.getTicketTypeRequests();

		int numberOfAdults = Stream.of(ticketRequests)
				.filter(ticketRequest -> ticketRequest.getTicketType().equals(Type.ADULT))
				.mapToInt(TicketRequest::getNoOfTickets).sum();

		int numberOfInfants = Stream.of(ticketRequests)
				.filter(ticketRequest -> ticketRequest.getTicketType().equals(Type.INFANT))
				.mapToInt(TicketRequest::getNoOfTickets).sum();
		return (numberOfInfants > numberOfAdults);

	}

}
