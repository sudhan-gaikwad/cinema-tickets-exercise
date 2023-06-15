package uk.gov.dwp.uc.pairtest;

import java.util.stream.Stream;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.util.TicketServiceValidator;
import uk.gov.dwp.uc.pairtest.util.Validator;

/**
 * 
 * Implementation class which responsible for validation , make a payment and
 * reserve a seats for the ticket request
 *
 */
public class TicketServiceImpl implements TicketService {

	private static final int ADULT_FARE = 20;
	private static final int CHILD_FARE = 10;
	private TicketPaymentService ticketPaymentService;
	private SeatReservationService seatReservationService;
	private Validator validator = new TicketServiceValidator();
	private int numberOfAdults;
	private int numberOfChilds;

	TicketServiceImpl(SeatReservationService seatReservationService, TicketPaymentService ticketPaymentService) {
		super();
		this.seatReservationService = seatReservationService;
		this.ticketPaymentService = ticketPaymentService;
	}

	/**
	 * Should only have private methods other than the one below. 
	 * Method to validate ticket request and make a payment and seat reservation for the ticket
	 * Purchase Request request
	 * 
	 */

	@Override
	public void purchaseTickets(TicketPurchaseRequest ticketPurchaseRequest) throws InvalidPurchaseException {
		// validate the ticket request for all business validations
		validator.validate(ticketPurchaseRequest);

		// calculates the total amount and no. of seats while ignoring the infant
		TicketRequest[] ticketRequests = ticketPurchaseRequest.getTicketTypeRequests();
		numberOfAdults = countNumberOfAdult(ticketRequests);
		numberOfChilds = countNumberOfChild(ticketRequests);

		int amountToPay = calculatePaymentAmount();
		int numberOfSeats = getSeatCountToBeReserved();

		ticketPaymentService.makePayment(ticketPurchaseRequest.getAccountId(), amountToPay);
		seatReservationService.reserveSeat(ticketPurchaseRequest.getAccountId(), numberOfSeats);
	}

	/**
	 * Method to calculate the total number of seats
	 * 
	 * @return
	 */
	private int getSeatCountToBeReserved() {
		return numberOfAdults + numberOfChilds;
	}

	/**
	 * Method to calculate the total amount based on number of seats
	 * 
	 * @return
	 */
	private int calculatePaymentAmount() {
		return ADULT_FARE * numberOfAdults + CHILD_FARE * numberOfChilds;
	}

	/**
	 * Helper method to count the no. of adults in given ticket request
	 * 
	 * @param ticketRequests
	 * @return
	 */
	private int countNumberOfAdult(TicketRequest[] ticketRequests) {
		return Stream.of(ticketRequests).filter(ticketRequest -> ticketRequest.getTicketType().equals(Type.ADULT))
				.mapToInt(TicketRequest::getNoOfTickets).sum();
	}

	/**
	 * Helper method to count the no. of child in given ticket request
	 * 
	 * @param ticketRequests
	 * @return
	 */
	private int countNumberOfChild(TicketRequest[] ticketRequests) {
		return Stream.of(ticketRequests).filter(ticketRequest -> ticketRequest.getTicketType().equals(Type.CHILD))
				.mapToInt(TicketRequest::getNoOfTickets).sum();
	}
}