package uk.gov.dwp.uc.pairtest.domain;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Should be an Immutable Object
 */
public class TicketPurchaseRequest {
	private final long accountId;
	private final TicketRequest[] ticketRequests;

	public TicketPurchaseRequest(long accountId, TicketRequest[] ticketRequests) {
		// handling the invalid account case in Constructor itself
		if (accountId < 0) {
			throw new IllegalArgumentException("Account Id can not be negative.");
		}
		this.accountId = accountId;
		// Making a deep copy of mutable ticketRequests and assigning
		TicketRequest[] tempTicketRequest = Stream.of(ticketRequests).collect(Collectors.toList())
				.toArray(TicketRequest[]::new);
		this.ticketRequests = tempTicketRequest;
	}

	public long getAccountId() {
		return accountId;
	}

	public TicketRequest[] getTicketTypeRequests() {
		// returning a clone to prevent the modification of mutable data type
		return ticketRequests.clone();
	}
}
