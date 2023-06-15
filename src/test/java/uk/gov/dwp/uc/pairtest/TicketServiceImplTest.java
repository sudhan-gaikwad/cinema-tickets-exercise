package uk.gov.dwp.uc.pairtest;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import thirdparty.paymentgateway.TicketPaymentService;
import thirdparty.seatbooking.SeatReservationService;
import uk.gov.dwp.uc.pairtest.TicketServiceImpl;
import uk.gov.dwp.uc.pairtest.domain.TicketPurchaseRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest;
import uk.gov.dwp.uc.pairtest.domain.TicketRequest.Type;
import uk.gov.dwp.uc.pairtest.exception.InvalidPurchaseException;
import uk.gov.dwp.uc.pairtest.util.Validator;

@ExtendWith(MockitoExtension.class)
public class TicketServiceImplTest {

	private TicketServiceImpl unitUnderTest;
	private static final int ADULT_FARE = 20;
	private static final int CHILD_FARE = 10;

	@Mock
	private SeatReservationService seatReservationService;

	@Mock
	TicketPaymentService ticketPaymentService;

	@BeforeEach
	public void setUp() {

		unitUnderTest = new TicketServiceImpl(seatReservationService, ticketPaymentService);

	}

	@Test
	public void when_ticket_request_set_zero_quantity_for_Adult() {

		assertThrows(IllegalArgumentException.class, () -> new TicketRequest(Type.ADULT, 0));
		assertThrows(IllegalArgumentException.class, () -> new TicketRequest(Type.CHILD, 0));
		assertThrows(IllegalArgumentException.class, () -> new TicketRequest(Type.INFANT, 0));
	}

	@Test
	public void when_account_id_is_invalid_should_thow_exception() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 1) };
		assertThrows(IllegalArgumentException.class, () -> new TicketPurchaseRequest(-2L, noOfTickets));
	}

	@Test
	public void when_purchase_ticket_for_infant_without_adult_should_throw_exception() {

		TicketRequest[] noOfTickets = { new TicketRequest(Type.INFANT, 1) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1L, noOfTickets);
		String errorMessage = assertThrows(InvalidPurchaseException.class,
				() -> unitUnderTest.purchaseTickets(ticketPurchaseRequest)).getMessage();
		assertEquals(errorMessage, Validator.NO_ADULT_ERROR);

	}

	@Test
	public void when_purchase_ticket_for_child_without_adult_should_throw_exception() {

		TicketRequest[] noOfTickets = { new TicketRequest(Type.CHILD, 1) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1L, noOfTickets);

		String errorMessage = assertThrows(InvalidPurchaseException.class,
				() -> unitUnderTest.purchaseTickets(ticketPurchaseRequest)).getMessage();

		assertEquals(errorMessage, Validator.NO_ADULT_ERROR);

	}

	@Test
	public void when_purchase_ticket_for_Infant_and_child_without_adult_should_throw_exception() {

		TicketRequest[] noOfTickets = { new TicketRequest(Type.INFANT, 1), new TicketRequest(Type.CHILD, 2) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1L, noOfTickets);

		String errorMessage = assertThrows(InvalidPurchaseException.class,
				() -> unitUnderTest.purchaseTickets(ticketPurchaseRequest)).getMessage();

		assertEquals(errorMessage, Validator.NO_ADULT_ERROR);

	}

	@Test
	public void when_purchase_more_than_20_tickets_in_singleRequest_should_throw_exception() {
		List<TicketRequest> ticketList = List.of(new TicketRequest(Type.ADULT, 21));
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1L,
				ticketList.stream().toArray(TicketRequest[]::new));

		String errorMessage = assertThrows(InvalidPurchaseException.class,
				() -> unitUnderTest.purchaseTickets(ticketPurchaseRequest)).getMessage();

		assertEquals(errorMessage, Validator.MAX_TICKET_ERROR);
	}

	@Test
	public void when_purchase_more_than_20_tickets_in_one_purchase_Order_should_throw_exception() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 15), new TicketRequest(Type.CHILD, 11),
				new TicketRequest(Type.INFANT, 2) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(5L, noOfTickets);

		String errorMessage = assertThrows(InvalidPurchaseException.class,
				() -> unitUnderTest.purchaseTickets(ticketPurchaseRequest)).getMessage();

		assertEquals(errorMessage, Validator.MAX_TICKET_ERROR);
	}

	@Test // positive case
	public void when_purchase_less_than_20_tickets_should_success() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 10), new TicketRequest(Type.CHILD, 2),
				new TicketRequest(Type.INFANT, 2) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(1L, noOfTickets);

		int totalAmount = 10 * ADULT_FARE + 2 * CHILD_FARE;

		unitUnderTest.purchaseTickets(ticketPurchaseRequest);
		verify(ticketPaymentService).makePayment(ticketPurchaseRequest.getAccountId(), totalAmount);
		verify(seatReservationService).reserveSeat(ticketPurchaseRequest.getAccountId(), 12);
	}

	@Test
	public void when_noOfInfants_greater_than_adults_should_throw_exception() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 5), new TicketRequest(Type.INFANT, 6) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(5L, noOfTickets);

		String errorMessage = assertThrows(InvalidPurchaseException.class,
				() -> unitUnderTest.purchaseTickets(ticketPurchaseRequest)).getMessage();

		assertEquals(errorMessage, Validator.ADULTS_LESS_THAN_INFANTS_ERROR);
	}

	@Test // Positive
	public void when_noOfInfants_less_than_adults_should_success() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 5), new TicketRequest(Type.INFANT, 2) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(5L, noOfTickets);

		assertDoesNotThrow(() -> unitUnderTest.purchaseTickets(ticketPurchaseRequest));

	}

	@Test // Positive
	public void when_purchase_ticket_with_Adult_and_Infant_combination_should_success() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 10), new TicketRequest(Type.INFANT, 5) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(5L, noOfTickets);

		int totalAmount = 10 * ADULT_FARE;

		unitUnderTest.purchaseTickets(ticketPurchaseRequest);
		verify(ticketPaymentService).makePayment(ticketPurchaseRequest.getAccountId(), totalAmount);
		verify(seatReservationService).reserveSeat(ticketPurchaseRequest.getAccountId(), 10);

	}

	@Test // Positive
	public void when_purchase_ticket_with_Adult_and_Child_combination_should_success() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 10), new TicketRequest(Type.CHILD, 5) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(5L, noOfTickets);

		int totalAmount = 10 * ADULT_FARE + 5 * CHILD_FARE;

		unitUnderTest.purchaseTickets(ticketPurchaseRequest);
		verify(ticketPaymentService).makePayment(ticketPurchaseRequest.getAccountId(), totalAmount);
		verify(seatReservationService).reserveSeat(ticketPurchaseRequest.getAccountId(), 15);

	}

	@Test // Positive
	public void when_purchase_ticket_with_Adult_and_Child_and_Infant_all_combination_should_success() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 10), new TicketRequest(Type.CHILD, 5),
				new TicketRequest(Type.INFANT, 5) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(5L, noOfTickets);

		int totalAmount = 10 * ADULT_FARE + 5 * CHILD_FARE;

		unitUnderTest.purchaseTickets(ticketPurchaseRequest);
		verify(ticketPaymentService).makePayment(ticketPurchaseRequest.getAccountId(), totalAmount);
		verify(seatReservationService).reserveSeat(ticketPurchaseRequest.getAccountId(), 15);

	}

	@Test // check infant do not allocate a seat
	public void when_valid_adult_and_infant_combination_totalNoOfSeats_should_be_as_per_adults_seat() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 5), new TicketRequest(Type.INFANT, 2) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(5L, noOfTickets);

		int totalAmount = 5 * ADULT_FARE;
		unitUnderTest.purchaseTickets(ticketPurchaseRequest);

		verify(seatReservationService).reserveSeat(ticketPurchaseRequest.getAccountId(), 5);

	}

	@Test // check infant should not be charged any amount
	public void when_valid_adult_and_infant_combination_totalAmount_should_be_as_per_adults_amount() {
		TicketRequest[] noOfTickets = { new TicketRequest(Type.ADULT, 10), new TicketRequest(Type.INFANT, 3) };
		TicketPurchaseRequest ticketPurchaseRequest = new TicketPurchaseRequest(5L, noOfTickets);

		int totalAmount = 10 * ADULT_FARE;
		unitUnderTest.purchaseTickets(ticketPurchaseRequest);

		verify(ticketPaymentService).makePayment(ticketPurchaseRequest.getAccountId(), totalAmount);

	}

}
